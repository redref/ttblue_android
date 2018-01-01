package ttblue_android.com.ttblue_android;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.util.Log;

import java.util.UUID;

public class AuthService {
    private static final String TAG = "AuthService";

    private BluetoothGatt mGatt;
    protected BluetoothGattService mBtService;

    private int pin;

    public AuthService(BluetoothGatt gatt, int pin) {
        this.mGatt = gatt;
        this.mBtService = mGatt.getService(UUIDs.UUID_SERVICE_COMMUNICATIONS_SETUP);
        this.pin = pin;
    }

    public void destroy() {
        setCharacteristicNotification(UUIDs.UUID_CHARACTERISTIC_AUTH_TOKEN, false);
    }

    public void startAuth() {
        setCharacteristicNotification(UUIDs.UUID_CHARACTERISTIC_AUTH_TOKEN, true);
    }

    public void sendAppVersion() {
        BluetoothGattCharacteristic characteristic = mBtService.getCharacteristic(UUIDs.UUID_CHARACTERISTIC_APP_VERSION);
        // ostype, os_version, 0, 0, app_type, app_version, 0, 0
        characteristic.setValue(new byte[]{(byte) 1, (byte) Build.VERSION.SDK_INT, (byte) 0, (byte) 0, (byte) 1, (byte) 31, (byte) 0, (byte) 0});
        characteristic.setWriteType(2);
        mGatt.writeCharacteristic(characteristic);
    }

    public void sendPin() {
        BluetoothGattCharacteristic characteristic = mBtService.getCharacteristic(UUIDs.UUID_CHARACTERISTIC_AUTH_TOKEN);
        characteristic.setValue(new byte[]{(byte) (this.pin & 255), (byte) ((this.pin >> 8) & 255), (byte) ((this.pin >> 16) & 255), (byte) ((this.pin >> 24) & 255)});
        characteristic.setWriteType(2);
        mGatt.writeCharacteristic(characteristic);
    }

    public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status) {
        if (characteristic.getUuid().equals(UUIDs.UUID_CHARACTERISTIC_APP_VERSION)) {
            if (status != 0) {
                Log.e(TAG, "Error writing APP_VERSION");
            }
            sendPin();
        }
    }

    public String onCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().equals(UUIDs.UUID_CHARACTERISTIC_AUTH_TOKEN)) {
            byte[] bytes = characteristic.getValue();
            switch (bytes[0]) {
                case (byte) 1:
                    return "SUCCESS";
                case (byte) 2:
                    return "REJECTED";
                case (byte) 3: // RECONNECT
                    sendAppVersion();
                    Log.w(TAG, "Reconnect");
                    return "RECONNECT";
                default:
                    return "Abort ! Received unknown auth response";
            }
        }
        return "AUTH SERVICE DEAD END";
    }

    public void onDescriptorWrite(BluetoothGattDescriptor descriptor, int status) {
        sendAppVersion();
    }

    public void setCharacteristicNotification(UUID characteristic_id, Boolean enable) {
        BluetoothGattCharacteristic characteristic = mBtService.getCharacteristic(characteristic_id);
        mGatt.setCharacteristicNotification(characteristic,true);
        BluetoothGattDescriptor blockDesc = characteristic.getDescriptor(UUIDs.UUID_UPDATE_NOTIFICATION_DESCRIPTOR);
        if (blockDesc == null) {
            Log.e(TAG, new NullPointerException("Failed to get Notification Descriptor for " + characteristic.getUuid().toString()).toString());
            return;
        }
        byte[] descVal;
        if (enable) {
            descVal = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
        } else {
            descVal = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
        }
        blockDesc.setValue(descVal);
        blockDesc.getCharacteristic().setWriteType(2);
        mGatt.writeDescriptor(blockDesc);
    }
}