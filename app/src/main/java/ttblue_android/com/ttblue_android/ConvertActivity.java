package ttblue_android.com.ttblue_android;

import android.app.Activity;
import android.os.Bundle;

public class ConvertActivity extends Activity {
    private static final String TAG = "ConvertActivity";
    private TtblueApplication mApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mApplication = (TtblueApplication) getApplicationContext();
    }
}
