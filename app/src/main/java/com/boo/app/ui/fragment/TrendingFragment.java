package com.boo.app.ui.fragment;

import android.Manifest;
import android.app.Activity;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.boo.app.App;
import com.boo.app.AppConfig;
import com.boo.app.R;
import com.boo.app.api.GetDataTask;
import com.boo.app.api.OnTaskCompleted;
import com.boo.app.model.PostObject;
import com.boo.app.model.UserObject;
import com.boo.app.ui.activity.Comment;
import com.boo.app.ui.activity.Main;
import com.boo.app.ui.activity.PhotoReview;
import com.boo.app.ui.activity.ShowBooers;
import com.boo.app.ui.adapter.PostAdapter;
import com.boo.app.utility.HidingScrollListener;
import com.boo.app.utility.LocationTracker;
import com.boo.app.utility.Utility;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TrendingFragment extends BaseFragment implements View.OnClickListener, PostAdapter.ActionListener, OnTaskCompleted{
    private LinearLayout mHeader;
    private SwipyRefreshLayout mSwipyRefreshLayout;
    private RelativeLayout btnNearYou, btnGlobally;
    private ImageView      ivNearYou, ivGlobally;
    private TextView       tvNearYou, tvGlobally;
    private View           indicatorNearYou, indicatorGlobally;
    private RecyclerView   mRecyclerView;

    private Context mContext;
    private ArrayList<PostObject> postObjects;
    private PostAdapter mPostAdapter;

    private ProgressDialog mProgressDialog;

    private boolean isRefresh, isLoadMore, isNearYou;
    private int current_page;
    private Bitmap bitmapToShare;

    private int taskMode; // 0 : GetPosts, 1 : BooPost, 2 : Tag Click

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_trending, container, false);

        mContext = getContext();
        mProgressDialog = new ProgressDialog(getContext(), R.style.ProgressDialogTheme);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);

        findViews(view);

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
        } else {
            init();
        }

        return view;
    }

    private void findViews(View view) {
        mHeader             = (LinearLayout)      view.findViewById(R.id.trending_header);
        mSwipyRefreshLayout = (SwipyRefreshLayout)view.findViewById(R.id.swipe_refresh_layout_trending);
        btnNearYou          = (RelativeLayout)    view.findViewById(R.id.trending_near_you);
        btnGlobally         = (RelativeLayout)    view.findViewById(R.id.trending_globally);
        ivNearYou           = (ImageView)         view.findViewById(R.id.iv_trending_near_you);
        ivGlobally          = (ImageView)         view.findViewById(R.id.iv_trending_globally);
        tvNearYou           = (TextView)          view.findViewById(R.id.tv_trending_near_you);
        tvGlobally          = (TextView)          view.findViewById(R.id.tv_trending_globally);
        indicatorNearYou    =                     view.findViewById(R.id.indicator_trending_near_you);
        indicatorGlobally   =                     view.findViewById(R.id.indicator_trending_globally);
        mRecyclerView       = (RecyclerView)      view.findViewById(R.id.rv_trending);
    }

    private void init() {
        current_page = 0;
        isRefresh = true;
        isLoadMore = false;
        postObjects = new ArrayList<>();

        mPostAdapter = new PostAdapter(mContext, postObjects, this);

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
                if (direction == SwipyRefreshLayoutDirection.TOP){
                    isRefresh = true;
                    current_page = 0;
                } else {
                    isLoadMore = true;
                    current_page++;
                }

                if (isNearYou) {
                    loadNearYou();
                } else {
                    loadGlobally();
                }
            }
        });

        btnNearYou .setOnClickListener(this);
        btnGlobally.setOnClickListener(this);

        loadNearYou();
    }

    private void loadNearYou() {
        isNearYou = true;
        changeBtnStatus();
        onGetPosts();
    }

    private void loadGlobally() {
        isNearYou = false;
        changeBtnStatus();
        onGetPosts();
    }

    private void onGetPosts() {
        taskMode = 0;

        String url = URL_SERVER + (isNearYou ? URL_GET_NEARBY_POSTS: URL_GET_GLOBAL_POSTS);
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(USER_ID,       ((App)getActivity().getApplication()).getCurrentUser().user_id);
            jsonRequest.put(SEARCH_OFFSET, current_page * search_limit);
            jsonRequest.put(SEARCH_LIMIT,  search_limit);

            if (isNearYou) {
                LocationTracker mLocationTracker = new LocationTracker(getActivity());
                double latitude  = mLocationTracker.getLatitude();
                double longitude = mLocationTracker.getLongitude();

                jsonRequest.put(USER_LOCATION_LATITUDE,  latitude);
                jsonRequest.put(USER_LOCATION_LONGITUDE, longitude);
            }

            mProgressDialog.show();

            new GetDataTask(url, jsonRequest, this).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void displayPosts(ArrayList<PostObject> mPostObjects) {
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

//    ********************************** View.OnClickListener **********************************
    @Override
    public void onClick(View v) {
        if (v.equals(btnNearYou)) {
            isRefresh = true;
            current_page = 0;
            loadNearYou();
        }

        if (v.equals(btnGlobally)) {
            isRefresh = true;
            current_page = 0;
            loadGlobally();
        }
    }

//    ********************************** PostAdapter.ActionListener **********************************
    @Override
    public void onUser(PostObject postObject) {
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
        if (postObject.getPost_type() == 1) {
            Intent intent = new Intent(mContext, PhotoReview.class);
            intent.putExtra(POST_PHOTO_URL, postObject.getPost_photo_url());
            startActivity(intent);
        }
    }

    @Override
    public void onBooToo(PostObject postObject) {
        taskMode = 1;

        String url = URL_SERVER + URL_BOO_POST;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(USER_ID, ((App)getActivity().getApplication()).getCurrentUser().user_id);
            jsonRequest.put(REF_ID,  postObject.getPost_id());

            mProgressDialog.show();

            new GetDataTask(url, jsonRequest, this).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBooTooLong(PostObject postObject) {
        Intent intent = new Intent(mContext, ShowBooers.class);
        intent.putExtra(REF_POST, postObject);
        startActivity(intent);
    }

    @Override
    public void onComment(PostObject postObject) {
        Intent intent = new Intent(mContext, Comment.class);
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
        taskMode = 2;

        String url = URL_SERVER + URL_GET_USER_BY_NAME;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(USER_NAME, user_name);

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
            case 0:
                try {
                    UserObject user = new UserObject(jsonResponse.getJSONObject(CURRENT_USER));
                    user.saveOnDisk(mContext);
                    ((App) getActivity().getApplication()).setCurrentUser(user);

                    JSONArray posts = jsonResponse.getJSONArray(POSTS);
                    ArrayList<PostObject> result = new ArrayList<>();
                    for (int i = 0; i < posts.length(); i++) {
                        result.add(new PostObject(posts.getJSONObject(i)));
                    }

                    displayPosts(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case 1:
                try {
                    UserObject currentUser = new UserObject(jsonResponse.getJSONObject(AppConfig.CURRENT_USER));
                    currentUser.saveOnDisk(mContext);
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
            case 2: // Tag Click
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

//    ********************************** onActivityResult **********************************
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
        }
    }

    //    ********************************* Request Permissions Result *********************************
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
                init();
            }

            if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
                shareImage(bitmapToShare);
            }
        }
    }


    private void changeBtnStatus() {
        ivNearYou.setColorFilter(ContextCompat.getColor(mContext, isNearYou ? R.color.colorMainButtonSelected: R.color.colorMainButtonUnselected));
        tvNearYou.setTextColor  (ContextCompat.getColor(mContext, isNearYou ? R.color.colorMainButtonSelected: R.color.colorMainButtonUnselected));
        indicatorNearYou.setVisibility(isNearYou ? View.VISIBLE : View.INVISIBLE);

        ivGlobally.setColorFilter(ContextCompat.getColor(mContext, isNearYou ? R.color.colorMainButtonUnselected: R.color.colorMainButtonSelected));
        tvGlobally.setTextColor  (ContextCompat.getColor(mContext, isNearYou ? R.color.colorMainButtonUnselected: R.color.colorMainButtonSelected));
        indicatorGlobally.setVisibility(!isNearYou ? View.VISIBLE : View.INVISIBLE);
    }
}