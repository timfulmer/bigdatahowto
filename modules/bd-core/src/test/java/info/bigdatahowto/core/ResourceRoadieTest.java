package info.bigdatahowto.core;

import org.junit.Before;
import org.junit.Test;

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

        this.resourceRoadie= new ResourceRoadie( this.authenticatorMock);
        when(this.resourceMock.getName()).thenReturn(
                TestUtils.MESSAGE_RESOURCE_KEY);
        this.resourceRoadie.addResource( this.resourceMock);
    }

    @Test
    public void testResourceRoadie(){

        String authentication= "test-authentication";
        Message message= fakeMessage();
        when(this.resourceMock.get(message)).thenReturn( message);
        when(this.authenticatorMock.authorize( message, authentication,
                BehaviorType.Persist)).thenReturn(true);
        Message result= this.resourceRoadie.accessMessage(
                message, authentication, BehaviorType.Persist);
        assert result!= null && result.equals( message):
                "ResourceRoadie.accessMessage is not implemented correctly.";

        this.resourceRoadie.updateMessage(message);

        this.resourceRoadie.updateMessage(message);

        verify(this.resourceMock, times(2)).put(message);
        verify( this.authenticatorMock).authorize(message, authentication,
                BehaviorType.Persist);
    }

    @Test( expected = IllegalAccessError.class)
    public void testUnauthorized(){

        String authentication= "test-authentication";
        Message message= fakeMessage();
        when(this.resourceMock.get(message)).thenReturn( null, message);
        when(this.authenticatorMock.authorize(message, authentication,
                BehaviorType.Persist)).thenReturn(false);

        Message result= this.resourceRoadie.accessMessage( message,
                authentication, BehaviorType.Persist);
        assert result== null:
                "ResourceRoadie.accessMessage is not implemented correctly.";
        this.resourceRoadie.accessMessage(message, authentication,
                BehaviorType.Persist);
    }

    @SuppressWarnings({"unchecked", "EqualsBetweenInconvertibleTypes"})
    @Test
    public void testStoreMessage(){

        String authentication= "test-authentication";
        Message persistent= fakeMessage();
        when(this.resourceMock.get(any(Message.class))).thenReturn( persistent);
        when(this.authenticatorMock.authorize( persistent, authentication,
                BehaviorType.Persist)).thenReturn(true);

        Behavior behavior= new Behavior( BehaviorType.Persist, "test-behavior");
        Message message= fakeMessage();
        message.getValues().put( "testing", "testing");
        message.setSecure( true);
        Message result= this.resourceRoadie.storeMessage( message, behavior,
                authentication);
        assert result!= null:
                "ResourceRoadie.storeMessage is implemented incorrectly.";
        assert result.equals( persistent):
                "ResourceRoadie.storeMessage is implemented incorrectly.";
        assert !result.getValues().isEmpty():
                "ResourceRoadie.storeMessage is implemented incorrectly.";
        assert result.getSecure().equals( persistent.getSecure()):
                "ResourceRoadie.storeMessage is implemented incorrectly.";
        assert result.getBehavior()!= null:
                "ResourceRoadie.storeMessage is implemented incorrectly.";
        assert result.getBehavior().containsKey( BehaviorType.Persist):
                "ResourceRoadie.storeMessage is implemented incorrectly.";
        assert result.getBehavior().get( BehaviorType.Persist).equals( behavior):
                "ResourceRoadie.storeMessage is implemented incorrectly.";

        verify( this.resourceMock).put(persistent);
    }

    @SuppressWarnings({"unchecked", "EqualsBetweenInconvertibleTypes"})
    @Test
    public void testStoreMessage_New(){

        String authentication= "test-authentication";
        when(this.resourceMock.get(any(Message.class))).thenReturn( null);

        Behavior behavior= new Behavior( BehaviorType.Persist, "test-behavior");
        Message message= fakeMessage();
        message.getValues().put( "testing", "testing");
        message.setSecure( true);
        Message result= this.resourceRoadie.storeMessage( message, behavior,
                authentication);
        assert result!= null:
                "ResourceRoadie.storeMessage is implemented incorrectly.";
        assert result.equals( message):
                "ResourceRoadie.storeMessage is implemented incorrectly.";
        assert !result.getValues().isEmpty():
                "ResourceRoadie.storeMessage is implemented incorrectly.";
        assert result.getSecure().equals( message.getSecure()):
                "ResourceRoadie.storeMessage is implemented incorrectly.";
        assert result.getBehavior()!= null:
                "ResourceRoadie.storeMessage is implemented incorrectly.";
        assert result.getBehavior().containsKey( BehaviorType.Persist):
                "ResourceRoadie.storeMessage is implemented incorrectly.";
        assert result.getBehavior().get( BehaviorType.Persist).equals( behavior):
                "ResourceRoadie.storeMessage is implemented incorrectly.";

        verify( this.authenticatorMock).provision( message, authentication);
        verify(this.resourceMock).put( message);
    }
    
@Test
    public void testDeleteMessage(){

        Message message= fakeMessage();
        this.resourceRoadie.deleteMessage( message);

        verify( this.resourceMock).delete( message);
    }
}
