package sugar.free.sightparser.applayer.messages.configuration;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.ConfigurationBlock;
import sugar.free.sightparser.applayer.descriptors.Service;
import sugar.free.sightparser.applayer.descriptors.RestrictionLevel;
import sugar.free.sightparser.pipeline.ByteBuf;

public class ReadConfigurationBlockMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

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
