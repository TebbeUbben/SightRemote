package sugar.free.sightparser.applayer.messages.history;

import android.util.Log;

import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.descriptors.Service;
import sugar.free.sightparser.applayer.descriptors.history_frames.HistoryFrame;
import sugar.free.sightparser.pipeline.ByteBuf;

public class ReadHistoryFramesMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

    @Getter
    private List<HistoryFrame> historyFrames;
    @Getter
    private long latestEventNumber = -1;

    @Override
    public Service getService() {
        return Service.HISTORY;
    }

    @Override
    public short getCommand() {
        return (short) 0xA828;
    }

    @Override
    protected boolean inCRC() {
        return true;
    }

    @Override
    protected void parse(ByteBuf byteBuf) throws Exception {
        historyFrames = new ArrayList<>();
        byteBuf.shift(2);
        int frameCount = byteBuf.readUInt16LE();
        for (int i = 0; i < frameCount; i++) {
            int length = byteBuf.readUInt16LE();
            short eventType = byteBuf.readShort();
            ByteBuf eventBuf = new ByteBuf(length - 2);
            eventBuf.putBytes(byteBuf.readBytes(length - 2));

            long eventNumber = eventBuf.getUInt32LE(8);
            if (eventNumber > latestEventNumber) latestEventNumber = eventNumber;

            Class<? extends HistoryFrame> clazz = HistoryFrame.HISTORY_FRAMES.get(eventType);
            if (clazz != null) {
                HistoryFrame historyFrame = clazz.newInstance();
                historyFrame.parseHeader(eventBuf);
                historyFrame.parse(eventBuf);
                historyFrames.add(historyFrame);
            } else Log.d("SightServiceHistory", "UNKNOWN HISTORY FRAME: EVENT TYPE: " + eventType + " CONTENT: " + Hex.toHexString(eventBuf.getBytes()));
        }
    }
}
