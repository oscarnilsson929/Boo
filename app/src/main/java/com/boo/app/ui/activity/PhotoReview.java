package com.boo.app.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.boo.app.R;
import com.boo.app.utility.BitmapTransform;
import com.boo.app.utility.Utility;
import com.squareup.picasso.Picasso;

public class PhotoReview extends BaseActivity{
    private ImageView ivPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_review);

        initUI();
    }

    private void initUI() {
        ivPhoto = _findViewById(R.id.iv_photo_review);

        ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        displayPhoto();
    }

    private void displayPhoto() {
        String url = getIntent().getStringExtra(POST_PHOTO_URL);
        url = Utility.utility.getMediaUrl(url);

        Picasso.with(this)
                .load(url)
                .fit()
                .centerInside()
                .into(ivPhoto);
    }
}
