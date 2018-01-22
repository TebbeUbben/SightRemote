package sugar.free.sightparser.applayer.configuration.blocks;

import lombok.Getter;
import sugar.free.sightparser.applayer.configuration.ConfigurationBlock;
import sugar.free.sightparser.pipeline.ByteBuf;

public class FactoryMinBolusAmountBlock extends ConfigurationBlock {

    public static final short ID = 0x17EB;

    @Getter
    private float minimumAmount;

    @Override
    public short getID() {
        return ID;
    }

    @Override
    public void parse(ByteBuf byteBuf) {
        minimumAmount = ((float) byteBuf.readShortLE()) / 100F;
    }

    @Override
    public byte[] getData() {
        ByteBuf byteBuf = new ByteBuf(2);
        byteBuf.putShort((short) (minimumAmount * 100F));
        return byteBuf.getBytes();
    }
}
