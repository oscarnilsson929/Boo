package com.boo.app;

public interface AppConfig {
    String APP_NAME = "Boo";

    int SPLASH_TIME_OUT = 2000;
    int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    String google_map_api_key = "AIzaSyCh9y9S9eatFVpkeFRKgq3VC9ICWjG2vBU";

//    User Object
    String CURRENT_USER = "current_user";
    String REF_USER = "ref_user";
    String USER_GCM_ID = "user_gcm_id";

    String USER_ID                 = "user_id";
    String USER_NAME               = "user_name";
    String USER_FIRST_NAME         = "user_first_name";
    String USER_LAST_NAME          = "user_last_name";
    String USER_FULL_NAME          = "user_full_name";
    String USER_EMAIL              = "user_email";
    String USER_BIO                = "user_bio";
    String USER_PHOTO              = "user_photo";
    String USER_PHOTO_URL          = "user_photo_url";
    String USER_LOCATION_ADDRESS   = "user_location_address";
    String USER_LOCATION_LATITUDE  = "user_location_latitude";
    String USER_LOCATION_LONGITUDE = "user_location_longitude";
    String USER_BOOED_COUNT        = "user_booed_count";
    String USER_BOOING_COUNT       = "user_booing_count";
    String USER_FOLLOWERS_COUNT    = "user_followers_count";
    String USER_FOLLOWING_COUNT    = "user_following_count";
    String USER_POSTS_COUNT        = "user_posts_count";
    String USER_PASSWORD           = "user_password";
    String USER_OLD_PASSWORD       = "user_old_password";
    String USER_NEW_PASSWORD       = "user_new_password";
    String SIGNUP_MODE             = "signup_mode";
    String USER_FACEBOOK_ID        = "user_facebook_id";
    String USER_TWITTER_ID         = "user_twitter_id";
    String IS_FOLLOWED_BY_ME       = "is_followed_by_me";

    String USERS                   = "users";

//    Feed
    String POSTS = "posts";
    String POST_ID = "post_id";
    String POST_DATE = "post_date";
    String POST_USER_ID = "post_user_id";
    String POST_USER_PHOTO_URL = "post_user_photo_url";
    String POST_USER_FULL_NAME = "post_user_full_name";
    String POST_USER_NAME = "post_user_name";
    String POST_TEXT = "post_text";
    String POST_PHOTO = "post_photo";
    String POST_PHOTO_URL = "post_photo_url";
    String POST_VIDEO = "post_video";
    String POST_VIDEO_URL = "post_video_url";
    String POST_VIDEO_THUMB = "post_video_thumb";
    String POST_VIDEO_THUMB_URL = "post_video_thumb_url";
    String POST_LOCATION_ADDRESS = "post_location_address";
    String POST_LOCATION_LATITUDE = "post_location_latitude";
    String POST_LOCATION_LONGITUDE = "post_location_longitude";
    String POST_BOOED_COUNT = "post_booed_count";
    String POST_COMMENTS_COUNT = "post_comments_count";
    String POST_TIME_DIFF = "post_time_diff";
    String IS_BOOED_BY_ME = "is_booed_by_me";
    String MANAGE_TYPE = "manage_type";
    String REF_POST = "ref_post";
    String REF_ID = "ref_id";

//    Comment
    String POST_COMMENTS          = "post_comments";
    String COMMENT_ID             = "comment_id";
    String COMMENT_POST_ID        = "comment_post_id";
    String COMMENT_TIME_DIFF      = "comment_time_diff";
    String COMMENT_CONTENT        = "comment_content";
    String COMMENT_DATE           = "comment_date";
    String COMMENT_USER_ID        = "comment_user_id";
    String COMMENT_USER_PHOTO_URL = "comment_user_photo_url";
    String COMMENT_USER_FULL_NAME = "comment_user_full_name";
    String COMMENT_USER_NAME      = "comment_user_name";

//    Activity
    String ACTIVITIES              = "activities";
    String ACTIVITY_TYPE           = "activity_type";
    String ACTIVITY_REF_ID         = "activity_ref_id";
    String ACTIVITY_POST_ID        = "activity_post_id";
    String ACTIVITY_USER_ID        = "activity_user_id";
    String ACTIVITY_USER_FULL_NAME = "activity_user_full_name";
    String ACTIVITY_USER_PHOTO_URL = "activity_user_photo_url";
    String ACTIVITY_DATE           = "activity_date";
    String ACTIVITY_TIME_DIFF      = "activity_time_diff";
    String ACTIVITY_ID             = "activity_id";

//    General
    String DATA   = "data";
    String MODE   = "mode";
    String ADD    = "add";
    String DELETE = "delete";
    String SEARCH_OFFSET = "search_offset";
    String SEARCH_LIMIT  = "search_limit";
    String POSITION      = "position";
    String TAG_STR       = "tag_str";
    String SUBJECT       = "subject";
    String MESSAGE_CONTENT = "message";

    int PICK_PHOTO_CAMERA  = 1;
    int PICK_PHOTO_GALLERY = 2;
    int PICK_VIDEO_GALLERY = 3;
    int PICK_VIDEO_CAMERA  = 4;
    int REQUEST_COMMENT    = 5;
    int REQUEST_FOLLOW     = 6;
    int REQUEST_TAG        = 7;
    int REQUEST_POST_BOO   = 8;
    int PERMISSIONS_REQUEST_PHOTO_CAMERA          = 100;
    int PERMISSIONS_REQUEST_VIDEO_CAMERA          = 101;
    int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 102;
    int PERMISSIONS_REQUEST_LOCATION              = 103;
    int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 104;

    int VIDEO_DURATION_LIMIT = 10;

//    Response
    String MESSAGE = "msg";
    String STATUS  = "status";

    int MAX_WIDTH  = 512;
    int MAX_HEIGHT = 512;

    int search_limit = 30;

    String TYPE = "type";
    String FROM = "from";

    String GCM_LISTENER = "GCM_Listener";
}
