package info.bigdatahowto.core;

import java.util.ArrayList;
import java.util.List;

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
