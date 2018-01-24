package sugar.free.sightparser.error;

import sugar.free.sightparser.applayer.AppLayerMessage;

public class BolusDurationLimitExceededError extends AppErrorCodeError {
    public BolusDurationLimitExceededError(Class<? extends AppLayerMessage> clazz, short error) {
        super(clazz, error);
    }
}
