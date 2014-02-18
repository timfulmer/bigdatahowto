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

    public Message accessMessage( Message message, String authentication,
                                  BehaviorType behaviorType){

        Resource resource= this.resources.get(
                message.getMessageKey().getResourceName());
        message= resource.get(message);
        if( message== null){

            return null;
        }
        if( !this.authenticator.authorize( message, authentication,
                behaviorType)){

            throw new IllegalAccessError( String.format(
                    "Authentication '%s' does not have '%s' access to " +
                            "message with key '%s'", authentication,
                    behaviorType.toString(), message.getKey()));
        }

        return message;
    }

    /**
     * Checks if authentication has access to an existing message.  If so, that
     * message's metadata is used.  Otherwise a new message is created and
     * provisioned for authentication.  Message's behavior is updated with
     * new behavior.
     *
     * !!WARNING: Potential data loss with concurrent processing and writes to
     * the same message!!
     *
     * TODO: Introduce retry with exponential backoff up to a short timeout.
     *
     * @param message New message to store.
     * @param behavior New behavior.
     * @param authentication Authenticates access to messages.
     */
    @SuppressWarnings("unchecked")
    public Message storeMessage(Message message, Behavior behavior,
                             String authentication) {

        // TODO: Revisit null behavior handling.

        assert authentication!= null:
                "Authentication cannot be null.";

        // tf - Merge behavior & values meta data for existing message.
        Message persistent= this.accessMessage( message, authentication,
                behavior== null ? BehaviorType.Persist : behavior.getBehaviorType());
        if( persistent== null){

            this.authenticator.provision( message, authentication);
        }else{

            persistent.getValues().putAll( message.getValues());
            persistent.setSecure( message.isSecure());
            message= persistent;
        }
        if( behavior!= null){

            message.getBehavior().put( behavior.getBehaviorType(), behavior);
        }
        this.updateMessage(message);

        return message;
    }

    /**
     * Updates persistent message state on an already configured message.
     *
     * @param message    Message to update.
     */
    public void updateMessage(Message message) {

        Resource resource= this.resources.get(
                message.getMessageKey().getResourceName());
        resource.put(message);
    }

    /**
     * Deletes a message from the underlying resource.
     *
     * @param message    Message to delete.
     */
    public void deleteMessage(Message message) {

        this.resources.get(
                message.getMessageKey().getResourceName()).delete( message);
    }

    /**
     * Add a resource.  Please note there is only one resource per-resource
     * name.
     *
     * @param resource    Resource to add.
     */
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
