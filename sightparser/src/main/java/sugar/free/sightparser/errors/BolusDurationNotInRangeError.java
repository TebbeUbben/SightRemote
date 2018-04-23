package sugar.free.sightparser.errors;

import sugar.free.sightparser.applayer.messages.AppLayerMessage;

public class BolusDurationNotInRangeError extends AppError {

    private static final long serialVersionUID = 1L;

    public BolusDurationNotInRangeError(Class<? extends AppLayerMessage> clazz, short error) {
        super(clazz, error);
    }
}
