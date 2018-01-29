package sugar.free.sightparser.applayer.messages.connection;

import org.spongycastle.util.encoders.Hex;

import lombok.Getter;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.descriptors.Service;
import sugar.free.sightparser.pipeline.ByteBuf;

public class BindMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

    @Getter
    private byte[] modelNumber;

    @Override
    public Service getService() {
        return Service.CONNECTION;
    }

    @Override
    public short getCommand() {
        return (short) 0xCDF3;
    }

    @Override
    protected byte[] getData() {
        return Hex.decode("3438310000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
    }

    @Override
    protected void parse(ByteBuf byteBuf) throws Exception {
        modelNumber = byteBuf.readBytesLE(16);
    }
}
