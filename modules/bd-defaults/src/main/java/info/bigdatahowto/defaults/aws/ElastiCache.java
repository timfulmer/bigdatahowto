package info.bigdatahowto.defaults.aws;

import info.bigdatahowto.core.Cache;
import net.spy.memcached.CachedData;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.transcoders.SerializingTranscoder;
import net.spy.memcached.transcoders.Transcoder;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cache implementation using AWS ElastiCache.
 *
 * @author timfulmer
 */
public class ElastiCache implements Cache {

    private transient Logger logger= Logger.getLogger( this.getClass().getName());

    private MemcachedClient memcachedClient;
    private Transcoder<String> transcoder;

    public ElastiCache() {

        super();

        try {

            this.memcachedClient= new MemcachedClient(
                    new BdProperties().getElastiCacheLocation());
            this.transcoder= new Transcoder<String>() {
                private SerializingTranscoder serializingTranscoder=
                        new SerializingTranscoder();

                @Override
                public boolean asyncDecode(CachedData d) {
                    return serializingTranscoder.asyncDecode(d);
                }

                @Override
                public String decode(CachedData d) {
                    return (String)serializingTranscoder.decode(d);
                }

                public CachedData encode(String o) {
                    return serializingTranscoder.encode(o);
                }

                @Override
                public int getMaxSize() {
                    return serializingTranscoder.getMaxSize();
                }
            };
        } catch (IOException e) {

            String msg = String.format("Could not create memcached client.");
            this.logger.log(Level.SEVERE, msg, e);

            throw new RuntimeException(msg, e);
        }
    }

    /**
     * Puts a key-value pair into cache.
     *
     * @param key   Key to put.
     * @param value Value to put.
     * @return True if the cache did not already contain the key.
     */
    @Override
    public boolean put(String key, final String value) {

        try {

            return this.memcachedClient.add( key, 30, value,
                    this.transcoder).get();
        } catch (Exception e) {

            String msg = String.format("Could not put key '%s' with value " +
                    "'%s' into cache.", key, value);
            this.logger.log(Level.SEVERE, msg, e);

            throw new RuntimeException(msg, e);
        }
    }

    /**
     * Gets a value for a key from cache, returns null if not found.
     *
     * @param key Key to get.
     * @return Value if present, null otherwise.
     */
    @Override
    public String get(String key) {
        return this.memcachedClient.get( key, this.transcoder);
    }

    /**
     * Removes a key-value pair from cache;
     *
     * @param key Key to remove.
     */
    @Override
    public void remove(String key) {

        this.memcachedClient.delete( key);
    }

    /**
     * Removes all elements from cache.
     */
    @Override
    public void clear() {

        this.memcachedClient.flush();
    }
}
