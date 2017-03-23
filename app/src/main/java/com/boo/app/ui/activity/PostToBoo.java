package com.boo.app.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.boo.app.App;
import com.boo.app.R;
import com.boo.app.api.GetDataTask;
import com.boo.app.api.OnTaskCompleted;
import com.boo.app.utility.BitmapTransform;
import com.boo.app.utility.LocationTracker;
import com.boo.app.utility.Utility;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

public class PostToBoo extends BaseActivity implements OnTaskCompleted {
    private TextView tvTitle;
    private ImageView ivAvatar, ivPhoto;
    private EditText etText;
    private VideoView vvVideo;

    private ProgressDialog mProgressDialog;
    private MediaController mediaController;

    private String strText = "";
    private String strPhoto = "";
    private String strVideo = "";
    private String strVideoThumb = "";
    private Activity activity;

    private int mediaTypeToPost;

    private Uri mMediaUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_to_boo);

        activity = this;

        findViews();
        init();
    }

    @Override
    public void onBackPressed() {
        onBack(0);
    }

    private void findViews() {
        ivAvatar   = _findViewById(R.id.iv_post_user_avatar);
        ivPhoto    = _findViewById(R.id.iv_post_photo);
        vvVideo    = _findViewById(R.id.vv_post_video);
        etText     = _findViewById(R.id.et_post_text);
        tvTitle    = _findViewById(R.id.tv_post_boo_title);

        ivPhoto.setVisibility(View.INVISIBLE);
        vvVideo.setVisibility(View.INVISIBLE);

//        Set OnClickListeners
        _findViewById(R.id.iv_post_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack(0);
            }
        });

        _findViewById(R.id.iv_post_apply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                post();
            }
        });

//        Set TextChangedListener
        etText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String string = charSequence.toString();
                if ((string.isEmpty())) return;
                String str = string.substring(string.length() - 1);
                if (str.equals("@")) {
                    startActivityForResult(new Intent(PostToBoo.this, TagUser.class), REQUEST_TAG);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void init() {
//        Hide Keyboard
        Utility.utility.setupUI(findViewById(R.id.parent_post_boo), this);

        String title = ((App)getApplication()).getCurrentUser().user_full_name + " is booing at";
        tvTitle.setText(title);

        String url = Utility.utility.getMediaUrl(((App) getApplication()).getCurrentUser().user_photo_url);
        Picasso.with(this).load(url).placeholder(R.drawable.img_no_avatar).fit().centerCrop().into(ivAvatar);

        int type = getIntent().getIntExtra(TYPE, 0);
        Uri uri;
        switch (type) {
            case 1:
                uri = Uri.parse(getIntent().getStringExtra("uri"));
                if (uri != null) {
                    loadPhoto(uri);
                }
                break;
            case 2:
                mMediaUri = Uri.parse(getIntent().getStringExtra("uri"));
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                } else {
                    if (mMediaUri != null) {
                        loadVideo(mMediaUri);
                    }
                }
                break;
            case 3:
                strText = getIntent().getStringExtra(POST_TEXT);
                strPhoto = getIntent().getStringExtra(POST_PHOTO);
                displayPost();
                break;
        }
    }

    private void onBack(int result) {
        Intent returnIntent = new Intent();
        if (result == 0) {
            setResult(Activity.RESULT_CANCELED, returnIntent);
        } else {
            setResult(Activity.RESULT_OK, returnIntent);
        }

        finish();
    }

    private void displayPost() {
        ivPhoto.setVisibility(View.VISIBLE);
        vvVideo.setVisibility(View.INVISIBLE);
        
        etText.setText(strText);
        ivPhoto.setImageBitmap(Utility.base64ToBitmap(strPhoto));
    }

    private void loadPhoto(Uri uri) {
        mediaTypeToPost = 0;
        ivPhoto.setVisibility(View.VISIBLE);
        vvVideo.setVisibility(View.INVISIBLE);

        Log.d(APP_NAME + " : URI", uri.toString());

        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                ivPhoto.setImageBitmap(bitmap);
                strPhoto = Utility.bitmapToBase64(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        ivPhoto.setTag(target);

        Picasso.with(this)
                .load(uri)
                .transform(new BitmapTransform(MAX_WIDTH, MAX_HEIGHT))
                .resize(MAX_WIDTH, MAX_HEIGHT)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .centerInside()
                .into(target);
    }

    private void loadVideo(Uri uri) {
        mediaTypeToPost = 1;
        ivPhoto.setVisibility(View.INVISIBLE);
        vvVideo.setVisibility(View.VISIBLE);

        if (mediaController == null) {
            mediaController = new MediaController(this);
        }

        try {
            vvVideo.setMediaController(mediaController);
            vvVideo.setVideoURI(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }

        vvVideo.requestFocus();
        vvVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mediaPlayer) {
                vvVideo.start();
            }
        });

        Bitmap videoThumb = Utility.utility.createThumbnailFromVideoUri(uri);
        if (videoThumb != null) {
            strVideoThumb = Utility.bitmapToBase64(videoThumb);
        } else {
            strVideoThumb = "";
        }

        Log.d(APP_NAME, strVideoThumb);
        strVideo = Utility.utility.videoToBase64(uri, this);
    }

    private void post() {
        strText = etText.getText().toString().trim();

        if (strText.isEmpty() && strPhoto.isEmpty() && strVideo.isEmpty()) {
            return;
        }

        String url = URL_SERVER + URL_MANAGE_MY_POSTS;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(USER_ID, ((App)getApplication()).getCurrentUser().user_id);
            jsonRequest.put(MANAGE_TYPE, "add");

            LocationTracker mLocationTracker = new LocationTracker(this);
            double latitude  = mLocationTracker.getLatitude();
            double longitude = mLocationTracker.getLongitude();

            jsonRequest.put(POST_LOCATION_LATITUDE, latitude);
            jsonRequest.put(POST_LOCATION_LONGITUDE, longitude);

            if (!strText.isEmpty()) {
                jsonRequest.put(POST_TEXT, strText);
            }

            if (mediaTypeToPost == 0) {
                jsonRequest.put(POST_PHOTO, strPhoto);
            } else {
                jsonRequest.put(POST_VIDEO, strVideo);
                jsonRequest.put(POST_VIDEO_THUMB, strVideoThumb);
            }

            mProgressDialog = new ProgressDialog(this, R.style.ProgressDialogTheme);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Posting to Boo");
            mProgressDialog.show();


            new GetDataTask(url, jsonRequest, this).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTaskSuccess(JSONObject jsonResponse) {
        mProgressDialog.dismiss();
        onBack(1);
    }

    @Override
    public void onTaskError(String msg) {
        mProgressDialog.dismiss();
        Utility.showToast(this, msg);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadVideo(mMediaUri);
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TAG) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra(USER_NAME);
                etText.append(result);
            } else {
                String str1 = etText.getText().toString();
                String str2 = str1.substring(0, str1.length() - 1);
                etText.setText(str2);
            }
        }
    }
}
