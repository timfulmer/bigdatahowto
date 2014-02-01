package info.bigdatahowto.defaults;

import info.bigdatahowto.core.Message;
import info.bigdatahowto.core.ProcessingResult;
import info.bigdatahowto.core.Processor;

import javax.script.*;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Executes message behavior in a JavaScript runtime.
 *
 * @author timfulmer
 */
public class JavaScriptProcessor extends Processor {

    private static final String JS_PROCESS_TEMPLATE =
            "function parseResults(result){\n" +
                    "  var persist;\n" +
                    "  if(result.persist) persist=result.persist.toString();\n" +
                    "  processingResult.addMessage(result.key,result.meta,persist);\n" +
                    "}\n" +
                    "var env= {};\n" +
                    "var meta= {};\n" +
                    "var results= %s(env,key,meta);\n" +
                    "if( !results){\n" +
                    "  processingResult.continueProcessing= false;\n" +
                    "}else if( toString.call(results) === \"[object Array]\"){\n" +
                    "  results.forEach(function(result){\n" +
                    "    parseResults(result);\n" +
                    "  });\n" +
                    "}";
    private static final String JS_ERROR_TEMPLATE=
            "function parseResults(result){\n" +
                    "  var persist;\n" +
                    "  if(result.persist) persist=result.persist.toString();\n" +
                    "  processingResult.addMessage(result.key,result.meta,persist);\n" +
                    "}\n" +
                    "var env= {};\n" +
                    "var meta= {};\n" +
                    "var results= %s(env,key,meta,tries);\n" +
                    "if( !results){\n" +
                    "  processingResult.continueProcessing= false;\n" +
                    "}else if( toString.call(results) === \"[object Array]\"){\n" +
                    "  results.forEach(function(result){\n" +
                    "    parseResults(result);\n" +
                    "  });\n" +
                    "}";

    private transient Logger logger= Logger.getLogger(
            this.getClass().getName());

    /**
     * Applies behavior to data defined in a message.
     *
     * @param message Message containing behavior and data.
     * @return Results of processing.
     */
    @Override
    protected ProcessingResult process(Message message) {

        String script= String.format(JS_PROCESS_TEMPLATE,
                message.getBehavior().get( "persist"));
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

        String script= String.format(JS_ERROR_TEMPLATE,
                message.getBehavior().get( "error"));
        Bindings bindings= new SimpleBindings();
        bindings.put( "tries", tries);

        return this.getProcessingResult( message, script, bindings);
    }

    private ProcessingResult getProcessingResult(
            Message message, String script, Bindings bindings) {

        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        bindings.put( "key", message.getMessageKey().getKey());
        ProcessingResult processingResult= new ProcessingResult();
        processingResult.setMessage( message);
        bindings.put( "processingResult", processingResult);

        StringWriter errorWriter= new StringWriter();
        engine.getContext().setErrorWriter( errorWriter);

        try {

            engine.eval(script, bindings);
        } catch (ScriptException e) {

            String msg = String.format("Could not evaluate JS script '%s', " +
                    "returned error '%s'.", script, errorWriter.toString());
            this.logger.log(Level.SEVERE, msg, e);

            throw new RuntimeException(msg, e);
        }
        processingResult.getMessage().setValues( (Map)bindings.get( "meta"));

        return processingResult;
    }
}
