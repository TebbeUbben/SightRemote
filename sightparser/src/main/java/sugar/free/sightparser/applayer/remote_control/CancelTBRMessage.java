package sugar.free.sightparser.applayer.remote_control;

import sugar.free.sightparser.applayer.AppLayerMessage;
import sugar.free.sightparser.applayer.Service;

public class CancelTBRMessage extends AppLayerMessage {

    @Override
    public Service getService() {
        return Service.REMOTE_CONTROL;
    }

    @Override
    public short getCommand() {
        return 0x3918;
    }
}
