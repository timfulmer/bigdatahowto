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
        assert user.getUserContext()== null:
                "User.userContext is not initialized correctly";

        String authentication= "test@bigdatahowto.info";
        user.setAuthentication( authentication);
        assert authentication.equals( user.getAuthentication()):
                "User.authentication is not set correctly.";
        user= new User( authentication);
        assert authentication.equals( user.getAuthentication()):
                "User.authentication is not initialized correctly.";
        assert String.format( "users/%s", authentication).equals(
                user.resourceKey()):
                "User.resourceKey is not implemented correctly.";

        String userContext= "test-userContext";
        user.setUserContext( userContext);
        assert userContext.equals( user.getUserContext()):
                "User.userContext is not set correctly.";

        user.register();
        assert user.isRegistered():
                "User.register is not implemented correctly.";
        user.setRegistered( true);
        assert user.isRegistered():
                "User.register is not implemented correctly.";
        user.setRegistered( false);
        assert !user.isRegistered():
                "User.register is not implemented correctly.";
        user.setRegistered( null);
        assert !user.isRegistered():
                "User.register is not implemented correctly.";
    }
}
