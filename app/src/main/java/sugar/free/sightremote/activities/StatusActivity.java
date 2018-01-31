package sugar.free.sightremote.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.text.DateFormat;

import sugar.free.sightparser.SerializationUtils;
import sugar.free.sightparser.applayer.descriptors.ActiveBolus;
import sugar.free.sightparser.applayer.descriptors.ActiveBolusType;
import sugar.free.sightparser.applayer.descriptors.HistoryBolusType;
import sugar.free.sightparser.applayer.descriptors.PumpStatus;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.messages.remote_control.CancelBolusMessage;
import sugar.free.sightparser.applayer.messages.remote_control.CancelTBRMessage;
import sugar.free.sightparser.applayer.messages.remote_control.SetPumpStatusMessage;
import sugar.free.sightparser.error.CancelledException;
import sugar.free.sightparser.error.DisconnectedError;
import sugar.free.sightparser.handling.HistoryBroadcast;
import sugar.free.sightparser.handling.SingleMessageTaskRunner;
import sugar.free.sightparser.handling.TaskRunner;
import sugar.free.sightparser.handling.taskrunners.StatusTaskRunner;
import sugar.free.sightparser.pipeline.Status;
import sugar.free.sightremote.R;
import sugar.free.sightremote.database.BolusDelivered;
import sugar.free.sightremote.utils.ActivationWarningDialogChain;
import sugar.free.sightremote.utils.HTMLUtil;
import sugar.free.sightremote.utils.UnitFormatter;

public class StatusActivity extends SightActivity implements TaskRunner.ResultCallback, View.OnClickListener {

    private TaskRunner taskRunner;
    private StatusTaskRunner.StatusResult statusResult;
    private Handler handler = new Handler();
    private Runnable taskRunnerRunnable = new Runnable() {
        @Override
        public void run() {
            taskRunner = new StatusTaskRunner(getServiceConnector());
            taskRunner.fetch(StatusActivity.this);
        }
    };

    private TextView status;
    private TextView cartridge;
    private TextView battery;
    private TextView basalAmount;
    private TextView latestBolus;
    private CardView temporaryBasalrate;
    private TextView tbrText;
    private ProgressBar tbrProgress2;
    private CardView bolus1;
    private TextView bolus1Title;
    private TextView bolus1Text;
    private ProgressBar bolus1Progress;
    private CardView bolus2;
    private TextView bolus2Title;
    private TextView bolus2Text;
    private ProgressBar bolus2Progress;
    private CardView bolus3;
    private TextView bolus3Title;
    private TextView bolus3Text;
    private ProgressBar bolus3Progress;
    private TextView noActiveProcesses;
    private MenuItem startPump;
    private MenuItem stopPump;
    private MenuItem backgroundSync;
    private Button cancelTBR;
    private Button cancelBolus1;
    private Button cancelBolus2;
    private Button cancelBolus3;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        status = findViewById(R.id.status);
        cartridge = findViewById(R.id.catridge);
        battery = findViewById(R.id.battery);
        basalAmount = findViewById(R.id.basal_amount);
        latestBolus = findViewById(R.id.latest_bolus);
        temporaryBasalrate = findViewById(R.id.tempory_basalrate);
        tbrText = findViewById(R.id.tbr_text);
        tbrProgress2 = findViewById(R.id.tbr_progress2);
        bolus1 = findViewById(R.id.bolus1);
        bolus1Title = findViewById(R.id.bolus1_title);
        bolus1Text = findViewById(R.id.bolus1_text);
        bolus1Progress = findViewById(R.id.bolus1_progress);
        bolus2 = findViewById(R.id.bolus2);
        bolus2Title = findViewById(R.id.bolus2_title);
        bolus2Text = findViewById(R.id.bolus2_text);
        bolus2Progress = findViewById(R.id.bolus2_progress);
        bolus3 = findViewById(R.id.bolus3);
        bolus3Title = findViewById(R.id.bolus3_title);
        bolus3Text = findViewById(R.id.bolus3_text);
        bolus3Progress = findViewById(R.id.bolus3_progress);
        noActiveProcesses = findViewById(R.id.no_active_processes);
        cancelTBR = findViewById(R.id.cancelTBR);
        cancelBolus1 = findViewById(R.id.cancelBolus1);
        cancelBolus2 = findViewById(R.id.cancelBolus2);
        cancelBolus3 = findViewById(R.id.cancelBolus3);

