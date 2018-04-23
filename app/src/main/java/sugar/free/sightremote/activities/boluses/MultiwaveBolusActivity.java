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

import sugar.free.sightparser.applayer.messages.remote_control.MultiwaveBolusMessage;
import sugar.free.sightparser.handling.SingleMessageTaskRunner;
import sugar.free.sightparser.handling.TaskRunner;
import sugar.free.sightremote.taskrunners.BolusPreparationTaskRunner;
import sugar.free.sightparser.pipeline.Status;
import sugar.free.sightremote.R;
import sugar.free.sightremote.activities.SightActivity;
import sugar.free.sightremote.utils.BolusAmountPicker;
import sugar.free.sightremote.dialogs.ConfirmationDialog;
import sugar.free.sightremote.utils.DurationPicker;
import sugar.free.sightremote.utils.CrashlyticsUtil;
import sugar.free.sightremote.utils.HTMLUtil;
import sugar.free.sightremote.utils.UnitFormatter;

public class MultiwaveBolusActivity extends SightActivity implements TaskRunner.ResultCallback, View.OnClickListener, BolusAmountPicker.OnAmountChangeListener, DurationPicker.OnDurationChangeListener {

    private ConfirmationDialog confirmationDialog;
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
                            .putCustomAttribute("Bolus Type", "Multiwave"));
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (confirmationDialog != null) confirmationDialog.hide();
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
    public void onClick(View view) {
        MultiwaveBolusMessage message = new MultiwaveBolusMessage();
        message.setAmount(immediateBolusAmountPicker.getPickerValue());
        message.setDelayedAmount(delayedBolusAmountPicker.getPickerValue());
        message.setDuration(durationPicker.getPickerValue());
        SingleMessageTaskRunner taskRunner = new SingleMessageTaskRunner(getServiceConnector(), message);
        (confirmationDialog = new ConfirmationDialog(this, HTMLUtil.getHTML(R.string.multiwave_bolus_confirmation,
                UnitFormatter.formatUnits(immediateBolusAmountPicker.getPickerValue()),
                UnitFormatter.formatUnits(delayedBolusAmountPicker.getPickerValue()),
                UnitFormatter.formatDuration(durationPicker.getPickerValue())), () -> {
            showLoadingIndicator();
            taskRunner.fetch(MultiwaveBolusActivity.this);
        })).show();
    }

    @Override
    protected int getSelectedNavItemID() {
        return R.id.nav_multiwave_bolus;
    }

    @Override
    public void onAmountChange(BolusAmountPicker bolusAmountPicker, double newValue) {
        double total = immediateBolusAmountPicker.getPickerValue() + delayedBolusAmountPicker.getPickerValue();
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