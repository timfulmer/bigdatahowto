package info.bigdatahowto.defaults.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author timfulmer
 */
public class AmazonClient {

    private transient Logger logger= Logger.getLogger(
            this.getClass().getName());

    public AmazonClient() {
        super();
    }

    public AWSCredentials getAwsCredentials(String access, String key) {
        Properties properties= new Properties();
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
        return new BasicAWSCredentials(
                properties.getProperty( access),
                properties.getProperty( key));
    }
}
