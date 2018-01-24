package sugar.free.sightparser.error;

import lombok.Getter;
import sugar.free.sightparser.applayer.AppLayerMessage;

public class UnknownAppErrorCodeError extends AppErrorCodeError {

    private static final long serialVersionUID = 1L;

    public UnknownAppErrorCodeError(Class<? extends AppLayerMessage> clazz, short error) {
        super(clazz, error);
    }
}
