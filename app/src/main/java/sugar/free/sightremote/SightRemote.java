package sugar.free.sightremote;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import lombok.Getter;
import sugar.free.sightparser.handling.SightService;
import sugar.free.sightparser.handling.SightServiceConnector;
import sugar.free.sightremote.services.AlertService;
import sugar.free.sightremote.services.HistorySyncService;
import sugar.free.sightremote.services.OngoingNotificationService;
import sugar.free.sightremote.utils.NotificationCenter;
import sugar.free.sightremote.utils.Preferences;

public class SightRemote extends Application {

    @Getter
    private static SightRemote instance;
    @Getter
    private SightServiceConnector serviceConnector;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Fabric.with(this, new Crashlytics());

        PreferenceManager.setDefaultValues(this, R.xml.settings, true);

        NotificationCenter.setupUpChannels();
        serviceConnector = new SightServiceConnector(this);
        serviceConnector.connectToService();

        ContextCompat.startForegroundService(this, new Intent(this, OngoingNotificationService.class));
        startService(new Intent(this, SightService.class));
        startService(new Intent(this, HistorySyncService.class));
        startService(new Intent(this, AlertService.class));
    }
}
