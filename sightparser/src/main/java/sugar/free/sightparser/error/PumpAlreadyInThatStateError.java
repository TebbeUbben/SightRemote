package sugar.free.sightparser.error;

import sugar.free.sightparser.applayer.AppLayerMessage;

public class PumpAlreadyInThatStateError extends AppErrorCodeError {
    public PumpAlreadyInThatStateError(Class<? extends AppLayerMessage> clazz, short error) {
        super(clazz, error);
    }
}
