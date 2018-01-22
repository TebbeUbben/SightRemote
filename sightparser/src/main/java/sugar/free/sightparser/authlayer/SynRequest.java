package sugar.free.sightparser.authlayer;

public final class SynRequest extends AuthLayerMessage {
    @Override
    protected byte getCommand() {
        return 0x17;
    }
}
