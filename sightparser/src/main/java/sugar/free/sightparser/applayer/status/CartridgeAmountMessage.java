package sugar.free.sightparser.applayer.status;

import lombok.Getter;
import sugar.free.sightparser.applayer.AppLayerMessage;
import sugar.free.sightparser.applayer.Service;
import sugar.free.sightparser.error.NotAvailableError;
import sugar.free.sightparser.error.SightError;
import sugar.free.sightparser.error.UnknownAppErrorCodeError;
import sugar.free.sightparser.pipeline.ByteBuf;

public class CartridgeAmountMessage extends AppLayerMessage {

    @Getter
    private float cartridgeAmount = 0;

    @Override
    public Service getService() {
        return Service.STATUS;
    }

    @Override
    public short getCommand() {
        return 0x3A03;
    }

    @Override
    protected void parse(ByteBuf byteBuf) throws Exception {
        cartridgeAmount = ((float) byteBuf.getShortLE(6)) / 100F;
    }
}
