package com.boo.app.model;

import android.content.Context;

import com.boo.app.AppConfig;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class UserObject extends BaseObject implements Serializable, AppConfig{

    public long   user_id;
    public String user_name;
    public String user_full_name;
    public String user_email;
    public String user_bio;
    public String user_photo_url;
    public String user_location_address;
    public double user_location_latitude;
    public double user_location_longitude;
    public int    user_booed_count;
    public int    user_booing_count;
    public int    user_followers_count;
    public int    user_following_count;
    public int    user_posts_count;
    public int    is_followed_by_me;

//    Default Constructor
    public UserObject() {}

//    Constructor
    public UserObject(JSONObject jsonObject) {
        this.user_id                 = getLongFromJSONObject  (jsonObject, USER_ID);
        this.user_name               = getStringFromJSONObject(jsonObject, USER_NAME);
        this.user_full_name          = getStringFromJSONObject(jsonObject, USER_FULL_NAME);
        this.user_email              = getStringFromJSONObject(jsonObject, USER_EMAIL);
        this.user_bio                = getStringFromJSONObject(jsonObject, USER_BIO);
        this.user_photo_url          = getStringFromJSONObject(jsonObject, USER_PHOTO_URL);
        this.user_location_address   = getStringFromJSONObject(jsonObject, USER_LOCATION_ADDRESS);
        this.user_location_latitude  = getDoubleFromJSONObject(jsonObject, USER_LOCATION_LATITUDE);
        this.user_location_longitude = getDoubleFromJSONObject(jsonObject, USER_LOCATION_LONGITUDE);
        this.user_booed_count        = getIntFromJSONObject   (jsonObject, USER_BOOED_COUNT);
        this.user_booing_count       = getIntFromJSONObject   (jsonObject, USER_BOOING_COUNT);
        this.user_followers_count    = getIntFromJSONObject   (jsonObject, USER_FOLLOWERS_COUNT);
        this.user_following_count    = getIntFromJSONObject   (jsonObject, USER_FOLLOWING_COUNT);
        this.user_posts_count        = getIntFromJSONObject   (jsonObject, USER_POSTS_COUNT);
        this.is_followed_by_me       = getIntFromJSONObject   (jsonObject, IS_FOLLOWED_BY_ME);
    }

    public UserObject(PostObject postObject) {
        user_id        = postObject.getPost_user_id();
        user_name      = postObject.getPost_user_name();
        user_full_name = postObject.getPost_user_full_name();
        user_photo_url = postObject.getPost_user_photo_url();
    }

    public UserObject(ActivityObject activityObject) {
        user_id        = activityObject.getActivity_user_id();
        user_full_name = activityObject.getActivity_user_full_name();
        user_photo_url = activityObject.getActivity_user_photo_url();
    }

    public UserObject(CommentObject commentObject) {
        user_id        = commentObject.getComment_user_id();
        user_full_name = commentObject.getComment_user_full_name();
        user_photo_url = commentObject.getComment_user_photo_url();
        user_name      = commentObject.getComment_user_name();
    }

    public void saveOnDisk(Context context) {
        FileOutputStream fos;
        try {
            fos = context.openFileOutput(CURRENT_USER, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(this);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static UserObject loadFromDisk(Context context) {
        FileInputStream fis;
        try {
            fis = context.openFileInput(CURRENT_USER);
            ObjectInputStream is = new ObjectInputStream(fis);
            UserObject user = (UserObject)is.readObject();
            is.close();
            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteFromDisk(Context context) {
        context.deleteFile(CURRENT_USER);
    }
}
