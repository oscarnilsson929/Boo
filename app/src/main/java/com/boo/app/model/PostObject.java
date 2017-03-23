package com.boo.app.model;

import com.boo.app.AppConfig;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

public class PostObject extends BaseObject implements AppConfig, Serializable{
    private long    post_id;
    private Date    post_date;
    private long    post_user_id;
    private String  post_user_photo_url;
    private String  post_user_full_name;
    private String  post_user_name;
    private String  post_text;
    private String  post_photo_url;
    private String  post_video_url;
    private String  post_video_thumb_url;
    private String  post_location_address;
    private double  post_location_latitude;
    private double  post_location_longitude;
    private int     post_booed_count;
    private int     post_comments_count;
    private long    post_time_diff;
    private int     is_booed_by_me;

    private int     post_type;

    public PostObject(JSONObject jsonObject) {
        this.post_id                 = getLongFromJSONObject(jsonObject, POST_ID);
        this.post_date               = getDateFromJSONObject(jsonObject, POST_DATE);
        this.post_user_id            = getLongFromJSONObject(jsonObject, POST_USER_ID);
        this.post_user_photo_url     = getStringFromJSONObject(jsonObject, POST_USER_PHOTO_URL);
        this.post_user_full_name     = getStringFromJSONObject(jsonObject, POST_USER_FULL_NAME);
        this.post_user_name          = getStringFromJSONObject(jsonObject, POST_USER_NAME);
        this.post_text               = getStringFromJSONObject(jsonObject, POST_TEXT);
        this.post_photo_url          = getStringFromJSONObject(jsonObject, POST_PHOTO_URL);
        this.post_video_url          = getStringFromJSONObject(jsonObject, POST_VIDEO_URL);
        this.post_video_thumb_url    = getStringFromJSONObject(jsonObject, POST_VIDEO_THUMB_URL);
        this.post_location_address   = getStringFromJSONObject(jsonObject, POST_LOCATION_ADDRESS);
        this.post_location_latitude  = getDoubleFromJSONObject(jsonObject, POST_LOCATION_LATITUDE);
        this.post_location_longitude = getDoubleFromJSONObject(jsonObject, POST_LOCATION_LONGITUDE);
        this.post_booed_count        = getIntFromJSONObject(jsonObject, POST_BOOED_COUNT);
        this.post_comments_count     = getIntFromJSONObject(jsonObject, POST_COMMENTS_COUNT);
        this.post_time_diff          = getLongFromJSONObject(jsonObject, POST_TIME_DIFF);
        this.is_booed_by_me          = getIntFromJSONObject(jsonObject, IS_BOOED_BY_ME);
    }

    public long getPost_id() {
        return post_id;
    }

    public void setPost_id(long post_id) {
        this.post_id = post_id;
    }

    public Date getPost_date() {
        return post_date;
    }

    public void setPost_date(Date post_date) {
        this.post_date = post_date;
    }

    public long getPost_user_id() {
        return post_user_id;
    }

    public void setPost_user_id(long post_user_id) {
        this.post_user_id = post_user_id;
    }

    public String getPost_user_photo_url() {
        return post_user_photo_url;
    }

    public void setPost_user_photo_url(String post_user_photo_url) {
        this.post_user_photo_url = post_user_photo_url;
    }

    public String getPost_user_full_name() {
        return post_user_full_name;
    }

    public void setPost_user_full_name(String post_user_full_name) {
        this.post_user_full_name = post_user_full_name;
    }

    public String getPost_user_name() {
        return post_user_name;
    }

    public void setPost_user_name(String post_user_name) {
        this.post_user_name = post_user_name;
    }

    public String getPost_text() {
        return post_text;
    }

    public void setPost_text(String post_text) {
        this.post_text = post_text;
    }

    public String getPost_photo_url() {
        return post_photo_url;
    }

    public void setPost_photo_url(String post_photo_url) {
        this.post_photo_url = post_photo_url;
    }

    public String getPost_video_url() {
        return post_video_url;
    }

    public void setPost_video_url(String post_video_url) {
        this.post_video_url = post_video_url;
    }

    public String getPost_video_thumb_url() {
        return post_video_thumb_url;
    }

    public void setPost_video_thumb_url(String post_video_thumb_url) {
        this.post_video_thumb_url = post_video_thumb_url;
    }

    public String getPost_location_address() {
        return post_location_address;
    }

    public void setPost_location_address(String post_location_address) {
        this.post_location_address = post_location_address;
    }

    public double getPost_location_latitude() {
        return post_location_latitude;
    }

    public void setPost_location_latitude(double post_location_latitude) {
        this.post_location_latitude = post_location_latitude;
    }

    public double getPost_location_longitude() {
        return post_location_longitude;
    }

    public void setPost_location_longitude(double post_location_longitude) {
        this.post_location_longitude = post_location_longitude;
    }

    public int getPost_booed_count() {
        return post_booed_count;
    }

    public void setPost_booed_count(int post_booed_count) {
        this.post_booed_count = post_booed_count;
    }

    public int getPost_comments_count() {
        return post_comments_count;
    }

    public void setPost_comments_count(int post_comments_count) {
        this.post_comments_count = post_comments_count;
    }

    public long getPost_time_diff() {
        return post_time_diff;
    }

    public void setPost_time_diff(long post_time_diff) {
        this.post_time_diff = post_time_diff;
    }

    public int getIs_booed_by_me() {
        return is_booed_by_me;
    }

    public void setIs_booed_by_me(int is_booed_by_me) {
        this.is_booed_by_me = is_booed_by_me;
    }

    public int getPost_type() {
        return post_type;
    }

    public void setPost_type(int post_type) {
        this.post_type = post_type;
    }
}
