package info.bigdatahowto.core;

import java.util.UUID;

/**
 * Represents the processing of one message.
 *
 * @author timfulmer
 */
public class Job extends AggregateRoot {

    /**
     * Message this job is processing.
     */
    private MessageKey messageKey;

    /**
     * Behavior to execute for this message.
     */
    private BehaviorType behaviorType;

    /**
     * Hold authentication information for original user requesting a job.
     */
    private String jobOwner;

    /**
     * Holds authentication information for the owner of the context this job is
     * running in.
     */
    private String contextOwner;

    /**
     * String containing status information about this job.
     */
    private String status;

    /**
     * Times this job has been popped out of queue.
     */
    private Integer tries;

    /**
     * State of this job.
     */
    private JobState state;

    public Job() {

        super();
    }

    /**
     * Instantiates a 'Job' for a 'Message'.  Initializes 'tries' to '0' and
     * 'state' to 'Created'.
     *
     * @param message Job tracks the processing of message.
     * @param behaviorType Behavior to execute for this message.
     * @param jobOwner Authentication of user making original job request.
     * @param contextOwner Authentication of owner of context job is running in.
     */
    public Job(Message message, BehaviorType behaviorType, String jobOwner,
               String contextOwner) {

        this();

        this.setMessageKey( message.getMessageKey());
        this.setBehaviorType( behaviorType);
        this.setJobOwner(jobOwner);
        this.setContextOwner( contextOwner);
        this.setTries( 0);
        this.setState(JobState.Created);
    }

    public Job(UUID uuid) {

        this();

        this.setUuid( uuid);
    }

    public MessageKey getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(MessageKey messageKey) {
        this.messageKey = messageKey;
    }

    public BehaviorType getBehaviorType() {
        return behaviorType;
    }

    public void setBehaviorType(BehaviorType behaviorType) {
        this.behaviorType = behaviorType;
    }

    public String getJobOwner() {
        return jobOwner;
    }

    public void setJobOwner(String jobOwner) {
        this.jobOwner = jobOwner;
    }

    public String getContextOwner() {
        return contextOwner;
    }

    public void setContextOwner(String contextOwner) {
        this.contextOwner = contextOwner;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getTries() {
        return tries;
    }

    public void setTries(Integer tries) {
        this.tries = tries;
    }

    /**
     * Increment number of tries by one.
     */
    public void incrementTries(){

        this.setTries( this.getTries()+ 1);
    }

    public JobState getState() {
        return state;
    }

    public void setState(JobState state) {
        this.state = state;
    }

    /**
     * Moves job to 'Queued' state.  Only allowed from 'Created' state.
     */
    public void toQueued() {

        this.checkAndSetState(JobState.Queued, "queue", JobState.Created,
                JobState.Processing);
    }

    /**
     * Moves job to 'Processing' state.  Only allowed from 'Queued' state.
     */
    public void toProcessing() {

        this.checkAndSetState(JobState.Processing, "process", JobState.Queued);
        this.incrementTries();
    }

    /**
     * Moves job to 'Complete' state.  Only allowed from 'Processing' state.
     */
    public void toComplete() {

        this.checkAndSetState(JobState.Complete, "complete",
                JobState.Processing);
    }

    /**
     * Moves job to 'Error' state.  Only allowed from 'Processing' state.
     */
    public void toError() {

        this.checkAndSetState(JobState.Error, "error", JobState.Processing);
    }

    private void checkAndSetState(
            JobState toState, String action, JobState... fromStates) {

        boolean found= false;
        for( JobState state: fromStates){

            if( this.getState()== state){

                found= true;
                break;
            }
        }
        if( !found){

            throw new IllegalStateException( String.format(
                    "Cannot '%s' a job in state '%s'.", action,
                    this.getState()));
        }

        this.setState(toState);
    }

    @Override
    public String resourceKey() {
        return String.format( "jobs/%s", this.getUuid().toString());
    }

    @Override
    public String toString() {
        return "Job{" +
                "messageKey=" + messageKey +
                ", tries=" + tries +
                ", state=" + state +
                "} " + super.toString();
    }
}
