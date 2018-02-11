package sugar.free.sightparser.applayer.messages.status;

import lombok.Getter;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.descriptors.Service;
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
        batteryAmount = byteBuf.getUInt16LE(2);
    }
}
