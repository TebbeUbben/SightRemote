package sugar.free.sightparser.applayer.configuration.blocks;

import lombok.Getter;
import sugar.free.sightparser.applayer.configuration.ConfigurationBlock;
import sugar.free.sightparser.pipeline.ByteBuf;

public class FactoryMaxBolusAmountBlock extends ConfigurationBlock {

    private static final long serialVersionUID = 1L;

    public static final short ID = 0x06A1;

    @Getter
    private static float maximumAmount;

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
        byteBuf.putShort((short) (maximumAmount * 100F));
        return byteBuf.getBytes();
    }
}
