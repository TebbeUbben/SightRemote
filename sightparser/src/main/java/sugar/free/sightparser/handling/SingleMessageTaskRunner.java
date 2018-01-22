package sugar.free.sightparser.handling;

import sugar.free.sightparser.applayer.AppLayerMessage;

public class SingleMessageTaskRunner extends TaskRunner {

    private AppLayerMessage message;

    public SingleMessageTaskRunner(SightServiceConnector serviceConnector, AppLayerMessage message) {
        super(serviceConnector);
        this.message = message;
    }

    @Override
    protected AppLayerMessage run(AppLayerMessage message) throws Exception {
        if (message == null) return this.message;
        else finish(message);
        return null;
    }
}
