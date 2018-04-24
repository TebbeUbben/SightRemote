package sugar.free.sightremote.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.util.Timer;
import java.util.TimerTask;

import lombok.Setter;
import sugar.free.sightparser.SerializationUtils;
import sugar.free.sightparser.applayer.descriptors.AlertStatus;
import sugar.free.sightparser.applayer.descriptors.alerts.Alert;
import sugar.free.sightparser.applayer.descriptors.alerts.Error7ElectronicError;
import sugar.free.sightparser.applayer.messages.remote_control.DismissAlertMessage;
import sugar.free.sightparser.applayer.messages.remote_control.MuteAlertMessage;
import sugar.free.sightparser.applayer.messages.status.ActiveAlertMessage;
import sugar.free.sightparser.handling.ServiceConnectionCallback;
import sugar.free.sightparser.handling.SightServiceConnector;
import sugar.free.sightparser.handling.SingleMessageTaskRunner;
import sugar.free.sightparser.handling.StatusCallback;
import sugar.free.sightparser.handling.TaskRunner;
import sugar.free.sightparser.pipeline.Status;
import sugar.free.sightremote.R;
import sugar.free.sightremote.activities.AlertActivity;
import sugar.free.sightremote.utils.CrashlyticsUtil;

public class AlertService extends Service implements StatusCallback, ServiceConnectionCallback, TaskRunner.ResultCallback {

    private SightServiceConnector serviceConnector;
    private Timer fetchTimer;
    @Setter
    private AlertActivity alertActivity;
    private int latestId;

    @Override
    public IBinder onBind(Intent intent) {
        return new AlertServiceBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceConnector = new SightServiceConnector(this);
        serviceConnector.connectToService();
        serviceConnector.setConnectionCallback(this);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (fetchTimer != null) fetchTimer.cancel();
        serviceConnector.disconnectFromService();
    }

    @Override
    public void onStatusChange(Status status, long statusTime, long waitTime) {
        if (status == Status.CONNECTED) {
            if (fetchTimer != null) return;
            fetchTimer = new Timer(false);
            fetchTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    new SingleMessageTaskRunner(serviceConnector, new ActiveAlertMessage()).fetch(AlertService.this);
                }
            }, 0, 2000);
        } else {
            if (alertActivity != null) alertActivity.finish();
            if (fetchTimer != null) {
                fetchTimer.cancel();
                fetchTimer = null;
            }
            serviceConnector.disconnect();
        }
    }

    @Override
    public void onServiceConnected() {
        serviceConnector.addStatusCallback(this);
        onStatusChange(serviceConnector.getStatus(), 0, 0);
    }

    @Override
    public void onServiceDisconnected() {
        if (fetchTimer != null) {
            fetchTimer.cancel();
            fetchTimer = null;
        }
        latestId = -1;
        if (alertActivity != null) alertActivity.finish();
    }

    @Override
    public void onResult(Object result) {
        if (result instanceof ActiveAlertMessage) {
            ActiveAlertMessage activeAlertMessage = (ActiveAlertMessage) result;
            if (activeAlertMessage.getAlert() instanceof Error7ElectronicError) {
                serviceConnector.disconnect();
                return;
            }
            Alert alert = activeAlertMessage.getAlert();
            if (alert == null) {
                serviceConnector.disconnect();
                return;
            } else serviceConnector.connect();
            if (latestId != activeAlertMessage.getAlertID()) {
                if (alertActivity != null) alertActivity.finish();
                if (activeAlertMessage.getAlertStatus() == AlertStatus.MUTED) return;
                latestId = activeAlertMessage.getAlertID();
                Intent intent = new Intent(this, AlertActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("alertMessage", SerializationUtils.serialize(activeAlertMessage));
                startActivity(intent);
                Answers.getInstance().logCustom(new CustomEvent("Active Alert").putCustomAttribute("Alert", alert.getClass().getSimpleName()));
            } else if (alertActivity != null) {
                alertActivity.setAlertMessage(activeAlertMessage);
                alertActivity.update();
            }
        }
    }

    public void muteAlert() {
        MuteAlertMessage muteAlertMessage = new MuteAlertMessage();
        muteAlertMessage.setAlertID(latestId);
        new SingleMessageTaskRunner(serviceConnector, muteAlertMessage).fetch(errorToastCallback);
        Answers.getInstance().logCustom(new CustomEvent("Mute Alert"));
    }

    public void dismissAlert() {
        DismissAlertMessage dismissAlertMessage = new DismissAlertMessage();
        dismissAlertMessage.setAlertID(latestId);
        new SingleMessageTaskRunner(serviceConnector, dismissAlertMessage).fetch(errorToastCallback);
        Answers.getInstance().logCustom(new CustomEvent("Dismiss Alert"));
    }

    @Override
    public void onError(Exception e) {
        CrashlyticsUtil.logExceptionWithCallStackTrace(e);
    }

    public class AlertServiceBinder extends Binder {

        public AlertService getService() {
            return AlertService.this;
        }

    }

    private TaskRunner.ResultCallback errorToastCallback = new TaskRunner.ResultCallback() {
        @Override
        public void onResult(Object result) {
            new SingleMessageTaskRunner(serviceConnector, new ActiveAlertMessage()).fetch(AlertService.this);
        }

        @Override
        public void onError(Exception e) {
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(AlertService.this, R.string.error, Toast.LENGTH_SHORT).show());
        }
    };
}
