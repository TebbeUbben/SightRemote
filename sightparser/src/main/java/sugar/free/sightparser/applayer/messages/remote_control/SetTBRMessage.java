package sugar.free.sightparser.applayer.messages.remote_control;

import lombok.Setter;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.descriptors.Service;
import sugar.free.sightparser.pipeline.ByteBuf;

public class SetTBRMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

    @Setter
    private short amount;
    @Setter
    private short duration;

    @Override
    public Service getService() {
        return Service.REMOTE_CONTROL;
    }

    @Override
    public short getCommand() {
        return (short) 0xC518;
    }

    @Override
    protected byte[] getData() throws Exception {
        ByteBuf byteBuf = new ByteBuf(6);
        byteBuf.putShortLE(amount);
        byteBuf.putShortLE(duration);
        byteBuf.putShort((short) 0x1F00);
        return byteBuf.getBytes();
    }

    @Override
    protected boolean outCRC() {
        return true;
    }
}
