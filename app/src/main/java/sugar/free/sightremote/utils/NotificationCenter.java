package sugar.free.sightremote.utils;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import lombok.Getter;
import sugar.free.sightremote.R;
import sugar.free.sightremote.SightRemote;

public final class NotificationCenter {

    private static final String ONGOING_NOTIFICATION_CHANNEL_ID = "sugar.free.sightremote.OTHER";
    public static final int ONGOING_NOTIFICATION_ID = 156;

    @Getter
    private static Notification ongoingNotification;

    public static void setupUpChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(ONGOING_NOTIFICATION_CHANNEL_ID,
                    SightRemote.getInstance().getString(R.string.ongoing_notification_name),
                    NotificationManager.IMPORTANCE_MIN);
            notificationChannel.setDescription(SightRemote.getInstance().getString(R.string.ongoing_notification_description));
            NotificationManager notificationManager = (NotificationManager) SightRemote.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public static void showOngoingNotification(int text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(SightRemote.getInstance(), ONGOING_NOTIFICATION_CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setContentTitle(SightRemote.getInstance().getString(R.string.sightremote_is_active));
        builder.setContentText(SightRemote.getInstance().getString(text));
        builder.setPriority(NotificationManagerCompat.IMPORTANCE_MIN);
        builder.setShowWhen(false);

        NotificationManagerCompat.from(SightRemote.getInstance()).notify(ONGOING_NOTIFICATION_ID, ongoingNotification = builder.build());
    }

}
