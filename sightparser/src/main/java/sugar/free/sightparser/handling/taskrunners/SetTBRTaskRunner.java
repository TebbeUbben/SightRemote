package sugar.free.sightparser.handling.taskrunners;

import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.messages.remote_control.ChangeTBRMessage;
import sugar.free.sightparser.applayer.messages.remote_control.SetTBRMessage;
import sugar.free.sightparser.applayer.messages.status.CurrentTBRMessage;
import sugar.free.sightparser.handling.SightServiceConnector;
import sugar.free.sightparser.handling.TaskRunner;

public class SetTBRTaskRunner extends TaskRunner {

    private int amount;
    private int duration;

    public SetTBRTaskRunner(SightServiceConnector serviceConnector, int amount, int duration) {
        super(serviceConnector);
        this.amount = amount;
        this.duration = duration;
    }

    @Override
    protected AppLayerMessage run(AppLayerMessage message) throws Exception {
        if (message == null) {
            return new CurrentTBRMessage();
        } else if (message instanceof CurrentTBRMessage) {
            SetTBRMessage setTBRMessage;
            if (((CurrentTBRMessage) message).getPercentage() == 100) setTBRMessage = new SetTBRMessage();
            else setTBRMessage = new ChangeTBRMessage();
            setTBRMessage.setAmount(amount);
            setTBRMessage.setDuration(duration);
            return setTBRMessage;
        } else if (message instanceof SetTBRMessage) finish(null);
        return null;
    }
}
