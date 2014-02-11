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

    private UserRoadie userRoadie;
    private ResourceRoadie resourceRoadie;
    private Queue queue;
    private Processor processor;

    public Bd() {

        this( null);
    }

    public Bd( String directory) {

        super();

        Resource resource= new FileResource( directory);
        this.setUserRoadie( new UserRoadie( resource));
        this.setQueue( new InMemoryQueue(resource));
        this.setResourceRoadie( new ResourceRoadie(
                new AlwaysAllowAuthenticator()));
        this.getResourceRoadie().addResource(resource);
        this.setProcessor( new JavaScriptProcessor(this.getQueue(),
                this.getResourceRoadie()));
    }

    public UUID addMessage( String key, String behaviorString,
                            String behaviorType, String authentication){

        return this.addMessage( key, behaviorString, behaviorType,
                authentication, false);
    }

    public UUID addMessage( String key, String behaviorString,
                            String behaviorTypeString, String authentication,
                            boolean secure){

        MessageKey messageKey= new MessageKey( key);
        BehaviorType behaviorType= BehaviorType.valueOf( behaviorTypeString);
        Behavior behavior= new Behavior( behaviorType,
                behaviorString);
        Message message= new Message( messageKey);
        message.setSecure( secure);
        message= this.resourceRoadie.storeMessage( message, behavior,
                authentication);

        return this.queue.push( message, behaviorType, authentication);
    }

    public Job queryJob( UUID uuid, String authentication){

        assert authentication!= null:
                "Cannot query job without authentication.";

        Job job= this.queue.getJob( uuid);
        if( job== null || authentication.equals( job.getJobOwner())
                || authentication.equals( job.getContextOwner())){

            return job;
        }

        return null;
    }

    public Object queryMetaData( String key, String name,
                                 String authentication){

        assert key!= null: "No key to query.";
        assert name!= null: "No name to query.";

        Message message= this.resourceRoadie.accessMessage(
                new Message( key), authentication, BehaviorType.Get);
        if( message== null || isEmpty(message.getValues())){

            return null;
        }

        return message.getValues().get( name);
    }

    public void processJob(){

        this.processor.pullJob();
    }

    public void register( String authentication, String userContext){

        this.userRoadie.register( authentication, userContext);
    }

    public UserRoadie getUserRoadie() {
        return userRoadie;
    }

    public void setUserRoadie(UserRoadie userRoadie) {
        this.userRoadie = userRoadie;
    }

    public ResourceRoadie getResourceRoadie() {
        return resourceRoadie;
    }

    public void setResourceRoadie(ResourceRoadie resourceRoadie) {
        this.resourceRoadie = resourceRoadie;
    }

    public Queue getQueue() {
        return queue;
    }

    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    public Processor getProcessor() {
        return processor;
    }

    public void setProcessor(Processor processor) {
        this.processor = processor;
    }

    public void setAuthenticator( Authenticator authenticator){
         this.resourceRoadie.setAuthenticator( authenticator);
    }
}
