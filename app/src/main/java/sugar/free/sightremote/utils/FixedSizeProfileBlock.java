package sugar.free.sightremote.utils;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.BRProfileBlock;

@Setter
@Getter
public class FixedSizeProfileBlock {

    private int startTime;
    private int endTime;
    private float amount;

    public FixedSizeProfileBlock(int startTime, int endTime, float amount) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.amount = amount;
    }

    public static List<FixedSizeProfileBlock> convertToFixed(List<BRProfileBlock.ProfileBlock> profileBlocks) {
        List<FixedSizeProfileBlock> fixedSizeProfileBlocks = new ArrayList<>();
        int startTime = 0;
        for (BRProfileBlock.ProfileBlock profileBlock : profileBlocks)
            fixedSizeProfileBlocks.add(new FixedSizeProfileBlock(startTime,
                    (startTime += profileBlock.getDuration()), profileBlock.getAmount()));
        return fixedSizeProfileBlocks;
    }

    public static List<BRProfileBlock.ProfileBlock> convertToRelative(List<FixedSizeProfileBlock> fixedSizeProfileBlocks) {
        List<BRProfileBlock.ProfileBlock> profileBlocks = new ArrayList<>();
        for (FixedSizeProfileBlock fixedSizeProfileBlock : fixedSizeProfileBlocks)
            profileBlocks.add(new BRProfileBlock.ProfileBlock((short) fixedSizeProfileBlock.getDuration(), fixedSizeProfileBlock.getAmount()));
        return profileBlocks;
    }

    public int getDuration() {
        return endTime - startTime;
    }
}
