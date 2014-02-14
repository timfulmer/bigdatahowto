#Big Data Howto

A refreshingly technology independent view of Big Data.

In this eighth installment we start to move towards a production ready
configuration.  We'll swap out the local file system causing trouble with
concurrency, check performance, and tune as needed.  Same steps applied to
swapping the in-memory queue with SQS for durability.

##S3Resource

Pretty simple stuff, here's the test case:

```
package info.bigdatahowto.defaults;

import org.junit.Before;
import org.junit.Test;

/**
 * @author timfulmer
 */
public class S3ResourceTest {

    private static final String KEY= "test-key";

    @Before
    public void before(){

        new S3Resource().clean();
    }

    @Test
    public void testS3Resource(){

        S3Resource s3Resource= new S3Resource();
        String value= "test-value";
        String read= s3Resource.read( KEY);
        assert read== null: "Read a non-existent value.";

        s3Resource.write( KEY, value);
        read= s3Resource.read( KEY);
        assert read!= null: "Could not read value from s3.";
        assert value.equals( read): "Could not read value from s3.";

        s3Resource.remove( KEY);
        read= s3Resource.read( KEY);
        assert read== null: "Read a non-existent value.";
    }

    @Test
    public void testClean(){

        new S3Resource().clean();
    }
}
```

##S3Resource Benchmarking

Again, we're leaving registration out of benchmarking for the moment.  Let's
push in a few messages.

 - `ab -n 1000 -p ./test/resources/abtest.js`
 `http://localhost:9000/data/wordoink/testing?authentication=wordoinkOwner`

```
Requests per second:    1.07 [#/sec] (mean)
Time per request:       930.709 [ms] (mean, across all concurrent requests)

-c 4
Requests per second:    4.41 [#/sec] (mean)
Time per request:       226.985 [ms] (mean, across all concurrent requests)

-c 16
Requests per second:    8.84 [#/sec] (mean)
Time per request:       113.151 [ms] (mean, across all concurrent requests)

-c 32
Requests per second:    8.75 [#/sec] (mean)
Time per request:       114.278 [ms] (mean, across all concurrent requests)
```

Yikes!  Good news is it's handling concurrency and scaling very well.  While the
individual response times are not terrible, it would be nice to avoid the
performance hit we're seeing on this service.

Architecturally the goal is to let Scala+Play handle any threading, and be able
to respond with the Job UUID before the message is actually persisted.  The
easiest way to do this may be moving UUID generation into the runtime, and pass
it into the `Bd` implementation.  This makes `Application.scala` look like this:

```
  def postData(key: String, authentication:String)= Action{request =>
    val body: Option[String] = request.body.asText
    body.map { text =>
      val jobUuid= UUID.randomUUID()
      scala.concurrent.Future { bd.addMessage(jobUuid,"//s3/"+ key,text,"Persist",authentication) }
      Ok(okString(jobUuid))
    }.getOrElse {
      BadRequest("Expecting 'Content-Type:text/plain' request header.")
    }
  }
```

Running benchmarks again shows:

```
Requests per second:    7.63 [#/sec] (mean)
Time per request:       131.117 [ms] (mean, across all concurrent requests)

-c 32
Requests per second:    9.31 [#/sec] (mean)
Time per request:       107.410 [ms] (mean, across all concurrent requests)
```

Splitting off message creation into it's own thread seems to have leveled the
differences between serial and concurrent access.  Success!

It's also worth noting the limiting performance factor here seems to be on the
AWS side.  There's really no other explanation for request rate leveling off
like it does.

 - `ab -n 1000 http://localhost:9000/job/poll`

Initially ApacheBench gives `apr_poll: The timeout specified has expired
(70007)`.  Looks like talking to s3 is adding too much time to our poll method.
Let's add in the async trick above:

```
  def pollJob() = Action{
    for( loopCount <- 0 to 10){
      scala.concurrent.Future { bd.processJob() }
    }
    Ok("Job processing complete.")
  }
```

This kicked the can down the road, but we were still starting way too many
threads in the JVM.  Things bog down, and we end up with the `apr_poll` timeout
after a hundred requests or so.  Removing the loop helped, but then we had many
many threads each hitting the same message.  This resulted in many messages
being processed without actually persisting their changes, due to stale data
coming from s3.

To address this case `Bd` is implemented as a singleton and `Queue` is updated
to be aware of the message keys it's handing out:

```
    /**
     * TODO: Use a cache for this.
     */
    private Map<String,KeyTimeout> keys= new HashMap<>();

    /**
     * Reads a job from the underlying queue; updating state information in
     * external resource.
     *
     * @return Job instance.
     */
    public Job pop(){

        Job job;
        do{
            UUID uuid= this.read();
            if( uuid== null){

                this.logger.info( "Job queue empty, returning null.");

                // tf - There are no jobs in the queue.
                return null;
            }
            job = getJob(uuid);
        }while( job.getState()!= JobState.Queued);
        if( this.alreadyProcessingKey(job.getMessageKey().getKey())){

            return null;
        }
        job.toProcessing();
        job.setStatus("Job processing in progress ...");
        this.resource.put(job);

        return job;
    }

    private synchronized boolean alreadyProcessingKey(String key){

        KeyTimeout timeout= this.keys.get(key);
        if( timeout== null || !timeout.valid()){

            this.keys.put( key, new KeyTimeout());

            return false;
        }

        return true;
    }

    private class KeyTimeout{
        private Date creation;
        private KeyTimeout() {this.creation = new Date();}
        private boolean valid(){
            Calendar timeout= GregorianCalendar.getInstance();
            timeout.add(Calendar.MINUTE, 5);
            return this.creation.before( timeout.getTime());
        }
    }
```

