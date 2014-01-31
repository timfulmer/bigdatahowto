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

        String value= "test-value";
        Map<String,String> behavior= new HashMap<>();
        Map<String,String> options= new HashMap<>();
        Message message= new Message(
                TestUtils.MESSAGE_KEY, "test-value", behavior, options);

        this.assertMessage(new MessageKey( TestUtils.MESSAGE_KEY), value,
                behavior, options, message);

        // tf - Test mutation while we're here.
        MessageKey messageKey= new MessageKey();
        message.setMessageKey(messageKey);
        message.setValue(value);
        behavior.put( "test-key", "test-value");
        message.setBehavior( behavior);
        message.setOptions( options);

        this.assertMessage(messageKey, value, behavior, options, message);
    }

    @Test
    public void testResourceKey(){

        Message message= fakeMessage();
        assert TestUtils.MESSAGE_RESOURCE_KEY.equals( message.resourceKey()):
                "Message.resourceKey is implemented incorrectly.";
    }

    private void assertMessage(
            MessageKey messageKey,String value, Map<String, String> behavior,
            Map<String, String> options, Message message) {

        assert message.getMessageKey()!= null && messageKey.equals(
                message.getMessageKey()):
                "Message.key is not set correctly.";
        assert value.equals( message.getValue()):
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
