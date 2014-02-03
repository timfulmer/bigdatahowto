package info.bigdatahowto.defaults;

import info.bigdatahowto.core.Queue;
import info.bigdatahowto.core.Resource;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple in-memory queue, backed by a ConcurrentLinkedQueue, for testing
 * only.
 *
 * @author timfulmer
 */
public class InMemoryQueue extends Queue {

    private transient Logger logger= Logger.getLogger(
            this.getClass().getName());

    private java.util.Queue<UUID> head, tail;

    public InMemoryQueue( Resource resource) {

        super( resource);

        this.head= new ConcurrentLinkedQueue<>();
        this.tail= new ConcurrentLinkedQueue<>();
    }

    /**
     * Writes a UUID into the underlying queue.
     *
     * @param uuid UUID to queue.
     */
    @Override
    protected void write(UUID uuid) {

        this.head.add( uuid);
    }

    /**
     * Reads a  UUID from the underlying queue.  Reading must not delete
     * from the underlying queue.  Repeated calls to read may return the same
     * UUID.
     *
     * @return UUID in the queue.
     */
    @Override
    protected UUID read() {

        if( this.head.isEmpty() && !this.tail.isEmpty()){

            this.swap();
        }
        UUID current= this.head.poll();
        if( current!= null){

            this.tail.add( current);
        }

        return current;
    }

    /**
     * Deletes a uuid from the queue.
     *
     * @param uuid Uuid to delete.
     */
    @Override
    protected void delete(UUID uuid) {

        if( !this.head.remove( uuid) && !this.tail.remove( uuid)){

            String msg= String.format( "Could not remove UUID '%s' from " +
                    "head of type '%s' or tail of type '%s'.", uuid.toString(),
                    this.head.getClass().getName(),
                    this.tail.getClass().getName());
            this.logger.log(Level.SEVERE, msg);

            throw new RuntimeException( msg);
        }
    }

    private void swap(){

        this.head= this.tail;
        this.tail= new ConcurrentLinkedQueue<>();
    }
}
