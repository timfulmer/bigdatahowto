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
    private List<Message> messages= new ArrayList<>();

    /**
     * If true, modifications made during processing are propagated.
     */
    private boolean continueProcessing= true;

    public ProcessingResult() {

        super();
    }

    public void addMessage( String key, Map values, String behavior){

        Map<String,String> behaviorMap= new HashMap<>( 1);
        behaviorMap.put( "persist", behavior);
        this.messages.add( new Message( key, values, behaviorMap,
                new HashMap<String,String>(0)));
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public boolean isContinueProcessing() {
        return continueProcessing;
    }

    public void setContinueProcessing(boolean continueProcessing) {
        this.continueProcessing = continueProcessing;
    }
}
