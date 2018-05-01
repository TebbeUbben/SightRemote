package sugar.free.sightparser.errors;

import sugar.free.sightparser.applayer.messages.AppLayerMessage;

public class ImplausiblePortionLengthValueError extends AppError {

    private static final long serialVersionUID = 1L;

    public ImplausiblePortionLengthValueError(Class<? extends AppLayerMessage> clazz, short error) {
        super(clazz, error);
    }
}
