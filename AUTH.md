#Big Data Howto

A refreshingly technology independent view of Big Data.

In this seventh installment we sort out exactly how authentication and
authorization will work in our Big Data system.  First a simple use case,
followed by analysis, design and implementation.

We're trying to keep
authorization completely orthogonal to
the message key scheme, while presenting a domain specific authorization model.

User registration and login work flows are stubbed out for this iteration.

##Auth Use Case

An unauthenticated user visits a site.  Site loads information from `Message`
meta data and displays to user.  User adds a few public messages, possibly in
comments and ratings.

System creates an unverified
user, based on a mandatory email address field, when new messages are created.
The only way for a user to access, modify or delete this information is if they
register by confirming their email address.

User adds a private message, as a payment record.  User registers to modify a
private message, to request a refund.  Registered user can modify ratings and
comments.

##Auth Reqs

Permissions identified above:

 - Access;

 - Create;

 - Modify;

 - Delete.

And auth concepts are:

 - Context ownership;

 - Message ownership;

 - Public/private messages.

All messages must live in a context.  A context is owned by one and only one
registered user.  All users may add new messages to all contexts.  All users may
access, modify and delete public messages within a context.  Only the context
owner and message owner may see a private message.  Only a context owner may
modify or delete a private message.

##Authentication & Authorization

Authentication is done by email address.  An email address authentication token
is required for every Create, Modify and Delete request.  When a new email
address is used for authentication, an unregistered account is automatically
created with that email address.  Unregistered accounts gain Create privileges
immediately.  Upon registering through email address verification & password
creation, the user gains Modify & Delete permissions on their messages only.

When an existing email address is used the system first checks if the user is
registered or not.  If the user is registered, the system grants Create, Modify
or Delete privileges.

Context owners may modify or delete any messages within their context.

##Detailed Design

Authenticating should be a simple document lookup.  When a private message is
created, two documents are also created.  One for the owner of the context and
one for the owner of the message.  The following steps are performed to
authorize message actions:

 - When a messages is accessed, it's checked for private access;

 - If it has public access, Access permissions are granted;

 - If it has private access, an owner record for current authentication is
 checked;

 - If the owner exists, Access permissions are granted;

 - If the owner exists and is registered, Modify & Delete permissions are
 granted.

The trick is to not require too many document lookups while getting the job
done.  We'll get a chance to see how auth plays out in performance benchmarking
at the end of this chapter.

##User Records

We've identified a new concept on the system, a User.  Let's stub out a test
case documenting User requirements so far, in the bd-core module:

```
package info.bigdatahowto.core;

import org.junit.Test;

/**
 * @author timfulmer
 */
public class UserTest {

    @Test
    public void testUser(){

        User user= new User();
        assert user.getAuthentication()== null:
                "User.authentication is not initialized correctly.";
        assert !user.isRegistered():
                "User.registered is not initialized correctly.";

        String authentication= "test@bigdatahowto.info";
        user.setAuthentication( authentication);
        assert authentication.equals( user.getAuthentication()):
                "User.authentication is not set correctly.";

        user.register();
        assert user.isRegistered():
                "User.register is not implemented correctly.";
    }
}
```

The `User` definition can be seen in GitHub.  A few snippets to highlight some
important details.  First we make `User` an `AggregateRoot` so we can store it
in `Resource` implementations:

```
public class User extends AggregateRoot
```

Then we implement `AggregateRoot.resourceKey` according to Users's natural
primary key, and give it a prefix to keep things nicely organized in our default
file resource:

```
    @Override
    public String resourceKey() {
        return String.format("users/%s", this.getAuthentication());
    }
```

The other `resourceKey` implementations were also updated to give prefixes,
things were starting to get a little confusing on the file system.  This also
exposed an issue with the `Resource.put` interface, which was tightened down to
accept `AggregateRoot` instances.  This way we can always have a regular way of
generating keys.

The only
other interesting thing here is ignoring the `isRegistered` method when
serializing into JSON for storage in `Resource` implementations:

```
    @JsonIgnore
    public boolean isRegistered(){

        return this.getRegistered()!= null && this.getRegistered();
    }
```

We'll also need a `UserRoadie` to tie together a resource for users and a
facade:

