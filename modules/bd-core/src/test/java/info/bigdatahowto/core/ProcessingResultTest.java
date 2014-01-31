package info.bigdatahowto.core;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static info.bigdatahowto.core.TestUtils.fakeMessage;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

/**
 * @author timfulmer
 */
public class ProcessingResultTest {

    @Test
    public void testProcessingResult(){

        ProcessingResult processingResult= new ProcessingResult();
        assert processingResult.getMessage()== null:
                "ProcessingResult.message is not initialized correctly.";
        assert isEmpty(processingResult.getMessages()):
                "ProcessingResult.messages is not initialized correctly.";
        assert processingResult.isContinueProcessing():
                "ProcessingResult.continueProcessing is not initialized " +
                        "correctly.";

        Message message= fakeMessage();
        processingResult.setMessage( message);
        List<Message> messages= new ArrayList<>( 1);
        messages.add( message);
        processingResult.setMessages( messages);
        processingResult.setContinueProcessing( false);
        assert message.equals( processingResult.getMessage()):
                "ProcessingResult.message is not initialized correctly.";
        assert messages.equals( processingResult.getMessages()):
                "ProcessingResult.messages is not initialized correctly.";
        assert !processingResult.isContinueProcessing():
                "ProcessingResult.continueProcessing is not initialized " +
                        "correctly.";
    }
}
