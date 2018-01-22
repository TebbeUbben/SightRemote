package sugar.free.sightparser.applayer.status;

import lombok.Getter;
import sugar.free.sightparser.applayer.AppLayerMessage;
import sugar.free.sightparser.applayer.Service;
import sugar.free.sightparser.error.NotAvailableError;
import sugar.free.sightparser.error.SightError;
import sugar.free.sightparser.error.UnknownAppErrorCodeError;
import sugar.free.sightparser.pipeline.ByteBuf;

public class WarrantyTimerMessage extends AppLayerMessage {

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
