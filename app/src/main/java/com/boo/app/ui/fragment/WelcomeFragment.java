package com.boo.app.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boo.app.R;

public class WelcomeFragment extends Fragment {
    private static final String BACKGROUND_IMAGE    = "backgroundImage";
    private static final String CONTENT             = "content";

    private int mBackgroundImageId, mContentId;

    public static WelcomeFragment newInstance(int backgroundImageId, int contentId) {
        WelcomeFragment frag = new WelcomeFragment();
        Bundle b = new Bundle();
        b.putInt(BACKGROUND_IMAGE, backgroundImageId);
        b.putInt(CONTENT, contentId);
        frag.setArguments(b);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getArguments().containsKey(BACKGROUND_IMAGE))
            throw new RuntimeException("Fragment must contain a \"" + BACKGROUND_IMAGE + "\" argument!");
        mBackgroundImageId = getArguments().getInt(BACKGROUND_IMAGE);

        if (!getArguments().containsKey(CONTENT))
            throw new RuntimeException("Fragment must contain a \"" + CONTENT + "\" argument!");
        mContentId = getArguments().getInt(CONTENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_welcome, container, false);

        ImageView ivBackground = (ImageView)view.findViewById(R.id.iv_welcome);
        ivBackground.setImageResource(mBackgroundImageId);

        TextView tvContent = (TextView) view.findViewById(R.id.tv_welcome_content);
        tvContent.setText(mContentId);
        return view;
    }
}