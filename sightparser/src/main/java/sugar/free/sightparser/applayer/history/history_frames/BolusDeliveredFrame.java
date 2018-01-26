package sugar.free.sightparser.applayer.history.history_frames;

import android.util.Log;

import lombok.Getter;
import sugar.free.sightparser.BOCUtils;
import sugar.free.sightparser.applayer.descriptors.HistoryBolusType;
import sugar.free.sightparser.applayer.history.HistoryFrame;
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
    private short duration;
    private short bolusId;

    @Override
    public void parse(ByteBuf byteBuf) {
        bolusType = HistoryBolusType.getBolusType(byteBuf.readShort());
        byteBuf.shift(1);
        startHour = BOCUtils.parseBOC(byteBuf.readByte());
        startMinute = BOCUtils.parseBOC(byteBuf.readByte());
        startSecond = BOCUtils.parseBOC(byteBuf.readByte());
        immediateAmount = ((float) byteBuf.readShortLE()) / 100F;
        extendedAmount = ((float) byteBuf.readShortLE()) / 100F;
        duration = byteBuf.readShortLE();
        byteBuf.shift(2);
        bolusId = byteBuf.readShortLE();
    }
}
