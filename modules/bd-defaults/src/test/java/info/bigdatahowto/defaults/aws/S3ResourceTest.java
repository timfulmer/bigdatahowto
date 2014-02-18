package info.bigdatahowto.defaults.aws;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author timfulmer
 */
public class S3ResourceTest {

    private static final String KEY= "test-key";

    @Before
    public void before(){

        new S3Resource().clean();
    }

    @Test
    public void testS3Resource(){

        S3Resource s3Resource= new S3Resource();
        String value= "test-value";
        String read= s3Resource.read( KEY);
        assert read== null: "Read a non-existent value.";

        s3Resource.write( KEY, value);
        read= s3Resource.read( KEY);
        assert read!= null: "Could not read value from s3.";
        assert value.equals( read): "Could not read value from s3.";

        s3Resource.remove( KEY);
        read= s3Resource.read( KEY);
        assert read== null: "Read a non-existent value.";
    }

    @Test
    @Ignore
    public void testClean(){

        new S3Resource().clean();
    }
}