```
package info.bigdatahowto.core;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author timfulmer
 */
public class UserRoadieTest {

    @Test
    public void testUserRoadie(){

        Resource resourceMock= mock( Resource.class);
        String authentication= "test@bigdatahowto.info";
        UserRoadie userRoadie= new UserRoadie( resourceMock);
        User user= new User();
        user.setAuthentication( authentication);

        // tf - User exists case.
        when( resourceMock.get( user.resourceKey(), User.class)).thenReturn(
                user);
        User result= userRoadie.getUser( authentication);
        assert result!= null:
                "UserRoadie.getUser is not implemented correctly.";
        assert user.equals( result):
                "UserRoadie.getUser is not implemented correctly.";
        assert user.getAuthentication().equals( result.getAuthentication()):
                "UserRoadie.getUser is not implemented correctly.";
        assert !user.isRegistered():
                "UserRoadie.getUser is not implemented correctly.";

        // tf - User does not exist case.
        when( resourceMock.get( user.resourceKey(), User.class)).thenReturn(
                null);
        result= userRoadie.getUser( authentication);
        assert result!= null:
                "UserRoadie.getUser is not implemented correctly.";
        assert !user.equals( result):
                "UserRoadie.getUser is not implemented correctly.";
        assert user.getAuthentication().equals( result.getAuthentication()):
                "UserRoadie.getUser is not implemented correctly.";
        assert !user.isRegistered():
                "UserRoadie.getUser is not implemented correctly.";

        verify( resourceMock).put( result);

        when( resourceMock.get( user.resourceKey(), User.class)).thenReturn(
                user);
        userRoadie.register(authentication);
        verify( resourceMock).put( user);
        assert user.isRegistered():
                "UserRoadie.register is not implemented correctly.";
    }
}
```

Next we wire `UserRoadie` into bd-api, configure it to use the same
`FileResource` as the rest of the system and expose `UserRoadie.register`
as a method on `Bd`.

```
    private UserRoadie userRoadie;
...
        this.userRoadie= new UserRoadie( resource);
...
    public void register( String authentication){

        this.userRoadie.register( authentication);
    }
```

Very soon registration will be tied into an email request containing a strongly
encrypted key.  The strongly encrypted key allows us to confirm the user
requesting registration got the request from their email account.  This allows
us to duck the issue of multi-factor authentication by using whatever the user
in question already has setup on their email.  Since most bank accounts and
other secure systems use email access as identity of record, this covers users
of our Big Data system as securely as anyone else on the internet.

It is strongly recommended that anyone reading this setup MFA like Google's
2-Step Verification on their email accounts.

##Message Owners

Next up, let's model ownership on our messages.  Messages have two owners, a
context owner and a message owner.  Let's model these as two authentication
strings on `Message`:

```
    @Test
    public void testOwners(){

        Message message= fakeMessage();
        assert message.getContextOwner()== null:
                "Message.contextOwner is not initialized correctly.";
        assert message.getMessageOwner()== null:
                "Message.messageOwner is not initialized correctly.";
        String contextOwner= "test-contextOwner";
        String messageOwner= "test-messageOwner";
        message.setContextOwner( contextOwner);
        message.setMessageOwner( messageOwner);
        assert contextOwner.equals(message.getContextOwner()):
                "Message.contextOwner is not set correctly.";
        assert messageOwner.equals(message.getMessageOwner()):
                "Message.messageOwner is not set correctly.";
    }
```

For context ownership, it looks like we need a `Context` concept to model the
relationship between `MessageKey.userContext` and an authentication:

```
package info.bigdatahowto.core;

import org.junit.Test;

/**
 * @author timfulmer
 */
public class UserContextTest {

    @Test
    public void testUserContext(){

        UserContext userContext= new UserContext();
        assert userContext.getUserContext()== null:
                "UserContext.userContext is not initialized correctly.";
        assert userContext.getAuthentication()== null:
                "UserContext.authentication is not initialized correctly.";

        String userContextString= "test-userContext";
        String authentication= "test-authentication";
        userContext.setUserContext( userContextString);
        userContext.setAuthentication( authentication);
        assert userContextString.equals( userContext.getUserContext()):
                "UserContext.userContext is not set correctly.";
        assert authentication.equals( userContext.getAuthentication()):
                "UserContext.authentication is not set correctly.";
        assert String.format( "userContexts/%s", userContextString).equals(
                userContext.resourceKey()):
                "UserContext.resourceKey is not implemented correctly.";
    }
}
```

