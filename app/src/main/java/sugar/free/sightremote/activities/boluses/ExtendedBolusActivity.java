package sugar.free.sightremote.activities.boluses;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import sugar.free.sightparser.applayer.messages.remote_control.ExtendedBolusMessage;
import sugar.free.sightparser.handling.SingleMessageTaskRunner;
import sugar.free.sightparser.handling.TaskRunner;
import sugar.free.sightparser.handling.taskrunners.BolusPreparationTaskRunner;
import sugar.free.sightparser.pipeline.Status;
import sugar.free.sightremote.R;
import sugar.free.sightremote.activities.SightActivity;
import sugar.free.sightremote.utils.BolusAmountPicker;
import sugar.free.sightremote.dialogs.ConfirmationDialog;
import sugar.free.sightremote.utils.DurationPicker;
import sugar.free.sightremote.utils.CrashlyticsUtil;
import sugar.free.sightremote.utils.HTMLUtil;
import sugar.free.sightremote.utils.UnitFormatter;

public class ExtendedBolusActivity extends SightActivity implements TaskRunner.ResultCallback, View.OnClickListener, BolusAmountPicker.OnAmountChangeListener, DurationPicker.OnDurationChangeListener {

    private ConfirmationDialog confirmationDialog;
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
                    hideLoadingIndicator();
                    dismissSnackbar();
                } else {
                    showManualOverlay();
                    hideLoadingIndicator();
                    showSnackbar(Snackbar.make(getRootView(), R.string.bolus_type_not_available, Snackbar.LENGTH_INDEFINITE));
                }
            } else {
                showManualOverlay();
                hideLoadingIndicator();
                showSnackbar(Snackbar.make(getRootView(), R.string.pump_not_started, Snackbar.LENGTH_INDEFINITE));
            }
        } else {
            Answers.getInstance().logCustom(
                    new CustomEvent("Bolus Programmed")
                            .putCustomAttribute("Bolus Type", "Extended"));
            finish();
        }
    }


    @Override
    protected void connectedToService() {
        getServiceConnector().connect();
        statusChanged(getServiceConnector().getStatus());
    }

    @Override
    public void onError(Exception e) {
        runOnUiThread(() -> Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show());
        CrashlyticsUtil.logExceptionWithCallStackTrace(e);
    }

    @Override
    protected void statusChanged(Status status) {
        if (status == Status.CONNECTED) {
            showLoadingIndicator();
            hideManualOverlay();
            BolusPreparationTaskRunner taskRunner = new BolusPreparationTaskRunner(getServiceConnector());
            taskRunner.fetch(this);
        } else {
            if (confirmationDialog != null) confirmationDialog.hide();
            showManualOverlay();
            hideLoadingIndicator();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (confirmationDialog != null) confirmationDialog.hide();
    }

    @Override
    public void onClick(View view) {
        ExtendedBolusMessage message = new ExtendedBolusMessage();
        message.setAmount(bolusAmountPicker.getPickerValue());
        message.setDuration(durationPicker.getPickerValue());
        SingleMessageTaskRunner taskRunner = new SingleMessageTaskRunner(getServiceConnector(), message);
        (confirmationDialog = new ConfirmationDialog(this, HTMLUtil.getHTML(R.string.extended_bolus_confirmation,
                UnitFormatter.formatUnits(bolusAmountPicker.getPickerValue()),
                UnitFormatter.formatDuration(durationPicker.getPickerValue())), () -> {
            showLoadingIndicator();
            taskRunner.fetch(ExtendedBolusActivity.this);
        })).show();
    }

    @Override
    protected int getSelectedNavItemID() {
        return R.id.nav_extended_bolus;
    }

    @Override
    public void onAmountChange(BolusAmountPicker bolusAmountPicker, double newValue) {
        deliver.setEnabled(newValue >= preperationResult.getMinBolusAmount() && durationPicker.getPickerValue() > 0);
    }

    @Override
    public void onDurationChange(int newValue) {
        deliver.setEnabled(bolusAmountPicker.getPickerValue() >= preperationResult.getMinBolusAmount() && newValue > 0);
    }
}