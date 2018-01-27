package sugar.free.sightparser.applayer.history.history_frames;

import lombok.Getter;
import sugar.free.sightparser.BOCUtils;
import sugar.free.sightparser.applayer.history.HistoryFrame;
import sugar.free.sightparser.pipeline.ByteBuf;

@Getter
public class TimeChangedFrame extends HistoryFrame {

    private int beforeYear;
    private int beforeMonth;
    private int beforeDay;
    private int beforeHour;
    private int beforeMinute;
    private int beforeSecond;

    @Override
    public void parse(ByteBuf byteBuf) {
        beforeYear = BOCUtils.parseBOC(byteBuf.readByte()) * 100 + BOCUtils.parseBOC(byteBuf.readByte());
        beforeMonth = BOCUtils.parseBOC(byteBuf.readByte());
        beforeDay = BOCUtils.parseBOC(byteBuf.readByte());
        byteBuf.shift(1);
        beforeHour = BOCUtils.parseBOC(byteBuf.readByte());
        beforeMinute = BOCUtils.parseBOC(byteBuf.readByte());
        beforeSecond = BOCUtils.parseBOC(byteBuf.readByte());
    }
}
