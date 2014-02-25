package info.bigdatahowto.defaults.js;

import info.bigdatahowto.core.*;

import javax.script.*;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Executes message behavior in a JavaScript runtime.
 *
 * @author timfulmer
 */
public class JavaScriptProcessor extends Processor {

    private transient Logger logger= Logger.getLogger(
            this.getClass().getName());

    private JavaScriptProcessorTemplate javaScriptProcessorTemplate;
    private ScriptEngine scriptEngine;

    public JavaScriptProcessor(Queue queue, ResourceRoadie resourceRoadie) {

        super(queue, resourceRoadie);

        this.javaScriptProcessorTemplate=
                new DefaultJavaScriptProcessorTemplate();
        this.scriptEngine= new ScriptEngineManager().getEngineByName(
                "JavaScript");
    }

    /**
     * Applies behavior to data defined in a message.
     *
     * @param message Message containing behavior and data.
     * @return Results of processing.
     */
    @Override
    protected ProcessingResult process(Message message,
                                       BehaviorType behaviorType) {

        String script = this.javaScriptProcessorTemplate.composeProcessScript(
                message, message.getBehavior().get(behaviorType).getFunction());
        Bindings bindings= new SimpleBindings();

        return getProcessingResult(message, script, bindings);
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

        String script= this.javaScriptProcessorTemplate.composeErrorScript(
                message, message.getBehavior().get(
                        BehaviorType.Error).getFunction());
        Bindings bindings= new SimpleBindings();
        bindings.put( "tries", tries);

        return this.getProcessingResult(message, script, bindings);
    }

    @SuppressWarnings("unchecked")
    private ProcessingResult getProcessingResult(
            Message message, String script, Bindings bindings) {

        bindings.put( "key", message.getMessageKey().getUserKey());
        ProcessingResult processingResult= new ProcessingResult();
        bindings.put( "processingResult", processingResult);
        StringWriter errorWriter= new StringWriter();
        this.scriptEngine.getContext().setErrorWriter( errorWriter);
        try {

            this.scriptEngine.eval(script, bindings);
        } catch (ScriptException e) {

            String msg = String.format("Could not evaluate JS script '%s', " +
                    "returned error '%s'.", script, errorWriter.toString());
            this.logger.log(Level.SEVERE, msg, e);

            throw new RuntimeException(msg, e);
        }
        message.setValues( new HashMap( (Map)bindings.get( "meta")));
        processingResult.setMessage( message);

        return processingResult;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setJavaScriptProcessorTemplate(
            JavaScriptProcessorTemplate javaScriptProcessorTemplate) {
        this.javaScriptProcessorTemplate = javaScriptProcessorTemplate;
    }
}
