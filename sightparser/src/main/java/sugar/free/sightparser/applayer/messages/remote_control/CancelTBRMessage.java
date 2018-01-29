package sugar.free.sightparser.applayer.messages.remote_control;

import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.descriptors.Service;

public class CancelTBRMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

    @Override
    public Service getService() {
        return Service.REMOTE_CONTROL;
    }

    @Override
    public short getCommand() {
        return 0x3918;
    }
}
