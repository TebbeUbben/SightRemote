package sugar.free.sightparser.applayer.configuration;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.applayer.AppLayerMessage;
import sugar.free.sightparser.applayer.Service;
import sugar.free.sightparser.error.SightError;
import sugar.free.sightparser.error.UnknownAppErrorCodeError;
import sugar.free.sightparser.pipeline.ByteBuf;

public class ReadConfigurationBlockMessage extends AppLayerMessage {

    @Setter
    @Getter
    private short configurationBlockID;
    @Getter
    private RestrictionLevel restriction;
    @Getter
    private ConfigurationBlock configurationBlock;

    @Override
    public Service getService() {
        return Service.CONFIGURATION;
    }

    @Override
    public short getCommand() {
        return 0x561E;
    }

    @Override
    protected byte[] getData() throws Exception {
        ByteBuf byteBuf = new ByteBuf(2);
        byteBuf.putShort(configurationBlockID);
        return byteBuf.getBytes();
    }

    @Override
    protected void parse(ByteBuf byteBuf) throws Exception {
        configurationBlockID = byteBuf.readShort();
        restriction = RestrictionLevel.getByValue(byteBuf.readShort());
        try {
            configurationBlock = ConfigurationBlock.CONFIGURATIONBLOCKS.get(configurationBlockID).newInstance();
            configurationBlock.parse(byteBuf);
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
