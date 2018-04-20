package sugar.free.sightremote.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import sugar.free.sightremote.services.HistorySyncService;

public class HistoryReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, HistorySyncService.class);
        serviceIntent.putExtra("action", intent.getAction());
        context.startService(serviceIntent);
    }
}
