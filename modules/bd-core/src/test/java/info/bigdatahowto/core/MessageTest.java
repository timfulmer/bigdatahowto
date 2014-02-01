package info.bigdatahowto.core;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static info.bigdatahowto.core.TestUtils.fakeMessage;
import static org.apache.commons.collections.MapUtils.isEmpty;

/**
 * @author timfulmer
 */
public class MessageTest {

    @Test
    public void testMessage(){

        Map<String,String> values= new HashMap<>();
        Map<String,String> behavior= new HashMap<>();
        Map<String,String> options= new HashMap<>();
        Message message= new Message(
                TestUtils.MESSAGE_KEY, values, behavior, options);

        this.assertMessage(new MessageKey( TestUtils.MESSAGE_KEY), values,
                behavior, options, message);

        // tf - Test mutation while we're here.
        MessageKey messageKey= new MessageKey();
        message.setMessageKey(messageKey);
        message.setValues(values);
        behavior.put( "test-key", "test-value");
        message.setBehavior( behavior);
        message.setOptions( options);

        this.assertMessage(messageKey, values, behavior, options, message);
    }

    @Test
    public void testResourceKey(){

        Message message= fakeMessage();
        assert TestUtils.MESSAGE_RESOURCE_KEY.equals( message.resourceKey()):
                "Message.resourceKey is implemented incorrectly.";
    }

    private void assertMessage(
            MessageKey messageKey, Map<String, String> values, Map<String, String> behavior,
            Map<String, String> options, Message message) {

        assert message.getMessageKey()!= null && messageKey.equals(
                message.getMessageKey()):
                "Message.key is not set correctly.";
        assert values.equals( message.getValues()):
                "Message.value is not set correctly.";
        assert behavior== message.getBehavior() && behavior.equals(
                message.getBehavior()):
                "Message.behavior is not set correctly.";
        assert isEmpty(behavior)!= message.hasBehavior():
                "Message.hasBehavior is not implemented correctly.";
        assert options== message.getOptions():
                "Message.options is not set correctly.";
    }
}
