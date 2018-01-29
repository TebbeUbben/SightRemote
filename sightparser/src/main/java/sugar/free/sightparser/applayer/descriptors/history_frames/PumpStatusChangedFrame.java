package sugar.free.sightparser.applayer.descriptors.history_frames;

import lombok.Getter;
import sugar.free.sightparser.applayer.descriptors.PumpStatus;
import sugar.free.sightparser.pipeline.ByteBuf;

@Getter
public class PumpStatusChangedFrame extends HistoryFrame {

    private static final long serialVersionUID = 1L;

    private PumpStatus oldValue;
    private PumpStatus newValue;

    @Override
    public void parse(ByteBuf byteBuf) {
        oldValue = PumpStatus.getPumpStatus(byteBuf.readShort());
        newValue = PumpStatus.getPumpStatus(byteBuf.readShort());
    }
}
