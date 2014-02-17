package info.bigdatahowto.defaults.aws;

import info.bigdatahowto.core.Cache;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author timfulmer
 */
public class ElastiCacheTest {

    private Cache cache;

    @Before
    public void before(){

        this.cache= new ElastiCache();
        this.cache.clear();
    }

    @Test
    public void testElastiCache(){

        String key= "test-key";
        assert this.cache.get( key)== null:
                "ElastiCache.get is not implemented correctly.";

        String value= "test-value";
        assert this.cache.put( key, value):
                "ElastiCache.put is not implemented correctly.";
        assert this.cache.get( key).equals( value):
                "ElastiCache.get is not implemented correctly.";
        assert !this.cache.put( key, value):
                "ElastiCache.put is not implemented correctly.";

        this.cache.remove( key);
        assert this.cache.get( key)== null:
                "ElastiCache.remove is not implemented correctly.";

        this.cache.put( key, value);
        this.cache.clear();
        assert this.cache.get( key)== null:
                "ElastiCache.clear is not implemented correctly.";
    }
}
