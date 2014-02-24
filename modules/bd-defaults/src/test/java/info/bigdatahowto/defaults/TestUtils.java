package info.bigdatahowto.defaults;

import info.bigdatahowto.core.BehaviorType;
import info.bigdatahowto.core.Job;
import info.bigdatahowto.core.Message;
import info.bigdatahowto.core.MessageKey;

/**
 * @author timfulmer
 */
public class TestUtils {

    public static final String MESSAGE_RESOURCE_KEY= "test-resource";
    public static final String MESSAGE_USER_CONTEXT_KEY= "test-userContext";
    public static final String MESSAGE_JSON_KEY = "test-key";
    public static final String MESSAGE_USER_KEY= MESSAGE_USER_CONTEXT_KEY+ "/"+
            MESSAGE_JSON_KEY;
    public static final String MESSAGE_KEY=
            "//"+ MESSAGE_RESOURCE_KEY+ "/"+ MESSAGE_USER_CONTEXT_KEY+ "/"+
                    MESSAGE_JSON_KEY;

    public static Message fakeMessage() {

        Message message= new Message(new MessageKey( MESSAGE_KEY));
        message.setContextOwner( MESSAGE_USER_CONTEXT_KEY);

        return message;
    }

    public static Job fakeJob(){

        return fakeJob( fakeMessage());
    }

    public static Job fakeJob(Message message){

        String authentication= "test-authentication";

        return fakeJob( message, authentication);
    }

    public static Job fakeJob(Message message, String authentication){

        return new Job( message, BehaviorType.Persist, authentication,
                message.getContextOwner());
    }
}
