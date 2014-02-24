package info.bigdatahowto.api;

import info.bigdatahowto.core.BehaviorType;
import info.bigdatahowto.core.Job;
import info.bigdatahowto.core.JobState;
import info.bigdatahowto.defaults.aws.S3Resource;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

/**
 * @author timfulmer
 */
public class ProductionBdTest {

    private static final int SQS_SLEEP_TIME= 250;
    private static final String BEHAVIOR= "function(env,word,meta){\n" +
            "        // Input validation.\n" +
            "        if(!word || word.length>7) return false;\n" +
            "        // Define stem behavior.\n" +
            "        env.persistFunction= function(env,word,meta){\n" +
            "            // Update count returned from the GET request.\n" +
            "            if(!meta.count) meta.count= 0;\n" +
            "            meta.count++;\n" +
            "            return true;\n" +
            "        }\n" +
            "        // Set meta data for this word.\n" +
            "        if(!meta.count) meta.count= 0;\n" +
            "        meta.count++;\n" +
            "        // Decompose into stems, run persistFunction on each one.\n" +
            "        var stems= [];\n" +
            "        for(var i=1; i<word.length;i++){\n" +
            "            stems.push({key:word.substring(0,i),\n" +
            "                    persist:env.persistFunction});\n" +
            "        }\n" +
            "        return stems;\n" +
            "    }";

    private Bd bd= Bd.productionInstance();

    @Before
    public void before() throws IOException {

        this.clean();
    }

    @After
    public void after() throws IOException {

        this.clean();
    }

    @Test
    public void testBd() throws InterruptedException {

        this.bd.register( "test-authentication", "wordoink");

        String word= "testing";
        String key= this.makeKey( word);
        String authentication= "test-authentication";
        UUID jobUuid= UUID.randomUUID();
        this.bd.addMessage( jobUuid, key, BEHAVIOR,
                BehaviorType.Persist.toString(), authentication);
        assert jobUuid!= null:
                "Bd.addMessage is not implemented correctly.";

        Job job= this.bd.queryJob( jobUuid, authentication);
        assert job!= null:
                "Bd.queryJob is not implemented correctly.";
        assert jobUuid.equals(job.getUuid()):
                "Bd.queryJob is not implemented correctly.";
        assert JobState.Queued== job.getState():
                "Bd.queryJob is not implemented correctly.";

        Object object= this.bd.queryMetaData( key, "count", authentication);
        assert "{}".equals( object):
                "Bd.queryMetaData is not implemented correctly.";
        for( int i= 1; i< word.length()- 1; i++){

            object= this.bd.queryMetaData( this.makeKey(
                    word.substring( 0, i)), "count", authentication);
            assert "{}".equals( object):
                    "Bd.queryMetaData is not implemented correctly.";
        }

        for( int i= 0; i< 3; i++){

            this.bd.processJob();
            Thread.sleep(SQS_SLEEP_TIME);
        }

        job= this.bd.queryJob( jobUuid, authentication);
        assert JobState.Complete== job.getState():
                "Job processing incorrect.";
        assert 1== job.getTries():
                "Job processing incorrect.";

        object= this.bd.queryMetaData( key, "count", authentication);
        this.assertCount(object);

        // Initial message should have made 6 more.
        for( int i= 0; i< 12; i++){

            this.bd.processJob();
            Thread.sleep( SQS_SLEEP_TIME);
        }
        for( int i= 1; i< word.length(); i++){

            key= this.makeKey( word.substring( 0, i));
            object= this.bd.queryMetaData( key, "count", authentication);
            this.assertCount(object);
        }
    }

    @Test
    public void testPollJob(){

        this.bd.processJob();
    }

    @Test( expected = AssertionError.class)
    public void testUnregistered(){

        String word= "testing";
        String key= this.makeKey( word);
        String authentication= "test-authentication";
        UUID jobUuid1= UUID.randomUUID();
        UUID jobUuid2= UUID.randomUUID();
        this.bd.addMessage( jobUuid1, key, BEHAVIOR, BehaviorType.Persist.toString(),
                authentication);
        this.bd.addMessage( jobUuid2, key, BEHAVIOR, BehaviorType.Persist.toString(),
                authentication);
    }

    @Test
    public void testDoubleTap() throws InterruptedException {

        this.bd.register( "test-authentication", "wordoink");

        String word= "testing";
        String key= this.makeKey( word);
        String authentication= "test-authentication";
        UUID jobUuid1= UUID.randomUUID();
        UUID jobUuid2= UUID.randomUUID();
        this.bd.addMessage( jobUuid1, key, BEHAVIOR,
                BehaviorType.Persist.toString(), authentication);
        this.bd.addMessage( jobUuid2, key, BEHAVIOR,
                BehaviorType.Persist.toString(), authentication);
        for( int i= 0; i< 4; i++){

            Thread.sleep( SQS_SLEEP_TIME);
            this.bd.processJob();
        }

        Integer count= ((Double) Double.parseDouble(this.bd.queryMetaData( key,
                "count", authentication))).intValue();
        assert count.equals( 2): "Count is not incrementing.";

        // tf - Check on spawned jobs.
        for( int i= 0; i< 28; i++){

            Thread.sleep( SQS_SLEEP_TIME);
            this.bd.processJob();
        }
        count= ((Double) Double.parseDouble(this.bd.queryMetaData( makeKey(
                "tes"), "count", authentication))).intValue();
        assert count.equals( 2): "Count is not incrementing.";
    }

