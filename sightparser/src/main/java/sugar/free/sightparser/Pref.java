package sugar.free.sightparser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.util.StringTokenizer;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by jamorham on 01/01/2018.
 *
 * Simplified, access to default preferences store and helper functionality
 *
 * Easier to use in static mode with an application based context, but we don't have that here.
 *
 */

public class Pref {
    public static final String CHANGE_PREFS_SPECIAL_CASE = "CHANGE_PREFS";
    public static final String CHANGE_PREFS_SPECIAL_CASE_DELIMITER = "^";

    private static final String TAG = "Pref";
    private static SharedPreferences prefs;
    private static Pref instance;

    private Pref(Context context, String prefix) {
        reloadPrefs(context, prefix);
    }

    @NonNull
    public static Pref get(Context context, String prefix) {
        if (instance == null) {
            instance = new Pref(context, prefix);
        }
        return instance;
    }

    public static Pref get() {
        return instance;
    }

    public void reloadPrefs(Context context, String prefix) {
        prefs = context.getSharedPreferences(prefix, MODE_PRIVATE);
    }

    // strings
    public String getStringDefaultBlank(final String pref) {
        return prefs.getString(pref, "");
    }

    public String getString(final String pref, final String def) {
        return prefs.getString(pref, def);
    }

    public boolean setString(final String pref, final String str) {
        prefs.edit().putString(pref, str).apply();
        return true;
    }

    // numbers
    public long getLong(final String pref, final long def) {
        return prefs.getLong(pref, def);
    }

    public boolean setLong(final String pref, final long lng) {
        prefs.edit().putLong(pref, lng).apply();
        return true;
    }

    public int getInt(final String pref, final int def) {
        return prefs.getInt(pref, def);
    }

    public int getStringToInt(final String pref, final int defaultValue) {
        try {
            return Integer.parseInt(getString(pref, Integer.toString(defaultValue)));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public boolean setInt(final String pref, final int num) {
        prefs.edit().putInt(pref, num).apply();
        return true;
    }

    // misc
    public boolean removeItem(final String pref) {
        prefs.edit().remove(pref).apply();
        return true;
    }

    public boolean isSet(final String pref) {
        return prefs.contains(pref);
    }

    // booleans
    public boolean getBooleanDefaultFalse(final String pref) {
        return prefs.getBoolean(pref, false);
    }

    public boolean getBoolean(final String pref, boolean def) {
        return prefs.getBoolean(pref, def);
    }

    public boolean setBoolean(final String pref, final boolean lng) {
        prefs.edit().putBoolean(pref, lng).apply();
        return true;
    }

    public void toggleBoolean(final String pref) {
        prefs.edit().putBoolean(pref, !prefs.getBoolean(pref, false)).apply();
    }

    public void parsePrefs(final String tag, final String msg) {
        if (msg.startsWith(CHANGE_PREFS_SPECIAL_CASE)) {
            final StringTokenizer st = new StringTokenizer(msg, CHANGE_PREFS_SPECIAL_CASE_DELIMITER);
            if (st.countTokens() == 3) {
                st.nextToken(); // skip prefix
                final String name = st.nextToken();
                final String value = st.nextToken();
                android.util.Log.d(tag, "Setting: " + name + " -> " + value);
                if ((name != null) && (name.length() > 0)) {
                    switch (value) {
                        case "true":
                            setBoolean(name, true);
                            break;
                        case "false":
                            setBoolean(name, false);
                            break;
                        default:
                            setString(name, value);
                            break;
                    }
                }

            } else {
                android.util.Log.d(tag, "Invalid number of prefs tokens: " + st.countTokens());
            }
        }
    }

    @SuppressLint("ApplySharedPref")
    public void commit() {
        prefs.edit().commit();
    }

}
