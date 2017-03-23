package com.boo.app.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.boo.app.App;
import com.boo.app.AppConfig;
import com.boo.app.R;
import com.boo.app.api.GetDataTask;
import com.boo.app.api.OnTaskCompleted;
import com.boo.app.model.PostObject;
import com.boo.app.model.UserObject;
import com.boo.app.ui.activity.Comment;
import com.boo.app.ui.activity.EditProfile;
import com.boo.app.ui.activity.Main;
import com.boo.app.ui.activity.PhotoReview;
import com.boo.app.ui.activity.ShowBooers;
import com.boo.app.ui.activity.ShowFollowUsers;
import com.boo.app.ui.adapter.PostAdapter;
import com.boo.app.utility.Utility;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import mehdi.sakout.fancybuttons.FancyButton;

public class BootiqueFragment extends BaseFragment implements View.OnClickListener, PostAdapter.ActionListener, OnTaskCompleted {
    private SwipyRefreshLayout mSwipyRefreshLayout;
    private ImageView ivAvatar;
    private TextView tvUsername, tvBio, tvFollowingCount, tvFollowersCount, tvPostsCount;
    private LinearLayout btnFollowing, btnFollowers, btnPosts;
    private FancyButton btnFollow;
    private RecyclerView mRecyclerView;

    private ProgressDialog mProgressDialog;
    private ArrayList<PostObject> postObjects;
    private PostAdapter mPostAdapter;
    private UserObject currentUser, refUser;
    private Context context;

    private boolean isSelf = false;
    private boolean isRefresh, isLoadMore;
    private int current_page;
    private int taskMode; // 0 : GetProfile, 1 : GetUserPosts, 2 : BooPost, 3 : FollowUser, 4 : DeletePost, 5 : Tag Click
    private int positionToDelete;
    private Bitmap bitmapToShare;

    public static BootiqueFragment newInstance(UserObject refUser) {
        BootiqueFragment fragment = new BootiqueFragment();
        Bundle args = new Bundle();
        args.putSerializable("user", refUser);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_bootique, container, false);

        currentUser = ((App)getActivity().getApplication()).getCurrentUser();
        refUser = (UserObject) getArguments().getSerializable("user");

