package sugar.free.sightremote;

import android.app.Application;
import android.content.Intent;

import sugar.free.sightparser.handling.SightService;

public class SightRemote extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(this, SightService.class);
        startService(intent);
    }
}
