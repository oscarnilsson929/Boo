package com.boo.app.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.boo.app.App;
import com.boo.app.R;
import com.boo.app.api.GetDataTask;
import com.boo.app.api.OnTaskCompleted;
import com.boo.app.model.UserObject;
import com.boo.app.utility.LocationTracker;
import com.boo.app.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import mehdi.sakout.fancybuttons.FancyButton;

public class Login extends SocialActivity implements View.OnClickListener, OnTaskCompleted {
    private FancyButton mFacebookBtn, mTwitterBtn, mEmailBtn, mSignUpBtn;
    private boolean isFacebook;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        initUI();
    }

    private void initUI() {
        mFacebookBtn = _findViewById(R.id.btn_login_facebook);
        mTwitterBtn  = _findViewById(R.id.btn_login_twitter);
        mEmailBtn    = _findViewById(R.id.btn_login_email);
        mSignUpBtn   = _findViewById(R.id.btn_login_signup);

        mFacebookBtn.setOnClickListener(this);
        mTwitterBtn.setOnClickListener(this);
        mEmailBtn.setOnClickListener(this);
        mSignUpBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mFacebookBtn)) {
            isFacebook = true;
            facebookLogIn();
        }

        if (v.equals(mTwitterBtn)) {
            isFacebook = false;
            twitterLogin();
        }

        if (v.equals(mEmailBtn)) {
            startActivity(new Intent(this, EmailLogin.class));
            finish();
        }
        if (v.equals(mSignUpBtn)) {
            Intent intent = new Intent(this, SignUp.class);
            intent.putExtra("from", 1);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, Welcome.class));
        finish();
    }

    //    *************************** SocialActivity ***************************
    @Override
    public void onDataReady(JSONObject data) {
        Log.d(APP_NAME + " : Social Data", data.toString());

        String url = URL_SERVER + URL_SOCIAL_SIGNUP;
        try {
            data.put(SIGNUP_MODE, isFacebook ? 1 : 2);

            LocationTracker mLocationTracker = new LocationTracker(this);
            double latitude  = mLocationTracker.getLatitude();
            double longitude = mLocationTracker.getLongitude();

            data.put(USER_LOCATION_LATITUDE,  latitude);
            data.put(USER_LOCATION_LONGITUDE, longitude);

            String gcmId = ((App) getApplication()).getGcm_id();
            if (!gcmId.isEmpty()) {
                data.put(USER_GCM_ID, gcmId);
            }

            mProgressDialog.setMessage(isFacebook? "Login with Facebook...": "Login with Twitter...");
            mProgressDialog.show();

            new GetDataTask(url, data, this).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    *************************** OnTaskCompleted ***************************
    @Override
    public void onTaskSuccess(JSONObject jsonResponse) {
        mProgressDialog.dismiss();
        try {
            UserObject current_user = new UserObject(jsonResponse.getJSONObject(CURRENT_USER));
            current_user.saveOnDisk(this);
            ((App) getApplication()).setCurrentUser(current_user);
            startActivity(new Intent(this, Main.class));
            finish();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTaskError(String msg) {
        mProgressDialog.dismiss();
        Utility.showToast(this, msg);
    }
}
