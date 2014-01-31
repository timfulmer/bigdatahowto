package info.bigdatahowto.core;

/**
 * TODO: Simplify.
 *
 * @author timfulmer
 */
public class MessageKey {

    private String resourceName;
    private String context;
    private String userKey;
    private String resourceKey;
    private String key;

    public MessageKey() {

        super();
    }

    public MessageKey( String key) {

        this();

        // TODO: Build up defaults.
//        if( !key.startsWith( "/")){
//
//            key= new StringBuilder(key.length()+ 1).append(
//                    '/').append( key).toString();
//        }

        this.setKey( key);
        int firstSeparator= key.indexOf("/", 2);
        this.setResourceName( key.substring( 2, firstSeparator));
        int secondSeparator= key.indexOf("/", firstSeparator + 1);
        this.setContext( key.substring( firstSeparator+ 1,
                secondSeparator));
        this.setUserKey( key.substring( secondSeparator+ 1));
        this.setResourceKey( key.substring( firstSeparator+ 1));
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
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

        if (context != null ? !context.equals(that.context) : that.context != null)
            return false;
        if (key != null ? !key.equals(that.key) : that.key != null)
            return false;
        if (resourceKey != null ? !resourceKey.equals(that.resourceKey) : that.resourceKey != null)
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
        result = 31 * result + (context != null ? context.hashCode() : 0);
        result = 31 * result + (userKey != null ? userKey.hashCode() : 0);
        result = 31 * result + (resourceKey != null ? resourceKey.hashCode() : 0);
        result = 31 * result + (key != null ? key.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MessageKey{" +
                "resourceName='" + resourceName + '\'' +
                ", context='" + context + '\'' +
                ", userKey='" + userKey + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
}
