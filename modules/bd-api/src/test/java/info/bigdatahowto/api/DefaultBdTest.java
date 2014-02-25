package info.bigdatahowto.api;

import info.bigdatahowto.core.BehaviorType;
import info.bigdatahowto.core.Job;
import info.bigdatahowto.core.JobState;
import info.bigdatahowto.defaults.FileResource;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author timfulmer
 */
public class DefaultBdTest {

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

    private Bd bd= Bd.defaultInstance( null);

    @Before
    public void before() throws IOException {

        this.bd.clear();
        this.clean();
    }

    @After
    public void after() throws IOException {

        this.clean();
    }

    @Test
    public void testBd(){

        String word= "testing";
        String key= this.makeKey( word);
        String authentication= "test-authentication";
        UUID jobUuid= UUID.randomUUID();
        this.bd.addMessage( jobUuid, key, BEHAVIOR,
                BehaviorType.Persist.toString(), new HashMap<String,String>( 0),
                authentication);
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

        this.bd.addMessage( jobUuid, key, BEHAVIOR,
                BehaviorType.Delete.toString(), new HashMap<String,String>( 0),
                authentication);
        this.bd.processJob();
        object= this.bd.queryMetaData( key, "count", authentication);
        assert "{}".equals( object):
                "BehaviorType.Delete is not implemented correctly.";
    }

    @Test
    public void testPollJob(){

        this.bd.processJob();
    }
    @Test
    public void testQueryJob(){

        this.bd.queryJob( UUID.randomUUID(), "test-authentication");
    }

    @Test
    public void testDoubleTap(){

        String word= "testing";
        String key= this.makeKey( word);
        String authentication= "test-authentication";
        UUID jobUuid1= UUID.randomUUID();
        UUID jobUuid2= UUID.randomUUID();
        this.bd.addMessage( jobUuid1, key, BEHAVIOR,
                BehaviorType.Persist.toString(), new HashMap<String,String>( 0),
                authentication);
        this.bd.addMessage( jobUuid2, key, BEHAVIOR,
                BehaviorType.Persist.toString(), new HashMap<String,String>( 0),
                authentication);
        this.bd.processJob();
        this.bd.processJob();

        Integer count= ((Double)Double.parseDouble(this.bd.queryMetaData( key,
                "count", authentication))).intValue();
        assert count.equals( 2): "Count is not incrementing.";

        // tf - Check on spawned jobs.
        for( int i= 0; i< 12; i++){

            this.bd.processJob();
        }

        count= ((Double) Double.parseDouble(this.bd.queryMetaData(
                makeKey( "tes"), "count", authentication))).intValue();
        assert count.equals( 2): "Count is not incrementing.";
    }

    @Test
    public void testMetaListMod(){

        String behavior= "function(env,key,meta){meta.list=[];meta.list.push('one');meta.list.push('two');return true;}";
        String key= this.makeKey( "testing");
        String authentication= "test-authentication";
        UUID jobUuid= UUID.randomUUID();
        this.bd.addMessage( jobUuid, key, behavior,
                BehaviorType.Persist.toString(), new HashMap<String,String>( 0),
                authentication);
        this.bd.processJob();
        String result= this.bd.queryMetaData( key, "list", authentication);
        assert result!= null: "List is not behaving.";
        assert result.equals( "[\"one\",\"two\"]"): "List is not behaving.";

        behavior= "function(env,key,meta){meta.list=[];meta.list.push('three');return true;}";
        jobUuid= UUID.randomUUID();
        this.bd.addMessage( jobUuid, key, behavior,
                BehaviorType.Persist.toString(), new HashMap<String,String>( 0),
                authentication);
        this.bd.processJob();
        result= this.bd.queryMetaData( key, "list", authentication);
        assert result!= null: "List is not behaving.";
        assert result.equals( "[\"three\"]"): "List is not behaving.";
    }

