package info.bigdatahowto.defaults.js;

import info.bigdatahowto.core.Message;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static info.bigdatahowto.defaults.TestUtils.fakeMessage;

/**
 * @author timfulmer
 */
public class JavaScriptProcessorTemplateTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testJavaScriptProcessorTemplate(){

        JavaScriptProcessorTemplate javaScriptProcessorTemplate=
                new JavaScriptProcessorTemplate() {
                    @Override
                    public String processWithMeta() {
                        return "meta: %s\nfunction: %s";
                    }

                    @Override
                    public String processWithoutMeta() {
                        return "function: %s";
                    }

                    @Override
                    public String errorWithMeta() {
                        return "meta: %s\nfunction: %s";
                    }

                    @Override
                    public String errorWithoutMeta() {
                        return "function: %s";
                    }
                };
        Message message= fakeMessage();
        Map<String,String> value= new HashMap<>( 1);
        value.put( "k", "v");
        message.getValues().put( "key", value);
        String result= javaScriptProcessorTemplate.composeProcessScript(
                message, "test-behavior");
        assert result!= null:
                "JavaScriptProcessorTemplate.composeProcessScript is not " +
                        "implemented correctly.";
    }
}
