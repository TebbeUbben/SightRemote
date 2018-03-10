package sugar.free.sightparser.applayer.descriptors.history_frames;

import lombok.Getter;
import sugar.free.sightparser.BOCUtil;
import sugar.free.sightparser.pipeline.ByteBuf;

@Getter
public class TimeChangedFrame extends HistoryFrame {

    private static final long serialVersionUID = 1L;

    private int beforeYear;
    private int beforeMonth;
    private int beforeDay;
    private int beforeHour;
    private int beforeMinute;
    private int beforeSecond;

    @Override
    public void parse(ByteBuf byteBuf) {
        beforeYear = BOCUtil.parseBOC(byteBuf.readByte()) * 100 + BOCUtil.parseBOC(byteBuf.readByte());
        beforeMonth = BOCUtil.parseBOC(byteBuf.readByte());
        beforeDay = BOCUtil.parseBOC(byteBuf.readByte());
        byteBuf.shift(1);
        beforeHour = BOCUtil.parseBOC(byteBuf.readByte());
        beforeMinute = BOCUtil.parseBOC(byteBuf.readByte());
        beforeSecond = BOCUtil.parseBOC(byteBuf.readByte());
    }
}
