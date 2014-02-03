package info.bigdatahowto.core;

import java.util.UUID;

/**
 * Represents a queuing system.
 *
 * @author timfulmer
 */
public abstract class Queue {

    /**
     * Persistent storage for jobs.
     */
    protected Resource resource;

    protected Queue(Resource resource) {

        super();

        this.resource = resource;
    }

    /**
     * Creates a new job instance for a message; updates job status; stores the
     * job to the external resource; writes the job to the underlying queue;
     * and updates job status in the external resource once queued.
     *
     * @param message Message to process in a job.
     * @param authentication Identifies the user originally requesting this job.
     */
    public UUID push(Message message, String authentication){

        Job job= new Job( message, authentication);
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
            UUID uuid= this.read();
            job = getJob(uuid);
        }while( job.getState()!= JobState.Queued);
        job.toProcessing();
        job.incrementTries();
        job.setStatus("Job processing in progress ...");
        this.resource.put(job);

        return job;
    }

    public void complete(Job job){

        job.toComplete();
        this.resource.put( job);
        this.delete( job.getUuid());
    }

    public Job getJob(UUID uuid) {
        return this.resource.get( uuid.toString(), Job.class);
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

        if( msg== null){

            msg= "Job processing encountered an unrecoverable error.  " +
                    "Processing has been suspended for this job.";
        }
        job.toError();
        job.setStatus( msg);
        this.resource.put(job);
        this.delete(job.getUuid());
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
    protected abstract UUID read();

    /**
     * Deletes a uuid from the queue.
     *
     * @param uuid Uuid to delete.
     */
    protected abstract void delete( UUID uuid);

    @javax.annotation.Resource
    public void setResource(Resource resource) {
        this.resource = resource;
    }
}
