package info.bigdatahowto.defaults.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author timfulmer
 */
public class AmazonClient {

    private transient Logger logger= Logger.getLogger(
            this.getClass().getName());

    private Properties properties;

    public AmazonClient() {

        super();

        this.properties= new Properties();
        try {

            properties.load( this.getClass().getResourceAsStream(
                    "/aws.properties"));
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
}
