package sugar.free.sightparser.applayer.messages.status;

import lombok.Getter;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.descriptors.Service;
import sugar.free.sightparser.applayer.descriptors.ActiveBolus;
import sugar.free.sightparser.pipeline.ByteBuf;

public class ActiveBolusesMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

    @Getter
    private ActiveBolus bolus1;
    @Getter
    private ActiveBolus bolus2;
    @Getter
    private ActiveBolus bolus3;

    @Override
    public Service getService() {
        return Service.STATUS;
    }

    @Override
    public short getCommand() {
        return 0x6F06;
    }

    @Override
    protected boolean inCRC() {
        return true;
    }

    @Override
    protected void parse(ByteBuf byteBuf) throws Exception {
        bolus1 = ActiveBolus.parse(byteBuf);
        bolus2 = ActiveBolus.parse(byteBuf);
        bolus3 = ActiveBolus.parse(byteBuf);
        if (bolus1.getLeftoverAmount() == 0) bolus1 = null;
        if (bolus2.getLeftoverAmount() == 0) bolus2 = null;
        if (bolus3.getLeftoverAmount() == 0) bolus3 = null;
    }
}
