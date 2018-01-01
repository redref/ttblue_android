package ttblue_android.com.ttblue_android;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ToggleButton;

import java.util.Set;

public class MainActivity extends Activity {
    private static final String TAG = "Main";

    /* Bluetooh variables */
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> mDevices;

    /* Client UI elements */
    private ToggleButton ScanButton;
    private ListView DevicesList;
    private DevicesAdapter ListViewAdapter;

    /* Tools */
    private SingBroadcastReceiver mReceiver;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TtblueApplication mApplication = (TtblueApplication)getApplicationContext();

        // Init togglebutton
        ScanButton = (ToggleButton) findViewById(R.id.scan_button);

        // Init devices listing
        DevicesList = (ListView) findViewById(R.id.device_list);
        ListViewAdapter = new DevicesAdapter(this, mDevices);
        DevicesList.setAdapter(ListViewAdapter);
        DevicesList.setClickable(true);
        DevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mApplication.device = (BluetoothDevice)parent.getAdapter().getItem(position);
                Intent intent = new Intent(MainActivity.this, GattActivity.class);
                startActivity(intent);
            }
        });

        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mDevices = mBluetoothAdapter.getBondedDevices();
        ListViewAdapter.notifyDataSetChanged();

        //let's make a broadcast receiver to register our things
        mReceiver = new SingBroadcastReceiver();
        IntentFilter ifilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, ifilter);
    }

    @Override
    protected void onDestroy() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    public void scanToggleListener(View v) {
        if (ScanButton.isChecked()) {
            if (! mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
                ScanButton.setChecked(false);
            } else {
                mBluetoothAdapter.startDiscovery();
            }
        } else {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    private class SingBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                ListViewAdapter.append(device);
            }
        }
    }
}
