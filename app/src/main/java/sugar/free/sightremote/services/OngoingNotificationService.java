package sugar.free.sightremote.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import sugar.free.sightparser.handling.ServiceConnectionCallback;
import sugar.free.sightparser.handling.SightServiceConnector;
import sugar.free.sightparser.pipeline.Status;
import sugar.free.sightremote.R;
import sugar.free.sightremote.utils.NotificationCenter;

public class OngoingNotificationService extends Service {

    private SightServiceConnector serviceConnector;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        serviceConnector = new SightServiceConnector(this);
        serviceConnector.setConnectionCallback(new ServiceConnectionCallback() {
            @Override
            public void onServiceConnected() {
                statusChanged(serviceConnector.getStatus(), 0, 0);
            }

            @Override
            public void onServiceDisconnected() {
                statusChanged(Status.DISCONNECTED, 0, 0);
            }
        });
        serviceConnector.addStatusCallback(this::statusChanged);
        serviceConnector.connectToService();
    }

    private void statusChanged(Status status, long statusTime, long waitTime) {
        switch (status) {
            case CONNECTED:
                NotificationCenter.showOngoingNotification(R.string.connected);
                break;
            case DISCONNECTED:
                NotificationCenter.showOngoingNotification(R.string.disconnected);
                break;
            case CONNECTING:
                NotificationCenter.showOngoingNotification(R.string.connecting);
                break;
            case WAITING:
                NotificationCenter.showOngoingNotification(R.string.waiting);
                break;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        statusChanged(serviceConnector.isConnectedToService() ? serviceConnector.getStatus() : Status.DISCONNECTED, 0, 0);
        startForeground(NotificationCenter.ONGOING_NOTIFICATION_ID, NotificationCenter.getOngoingNotification());
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        serviceConnector.disconnectFromService();
    }
}
