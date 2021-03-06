package com.boo.app.library.hashtag.util;

import android.text.Layout;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Patterns;
import android.view.MotionEvent;

public class SocialMovementMethod extends LinkMovementMethod {

    public static final String SOCIAL_UI_BASE_SCHEME = "http://www.socialui.carrotcreative.com";
    public static final String SOCIAL_UI_HASHTAG_SCHEME = SOCIAL_UI_BASE_SCHEME + "/hashtag";
    public static final String SOCIAL_UI_MENTION_SCHEME = SOCIAL_UI_BASE_SCHEME + "/mention";

    // ===== Class =====

    private SocialActionCallback mSocialActionCallback;

    public SocialMovementMethod(SocialActionCallback socialActionCallback)
    {
        super();
        mSocialActionCallback = socialActionCallback;
    }

    public boolean onTouchEvent(android.widget.TextView widget, android.text.Spannable buffer, MotionEvent event)
    {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP)
        {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);
            if (link.length != 0)
            {
                String url = link[0].getURL();
                handleURL(url);
                return true;
            }
        }
        return super.onTouchEvent(widget, buffer, event);
    }

    private void handleURL(String url)
    {
        if(url.startsWith(SOCIAL_UI_HASHTAG_SCHEME))
        {
            String hashtag = url.replaceFirst(SOCIAL_UI_HASHTAG_SCHEME, "");
            hashtag = hashtag.replaceFirst(".*#", "");
            mSocialActionCallback.onMatch(SocialActionType.HASH_TAG, hashtag);
        }
        else if(url.startsWith(SOCIAL_UI_MENTION_SCHEME))
        {
            String mention = url.replaceFirst(SOCIAL_UI_MENTION_SCHEME, "");
            mention = mention.replaceFirst(".*@", "");
            mSocialActionCallback.onMatch(SocialActionType.MENTION, mention);
        }
        else if(Patterns.EMAIL_ADDRESS.matcher(url).matches())
        {
            mSocialActionCallback.onMatch(SocialActionType.EMAIL, url);
        }
        else
        {
            mSocialActionCallback.onMatch(SocialActionType.URL, url);
        }
    }

}
