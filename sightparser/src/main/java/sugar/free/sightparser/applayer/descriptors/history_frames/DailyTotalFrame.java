package sugar.free.sightparser.applayer.descriptors.history_frames;

import lombok.Getter;
import sugar.free.sightparser.BOCUtil;
import sugar.free.sightparser.Helpers;
import sugar.free.sightparser.pipeline.ByteBuf;

@Getter
public class DailyTotalFrame extends HistoryFrame {

    private static final long serialVersionUID = 1L;

    private double basalTotal;
    private double bolusTotal;
    private int totalYear;
    private int totalMonth;
    private int totalDay;

    @Override
    public void parse(ByteBuf byteBuf) {
        basalTotal = Helpers.roundDouble(((double) byteBuf.readUInt32LE()) / 100D);
        bolusTotal = Helpers.roundDouble(((double) byteBuf.readUInt32LE()) / 100D);
        totalYear = BOCUtil.parseBOC(byteBuf.readByte()) * 100 + BOCUtil.parseBOC(byteBuf.readByte());
        totalMonth = BOCUtil.parseBOC(byteBuf.readByte());
        totalDay = BOCUtil.parseBOC(byteBuf.readByte());
    }
}
