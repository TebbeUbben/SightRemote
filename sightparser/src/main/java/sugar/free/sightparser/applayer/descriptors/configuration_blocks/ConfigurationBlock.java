package sugar.free.sightparser.applayer.descriptors.configuration_blocks;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import sugar.free.sightparser.pipeline.ByteBuf;

public abstract class ConfigurationBlock implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final Map<Short, Class<? extends ConfigurationBlock>> CONFIGURATIONBLOCKS = new HashMap<>();

    static {
        CONFIGURATIONBLOCKS.put(FactoryMaxBolusAmountBlock.ID, FactoryMaxBolusAmountBlock.class);
        CONFIGURATIONBLOCKS.put(FactoryMinBolusAmountBlock.ID, FactoryMinBolusAmountBlock.class);
        CONFIGURATIONBLOCKS.put(MaxBolusAmountBlock.ID, MaxBolusAmountBlock.class);
        CONFIGURATIONBLOCKS.put(ActiveProfileBlock.ID, ActiveProfileBlock.class);
        CONFIGURATIONBLOCKS.put(BRName1Block.ID, BRName1Block.class);
        CONFIGURATIONBLOCKS.put(BRName2Block.ID, BRName2Block.class);
        CONFIGURATIONBLOCKS.put(BRName3Block.ID, BRName3Block.class);
        CONFIGURATIONBLOCKS.put(BRName4Block.ID, BRName4Block.class);
        CONFIGURATIONBLOCKS.put(BRName5Block.ID, BRName5Block.class);
        CONFIGURATIONBLOCKS.put(BRProfile1Block.ID, BRProfile1Block.class);
        CONFIGURATIONBLOCKS.put(BRProfile2Block.ID, BRProfile2Block.class);
        CONFIGURATIONBLOCKS.put(BRProfile3Block.ID, BRProfile3Block.class);
        CONFIGURATIONBLOCKS.put(BRProfile4Block.ID, BRProfile4Block.class);
        CONFIGURATIONBLOCKS.put(BRProfile5Block.ID, BRProfile5Block.class);
        CONFIGURATIONBLOCKS.put(FactoryMaxBRAmountBlock.ID, FactoryMaxBRAmountBlock.class);
        CONFIGURATIONBLOCKS.put(FactoryMinBRAmountBlock.ID, FactoryMinBRAmountBlock.class);
        CONFIGURATIONBLOCKS.put(MaxBRAmountBlock.ID, MaxBRAmountBlock.class);
        CONFIGURATIONBLOCKS.put(CustomBolus1Block.ID, CustomBolus1Block.class);
        CONFIGURATIONBLOCKS.put(CustomBolus2Block.ID, CustomBolus2Block.class);
        CONFIGURATIONBLOCKS.put(CustomBolus3Block.ID, CustomBolus3Block.class);
        CONFIGURATIONBLOCKS.put(CustomBolus4Block.ID, CustomBolus4Block.class);
        CONFIGURATIONBLOCKS.put(CustomBolus5Block.ID, CustomBolus5Block.class);
        CONFIGURATIONBLOCKS.put(CustomBolus6Block.ID, CustomBolus6Block.class);
        CONFIGURATIONBLOCKS.put(CustomBolus7Block.ID, CustomBolus7Block.class);
        CONFIGURATIONBLOCKS.put(CustomBolus8Block.ID, CustomBolus8Block.class);
        CONFIGURATIONBLOCKS.put(CustomBolus9Block.ID, CustomBolus9Block.class);
        CONFIGURATIONBLOCKS.put(CustomBolus10Block.ID, CustomBolus10Block.class);
        CONFIGURATIONBLOCKS.put(CustomBolus1NameBlock.ID, CustomBolus1NameBlock.class);
        CONFIGURATIONBLOCKS.put(CustomBolus2NameBlock.ID, CustomBolus2NameBlock.class);
        CONFIGURATIONBLOCKS.put(CustomBolus3NameBlock.ID, CustomBolus3NameBlock.class);
        CONFIGURATIONBLOCKS.put(CustomBolus4NameBlock.ID, CustomBolus4NameBlock.class);
        CONFIGURATIONBLOCKS.put(CustomBolus5NameBlock.ID, CustomBolus5NameBlock.class);
        CONFIGURATIONBLOCKS.put(CustomBolus6NameBlock.ID, CustomBolus6NameBlock.class);
        CONFIGURATIONBLOCKS.put(CustomBolus7NameBlock.ID, CustomBolus7NameBlock.class);
        CONFIGURATIONBLOCKS.put(CustomBolus8NameBlock.ID, CustomBolus8NameBlock.class);
        CONFIGURATIONBLOCKS.put(CustomBolus9NameBlock.ID, CustomBolus9NameBlock.class);
        CONFIGURATIONBLOCKS.put(CustomBolus10NameBlock.ID, CustomBolus10NameBlock.class);
        CONFIGURATIONBLOCKS.put(CustomTBR1Block.ID, CustomTBR1Block.class);
        CONFIGURATIONBLOCKS.put(CustomTBR2Block.ID, CustomTBR2Block.class);
        CONFIGURATIONBLOCKS.put(CustomTBR3Block.ID, CustomTBR3Block.class);
        CONFIGURATIONBLOCKS.put(CustomTBR4Block.ID, CustomTBR4Block.class);
        CONFIGURATIONBLOCKS.put(CustomTBR5Block.ID, CustomTBR5Block.class);
        CONFIGURATIONBLOCKS.put(CustomTBR1NameBlock.ID, CustomTBR1NameBlock.class);
        CONFIGURATIONBLOCKS.put(CustomTBR2NameBlock.ID, CustomTBR2NameBlock.class);
        CONFIGURATIONBLOCKS.put(CustomTBR3NameBlock.ID, CustomTBR3NameBlock.class);
        CONFIGURATIONBLOCKS.put(CustomTBR4NameBlock.ID, CustomTBR4NameBlock.class);
        CONFIGURATIONBLOCKS.put(CustomTBR5NameBlock.ID, CustomTBR5NameBlock.class);
    }

    public abstract short getID();
    public abstract void parse(ByteBuf byteBuf);
    public abstract byte[] getData();

}
