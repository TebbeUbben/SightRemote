package sugar.free.sightparser.applayer.descriptors.status_param_blocks;

import lombok.Getter;
import sugar.free.sightparser.pipeline.ByteBuf;

public class SystemIdentificationBlock extends StatusBlock {

    public static final short ID = (short) 0x948A;

    private static final long serialVersionUID = 1L;

    @Getter
    private String serialNumber;
    @Getter
    private int systemIdAppendix;
    @Getter
    private String manufacturingDate;

    @Override
    public short getID() {
        return ID;
    }

    @Override
    public void parse(ByteBuf byteBuf) {
        serialNumber = byteBuf.readUTF16LE(20);
        systemIdAppendix = (int) byteBuf.readUInt32LE();
        manufacturingDate = byteBuf.readUTF16LE(24);
    }

    @Override
    public byte[] getData() {
        return new byte[0];
    }
}
