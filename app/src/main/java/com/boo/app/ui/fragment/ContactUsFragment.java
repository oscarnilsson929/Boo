package com.boo.app.ui.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.boo.app.App;
import com.boo.app.R;
import com.boo.app.api.GetDataTask;
import com.boo.app.api.OnTaskCompleted;
import com.boo.app.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import mehdi.sakout.fancybuttons.FancyButton;

public class ContactUsFragment extends BaseFragment implements OnTaskCompleted{

    private EditText etSubject, etMessage;
    private FancyButton btnSend;

    private ProgressDialog mProgressDialog;

    private String strSubject, strMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_contact_us, container, false);

        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setCancelable(false);

        initUI(view);
        return view;
    }

    private void initUI(View v) {
        etSubject = (EditText)v.findViewById(R.id.et_contact_us_subject);
        etMessage = (EditText)v.findViewById(R.id.et_contact_us_message);
        btnSend = (FancyButton)v.findViewById(R.id.btn_contact_us_send);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSend();
            }
        });
    }

    private void onSend() {
        if (validInfo()) {
            String url = URL_SERVER + URL_SEND_EMAIL;
            JSONObject jsonRequest = new JSONObject();
            try {
                jsonRequest.put(USER_ID, ((App) getActivity().getApplication()).getCurrentUser().user_id);
                jsonRequest.put(SUBJECT, strSubject);
                jsonRequest.put(MESSAGE_CONTENT, strMessage);

                mProgressDialog.setMessage("Please wait...");
                mProgressDialog.show();

                new GetDataTask(url, jsonRequest, this).execute();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onTaskSuccess(JSONObject jsonResponse) {
        mProgressDialog.dismiss();
        Utility.showToast(getContext(), "Send the message successfully.");
    }

    @Override
    public void onTaskError(String msg) {
        mProgressDialog.dismiss();
        Utility.showToast(getContext(), msg);
    }

    private boolean validInfo() {
        boolean valid  = true;
        View focusView = null;

        etSubject.setError(null);
        etMessage.setError(null);

        strSubject = etSubject.getText().toString().trim();
        strMessage = etMessage.getText().toString().trim();

        if (strSubject.isEmpty()) {
            etSubject.setError(getString(R.string.error_field_required));
            focusView = etSubject;
            valid = false;
        } else if (strMessage.isEmpty()) {
            etMessage.setError(getString(R.string.error_field_required));
            focusView = etMessage;
            valid = false;
        }

        if (!valid) {
            focusView.requestFocus();
        }

        return valid;
    }
}