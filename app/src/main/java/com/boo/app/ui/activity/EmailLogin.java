package com.boo.app.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.boo.app.App;
import com.boo.app.R;
import com.boo.app.api.GetDataTask;
import com.boo.app.api.OnTaskCompleted;
import com.boo.app.model.UserObject;
import com.boo.app.utility.LocationTracker;
import com.boo.app.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

public class EmailLogin extends BaseActivity implements OnTaskCompleted{
    private EditText etUsername, etPassword;

    private String strUsername, strPassword;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        initUI();
    }

    private void initUI() {
        Utility.utility.setupUI(findViewById(R.id.parent_email_login), this);

        etUsername = _findViewById(R.id.et_email_login_username);
        etPassword = _findViewById(R.id.et_email_login_password);

        _findViewById(R.id.email_login_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
            }
        });
        _findViewById(R.id.btn_email_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLogin();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onBack();
    }

    private void onBack() {
        startActivity(new Intent(this, Login.class));
        finish();
    }

    private void onLogin() {
        if (validUserInfo()) {
            strPassword = Utility.utility.md5(strPassword);
            String url = URL_SERVER + URL_MANUAL_LOGIN;
            JSONObject jsonRequest = new JSONObject();
            try {
                jsonRequest.put(USER_EMAIL, strUsername);
                jsonRequest.put(USER_PASSWORD, strPassword);

                LocationTracker mLocationTracker = new LocationTracker(this);
                double latitude  = mLocationTracker.getLatitude();
                double longitude = mLocationTracker.getLongitude();

                jsonRequest.put(USER_LOCATION_LATITUDE,  latitude);
                jsonRequest.put(USER_LOCATION_LONGITUDE, longitude);

                String gcmId = ((App) getApplication()).getGcm_id();
                if (!gcmId.isEmpty()) {
                    jsonRequest.put(USER_GCM_ID, gcmId);
                }

                mProgressDialog.setMessage("Login...");
                mProgressDialog.show();

                new GetDataTask(url, jsonRequest, this).execute();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

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

    //    Check User Info
    private boolean validUserInfo() {
        boolean valid = true;
        View focusView = null;

        etUsername.setError(null);
        etPassword.setError(null);
        strUsername = etUsername.getText().toString().trim();
        strPassword = etPassword.getText().toString().trim();

        if (strUsername.isEmpty()) {
            etUsername.setError(getString(R.string.error_field_required));
            focusView = etUsername;
            valid = false;
        }
        else if (strPassword.isEmpty()) {
            etPassword.setError(getString(R.string.error_field_required));
            focusView = etPassword;
            valid = false;
        }

        if (!valid) {
            focusView.requestFocus();
        }

        return valid;
    }
}
