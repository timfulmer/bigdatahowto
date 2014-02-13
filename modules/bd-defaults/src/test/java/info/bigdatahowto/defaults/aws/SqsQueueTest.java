package info.bigdatahowto.defaults.aws;

import info.bigdatahowto.core.Queue;
import info.bigdatahowto.defaults.FileResource;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

/**
 * @author timfulmer
 */
public class SqsQueueTest {

    private SqsQueue sqsQueue;

    @Before
    public void before(){

        this.sqsQueue= new SqsQueue( new FileResource());
        this.sqsQueue.clear();
    }

    @Test
    public void testQueue() throws InterruptedException {

        UUID uuid= UUID.randomUUID();
        this.sqsQueue.write( uuid);

        Queue.ResultTuple result;
        int times= 0;
        do{
            result= this.sqsQueue.read();
            times++;
            Thread.sleep( 500);
        }while( result== null && times< 5);
        assert result!= null: "Could not read message.";
        assert uuid.equals( result.uuid): "Received incorrect result.";

        this.sqsQueue.delete( result.identifier);
        times= 0;
        do{
            result=  this.sqsQueue.read();
            times++;
            Thread.sleep( 500);
        }while( result!= null && times< 5);
        assert result== null: "Could not delete.";
    }
}
