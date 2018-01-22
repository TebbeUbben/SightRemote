package sugar.free.sightparser.authlayer;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.pipeline.ByteBuf;

public final class DataMessage extends AuthLayerMessage {

    @Getter
    @Setter
    private byte[] data;

    @Override
    protected byte getCommand() {
        return 0x03;
    }

    @Override
    protected void parse(ByteBuf byteBuf) {
        data = byteBuf.getBytes();
    }
}
