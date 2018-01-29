package sugar.free.sightparser.applayer.messages.configuration;

import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.descriptors.Service;

public class CloseWriteSessionMessage extends AppLayerMessage {
    @Override
    public Service getService() {
        return Service.CONFIGURATION;
    }

    @Override
    public short getCommand() {
        return (short) 0xB51E;
    }
}
