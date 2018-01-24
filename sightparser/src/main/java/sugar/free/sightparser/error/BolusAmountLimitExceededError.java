package sugar.free.sightparser.error;

import sugar.free.sightparser.applayer.AppLayerMessage;

public class BolusAmountLimitExceededError extends AppErrorCodeError {
    public BolusAmountLimitExceededError(Class<? extends AppLayerMessage> clazz, short error) {
        super(clazz, error);
    }
}
