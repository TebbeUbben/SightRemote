package sugar.free.sightparser.authlayer;

import lombok.Getter;
import sugar.free.sightparser.pipeline.ByteBuf;

public final class KeyResponse extends CRCAuthLayerMessage {

    @Getter
    private byte[] randomData;
    @Getter
    private byte[] preMasterSecret;

    @Override
    protected byte getCommand() {
        return 0x11;
    }

    @Override
    protected void parse(ByteBuf byteBuf) {
        randomData = byteBuf.readBytes(28);
        byteBuf.shift(4); //Date
        preMasterSecret = byteBuf.getBytes(256);
    }
}
