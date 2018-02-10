package sugar.free.sightremote.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.text.SimpleDateFormat;

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
import sugar.free.sightremote.dialogs.ConfirmationDialog;
import sugar.free.sightremote.utils.HTMLUtil;
import sugar.free.sightremote.utils.UnitFormatter;

public class StatusActivity extends SightActivity implements View.OnClickListener, TaskRunner.ResultCallback {

    private ConfirmationDialog confirmationDialog;
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
    private TextView battery;
    private TextView cartridge;
    private TextView activeBasalRate;
    private TextView latestBolus;

    private TextView dailyBolus;
    private TextView dailyBasal;
    private TextView dailyTotal;

    private LinearLayout tbrContainer;
    private TextView tbrText;
    private ProgressBar tbrProgress;
    private ImageButton tbrCancel;

    private LinearLayout bolus1Container;
    private View bolus1Circle;
    private TextView bolus1Title;
    private TextView bolus1Text;
    private ProgressBar bolus1Progress;
    private ImageButton bolus1Cancel;

    private LinearLayout bolus2Container;
    private View bolus2Circle;
    private TextView bolus2Title;
    private TextView bolus2Text;
    private ProgressBar bolus2Progress;
    private ImageButton bolus2Cancel;

    private LinearLayout bolus3Container;
    private View bolus3Circle;
    private TextView bolus3Title;
    private TextView bolus3Text;
    private ProgressBar bolus3Progress;
    private ImageButton bolus3Cancel;

