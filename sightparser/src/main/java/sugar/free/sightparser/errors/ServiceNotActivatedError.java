package sugar.free.sightparser.errors;

import sugar.free.sightparser.applayer.messages.AppLayerMessage;

public class ServiceNotActivatedError extends AppError {

    private static final long serialVersionUID = 1L;

    public ServiceNotActivatedError(Class<? extends AppLayerMessage> clazz, short error) {
        super(clazz, error);
    }
}
