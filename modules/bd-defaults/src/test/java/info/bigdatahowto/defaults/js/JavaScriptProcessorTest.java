package info.bigdatahowto.defaults.js;

import info.bigdatahowto.core.Behavior;
import info.bigdatahowto.core.BehaviorType;
import info.bigdatahowto.core.Message;
import info.bigdatahowto.core.ProcessingResult;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @author timfulmer
 */
@SuppressWarnings("unchecked")
public class JavaScriptProcessorTest {

    @Test
    public void testScriptEngine(){

        Message message= new Message();
        message.setKey( "//test-resource/test-context/test-key");
        message.getValues().put( "testing","testing");
        Behavior behavior= new Behavior(BehaviorType.Persist,
                "function(env,key,meta){meta.processing=true; return true;}");
        message.getBehavior().put( behavior.getBehaviorType(), behavior);
        JavaScriptProcessor javaScriptProcessor= new JavaScriptProcessor(null, null);
        ProcessingResult processingResult= javaScriptProcessor.process(message,
                behavior.getBehaviorType());
        assert processingResult!= null:
                "JavaScriptProcessor.process did not return a processingResult.";
        assert processingResult.getMessage()!= null:
                "JavaScriptProcessor.message is not populated.";
        assert processingResult.getMessage().getValues()!= null:
                "JavaScriptProcessor.message.values is not populated.";
        assert processingResult.getMessage().getValues().containsKey( "testing"):
                "JavaScriptProcessor.message.values does not contain key.";
        assert processingResult.getMessage().getValues().get(
                "testing").equals( "testing"):
                "JavaScriptProcessor.message.values does not contain key.";
        assert processingResult.getMessage().getValues().size()== 2:
                "JavaScriptProcessor.process processingResult.message.values is incorrect size.";
        assert processingResult.getMessage().getValues().containsKey( "processing"):
                "JavaScriptProcessor.process processingResult.message.values does not contain key.";
        assert processingResult.getMessage().getValues().get(
                        "processing").equals( Boolean.TRUE):
                "JavaScriptProcessor.message.values does not contain key.";
    }

    @Test
    public void testScriptEngine_NewMessages(){

        Message message= new Message();
        message.setKey( "//test-resource/test-context/test-key");
        message.getValues().put( "testing","testing");
        Behavior behavior= new Behavior(BehaviorType.Persist,
                "function(env,key,meta){var newMessages=[];\n" +
                "for(var i=0;i<5;i++){\n" +
                "  var newMessage={};\n" +
                "  newMessage.key='//test-resource/test-context/test-key0'+i;\n" +
                "  newMessage.meta= {};\n" +
                "  newMessage.meta.dynamic=true;\n" +
                "  newMessage.persist=function(env,key,values){return true;}\n" +
                "  newMessages.push(newMessage);\n" +
                "}\n" +
                "return newMessages;}");
        message.getBehavior().put( behavior.getBehaviorType(), behavior);
        JavaScriptProcessor javaScriptProcessor= new JavaScriptProcessor(null, null);
        ProcessingResult processingResult= javaScriptProcessor.process(message,
                behavior.getBehaviorType());
        assert processingResult!= null:
                "JavaScriptProcessor.process did not return a processingResult.";
        assert processingResult.getMessages()!= null:
                "JavaScriptProcessor.process did not return processingResult.messages";
        assert processingResult.getMessages().size()== 5:
                "JavaScriptProcessor.process did not return processingResult.messages";
        assert processingResult.getMessages().get(0).behavior!= null:
                "JavaScriptProcessor.process did not return processingResult.messages.behavior";
        assert processingResult.getMessages().get(0).behavior.getBehaviorType()== BehaviorType.Persist:
                "JavaScriptProcessor.process did not return processingResult.messages.behavior";
        assert processingResult.getMessages().get(0).behavior.getFunction().equals(
                "\nfunction (env, key, values) {\n    return true;\n}\n");
    }

