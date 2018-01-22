package sugar.free.sightparser.authlayer;

import lombok.Getter;
import sugar.free.sightparser.pipeline.ByteBuf;

public final class ErrorMessage extends AuthLayerMessage {

    @Getter
    private byte[] errorCode;

    @Override
    protected byte getCommand() {
        return 0x06;
    }

    @Override
    protected void parse(ByteBuf byteBuf) {
        errorCode = byteBuf.getBytes();
    }
}
