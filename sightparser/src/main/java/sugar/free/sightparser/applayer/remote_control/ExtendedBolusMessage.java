package sugar.free.sightparser.applayer.remote_control;

import lombok.Setter;
import sugar.free.sightparser.pipeline.ByteBuf;

public class ExtendedBolusMessage extends BolusMessage {

    private static final long serialVersionUID = 1L;

    @Setter
    private float amount;
    @Setter
    private short duration;

    @Override
    protected byte[] getData() throws Exception {
        ByteBuf data = new ByteBuf(22);
        data.putShort((short) 0x2503);
        data.putShort((short) 0xE300);
        data.putShort((short) 0x1F00);
        data.putShort((short) 0x0000);
        data.putShort((short) 0x0000);
        data.putShortLE((short) (amount * 100F));
        data.putShortLE(duration);
        data.putShort((short) 0x0000);
        data.putShort((short) 0x0000);
        data.putShortLE((short) (amount * 100F));
        data.putShortLE(duration);
        return data.getBytes();
    }
}
