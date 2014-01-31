package info.bigdatahowto.defaults;

import info.bigdatahowto.core.Queue;

import java.util.LinkedList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple in-memory queue, backed by a LinkedList, for testing only.
 *
 * @author timfulmer
 */
public class InMemoryQueue extends Queue {

    private transient Logger logger= Logger.getLogger(
            this.getClass().getName());

    private java.util.Queue<UUID> queue;

    public InMemoryQueue() {

        super();

        this.queue= new LinkedList<>();
    }

    /**
     * Writes a UUID into the underlying queue.
     *
     * @param uuid UUID to queue.
     */
    @Override
    protected void write(UUID uuid) {

        if( !this.queue.offer(uuid)){

            String msg= String.format( "Could not put UUID '%s' into queue " +
                    "of type '%s'.", uuid.toString(),
                    this.queue.getClass().getName());
            this.logger.log(Level.SEVERE, msg);

            throw new RuntimeException( msg);
        }
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

        return this.queue.peek();
    }

    /**
     * Deletes a uuid from the queue.
     *
     * @param uuid Uuid to delete.
     */
    @Override
    protected void delete(UUID uuid) {

        if( !this.queue.remove( uuid)){

            String msg= String.format( "Could not remove UUID '%s' from " +
                    "queue of type '%s'.", uuid.toString(),
                    this.queue.getClass().getName());
            this.logger.log(Level.SEVERE, msg);

            throw new RuntimeException( msg);
        }
    }
}
