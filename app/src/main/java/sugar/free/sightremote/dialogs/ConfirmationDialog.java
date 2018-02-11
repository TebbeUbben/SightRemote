package sugar.free.sightremote.dialogs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sugar.free.sightremote.R;
import sugar.free.sightremote.SightRemote;
import sugar.free.sightremote.utils.HTMLUtil;

import static sugar.free.sightremote.utils.Preferences.PREF_BOOLEAN_CONFIRMATION_USE_FINGERPRINT;
import static sugar.free.sightremote.utils.Preferences.PREF_BOOLEAN_CONFIRMATION_USE_PIN;
import static sugar.free.sightremote.utils.Preferences.PREF_BOOLEAN_ENABLE_CONFIRMATION_CHALLENGES;
import static sugar.free.sightremote.utils.Preferences.PREF_STRING_CONFIRMATION_PIN;
import static sugar.free.sightremote.utils.Preferences.getBooleanPref;
import static sugar.free.sightremote.utils.Preferences.getStringPref;

public class ConfirmationDialog implements TextWatcher, View.OnClickListener {

    private Handler handler;
    private Vibrator vibrator;

    private ConfirmationCallback callback;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    private View spacer;
    private LinearLayout rootView;
    private LinearLayout fingerprintLayout;
    private View fingerprintIcon;
    private TextView fingerprintText;
    private TextView confirmationText;
    private LinearLayout challengeButtons;
    private Button challenge1;
    private Button challenge2;
    private Button challenge3;
    private Button challenge4;
    private TextView pin;

    private List<Button> challengeOrder;

    private CancellationSignal cancellationSignal;

