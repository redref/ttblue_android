package ttblue_android.com.ttblue_android;

import com.google.common.primitives.UnsignedInteger;

public enum FileTransferType {
    EPHEMERIS(1),
    MANIFEST(2),
    WORKOUT(145),
    GOLF_SCORECARDS(148),
    GOLF_ROUNDS(148),
    GOLF_MANIFEST(176),
    STEP_BUCKET(177),
    PREFERENCES(242),
    FIRMWARE_CHUNK(253),
    BRIDGEHEAD(0);

    private final int mType;

    private FileTransferType(int type) {
        this.mType = type;
    }

    public int intValue() {
        return this.mType;
    }

    public UnsignedInteger uintValue() {
        return UnsignedInteger.fromIntBits(this.mType);
    }

    public static String getName(UnsignedInteger uInt) {
        if (uInt.equals(EPHEMERIS.uintValue())) {
            return EPHEMERIS.name();
        }
        if (uInt.equals(WORKOUT.uintValue())) {
            return WORKOUT.name();
        }
        if (uInt.equals(MANIFEST.uintValue())) {
            return MANIFEST.name();
        }
        if (uInt.equals(GOLF_MANIFEST.uintValue())) {
            return GOLF_MANIFEST.name();
        }
        if (uInt.equals(STEP_BUCKET.uintValue())) {
            return STEP_BUCKET.name();
        }
        if (uInt.equals(GOLF_SCORECARDS.uintValue())) {
            return GOLF_SCORECARDS.name();
        }
        if (uInt.equals(GOLF_ROUNDS.uintValue())) {
            return GOLF_ROUNDS.name();
        }
        if (uInt.equals(FIRMWARE_CHUNK.uintValue())) {
            return FIRMWARE_CHUNK.name();
        }
        if (uInt.equals(BRIDGEHEAD.uintValue())) {
            return BRIDGEHEAD.name();
        }
        return "UNKNOWN";
    }
}
