package sugar.free.sightparser.applayer.descriptors.configuration_blocks;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.pipeline.ByteBuf;

public abstract class NameBlock extends ConfigurationBlock {

    private static final long serialVersionUID = 1L;

    @Setter
    @Getter
    private String name;

    @Override
    public void parse(ByteBuf byteBuf) {
        name = byteBuf.readUTF16LE(42);
    }

    @Override
    public byte[] getData() {
        ByteBuf byteBuf = new ByteBuf(42);
        byteBuf.putUTF16LE(name, 42);
        return byteBuf.getBytes();
    }
}
