package sugar.free.sightparser.applayer.status_param;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.applayer.AppLayerMessage;
import sugar.free.sightparser.applayer.Service;
import sugar.free.sightparser.applayer.configuration.RestrictionLevel;
import sugar.free.sightparser.pipeline.ByteBuf;

public class ReadStatusParamBlockMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

    @Setter
    @Getter
    private short statusBlockId;
    @Getter
    private RestrictionLevel restriction;
    @Getter
    private StatusBlock statusBlock;

    @Override
    public Service getService() {
        return Service.STATUS_PARAM;
    }

    @Override
    public short getCommand() {
        return 0x561E;
    }

    @Override
    protected byte[] getData() throws Exception {
        ByteBuf byteBuf = new ByteBuf(2);
        byteBuf.putShort(statusBlockId);
        return byteBuf.getBytes();
    }

    @Override
    protected void parse(ByteBuf byteBuf) throws Exception {
        statusBlockId = byteBuf.readShort();
        restriction = RestrictionLevel.getByValue(byteBuf.readShort());
        try {
            statusBlock = StatusBlock.STATUSBLOCKS.get(statusBlockId).newInstance();
            statusBlock.parse(byteBuf);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean inCRC() {
        return true;
    }
}
