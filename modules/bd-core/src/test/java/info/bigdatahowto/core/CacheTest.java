package info.bigdatahowto.core;

import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * @author timfulmer
 */
public class CacheTest {

    @Test
    public void testCache(){

        Cache cache= mock( Cache.class);
        boolean newKey= cache.put( "test-key", "test-value");
        cache.get( "test-key");
        cache.remove( "test-key");
    }
}
