package sugar.free.sightparser.applayer.messages.remote_control;

import lombok.Setter;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.descriptors.Service;
import sugar.free.sightparser.applayer.descriptors.PumpStatus;
import sugar.free.sightparser.pipeline.ByteBuf;

public class SetPumpStatusMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

    @Setter
    private PumpStatus pumpStatus;

    @Override
    public Service getService() {
        return Service.REMOTE_CONTROL;
    }

    @Override
    public short getCommand() {
        return 0x2618;
    }

    @Override
    protected byte[] getData() throws Exception {
        ByteBuf byteBuf = new ByteBuf(2);
        byteBuf.putShort(pumpStatus.getValue());
        return byteBuf.getBytes();
    }

    @Override
    protected boolean outCRC() {
        return true;
    }
}
