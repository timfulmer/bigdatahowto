package info.bigdatahowto.core;

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
     * Hold authentication information for original user requesting a job.
     */
    private String authentication;

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
     * @param authentication Authentication of user making original job request.
     */
    public Job(Message message, String authentication) {

        this();

        this.setAuthentication( authentication);
        this.setMessageKey( message.getMessageKey());
        this.setTries( 0);
        this.setState(JobState.Created);
    }

    public MessageKey getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(MessageKey messageKey) {
        this.messageKey = messageKey;
    }

    public String getAuthentication() {
        return authentication;
    }

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
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

        this.checkAndSetState(JobState.Queued, JobState.Created, "queue");
    }

    /**
     * Moves job to 'Processing' state.  Only allowed from 'Queued' state.
     */
    public void toProcessing() {

        this.checkAndSetState(JobState.Processing, JobState.Queued, "process");
        this.incrementTries();
    }

    /**
     * Moves job to 'Complete' state.  Only allowed from 'Processing' state.
     */
    public void toComplete() {

        this.checkAndSetState(JobState.Complete, JobState.Processing,
                "complete");
    }

    /**
     * Moves job to 'Error' state.  Only allowed from 'Processing' state.
     */
    public void toError() {

        this.checkAndSetState(JobState.Error, JobState.Processing, "error");
    }

    private void checkAndSetState(
            JobState toState, JobState fromState, String action) {

        if( this.getState()!= fromState){
            throw new IllegalStateException( String.format(
                    "Cannot '%s' a job in state '%s'.", action,
                    this.getState()));
        }

        this.setState(toState);
    }

    @Override
    public String resourceKey() {
        return this.getUuid().toString();
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
