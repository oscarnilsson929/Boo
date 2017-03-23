package com.boo.app.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.boo.app.App;
import com.boo.app.R;
import com.boo.app.api.GetDataTask;
import com.boo.app.api.OnTaskCompleted;
import com.boo.app.model.UserObject;
import com.boo.app.ui.adapter.FollowAdapter;
import com.boo.app.utility.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ShowFollowUsers extends BaseActivity implements FollowAdapter.ActionListener, OnTaskCompleted{
    private TextView tvTitle;
    private RecyclerView mRecyclerView;

    private ProgressDialog mProgressDialog;

    private ArrayList<UserObject> mFollowUsers;
    private FollowAdapter mFollowAdapter;

    private int from, current_page = 0;
    private int taskMode; // 0: Get Following Users, 1: Follow User
    private long user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_follow_users);

        from = getIntent().getIntExtra(FROM, 0);
        user_id = getIntent().getLongExtra(USER_ID, 0);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        findViews();
    }

    @Override
    public void onBackPressed() {
        onBack();
    }

    private void findViews() {
        ImageView ivBack = _findViewById(R.id.iv_show_follow_users_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBack();
            }
        });

        tvTitle       = _findViewById(R.id.tv_show_follow_users_title);

        mRecyclerView = _findViewById(R.id.rv_show_follow_users);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        init();
    }

    private void init() {
        mFollowUsers = new ArrayList<>();
        mFollowAdapter = new FollowAdapter(this, mFollowUsers, this);
        mRecyclerView.setAdapter(mFollowAdapter);

        String url = URL_SERVER;
        switch (from) {
            case 0:
                tvTitle.setText(R.string.following);
                url += URL_GET_FOLLOWING;
                break;
            case 1:
                tvTitle.setText(R.string.followers);
                url += URL_GET_FOLLOWERS;
                break;
        }

        getFollowUsers(url);
    }

    private void getFollowUsers(String url) {
        taskMode = 0;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(USER_ID, user_id);
            jsonRequest.put(SEARCH_LIMIT,  search_limit);
            jsonRequest.put(SEARCH_OFFSET, current_page * search_limit);

            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();

            new GetDataTask(url, jsonRequest, this).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void displayUsers(ArrayList<UserObject> userObjects) {
        mFollowUsers.addAll(userObjects);
        mFollowAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTaskSuccess(JSONObject jsonResponse) {
        mProgressDialog.dismiss();

        switch (taskMode) {
            case 0:
                try {
                    JSONArray users = jsonResponse.getJSONArray(USERS);
                    ArrayList<UserObject> result = new ArrayList<>();
                    for (int i = 0; i < users.length(); i++) {
                        result.add(new UserObject(users.getJSONObject(i)));
                    }

                    displayUsers(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case 1:
                try {
                    UserObject refUser = new UserObject(jsonResponse.getJSONObject(REF_USER));
                    for (UserObject item: mFollowUsers) {
                        if (item.user_id == refUser.user_id) {
                            mFollowUsers.set(mFollowUsers.indexOf(item), refUser);
                        }
                    }
                    mFollowAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onTaskError(String msg) {
        mProgressDialog.dismiss();
        Utility.showToast(this, msg);
    }

    @Override
    public void onAvatar(UserObject userObject) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(TYPE, 1);
        resultIntent.putExtra(REF_USER, userObject);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onName(UserObject userObject) {
        onAvatar(userObject);
    }

    @Override
    public void onFollow(UserObject userObject) {
        taskMode = 1;
        String url = URL_SERVER + URL_FOLLOW_USER;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(USER_ID, ((App)getApplication()).getCurrentUser().user_id);
            jsonRequest.put(REF_ID,  userObject.user_id);

            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();

            new GetDataTask(url, jsonRequest, this).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onBack() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(TYPE, 0);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
