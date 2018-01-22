package sugar.free.sightparser.authlayer;

public final class ConnectionRequest extends CRCAuthLayerMessage {
    @Override
    protected byte getCommand() {
        return 0x09;
    }
}
