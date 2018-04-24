package sugar.free.sightremote.activities.boluses;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import sugar.free.sightparser.applayer.messages.remote_control.StandardBolusMessage;
import sugar.free.sightparser.handling.SingleMessageTaskRunner;
import sugar.free.sightparser.handling.TaskRunner;
import sugar.free.sightremote.taskrunners.BolusPreparationTaskRunner;
import sugar.free.sightparser.pipeline.Status;
import sugar.free.sightremote.R;
import sugar.free.sightremote.activities.SightActivity;
import sugar.free.sightremote.utils.BolusAmountPicker;
import sugar.free.sightremote.dialogs.ConfirmationDialog;
import sugar.free.sightremote.utils.CrashlyticsUtil;
import sugar.free.sightremote.utils.HTMLUtil;
import sugar.free.sightremote.utils.UnitFormatter;

public class StandardBolusActivity extends SightActivity implements TaskRunner.ResultCallback, View.OnClickListener, BolusAmountPicker.OnAmountChangeListener {

    private ConfirmationDialog confirmationDialog;
    private BolusAmountPicker bolusAmountPicker;

    private BolusPreparationTaskRunner.PreperationResult preperationResult;

    private NumberPicker digit1;
    private NumberPicker digit2;
    private NumberPicker digit3;
    private NumberPicker digit4;
    private Button deliver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_standard_bolus);

        showManualOverlay();

        digit1 = findViewById(R.id.digit1);
        digit2 = findViewById(R.id.digit2);
        digit3 = findViewById(R.id.digit3);
        digit4 = findViewById(R.id.digit4);

        bolusAmountPicker = new BolusAmountPicker(digit1, digit2, digit3, digit4);
        bolusAmountPicker.setOnAmountChangeListener(this);

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
                if (preperationResult.getAvailableBoluses().isStandardAvailable()) {
                    hideLoadingIndicator();
                    hideManualOverlay();
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
                            .putCustomAttribute("Bolus Type", "Standard"));
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
        runOnUiThread(() -> Toast.makeText(this, getString(R.string.error, e.getClass().getSimpleName()), Toast.LENGTH_SHORT).show());
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
        StandardBolusMessage message = new StandardBolusMessage();
        message.setAmount(bolusAmountPicker.getPickerValue());
        final SingleMessageTaskRunner taskRunner = new SingleMessageTaskRunner(getServiceConnector(), message);
        (confirmationDialog = new ConfirmationDialog(this,
                HTMLUtil.getHTML(R.string.standard_bolus_confirmation, UnitFormatter.formatUnits(bolusAmountPicker.getPickerValue())), () -> {
            showLoadingIndicator();
            taskRunner.fetch(StandardBolusActivity.this);
        })).show();
    }

    @Override
    protected int getSelectedNavItemID() {
        return R.id.nav_standard_bolus;
    }

    @Override
    public void onAmountChange(BolusAmountPicker bolusAmountPicker, double newValue) {
        deliver.setEnabled(newValue >= preperationResult.getMinBolusAmount());
    }
}