package info.bigdatahowto.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Captures the results of processing or error handling of a message.
 *
 * @author timfulmer
 */
public class ProcessingResult {

    /**
     * The message processed, it may have been modified during processing.
     */
    private Message message;

    /**
     * List of new messages created during processing.
     */
    private List<NewMessage> messages= new ArrayList<>();

    /**
     * If true, modifications made during processing are propagated.
     */
    private boolean continueProcessing= true;

    public ProcessingResult() {

        super();
    }

    public void addMessage( String key){

        this.addMessage(key, new HashMap(), null, null);
    }

    public void addMessage( String key, Map values){

        this.addMessage( key, values, null, null);
    }

    public void addMessage( String key, String behaviorType,
                            String behaviorString){

        this.addMessage(key, new HashMap(), behaviorType, behaviorString);
    }

    public void addMessage( String key, Map values, String behaviorType,
                            String behaviorString){

        Behavior behavior= null;
        if( behaviorType!= null && behaviorString!= null){

            behavior= new Behavior( BehaviorType.valueOf( behaviorType),
                    behaviorString);
        }
        this.messages.add(new NewMessage(key, behavior, values));
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public List<NewMessage> getMessages() {

        return this.messages;
    }

    public boolean isContinueProcessing() {
        return continueProcessing;
    }

    public void setContinueProcessing(boolean continueProcessing) {
        this.continueProcessing = continueProcessing;
    }

    public class NewMessage{
        public String key;
        public Behavior behavior;
        public Map values;

        public NewMessage(String key, Behavior behavior, Map values) {
            this.key = key;
            this.behavior = behavior;
            this.values = values;
        }

        MessageKey makeKey(){

            // TODO: Handle additional key formats.
            return new MessageKey( String.format( "//%s/%s/%s",
                    getMessage().getMessageKey().getResourceName(),
                    getMessage().getMessageKey().getUserContext(),
                    this.key));
        }
    }
}
