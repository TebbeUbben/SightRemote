package sugar.free.sightparser.applayer.messages.status;

import lombok.Getter;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.descriptors.Service;
import sugar.free.sightparser.pipeline.ByteBuf;

public class CurrentTBRMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

    @Getter
    private int percentage = 100;
    @Getter
    private int leftoverTime = 0;
    @Getter
    private int initialTime = 0;

    @Override
    public Service getService() {
        return Service.STATUS;
    }

    @Override
    public short getCommand() {
        return (short) 0xB605;
    }

    @Override
    protected boolean inCRC() {
        return true;
    }

    @Override
    protected void parse(ByteBuf byteBuf) throws Exception {
        percentage = byteBuf.readShortLE();
        leftoverTime = byteBuf.readShortLE();
        initialTime = byteBuf.readShortLE();
    }
}
