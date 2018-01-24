package sugar.free.sightparser.applayer.status;

import lombok.Getter;
import sugar.free.sightparser.applayer.AppLayerMessage;
import sugar.free.sightparser.applayer.Service;
import sugar.free.sightparser.error.SightError;
import sugar.free.sightparser.error.UnknownAppErrorCodeError;
import sugar.free.sightparser.pipeline.ByteBuf;

public class BatteryAmountMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

    @Getter
    private int batteryAmount;

    @Override
    public Service getService() {
        return Service.STATUS;
    }

    @Override
    public short getCommand() {
        return 0x2503;
    }

    @Override
    protected void parse(ByteBuf byteBuf) throws Exception {
        batteryAmount = byteBuf.getShortLE(2);
    }
}
