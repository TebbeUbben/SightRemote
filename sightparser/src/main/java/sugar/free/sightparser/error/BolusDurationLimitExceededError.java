package sugar.free.sightparser.error;

import sugar.free.sightparser.applayer.messages.AppLayerMessage;

public class BolusDurationLimitExceededError extends AppErrorCodeError {

    private static final long serialVersionUID = 1L;

    public BolusDurationLimitExceededError(Class<? extends AppLayerMessage> clazz, short error) {
        super(clazz, error);
    }
}