    @Test
    public void testErrors(){

        String behavior= "function(env,key,meta){undefined.list=[];}";
        String key= this.makeKey( "testing");
        String authentication= "test-authentication";
        UUID jobUuid= UUID.randomUUID();
        this.bd.addMessage( jobUuid, key, behavior,
                BehaviorType.Persist.toString(), new HashMap<String,String>( 0),
                authentication);
        this.bd.processJob();
        this.bd.processJob();
        this.bd.processJob();
        this.bd.processJob();
        try{
            this.bd.processJob();
            assert false: "Job did not error correctly.";
        }catch( RuntimeException e){

            // Noop.
        }

        Job job= this.bd.queryJob( jobUuid, authentication);
        assert job!= null: "Query job returned null.";
        assert job.getStatus()!= null: "Errored job has null status.";
    }

    @Test
    public void testNullGet(){

        String result= this.bd.queryMetaData( this.makeKey( "dummy-word"),
                "value", "test-authentication");
        assert result!= null: "Bd.queryMetaData handling null incorrectly.";
        assert "{}".equals( result): "Bd.queryMetaData handling null incorrectly.";
    }

    @Test
    public void testInputValues(){

        String behavior= "function(env,key,meta){meta.testString.indexOf('/');}";
        String key= this.makeKey( "testing");
        String authentication= "test-authentication";
        UUID jobUuid= UUID.randomUUID();
        Map<String,String> params= new HashMap<>( 1);
        params.put( "testString", "testString");
        this.bd.addMessage( jobUuid, key, behavior,
                BehaviorType.Persist.toString(), params,
                authentication);
        this.bd.processJob();
        Job job= this.bd.queryJob( jobUuid, authentication);
        assert job!= null:
                "Bd.addMessage is not handling input parameters correctly.";
        assert job.getState()== JobState.Complete:
                "Bd.addMessage is not handling input parameters correctly.";
    }

    @Test
    public void testInputList(){

        String behavior= "function(env,key,meta){meta.testString.indexOf('/');}";
        String key= this.makeKey( "testing");
        String authentication= "test-authentication";
        UUID jobUuid= UUID.randomUUID();
        Map<String,String> params= new HashMap<>( 1);
        String valueString= "[{testString:'testValue'}," +
                "{testString:'testValue'}]";
        params.put( "testString", valueString);
        this.bd.addMessage( jobUuid, key, behavior,
                BehaviorType.Persist.toString(), params,
                authentication);
        this.bd.processJob();
        Job job= this.bd.queryJob( jobUuid, authentication);
        assert job!= null:
                "Bd.addMessage is not handling input parameters correctly.";
        assert job.getState()== JobState.Complete:
                "Bd.addMessage is not handling input parameters correctly.";
        String result= this.bd.queryMetaData( key, "testString",
                authentication);
        assert result!= null: "Metadata list is not implemented correctly.";
        assert result.equals( "\""+ valueString+ "\""):
                "Metadata list is not implemented correctly.";

        // tf - do it again to test initialized case
        jobUuid= UUID.randomUUID();
        this.bd.addMessage( jobUuid, key, behavior,
                BehaviorType.Persist.toString(), params,
                authentication);
        this.bd.processJob();
        job= this.bd.queryJob( jobUuid, authentication);
        assert job!= null:
                "Bd.addMessage is not handling input parameters correctly.";
        assert job.getState()== JobState.Complete:
                "Bd.addMessage is not handling input parameters correctly.";
        result= this.bd.queryMetaData( key, "testString",
                authentication);
        assert result!= null: "Metadata list is not implemented correctly.";
        assert result.equals( "\""+ valueString+ "\""):
                "Metadata list is not implemented correctly.";
    }

    public void assertCount(Object object) {
        assert object!= null:
                "Bd.queryMetaData is not implemented correctly.";
        assert object instanceof String:
                "Bd.queryMetaData is not implemented correctly.";
        assert (object).equals( "1.0"):
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

        return String.format( "//file/wordoink/%s", word);
    }
}