And now we need a way to create a context.  Let's make context creation part of
`UserRoadie.register`, with an updated `UserRoadieTest`:

```
        UserContext userContext= new UserContext();
        userContext.setUserContext( "test-userContext");
        when( resourceMock.get( userContext.resourceKey(),
                UserContext.class)).thenReturn(null);
        when( resourceMock.get( user.resourceKey(), User.class)).thenReturn(
                user);
        userRoadie.register(authentication, userContext.getUserContext());
        verify(resourceMock, times( 3)).put( any(UserContext.class));
        verify(resourceMock).put( user);
        assert user.isRegistered():
                "UserRoadie.register is not implemented correctly.";
```

And to make sure somebody is responsible for all data added to the system, we
enforce one context per user on registration as part of `UserRoadie.register`:

```
    @Test( expected = AssertionError.class)
    public void testContextExists(){

        User user= new User();
        user.setAuthentication( AUTHENTICATION);

        UserContext userContext= new UserContext();
        userContext.setUserContext( "test-userContext");
        when( this.resourceMock.get( userContext.resourceKey(),
                UserContext.class)).thenReturn( userContext);
        this.userRoadie.register(AUTHENTICATION, userContext.getUserContext());
    }
```

##Authenticator

Now that we've got the modeling setup and populating, let's implement another
default `Authenticator` instance using the new data in bd-defaults module. Let's
jump right to the implementation:

```
package info.bigdatahowto.defaults;

import info.bigdatahowto.core.*;

/**
 * Authenticates access to messages according to domain rules.
 *
 * @author timfulmer
 */
public class BdAuthenticator implements Authenticator {

    private UserRoadie userRoadie;

    public BdAuthenticator() {

        super();
    }

    public BdAuthenticator(UserRoadie userRoadie) {

        this();

        this.setUserRoadie( userRoadie);
    }

    /**
     * Authenticates access to a resource identified by a key.
     *
     * @param message        Resource to authorize.
     * @param authentication Authentication identifying a user.
     * @param privilege      Privilege to authorize.
     * @return True if the access to the resource is granted.
     */
    @Override
    public boolean authorize(Message message, String authentication,
                             BehaviorType privilege) {

        switch( privilege){
            case Get: return !message.isSecure()
                    || isOwner( authentication, message);
            case Persist:
            case Delete: User user= this.userRoadie.getUser( authentication);
                return isOwner( authentication, message) && user.isRegistered();
            default: return false;
        }
    }

    /**
     * Provisions an authentication for access to a key.  Please note this
     * method modifies message state, but does not persist it.
     *
     * @param message        Resource to authorize.
     * @param authentication Authentication identifying a user.
     */
    @Override
    public void provision(Message message, String authentication) {

        UserContext context= this.userRoadie.accessUserContext(
                message.getMessageKey().getUserContext());
        message.setContextOwner( context.getAuthentication());
        message.setMessageOwner( authentication);
    }

    public void setUserRoadie(UserRoadie userRoadie) {
        this.userRoadie = userRoadie;
    }

    private boolean isOwner( String authentication, Message message){

        return authentication.equals( message.getContextOwner())
                || authentication.equals( message.getMessageOwner());
    }
}
```

Enhance Bd a bit to allow us to set `BdAuthenticator`, and we create a new test
class `BdAuthTest` to regression with the new authenticator, and setup a new
test case:

