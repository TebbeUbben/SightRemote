package sugar.free.sightparser.applayer.messages.connection;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.descriptors.Service;
import sugar.free.sightparser.pipeline.ByteBuf;

public class ServiceChallengeMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

    @Setter
    private byte serviceID;
    @Getter
    private byte[] randomData;
    @Setter
    private short version;

    @Override
    public Service getService() {
        return Service.CONNECTION;
    }

    @Override
    public short getCommand() {
        return (short) 0xD2F3;
    }

    @Override
    protected void parse(ByteBuf byteBuf) throws Exception {
        randomData = byteBuf.getBytes(16);
    }

    @Override
    protected byte[] getData() throws Exception {
        ByteBuf byteBuf = new ByteBuf(3);
        byteBuf.putByte(serviceID);
        byteBuf.putShort(version);
        return byteBuf.getBytes();
    }
}
