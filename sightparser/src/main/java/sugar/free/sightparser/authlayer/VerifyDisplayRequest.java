package sugar.free.sightparser.authlayer;

public final class VerifyDisplayRequest extends AuthLayerMessage {
    @Override
    protected byte getCommand() {
        return 0x12;
    }
}
