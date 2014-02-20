package info.bigdatahowto.core;

import java.util.*;
import java.util.logging.Logger;

/**
 * Represents a queuing system.
 *
 * @author timfulmer
 */
public abstract class Queue {

    private transient Logger logger= Logger.getLogger(
            this.getClass().getName());

    /**
     * Persistent storage for jobs.
     */
    protected Resource resource;

    private Cache cache;

    protected Queue() {

        super();
    }

    protected Queue(Resource resource, Cache cache) {

        this();

        this.resource = resource;
        this.cache= cache;
    }

    /**
     * Creates a new job instance for a message; updates job status; stores the
     * job to the external resource; writes the job to the underlying queue;
     * and updates job status in the external resource once queued.
     *
     * !!WARNING: This method assumes unique job UUIDs!!
     *
     * @param jobUuid Use this as the Job.uuid if not null.
     * @param message Message to process in a job.
     * @param behaviorType Behavior to execute.
     * @param authentication Identifies the user originally requesting this job.
     */
    public UUID push(UUID jobUuid, Message message, BehaviorType behaviorType,
                     String authentication){

        Job job= new Job( message, behaviorType, authentication,
                message.getContextOwner());
        if( jobUuid!= null){

            job.setUuid( jobUuid);
        }
        job.setStatus( "Job creation request has been received.");
        this.resource.put(job);
        this.write(job.getUuid());
        job.toQueued();
        job.setStatus("Job creation request has been processed.");
        this.resource.put(job);

        return job.getUuid();
    }

    /**
     * Reads a job from the underlying queue; updating state information in
     * external resource.
     *
     * @return Job instance.
     */
    public Job pop(){

        Job job;
        do{

            ResultTuple resultTuple= this.read();
            if( resultTuple== null){

                this.logger.info( "Job queue empty, returning null.");

                // tf - There are no jobs in the queue.
                return null;
            }
            job = getJob(resultTuple.uuid);
            job.setQueueIdentifier( resultTuple.identifier);
        }while( !readyToProcess( job));
        if( this.alreadyProcessingKey(job.getMessageKey().getKey())){

            this.logger.info( String.format( "Message with key '%s' already " +
                    "being processed, returning null.",
                    job.getMessageKey().getKey()));

            return null;
        }
        job.toProcessing();
        job.setStatus("Job processing in progress ...");
        this.resource.put(job);

        return job;
    }

    /**
     * Checks if a job is ready to be processed.
     *
     * TODO: TESTME
     *
     * @param job    Job to check
     * @return True if job is ready to process.
     */
    private boolean readyToProcess(Job job) {

        Calendar calendar= new GregorianCalendar();
        calendar.add( Calendar.SECOND, -5);
        if( (job.getState()== JobState.Processing
                || job.getState()== JobState.Created)
                && job.getModifiedDate().before( calendar.getTime())){

            job.toQueued();
        }

        return job.getState()== JobState.Queued;
    }

    private synchronized boolean alreadyProcessingKey(String key){

        return !this.cache.put( key, key);
    }

    public void complete(Job job){

        this.cache.remove( job.getMessageKey().getKey());
        job.toComplete();
        job.setStatus("Job processing complete.");
        this.resource.put(job);
        this.delete(job.getQueueIdentifier());
    }

    public Job getJob(UUID uuid) {

        Job job= new Job( uuid);
        return this.resource.get( job);
    }

    /**
     * Convenience method, delegates to Queue.error(job,null).
     *
     * @param job Job entering error state.
     */
    public void error( Job job){

        this.error( job, null);
    }

    /**
     * Error a job by updating it's state and status in external resource, and
     * deleting the job from the underlying queue.
     *
     * @param job Job entering error state.
     * @param msg Error message.
     */
    public void error( Job job, String msg){

        this.cache.remove( job.getMessageKey().getKey());
        if( msg== null){

            msg= "Job processing encountered an unrecoverable error.  " +
                    "Processing has been suspended for this job.";
        }
        job.toError();
        job.setStatus( msg);
        this.resource.put(job);
        this.delete(job.getQueueIdentifier());
    }

    /**
     * Empty the queue.
     */
    public void clear(){

        this.cache.clear();
    }

    /**
     * Writes a UUID into the underlying queue.
     *
     * @param uuid UUID to queue.
     */
    protected abstract void write( UUID uuid);

    /**
     * Reads a  UUID from the underlying queue.  Reading must not delete
     * from the underlying queue.  Repeated calls to read may return the same
     * UUID.
     *
     * @return UUID in the queue.
     */
    protected abstract ResultTuple read();

    /**
     * Deletes a uuid from the queue.
     *
     * @param identifier Identifies message within queue.
     */
    protected abstract void delete( String identifier);

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public static class ResultTuple{
        public UUID uuid;
        public String identifier;
        public ResultTuple(UUID uuid, String identifier) {
            this.uuid = uuid;
            this.identifier = identifier;
        }
    }
}
