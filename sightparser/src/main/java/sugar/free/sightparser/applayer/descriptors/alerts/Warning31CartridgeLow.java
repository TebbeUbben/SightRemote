package sugar.free.sightparser.applayer.descriptors.alerts;

import lombok.Getter;
import sugar.free.sightparser.RoundingUtil;
import sugar.free.sightparser.pipeline.ByteBuf;

public class Warning31CartridgeLow extends Alert {

    private static final long serialVersionUID = 1L;

    @Getter
    private float cartridgeAmount;

    @Override
    public void parse(ByteBuf byteBuf) {
        cartridgeAmount = RoundingUtil.roundFloat(((float) byteBuf.readUInt16LE()) / 100F, 2);
    }
}
