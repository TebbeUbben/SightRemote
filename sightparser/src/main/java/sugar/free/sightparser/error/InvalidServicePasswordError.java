package sugar.free.sightparser.error;

import sugar.free.sightparser.applayer.AppLayerMessage;

public class InvalidServicePasswordError extends AppErrorCodeError {

    private static final long serialVersionUID = 1L;

    public InvalidServicePasswordError(Class<? extends AppLayerMessage> clazz, short error) {
        super(clazz, error);
    }
}
