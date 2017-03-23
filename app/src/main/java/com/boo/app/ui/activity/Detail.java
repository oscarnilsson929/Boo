
package com.boo.app.ui.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.boo.app.App;
import com.boo.app.R;
import com.boo.app.api.GetDataTask;
import com.boo.app.api.OnTaskCompleted;
import com.boo.app.library.hashtag.annotation.SocialActionIntDef;
import com.boo.app.library.hashtag.util.SocialActionCallback;
import com.boo.app.library.hashtag.util.SocialActionType;
import com.boo.app.library.hashtag.widget.SocialTextView;
import com.boo.app.model.CommentObject;
import com.boo.app.model.PostObject;
import com.boo.app.model.UserObject;
import com.boo.app.ui.fragment.BootiqueFragment;
import com.boo.app.utility.FileUtility;
import com.boo.app.utility.Utility;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class Detail extends BaseActivity implements OnTaskCompleted, OnClickListener{

    private RelativeLayout mContainer;
    private ImageView ivAvatar, ivPhoto, ivPlay, ivBooToo, ivBack;
    private TextView tvName, tvTime, tvBooToo, tvComment, tvShare;
    private SocialTextView tvText;
    private VideoView vvVideo;
    private LinearLayout btnBooToo, btnComment, btnShare;

    private PostObject mPost;
    private ArrayList<CommentObject> mComments;
    private long post_id;
    private UserObject currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initData();
        initUI();
    }

    private void initData() {
        post_id = getIntent().getLongExtra(POST_ID, 0);
        currentUser = ((App)getApplication()).getCurrentUser();

        String url = URL_SERVER + URL_GET_POST;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(USER_ID, currentUser.user_id);
            jsonRequest.put(POST_ID, post_id);

            new GetDataTask(url, jsonRequest, this).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initUI() {
        mContainer  = _findViewById(R.id.detail_container);

        ivAvatar    = _findViewById(R.id.iv_detail_avatar);
        ivPhoto     = _findViewById(R.id.iv_detail_photo);
        ivPlay      = _findViewById(R.id.iv_detail_play);
        ivBooToo    = _findViewById(R.id.iv_detail_boo_too);
        ivBack      = _findViewById(R.id.iv_detail_back);

        vvVideo     = _findViewById(R.id.vv_detail_video);

        tvName      = _findViewById(R.id.tv_detail_name);
        tvTime      = _findViewById(R.id.tv_detail_time_diff);
        tvText      = _findViewById(R.id.tv_detail_text);
        tvBooToo    = _findViewById(R.id.tv_detail_boo_too);
        tvComment   = _findViewById(R.id.tv_detail_comment);

        btnBooToo   = _findViewById(R.id.detail_boo_too);
        btnComment  = _findViewById(R.id.detail_comment);
        btnShare    = _findViewById(R.id.detail_share);

        ivPlay .setVisibility(View.VISIBLE);
        ivPhoto.setVisibility(View.VISIBLE);
        vvVideo.setVisibility(View.INVISIBLE);

        ivBack.setOnClickListener(this);
        ivPhoto.setOnClickListener(this);
        ivPlay.setOnClickListener(this);

        btnBooToo.setOnClickListener(this);
        btnComment.setOnClickListener(this);
        btnShare.setOnClickListener(this);
    }

    private void displayDetail() {
        String url = Utility.utility.getMediaUrl(currentUser.user_photo_url);
        Picasso.with(this).load(url).placeholder(R.drawable.img_no_avatar).fit().centerCrop().into(ivAvatar);

        tvName.setText(currentUser.user_full_name);
        tvTime.setText(Utility.utility.getTimeDiff(mPost.getPost_time_diff()));
        tvText.setText(mPost.getPost_text());
        tvText.linkify(new SocialActionCallback() {
            @Override
            public void onMatch(@SocialActionIntDef int type, String value) {
                if(type == SocialActionType.MENTION) {

                } else if (type == SocialActionType.HASH_TAG) {

                }
            }
        });
        tvComment.setText(mPost.getPost_comments_count() > 0 ? "Comment(" + mPost.getPost_comments_count() + ")" : "Comment");

        url = Utility.utility.getMediaUrl(mPost.getPost_photo_url());

        if (url.isEmpty()) {
            url = Utility.utility.getMediaUrl(mPost.getPost_video_thumb_url());
            if (url.isEmpty()) {
                mPost.setPost_type(0);
            } else {
                mPost.setPost_type(2);
            }
        } else {
            mPost.setPost_type(1);
        }

        int h = Utility.utility.getDeviceScreenSize(this).x;

        switch (mPost.getPost_type()) {
            case 0:
                mContainer.getLayoutParams().height = 0;
                break;
            case 1:
                ivPlay .setVisibility(View.INVISIBLE);

                mContainer.getLayoutParams().height = h;
                url = Utility.utility.getMediaUrl(mPost.getPost_photo_url());
                Picasso.with(this)
                        .load(url)
                        .into(ivPhoto);
                break;
            case 2:
                mContainer.getLayoutParams().height = h;
                url = Utility.utility.getMediaUrl(mPost.getPost_video_thumb_url());
                Picasso.with(this)
                        .load(url)
                        .into(ivPhoto);

                vvVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        ivPlay.setVisibility(View.VISIBLE);
                        ivPhoto.setVisibility(View.VISIBLE);
                        vvVideo.setVisibility(View.INVISIBLE);
                    }
                });

                break;
        }

        changeBooTooStatus();
    }

    private void changeBooTooStatus() {
        ivBooToo.setColorFilter(ContextCompat.getColor(this, mPost.getIs_booed_by_me() == 1 ? R.color.colorMainButtonSelected: R.color.colorMainButtonUnselected));
        tvBooToo.setTextColor(ContextCompat.getColor(this, mPost.getIs_booed_by_me() == 1 ? R.color.colorMainButtonSelected: R.color.colorMainButtonUnselected));

        tvBooToo.setText(mPost.getPost_booed_count() > 0 ? "Boo-Too(" + mPost.getPost_booed_count() + ")" : "Boo-Too");
    }

    private View.OnClickListener playClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            v.setVisibility(View.INVISIBLE);
            ivPhoto.setVisibility(View.INVISIBLE);
            vvVideo.setVisibility(View.VISIBLE);

            final String videoUrl = Utility.utility.getMediaUrl(mPost.getPost_video_url());
            final String fileName = mPost.getPost_id() + ".mp4";
            Uri uri = FileUtility.searchVideoFile(Detail.this, fileName);
            if (uri != null) {
                vvVideo.setVideoURI(uri);
                vvVideo.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        new DownloadTask(vvVideo).execute(videoUrl, fileName);
                        return true;
                    }
                });
                vvVideo.requestFocus();
                vvVideo.start();
            } else {
                new DownloadTask(vvVideo).execute(videoUrl, fileName);
            }
        }
    };

    @Override
    public void onTaskSuccess(JSONObject jsonResponse) {
        mComments = new ArrayList<>();
        JSONObject jsonObject;
        try {
            jsonObject = jsonResponse.getJSONObject(REF_POST);
            mPost = new PostObject(jsonObject);
            JSONArray jsonArray = jsonObject.getJSONArray(POST_COMMENTS);
            for (int i = 0; i < jsonArray.length(); i++) {
                CommentObject commentObject = new CommentObject(jsonArray.getJSONObject(i));
                mComments.add(commentObject);
            }

            Collections.reverse(mComments);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        displayDetail();
    }

    @Override
    public void onTaskError(String msg) {
        Utility.showToast(this, msg);
    }

    @Override
    public void onClick(View v) {
        btnBooToo.setOnClickListener(this);
        btnComment.setOnClickListener(this);
        btnShare.setOnClickListener(this);

        if (v.equals(ivBack)) finish();
        if (v.equals(ivPhoto)) viewPhoto();
        if (v.equals(ivPlay)) viewVideo();
        if (v.equals(btnBooToo)) onBooToo();
        if (v.equals(btnComment)) onComment();
        if (v.equals(btnShare)) onShare();
    }

    private void viewPhoto() {
        if (mPost.getPost_type() == 1) {
            Intent intent = new Intent(this, PhotoReview.class);
            intent.putExtra(POST_PHOTO_URL, mPost.getPost_photo_url());
            startActivity(intent);
        }
    }

    private void viewVideo() {
        ivPlay.setVisibility(View.INVISIBLE);
        ivPhoto.setVisibility(View.INVISIBLE);
        vvVideo.setVisibility(View.VISIBLE);

        final String videoUrl = Utility.utility.getMediaUrl(mPost.getPost_video_url());
        final String fileName = mPost.getPost_id() + ".mp4";
        Uri uri = FileUtility.searchVideoFile(Detail.this, fileName);
        if (uri != null) {
            vvVideo.setVideoURI(uri);
            vvVideo.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    new DownloadTask(vvVideo).execute(videoUrl, fileName);
                    return true;
                }
            });
            vvVideo.requestFocus();
            vvVideo.start();
        } else {
            new DownloadTask(vvVideo).execute(videoUrl, fileName);
        }
    }

    private void onBooToo() {
        if (mPost.getPost_booed_count() > 0) {
            Intent intent = new Intent(this, ShowBooers.class);
            intent.putExtra(REF_POST, mPost);
            startActivity(intent);
        }
    }

    private void onComment() {
        Intent intent = new Intent(this, Comment.class);
        intent.putExtra("post", mPost.getPost_id());
        startActivityForResult(intent, REQUEST_COMMENT);
    }

    private void onShare() {

    }

    //    Post AsyncTask
    public class DownloadTask extends AsyncTask<String, Void, Uri> {
        ProgressDialog mProgressDialog = new ProgressDialog(Detail.this, R.style.ProgressDialogTheme);
        private VideoView mVideoView;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        public DownloadTask(VideoView videoView) {
            this.mVideoView = videoView;
        }

        @Override
        protected Uri doInBackground(String... params) {
            return FileUtility.downloadVideoFile(Detail.this, params[0], params[1]);
        }

        @Override
        protected void onPostExecute(Uri result) {
            if (result != null) {
                mVideoView.setVideoURI(result);
                mVideoView.start();
            }

            mProgressDialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            mProgressDialog.dismiss();
        }
    }

    //    ********************************* onActivityResult *********************************
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_COMMENT) {
                mPost = (PostObject) data.getExtras().getSerializable("post");
                displayDetail();
            }
        }
    }
}
