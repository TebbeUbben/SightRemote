package sugar.free.sightparser.applayer.status;

import java.util.Date;

import lombok.Getter;
import sugar.free.sightparser.applayer.AppLayerMessage;
import sugar.free.sightparser.applayer.Service;
import sugar.free.sightparser.error.SightError;
import sugar.free.sightparser.error.UnknownAppErrorCodeError;
import sugar.free.sightparser.pipeline.ByteBuf;

public class DateTimeMesssage extends AppLayerMessage {

    @Getter
    private Date dateTime;

    @Override
    public Service getService() {
        return Service.STATUS;
    }

    @Override
    public short getCommand() {
        return (short) 0xE300;
    }

    @Override
    protected void parse(ByteBuf byteBuf) throws Exception {
        dateTime = DateTimeData.parseDateTime(byteBuf);
    }
}
