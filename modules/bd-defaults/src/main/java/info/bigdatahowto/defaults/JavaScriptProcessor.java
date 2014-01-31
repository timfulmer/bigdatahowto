package info.bigdatahowto.defaults;

import info.bigdatahowto.core.Message;
import info.bigdatahowto.core.ProcessingResult;
import info.bigdatahowto.core.Processor;

/**
 * Executes message behavior in a JavaScript runtime.
 *
 * @author timfulmer
 */
public class JavaScriptProcessor extends Processor {

    /**
     * Applies behavior to data defined in a message.
     *
     * @param message Message containing behavior and data.
     * @return Results of processing.
     */
    @Override
    protected ProcessingResult process(Message message) {
        return null;
    }

    /**
     * Applies error handling to data defined in a message.  This method is
     * called whenever an unhandled error happens during behavior processing.
     *
     * @param message Message containing error handling and data.
     * @param tries   Number of times processing this message has been tried.
     * @return Results of processing.
     */
    @Override
    protected ProcessingResult error(Message message, int tries) {
        return null;
    }
}
