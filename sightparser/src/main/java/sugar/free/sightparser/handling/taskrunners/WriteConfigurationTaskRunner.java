package sugar.free.sightparser.handling.taskrunners;

import java.util.List;

import sugar.free.sightparser.applayer.AppLayerMessage;
import sugar.free.sightparser.applayer.configuration.CloseWriteSessionMessage;
import sugar.free.sightparser.applayer.configuration.ConfigurationBlock;
import sugar.free.sightparser.applayer.configuration.OpenWriteSessionMessage;
import sugar.free.sightparser.applayer.configuration.WriteConfigurationBlockMessage;
import sugar.free.sightparser.handling.SightServiceConnector;
import sugar.free.sightparser.handling.TaskRunner;

public class WriteConfigurationTaskRunner extends TaskRunner {

    private List<ConfigurationBlock> configurationBlocks;

    public WriteConfigurationTaskRunner(SightServiceConnector serviceConnector, List<ConfigurationBlock> configurationBlocks) {
        super(serviceConnector);
        this.configurationBlocks = configurationBlocks;
    }

    @Override
    protected AppLayerMessage run(AppLayerMessage message) throws Exception {
        if (message == null) return new OpenWriteSessionMessage();
        else if (message instanceof OpenWriteSessionMessage || message instanceof WriteConfigurationBlockMessage) {
            if (configurationBlocks.size() == 0) return new CloseWriteSessionMessage();
            else {
                WriteConfigurationBlockMessage writeMessage = new WriteConfigurationBlockMessage();
                writeMessage.setConfigurationBlock(configurationBlocks.get(0));
                configurationBlocks.remove(0);
                return writeMessage;
            }
        } else if (message instanceof CloseWriteSessionMessage) finish(null);
        return null;
    }
}
