package com.boo.app.ui.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import com.boo.app.App;
import com.boo.app.R;
import com.boo.app.api.GetDataTask;
import com.boo.app.api.OnTaskCompleted;
import com.boo.app.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import mehdi.sakout.fancybuttons.FancyButton;

public class ChangePasswordFragment extends BaseFragment implements OnTaskCompleted{
    private EditText etOld, etNew, etConfirm;

    private String strOld;
    private String strNew;

    private ProgressDialog mProgressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_change_password, container, false);
        findViews(view);
        return view;
    }

    private void findViews(View view) {
        etOld     = (EditText)view.findViewById(R.id.et_change_password_old);
        etNew     = (EditText)view.findViewById(R.id.et_change_password_new);
        etConfirm = (EditText)view.findViewById(R.id.et_change_password_confirm);
        FancyButton btnChange = (FancyButton) view.findViewById(R.id.btn_change_password);

        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

        //        Hide Keyboard
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        Utility.utility.setupUI(view.findViewById(R.id.parent_change_password), getActivity());

    }

    private void changePassword() {
        if (isValidInfo()) {
            strOld = Utility.utility.md5(strOld);
            strNew = Utility.utility.md5(strNew);

            String url = URL_SERVER + URL_USER_CHANGE_PASSWORD;
            JSONObject jsonRequest = new JSONObject();
            try {
                jsonRequest.put(USER_ID, ((App)getActivity().getApplication()).getCurrentUser().user_id);
                jsonRequest.put(USER_OLD_PASSWORD, strOld);
                jsonRequest.put(USER_NEW_PASSWORD, strNew);

                mProgressDialog = new ProgressDialog(getContext());
                mProgressDialog.setCancelable(false);
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
        Utility.showToast(getContext(), "Changed password correctly.");
        clearEditText();
    }

    @Override
    public void onTaskError(String msg) {
        mProgressDialog.dismiss();
        Utility.showToast(getContext(), msg);
    }

    private void clearEditText() {
        etOld    .setText("");
        etNew    .setText("");
        etConfirm.setText("");
    }

    //    Check User Info
    private boolean isValidInfo() {
        boolean valid = true;
        View focusView = null;

        etOld.setError(null);
        etNew.setError(null);
        etConfirm.setError(null);

        strOld     = etOld.getText().toString().trim();
        strNew     = etNew.getText().toString().trim();
        String strConfirm = etConfirm.getText().toString().trim();

        if (strOld.isEmpty()) {
            etOld.setError(getString(R.string.error_field_required));
            focusView = etOld;
            valid = false;
        } else if (strNew.isEmpty()) {
            etNew.setError(getString(R.string.error_field_required));
            focusView = etNew;
            valid = false;
        } else if (!strConfirm.equals(strNew)) {
            etConfirm.setText("");
            etConfirm.setError(getString(R.string.error_matched_password_required));
            focusView = etConfirm;
            valid = false;
        }

        if (!valid) {
            focusView.requestFocus();
        }

        return valid;
    }
}