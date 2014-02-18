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

        this.bd.addMessage( jobUuid, key, BEHAVIOR, BehaviorType.Delete.toString(),
                authentication);
        this.bd.processJob();
        object= this.bd.queryMetaData( key, "count", authentication);
        assert object== null:
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
                BehaviorType.Persist.toString(), authentication);
        this.bd.addMessage( jobUuid2, key, BEHAVIOR,
                BehaviorType.Persist.toString(), authentication);
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
