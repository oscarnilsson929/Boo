package com.boo.app.api;

import android.os.AsyncTask;

import com.boo.app.AppConfig;

import org.json.JSONException;
import org.json.JSONObject;

public class GetDataTask extends AsyncTask<Void, Void, JSONObject>{
    private OnTaskCompleted listener;
    private String url;
    private JSONObject jsonRequest;
    private String msg;

    public GetDataTask(String url, JSONObject jsonRequest, OnTaskCompleted listener) {
        this.listener = listener;
        this.url = url;
        this.jsonRequest = jsonRequest;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        JSONObject jsonResponse, result = null;
        try {
            jsonResponse = Connect.getInstance().getJSONObject(url, jsonRequest);

            if (jsonResponse != null) {
                if (jsonResponse.getInt(AppConfig.STATUS) == 1) {
                    result = jsonResponse;
                } else {
                    msg = jsonResponse.getString(AppConfig.MESSAGE);
                }
            } else {
                msg = "Please check your internet connection";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        if (result != null) {
            listener.onTaskSuccess(result);
        }
        else {
            listener.onTaskError(msg);
        }
    }
}
