package com.boo.app.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.boo.app.App;
import com.boo.app.AppConfig;
import com.boo.app.R;
import com.boo.app.api.GetDataTask;
import com.boo.app.api.OnTaskCompleted;
import com.boo.app.model.CommentObject;
import com.boo.app.model.PostObject;
import com.boo.app.model.UserObject;
import com.boo.app.ui.activity.Comment;
import com.boo.app.ui.activity.FindIt;
import com.boo.app.ui.activity.Main;
import com.boo.app.ui.activity.PhotoReview;
import com.boo.app.ui.activity.PostToBoo;
import com.boo.app.ui.activity.ShowBooers;
import com.boo.app.ui.adapter.PostAdapter;
import com.boo.app.utility.FileUtility;
import com.boo.app.utility.HidingScrollListener;
import com.boo.app.utility.Utility;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class HomeFragment extends BaseFragment implements View.OnClickListener, PostAdapter.ActionListener, OnTaskCompleted {
    private LinearLayout mHeader;
    private SwipyRefreshLayout mSwipyRefreshLayout;
    private ImageView ivAvatar;
    private RecyclerView mRecyclerView;
    private LinearLayout btnText, btnMedia, btnPlace;

    private Context mContext;
    private ArrayList<PostObject> postObjects;
    private PostAdapter mPostAdapter;

    private Uri mMediaUri;
    private boolean isLoadMore, isBooToo, isMentionClicked;
    private int current_page;

    private ProgressDialog mProgressDialog;
    private Bitmap bitmapToShare;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_home, container, false);

        initUI(view);

        return view;
    }

    private void initUI(View v) {
        mContext = getContext();

        mProgressDialog = new ProgressDialog(mContext, R.style.ProgressDialogTheme);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);

        mHeader             = (LinearLayout)v.findViewById(R.id.home_header);
        mSwipyRefreshLayout = (SwipyRefreshLayout)v.findViewById(R.id.swipe_refresh_layout_home);
        ivAvatar            = (ImageView)   v.findViewById(R.id.iv_home_avatar);
        btnText             = (LinearLayout)v.findViewById(R.id.btn_home_text);
        btnMedia            = (LinearLayout)v.findViewById(R.id.btn_home_media);
        btnPlace            = (LinearLayout)v.findViewById(R.id.btn_home_place);
        mRecyclerView       = (RecyclerView)v.findViewById(R.id.rv_home);

        String url = Utility.utility.getMediaUrl(((App) getActivity().getApplication()).getCurrentUser().user_photo_url);
        Picasso.with(mContext).load(url).placeholder(R.drawable.img_no_avatar).fit().centerCrop().into(ivAvatar);

        btnText .setOnClickListener(this);
        btnMedia.setOnClickListener(this);
        btnPlace.setOnClickListener(this);

        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.addOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                hideHeader(mHeader);
            }

            @Override
            public void onShow() {
                showHeader(mHeader);
            }
        });

        mSwipyRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                isLoadMore = true;
                current_page++;
                loadPosts();
            }
        });

        init();
    }

    private void init() {
        current_page = 0;
        isBooToo = isMentionClicked = false;
        postObjects = new ArrayList<>();
        mPostAdapter = new PostAdapter(mContext, postObjects, this);
        mRecyclerView.setAdapter(mPostAdapter);

        isLoadMore = true;
        loadPosts();
    }

    private void loadPosts() {
        String url = URL_SERVER + URL_GET_FEED_POSTS;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(USER_ID, ((App) getActivity().getApplication()).getCurrentUser().user_id);
            jsonRequest.put(SEARCH_LIMIT, search_limit);
            jsonRequest.put(SEARCH_OFFSET, current_page * search_limit);

            showProgressDialog();

            new GetDataTask(url, jsonRequest, this).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void displayPosts(ArrayList<PostObject> mPostObjects) {
        postObjects.addAll(mPostObjects);
        mPostAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (App.isLoading) return;

        if (v.equals(btnText)) onWriteIt();
        if (v.equals(btnMedia)) onShowIt();
        if (v.equals(btnPlace)) onFindIt();
    }

//    ********************************* Write, Show, Find it *********************************
    private void onWriteIt() {
        Intent intent = new Intent(mContext, PostToBoo.class);
        intent.putExtra("type", 0);
        startActivityForResult(intent, REQUEST_POST_BOO);
    }

    private void onShowIt() {
        final Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.dialog_video_photo);
        Button btnPhoto = (Button)dialog.findViewById(R.id.btn_dialog_video_photo_photo);
        Button btnVideo = (Button)dialog.findViewById(R.id.btn_dialog_video_photo_video);

        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onShowMedia(0);
                dialog.dismiss();
            }
        });

        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onShowMedia(1);
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

    private void onShowMedia(final int type) {
        final Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.dialog_choose_media);
        Button btnGallery = (Button)dialog.findViewById(R.id.btn_dialog_choose_media_Library);
        Button btnCamera  = (Button)dialog.findViewById(R.id.btn_dialog_choose_media_camera);

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                switch (type) {
                    case 0:
                        intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, PICK_PHOTO_GALLERY);
                        break;
                    case 1:
                        intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, PICK_VIDEO_GALLERY);
                        break;
                }

                dialog.dismiss();
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (type) {
                    case 0:
                        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_PHOTO_CAMERA);
                        } else {
                            openPhotoCamera();
                        }
                        break;
                    case 1:
                        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_VIDEO_CAMERA);
                        } else {
                            openVideoCamera();
                        }
                        break;
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

    private void onFindIt() {
        Intent intent = new Intent(mContext, FindIt.class);
        startActivityForResult(intent, REQUEST_POST_BOO);
    }

