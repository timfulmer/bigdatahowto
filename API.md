#Big Data Howto

A refreshingly technology independent view of Big Data.

In this fifth installment we wrap up our library development with a simple
implementation of bd-api.  Initially bd-api wraps up the defaults defined
earlier without additional configuration.  This is to get to an end to end,
system-wide test case as quickly as possible.

##Bd

The Bd class is the central API facade.  We've implemented four methods matching
the initial four services identified in the use case, using the bd-core and
bd-default artifacts:

```
package info.bigdatahowto.api;

import info.bigdatahowto.core.Job;
import info.bigdatahowto.core.JobState;
import info.bigdatahowto.defaults.FileResource;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author timfulmer
 */
public class BdTest {

    private static final String BEHAVIOR= "function(env,word,meta){\n" +
            "        // Input validation.\n" +
            "        if(!word || word.length>7) return false;\n" +
            "        // Define stem behavior.\n" +
            "        env.persistFunction= function(env,word,meta){\n" +
            "            // Update count returned from the GET request.\n" +
            "            if(!meta.count) meta.count= 0;\n" +
            "            meta.count= meta.count+ 1;\n" +
            "            return true;\n" +
            "        }\n" +
            "        // Set meta data for this word.\n" +
            "        if(!meta.count) meta.count= 0;\n" +
            "        meta.count= meta.count+ 1;\n" +
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

        // tf - Clean any files from previous runs.
        File directory= new File(FileResource.DEFAULT_DIRECTORY);
        if( directory.exists()){

            FileUtils.cleanDirectory( directory);
        }
    }

    @Test
    public void testBd(){

        String word= "testing";
        String key= this.makeKey( word);
        String authentication= "test-authentication";
        UUID jobUuid= this.bd.addMessage( key, new HashMap(), BEHAVIOR,
                "persist", new HashMap<String,String>(), authentication);
        assert jobUuid!= null:
                "Bd.addMessage is not implemented correctly.";

        Job job= this.bd.queryJob( jobUuid);
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

    public void assertCount(Object object) {
        assert object!= null:
                "Bd.queryMetaData is not implemented correctly.";
        assert object instanceof Double:
                "Bd.queryMetaData is not implemented correctly.";
        assert (object).equals( 1.0):
                "Bd.queryMetaData is not implemented correctly.";
    }

    private String makeKey( String word){

        return String.format( "//file/wordoink/%s", word);
    }
}
```

##BdTest

Next we build out a test case implementing the use case identified earlier:

```
package info.bigdatahowto.api;

import info.bigdatahowto.core.Job;
import info.bigdatahowto.core.JobState;
import info.bigdatahowto.defaults.FileResource;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author timfulmer
 */
public class BdTest {

    private static final String BEHAVIOR= "function(env,word,meta){\n" +
            "        // Input validation.\n" +
            "        if(!word || word.length>7) return false;\n" +
            "        // Define stem behavior.\n" +
            "        env.persistFunction= function(env,word,meta){\n" +
            "            // Update count returned from the GET request.\n" +
            "            if(!meta.count) meta.count= 0;\n" +
            "            meta.count= meta.count+ 1;\n" +
            "            return true;\n" +
            "        }\n" +
            "        // Set meta data for this word.\n" +
            "        if(!meta.count) meta.count= 0;\n" +
            "        meta.count= meta.count+ 1;\n" +
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
        File directory= new File(FileResource.DEFAULT_DIRECTORY);
        if( directory.exists()){

            FileUtils.cleanDirectory( directory);
        }
    }

    @Test
    public void testBd(){

        String word= "testing";
        String key= this.makeKey( word);
        String authentication= "test-authentication";
        UUID jobUuid= this.bd.addMessage( key, new HashMap(), BEHAVIOR,
                "persist", new HashMap<String,String>(), authentication);
        assert jobUuid!= null:
                "Bd.addMessage is not implemented correctly.";

        Job job= this.bd.queryJob( jobUuid);
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

    public void assertCount(Object object) {
        assert object!= null:
                "Bd.queryMetaData is not implemented correctly.";
        assert object instanceof Double:
                "Bd.queryMetaData is not implemented correctly.";
        assert (object).equals( 1.0):
                "Bd.queryMetaData is not implemented correctly.";
    }

    private String makeKey( String word){

        return String.format( "//file/wordoink/%s", word);
    }
}
```

The major gotcha here was realizing the simple implementation of InMemoryQueue
would not work for the case of multiple jobs in queue.  InMemoryQueue got
changed to use two queues, a head and a tail, to get the behavior we're looking
for.  Please note InMemoryQueue has not been tested in a multi-threaded, heavily
concurrent environment.

##JavaScript Glue Code

We also found out our JavaScript glue code was lacking in many result outcomes.
It's been enhanced to a few flavors of:

```
    function parseResults(result){
      var persist;
      if(result.persist) persist=result.persist.toString();
      var meta= result.meta;
      if(persist && meta){
        processingResult.addMessage(result.key,result.meta,persist);
      }else if(persist){
        processingResult.addMessage(result.key,persist);
      }else if(meta){
        processingResult.addMessage(result.key,result.meta);
      }else{
        processingResult.addMessage(result.key);
      }
    }
    var env= {};
    var meta= {};
    // inject meta
    %s
    // inject behavior function
    var results= %s(env,key,meta);
    if( !results){
      processingResult.continueProcessing= false;
    }else if( toString.call(results) === '[object Array]'){
      results.forEach(function(result){
        parseResults(result);
      });
    }
```

An interface has been created to encapsulate the different flavors of JS glue
code:

```
package info.bigdatahowto.defaults.js;

/**
 * Interface defining templates used by JavaScriptProcessor to interface with
 * JavaScript behavior functions.
 *
 * @author timfulmer
 */
public interface JavaScriptProcessorTemplate {
    String processWithMeta();
    String processWithoutMeta();
    String errorWithMeta();
    String errorWithoutMeta();
}
```

##Bd-Api Wrapup

There were a lot of little changes to both bd-core and bd-defaults modules to
get bd-api working.  Except for the major queue re-work, it was all handling
null and error cases, with some extra helper methods to make classes easier to
use.

Next up we'll dive right into wiring bd-api into a Play runtime, and start
setting up performance benchmarks to guide us through the rest of development.
Remember: Big Data is all about performance.