    private MenuItem startPump;
    private MenuItem stopPump;
    private TaskRunner.ResultCallback errorToastResultCallback = new TaskRunner.ResultCallback() {
        @Override
        public void onResult(Object result) {

        }

        @Override
        public void onError(Exception e) {
            runOnUiThread(() -> Toast.makeText(StatusActivity.this, R.string.error, Toast.LENGTH_SHORT).show());
        }
    };
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
                                new SimpleDateFormat(getString(R.string.time_formatter)).format(dbLatestBolus.getDateTime()),
                                UnitFormatter.formatUnits(dbLatestBolus.getImmediateAmount()))));
                    } else {
                        if (dbLatestBolus.getBolusType() == HistoryBolusType.MULTIWAVE)
                            runOnUiThread(() -> latestBolus.setText(HTMLUtil.getHTML(R.string.latest_bolus_multiwave,
                                    new SimpleDateFormat(getString(R.string.time_formatter)).format(dbLatestBolus.getDateTime()),
                                    UnitFormatter.formatUnits(dbLatestBolus.getImmediateAmount()),
                                    UnitFormatter.formatUnits(dbLatestBolus.getExtendedAmount()),
                                    UnitFormatter.formatDuration(dbLatestBolus.getDuration()))));
                        else
                            runOnUiThread(() -> latestBolus.setText(HTMLUtil.getHTML(R.string.latest_bolus_extended,
                                    new SimpleDateFormat(getString(R.string.time_formatter)).format(dbLatestBolus.getDateTime()),
                                    UnitFormatter.formatUnits(dbLatestBolus.getExtendedAmount()),
                                    UnitFormatter.formatDuration(dbLatestBolus.getDuration()))));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_status);

        status = findViewById(R.id.status);
        battery = findViewById(R.id.battery);
        cartridge = findViewById(R.id.cartridge);
        activeBasalRate = findViewById(R.id.active_basal_rate);
        latestBolus = findViewById(R.id.latest_bolus);
        dailyBolus = findViewById(R.id.daily_bolus);
        dailyBasal = findViewById(R.id.daily_basal);
        dailyTotal = findViewById(R.id.daily_total);
        tbrContainer = findViewById(R.id.tbr_container);
        tbrText = findViewById(R.id.tbr_text);
        tbrProgress = findViewById(R.id.tbr_progress);
        tbrCancel = findViewById(R.id.tbr_cancel);
        bolus1Container = findViewById(R.id.bolus1_container);
        bolus1Circle = findViewById(R.id.bolus1_circle);
        bolus1Title = findViewById(R.id.bolus1_title);
        bolus1Text = findViewById(R.id.bolus1_text);
        bolus1Progress = findViewById(R.id.bolus1_progress);
        bolus1Cancel = findViewById(R.id.bolus1_cancel);
        bolus2Container = findViewById(R.id.bolus2_container);
        bolus2Circle = findViewById(R.id.bolus2_circle);
        bolus2Title = findViewById(R.id.bolus2_title);
        bolus2Text = findViewById(R.id.bolus2_text);
        bolus2Progress = findViewById(R.id.bolus2_progress);
        bolus2Cancel = findViewById(R.id.bolus2_cancel);
        bolus3Container = findViewById(R.id.bolus3_container);
        bolus3Circle = findViewById(R.id.bolus3_circle);
        bolus3Title = findViewById(R.id.bolus3_title);
        bolus3Text = findViewById(R.id.bolus3_text);
        bolus3Progress = findViewById(R.id.bolus3_progress);
        bolus3Cancel = findViewById(R.id.bolus3_cancel);

        tbrCancel.setOnClickListener(this);
        bolus1Cancel.setOnClickListener(this);
        bolus2Cancel.setOnClickListener(this);
        bolus3Cancel.setOnClickListener(this);
    }

    private void updateViews() {
        PumpStatus pumpStatus = statusResult.getPumpStatusMessage().getPumpStatus();
        if (pumpStatus == PumpStatus.STARTED) status.setText(R.string.started);
        else if (pumpStatus == PumpStatus.PAUSED) status.setText(R.string.paused);
        else if (pumpStatus == PumpStatus.STOPPED) status.setText(R.string.stopped);
        cartridge.setText(UnitFormatter.formatUnits(statusResult.getCartridgeAmountMessage().getCartridgeAmount()));
        battery.setText(getString(R.string.battery_formatter, statusResult.getBatteryAmountMessage().getBatteryAmount()));
        dailyBolus.setText(UnitFormatter.formatUnits(statusResult.getDailyTotalMessage().getBolusTotal()));
        dailyBasal.setText(UnitFormatter.formatUnits(statusResult.getDailyTotalMessage().getBasalTotal()));
        dailyTotal.setText(UnitFormatter.formatUnits(statusResult.getDailyTotalMessage().getTotal()));
        if (pumpStatus == PumpStatus.STOPPED) {
            activeBasalRate.setText(UnitFormatter.formatBR(0));
            tbrContainer.setVisibility(View.GONE);
            bolus1Container.setVisibility(View.GONE);
            bolus2Container.setVisibility(View.GONE);
            bolus3Container.setVisibility(View.GONE);
            startPump.setVisible(true);
            stopPump.setVisible(false);
        } else {
            startPump.setVisible(false);
            stopPump.setVisible(true);
            int tbrAmount = statusResult.getCurrentTBRMessage().getPercentage();
            int tbrDuration = statusResult.getCurrentTBRMessage().getLeftoverTime();
            int tbrInitialDuration = statusResult.getCurrentTBRMessage().getInitialTime();
            float basalAmount = statusResult.getCurrentBasalMessage().getCurrentBasalAmount();
            String basalName = statusResult.getCurrentBasalMessage().getCurrentBasalName();
            if (tbrAmount == 100) {
                tbrContainer.setVisibility(View.GONE);
                activeBasalRate.setText(HTMLUtil.getHTML(R.string.basal_amount_formatter, basalName, UnitFormatter.formatBR(basalAmount)));
                activeBasalRate.setTypeface(activeBasalRate.getTypeface(), Typeface.NORMAL);
            } else {
                tbrContainer.setVisibility(View.VISIBLE);
                activeBasalRate.setText(HTMLUtil.getHTML(R.string.basal_amount_formatter, basalName, UnitFormatter.formatBR(basalAmount / 100F * ((float) tbrAmount))));
                activeBasalRate.setTypeface(activeBasalRate.getTypeface(), Typeface.ITALIC);
                tbrProgress.getProgressDrawable().setColorFilter(ContextCompat.getColor(this, R.color.colorTBR), PorterDuff.Mode.SRC_IN);
                tbrProgress.setMax(tbrInitialDuration);
                tbrProgress.setProgress(tbrDuration);
                tbrCancel.setVisibility(View.VISIBLE);
                tbrText.setText(getString(R.string.tbr_text, tbrAmount, UnitFormatter.formatDuration(tbrDuration), UnitFormatter.formatDuration(tbrInitialDuration)));
            }
            ActiveBolus bolus1 = statusResult.getActiveBolusesMessage().getBolus1();
            if (bolus1 != null) {
                bolus1Container.setVisibility(View.VISIBLE);
                bolus1Title.setText(getBolusTitle(bolus1.getBolusType()));
                bolus1Text.setText(getBolusText(bolus1));
                bolus1Circle.setBackground(ContextCompat.getDrawable(this, getBolusDrawable(bolus1.getBolusType())));
                bolus1Progress.setMax((int) (bolus1.getInitialAmount() * 100));
                bolus1Progress.setProgress((int) (bolus1.getLeftoverAmount() * 100));
                bolus1Progress.getProgressDrawable().setColorFilter(ContextCompat.getColor(this, getBolusColor(bolus1.getBolusType())), PorterDuff.Mode.SRC_IN);
                bolus1Cancel.setVisibility(View.VISIBLE);
            } else if (bolus1Container.getVisibility() == View.VISIBLE) {
                sendBroadcast(new Intent(HistoryBroadcast.ACTION_START_SYNC));
                bolus1Container.setVisibility(View.GONE);
            }

            ActiveBolus bolus2 = statusResult.getActiveBolusesMessage().getBolus2();
            if (bolus2 != null) {
                bolus2Container.setVisibility(View.VISIBLE);
                bolus2Title.setText(getBolusTitle(bolus2.getBolusType()));
                bolus2Text.setText(getBolusText(bolus2));
                bolus2Circle.setBackground(ContextCompat.getDrawable(this, getBolusDrawable(bolus2.getBolusType())));
                bolus2Progress.setMax((int) (bolus2.getInitialAmount() * 100));
                bolus2Progress.setProgress((int) (bolus2.getLeftoverAmount() * 100));
                bolus2Cancel.setVisibility(View.VISIBLE);
                bolus2Progress.getProgressDrawable().setColorFilter(ContextCompat.getColor(this, getBolusColor(bolus2.getBolusType())), PorterDuff.Mode.SRC_IN);
            } else if (bolus2Container.getVisibility() == View.VISIBLE) {
                sendBroadcast(new Intent(HistoryBroadcast.ACTION_START_SYNC));
                bolus2Container.setVisibility(View.GONE);
            }

            ActiveBolus bolus3 = statusResult.getActiveBolusesMessage().getBolus3();
            if (bolus3 != null) {
                bolus3Container.setVisibility(View.VISIBLE);
                bolus3Title.setText(getBolusTitle(bolus3.getBolusType()));
                bolus3Text.setText(getBolusText(bolus3));
                bolus3Circle.setBackground(ContextCompat.getDrawable(this, getBolusDrawable(bolus3.getBolusType())));
                bolus3Progress.setMax((int) (bolus3.getInitialAmount() * 100));
                bolus3Progress.setProgress((int) (bolus3.getLeftoverAmount() * 100));
                bolus3Progress.getProgressDrawable().setColorFilter(ContextCompat.getColor(this, getBolusColor(bolus3.getBolusType())), PorterDuff.Mode.SRC_IN);
                bolus3Cancel.setVisibility(View.VISIBLE);
            } else if (bolus3Container.getVisibility() == View.VISIBLE) {
                sendBroadcast(new Intent(HistoryBroadcast.ACTION_START_SYNC));
                bolus3Container.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (confirmationDialog != null) confirmationDialog.hide();
    }

    private int getBolusTitle(ActiveBolusType bolusType) {
        if (bolusType == ActiveBolusType.STANDARD) return R.string.standard_bolus;
        else if (bolusType == ActiveBolusType.EXTENDED) return R.string.extended_bolus;
        else if (bolusType == ActiveBolusType.MULTIWAVE) return R.string.multiwave_bolus;
        return 0;
    }

    private String getBolusText(ActiveBolus activeBolus) {
        if (activeBolus.getBolusType() == ActiveBolusType.STANDARD)
            return getString(R.string.normal_bolus_text, activeBolus.getLeftoverAmount(), activeBolus.getInitialAmount());
        else
            return getString(R.string.extended_bolus_text, activeBolus.getLeftoverAmount(), activeBolus.getInitialAmount(), UnitFormatter.formatDuration(activeBolus.getDuration()));
    }

    private int getBolusDrawable(ActiveBolusType bolusType) {
        if (bolusType == ActiveBolusType.STANDARD) return R.drawable.standard_bolus_circle;
        else if (bolusType == ActiveBolusType.EXTENDED) return R.drawable.extended_bolus_circle;
        else if (bolusType == ActiveBolusType.MULTIWAVE) return R.drawable.multiwave_bolus_circle;
        return 0;
    }

    private int getBolusColor(ActiveBolusType bolusType) {
        if (bolusType == ActiveBolusType.STANDARD) return R.color.colorStandard;
        else if (bolusType == ActiveBolusType.EXTENDED) return R.color.colorExtended;
        else if (bolusType == ActiveBolusType.MULTIWAVE) return R.color.colorMultiwave;
        return 0;
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
    protected void statusChanged(Status status) {
        if (status == Status.CONNECTED) {
            taskRunnerRunnable.run();
            sendBroadcast(new Intent(HistoryBroadcast.ACTION_START_SYNC));
        }
    }

    @Override
    protected int getSelectedNavItemID() {
        return R.id.nav_status;
    }

    @Override
    public void onClick(View v) {
        if (StatusActivity.this.taskRunner != null) StatusActivity.this.taskRunner.cancel();
        handler.removeCallbacks(taskRunnerRunnable);
        AppLayerMessage message = null;
        if (v == tbrCancel) {
            tbrCancel.setVisibility(View.GONE);
            message = new CancelTBRMessage();
        } else if (v == bolus1Cancel) {
            bolus1Cancel.setVisibility(View.INVISIBLE);
            CancelBolusMessage cancelBolusMessage = new CancelBolusMessage();
            cancelBolusMessage.setBolusId(statusResult.getActiveBolusesMessage().getBolus1().getBolusID());
            message = cancelBolusMessage;
        } else if (v == bolus2Cancel) {
            bolus2Cancel.setVisibility(View.INVISIBLE);
            CancelBolusMessage cancelBolusMessage = new CancelBolusMessage();
            cancelBolusMessage.setBolusId(statusResult.getActiveBolusesMessage().getBolus2().getBolusID());
            message = cancelBolusMessage;
        } else if (v == bolus3Cancel) {
            bolus3Cancel.setVisibility(View.INVISIBLE);
            CancelBolusMessage cancelBolusMessage = new CancelBolusMessage();
            cancelBolusMessage.setBolusId(statusResult.getActiveBolusesMessage().getBolus3().getBolusID());
            message = cancelBolusMessage;
        }
        SingleMessageTaskRunner taskRunner = new SingleMessageTaskRunner(getServiceConnector(), message);
        taskRunner.fetch(errorToastResultCallback);
        handler.postDelayed(taskRunnerRunnable, 500);
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
    public void onError(Exception e) {
        if (!(e instanceof CancelledException) && !(e instanceof DisconnectedError)) {
            Snackbar snackbar = Snackbar.make(getRootView(), R.string.error, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.retry, view -> taskRunnerRunnable.run());
            showSnackbar(snackbar);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.status_menu, menu);
        startPump = menu.findItem(R.id.status_nav_start);
        stopPump = menu.findItem(R.id.status_nav_stop);
        if (statusResult != null) {
            if (statusResult.getPumpStatusMessage().getPumpStatus() == PumpStatus.STARTED)
                startPump.setVisible(false);
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
                (confirmationDialog = new ConfirmationDialog(this, HTMLUtil.getHTML(R.string.start_pump_confirmation), () -> {
                    startPump.setVisible(false);
                    if (StatusActivity.this.taskRunner != null)
                        StatusActivity.this.taskRunner.cancel();
                    taskRunner.fetch(errorToastResultCallback);
                    handler.removeCallbacks(taskRunnerRunnable);
                    handler.postDelayed(taskRunnerRunnable, 500);
                })).show();
            }
            return true;
        } else if (item == stopPump) {
            if (getServiceConnector().getStatus() == Status.CONNECTED) {
                SetPumpStatusMessage message = new SetPumpStatusMessage();
                message.setPumpStatus(PumpStatus.STOPPED);
                SingleMessageTaskRunner taskRunner = new SingleMessageTaskRunner(getServiceConnector(), message);
                (confirmationDialog = new ConfirmationDialog(this, HTMLUtil.getHTML(R.string.stop_pump_confirmation), () -> {
                    stopPump.setVisible(false);
                    if (StatusActivity.this.taskRunner != null)
                        StatusActivity.this.taskRunner.cancel();
                    taskRunner.fetch(errorToastResultCallback);
                    handler.removeCallbacks(taskRunnerRunnable);
                    handler.postDelayed(taskRunnerRunnable, 500);
                })).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected boolean finishAfterNavigationClick() {
        return false;
    }
}
