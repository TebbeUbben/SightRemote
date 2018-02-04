package sugar.free.sightremote.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import sugar.free.sightparser.handling.FirewallConstraint;
import sugar.free.sightremote.R;
import sugar.free.sightremote.adapters.PrefsViewImpl;
import sugar.free.sightremote.databinding.ActivityFirewallBinding;

/**
 * Created by jamorham on 30/01/2018.
 *
 * Activity to manage firewall preferences
 *
 * Layout and wiring is in activity_firewall.xml
 */

public class FirewallActivity extends SightActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firewall);

        // to initialize defaults
        FirewallConstraint fw = new FirewallConstraint(getApplicationContext());
        fw = null;

        final ActivityFirewallBinding binding = ActivityFirewallBinding.inflate(getLayoutInflater());
        binding.setPrefs(new PrefsViewImpl(getApplicationContext(), "ACTIVITY_FIREWALL", getServiceConnector()));
        setContentView(binding.getRoot());
    }

    @Override
    protected void connectedToService() {
        getServiceConnector().connect();
    }

    @Override
    protected boolean useNavigationDrawer() {
        return false;
    }
}