        findViews(view);
        init();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        getProfile();
    }

    private void findViews(View v) {
        mSwipyRefreshLayout = (SwipyRefreshLayout)v.findViewById(R.id.swipe_refresh_layout_bootique);
        ivAvatar            = (ImageView)         v.findViewById(R.id.iv_bootique_avatar);
        tvUsername          = (TextView)          v.findViewById(R.id.tv_bootique_user_name);
        tvBio               = (TextView)          v.findViewById(R.id.tv_bootique_bio);
        tvFollowingCount    = (TextView)          v.findViewById(R.id.tv_bootique_following_count);
        tvFollowersCount    = (TextView)          v.findViewById(R.id.tv_bootique_followers_count);
        tvPostsCount        = (TextView)          v.findViewById(R.id.tv_bootique_posts_count);
        btnFollowing        = (LinearLayout)      v.findViewById(R.id.bootique_following);
        btnFollowers        = (LinearLayout)      v.findViewById(R.id.bootique_followers);
        btnPosts            = (LinearLayout)      v.findViewById(R.id.bootique_posts);
        btnFollow           = (FancyButton)       v.findViewById(R.id.btn_bootique_follow);
        mRecyclerView       = (RecyclerView)      v.findViewById(R.id.rv_bootique);
    }

    private void init() {
        context = getContext();

        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setCancelable(false);

        isRefresh = true;
        isLoadMore = false;
        current_page = 0;

        postObjects = new ArrayList<>();
        mPostAdapter = new PostAdapter(context, postObjects, this);

        if (refUser != null) {
            isSelf = (currentUser.user_id == refUser.user_id);
        }

        if (isSelf) {
            setUpItemTouchHelper();
        }

        changeFollowButton();

        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mSwipyRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if (direction == SwipyRefreshLayoutDirection.TOP){
                    isRefresh = true;
                    current_page = 0;
                } else {
                    isLoadMore = true;
                    current_page++;
                }

                getPosts();
            }
        });

        btnFollowing.setOnClickListener(this);
        btnFollowers.setOnClickListener(this);
        btnPosts    .setOnClickListener(this);
        btnFollow   .setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(btnFollow)) {
            if (isSelf) {
                startActivity(new Intent(context, EditProfile.class));
            } else {
                onFollowUser();
            }
        }

        if (v.equals(btnFollowing)) showFollowUsers(true);
        if (v.equals(btnFollowers)) showFollowUsers(false);
    }

    private void getProfile() {
        taskMode = 0;

        String url = URL_SERVER + URL_GET_PROFILE;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(USER_ID, currentUser.user_id);
            jsonRequest.put(REF_ID,  refUser.user_id);

            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();

            new GetDataTask(url, jsonRequest, this).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getPosts() {
        taskMode = 1;

        String url = URL_SERVER + URL_GET_USER_POSTS;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(USER_ID,       currentUser.user_id);
            jsonRequest.put(REF_ID,        refUser.user_id);
            jsonRequest.put(SEARCH_OFFSET, current_page * search_limit);
            jsonRequest.put(SEARCH_LIMIT,  search_limit);

            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();

            new GetDataTask(url, jsonRequest, this).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onFollowUser() {
        taskMode = 3;

        String url = URL_SERVER + URL_FOLLOW_USER;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(USER_ID, currentUser.user_id);
            jsonRequest.put(REF_ID,  refUser.user_id);

            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();

            new GetDataTask(url, jsonRequest, this).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onDeletePost(int position) {
        taskMode = 4;
        positionToDelete = position;

        String url = URL_SERVER + URL_MANAGE_MY_POSTS;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(USER_ID, currentUser.user_id);
            jsonRequest.put(MANAGE_TYPE, DELETE);
            jsonRequest.put(POST_ID, postObjects.get(position).getPost_id());

            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();

            new GetDataTask(url, jsonRequest, this).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showFollowUsers(boolean isFollowing) {
        if (isSelf) {
            Intent intent = new Intent(getContext(), ShowFollowUsers.class);
            intent.putExtra(FROM, isFollowing ? 0 : 1);
            intent.putExtra(USER_ID, refUser.user_id);
            startActivityForResult(intent, REQUEST_FOLLOW);
        }
    }

    private void displayProfile() {
        String url = Utility.utility.getMediaUrl(refUser.user_photo_url);
        Picasso.with(getContext()).load(url).placeholder(R.drawable.img_no_avatar).fit().centerCrop().into(ivAvatar);

        tvUsername      .setText(refUser.user_full_name);
        tvBio           .setText(refUser.user_bio);
        tvFollowingCount.setText(String.valueOf(refUser.user_following_count));
        tvFollowersCount.setText(String.valueOf(refUser.user_followers_count));
        tvPostsCount    .setText(String.valueOf(refUser.user_posts_count));

        changeFollowButton();
    }

    private void displayUserPosts(ArrayList<PostObject> mPostObjects) {
        if (isRefresh) {
            isRefresh = false;
            postObjects.clear();
            postObjects.addAll(mPostObjects);
        }

        if (isLoadMore) {
            isLoadMore = false;
            postObjects.addAll(mPostObjects);
        }

        mSwipyRefreshLayout.setRefreshing(false);
        mRecyclerView.setAdapter(mPostAdapter);
    }

//    ****************************** PostAdapter.ActionListener ******************************
    @Override
    public void onUser(PostObject postObject) {
    }

    @Override
    public void onUserName(PostObject postObject) {

    }

    @Override
    public void onPhoto(PostObject postObject) {
        if (postObject.getPost_type() == 1) {
            Intent intent = new Intent(context, PhotoReview.class);
            intent.putExtra(POST_PHOTO_URL, postObject.getPost_photo_url());
            startActivity(intent);
        }
    }

    @Override
    public void onBooToo(PostObject postObject) {
        taskMode = 2;

        String url = URL_SERVER + URL_BOO_POST;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(USER_ID, currentUser.user_id);
            jsonRequest.put(REF_ID,  postObject.getPost_id());

            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();

            new GetDataTask(url, jsonRequest, this).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBooTooLong(PostObject postObject) {
        Intent intent = new Intent(context, ShowBooers.class);
        intent.putExtra(REF_POST, postObject);
        startActivity(intent);
    }

    @Override
    public void onComment(PostObject postObject) {
        Intent intent = new Intent(context, Comment.class);
        intent.putExtra("post", postObject);
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

            Picasso.with(context)
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
            String pathofBmp = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "title", null);
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
        taskMode = 5;

        String url = URL_SERVER + URL_GET_USER_BY_NAME;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(USER_NAME, user_name);

            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();

            new GetDataTask(url, jsonRequest, this).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTagClick(String tag) {
        Bundle bundle = new Bundle();
        bundle.putString("query", "#" + tag);
        Fragment fragment = new SearchFragment();
        fragment.setArguments(bundle);

        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment, "");
        ft.commit();
        ((Main)getActivity()).changeButtonColor(10);
    }

    @Override
    public void onTaskSuccess(JSONObject jsonResponse) {
        mProgressDialog.dismiss();

        switch (taskMode) {
            case 0: // GetProfile
                try {
                    refUser = new UserObject(jsonResponse.getJSONObject(REF_USER));
                    displayProfile();
                    getPosts();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
            case 1: // GetUserPosts
                try {
                    JSONArray posts = jsonResponse.getJSONArray(POSTS);
                    ArrayList<PostObject> result = new ArrayList<>();
                    for (int i = 0; i < posts.length(); i++) {
                        result.add(new PostObject(posts.getJSONObject(i)));
                    }

                    displayUserPosts(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case 2: // BooPost
                try {
                    UserObject currentUser = new UserObject(jsonResponse.getJSONObject(AppConfig.CURRENT_USER));
                    currentUser.saveOnDisk(context);
                    ((App)getActivity().getApplication()).setCurrentUser(currentUser);

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
                break;
            case 3: // FollowUser
                try {
                    refUser = new UserObject(jsonResponse.getJSONObject(REF_USER));
                    displayProfile();
                    changeFollowButton();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case 4: // DeletePost
                try {
                    refUser = new UserObject(jsonResponse.getJSONObject(CURRENT_USER));
                    refUser.saveOnDisk(context);
                    ((App)context.getApplicationContext()).setCurrentUser(refUser);
                    displayProfile();

                    postObjects.remove(positionToDelete);
                    mPostAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case 5: // Tag Select;
                try {
                    UserObject refUser = new UserObject(jsonResponse.getJSONObject(CURRENT_USER));
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame, BootiqueFragment.newInstance(refUser), "");
                    ft.commit();
                    ((Main)getActivity()).changeButtonColor(9);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onTaskError(String msg) {
        mProgressDialog.dismiss();
        Utility.showToast(getContext(), msg);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_COMMENT) {
                PostObject postObject = (PostObject) data.getExtras().getSerializable("post");

                for(PostObject item : postObjects){
                    if(postObject != null && item.getPost_id() == postObject.getPost_id()) {
                        postObjects.set(postObjects.indexOf(item), postObject);
                        break;
                    }
                }

                mPostAdapter.notifyDataSetChanged();
            }

            if (requestCode == REQUEST_FOLLOW) {
                int type = data.getExtras().getInt(TYPE);

                switch (type) {
                    case 0:
                        break;
                    case 1:
                        refUser = (UserObject) data.getExtras().getSerializable(REF_USER);
                        init();
                        break;
                }
            }
        }
    }

//    Utility
    private void changeFollowButton() {
        if (isSelf) {
            btnFollow.setText("EDIT PROFILE");
        } else {
            if (refUser.is_followed_by_me == 1) {
                btnFollow.setBackgroundColor(ContextCompat.getColor(context, R.color.colorFollow));
                btnFollow.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                btnFollow.setText("FOLLOWING");
            } else {
                btnFollow.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                btnFollow.setTextColor(ContextCompat.getColor(context, R.color.colorFollow));
                btnFollow.setText("FOLLOW");
            }
        }
    }

    private void setUpItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Do you really want to delete?").setTitle("DELETE");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int position = viewHolder.getAdapterPosition();
                        onDeletePost(position);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mPostAdapter.notifyDataSetChanged();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);

        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    //    ********************************* Request Permissions Result *********************************
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                    shareImage(bitmapToShare);
                    break;
                }
            }
        }
    }
}