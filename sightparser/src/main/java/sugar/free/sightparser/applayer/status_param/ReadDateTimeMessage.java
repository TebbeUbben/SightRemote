package sugar.free.sightparser.applayer.status_param;

import lombok.Getter;
import sugar.free.sightparser.applayer.AppLayerMessage;
import sugar.free.sightparser.applayer.Service;
import sugar.free.sightparser.pipeline.ByteBuf;

@Getter
public class ReadDateTimeMessage extends AppLayerMessage {

    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;

    @Override
    public Service getService() {
        return Service.STATUS_PARAM;
    }

    @Override
    public short getCommand() {
        return (short) 0xE300;
    }

    @Override
    protected boolean inCRC() {
        return true;
    }

    @Override
    protected void parse(ByteBuf byteBuf) throws Exception {
        year = byteBuf.readShortLE();
        month = byteBuf.readByte();
        day = byteBuf.readByte();
        hour = byteBuf.readByte();
        minute = byteBuf.readByte();
        second = byteBuf.readByte();
    }
}
