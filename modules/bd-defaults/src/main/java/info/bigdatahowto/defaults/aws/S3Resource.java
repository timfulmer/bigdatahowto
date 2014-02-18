package info.bigdatahowto.defaults.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import info.bigdatahowto.core.Resource;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

/**
 * Resource storing data in AWS s3.
 *
 * @author timfulmer
 */
public class S3Resource extends Resource {

    private static final String DEFAULT_BUCKET= "bd-bucket1-usstandard";

    private transient Logger logger= Logger.getLogger(
            this.getClass().getName());
    private transient AmazonS3 amazonS3;

    private String bucketName;

    public S3Resource() {

        super("s3");

        BdProperties bdProperties= new BdProperties();
        this.amazonS3= new AmazonS3Client(
                bdProperties.getAwsCredentials("aws.s3.accessKeyId",
                        "aws.s3.secretKey"));
        String bucketName= bdProperties.getBucketName();
        if( isEmpty( bucketName)){

            bucketName= DEFAULT_BUCKET;
        }
        if( !this.amazonS3.doesBucketExist( bucketName)){

            this.amazonS3.createBucket( bucketName);
        }
        this.setBucketName( bucketName);
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

        try {

            S3Object s3object = this.amazonS3.getObject(new GetObjectRequest(
                    this.bucketName, key));

            return IOUtils.toString( s3object.getObjectContent());
        } catch (AmazonS3Exception e){

            if( e.getStatusCode()== 404){

                return null;
            }

            String msg = String.format("Could not access '%s' key contents " +
                    "in bucket '%s'.", key, this.bucketName);
            this.logger.log(Level.SEVERE, msg, e);

            throw new RuntimeException(msg, e);
        } catch (IOException e) {

            String msg = String.format("Could not access '%s' key contents " +
                    "in bucket '%s'.", key, this.bucketName);
            this.logger.log(Level.SEVERE, msg, e);

            throw new RuntimeException(msg, e);
        }
    }

    /**
     * Communicate information from bd internals out to the external resource.
     *
     * @param key   Represents a key to use when communicating to the resource.
     * @param value Data payload to communicate to the resource.
     */
    @Override
    public void write(String key, String value) {

        ObjectMetadata objectMetadata= new ObjectMetadata();
        try {

            objectMetadata.setContentLength(
                    value.getBytes(StandardCharsets.UTF_8.toString()).length);
            this.amazonS3.putObject( this.bucketName, key,
                    IOUtils.toInputStream( value), objectMetadata);
        } catch (UnsupportedEncodingException e) {

            String msg = String.format("Could not encode '%s' to UTF-8.",
                    value);
            this.logger.log(Level.SEVERE, msg, e);

            throw new RuntimeException(msg, e);
        }
    }

    /**
     * Remove information from the external resource.
     *
     * @param key Identifies data within the external system.
     */
    @Override
    public boolean remove(String key) {

        this.amazonS3.deleteObject( new DeleteObjectRequest( this.bucketName,
                key));

        return true;
    }

    public void clean(){

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                .withBucketName(bucketName);
        ObjectListing objectListing;
        do {
            List<DeleteObjectsRequest.KeyVersion> keyVersions= new ArrayList<>();
            objectListing = this.amazonS3.listObjects(listObjectsRequest);
            for (S3ObjectSummary objectSummary :
                    objectListing.getObjectSummaries()) {
                keyVersions.add( new DeleteObjectsRequest.KeyVersion(
                        objectSummary.getKey()));
            }
            if( !isEmpty( keyVersions)){

                DeleteObjectsRequest deleteObjectsRequest= new DeleteObjectsRequest(
                        this.bucketName);
                deleteObjectsRequest.setKeys( keyVersions);
                this.amazonS3.deleteObjects( deleteObjectsRequest);
            }
            listObjectsRequest.setMarker(objectListing.getNextMarker());
        } while (objectListing.isTruncated());
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
}
