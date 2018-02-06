package sugar.free.sightremote.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import sugar.free.sightremote.R;
import sugar.free.sightremote.utils.ActivationWarningDialogChain;
import sugar.free.sightremote.utils.HTMLUtil;

public class SettingsActivity extends SightActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_settings);
        if (savedInstanceState == null)
            getFragmentManager().beginTransaction().replace(R.id.frame_layout, new SettingsFragment()).commit();
    }

    @Override
    protected void connectedToService() {
        getServiceConnector().connect();
    }

    @Override
    protected boolean snackbarEnabled() {
        return false;
    }

    @Override
    protected boolean useOverlay() {
        return false;
    }

    public static class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            getPreferenceScreen().setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (preference.getKey().equals("enter_password")) new ActivationWarningDialogChain(getSettingsActivity(), getSettingsActivity().getServiceConnector()).doActivationWarning();
            else if (preference.getKey().equals("delete_pairing")) {
                new AlertDialog.Builder(getSettingsActivity())
                        .setTitle(R.string.confirmation)
                        .setMessage(HTMLUtil.getHTML(R.string.delete_pairing_confirmation))
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            getSettingsActivity().getServiceConnector().reset();
                            Intent intent = new Intent(getSettingsActivity(), SetupActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            getSettingsActivity().finish();
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
            return false;
        }

        private SettingsActivity getSettingsActivity() {
            return (SettingsActivity) getActivity();
        }
    }
}
