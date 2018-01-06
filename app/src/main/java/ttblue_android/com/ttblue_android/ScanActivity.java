package ttblue_android.com.ttblue_android;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ToggleButton;

import java.util.Set;

public class ScanActivity extends Activity {
    private static final String TAG = "ScanActivity";
    private TtblueApplication mApplication;

    /* Bluetooh variables */
    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> mDevices;

    /* Client UI elements */
    private ToggleButton ScanButton;
    private DevicesAdapter ListViewAdapter;

    /* Tools */
    private SingBroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        mApplication = (TtblueApplication)getApplicationContext();

        // Init togglebutton
        ScanButton = (ToggleButton) findViewById(R.id.scan_button);

        // Init devices listing
        ListView DevicesList = (ListView) findViewById(R.id.device_list);
        ListViewAdapter = new DevicesAdapter(this, mDevices);
        DevicesList.setAdapter(ListViewAdapter);
        DevicesList.setClickable(true);
        DevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mApplication.savePreference("saved_device", parent.getAdapter().getItem(position).toString());
                finish();
            }
        });

        BluetoothManager mBluetoothManager = (BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
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
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    public void scanToggleListener(View v) {
        Log.i(TAG, "Start scan");
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
