package sugar.free.sightparser.error;

import lombok.Getter;
import sugar.free.sightparser.applayer.AppLayerMessage;

public class UnknownAppErrorCodeError extends AppErrorCodeError {
    public UnknownAppErrorCodeError(Class<? extends AppLayerMessage> clazz, short error) {
        super(clazz, error);
    }
}
