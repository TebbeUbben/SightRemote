package sugar.free.sightparser.applayer.messages.status;

import lombok.Getter;
import sugar.free.sightparser.RoundingUtil;
import sugar.free.sightparser.applayer.descriptors.Service;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.pipeline.ByteBuf;

@Getter
public class DailyTotalMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

    private float bolusTotal;
    private float basalTotal;
    private float total;

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
        bolusTotal = RoundingUtil.roundFloat(((double) byteBuf.readUInt16LE()) / 100D, 3);
        basalTotal = RoundingUtil.roundFloat(((double) byteBuf.readUInt16LE()) / 100D, 3);
        total = RoundingUtil.roundFloat(((double) byteBuf.readUInt16LE()) / 100D, 3);
    }

    @Override
    protected boolean inCRC() {
        return true;
    }
}
