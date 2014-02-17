package info.bigdatahowto.defaults;

import info.bigdatahowto.core.Cache;

import java.util.*;

/**
 * Simple in-memory cache implementing a timeout.
 *
 * @author timfulmer
 */
public class InMemoryCache implements Cache {

    private Map<String,KeyTimeout> cache= new HashMap<>();

    /**
     * Puts a key-value pair into cache.
     *
     * @param key   Key to put.
     * @param value Value to put.
     * @return True if the cache did not already contain the key.
     */
    @Override
    public boolean put(String key, String value) {

        return this.cache.put( key, new KeyTimeout( value))== null;
    }

    /**
     * Gets a value for a key from cache, returns null if not found.
     *
     * @param key Key to get.
     * @return Value if present, null otherwise.
     */
    @Override
    public String get(String key) {

        KeyTimeout timeout= this.cache.get(key);
        if( timeout== null || !timeout.valid()){

            this.cache.remove( key);

            return null;
        }

        return timeout.value;
    }

    /**
     * Removes a key-value pair from cache;
     *
     * @param key Key to remove.
     */
    @Override
    public void remove(String key) {

        this.cache.remove( key);
    }

    /**
     * Removes all elements from cache.
     */
    @Override
    public void clear() {

        this.cache.clear();
    }

    private class KeyTimeout{
        private Date creation;
        private String value;
        private KeyTimeout(String value) {this.creation = new Date();
            this.value= value;}
        private boolean valid(){
            Calendar timeout= GregorianCalendar.getInstance();
            timeout.add(Calendar.MINUTE, -5);
            return timeout.getTime().before( this.creation);
        }
    }
}
