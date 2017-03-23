package com.boo.app.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boo.app.R;
import com.boo.app.model.CommentObject;
import com.boo.app.utility.Utility;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private Context context;
    private ArrayList<CommentObject> commentObjects;

    WeakReference<ActionListener> listener;

    public interface ActionListener {
        void onAvatar(CommentObject commentObject);
        void onUsername(CommentObject commentObject);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivAvatar;
        public TextView tvUsername, tvComment, tvTime;

        public ViewHolder(View v) {
            super(v);
            ivAvatar   = (ImageView) v.findViewById(R.id.iv_comment_avatar);
            tvUsername = (TextView)  v.findViewById(R.id.tv_comment_username);
            tvComment  = (TextView)  v.findViewById(R.id.tv_comment_content);
            tvTime     = (TextView)  v.findViewById(R.id.tv_comment_time);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public CommentAdapter(Context context, ArrayList<CommentObject> commentObjects, ActionListener listener) {
        this.context = context;
        this.commentObjects = commentObjects;
        this.listener = new WeakReference<>(listener);
    }

    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_comment_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final CommentObject commentObject= commentObjects.get(position);
        ////        Set OnClickListener
        holder.ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.get().onAvatar(commentObject);
            }
        });

        holder.tvUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.get().onUsername(commentObject);
            }
        });

        String url = Utility.utility.getMediaUrl(commentObject.getComment_user_photo_url());
        Picasso.with(context).load(url).placeholder(R.drawable.img_no_avatar).fit().centerCrop().into(holder.ivAvatar);

        holder.tvUsername.setText(commentObject.getComment_user_full_name());
        holder.tvComment.setText(commentObject.getComment_content());
        holder.tvTime.setText(Utility.utility.getTimeDiff(commentObject.getComment_time_diff()));
    }

    @Override
    public int getItemCount() {
        return commentObjects.size();
    }
}
