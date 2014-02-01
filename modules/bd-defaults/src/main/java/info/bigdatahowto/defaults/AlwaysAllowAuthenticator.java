package info.bigdatahowto.defaults;

import info.bigdatahowto.core.Authenticator;

/**
 * Implements Authenticator with an always allow policy.
 *
 * @author timfulmer
 */
public class AlwaysAllowAuthenticator implements Authenticator {

    /**
     * Authenticates access to a resource identified by a key.
     *
     * @param key            Key identifying a resource.
     * @param authentication Authentication identifying a user.
     * @return True if the access to the resource is granted.
     */
    @Override
    public boolean authorize(String key, String authentication) {

        return true;
    }

    /**
     * Provisions an authentication for access to a key.
     *
     * @param key            Key identifying a resource.
     * @param authentication Authentication identifying a user.
     */
    @Override
    public void provision(String key, String authentication) {

        // Noop.
    }
}
