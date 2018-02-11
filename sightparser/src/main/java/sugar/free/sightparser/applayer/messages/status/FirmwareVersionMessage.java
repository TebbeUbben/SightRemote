package sugar.free.sightparser.applayer.messages.status;

import lombok.Getter;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.descriptors.Service;
import sugar.free.sightparser.pipeline.ByteBuf;

public class FirmwareVersionMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

    @Getter
    private String releaseSwVersion;
    @Getter
    private String uiProcSwVersion;
    @Getter
    private String pcProcSwVersion;
    @Getter
    private String mdTelProcSwVersion;
    @Getter
    private String btInfoPageVersion;
    @Getter
    private String safetyProcSwVersion;
    @Getter
    private int configIndex;
    @Getter
    private int historyIndex;
    @Getter
    private int stateIndex;
    @Getter
    private int vocabularyIndex;

    @Override
    public Service getService() {
        return Service.STATUS;
    }

    @Override
    public short getCommand() {
        return (short) 0xD82E;
    }

    @Override
    protected void parse(ByteBuf byteBuf) throws Exception {
        releaseSwVersion = byteBuf.readASCII(14);
        uiProcSwVersion = byteBuf.readASCII(12);
        pcProcSwVersion = byteBuf.readASCII(12);
        mdTelProcSwVersion = byteBuf.readASCII(12);
        btInfoPageVersion = byteBuf.readASCII(12);
        safetyProcSwVersion = byteBuf.readASCII(12);
        configIndex = byteBuf.readUInt16LE();
        historyIndex = byteBuf.readUInt16LE();
        stateIndex = byteBuf.readUInt16LE();
        vocabularyIndex = byteBuf.readUInt16LE();
    }
}
