package com.boo.app.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boo.app.R;
import com.boo.app.model.ActivityObject;
import com.boo.app.utility.Utility;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ActivityObject> activityObjects;

    WeakReference<ActionListener> listener;

    public interface ActionListener {
        void onAvatar(ActivityObject activityObject);
        void onName(ActivityObject activityObject);
        void onYou(ActivityObject activityObject);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivAvatar;
        public TextView tvUsername, tvContent, tvYou, tvTime;

        public ViewHolder(View v) {
            super(v);
            ivAvatar   = (ImageView) v.findViewById(R.id.iv_activity_avatar);
            tvUsername = (TextView)  v.findViewById(R.id.tv_activity_user_name);
            tvContent  = (TextView)  v.findViewById(R.id.tv_activity_content);
            tvYou      = (TextView)  v.findViewById(R.id.tv_activity_you);
            tvTime     = (TextView)  v.findViewById(R.id.tv_activity_time);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ActivityAdapter(Context context, ArrayList<ActivityObject> activityObjects, ActionListener listener) {
        this.context = context;
        this.activityObjects = activityObjects;
        this.listener = new WeakReference<>(listener);
    }

    @Override
    public ActivityAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_activity_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ActivityObject activityObject = activityObjects.get(position);
        ////        Set OnClickListener
        holder.ivAvatar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.get().onAvatar(activityObject);
            }
        });

        holder.tvUsername.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.get().onName(activityObject);
            }
        });

        holder.tvYou.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.get().onYou(activityObject);
            }
        });

        String url = Utility.utility.getMediaUrl(activityObject.getActivity_user_photo_url());
        Picasso.with(context).load(url).placeholder(R.drawable.img_no_avatar).fit().centerCrop().into(holder.ivAvatar);

        holder.tvUsername.setText(activityObject.getActivity_user_full_name());
        holder.tvTime.setText(Utility.utility.getTimeDiff(activityObject.getActivity_time_diff()));

        switch (activityObject.getActivity_type()) {
            case 1:
                holder.tvContent.setText(" started following ");
                holder.tvYou.setText("you.");
                break;
            case 2:
                holder.tvContent.setText(" booed at ");
                holder.tvYou.setText("your post.");
                break;
            case 3:
                holder.tvContent.setText(" commented on ");
                holder.tvYou.setText("your post.");
                break;
        }
    }

    @Override
    public int getItemCount() {
        return activityObjects.size();
    }
}
