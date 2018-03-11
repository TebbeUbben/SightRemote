package sugar.free.sightparser.applayer.messages.status;

import lombok.Getter;
import sugar.free.sightparser.Helpers;
import sugar.free.sightparser.applayer.descriptors.Service;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.pipeline.ByteBuf;

@Getter
public class DailyTotalMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

    private double bolusTotal;
    private double basalTotal;
    private double total;

    @Override
    public Service getService() {
        return Service.STATUS;
    }

    @Override
    public short getCommand() {
        return (short) 0xC603;
    }

    @Override
    protected void parse(ByteBuf byteBuf) throws Exception {
        bolusTotal = Helpers.roundDouble(((double) byteBuf.readUInt32LE()) / 100D);
        basalTotal = Helpers.roundDouble(((double) byteBuf.readUInt32LE()) / 100D);
        total = Helpers.roundDouble(((double) byteBuf.readUInt32LE()) / 100D);
    }

    @Override
    protected boolean inCRC() {
        return true;
    }
}
