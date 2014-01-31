#Big Data Howto

A refreshingly technology independent view of Big Data.

In this third installment we setup a build environment using Gradle, and
complete an iteration of the Big Data Core system.

##Build Environment

Let's start out with a Gradle multi-module Java project, with one module for
each JAR package.  IntelliJ's Gradle support presents a bit of a
chicken-egg problem when creating a new Gradle project from scratch.  To get the
project up and running, first create a directory structure to hold the
modules:

```
mkdir modules
mkdir modules/bd-core
mkdir modules/bd-default
mkdir modules/bd-api
```

Then populate `modules/build.gradle` & `modules/settings.gradle`:

####build.gradle
```
subprojects{
    apply plugin: 'java'

    sourceCompatibility = 1.7
    version = '1.0'

    jar {
        manifest {
            attributes 'Implementation-Title': 'BigDataHowto', 'Implementation-Version': version
        }
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        compile group: 'commons-collections', name: 'commons-collections', version: '3.2'
        testCompile group: 'junit', name: 'junit', version: '4.+'
    }
}
```

Don't forget to wrap the root build.gradle in `subprojects{...}`, otherwise
Gradle won't see the modules.

####settings.gradle
```
include "bd-core", "bd-default", "bd-api"
```

We are keeping the structure within `modules` flat to make things simple.

Next, setup the inter-module dependencies.  The bd-core module does not have
any dependencies yet, and does not need a `gradle.build` file yet.

####bd-default
```
dependencies {
    compile project(':bd-core')
}
```

####bd-api
```
dependencies {
    compile project(':bd-default')
}
```

Now let's run the build from command line to make sure it all works.  Please use
the latest version of Gradle:

```
$ gradle build
:bd-core:compileJava
:bd-core:processResources UP-TO-DATE
:bd-core:classes
:bd-core:jar
:bd-default:compileJava UP-TO-DATE
:bd-default:processResources UP-TO-DATE
:bd-default:classes UP-TO-DATE
:bd-default:jar UP-TO-DATE
:bd-api:compileJava UP-TO-DATE
:bd-api:processResources UP-TO-DATE
:bd-api:classes UP-TO-DATE
:bd-api:jar UP-TO-DATE
:bd-api:assemble UP-TO-DATE
:bd-api:compileTestJava UP-TO-DATE
:bd-api:processTestResources UP-TO-DATE
:bd-api:testClasses UP-TO-DATE
:bd-api:test UP-TO-DATE
:bd-api:check UP-TO-DATE
:bd-api:build UP-TO-DATE
:bd-core:assemble
:bd-core:compileTestJava UP-TO-DATE
:bd-core:processTestResources UP-TO-DATE
:bd-core:testClasses UP-TO-DATE
:bd-core:test UP-TO-DATE
:bd-core:check UP-TO-DATE
:bd-core:build
:bd-default:assemble UP-TO-DATE
:bd-default:compileTestJava UP-TO-DATE
:bd-default:processTestResources UP-TO-DATE
:bd-default:testClasses UP-TO-DATE
:bd-default:test UP-TO-DATE
:bd-default:check UP-TO-DATE
:bd-default:build UP-TO-DATE

BUILD SUCCESSFUL

Total time: 11.966 secs
```

Success!  Let's check on the build artifacts, just to make sure we know what's
going on:

