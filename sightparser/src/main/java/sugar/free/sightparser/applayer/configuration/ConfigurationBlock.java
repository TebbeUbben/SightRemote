package sugar.free.sightparser.applayer.configuration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import sugar.free.sightparser.applayer.configuration.blocks.ActiveProfileBlock;
import sugar.free.sightparser.applayer.configuration.blocks.BRName1Block;
import sugar.free.sightparser.applayer.configuration.blocks.BRName2Block;
import sugar.free.sightparser.applayer.configuration.blocks.BRName3Block;
import sugar.free.sightparser.applayer.configuration.blocks.BRName4Block;
import sugar.free.sightparser.applayer.configuration.blocks.BRName5Block;
import sugar.free.sightparser.applayer.configuration.blocks.BRProfile1Block;
import sugar.free.sightparser.applayer.configuration.blocks.BRProfile2Block;
import sugar.free.sightparser.applayer.configuration.blocks.BRProfile3Block;
import sugar.free.sightparser.applayer.configuration.blocks.BRProfile4Block;
import sugar.free.sightparser.applayer.configuration.blocks.BRProfile5Block;
import sugar.free.sightparser.applayer.configuration.blocks.BRProfileBlock;
import sugar.free.sightparser.applayer.configuration.blocks.FactoryMaxBRAmountBlock;
import sugar.free.sightparser.applayer.configuration.blocks.FactoryMaxBolusAmountBlock;
import sugar.free.sightparser.applayer.configuration.blocks.FactoryMinBRAmountBlock;
import sugar.free.sightparser.applayer.configuration.blocks.FactoryMinBolusAmountBlock;
import sugar.free.sightparser.applayer.configuration.blocks.MaxBRAmountBlock;
import sugar.free.sightparser.applayer.configuration.blocks.MaxBolusAmountBlock;
import sugar.free.sightparser.pipeline.ByteBuf;

public abstract class ConfigurationBlock implements Serializable {


    public static final Map<Short, Class<? extends ConfigurationBlock>> CONFIGURATIONBLOCKS = new HashMap<>();

    static {
        CONFIGURATIONBLOCKS.put(FactoryMaxBolusAmountBlock.ID, FactoryMaxBolusAmountBlock.class);
        CONFIGURATIONBLOCKS.put(FactoryMinBolusAmountBlock.ID, FactoryMinBolusAmountBlock.class);
        CONFIGURATIONBLOCKS.put(ActiveProfileBlock.ID, MaxBolusAmountBlock.class);
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
    }

    public abstract short getID();
    public abstract void parse(ByteBuf byteBuf);
    public abstract byte[] getData();

}