```
    @Test
    public void testAuthUseCase(){

        // tf - Register a context owner and create some messages.
        String ownerAuthentication= "test-ownerAuthentication";
        String userContext= "test-userContext";
        this.bd.register( ownerAuthentication, userContext);

        this.bd.addMessage( makeKey( userContext, "message01"), BEHAVIOR,
                BehaviorType.Persist.toString(), ownerAuthentication);
        this.bd.addMessage( makeKey( userContext, "message02"), BEHAVIOR,
                BehaviorType.Persist.toString(), ownerAuthentication, true);

        // tf - Access messages as an unregistered user.
        String firstVisitor= "test-firstVisitorAuthentication";
        this.bd.queryMetaData( makeKey( userContext, "message01"), "count",
                firstVisitor);

        // tf - Access secured messages as an unregistered user.
        try{

            this.bd.queryMetaData( makeKey( userContext, "message02"), "count",
                    firstVisitor);
            assert false: "Unregistered user can access secure messages.";
        }catch( IllegalAccessError e){

            // Noop.
        }

        // tf - Attempt to modify and delete messages as an unregistered user.
        try{

            this.bd.addMessage( makeKey( userContext, "message01"), BEHAVIOR,
                    BehaviorType.Persist.toString(), firstVisitor);
            assert false: "Unregistered user can access secure messages.";
        }catch( IllegalAccessError e){

            // Noop.
        }
        try{

            this.bd.addMessage(makeKey(userContext, "message01"), BEHAVIOR,
                    BehaviorType.Delete.toString(), firstVisitor);
            assert false: "Unregistered user can access secure messages.";
        }catch( IllegalAccessError e){

            // Noop.
        }

        // tf - Create messages as unregistered, and attempt to modify & delete.
        this.bd.addMessage( makeKey( userContext, "message03"), BEHAVIOR,
                BehaviorType.Persist.toString(), firstVisitor);
        this.bd.queryMetaData( makeKey( userContext, "message03"), "count",
                firstVisitor);
        try{

            this.bd.addMessage( makeKey( userContext, "message03"), BEHAVIOR,
                    BehaviorType.Persist.toString(), firstVisitor);
            assert false: "Unregistered user can access secure messages.";
        }catch( IllegalAccessError e){

            // Noop.
        }
        try{

            this.bd.addMessage( makeKey( userContext, "message03"), BEHAVIOR,
                    BehaviorType.Delete.toString(), firstVisitor);
            assert false: "Unregistered user can access secure messages.";
        }catch( IllegalAccessError e){

            // Noop.
        }

        this.bd.addMessage( makeKey( userContext, "message04"), BEHAVIOR,
                BehaviorType.Persist.toString(), firstVisitor, true);
        this.bd.queryMetaData(makeKey(userContext, "message04"), "count",
                firstVisitor);
        try{

            this.bd.addMessage( makeKey( userContext, "message04"), BEHAVIOR,
                    BehaviorType.Persist.toString(), firstVisitor);
            assert false: "Unregistered user can access secure messages.";
        }catch( IllegalAccessError e){

            // Noop.
        }
        try{

            this.bd.addMessage( makeKey( userContext, "message04"), BEHAVIOR,
                    BehaviorType.Delete.toString(), firstVisitor);
            assert false: "Unregistered user can access secure messages.";
        }catch( IllegalAccessError e){

            // Noop.
        }

        // tf - Access non-secured and attempt secured messages as third user.
        String secondVisitor= "test-secondVisitorAuthentication";
        this.bd.queryMetaData( makeKey( userContext, "message03"), "count",
                secondVisitor);
        try{

            this.bd.queryMetaData(makeKey(userContext, "message04"), "count",
                    secondVisitor);
            assert false: "Unregistered user can access secure messages.";
        }catch( IllegalAccessError e){

            // Noop.
        }

        // tf - Register second user, modify & delete.
        this.bd.register( firstVisitor, null);
        this.bd.addMessage( makeKey( userContext, "message03"), BEHAVIOR,
                BehaviorType.Persist.toString(), firstVisitor);
        this.bd.addMessage( makeKey( userContext, "message03"), BEHAVIOR,
                BehaviorType.Delete.toString(), firstVisitor);
        this.bd.addMessage( makeKey( userContext, "message04"), BEHAVIOR,
                BehaviorType.Persist.toString(), firstVisitor);
        this.bd.addMessage( makeKey( userContext, "message04"), BEHAVIOR,
                BehaviorType.Delete.toString(), firstVisitor);

        // tf - context owner can see all messages created by second user.
        this.bd.queryMetaData( makeKey( userContext, "message03"), "count",
                ownerAuthentication);
        this.bd.queryMetaData( makeKey( userContext, "message04"), "count",
                ownerAuthentication);
    }
```

