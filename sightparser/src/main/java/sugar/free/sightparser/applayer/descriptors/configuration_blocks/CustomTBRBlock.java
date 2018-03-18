package sugar.free.sightparser.applayer.descriptors.configuration_blocks;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.pipeline.ByteBuf;

@Getter
@Setter
public abstract class CustomTBRBlock extends ConfigurationBlock {

    private static final long serialVersionUID = 1L;

    private int amount;
    private int duration;
    private boolean configured;

    @Override
    public byte[] getData() {
        ByteBuf byteBuf = new ByteBuf(6);
        byteBuf.putUInt16LE(amount);
        byteBuf.putUInt16LE(duration);
        byteBuf.putBoolean(configured);
        return byteBuf.getBytes();
    }

    @Override
    public void parse(ByteBuf byteBuf) {
        amount = byteBuf.readUInt16LE();
        duration = byteBuf.readUInt16LE();
        configured = byteBuf.readBoolean();
    }
}
