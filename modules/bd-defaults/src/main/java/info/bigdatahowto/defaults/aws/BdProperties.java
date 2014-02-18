package info.bigdatahowto.defaults.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Facade for application specific properties.
 *
 * @author timfulmer
 */
public class BdProperties {

    public static final String SYSTEM_PROPERTY= "bdProperties";

    private transient Logger logger= Logger.getLogger(
            this.getClass().getName());

    private Properties properties;

    public BdProperties() {

        super();

        this.properties= new Properties();
        this.loadProperties(getInputStream(System.getProperty(
                SYSTEM_PROPERTY)));
    }

    private InputStream getInputStream(String propertyFile) {

        InputStream inputStream;
        if( !isEmpty( propertyFile)){

            try {

                inputStream= new FileInputStream( propertyFile);
            } catch (FileNotFoundException e) {

                String msg = String.format(
                        "Could not access property file '%s'.", propertyFile);
                this.logger.log(Level.SEVERE, msg, e);

                throw new RuntimeException(msg, e);
            }
        }else{

            inputStream= this.getClass().getResourceAsStream(
                    "/aws.properties");
        }

        return inputStream;
    }

    private void loadProperties(InputStream inputStream) {
        try {

            properties.load( inputStream);
        } catch (IOException e) {

            String msg = String.format(
                    "Could not load properties file from classpath resource " +
                            "'/aws.properties'.");
            this.logger.log(Level.SEVERE, msg, e);

            throw new RuntimeException(msg, e);
        }
    }

    public AWSCredentials getAwsCredentials(String access, String key) {

        return new BasicAWSCredentials(
                properties.getProperty( access),
                properties.getProperty( key));
    }

    public InetSocketAddress getElastiCacheLocation(){

        return new InetSocketAddress( properties.getProperty(
                "aws.elasticache.host"), Integer.parseInt(
                        this.properties.getProperty( "aws.elasticache.port")));
    }

    public String getQueueName() {

        return this.properties.getProperty( "aws.sqs.queueName");
    }

    public String getBucketName() {

        return this.properties.getProperty( "aws.s3.bucketName");
    }
}
