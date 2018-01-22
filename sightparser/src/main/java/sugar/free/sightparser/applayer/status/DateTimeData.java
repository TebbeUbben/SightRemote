package sugar.free.sightparser.applayer.status;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import sugar.free.sightparser.pipeline.ByteBuf;

public final class DateTimeData {

    private DateTimeData() {
    }

    public static Date parseDateTime(ByteBuf byteBuf) {
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, byteBuf.readShort());
        calendar.set(Calendar.MONTH, byteBuf.readByte());
        calendar.set(Calendar.DAY_OF_MONTH, byteBuf.readByte());
        calendar.set(Calendar.HOUR_OF_DAY, byteBuf.readByte());
        calendar.set(Calendar.MINUTE, byteBuf.readByte());
        calendar.set(Calendar.SECOND, byteBuf.readByte());
        return calendar.getTime();
    }

}
