package sugar.free.sightparser.applayer.descriptors.configuration_blocks;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.RoundingUtil;
import sugar.free.sightparser.pipeline.ByteBuf;

public class FactoryMinBRAmountBlock extends ConfigurationBlock {

    private static final long serialVersionUID = 1L;

    public static final short ID = (short) 0xEBEB;

    @Getter
    @Setter
    private float minimumAmount;

    @Override
    public short getID() {
        return ID;
    }

    @Override
    public void parse(ByteBuf byteBuf) {
        minimumAmount = RoundingUtil.roundFloat(byteBuf.readUInt16LE() / 100F, 2);
    }

    @Override
    public byte[] getData() {
        return null;
    }
}
