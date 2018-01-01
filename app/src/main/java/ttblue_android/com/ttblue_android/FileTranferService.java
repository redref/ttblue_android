package ttblue_android.com.ttblue_android;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.google.common.primitives.UnsignedInteger;

import java.util.List;
import java.util.UUID;

public class FileTranferService {
    private static final String TAG = "FileTranferService";

    private BluetoothGatt mGatt;
    protected BluetoothGattService mBtService;

    private UnsignedInteger mFileTransferNumber = UnsignedInteger.ZERO;

    public FileTranferService(BluetoothGatt gatt, UUID serviceuuid) {
        this.mGatt = gatt;
        this.mBtService = mGatt.getService(serviceuuid);
    }

    public void destroy() {
        setCharacteristicNotification(UUIDs.UUID_CHARACTERISTIC_TRANSFER_PACKET, false);
        setCharacteristicNotification(UUIDs.UUID_CHARACTERISTIC_TRANSFER_BLOCK, false);
        setCharacteristicNotification(UUIDs.UUID_CHARACTERISTIC_TRANSFER_LENGTH, false);
    }

    protected BluetoothGattService getBtService() {
        return this.mBtService;
    }

    public void listWorkouts() {
        listFiles(FileTransferType.WORKOUT.uintValue());
    }

    private void listFiles(UnsignedInteger fileTransferType) {
        // Prepare command packet
        byte[] packet;
        packet = new byte[4];
        packet[0] = (byte) 3; // List file command
        packet[1] = fileTransferType.byteValue();
        packet[2] = (byte) this.mFileTransferNumber.intValue();
        packet[3] = (byte) (this.mFileTransferNumber.intValue() >> 8);

        // Set Notifications (for the response
        setCharacteristicNotification(UUIDs.UUID_CHARACTERISTIC_TRANSFER_PACKET, true);
        setCharacteristicNotification(UUIDs.UUID_CHARACTERISTIC_TRANSFER_BLOCK, true);
        setCharacteristicNotification(UUIDs.UUID_CHARACTERISTIC_TRANSFER_LENGTH, true);

        // Write
        BluetoothGattCharacteristic characteristic = mBtService.getCharacteristic(UUIDs.UUID_CHARACTERISTIC_COMMAND);
        characteristic.setValue(packet);
        characteristic.setWriteType(2);
        mGatt.writeCharacteristic(characteristic);
    }

    public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status) {

    }

    public String onCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
        return "NONE";
    }

    public void onDescriptorWrite(BluetoothGattDescriptor descriptor, int status) {

    }

/*
    protected void setCharacteristicNotification(int characteristicId, boolean enabled) {
        if (isKilled()) {
            Log.e(TAG, "Trying to set notification after service has been killed.");
        } else if (this.mGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
        } else {
            BluetoothGattCharacteristic charac = getCharacteristics(characteristicId);
            if (charac == null) {
                Log.w(TAG, "Characteristic " + characteristicId + " does not exist");
                return;
            }
            this.mBleDevice.(charac, enabled);
            BluetoothGattDescriptor blockDesc = charac.getDescriptor(UUID_UPDATE_NOTIFICATION_DESCRIPTOR);
            if (blockDesc == null) {
                Logger.exception(new NullPointerException("Failed to get Notification Descriptor for " + characteristicId));
                return;
            }
            byte[] descVal;
            if (enabled) {
                descVal = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
            } else {
                descVal = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
            }
            blockDesc.setValue(descVal);
            blockDesc.getCharacteristic().setWriteType(2);
            this.mBleDevice.writeDescriptor(blockDesc);
        }
    }

         private void listTransferPacketUpdated(BluetoothGattCharacteristic characteristic) {
        startCommandTimeoutTimer();
        byte[] charData = characteristic.getValue();
        if (charData == null || charData.length < 2) {
            Logger.warning(TAG, "FileListPacketUpdated - Packet not long enough");
            return;
        }
        int i;
        int start;
        int len = charData.length;
        int[] data = new int[len];
        for (i = 0; i < len; i++) {
            data[i] = (byte) (charData[i] & 65535);
        }
        if (this.mFileListCount == -1) {
            Logger.info(TAG, "First packet. FileListCount 0 and starting...");
            this.mFileListCount = data[0] + (data[1] << 8);
            Logger.info(TAG, StringHelper.join("File count from first bytes is ", Integer.toString(this.mFileListCount)));
            start = 2;
        } else {
            start = 0;
        }
        int fileCount = (len - start) / 2;
        for (i = 0; i < fileCount; i++) {
            this.mFileList.add(UnsignedInteger.fromIntBits(data[(i * 2) + start] + (data[((i * 2) + start) + 1] << 8)));
            Logger.info(TAG, StringHelper.join("FileListPacketUpdated got file number ", Integer.toHexString(fileNumber)));
        }
        if (this.mFileList.size() == this.mFileListCount) {
            Logger.warning(TAG, StringHelper.join("FileListTransfer successfully completed... list has ", Integer.toString(this.mFileList.size()), " files"));
            this.mFileTransferState = FileTransferState.WAITING_FOR_COMPLETE;
            waitForCompletionWithStatus(FileTransferStatus.OK);
        }
    }
     */

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

