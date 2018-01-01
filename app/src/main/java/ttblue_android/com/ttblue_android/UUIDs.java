package ttblue_android.com.ttblue_android;

import java.util.HashMap;
import java.util.UUID;

public class UUIDs {
    // Services
    public static final UUID UUID_SERVICE_DEVICE_INFORMATION = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_FILE_TRANSFER_V1 = UUID.fromString("170d0d30-4213-11e3-aa6e-0800200c9a66");
    public static final UUID UUID_SERVICE_FILE_TRANSFER_V2 = UUID.fromString("b993bf90-81e1-11e4-b4a9-0800200c9a66");
    public static final UUID UUID_SERVICE_COMMUNICATIONS_SETUP = UUID.fromString("b993bf91-81e1-11e4-b4a9-0800200c9a66");

    // UUID_SERVICE_DEVICE_INFORMATION characteristics
    public static final UUID UUID_CHARACTERISTIC_HARDWARE_REVISION = UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb");

    // UUID_SERVICE_FILE_TRANSFER_* characteristics
    public static final UUID UUID_CHARACTERISTIC_COMMAND = UUID.fromString("170d0d31-4213-11e3-aa6e-0800200c9a66");
    public static final UUID UUID_CHARACTERISTIC_TRANSFER_BLOCK = UUID.fromString("170d0d34-4213-11e3-aa6e-0800200c9a66");
    public static final UUID UUID_CHARACTERISTIC_TRANSFER_LENGTH = UUID.fromString("170d0d32-4213-11e3-aa6e-0800200c9a66");
    public static final UUID UUID_CHARACTERISTIC_TRANSFER_PACKET = UUID.fromString("170d0d33-4213-11e3-aa6e-0800200c9a66");

    // UUID_SERVICE_COMMUNICATIONS_SETUP characteristics
    public static final UUID UUID_CHARACTERISTIC_AUTH_TOKEN = UUID.fromString("b993bf92-81e1-11e4-b4a9-0800200c9a66");
    public static final UUID UUID_CHARACTERISTIC_APP_VERSION = UUID.fromString("b993bf93-81e1-11e4-b4a9-0800200c9a66");
    public static final UUID UUID_CHARACTERISTIC_SYNC = UUID.fromString("47ec27b0-5c56-11e5-a837-0800200c9a66");
    public static final UUID UUID_CHARACTERISTIC_DEVICE_CAPABILITY = UUID.fromString("c6945cb0-4ab4-11e7-9598-0800200c9a66");

    //
    public static final UUID UUID_UPDATE_NOTIFICATION_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // From APP (UUID not used are not declared above)
    /*
    public static final int HARDWARE_REVISION = 3;
    public static final int MANUFACTURER_NAME = 5;
    public static final int MODEL_NUMBER = 1;
    public static final int SERIAL_NUMBER = 2;
    public static final int SOFTWARE_REVISION = 4;
    public static final int SYSTEM_ID = 0;
    private static final String TAG = "DeviceInformationService";
    private static final String UUID_CHARACTERISTIC_HARDWARE_REVISION = "00002a27-0000-1000-8000-00805f9b34fb";
    private static final String UUID_CHARACTERISTIC_MANUFACTURER_NAME = "00002a29-0000-1000-8000-00805f9b34fb";
    private static final String UUID_CHARACTERISTIC_MODEL_NUMBER = "00002a24-0000-1000-8000-00805f9b34fb";
    private static final String UUID_CHARACTERISTIC_SERIAL_NUMBER = "00002a25-0000-1000-8000-00805f9b34fb";
    private static final String UUID_CHARACTERISTIC_SOFTWARE_REVISION = "00002a28-0000-1000-8000-00805f9b34fb";
    private static final String UUID_CHARACTERISTIC_SYSTEM_ID = "00002a23-0000-1000-8000-00805f9b34fb";
    public static final UUID UUID_SERVICE_DEVICE_INFORMATION = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");

    public static final UUID UUID_SERVICE_FILE_TRANSFER_V1 = UUID.fromString("170d0d30-4213-11e3-aa6e-0800200c9a66");
    public static final UUID UUID_SERVICE_FILE_TRANSFER_V2 = UUID.fromString("b993bf90-81e1-11e4-b4a9-0800200c9a66");
    */

    /* From CommsSetupGattService
    private static final int APP_VERSION_CHARACTERISTIC = 1;
    private static final int AUTH_TOKEN_CHARACTERISTIC = 0;
    private static final int AUTH_TOKEN_RESPONSE_INVALID = 2;
    private static final int AUTH_TOKEN_RESPONSE_RECONNECT = 3;
    private static final int AUTH_TOKEN_RESPONSE_VALID = 1;
    private static final int DEVICE_CAPABILITY_CHARACTERISTIC = 3;
    private static final int SYNC_CHARACTERISTIC = 2;
    public static final String TAG = "CommsSetupGattService";
    public static final UUID UUID_CHARACTERISTIC_APP_VERSION = UUID.fromString("b993bf93-81e1-11e4-b4a9-0800200c9a66");
    public static final UUID UUID_CHARACTERISTIC_AUTH_TOKEN = UUID.fromString("b993bf92-81e1-11e4-b4a9-0800200c9a66");
    public static final UUID UUID_CHARACTERISTIC_DEVICE_CAPABILITY = UUID.fromString("c6945cb0-4ab4-11e7-9598-0800200c9a66");
    public static final UUID UUID_CHARACTERISTIC_SYNC = UUID.fromString("47ec27b0-5c56-11e5-a837-0800200c9a66");
    public static final UUID UUID_SERVICE_COMMUNICATIONS_SETUP = UUID.fromString("b993bf91-81e1-11e4-b4a9-0800200c9a66");
    */

