package sugar.free.sightparser.errors;

import sugar.free.sightparser.applayer.messages.AppLayerMessage;

public class ServiceIncompatibleError extends AppError {

    private static final long serialVersionUID = 1L;

    public ServiceIncompatibleError(Class<? extends AppLayerMessage> clazz, short error) {
        super(clazz, error);
    }
}
