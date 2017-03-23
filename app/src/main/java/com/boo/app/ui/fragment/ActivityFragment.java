package com.boo.app.ui.fragment;

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
import com.boo.app.R;
import com.boo.app.api.GetDataTask;
import com.boo.app.api.OnTaskCompleted;
import com.boo.app.model.ActivityObject;
import com.boo.app.model.UserObject;
import com.boo.app.ui.activity.Detail;
import com.boo.app.ui.activity.Main;
import com.boo.app.ui.adapter.ActivityAdapter;
import com.boo.app.utility.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ActivityFragment extends BaseFragment implements ActivityAdapter.ActionListener, OnTaskCompleted{
    private Context context;

    private RecyclerView mRecyclerView;

    private ProgressDialog mProgressDialog;

    private int request_type;
    private ActivityObject selectedActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_activity, container, false);

        findViews(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
    }

    private void findViews(View v) {
        mRecyclerView = (RecyclerView) v.findViewById(R.id.rv_activity);
    }

    private void init() {
        context = getContext();

        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setCancelable(false);


//        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);

        loadActivities();
    }

    private void loadActivities() {
        request_type = 0;
        String url = URL_SERVER + URL_ACTIVITIES;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(USER_ID, ((App) context.getApplicationContext()).getCurrentUser().user_id);

            mProgressDialog.setMessage("Loading...");
            mProgressDialog.show();

            new GetDataTask(url, jsonRequest, this).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTaskSuccess(JSONObject jsonResponse) {
        mProgressDialog.dismiss();

        if (request_type == 0) {
            try {
                UserObject user = new UserObject(jsonResponse.getJSONObject(CURRENT_USER));
                user.saveOnDisk(context);
                ((App) context.getApplicationContext()).setCurrentUser(user);

                JSONArray activities = jsonResponse.getJSONArray(ACTIVITIES);
                ArrayList<ActivityObject> result = new ArrayList<>();
                for (int i = 0; i < activities.length(); i++) {
                    result.add(new ActivityObject(activities.getJSONObject(i)));
                }

                displayActivities(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            switch (request_type) {
                case 1:
                    UserObject refUser = new UserObject(selectedActivity);
                    ft.replace(R.id.content_frame, BootiqueFragment.newInstance(refUser), "");
                    ft.commit();
                    ((Main)getActivity()).changeButtonColor(9);

                    break;
                case 2:
                    UserObject currentUser = ((App)context.getApplicationContext()).getCurrentUser();
                    Intent intent;
                    int type = selectedActivity.getActivity_type();
                    switch (type) {
                        case 1:
                            ft.replace(R.id.content_frame, BootiqueFragment.newInstance(currentUser), "");
                            ft.commit();
                            ((Main)getActivity()).changeButtonColor(9);
                            break;
                        default:
                            intent = new Intent(context, Detail.class);
                            intent.putExtra(POST_ID, selectedActivity.getActivity_post_id());
                            startActivity(intent);
                            break;
                    }
                    break;
            }
        }
    }

    @Override
    public void onTaskError(String msg) {
        mProgressDialog.dismiss();
        Utility.showToast(getContext(), msg);
    }

    private void displayActivities(ArrayList<ActivityObject> result) {
        if (result.size() > 0) {
            ActivityAdapter mActivityAdapter = new ActivityAdapter(context, result, this);
            mRecyclerView.setAdapter(mActivityAdapter);
        }
    }

    @Override
    public void onAvatar(ActivityObject activityObject) {
        request_type = 1;
        selectedActivity = activityObject;
        activityDone();
    }

    @Override
    public void onName(ActivityObject activityObject) {
        onAvatar(activityObject);
    }

    @Override
    public void onYou(ActivityObject activityObject) {
        request_type = 2;
        selectedActivity = activityObject;
        activityDone();
    }

    private void activityDone() {
        String url = URL_SERVER + URL_ACTIVITY_DONE;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(USER_ID, ((App) context.getApplicationContext()).getCurrentUser().user_id);
            jsonRequest.put(ACTIVITY_TYPE, selectedActivity.getActivity_type());
            jsonRequest.put(ACTIVITY_REF_ID, selectedActivity.getActivity_ref_id());
            jsonRequest.put(ACTIVITY_POST_ID, selectedActivity.getActivity_post_id());

            mProgressDialog.setMessage("Loading...");
            mProgressDialog.show();

            new GetDataTask(url, jsonRequest, this).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}