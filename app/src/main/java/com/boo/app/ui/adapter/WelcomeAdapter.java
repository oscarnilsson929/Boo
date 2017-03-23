package com.boo.app.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.boo.app.R;
import com.boo.app.ui.fragment.WelcomeFragment;

public class WelcomeAdapter extends FragmentStatePagerAdapter {
    public WelcomeAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return WelcomeFragment.newInstance(R.drawable.img_bg_page_1, R.string.page_content);
            default:
                return WelcomeFragment.newInstance(R.drawable.img_bg_page_2, R.string.page_content);
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
