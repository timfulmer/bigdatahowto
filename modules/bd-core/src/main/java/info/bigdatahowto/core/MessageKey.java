package info.bigdatahowto.core;

/**
 * Represents key information for a message.  Keys are used to communicate with
 * external resources.
 *
 * @author timfulmer
 */
public class MessageKey {

    private static final String DEFAULT_RESOURCE_NAME= "file";

    /**
     * Identifies a resource instance.
     */
    private String resourceName;

    /**
     * Arbitrary path information, used to scope data.
     */
    private String userContext;

    /**
     * Unique key identifying a message.
     */
    private String userKey;

    /**
     * Identifies a message's location within a resource.
     */
    private String aggregateRootKey;

    /**
     * Original string representation of this key.
     */
    private String key;

    public MessageKey() {

        super();

        this.setResourceName( DEFAULT_RESOURCE_NAME);
    }

    /**
     * Parse string representation of a key:
     *
     * //resource-name/user-context/user-key
     *
     * @param key Key identifying a message stored within a resource.
     */
    public MessageKey( String key) {

        this();

        this.setKey( key);
        if( key.startsWith( "//")){

            int pivot= key.indexOf( "/", 2);
            this.setResourceName( key.substring( 2, pivot));
            key= key.substring( pivot+ 1);
        }
        if( key.startsWith( "/")){

            key= key.substring( 1);
        }
        if( !key.contains( "/")){

            throw new IllegalArgumentException(
                    String.format( "MessageKey '%s' must have at least " +
                            "<context>/<key>.", getKey()));
        }
        this.setAggregateRootKey( key);
        int pivot= key.indexOf( "/");
        this.setUserContext( key.substring( 0, pivot));
        this.setUserKey( key.substring( pivot+ 1));
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getUserContext() {
        return userContext;
    }

    public void setUserContext(String userContext) {
        this.userContext = userContext;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getAggregateRootKey() {
        return aggregateRootKey;
    }

    public void setAggregateRootKey(String aggregateRootKey) {
        this.aggregateRootKey = aggregateRootKey;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageKey that = (MessageKey) o;

        if (userContext != null ? !userContext.equals(that.userContext) : that.userContext != null)
            return false;
        if (key != null ? !key.equals(that.key) : that.key != null)
            return false;
        if (aggregateRootKey != null ? !aggregateRootKey.equals(that.aggregateRootKey) : that.aggregateRootKey != null)
            return false;
        if (resourceName != null ? !resourceName.equals(that.resourceName) : that.resourceName != null)
            return false;
        if (userKey != null ? !userKey.equals(that.userKey) : that.userKey != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = resourceName != null ? resourceName.hashCode() : 0;
        result = 31 * result + (userContext != null ? userContext.hashCode() : 0);
        result = 31 * result + (userKey != null ? userKey.hashCode() : 0);
        result = 31 * result + (aggregateRootKey != null ? aggregateRootKey.hashCode() : 0);
        result = 31 * result + (key != null ? key.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MessageKey{" +
                "resourceName='" + resourceName + '\'' +
                ", userContext='" + userContext + '\'' +
                ", userKey='" + userKey + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
}