```
$ ll
total 24
drwxr-xr-x   9 timprilfulmer  staff  306 Jan 29 11:46 .
drwxr-xr-x  13 timprilfulmer  staff  442 Jan 29 14:00 ..
drwxr-xr-x   3 timprilfulmer  staff  102 Jan 29 11:33 .gradle
drwxr-xr-x   6 timprilfulmer  staff  204 Jan 29 11:46 bd-api
drwxr-xr-x   5 timprilfulmer  staff  170 Jan 29 11:46 bd-core
drwxr-xr-x   6 timprilfulmer  staff  204 Jan 29 11:46 bd-default
-rw-r--r--   1 timprilfulmer  staff  465 Jan 29 11:42 build.gradle
-rw-r--r--   1 timprilfulmer  staff  656 Jan 29 11:46 modules.iml
-rw-r--r--   1 timprilfulmer  staff   42 Jan 29 11:45 settings.gradle

$ ll ./bd-core/build/libs/
total 8
drwxr-xr-x  3 timprilfulmer  staff   102 Jan 29 11:44 .
drwxr-xr-x  6 timprilfulmer  staff   204 Jan 29 13:59 ..
-rw-r--r--  1 timprilfulmer  staff  3693 Jan 29 13:59 bd-core-1.0.jar

$ ll ./bd-default/build/libs/
total 8
drwxr-xr-x  3 timprilfulmer  staff  102 Jan 29 11:45 .
drwxr-xr-x  4 timprilfulmer  staff  136 Jan 29 11:45 ..
-rw-r--r--  1 timprilfulmer  staff  298 Jan 29 11:45 bd-default-1.0.jar

$ ll ./bd-api/build/libs/
total 8
drwxr-xr-x  3 timprilfulmer  staff  102 Jan 29 11:45 .
drwxr-xr-x  4 timprilfulmer  staff  136 Jan 29 11:45 ..
-rw-r--r--  1 timprilfulmer  staff  298 Jan 29 11:45 bd-api-1.0.jar
```

Everything looks good.  Now we can import our new Gradle module into IntelliJ,
pointing to `modules/build.gradle`, and we're up and running.

##Core Interfaces

Let's trace through our interaction diagram and take it one piece at a time.

####Message

The first interaction on our diagram is creating a `Message` instance.  We'll
use TDD in this project, so let's setup a test case:

```
package info.bigdatahowto.core;

import org.junit.Test;

public class MessageTest {

    @Test
    public void testMessage(){


    }
}
```

Let's use the easiest way to create a message for right now, a constructor.  We
can introduce factory or builder patterns later if we find a need.

Here are our reqs so far for a `Message` instance:

> Message object: {key[,value][,persist][,delete][,get][,error][,options]}

We'll notice `persist`, `delete`, `get` and `error` all refer to behavior
associated with actions on the system.  And `options` is a bag of key-value
pairs.  Let's make both of these items Maps of Strings to give us some
extensibility, and add some simple asserts to keep coverage up:

```
package info.bigdatahowto.core;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author timfulmer
 */
public class MessageTest {

    @Test
    public void testMessage(){

        String key= "test-key";
        String value= "test-value";
        Map<String,String> behavior= new HashMap<>();
        Map<String,String> options= new HashMap<>();
        Message message= new Message(
                "test-key", "test-value", behavior, options);

        this.assertMessage(key, value, behavior, options, message);

        // tf - Test mutation while we're here.
        message.setKey( key);
        message.setValue( value);
        message.setBehavior( behavior);
        message.setOptions( options);

        this.assertMessage(key, value, behavior, options, message);
    }

    private void assertMessage(
            String key, String value, Map<String, String> behavior,
            Map<String, String> options, Message message) {

        assert key.equals( message.getKey()):
                "Message.key is not set correctly.";
        assert value.equals( message.getValue()):
                "Message.value is not set correctly.";
        assert behavior== message.getBehavior():
                "Message.behavior is not set correctly.";
        assert options== message.getOptions():
                "Message.options is not set correctly.";
    }
}
```

Let's run our gradle build and see if everything still works:

```
$ gradle build
...
BUILD SUCCESSFUL
```

Looking good!  On to the next object.

####Job

Let's drill down on requirements for Job a little bit more.  We want a UUID to
identify each Job instance, and probably some metadata like creation and
modified timestamps.  In fact, these would be good to have on the Message object
as well.  This is all starting to look very persistent, let's give these objects
a way to persist themselves as well.  Here's what a super class might look like
for this, using the Resource interface from below:

