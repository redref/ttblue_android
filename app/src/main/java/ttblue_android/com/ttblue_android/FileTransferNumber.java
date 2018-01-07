package ttblue_android.com.ttblue_android;

import com.google.common.primitives.UnsignedInteger;

public enum FileTransferNumber {
    DEVICE_INFORMATION(1),
    MASTER_NAME(2),
    PREFERENCES_FILE(0),
    EVENTLOG(2);

    private final int mNumber;

    private FileTransferNumber(int number) {
        this.mNumber = number;
    }

    int intValue() {
        return this.mNumber;
    }

    public UnsignedInteger uintValue() {
        return UnsignedInteger.fromIntBits(this.mNumber);
    }
}