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
$ ls -n
total 24
drwxr-xr-x  6 501  20  204 Jan 30 22:49 bd-api
drwxr-xr-x  6 501  20  204 Jan 29 16:58 bd-core
drwxr-xr-x  6 501  20  204 Jan 30 22:53 bd-defaults
-rw-r--r--  1 501  20  465 Jan 29 11:42 build.gradle
-rw-r--r--  1 501  20  656 Jan 29 11:46 modules.iml
-rw-r--r--  1 501  20   42 Jan 29 11:45 settings.gradle

$ ls -n ./bd-api/build/libs/
total 8
-rw-r--r--  1 501  20  298 Jan 29 11:45 bd-api-1.0.jar

$ ls -n ./bd-core/build/libs/
total 16
-rw-r--r--  1 501  20  5887 Jan 29 19:23 bd-core-1.0.jar

$ ls -n ./bd-defaults/build/libs/
total 8
-rw-r--r--  1 501  20  298 Jan 29 11:45 bd-default-1.0.jar
```

Everything looks good.  Now we can import our new Gradle module into IntelliJ,
pointing to `modules/build.gradle`, and we're up and running.

##Core Interfaces

Next up we'll trace through our interaction diagram and flesh out some classes.
This was done in several passes, with a lot of details in between.  Below we'll
give test
cases written with Mockito, and a description of any gotchas found along the
way.

##Message

The first interaction on our diagram is creating a `Message` instance.  Let's
setup an empty test case:

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

import static info.bigdatahowto.core.TestUtils.fakeMessage;
import static org.apache.commons.collections.MapUtils.isEmpty;

/**
 * @author timfulmer
 */
public class MessageTest {

    @Test
    public void testMessage(){

        String value= "test-value";
        Map<String,String> behavior= new HashMap<>();
        Map<String,String> options= new HashMap<>();
        Message message= new Message(
                TestUtils.MESSAGE_KEY, "test-value", behavior, options);

        this.assertMessage(new MessageKey( TestUtils.MESSAGE_KEY), value,
                behavior, options, message);

        // tf - Test mutation while we're here.
        MessageKey messageKey= new MessageKey();
        message.setMessageKey(messageKey);
        message.setValue(value);
        behavior.put( "test-key", "test-value");
        message.setBehavior( behavior);
        message.setOptions( options);

        this.assertMessage(messageKey, value, behavior, options, message);
    }

    @Test
    public void testResourceKey(){

        Message message= fakeMessage();
        assert TestUtils.MESSAGE_RESOURCE_KEY.equals( message.resourceKey()):
                "Message.resourceKey is implemented incorrectly.";
    }

    private void assertMessage(
            MessageKey messageKey,String value, Map<String, String> behavior,
            Map<String, String> options, Message message) {

        assert message.getMessageKey()!= null && messageKey.equals(
                message.getMessageKey()):
                "Message.key is not set correctly.";
        assert value.equals( message.getValue()):
                "Message.value is not set correctly.";
        assert behavior== message.getBehavior() && behavior.equals(
                message.getBehavior()):
                "Message.behavior is not set correctly.";
        assert isEmpty(behavior)!= message.hasBehavior():
                "Message.hasBehavior is not implemented correctly.";
        assert options== message.getOptions():
                "Message.options is not set correctly.";
    }
}
```

We've also added a resourceKey interface.  This returns a key to use when
storing messages to a resource.  More on resource persistence below.

Let's run our gradle build and see if everything still works:

```
$ gradle build
...
BUILD SUCCESSFUL
```

Looking good!  You'll notice we've made Message's key it's own top-level object.
Let's take a look at this one next.

##MessageKey

MessageKey uses a convention to identify various pieces of information used to
access messages within resources, using a path syntax:

```
//resource-name/user-context/user-key
```

Here's the test case:

```
package info.bigdatahowto.core;

import org.junit.Test;

/**
 * @author timfulmer
 */
public class MessageKeyTest {

    @Test
    public void testMessageKey(){

        String key= "//resource-name/user-context/user-key";
        MessageKey messageKey= new MessageKey( key);
        assert "resource-name".equals( messageKey.getResourceName()):
                "MessageKey.resourceName not initialized correctly.";
        assert "user-context".equals( messageKey.getUserContext()):
                "MessageKey.userContext not initialized correctly.";
        assert "user-key".equals( messageKey.getUserKey()):
                "MessageKey.userKey not initialized correctly.";
        assert "user-context/user-key".equals( messageKey.getAggregateRootKey()):
                "MessageKey.aggregateRootKey not initialized correctly.";
    }
}
```

