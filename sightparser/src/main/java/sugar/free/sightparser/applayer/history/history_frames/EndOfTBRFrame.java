package sugar.free.sightparser.applayer.history.history_frames;

import lombok.Getter;
import sugar.free.sightparser.BOCUtil;
import sugar.free.sightparser.applayer.history.HistoryFrame;
import sugar.free.sightparser.pipeline.ByteBuf;

@Getter
public class EndOfTBRFrame extends HistoryFrame {

    private static final long serialVersionUID = 1L;

    private int startHour;
    private int startMinute;
    private int startSecond;
    private short amount;
    private short duration;

    @Override
    public void parse(ByteBuf byteBuf) {
        byteBuf.shift(1);
        startHour = BOCUtil.parseBOC(byteBuf.readByte());
        startMinute = BOCUtil.parseBOC(byteBuf.readByte());
        startSecond = BOCUtil.parseBOC(byteBuf.readByte());
        amount = byteBuf.readShortLE();
        duration = byteBuf.readShortLE();
    }
}
