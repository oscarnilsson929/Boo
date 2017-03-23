package com.boo.app.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.boo.app.R;
import com.boo.app.api.GetDataTask;
import com.boo.app.api.OnTaskCompleted;
import com.boo.app.model.PostObject;
import com.boo.app.model.UserObject;
import com.boo.app.ui.adapter.BooedUserAdapter;
import com.boo.app.utility.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ShowBooers extends BaseActivity implements OnTaskCompleted, BooedUserAdapter.ActionListener{

    private PostObject postObject;

    private RecyclerView mRecyclerView;

    private ArrayList<UserObject> mBooedUsers;
    private BooedUserAdapter mBooedUserAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_booers);

        postObject = (PostObject) getIntent().getSerializableExtra(REF_POST);

        findViews();
    }

    @Override
    public void onBackPressed() {
        onBack();
    }

    private void findViews() {
        ImageView ivBack = _findViewById(R.id.iv_show_booers_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBack();
            }
        });

        mRecyclerView = _findViewById(R.id.rv_show_booers);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        init();
    }

    private void init() {
        mBooedUsers = new ArrayList<>();
        mBooedUserAdapter = new BooedUserAdapter(this, mBooedUsers, this);
        mRecyclerView.setAdapter(mBooedUserAdapter);

        String url = URL_SERVER + URL_GET_BOOERS;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(POST_ID, postObject.getPost_id());

            new GetDataTask(url, jsonRequest, this).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void displayUsers(ArrayList<UserObject> userObjects) {
        mBooedUsers.addAll(userObjects);
        mBooedUserAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTaskSuccess(JSONObject jsonResponse) {
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
    }

    @Override
    public void onTaskError(String msg) {
        Utility.showToast(this, msg);
    }

    @Override
    public void onAvatar(UserObject userObject) {
    }

    @Override
    public void onName(UserObject userObject) {
    }

    private void onBack() {
        finish();
    }

}
