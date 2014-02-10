package info.bigdatahowto.core;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Manages the resources available to the system.
 *
 * @author timfulmer
 */
public class ResourceRoadie {

    private transient Logger logger= Logger.getLogger(
            this.getClass().getName());

    private Authenticator authenticator;
    private Map<String,Resource> resources= new HashMap<>();

    public ResourceRoadie( Authenticator authenticator) {

        super();

        this.setAuthenticator( authenticator);
    }

    public Message accessMessage( MessageKey messageKey, String authentication){

        if( !this.authenticator.authorize( messageKey.getKey(),
                authentication)){

            return null;
        }

        Resource resource= this.resources.get( messageKey.getResourceName());

        return resource.get(messageKey.getAggregateRootKey(), Message.class);
    }

    /**
     * Checks if authentication has access to an existing message.  If so, that
     * message's metadata is used.  Otherwise a new message is created and
     * provisioned for authentication.  Message's behavior is updated with
     * new behavior.
     *
     * @param messageKey Identifies a message.
     * @param behavior New behavior.
     * @param authentication Authenticates access to messages.
     */
    public Message storeMessage(MessageKey messageKey, Behavior behavior,
                             String authentication) {

        assert authentication!= null:
                "Authentication cannot be null.";

        // tf - Merge meta data for existing key.
        Message message= this.accessMessage( messageKey, authentication);
        if( message== null){

            message= new Message( messageKey.getKey());
            this.authenticator.provision( message.getMessageKey().getKey(),
                    authentication);
        }
        message.getBehavior().put( behavior.getBehaviorType(), behavior);
        this.storeMessage( message);

        return message;
    }

    public void storeMessage(Message message) {

        Resource resource= this.resources.get(
                message.getMessageKey().getResourceName());
        resource.put(message);
    }

    public void addResource( Resource resource){

        this.resources.put( resource.getName(), resource);
    }

    @javax.annotation.Resource
    public void setResources(Map<String, Resource> resources) {
        this.resources = resources;
    }

    @javax.annotation.Resource
    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }
}
