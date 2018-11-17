package sugar.free.sightparser.applayer.descriptors.configuration_blocks;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.pipeline.ByteBuf;

public class TBROverNotificationBlock extends ConfigurationBlock {

    private static final long serialVersionUID = 1L;

    public static short ID = (short) 0xD664;

    @Getter
    @Setter
    private boolean enabled;
    @Getter
    @Setter
    private short melody;

    @Override
    public short getID() {
        return ID;
    }

    @Override
    public void parse(ByteBuf byteBuf) {
        enabled = byteBuf.readBoolean();
        melody = byteBuf.readShort();
    }

    @Override
    public byte[] getData() {
        ByteBuf byteBuf = new ByteBuf(4);
        byteBuf.putBoolean(enabled);
        byteBuf.putShort(melody);
        return byteBuf.getBytes();
    }
}
