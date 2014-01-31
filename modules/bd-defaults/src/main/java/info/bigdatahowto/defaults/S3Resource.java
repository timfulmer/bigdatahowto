package info.bigdatahowto.defaults;

import info.bigdatahowto.core.Resource;

/**
 * Resource using S3 for storage.
 *
 * @author timfulmer
 */
public class S3Resource extends Resource {

    private int maximumKeyLength= 999;

    /**
     * Communicate information from bd internals out to the external resource.
     *
     * @param key   Represents a key to use when communicating to the resource.
     * @param value Data payload to communicate to the resource.
     */
    @Override
    public void write(String key, String value) {

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
        return super.read(key);
    }

    public void setMaximumKeyLength(int maximumKeyLength) {
        this.maximumKeyLength = maximumKeyLength;
    }
}
