package com.boo.app.library.hashtag.util;

public interface SocialActionHandler {
    public void handleHashtag(String hashtag);
    public void handleMention(String mention);
    public void handleEmail(String email);
    public void handleUrl(String url);
}
