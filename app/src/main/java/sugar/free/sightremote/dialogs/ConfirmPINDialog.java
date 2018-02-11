package sugar.free.sightremote.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

import sugar.free.sightremote.R;
import sugar.free.sightremote.SightRemote;

import static sugar.free.sightremote.utils.Preferences.PREF_STRING_CONFIRMATION_PIN;
import static sugar.free.sightremote.utils.Preferences.getStringPref;

public class ConfirmPINDialog implements TextWatcher {

    private LinearLayout rootLayout;
    private EditText pin;

    private Vibrator vibrator;
    private Context context;
    private ConfirmationCallback callback;
    private AlertDialog dialog;

    public ConfirmPINDialog(Context context, ConfirmationCallback callback) {
        this.context = context;
        this.callback = callback;
        vibrator = (Vibrator) SightRemote.getInstance().getSystemService(Context.VIBRATOR_SERVICE);
    }

    public AlertDialog show() {
        rootLayout = (LinearLayout) LayoutInflater.from(SightRemote.getInstance()).inflate(R.layout.dialog_confirm_pin, null);
        pin = rootLayout.findViewById(R.id.pin);
        pin.addTextChangedListener(this);
        dialog = new AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancel, null)
                .setView(rootLayout)
                .show();
        return dialog;
    }

    private void onConfirm() {
        dialog.hide();
        callback.onDialogConfirmed();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        String enteredPin = editable.toString();
        String realPin = getStringPref(PREF_STRING_CONFIRMATION_PIN);
        if (enteredPin.equals(realPin)) onConfirm();
        else if (enteredPin.length() >= realPin.length()) {
            vibrator.vibrate(500L);
            pin.setText("");
            pin.setError(SightRemote.getInstance().getString(R.string.wrong_pin));
        }
    }

    public interface ConfirmationCallback {
        void onDialogConfirmed();
    }
}
