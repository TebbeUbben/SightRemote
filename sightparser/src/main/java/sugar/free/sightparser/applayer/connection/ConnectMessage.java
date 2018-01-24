package sugar.free.sightparser.applayer.connection;

import org.spongycastle.util.encoders.Hex;

import sugar.free.sightparser.applayer.AppLayerMessage;
import sugar.free.sightparser.applayer.Service;
import sugar.free.sightparser.error.SightError;
import sugar.free.sightparser.error.UnknownAppErrorCodeError;
import sugar.free.sightparser.pipeline.ByteBuf;

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