A little noodling and it all works.

##Delete

Before going any further, this might be a
good time to implement `BehaviorType.Delete`.  So far we've only worked with one
behavior in the main processing loop, Persist.  This ended up being hard-coded
in a couple spots, since we didn't have a better way.

After working with it, it seems like `Job` is a good place to put the behavior
we're processing for the job.  A little refactoring later and everything is
working with delete in `Processor.pullJob`:

```
        if( job.getBehaviorType()== BehaviorType.Delete){

            this.resourceRoadie.deleteMessage( message);
        }else if( processingResult.getMessage()!= null){

            // tf - Do not need an authentication here since we're updating a
            //  message already authenticated above.
            this.resourceRoadie.storeMessage( processingResult.getMessage());
        }
```

And added to `BdTest`:

```
        this.bd.addMessage( key, BEHAVIOR, BehaviorType.Delete.toString(),
                authentication);
        this.bd.processJob();
        object= this.bd.queryMetaData( key, "count", authentication);
        assert object== null:
                "BehaviorType.Delete is not implemented correctly.";
```

##Runtime

Now we wire our new methods into the Runtime.  So far we haven't addressed
getting an authentication into the system.  We'll use a request parameter, since
it's easy.  Here are the updated routes:

```
GET         /data/*key              controllers.Application.getData( key:String, authentication:String= null)
POST        /data/*key              controllers.Application.postData( key:String, authentication:String)

GET         /job/poll               controllers.Application.pollJob()
GET         /job/:jobUuid           controllers.Application.getJob( jobUuid:java.util.UUID, authentication:String)

PUT         /user/:authentication   controllers.Application.register( authentication:String, userContext:String= null)
PUT         /user/:userContext/:authentication   controllers.Application.register( authentication:String, userContext:String)
```

Hitting the updated URLs in browser shows everything is wired up correctly.
Let's see what impact on performance we've had.

##Benchmarking

Now that we're requiring auth, we first setup a new context.  Please note this
call will fail if the context has been registered already.  Running `play clean`
brings the system back to initial state.

 - `ab -u ./test/resources/abtest.js`
 `http://localhost:9000/user/wordoink/wordoinkOwner`

Passing in -u tells Apache Bench to use PUT, the contents of the PUT request are
ignored our controller.  We're leaving this call out of benchmarking for
a while.  It's going to
be changing a lot as we lock down registration to require access to the user's
email account.

Moving on:

 - `ab -n 1000 -p ./test/resources/abtest.js`
 `http://localhost:9000/data/wordoink/testing?authentication=wordoinkOwner`

```
              min  mean[+/-sd] median   max
Connect:        0    0   0.2      0       5
Processing:     4    5   1.5      5      22
Waiting:        4    5   1.4      5      22
Total:          4    6   1.5      5      22
```

We're seeing maybe 20% slowdown from adding auth.  Not so bad, let's check
processing next.

 - `ab -n 1000 http://localhost:9000/job/poll`

```
              min  mean[+/-sd] median   max
Connect:        0    0   0.1      0       3
Processing:    24   35  14.4     31     155
Waiting:       24   35  14.4     31     155
Total:         24   35  14.4     31     155
```

Again, about 20% slower than without auth.

 - `ab -n 1000 http://localhost:9000/data/wordoink/testing/count`

```
              min  mean[+/-sd] median   max
Connect:        0    0   0.2      0       5
Processing:     1    2   0.8      1      20
Waiting:        1    1   0.8      1      20
Total:          1    2   0.9      1      20
```

Looks like we did a pretty good job on designing our runtime models.  Because
all the authorization information is included in the `Message`, we do not incur
a performance penalty on read.

FIXME: Bad URL Used.

 - `ab -n 1000 http://localhost:9000/job/0244f045-6d9e-4f83-81c1-5c78065a3303`

```
              min  mean[+/-sd] median   max
Connect:        0    0   0.2      0       6
Processing:     1    1   1.2      1      27
Waiting:        1    1   1.2      1      27
Total:          1    1   1.4      1      29
```

We can see the same performance benefits apply to `Job` read as well.