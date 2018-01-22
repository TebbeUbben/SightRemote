package sugar.free.sightparser.handling.taskrunners;

import java.io.Serializable;

import lombok.Getter;
import sugar.free.sightparser.applayer.AppLayerMessage;
import sugar.free.sightparser.applayer.status.DateTimeMesssage;
import sugar.free.sightparser.applayer.status.FirmwareVersionMessage;
import sugar.free.sightparser.applayer.status.WarrantyTimerMessage;
import sugar.free.sightparser.handling.SightServiceConnector;
import sugar.free.sightparser.handling.TaskRunner;

public class InfoTaskRunner extends TaskRunner {

    private InfoResult infoResult = new InfoResult();

    public InfoTaskRunner(SightServiceConnector serviceConnector) {
        super(serviceConnector);
    }

    @Override
    protected AppLayerMessage run(AppLayerMessage message) throws Exception {
        if (message == null) return new DateTimeMesssage();
        else if (message instanceof DateTimeMesssage) {
            infoResult.dateTimeMesssage = (DateTimeMesssage) message;
            return new FirmwareVersionMessage();
        } else if (message instanceof FirmwareVersionMessage) {
            infoResult.firmwareVersionMessage = (FirmwareVersionMessage) message;
            return new WarrantyTimerMessage();
        } else if (message instanceof WarrantyTimerMessage) {
            infoResult.warrantyTimerMessage = (WarrantyTimerMessage) message;
            finish(infoResult);
        }
        return null;
    }

    @Getter
    public static final class InfoResult implements Serializable {
        private DateTimeMesssage dateTimeMesssage;
        private FirmwareVersionMessage firmwareVersionMessage;
        private WarrantyTimerMessage warrantyTimerMessage;
    }
}
