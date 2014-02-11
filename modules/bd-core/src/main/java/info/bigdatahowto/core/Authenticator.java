package info.bigdatahowto.core;

/**
 * Authenticates access to a message.  Answers the question:
 *
 *   Can the user who made the request operate on the requested message.
 *
 * @author timfulmer
 */
public interface Authenticator {

    /**
     * Authenticates access to a resource identified by a key.
     *
     * @param message Resource to authorize.
     * @param authentication Authentication identifying a user.
     * @param privilege Privilege to authorize.
     * @return True if the access to the resource is granted.
     */
    boolean authorize( Message message, String authentication,
                       BehaviorType privilege);

    /**
     * Provisions an authentication for access to a key.
     *
     * @param message Resource to authorize.
     * @param authentication Authentication identifying a user.
     */
    void provision( Message message, String authentication);
}
