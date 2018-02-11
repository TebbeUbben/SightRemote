package sugar.free.sightparser.applayer.descriptors.history_frames;

import lombok.Getter;
import sugar.free.sightparser.BOCUtil;
import sugar.free.sightparser.RoundingUtil;
import sugar.free.sightparser.applayer.descriptors.HistoryBolusType;
import sugar.free.sightparser.pipeline.ByteBuf;

@Getter
public class BolusDeliveredFrame extends HistoryFrame {

    private static final long serialVersionUID = 1L;

    private HistoryBolusType bolusType;
    private int startHour;
    private int startMinute;
    private int startSecond;
    private float immediateAmount;
    private float extendedAmount;
    private int duration;
    private int bolusId;

    @Override
    public void parse(ByteBuf byteBuf) {
        bolusType = HistoryBolusType.getBolusType(byteBuf.readShort());
        byteBuf.shift(1);
        startHour = BOCUtil.parseBOC(byteBuf.readByte());
        startMinute = BOCUtil.parseBOC(byteBuf.readByte());
        startSecond = BOCUtil.parseBOC(byteBuf.readByte());
        immediateAmount = RoundingUtil.roundFloat(((float) byteBuf.readUInt16LE()) / 100F, 2);
        extendedAmount = RoundingUtil.roundFloat(((float) byteBuf.readUInt16LE()) / 100F, 2);
        duration = byteBuf.readUInt16LE();
        byteBuf.shift(2);
        bolusId = byteBuf.readUInt16LE();
    }
}
