package com.boo.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.boo.app.utility.Utility;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public abstract class SocialActivity extends BaseActivity {
    CallbackManager callbackManager;
    TwitterAuthClient mTwitterAuthClient;

    private String strFacebookId, strTwitterId;
    private String strEmail, strFirstName, strLastName, strPhotoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTwitterAuthClient = new TwitterAuthClient();
        callbackManager = CallbackManager.Factory.create();
    }

    protected void facebookLogIn() {
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(APP_NAME, "Facebook login : Success");
                Utility.showToast(SocialActivity.this, "Facebook Login : Success");

                fetchFacebookUser();
            }

            @Override
            public void onCancel() {
                Log.d(APP_NAME, "Facebook login : Cancel");
                Utility.showToast(SocialActivity.this, "Facebook Login : Cancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(APP_NAME, "Facebook login : Error : " + error.getMessage());
                Utility.showToast(SocialActivity.this, "Facebook Login : Error : " + error.getMessage());

                if (error instanceof FacebookAuthorizationException) {
                    if (AccessToken.getCurrentAccessToken() != null) {
                        LoginManager.getInstance().logOut();
                    }
                }
            }
        });

        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
    }

    protected void fetchFacebookUser() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject user, GraphResponse graphResponse) {
                if (user != null) {
                    onFacebookUserCompleted(user);
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,first_name,last_name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    protected void onFacebookUserCompleted(JSONObject user) {
        try {
            strFacebookId = user.getString("id");
            strEmail      = user.getString("email");
            strFirstName  = user.getString("first_name");
            strLastName   = user.getString("last_name");
            strPhotoUrl   = "https://graph.facebook.com/" + strFacebookId + "/picture?type=large";

            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put(USER_EMAIL, strEmail);
            jsonRequest.put(USER_FIRST_NAME, strFirstName);
            jsonRequest.put(USER_LAST_NAME, strLastName);
            jsonRequest.put(USER_PHOTO_URL, strPhotoUrl);
            jsonRequest.put(USER_FACEBOOK_ID, strFacebookId);

            Log.d(APP_NAME + " : Facebook User", jsonRequest.toString());

            onDataReady(jsonRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void twitterLogin() {
        mTwitterAuthClient.authorize(this, new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> twitterSessionResult) {
                Log.d(APP_NAME, "Twitter Authorize : Success");
                Utility.showToast(SocialActivity.this, "Twitter Authorize : Success");

                mTwitterAuthClient.requestEmail(twitterSessionResult.data, new Callback<String>() {
                    @Override
                    public void success(Result<String> result) {
                        Log.d(APP_NAME, "Twitter RequestEmail : Success");
                        Utility.showToast(SocialActivity.this, "Twitter RequestEmail : Success");

                        fetchTwitterUser(result.data);
                    }

                    @Override
                    public void failure(TwitterException e) {
                        Log.d(APP_NAME, "Twitter RequestEmail : Failure : " + e.getMessage());
                        Utility.showToast(SocialActivity.this, "Twitter RequestEmail : Failure : " + e.getMessage());

                        fetchTwitterUser("");
                    }
                });
            }

            @Override
            public void failure(TwitterException e) {
                Log.d(APP_NAME, "Twitter Authorize : Failure : " + e.getMessage());
                Utility.showToast(SocialActivity.this, "Twitter Authorize : Failure : " + e.getMessage());
            }
        });
    }

    private void fetchTwitterUser(final String email) {
        Twitter.getApiClient().getAccountService().verifyCredentials(true, false, new Callback<com.twitter.sdk.android.core.models.User>() {
            @Override
            public void success(Result<com.twitter.sdk.android.core.models.User> result) {
                Log.d(APP_NAME, "Twitter verifyCredentials : Success");
                Utility.showToast(SocialActivity.this, "Twitter verifyCredentials : Success");

                try {
                    strTwitterId = String.valueOf(result.data.getId());
                    String name = result.data.name;
                    String[] splited = name.split("\\s+");
                    switch (splited.length) {
                        case 1:
                            strFirstName = name.substring(0, 1);
                            strLastName = name.substring(1);
                            break;
                        case 2:
                            strFirstName = splited[0];
                            strLastName = splited[1];
                            break;
                        default:
                            strFirstName = splited[0];
                            for (int i = 1; i < splited.length; i++) {
                                strLastName += splited[i];
                            }
                            break;
                    }
                    strPhotoUrl = result.data.profileImageUrl;

                    if (email.isEmpty()) {
                        strEmail = result.data.screenName + "@boo.com";
                    } else {
                        strEmail = email;
                    }

                    JSONObject jsonRequest = new JSONObject();

                    jsonRequest.put(USER_EMAIL, strEmail);
                    jsonRequest.put(USER_FIRST_NAME, strFirstName);
                    jsonRequest.put(USER_LAST_NAME, strLastName);
                    jsonRequest.put(USER_PHOTO_URL, strPhotoUrl);
                    jsonRequest.put(USER_TWITTER_ID, strTwitterId);

                    Log.d(APP_NAME + " : Twitter Data", jsonRequest.toString());

                    onDataReady(jsonRequest);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failure(TwitterException e) {
                Log.d(APP_NAME, "Twitter verifyCredentials : Failure : " + e.getMessage());
                Utility.showToast(SocialActivity.this, "Twitter verifyCredentials : Failure : " + e.getMessage());
            }
        });
    }

    public abstract void onDataReady(JSONObject data);

    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (mTwitterAuthClient.getRequestCode() == requestCode) {
            mTwitterAuthClient.onActivityResult(requestCode, responseCode, intent);
            return;
        }

        callbackManager.onActivityResult(requestCode, responseCode, intent);
    }
}
