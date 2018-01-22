package sugar.free.sightparser.applayer.remote_control;

import lombok.Getter;
import sugar.free.sightparser.applayer.AppLayerMessage;
import sugar.free.sightparser.applayer.Service;
import sugar.free.sightparser.pipeline.ByteBuf;

public class BolusMessage extends AppLayerMessage {

    @Getter
    private short bolusId;

    @Override
    public Service getService() {
        return Service.REMOTE_CONTROL;
    }

    @Override
    public short getCommand() {
        return 0x031B;
    }

    @Override
    protected void parse(ByteBuf byteBuf) throws Exception {
        bolusId = byteBuf.readShortLE();
    }

    @Override
    protected boolean inCRC() {
        return true;
    }

    @Override
    protected boolean outCRC() {
        return true;
    }
}
