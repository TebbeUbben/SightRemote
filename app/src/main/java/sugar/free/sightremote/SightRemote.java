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
import sugar.free.sightparser.handling.SightServiceConnector;
import sugar.free.sightremote.services.AlertService;
import sugar.free.sightremote.services.HistorySyncService;

public class SightRemote extends Application {

    @Getter
    private static SightRemote instance;
    @Getter
    private SightServiceConnector serviceConnector;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        PreferenceManager.setDefaultValues(this, R.xml.settings, true);

        instance = this;
        serviceConnector = new SightServiceConnector(this);
        serviceConnector.connectToService();

        startService(new Intent(this, SightService.class));
        startService(new Intent(this, HistorySyncService.class));
        startService(new Intent(this, AlertService.class));
    }
}
