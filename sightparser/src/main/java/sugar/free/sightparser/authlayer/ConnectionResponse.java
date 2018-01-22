package sugar.free.sightparser.authlayer;

public final class ConnectionResponse extends CRCAuthLayerMessage {
    @Override
    protected byte getCommand() {
        return 0x0A;
    }
}
