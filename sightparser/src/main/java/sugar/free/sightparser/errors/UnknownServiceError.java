package sugar.free.sightparser.errors;

import sugar.free.sightparser.applayer.messages.AppLayerMessage;

public class UnknownServiceError extends AppError {

    private static final long serialVersionUID = 1L;

    public UnknownServiceError(Class<? extends AppLayerMessage> clazz, short error) {
        super(clazz, error);
    }
}
