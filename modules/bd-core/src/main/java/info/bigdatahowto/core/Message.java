package info.bigdatahowto.core;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.collections.MapUtils.isEmpty;

/**
 * Associates behavior and options with a key-value pair.
 *
 * @author timfulmer
 */
public class Message extends AggregateRoot {

    /**
     * User supplied key identifying a resource, context and key mapping to a
     * value.
     */
    private MessageKey messageKey;

    /**
     * Value, containing JSON data.
     */
    private Map values= new HashMap();

    /**
     * Map of JavaScript behavior functions.
     */
    private Map<String,String> behavior= new HashMap<>();

    /**
     * Map of options used to configure message processing.
     */
    private Map<String,String> options= new HashMap<>();

    public Message() {

        super();
    }

    public Message( String key){

        this(key, null, new HashMap<String, String>(0),
                new HashMap<String, String>(0));
    }

    public Message( String key, Map values, Map<String, String> behavior,
            Map<String, String> options) {

        this();

        this.setMessageKey( new MessageKey( key));
        this.setValues( values);
        this.setBehavior( behavior);
        this.setOptions(options);
    }

    public MessageKey getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(MessageKey messageKey) {
        this.messageKey = messageKey;
    }

    // TODO: Test me & figure out serialization.
    public String getKey(){

        return this.messageKey.getKey();
    }

    public void setKey( String key){

        this.setMessageKey( new MessageKey( key));
    }

    public Map getValues() {
        return values;
    }

    public void setValues(Map values) {
        this.values = values;
    }

    public Map<String, String> getBehavior() {
        return behavior;
    }

    public void setBehavior(Map<String, String> behavior) {
        this.behavior = behavior;
    }

    public Boolean hasBehavior() {

        return !isEmpty( this.getBehavior());
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    @Override
    public String resourceKey() {
        return this.getMessageKey().getResourceName();
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageKey=" + messageKey +
                ", values=" + values +
                ", behavior=" + behavior +
                ", options=" + options +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (behavior != null ? !behavior.equals(message.behavior) : message.behavior != null)
            return false;
        if (messageKey != null ? !messageKey.equals(message.messageKey) : message.messageKey != null)
            return false;
        if (options != null ? !options.equals(message.options) : message.options != null)
            return false;
        if (values != null ? !values.equals(message.values) : message.values != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = messageKey != null ? messageKey.hashCode() : 0;
        result = 31 * result + (values != null ? values.hashCode() : 0);
        result = 31 * result + (behavior != null ? behavior.hashCode() : 0);
        result = 31 * result + (options != null ? options.hashCode() : 0);
        return result;
    }
}
