package ttblue_android.com.ttblue_android;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import com.google.common.primitives.UnsignedInteger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

public class FileTranferService extends BaseBluetoothService {
    private static final String TAG = "FileTranferService";

    private UnsignedInteger mZeroFileTransferNumber = UnsignedInteger.ZERO;
    private boolean mNotificationState = true;
    private String inFlightCommand;
    private String inFlightCommandFile;
    private FileHandler inFlightCommandFileHandler;
    private byte[] inFlightCommandFileData;
    private Integer inFlightCommandFileLength;
    private int inFlightCommandFileOffset;

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

    public void uploadMasterName(String content) {
        FileHandler masterNameFileHandler = FileHandler.createFileWriter(mServiceCallbacks.getExternalFilesDir(),"master_name");
        try {
            masterNameFileHandler.writeData(content.getBytes());
            File masterName = masterNameFileHandler.getFile();
            masterNameFileHandler.finish();
            uploadFile(masterName.getAbsolutePath(), FileTransferNumber.MASTER_NAME.uintValue(), FileTransferType.MANIFEST.uintValue());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            masterNameFileHandler.finish();
        }
    }

    public void listWorkouts() {
        listFiles(FileTransferType.WORKOUT.uintValue());
    }

    public void deleteFile(UnsignedInteger fileNumber, UnsignedInteger type) {
        inFlightCommand = "delete";
        writeCommand(getCommandPacket(4, fileNumber, type));
    }

    public void uploadFile(String filePath, UnsignedInteger fileNumber, UnsignedInteger type) {
        inFlightCommand = "upload";
        inFlightCommandFile = filePath;
        writeCommand(getCommandPacket(0, fileNumber, type));
    }

    private void listFiles(UnsignedInteger fileTransferType) {
        inFlightCommand = "list";
        writeCommand(getCommandPacket(3, mZeroFileTransferNumber, fileTransferType));
    }

    private byte[] getCommandPacket(Integer command, UnsignedInteger fileNumber, UnsignedInteger type) {
        // Prepare command packet
        byte[] packet;
        packet = new byte[4];
        packet[0] = command.byteValue(); // Write to file command
        packet[1] = type.byteValue();
        packet[2] = (byte) fileNumber.intValue();
        packet[3] = (byte) (fileNumber.intValue() >> 8);
        return packet;
    }

    public String bytesToHex(byte[] packet) {
        String spacket = "";
        for (byte a : packet) {
            spacket += Integer.toHexString(a) + " ";
        }
        return spacket;
    }

    public void writeCommand(byte[] packet) {
        writeRealPacket(mBtService.getCharacteristic(UUIDs.UUID_CHARACTERISTIC_COMMAND), packet, 2);
    }

    public void writeLength(byte[] packet) {
        Log.d(TAG, "Write length: " + bytesToHex(packet));
        writeRealPacket(mBtService.getCharacteristic(UUIDs.UUID_CHARACTERISTIC_TRANSFER_LENGTH), packet, 1);
    }

    public void writePacket(byte[] packet) {
        Log.d(TAG, "Write packet: " + bytesToHex(packet));
        writeRealPacket(mBtService.getCharacteristic(UUIDs.UUID_CHARACTERISTIC_TRANSFER_PACKET), packet, 1);
    }

    private void writeRealPacket(BluetoothGattCharacteristic characteristic, byte[] packet, int type) {
        characteristic.setValue(packet);
        characteristic.setWriteType(type);
        mGatt.writeCharacteristic(characteristic);
    }

    public void onCharacteristicRead(BluetoothGattCharacteristic characteristic, int status) {
        Log.e(TAG, "onCharacteristicRead : unknown characteristic" + characteristic.getUuid());
    }

    public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status) {
        if (characteristic.getUuid().equals(UUIDs.UUID_CHARACTERISTIC_COMMAND)) {
            Log.i(TAG, "Command writing success");
        } else if (characteristic.getUuid().equals(UUIDs.UUID_CHARACTERISTIC_TRANSFER_LENGTH) || characteristic.getUuid().equals(UUIDs.UUID_CHARACTERISTIC_TRANSFER_PACKET)) {
            if (inFlightCommandFile != null && inFlightCommandFileData == null) {
                try {
                    inFlightCommandFileLength = inFlightCommandFileHandler.getFileLength();
                    inFlightCommandFileData = inFlightCommandFileHandler.readData(inFlightCommandFileLength);
                    inFlightCommandFileOffset = 0;
                    inFlightCommandFileHandler.finish();
                } catch (IOException e) {
                    Log.e(TAG, "Read file failed: " + e.getMessage());
                    inFlightCommandFileHandler.finish();
                    inFlightCommandFileData = null;
                }
            }
            if (inFlightCommandFileData != null) {
                if (inFlightCommandFileOffset > inFlightCommandFileLength) {
                    byte[] packet = CRCUtil.toByteArray(CRCUtil.calculateCrc16(inFlightCommandFileData));
                    Log.i(TAG, "Send packet CRC");
                    inFlightCommandFileData = null;
                    inFlightCommandFile = null;
                    writePacket(packet);
                } else {
                    int remain = inFlightCommandFileLength - inFlightCommandFileOffset;
                    int read;
                    if (remain < 20) {
                        read = remain;
                    } else {
                        read = 20;
                    }
                    byte[] packet = Arrays.copyOfRange(inFlightCommandFileData, inFlightCommandFileOffset, inFlightCommandFileOffset + read);
                    Log.i(TAG, "Send packet data");
                    inFlightCommandFileOffset += 20; // Packet length
                    writePacket(packet);
                }
            }
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
                case "upload":
                    switch (status) {
                        case 0:
                            inFlightCommand = null;
                            mServiceCallbacks.startOperation();
                            break;
                        case 1:
                            inFlightCommandFileHandler = FileHandler.createFileReader(inFlightCommandFile);
                            byte[] packet = inFlightCommandFileHandler.getFileLengthByteArray();
                            writeLength(packet);
                            break;
                        default:
                            Log.i(TAG, "Upload operation unknown status");
                            mServiceCallbacks.disconnectGatt();
                    }
                    break;
                default:
                    Log.e(TAG, "Received COMMAND notification for unknown command");
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
                            Log.i(TAG, "Delete operation response");
                            break;
                        case 3:
                            // increment progression
                            Log.i(TAG, "Delete operation on-going");
                            break;
                        default:
                            Log.e(TAG, "Delete operation failed");
                            mServiceCallbacks.disconnectGatt();
                    }
                    break;
                default:
                    Log.e(TAG, "Received PACKET notification for unknown command");
                    mServiceCallbacks.disconnectGatt();
            }
        } else if (characteristic.getUuid().equals(UUIDs.UUID_CHARACTERISTIC_TRANSFER_BLOCK)) {
            byte[] data = characteristic.getValue();
            switch (inFlightCommand) {
                case "upload":
                    Log.i(TAG, "Upload validate block " + bytesToHex(data));
                    break;
                default:
                    Log.e(TAG, "Received BLOCK notification for unknown command");
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
}

