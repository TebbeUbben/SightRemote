package sugar.free.sightparser.authlayer;

public final class VerifyDisplayResponse extends AuthLayerMessage {

    @Override
    protected byte getCommand() {
        return 0x14;
    }
}
