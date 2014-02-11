package info.bigdatahowto.core;

/**
 * Represents a user context on the system, mapping MessageKey.userContext onto
 * a context owner authentication.
 *
 * @author timfulmer
 */
public class UserContext extends AggregateRoot {

    /**
     * Identifies a user context on the system.
     */
    private String userContext;

    /**
     * Identified the authentication owning a user context.
     */
    private String authentication;

    public UserContext() {

        super();
    }

    public UserContext(String userContext) {

        this();

        this.setUserContext( userContext);
    }

    public String getUserContext() {
        return userContext;
    }

    public void setUserContext(String userContext) {
        this.userContext = userContext;
    }

    public String getAuthentication() {
        return authentication;
    }

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    @Override
    public String resourceKey() {

        return String.format( "userContexts/%s", this.getUserContext());
    }
}
