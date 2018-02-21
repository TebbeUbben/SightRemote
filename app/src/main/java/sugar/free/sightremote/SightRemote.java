package sugar.free.sightremote;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
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
        Fabric.with(this, new Crashlytics());

        PreferenceManager.setDefaultValues(this, R.xml.settings, true);

        instance = this;
        startService(new Intent(this, SightService.class));
        startService(new Intent(this, HistorySyncService.class));
        startService(new Intent(this, AlertService.class));
    }
}
