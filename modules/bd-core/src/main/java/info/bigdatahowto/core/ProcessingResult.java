package info.bigdatahowto.core;

import java.util.ArrayList;
import java.util.List;

/**
 * @author timfulmer
 */
public class ProcessingResult {

    private Message message;
    private List<Message> messages= new ArrayList<>();
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
