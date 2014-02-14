package info.bigdatahowto.core;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static info.bigdatahowto.core.TestUtils.fakeMessage;

/**
 * @author timfulmer
 */
public class ResourceTest {
    
    private static final String RESOURCE_NAME= "test-resource";
    private static final String AUTHENTICATION= "test-authentication";

    private final Map<String,String> hackery= new HashMap<>(1);
    private Resource resource;

    @Before
    public void before(){

        this.hackery.clear();
        this.resource= new Resource(RESOURCE_NAME) {
            @Override
            public void write(String key, String value) {
                hackery.put(key,value);
            }

            @Override
            public String read(String key) {
                return hackery.get(key);
            }

            @Override
            public boolean remove(String key) {
                return true;
            }
        };
    }

    @Test
    public void testResource(){

        assert RESOURCE_NAME.equals( resource.getName()):
                "Resource.name is not initialized correctly.";

        // Could be s3, elasticache, firebase, etc.
        this.resource.write(UUID.randomUUID().toString(),
                "{'name':'John Smith'," +
                        "'username':'jsmith'," +
                        "'password':'41b9df4a217bb3c10b1c339358111b0d'}");
        // Could be email server.
        this.resource.write("Super Urgent Subject Line",
                "Hi There, We noticed you haven't visited us in a while.  We " +
                        "hope you come back soon.  Cheers, the Team.");
    }

    @Test
    public void testPut() throws InterruptedException {

        final String testKey= "test-key";
        AggregateRoot aggregateRoot= new AggregateRoot() {
            @Override
            public String resourceKey() {
                return testKey;
            }
        };
        Date modifiedDate= aggregateRoot.getModifiedDate();
        Thread.sleep( 1);
        this.resource.put(aggregateRoot);
        assert modifiedDate.before( aggregateRoot.getModifiedDate()):
                "Resource.put is not updating AggregateRoot.modifiedDate";

        String value= String.format(
                "{\"uuid\":\"%s\",\"creationDate\":%s,\"modifiedDate\":%s}",
                aggregateRoot.getUuid().toString(),
                aggregateRoot.getCreationDate().getTime(),
                aggregateRoot.getModifiedDate().getTime());
        assert this.hackery.containsKey( testKey)
                && this.hackery.containsValue( value):
                "Resource.put is not calling Resource.write correctly.";
    }

    @Test( expected = RuntimeException.class)
    public void testPut_JsonException() throws InterruptedException {

        final String testKey= "test-key";
        AggregateRoot aggregateRoot= new AggregateRoot() {
            @Override
            public String resourceKey() {
                return testKey;
            }
            @Override
            public Date getCreationDate(){
                throw new UnsupportedOperationException();
            }
        };
        this.resource.put(aggregateRoot);
    }

    @Test( expected = RuntimeException.class)
    public void testPut_Exception(){

        final String testKey= "test-key";
        AggregateRoot aggregateRoot= new AggregateRoot() {
            @Override
            public String resourceKey() {
                return testKey;
            }
        };
        this.resource= new Resource(RESOURCE_NAME) {
            @Override
            public void write(String key, String value) {
                throw new RuntimeException();
            }

            @Override
            public boolean remove(String key) {
                return true;
            }
        };
        this.resource.put(aggregateRoot);
    }

    @Test( expected = UnsupportedOperationException.class)
    public void testRead(){

        this.resource= new Resource(RESOURCE_NAME) {
            @Override
            public void write(String key, String value) {
                // Noop.
            }

            @Override
            public boolean remove(String key) {
                return true;
            }
        };
        this.resource.read("test-key");
    }

    @Test
    public void testGet(){

        Message message= fakeMessage();
        this.resource.put(message);
        Message result= this.resource.get( message);
        assert message.equals( result):
                "Resource.get is not calling Resource.read correctly.";
    }

    @Test( expected = RuntimeException.class)
    public void testGet_BadReads(){

        this.resource= new Resource(RESOURCE_NAME) {
            @Override
            public void write(String key, String value) {
                hackery.put(key,value);
            }

            @Override
            public String read(String key) {
                return null;
            }

            @Override
            public boolean remove(String key) {
                return true;
            }
        };
        Message result= this.resource.get( fakeMessage());
        assert result== null:
                "Resource.get is not handling null read correctly.";

        this.resource= new Resource(RESOURCE_NAME) {
            @Override
            public void write(String key, String value) {
                hackery.put(key,value);
            }

            @Override
            public String read(String key) {
                return "dummy-json";
            }

            @Override
            public boolean remove(String key) {
                return true;
            }
        };
        this.resource.get( fakeMessage());
    }

    @Test( expected = IllegalStateException.class)
    public void testDelete(){

        Message message= fakeMessage();
        this.resource.put(message);

        Message result= this.resource.delete( message);
        assert result!= null: "Resource.delete is not implemented correctly.";
        assert message.equals( result):
                "Resource.delete is not implemented correctly.";

        this.resource= new Resource(RESOURCE_NAME) {
            @Override
            public void write(String key, String value) {
                hackery.put(key,value);
            }

            @Override
            public String read(String key) {
                return hackery.get(key);
            }

            @Override
            public boolean remove(String key) {
                return false;
            }
        };
        this.resource.delete( message);
    }
}
