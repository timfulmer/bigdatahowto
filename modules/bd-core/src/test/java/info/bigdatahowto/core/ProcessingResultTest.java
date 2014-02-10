package info.bigdatahowto.core;

import org.junit.Test;

import java.util.HashMap;
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
        processingResult.setMessage(message);
        processingResult.setContinueProcessing( false);
        assert message.equals( processingResult.getMessage()):
                "ProcessingResult.message is not set correctly.";
        assert !processingResult.isContinueProcessing():
                "ProcessingResult.continueProcessing is not set correctly.";
    }

    @Test
    public void testProcessingResult_AddMessage(){

        ProcessingResult processingResult= new ProcessingResult();
        Message message= fakeMessage();
        processingResult.setMessage( message);
        processingResult.addMessage( "test-key01");
        processingResult.addMessage( "test-key02",
                BehaviorType.Persist.toString(), "test-behavior");
        processingResult.addMessage( "test-key03", new HashMap());
        processingResult.addMessage( "test-key04", new HashMap(),
                BehaviorType.Persist.toString(), "test-behavior");

        List<ProcessingResult.NewMessage> messages=
                processingResult.getMessages();
        assert messages!= null:
                "ProcessingResult.messages is not implemented correctly.";
        assert messages.size()== 4:
                "ProcessingResult.messages is not implemented correctly.";
    }
}
