package ttblue_android.com.ttblue_android;

public interface ServiceCallbacks {
    void startOperation();
    void retryOperation();
    void displayToUser(String msg);
    void disconnectGatt();
    void showPinPrompt();
}
