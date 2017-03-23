package com.boo.app.utility;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.boo.app.AppConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileUtility {
    public static File createImageTempFile(Context context) throws IOException {
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(storageDir, "temp.jpg");
    }

    public static File createVideoTempFile(Context context) throws IOException {
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        return new File(storageDir, "temp.mp4");
    }

    public static File createVideoFile(Context context, String fileName) throws IOException {
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        return new File(storageDir, fileName);
    }

    public static Uri downloadVideoFile(Context context, String fileURL, String fileName) {
        Uri uri = null;
        try {
            Log.d(AppConfig.APP_NAME, "Download : " + fileName);
            Log.d(AppConfig.APP_NAME, "Download : " + fileURL);

            File file = createVideoFile(context, fileName);

            URL u = new URL(fileURL);
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();
            FileOutputStream f = new FileOutputStream(file);
            InputStream in = c.getInputStream();
            byte[] buffer = new byte[1024];
            int len1;

            while ((len1 = in.read(buffer)) > 0) {
                f.write(buffer, 0, len1);
            }
            f.close();

            uri = Uri.fromFile(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return uri;
    }

    public static Uri searchVideoFile(Context context, String file) {
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);

        if (storageDir != null) {
            for (File savedFile : storageDir.listFiles()) {
                if (savedFile.getName().equals(file)) {
                    return Uri.fromFile(savedFile);
                }
            }
        }

        return null;
    }
}
