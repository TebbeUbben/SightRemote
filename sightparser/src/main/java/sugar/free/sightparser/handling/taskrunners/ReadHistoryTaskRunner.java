package sugar.free.sightparser.handling.taskrunners;

import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.messages.history.CloseHistoryReadingSessionMessage;
import sugar.free.sightparser.applayer.descriptors.history_frames.HistoryFrame;
import sugar.free.sightparser.applayer.messages.history.OpenHistoryReadingSessionMessage;
import sugar.free.sightparser.applayer.messages.history.ReadHistoryFramesMessage;
import sugar.free.sightparser.handling.SightServiceConnector;
import sugar.free.sightparser.handling.TaskRunner;

public class ReadHistoryTaskRunner extends TaskRunner {

    private OpenHistoryReadingSessionMessage openMessage;
    private HistoryResult historyResult = new HistoryResult();
    private int limit;
    private int receivedPackets = 0;

    public ReadHistoryTaskRunner(SightServiceConnector serviceConnector, OpenHistoryReadingSessionMessage openMessage, int limit) {
        super(serviceConnector);
        this.openMessage = openMessage;
        this.limit = limit;
    }

    @Override
    protected AppLayerMessage run(AppLayerMessage message) throws Exception {
        if (message == null) return openMessage;
        else if (message instanceof OpenHistoryReadingSessionMessage) return new ReadHistoryFramesMessage();
        else if (message instanceof ReadHistoryFramesMessage) {
            ReadHistoryFramesMessage readMessage = (ReadHistoryFramesMessage) message;
            historyResult.historyFrames.addAll(readMessage.getHistoryFrames());
            if (readMessage.getLatestEventNumber() > historyResult.latestEventNumber)
                historyResult.latestEventNumber = readMessage.getLatestEventNumber();
            if (readMessage.getLatestEventNumber() == -1 || ++receivedPackets >= limit) return new CloseHistoryReadingSessionMessage();
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