    @Test
    public void testAuthUseCase(){

        // tf - Register a context owner and create some messages.
        String ownerAuthentication= "test-ownerAuthentication";
        String userContext= "test-userContext";
        this.bd.register( ownerAuthentication, userContext);

        UUID jobUuid1= UUID.randomUUID();
        UUID jobUuid2= UUID.randomUUID();
        this.bd.addMessage( jobUuid1, makeKey( userContext, "message01"),
                BEHAVIOR, BehaviorType.Persist.toString(), ownerAuthentication);
        this.bd.addMessage( jobUuid2, makeKey( userContext, "message02"),
                BEHAVIOR, BehaviorType.Persist.toString(), ownerAuthentication,
                true);

        // tf - Access messages as an unregistered user.
        String firstVisitor= "test-firstVisitorAuthentication";
        this.bd.queryMetaData( makeKey( userContext, "message01"), "count",
                firstVisitor);

        // tf - Access secured messages as an unregistered user.
        try{

            this.bd.queryMetaData( makeKey( userContext, "message02"), "count",
                    firstVisitor);
            assert false: "Unregistered user can access secure messages.";
        }catch( IllegalAccessError e){

            // Noop.
        }

        // tf - Attempt to modify and delete messages as an unregistered user.
        try{

            this.bd.addMessage( UUID.randomUUID(), makeKey( userContext,
                    "message01"), BEHAVIOR,
                    BehaviorType.Persist.toString(), firstVisitor);
            assert false: "Unregistered user can access secure messages.";
        }catch( IllegalAccessError e){

            // Noop.
        }
        try{

            this.bd.addMessage(UUID.randomUUID(), makeKey(userContext,
                    "message01"), BEHAVIOR,
                    BehaviorType.Delete.toString(), firstVisitor);
            assert false: "Unregistered user can access secure messages.";
        }catch( IllegalAccessError e){

            // Noop.
        }

        // tf - Create messages as unregistered, and attempt to modify & delete.
        this.bd.addMessage( UUID.randomUUID(), makeKey( userContext,
                "message03"), BEHAVIOR,
                BehaviorType.Persist.toString(), firstVisitor);
        this.bd.queryMetaData( makeKey( userContext, "message03"), "count",
                firstVisitor);
        try{

            this.bd.addMessage( UUID.randomUUID(), makeKey( userContext,
                    "message03"), BEHAVIOR,
                    BehaviorType.Persist.toString(), firstVisitor);
            assert false: "Unregistered user can access secure messages.";
        }catch( IllegalAccessError e){

            // Noop.
        }
        try{

            this.bd.addMessage( UUID.randomUUID(), makeKey( userContext,
                    "message03"), BEHAVIOR,
                    BehaviorType.Delete.toString(), firstVisitor);
            assert false: "Unregistered user can access secure messages.";
        }catch( IllegalAccessError e){

            // Noop.
        }

        this.bd.addMessage( UUID.randomUUID(), makeKey( userContext,
                "message04"), BEHAVIOR,
                BehaviorType.Persist.toString(), firstVisitor, true);
        this.bd.queryMetaData(makeKey(userContext, "message04"), "count",
                firstVisitor);
        try{

            this.bd.addMessage( UUID.randomUUID(), makeKey( userContext,
                    "message04"), BEHAVIOR,
                    BehaviorType.Persist.toString(), firstVisitor);
            assert false: "Unregistered user can access secure messages.";
        }catch( IllegalAccessError e){

            // Noop.
        }
        try{

            this.bd.addMessage( UUID.randomUUID(), makeKey( userContext,
                    "message04"), BEHAVIOR,
                    BehaviorType.Delete.toString(), firstVisitor);
            assert false: "Unregistered user can access secure messages.";
        }catch( IllegalAccessError e){

            // Noop.
        }

        // tf - Access non-secured and attempt secured messages as third user.
        String secondVisitor= "test-secondVisitorAuthentication";
        this.bd.queryMetaData( makeKey( userContext, "message03"), "count",
                secondVisitor);
        try{

            this.bd.queryMetaData(makeKey(userContext, "message04"), "count",
                    secondVisitor);
            assert false: "Unregistered user can access secure messages.";
        }catch( IllegalAccessError e){

            // Noop.
        }

        // tf - Register second user, modify & delete.
        this.bd.register( firstVisitor, null);
        this.bd.addMessage( UUID.randomUUID(), makeKey( userContext,
                "message03"), BEHAVIOR,
                BehaviorType.Persist.toString(), firstVisitor);
        this.bd.addMessage( UUID.randomUUID(), makeKey( userContext,
                "message03"), BEHAVIOR,
                BehaviorType.Delete.toString(), firstVisitor);
        this.bd.addMessage( UUID.randomUUID(), makeKey( userContext,
                "message04"), BEHAVIOR,
                BehaviorType.Persist.toString(), firstVisitor);
        this.bd.addMessage( UUID.randomUUID(), makeKey( userContext,
                "message04"), BEHAVIOR,
                BehaviorType.Delete.toString(), firstVisitor);

        // tf - Context owner can see all messages created by second user.
        this.bd.queryMetaData( makeKey( userContext, "message03"), "count",
                ownerAuthentication);
        this.bd.queryMetaData( makeKey( userContext, "message04"), "count",
                ownerAuthentication);
        // tf - Modify and delete.
        this.bd.addMessage( UUID.randomUUID(), makeKey( userContext,
                "message03"), BEHAVIOR,
                BehaviorType.Persist.toString(), ownerAuthentication);
        this.bd.addMessage( UUID.randomUUID(), makeKey( userContext,
                "message03"), BEHAVIOR,
                BehaviorType.Delete.toString(), ownerAuthentication);
        this.bd.addMessage( UUID.randomUUID(), makeKey( userContext,
                "message04"), BEHAVIOR,
                BehaviorType.Persist.toString(), ownerAuthentication);
        this.bd.addMessage( UUID.randomUUID(), makeKey( userContext,
                "message04"), BEHAVIOR,
                BehaviorType.Delete.toString(), ownerAuthentication);
    }

