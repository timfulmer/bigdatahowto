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
     * @param key Key identifying a resource.
     * @param authentication Authentication identifying a user.
     * @return True if the access to the resource is granted.
     */
    boolean authorize( String key, String authentication);

    /**
     * Provisions an authentication for access to a key.
     *
     * @param key Key identifying a resource.
     * @param authentication Authentication identifying a user.
     */
    void provision( String key, String authentication);
}
