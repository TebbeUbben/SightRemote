package sugar.free.sightparser.authlayer;

import java.util.Calendar;

import lombok.Setter;
import sugar.free.sightparser.pipeline.ByteBuf;

public final class KeyRequest extends CRCAuthLayerMessage {

    @Setter
    private byte[] randomBytes;
    @Setter
    private byte[] preMasterKey;

    @Override
    protected byte getCommand() {
        return 0x0C;
    }

    @Override
    protected void parse(ByteBuf byteBuf) {
        super.parse(byteBuf);
    }

    @Override
    protected byte[] getData() {
        ByteBuf byteBuf = new ByteBuf(288);
        byteBuf.putBytes(randomBytes);
        byteBuf.putIntLE(translateDate());
        byteBuf.putBytes(preMasterKey);
        return byteBuf.getBytes();
    }

    private static int translateDate() {
        Calendar calendar = Calendar.getInstance();
        int second = calendar.get(Calendar.SECOND);
        int minute = calendar.get(Calendar.MINUTE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        return (year % 100 & 0x3f) << 26 | (month & 0x0f) << 22 | (day & 0x1f) << 17 | (hour & 0x1f) << 12 | (minute & 0x3f) << 6 | (second & 0x3f);
    }
}
