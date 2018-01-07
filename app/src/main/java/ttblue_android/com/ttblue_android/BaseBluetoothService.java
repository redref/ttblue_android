package ttblue_android.com.ttblue_android;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.UUID;

public abstract class BaseBluetoothService {
    protected ServiceCallbacks mServiceCallbacks;

    private static final String TAG = "BaseService";

    protected BluetoothGatt mGatt;
    protected BluetoothGattService mBtService;

    public void setCallbacks(ServiceCallbacks callbacks) {
        mServiceCallbacks = callbacks;
    }

    public BaseBluetoothService(BluetoothGatt gatt, UUID serviceuuid) {
        this.mGatt = gatt;
        this.mBtService = mGatt.getService(serviceuuid);
    }

    public void stop() {

    }

    public void readCharacteristic(UUID char_uuid) {
        BluetoothGattCharacteristic characteristic = mBtService.getCharacteristic(char_uuid);
        if (mGatt.readCharacteristic(characteristic)) {
            Log.i(TAG, "Begin read characteristic " + char_uuid);
        } else {
            Log.e(TAG, "Not able to begin read characteristic" + char_uuid);
        }
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

    public abstract void onCharacteristicRead(BluetoothGattCharacteristic characteristic, int status);

    public abstract void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status);

    public abstract void onCharacteristicChanged(BluetoothGattCharacteristic characteristic);

    public abstract void onDescriptorWrite(BluetoothGattDescriptor descriptor, int status);
}
