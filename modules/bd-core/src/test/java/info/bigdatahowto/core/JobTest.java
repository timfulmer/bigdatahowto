package info.bigdatahowto.core;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static info.bigdatahowto.core.TestUtils.fakeJob;
import static info.bigdatahowto.core.TestUtils.fakeMessage;

/**
 * @author timfulmer
 */
public class JobTest {

    @Test
    public void testJob(){

        Message message= fakeMessage();
        Job job= fakeJob( message);

        assert message.getMessageKey().equals(job.getMessageKey()):
                "Job.messageKey is not set correctly.";
        assert job.getTries()== 0:
                "Job.tries is not initialized correctly.";
        assert job.getState()== JobState.Created:
                "Job.jobState is not initialized correctly.";
        assert job.getStatus()== null:
                "Job.status is not initialized correctly.";

        //tf - Test mutation.
        MessageKey messageKey= new MessageKey();
        job.setMessageKey(messageKey);
        Integer tries= 0;
        job.setTries( tries);
        job.setState( JobState.Complete);
        String status="test-status";
        job.setStatus( status);
        assert !message.getMessageKey().equals(job.getMessageKey())
                && messageKey.equals( job.getMessageKey()):
                "Job.messageKey is not set correctly.";
        assert tries.equals(job.getTries()):
                "Job.tries is not set correctly.";
        assert JobState.Complete.equals( job.getState()):
                "Job.state is not set correctly.";
        assert status.equals( job.getStatus()):
                "Job.status is not set correctly.";
    }

    @Test
    public void testIncrementTries(){

        Job job= fakeJob(fakeMessage());
        int tries= job.getTries();
        job.incrementTries();
        assert tries+1== job.getTries():
                "Job.incrementTries is not implemented correctly.";
    }

    @Test
    public void testToQueued() throws NoSuchMethodException {

        Message message= fakeMessage();
        Job job= fakeJob(message);

        this.tryState(JobState.Queued, job, job.getClass().getMethod( "toQueued"));
        this.tryState(JobState.Processing, job, job.getClass().getMethod( "toQueued"));
        this.tryState(JobState.Complete, job, job.getClass().getMethod( "toQueued"));
        this.tryState(JobState.Error, job, job.getClass().getMethod( "toQueued"));

        job.setState(JobState.Created);
        job.toQueued();
    }

    @Test
    public void testToProcessing() throws NoSuchMethodException {

        Message message= fakeMessage();
        Job job= fakeJob( message);

        this.tryState(JobState.Created, job, job.getClass().getMethod( "toProcessing"));
        this.tryState(JobState.Processing, job, job.getClass().getMethod( "toProcessing"));
        this.tryState(JobState.Complete, job, job.getClass().getMethod( "toProcessing"));
        this.tryState(JobState.Error, job, job.getClass().getMethod( "toProcessing"));

        int tries= job.getTries();
        job.setState(JobState.Queued);
        job.toProcessing();
        assert tries+1== job.getTries():
                "Job.toProcessing is not incrementing tries";
    }

    @Test
    public void testToComplete() throws NoSuchMethodException {

        Message message= fakeMessage();
        Job job= fakeJob( message);

        this.tryState(JobState.Created, job, job.getClass().getMethod( "toComplete"));
        this.tryState(JobState.Queued, job, job.getClass().getMethod( "toComplete"));
        this.tryState(JobState.Complete, job, job.getClass().getMethod( "toComplete"));
        this.tryState(JobState.Error, job, job.getClass().getMethod( "toComplete"));

        job.setState(JobState.Processing);
        job.toComplete();
    }

    @Test
    public void testToError() throws NoSuchMethodException {

        Message message= fakeMessage();
        Job job= fakeJob( message);

        this.tryState(JobState.Created, job, job.getClass().getMethod( "toError"));
        this.tryState(JobState.Queued, job, job.getClass().getMethod( "toError"));
        this.tryState(JobState.Complete, job, job.getClass().getMethod( "toError"));
        this.tryState(JobState.Error, job, job.getClass().getMethod( "toError"));

        job.setState(JobState.Processing);
        job.toError();
    }

    @Test
    public void testResourceKey(){

        Job job= fakeJob( fakeMessage());
        assert job.getUuid().toString().equals( job.resourceKey()):
                "Job.resourceKey is not implemented correctly.";
    }

    private void tryState(JobState state, Job job, Method stateChange) {
        try{
            job.setState( state);
            stateChange.invoke(job);
            throw new RuntimeException(
                    "Job.toQueued allows from JobState.Queued.");
        } catch (InvocationTargetException | IllegalAccessException e) {
            // Noop.
        }
    }
}
