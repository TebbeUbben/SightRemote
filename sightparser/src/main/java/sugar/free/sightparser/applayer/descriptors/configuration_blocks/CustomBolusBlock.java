package sugar.free.sightparser.applayer.descriptors.configuration_blocks;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.Helpers;
import sugar.free.sightparser.applayer.descriptors.HistoryBolusType;
import sugar.free.sightparser.pipeline.ByteBuf;

@Getter
@Setter
public abstract class CustomBolusBlock extends ConfigurationBlock {

    private static final long serialVersionUID = 1L;

    private HistoryBolusType bolusType;
    private double immediateAmount;
    private double extendedAmount;
    private int duration;
    private boolean configured;

    @Override
    public byte[] getData() {
        ByteBuf byteBuf = new ByteBuf(14);
        byteBuf.putShort(bolusType.getValue());
        byteBuf.putUInt16LE(Helpers.roundDoubleToInt(immediateAmount * 100D));
        byteBuf.putUInt16LE(Helpers.roundDoubleToInt(extendedAmount * 100D));
        byteBuf.putShort((short) 0x0000);
        byteBuf.putUInt16LE(duration);
        byteBuf.putBoolean(configured);
        return byteBuf.getBytes();
    }

    @Override
    public void parse(ByteBuf byteBuf) {
        bolusType = HistoryBolusType.getBolusType(byteBuf.readShort());
        immediateAmount = Helpers.roundDouble(((double) byteBuf.readUInt16LE()) / 100D);
        extendedAmount = Helpers.roundDouble(((double) byteBuf.readUInt16LE()) / 100D);
        byteBuf.shift(2);
        duration = byteBuf.readUInt16LE();
        configured = byteBuf.readBoolean();
    }
}
