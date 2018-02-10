package sugar.free.sightremote.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import lombok.Setter;
import sugar.free.sightparser.SerializationUtils;
import sugar.free.sightparser.applayer.descriptors.AlertStatus;
import sugar.free.sightparser.applayer.descriptors.alerts.Alert;
import sugar.free.sightparser.applayer.descriptors.alerts.Error10RewindError;
import sugar.free.sightparser.applayer.descriptors.alerts.Error13LanguageError;
import sugar.free.sightparser.applayer.descriptors.alerts.Error6MechanicalError;
import sugar.free.sightparser.applayer.descriptors.alerts.Maintenance20CartridgeNotInserted;
import sugar.free.sightparser.applayer.descriptors.alerts.Maintenance21CartridgeEmpty;
import sugar.free.sightparser.applayer.descriptors.alerts.Maintenance22BatteryEmpty;
import sugar.free.sightparser.applayer.descriptors.alerts.Maintenance23AutomaticOff;
import sugar.free.sightparser.applayer.descriptors.alerts.Maintenance24Occlusion;
import sugar.free.sightparser.applayer.descriptors.alerts.Maintenance25LoantimeOver;
import sugar.free.sightparser.applayer.descriptors.alerts.Maintenance26CartridgeChangeNotCompleted;
import sugar.free.sightparser.applayer.descriptors.alerts.Maintenance27DataDownloadFailed;
import sugar.free.sightparser.applayer.descriptors.alerts.Maintenance28PauseModeTimeout;
import sugar.free.sightparser.applayer.descriptors.alerts.Maintenance29BatteryTypeNotSet;
import sugar.free.sightparser.applayer.descriptors.alerts.Maintenance30CartridgeTypeNotSet;
import sugar.free.sightparser.applayer.descriptors.alerts.Reminder1DeliverBolus;
import sugar.free.sightparser.applayer.descriptors.alerts.Reminder2MissedBolus;
import sugar.free.sightparser.applayer.descriptors.alerts.Reminder3AlarmClock;
import sugar.free.sightparser.applayer.descriptors.alerts.Reminder4ChangeInfusionSet;
import sugar.free.sightparser.applayer.descriptors.alerts.Reminder7TBRCompleted;
import sugar.free.sightparser.applayer.descriptors.alerts.Warning31CartridgeLow;
import sugar.free.sightparser.applayer.descriptors.alerts.Warning32BatteryLow;
import sugar.free.sightparser.applayer.descriptors.alerts.Warning33InvalidDateTime;
import sugar.free.sightparser.applayer.descriptors.alerts.Warning34EndOfWarranty;
import sugar.free.sightparser.applayer.descriptors.alerts.Warning36TBRCancelled;
import sugar.free.sightparser.applayer.descriptors.alerts.Warning38BolusCancelled;
import sugar.free.sightparser.applayer.descriptors.alerts.Warning39LoantimeWarning;
import sugar.free.sightparser.applayer.messages.status.ActiveAlertMessage;
import sugar.free.sightremote.R;
import sugar.free.sightremote.services.AlertService;
import sugar.free.sightremote.utils.HTMLUtil;
import sugar.free.sightremote.utils.UnitFormatter;

import static sugar.free.sightremote.utils.Preferences.*;

public class AlertActivity extends AppCompatActivity implements View.OnClickListener {

    private ServiceConnection serviceConnection;

    @Setter
    private ActiveAlertMessage alertMessage;
    private AlertService alertService;
    private Vibrator vibrator;
    private boolean alerting;
    private Ringtone ringtone;

