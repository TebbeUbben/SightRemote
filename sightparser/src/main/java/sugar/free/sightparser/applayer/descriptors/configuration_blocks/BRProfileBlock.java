package sugar.free.sightparser.applayer.descriptors.configuration_blocks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.RoundingUtil;
import sugar.free.sightparser.pipeline.ByteBuf;

public abstract class BRProfileBlock extends ConfigurationBlock {

    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private List<ProfileBlock> profileBlocks;

    @Override
    public void parse(ByteBuf byteBuf) {
        profileBlocks = new ArrayList<>();
        int duration;
        for (int i = 0; i < 24; i++)
            if ((duration = byteBuf.readUInt16LE()) > 0)
                profileBlocks.add(new ProfileBlock(duration, 0));
        for (ProfileBlock profileBlock : profileBlocks)
            profileBlock.setAmount(((double) byteBuf.readUInt16LE()) / 100D);
    }

    @Override
    public byte[] getData() {
        ByteBuf byteBuf = new ByteBuf(96);
        for (int i = 0; i < 24; i++) {
            if (i < profileBlocks.size())
                byteBuf.putUInt16LE(profileBlocks.get(i).getDuration());
            else byteBuf.putShort((short) 0x0000);
        }
        for (int i = 0; i < 24; i++) {
            if (i < profileBlocks.size())
                byteBuf.putUInt16LE((int) (profileBlocks.get(i).getAmount() * 100D));
            else byteBuf.putShort((short) 0x0000);
        }
        return byteBuf.getBytes();
    }

    @Setter
    @Getter
    public static class ProfileBlock implements Serializable {
        private int duration;
        private double amount;

        public ProfileBlock(int duration, double amount) {
            this.duration = duration;
            this.amount = amount;
        }
    }

    public double getTotalAmount() {
        double total = 0;
        for (ProfileBlock profileBlock : profileBlocks)
            total += RoundingUtil.roundDouble(profileBlock.getAmount() / 60D * ((double) profileBlock.getDuration()), 2);
        return total;
    }
}
