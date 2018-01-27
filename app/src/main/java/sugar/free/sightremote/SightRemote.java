package sugar.free.sightremote;

import android.app.Application;
import android.content.Intent;

import sugar.free.sightparser.handling.HistoryBroadcast;
import sugar.free.sightparser.handling.SightService;
import sugar.free.sightremote.services.HistorySyncService;

public class SightRemote extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this, SightService.class));
        startService(new Intent(this, HistorySyncService.class));
        sendBroadcast(new Intent(HistoryBroadcast.ACTION_START_SYNC));
    }
}
