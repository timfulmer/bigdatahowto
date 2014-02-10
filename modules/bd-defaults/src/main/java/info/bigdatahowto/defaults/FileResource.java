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

    public static final String DEFAULT_DIRECTORY= "/tmp/file-resource";

    private transient Logger logger= Logger.getLogger(
            this.getClass().getName());

    private String directoryName;
    private File directory;

    public FileResource() {

        this( null);
    }

    public FileResource( String directoryName) {

        super( "file");

        if( directoryName== null){

            directoryName= DEFAULT_DIRECTORY;
        }
        this.setDirectoryName( directoryName);
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
        if( !file.exists()){

            return null;
        }
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
