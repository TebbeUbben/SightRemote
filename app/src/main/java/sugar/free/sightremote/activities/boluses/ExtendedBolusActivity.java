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

import sugar.free.sightparser.applayer.messages.remote_control.ExtendedBolusMessage;
import sugar.free.sightparser.handling.SingleMessageTaskRunner;
import sugar.free.sightparser.handling.TaskRunner;
import sugar.free.sightparser.handling.taskrunners.BolusPreparationTaskRunner;
import sugar.free.sightparser.pipeline.Status;
import sugar.free.sightremote.R;
import sugar.free.sightremote.activities.SightActivity;
import sugar.free.sightremote.utils.BolusAmountPicker;
import sugar.free.sightremote.utils.DurationPicker;
import sugar.free.sightremote.utils.HTMLUtil;
import sugar.free.sightremote.utils.UnitFormatter;

public class ExtendedBolusActivity extends SightActivity implements TaskRunner.ResultCallback, View.OnClickListener, BolusAmountPicker.OnAmountChangeListener, DurationPicker.OnDurationChangeListener {

    private BolusAmountPicker bolusAmountPicker;
    private DurationPicker durationPicker;

    private BolusPreparationTaskRunner.PreperationResult preperationResult;

    private NumberPicker digit1;
    private NumberPicker digit2;
    private NumberPicker digit3;
    private NumberPicker digit4;
    private NumberPicker digit5;
    private NumberPicker digit6;
    private Button deliver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_extended_bolus);
        disconnectedFromService();

        showManualOverlay();

        digit1 = findViewById(R.id.digit1);
        digit2 = findViewById(R.id.digit2);
        digit3 = findViewById(R.id.digit3);
        digit4 = findViewById(R.id.digit4);
        digit5 = findViewById(R.id.digit5);
        digit6 = findViewById(R.id.digit6);

        bolusAmountPicker = new BolusAmountPicker(digit1, digit2, digit3, digit4);
        bolusAmountPicker.setOnAmountChangeListener(this);

        durationPicker = new DurationPicker(digit5, digit6);
        durationPicker.setOnDurationChangeListener(this);

        deliver = findViewById(R.id.deliver);
        deliver.setOnClickListener(this);
        deliver.setEnabled(false);
    }

    @Override
    public void onResult(Object result) {
        if (result instanceof BolusPreparationTaskRunner.PreperationResult) {
            preperationResult = (BolusPreparationTaskRunner.PreperationResult) result;
            runOnUiThread(() -> bolusAmountPicker.adjustNumberPickers(preperationResult.getMaxBolusAmount()));
            if (preperationResult.isPumpStarted()) {
                if (preperationResult.getAvailableBoluses().isExtendedAvailable()) {
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
        ExtendedBolusMessage message = new ExtendedBolusMessage();
        message.setAmount(bolusAmountPicker.getPickerValue());
        message.setDuration((short) durationPicker.getPickerValue());
        SingleMessageTaskRunner taskRunner = new SingleMessageTaskRunner(getServiceConnector(), message);
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirmation)
                .setMessage(HTMLUtil.getHTML(R.string.extended_bolus_confirmation,
                        UnitFormatter.formatUnits(bolusAmountPicker.getPickerValue()),
                        UnitFormatter.formatDuration(durationPicker.getPickerValue())))
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    showManualOverlay();
                    taskRunner.fetch(ExtendedBolusActivity.this);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    protected int getSelectedNavItemID() {
        return R.id.nav_extended_bolus;
    }

    @Override
    public void onAmountChange(BolusAmountPicker bolusAmountPicker, float newValue) {
        deliver.setEnabled(newValue >= preperationResult.getMinBolusAmount() && durationPicker.getPickerValue() > 0);
    }

    @Override
    public void onDurationChange(int newValue) {
        deliver.setEnabled(bolusAmountPicker.getPickerValue() >= preperationResult.getMinBolusAmount() && newValue > 0);
    }
}