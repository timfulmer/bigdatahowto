#Big Data Howto

A refreshingly technology independent view of Big Data.

In this sixth installment we wrap our bd-api implementation into a Play runtime
environment.  This lets us start interfacing with the outside world using JSON
services.  We'll finish up this iteration with a simple stress-test using Apache
Base.

##Play Runtime

I've honestly never used Scala before, and a quick google search did not turn
up any activator templates specifically for Play+Scala+RESTful JSON.  Since it's
really pretty simple, let's walk through Play's getting started steps.

First, let's run `play new runtime` to setup a new project named `runtime`.
Last step is importing to IntelliJ for editing.  With the Play 2.0 & Scala
plugins installed, the latest IntelliJ 13 build seems to have imported
everything correctly.  Hopefully it will stay in sync without running
`play idea`.

And we're ready to start building out our runtime app.

##Routes

Play docs recommend setting up the routes first, so let's do that.  Here are the
routes corresponding to the interface methods identified:

```
GET         /data/*key                         controllers.Application.getData( key:String)
POST        /data/*key                         controllers.Application.postData( key:String)

GET         /job/poll                          controllers.Application.pollJob()
GET         /job/:jobUuid                      controllers.Application.getJob( jobUuid:java.util.UUID)
```

Initially it was giving:

```
play.PlayExceptions$CompilationException: Compilation error[not found: type UUID]
```

Fully qualifying UUID as `java.util.UUID` fixed that one.  And `/job/poll` comes
before `/job/:jobUuid`, otherwise Play gets confused and tries to cast "poll" to
type UUID.  Next let's build out a simple controller, echoing the request:

```
package controllers

import play.api.mvc._
import java.util.UUID

object Application extends Controller {

  def index = Action {
    BadRequest("Operation not supported.");
  }

  def getData(key: String) = Action{
    Ok("_Received_\nkey: '"+ key)
  }

  def postData(key: String)= Action{request =>
    val body: Option[String] = request.body.asText
    body.map { text =>
      Ok("_Received_\nkey: '"+ key+
        "'\nbehavior:\n" + text)
    }.getOrElse {
      BadRequest("Expecting 'Content-Type:text/plain' request header.")
    }
  }

  def getJob(jobUuid: UUID) = Action{
    Ok("_Received_\njobUuid: '"+ jobUuid.toString+ "'")
  }

  def pollJob() = Action{
    Ok("_Received_\npollJob Request");
  }
}
```

Had to noodle on getting the body content out of the request a bit.  Turns out
we need to require a MIME type from the client to get `postData` working.
Pinging each service returns the correct results.

##Wire in bd-api

At this point we've got everything needed to wire in bd-api.  Here we take a
little departure into build tool hell.  With Gradle deploying to a local Maven
repository, SBT is configured with a local repo resolver and a dependency is
added:

```
resolvers += Resolver.mavenLocal
...
libraryDependencies+= "info.bigdatahowto" % "bd-api" % "1.0-SNAPSHOT" changing()
```

Please note `changing()` at the end of the dependency.  I've gotten the feeling
it is very important to get that right the first time.

Moving on, let's scoop out the debug info from the controller and swap in calls
to `Bd`:

```
  val bd:Bd = new Bd("target/file-resources")
...
  def getData(key: String) = Action{
    val pivot:Int= key.lastIndexOf( '/')
    val userKey= key.substring( 0, pivot)
    val metaName= key.substring( pivot+ 1)
    Ok(okString(bd.queryMetaData(userKey,metaName,"test-authentication")))
  }

  def postData(key: String)= Action{request =>
    val body: Option[String] = request.body.asText
    body.map { text =>
      Ok(okString(bd.addMessage(key,text,"persist",
        "test-authentication").toString))
    }.getOrElse {
      BadRequest("Expecting 'Content-Type:text/plain' request header.")
    }
  }

  def getJob(jobUuid: UUID) = Action{
    Ok(okString(bd.queryJob(jobUuid)))
  }

  def pollJob() = Action{
    var loopCount= 0
    for( loopCount <- 0 to 10){
      bd.processJob()
    }
    Ok("Job processing complete.")
  }
```

Pretty basic, we do a little input processing to match the RESTful parameters to
the API methods.  Hitting refresh on `/job/poll` got a little old too, so
there's some initial batching going on.  Eventually we might play around with
some cool async stuff there.

It turned out we weren't updating metadata for new messages, we were stomping
old entries on add.  A new test method was added to `BdTest` to handle the
case:

```
    @Test
    public void testDoubleTap(){

        String word= "testing";
        String key= this.makeKey( word);
        String authentication= "test-authentication";
        this.bd.addMessage( key, BEHAVIOR, BehaviorType.Persist.toString(),
                authentication);
        this.bd.addMessage( key, BEHAVIOR, BehaviorType.Persist.toString(),
                authentication);
        this.bd.processJob();
        this.bd.processJob();

        Integer count= ((Double) this.bd.queryMetaData( key, "count",
                authentication)).intValue();
        assert count.equals( 2): "Count is not incrementing.";

        // tf - Check on spawned jobs.
        for( int i= 0; i< 12; i++){

            this.bd.processJob();
        }

        count= ((Double) this.bd.queryMetaData( makeKey( "tes"),
                "count", authentication)).intValue();
        assert count.equals( 2): "Count is not incrementing.";
    }
```

Internal to bd-api, the test case above introduced additional processing on add
message.  This processing happens not only on messages added through bd-api
layer, but also on messages created as artifacts of processing.  A `Behavior`
concept was introduced to package up behavior information from both bd-api and
`Processor` implementations.

##Stress Testing

First, let's take a look at GET performance against an empty file system.  It's
a good idea to exclude `runtime/target` from your IDE paths before running these
tests.  Let's push in a few messages:

 - `ab -n 1000 -p ./test/resources/abtest.js http://localhost:9000/data/wordoink/testing`

```
              min  mean[+/-sd] median   max
Connect:        0    0   0.1      0       1
Processing:     3    4   3.1      3      31
Waiting:        1    4   3.1      3      31
Total:          3    5   3.1      4      31
```

And then we process a few:

 - `ab -n 1000 http://localhost:9000/job/poll`

```
              min  mean[+/-sd] median   max
Connect:        0    0   0.1      0       1
Processing:     2   29  23.2     26     548
Waiting:        2   29  23.2     25     548
Total:          2   29  23.2     26     548
```

And now let's spot check a couple GET queries:

 - `ab -n 1000 http://localhost:9000/data/wordoink/testing/count`

```
              min  mean[+/-sd] median   max
Connect:        0    0   0.0      0       1
Processing:     1    2   2.7      2      83
Waiting:        1    2   2.7      2      83
Total:          1    2   2.7      2      84
```

 - `ab -n 1000 http://localhost:9000/job/17957e5b-e0d9-4e9e-87be-945860224628`

```
              min  mean[+/-sd] median   max
Connect:        0    0   0.1      0       3
Processing:     1    2   0.8      1      11
Waiting:        1    2   0.8      1      11
Total:          1    2   0.9      2      13
```

And we have our baselines for initial system performance.  We're not exploring
concurrency here, since the defaults we've built up so far do not support it.
Before doing that, it might be time to drill down on authentication and
authorization.