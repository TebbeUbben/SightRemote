package sugar.free.sightparser.applayer.remote_control;

import lombok.Getter;
import sugar.free.sightparser.applayer.AppLayerMessage;
import sugar.free.sightparser.applayer.Service;
import sugar.free.sightparser.pipeline.ByteBuf;

@Getter
public class AvailableBolusesMessage extends AppLayerMessage {

    private boolean standardAvailable;
    private boolean extendedAvailable;
    private boolean multiwaveAvailable;

    @Override
    public Service getService() {
        return Service.REMOTE_CONTROL;
    }

    @Override
    public short getCommand() {
        return (short) 0xDA18;
    }

    @Override
    protected void parse(ByteBuf byteBuf) throws Exception {
        standardAvailable = byteBuf.readShort() == 0x4B00;
        extendedAvailable = byteBuf.readShort() == 0x4B00;
        multiwaveAvailable = byteBuf.readShort() == 0x4B00;
    }
}
