package sugar.free.sightparser.applayer.descriptors.alerts;

import lombok.Getter;
import sugar.free.sightparser.Helpers;
import sugar.free.sightparser.applayer.descriptors.HistoryBolusType;
import sugar.free.sightparser.pipeline.ByteBuf;

public class Warning38BolusCancelled extends Alert {

    @Getter
    private HistoryBolusType bolusType;
    @Getter
    private double programmedAmount;
    @Getter
    private double deliveredAmount;

    private static final long serialVersionUID = 1L;

    @Override
    public void parse(ByteBuf byteBuf) {
        bolusType = HistoryBolusType.getBolusType(byteBuf.readShort());
        byteBuf.shift(2);
        programmedAmount = Helpers.roundDouble(((double) byteBuf.readUInt16LE()) / 100D);
        deliveredAmount = Helpers.roundDouble(((double) byteBuf.readUInt16LE()) / 100D);
    }
}
