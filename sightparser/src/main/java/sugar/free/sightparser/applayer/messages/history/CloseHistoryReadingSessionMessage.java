package sugar.free.sightparser.applayer.messages.history;

import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.descriptors.Service;

public class CloseHistoryReadingSessionMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

    @Override
    public Service getService() {
        return Service.HISTORY;
    }

    @Override
    public short getCommand() {
        return (short) 0xE797;
    }
}
