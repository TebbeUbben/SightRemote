package sugar.free.sightparser.applayer.descriptors.configuration_blocks;

import lombok.Getter;
import sugar.free.sightparser.RoundingUtil;
import sugar.free.sightparser.pipeline.ByteBuf;

public class MaxBolusAmountBlock extends ConfigurationBlock {

    private static final long serialVersionUID = 1L;

    public static final short ID = 0x1F00;

    @Getter
    private double maximumAmount;

    @Override
    public short getID() {
        return ID;
    }

    @Override
    public void parse(ByteBuf byteBuf) {
        maximumAmount = RoundingUtil.roundDouble(((double) byteBuf.readUInt16LE()) / 100D, 2);
    }

    @Override
    public byte[] getData() {
        ByteBuf byteBuf = new ByteBuf(2);
        byteBuf.putUInt16LE((short) (maximumAmount * 100F));
        return byteBuf.getBytes();
    }
}
