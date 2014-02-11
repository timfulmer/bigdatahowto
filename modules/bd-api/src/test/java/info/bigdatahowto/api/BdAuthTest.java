package info.bigdatahowto.api;

import info.bigdatahowto.core.BehaviorType;
import info.bigdatahowto.core.Job;
import info.bigdatahowto.core.JobState;
import info.bigdatahowto.defaults.BdAuthenticator;
import info.bigdatahowto.defaults.FileResource;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author timfulmer
 */
public class BdAuthTest {

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

    private Bd bd;

    @Before
    public void before() throws IOException {

        this.bd= new Bd();
        this.bd.setAuthenticator( new BdAuthenticator( this.bd.getUserRoadie()));
        this.clean();
    }

    @After
    public void after() throws IOException {

        this.clean();
    }

    @Test
    public void testBd(){

        this.bd.register( "test-authentication", "wordoink");

        String word= "testing";
        String key= this.makeKey( word);
        String authentication= "test-authentication";
        UUID jobUuid= this.bd.addMessage( key, BEHAVIOR,
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
        assert object== null:
                "Bd.queryMetaData is not implemented correctly.";
        for( int i= 1; i< word.length()- 1; i++){

            object= this.bd.queryMetaData( this.makeKey(
                    word.substring( 0, i)), "count", authentication);
            assert object== null:
                    "Bd.queryMetaData is not implemented correctly.";
        }

        this.bd.processJob();

        job= this.bd.queryJob( jobUuid, authentication);
        assert JobState.Complete== job.getState():
                "Job processing incorrect.";
        assert 1== job.getTries():
                "Job processing incorrect.";

        object= this.bd.queryMetaData( key, "count", authentication);
        this.assertCount(object);

        // Initial message should have made 6 more.
        for( int i= 0; i< 6; i++){

            this.bd.processJob();
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
        this.bd.addMessage( key, BEHAVIOR, BehaviorType.Persist.toString(),
                authentication);
    }

    @Test
    public void testDoubleTap(){

        this.bd.register( "test-authentication", "wordoink");

        String word= "testing";
        String key= this.makeKey( word);
        String authentication= "test-authentication";
        this.bd.addMessage( key, BEHAVIOR, BehaviorType.Persist.toString(),
                authentication);
        this.bd.addMessage( key, BEHAVIOR, BehaviorType.Persist.toString(),
                authentication);
        this.bd.processJob();
        this.bd.processJob();

        Integer count= ((Double) this.bd.queryMetaData( key, "count",
                authentication)).intValue();
        assert count.equals( 2): "Count is not incrementing.";

        // tf - Check on spawned jobs.
        for( int i= 0; i< 12; i++){

            this.bd.processJob();
        }

        count= ((Double) this.bd.queryMetaData( makeKey( "tes"),
                "count", authentication)).intValue();
        assert count.equals( 2): "Count is not incrementing.";
    }

    @Test
    public void testAuthUseCase(){

        // tf - Register a context owner and create some messages.
        String ownerAuthentication= "test-ownerAuthentication";
        String userContext= "test-userContext";
        this.bd.register( ownerAuthentication, userContext);

        this.bd.addMessage( makeKey( userContext, "message01"), BEHAVIOR,
                BehaviorType.Persist.toString(), ownerAuthentication);
        this.bd.addMessage( makeKey( userContext, "message02"), BEHAVIOR,
                BehaviorType.Persist.toString(), ownerAuthentication, true);

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

            this.bd.addMessage( makeKey( userContext, "message01"), BEHAVIOR,
                    BehaviorType.Persist.toString(), firstVisitor);
            assert false: "Unregistered user can access secure messages.";
        }catch( IllegalAccessError e){

            // Noop.
        }
        try{

            this.bd.addMessage(makeKey(userContext, "message01"), BEHAVIOR,
                    BehaviorType.Delete.toString(), firstVisitor);
            assert false: "Unregistered user can access secure messages.";
        }catch( IllegalAccessError e){

            // Noop.
        }

        // tf - Create messages as unregistered, and attempt to modify & delete.
        this.bd.addMessage( makeKey( userContext, "message03"), BEHAVIOR,
                BehaviorType.Persist.toString(), firstVisitor);
        this.bd.queryMetaData( makeKey( userContext, "message03"), "count",
                firstVisitor);
        try{

            this.bd.addMessage( makeKey( userContext, "message03"), BEHAVIOR,
                    BehaviorType.Persist.toString(), firstVisitor);
            assert false: "Unregistered user can access secure messages.";
        }catch( IllegalAccessError e){

            // Noop.
        }
        try{

            this.bd.addMessage( makeKey( userContext, "message03"), BEHAVIOR,
                    BehaviorType.Delete.toString(), firstVisitor);
            assert false: "Unregistered user can access secure messages.";
        }catch( IllegalAccessError e){

            // Noop.
        }

        this.bd.addMessage( makeKey( userContext, "message04"), BEHAVIOR,
                BehaviorType.Persist.toString(), firstVisitor, true);
        this.bd.queryMetaData(makeKey(userContext, "message04"), "count",
                firstVisitor);
        try{

            this.bd.addMessage( makeKey( userContext, "message04"), BEHAVIOR,
                    BehaviorType.Persist.toString(), firstVisitor);
            assert false: "Unregistered user can access secure messages.";
        }catch( IllegalAccessError e){

            // Noop.
        }
        try{

            this.bd.addMessage( makeKey( userContext, "message04"), BEHAVIOR,
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
        this.bd.addMessage( makeKey( userContext, "message03"), BEHAVIOR,
                BehaviorType.Persist.toString(), firstVisitor);
        this.bd.addMessage( makeKey( userContext, "message03"), BEHAVIOR,
                BehaviorType.Delete.toString(), firstVisitor);
        this.bd.addMessage( makeKey( userContext, "message04"), BEHAVIOR,
                BehaviorType.Persist.toString(), firstVisitor);
        this.bd.addMessage( makeKey( userContext, "message04"), BEHAVIOR,
                BehaviorType.Delete.toString(), firstVisitor);

        // tf - Context owner can see all messages created by second user.
        this.bd.queryMetaData( makeKey( userContext, "message03"), "count",
                ownerAuthentication);
        this.bd.queryMetaData( makeKey( userContext, "message04"), "count",
                ownerAuthentication);
        // tf - Modify and delete.
        this.bd.addMessage( makeKey( userContext, "message03"), BEHAVIOR,
                BehaviorType.Persist.toString(), ownerAuthentication);
        this.bd.addMessage( makeKey( userContext, "message03"), BEHAVIOR,
                BehaviorType.Delete.toString(), ownerAuthentication);
        this.bd.addMessage( makeKey( userContext, "message04"), BEHAVIOR,
                BehaviorType.Persist.toString(), ownerAuthentication);
        this.bd.addMessage( makeKey( userContext, "message04"), BEHAVIOR,
                BehaviorType.Delete.toString(), ownerAuthentication);
    }

    public void assertCount(Object object) {
        assert object!= null:
                "Bd.queryMetaData is not implemented correctly.";
        assert object instanceof Double:
                "Bd.queryMetaData is not implemented correctly.";
        assert (object).equals( 1.0):
                "Bd.queryMetaData is not implemented correctly.";
    }

    private void clean() throws IOException {

        // tf - Clean any files from previous runs.
        File directory= new File(FileResource.DEFAULT_DIRECTORY);
        if( directory.exists()){

            FileUtils.cleanDirectory(directory);
        }
    }

    private String makeKey( String word){

        return makeKey( "wordoink", word);
    }

    private String makeKey( String context, String word){

        return String.format( "//file/%s/%s", context, word);
    }
}