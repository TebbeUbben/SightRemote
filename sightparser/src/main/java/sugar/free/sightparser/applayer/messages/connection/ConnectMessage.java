package sugar.free.sightparser.applayer.messages.connection;

import org.spongycastle.util.encoders.Hex;

import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.descriptors.Service;

public class ConnectMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

    @Override
    public Service getService() {
        return Service.CONNECTION;
    }

    @Override
    public short getCommand() {
        return 0x0BF0;
    }

    @Override
    protected byte[] getData() {
        return Hex.decode("0000080100196000");
    }
}
