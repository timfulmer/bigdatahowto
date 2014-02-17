package info.bigdatahowto.api;

import info.bigdatahowto.core.*;
import info.bigdatahowto.defaults.*;
import info.bigdatahowto.defaults.aws.ElastiCache;
import info.bigdatahowto.defaults.aws.S3Resource;
import info.bigdatahowto.defaults.aws.SqsQueue;
import info.bigdatahowto.defaults.js.JavaScriptProcessor;

import java.util.UUID;

import static org.apache.commons.collections.MapUtils.isEmpty;

/**
 * External API to our Big Data system.
 *
 * @author timfulmer
 */
public class Bd {

    private static Bd defaultInstance;

    public static Bd defaultInstance( String directory){

        if( defaultInstance== null){

            defaultInstance= new Bd( directory);
        }

        return defaultInstance;
    }

    private static Bd productionInstance;

    public static Bd productionInstance(){

        if( productionInstance== null){

            Resource resource= new S3Resource();
            UserRoadie userRoadie= new UserRoadie( resource);
            Queue queue= new SqsQueue( resource, new ElastiCache());
            productionInstance= new Bd(resource, new BdAuthenticator(
                    userRoadie), userRoadie, queue);
        }

        return productionInstance;
    }

    private UserRoadie userRoadie;
    private ResourceRoadie resourceRoadie;
    private Queue queue;
    private Processor processor;

    private Bd( String directory) {

        this( new FileResource( directory), new AlwaysAllowAuthenticator(),
                new UserRoadie(), new InMemoryQueue());
    }

    private Bd(Resource resource, Authenticator authenticator, UserRoadie userRoadie, Queue queue){

        super();

        this.userRoadie= userRoadie;
        this.userRoadie.setResource( resource);
        this.queue= queue;
        this.queue.setResource( resource);
        this.resourceRoadie= new ResourceRoadie( authenticator);
        this.resourceRoadie.addResource(resource);
        this.processor= new JavaScriptProcessor(this.queue,
                this.resourceRoadie);
    }

    public UUID addMessage( UUID jobUuid, String key, String behaviorString,
                            String behaviorType, String authentication){

        return this.addMessage( jobUuid, key, behaviorString, behaviorType,
                authentication, false);
    }

    public UUID addMessage( UUID jobUuid, String key, String behaviorString,
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

        return this.queue.push( jobUuid, message, behaviorType, authentication);
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

        this.userRoadie.register(authentication, userContext);
    }

    public void clear() {

        this.queue.clear();
    }
}
