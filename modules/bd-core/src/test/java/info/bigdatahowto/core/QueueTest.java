package info.bigdatahowto.core;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static info.bigdatahowto.core.TestUtils.fakeJob;
import static info.bigdatahowto.core.TestUtils.fakeMessage;
import static org.mockito.Mockito.*;

/**
 * @author timfulmer
 */
public class QueueTest {

    private final Set<UUID> jobs= new HashSet<>();

    private Resource resourceMock;
    private Queue queue;

    @Before
    public void before(){

        this.resourceMock= mock( Resource.class);

        this.queue= new Queue() {
            @Override
            protected void write(UUID uuid) {
                jobs.add(uuid);
            }

            @Override
            protected UUID read() {
                return popUuid();
            }

            @Override
            protected void delete(UUID uuid) {
                jobs.remove( uuid);
            }
        };
        this.queue.setResource(this.resourceMock);
    }

    @Test
    public void testQueue(){

        Message message= fakeMessage();
        String authentication= "test-authentication";
        this.queue.push( message, authentication);

        Job job= fakeJob();
        job.setUuid( popUuid());
        job.setState(JobState.Queued);
        when(this.resourceMock.get( popUuid().toString(), Job.class)
                ).thenReturn( job);
        Job result= this.queue.pop();
        assert result.getUuid().equals(job.getUuid()):
                "Queue.push is not writing job correctly.";
        assert JobState.Processing== job.getState():
                "Queue.pop is not updating job state correctly.";

        this.queue.error( job);
        assert this.jobs.size()== 0 && !this.jobs.contains( job.getUuid()):
                "Queue.error is not deleting job correctly.";
        assert JobState.Error== job.getState():
                "Queue.error is not updating job state correctly.";

        verify( this.resourceMock, times(2)).put(job);
    }

    @Test
    public void testQueue_ErrorMessage(){

        Job job= fakeJob();
        job.setState( JobState.Processing);
        this.queue.error(job, "test-message");
    }

    private final UUID popUuid(){
        return jobs.iterator().next();
    }
}