    public void assertCount(Object object) {
        assert object!= null:
                "Bd.queryMetaData is not implemented correctly.";
        assert object instanceof String:
                "Bd.queryMetaData is not implemented correctly.";
        assert (object).equals( "1.0"):
                "Bd.queryMetaData is not implemented correctly.";
    }

    @Test
    @Ignore
    public void clean() throws IOException {

        // tf - Clean any artifacts from previous runs.
        new S3Resource().clean();
        this.bd.clear();
    }

    private String makeKey( String word){

        return makeKey( "wordoink", word);
    }

    private String makeKey( String context, String word){

        return String.format( "//s3/%s/%s", context, word);
    }
}

/*
from: http://backendjs.net/bd/data/lifechanger/interactions/smoking/stop/000a3f7f-d6bc-4545-8eba-e6aaef888dd3/2014/02/19/13/10/24/metadata
to:   http://backendjs.net/bd/data/lifechanger/suggestions/%s/latest

function(env,key,meta){
  // input validation.
  if(!key) return false;
  if( key.indexOf('interactions')!=0) return false;
  // get activity metadata.
  var activity= {};
  var pos=key.indexOf('/')+1;
  activity.name= key.substring(pos,key.indexOf('/',pos));
  key= key.substring(pos);
  var pos=key.indexOf('/')+1;
  activity.goal= key.substring(pos,key.indexOf('/',pos));
  if(!meta.activities) meta.activities=[];
  meta.activities.push(activity);
  key= key.substring(key.indexOf('/',pos)+ 1);
  key= key.substring(key.indexOf('/',pos)+ 1);
  // define metadata activity update behavior
  env.updateMetadata= function(env,key,meta){
    var activity= meta.activity;
    if(!meta.activities) meta.activities= [];
    var deduped= [];
    var names= {};
    meta.activities.forEach(function(a){
      if(a.name=== activity.name){
        a.goal= activity.goal;
      }
      if(!names[a.name]){
        deduped.push(a);
        names[a.name]= true;
      }
    });
    meta.activities= deduped;
    if(!names[activity.name]){
      meta.activities.push(activity);
    }
  }
  // decompose path and update suggestions
  var messages= [];
  var activities= {};
  activities.activity=activity;
  do{
    messages.push({'key':'suggestions/'+key+'/latest','meta':activities, persist:env.updateMetadata});
    pos=key.lastIndexOf('/');
    key= key.substring(0,pos);
  }while(pos> -1);
  // update activity
  messages.push({'key':'activities/'+ activity.name+ '/'+ activity.goal+ '/metadata', 'meta':activities, persist:env.updateMetadata});
  // update activities meta data
  messages.push({'key':'activities/metadata','meta':activities, persist:env.updateMetadata});
  return messages;
}

bb097733-0bc7-4003-b7e2-6d8ac064c80b

Job{messageKey=MessageKey{resourceName='s3', userContext='lifechanger', userKey='interactions/smoking/stop/000a3f7f-d6bc-4545-8eba-e6aaef888dd3/2014/02/19/13/10/24', key='//s3/lifechanger/interactions/smoking/stop/000a3f7f-d6bc-4545-8eba-e6aaef888dd3/2014/02/19/13/10/24'}, tries=1, state=Complete} AggregateRoot{uuid=bff99b0f-8333-4fa6-9564-36edbdf3e94f, creationDate=Fri Feb 21 11:27:53 PST 2014, modifiedDate=Fri Feb 21 11:28:58 PST 2014}

 */