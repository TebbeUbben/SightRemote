package sugar.free.sightremote;

import android.app.Application;
import android.content.Intent;

import lombok.Getter;
import sugar.free.sightparser.handling.HistoryBroadcast;
import sugar.free.sightparser.handling.SightService;
import sugar.free.sightremote.services.AlertService;
import sugar.free.sightremote.services.HistorySyncService;

public class SightRemote extends Application {

    @Getter
    private static SightRemote instance;


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        startService(new Intent(this, SightService.class));
        startService(new Intent(this, HistorySyncService.class));
        startService(new Intent(this, AlertService.class));
    }
}
