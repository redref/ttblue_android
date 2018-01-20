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
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GattActivity extends Activity implements ServiceCallbacks {
    private static final String TAG = "GattActivity";

    private TtblueApplication mApplication;

    // The connection to the device, if we are connected.
    private BluetoothDevice mDevice;
    private BluetoothGatt mGatt;

    // Tomtom related
    private FileTranferService mFTService;
    private AuthService mAuthService;
    private DeviceInfoService mDeviceService;
    private List<BaseBluetoothService> services;
    private Integer mCurrentOp = 0;

    /* UI */
    private TextView LogText;
    private static final String linesep = System.getProperty("line.separator");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gatt);
        mApplication = (TtblueApplication)getApplicationContext();

        // Init GUI
        LogText = (TextView)findViewById(R.id.text_log);

        // Bluetooth
        BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
        mDevice = mBluetoothAdapter.getRemoteDevice(mApplication.getPreferenceString("saved_device"));

        services = new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectGatt();
    }

    @Override
    public void onDestroy() {
        disconnectGatt();
        super.onDestroy();
    }

    public String getExternalFilesDir() {
        return mApplication.getExternalFilesDir(null).getAbsolutePath();
    }

    public void resetConnection(View view) {
        deleteBondInformation(mDevice);
        disconnectGatt();
        mCurrentOp = 0;
        connectGatt();
    }

    private void connectGatt() {
        if (mGatt == null) {
            mGatt = mDevice.connectGatt(this, true, mGattCallback);
        }
    }

    public void disconnectGatt() {
        for (BaseBluetoothService service : services) {
            if (service != null) {
                service.setCallbacks(null);
                service.stop();
            }
        }
        if (mGatt != null) {
            mGatt.disconnect();
            mGatt.close();
            mGatt = null;
        }
        displayToUser("Disconnected");
    }

    public void displayToUser(String msg) {
        runOnUiThread(() -> LogText.append(msg.concat(linesep)));
    }

    public void retryOperation() {
        mCurrentOp--;
        startOperation();
    }

    public void startOperation() {
        /* Contain application logic behind callbacks, services */
        Log.i(TAG, "StartOperation " + mCurrentOp.toString());
        switch (mCurrentOp) {
            case 0:
                // Connected to the device. Try to discover services.
                if (mGatt.discoverServices()) {
                    Log.i(TAG, "Begin services discovery");
                } else {
                    // Couldn't discover services for some reason. Fail.
                    disconnectGatt();
                }
                break;
            case 1:
                // Tomtom watches version handle (V1 or V2) - TODO : Handle V1
                for (BluetoothGattService service : mGatt.getServices()) {
                    if (service.getUuid().equals(UUIDs.UUID_SERVICE_FILE_TRANSFER_V2)) {
                        mFTService = new FileTranferService(mGatt, UUIDs.UUID_SERVICE_FILE_TRANSFER_V2);
                        mFTService.setCallbacks(GattActivity.this);
                        services.add(mFTService);
                        displayToUser("V2 FOUND!");
                        break;
                    }
                }
                if (mFTService == null) {
                    displayToUser("V1 FOUND!");
                    disconnectGatt();
                } else {
                    // Check hardware revision
                    mDeviceService = new DeviceInfoService(mGatt, UUIDs.UUID_SERVICE_DEVICE_INFORMATION);
                    services.add(mDeviceService);
                    mDeviceService.setCallbacks(GattActivity.this);
                    mDeviceService.checkHardwareRevision();
                }
                break;
            case 2:
                String pin = mApplication.getPreferenceString("saved_code");
                if (pin.equals("")) {
                    showPinPrompt();
                } else {
                    mCurrentOp++;
                    startOperation();
                    return;
                }
                break;
            case 3:
                if (mAuthService == null) {
                    mAuthService = new AuthService(mGatt, UUIDs.UUID_SERVICE_COMMUNICATIONS_SETUP);
                    services.add(mAuthService);
                    mAuthService.setCallbacks(GattActivity.this);
                }
                mAuthService.setPin(mApplication.getPreferenceString("saved_code"));
                // Read it (like tomtom app) but not using it
                mAuthService.readCharacteristic(UUIDs.UUID_CHARACTERISTIC_DEVICE_CAPABILITY);
                break;
            case 4:
                mAuthService.sendAuth();
                break;
            case 5:
                mFTService.setNotifications();
                break;
            case 6:
                mFTService.deleteMasterName();
                break;
            case 7:
                mFTService.uploadMasterName("TTBLUE");
                break;
            case 8:
                mFTService.deleteEphemeris();
                break;
            case 9:
                mFTService.updateEphemeris();
                break;
        }
        mCurrentOp++;
    }

    public void showPinPrompt() {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.prompt, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);
        final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
        // set dialog message
        alertDialogBuilder.setCancelable(false).setPositiveButton(
                "OK",
                (dialog, which) -> {
                    String code = userInput.getText().toString();
                    mApplication.savePreference("saved_code", code);
                    // Retry - Fix current op
                    if (mCurrentOp == 4 || mCurrentOp == 5) {
                        mCurrentOp = 3;
                    }
                    startOperation();
                }
        );
        runOnUiThread(() -> {
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        });
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

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");
            }
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    startOperation();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    connectGatt();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            startOperation();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.i(TAG, "Read characteristic " + characteristic.getUuid());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Characteristic read. Check it is the right one.
                UUID this_service = characteristic.getService().getUuid();
                for (BaseBluetoothService service: services) {
                    if (service.mBtService.getUuid().equals(this_service)) {
                        service.onCharacteristicRead(characteristic, status);
                        return;
                    }
                }
                Log.e(TAG,"Error reading characteristic " + characteristic.getUuid() + " : no matching service");
            }
            else {
                Log.e(TAG,"Error reading characteristic " + characteristic.getUuid());
                disconnectGatt();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.i(TAG, "Wrote characteristic " + characteristic.getUuid());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Characteristic read. Check it is the right one.
                UUID this_service = characteristic.getService().getUuid();
                for (BaseBluetoothService service: services) {
                    if (service.mBtService.getUuid().equals(this_service)) {
                        service.onCharacteristicWrite(characteristic, status);
                        return;
                    }
                }
                Log.e(TAG,"Error writing characteristic " + characteristic.getUuid() + " : no matching service");
            }
            else {
                Log.e(TAG,"Error writing characteristic " + characteristic.getUuid());
                disconnectGatt();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i(TAG, "Notified on characteristic " + characteristic.getUuid());
            UUID this_service = characteristic.getService().getUuid();
            for (BaseBluetoothService service: services) {
                if (service.mBtService.getUuid().equals(this_service)) {
                    service.onCharacteristicChanged(characteristic);
                    return;
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.i(TAG, "Wrote descriptor " + descriptor.getUuid());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Characteristic read. Check it is the right one.
                UUID this_service = descriptor.getCharacteristic().getService().getUuid();
                for (BaseBluetoothService service: services) {
                    if (service.mBtService.getUuid().equals(this_service)) {
                        service.onDescriptorWrite(descriptor, status);
                        return;
                    }
                }
                Log.e(TAG,"Error writing descriptor " + descriptor.getUuid() + " : no matching service");
            }
            else {
                Log.e(TAG,"Error writing descriptor " + descriptor.getUuid());
                disconnectGatt();
            }
        }
    };
}
