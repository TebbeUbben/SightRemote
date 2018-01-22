package sugar.free.sightparser.authlayer;

public final class DisconnectRequest extends AuthLayerMessage {
    @Override
    protected byte getCommand() {
        return 0x1B;
    }
}
