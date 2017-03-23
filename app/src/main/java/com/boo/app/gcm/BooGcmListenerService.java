package com.boo.app.gcm;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.boo.app.App;
import com.boo.app.AppConfig;
import com.boo.app.R;
import com.boo.app.ui.activity.Main;
import com.boo.app.ui.activity.Splash;
import com.google.android.gms.gcm.GcmListenerService;

import java.util.List;

public class BooGcmListenerService extends GcmListenerService implements AppConfig {
    public static int gcm_count = 0;

    private static CountListener mCountListener;

    @Override
    public void onMessageReceived(String s, Bundle bundle) {
        String message = bundle.getString("message");
        App.receivedNotification = true;

        sendNotification(message);
    }

    private void sendNotification(String message) {
        gcm_count++;
        Log.d(APP_NAME, "Count : " + gcm_count);

        boolean isForeground = false;

        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            isForeground = false;
        } else {
            String packageName = getPackageName();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                    isForeground = true;
                }
            }
        }

        Intent intent;
        if (isForeground) {
            Log.d(APP_NAME, "Boo is running in foreground : " + gcm_count);
            if (mCountListener != null) {
                mCountListener.getCount(gcm_count);
            }
            intent = new Intent(this, Main.class);
            intent.putExtra(FROM, GCM_LISTENER);
        } else {
            Log.d(APP_NAME, "Boo is running in background : " + gcm_count);
            intent = new Intent(this, Splash.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int requestCode = 0; // Your request code
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);
        //Setup notification
        //Sound
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        //Build notification
        NotificationCompat.Builder noBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Boo")
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSound(sound);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, noBuilder.build()); //0 = ID of notification
    }

    public interface CountListener {
        void getCount(int count);
    }

    public void registerListener(CountListener listener) {
        mCountListener = listener;
    }
}
