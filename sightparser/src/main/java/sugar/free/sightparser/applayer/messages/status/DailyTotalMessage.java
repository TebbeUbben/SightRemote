package sugar.free.sightparser.applayer.messages.status;

import android.util.Log;

import lombok.Getter;
import sugar.free.sightparser.RoundingUtil;
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
        bolusTotal = RoundingUtil.roundDouble(((double) byteBuf.readUInt32LE()) / 100D, 2);
        basalTotal = RoundingUtil.roundDouble(((double) byteBuf.readUInt32LE()) / 100D, 2);
        total = RoundingUtil.roundDouble(((double) byteBuf.readUInt32LE()) / 100D, 2);
    }

    @Override
    protected boolean inCRC() {
        return true;
    }
}
