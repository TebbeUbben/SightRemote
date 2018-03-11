package sugar.free.sightparser.applayer.descriptors.history_frames;

import lombok.Getter;
import sugar.free.sightparser.Helpers;
import sugar.free.sightparser.applayer.descriptors.HistoryBolusType;
import sugar.free.sightparser.pipeline.ByteBuf;

@Getter
public class BolusProgrammedFrame extends HistoryFrame {

    private static final long serialVersionUID = 1L;

    private HistoryBolusType bolusType;
    private double immediateAmount;
    private double extendedAmount;
    private int duration;
    private int bolusId;

    @Override
    public void parse(ByteBuf byteBuf) {
        bolusType = HistoryBolusType.getBolusType(byteBuf.readShort());
        immediateAmount = Helpers.roundDouble(((double) byteBuf.readUInt16LE()) / 100D);
        extendedAmount =  Helpers.roundDouble(((double) byteBuf.readUInt16LE()) / 100D);
        duration = byteBuf.readUInt16LE();
        byteBuf.shift(4);
        bolusId = byteBuf.readUInt16LE();
    }
}
