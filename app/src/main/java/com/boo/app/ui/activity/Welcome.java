package com.boo.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.boo.app.R;
import com.boo.app.ui.adapter.WelcomeAdapter;
import com.viewpagerindicator.CirclePageIndicator;

import mehdi.sakout.fancybuttons.FancyButton;

public class Welcome extends BaseActivity {
    private ViewPager mPager;
    private CirclePageIndicator titleIndicator;

    private FancyButton mLoginBtn, mSignUpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        findViews();
        init();
    }

    private void findViews() {
        mPager         = _findViewById(R.id.welcome_pager);
        titleIndicator = _findViewById(R.id.indicator);
        mLoginBtn      = _findViewById(R.id.btn_welcome_login);
        mSignUpBtn     = _findViewById(R.id.btn_welcome_signup);
    }

    private void init() {
        mPager.setAdapter(new WelcomeAdapter(getSupportFragmentManager()));
        titleIndicator.setViewPager(mPager);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Welcome.this, Login.class));
                Welcome.this.finish();
            }
        });

        mSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Welcome.this, SignUp.class);
                intent.putExtra(FROM, 0);
                startActivity(intent);
                Welcome.this.finish();
            }
        });
    }
}
