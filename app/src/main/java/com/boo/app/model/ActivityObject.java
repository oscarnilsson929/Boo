package com.boo.app.model;

import com.boo.app.AppConfig;

import org.json.JSONObject;

import java.util.Date;

public class ActivityObject extends BaseObject implements AppConfig{
    private long   activity_id;
    private int    activity_type;
    private long   activity_ref_id;
    private long   activity_post_id;
    private long   activity_user_id;
    private String activity_user_full_name;
    private String activity_user_photo_url;
    private Date   activity_date;
    private long   activity_time_diff;


    public ActivityObject(JSONObject jsonObject)  {
        this.activity_id             = getLongFromJSONObject(jsonObject, ACTIVITY_ID);
        this.activity_type           = getIntFromJSONObject(jsonObject, ACTIVITY_TYPE);
        this.activity_ref_id         = getLongFromJSONObject(jsonObject, ACTIVITY_REF_ID);
        this.activity_post_id        = getLongFromJSONObject(jsonObject, ACTIVITY_POST_ID);
        this.activity_user_id        = getLongFromJSONObject(jsonObject, ACTIVITY_USER_ID);
        this.activity_user_full_name = getStringFromJSONObject(jsonObject, ACTIVITY_USER_FULL_NAME);
        this.activity_user_photo_url = getStringFromJSONObject(jsonObject, ACTIVITY_USER_PHOTO_URL);
        this.activity_date           = getDateFromJSONObject(jsonObject, ACTIVITY_DATE);
        this.activity_time_diff      = getLongFromJSONObject(jsonObject, ACTIVITY_TIME_DIFF);
    }

    public long getActivity_id() {
        return activity_id;
    }

    public int getActivity_type() {
        return activity_type;
    }

    public long getActivity_ref_id() {
        return activity_ref_id;
    }

    public long getActivity_post_id() {
        return activity_post_id;
    }

    public long getActivity_user_id() {
        return activity_user_id;
    }

    public String getActivity_user_full_name() {
        return activity_user_full_name;
    }

    public String getActivity_user_photo_url() {
        return activity_user_photo_url;
    }

    public Date getActivity_date() {
        return activity_date;
    }

    public long getActivity_time_diff() {
        return activity_time_diff;
    }
}
