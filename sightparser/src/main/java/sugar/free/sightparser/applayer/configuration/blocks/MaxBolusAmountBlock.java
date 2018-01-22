package sugar.free.sightparser.applayer.configuration.blocks;

import lombok.Getter;
import sugar.free.sightparser.applayer.configuration.ConfigurationBlock;
import sugar.free.sightparser.pipeline.ByteBuf;

public class MaxBolusAmountBlock extends ConfigurationBlock {

    public static final short ID = 0x1F00;

    @Getter
    private float maximumAmount;

    @Override
    public short getID() {
        return ID;
    }

    @Override
    public void parse(ByteBuf byteBuf) {
        maximumAmount = ((float) byteBuf.readShortLE()) / 100F;
    }

    @Override
    public byte[] getData() {
        ByteBuf byteBuf = new ByteBuf(2);
        byteBuf.putShortLE((short) (maximumAmount * 100F));
        return byteBuf.getBytes();
    }
}
