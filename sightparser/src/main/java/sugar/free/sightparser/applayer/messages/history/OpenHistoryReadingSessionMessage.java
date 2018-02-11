package sugar.free.sightparser.applayer.messages.history;

import lombok.Setter;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.descriptors.Service;
import sugar.free.sightparser.applayer.descriptors.HistoryReadingDirection;
import sugar.free.sightparser.applayer.descriptors.HistoryType;
import sugar.free.sightparser.pipeline.ByteBuf;

public class OpenHistoryReadingSessionMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

    @Setter
    private HistoryType historyType;
    @Setter
    private HistoryReadingDirection readingDirection;
    @Setter
    private long offset;

    @Override
    public Service getService() {
        return Service.HISTORY;
    }

    @Override
    public short getCommand() {
        return 0x5428;
    }

    @Override
    protected boolean outCRC() {
        return true;
    }

    @Override
    protected byte[] getData() throws Exception {
        ByteBuf byteBuf = new ByteBuf(8);
        byteBuf.putShort(historyType.getValue());
        byteBuf.putShort(readingDirection.getValue());
        byteBuf.putUInt32LE(offset);
        return byteBuf.getBytes();
    }
}
