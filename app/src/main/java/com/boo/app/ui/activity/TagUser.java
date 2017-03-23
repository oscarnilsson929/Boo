package com.boo.app.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;

import com.boo.app.R;
import com.boo.app.api.GetDataTask;
import com.boo.app.api.OnTaskCompleted;
import com.boo.app.model.UserObject;
import com.boo.app.ui.adapter.BooedUserAdapter;
import com.boo.app.ui.adapter.UserAdapter;
import com.boo.app.utility.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TagUser extends BaseActivity implements OnTaskCompleted, UserAdapter.OnItemClickListener {
    private EditText mSearch;
    private ImageView mClose;
    private RecyclerView mRecyclerView;

    private ArrayList<UserObject> users;
    private UserAdapter mUserAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_user);

        users = new ArrayList<>();
        mUserAdapter = new UserAdapter(this, users, this);

        initUI();
    }

    private void initUI() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        mSearch = _findViewById(R.id.et_tag_user_search);
        mSearch.requestFocus();
        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0)
                    loadUsers(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        _findViewById(R.id.iv_tag_user_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClose();
            }
        });
        mRecyclerView = _findViewById(R.id.rv_tag_user);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mUserAdapter);
    }

    private void onClose() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    private void loadUsers(String strSearch) {
        if ((strSearch.trim()).isEmpty()) return;

        String url = URL_SERVER + URL_GET_TAGGED_USERS;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(TAG_STR, strSearch);

            new GetDataTask(url, jsonRequest, this).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void displayUsers(ArrayList<UserObject> userObjects) {
        users.clear();
        users.addAll(userObjects);
        mUserAdapter.notifyDataSetChanged();
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
    public void itemClicked(UserObject refUser) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(USER_NAME, refUser.user_name);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
