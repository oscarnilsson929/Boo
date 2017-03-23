package com.boo.app.model;

import com.boo.app.AppConfig;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

public class CommentObject extends BaseObject implements AppConfig, Serializable{
    private long   comment_id;
    private long   comment_post_id;
    private long   comment_time_diff;
    private String comment_content;
    private Date   comment_date;
    private long   comment_user_id;
    private String comment_user_photo_url;
    private String comment_user_full_name;
    private String comment_user_name;


    public CommentObject(JSONObject jsonObject)  {
        this.comment_id             = getLongFromJSONObject(jsonObject, COMMENT_ID);
        this.comment_post_id        = getLongFromJSONObject(jsonObject, COMMENT_POST_ID);
        this.comment_time_diff      = getLongFromJSONObject(jsonObject, COMMENT_TIME_DIFF);
        this.comment_content        = getStringFromJSONObject(jsonObject, COMMENT_CONTENT);
        this.comment_date           = getDateFromJSONObject(jsonObject, COMMENT_DATE);
        this.comment_user_id        = getLongFromJSONObject(jsonObject, COMMENT_USER_ID);
        this.comment_user_photo_url = getStringFromJSONObject(jsonObject, COMMENT_USER_PHOTO_URL);
        this.comment_user_full_name = getStringFromJSONObject(jsonObject, COMMENT_USER_FULL_NAME);
        this.comment_user_name      = getStringFromJSONObject(jsonObject, COMMENT_USER_NAME);
    }

    public long getComment_id() {
        return comment_id;
    }

    public long getComment_post_id() {
        return comment_post_id;
    }

    public long getComment_time_diff() {
        return comment_time_diff;
    }

    public String getComment_content() {
        return comment_content;
    }

    public Date getComment_date() {
        return comment_date;
    }

    public long getComment_user_id() {
        return comment_user_id;
    }

    public String getComment_user_photo_url() {
        return comment_user_photo_url;
    }

    public String getComment_user_full_name() {
        return comment_user_full_name;
    }

    public String getComment_user_name() {
        return comment_user_name;
    }
}
