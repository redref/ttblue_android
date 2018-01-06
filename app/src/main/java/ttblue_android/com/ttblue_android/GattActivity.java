package ttblue_android.com.ttblue_android;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothGattCallback;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.UUID;

public class GattActivity extends Activity {
    private static final String TAG = "GattActivity";

    // The connection to the device, if we are connected.
    private BluetoothDevice mDevice;
    private BluetoothGatt mGatt;

    // Tomtom related
    private FileTranferService mFTService;
    private AuthService mAuthService;

    // See https://android.googlesource.com/platform/external/bluetooth/bluedroid/+/master/stack/include/gatt_api.h
    private static final int GATT_ERROR = 0x85;
    private static final int GATT_AUTH_FAIL = 0x89;
    private static final String linesep = System.getProperty("line.separator");

    /* UI */
    private TextView LogText;

    /* UI related methods */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gatt);
        TtblueApplication mApplication = (TtblueApplication)getApplicationContext();

        // Init GUI
        LogText = (TextView)findViewById(R.id.text_log);

        // Connect
        BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
        mDevice = mBluetoothAdapter.getRemoteDevice(mApplication.getPreferenceString("saved_device"));
        deleteBondInformation(mDevice);
        connectGatt();
    }

    @Override
    public void onDestroy() {
        // Disconnect from the device if we're still connected.
        disconnectGatt();
        super.onDestroy();
    }

    private void log(String msg) {
        LogText.append(msg.concat(linesep));
    }

    private void showPinPrompt() {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.prompt, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);
        final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // CHAIN TO : read devices caps
                                mAuthService = new AuthService(mGatt, Integer.parseInt(userInput.getText().toString()));
                                readCharacteristic(UUIDs.UUID_SERVICE_COMMUNICATIONS_SETUP, UUIDs.UUID_CHARACTERISTIC_DEVICE_CAPABILITY);
                            }
                        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();
            }
        });
    }

    /* Bluetooth related methods */
    private boolean isAlreadyBonded() {
        if (!(BluetoothAdapter.getDefaultAdapter() == null || BluetoothAdapter.getDefaultAdapter().getBondedDevices() == null)) {
            for (BluetoothDevice dev : BluetoothAdapter.getDefaultAdapter().getBondedDevices()) {
                if (mDevice.getAddress().equals(dev.getAddress())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void deleteBondInformation(BluetoothDevice device) {
        try {
            // FFS Google, just unhide the method.
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        }
        catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void connectGatt() {
        // Disconnect if we are already connected.
        disconnectGatt();
        // Connect!
        mGatt = mDevice.connectGatt(this, true, mGattCallback);
    }

    private void disconnectGatt() {
        if (mFTService != null) {
            mFTService.destroy();
        }
        if (mGatt != null) {
            mGatt.disconnect();
            mGatt.close();
            mGatt = null;
        }
    }

    private void readCharacteristic(UUID service, UUID char_uuid) {
        BluetoothGattService deviceServices = mGatt.getService(service);
        if (deviceServices == null) {
            // Service not found.
            disconnectGatt();
            return;
        }
        BluetoothGattCharacteristic characteristic = deviceServices.getCharacteristic(char_uuid);
        if (characteristic == null) {
            // Characteristic not found.
            disconnectGatt();
            return;
        }
        // Read the characteristic.
        if (mGatt.readCharacteristic(characteristic)) {
            Log.i(TAG, "Ask to read characteristic " + char_uuid + " OK !");
        }
    }

    /* Bluetooth callbacks (initial behavior + forward to services) */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    // Connected to the device. Try to discover services.
                    if (!gatt.discoverServices()) {
                        // Couldn't discover services for some reason. Fail.
                        disconnectGatt();
                    }
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    deleteBondInformation(gatt.getDevice());
                    connectGatt();
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" + gatt.discoverServices());
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            // Tomtom watches version handle (V1 or V2)
            // Manage only V2 by now
            boolean fileTransfer2Found = false;
            for (BluetoothGattService service : gatt.getServices()) {
                if (service.getUuid().equals(UUIDs.UUID_SERVICE_FILE_TRANSFER_V2)) {
                    fileTransfer2Found = true;
                }
            }
            if (fileTransfer2Found) {
                runOnUiThread(() -> log("V2 FOUND!"));
                // CHAIN TO : read device info (beginning by the first)
                mFTService = new FileTranferService(mGatt, UUIDs.UUID_SERVICE_FILE_TRANSFER_V2);
                readCharacteristic(UUIDs.UUID_SERVICE_DEVICE_INFORMATION, UUIDs.UUID_CHARACTERISTIC_HARDWARE_REVISION);
            } else {
                runOnUiThread(() -> log("V1 FOUND! ABORT!"));
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.i(TAG, "Read characteristic " + characteristic.getUuid());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Characteristic read. Check it is the right one.
                if (UUIDs.UUID_CHARACTERISTIC_HARDWARE_REVISION.equals(characteristic.getUuid())) {
                    String value = characteristic.getStringValue(0).replaceAll("[^a-zA-Z0-9 .]", "");
                    Integer hwrev = Integer.parseInt(value);
                    runOnUiThread(() -> log("HWREV: " + hwrev));
                    if (!DeviceTypes.contains(hwrev)) {
                        runOnUiThread(() -> log("UNKNOWN DEVICE TYPE!"));
                    }
                    // CHAIN TO : Prompt for pin + read device capability (delay auth -> bug workaround)
                    showPinPrompt();
                } else if (UUIDs.UUID_CHARACTERISTIC_DEVICE_CAPABILITY.equals(characteristic.getUuid())) {
                    byte[] value = characteristic.getValue();
                    if (value.length < 1) {
                        runOnUiThread(() -> log("READ CAPABILITIES FAILED!"));
                        disconnectGatt();
                    } else {
                        // CHAIN TO : Send auth
                        mAuthService.startAuth();
                    }
                } else {
                    runOnUiThread(() -> log("READ WRONG CHARACTERISTIC!"));
                    disconnectGatt();
                }
            }
            else {
                runOnUiThread(() -> log("CHAR READ FAILED WITH " + status + "!"));
                disconnectGatt();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.i(TAG, "Wrote characteristic " + characteristic.getUuid());
            if (characteristic.getService().getUuid().equals(mFTService.mBtService.getUuid())) {
                mFTService.onCharacteristicWrite(characteristic, status);
            } else if (characteristic.getService().getUuid().equals(mAuthService.mBtService.getUuid())) {
                mAuthService.onCharacteristicWrite(characteristic, status);
            } else {
                Log.e(TAG, "onCharacteristicWrite NO SERVICE");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i(TAG, "Notified for characteristic " + characteristic.getUuid());

            if (characteristic.getService().getUuid().equals(mFTService.mBtService.getUuid())) {
                mFTService.onCharacteristicChanged(characteristic);
            } else if (characteristic.getService().getUuid().equals(mAuthService.mBtService.getUuid())) {
                String result = mAuthService.onCharacteristicChanged(characteristic);
                runOnUiThread(() -> log("TOKEN RES: " + result));

                if (result.equals("SUCCESS")) {
                    // CHAIN TO : List workouts files
                    mFTService.listWorkouts();
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.i(TAG, "Wrote descriptor " + descriptor.getUuid());
            if (descriptor.getCharacteristic().getService().getUuid().equals(mFTService.mBtService.getUuid())) {
                mFTService.onDescriptorWrite(descriptor, status);
            } else if (descriptor.getCharacteristic().getService().getUuid().equals(mAuthService.mBtService.getUuid())) {
                mAuthService.onDescriptorWrite(descriptor, status);
            }
        }
    };
}
