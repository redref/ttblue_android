package ttblue_android.com.ttblue_android;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.content.res.Configuration;


public class TtblueApplication extends Application {
    public BluetoothDevice device;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

}
