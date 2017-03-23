package com.boo.app.api;

public interface HttpUrlManager {
    String API_KEY = "03c48c1b03bde4946f63150e8c0ebc64";
    String CONTENT_TYPE = "application/json; charset=UTF-8";

//  Base API urls
    String URL_SERVER        = "http://www.boo-it.com/";
//    String URL_SERVER        = "http://172.16.1.210:8080/boo/";
    String MEDIA_USERS       = "assets/media/users/";
    String MEDIA_PHOTOS      = "assets/media/post_photos/";
    String MEDIA_VIDEOS      = "assets/media/post_videos/";
    String MEDIA_VIDEO_THUMB = "assets/media/post_video_thumbs/";


//    API URLs
    String URL_MANUAL_LOGIN         = "api/user_manual_login";
    String URL_MANUAL_SIGNUP        = "api/user_manual_signup";
    String URL_SOCIAL_SIGNUP        = "api/user_social_signup";
    String URL_MANAGE_MY_POSTS      = "api/manage_my_posts";
    String URL_BOO_POST             = "api/boo_post";
    String URL_COMMENT_POST         = "api/comment_post";
    String URL_ACTIVITIES           = "api/activities";
    String URL_ACTIVITY_DONE        = "api/activity_done";
    String URL_USER_CHANGE_PASSWORD = "api/user_change_password";
    String URL_FOLLOW_USER          = "api/follow_user";
    String URL_USER_PROFILE_UPDATE  = "api/user_profile_update";
    String URL_USER_PROFILE_PHOTO_UPDATE  = "api/user_profile_photo_update";
    String URL_GET_FEED_POSTS       = "api/get_feed_posts";
    String URL_GET_POST             = "api/get_post";
    String URL_GET_NEARBY_POSTS     = "api/get_nearby_posts";
    String URL_GET_GLOBAL_POSTS     = "api/get_global_posts";
    String URL_GET_PROFILE          = "api/get_profile";
    String URL_GET_USER_POSTS       = "api/get_user_posts";
    String URL_GET_FOLLOWING        = "api/get_following";
    String URL_GET_FOLLOWERS        = "api/get_followers";
    String URL_GET_BOOERS           = "api/get_booers";
    String URL_GET_TAGGED_USERS     = "api/get_tagged_users";
    String URL_GET_TAGGED_POSTS     = "api/get_tagged_posts";
    String URL_GET_USER_BY_NAME     = "api/get_user_by_name";
    String URL_SEND_EMAIL           = "api/send_email";
}
