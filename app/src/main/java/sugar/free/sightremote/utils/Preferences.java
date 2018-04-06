package sugar.free.sightremote.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import sugar.free.sightremote.SightRemote;

public class Preferences {

    public static String PREF_STRING_ALERT_ALARM_TONE = "alert_alarm_tone";
    public static String PREF_STRING_CONFIRMATION_PIN = "confirmation_pin";

    public static String PREF_BOOLEAN_BACKGROUND_SYNC_ENABLED = "background_sync_enabled";
    public static String PREF_BOOLEAN_ENABLE_CONFIRMATION_CHALLENGES = "enable_confirmation_challenges";
    public static String PREF_BOOLEAN_CONFIRMATION_USE_FINGERPRINT = "confirmation_use_fingerprint";
    public static String PREF_BOOLEAN_CONFIRMATION_USE_PIN = "confirmation_use_pin";
    public static String PREF_BOOLEAN_AUTO_ADJUST_TIME = "auto_adjust_time";

    private static SharedPreferences sharedPreferences;

    public static boolean getBooleanPref(String key) {
        return getPreferences().getBoolean(key, false);
    }

    public static String getStringPref(String key) {
        return getPreferences().getString(key, null);
    }

    public static void setStringPref(String key, String value) {
        getPreferences().edit().putString(key, value).apply();
    }

    public static void setBooleanPref(String key, boolean value) {
        getPreferences().edit().putBoolean(key, value).apply();
    }

    private static SharedPreferences getPreferences() {
        if (sharedPreferences == null)
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SightRemote.getInstance());
        return sharedPreferences;
    }

}
