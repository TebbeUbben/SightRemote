package sugar.free.sightparser.applayer.messages.status;

import lombok.Getter;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.descriptors.Service;
import sugar.free.sightparser.applayer.descriptors.PumpStatus;
import sugar.free.sightparser.pipeline.ByteBuf;

public class PumpStatusMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

    @Getter
    private PumpStatus pumpStatus;

    @Override
    public Service getService() {
        return Service.STATUS;
    }

    @Override
    public short getCommand() {
        return (short) 0xFC00;
    }

    @Override
    protected boolean inCRC() {
        return true;
    }

    @Override
    protected void parse(ByteBuf byteBuf) throws Exception {
        pumpStatus = PumpStatus.getPumpStatus(byteBuf.readShort());
    }
}