On to the next object.

##Job

Let's drill down on requirements for Job a little bit more.  We want a UUID to
identify each Job instance, and probably some metadata like creation and
modified timestamps.  In fact, these would be good to have on the Message object
as well.  Here's what a super class might look like for this:

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

        AggregateRoot aggregateRoot= new AggregateRoot() {
            @Override
            public String resourceKey() {
                return "test-key";
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

Job captures the authentication information of the user making the original
request.  This is used to authorize message access later.  Job should also
capture job state & retry information, as well as handle
transitions between job states.  We've given Job a resourceKey as well, so it
can be persisted to resources.  Let's capture this in a test case:

```
package info.bigdatahowto.core;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static info.bigdatahowto.core.TestUtils.fakeJob;
import static info.bigdatahowto.core.TestUtils.fakeMessage;

/**
 * @author timfulmer
 */
public class JobTest {

    @Test
    public void testJob(){

        Message message= fakeMessage();
        String authentication= "test-authentication";
        Job job= fakeJob( message, authentication);

        assert message.getMessageKey().equals(job.getMessageKey()):
                "Job.messageKey is not initialized correctly.";
        assert authentication.equals( job.getAuthentication()):
                "Job.authentication is not initialized correctly.";
        assert job.getTries()== 0:
                "Job.tries is not initialized correctly.";
        assert job.getState()== JobState.Created:
                "Job.jobState is not initialized correctly.";
        assert job.getStatus()== null:
                "Job.status is not initialized correctly.";

        //tf - Test mutation.
        MessageKey messageKey= new MessageKey();
        job.setMessageKey(messageKey);
        String mutatedAuthentication= "mutated-authentication";
        job.setAuthentication( mutatedAuthentication);
        Integer tries= 0;
        job.setTries( tries);
        job.setState( JobState.Complete);
        String status="test-status";
        job.setStatus( status);
        assert !message.getMessageKey().equals(job.getMessageKey())
                && messageKey.equals( job.getMessageKey()):
                "Job.messageKey is not set correctly.";
        assert !authentication.equals( job.getAuthentication())
                && mutatedAuthentication.equals( job.getAuthentication()):
                "Job.authentication is not set correctly.";
        assert tries.equals(job.getTries()):
                "Job.tries is not set correctly.";
        assert JobState.Complete.equals( job.getState()):
                "Job.state is not set correctly.";
        assert status.equals( job.getStatus()):
                "Job.status is not set correctly.";
    }

    @Test
    public void testIncrementTries(){

        Job job= fakeJob(fakeMessage());
        int tries= job.getTries();
        job.incrementTries();
        assert tries+1== job.getTries():
                "Job.incrementTries is not implemented correctly.";
    }

    @Test
    public void testToQueued() throws NoSuchMethodException {

        Message message= fakeMessage();
        Job job= fakeJob(message);

        this.tryState(JobState.Queued, job, job.getClass().getMethod( "toQueued"));
        this.tryState(JobState.Processing, job, job.getClass().getMethod( "toQueued"));
        this.tryState(JobState.Complete, job, job.getClass().getMethod( "toQueued"));
        this.tryState(JobState.Error, job, job.getClass().getMethod( "toQueued"));

        job.setState(JobState.Created);
        job.toQueued();
    }

    @Test
    public void testToProcessing() throws NoSuchMethodException {

        Message message= fakeMessage();
        Job job= fakeJob( message);

        this.tryState(JobState.Created, job, job.getClass().getMethod( "toProcessing"));
        this.tryState(JobState.Processing, job, job.getClass().getMethod( "toProcessing"));
        this.tryState(JobState.Complete, job, job.getClass().getMethod( "toProcessing"));
        this.tryState(JobState.Error, job, job.getClass().getMethod( "toProcessing"));

        int tries= job.getTries();
        job.setState(JobState.Queued);
        job.toProcessing();
        assert tries+1== job.getTries():
                "Job.toProcessing is not incrementing tries";
    }

    @Test
    public void testToComplete() throws NoSuchMethodException {

        Message message= fakeMessage();
        Job job= fakeJob( message);

        this.tryState(JobState.Created, job, job.getClass().getMethod( "toComplete"));
        this.tryState(JobState.Queued, job, job.getClass().getMethod( "toComplete"));
        this.tryState(JobState.Complete, job, job.getClass().getMethod( "toComplete"));
        this.tryState(JobState.Error, job, job.getClass().getMethod( "toComplete"));

        job.setState(JobState.Processing);
        job.toComplete();
    }

    @Test
    public void testToError() throws NoSuchMethodException {

        Message message= fakeMessage();
        Job job= fakeJob( message);

        this.tryState(JobState.Created, job, job.getClass().getMethod( "toError"));
        this.tryState(JobState.Queued, job, job.getClass().getMethod( "toError"));
        this.tryState(JobState.Complete, job, job.getClass().getMethod( "toError"));
        this.tryState(JobState.Error, job, job.getClass().getMethod( "toError"));

        job.setState(JobState.Processing);
        job.toError();
    }

    @Test
    public void testResourceKey(){

        Job job= fakeJob( fakeMessage());
        assert job.getUuid().toString().equals( job.resourceKey()):
                "Job.resourceKey is not implemented correctly.";
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

As you can see there is a lot of test around the state transitions.  Since this
is the main gate mechanism, keeping data flowing correctly through the system,
the test coverage seems warranted :)

Gradle build, check, moving on.

##Queue

We'll want to push messages into the queue, pop messages out of the queue, and
delete messages.  We also want Queue to be extensible, so we can implement
specific queuing technologies later.  Queue is responsible for persisting the
messages it's processing to resources too.  Which is a great excuse to
introduce Mockito!  Let's add a bd-core/build.gradle file containing:

```
dependencies {
    compile "com.fasterxml.jackson.core:jackson-databind:2.2.3"
    testCompile "org.mockito:mockito-core:1.9.5"
}
```

Here's a test case documenting the Queue class.  Queue is an abstract class to
capture common queuing code.  You can see we're simply
extending the abstract Queue class with an anonymous inner class for test.  This
is a pattern used below as well.

```
package info.bigdatahowto.core;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static info.bigdatahowto.core.TestUtils.fakeJob;
import static info.bigdatahowto.core.TestUtils.fakeMessage;
import static org.mockito.Mockito.*;

/**
 * @author timfulmer
 */
public class QueueTest {

    private final Set<UUID> jobs= new HashSet<>();

    private Resource resourceMock;
    private Queue queue;

    @Before
    public void before(){

        this.resourceMock= mock( Resource.class);

        this.queue= new Queue() {
            @Override
            protected void write(UUID uuid) {
                jobs.add(uuid);
            }

            @Override
            protected UUID read() {
                return popUuid();
            }

            @Override
            protected void delete(UUID uuid) {
                jobs.remove( uuid);
            }
        };
        this.queue.setResource(this.resourceMock);
    }

    @Test
    public void testQueue(){

        Message message= fakeMessage();
        String authentication= "test-authentication";
        this.queue.push( message, authentication);

        Job job= fakeJob();
        job.setUuid( popUuid());
        job.setState(JobState.Queued);
        when(this.resourceMock.get( popUuid().toString(), Job.class)
                ).thenReturn( job);
        Job result= this.queue.pop();
        assert result.getUuid().equals(job.getUuid()):
                "Queue.push is not writing job correctly.";
        assert JobState.Processing== job.getState():
                "Queue.pop is not updating job state correctly.";

        this.queue.error( job);
        assert this.jobs.size()== 0 && !this.jobs.contains( job.getUuid()):
                "Queue.error is not deleting job correctly.";
        assert JobState.Error== job.getState():
                "Queue.error is not updating job state correctly.";

        verify( this.resourceMock, times(2)).put(job);
    }

    @Test
    public void testQueue_ErrorMessage(){

        Job job= fakeJob();
        job.setState( JobState.Processing);
        this.queue.error(job, "test-message");
    }

    private final UUID popUuid(){
        return jobs.iterator().next();
    }
}
```

####Authenticator

The `Authenticator` authorizes access to a key.  We'll defer tying ourselves to an authentication platform
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

##Resource

A resource is an interface to an external system.  This could be S3, or an email
server.  A resource has a name to identify it, and operates on a key-value pair.
`Resource` is also implemented as an abstract class, using an anonymous inner
class for test:

```
package info.bigdatahowto.core;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static info.bigdatahowto.core.TestUtils.fakeMessage;

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
            public void write(String key, String value) {
                hackery.put(key,value);
            }

            @Override
            public String read(String key) {
                return hackery.get(key);
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
        this.resource.write(UUID.randomUUID().toString(),
                "{'name':'John Smith'," +
                        "'username':'jsmith'," +
                        "'password':'41b9df4a217bb3c10b1c339358111b0d'}");
        // Could be email server.
        this.resource.write("Super Urgent Subject Line",
                "Hi There, We noticed you haven't visited us in a while.  We " +
                        "hope you come back soon.  Cheers, the Team.");
    }

    @Test
    public void testStore(){

        final String testKey= "test-key";
        AggregateRoot aggregateRoot= new AggregateRoot() {
            @Override
            public String resourceKey() {
                return testKey;
            }
        };
        this.resource.put(aggregateRoot);

        String value= String.format(
                "{\"uuid\":\"%s\",\"creationDate\":%s,\"modifiedDate\":%s}",
                aggregateRoot.getUuid().toString(),
                aggregateRoot.getCreationDate().getTime(),
                aggregateRoot.getModifiedDate().getTime());
        assert this.hackery.containsKey( testKey)
                && this.hackery.containsValue( value):
                "Resource.put is not calling Resource.write correctly.";
    }

    @Test( expected = RuntimeException.class)
    public void testStore_Exception(){

        final String testKey= "test-key";
        AggregateRoot aggregateRoot= new AggregateRoot() {
            @Override
            public String resourceKey() {
                return testKey;
            }
        };
        this.resource= new Resource() {
            @Override
            public void write(String key, String value) {
                throw new RuntimeException();
            }
        };
        this.resource.put(aggregateRoot);
    }

    @Test( expected = UnsupportedOperationException.class)
    public void testRead(){

        this.resource= new Resource() {
            @Override
            public void write(String key, String value) {
                // Noop.
            }
        };
        this.resource.read("test-key");
    }

    @Test
    public void testGet(){

        Message message= fakeMessage();
        this.resource.put( message);
        Message result= this.resource.get( message.resourceKey(),
                Message.class);
        assert message.equals( result):
                "Resource.get is not calling Resource.read correctly.";
    }
}
```

##ResourceRoadie

As often happens, a new class was discovered
while building.  These have been referred to as artifacts of implementation in
the literature.  I like to call then 'Roadies' :)  This one captures the
available resources on the system, associates the correct resource for a
message, and authorizes access to messages within the resource.

```
package info.bigdatahowto.core;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static info.bigdatahowto.core.TestUtils.fakeMessage;
import static org.mockito.Mockito.*;

/**
 * @author timfulmer
 */
public class ResourceRoadieTest {

    private Authenticator authenticatorMock;
    private Resource resourceMock;
    private ResourceRoadie resourceRoadie;

    @Before
    public void before(){

        this.authenticatorMock= mock( Authenticator.class);
        this.resourceMock= mock( Resource.class);

        this.resourceRoadie= new ResourceRoadie();
        this.resourceRoadie.setAuthenticator( this.authenticatorMock);
        Map<String,Resource> resources= new HashMap<>(1);
        resources.put( TestUtils.MESSAGE_RESOURCE_KEY, this.resourceMock);
        this.resourceRoadie.setResources( resources);
    }

    @Test
    public void testResourceRoadie(){

        String authentication= "test-authentication";
        when(this.authenticatorMock.authorize( TestUtils.MESSAGE_KEY,
                authentication)).thenReturn( true);
        Message message= fakeMessage();
        when(this.resourceMock.get(TestUtils.MESSAGE_KEY, Message.class)
                ).thenReturn( message);

        Message result= this.resourceRoadie.accessMessage(
                message.getMessageKey(), authentication);

        assert result!= null && result.equals( message):
                "ResourceRoadie.accessMessage is not implemented correctly.";

        this.resourceRoadie.storeMessage(message);

        this.resourceRoadie.storeMessage( message, authentication);

        verify( this.resourceMock, times(2)).put(message);
        verify( this.authenticatorMock).authorize(
                TestUtils.MESSAGE_KEY, authentication);
    }

    @Test
    public void testUnauthorized(){

        String authentication= "test-authentication";
        when(this.authenticatorMock.authorize( TestUtils.MESSAGE_KEY,
                authentication)).thenReturn( false);

        Message message= fakeMessage();
        Message result= this.resourceRoadie.accessMessage(
                message.getMessageKey(), authentication);

        assert result== null:
                "ResourceRoadie.accessMessage is not authenticating correctly.";
    }
}
```

##Processor

Processor seems to have taken on a Controller role, coordinating between
the queue and resource implementations, handling retry and error conditions,
and parsing out processing results.

```
package info.bigdatahowto.core;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static info.bigdatahowto.core.TestUtils.fakeJob;
import static info.bigdatahowto.core.TestUtils.fakeMessage;
import static org.mockito.Mockito.*;

/**
 * @author timfulmer
 */
public class ProcessorTest {

    private final ProcessingResult processingResult=
            mock( ProcessingResult.class);

    private Queue queueMock;
    private ResourceRoadie resourceRoadieMock;
    private Processor processor;

    @Before
    public void before(){

        this.queueMock= mock( Queue.class);
        this.resourceRoadieMock= mock( ResourceRoadie.class);

        this.processor= new Processor() {
            @Override
            protected ProcessingResult process(Message message) {
                return processingResult;
            }

            @Override
            protected ProcessingResult error(Message message, int tries) {
                return processingResult;
            }
        };
        this.processor.setQueue( this.queueMock);
        this.processor.setResourceRoadie( this.resourceRoadieMock);
    }

    @Test
    public void testProcessor(){

        Job job= fakeJob();
        when(this.queueMock.pop()).thenReturn( job);

        Message message= fakeMessage();
        message.getBehavior().put("test-key", "test-value");
        when(this.resourceRoadieMock.accessMessage(job.getMessageKey(),
                job.getAuthentication())).thenReturn(message);
        when(this.processingResult.isContinueProcessing()).thenReturn( true);
        when(this.processingResult.getMessage()).thenReturn(message);
        List<Message> messages= new ArrayList<>(1);
        messages.add(message);
        when(this.processingResult.getMessages()).thenReturn(messages);

        this.processor.pullJob();

        verify(this.resourceRoadieMock).storeMessage(message);
        verify(this.resourceRoadieMock).storeMessage(message,
                job.getAuthentication());
        verify(this.queueMock).push(message, job.getAuthentication());
    }

    @Test
    public void testProcessor_NullJob(){

        this.processor.pullJob();
    }

    @Test
    public void testProcessor_NullMessage(){

        Job job= fakeJob();
        when(this.queueMock.pop()).thenReturn( job);

        when(this.resourceRoadieMock.accessMessage(job.getMessageKey(),
                job.getAuthentication())).thenReturn( null);
          this.processor.pullJob();

        verify(this.queueMock).error(eq(job), anyString());
    }

    @Test
    public void testProcessor_NoBehavior(){

        Job job= fakeJob();
        when(this.queueMock.pop()).thenReturn( job);

        Message message= fakeMessage();
        when(this.resourceRoadieMock.accessMessage(job.getMessageKey(),
                job.getAuthentication())).thenReturn(message);
        when(this.processingResult.getMessage()).thenReturn(message);

        this.processor.pullJob();
    }

    @Test
    public void testProcessor_Error(){

        Job job= fakeJob();
        when(this.queueMock.pop()).thenReturn( job);

        Message message= fakeMessage();
        message.getBehavior().put("test-key", "test-value");
        when(this.resourceRoadieMock.accessMessage(job.getMessageKey(),
                job.getAuthentication())).thenReturn(message);
        when(this.processingResult.isContinueProcessing()).thenReturn( true);
        when(this.processingResult.getMessage()).thenReturn(message);
        List<Message> messages= new ArrayList<>(1);
        messages.add(message);
        when(this.processingResult.getMessages()).thenReturn(messages);

        this.processor= new Processor() {
            @Override
            protected ProcessingResult process(Message message) {
                throw new RuntimeException();
            }

            @Override
            protected ProcessingResult error(Message message, int tries) {
                return processingResult;
            }
        };
        this.processor.setQueue( this.queueMock);
        this.processor.setResourceRoadie( this.resourceRoadieMock);
        this.processor.pullJob();

        verify(this.resourceRoadieMock).storeMessage(message);
        verify(this.resourceRoadieMock).storeMessage(message,
                job.getAuthentication());
        verify(this.queueMock).push(message, job.getAuthentication());
    }

    @Test( expected = RuntimeException.class)
    public void testProcessor_FatalError(){

        Job job= fakeJob();
        job.setTries(5);
        when(this.queueMock.pop()).thenReturn( job);

        Message message= fakeMessage();
        message.getBehavior().put("test-key", "test-value");
        when(this.resourceRoadieMock.accessMessage(job.getMessageKey(),
                job.getAuthentication())).thenReturn(message);
        when(this.processingResult.getMessage()).thenReturn(message);
        List<Message> messages= new ArrayList<>(1);
        messages.add(message);
        when(this.processingResult.getMessages()).thenReturn(messages);

        this.processor= new Processor() {
            @Override
            protected ProcessingResult process(Message message) {
                throw new RuntimeException();
            }

            @Override
            protected ProcessingResult error(Message message, int tries) {
                return null;
            }
        };
        this.processor.setQueue( this.queueMock);
        this.processor.setResourceRoadie( this.resourceRoadieMock);
        this.processor.pullJob();

        verify(this.queueMock).error(job);
    }

    @Test
    public void testProcessor_NullProcessingResult(){

        Job job= fakeJob();
        when(this.queueMock.pop()).thenReturn(job);

        Message message= fakeMessage();
        message.getBehavior().put("test-key", "test-value");
        when(this.resourceRoadieMock.accessMessage(job.getMessageKey(),
                job.getAuthentication())).thenReturn(message);

        this.processor= new Processor() {
            @Override
            protected ProcessingResult process(Message message) {
                return null;
            }

            @Override
            protected ProcessingResult error(Message message, int tries) {
                return null;
            }
        };
        this.processor.setQueue(this.queueMock);
        this.processor.setResourceRoadie( this.resourceRoadieMock);
        this.processor.pullJob();
    }

    @Test
    public void testProcessor_NotContinuing(){

        Job job= fakeJob();
        when(this.queueMock.pop()).thenReturn( job);

        Message message= fakeMessage();
        message.getBehavior().put("test-key", "test-value");
        when(this.resourceRoadieMock.accessMessage(job.getMessageKey(),
                job.getAuthentication())).thenReturn(message);
        when(this.processingResult.isContinueProcessing()).thenReturn( false);

        this.processor.pullJob();
    }

    @Test
    public void testProcessor_EmptyProcessingResult(){

        Job job= fakeJob();
        when(this.queueMock.pop()).thenReturn(job);

        Message message= fakeMessage();
        message.getBehavior().put("test-key", "test-value");
        when(this.resourceRoadieMock.accessMessage(job.getMessageKey(),
                job.getAuthentication())).thenReturn(message);
        when(this.processingResult.getMessage()).thenReturn(null);
        when(this.processingResult.getMessages()).thenReturn(null);

        this.processor.pullJob();
    }
}
```

##ProcessingResult

Processing can result in a number of outcomes.  A message may stop any
additional processing, it may modify the metadata for the message being
processed, and it may create new messages for further processing.
ProcessingResult captures this information.

```
package info.bigdatahowto.core;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static info.bigdatahowto.core.TestUtils.fakeMessage;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

/**
 * @author timfulmer
 */
public class ProcessingResultTest {

    @Test
    public void testProcessingResult(){

        ProcessingResult processingResult= new ProcessingResult();
        assert processingResult.getMessage()== null:
                "ProcessingResult.message is not initialized correctly.";
        assert isEmpty(processingResult.getMessages()):
                "ProcessingResult.messages is not initialized correctly.";
        assert processingResult.isContinueProcessing():
                "ProcessingResult.continueProcessing is not initialized " +
                        "correctly.";

        Message message= fakeMessage();
        processingResult.setMessage( message);
        List<Message> messages= new ArrayList<>( 1);
        messages.add( message);
        processingResult.setMessages( messages);
        processingResult.setContinueProcessing( false);
        assert message.equals( processingResult.getMessage()):
                "ProcessingResult.message is not initialized correctly.";
        assert messages.equals( processingResult.getMessages()):
                "ProcessingResult.messages is not initialized correctly.";
        assert !processingResult.isContinueProcessing():
                "ProcessingResult.continueProcessing is not initialized " +
                        "correctly.";
    }
}
```

Gradle build, check; first iteration, done!