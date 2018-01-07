package ttblue_android.com.ttblue_android;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.util.Log;

import java.util.UUID;

public class AuthService extends BaseBluetoothService {
    private static final String TAG = "AuthService";

    private int pin;

    public AuthService(BluetoothGatt gatt, UUID serviceuuid) {
        super(gatt, serviceuuid);
    }

    public void setPin(String pin) {
        this.pin = Integer.parseInt(pin);
    }

    public void stop() {
        setCharacteristicNotification(UUIDs.UUID_CHARACTERISTIC_AUTH_TOKEN, false);
    }

    public void sendAuth() {
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

    public void onCharacteristicRead(BluetoothGattCharacteristic characteristic, int status) {
        if (characteristic.getUuid().equals(UUIDs.UUID_CHARACTERISTIC_DEVICE_CAPABILITY)) {
            mServiceCallbacks.startOperation();
        } else {
            Log.e(TAG, "onCharacteristicRead : unknown characteristic" + characteristic.getUuid());
        }
    }

    public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status) {
        if (characteristic.getUuid().equals(UUIDs.UUID_CHARACTERISTIC_APP_VERSION)) {
            sendPin();
        } else if (characteristic.getUuid().equals(UUIDs.UUID_CHARACTERISTIC_AUTH_TOKEN)) {
            Log.i(TAG, "Pin writing success");
        } else {
            Log.e(TAG, "onCharacteristicWrite : unknown characteristic" + characteristic.getUuid());
        }
    }

    public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().equals(UUIDs.UUID_CHARACTERISTIC_AUTH_TOKEN)) {
            byte[] bytes = characteristic.getValue();
            switch (bytes[0]) {
                case (byte) 1:
                    // Success - go next
                    Log.i(TAG, "Auth success");
                    mServiceCallbacks.startOperation();
                    break;
                case (byte) 2:
                    // Reject - reask Pin
                    Log.i(TAG, "Auth retry");
                    mServiceCallbacks.showPinPrompt();
                    break;
                case (byte) 3:
                    // Reconnect - redo
                    sendAppVersion();
                    Log.w(TAG, "Try to reconnect");
                    break;
                default:
                    mServiceCallbacks.displayToUser("Auth unknown return value");
                    mServiceCallbacks.disconnectGatt();
            }
        } else {
            Log.e(TAG, "onCharacteristicChanged : unknown characteristic" + characteristic.getUuid());
        }
    }

    public void onDescriptorWrite(BluetoothGattDescriptor descriptor, int status) {
        if (descriptor.getCharacteristic().getUuid().equals(UUIDs.UUID_CHARACTERISTIC_AUTH_TOKEN)) {
            sendAppVersion();
        } else {
            Log.e(TAG, "onDescriptorWrite : unknown descriptor");
        }
    }
}