    private TextView alertCode;
    private TextView alertTitle;
    private TextView alertDescription;
    private FloatingActionButton mute;
    private FloatingActionButton dismiss;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        String selectedTone = getStringPref(PREF_STRING_ALERT_ALARM_TONE);
        Uri uri = selectedTone == null ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE) : Uri.parse(selectedTone);
        ringtone = RingtoneManager.getRingtone(this, uri);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                             WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                             WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                             WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = 1.0F;
        getWindow().setAttributes(layoutParams);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        if (savedInstanceState == null)
            alertMessage = (ActiveAlertMessage) SerializationUtils.deserialize(getIntent().getByteArrayExtra("alertMessage"));
        else
            alertMessage = (ActiveAlertMessage) SerializationUtils.deserialize(savedInstanceState.getByteArray("alertMessage"));

        setContentView(getLayoutFile());

        alertCode = findViewById(R.id.alert_code);
        alertTitle = findViewById(R.id.alert_title);
        alertDescription = findViewById(R.id.alert_description);
        mute = findViewById(R.id.mute);
        dismiss = findViewById(R.id.dismiss);

        mute.setOnClickListener(this);
        dismiss.setOnClickListener(this);

        setAlertCode();
        setAlertTitle();
        setAlertDescription();
        update();

        bindService(new Intent(this, AlertService.class), serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                alertService = ((AlertService.AlertServiceBinder) service).getService();
                alertService.setAlertActivity(AlertActivity.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                finish();
            }
        }, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (alerting) {
            vibrator.cancel();
            vibrate();
        }
    }

    public void update() {
        runOnUiThread(() -> {
            if (alertMessage.getAlertStatus() == AlertStatus.ACTIVE) {
                mute.show();
                if (!alerting) {
                    vibrate();
                    if (ringtone != null) ringtone.play();
                }
            } else if (alertMessage.getAlertStatus() == AlertStatus.MUTED) {
                mute.hide();
                vibrator.cancel();
                if (ringtone != null) ringtone.stop();
                alerting = false;
            }
        });
    }

    private void vibrate() {
        vibrator.vibrate(new long[] {0, 1000, 1000}, 0);
        alerting = true;
    }

    private void setAlertCode() {
        Alert alert = alertMessage.getAlert();
        if (alert instanceof Reminder1DeliverBolus)
            alertCode.setText(R.string.alert_r1_code);
        else if (alert instanceof Reminder2MissedBolus)
            alertCode.setText(R.string.alert_r2_code);
        else if (alert instanceof Reminder3AlarmClock)
            alertCode.setText(R.string.alert_r3_code);
        else if (alert instanceof Reminder4ChangeInfusionSet)
            alertCode.setText(R.string.alert_r4_code);
        else if (alert instanceof Reminder7TBRCompleted)
            alertCode.setText(R.string.alert_r7_code);

        else if (alert instanceof Warning31CartridgeLow)
            alertCode.setText(R.string.alert_w31_code);
        else if (alert instanceof Warning32BatteryLow)
            alertCode.setText(R.string.alert_w32_code);
        else if (alert instanceof Warning33InvalidDateTime)
            alertCode.setText(R.string.alert_w33_code);
        else if (alert instanceof Warning34EndOfWarranty)
            alertCode.setText(R.string.alert_w34_code);
        else if (alert instanceof Warning36TBRCancelled)
            alertCode.setText(R.string.alert_w36_code);
        else if (alert instanceof Warning38BolusCancelled)
            alertCode.setText(R.string.alert_w38_code);
        else if (alert instanceof Warning39LoantimeWarning)
            alertCode.setText(R.string.alert_w39_code);

        else if (alert instanceof Maintenance20CartridgeNotInserted)
            alertCode.setText(R.string.alert_m20_code);
        else if (alert instanceof Maintenance21CartridgeEmpty)
            alertCode.setText(R.string.alert_m21_code);
        else if (alert instanceof Maintenance22BatteryEmpty)
            alertCode.setText(R.string.alert_m22_code);
        else if (alert instanceof Maintenance23AutomaticOff)
            alertCode.setText(R.string.alert_m23_code);
        else if (alert instanceof Maintenance24Occlusion)
            alertCode.setText(R.string.alert_m24_code);
        else if (alert instanceof Maintenance25LoantimeOver)
            alertCode.setText(R.string.alert_m25_code);
        else if (alert instanceof Maintenance26CartridgeChangeNotCompleted)
            alertCode.setText(R.string.alert_m26_code);
        else if (alert instanceof Maintenance27DataDownloadFailed)
            alertCode.setText(R.string.alert_m27_code);
        else if (alert instanceof Maintenance28PauseModeTimeout)
            alertCode.setText(R.string.alert_m28_code);
        else if (alert instanceof Maintenance29BatteryTypeNotSet)
            alertCode.setText(R.string.alert_m29_code);
        else if (alert instanceof Maintenance30CartridgeTypeNotSet)
            alertCode.setText(R.string.alert_m30_code);
        else if (alert instanceof Error6MechanicalError)
            alertCode.setText(R.string.alert_e6_code);
        else if (alert instanceof Error10RewindError)
            alertCode.setText(R.string.alert_e10_code);
        else if (alert instanceof Error13LanguageError)
            alertCode.setText(R.string.alert_e13_code);
    }

    private void setAlertTitle() {
        Alert alert = alertMessage.getAlert();
        if (alert instanceof Reminder1DeliverBolus)
            alertTitle.setText(R.string.alert_r1_title);
        else if (alert instanceof Reminder2MissedBolus)
            alertTitle.setText(R.string.alert_r2_title);
        else if (alert instanceof Reminder3AlarmClock)
            alertTitle.setText(R.string.alert_r3_title);
        else if (alert instanceof Reminder4ChangeInfusionSet)
            alertTitle.setText(R.string.alert_r4_title);
        else if (alert instanceof Reminder7TBRCompleted)
            alertTitle.setText(R.string.alert_r7_title);

        else if (alert instanceof Warning31CartridgeLow)
            alertTitle.setText(R.string.alert_w31_title);
        else if (alert instanceof Warning32BatteryLow)
            alertTitle.setText(R.string.alert_w32_title);
        else if (alert instanceof Warning33InvalidDateTime)
            alertTitle.setText(R.string.alert_w33_title);
        else if (alert instanceof Warning34EndOfWarranty)
            alertTitle.setText(R.string.alert_w34_title);
        else if (alert instanceof Warning36TBRCancelled)
            alertTitle.setText(R.string.alert_w36_title);
        else if (alert instanceof Warning38BolusCancelled)
            alertTitle.setText(R.string.alert_w38_title);
        else if (alert instanceof Warning39LoantimeWarning)
            alertTitle.setText(R.string.alert_w39_title);

        else if (alert instanceof Maintenance20CartridgeNotInserted)
            alertTitle.setText(R.string.alert_m20_title);
        else if (alert instanceof Maintenance21CartridgeEmpty)
            alertTitle.setText(R.string.alert_m21_title);
        else if (alert instanceof Maintenance22BatteryEmpty)
            alertTitle.setText(R.string.alert_m22_title);
        else if (alert instanceof Maintenance23AutomaticOff)
            alertTitle.setText(R.string.alert_m23_title);
        else if (alert instanceof Maintenance24Occlusion)
            alertTitle.setText(R.string.alert_m24_title);
        else if (alert instanceof Maintenance25LoantimeOver)
            alertTitle.setText(R.string.alert_m25_title);
        else if (alert instanceof Maintenance26CartridgeChangeNotCompleted)
            alertTitle.setText(R.string.alert_m26_title);
        else if (alert instanceof Maintenance27DataDownloadFailed)
            alertTitle.setText(R.string.alert_m27_title);
        else if (alert instanceof Maintenance28PauseModeTimeout)
            alertTitle.setText(R.string.alert_m28_title);
        else if (alert instanceof Maintenance29BatteryTypeNotSet)
            alertTitle.setText(R.string.alert_m29_title);
        else if (alert instanceof Maintenance30CartridgeTypeNotSet)
            alertTitle.setText(R.string.alert_m30_title);

        else if (alert instanceof Error6MechanicalError)
            alertTitle.setText(R.string.alert_e6_title);
        else if (alert instanceof Error10RewindError)
            alertTitle.setText(R.string.alert_e10_title);
        else if (alert instanceof Error13LanguageError)
            alertTitle.setText(R.string.alert_e13_title);
    }

    private void setAlertDescription() {
        Alert alert = alertMessage.getAlert();
        if (alert instanceof Reminder1DeliverBolus)
            alertDescription.setText(R.string.alert_r1_description);
        else if (alert instanceof Reminder2MissedBolus)
            alertDescription.setText(R.string.alert_r2_description);
        else if (alert instanceof Reminder3AlarmClock)
            alertDescription.setText(R.string.alert_r3_description);
        else if (alert instanceof Reminder4ChangeInfusionSet)
            alertDescription.setText(R.string.alert_r4_description);
        else if (alert instanceof Reminder7TBRCompleted) {
            Reminder7TBRCompleted tbrCompleted = (Reminder7TBRCompleted) alert;
            alertDescription.setText(HTMLUtil.getHTML(R.string.alert_r7_description,
                    tbrCompleted.getAmount(),
                    UnitFormatter.formatDuration(tbrCompleted.getDuration())));
        }


        else if (alert instanceof Warning31CartridgeLow) {
            Warning31CartridgeLow cartridgeLow = (Warning31CartridgeLow) alert;
            alertDescription.setText(HTMLUtil.getHTML(R.string.alert_w31_description, UnitFormatter.formatUnits(cartridgeLow.getCartridgeAmount())));
        }
        else if (alert instanceof Warning32BatteryLow)
            alertDescription.setText(R.string.alert_w32_description);
        else if (alert instanceof Warning33InvalidDateTime)
            alertDescription.setText(R.string.alert_w33_description);
        else if (alert instanceof Warning34EndOfWarranty)
            alertDescription.setText(R.string.alert_w34_description);
        else if (alert instanceof Warning36TBRCancelled) {
            Warning36TBRCancelled tbrCancelled = (Warning36TBRCancelled) alert;
            alertDescription.setText(HTMLUtil.getHTML(R.string.alert_w36_description,
                    tbrCancelled.getAmount(),
                    UnitFormatter.formatDuration(tbrCancelled.getDuration())));
        }
        else if (alert instanceof Warning38BolusCancelled) {
            Warning38BolusCancelled bolusCancelled = (Warning38BolusCancelled) alert;
            alertDescription.setText(HTMLUtil.getHTML(R.string.alert_w38_description,
                    UnitFormatter.formatUnits(bolusCancelled.getProgrammedAmount()),
                    UnitFormatter.formatUnits(bolusCancelled.getDeliveredAmount())));
        }
        else if (alert instanceof Warning39LoantimeWarning)
            alertDescription.setText(R.string.alert_w39_description);

        else if (alert instanceof Maintenance20CartridgeNotInserted)
            alertDescription.setText(R.string.alert_m20_description);
        else if (alert instanceof Maintenance21CartridgeEmpty)
            alertDescription.setText(R.string.alert_m21_description);
        else if (alert instanceof Maintenance22BatteryEmpty)
            alertDescription.setText(R.string.alert_m22_description);
        else if (alert instanceof Maintenance23AutomaticOff)
            alertDescription.setText(R.string.alert_m23_description);
        else if (alert instanceof Maintenance24Occlusion)
            alertDescription.setText(R.string.alert_m24_description);
        else if (alert instanceof Maintenance25LoantimeOver)
            alertDescription.setText(R.string.alert_m25_description);
        else if (alert instanceof Maintenance26CartridgeChangeNotCompleted)
            alertDescription.setText(R.string.alert_m26_description);
        else if (alert instanceof Maintenance27DataDownloadFailed)
            alertDescription.setText(R.string.alert_m27_description);
        else if (alert instanceof Maintenance28PauseModeTimeout)
            alertDescription.setText(R.string.alert_m28_description);
        else if (alert instanceof Maintenance29BatteryTypeNotSet)
            alertDescription.setText(R.string.alert_m29_description);
        else if (alert instanceof Maintenance30CartridgeTypeNotSet)
            alertDescription.setText(R.string.alert_m30_description);

        else if (alert instanceof Error6MechanicalError)
            alertDescription.setText(R.string.alert_e6_description);
        else if (alert instanceof Error10RewindError)
            alertDescription.setText(R.string.alert_e10_description);
        else if (alert instanceof Error13LanguageError)
            alertDescription.setText(R.string.alert_e13_description);
    }

    private int getLayoutFile() {
        switch (alertMessage.getAlertCategory()) {
            case ERROR:
                return R.layout.activity_alert_error;
            case MAINTENANCE:
                return R.layout.activity_alert_maintenance;
            case WARNING:
                return R.layout.activity_alert_warning;
            case REMINDER:
                return R.layout.activity_alert_reminder;
        }
        return 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alertService != null) alertService.setAlertActivity(null);
        unbindService(serviceConnection);
        vibrator.cancel();
        if (ringtone != null) ringtone.stop();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putByteArray("alertMessage", SerializationUtils.serialize(alertMessage));
    }

    @Override
    public void onClick(View v) {
        if (alertService != null) {
            vibrator.vibrate(100);
            if (v == mute) {
                mute.setVisibility(View.GONE);
                alertService.muteAlert();
            } else if (v == dismiss) {
                finish();
                alertService.dismissAlert();
            }
        }
    }
}
