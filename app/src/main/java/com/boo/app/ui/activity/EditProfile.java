package com.boo.app.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.boo.app.App;
import com.boo.app.R;
import com.boo.app.api.GetDataTask;
import com.boo.app.api.OnTaskCompleted;
import com.boo.app.model.UserObject;
import com.boo.app.utility.BitmapTransform;
import com.boo.app.utility.FileUtility;
import com.boo.app.utility.LocationTracker;
import com.boo.app.utility.Utility;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import mehdi.sakout.fancybuttons.FancyButton;

public class EditProfile extends BaseActivity implements OnTaskCompleted{
    private ImageView ivAvatar, ivBack, ivApply;
    private EditText etUsername, etFullName, etEmail, etBio;

    private String strUsername, strFullName, strEmail, strBio, strAvatar;

    private ProgressDialog mProgressDialog;

    private Uri mMediaUri;

    private UserObject currentUser;

    private boolean isPhotoUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        findViews();
    }

    @Override
    public void onBackPressed() {
        onBack();
    }

    private void findViews() {
        ivAvatar = _findViewById(R.id.iv_edit_profile_avatar);
        ivBack  = _findViewById(R.id.iv_edit_profile_back);
        ivApply = _findViewById(R.id.iv_edit_profile_apply);

        etUsername = _findViewById(R.id.et_edit_profile_username);
        etFullName = _findViewById(R.id.et_edit_profile_full_name);
        etEmail = _findViewById(R.id.et_edit_profile_email);
        etBio = _findViewById(R.id.et_edit_profile_bio);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Profile update...");
        mProgressDialog.setCancelable(false);

        init();
    }

    private void init() {
        ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAvatar();
            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBack();
            }
        });

        ivApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onApply();
            }
        });

        currentUser = ((App)getApplication()).getCurrentUser();

        strUsername = currentUser.user_name;
        strFullName = currentUser.user_full_name;
        strEmail = currentUser.user_email;
        strBio = currentUser.user_bio;
        strAvatar = "";

        etUsername.setText(strUsername);
        etFullName.setText(strFullName);
        etEmail.setText(strEmail);
        etBio.setText(strBio);

        String url = Utility.utility.getMediaUrl(currentUser.user_photo_url);
        Picasso.with(this).load(url).placeholder(R.drawable.img_no_avatar).fit().centerCrop().into(ivAvatar);
    }

    private void onAvatar() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_choose_media);
        Button btnLibrary = (Button)dialog.findViewById(R.id.btn_dialog_choose_media_Library);
        Button btnCamera  = (Button)dialog.findViewById(R.id.btn_dialog_choose_media_camera);

        btnLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_PHOTO_GALLERY);
                dialog.dismiss();
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(EditProfile.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(EditProfile.this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_PHOTO_CAMERA);
                } else {
                    openPhotoCamera();
                }

                dialog.dismiss();
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialog.getWindow().setAttributes(lp);

        dialog.show();
    }

    //    ********************************* Open Camera *********************************
    private void openPhotoCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = FileUtility.createImageTempFile(this);
                mMediaUri = Uri.fromFile(photoFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                startActivityForResult(takePictureIntent, PICK_PHOTO_CAMERA);
            }
        }
    }

    private void onBack() {
        finish();
    }

    private void onApply() {
        isPhotoUpdate = false;

        if (!strAvatar.isEmpty()) {
            user_profile_photo_update();
        } else {
            user_profile_update();
        }
    }

    private void user_profile_photo_update() {
        isPhotoUpdate = true;

        String url = URL_SERVER + URL_USER_PROFILE_PHOTO_UPDATE;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(USER_ID, currentUser.user_id);
            jsonRequest.put(USER_PHOTO, strAvatar);

            mProgressDialog.show();

            new GetDataTask(url, jsonRequest, this).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void user_profile_update() {
        isPhotoUpdate = false;

        if (validUserInfo()) {
            String url = URL_SERVER + URL_USER_PROFILE_UPDATE;
            JSONObject jsonRequest = new JSONObject();
            try {
                jsonRequest.put(USER_ID, ((App)getApplication()).getCurrentUser().user_id);
                jsonRequest.put(USER_NAME, strUsername);
                jsonRequest.put(USER_EMAIL, strEmail);
                jsonRequest.put(USER_FULL_NAME, strFullName);
                jsonRequest.put(USER_BIO, strBio);

                LocationTracker mLocationTracker = new LocationTracker(this);
                double latitude  = mLocationTracker.getLatitude();
                double longitude = mLocationTracker.getLongitude();

                jsonRequest.put(USER_LOCATION_LATITUDE, latitude);
                jsonRequest.put(USER_LOCATION_LONGITUDE, longitude);

                if (!mProgressDialog.isShowing())
                    mProgressDialog.show();

                new GetDataTask(url, jsonRequest, this).execute();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //    Check User Info
    private boolean validUserInfo() {
        boolean valid = true;
        View focusView = null;

        etUsername.setError(null);
        etEmail.setError(null);

        strUsername = etUsername.getText().toString().trim();
        strFullName = etFullName.getText().toString().trim();
        strEmail    = etEmail.getText().toString().trim();
        strBio      = etBio.getText().toString().trim();

        if (strUsername.isEmpty()) {
            etUsername.setError(getString(R.string.error_field_required));
            focusView = etUsername;
            valid = false;
        } else if (!Utility.utility.isValidEmail(strEmail)) {
            etEmail.setError(getString(R.string.error_valid_email_required));
            focusView = etEmail;
            valid = false;
        }

        if (!valid) {
            focusView.requestFocus();
        }

        return valid;
    }

    @Override
    public void onTaskSuccess(JSONObject jsonResponse) {
        mProgressDialog.dismiss();

        if (isPhotoUpdate) {
            user_profile_update();
        } else {
            try {
                UserObject current_user = new UserObject(jsonResponse.getJSONObject(CURRENT_USER));
                current_user.saveOnDisk(this);
                ((App) getApplication()).setCurrentUser(current_user);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onTaskError(String msg) {
        mProgressDialog.dismiss();
        Utility.showToast(this, msg);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_PHOTO_GALLERY || requestCode == PICK_PHOTO_CAMERA) {
                if (requestCode == PICK_PHOTO_GALLERY) {
                    mMediaUri = data.getData();
                }

                Target target = new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        strAvatar = Utility.bitmapToBase64(bitmap);
                        ivAvatar.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                };

                ivAvatar.setTag(target);

                Picasso.with(this)
                        .load(mMediaUri)
                        .resize(MAX_WIDTH / 4, MAX_HEIGHT / 4)
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                        .centerCrop()
                        .into(target);

                Log.d("URI", mMediaUri.toString());
            }
        }
    }

    //    ********************************* Request Permissions Result *********************************
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == PERMISSIONS_REQUEST_PHOTO_CAMERA)
                openPhotoCamera();
        }
    }
}
