package info.bigdatahowto.api;

import info.bigdatahowto.core.*;
import info.bigdatahowto.defaults.AlwaysAllowAuthenticator;
import info.bigdatahowto.defaults.FileResource;
import info.bigdatahowto.defaults.InMemoryQueue;
import info.bigdatahowto.defaults.js.JavaScriptProcessor;

import java.util.UUID;

import static org.apache.commons.collections.MapUtils.isEmpty;

/**
 * External API to our Big Data system.
 *
 * @author timfulmer
 */
public class Bd {

    private ResourceRoadie resourceRoadie;
    private Queue queue;
    private Processor processor;

    public Bd() {

        this( null);
    }

    public Bd( String directory) {

        super();

        Resource resource= new FileResource( directory);
        Queue queue= new InMemoryQueue(resource);
        this.setQueue(queue);
        ResourceRoadie resourceRoadie= new ResourceRoadie(
                new AlwaysAllowAuthenticator());
        resourceRoadie.addResource( resource);
        this.setResourceRoadie(resourceRoadie);
        this.setProcessor(new JavaScriptProcessor(queue, resourceRoadie));
    }

    public UUID addMessage( String key, String behaviorString,
                            String behaviorType,
                            String authentication){

        MessageKey messageKey= new MessageKey( key);
        Behavior behavior= new Behavior( BehaviorType.valueOf( behaviorType),
                behaviorString);
        Message message= this.resourceRoadie.storeMessage( messageKey,
                behavior, authentication);

        return this.queue.push( message, authentication);
    }

    public Job queryJob( UUID uuid){

        return this.queue.getJob( uuid);
    }

    public Object queryMetaData( String key, String name,
                                 String authentication){

        Message message= this.resourceRoadie.accessMessage(
                new MessageKey( key), authentication);
        if( message== null || isEmpty(message.getValues())){

            return null;
        }

        return message.getValues().get( name);
    }

    public void processJob(){

        this.processor.pullJob();
    }

    public void setResourceRoadie(ResourceRoadie resourceRoadie) {
        this.resourceRoadie = resourceRoadie;
    }

    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    public void setProcessor(Processor processor) {
        this.processor = processor;
    }
}
