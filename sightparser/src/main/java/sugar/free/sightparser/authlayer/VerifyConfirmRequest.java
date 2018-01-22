package sugar.free.sightparser.authlayer;

import lombok.Setter;
import sugar.free.sightparser.pipeline.ByteBuf;

public final class VerifyConfirmRequest extends AuthLayerMessage {

    @Setter
    private PairingStatus pairingStatus;

    @Override
    protected byte getCommand() {
        return 0x0E;
    }

    @Override
    protected byte[] getData() {
        ByteBuf byteBuf = new ByteBuf(2);
        byteBuf.putShort(pairingStatus.getValue());
        return byteBuf.getBytes();
    }
}
