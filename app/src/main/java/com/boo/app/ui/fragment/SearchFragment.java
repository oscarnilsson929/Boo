package com.boo.app.ui.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.boo.app.App;
import com.boo.app.AppConfig;
import com.boo.app.R;
import com.boo.app.api.GetDataTask;
import com.boo.app.api.OnTaskCompleted;
import com.boo.app.model.CommentObject;
import com.boo.app.model.PostObject;
import com.boo.app.model.UserObject;
import com.boo.app.ui.activity.Comment;
import com.boo.app.ui.activity.Main;
import com.boo.app.ui.activity.PhotoReview;
import com.boo.app.ui.activity.ShowBooers;
import com.boo.app.ui.adapter.PostAdapter;
import com.boo.app.utility.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchFragment extends BaseFragment implements PostAdapter.ActionListener, OnTaskCompleted {
    private RecyclerView mRecyclerView;

    private Context context;
    private ArrayList<PostObject> postObjects;
    private PostAdapter mPostAdapter;

    private boolean isLoad, isBooToo, isTagClicked, isMentionClicked;

    private ProgressDialog mProgressDialog;

    private String strQuery;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_search, container, false);

        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setCancelable(false);

        strQuery = getArguments().getString("query");
        findViews(view);
        init();
        return view;
    }

    @Override
    public void onDestroyView() {
        System.gc();
        super.onDestroyView();
    }

    private void findViews(View v) {
        mRecyclerView = (RecyclerView)v.findViewById(R.id.rv_search);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    private void init() {
        isLoad = isBooToo = isTagClicked = isMentionClicked = false;
        context = getContext();
        postObjects = new ArrayList<>();

        isLoad = true;
        loadPosts();
    }

    private void loadPosts() {
        String url = URL_SERVER + URL_GET_TAGGED_POSTS;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(USER_ID, ((App)context.getApplicationContext()).getCurrentUser().user_id);
            jsonRequest.put(TAG_STR, strQuery);

            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();

            new GetDataTask(url, jsonRequest, this).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    ********************************* OnTaskCompleted *********************************
    @Override
    public void onTaskSuccess(JSONObject jsonResponse) {
        mProgressDialog.dismiss();

        if (isLoad) {
            try {
                UserObject current_user = new UserObject(jsonResponse.getJSONObject(CURRENT_USER));
                ((App) getActivity().getApplication()).setCurrentUser(current_user);
                current_user.saveOnDisk(context);

                JSONArray posts = jsonResponse.getJSONArray(POSTS);
                ArrayList<PostObject> result = new ArrayList<>();
                for (int i = 0; i < posts.length(); i++) {
                    result.add(new PostObject(posts.getJSONObject(i)));
                }

                displayPosts(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            isLoad = false;
            return;
        }

        if (isBooToo) {
            isBooToo = false;
            try {
                UserObject current_user = new UserObject(jsonResponse.getJSONObject(CURRENT_USER));
                ((App) getActivity().getApplication()).setCurrentUser(current_user);
                current_user.saveOnDisk(context);

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

        if (isTagClicked) {
            try {
                JSONArray posts = jsonResponse.getJSONArray(POSTS);
                ArrayList<PostObject> result = new ArrayList<>();
                for (int i = 0; i < posts.length(); i++) {
                    result.add(new PostObject(posts.getJSONObject(i)));
                }

                displayPosts(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onTaskError(String msg) {
        mProgressDialog.dismiss();
        Utility.showToast(getContext(), msg);
    }


    private void displayPosts(ArrayList<PostObject> mPostObjects) {
        if (isLoad) {
            isLoad = false;
            postObjects.addAll(mPostObjects);
        }

        if (isTagClicked) {
            postObjects.clear();
            postObjects.addAll(mPostObjects);
        }

        mPostAdapter = new PostAdapter(context, postObjects, this);
        mRecyclerView.setAdapter(mPostAdapter);
    }

//    ********************************* PostAdapter.ActionListener *********************************
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
            Intent intent = new Intent(context, PhotoReview.class);
            intent.putExtra(POST_PHOTO_URL, postObject.getPost_photo_url());
            startActivity(intent);
        }
    }

    @Override
    public void onBooToo(PostObject postObject) {
        if (isLoad) return;

        isBooToo = true;
        String url = URL_SERVER + URL_BOO_POST;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(USER_ID, ((App)getActivity().getApplication()).getCurrentUser().user_id);
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

    }

    @Override
    public void onMentionClick(String user_name) {
        isMentionClicked = true;

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
        isTagClicked = true;

        String url = URL_SERVER + URL_GET_TAGGED_POSTS;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(USER_ID, ((App)context.getApplicationContext()).getCurrentUser().user_id);
            jsonRequest.put(TAG_STR, "#" + tag);

            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();

            new GetDataTask(url, jsonRequest, this).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //    ********************************* onActivityResult *********************************
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
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
}