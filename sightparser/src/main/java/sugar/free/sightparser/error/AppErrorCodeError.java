package sugar.free.sightparser.error;

import lombok.Getter;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;

public abstract class AppErrorCodeError extends SightError {

    @Getter
    private Class<? extends AppLayerMessage> clazz;
    @Getter
    private short error;

    public AppErrorCodeError(Class<? extends AppLayerMessage> clazz, short error) {
        this.clazz = clazz;
        this.error = error;
    }

    @Override
    public String getMessage() {
        return "Class: " + clazz.getCanonicalName() + " Error: " + error;
    }

}