```
package info.bigdatahowto.core;

import org.junit.Test;

import java.util.Date;
import java.util.UUID;

/**
 * @author timfulmer
 */
public class AggregateRootTest {

    @Test
    public void testAggregateRoot(){

        final String testKey= "test-key";
        AggregateRoot aggregateRoot= new AggregateRoot() {
            @Override
            public String getResourceKey() {
                return testKey;
            }
        };

        assert aggregateRoot.getUuid()!= null:
                "Message.uuid is null.";
        assert aggregateRoot.getCreationDate()!= null:
                "Message.creationDate is null.";
        assert aggregateRoot.getModifiedDate()!= null:
                "Message.modifiedData is null.";

        // tf - Test mutation.
        UUID uuid= UUID.randomUUID();
        aggregateRoot.setUuid( uuid);
        Date creationDate= new Date();
        aggregateRoot.setCreationDate( creationDate);
        Date modifiedDate= new Date();
        aggregateRoot.setModifiedDate( modifiedDate);

        assert uuid.equals( aggregateRoot.getUuid()):
                "AggregateRoot.uuid is not set correctly.";
        assert creationDate.equals( aggregateRoot.getCreationDate()):
                "AggregateRoot.creationDate is not set correctly.";
        assert modifiedDate.equals( aggregateRoot.getModifiedDate()):
                "AggregateRoot.modifiedDate is not set correctly.";
    }
}
```

Now we make Message and Job extend AggregateRoot.


Job should also capture job state & retry information, as well as handle
transitions between job states.  Let's capture this in a test case too:


```
package info.bigdatahowto.core;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author timfulmer
 */
public class JobTest {

    @Test
    public void testJob(){

        Message message= new Message();
        Job job= new Job( message);

        assert message== job.getMessage():
                "Job.message is not set correctly.";
        assert job.getTries()== 0:
                "Job.tries is not initialized correctly.";
        assert job.getState()== JobState.Created:
                "Job.jobState is not initialized correctly.";

        //tf - Test mutation.
        job.setMessage( message);
        Integer tries= 0;
        job.setTries( tries);
        job.setState( JobState.Complete);
        assert message== job.getMessage():
                "Job.message is not set correctly.";
        assert tries.equals(job.getTries()):
                "Job.tries is not set correctly.";
        assert JobState.Complete.equals( job.getState()):
                "Job.state is not set correctly.";
    }

    @Test
    public void testToQueued() throws NoSuchMethodException {

        Message message= new Message();
        Job job= new Job( message);

        this.tryState(JobState.Queued, job, job.getClass().getMethod( "toQueued"));
        this.tryState(JobState.Processing, job, job.getClass().getMethod( "toQueued"));
        this.tryState(JobState.Complete, job, job.getClass().getMethod( "toQueued"));
        this.tryState(JobState.Error, job, job.getClass().getMethod( "toQueued"));

        job.setState(JobState.Created);
        job.toQueued();
    }

    @Test
    public void testToProcessing() throws NoSuchMethodException {

        Message message= new Message();
        Job job= new Job( message);

        this.tryState(JobState.Created, job, job.getClass().getMethod( "toProcessing"));
        this.tryState(JobState.Processing, job, job.getClass().getMethod( "toProcessing"));
        this.tryState(JobState.Complete, job, job.getClass().getMethod( "toProcessing"));
        this.tryState(JobState.Error, job, job.getClass().getMethod( "toProcessing"));

        job.setState(JobState.Queued);
        job.toProcessing();
    }

    @Test
    public void testToComplete() throws NoSuchMethodException {

        Message message= new Message();
        Job job= new Job( message);

        this.tryState(JobState.Created, job, job.getClass().getMethod( "toComplete"));
        this.tryState(JobState.Queued, job, job.getClass().getMethod( "toComplete"));
        this.tryState(JobState.Complete, job, job.getClass().getMethod( "toComplete"));
        this.tryState(JobState.Error, job, job.getClass().getMethod( "toComplete"));

        job.setState(JobState.Processing);
        job.toComplete();
    }

    @Test
    public void testToError() throws NoSuchMethodException {

        Message message= new Message();
        Job job= new Job( message);

        this.tryState(JobState.Created, job, job.getClass().getMethod( "toError"));
        this.tryState(JobState.Queued, job, job.getClass().getMethod( "toError"));
        this.tryState(JobState.Complete, job, job.getClass().getMethod( "toError"));
        this.tryState(JobState.Error, job, job.getClass().getMethod( "toError"));

        job.setState(JobState.Processing);
        job.toError();
    }

    private void tryState(JobState state, Job job, Method stateChange) {
        try{
            job.setState( state);
            stateChange.invoke(job);
            throw new RuntimeException(
                    "Job.toQueued allows from JobState.Queued.");
        } catch (InvocationTargetException | IllegalAccessException e) {
            // Noop.
        }
    }
}
```

