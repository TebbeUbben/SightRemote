package sugar.free.sightparser.applayer.descriptors.alerts;

import lombok.Getter;
import sugar.free.sightparser.RoundingUtil;
import sugar.free.sightparser.pipeline.ByteBuf;

public class Warning36TBRCancelled extends Alert {

    @Getter
    private short amount;
    @Getter
    private short duration;

    private static final long serialVersionUID = 1L;

    @Override
    public void parse(ByteBuf byteBuf) {
        byteBuf.shift(2);
        amount = byteBuf.readShortLE();
        duration = byteBuf.readShortLE();
    }
}
