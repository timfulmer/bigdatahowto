#Big Data Howto

A refreshingly technology independent view of Big Data.

In this fourth installment we drill down on a quick implementation of
bd-defaults.
The focus of this one is on getting a system up and running; we'll be a little
fast and loose with less emphasis on test case coverage for the first
iterations.  Woohoo!

##JavaScriptProcessor

Probably the most interesting piece, let's take a look at implementing the
bd-core `Processor` concept with JavaScript.  Java provides a `ScriptEngine`
interface to run scripts, with a default implementation for JavaScript based
on Rhino.  We'll
need a little JavaScript glue code to fold in the behavior from the `Message`:

```
function parseResults(result){
  var persist;
  if(result.persist) persist=result.persist.toString();
  processingResult.addMessage(result.key,result.meta,persist);
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

You'll notice a few gotcha's here.  First off we're using `String.format` to
inject meta data from `Message.values`.  Turns out going between a Java `Map`
and a JavaScript `Object` is not the easiest thing in the world using Rhino.
We're also injecting the behavior function from the `Message` instance in the
same way, and execute it using
the () operator in `var results= %s(env,key,meta);`.

Instantiating a Java object from JavaScript presents some
challenges as well.  A quick helper method on `ProcessingResult` instantiates
new
`Message` instances based on the JavaScript objects created in the injected
behavior function.

Sprinkle in a little error handling, and the Processor.process method
implementation looks something like:

```
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        bindings.put( "key", message.getMessageKey().getKey());
        ProcessingResult processingResult= new ProcessingResult();
        bindings.put( "processingResult", processingResult);
        StringWriter errorWriter= new StringWriter();
        engine.getContext().setErrorWriter( errorWriter);
        try {

            engine.eval(script, bindings);
        } catch (ScriptException e) {

            String msg = String.format("Could not evaluate JS script '%s', " +
                    "returned error '%s'.", script, errorWriter.toString());
            this.logger.log(Level.SEVERE, msg, e);

            throw new RuntimeException(msg, e);
        }
        //noinspection unchecked
        message.setValues( new HashMap( (Map)bindings.get( "meta")));
        processingResult.setMessage( message);

        return processingResult;
```

A little refactoring gives us the `Processor.error` implementation as well:

```
        String script= composeScript(message,
                this.javaScriptProcessorTemplate.errorWithMeta(),
                this.javaScriptProcessorTemplate.errorWithoutMeta(),
                message.getBehavior().get("error"));
        Bindings bindings= new SimpleBindings();
        bindings.put( "tries", tries);

        return this.getProcessingResult(message, script, bindings);
```

##InMemoryQueue

We're implementing `Queue` with an in-memory `ConcurrentLinkedQueue` at first,
to limit
the number of integration points tackled at once
and ease development efforts.  Not too much to talk about,
`ConcurrentLinkedQueue` was chosen for concurrency and simplicity.  A
`BlockingQueue` implementation was considered and discarded, since non-blocking
behavior seems more fitting to the use case.

```
package info.bigdatahowto.defaults;

import info.bigdatahowto.core.Queue;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple in-memory queue, backed by a LinkedList, for testing only.
 *
 * @author timfulmer
 */
public class InMemoryQueue extends Queue {

    private transient Logger logger= Logger.getLogger(
            this.getClass().getName());

    private java.util.Queue<UUID> queue;

    public InMemoryQueue() {

        super();

        this.queue= new ConcurrentLinkedQueue<>();
    }

    /**
     * Writes a UUID into the underlying queue.
     *
     * @param uuid UUID to queue.
     */
    @Override
    protected void write(UUID uuid) {

        if( !this.queue.offer(uuid)){

            String msg= String.format( "Could not put UUID '%s' into queue " +
                    "of type '%s'.", uuid.toString(),
                    this.queue.getClass().getName());
            this.logger.log(Level.SEVERE, msg);

            throw new RuntimeException( msg);
        }
    }

    /**
     * Reads a  UUID from the underlying queue.  Reading must not delete
     * from the underlying queue.  Repeated calls to read may return the same
     * UUID.
     *
     * @return UUID in the queue.
     */
    @Override
    protected UUID read() {

        return this.queue.peek();
    }

    /**
     * Deletes a uuid from the queue.
     *
     * @param uuid Uuid to delete.
     */
    @Override
    protected void delete(UUID uuid) {

        if( !this.queue.remove( uuid)){

            String msg= String.format( "Could not remove UUID '%s' from " +
                    "queue of type '%s'.", uuid.toString(),
                    this.queue.getClass().getName());
            this.logger.log(Level.SEVERE, msg);

            throw new RuntimeException( msg);
        }
    }
}
```

##FileResource

Again, fairly straight forward.  We're using a file system at first to keep
things simple and get a baseline.

```
package info.bigdatahowto.defaults;

import info.bigdatahowto.core.Resource;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple file system backed resource.
 *
 * @author timfulmer
 */
public class FileResource extends Resource {

    private transient Logger logger= Logger.getLogger(
            this.getClass().getName());

    private String directoryName= "file-resource";
    private File directory;

    public FileResource() {

        super();
    }

    /**
     * Communicate information from bd internals out to the external resource.
     *
     * @param key   Represents a key to use when communicating to the resource.
     * @param value Data payload to communicate to the resource.
     */
    @Override
    public void write(String key, String value) {

        File file= this.getFile( key);
        try {

            FileUtils.writeStringToFile( file, value,
                    StandardCharsets.UTF_8.displayName());
        } catch (IOException e) {

            String msg = String.format("Could not write value '%s' with key " +
                    "'%s' to file '%s'.", value, key, file.getAbsolutePath());
            this.logger.log(Level.SEVERE, msg, e);

            throw new RuntimeException(msg, e);
        }
    }

    /**
     * A resource may optionally retrieve information by overriding this
     * method.
     *
     * @param key Identifies data within the external system.
     * @return Data from an external system.
     */
    @Override
    public String read(String key) {

        File file= this.getFile( key);
        try {

            return FileUtils.readFileToString(file,
                    StandardCharsets.UTF_8.displayName());
        } catch (IOException e) {

            String msg = String.format("Could not read value with key '%s' " +
                    "from file '%s'.", key, file.getAbsolutePath());
            this.logger.log(Level.SEVERE, msg, e);

            throw new RuntimeException(msg, e);
        }
    }

    private File getDirectory(){

        if( this.directory== null){

            this.directory= new File( this.directoryName);
        }

        return this.directory;
    }

    private File getFile( String key){

        return new File(this.getDirectory(), key);
    }

    public void setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
    }
}
```

##AlwaysAllowAuthenticator

Probably the least interesting class on the system so far:

```
package info.bigdatahowto.defaults;

import info.bigdatahowto.core.Authenticator;

/**
 * Implements Authenticator with an always allow policy.
 *
 * @author timfulmer
 */
public class AlwaysAllowAuthenticator implements Authenticator {

    /**
     * Authenticates access to a resource identified by a key.
     *
     * @param key            Key identifying a resource.
     * @param authentication Authentication identifying a user.
     * @return True if the access to the resource is granted.
     */
    @Override
    public boolean authorize(String key, String authentication) {

        return true;
    }

    /**
     * Provisions an authentication for access to a key.
     *
     * @param key            Key identifying a resource.
     * @param authentication Authentication identifying a user.
     */
    @Override
    public void provision(String key, String authentication) {

        // Noop.
    }
}
```
