package sugar.free.sightremote.dialogs;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import sugar.free.sightremote.R;
import sugar.free.sightremote.SightRemote;

import static sugar.free.sightremote.utils.Preferences.PREF_STRING_CONFIRMATION_PIN;
import static sugar.free.sightremote.utils.Preferences.getStringPref;
import static sugar.free.sightremote.utils.Preferences.setStringPref;

public class ChangePINDialog implements TextWatcher {

    private LinearLayout rootLayout;
    private LinearLayout oldPinContainer;
    private EditText oldPin;
    private EditText newPin;
    private EditText confirmPin;
    private Button okayButton;

    private Context context;
    private AlertDialog dialog;
    private PINChangedCallback pinChangedCallback;

    public ChangePINDialog(Context context, PINChangedCallback pinChangedCallback) {
        this.context = context;
        this.pinChangedCallback = pinChangedCallback;
    }

    public AlertDialog show() {
        rootLayout = (LinearLayout) LayoutInflater.from(SightRemote.getInstance()).inflate(R.layout.dialog_change_pin, null);
        oldPinContainer = rootLayout.findViewById(R.id.old_pin_container);
        oldPin = rootLayout.findViewById(R.id.old_pin);
        newPin = rootLayout.findViewById(R.id.new_pin);
        confirmPin = rootLayout.findViewById(R.id.confirm_pin);
        if (getStringPref(PREF_STRING_CONFIRMATION_PIN) == null) oldPinContainer.setVisibility(View.GONE);
        oldPin.addTextChangedListener(this);
        newPin.addTextChangedListener(this);
        confirmPin.addTextChangedListener(this);
        dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.change_pin)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.okay, (dialog, which) -> onConfirm())
                .setView(rootLayout)
                .show();
        okayButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        okayButton.setEnabled(false);
        return dialog;
    }

    private void onConfirm() {
        setStringPref(PREF_STRING_CONFIRMATION_PIN, newPin.getText().toString());
        if (pinChangedCallback != null) pinChangedCallback.onPINChanged();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        String oldPin2 = getStringPref(PREF_STRING_CONFIRMATION_PIN);
        if (oldPin2 != null && !oldPin2.equals(oldPin.getText().toString())) {
            oldPin.setError(SightRemote.getInstance().getString(R.string.wrong_pin));
            okayButton.setEnabled(false);
        } else if (newPin.getText().toString().length() < 4) {
            oldPin.setError(null);
            newPin.setError(SightRemote.getInstance().getString(R.string.pin_must_have_a_least_four_digits));
            okayButton.setEnabled(false);
        } else if (!newPin.getText().toString().equals(confirmPin.getText().toString())) {
            oldPin.setError(null);
            newPin.setError(null);
            confirmPin.setError(SightRemote.getInstance().getString(R.string.no_match));
            okayButton.setEnabled(false);
        } else {
            oldPin.setError(null);
            newPin.setError(null);
            confirmPin.setError(null);
            okayButton.setEnabled(true);
        }
    }

    public interface PINChangedCallback {
        void onPINChanged();
    }
}