    /*
        private static final String ACCESS_SECRET_NAME = "accessSecret";
    private static final String ACCESS_TOKEN_NAME = "accessToken";
    public static final String ACTIVITY_BAND_2_HW_REVISION_01 = "3002";
    public static final String ACTIVITY_BAND_3_HW_REVISION_01 = "3003";
    public static final String ACTIVITY_BAND_3_HW_REVISION_02 = "3004";
    public static final String ACTIVITY_BAND_HW_REVISION_01 = "3001";
    public static final byte APP_TYPE = (byte) 1;
    private static final String DEVICE_NAME_TOMTOM_GPS_WATCH = "TomTom GPS Watch";
    private static final String DEVICE_NAME_TOMTOM_TRACKER = "TomTom Tracker";
    public static final String FILE_NAME_MASTER_NAME = "mastername";
    public static final String GOLF_WATCH_2_HW_REVISION_01 = "2011";
    public static final String GOLF_WATCH_2_HW_REVISION_02 = "2022";
    public static final String GOLF_WATCH_3_HW_REVISION_01 = "2023";
    public static final String GOLF_WATCH_3_HW_REVISION_02 = "2024";
    public static final String GOLF_WATCH_HW_REVISION_0 = "1100";
    public static final String GOLF_WATCH_HW_REVISION_1 = "1101";
    public static final String GOLF_WATCH_HW_REVISION_2 = "1102";
    public static final String GOLF_WATCH_HW_REVISION_3 = "1103";
    public static final int MAX_PREF_FETCH_RETRIES = 1;
    public static final int MAX_PREF_SEND_RETRIES = 1;
    public static final byte OS_TYPE = (byte) 1;
    public static final String SPORTS_WATCH_2_HW_REVISION_01 = "2001";
    public static final String SPORTS_WATCH_2_HW_REVISION_02 = "2002";
    public static final String SPORTS_WATCH_2_HW_REVISION_03 = "2003";
    public static final String SPORTS_WATCH_2_HW_REVISION_04 = "2004";
    public static final String SPORTS_WATCH_2_HW_REVISION_05 = "2005";
    public static final String SPORTS_WATCH_2_HW_REVISION_06 = "2006";
    public static final String SPORTS_WATCH_2_HW_REVISION_07 = "2007";
    public static final String SPORTS_WATCH_2_HW_REVISION_08 = "2008";
    public static final String SPORTS_WATCH_2_HW_REVISION_09 = "2009";
    public static final String SPORTS_WATCH_2_HW_REVISION_10 = "2010";
    public static final String SPORTS_WATCH_3_HW_REVISION_01 = "2012";
    public static final String SPORTS_WATCH_3_HW_REVISION_02 = "2013";
    public static final String SPORTS_WATCH_3_HW_REVISION_03 = "2014";
    public static final String SPORTS_WATCH_3_HW_REVISION_04 = "2015";
    public static final String SPORTS_WATCH_3_HW_REVISION_05 = "2016";
    public static final String SPORTS_WATCH_4_HW_REVISION_01 = "2017";
    public static final String SPORTS_WATCH_4_HW_REVISION_02 = "2018";
    public static final String SPORTS_WATCH_4_HW_REVISION_03 = "2019";
    public static final String SPORTS_WATCH_4_HW_REVISION_04 = "2020";
    public static final String SPORTS_WATCH_4_HW_REVISION_05 = "2021";
    public static final String SPORTS_WATCH_4_HW_REVISION_06 = "2025";
    public static final String SPORTS_WATCH_4_HW_REVISION_07 = "2026";
    public static final String SPORTS_WATCH_HW_REVISION_01 = "1001";
    public static final String SPORTS_WATCH_HW_REVISION_02 = "1002";
    public static final String SPORTS_WATCH_HW_REVISION_03 = "1003";
    public static final String SPORTS_WATCH_HW_REVISION_04 = "1004";
    public static final String SPORTS_WATCH_HW_REVISION_05 = "1005";
    public static final String SPORTS_WATCH_HW_REVISION_06 = "1006";
    public static final String SPORTS_WATCH_HW_REVISION_07 = "1007";
    public static final String SPORTS_WATCH_HW_REVISION_08 = "1008";
    public static final String SPORTS_WATCH_HW_REVISION_09 = "1009";
    public static final String SPORTS_WATCH_HW_REVISION_10 = "1010";
    public static final String SPORTS_WATCH_HW_REVISION_11 = "1011";
    public static final String SPORTS_WATCH_HW_REVISION_BERLIN = "Berlin";
    public static final String SPORTS_WATCH_HW_REVISION_HAMBURG = "Hamburg";
     */

}
