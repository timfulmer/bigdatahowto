package info.bigdatahowto.core;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static info.bigdatahowto.core.TestUtils.fakeJob;
import static info.bigdatahowto.core.TestUtils.fakeMessage;
import static org.mockito.Mockito.*;

/**
 * @author timfulmer
 */
public class ProcessorTest {

    private final ProcessingResult processingResult=
            mock( ProcessingResult.class);

    private Queue queueMock;
    private ResourceRoadie resourceRoadieMock;
    private Processor processor;

    @Before
    public void before(){

        this.queueMock= mock( Queue.class);
        this.resourceRoadieMock= mock( ResourceRoadie.class);

        this.processor= new Processor() {
            @Override
            protected ProcessingResult process(Message message) {
                return processingResult;
            }

            @Override
            protected ProcessingResult error(Message message, int tries) {
                return processingResult;
            }
        };
        this.processor.setQueue( this.queueMock);
        this.processor.setResourceRoadie( this.resourceRoadieMock);
    }

    @Test
    public void testProcessor(){

        Job job= fakeJob();
        when(this.queueMock.pop()).thenReturn( job);

        Message message= fakeMessage();
        message.getBehavior().put("test-key", "test-value");
        when(this.resourceRoadieMock.accessMessage(job.getMessageKey(),
                job.getAuthentication())).thenReturn(message);
        when(this.processingResult.isContinueProcessing()).thenReturn( true);
        when(this.processingResult.getMessage()).thenReturn(message);
        List<Message> messages= new ArrayList<>(1);
        messages.add(message);
        when(this.processingResult.getMessages()).thenReturn(messages);

        this.processor.pullJob();

        verify(this.resourceRoadieMock).storeMessage(message);
        verify(this.resourceRoadieMock).storeMessage(message,
                job.getAuthentication());
        verify(this.queueMock).push(message, job.getAuthentication());
    }

    @Test
    public void testProcessor_NullJob(){

        this.processor.pullJob();
    }

    @Test
    public void testProcessor_NullMessage(){

        Job job= fakeJob();
        when(this.queueMock.pop()).thenReturn( job);

        when(this.resourceRoadieMock.accessMessage(job.getMessageKey(),
                job.getAuthentication())).thenReturn( null);
          this.processor.pullJob();

        verify(this.queueMock).error(eq(job), anyString());
    }

    @Test
    public void testProcessor_NoBehavior(){

        Job job= fakeJob();
        when(this.queueMock.pop()).thenReturn( job);

        Message message= fakeMessage();
        when(this.resourceRoadieMock.accessMessage(job.getMessageKey(),
                job.getAuthentication())).thenReturn(message);
        when(this.processingResult.getMessage()).thenReturn(message);

        this.processor.pullJob();
    }

    @Test
    public void testProcessor_Error(){

        Job job= fakeJob();
        when(this.queueMock.pop()).thenReturn( job);

        Message message= fakeMessage();
        message.getBehavior().put("test-key", "test-value");
        when(this.resourceRoadieMock.accessMessage(job.getMessageKey(),
                job.getAuthentication())).thenReturn(message);
        when(this.processingResult.isContinueProcessing()).thenReturn( true);
        when(this.processingResult.getMessage()).thenReturn(message);
        List<Message> messages= new ArrayList<>(1);
        messages.add(message);
        when(this.processingResult.getMessages()).thenReturn(messages);

        this.processor= new Processor() {
            @Override
            protected ProcessingResult process(Message message) {
                throw new RuntimeException();
            }

            @Override
            protected ProcessingResult error(Message message, int tries) {
                return processingResult;
            }
        };
        this.processor.setQueue( this.queueMock);
        this.processor.setResourceRoadie( this.resourceRoadieMock);
        this.processor.pullJob();

        verify(this.resourceRoadieMock).storeMessage(message);
        verify(this.resourceRoadieMock).storeMessage(message,
                job.getAuthentication());
        verify(this.queueMock).push(message, job.getAuthentication());
    }

    @Test( expected = RuntimeException.class)
    public void testProcessor_FatalError(){

        Job job= fakeJob();
        job.setTries(5);
        when(this.queueMock.pop()).thenReturn( job);

        Message message= fakeMessage();
        message.getBehavior().put("test-key", "test-value");
        when(this.resourceRoadieMock.accessMessage(job.getMessageKey(),
                job.getAuthentication())).thenReturn(message);
        when(this.processingResult.getMessage()).thenReturn(message);
        List<Message> messages= new ArrayList<>(1);
        messages.add(message);
        when(this.processingResult.getMessages()).thenReturn(messages);

        this.processor= new Processor() {
            @Override
            protected ProcessingResult process(Message message) {
                throw new RuntimeException();
            }

            @Override
            protected ProcessingResult error(Message message, int tries) {
                return null;
            }
        };
        this.processor.setQueue( this.queueMock);
        this.processor.setResourceRoadie( this.resourceRoadieMock);
        this.processor.pullJob();

        verify(this.queueMock).error(job);
    }

    @Test
    public void testProcessor_NullProcessingResult(){

        Job job= fakeJob();
        when(this.queueMock.pop()).thenReturn(job);

        Message message= fakeMessage();
        message.getBehavior().put("test-key", "test-value");
        when(this.resourceRoadieMock.accessMessage(job.getMessageKey(),
                job.getAuthentication())).thenReturn(message);

        this.processor= new Processor() {
            @Override
            protected ProcessingResult process(Message message) {
                return null;
            }

            @Override
            protected ProcessingResult error(Message message, int tries) {
                return null;
            }
        };
        this.processor.setQueue(this.queueMock);
        this.processor.setResourceRoadie( this.resourceRoadieMock);
        this.processor.pullJob();
    }

    @Test
    public void testProcessor_NotContinuing(){

        Job job= fakeJob();
        when(this.queueMock.pop()).thenReturn( job);

        Message message= fakeMessage();
        message.getBehavior().put("test-key", "test-value");
        when(this.resourceRoadieMock.accessMessage(job.getMessageKey(),
                job.getAuthentication())).thenReturn(message);
        when(this.processingResult.isContinueProcessing()).thenReturn( false);

        this.processor.pullJob();
    }

    @Test
    public void testProcessor_EmptyProcessingResult(){

        Job job= fakeJob();
        when(this.queueMock.pop()).thenReturn(job);

        Message message= fakeMessage();
        message.getBehavior().put("test-key", "test-value");
        when(this.resourceRoadieMock.accessMessage(job.getMessageKey(),
                job.getAuthentication())).thenReturn(message);
        when(this.processingResult.getMessage()).thenReturn(null);
        when(this.processingResult.getMessages()).thenReturn(null);

        this.processor.pullJob();
    }
}
