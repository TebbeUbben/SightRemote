package sugar.free.sightparser.applayer.history;

import android.util.Log;

import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import sugar.free.sightparser.applayer.AppLayerMessage;
import sugar.free.sightparser.applayer.Service;
import sugar.free.sightparser.pipeline.ByteBuf;

public class ReadHistoryFramesMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

    @Getter
    private List<HistoryFrame> historyFrames;

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
        int frameCount = byteBuf.readShortLE();
        for (int i = 0; i < frameCount; i++) {
            short length = byteBuf.readShortLE();
            short eventType = byteBuf.readShort();
            ByteBuf eventBuf = new ByteBuf(length - 2);
            eventBuf.putBytes(byteBuf.readBytes(length - 2));
            Class<? extends HistoryFrame> clazz = HistoryFrame.HISTORY_FRAMES.get(eventType);
            if (clazz != null) {
                HistoryFrame historyFrame = clazz.newInstance();
                historyFrame.parseHeader(eventBuf);
                historyFrame.parse(eventBuf);
                historyFrames.add(historyFrame);
            }
        }
    }
}
