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
