package sugar.free.sightparser.handling;

import java.util.concurrent.CountDownLatch;

import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.errors.CancelledException;
import sugar.free.sightparser.exceptions.DisconnectedException;
import sugar.free.sightparser.exceptions.TimeoutException;
import sugar.free.sightparser.pipeline.Status;

public abstract class TaskRunner {

    private SightServiceConnector serviceConnector;
    private ResultCallback resultCallback;
    private boolean run;
    private Object result;
    private Exception error;
    private CountDownLatch resultLatch = new CountDownLatch(1);
    private boolean cancelled;
    private boolean active;
    private boolean statusCallbackRegistered;

    public TaskRunner(SightServiceConnector serviceConnector) {
        this.serviceConnector = serviceConnector;
    }

    protected abstract AppLayerMessage run(AppLayerMessage message) throws Exception;

    protected void finish(Object result) {
        active = false;
        resultCallback.onResult(result);
        if (statusCallbackRegistered) serviceConnector.removeStatusCallback(statusCallback);
    }

    public void fetch(ResultCallback resultCallback) {
        if (run) throw new IllegalStateException("TaskRunners can only be run once.");
        run = true;
        this.resultCallback = resultCallback;
        active = true;
        if (serviceConnector.getStatus() == Status.CONNECTED || serviceConnector.getStatus() == Status.WAITING)
            messageCallback.onMessage((AppLayerMessage) null);
        else messageCallback.onError(new DisconnectedException());
    }

    public Object fetchAndWaitUsingLatch() throws Exception {
        fetch(new BlockingCallResultCallback(this));
        resultLatch.wait();
        if (error != null) throw error;
        return result;
    }

    public Object fetchAndWaitUsingLoop(long frequency, long timeout) throws Exception {
        fetch(new BlockingCallResultCallback(this));
        long timeStarted = System.currentTimeMillis();
        while (true) {
            Thread.sleep(frequency);
            if (error != null) throw error;
            if (result != null) return result;
            if (timeout > 0 && System.currentTimeMillis() - timeStarted >= timeout) throw new TimeoutException();
        }
    }

    public void cancel() {
        if (resultCallback != null) {
            cancelled = true;
            active = false;
            messageCallback.onError(new CancelledException());
        }
    }

    private MessageCallback messageCallback = new MessageCallback() {
        @Override
        public void onMessage(AppLayerMessage message) {
            if (!cancelled && active) {
                try {
                    AppLayerMessage next = run(message);
                    if (next != null) serviceConnector.requestMessage(next, this);
                } catch (Exception e) {
                    onError(e);
                }
            }
        }

        @Override
        public void onError(Exception error) {
            serviceConnector.removeStatusCallback(statusCallback);
            if (active) {
                active = false;
                resultCallback.onError(error);
            }
        }
    };

    private StatusCallback statusCallback = new StatusCallback() {
        @Override
        public void onStatusChange(Status status, long statusTime, long waitTime) {
            if (status == Status.CONNECTED) {
                serviceConnector.removeStatusCallback(this);
                statusCallbackRegistered = false;
                messageCallback.onMessage((AppLayerMessage) null);
            } else if (status == Status.DISCONNECTED || status == Status.WAITING) {
                messageCallback.onError(new DisconnectedException());
            }
        }
    };

    public interface ResultCallback {
        void onResult(Object result);
        void onError(Exception e);
    }

    private static class BlockingCallResultCallback implements ResultCallback {

        private TaskRunner taskRunner;

        BlockingCallResultCallback(TaskRunner taskRunner) {
            this.taskRunner = taskRunner;
        }

        @Override
        public void onResult(Object result) {
            taskRunner.result = result;
            taskRunner.resultLatch.countDown();
        }

        @Override
        public void onError(Exception e) {
            taskRunner.error = e;
            taskRunner.resultLatch.countDown();
        }
    }
}
