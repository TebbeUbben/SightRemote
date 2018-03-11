package sugar.free.sightparser.applayer.descriptors.configuration_blocks;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.Helpers;
import sugar.free.sightparser.pipeline.ByteBuf;

public class FactoryMaxBRAmountBlock extends ConfigurationBlock {

    private static final long serialVersionUID = 1L;

    public static final short ID = 0x19A1;

    @Getter
    @Setter
    private double maximumAmount;

    @Override
    public short getID() {
        return ID;
    }

    @Override
    public void parse(ByteBuf byteBuf) {
        maximumAmount = Helpers.roundDouble(((double) byteBuf.readUInt16LE()) / 100D);
    }

    @Override
    public byte[] getData() {
        return null;
    }
}
