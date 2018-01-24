package sugar.free.sightparser.error;

import sugar.free.sightparser.applayer.AppLayerMessage;

public class InvalidServicePasswordError extends AppErrorCodeError {
    public InvalidServicePasswordError(Class<? extends AppLayerMessage> clazz, short error) {
        super(clazz, error);
    }
}
