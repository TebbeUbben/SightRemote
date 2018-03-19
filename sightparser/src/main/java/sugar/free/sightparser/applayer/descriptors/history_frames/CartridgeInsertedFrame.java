package sugar.free.sightparser.applayer.descriptors.history_frames;

import lombok.Getter;
import sugar.free.sightparser.Helpers;
import sugar.free.sightparser.pipeline.ByteBuf;

public class CartridgeInsertedFrame extends HistoryFrame  {

    private static final long serialVersionUID = 1L;

    @Getter
    private double cartridgeAmount;

    @Override
    public void parse(ByteBuf byteBuf) {
        cartridgeAmount = Helpers.roundDouble(((double) byteBuf.readUInt16LE()) / 100D);
    }
}