    private ConfirmationDialog(Context context, ConfirmationCallback callback) {
        this.callback = callback;
        builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.confirmation);
        builder.setNegativeButton(R.string.cancel, null);
        vibrator = (Vibrator) SightRemote.getInstance().getSystemService(Context.VIBRATOR_SERVICE);
        handler = new Handler(Looper.getMainLooper());
    }

    public ConfirmationDialog(Context context, String message, ConfirmationCallback callback) {
        this(context, callback);
        builder.setMessage(message);
    }

    public ConfirmationDialog(Context context, Spanned message, ConfirmationCallback callback) {
        this(context, callback);
        builder.setMessage(message);
    }

    @SuppressLint("NewApi")
    public void show() {
        prepareViews();
        dialog = builder.show();
        dialog.setOnDismissListener((dialogInterface -> onClose()));
        if (useFingerprint()) getFingerprintManager().authenticate(null,
                cancellationSignal = new CancellationSignal(), 0, new FingerprintManager.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                        onConfirm();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        fingerprintIcon.getBackground().setColorFilter(ContextCompat.getColor(SightRemote.getInstance(), R.color.colorWrong), PorterDuff.Mode.MULTIPLY);
                        fingerprintText.setText(R.string.couldnt_recognize_fingerprint);
                        fingerprintText.setTextColor(ContextCompat.getColor(SightRemote.getInstance(), R.color.colorWrong));
                    }
                }, null);
        if (getBooleanPref(PREF_BOOLEAN_ENABLE_CONFIRMATION_CHALLENGES)) pin.requestFocus();
    }

    private void onClose() {
        if (cancellationSignal != null) cancellationSignal.cancel();
        handler.removeCallbacks(cancelRunnable);
    }

    private void onConfirm() {
        dialog.hide();
        callback.onDialogConfirmed();
    }

    public void hide() {
        dialog.hide();
        onClose();
    }

    private void loadViews() {
        rootView = (LinearLayout) LayoutInflater.from(SightRemote.getInstance()).inflate(R.layout.dialog_confirmation, null);
        spacer = rootView.findViewById(R.id.spacer);
        fingerprintLayout = rootView.findViewById(R.id.fingerprint_layout);
        fingerprintIcon = rootView.findViewById(R.id.fingerprint_icon);
        fingerprintText = rootView.findViewById(R.id.fingerprint_text);
        confirmationText = rootView.findViewById(R.id.confirmation_text);
        challengeButtons = rootView.findViewById(R.id.challenge_buttons);
        challenge1 = rootView.findViewById(R.id.challenge_1);
        challenge2 = rootView.findViewById(R.id.challenge_2);
        challenge3 = rootView.findViewById(R.id.challenge_3);
        challenge4 = rootView.findViewById(R.id.challenge_4);
        challenge1.setOnClickListener(this);
        challenge2.setOnClickListener(this);
        challenge3.setOnClickListener(this);
        challenge4.setOnClickListener(this);
        pin = rootView.findViewById(R.id.pin);
    }

    private void prepareViews() {
        loadViews();
        if (!getBooleanPref(PREF_BOOLEAN_ENABLE_CONFIRMATION_CHALLENGES)) {
            builder.setView(null);
            builder.setPositiveButton(R.string.okay, (dialog, which) -> onConfirm());
            return;
        }
        builder.setPositiveButton(null, null);
        builder.setView(rootView);

        boolean disableButtons = false;
        boolean useFingerprint = useFingerprint();
        if (useFingerprint) {
            fingerprintLayout.setVisibility(View.VISIBLE);
            fingerprintIcon.getBackground().setColorFilter(ContextCompat.getColor(SightRemote.getInstance(), R.color.colorNeutral), PorterDuff.Mode.MULTIPLY);
            disableButtons = true;
        }
        if (getBooleanPref(PREF_BOOLEAN_CONFIRMATION_USE_PIN)) {
            disableButtons = true;
            confirmationText.setVisibility(View.VISIBLE);
            confirmationText.setText(useFingerprint ? R.string.or_enter_your_pin : R.string.enter_your_pin);
            pin.setVisibility(View.VISIBLE);
            pin.addTextChangedListener(this);
            if (useFingerprint) spacer.setVisibility(View.VISIBLE);
        }
        if (disableButtons) {
            challengeButtons.setVisibility(View.GONE);
        } else {
            confirmationText.setVisibility(View.VISIBLE);
            confirmationText.setText(generateChallenge());
            for (Button button : challengeOrder)
                button.getBackground().setColorFilter(ContextCompat.getColor(SightRemote.getInstance(), R.color.colorNeutral), PorterDuff.Mode.MULTIPLY);
        }
    }

    private Spanned generateChallenge() {
        challengeOrder = new ArrayList<>();
        challengeOrder.add(challenge1);
        challengeOrder.add(challenge2);
        challengeOrder.add(challenge3);
        challengeOrder.add(challenge4);
        Collections.shuffle(challengeOrder);

        int button1 = getButtonNumber(challengeOrder.get(0));
        int button2 = getButtonNumber(challengeOrder.get(1));
        int button3 = getButtonNumber(challengeOrder.get(2));
        int button4 = getButtonNumber(challengeOrder.get(3));

        return HTMLUtil.getHTML(useFingerprint() ? R.string.or_press_the_buttons_in_the_following_order : R.string.press_the_buttons_in_the_following_order,
                button1, button2, button3, button4);
    }

    private int getButtonNumber(Button button) {
        if (button == challenge1) return 1;
        else if (button == challenge2) return 2;
        else if (button == challenge3) return 3;
        else if (button == challenge4) return 4;
        return 0;
    }

    public static boolean useFingerprint() {
        if (!getBooleanPref(PREF_BOOLEAN_CONFIRMATION_USE_FINGERPRINT)) return false;
        if (!isFingerprintScannerAvailable()) return false;
        return true;
    }

    @SuppressLint("NewApi")
    public static boolean isFingerprintScannerAvailable() {
        if (ContextCompat.checkSelfPermission(SightRemote.getInstance(), Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED)
            return false;
        FingerprintManager fingerprintManager = getFingerprintManager();
        if (fingerprintManager == null) return false;
        if (!fingerprintManager.isHardwareDetected()) return false;
        if (!fingerprintManager.hasEnrolledFingerprints()) return false;
        return true;
    }

    private static FingerprintManager getFingerprintManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return (FingerprintManager) SightRemote.getInstance().getSystemService(Context.FINGERPRINT_SERVICE);
        else return null;
    }

    @Override
    public void onClick(View view) {
        if (view == challengeOrder.get(0)) {
            vibrator.vibrate(50L);
            challengeOrder.remove(0);
            view.getBackground().setColorFilter(ContextCompat.getColor(SightRemote.getInstance(), R.color.colorRight), PorterDuff.Mode.MULTIPLY);
            for (Button button : challengeOrder)
                button.getBackground().setColorFilter(ContextCompat.getColor(SightRemote.getInstance(), R.color.colorNeutral), PorterDuff.Mode.MULTIPLY);
            if (challengeOrder.size() == 0) onConfirm();
            handler.removeCallbacks(cancelRunnable);
            handler.postDelayed(cancelRunnable, 5000L);
        } else {
            vibrator.vibrate(500L);
            handler.removeCallbacks(cancelRunnable);
            confirmationText.setText(generateChallenge());
            for (Button button : challengeOrder)
                button.getBackground().setColorFilter(ContextCompat.getColor(SightRemote.getInstance(), R.color.colorNeutral), PorterDuff.Mode.MULTIPLY);
            view.getBackground().setColorFilter(ContextCompat.getColor(SightRemote.getInstance(), R.color.colorWrong), PorterDuff.Mode.MULTIPLY);
        }
    }

    private Runnable cancelRunnable = new Runnable() {
        @Override
        public void run() {
            vibrator.vibrate(500L);
            confirmationText.setText(generateChallenge());
            for (Button button : challengeOrder)
                button.getBackground().setColorFilter(ContextCompat.getColor(SightRemote.getInstance(), R.color.colorNeutral), PorterDuff.Mode.MULTIPLY);
        }
    };

    public interface ConfirmationCallback {
        void onDialogConfirmed();
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

}
