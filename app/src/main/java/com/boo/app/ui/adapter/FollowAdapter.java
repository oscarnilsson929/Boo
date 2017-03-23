package com.boo.app.ui.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boo.app.R;
import com.boo.app.model.ActivityObject;
import com.boo.app.model.UserObject;
import com.boo.app.utility.Utility;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import mehdi.sakout.fancybuttons.FancyButton;

public class FollowAdapter extends RecyclerView.Adapter<FollowAdapter.ViewHolder> {

    private Context context;
    private ArrayList<UserObject> userObjects;

    WeakReference<ActionListener> listener;

    public interface ActionListener {
        void onAvatar(UserObject userObject);
        void onName(UserObject userObject);
        void onFollow(UserObject userObject);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivAvatar;
        public TextView tvUsername;
        public FancyButton btnFollow;

        public ViewHolder(View v) {
            super(v);
            ivAvatar   = (ImageView) v.findViewById(R.id.iv_follow_user_avatar);
            tvUsername = (TextView)  v.findViewById(R.id.tv_follow_user_name);
            btnFollow  = (FancyButton)v.findViewById(R.id.btn_follow_user_follow);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public FollowAdapter(Context context, ArrayList<UserObject> userObjects, ActionListener listener) {
        this.context = context;
        this.userObjects = userObjects;
        this.listener = new WeakReference<>(listener);
    }

    @Override
    public FollowAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_follow_user_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final UserObject userObject = userObjects.get(position);
        ////        Set OnClickListener
        holder.ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.get().onAvatar(userObject);
            }
        });

        holder.tvUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.get().onName(userObject);
            }
        });

        holder.btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.get().onFollow(userObject);
            }
        });

        String url = Utility.utility.getMediaUrl(userObject.user_photo_url);
        Picasso.with(context).load(url).placeholder(R.drawable.img_no_avatar).fit().centerCrop().into(holder.ivAvatar);

        holder.tvUsername.setText(userObject.user_full_name);

        changeFollowButton(userObject, holder.btnFollow);
    }

    @Override
    public int getItemCount() {
        return userObjects.size();
    }

    private void changeFollowButton(UserObject userObject, FancyButton btnFollow) {
        if (userObject.is_followed_by_me == 1) {
            btnFollow.setBackgroundColor(ContextCompat.getColor(context, R.color.colorFollow));
            btnFollow.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            btnFollow.setText("FOLLOWING");
        } else {
            btnFollow.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            btnFollow.setTextColor(ContextCompat.getColor(context, R.color.colorFollow));
            btnFollow.setText("FOLLOW");
        }
    }
}
