package sugar.free.sightparser.applayer.messages.status;

import lombok.Getter;
import sugar.free.sightparser.RoundingUtil;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.descriptors.Service;
import sugar.free.sightparser.pipeline.ByteBuf;

public class CartridgeAmountMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

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
        cartridgeAmount = RoundingUtil.roundFloat(((float) byteBuf.getShortLE(6)) / 100F, 2);
    }
}
