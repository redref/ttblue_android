package ttblue_android.com.ttblue_android;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import javax.annotation.Nonnull;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private TtblueApplication mApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mApplication = (TtblueApplication) getApplicationContext();

        askPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Resume");
        TextView saved_device = (TextView)findViewById(R.id.saved_device);
        saved_device.setText(mApplication.getPreferenceString("saved_device"));
        TextView saved_code = (TextView)findViewById(R.id.saved_code);
        saved_code.setText(mApplication.getPreferenceString("saved_code"));

        Button gatt = (Button)findViewById(R.id.start_gatt);
        if (saved_device.getText().toString().equals("")) {
            gatt.setEnabled(false);
        } else {
            gatt.setEnabled(true);
        }
    }

    private void askPermissions() {
        try {
            String[] permissions = this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions;
            for (String permission : permissions) {
                if (this.getPackageManager().checkPermission(permission, this.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                    this.requestPermissions(permissions, 666);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("This should have never happened.", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @Nonnull String[] permissions, @Nonnull int[] grantResults) {
        switch (requestCode) {
            case 666:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    Log.i(TAG, "Permissions granted");
                } else {
                    // notgranted - re-ask
                    askPermissions();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void startScan(View view) {
        Intent intent = new Intent(MainActivity.this, ScanActivity.class);
        startActivity(intent);
    }

    public void startGatt(View view) {
        Intent intent = new Intent(MainActivity.this, GattActivity.class);
        startActivity(intent);
    }

    public void startConvert(View view) {
        Intent intent = new Intent(MainActivity.this, ConvertActivity.class);
        startActivity(intent);
    }
}