You'll notice this has been implemented using a non-blocking pattern.  If we
blocked by waiting until the message key was completed, or looping until we find
a message key that hasn't been checked out yet, we end up right back at
`apr_poll`.  Which is pretty typical for high concurrency: blocking == bad.

And it turns out doing our own thread handling by looping inside `def pollJob`
actually limits our message/s throughput.  No idea why, but you can see it by
kicking off a set of poll requests while watching the network io stats.  Next
add the loop in, let Play recompile and
hot deploy the loop version, and notice the network throughput to s3 drop by 50%
or so.

Back to the performanc issue, essentially what we've done is said, "It is going
to take X time to process N
messages for key K.  You can ask as many times as you want, but it will still
take that long."  Which separates the impact of how long it takes to process a
message from how many requests for message processing are happening.
And it points to how performance optimizing message processing is probably the
single biggest thing we can do to improve throughput we've found so far.

All of this gives us the following benchmark results:

```
Requests per second:    53.72 [#/sec] (mean)
Time per request:       18.615 [ms] (mean, across all concurrent requests)

-c 16 -n 10000
Requests per second:    52.91 [#/sec] (mean)
Time per request:       18.901 [ms] (mean, across all concurrent requests)
```

With that last one the network connection to s3 finally got saturated.  Not sure
why that would happen with more read intensive operations than with write
intensive.  Again, feels like something on the AWS side.

 - `ab -n 1000 http://localhost:9000/data/wordoink/testing/count`

```
Requests per second:    8.13 [#/sec] (mean)
Time per request:       123.052 [ms] (mean, across all concurrent requests)

-c 16
Requests per second:    72.58 [#/sec] (mean)
Time per request:       13.779 [ms] (mean, across all concurrent requests)
```

 - `ab -n 1000 http://localhost:9000/job/2e5b249e-f6d5-452f-9ec1-43aa50e065e1?authentication=wordoinkOwner`

```
Requests per second:    8.47 [#/sec] (mean)
Time per request:       118.105 [ms] (mean, across all concurrent requests)

-c 16
Requests per second:    68.40 [#/sec] (mean)
Time per request:       14.621 [ms] (mean, across all concurrent requests)
```

Gotta love AWS for scaling up.

##SQSQueue

Test case implementation:

```
package info.bigdatahowto.defaults.aws;

import info.bigdatahowto.core.Queue;
import info.bigdatahowto.defaults.FileResource;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

/**
 * @author timfulmer
 */
public class SqsQueueTest {

    private SqsQueue sqsQueue;

    @Before
    public void before(){

        this.sqsQueue= new SqsQueue( new FileResource());
        this.sqsQueue.clear();
    }

    @Test
    public void testQueue() throws InterruptedException {

        UUID uuid= UUID.randomUUID();
        this.sqsQueue.write( uuid);

        Queue.ResultTuple result;
        int times= 0;
        do{
            result= this.sqsQueue.read();
            times++;
            Thread.sleep( 500);
        }while( result== null && times< 5);
        assert result!= null: "Could not read message.";
        assert uuid.equals( result.uuid): "Received incorrect result.";

        this.sqsQueue.delete( result.identifier);
        times= 0;
        do{
            result=  this.sqsQueue.read();
            times++;
            Thread.sleep( 500);
        }while( result!= null && times< 5);
        assert result== null: "Could not delete.";
    }
}
```

And benchmarking:

```
Requests per second:    8.31 [#/sec] (mean)
Time per request:       120.315 [ms] (mean, across all concurrent requests)

-c 16
Requests per second:    7.90 [#/sec] (mean)
Time per request:       126.580 [ms] (mean, across all concurrent requests)

--poll
Requests per second:    16.42 [#/sec] (mean)
Time per request:       60.900 [ms] (mean, across all concurrent requests)

-c 16
Requests per second:    19.46 [#/sec] (mean)
Time per request:       51.387 [ms] (mean, across all concurrent requests)
```

And with `AmazonSQSBufferedAsyncClient`

```
Requests per second:    6.75 [#/sec] (mean)
Time per request:       148.136 [ms] (mean, across all concurrent requests)

Requests per second:    6.89 [#/sec] (mean)
Time per request:       145.151 [ms] (mean, across all concurrent requests)

-- poll
Requests per second:    10.45 [#/sec] (mean)
Time per request:       95.731 [ms] (mean, across all concurrent requests)

-c 16
Requests per second:    7.61 [#/sec] (mean)
Time per request:       131.428 [ms] (mean, across all concurrent requests)
```

Seems to be a little slower, though promises to be a little cheaper.  It's also
much harder to build an integration test using `AmazonSQSBufferedAsyncClient`,
so we're going to use the simple synchronous client for now.
