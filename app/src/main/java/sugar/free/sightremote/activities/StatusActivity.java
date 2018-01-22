package sugar.free.sightremote.activities;

import android.app.AlertDialog;
import android.graphics.Typeface;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import sugar.free.sightparser.SerializationUtils;
import sugar.free.sightparser.applayer.AppLayerMessage;
import sugar.free.sightparser.applayer.remote_control.CancelBolusMessage;
import sugar.free.sightparser.applayer.remote_control.CancelTBRMessage;
import sugar.free.sightparser.applayer.remote_control.SetPumpStatusMessage;
import sugar.free.sightparser.applayer.status.ActiveBolus;
import sugar.free.sightparser.applayer.status.BolusType;
import sugar.free.sightparser.applayer.status.PumpStatus;
import sugar.free.sightparser.error.CancelledException;
import sugar.free.sightparser.error.DisconnectedError;
import sugar.free.sightparser.handling.SingleMessageTaskRunner;
import sugar.free.sightparser.handling.TaskRunner;
import sugar.free.sightparser.pipeline.Status;
import sugar.free.sightremote.R;
import sugar.free.sightparser.handling.taskrunners.StatusTaskRunner;

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
    private TextView tbrDuration;
    private ProgressBar tbrProgress;
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
        tbrDuration = findViewById(R.id.tbr_duration);
        tbrProgress = findViewById(R.id.tbr_progress);
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
            basalAmount.setVisibility(View.GONE);
            tbrDuration.setVisibility(View.GONE);
            tbrProgress.setVisibility(View.INVISIBLE);
            temporaryBasalrate.setVisibility(View.GONE);
            bolus1.setVisibility(View.GONE);
            bolus2.setVisibility(View.GONE);
            bolus3.setVisibility(View.GONE);
            noActiveProcesses.setVisibility(View.VISIBLE);
            noActiveProcesses.setText(R.string.no_active_processes);
        } else {
            int activeProcesses = 0;
            if (statusResult.getCurrentTBRMessage().getPercentage() == 100) {
                tbrDuration.setVisibility(View.GONE);
                tbrProgress.setVisibility(View.INVISIBLE);
                basalAmount.setVisibility(View.VISIBLE);
                basalAmount.setTypeface(basalAmount.getTypeface(), Typeface.NORMAL);
                basalAmount.setText(getString(R.string.basal_amount_formatter, statusResult.getCurrentBasalMessage().getCurrentBasalName(), statusResult.getCurrentBasalMessage().getCurrentBasalAmount()));
                temporaryBasalrate.setVisibility(View.GONE);
            } else {
                activeProcesses++;
                cancelTBR.setVisibility(View.VISIBLE);
                tbrDuration.setVisibility(View.VISIBLE);
                tbrProgress.setVisibility(View.VISIBLE);
                temporaryBasalrate.setVisibility(View.VISIBLE);
                basalAmount.setVisibility(View.VISIBLE);
                basalAmount.setText(getString(R.string.basal_amount_formatter, statusResult.getCurrentBasalMessage().getCurrentBasalName(), statusResult.getCurrentBasalMessage().getCurrentBasalAmount() / 100F * ((float) statusResult.getCurrentTBRMessage().getPercentage())));
                basalAmount.setTypeface(basalAmount.getTypeface(), Typeface.ITALIC);
                tbrDuration.setText(getString(R.string.tbr_duration_formatter, formatTime(statusResult.getCurrentTBRMessage().getLeftoverTime()), statusResult.getCurrentTBRMessage().getPercentage()));
                int progress = (int) (100F / ((float) statusResult.getCurrentTBRMessage().getInitialTime()) * ((float) statusResult.getCurrentTBRMessage().getLeftoverTime()));
                tbrProgress.setProgress(progress);
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
            } else bolus1.setVisibility(View.GONE);

            if (bolus2Data != null) {
                activeProcesses++;
                cancelBolus2.setVisibility(View.VISIBLE);
                bolus2.setVisibility(View.VISIBLE);
                bolus2Title.setText(getBolusTitle(bolus2Data.getBolusType()));
                bolus2Text.setText(getBolusText(bolus2Data));
                bolus2Progress.setProgress((int) (100F / bolus2Data.getInitialAmount() * bolus2Data.getLeftoverAmount()));
            } else bolus2.setVisibility(View.GONE);

            if (bolus3Data != null) {
                activeProcesses++;
                cancelBolus3.setVisibility(View.VISIBLE);
                bolus3.setVisibility(View.VISIBLE);
                bolus3Title.setText(getBolusTitle(bolus3Data.getBolusType()));
                bolus3Text.setText(getBolusText(bolus3Data));
                bolus3Progress.setProgress((int) (100F / bolus3Data.getInitialAmount() * bolus3Data.getLeftoverAmount()));
            } else bolus3.setVisibility(View.GONE);

            noActiveProcesses.setVisibility(activeProcesses == 0 ? View.VISIBLE : View.GONE);
            noActiveProcesses.setText(R.string.no_active_processes);
        }
        if (startPump != null) {
            if (pumpStatus == PumpStatus.STARTED) {
                stopPump.setVisible(true);
                startPump.setVisible(false);
            } else {
                stopPump.setVisible(false);
                startPump.setVisible(true);
            }
        }
    }

    private int getBolusTitle(BolusType bolusType) {
        if (bolusType == BolusType.INSTANT) return R.string.standard_bolus;
        else if (bolusType == BolusType.EXTENDED) return R.string.extended_bolus;
        else if (bolusType == BolusType.MULTIWAVE) return R.string.multiwave_bolus;
        return 0;
    }

    private String getBolusText(ActiveBolus activeBolus) {
        if (activeBolus.getBolusType() == BolusType.INSTANT) return getString(R.string.normal_bolus_text, activeBolus.getLeftoverAmount(), activeBolus.getInitialAmount());
        else return getString(R.string.extended_bolus_text, activeBolus.getLeftoverAmount(), activeBolus.getInitialAmount(), formatTime(activeBolus.getDuration()));
    }

    private String formatTime(int minutes) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.time_formatter));
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return simpleDateFormat.format(new Date(minutes * 60000));
    }

    @Override
    protected void onPause() {
        if (taskRunner != null) taskRunner.cancel();
        handler.removeCallbacks(taskRunnerRunnable);
        super.onPause();
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
        if (status == Status.CONNECTED) taskRunnerRunnable.run();
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
        if (statusResult != null) {
            if (statusResult.getPumpStatusMessage().getPumpStatus() == PumpStatus.STARTED) startPump.setVisible(false);
            else stopPump.setVisible(false);
        } else {
            startPump.setVisible(false);
            stopPump.setVisible(false);
        }
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
                        .setMessage(getString(R.string.start_pump_confirmation))
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
                        .setMessage(getString(R.string.stop_pump_confirmation))
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
        }
        return super.onOptionsItemSelected(item);
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
}
