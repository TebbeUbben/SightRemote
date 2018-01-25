package sugar.free.sightparser.applayer.status;

import lombok.Getter;
import sugar.free.sightparser.applayer.AppLayerMessage;
import sugar.free.sightparser.applayer.Service;
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
    private short configIndex;
    @Getter
    private short historyIndex;
    @Getter
    private short stateIndex;
    @Getter
    private short vocabularyIndex;

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
        releaseSwVersion = byteBuf.readUTF16LE(14);
        uiProcSwVersion = byteBuf.readUTF16LE(12);
        pcProcSwVersion = byteBuf.readUTF16LE(12);
        mdTelProcSwVersion = byteBuf.readUTF16LE(12);
        btInfoPageVersion = byteBuf.readUTF16LE(12);
        safetyProcSwVersion = byteBuf.readUTF16LE(12);
        configIndex = byteBuf.readShortLE();
        historyIndex = byteBuf.readShortLE();
        stateIndex = byteBuf.readShortLE();
        vocabularyIndex = byteBuf.readShortLE();
    }
}
