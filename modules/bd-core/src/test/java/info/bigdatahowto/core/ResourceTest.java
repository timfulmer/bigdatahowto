package info.bigdatahowto.core;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static info.bigdatahowto.core.TestUtils.fakeMessage;

/**
 * @author timfulmer
 */
public class ResourceTest {

    private final Map<String,String> hackery= new HashMap<>(1);
    private Resource resource;

    @Before
    public void before(){

        this.hackery.clear();
        this.resource= new Resource() {
            @Override
            public void write(String key, String value) {
                hackery.put(key,value);
            }

            @Override
            public String read(String key) {
                return hackery.get(key);
            }
        };
    }

    @Test
    public void testResource(){

        String name= "test-name";
        this.resource.setName(name);
        assert name.equals( resource.getName()):
                "Resource.name is not set correctly.";

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
    public void testStore(){

        final String testKey= "test-key";
        AggregateRoot aggregateRoot= new AggregateRoot() {
            @Override
            public String resourceKey() {
                return testKey;
            }
        };
        this.resource.put(aggregateRoot);

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
    public void testStore_Exception(){

        final String testKey= "test-key";
        AggregateRoot aggregateRoot= new AggregateRoot() {
            @Override
            public String resourceKey() {
                return testKey;
            }
        };
        this.resource= new Resource() {
            @Override
            public void write(String key, String value) {
                throw new RuntimeException();
            }
        };
        this.resource.put(aggregateRoot);
    }

    @Test( expected = UnsupportedOperationException.class)
    public void testRead(){

        this.resource= new Resource() {
            @Override
            public void write(String key, String value) {
                // Noop.
            }
        };
        this.resource.read("test-key");
    }

    @Test
    public void testGet(){

        Message message= fakeMessage();
        this.resource.put( message);
        Message result= this.resource.get( message.resourceKey(),
                Message.class);
        assert message.equals( result):
                "Resource.get is not calling Resource.read correctly.";
    }
}
