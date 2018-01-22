package sugar.free.sightparser.authlayer;

public final class SynAckResponse extends AuthLayerMessage {

    @Override
    protected byte getCommand() {
        return 0x18;
    }
}