        cancelTBR.setOnClickListener(this);
        cancelBolus1.setOnClickListener(this);
        cancelBolus2.setOnClickListener(this);
        cancelBolus3.setOnClickListener(this);

        if (savedInstanceState != null && savedInstanceState.containsKey("StatusResult")) {
            statusResult = (StatusTaskRunner.StatusResult) SerializationUtils.deserialize(savedInstanceState.getByteArray("StatusResult"));
            updateViews();
        }
    }

    @Override
    protected int getRootLayout() {
        return R.layout.activity_status;
    }

    @Override
    protected void setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar));
    }

    @Override
    protected View getRootView() {
        return findViewById(R.id.root);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (statusResult != null) outState.putByteArray("StatusResult", SerializationUtils.serialize(statusResult));
    }

    private void updateViews() {
        PumpStatus pumpStatus = statusResult.getPumpStatusMessage().getPumpStatus();
        if (pumpStatus == PumpStatus.STARTED) status.setText(R.string.started);
        else if (pumpStatus == PumpStatus.PAUSED) status.setText(R.string.paused);
        else if (pumpStatus == PumpStatus.STOPPED) status.setText(R.string.stopped);
        cartridge.setText(getString(R.string.cartridge_formatter, statusResult.getCartridgeAmountMessage().getCartridgeAmount()));
        battery.setText(getString(R.string.battery_formatter, statusResult.getBatteryAmountMessage().getBatteryAmount()));
        if (pumpStatus == PumpStatus.STOPPED) {
            basalAmount.setVisibility(View.INVISIBLE);
            temporaryBasalrate.setVisibility(View.GONE);
            bolus1.setVisibility(View.GONE);
            bolus2.setVisibility(View.GONE);
            bolus3.setVisibility(View.GONE);
            noActiveProcesses.setVisibility(View.VISIBLE);
            noActiveProcesses.setText(R.string.no_active_processes);
        } else {
            int activeProcesses = 0;
            if (statusResult.getCurrentTBRMessage().getPercentage() == 100) {
                basalAmount.setVisibility(View.VISIBLE);
                basalAmount.setTypeface(basalAmount.getTypeface(), Typeface.NORMAL);
                basalAmount.setText(HTMLUtil.getHTML(R.string.basal_amount_formatter, statusResult.getCurrentBasalMessage().getCurrentBasalName(),
                        UnitFormatter.formatBR(statusResult.getCurrentBasalMessage().getCurrentBasalAmount())));
                temporaryBasalrate.setVisibility(View.GONE);
            } else {
                activeProcesses++;
                cancelTBR.setVisibility(View.VISIBLE);
                temporaryBasalrate.setVisibility(View.VISIBLE);
                basalAmount.setVisibility(View.VISIBLE);
                basalAmount.setText(HTMLUtil.getHTML(R.string.basal_amount_formatter, statusResult.getCurrentBasalMessage().getCurrentBasalName(),
                        UnitFormatter.formatBR(statusResult.getCurrentBasalMessage().getCurrentBasalAmount() / 100F * ((float) statusResult.getCurrentTBRMessage().getPercentage()))));
                basalAmount.setTypeface(basalAmount.getTypeface(), Typeface.ITALIC);
                int progress = (int) (100F / ((float) statusResult.getCurrentTBRMessage().getInitialTime()) * ((float) statusResult.getCurrentTBRMessage().getLeftoverTime()));
                tbrProgress2.setProgress(progress);
                tbrText.setText(getString(R.string.tbr_text, statusResult.getCurrentTBRMessage().getPercentage(), formatTime(statusResult.getCurrentTBRMessage().getLeftoverTime()), formatTime(statusResult.getCurrentTBRMessage().getInitialTime())));
            }
            ActiveBolus bolus1Data = statusResult.getActiveBolusesMessage().getBolus1();
            ActiveBolus bolus2Data = statusResult.getActiveBolusesMessage().getBolus2();
            ActiveBolus bolus3Data = statusResult.getActiveBolusesMessage().getBolus3();

            if (bolus1Data != null) {
                activeProcesses++;
                cancelBolus1.setVisibility(View.VISIBLE);
                bolus1.setVisibility(View.VISIBLE);
                bolus1Title.setText(getBolusTitle(bolus1Data.getBolusType()));
                bolus1Text.setText(getBolusText(bolus1Data));
                bolus1Progress.setProgress((int) (100F / bolus1Data.getInitialAmount() * bolus1Data.getLeftoverAmount()));
            } else if (bolus1.getVisibility() != View.GONE) {
                sendBroadcast(new Intent(HistoryBroadcast.ACTION_START_SYNC));
                bolus1.setVisibility(View.GONE);
            }

            if (bolus2Data != null) {
                activeProcesses++;
                cancelBolus2.setVisibility(View.VISIBLE);
                bolus2.setVisibility(View.VISIBLE);
                bolus2Title.setText(getBolusTitle(bolus2Data.getBolusType()));
                bolus2Text.setText(getBolusText(bolus2Data));
                bolus2Progress.setProgress((int) (100F / bolus2Data.getInitialAmount() * bolus2Data.getLeftoverAmount()));
            } else if (bolus2.getVisibility() != View.GONE) {
                sendBroadcast(new Intent(HistoryBroadcast.ACTION_START_SYNC));
                bolus2.setVisibility(View.GONE);
            }

            if (bolus3Data != null) {
                activeProcesses++;
                cancelBolus3.setVisibility(View.VISIBLE);
                bolus3.setVisibility(View.VISIBLE);
                bolus3Title.setText(getBolusTitle(bolus3Data.getBolusType()));
                bolus3Text.setText(getBolusText(bolus3Data));
                bolus3Progress.setProgress((int) (100F / bolus3Data.getInitialAmount() * bolus3Data.getLeftoverAmount()));
            } else if (bolus3.getVisibility() != View.GONE) {
                sendBroadcast(new Intent(HistoryBroadcast.ACTION_START_SYNC));
                bolus3.setVisibility(View.GONE);
            }

            noActiveProcesses.setVisibility(activeProcesses == 0 ? View.VISIBLE : View.GONE);
            noActiveProcesses.setText(R.string.no_active_processes);
        }
        if (startPump != null && stopPump != null) {
            if (pumpStatus == PumpStatus.STARTED) {
                stopPump.setVisible(true);
                startPump.setVisible(false);
            } else {
                stopPump.setVisible(false);
                startPump.setVisible(true);
            }
        }
    }

    private int getBolusTitle(ActiveBolusType bolusType) {
        if (bolusType == ActiveBolusType.STANDARD) return R.string.standard_bolus;
        else if (bolusType == ActiveBolusType.EXTENDED) return R.string.extended_bolus;
        else if (bolusType == ActiveBolusType.MULTIWAVE) return R.string.multiwave_bolus;
        return 0;
    }

    private String getBolusText(ActiveBolus activeBolus) {
        if (activeBolus.getBolusType() == ActiveBolusType.STANDARD) return getString(R.string.normal_bolus_text, activeBolus.getLeftoverAmount(), activeBolus.getInitialAmount());
        else return getString(R.string.extended_bolus_text, activeBolus.getLeftoverAmount(), activeBolus.getInitialAmount(), formatTime(activeBolus.getDuration()));
    }

    private String formatTime(int minutes) {
        int dMinutes = minutes % 60;
        int dHours = (minutes - dMinutes) / 60;
        return getString(R.string.duration_formatter, dHours, dMinutes);
    }

    @Override
    protected void onStop() {
        if (taskRunner != null) taskRunner.cancel();
        handler.removeCallbacks(taskRunnerRunnable);
        unregisterReceiver(historyBroadcastReceiver);
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(historyBroadcastReceiver, new IntentFilter(HistoryBroadcast.ACTION_SYNC_FINISHED));
    }

    @Override
    protected void connectedToService() {
        getServiceConnector().connect();
        statusChanged(getServiceConnector().getStatus());
    }

    @Override
    public void onResult(Object result) {
        if (result != null) {
            dismissSnackbar();
            statusResult = (StatusTaskRunner.StatusResult) result;
            runOnUiThread(() -> updateViews());
        }
        handler.postDelayed(taskRunnerRunnable, 1500);
    }

    @Override
    protected void statusChanged(Status status) {
        if (status == Status.CONNECTED) {
            taskRunnerRunnable.run();
            sendBroadcast(new Intent(HistoryBroadcast.ACTION_START_SYNC));
        }
    }

    @Override
    public void onError(Exception e) {
        if (!(e instanceof CancelledException) && !(e instanceof DisconnectedError)) {
            Snackbar snackbar = Snackbar.make(getRootView(), R.string.error, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.retry, view -> taskRunnerRunnable.run());
            showSnackbar(snackbar);
        }
    }

    @Override
    protected int getSelectedNavItemID() {
        return R.id.nav_status;
    }

    @Override
    protected boolean finishAfterNavigationClick() {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.status_menu, menu);
        startPump = menu.findItem(R.id.status_nav_start);
        stopPump = menu.findItem(R.id.status_nav_stop);
        backgroundSync = menu.findItem(R.id.status_nav_background_sync);
        if (statusResult != null) {
            if (statusResult.getPumpStatusMessage().getPumpStatus() == PumpStatus.STARTED) startPump.setVisible(false);
            else stopPump.setVisible(false);
        } else {
            startPump.setVisible(false);
            stopPump.setVisible(false);
        }
        updateBackgroundSyncMenu();
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == startPump) {
            if (getServiceConnector().getStatus() == Status.CONNECTED) {
                SetPumpStatusMessage message = new SetPumpStatusMessage();
                message.setPumpStatus(PumpStatus.STARTED);
                SingleMessageTaskRunner taskRunner = new SingleMessageTaskRunner(getServiceConnector(), message);
                new AlertDialog.Builder(this)
                        .setMessage(HTMLUtil.getHTML(R.string.start_pump_confirmation))
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            startPump.setVisible(false);
                            if (StatusActivity.this.taskRunner != null) StatusActivity.this.taskRunner.cancel();
                            taskRunner.fetch(errorToastResultCallback);
                            handler.removeCallbacks(taskRunnerRunnable);
                            handler.postDelayed(taskRunnerRunnable, 500);
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
            return true;
        } else if (item == stopPump) {
            if (getServiceConnector().getStatus() == Status.CONNECTED) {
                SetPumpStatusMessage message = new SetPumpStatusMessage();
                message.setPumpStatus(PumpStatus.STOPPED);
                SingleMessageTaskRunner taskRunner = new SingleMessageTaskRunner(getServiceConnector(), message);
                new AlertDialog.Builder(this)
                        .setMessage(HTMLUtil.getHTML(R.string.stop_pump_confirmation))
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            stopPump.setVisible(false);
                            if (StatusActivity.this.taskRunner != null) StatusActivity.this.taskRunner.cancel();
                            taskRunner.fetch(errorToastResultCallback);
                            handler.removeCallbacks(taskRunnerRunnable);
                            handler.postDelayed(taskRunnerRunnable, 500);
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
            return true;
        } else if (item.getItemId() == R.id.status_nav_delete_pairing) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.confirmation)
                    .setMessage(HTMLUtil.getHTML(R.string.delete_pairing_confirmation))
                    .setPositiveButton(R.string.yes, (dialog, which) -> {;
                        getServiceConnector().reset();
                        Intent intent = new Intent(this, SetupActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else if (item.getItemId() == R.id.status_nav_enter_password) {
            new ActivationWarningDialogChain(this, getServiceConnector()).doActivationWarning();
        } else if (item.getItemId() == R.id.status_nav_choose_alarm_tone) {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
            String selectedTone = getPreferences().getString("alert_alarm_tone", null);
            Uri defaultTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedTone == null ? defaultTone : Uri.parse(selectedTone));
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
            startActivityForResult(intent, 42);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 42) {
            getPreferences().edit().putString("alert_alarm_tone", data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI).toString()).apply();
        }
    }

    private TaskRunner.ResultCallback errorToastResultCallback = new TaskRunner.ResultCallback() {
        @Override
        public void onResult(Object result) {

        }

        @Override
        public void onError(Exception e) {
            runOnUiThread(() -> Toast.makeText(StatusActivity.this, R.string.error, Toast.LENGTH_SHORT).show());
        }
    };

    @Override
    public void onClick(View v) {
        if (StatusActivity.this.taskRunner != null) StatusActivity.this.taskRunner.cancel();
        handler.removeCallbacks(taskRunnerRunnable);
        AppLayerMessage message = null;
        if (v == cancelTBR) {
            cancelTBR.setVisibility(View.GONE);
            message = new CancelTBRMessage();
        } else if (v == cancelBolus1) {
            cancelBolus1.setVisibility(View.GONE);
            CancelBolusMessage cancelBolusMessage = new CancelBolusMessage();
            cancelBolusMessage.setBolusId(statusResult.getActiveBolusesMessage().getBolus1().getBolusID());
            message = cancelBolusMessage;
        } else if (v == cancelBolus2) {
            cancelBolus2.setVisibility(View.GONE);
            CancelBolusMessage cancelBolusMessage = new CancelBolusMessage();
            cancelBolusMessage.setBolusId(statusResult.getActiveBolusesMessage().getBolus2().getBolusID());
            message = cancelBolusMessage;
        } else if (v == cancelBolus3) {
            cancelBolus3.setVisibility(View.GONE);
            CancelBolusMessage cancelBolusMessage = new CancelBolusMessage();
            cancelBolusMessage.setBolusId(statusResult.getActiveBolusesMessage().getBolus3().getBolusID());
            message = cancelBolusMessage;
        }
        SingleMessageTaskRunner taskRunner = new SingleMessageTaskRunner(getServiceConnector(), message);
        taskRunner.fetch(errorToastResultCallback);
        handler.postDelayed(taskRunnerRunnable, 500);
    }

    private BroadcastReceiver historyBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                BolusDelivered dbLatestBolus = getDatabaseHelper().getBolusDeliveredDao().queryBuilder().orderBy("dateTime", false).queryForFirst();
                if (dbLatestBolus == null) return;
                if (System.currentTimeMillis() - dbLatestBolus.getDateTime().getTime() >= 24 * 60 * 60 * 1000)
                    runOnUiThread(() -> latestBolus.setVisibility(View.GONE));
                else {
                    if (dbLatestBolus.getBolusType() == HistoryBolusType.STANDARD) {
                        runOnUiThread(() -> latestBolus.setText(HTMLUtil.getHTML(R.string.latest_bolus_standard,
                                DateFormat.getTimeInstance(DateFormat.SHORT).format(dbLatestBolus.getDateTime()),
                                UnitFormatter.formatUnits(dbLatestBolus.getImmediateAmount()))));
                    } else {
                        if (dbLatestBolus.getBolusType() == HistoryBolusType.MULTIWAVE)
                            runOnUiThread(() -> latestBolus.setText(HTMLUtil.getHTML(R.string.latest_bolus_multiwave,
                                    DateFormat.getTimeInstance(DateFormat.SHORT).format(dbLatestBolus.getDateTime()),
                                    UnitFormatter.formatUnits(dbLatestBolus.getImmediateAmount()),
                                    UnitFormatter.formatUnits(dbLatestBolus.getExtendedAmount()),
                                    UnitFormatter.formatDuration(dbLatestBolus.getDuration()))));
                        else runOnUiThread(() -> latestBolus.setText(HTMLUtil.getHTML(R.string.latest_bolus_extended,
                                DateFormat.getTimeInstance(DateFormat.SHORT).format(dbLatestBolus.getDateTime()),
                                UnitFormatter.formatUnits(dbLatestBolus.getExtendedAmount()),
                                UnitFormatter.formatDuration(dbLatestBolus.getDuration()))));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    };

    public void backgroundSyncCheck(MenuItem item) {
        getPreferences().edit().putBoolean("background_sync_enabled", !isBackGroundSyncEnabled()).apply();
        updateBackgroundSyncMenu();
        sendBroadcast(new Intent(HistoryBroadcast.ACTION_START_SYNC));
    }

    private boolean isBackGroundSyncEnabled() {
        return getPreferences().getBoolean("background_sync_enabled",false);
    }

    private void updateBackgroundSyncMenu() {
        backgroundSync.setChecked(isBackGroundSyncEnabled());
    }
}
