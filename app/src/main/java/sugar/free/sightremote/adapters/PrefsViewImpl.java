package sugar.free.sightremote.adapters;

import android.content.Context;
import android.databinding.BaseObservable;

import sugar.free.sightparser.Pref;
import sugar.free.sightparser.handling.SightServiceConnector;

import static sugar.free.sightparser.Pref.CHANGE_PREFS_SPECIAL_CASE;
import static sugar.free.sightparser.Pref.CHANGE_PREFS_SPECIAL_CASE_DELIMITER;

/**
 * Created by jamorham on 05/10/2017.
 *
 * Implementation of PrefsView
 */

public class PrefsViewImpl extends BaseObservable implements PrefsView {


    private final Pref pref;
    private final SightServiceConnector connector;

    public PrefsViewImpl(Context context, String prefix, SightServiceConnector connector) {
        this.pref = Pref.get(context, prefix);
        this.connector = connector;
    }

    public boolean getbool(String name) {
        return pref.getBooleanDefaultFalse(name);
    }

    public void setbool(String name, boolean value) {
        pref.setBoolean(name, value);
        notifyChange();

        // send to the service process
        if (connector != null) {
            connector.setAuthorized(CHANGE_PREFS_SPECIAL_CASE + CHANGE_PREFS_SPECIAL_CASE_DELIMITER
                    + name + CHANGE_PREFS_SPECIAL_CASE_DELIMITER + value, false);
        }
    }

    public void togglebool(String name) {
        setbool(name, !getbool(name));
    }
}
