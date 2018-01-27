package sugar.free.sightparser.applayer.configuration;

import sugar.free.sightparser.applayer.AppLayerMessage;
import sugar.free.sightparser.applayer.Service;

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
