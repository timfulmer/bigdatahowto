package info.bigdatahowto.core;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author timfulmer
 */
public class UserRoadieTest {

    private static final String AUTHENTICATION= "test@bigdatahowto.info";

    private Resource resourceMock;
    private UserRoadie userRoadie;

    @Before
    public void before(){

        this.resourceMock= mock( Resource.class);
        this.userRoadie= new UserRoadie( resourceMock);
    }

    @Test
    public void testUserRoadie(){

        User user= new User();
        user.setAuthentication( AUTHENTICATION);

        // tf - User exists case.
        when( this.resourceMock.get( any( User.class))).thenReturn( user);
        User result= this.userRoadie.getUser( AUTHENTICATION);
        assert result!= null:
                "UserRoadie.getUser is not implemented correctly.";
        assert user.equals( result):
                "UserRoadie.getUser is not implemented correctly.";
        assert user.getAuthentication().equals( result.getAuthentication()):
                "UserRoadie.getUser is not implemented correctly.";
        assert !user.isRegistered():
                "UserRoadie.getUser is not implemented correctly.";

        // tf - User does not exist case.
        when( this.resourceMock.get( any( User.class))).thenReturn( null);
        result= this.userRoadie.getUser( AUTHENTICATION);
        verify( this.resourceMock).put( any( User.class));
        assert result!= null:
                "UserRoadie.getUser is not implemented correctly.";
        assert !user.equals( result):
                "UserRoadie.getUser is not implemented correctly.";
        assert user.getAuthentication().equals( result.getAuthentication()):
                "UserRoadie.getUser is not implemented correctly.";
        assert !user.isRegistered():
                "UserRoadie.getUser is not implemented correctly.";

        UserContext userContext= new UserContext();
        userContext.setUserContext( "test-userContext");
        when( this.resourceMock.get( any( AggregateRoot.class))).thenReturn(null, user);
        this.userRoadie.register(AUTHENTICATION, userContext.getUserContext());
        verify(this.resourceMock, times( 3)).put( any(AggregateRoot.class));
        assert user.isRegistered():
                "UserRoadie.register is not implemented correctly.";
    }

    @Test( expected = AssertionError.class)
    public void testContextExists(){

        User user= new User();
        user.setAuthentication( AUTHENTICATION);

        UserContext userContext= new UserContext();
        userContext.setUserContext( "test-userContext");
        userContext.setAuthentication( "dummy-authentication");
        when( this.resourceMock.get( any( UserContext.class))).thenReturn(
                userContext);
        this.userRoadie.register(AUTHENTICATION, userContext.getUserContext());
    }

    @Test
    public void testContextExists_Auth(){

        User user= new User();
        user.setAuthentication( AUTHENTICATION);

        UserContext userContext= new UserContext();
        userContext.setUserContext( "test-userContext");
        userContext.setAuthentication(AUTHENTICATION);
        when( this.resourceMock.get( any( UserContext.class))).thenReturn(
                userContext, user);
        this.userRoadie.register(AUTHENTICATION, userContext.getUserContext());
        verify( this.resourceMock).put(user);
    }

    @Test
    public void testAccessUserContext(){

        String userContextString= "test-userContext";
        UserContext userContext= new UserContext();
        when( this.resourceMock.get( any( UserContext.class))).thenReturn(
                userContext);

        UserContext result= this.userRoadie.accessUserContext(
                userContextString);
        assert result!= null:
                "UserContextRoadie.accessUserContext is not implemented correctly.";
    }

    @Test( expected = AssertionError.class)
    public void testAccessUserContext_NoContext(){

        String userContextString= "test-userContext";
        when( this.resourceMock.get( any( UserContext.class))).thenReturn(
                null);

        this.userRoadie.accessUserContext( userContextString);
    }
}
