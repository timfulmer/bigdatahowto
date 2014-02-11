package info.bigdatahowto.defaults;

import info.bigdatahowto.core.Authenticator;
import info.bigdatahowto.core.BehaviorType;
import info.bigdatahowto.core.Message;

/**
 * Implements Authenticator with an always allow policy.
 *
 * @author timfulmer
 */
public class AlwaysAllowAuthenticator implements Authenticator {

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

        return true;
    }

    /**
     * Provisions an authentication for access to a key.
     *
     * @param message        Resource to authorize.
     * @param authentication Authentication identifying a user.
     */
    @Override
    public void provision(Message message, String authentication) {

        // Noop.
    }

}
