package sugar.free.sightparser.applayer.messages.remote_control;

import lombok.Getter;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.descriptors.Service;
import sugar.free.sightparser.pipeline.ByteBuf;

@Getter
public class AvailableBolusesMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

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
        standardAvailable = byteBuf.readBoolean();
        extendedAvailable = byteBuf.readBoolean();
        multiwaveAvailable = byteBuf.readBoolean();
    }
}
