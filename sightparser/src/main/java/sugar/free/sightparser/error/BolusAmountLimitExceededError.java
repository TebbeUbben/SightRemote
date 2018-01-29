package sugar.free.sightparser.error;

import sugar.free.sightparser.applayer.messages.AppLayerMessage;

public class BolusAmountLimitExceededError extends AppErrorCodeError {

    private static final long serialVersionUID = 1L;

    public BolusAmountLimitExceededError(Class<? extends AppLayerMessage> clazz, short error) {
        super(clazz, error);
    }
}
