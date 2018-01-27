package sugar.free.sightparser.handling.taskrunners;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import sugar.free.sightparser.applayer.AppLayerMessage;
import sugar.free.sightparser.applayer.history.CloseHistoryReadingSessionMessage;
import sugar.free.sightparser.applayer.history.HistoryFrame;
import sugar.free.sightparser.applayer.history.OpenHistoryReadingSessionMessage;
import sugar.free.sightparser.applayer.history.ReadHistoryFramesMessage;
import sugar.free.sightparser.handling.SightServiceConnector;
import sugar.free.sightparser.handling.TaskRunner;

public class ReadHistoryTaskRunner extends TaskRunner {

    private OpenHistoryReadingSessionMessage openMessage;
    private HistoryResult historyResult = new HistoryResult();

    public ReadHistoryTaskRunner(SightServiceConnector serviceConnector, OpenHistoryReadingSessionMessage openMessage) {
        super(serviceConnector);
        this.openMessage = openMessage;
    }

    @Override
    protected AppLayerMessage run(AppLayerMessage message) throws Exception {
        if (message == null) return openMessage;
        else if (message instanceof OpenHistoryReadingSessionMessage) return new ReadHistoryFramesMessage();
        else if (message instanceof ReadHistoryFramesMessage) {
            ReadHistoryFramesMessage readMessage = (ReadHistoryFramesMessage) message;
            if (readMessage.getHistoryFrames().size() == 0) return new CloseHistoryReadingSessionMessage();
            historyResult.historyFrames.addAll(readMessage.getHistoryFrames());
            if (readMessage.getLatestEventNumber() > historyResult.latestEventNumber)
                historyResult.latestEventNumber = readMessage.getLatestEventNumber();
            return new ReadHistoryFramesMessage();
        } else if (message instanceof CloseHistoryReadingSessionMessage) finish(historyResult);
        return null;
    }

    @Getter
    public static class HistoryResult {
        private int latestEventNumber = -1;
        private List<HistoryFrame> historyFrames = new ArrayList<>();
    }

}
