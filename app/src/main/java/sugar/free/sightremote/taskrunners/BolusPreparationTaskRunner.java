package sugar.free.sightremote.taskrunners;

import java.io.Serializable;

import lombok.Getter;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.messages.configuration.ReadConfigurationBlockMessage;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.FactoryMinBolusAmountBlock;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.MaxBolusAmountBlock;
import sugar.free.sightparser.applayer.messages.remote_control.AvailableBolusesMessage;
import sugar.free.sightparser.applayer.descriptors.PumpStatus;
import sugar.free.sightparser.applayer.messages.status.PumpStatusMessage;
import sugar.free.sightparser.handling.SightServiceConnector;
import sugar.free.sightparser.handling.TaskRunner;

public class BolusPreparationTaskRunner extends TaskRunner {

    private PreperationResult preperationResult = new PreperationResult();

    public BolusPreparationTaskRunner(SightServiceConnector serviceConnector) {
        super(serviceConnector);
    }

    @Override
    protected AppLayerMessage run(AppLayerMessage message) throws Exception {
        if (message == null) return new PumpStatusMessage();
        else if (message instanceof PumpStatusMessage) {
            preperationResult.pumpStarted = ((PumpStatusMessage) message).getPumpStatus().equals(PumpStatus.STARTED);
            if (preperationResult.pumpStarted) return new AvailableBolusesMessage();
            else {
                ReadConfigurationBlockMessage readMessage = new ReadConfigurationBlockMessage();
                readMessage.setConfigurationBlockID(FactoryMinBolusAmountBlock.ID);
                return readMessage;
            }
        } else if (message instanceof AvailableBolusesMessage) {
            preperationResult.availableBoluses = (AvailableBolusesMessage) message;
            ReadConfigurationBlockMessage readMessage = new ReadConfigurationBlockMessage();
            readMessage.setConfigurationBlockID(FactoryMinBolusAmountBlock.ID);
            return readMessage;
        } else {
            ReadConfigurationBlockMessage readMessage = (ReadConfigurationBlockMessage) message;
            if (readMessage.getConfigurationBlock() instanceof FactoryMinBolusAmountBlock) {
                preperationResult.minBolusAmount =((FactoryMinBolusAmountBlock) readMessage.getConfigurationBlock()).getMinimumAmount();
                readMessage = new ReadConfigurationBlockMessage();
                readMessage.setConfigurationBlockID(MaxBolusAmountBlock.ID);
                return readMessage;
            } else if (readMessage.getConfigurationBlock() instanceof MaxBolusAmountBlock) {
                preperationResult.maxBolusAmount = ((MaxBolusAmountBlock) readMessage.getConfigurationBlock()).getMaximumAmount();
                finish(preperationResult);
            }
        }
        return null;
    }

    @Getter
    public static final class PreperationResult implements Serializable {
        private boolean pumpStarted;
        private double minBolusAmount;
        private double maxBolusAmount;
        private AvailableBolusesMessage availableBoluses;
    }
}
