package sugar.free.sightparser.applayer.descriptors.alerts;

import lombok.Getter;
import sugar.free.sightparser.Helpers;
import sugar.free.sightparser.pipeline.ByteBuf;

public class Warning31CartridgeLow extends Alert {

    private static final long serialVersionUID = 1L;

    @Getter
    private double cartridgeAmount;

    @Override
    public void parse(ByteBuf byteBuf) {
        cartridgeAmount = Helpers.roundDouble(((double) byteBuf.readUInt16LE()) / 100D);
    }
}
