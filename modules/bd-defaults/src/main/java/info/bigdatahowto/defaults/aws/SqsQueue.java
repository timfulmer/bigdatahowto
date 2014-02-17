package info.bigdatahowto.defaults.aws;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import info.bigdatahowto.core.Cache;
import info.bigdatahowto.core.Queue;
import info.bigdatahowto.core.Resource;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

/**
 * Queue implementation using SQS.
 *
 * @author timfulmer
 */
public class SqsQueue extends Queue {

    private static final String DEFAULT_QUEUE_NAME= "bd-sqs1-useast1";
    private static final int DEFAULT_VISIBILITY_TIMEOUT= 30;

    private transient Logger logger= Logger.getLogger(
            this.getClass().getName());

    private AmazonSQS amazonSQS;
    private String queueUrl;

    public SqsQueue(Resource resource, Cache cache) {

        this( resource, cache, DEFAULT_QUEUE_NAME);
    }

    public SqsQueue(Resource resource, Cache cache, String queueName) {

        super(resource, cache);

        this.amazonSQS= new AmazonSQSClient(
                new AmazonClient().getAwsCredentials("aws.sqs.accessKeyId",
                        "aws.sqs.secretKey"));
        try{

            this.queueUrl= this.amazonSQS.getQueueUrl( queueName).getQueueUrl();
        }catch ( QueueDoesNotExistException e){

            this.queueUrl= this.amazonSQS.createQueue( queueName).getQueueUrl();
        }
    }

    /**
     * Writes a UUID into the underlying queue.
     *
     * @param uuid UUID to queue.
     */
    @Override
    protected void write(UUID uuid) {

        this.amazonSQS.sendMessage( this.queueUrl, uuid.toString());
    }

    /**
     * Reads a  UUID from the underlying queue.  Reading must not delete
     * from the underlying queue.  Repeated calls to read may return the same
     * UUID.
     *
     * @return UUID in the queue.
     */
    @Override
    protected ResultTuple read() {

        ReceiveMessageResult receiveMessageResult=
                this.amazonSQS.receiveMessage( this.queueUrl);
        if( !isEmpty( receiveMessageResult.getMessages())){

            Message message= receiveMessageResult.getMessages().get( 0);
            this.amazonSQS.changeMessageVisibility( this.queueUrl,
                    message.getReceiptHandle(), DEFAULT_VISIBILITY_TIMEOUT);

            return new ResultTuple(UUID.fromString( message.getBody()),
                    message.getReceiptHandle());
        }

        return null;
    }

    /**
     * Deletes a uuid from the queue.
     *
     * @param identifier Identifies message within queue.
     */
    @Override
    protected void delete(String identifier) {

        this.amazonSQS.deleteMessage( this.queueUrl, identifier);
    }

    /**
     * Empty the queue.
     *
     * !!WARNING: THIS METHOD SHOULD NEVER BE USED IN PRODUCTION!!
     */
    @Override
    public void clear() {

        super.clear();

        boolean hadMessages;
        do{

            ReceiveMessageResult receiveMessageResult=
                    this.amazonSQS.receiveMessage( this.queueUrl);
            List<Message> messages= receiveMessageResult.getMessages();
            hadMessages= !isEmpty( messages);
            for( Message message: messages){

                this.amazonSQS.deleteMessage( this.queueUrl,
                        message.getReceiptHandle());
            }
        }while( hadMessages);
    }
}
