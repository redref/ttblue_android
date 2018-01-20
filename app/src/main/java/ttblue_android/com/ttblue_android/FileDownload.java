package ttblue_android.com.ttblue_android;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class FileDownload {
    private static final String TAG = "FileDownload";

    private URL mUrl;

    public FileDownload(String url) {
        try {
            mUrl = new URL(url);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Malformed URL " + url);
        }
    }

    public byte[] download() throws IOException {
        int count;
        URLConnection conection = mUrl.openConnection();
        conection.connect();
        int lenghtOfFile = conection.getContentLength();
        byte[] result = new byte[lenghtOfFile];
        mUrl.openStream().read(result);
        return result;
    }
}
