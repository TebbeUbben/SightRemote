package sugar.free.sightparser.authlayer;

import lombok.Getter;
import sugar.free.sightparser.pipeline.ByteBuf;

public final class VerifyConfirmResponse extends AuthLayerMessage {

    @Getter
    private PairingStatus pairingStatus;

    @Override
    protected byte getCommand() {
        return 0x1E;
    }

    @Override
    protected void parse(ByteBuf byteBuf) {
        pairingStatus = PairingStatus.getByValue(byteBuf.getShort());
    }
}