Gradle build, check, moving on.

####Queue

We'll want to push messages into the queue, pop messages out of the queue, and
delete messages.  Let's make this an interface, since it's purely an artifact of
the messaging system.  And it's a good excuse to introduce Mockito, by adding a
bd-core/build.gradle file containing:

```
dependencies {
    compile "com.fasterxml.jackson.core:jackson-databind:2.2.3"
    testCompile "org.mockito:mockito-core:1.9.5"
}
```

Here's a test case documenting the Queue interface:

```
package info.bigdatahowto.core;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author timfulmer
 */
public class QueueTest {

    @Test
    public void testQueue(){

        Job job= new Job();
        Queue queue= mock( Queue.class);
        when(queue.pop()).thenReturn( job);

        queue.push(job);
        job= queue.pop();
        queue.delete( job.getUuid());
    }
}
```

####Resource

A resource is an interface to an external system.  This could be S3, or an email
server.  A resource has a name to identify it, and operates on a key-value pair.
`Resource` is implemented as an abstract class to capture common code:

```
package info.bigdatahowto.core;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author timfulmer
 */
public class ResourceTest {

    private final Map<String,String> hackery= new HashMap<>(1);
    private Resource resource;

    @Before
    public void before(){

        this.hackery.clear();
        this.resource= new Resource() {
            @Override
            public void put(String key, String value) {
                hackery.put(key,value);
            }
        };
    }

    @Test
    public void testResource(){

        String name= "test-name";
        this.resource.setName(name);
        assert name.equals( resource.getName()):
                "Resource.name is not set correctly.";

        // Could be s3, elasticache, firebase, etc.
        this.resource.put(UUID.randomUUID().toString(),
                "{'name':'John Smith'," +
                        "'username':'jsmith'," +
                        "'password':'41b9df4a217bb3c10b1c339358111b0d'}");
        // Could be email server.
        this.resource.put("Super Urgent Subject Line",
                "Hi There, We noticed you haven't visited us in a while.  We " +
                        "hope you come back soon.  Cheers, the Team.");
    }

    @Test
    public void testStore(){

        final String testKey= "test-key";
        AggregateRoot aggregateRoot= new AggregateRoot() {
            @Override
            public String getResourceKey() {
                return testKey;
            }
        };
        this.resource.store( aggregateRoot);

        String value= String.format(
                "{\"uuid\":\"%s\",\"creationDate\":%s,\"modifiedDate\":%s}",
                aggregateRoot.getUuid().toString(),
                aggregateRoot.getCreationDate().getTime(),
                aggregateRoot.getModifiedDate().getTime());
        assert this.hackery.containsKey( testKey)
                && this.hackery.containsValue( value):
                "Resource.store is not calling Resource.put correctly.";

    }

    @Test( expected = UnsupportedOperationException.class)
    public void testResource_Get(){

        this.resource.get( "test-key");
    }
}
```

####Authenticator

The `Authenticator` acts as a proxy for untrusted code accessing `Resource`
instances.  We'll defer tying ourselves to an authentication platform
until later, and implement this as an interface for now.


```
package info.bigdatahowto.core;

import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * @author timfulmer
 */
public class AuthenticatorTest {

    @Test
    public void testAuthenticator(){

        Authenticator authenticator= mock( Authenticator.class);

        authenticator.authorize( "test-key", "test-authorization");
    }
}
```

####Processor

```
```