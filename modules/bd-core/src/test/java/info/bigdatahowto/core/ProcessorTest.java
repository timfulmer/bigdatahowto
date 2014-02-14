package info.bigdatahowto.core;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static info.bigdatahowto.core.TestUtils.fakeJob;
import static info.bigdatahowto.core.TestUtils.fakeMessage;
import static org.mockito.Mockito.*;

/**
 * @author timfulmer
 */
public class ProcessorTest {

    private final ProcessingResult processingResultMock=
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
            protected ProcessingResult process(Message message,
                                               BehaviorType behaviorType) {
                return processingResultMock;
            }

            @Override
            protected ProcessingResult error(Message message, int tries) {
                return processingResultMock;
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
        when(this.resourceRoadieMock.accessMessage(
                any( Message.class),
                eq(job.getJobOwner()),
                eq(BehaviorType.Persist)
        )).thenReturn(message);
        when(this.processingResultMock.isContinueProcessing()).thenReturn(true);
        when(this.processingResultMock.getMessage()).thenReturn(message);
        List<ProcessingResult.NewMessage> messages= new ArrayList<>(1);
        ProcessingResult pr= new ProcessingResult();
        pr.setMessage(message);
        messages.add(pr.new NewMessage(message.getMessageKey().getUserKey(),
                behavior, null));
        when(this.processingResultMock.getMessages()).thenReturn(messages);
        when(this.resourceRoadieMock.storeMessage(any(Message.class),
                eq(message.getBehavior().get(BehaviorType.Persist)),
                eq(job.getJobOwner())
        )).thenReturn( message);

        this.processor.pullJob();

        verify(this.queueMock).push(any(UUID.class), eq(message),
                eq(BehaviorType.Persist), eq(job.getJobOwner()));
        verify(this.queueMock).complete( job);
    }

    @Test
    public void testProcessor_NullJob(){

        this.processor.pullJob();
    }

    @Test
    public void testProcessor_NullMessage(){

        Job job= fakeJob();
        when(this.queueMock.pop()).thenReturn( job);

        when(this.resourceRoadieMock.accessMessage(
                new Message( job.getMessageKey()), job.getJobOwner(),
                BehaviorType.Persist
        )).thenReturn( null);
        this.processor.pullJob();
    }

    @Test
    public void testProcessor_NoBehavior(){

        Job job= fakeJob();
        when(this.queueMock.pop()).thenReturn( job);

        Message message= fakeMessage();
        when(this.resourceRoadieMock.accessMessage(
                new Message( job.getMessageKey()), job.getJobOwner(),
                BehaviorType.Persist
        )).thenReturn(message);
        when(this.processingResultMock.getMessage()).thenReturn(message);

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
        when(this.resourceRoadieMock.accessMessage(
                any( Message.class),
                eq(job.getJobOwner()),
                eq(BehaviorType.Persist)
        )).thenReturn(message);
        when(this.processingResultMock.isContinueProcessing()).thenReturn(true);
        when(this.processingResultMock.getMessage()).thenReturn(message);
        List<ProcessingResult.NewMessage> messages= new ArrayList<>(1);
        ProcessingResult pr= new ProcessingResult();
        pr.setMessage(message);
        messages.add(pr.new NewMessage(message.getMessageKey().getUserKey(),
                persist, null));
        when(this.processingResultMock.getMessages()).thenReturn(messages);
        when(this.resourceRoadieMock.storeMessage(
                any( Message.class),
                eq( message.getBehavior().get( BehaviorType.Persist)),
                eq(job.getJobOwner())
        )).thenReturn(message);

        this.processor= new Processor(this.queueMock, this.resourceRoadieMock) {
            @Override
            protected ProcessingResult process(Message message,
                                               BehaviorType behaviorType) {
                throw new RuntimeException();
            }

            @Override
            protected ProcessingResult error(Message message, int tries) {
                return processingResultMock;
            }
        };
        this.processor.setQueue( this.queueMock);
        this.processor.setResourceRoadie( this.resourceRoadieMock);
        this.processor.pullJob();

        verify(this.queueMock).push(any(UUID.class), eq(message),
                eq(BehaviorType.Persist), eq(job.getJobOwner()));
    }

