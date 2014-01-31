package info.bigdatahowto.core;

import org.junit.Test;

import java.util.Date;
import java.util.UUID;

/**
 * @author timfulmer
 */
public class AggregateRootTest {

    @Test
    public void testAggregateRoot(){

        AggregateRoot aggregateRoot= new AggregateRoot() {
            @Override
            public String resourceKey() {
                return "test-key";
            }
        };

        assert aggregateRoot.getUuid()!= null:
                "Message.uuid is null.";
        assert aggregateRoot.getCreationDate()!= null:
                "Message.creationDate is null.";
        assert aggregateRoot.getModifiedDate()!= null:
                "Message.modifiedData is null.";

        // tf - Test mutation.
        UUID uuid= UUID.randomUUID();
        aggregateRoot.setUuid( uuid);
        Date creationDate= new Date();
        aggregateRoot.setCreationDate( creationDate);
        Date modifiedDate= new Date();
        aggregateRoot.setModifiedDate( modifiedDate);

        assert uuid.equals( aggregateRoot.getUuid()):
                "AggregateRoot.uuid is not set correctly.";
        assert creationDate.equals( aggregateRoot.getCreationDate()):
                "AggregateRoot.creationDate is not set correctly.";
        assert modifiedDate.equals( aggregateRoot.getModifiedDate()):
                "AggregateRoot.modifiedDate is not set correctly.";
    }
}
