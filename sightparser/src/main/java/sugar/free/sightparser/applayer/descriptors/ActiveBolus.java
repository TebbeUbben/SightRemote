package sugar.free.sightparser.applayer.descriptors;

import java.io.Serializable;

import lombok.Getter;
import sugar.free.sightparser.RoundingUtil;
import sugar.free.sightparser.pipeline.ByteBuf;

public class ActiveBolus implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    private int bolusID;
    @Getter
    private ActiveBolusType bolusType;
    @Getter
    private float initialAmount;
    @Getter
    private float leftoverAmount;
    @Getter
    private int duration;

    public static ActiveBolus parse(ByteBuf byteBuf) {
        ActiveBolus activeBolus = new ActiveBolus();
        activeBolus.bolusID = byteBuf.readUInt16LE();
        activeBolus.bolusType = ActiveBolusType.getBolusType(byteBuf.readShort());
        byteBuf.shift(2);
        byteBuf.shift(2);
        activeBolus.initialAmount = RoundingUtil.roundFloat(((float) byteBuf.readUInt16LE()) / 100F, 2);
        activeBolus.leftoverAmount = RoundingUtil.roundFloat(((float) byteBuf.readUInt16LE()) / 100F, 2);
        activeBolus.duration = byteBuf.readUInt16LE();
        return activeBolus;
    }
}
