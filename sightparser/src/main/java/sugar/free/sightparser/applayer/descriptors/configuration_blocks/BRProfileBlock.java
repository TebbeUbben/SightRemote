package sugar.free.sightparser.applayer.descriptors.configuration_blocks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.RoundingUtil;
import sugar.free.sightparser.pipeline.ByteBuf;

public abstract class BRProfileBlock extends ConfigurationBlock {

    @Getter
    @Setter
    private List<ProfileBlock> profileBlocks;

    @Override
    public void parse(ByteBuf byteBuf) {
        profileBlocks = new ArrayList<>();
        short duration;
        for (int i = 0; i < 24; i++)
            if ((duration = byteBuf.readShortLE()) > 0)
                profileBlocks.add(new ProfileBlock(duration, 0));
        for (ProfileBlock profileBlock : profileBlocks)
            profileBlock.setAmount(((float) byteBuf.readShortLE()) / 100F);
    }

    @Override
    public byte[] getData() {
        ByteBuf byteBuf = new ByteBuf(96);
        for (int i = 0; i < 24; i++) {
            if (i < profileBlocks.size())
                byteBuf.putShortLE(profileBlocks.get(i).getDuration());
            else byteBuf.putShort((short) 0x0000);
        }
        for (int i = 0; i < 24; i++) {
            if (i < profileBlocks.size())
                byteBuf.putShortLE((short) (profileBlocks.get(i).getAmount() * 100F));
            else byteBuf.putShort((short) 0x0000);
        }
        return byteBuf.getBytes();
    }

    @Setter
    @Getter
    public static class ProfileBlock implements Serializable {
        private short duration;
        private float amount;

        public ProfileBlock(short duration, float amount) {
            this.duration = duration;
            this.amount = amount;
        }
    }

    public float getTotalAmount() {
        float total = 0;
        for (ProfileBlock profileBlock : profileBlocks)
            total += RoundingUtil.roundFloat(profileBlock.getAmount() / 60 * profileBlock.getDuration(), 2);
        return total;
    }
}
