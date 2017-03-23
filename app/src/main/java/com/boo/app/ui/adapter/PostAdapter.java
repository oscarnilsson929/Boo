package com.boo.app.ui.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.boo.app.R;
import com.boo.app.library.hashtag.annotation.SocialActionIntDef;
import com.boo.app.library.hashtag.util.SocialActionCallback;
import com.boo.app.library.hashtag.util.SocialActionType;
import com.boo.app.library.hashtag.widget.SocialTextView;
import com.boo.app.model.PostObject;
import com.boo.app.utility.FileUtility;
import com.boo.app.utility.Utility;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private Context context;
    private ArrayList<PostObject> postObjects;

    WeakReference<ActionListener> listener;

    public interface ActionListener {
        void onUser(PostObject postObject);
        void onUserName(PostObject postObject);
        void onPhoto(PostObject postObject);
        void onBooToo(PostObject postObject);
        void onBooTooLong(PostObject postObject);
        void onComment(PostObject postObject);
        void onShare(PostObject postObject);
        void onMentionClick(String user_name);
        void onTagClick(String tag);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout mContainer;
        public ImageView ivAvatar, ivPhoto, ivPlay, ivBooToo;
        public TextView tvName, tvTime, tvBooToo, tvComment, tvShare;
        public SocialTextView tvText;
        public VideoView vvVideo;
        public LinearLayout btnBooToo, btnComment, btnSahre;

        public ViewHolder(View v) {
            super(v);

            mContainer = (RelativeLayout)v.findViewById(R.id.post_item_container);

            ivAvatar        = (ImageView) v.findViewById(R.id.iv_post_item_avatar);
            ivPhoto         = (ImageView)v.findViewById(R.id.iv_post_item_photo);
            ivPlay          = (ImageView)v.findViewById(R.id.iv_post_item_play);
            ivBooToo        = (ImageView)v.findViewById(R.id.iv_post_item_boo_too);

            vvVideo         = (VideoView) v.findViewById(R.id.vv_post_item_video);

            tvName          = (TextView) v.findViewById(R.id.tv_post_item_user_name);
            tvTime          = (TextView) v.findViewById(R.id.tv_post_item_time_diff);
            tvText          = (SocialTextView) v.findViewById(R.id.tv_post_item_text);
            tvBooToo        = (TextView) v.findViewById(R.id.tv_post_item_boo_too);
            tvComment       = (TextView) v.findViewById(R.id.tv_post_item_comment);

            btnBooToo       = (LinearLayout)v.findViewById(R.id.post_item_boo_too);
            btnComment      = (LinearLayout)v.findViewById(R.id.post_item_comment);
            btnSahre        = (LinearLayout)v.findViewById(R.id.post_item_share);

            ivPlay .setVisibility(View.VISIBLE);
            ivPhoto.setVisibility(View.VISIBLE);
            vvVideo.setVisibility(View.INVISIBLE);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public PostAdapter(Context context, ArrayList<PostObject> postObjects, ActionListener listener) {
        this.context = context;
        this.postObjects = postObjects;
        this.listener = new WeakReference<>(listener);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_post_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final PostObject postObject = postObjects.get(position);
        //        Set Tag
        holder.ivPlay    .setTag("play"       + position);
        holder.ivPhoto   .setTag("image_view" + position);
        holder.vvVideo   .setTag("video_view" + position);

        ////        Set OnClickListener
        holder.ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.get().onUser(postObject);
            }
        });

        holder.tvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.get().onUserName(postObject);
            }
        });

        holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.get().onPhoto(postObject);
            }
        });

        holder.btnBooToo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.get().onBooToo(postObject);
            }
        });

        holder.btnBooToo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.get().onBooTooLong(postObject);
                return true;
            }
        });


        holder.btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.get().onComment(postObject);
            }
        });

        holder.btnSahre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.get().onShare(postObject);
            }
        });

        holder.ivPlay.setOnClickListener(playClickListener);


        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        String url = Utility.utility.getMediaUrl(postObject.getPost_user_photo_url());
        Picasso.with(context).load(url).placeholder(R.drawable.img_no_avatar).fit().centerCrop().into(holder.ivAvatar);

        holder.tvName.setText(postObject.getPost_user_full_name());
        holder.tvTime.setText(Utility.utility.getTimeDiff(postObject.getPost_time_diff()));
        holder.tvText.setText(postObject.getPost_text());
        holder.tvText.linkify(new SocialActionCallback() {
            @Override
            public void onMatch(@SocialActionIntDef int type, String value) {
                if(type == SocialActionType.MENTION) {
                    listener.get().onMentionClick(value);
                } else if (type == SocialActionType.HASH_TAG) {
                    listener.get().onTagClick(value);
                }
            }
        });
        holder.tvComment.setText(postObject.getPost_comments_count() > 0 ? "Comment(" + postObject.getPost_comments_count() + ")" : "Comment");

        url = Utility.utility.getMediaUrl(postObject.getPost_photo_url());

        if (url.isEmpty()) {
            url = Utility.utility.getMediaUrl(postObject.getPost_video_thumb_url());
            if (url.isEmpty()) {
                postObject.setPost_type(0);
            } else {
                postObject.setPost_type(2);
            }
        } else {
            postObject.setPost_type(1);
        }

        int h = Utility.utility.getDeviceScreenSize(context).x;

        switch (postObject.getPost_type()) {
            case 0:
                holder.mContainer.setVisibility(View.GONE);
                break;
            case 1:
                holder.mContainer.setVisibility(View.VISIBLE);
                holder.ivPlay .setVisibility(View.INVISIBLE);
                holder.vvVideo.setVisibility(View.INVISIBLE);
                holder.ivPhoto.setVisibility(View.VISIBLE);

                holder.mContainer.getLayoutParams().height = h;
                url = Utility.utility.getMediaUrl(postObject.getPost_photo_url());
                Picasso.with(context)
                        .load(url)
                        .into(holder.ivPhoto);
                break;
            case 2:
                holder.mContainer.setVisibility(View.VISIBLE);
                holder.ivPlay .setVisibility(View.VISIBLE);
                holder.vvVideo.setVisibility(View.INVISIBLE);
                holder.ivPhoto.setVisibility(View.VISIBLE);

                holder.mContainer.getLayoutParams().height = h;
                url = Utility.utility.getMediaUrl(postObject.getPost_video_thumb_url());
                Picasso.with(context)
                        .load(url)
                        .into(holder.ivPhoto);

                holder.vvVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        holder.ivPlay.setVisibility(View.VISIBLE);
                        holder.ivPhoto.setVisibility(View.VISIBLE);
                        holder.vvVideo.setVisibility(View.INVISIBLE);
                    }
                });

                break;
        }

        changeBooTooStatus(holder.ivBooToo, holder.tvBooToo, position);
    }

    @Override
    public int getItemCount() {
        return postObjects.size();
    }

    private void changeBooTooStatus(ImageView imageBoo, TextView textBoo, int position) {
        imageBoo.setColorFilter(ContextCompat.getColor(context, postObjects.get(position).getIs_booed_by_me() == 1 ? R.color.colorMainButtonSelected: R.color.colorMainButtonUnselected));
        textBoo .setTextColor(ContextCompat.getColor(context, postObjects.get(position).getIs_booed_by_me() == 1 ? R.color.colorMainButtonSelected: R.color.colorMainButtonUnselected));

        textBoo.setText(postObjects.get(position).getPost_booed_count() > 0 ? "Boo-Too(" + postObjects.get(position).getPost_booed_count() + ")" : "Boo-Too");
    }

    private View.OnClickListener playClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            String tag = (String)v.getTag();
            int position = Integer.parseInt(tag.substring(4));

            ImageView imageView = (ImageView)Utility.utility.getViewByTag((ViewGroup) v.getParent(), "image_view" + position);
            final VideoView videoView = (VideoView)Utility.utility.getViewByTag((ViewGroup) v.getParent(), "video_view" + position);

            v.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.INVISIBLE);
            videoView.setVisibility(View.VISIBLE);

            final String videoUrl = Utility.utility.getMediaUrl(postObjects.get(position).getPost_video_url());
            final String fileName = postObjects.get(position).getPost_id() + ".mp4";
            Uri uri = FileUtility.searchVideoFile(context, fileName);
            if (uri != null) {
                videoView.setVideoURI(uri);
                videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        new DownloadTask(videoView).execute(videoUrl, fileName);
                        return true;
                    }
                });
                videoView.requestFocus();
                videoView.start();
            } else {
                new DownloadTask(videoView).execute(videoUrl, fileName);
            }
        }
    };

    //    Post AsyncTask
    public class DownloadTask extends AsyncTask<String, Void, Uri> {
        ProgressDialog mProgressDialog = new ProgressDialog(context, R.style.ProgressDialogTheme);
        private VideoView mVideoView;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        public DownloadTask(VideoView videoView) {
            this.mVideoView = videoView;
        }

        @Override
        protected Uri doInBackground(String... params) {
            return FileUtility.downloadVideoFile(context, params[0], params[1]);
        }

        @Override
        protected void onPostExecute(Uri result) {
            if (result != null) {
                mVideoView.setVideoURI(result);
                mVideoView.start();
            }

            mProgressDialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            mProgressDialog.dismiss();
        }
    }
}
