package ttblue_android.com.ttblue_android;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import java.util.UUID;

public class DeviceInfoService extends BaseBluetoothService {
    private static final String TAG = "DeviceInfoService";

    public DeviceInfoService(BluetoothGatt gatt, UUID serviceuuid) {
        super(gatt, serviceuuid);
    }

    public void checkHardwareRevision() {
        readCharacteristic(UUIDs.UUID_CHARACTERISTIC_HARDWARE_REVISION);
    }

    public void onCharacteristicRead(BluetoothGattCharacteristic characteristic, int status) {
        if (characteristic.getUuid().equals(UUIDs.UUID_CHARACTERISTIC_HARDWARE_REVISION)) {
            // checkHardwareRevision callback
            String value = characteristic.getStringValue(0).replaceAll("[^a-zA-Z0-9 .]", "");
            Integer hwrev = Integer.parseInt(value);
            mServiceCallbacks.displayToUser("HWREV: " + hwrev);
            if (DeviceTypes.contains(hwrev)) {
                mServiceCallbacks.startOperation();
            } else {
                mServiceCallbacks.displayToUser("UNKNOWN DEVICE TYPE!");
                mServiceCallbacks.disconnectGatt();
            }
        }  else {
            Log.e(TAG, "onCharacteristicRead : unknown characteristic" + characteristic.getUuid());
        }
    }

    public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status) {
        Log.e(TAG, "onCharacteristicWrite : unknown characteristic" + characteristic.getUuid());
    }

    public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
        Log.e(TAG, "onCharacteristicChanged : unknown characteristic" + characteristic.getUuid());
    }

    public void onDescriptorWrite(BluetoothGattDescriptor descriptor, int status) {
        Log.e(TAG, "onDescriptorWrite : unknown descriptor");
    }
}

