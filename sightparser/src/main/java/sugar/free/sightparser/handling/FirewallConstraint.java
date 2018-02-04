package sugar.free.sightparser.handling;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import sugar.free.sightparser.Pref;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.messages.remote_control.ChangeTBRMessage;
import sugar.free.sightparser.applayer.messages.remote_control.ExtendedBolusMessage;
import sugar.free.sightparser.applayer.messages.remote_control.SetTBRMessage;
import sugar.free.sightparser.applayer.messages.remote_control.StandardBolusMessage;


/**
 * Created by jamorham on 01/02/2018.
 *
 * Allow or deny Applayer messages based on a mapped preference boolean
 *
 * Undefined items are allowed by default
 *
 */


public class FirewallConstraint {

    private static final String FIREWALL_STORAGE = "SERVICE_FIREWALL";
    private static final String TAG = "INSIGHTFIREWALL";
    private static final Map<Class, String> lookup;
    private static final boolean d = false;

    static {
        lookup = new HashMap<>();
        // these message types will be restricted by the named preference item
        lookup.put(StandardBolusMessage.class, "firewall_allow_standard_bolus");
        lookup.put(ExtendedBolusMessage.class, "firewall_allow_extended_bolus");
        lookup.put(ChangeTBRMessage.class, "firewall_allow_temporary_basal");
        lookup.put(SetTBRMessage.class, "firewall_allow_temporary_basal");
    }

    private final Pref pref;

    public FirewallConstraint(Context context) {
        pref = Pref.get(context, FIREWALL_STORAGE);
        initializeDefaults();
    }

    private void initializeDefaults() {
        // set unset class types to allow by default
        for (Map.Entry<Class, String> item : lookup.entrySet()) {
            if (!pref.isSet(item.getValue())) {
                pref.setBoolean(item.getValue(), true);
            }
        }
    }

    void parsePreference(String packageName) {
        android.util.Log.d(TAG, "Updating preferences");
        pref.parsePrefs(TAG, packageName);
    }

    boolean isAllowed(AppLayerMessage msg) {
        if (msg == null) return true; // not sure if this should be false as its invalid
        final String prefString = lookup.get(msg.getClass());
        if (prefString == null) {
            if (d) android.util.Log.e(TAG, "FIREWALL default Allow: " + msg);
            return true; // default allow
        }
        final boolean allow = pref.getBooleanDefaultFalse(prefString);
        if (!allow) {
            android.util.Log.e(TAG, "FIREWALL Blocked: " + msg);
        } else {
            android.util.Log.e(TAG, "FIREWALL Allow: " + msg);
        }
        return allow;
    }

}
