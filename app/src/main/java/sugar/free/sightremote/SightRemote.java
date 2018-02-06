package sugar.free.sightremote;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import lombok.Getter;
import sugar.free.sightparser.handling.HistoryBroadcast;
import sugar.free.sightparser.handling.SightService;
import sugar.free.sightremote.services.AlertService;
import sugar.free.sightremote.services.HistorySyncService;

public class SightRemote extends Application {

    private static SharedPreferences sharedPreferences;

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

    public static SharedPreferences getPreferences() {
        if (sharedPreferences == null)
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getInstance());
        return sharedPreferences;
    }
}
