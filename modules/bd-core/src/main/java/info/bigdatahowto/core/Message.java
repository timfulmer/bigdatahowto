package info.bigdatahowto.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
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
     * Map of meta data.
     */
    private Map values= new HashMap();

    /**
     * Map of JavaScript behavior functions.
     */
    private Map<BehaviorType,Behavior> behavior= new HashMap<>();

    /**
     * Map of options used to configure message processing.
     */
    private Map<String,String> options= new HashMap<>();

    /**
     * Is authorization required before reading this message.
     */
    private Boolean secure= false;

    /**
     * An authentication string identifying this message's context owner.
     */
    private String contextOwner;

    /**
     * An authentication string identifying this message's owner.
     */
    private String messageOwner;

    public Message() {

        super();
    }

    public Message( String key){

        this(key, new HashMap(0), new HashMap<BehaviorType, Behavior>(0),
                new HashMap<String, String>(0));
    }

    public Message( String key, Map values, Map<BehaviorType,Behavior> behavior,
            Map<String, String> options) {

        this();

        this.setMessageKey( new MessageKey( key));
        this.setValues( values);
        this.setBehavior( behavior);
        this.setOptions(options);
    }

    public Message(MessageKey messageKey) {

        this();

        this.setMessageKey( messageKey);
    }

    public MessageKey getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(MessageKey messageKey) {
        this.messageKey = messageKey;
    }

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

    public Map<BehaviorType, Behavior> getBehavior() {
        return behavior;
    }

    public void setBehavior(Map<BehaviorType, Behavior> behavior) {
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

    @JsonProperty
    public Boolean getSecure() {
        return secure;
    }

    public void setSecure(Boolean secure) {
        this.secure = secure;
    }

    @JsonIgnore
    public boolean isSecure(){

        return this.getSecure()!= null && this.getSecure();
    }

    public String getContextOwner() {
        return contextOwner;
    }

    public void setContextOwner(String contextOwner) {
        this.contextOwner = contextOwner;
    }

    public String getMessageOwner() {
        return messageOwner;
    }

    public void setMessageOwner(String messageOwner) {
        this.messageOwner = messageOwner;
    }

    @Override
    public String resourceKey() {
        return String.format( "messages/%s",
                this.getMessageKey().getAggregateRootKey());
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageKey=" + messageKey +
                ", values=" + values +
                ", behavior=" + behavior +
                ", options=" + options +
                ", secure=" + secure +
                ", contextOwner='" + contextOwner + '\'' +
                ", messageOwner='" + messageOwner + '\'' +
                "} " + super.toString();
    }

    @SuppressWarnings("unchecked")
    public void mergeValues(Map values) {

        Map incoming= new HashMap( values.size());
        for( Object key: values.keySet()){

            if( this.getValues().containsKey( key)){

                if( Map.class.isAssignableFrom( values.get(
                        key).getClass()) && Map.class.isAssignableFrom(
                        this.getValues().get(key).getClass())){

                    ((Map)this.getValues().get( key)).putAll( (Map)values.get(
                            key));
                }else if( Collection.class.isAssignableFrom( values.get(
                        key).getClass()) && Collection.class.isAssignableFrom(
                        this.getValues().get( key).getClass())){

                    ((Collection)this.getValues().get( key)).addAll( (Collection)
                            values.get( key));
                }
            }else{

                incoming.put( key, values.get( key));
            }
        }
        this.getValues().putAll( incoming);
    }
}
