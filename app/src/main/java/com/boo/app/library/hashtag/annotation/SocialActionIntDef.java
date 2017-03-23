package com.boo.app.library.hashtag.annotation;

import com.boo.app.library.hashtag.util.SocialActionType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SocialActionDef(
        {
                SocialActionType.HASH_TAG,
                SocialActionType.MENTION,
                SocialActionType.EMAIL,
                SocialActionType.URL
        })
@Retention(RetentionPolicy.SOURCE)
public @interface SocialActionIntDef
{
}
