package sugar.free.sightparser.applayer.descriptors.history_frames;

import lombok.Getter;
import sugar.free.sightparser.RoundingUtil;
import sugar.free.sightparser.applayer.descriptors.HistoryBolusType;
import sugar.free.sightparser.pipeline.ByteBuf;

@Getter
public class BolusProgrammedFrame extends HistoryFrame {

    private static final long serialVersionUID = 1L;

    private HistoryBolusType bolusType;
    private float immediateAmount;
    private float extendedAmount;
    private int duration;
    private int bolusId;

    @Override
    public void parse(ByteBuf byteBuf) {
        bolusType = HistoryBolusType.getBolusType(byteBuf.readShort());
        immediateAmount = RoundingUtil.roundFloat(((float) byteBuf.readUInt16LE()) / 100F,2 );
        extendedAmount =  RoundingUtil.roundFloat(((float) byteBuf.readUInt16LE()) / 100F, 2);
        duration = byteBuf.readUInt16LE();
        byteBuf.shift(4);
        bolusId = byteBuf.readUInt16LE();
    }
}
