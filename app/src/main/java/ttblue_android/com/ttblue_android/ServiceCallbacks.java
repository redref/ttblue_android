package ttblue_android.com.ttblue_android;

import java.io.File;

public interface ServiceCallbacks {
    void startOperation();
    void retryOperation();
    void displayToUser(String msg);
    void disconnectGatt();
    void showPinPrompt();
    String getExternalFilesDir();
}
