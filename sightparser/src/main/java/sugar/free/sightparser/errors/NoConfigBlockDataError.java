package sugar.free.sightparser.errors;

import sugar.free.sightparser.applayer.messages.AppLayerMessage;

public class NoConfigBlockDataError extends AppError {

    private static final long serialVersionUID = 1L;

    public NoConfigBlockDataError(Class<? extends AppLayerMessage> clazz, short error) {
        super(clazz, error);
    }
}
