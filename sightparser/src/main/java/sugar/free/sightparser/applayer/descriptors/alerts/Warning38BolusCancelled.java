package sugar.free.sightparser.applayer.descriptors.alerts;

import lombok.Getter;
import sugar.free.sightparser.RoundingUtil;
import sugar.free.sightparser.applayer.descriptors.HistoryBolusType;
import sugar.free.sightparser.pipeline.ByteBuf;

public class Warning38BolusCancelled extends Alert {

    @Getter
    private HistoryBolusType bolusType;
    @Getter
    private float programmedAmount;
    @Getter
    private float deliveredAmount;

    private static final long serialVersionUID = 1L;

    @Override
    public void parse(ByteBuf byteBuf) {
        bolusType = HistoryBolusType.getBolusType(byteBuf.readShort());
        byteBuf.shift(2);
        programmedAmount = RoundingUtil.roundFloat(((float) byteBuf.readUInt16LE()) / 100F, 2);
        deliveredAmount = RoundingUtil.roundFloat(((float) byteBuf.readUInt16LE()) / 100F, 2);
    }
}
