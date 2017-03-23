package com.boo.app.utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.boo.app.AppConfig;
import com.boo.app.api.HttpUrlManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utility implements HttpUrlManager, AppConfig{

    public static Utility utility = new Utility();

//    Interface Process
    @SuppressWarnings("ConstantConditions")
    public void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void setupUI(View view, final Activity activity) {
        if(!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(activity);
                    return false;
                }
            });
        }
        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView, activity);
            }
        }
    }

    public Boolean isValidEmail(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

//    Get Device Screen Size
    public Point getDeviceScreenSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

//    Bitmap Process
    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public static String compressBitmap(Bitmap bitmap) {
        int width, height, scale;
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        if (width > height) {
            scale = (int) (MAX_WIDTH * 100.0 / width);
        } else {
            scale = (int) (MAX_HEIGHT * 100.0 / height);
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, scale, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public static Bitmap base64ToBitmap(String b64) {
        byte[] imageAsBytes = Base64.decode(b64.getBytes(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }

    public String videoToBase64(Uri uri, Context context){
        String result = "";
        try {
            InputStream iStream = context.getContentResolver().openInputStream(uri);
            byte[] inputData = getBytes(iStream);
            result = Base64.encodeToString(inputData, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public Bitmap createThumbnailFromVideoUri(Uri uri) {
        return ThumbnailUtils.createVideoThumbnail(uri.getPath(), MediaStore.Images.Thumbnails.MINI_KIND);
    }

    public File base64ToVideo(String encodedString) {
        byte[] decodedBytes = Base64.decode(encodedString.getBytes(), Base64.DEFAULT);
        File file = null;
        try {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "KamiwazaMedia/tempVideo.mp4");
            FileOutputStream out = new FileOutputStream(file);
            out.write(decodedBytes);
            out.close();
        } catch (Exception e) {
            Log.e("Error", e.toString());

        }

        return file;
    }

//    Find View By Tag
    public View getViewByTag(ViewGroup root, String tag){
        int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                View view = getViewByTag((ViewGroup) child, tag);
                if (view != null) {
                    return view;
                }
            }

            Object object = child.getTag();
            if (object != null) {
                String tagObj = String.valueOf(object);
                if (tagObj.equals(tag)) {
                    return child;
                }
            }
        }

        return null;
    }

    public String md5(String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getMediaUrl(String url) {
        if (url.startsWith("bo_media_user")) {
            url = url.replace("bo_media_user_", "");
            return URL_SERVER + MEDIA_USERS + url;
        }

        if(url.startsWith("bo")){
            url=url.replace("bo_","");
            String[] params=url.split("_");
            if(params[0].startsWith("avatar")){
                return URL_SERVER + MEDIA_USERS + params[1];
            }
            else {
                StringBuilder sb = new StringBuilder();

                int startIndex = 2;

                if (url.contains("thumb"))
                    startIndex = 3;

                for (int i = startIndex; i < params.length; i++) {
                    sb.append(params[i]);
                    if (i != params.length - 1) {
                        sb.append("_");
                    }
                }
                String path=null;
                switch (params[1]){
                    case "photo":
                        path = MEDIA_PHOTOS;
                        break;
                    case "video":
                        path = MEDIA_VIDEOS;
                        break;
                }

                if (url.contains("thumb"))
                    path = MEDIA_VIDEO_THUMB;

                url = URL_SERVER + path + sb.toString();
            }
        }
        return url;
    }

    public String getTimeDiff(long time_diff) {
        String result;
        int months, days, hours, minutes, seconds;
        seconds = (int) (time_diff % 60);
        time_diff = (time_diff - seconds) / 60;
        minutes = (int) (time_diff % 60);
        time_diff = (time_diff - minutes) / 60;
        hours = (int) (time_diff % 24);
        time_diff = (time_diff - hours) / 24;
        days = (int) (time_diff % 30);
        time_diff = (time_diff - days) / 30;
        months = (int) time_diff;

        if (months > 0) {
            result = String.valueOf(months) + "month" + (months > 1 ? "s":"") + " ago";
        } else if (days > 0) {
            result = String.valueOf(days) + "d " + String.valueOf(hours) + "h";
        } else if (hours > 0) {
            result = String.valueOf(hours) + "h " + String.valueOf(minutes) + "m";
        } else if (minutes > 0) {
            result = String.valueOf(minutes) + "m";
        } else {
            result = "1m";
        }

        return result;
    }
}
