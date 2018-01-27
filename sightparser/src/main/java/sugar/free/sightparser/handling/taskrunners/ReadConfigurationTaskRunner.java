package sugar.free.sightparser.handling.taskrunners;

import java.util.ArrayList;
import java.util.List;

import sugar.free.sightparser.applayer.AppLayerMessage;
import sugar.free.sightparser.applayer.configuration.ConfigurationBlock;
import sugar.free.sightparser.applayer.configuration.ReadConfigurationBlockMessage;
import sugar.free.sightparser.handling.SightServiceConnector;
import sugar.free.sightparser.handling.TaskRunner;

public class ReadConfigurationTaskRunner extends TaskRunner {

    private List<Short> IDs;
    private List<ConfigurationBlock> configurationBlocks = new ArrayList<>();

    public ReadConfigurationTaskRunner(SightServiceConnector serviceConnector, List<Short> IDs) {
        super(serviceConnector);
        this.IDs = IDs;
    }

    @Override
    protected AppLayerMessage run(AppLayerMessage message) throws Exception {
        if (message instanceof ReadConfigurationBlockMessage) {
            ReadConfigurationBlockMessage readMessage = (ReadConfigurationBlockMessage) message;
            configurationBlocks.add(readMessage.getConfigurationBlock());
        }
        if (IDs.size() != 0) {
            ReadConfigurationBlockMessage readMessage = new ReadConfigurationBlockMessage();
            readMessage.setConfigurationBlockID(IDs.get(0));
            IDs.remove(0);
            return readMessage;
        } else finish(configurationBlocks);
        return null;
    }
}
