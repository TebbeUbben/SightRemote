package sugar.free.sightparser.applayer.history;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import sugar.free.sightparser.BOCUtils;
import sugar.free.sightparser.applayer.history.history_frames.BolusDeliveredFrame;
import sugar.free.sightparser.applayer.history.history_frames.BolusProgrammedFrame;
import sugar.free.sightparser.applayer.history.history_frames.EndOfTBRFrame;
import sugar.free.sightparser.pipeline.ByteBuf;

@Getter
public abstract class HistoryFrame implements Serializable {

    public static final Map<Short, Class<? extends HistoryFrame>> HISTORY_FRAMES = new HashMap<>();

    static {
        HISTORY_FRAMES.put((short) 0x0303, EndOfTBRFrame.class);
        HISTORY_FRAMES.put((short) 0x6A03, BolusProgrammedFrame.class);
        HISTORY_FRAMES.put((short) 0x9503, BolusDeliveredFrame.class);
    }

    private int eventYear;
    private int eventMonth;
    private int eventDay;
    private int eventHour;
    private int eventMinute;
    private int eventSecond;
    private int eventId;

    public final void parseHeader(ByteBuf byteBuf) {
        eventYear = BOCUtils.parseBOC(byteBuf.readBytes(2));
        eventMonth = BOCUtils.parseBOC(byteBuf.readByte());
        eventDay = BOCUtils.parseBOC(byteBuf.readByte());
        eventHour = BOCUtils.parseBOC(byteBuf.readByte());
        eventMinute = BOCUtils.parseBOC(byteBuf.readByte());
        eventSecond = BOCUtils.parseBOC(byteBuf.readByte());
        eventId = byteBuf.readIntLE();
    }

    public abstract void parse(ByteBuf byteBuf);

}
