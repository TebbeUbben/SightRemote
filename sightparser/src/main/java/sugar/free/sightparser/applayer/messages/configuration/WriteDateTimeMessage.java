package sugar.free.sightparser.applayer.messages.configuration;

import lombok.Setter;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.descriptors.Service;
import sugar.free.sightparser.pipeline.ByteBuf;

@Setter
public class WriteDateTimeMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;

    @Override
    public Service getService() {
        return Service.CONFIGURATION;
    }

    @Override
    public short getCommand() {
        return (short) 0xFF1B;
    }

    @Override
    protected boolean outCRC() {
        return true;
    }

    @Override
    protected byte[] getData() throws Exception {
        ByteBuf byteBuf = new ByteBuf(7);
        byteBuf.putUInt16LE(year);
        byteBuf.putByte((byte) month);
        byteBuf.putByte((byte) day);
        byteBuf.putByte((byte) hour);
        byteBuf.putByte((byte) minute);
        byteBuf.putByte((byte) second);
        return byteBuf.getBytes();
    }
}
