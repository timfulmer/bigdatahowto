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
        when(this.authenticatorMock.authorize( TestUtils.MESSAGE_KEY,
                authentication)).thenReturn( true);
        Message message= fakeMessage();
        when(this.resourceMock.get(TestUtils.MESSAGE_USER_KEY, Message.class)
                ).thenReturn( message);

        Message result= this.resourceRoadie.accessMessage(
                message.getMessageKey(), authentication);

        assert result!= null && result.equals( message):
                "ResourceRoadie.accessMessage is not implemented correctly.";

        this.resourceRoadie.storeMessage(message);

        this.resourceRoadie.storeMessage( message);

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
