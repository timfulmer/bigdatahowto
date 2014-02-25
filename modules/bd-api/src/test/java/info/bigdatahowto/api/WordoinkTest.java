package info.bigdatahowto.api;

import info.bigdatahowto.core.BehaviorType;
import info.bigdatahowto.defaults.AlwaysAllowAuthenticator;
import info.bigdatahowto.defaults.aws.S3Resource;
import org.junit.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author timfulmer
 */
public class WordoinkTest {

    private static final int SQS_SLEEP_TIME= 250;
    private static final String BEHAVIOR= "function(env,word,meta){\n" +
            "            // Input validation.\n" +
            "            if(!word || word.length>7) return false;\n" +
            "            // Define stem behavior.\n" +
            "            env.persistFunction= function(env,word,meta){\n" +
            "                // Update count returned from the GET request.\n" +
            "                if(!meta.count) meta.count= 0;\n" +
            "                meta.count++;\n" +
            "                // Operation successful, persist results.\n" +
            "                return true;\n" +
            "            }\n" +
            "            // Set meta data for this word.\n" +
            "            if(!meta.count) meta.count= 0;\n" +
            "            meta.count++;\n" +
            "            // Decompose word into stems, run persistFunction on each one.\n" +
            "            var stems= [];\n" +
            "            for(var i=1; i<word.length;i++){\n" +
            "                stems.push({key:word.substring(0,i),\n" +
            "                    persist:env.persistFunction});\n" +
            "            }\n" +
            "            // update latest record.\n" +
            "            var latest= {};\n" +
            "            latest.summary= {};\n" +
            "            latest.summary.word= word;\n" +
            "            latest.summary.count=meta.count;\n" +
            "            stems.push({key:\"latest\",meta:latest});\n" +
            "            return stems;\n" +
            "        }";

    private static Bd bd;

    @BeforeClass
    public static void beforeClass(){

        bd= Bd.productionInstance();
        bd.setAuthenticator( new AlwaysAllowAuthenticator());
    }

    @Before
    public void before() throws IOException {

        this.clean();
    }

    @After
    public void after() throws IOException {

        this.clean();
    }

    @Test
    public void testDoink() throws InterruptedException {

        String word= "testing";
        String key= this.makeKey( "wordoink", word);
        String authentication= "test-authentication";
        UUID jobUuid1= UUID.randomUUID();
        bd.addMessage( jobUuid1, key, BEHAVIOR, BehaviorType.Persist.toString(),
                new HashMap<String,String>( 0), authentication);
        for( int i= 0; i< 16; i++){

            Thread.sleep( SQS_SLEEP_TIME);
            bd.processJob();
        }

        Object results= bd.queryMetaData( this.makeKey( "wordoink", "latest"),
                "summary", authentication);
        assert results!= null: "Bd returning incorrect results.";
        assert "{\"word\":\"testing\",\"count\":1.0}".equals( results.toString()):
                "Bd returning incorrect results.";
    }

    @Test
    @Ignore
    public void clean() throws IOException {

        // tf - Clean any artifacts from previous runs.
        new S3Resource().clean();
        bd.clear();
    }

    private String makeKey( String context, String word){

        return String.format( "//s3/%s/%s", context, word);
    }
}
