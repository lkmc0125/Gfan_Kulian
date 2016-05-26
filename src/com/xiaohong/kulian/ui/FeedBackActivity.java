/**
 * 
 */
package com.xiaohong.kulian.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.widget.BaseActivity;
import com.xiaohong.kulian.common.widget.CustomDialog;

public class FeedBackActivity extends BaseActivity {
    private final static String TAG = "FeedBackActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_layout);
        initTopBar();
        initViews();
    }

    private void initTopBar() {
        ImageButton back = (ImageButton) findViewById(R.id.back_btn);
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                hideKeyBoard();
                finish();
            }
        });
    }

    private void initViews() {
        final Button sendFeedback = (Button) findViewById(R.id.ib_send);
        final EditText feedbackContent = (EditText) findViewById(R.id.et_comment);
        feedbackContent.requestFocus();
        sendFeedback.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = getApplicationContext();
                String content = feedbackContent.getText().toString();
                if (TextUtils.isEmpty(content)) {
                    showAlertMsg(getString(R.string.content_no_empty));
                    return;
                }

                if (!Utils.isNetworkAvailable(context)) {
                    showAlertMsg(getString(R.string.no_valid_network));
                    return;
                }

                Utils.trackEvent(getApplicationContext(), Constants.GROUP_13, Constants.SEND_FEEDBACK);
                sendFeedback(content);
            }
        });
    }

    private void sendFeedback(String content) {
        //Fix IllegalArgumentException
        String encodingContent = content;
        try {
            encodingContent = URLEncoder.encode(content, "UTF-8");
            //Log.d(TAG, "encodingContent = " + encodingContent);
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        String url = "http://www.dspmind.com/feedback/app_feedback.php?platform=Android&token=LUZ9EUzkELCyPIXLNrWrDbqzX&device_info="
                + mSession.getModel()
                + "&app_version="
                + mSession.getVersionName()
                + "Build"
                + mSession.getVersionCode() + "&feedback=" + encodingContent
                + "&encoded=1";
        if (mSession.isLogin()) {
            url = url + "&phone_number=" + mSession.getUserName();
        }
        Log.d(TAG, url);
        String ret = Utils.httpGet(url);
        if (ret != null) {
            try {
                JSONObject obj = new JSONObject(ret);

                if (obj.getInt("ret_code") == 0) {
                    CustomDialog dialog = new CustomDialog.Builder(this)
                    .setTitle("提交成功")
                    .setMessage("谢谢您的反馈!")
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    hideKeyBoard();
                                    finish();
                                }
                            }).create();
                    dialog.show();
                } else {
                    CustomDialog dialog = new CustomDialog.Builder(this)
                    .setTitle("提交失败")
                    .setMessage(obj.getString("ret_msg"))
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                }
                            }).create();
                    dialog.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d(TAG, ret);
        }
    }

    private void showAlertMsg(String msg) {
        CustomDialog dialog = new CustomDialog.Builder(this)
        .setMessage(msg)
        .setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }

    private void hideKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }
}
