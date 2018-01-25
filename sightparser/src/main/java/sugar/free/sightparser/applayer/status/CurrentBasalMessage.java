package sugar.free.sightparser.applayer.status;

import lombok.Getter;
import sugar.free.sightparser.applayer.AppLayerMessage;
import sugar.free.sightparser.applayer.Service;
import sugar.free.sightparser.pipeline.ByteBuf;

public class CurrentBasalMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

    @Getter
    private String currentBasalName;
    @Getter
    private float currentBasalAmount = 0;

    @Override
    public Service getService() {
        return Service.STATUS;
    }

    @Override
    public short getCommand() {
        return (short) 0xA905;
    }

    @Override
    protected boolean inCRC() {
        return true;
    }

    @Override
    protected void parse(ByteBuf byteBuf) throws Exception {
        byteBuf.shift(2);
        currentBasalName = byteBuf.readUTF16LE(62);
        currentBasalAmount = ((float) byteBuf.readShortLE()) /  100F;
    }
}
