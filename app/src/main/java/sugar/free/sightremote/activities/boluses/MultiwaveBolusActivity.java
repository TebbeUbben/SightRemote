package sugar.free.sightremote.activities.boluses;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import java.text.DecimalFormat;

import sugar.free.sightparser.applayer.remote_control.MultiwaveBolusMessage;
import sugar.free.sightparser.handling.SingleMessageTaskRunner;
import sugar.free.sightparser.handling.TaskRunner;
import sugar.free.sightparser.handling.taskrunners.BolusPreparationTaskRunner;
import sugar.free.sightparser.pipeline.Status;
import sugar.free.sightremote.R;
import sugar.free.sightremote.activities.SightActivity;
import sugar.free.sightremote.utils.BolusAmountPicker;
import sugar.free.sightremote.utils.DurationPicker;

public class MultiwaveBolusActivity extends SightActivity implements TaskRunner.ResultCallback, View.OnClickListener, BolusAmountPicker.OnAmountChangeListener, DurationPicker.OnDurationChangeListener {

    private BolusAmountPicker immediateBolusAmountPicker;
    private BolusAmountPicker delayedBolusAmountPicker;
    private DurationPicker durationPicker;

    private BolusPreparationTaskRunner.PreperationResult preperationResult;

    private NumberPicker digit1;
    private NumberPicker digit2;
    private NumberPicker digit3;
    private NumberPicker digit4;
    private NumberPicker digit5;
    private NumberPicker digit6;
    private NumberPicker digit7;
    private NumberPicker digit8;
    private NumberPicker digit9;
    private NumberPicker digit10;
    private Button deliver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_multiwave_bolus);
        disconnectedFromService();

        showManualOverlay();

        digit1 = findViewById(R.id.digit1);
        digit2 = findViewById(R.id.digit2);
        digit3 = findViewById(R.id.digit3);
        digit4 = findViewById(R.id.digit4);
        digit5 = findViewById(R.id.digit5);
        digit6 = findViewById(R.id.digit6);
        digit7 = findViewById(R.id.digit7);
        digit8 = findViewById(R.id.digit8);
        digit9 = findViewById(R.id.digit9);
        digit10 = findViewById(R.id.digit10);

        immediateBolusAmountPicker = new BolusAmountPicker(digit1, digit2, digit3, digit4);
        immediateBolusAmountPicker.setOnAmountChangeListener(this);

        delayedBolusAmountPicker = new BolusAmountPicker(digit5, digit6, digit7, digit8);
        delayedBolusAmountPicker.setOnAmountChangeListener(this);

        durationPicker = new DurationPicker(digit9, digit10);
        durationPicker.setOnDurationChangeListener(this);

        deliver = findViewById(R.id.deliver);
        deliver.setOnClickListener(this);
        deliver.setEnabled(false);
    }

    @Override
    public void onResult(Object result) {
        if (result instanceof BolusPreparationTaskRunner.PreperationResult) {
            preperationResult = (BolusPreparationTaskRunner.PreperationResult) result;
            runOnUiThread(() -> {
                immediateBolusAmountPicker.adjustNumberPickers(preperationResult.getMaxBolusAmount());
                delayedBolusAmountPicker.adjustNumberPickers(preperationResult.getMaxBolusAmount());
            });
            if (preperationResult.isPumpStarted()) {
                if (preperationResult.getAvailableBoluses().isMultiwaveAvailable()) {
                    hideManualOverlay();
                    dismissSnackbar();
                } else {
                    showManualOverlay();
                    showSnackbar(Snackbar.make(getRootView(), R.string.bolus_type_not_available, Snackbar.LENGTH_INDEFINITE));
                }
            } else {
                showManualOverlay();
                showSnackbar(Snackbar.make(getRootView(), R.string.pump_not_started, Snackbar.LENGTH_INDEFINITE));
            }
        } else finish();
    }


    @Override
    protected void connectedToService() {
        getServiceConnector().connect();
        statusChanged(getServiceConnector().getStatus());
    }

    @Override
    public void onError(Exception e) {
        runOnUiThread(() -> Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void statusChanged(Status status) {
        if (status == Status.CONNECTED) {
            BolusPreparationTaskRunner taskRunner = new BolusPreparationTaskRunner(getServiceConnector());
            taskRunner.fetch(this);
        } else {
            showManualOverlay();
        }
    }

    @Override
    public void onClick(View view) {
        MultiwaveBolusMessage message = new MultiwaveBolusMessage();
        message.setAmount(immediateBolusAmountPicker.getPickerValue());
        message.setDelayedAmount(delayedBolusAmountPicker.getPickerValue());
        message.setDuration((short) durationPicker.getPickerValue());
        SingleMessageTaskRunner taskRunner = new SingleMessageTaskRunner(getServiceConnector(), message);
        DecimalFormat decimalFormat = new DecimalFormat("0");
        decimalFormat.setMinimumFractionDigits(1);
        decimalFormat.setMaximumFractionDigits(2);
        int minutes = durationPicker.getPickerValue() % 60;
        int hours = (durationPicker.getPickerValue() - minutes) / 60;
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirmation)
                .setMessage(getString(R.string.multiwave_bolus_confirmation, decimalFormat.format(immediateBolusAmountPicker.getPickerValue()), decimalFormat.format(delayedBolusAmountPicker.getPickerValue()), hours, minutes))
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    showManualOverlay();
                    taskRunner.fetch(MultiwaveBolusActivity.this);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    protected int getSelectedNavItemID() {
        return R.id.nav_multiwave_bolus;
    }

    @Override
    public void onAmountChange(BolusAmountPicker bolusAmountPicker, float newValue) {
        float total = immediateBolusAmountPicker.getPickerValue() + delayedBolusAmountPicker.getPickerValue();
        if (total > preperationResult.getMaxBolusAmount()) {
            if (bolusAmountPicker == immediateBolusAmountPicker)
                delayedBolusAmountPicker.setValue(delayedBolusAmountPicker.getPickerValue() - (total - preperationResult.getMaxBolusAmount()));
            else
                immediateBolusAmountPicker.setValue(immediateBolusAmountPicker.getPickerValue() - (total - preperationResult.getMaxBolusAmount()));
        }
        deliver.setEnabled(immediateBolusAmountPicker.getPickerValue() >= preperationResult.getMinBolusAmount() && delayedBolusAmountPicker.getPickerValue() >= preperationResult.getMinBolusAmount() && durationPicker.getPickerValue() > 0);
    }

    @Override
    public void onDurationChange(int newValue) {
        deliver.setEnabled(immediateBolusAmountPicker.getPickerValue() >= preperationResult.getMinBolusAmount() && delayedBolusAmountPicker.getPickerValue() >= preperationResult.getMinBolusAmount() && newValue > 0);
    }
}