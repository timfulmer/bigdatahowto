package info.bigdatahowto.core;

import org.junit.Test;

/**
 * @author timfulmer
 */
public class MessageKeyTest {

    @Test
    public void testMessageKey(){

        String key= "//resource-name/user-context/user-key";
        MessageKey messageKey= new MessageKey( key);
        assert "resource-name".equals( messageKey.getResourceName()):
                "MessageKey.resourceName not initialized correctly.";
        assert "user-context".equals( messageKey.getUserContext()):
                "MessageKey.userContext not initialized correctly.";
        assert "user-key".equals( messageKey.getUserKey()):
                "MessageKey.userKey not initialized correctly.";
        assert "user-context/user-key".equals( messageKey.getAggregateRootKey()):
                "MessageKey.aggregateRootKey not initialized correctly.";
    }
}