//    ********************************* Open Camera *********************************
    private void openPhotoCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = FileUtility.createImageTempFile(mContext);
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

    private void openVideoCamera() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(mContext.getPackageManager()) != null) {
            File videoFile = null;
            try {
                videoFile = FileUtility.createVideoTempFile(mContext);
                mMediaUri = Uri.fromFile(videoFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (videoFile != null) {
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, VIDEO_DURATION_LIMIT);
                startActivityForResult(takeVideoIntent, PICK_VIDEO_CAMERA);
            }
        }
    }

//    ********************************* PostAdapter.ActionListener *********************************
    @Override
    public void onUser(PostObject postObject) {
        if (App.isLoading) return;

        UserObject refUser = new UserObject(postObject);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, BootiqueFragment.newInstance(refUser), "");
        ft.commit();
        ((Main)getActivity()).changeButtonColor(9);
    }

    @Override
    public void onUserName(PostObject postObject) {
        onUser(postObject);
    }

    @Override
    public void onPhoto(PostObject postObject) {
        if (App.isLoading) return;

        if (postObject.getPost_type() == 1) {
            Intent intent = new Intent(mContext, PhotoReview.class);
            intent.putExtra(POST_PHOTO_URL, postObject.getPost_photo_url());
            startActivity(intent);
        }
    }

    @Override
    public void onBooToo(PostObject postObject) {
        if (App.isLoading) return;

        isBooToo = true;
        String url = URL_SERVER + URL_BOO_POST;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(USER_ID, ((App)getActivity().getApplication()).getCurrentUser().user_id);
            jsonRequest.put(REF_ID,  postObject.getPost_id());

            showProgressDialog();

            new GetDataTask(url, jsonRequest, this).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBooTooLong(PostObject postObject) {
        if (App.isLoading) return;

        Intent intent = new Intent(mContext, ShowBooers.class);
        intent.putExtra(REF_POST, postObject);
        startActivity(intent);
    }

    @Override
    public void onComment(PostObject postObject) {
        if (App.isLoading) return;

        Intent intent = new Intent(mContext, Comment.class);
        intent.putExtra("post", postObject.getPost_id());
        startActivityForResult(intent, REQUEST_COMMENT);
    }

    @Override
    public void onShare(PostObject postObject) {
        if (App.isLoading) return;

        String photoUrl = postObject.getPost_photo_url();
        if (!photoUrl.isEmpty()) {
            photoUrl = Utility.utility.getMediaUrl(photoUrl);
            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    shareImage(bitmap);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };

            Picasso.with(mContext)
                    .load(photoUrl)
                    .into(target);
        }
    }

    private void shareImage(Bitmap bitmap){

        if (bitmap == null) return;

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            bitmapToShare = bitmap;
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            String pathofBmp = MediaStore.Images.Media.insertImage(mContext.getContentResolver(), bitmap, "title", null);
            Uri bmpUri = Uri.parse(pathofBmp);

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            shareIntent.setType("image/*");
            startActivity(Intent.createChooser(shareIntent, "Share to"));
        }
    }

    @Override
    public void onMentionClick(String user_name) {
        if (App.isLoading) return;

        isMentionClicked = true;

        String url = URL_SERVER + URL_GET_USER_BY_NAME;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(USER_NAME, user_name);

            showProgressDialog();

            new GetDataTask(url, jsonRequest, this).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTagClick(String tag) {
        if (App.isLoading) return;

        Bundle bundle = new Bundle();
        bundle.putString("query", "#" + tag);
        Fragment fragment = new SearchFragment();
        fragment.setArguments(bundle);

        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment, "");
        ft.commit();
        ((Main)getActivity()).changeButtonColor(10);
    }

    //    ********************************* Utility *********************************
    private void showProgressDialog() {
        App.isLoading = true;

        if (!mSwipyRefreshLayout.isRefreshing())
            mProgressDialog.show();
    }

    private void dismissProgressDialog() {
        App.isLoading = false;

        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();

        if (mSwipyRefreshLayout.isRefreshing())
            mSwipyRefreshLayout.setRefreshing(false);
    }



    //    ********************************* OnTaskCompleted *********************************
    @Override
    public void onTaskSuccess(JSONObject jsonResponse) {
        dismissProgressDialog();

        if (isLoadMore) {
            isLoadMore = false;
            try {
                UserObject current_user = new UserObject(jsonResponse.getJSONObject(CURRENT_USER));
                ((App) mContext.getApplicationContext()).setCurrentUser(current_user);
                current_user.saveOnDisk(mContext);

                JSONArray posts = jsonResponse.getJSONArray(POSTS);
                ArrayList<PostObject> result = new ArrayList<>();
                for (int i = 0; i < posts.length(); i++) {
                    result.add(new PostObject(posts.getJSONObject(i)));
                }

                displayPosts(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return;
        }

        if (isBooToo) {
            isBooToo = false;
            try {
                UserObject current_user = new UserObject(jsonResponse.getJSONObject(CURRENT_USER));
                ((App) getActivity().getApplication()).setCurrentUser(current_user);
                current_user.saveOnDisk(mContext);

                PostObject result = new PostObject(jsonResponse.getJSONObject(AppConfig.REF_POST));

                for(PostObject item : postObjects){
                    if(item.getPost_id() == result.getPost_id()) {
                        postObjects.set(postObjects.indexOf(item), result);
                        break;
                    }
                }

                mPostAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (isMentionClicked) {
            isMentionClicked = false;
            try {
                UserObject refUser = new UserObject(jsonResponse.getJSONObject(CURRENT_USER));
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, BootiqueFragment.newInstance(refUser), "");
                ft.commit();
                ((Main)getActivity()).changeButtonColor(9);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onTaskError(String msg) {
        dismissProgressDialog();
        Utility.showToast(getContext(), msg);
    }

    //    ********************************* onActivityResult *********************************
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_PHOTO_GALLERY || requestCode == PICK_PHOTO_CAMERA || requestCode == PICK_VIDEO_CAMERA || requestCode == PICK_VIDEO_GALLERY) {
                if (requestCode == PICK_PHOTO_GALLERY || requestCode == PICK_VIDEO_GALLERY) {
                    mMediaUri = data.getData();
                }

                Intent intent = new Intent(mContext, PostToBoo.class);
                intent.putExtra("uri", mMediaUri.toString());
                intent.putExtra("type", (requestCode == PICK_VIDEO_CAMERA || requestCode == PICK_VIDEO_GALLERY) ? 2: 1);
                startActivityForResult(intent, REQUEST_POST_BOO);
            }

            if (requestCode == REQUEST_POST_BOO) {
                init();
            }

            if (requestCode == REQUEST_COMMENT) {
                int type = data.getExtras().getInt("type");

                switch (type) {
                    case 0:
                        PostObject postObject = (PostObject) data.getExtras().getSerializable("post");
                        for(PostObject item : postObjects){
                            if(postObject != null && item.getPost_id() == postObject.getPost_id()) {
                                postObjects.set(postObjects.indexOf(item), postObject);
                                break;
                            }
                        }

                        mPostAdapter.notifyDataSetChanged();
                        break;
                    case 1:
                        CommentObject commentObject = (CommentObject) data.getExtras().getSerializable("comment");
                        UserObject refUser = new UserObject(commentObject);
                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.content_frame, BootiqueFragment.newInstance(refUser), "");
                        ft.commit();
                        ((Main)getActivity()).changeButtonColor(9);
                        break;
                }
            }
        }
    }

//    ********************************* Request Permissions Result *********************************
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case PERMISSIONS_REQUEST_PHOTO_CAMERA: {
                    openPhotoCamera();
                    break;
                }

                case PERMISSIONS_REQUEST_VIDEO_CAMERA: {
                    openVideoCamera();
                    break;
                }
                case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                    shareImage(bitmapToShare);
                    break;
                }
            }
        }
    }
}