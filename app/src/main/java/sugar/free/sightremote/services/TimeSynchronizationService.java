package sugar.free.sightremote.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import sugar.free.sightparser.applayer.messages.configuration.WriteDateTimeMessage;
import sugar.free.sightparser.applayer.messages.status.ReadDateTimeMessage;
import sugar.free.sightparser.handling.SightServiceConnector;
import sugar.free.sightparser.handling.SingleMessageTaskRunner;
import sugar.free.sightparser.handling.StatusCallback;
import sugar.free.sightparser.handling.TaskRunner;
import sugar.free.sightparser.pipeline.Status;
import sugar.free.sightremote.utils.NotificationCenter;

import java.util.Calendar;

public class TimeSynchronizationService extends Service implements StatusCallback, TaskRunner.ResultCallback {

    private SightServiceConnector serviceConnector;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        serviceConnector = new SightServiceConnector(this);
        serviceConnector.addStatusCallback(this);
        serviceConnector.connectToService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (serviceConnector.isConnectedToService()) {
            serviceConnector.connect();
            if (serviceConnector.getStatus() == Status.CONNECTED)
                onStatusChange(serviceConnector.getStatus());
        }
        Intent serviceIntent = new Intent(this, TimeSynchronizationService.class);
        pendingIntent = PendingIntent.getService(this, 0, serviceIntent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60 * 60 * 1000, pendingIntent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (pendingIntent != null) alarmManager.cancel(pendingIntent);
        if (serviceConnector.isConnectedToService()) serviceConnector.disconnect();
        serviceConnector.disconnectFromService();
    }

    @Override
    public void onStatusChange(Status status) {
        if (status == Status.CONNECTED) {
            serviceConnector.connect();
            new SingleMessageTaskRunner(serviceConnector, new ReadDateTimeMessage()).fetch(this);
        } else serviceConnector.disconnect();
    }

    @Override
    public void onResult(Object result) {
        if (result instanceof ReadDateTimeMessage) {
            ReadDateTimeMessage dateTime = (ReadDateTimeMessage) result;
            if (Math.abs(parseDateTime(dateTime) - System.currentTimeMillis()) >= 1000 * 30) {
                new SingleMessageTaskRunner(serviceConnector, createWriteMessage()).fetch(this);
            }
        } else if (result instanceof WriteDateTimeMessage) {
            NotificationCenter.showTimeSynchronizedNotification();
            serviceConnector.disconnect();
        }
    }

    private WriteDateTimeMessage createWriteMessage() {
        Calendar calendar = Calendar.getInstance();
        WriteDateTimeMessage writeMessage = new WriteDateTimeMessage();
        writeMessage.setYear(calendar.get(Calendar.YEAR));
        writeMessage.setMonth(calendar.get(Calendar.MONTH) + 1);
        writeMessage.setDay(calendar.get(Calendar.DAY_OF_MONTH));
        writeMessage.setHour(calendar.get(Calendar.HOUR_OF_DAY));
        writeMessage.setMinute(calendar.get(Calendar.MINUTE));
        writeMessage.setSecond(calendar.get(Calendar.SECOND));
        return writeMessage;
    }

    private long parseDateTime(ReadDateTimeMessage dateTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, dateTime.getYear());
        calendar.set(Calendar.MONTH, dateTime.getMonth() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, dateTime.getDay());
        calendar.set(Calendar.HOUR_OF_DAY, dateTime.getHour());
        calendar.set(Calendar.MINUTE, dateTime.getMinute());
        calendar.set(Calendar.SECOND, dateTime.getSecond());
        return calendar.getTime().getTime();
    }

    @Override
    public void onError(Exception e) {
        serviceConnector.disconnect();
    }
}
