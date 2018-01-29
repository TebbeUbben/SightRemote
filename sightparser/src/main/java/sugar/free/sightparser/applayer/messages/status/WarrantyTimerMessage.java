package sugar.free.sightparser.applayer.messages.status;

import lombok.Getter;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.descriptors.Service;
import sugar.free.sightparser.pipeline.ByteBuf;

public class WarrantyTimerMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

    @Getter
    private int warrantyTimer;

    @Override
    public Service getService() {
        return Service.STATUS;
    }

    @Override
    public short getCommand() {
        return 0x4A05;
    }

    @Override
    protected void parse(ByteBuf byteBuf) throws Exception {
        warrantyTimer = byteBuf.readIntLE();
    }
}
