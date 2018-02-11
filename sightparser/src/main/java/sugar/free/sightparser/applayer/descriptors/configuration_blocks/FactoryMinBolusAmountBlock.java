package sugar.free.sightparser.applayer.descriptors.configuration_blocks;

import lombok.Getter;
import sugar.free.sightparser.RoundingUtil;
import sugar.free.sightparser.pipeline.ByteBuf;

public class FactoryMinBolusAmountBlock extends ConfigurationBlock {

    private static final long serialVersionUID = 1L;

    public static final short ID = 0x17EB;

    @Getter
    private float minimumAmount;

    @Override
    public short getID() {
        return ID;
    }

    @Override
    public void parse(ByteBuf byteBuf) {
        minimumAmount = RoundingUtil.roundFloat(((float) byteBuf.readUInt16LE()) / 100F, 2);
    }

    @Override
    public byte[] getData() {
        return null;
    }
}
