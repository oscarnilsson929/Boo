package com.boo.app.ui.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.boo.app.App;
import com.boo.app.R;
import com.boo.app.api.GetDataTask;
import com.boo.app.api.OnTaskCompleted;
import com.boo.app.model.CommentObject;
import com.boo.app.model.PostObject;
import com.boo.app.ui.adapter.CommentAdapter;
import com.boo.app.utility.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class Comment extends BaseActivity implements View.OnClickListener, CommentAdapter.ActionListener, OnTaskCompleted {
    private ImageView ivBack, ivSend;
    private RecyclerView mRecyclerView;
    private EditText etComment;

    private PostObject mPostObject;
    private long post_id;
    private ArrayList<CommentObject> commentObjects;
    private CommentAdapter mCommentAdapter;
    private String comment_content;

    private ProgressDialog mProgressDialog;

    private int taskMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        post_id = getIntent().getLongExtra("post", 0);


        commentObjects = new ArrayList<>();
        mCommentAdapter = new CommentAdapter(this, commentObjects, this);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        initUI();
    }

    @Override
    public void onBackPressed() {
        onBack();
    }

    private void initUI() {
//        Hide Keyboard
        Utility.utility.setupUI(findViewById(R.id.parent_comment), this);
//        Find View Id
        ivBack        = _findViewById(R.id.iv_comment_back);
        ivSend        = _findViewById(R.id.iv_comment_send);
        mRecyclerView = _findViewById(R.id.rv_comments);
        etComment     = _findViewById(R.id.et_comment);

        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        setUpItemTouchHelper();

//        Set OnClickListener
        ivBack.setOnClickListener(this);
        ivSend.setOnClickListener(this);

        getComment();
    }

    private void setUpItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                long currentUserId = ((App)getApplication()).getCurrentUser().user_id;
                long refUserId = commentObjects.get(position).getComment_user_id();

                if (currentUserId != refUserId) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(Comment.this);
                    builder.setMessage("You can't delete this").setTitle("Warning");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mCommentAdapter.notifyDataSetChanged();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Comment.this);
                    builder.setMessage("Do you really want to delete?").setTitle("DELETE");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            int position = viewHolder.getAdapterPosition();
                            taskMode = 2;
                            createComment(false, position);

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mCommentAdapter.notifyDataSetChanged();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);

        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    private void getComment() {
        taskMode = 0;

        String url = URL_SERVER + URL_GET_POST;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(USER_ID, ((App)getApplication()).getCurrentUser().user_id);
            jsonRequest.put(POST_ID, post_id);

            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();

            new GetDataTask(url, jsonRequest, this).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void createComment(boolean mode, int position) {
        taskMode = 1;

        String url = URL_SERVER + URL_COMMENT_POST;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(USER_ID, ((App)getApplication()).getCurrentUser().user_id);
            jsonRequest.put(REF_ID, post_id);
            jsonRequest.put(MODE, mode? ADD: DELETE);

            if (mode) {
                jsonRequest.put(COMMENT_CONTENT, comment_content);
            } else {
                jsonRequest.put(COMMENT_ID, commentObjects.get(position).getComment_id());
            }
            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();

            new GetDataTask(url, jsonRequest, this).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAvatar(CommentObject commentObject) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("type", 1);
        resultIntent.putExtra("comment", commentObject);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onUsername(CommentObject commentObject) {

    }

    @Override
    public void onTaskSuccess(JSONObject jsonResponse) {
        mProgressDialog.dismiss();
        ArrayList<CommentObject> result = new ArrayList<>();
        JSONObject jsonObject;
        try {
            jsonObject = jsonResponse.getJSONObject(REF_POST);
            mPostObject = new PostObject(jsonObject);
            JSONArray jsonArray = jsonObject.getJSONArray(POST_COMMENTS);
            for (int i = 0; i < jsonArray.length(); i++) {
                CommentObject commentObject = new CommentObject(jsonArray.getJSONObject(i));
                result.add(commentObject);
            }

            Collections.reverse(result);
            commentObjects.clear();
            commentObjects.addAll(result);

            if (taskMode == 2) {
                mCommentAdapter.notifyDataSetChanged();
            } else {
                displayComment();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTaskError(String msg) {
        mProgressDialog.dismiss();
        Utility.showToast(this, msg);
    }

    private void displayComment() {
        mCommentAdapter = new CommentAdapter(this, commentObjects, this);
        mRecyclerView.setAdapter(mCommentAdapter);
        etComment.setText("");
    }

    @Override
    public void onClick(View v) {
        if (v.equals(ivBack)) {
            onBack();
        }

        if (v.equals(ivSend)) {
            comment_content = etComment.getText().toString().trim();
            if (!comment_content.isEmpty()) {
                createComment(true, 0);
                etComment.setText("");
            }
        }
    }

    private void onBack() {
        if (mPostObject == null) {
            Intent resultIntent = new Intent();
            setResult(RESULT_CANCELED, resultIntent);
        } else {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("type", 0);
            resultIntent.putExtra("post", mPostObject);
            setResult(RESULT_OK, resultIntent);
        }

        finish();
    }
}
