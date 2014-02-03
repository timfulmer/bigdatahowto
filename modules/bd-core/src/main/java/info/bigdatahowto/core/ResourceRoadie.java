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

    public void storeMessage(Message message) {

        this.storeMessage(message, null);
    }

    public void storeMessage(Message message, String authentication) {

        if( authentication!= null){

            this.authenticator.provision( message.getMessageKey().getKey(),
                    authentication);
        }
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
