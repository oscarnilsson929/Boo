package com.boo.app.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.boo.app.R;
import com.boo.app.model.UserObject;
import com.boo.app.utility.Utility;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context context;
    private ArrayList<UserObject> userObjects;
    WeakReference<OnItemClickListener> listener;

    public interface OnItemClickListener{
        void itemClicked(UserObject refUser);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout mContainer;
        public ImageView ivAvatar;
        public TextView tvUsername, tvBio;

        public ViewHolder(View v) {
            super(v);
            mContainer = (LinearLayout) v.findViewById(R.id.booed_user_container);
            ivAvatar   = (ImageView) v.findViewById(R.id.iv_booed_user_avatar);
            tvUsername = (TextView)  v.findViewById(R.id.tv_booed_user_name);
            tvBio      = (TextView)  v.findViewById(R.id.tv_booed_user_bio);
        }
    }

    public UserAdapter(Context context, ArrayList<UserObject> userObjects, OnItemClickListener listener) {
        this.context = context;
        this.userObjects = userObjects;
        this.listener = new WeakReference<>(listener);
    }

    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_booed_user_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final UserObject userObject = userObjects.get(position);

        String url = Utility.utility.getMediaUrl(userObject.user_photo_url);
        Picasso.with(context).load(url).placeholder(R.drawable.img_no_avatar).fit().centerCrop().into(holder.ivAvatar);
        holder.tvUsername.setText(userObject.user_name);
        holder.tvBio.setText(userObject.user_bio);
        holder.mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.get().itemClicked(userObject);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userObjects.size();
    }
}
