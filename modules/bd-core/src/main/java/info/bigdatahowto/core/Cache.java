package info.bigdatahowto.core;

/**
 * Represents caching to the rest of the system.
 *
 * @author timfulmer
 */
public interface Cache {

    /**
     * Puts a key-value pair into cache.
     *
     * @param key      Key to put.
     * @param value    Value to put.
     * @return         True if the cache did not already contain the key.
     */
    boolean put( String key, String value);

    /**
     * Gets a value for a key from cache, returns null if not found.
     *
     * @param key    Key to get.
     * @return  Value if present, null otherwise.
     */
    String get( String key);

    /**
     * Removes a key-value pair from cache;
     *
     * @param key    Key to remove.
     */
    void remove(String key);

    /**
     * Removes all elements from cache.
     */
    void clear();
}
