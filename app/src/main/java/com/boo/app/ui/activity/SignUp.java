package com.boo.app.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

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

public class SignUp extends BaseActivity implements OnTaskCompleted{
    private LinearLayout btnBack;
    private EditText etUsername, etFirstName, etLastName, etEmail, etPassword, etConfirmPassword;
    private FancyButton btnSignUp;

    private int mFrom;
    private String strUsername, strFirstName, strLastName, strEmail, strPassword;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        findViews();
        init();
    }

    private void findViews() {
        btnBack           = _findViewById(R.id.sign_up_back);
        etUsername        = _findViewById(R.id.et_sign_up_username);
        etFirstName       = _findViewById(R.id.et_sign_up_first_name);
        etLastName        = _findViewById(R.id.et_sign_up_last_name);
        etEmail           = _findViewById(R.id.et_sign_up_email);
        etPassword        = _findViewById(R.id.et_sign_up_password);
        etConfirmPassword = _findViewById(R.id.et_sign_up_confirm_password);
        btnSignUp         = _findViewById(R.id.btn_signup);
    }

    private void init() {
//        Hide Keyboard
        Utility.utility.setupUI(findViewById(R.id.parent_sign_up), this);

        mFrom = getIntent().getIntExtra("from", 0);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignUp();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onBack();
    }

    private void onBack() {
        switch (mFrom) {
            case 0:
                startActivity(new Intent(this, Welcome.class));
                break;
            default:
                startActivity(new Intent(this, Login.class));
                break;
        }
        finish();
    }

    private void onSignUp() {
        if (validUserInfo()) {
            strPassword = Utility.utility.md5(strPassword);
            String url = URL_SERVER + URL_MANUAL_SIGNUP;
            JSONObject jsonRequest = new JSONObject();
            try {
                jsonRequest.put(USER_NAME, strUsername);
                jsonRequest.put(USER_FIRST_NAME, strFirstName);
                jsonRequest.put(USER_LAST_NAME, strLastName);
                jsonRequest.put(USER_EMAIL, strEmail);
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

                mProgressDialog.setMessage("Sign Up");
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
        boolean valid  = true;
        View focusView = null;
        String strConfirmPassword;

        etUsername       .setError(null);
        etFirstName.setError(null);
        etLastName.setError(null);
        etEmail          .setError(null);
        etPassword       .setError(null);
        etConfirmPassword.setError(null);

        strUsername        = etUsername       .getText().toString().trim();
        strFirstName = etFirstName.getText().toString().trim();
        strLastName = etLastName.getText().toString().trim();
        strEmail           = etEmail          .getText().toString().trim();
        strPassword        = etPassword       .getText().toString().trim();
        strConfirmPassword = etConfirmPassword.getText().toString().trim();

        if (strUsername.isEmpty()) {
            etUsername.setError(getString(R.string.error_field_required));
            focusView = etUsername;
            valid = false;
        } else if (strFirstName.isEmpty()) {
            etFirstName.setError(getString(R.string.error_field_required));
            focusView = etFirstName;
            valid = false;
        } else if (strLastName.isEmpty()) {
            etLastName.setError(getString(R.string.error_field_required));
            focusView = etLastName;
            valid = false;
        } else if (!Utility.utility.isValidEmail(strEmail)) {
            etEmail.setError(getString(R.string.error_valid_email_required));
            focusView = etEmail;
            valid = false;
        } else if (strPassword.isEmpty()) {
            etPassword.setError(getString(R.string.error_field_required));
            focusView = etPassword;
            valid = false;
        } else if (!strConfirmPassword.equals(strPassword)) {
            etConfirmPassword.setText("");
            etConfirmPassword.setError(getString(R.string.error_matched_password_required));
            focusView = etConfirmPassword;
            valid = false;
        }

        if (!valid) {
            focusView.requestFocus();
        }

        return valid;
    }
}
