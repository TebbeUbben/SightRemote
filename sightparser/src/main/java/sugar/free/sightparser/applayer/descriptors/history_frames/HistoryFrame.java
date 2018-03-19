package sugar.free.sightparser.applayer.descriptors.history_frames;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import sugar.free.sightparser.BOCUtil;
import sugar.free.sightparser.pipeline.ByteBuf;

@Getter
public abstract class HistoryFrame implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final Map<Short, Class<? extends HistoryFrame>> HISTORY_FRAMES = new HashMap<>();

    static {
        HISTORY_FRAMES.put((short) 0x0303, EndOfTBRFrame.class);
        HISTORY_FRAMES.put((short) 0x6A03, BolusProgrammedFrame.class);
        HISTORY_FRAMES.put((short) 0x9503, BolusDeliveredFrame.class);
        HISTORY_FRAMES.put((short) 0xC300, PumpStatusChangedFrame.class);
        HISTORY_FRAMES.put((short) 0xA500, TimeChangedFrame.class);
        HISTORY_FRAMES.put((short) 0xCF0C, CannulaFilledFrame.class);
        HISTORY_FRAMES.put((short) 0xC003, DailyTotalFrame.class);
        HISTORY_FRAMES.put((short) 0x6900, TubeFilledFrame.class);
        HISTORY_FRAMES.put((short) 0x6600, CartridgeInsertedFrame.class);
        HISTORY_FRAMES.put((short) 0x0F00, BatteryInsertedFrame.class);
    }

    private int eventYear;
    private int eventMonth;
    private int eventDay;
    private int eventHour;
    private int eventMinute;
    private int eventSecond;
    private long eventNumber;

    public final void parseHeader(ByteBuf byteBuf) {
        eventYear = BOCUtil.parseBOC(byteBuf.readByte()) * 100 + BOCUtil.parseBOC(byteBuf.readByte());
        eventMonth = BOCUtil.parseBOC(byteBuf.readByte());
        eventDay = BOCUtil.parseBOC(byteBuf.readByte());
        byteBuf.shift(1);
        eventHour = BOCUtil.parseBOC(byteBuf.readByte());
        eventMinute = BOCUtil.parseBOC(byteBuf.readByte());
        eventSecond = BOCUtil.parseBOC(byteBuf.readByte());
        eventNumber = byteBuf.readUInt32LE();
    }

    public abstract void parse(ByteBuf byteBuf);

}
