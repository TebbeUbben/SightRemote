package sugar.free.sightparser.error;

import lombok.Getter;
import sugar.free.sightparser.applayer.AppLayerMessage;

public class UnknownAppErrorCodeError extends SightError {

    @Getter
    private Class<? extends AppLayerMessage> clazz;
    @Getter
    private short error;

    public UnknownAppErrorCodeError(Class<? extends AppLayerMessage> clazz, short error) {
        this.clazz = clazz;
        this.error = error;
    }

    @Override
    public String getMessage() {
        return "Class: " + clazz.getCanonicalName() + " Error: " + error;
    }
}
