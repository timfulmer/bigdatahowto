package info.bigdatahowto.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a resource external to bd code.  This may be an S3
 * bucket, an email server, etc.
 *
 * @author timfulmer
 */
public abstract class Resource {

    private transient Logger logger= Logger.getLogger(
            this.getClass().getName());

    private String name;
    private ObjectMapper objectMapper;

    protected Resource( String name) {

        super();

        this.objectMapper= new ObjectMapper();
        this.name= name;
    }

    /**
     * Identifies a resource instance to bd internals.
     *
     * @return Resource name.
     */
    public String getName() {
        return name;
    }

    /**
     * Communicate information from bd internals out to the external resource.
     *
     * @param key Represents a key to use when communicating to the resource.
     * @param value Data payload to communicate to the resource.
     */
    public abstract void write(String key, String value);

    /**
     * A resource may optionally retrieve information by overriding this
     * method.
     *
     * @param key Identifies data within the external system.
     * @return Data from an external system.
     */
    public String read(String key){

        throw new UnsupportedOperationException( String.format(
                "Class '%s' has not implemented 'Resource.read'.",
                this.getClass().getName()));
    }

    /**
     * Helper method to put an instance to an external resource.
     *
     * @param aggregateRoot Aggregate root to put.
     */
    public void put(AggregateRoot aggregateRoot){

        try {

            String json= this.objectMapper.writeValueAsString( aggregateRoot);
            this.write(aggregateRoot.resourceKey(), json);
        } catch (JsonProcessingException e) {

            String msg= String.format(
                    "Could not serialize class '%s' with state '%s' into JSON.",
                    this.getClass().getName(), this.toString());
            this.logger.log(Level.SEVERE, msg, e);

            throw new RuntimeException( msg, e);
        }
    }

    public <T extends AggregateRoot> T get( String resourceKey, Class<T> clazz){

        String json= this.read( resourceKey);
        if( json== null){

            return null;
        }
        try {

            return this.objectMapper.readValue( json, clazz);
        } catch (IOException e) {

            String msg= String.format( "Could not deserialize '%s' into " +
                    "class '%s'.", json, clazz.getName());
            this.logger.log( Level.SEVERE, msg, e);

            throw new RuntimeException( msg, e);
        }
    }
}
