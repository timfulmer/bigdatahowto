package info.bigdatahowto.defaults.js;

import info.bigdatahowto.core.Message;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static info.bigdatahowto.defaults.TestUtils.fakeMessage;

/**
 * @author timfulmer
 */
public class JavaScriptProcessorTemplateTest {

    private JavaScriptProcessorTemplate javaScriptProcessorTemplate;

    @Before
    public void before(){

        javaScriptProcessorTemplate = new JavaScriptProcessorTemplate() {
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
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testJavaScriptProcessorTemplate(){
        Message message= fakeMessage();
        Map<String,String> value= new HashMap<>( 1);
        value.put( "k", "v");
        message.getValues().put( "key", value);
        String result= this.javaScriptProcessorTemplate.composeProcessScript(
                message, "test-behavior");
        assert result!= null:
                "JavaScriptProcessorTemplate.composeProcessScript is not " +
                        "implemented correctly.";
        assert ("meta: meta.key={};\n" +
                "meta.key.k='v';\n" +
                "\n" +
                "function: test-behavior").equals( result):
                "JavaScriptProcessorTemplate.composeProcessScript is not " +
                        "implemented correctly.";
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCrazyListOfMaps(){
        Message message= fakeMessage();
        Map<String,String> value= new HashMap<>( 1);
        value.put( "k", "v");
        List<Map<String,String>> list= new ArrayList<>( 1);
        list.add( value);
        message.getValues().put( "key", list);
        String result= this.javaScriptProcessorTemplate.composeProcessScript(
                message, "test-behavior");
        assert result!= null:
                "JavaScriptProcessorTemplate.composeProcessScript is not " +
                        "implemented correctly.";
        assert ("meta: meta.key=[];\n" +
                "key0={};\n" +
                "key0.k='v';\n" +
                "meta.key.push(key0);\n" +
                "\n" +
                "function: test-behavior").equals( result):
                "JavaScriptProcessorTemplate.composeProcessScript is not " +
                        "implemented correctly.";
    }
}
