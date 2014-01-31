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
        Message message= this.resourceRoadie.accessMessage( messageKey,
                job.getAuthentication());
        if( message== null){

            String msg= String.format( "No message for key '%s' found; " +
                    "please check the key and security settings and try again.",
                    messageKey.getKey());
            this.queue.error( job, msg);
        }
        if( message== null || !message.hasBehavior()){

            return;
        }
        ProcessingResult processingResult;
        try{

            processingResult= this.process( message);
        }catch( Throwable t){

            if( job.getTries()< this.maximumTries){

                processingResult= this.error( message, job.getTries());
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
        if( processingResult.getMessage()!= null){

            this.resourceRoadie.storeMessage( processingResult.getMessage());
        }
        if( !isEmpty( processingResult.getMessages())){

            for( Message newMessage: processingResult.getMessages()){

                this.resourceRoadie.storeMessage( newMessage,
                        job.getAuthentication());
                this.queue.push( newMessage, job.getAuthentication());
            }
        }
    }

    protected abstract ProcessingResult process( Message message);

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
