package info.bigdatahowto.core;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

/**
 * Process a job.
 *
 * @author timfulmer
 */
public abstract class Processor {

    private transient Logger logger= Logger.getLogger(
            this.getClass().getName());

    private Queue queue;
    private ResourceRoadie resourceRoadie;
    private Integer maximumTries= 5;

    protected Processor(Queue queue, ResourceRoadie resourceRoadie) {

        super();

        this.queue = queue;
        this.resourceRoadie = resourceRoadie;
    }

    /**
     * Pops a job off the queue, accesses the current state of the job's
     * message, processes message, handles errors and updates job and message
     * state.
     *
     * PLEASE NOTE: Jobs are retried a total of five times.  Any job
     * failing more than five times will be parked in Error state.
     */
    public void pullJob(){

        // tf - Pop a new job off the queue.
        Job job= this.queue.pop();
        if( job== null){

            return;
        }

        // tf - Access current state of message.
        MessageKey messageKey= job.getMessageKey();
        Message message= this.resourceRoadie.accessMessage(
                new Message( messageKey), job.getJobOwner(),
                job.getBehaviorType());
        if( message== null || !message.hasBehavior()){

            return;
        }
        ProcessingResult processingResult= null;
        try{

            processingResult= this.process( message, job.getBehaviorType());
        }catch( Throwable t){

            if( job.getTries()< this.maximumTries){

                String msg= String.format(
                        "Caught exception processing message " +
                        "'%s' for job '%s'.", message.toString(),
                        job.toString());
                this.logger.log( Level.WARNING, msg, t);

                if( message.getBehavior().containsKey( BehaviorType.Error)){

                    processingResult= this.error( message, job.getTries());
                    if( processingResult!= null
                            && processingResult.isContinueProcessing()){

                        job.toQueued();
                    }
                }
            }else{

                this.queue.error( job);

                String msg= String.format(
                        "Attempted processing '%s' tries; job state '%s'.  " +
                                "Giving up and removing job from queue.",
                        job.toString(), job.getTries());
                this.logger.log(Level.SEVERE,msg,t);

                throw new RuntimeException( msg, t);
            }
        }

        if( processingResult== null
                || !processingResult.isContinueProcessing()){

            return;
        }
        if( job.getBehaviorType()== BehaviorType.Delete){

            this.resourceRoadie.deleteMessage(message);
        }else if( processingResult.getMessage()!= null){

            // tf - Do not need an authentication here since we're updating a
            //  message already authenticated above.
            this.resourceRoadie.storeMessage( processingResult.getMessage());
        }
        if( !isEmpty( processingResult.getMessages())){

            for(ProcessingResult.NewMessage newMessage:
                    processingResult.getMessages()){

                Message m= this.resourceRoadie.storeMessage(
                        new Message( newMessage.makeKey()), newMessage.behavior,
                        job.getJobOwner());
                this.queue.push( m, newMessage.behavior.getBehaviorType(),
                        job.getJobOwner());
            }
        }

        this.queue.complete(job);
    }

    /**
     * Applies behavior to data defined in a message.
     *
     * @param message Message containing behavior and data.
     * @param behaviorType Behavior to execute.
     * @return Results of processing.
     */
    protected abstract ProcessingResult process( Message message,
                                                 BehaviorType behaviorType);

    /**
     * Applies error handling to data defined in a message.  This method is
     * called whenever an unhandled error happens during behavior processing.
     *
     * @param message Message containing error handling and data.
     * @param tries Number of times processing this message has been tried.
     * @return Results of processing.
     */
    protected abstract ProcessingResult error( Message message, int tries);

    @javax.annotation.Resource
    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    @javax.annotation.Resource
    public void setResourceRoadie(ResourceRoadie resourceRoadie) {
        this.resourceRoadie = resourceRoadie;
    }

    public void setMaximumTries(Integer maximumTries) {
        this.maximumTries = maximumTries;
    }
}
