package sugar.free.sightparser.applayer.history.history_frames;

import lombok.Getter;
import sugar.free.sightparser.applayer.history.HistoryFrame;
import sugar.free.sightparser.pipeline.ByteBuf;

public class CannulaFilledFrame extends HistoryFrame {

    @Getter
    private float amount;

    @Override
    public void parse(ByteBuf byteBuf) {
        amount = byteBuf.readShortLE() / 100F;
    }
}
