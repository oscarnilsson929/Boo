package com.boo.app;

import android.app.Application;
import android.os.Bundle;

import com.boo.app.model.UserObject;
import com.facebook.FacebookSdk;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

public class App extends Application {
    private static final String TWITTER_KEY = "cL8iyT4B1xoOStqiv2GUZFyin";
    private static final String TWITTER_SECRET = "luPZuEaqRpPcWlAocnrDhx37LyvWSFJFmPkPT5Zqspc8rx20jq";

    private UserObject currentUser = null;
    private String gcm_id = "";
    public static boolean receivedNotification = false;

    public static boolean isLoading = false;

    @Override
    public void onCreate() {
        super.onCreate();

        FacebookSdk.sdkInitialize(this);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
    }

    public String getGcm_id() {
        return gcm_id;
    }

    public void setGcm_id(String gcm_id) {
        this.gcm_id = gcm_id;
    }

    public UserObject getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(UserObject currentUser) {
        this.currentUser = currentUser;
    }
}


