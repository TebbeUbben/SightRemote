package sugar.free.sightparser.applayer.history;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import sugar.free.sightparser.BOCUtil;
import sugar.free.sightparser.applayer.history.history_frames.BolusDeliveredFrame;
import sugar.free.sightparser.applayer.history.history_frames.BolusProgrammedFrame;
import sugar.free.sightparser.applayer.history.history_frames.CannulaFilledFrame;
import sugar.free.sightparser.applayer.history.history_frames.EndOfTBRFrame;
import sugar.free.sightparser.applayer.history.history_frames.PumpStatusChangedFrame;
import sugar.free.sightparser.applayer.history.history_frames.TimeChangedFrame;
import sugar.free.sightparser.pipeline.ByteBuf;

@Getter
public abstract class HistoryFrame implements Serializable {

    public static final Map<Short, Class<? extends HistoryFrame>> HISTORY_FRAMES = new HashMap<>();

    static {
        HISTORY_FRAMES.put((short) 0x0303, EndOfTBRFrame.class);
        HISTORY_FRAMES.put((short) 0x6A03, BolusProgrammedFrame.class);
        HISTORY_FRAMES.put((short) 0x9503, BolusDeliveredFrame.class);
        HISTORY_FRAMES.put((short) 0xC300, PumpStatusChangedFrame.class);
        HISTORY_FRAMES.put((short) 0xA500, TimeChangedFrame.class);
        HISTORY_FRAMES.put((short) 0xCF0C, CannulaFilledFrame.class);
    }

    private int eventYear;
    private int eventMonth;
    private int eventDay;
    private int eventHour;
    private int eventMinute;
    private int eventSecond;
    private int eventNumber;

    public final void parseHeader(ByteBuf byteBuf) {
        eventYear = BOCUtil.parseBOC(byteBuf.readByte()) * 100 + BOCUtil.parseBOC(byteBuf.readByte());
        eventMonth = BOCUtil.parseBOC(byteBuf.readByte());
        eventDay = BOCUtil.parseBOC(byteBuf.readByte());
        byteBuf.shift(1);
        eventHour = BOCUtil.parseBOC(byteBuf.readByte());
        eventMinute = BOCUtil.parseBOC(byteBuf.readByte());
        eventSecond = BOCUtil.parseBOC(byteBuf.readByte());
        eventNumber = byteBuf.readIntLE();
    }

    public abstract void parse(ByteBuf byteBuf);

}