    @Test
    public void testScriptEngine_Error(){

        Message message= new Message();
        message.setKey( "//test-resource/test-context/test-key");
        message.getValues().put( "testing","testing");
        Behavior behavior= new Behavior(BehaviorType.Error,
                "function(env,key,meta){meta.processing=true; return true;}");
        message.getBehavior().put( behavior.getBehaviorType(), behavior);
        JavaScriptProcessor javaScriptProcessor= new JavaScriptProcessor(null, null);
        ProcessingResult processingResult= javaScriptProcessor.error(message, 5);
        assert processingResult!= null:
                "JavaScriptProcessor.process did not return a processingResult.";
        assert processingResult.getMessage()!= null:
                "JavaScriptProcessor.message is not populated.";
        assert processingResult.getMessage().getValues()!= null:
                "JavaScriptProcessor.message.values is not populated.";
        assert processingResult.getMessage().getValues().containsKey( "processing"):
                "JavaScriptProcessor.message.values does not contain key.";
        assert processingResult.getMessage().getValues().get(
                "processing").equals( Boolean.TRUE):
                "JavaScriptProcessor.message.values does not contain key.";
    }

    @Test
    public void testScriptEngine_Meta(){

        Message message= new Message();
        message.setKey( "//test-resource/test-context/test-key");
        message.getValues().put( "testing","testing");
        Behavior behavior= new Behavior(BehaviorType.Persist,
                "function(env,key,meta){meta.testing='modified';meta.processing=true; return true;}");
        message.getBehavior().put( behavior.getBehaviorType(), behavior);
        JavaScriptProcessor javaScriptProcessor= new JavaScriptProcessor(null, null);
        ProcessingResult processingResult= javaScriptProcessor.process(message,
                behavior.getBehaviorType());
        assert processingResult!= null:
                "JavaScriptProcessor.process did not return a processingResult.";
        assert processingResult.getMessage()!= null:
                "JavaScriptProcessor.message is not populated.";
        assert processingResult.getMessage().getValues()!= null:
                "JavaScriptProcessor.message.values is not populated.";
        assert processingResult.getMessage().getValues().size()== 2:
                "JavaScriptProcessor.process processingResult.message.values is incorrect size.";
        assert processingResult.getMessage().getValues().containsKey( "testing"):
                "JavaScriptProcessor.message.values does not contain key.";
        assert processingResult.getMessage().getValues().get(
                "testing").equals( "modified"):
                "JavaScriptProcessor.message.values does not contain key.";
        assert processingResult.getMessage().getValues().containsKey( "processing"):
                "JavaScriptProcessor.message.values does not contain key.";
        assert processingResult.getMessage().getValues().get(
                "processing").equals( Boolean.TRUE):
                "JavaScriptProcessor.message.values does not contain key.";
    }

    @Test
    public void testMetaList(){

        Behavior behavior= new Behavior( BehaviorType.Persist,
                "function(env,key,meta){var item={};item.property='value';meta.items=[item];return true;}");
        Message message= new Message();
        message.setKey( "//test-resource/test-context/test-key");
        message.getValues().put("testing", "testing");
        message.getBehavior().put( behavior.getBehaviorType(), behavior);
        JavaScriptProcessor javaScriptProcessor= new JavaScriptProcessor( null, null);
        ProcessingResult processingResult= javaScriptProcessor.process( message,
                behavior.getBehaviorType());
        assert processingResult!= null:
                "JavaScriptProcessor.process did not return a processingResult.";
        assert processingResult.getMessage()!= null:
                "JavaScriptProcessor.message is not populated.";
        assert processingResult.getMessage().getValues()!= null:
                "JavaScriptProcessor.message.values is not populated.";
        assert processingResult.getMessage().getValues().containsKey( "items"):
                "JavaScriptProcessor.message.values does not contain key.";
        assert processingResult.getMessage().getValues().get( "items") instanceof List:
                "JavaScriptProcessor.process processingResult.message.values is incorrect.";
        assert ((List) processingResult.getMessage().getValues().get( "items")).get(0) instanceof Map :
                "JavaScriptProcessor.process processingResult.message.values is incorrect.";
        assert ((Map) ((List) processingResult.getMessage().getValues().get( "items")).get(0)).get("property")!= null:
                "JavaScriptProcessor.process processingResult.message.values is incorrect.";
        assert ((Map) ((List) processingResult.getMessage().getValues().get( "items")).get(0)).get("property").equals( "value"):
                "JavaScriptProcessor.process processingResult.message.values is incorrect.";
    }
}
