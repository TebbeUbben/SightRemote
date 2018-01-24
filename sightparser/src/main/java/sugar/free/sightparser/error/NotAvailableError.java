package sugar.free.sightparser.error;

import sugar.free.sightparser.applayer.AppLayerMessage;

public class NotAvailableError extends AppErrorCodeError {
    public NotAvailableError(Class<? extends AppLayerMessage> clazz, short error) {
        super(clazz, error);
    }
}
