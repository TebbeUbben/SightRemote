package sugar.free.sightparser.applayer.messages.remote_control;

import lombok.Setter;
import sugar.free.sightparser.pipeline.ByteBuf;

public class StandardBolusMessage extends BolusMessage {

    private static final long serialVersionUID = 1L;

    @Setter
    private double amount;

    @Override
    protected byte[] getData() throws Exception {
        ByteBuf data = new ByteBuf(22);
        data.putShort((short) 0x2503);
        data.putShort((short) 0x1F00);
        data.putShort((short) 0x1F00);
        data.putShort((short) 0x0000);
        data.putUInt16LE((int) (amount * 100D));
        data.putShort((short) 0x0000);
        data.putShort((short) 0x0000);
        data.putShort((short) 0x0000);
        data.putUInt16LE((int) (amount * 100D));
        data.putShort((short) 0x0000);
        data.putShort((short) 0x0000);
        return data.getBytes();
    }
}
