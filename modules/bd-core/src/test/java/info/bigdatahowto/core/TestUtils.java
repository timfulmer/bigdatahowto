package info.bigdatahowto.core;

/**
 * @author timfulmer
 */
public class TestUtils {

    static final String MESSAGE_RESOURCE_KEY= "test-resource";
    static final String MESSAGE_USER_CONTEXT_KEY= "test-userContext";
    static final String MESSAGE_JSON_KEY = "test-key";
    static final String MESSAGE_USER_KEY= MESSAGE_USER_CONTEXT_KEY+ "/"+
            MESSAGE_JSON_KEY;
    static final String MESSAGE_KEY=
            "//"+ MESSAGE_RESOURCE_KEY+ "/"+ MESSAGE_USER_CONTEXT_KEY+ "/"+
                    MESSAGE_JSON_KEY;

    static Message fakeMessage() {

        return new Message(MESSAGE_KEY);
    }

    static Job fakeJob(){

        return fakeJob( fakeMessage());
    }

    static Job fakeJob(Message message){

        String authentication= "test-authentication";

        return fakeJob( message, authentication);
    }

    static Job fakeJob(Message message, String authentication){

        return new Job( message, authentication);
    }
}
