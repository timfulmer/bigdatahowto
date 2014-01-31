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
