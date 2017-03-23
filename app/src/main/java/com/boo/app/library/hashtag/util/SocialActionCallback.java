package com.boo.app.library.hashtag.util;

import com.boo.app.library.hashtag.annotation.SocialActionIntDef;

public interface SocialActionCallback
{
    void onMatch(@SocialActionIntDef int type, String value);
}
