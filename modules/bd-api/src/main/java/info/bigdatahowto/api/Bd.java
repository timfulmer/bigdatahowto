package info.bigdatahowto.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.bigdatahowto.core.*;
import info.bigdatahowto.defaults.AlwaysAllowAuthenticator;
import info.bigdatahowto.defaults.BdAuthenticator;
import info.bigdatahowto.defaults.FileResource;
import info.bigdatahowto.defaults.InMemoryQueue;
import info.bigdatahowto.defaults.aws.ElastiCache;
import info.bigdatahowto.defaults.aws.S3Resource;
import info.bigdatahowto.defaults.aws.SqsQueue;
import info.bigdatahowto.defaults.js.JavaScriptProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private transient ObjectMapper objectMapper= new ObjectMapper();
    private transient Logger logger= Logger.getLogger(
            this.getClass().getName());

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
                            String behaviorType,
                            Map<String,String> values, String authentication){

        return this.addMessage( jobUuid, key, behaviorString, behaviorType,
                values, authentication, false);
    }

    public UUID addMessage( UUID jobUuid, String key, String behaviorString,
                            String behaviorTypeString, String authentication,
                            boolean secure){

        return this.addMessage( jobUuid, key, behaviorString,
                behaviorTypeString, new HashMap( 0), authentication, secure);
    }

    public UUID addMessage( UUID jobUuid, String key, String behaviorString,
                            String behaviorTypeString,
                            Map<String,String> values, String authentication,
                            boolean secure){

        MessageKey messageKey= new MessageKey( key);
        BehaviorType behaviorType= BehaviorType.valueOf( behaviorTypeString);
        Behavior behavior= new Behavior( behaviorType,
                behaviorString);
        Message message= new Message( messageKey, values);
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

    public String queryMetaData( String key, String name,
                                 String authentication){

        assert key!= null: "No key to query.";
        assert name!= null: "No name to query.";

        Message message= this.resourceRoadie.accessMessage(
                new Message( new MessageKey(key)), authentication,
                BehaviorType.Get);
        if( message== null || isEmpty(message.getValues())){

            return "{}";
        }

        if( message.getValues().get(name)== null){

            return "{}";
        }

        try {

            return this.objectMapper.writeValueAsString(
                    message.getValues().get(name));
        } catch (JsonProcessingException e) {

            String msg = String.format("Could not serialize object type '%s' " +
                    "with state '%s'.",
                    message.getValues().get(name).getClass(),
                    message.getValues().get(name).toString());
            this.logger.log(Level.SEVERE, msg, e);

            throw new RuntimeException(msg, e);
        }
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

    public void setAuthenticator( Authenticator authenticator){

        this.resourceRoadie.setAuthenticator( authenticator);
    }
}
