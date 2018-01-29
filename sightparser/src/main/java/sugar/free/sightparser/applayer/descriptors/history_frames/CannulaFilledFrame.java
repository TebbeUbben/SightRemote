package sugar.free.sightparser.applayer.descriptors.history_frames;

import lombok.Getter;
import sugar.free.sightparser.RoundingUtil;
import sugar.free.sightparser.pipeline.ByteBuf;

public class CannulaFilledFrame extends HistoryFrame {

    @Getter
    private float amount;

    @Override
    public void parse(ByteBuf byteBuf) {
        amount = RoundingUtil.roundFloat(byteBuf.readShortLE() / 100F, 2);
    }
}
