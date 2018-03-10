package sugar.free.sightparser.applayer.descriptors.history_frames;

import lombok.Getter;
import sugar.free.sightparser.RoundingUtil;
import sugar.free.sightparser.pipeline.ByteBuf;

public class CannulaFilledFrame extends HistoryFrame {

    private static final long serialVersionUID = 1L;

    @Getter
    private double amount;

    @Override
    public void parse(ByteBuf byteBuf) {
        amount = RoundingUtil.roundDouble(((double) byteBuf.readUInt16LE()) / 100D, 2);
    }
}
