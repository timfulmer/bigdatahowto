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

        this.queue= new Queue(this.resourceMock) {
            @Override
            protected void write(UUID uuid) {
                jobs.add(uuid);
            }

            @Override
            protected ResultTuple read() {
                UUID uuid= popUuid();
                return uuid== null ? null :
                        new ResultTuple(uuid, uuid.toString());
            }

            @Override
            protected void delete(String identifier) {
                if( identifier!= null){
                    jobs.remove( UUID.fromString( identifier));
                }
            }
        };
    }

    @Test
    public void testQueue(){

        Message message= fakeMessage();
        String authentication= "test-authentication";
        UUID jobUuid= UUID.randomUUID();
        this.queue.push(jobUuid, message, BehaviorType.Persist, authentication);

        Job job= fakeJob();
        job.setUuid( popUuid());
        job.setState(JobState.Queued);
        when(this.resourceMock.get( job)).thenReturn( job);
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

        verify( this.resourceMock, times(4)).put(job);
    }

    @Test
    public void testQueue_ErrorMessage(){

        Job job= fakeJob();
        job.setState( JobState.Processing);
        this.queue.error(job, "test-message");
    }

    @Test
    public void testComplete(){

        Job job= fakeJob();
        job.setState( JobState.Processing);
        this.queue.complete( job);
        assert JobState.Complete.equals(job.getState()):
                "Queue.complete is not implemented correctly.";
        assert !this.jobs.contains( job.getUuid()):
                "Queue.complete is not implemented correctly.";

        verify( this.resourceMock).put( job);
    }

    @Test
    public void testKeySynchronization(){

        Message message= fakeMessage();
        String authentication= "test-authentication";
        UUID jobUuid= UUID.randomUUID();
        this.queue.push(jobUuid, message, BehaviorType.Persist, authentication);
        this.queue.push(jobUuid, message, BehaviorType.Persist, authentication);

        Job job1= fakeJob();
        job1.setUuid(popUuid());
        job1.setState(JobState.Queued);
        Job job2= fakeJob();
        job2.setUuid(popUuid());
        job2.setState(JobState.Queued);
        when(this.resourceMock.get( any(Job.class))).thenReturn( job1, job2,
                job2);
        Job result= this.queue.pop();
        assert result.getUuid().equals(job1.getUuid()):
                "Queue.push is not writing job correctly.";
        assert JobState.Processing== job1.getState():
                "Queue.pop is not updating job state correctly.";

        result= this.queue.pop();
        assert result== null: "Job.pop is not synchronizing on key.";

        this.queue.clear();
        result= this.queue.pop();
        assert result!= null: "Job.clear is not clearing key cache.";
    }

    @Test
    public void testNullRead(){

        this.queue.pop();
    }

    private UUID popUuid(){
        return jobs.isEmpty() ? null : jobs.iterator().next();
    }
}
