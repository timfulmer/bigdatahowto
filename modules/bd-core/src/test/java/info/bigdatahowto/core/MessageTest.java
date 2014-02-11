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
        Map<BehaviorType,Behavior> behaviorMap= new HashMap<>();
        Map<String,String> options= new HashMap<>();
        Message message= new Message(
                TestUtils.MESSAGE_KEY, values, behaviorMap, options);

        this.assertMessage(new MessageKey( TestUtils.MESSAGE_KEY), values,
                behaviorMap, options, message);

        // tf - Test mutation while we're here.
        MessageKey messageKey= new MessageKey();
        message.setMessageKey(messageKey);
        message.setValues(values);
        Behavior behavior= new Behavior(BehaviorType.Persist, "test-value");
        behaviorMap.put(behavior.getBehaviorType(), behavior);
        message.setBehavior( behaviorMap);
        message.setOptions( options);

        this.assertMessage(messageKey, values, behaviorMap, options, message);
    }

    @Test
    public void testResourceKey(){

        Message message= fakeMessage();
        assert String.format( "messages/%s",
                TestUtils.MESSAGE_USER_KEY).equals(message.resourceKey()):
                "Message.resourceKey is implemented incorrectly.";
    }

    private void assertMessage(
            MessageKey messageKey, Map<String, String> values, Map<BehaviorType, Behavior> behavior,
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
