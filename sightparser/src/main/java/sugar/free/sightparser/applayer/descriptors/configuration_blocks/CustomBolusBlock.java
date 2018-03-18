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
    private double amount;
    private long duration;
    private boolean configured;

    @Override
    public byte[] getData() {
        ByteBuf byteBuf = new ByteBuf(14);
        byteBuf.putShort(bolusType.getValue());
        byteBuf.putUInt32LE(Helpers.roundDoubleToInt(amount * 100D));
        byteBuf.putUInt32LE(duration);
        byteBuf.putBoolean(configured);
        return byteBuf.getBytes();
    }

    @Override
    public void parse(ByteBuf byteBuf) {
        bolusType = HistoryBolusType.getBolusType(byteBuf.readShort());
        amount = Helpers.roundDouble(((double) byteBuf.readUInt32LE()) / 100D);
        duration = byteBuf.readUInt32LE();
        configured = byteBuf.readBoolean();
    }
}
