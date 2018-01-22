package sugar.free.sightparser;

import android.content.SharedPreferences;

public class DataStorage {

    private SharedPreferences sharedPreferences;

    public DataStorage(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public String get(String key) {
        return sharedPreferences.getString(key, null);
    }

    public void set(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }
}
