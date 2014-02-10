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

        this.processor= new Processor(this.queueMock, this.resourceRoadieMock) {
            @Override
            protected ProcessingResult process(Message message) {
                return processingResult;
            }

            @Override
            protected ProcessingResult error(Message message, int tries) {
                return processingResult;
            }
        };
    }

    @Test
    public void testProcessor(){

        Job job= fakeJob();
        when(this.queueMock.pop()).thenReturn( job);

        Message message= fakeMessage();
        Behavior behavior= new Behavior( BehaviorType.Persist, "test-value");
        message.getBehavior().put(behavior.getBehaviorType(), behavior);
        when(this.resourceRoadieMock.accessMessage(job.getMessageKey(),
                job.getAuthentication())).thenReturn(message);
        when(this.processingResult.isContinueProcessing()).thenReturn( true);
        when(this.processingResult.getMessage()).thenReturn(message);
        List<ProcessingResult.NewMessage> messages= new ArrayList<>(1);
        ProcessingResult pr= new ProcessingResult();
        pr.setMessage( message);
        messages.add(pr.new NewMessage(message.getMessageKey().getUserKey(),
                behavior, null));
        when(this.processingResult.getMessages()).thenReturn(messages);
        when(this.resourceRoadieMock.storeMessage(message.getMessageKey(),
                message.getBehavior().get( BehaviorType.Persist),
                job.getAuthentication())).thenReturn( message);

        this.processor.pullJob();

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
        Behavior persist= new Behavior( BehaviorType.Persist, "test-value");
        Behavior error= new Behavior( BehaviorType.Error, "test-error");
        message.getBehavior().put(persist.getBehaviorType(), persist);
        message.getBehavior().put( error.getBehaviorType(), error);
        when(this.resourceRoadieMock.accessMessage(job.getMessageKey(),
                job.getAuthentication())).thenReturn(message);
        when(this.processingResult.isContinueProcessing()).thenReturn( true);
        when(this.processingResult.getMessage()).thenReturn(message);
        List<ProcessingResult.NewMessage> messages= new ArrayList<>(1);
        ProcessingResult pr= new ProcessingResult();
        pr.setMessage( message);
        messages.add(pr.new NewMessage(message.getMessageKey().getUserKey(),
                persist, null));
        when(this.processingResult.getMessages()).thenReturn(messages);
        when(this.resourceRoadieMock.storeMessage(message.getMessageKey(),
                message.getBehavior().get( BehaviorType.Persist),
                job.getAuthentication())).thenReturn( message);

        this.processor= new Processor(this.queueMock, this.resourceRoadieMock) {
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

        verify(this.queueMock).push(message, job.getAuthentication());
    }

    @Test
    public void testProcessor_UndefinedError(){

        Job job= fakeJob();
        when(this.queueMock.pop()).thenReturn( job);

        Message message= fakeMessage();
        Behavior behavior= new Behavior( BehaviorType.Persist, "test-value");
        message.getBehavior().put(behavior.getBehaviorType(), behavior);
        when(this.resourceRoadieMock.accessMessage(job.getMessageKey(),
                job.getAuthentication())).thenReturn(message);
        when(this.processingResult.isContinueProcessing()).thenReturn( true);
        when(this.processingResult.getMessage()).thenReturn(message);
        List<ProcessingResult.NewMessage> messages= new ArrayList<>(1);
        ProcessingResult pr= new ProcessingResult();
        pr.setMessage( message);
        messages.add(pr.new NewMessage("", behavior, null));
        when(this.processingResult.getMessages()).thenReturn(messages);

        this.processor= new Processor(this.queueMock, this.resourceRoadieMock) {
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
    }

    @Test( expected = RuntimeException.class)
    public void testProcessor_FatalError(){

        Job job= fakeJob();
        job.setTries(5);
        when(this.queueMock.pop()).thenReturn( job);

        Message message= fakeMessage();
        Behavior behavior= new Behavior( BehaviorType.Persist, "test-value");
        message.getBehavior().put(behavior.getBehaviorType(), behavior);
        when(this.resourceRoadieMock.accessMessage(job.getMessageKey(),
                job.getAuthentication())).thenReturn(message);
        when(this.processingResult.getMessage()).thenReturn(message);
        List<ProcessingResult.NewMessage> messages= new ArrayList<>(1);
        ProcessingResult pr= new ProcessingResult();
        pr.setMessage( message);
        messages.add(pr.new NewMessage("", behavior, null));
        when(this.processingResult.getMessages()).thenReturn(messages);

        this.processor= new Processor(this.queueMock, this.resourceRoadieMock) {
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
        Behavior behavior= new Behavior( BehaviorType.Persist, "test-value");
        message.getBehavior().put(behavior.getBehaviorType(), behavior);
        when(this.resourceRoadieMock.accessMessage(job.getMessageKey(),
                job.getAuthentication())).thenReturn(message);

        this.processor= new Processor(this.queueMock, this.resourceRoadieMock) {
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
        Behavior behavior= new Behavior( BehaviorType.Persist, "test-value");
        message.getBehavior().put(behavior.getBehaviorType(), behavior);
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
        Behavior behavior= new Behavior( BehaviorType.Persist, "test-value");
        message.getBehavior().put(behavior.getBehaviorType(), behavior);
        when(this.resourceRoadieMock.accessMessage(job.getMessageKey(),
                job.getAuthentication())).thenReturn(message);
        when(this.processingResult.getMessage()).thenReturn(null);
        when(this.processingResult.getMessages()).thenReturn(null);

        this.processor.pullJob();
    }
}
