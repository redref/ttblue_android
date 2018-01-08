package ttblue_android.com.ttblue_android;

import android.util.Log;

import com.google.common.io.ByteStreams;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileHandler {
    public static final String TAG = "FileHandler";

    public static final int READ_MODE = 1;
    public static final int WRITE_MODE = 2;
    private File mFile;
    private byte[] mLastPacket;
    private int mMode;
    private InputStream mReadStream;
    private OutputStream mWriteStream;

    public static FileHandler createFileWriter(String path, String fileName) {
        FileHandler fileHandler = new FileHandler();
        if (fileHandler != null) {
            fileHandler.mMode = 2;
            if (fileHandler.openFile(path, fileName)) {
                return fileHandler;
            }
            Log.e(TAG, "createFileWriter failed to open file " + fileName + "in path " + path);
            return null;
        }
        Log.e(TAG, "createFileWriter failed to instantiate FileHandler");
        return null;
    }

    public static FileHandler createFileReader(String filePath) {
        FileHandler fileHandler = new FileHandler();
        if (fileHandler != null) {
            fileHandler.mMode = 1;
            if (fileHandler.openFile(filePath)) {
                return fileHandler;
            }
            Log.e(TAG, "createFileReader failed to open file " + filePath);
            return null;
        }
        Log.e(TAG, "createFileReader failed to instantiate FileHandler");
        return null;
    }

    public static FileHandler createFileReader(File file) {
        FileHandler fileHandler = new FileHandler();
        if (fileHandler != null) {
            fileHandler.mMode = 1;
            fileHandler.mFile = file;
            Log.i(TAG, "Opening file " + file.getName() + " for read");
            if (fileHandler.openReadStream()) {
                return fileHandler;
            }
            Log.e(TAG, "createFileReader failed to open read stream " + file.getName());
            return null;
        }
        Log.e(TAG, "createFileReader failed to instantiate FileHandler");
        return null;
    }

    private FileHandler() {
    }

    private boolean openStream() {
        if (this.mMode == 1) {
            Log.d(TAG, "Opening read stream.");
            return openReadStream();
        }
        Log.d(TAG, "Opening write stream.");
        return openWriteStream();
    }

    private boolean openReadStream() {
        try {
            Log.i(TAG, "Opening " + this.mFile.getName() + " for read. File length: " + this.mFile.length());
            this.mReadStream = new BufferedInputStream(new FileInputStream(this.mFile));
            return true;
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
    }

    private boolean openWriteStream() {
        this.mWriteStream = null;
        try {
            Log.i(TAG, "Opening " + this.mFile.getName() + " for write. File length: " + this.mFile.length());
            this.mFile.getParentFile().mkdirs();
            this.mWriteStream = new BufferedOutputStream(new FileOutputStream(this.mFile));
            return true;
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
    }

    public void writeData(byte[] data) throws IOException {
        this.mWriteStream.write(data, 0, data.length);
        this.mWriteStream.flush();
    }

    public byte[] readData(int length) throws IOException {
        byte[] data = new byte[length];
        ByteStreams.read(this.mReadStream, data, 0, length);
        this.mLastPacket = data;
        return data;
    }

    public boolean mark(int readlimit) {
        if (this.mReadStream.markSupported()) {
            Log.d(TAG, "markData");
            this.mReadStream.mark(readlimit);
            return true;
        }
        Log.e(TAG, "Mark not supported");
        return false;
    }

    public boolean resetToMark() throws IOException {
        if (this.mReadStream.markSupported()) {
            Log.d(TAG, "resetToMark");
            this.mReadStream.reset();
            return true;
        }
        Log.e(TAG, "Mark not supported");
        return false;
    }

    public byte[] getFileBytes() {
        int len = (int) this.mFile.length();
        byte[] fileBytes = new byte[len];
        try {
            int readLen = ByteStreams.read(this.mReadStream, fileBytes, 0, len);
            if (readLen == len) {
                return fileBytes;
            }
            throw new RuntimeException("Couldn't read complete file. File len: " + len + " Read len: " + readLen);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    public byte[] rereadData() {
        return this.mLastPacket;
    }

    public long getFileLength() {
        return this.mFile.length();
    }

    public byte[] getFileLengthByteArray() {
        int fileLength = (int) this.mFile.length();
        return new byte[]{(byte) (fileLength & 255), (byte) ((fileLength >> 8) & 255), (byte) ((fileLength >> 16) & 255), (byte) ((fileLength >> 24) & 255)};
    }

    public boolean openFile(String filePath) {
        Log.i(TAG, "Opening file " + filePath);
        this.mFile = new File(filePath);
        return openStream();
    }

    public boolean openFile(String path, String fileName) {
        this.mFile = new File(path, fileName);
        if (this.mFile != null) {
            return openStream();
        }
        Log.e(TAG, "File is NULL!");
        return false;
    }

    public File getFile() {
        return this.mFile;
    }

    public void finish() {
        closeStream();
        this.mFile = null;
    }

    private void closeStream() {
        try {
            if (this.mMode == 2) {
                Log.i(TAG, "Closing write stream.");
                this.mWriteStream.close();
                return;
            }
            Log.i(TAG, "Closing read stream.");
            this.mReadStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Error closing stream!");
            e.printStackTrace();
        }
    }
}
