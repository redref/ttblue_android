package ttblue_android.com.ttblue_android;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.content.res.Configuration;


public class TtblueApplication extends Application {
    public static final String PREFS_NAME = "Ttblue_android_preferences";

    public SharedPreferences settings;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        settings = getSharedPreferences(PREFS_NAME, 0);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public Boolean getPreferenceBoolean(String name) {
        return settings.getBoolean(name, false);
    }

    public Integer getPreferenceInteger(String name) {
        return settings.getInt(name, 0);
    }

    public String getPreferenceString(String name) {
        return settings.getString(name, "");
    }

    public void savePreference(String name, Boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(name, value);
        editor.commit();
    }

    public void savePreference(String name, Integer value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(name, value);
        editor.commit();
    }

    public void savePreference(String name, String value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(name, value);
        editor.commit();
    }

}