    @Test
    public void testProcessor_UndefinedError(){

        Job job= fakeJob();
        when(this.queueMock.pop()).thenReturn( job);

        Message message= fakeMessage();
        Behavior behavior= new Behavior( BehaviorType.Persist, "test-value");
        message.getBehavior().put(behavior.getBehaviorType(), behavior);
        when(this.resourceRoadieMock.accessMessage(any(Message.class),
                anyString(), any(BehaviorType.class))).thenReturn(message);
        when(this.processingResultMock.isContinueProcessing()).thenReturn(true);
        when(this.processingResultMock.getMessage()).thenReturn(message);
        List<ProcessingResult.NewMessage> messages= new ArrayList<>(1);
        ProcessingResult pr= new ProcessingResult();
        pr.setMessage(message);
        messages.add(pr.new NewMessage("", behavior, null));
        when(this.processingResultMock.getMessages()).thenReturn(messages);

        this.processor= new Processor(this.queueMock, this.resourceRoadieMock) {
            @Override
            protected ProcessingResult process(Message message,
                                               BehaviorType behaviorType) {
                throw new RuntimeException();
            }

            @Override
            protected ProcessingResult error(Message message, int tries) {
                return processingResultMock;
            }
        };
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
        when(this.resourceRoadieMock.accessMessage(
                any( Message.class),
                eq( job.getJobOwner()),
                eq(BehaviorType.Persist)
        )).thenReturn(message);
        when(this.processingResultMock.getMessage()).thenReturn(message);
        List<ProcessingResult.NewMessage> messages= new ArrayList<>(1);
        ProcessingResult pr= new ProcessingResult();
        pr.setMessage( message);
        messages.add(pr.new NewMessage("", behavior, null));
        when(this.processingResultMock.getMessages()).thenReturn(messages);

        this.processor= new Processor(this.queueMock, this.resourceRoadieMock) {
            @Override
            protected ProcessingResult process(Message message,
                                               BehaviorType behaviorType) {
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
        when(this.resourceRoadieMock.accessMessage(message,
                job.getJobOwner(), BehaviorType.Persist
        )).thenReturn(message);

        this.processor= new Processor(this.queueMock, this.resourceRoadieMock) {
            @Override
            protected ProcessingResult process(Message message,
                                               BehaviorType behaviorType) {
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
        when(this.resourceRoadieMock.accessMessage(message,
                job.getJobOwner(), BehaviorType.Persist
        )).thenReturn(message);
        when(this.processingResultMock.isContinueProcessing()).thenReturn(false);

        this.processor.pullJob();
    }

    @Test
    public void testProcessor_EmptyProcessingResult(){

        Job job= fakeJob();
        when(this.queueMock.pop()).thenReturn(job);

        Message message= fakeMessage();
        Behavior behavior= new Behavior( BehaviorType.Persist, "test-value");
        message.getBehavior().put(behavior.getBehaviorType(), behavior);
        when(this.resourceRoadieMock.accessMessage(message,
                job.getJobOwner(), BehaviorType.Persist
        )).thenReturn(message);
        when(this.processingResultMock.getMessage()).thenReturn(null);
        when(this.processingResultMock.getMessages()).thenReturn(null);

        this.processor.pullJob();
    }

    @Test
    public void testDelete(){

        Job job= fakeJob();
        job.setBehaviorType( BehaviorType.Delete);
        when(this.queueMock.pop()).thenReturn( job);

        Message message= fakeMessage();
        Behavior behavior= new Behavior( BehaviorType.Delete, "test-value");
        message.getBehavior().put(behavior.getBehaviorType(), behavior);
        when(this.resourceRoadieMock.accessMessage(
                any( Message.class),
                eq(job.getJobOwner()),
                eq(BehaviorType.Delete)
        )).thenReturn(message);
        when(this.processingResultMock.isContinueProcessing()).thenReturn(true);
        when(this.processingResultMock.getMessage()).thenReturn(message);
        List<ProcessingResult.NewMessage> messages= new ArrayList<>(0);
        when(this.processingResultMock.getMessages()).thenReturn(messages);

        this.processor.pullJob();

        verify(this.resourceRoadieMock).deleteMessage(any(Message.class));
        verify(this.queueMock).complete( job);
    }
}
