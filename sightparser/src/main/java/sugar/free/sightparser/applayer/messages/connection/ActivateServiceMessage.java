package sugar.free.sightparser.applayer.messages.connection;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.descriptors.Service;
import sugar.free.sightparser.pipeline.ByteBuf;

public class ActivateServiceMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private byte serviceID;
    @Getter
    @Setter
    private short version;
    @Setter
    private byte[] servicePassword;

    @Override
    public Service getService() {
        return Service.CONNECTION;
    }

    @Override
    public short getCommand() {
        return (short) 0xF7F0;
    }

    @Override
    protected void parse(ByteBuf byteBuf) throws Exception {
        serviceID = byteBuf.readByte();
        version = byteBuf.getShort();
    }

    @Override
    protected byte[] getData() {
        ByteBuf byteBuf = new ByteBuf(19);
        byteBuf.putByte(serviceID);
        byteBuf.putShort(version);
        byteBuf.putBytes(servicePassword);
        return byteBuf.getBytes();
    }
}
