package sugar.free.sightparser.applayer.configuration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import sugar.free.sightparser.applayer.configuration.blocks.FactoryMaxBolusAmountBlock;
import sugar.free.sightparser.applayer.configuration.blocks.FactoryMinBolusAmountBlock;
import sugar.free.sightparser.applayer.configuration.blocks.MaxBolusAmountBlock;
import sugar.free.sightparser.pipeline.ByteBuf;

public abstract class ConfigurationBlock implements Serializable {


    public static final Map<Short, Class<? extends ConfigurationBlock>> CONFIGURATIONBLOCKS = new HashMap<>();

    static {
        CONFIGURATIONBLOCKS.put(FactoryMaxBolusAmountBlock.ID, FactoryMaxBolusAmountBlock.class);
        CONFIGURATIONBLOCKS.put(FactoryMinBolusAmountBlock.ID, FactoryMinBolusAmountBlock.class);
        CONFIGURATIONBLOCKS.put(MaxBolusAmountBlock.ID, MaxBolusAmountBlock.class);
    }

    public abstract short getID();
    public abstract void parse(ByteBuf byteBuf);
    public abstract byte[] getData();

}
