package com.boo.app.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.boo.app.App;
import com.boo.app.R;
import com.boo.app.gcm.RegistrationIntentService;
import com.boo.app.model.UserObject;
import com.boo.app.utility.Utility;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class Splash extends BaseActivity {

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SharedPreferences prefs = getSharedPreferences(APP_NAME, MODE_PRIVATE);
        String token = prefs.getString(USER_GCM_ID, "");

        if (!token.isEmpty()) {
            Log.d(APP_NAME, "SharedPreferences : " + token);
            saveToken(token);
            onStartSplash();
        } else {
            mRegistrationBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().endsWith(RegistrationIntentService.REGISTRATION_SUCCESS)) {
                        String token = intent.getStringExtra("token");
                        Log.d(APP_NAME, "BroadcastReceiver : " + token);
                        saveToken(token);
                    } else if (intent.getAction().equals(RegistrationIntentService.REGISTRATION_ERROR)) {
                        saveToken("");
                    }
                    onStartSplash();
                }
            };

            if (isGooglePlayServicesAvailable()) {
                //Start service
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            } else {
                saveToken("");
                onStartSplash();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(RegistrationIntentService.REGISTRATION_SUCCESS));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(RegistrationIntentService.REGISTRATION_ERROR));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    private void onStartSplash() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                UserObject oldUser = UserObject.loadFromDisk(Splash.this);
                if (oldUser != null) {
                    ((App) getApplication()).setCurrentUser(oldUser);
                    startActivity(new Intent(Splash.this, Main.class));
                    Splash.this.finish();
                } else {
                    Intent intent = new Intent(Splash.this, Welcome.class);
                    startActivity(intent);
                    Splash.this.finish();
                }
            }
        }, SPLASH_TIME_OUT);
    }

    //Check status of Google play service in device
    private boolean isGooglePlayServicesAvailable() {
        int requestCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS != requestCode) {
            //Check type of error
            if (GoogleApiAvailability.getInstance().isUserResolvableError(requestCode)) {
                Utility.showToast(this, "Google Play Service is not install/enabled in this device!");
                //So notification
                GoogleApiAvailability.getInstance().showErrorNotification(this, requestCode);
            } else {
                Utility.showToast(this, "This device does not support for Google Play Service!");
            }
            return false;
        }
        return true;
    }

    private void saveToken(String token){
        SharedPreferences.Editor editor = getSharedPreferences(APP_NAME, MODE_PRIVATE).edit();
        editor.putString(USER_GCM_ID, token);
        editor.apply();

        ((App)getApplication()).setGcm_id(token);
    }
}
