package ttblue_android.com.ttblue_android;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.google.common.primitives.UnsignedInteger;

import java.util.List;
import java.util.UUID;

public class FileTranferService extends BaseBluetoothService {
    private static final String TAG = "FileTranferService";

    private UnsignedInteger mFileTransferNumber = UnsignedInteger.ZERO;
    private boolean mNotificationState = true;
    private String inFlightCommand;

    public FileTranferService(BluetoothGatt gatt, UUID serviceuuid) {
        super(gatt, serviceuuid);
    }

    public void setNotifications() {
        setCharacteristicNotification(UUIDs.UUID_CHARACTERISTIC_TRANSFER_PACKET, mNotificationState);
    }

    public void stop() {
        mNotificationState = false;
        setCharacteristicNotification(UUIDs.UUID_CHARACTERISTIC_TRANSFER_PACKET, mNotificationState);
    }

    public void deleteMasterName() {
        deleteFile(FileTransferNumber.MASTER_NAME.uintValue(), FileTransferType.MANIFEST.uintValue());
    }

    public void listWorkouts() {
        listFiles(FileTransferType.WORKOUT.uintValue());
    }

    public void deleteFile(UnsignedInteger fileNumber, UnsignedInteger type) {
        inFlightCommand = "delete";
        // Prepare command packet
        byte[] packet;
        packet = new byte[4];
        packet[0] = (byte) 4; // Delete file command
        packet[1] = type.byteValue();
        packet[2] = (byte) fileNumber.intValue();
        packet[3] = (byte) (fileNumber.intValue() >> 8);
        writeCommand(packet);
    }

    private void listFiles(UnsignedInteger fileTransferType) {
        inFlightCommand = "list";
        byte[] packet;
        packet = new byte[4];
        packet[0] = (byte) 3; // List file command
        packet[1] = fileTransferType.byteValue();
        packet[2] = (byte) mFileTransferNumber.intValue();
        packet[3] = (byte) (mFileTransferNumber.intValue() >> 8);
        writeCommand(packet);
    }

    public void writeCommand(byte[] packet) {
        BluetoothGattCharacteristic characteristic = mBtService.getCharacteristic(UUIDs.UUID_CHARACTERISTIC_COMMAND);
        characteristic.setValue(packet);
        characteristic.setWriteType(2);
        mGatt.writeCharacteristic(characteristic);
    }

    public void onCharacteristicRead(BluetoothGattCharacteristic characteristic, int status) {
        Log.e(TAG, "onCharacteristicRead : unknown characteristic" + characteristic.getUuid());
    }

    public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status) {
        if (characteristic.getUuid().equals(UUIDs.UUID_CHARACTERISTIC_COMMAND)) {
            Log.i(TAG, "Command writing success");
        } else {
            Log.e(TAG, "onCharacteristicWrite : unknown characteristic" + characteristic.getUuid());
        }
    }

    public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().equals(UUIDs.UUID_CHARACTERISTIC_COMMAND)) {
            byte[] bytes = characteristic.getValue();
            if (bytes == null) {
                Log.e(TAG, "Received zero length state");
            }
            int[] dataInt = new int[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                dataInt[i] = UnsignedInteger.fromIntBits(bytes[i]).intValue();
            }
            int status = dataInt[0];
            switch (inFlightCommand) {
                case "delete":
                    switch (status) {
                        case 0:
                            Log.i(TAG, "Delete operation done");
                            inFlightCommand = null;
                            mServiceCallbacks.startOperation();
                            break;
                        case 1:
                            Log.i(TAG, "Delete operation on-going");
                            break;
                        default:
                            Log.i(TAG, "Delete operation unknown status");
                            mServiceCallbacks.disconnectGatt();
                    }
                    break;
                default:
                    Log.e(TAG, "Received COMMAND notification for unknwon command");
                    mServiceCallbacks.disconnectGatt();
            }
        } else if (characteristic.getUuid().equals(UUIDs.UUID_CHARACTERISTIC_TRANSFER_PACKET)) {
            byte[] data = characteristic.getValue();
            UnsignedInteger[] unsignedData = new UnsignedInteger[data.length];
            for (int i = 0; i < data.length; i++) {
                unsignedData[i] = UnsignedInteger.fromIntBits(data[i]);
            }
            switch (inFlightCommand) {
                case "delete":
                    switch (unsignedData[0].intValue()) {
                        case 0:
                        case 2:
                            Log.i(TAG, "Delete operation done");
                            inFlightCommand = null;
                            mServiceCallbacks.startOperation();
                            break;
                        case 3:
                            // Delete increment not handled
                            break;
                        default:
                            Log.e(TAG, "Delete operation failed");
                            mServiceCallbacks.disconnectGatt();
                    }
                    break;
                default:
                    Log.e(TAG, "Received PACKET notification for unknwon command");
                    mServiceCallbacks.disconnectGatt();
            }
        } else {
            Log.e(TAG, "onCharacteristicChanged : unknown characteristic" + characteristic.getUuid());
        }
    }

    public void onDescriptorWrite(BluetoothGattDescriptor descriptor, int status) {
        if (descriptor.getCharacteristic().getUuid().equals(UUIDs.UUID_CHARACTERISTIC_TRANSFER_PACKET)) {
            setCharacteristicNotification(UUIDs.UUID_CHARACTERISTIC_TRANSFER_BLOCK, mNotificationState);
        } else if (descriptor.getCharacteristic().getUuid().equals(UUIDs.UUID_CHARACTERISTIC_TRANSFER_BLOCK)) {
            setCharacteristicNotification(UUIDs.UUID_CHARACTERISTIC_TRANSFER_LENGTH, mNotificationState);
        } else if (descriptor.getCharacteristic().getUuid().equals(UUIDs.UUID_CHARACTERISTIC_TRANSFER_LENGTH)) {
            setCharacteristicNotification(UUIDs.UUID_CHARACTERISTIC_COMMAND, mNotificationState);
        } else if (descriptor.getCharacteristic().getUuid().equals(UUIDs.UUID_CHARACTERISTIC_COMMAND)) {
            mServiceCallbacks.startOperation();
        } else {
            Log.e(TAG, "onDescriptorWrite : unknown descriptor");
        }
    }

/*
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
}

