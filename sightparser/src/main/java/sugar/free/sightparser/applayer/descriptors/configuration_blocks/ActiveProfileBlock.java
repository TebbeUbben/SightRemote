package sugar.free.sightparser.applayer.descriptors.configuration_blocks;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.pipeline.ByteBuf;

public class ActiveProfileBlock extends ConfigurationBlock {

    private static final long serialVersionUID = 1L;

    public static final short ID = (short) 0x901D;

    @Getter
    @Setter
    private ActiveProfile activeProfile;

    @Override
    public short getID() {
        return ID;
    }

    @Override
    public void parse(ByteBuf byteBuf) {
        activeProfile = ActiveProfile.getByValue(byteBuf.readShort());
    }

    @Override
    public byte[] getData() {
        ByteBuf byteBuf = new ByteBuf(2);
        byteBuf.putShort(activeProfile.getValue());
        return byteBuf.getBytes();
    }

    public enum ActiveProfile {

        BR_PROFILE_1((short) 0x1F00),
        BR_PROFILE_2((short) 0xE300),
        BR_PROFILE_3((short) 0xFC00),
        BR_PROFILE_4((short) 0x2503),
        BR_PROFILE_5((short) 0x3A03);

        private static final long serialVersionUID = 1L;

        @Getter
        private short value;

        ActiveProfile(short value) {
            this.value = value;
        }

        public static ActiveProfile getByValue(short value) {
            for (ActiveProfile activeProfile : values())
                if (activeProfile.getValue() == value) return activeProfile;
            return null;
        }
    }
}
