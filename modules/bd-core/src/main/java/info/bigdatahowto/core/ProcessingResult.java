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

        this.addMessage(key, new HashMap(), null);
    }

    public void addMessage( String key, Map values){

        this.addMessage( key, values, null);
    }

    public void addMessage( String key, String behavior){

        this.addMessage(key, new HashMap(), behavior);
    }

    public void addMessage( String key, Map values, String behavior){

        this.messages.add(new NewMessage(key, behavior, values));
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public List<Message> getMessages() {

        List<Message> messages= new ArrayList<>( this.messages.size());
        for( NewMessage newMessage: this.messages){

            messages.add( newMessage.toMessage( this.getMessage().getMessageKey()));
        }

        return messages;
    }

    public boolean isContinueProcessing() {
        return continueProcessing;
    }

    public void setContinueProcessing(boolean continueProcessing) {
        this.continueProcessing = continueProcessing;
    }

    private class NewMessage{
        String key, behavior;
        Map values;

        private NewMessage(String key, String behavior, Map values) {
            this.key = key;
            this.behavior = behavior;
            this.values = values;
        }

        public Message toMessage(MessageKey messageKey) {
            Map<String,String> behavior= new HashMap<>(1);
            behavior.put( "persist", this.behavior);

            // TODO: Handle additional key formats.
            String key= String.format( "//%s/%s/%s",
                    messageKey.getResourceName(), messageKey.getUserContext(),
                    this.key);
            return new Message( key, this.values, behavior,
                    new HashMap<String,String>(0));
        }
    }
}
