package sugar.free.sightparser.applayer.configuration.blocks;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.applayer.configuration.ConfigurationBlock;
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
        minimumAmount = byteBuf.readShortLE() / 100F;
    }

    @Override
    public byte[] getData() {
        ByteBuf byteBuf = new ByteBuf(2);
        byteBuf.putShortLE((short) (minimumAmount * 100F));
        return byteBuf.getBytes();
    }
}
