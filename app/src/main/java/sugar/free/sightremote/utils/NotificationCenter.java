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

    private static final String ONGOING_NOTIFICATION_CHANNEL_ID = "sugar.free.sightremote.ONGOING";
    private static final String TIME_SYNCHRONIZED_NOTIFICATION_CHANNEL_ID = "sugar.free.sightremote.TIME_SYNCHRONIZED";
    public static final int ONGOING_NOTIFICATION_ID = 156;
    public static final int TIME_SYNCHRONIZED_NOTIFICATION_ID = 157;

    @Getter
    private static Notification ongoingNotification;

    public static void setupUpChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) SightRemote.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel ongoingNotificationChannel = new NotificationChannel(ONGOING_NOTIFICATION_CHANNEL_ID,
                    SightRemote.getInstance().getString(R.string.ongoing_notification_name),
                    NotificationManager.IMPORTANCE_MIN);
            ongoingNotificationChannel.setDescription(SightRemote.getInstance().getString(R.string.ongoing_notification_description));
            notificationManager.createNotificationChannel(ongoingNotificationChannel);

            NotificationChannel timeSynchronizedNotificationChannel = new NotificationChannel(TIME_SYNCHRONIZED_NOTIFICATION_CHANNEL_ID,
                    SightRemote.getInstance().getString(R.string.pump_time_synchronized),
                    NotificationManager.IMPORTANCE_DEFAULT);
            timeSynchronizedNotificationChannel.setDescription(SightRemote.getInstance().getString(R.string.notifies_you_when_pump_time_is_changed));
            notificationManager.createNotificationChannel(timeSynchronizedNotificationChannel);
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

    public static void showTimeSynchronizedNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(SightRemote.getInstance(), TIME_SYNCHRONIZED_NOTIFICATION_CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setContentTitle(SightRemote.getInstance().getString(R.string.pump_time_synchronized));
        builder.setContentText(SightRemote.getInstance().getString(R.string.the_time_on_your_pump_was_inaccurate_and_has_been_synchronized));
        builder.setPriority(NotificationManagerCompat.IMPORTANCE_DEFAULT);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(SightRemote.getInstance().getString(R.string.the_time_on_your_pump_was_inaccurate_and_has_been_synchronized)));

        NotificationManagerCompat.from(SightRemote.getInstance()).notify(TIME_SYNCHRONIZED_NOTIFICATION_ID, ongoingNotification = builder.build());
    }

}
