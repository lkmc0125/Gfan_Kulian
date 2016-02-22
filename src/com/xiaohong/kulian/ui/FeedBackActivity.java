/**
 * 
 */
package com.xiaohong.kulian.ui;

import org.apache.http.HttpResponse;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.common.util.TopBar;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.widget.BaseActivity;

/**
 * @author rachel
 * 
 */
public class FeedBackActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feedback_layout);
		initTopBar();
		initViews();
	}

    private void initTopBar() {
        TopBar.createTopBar(getApplicationContext(),
                new View[] { findViewById(R.id.top_bar_title) }, new int[] { View.VISIBLE },
                getString(R.string.feedback_title));
    }

    private void initViews() {
        final Button sendFeedback = (Button) findViewById(R.id.ib_send);
        final EditText feedbackContent = (EditText) findViewById(R.id.et_comment);
        feedbackContent.requestFocus();
//        feedbackContent.setText(Utils.submitLogs());
        sendFeedback.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = getApplicationContext();
                String content = feedbackContent.getText().toString();
                if (TextUtils.isEmpty(content)) {
                    Utils.makeEventToast(context, getString(R.string.content_no_empty), false);
                    return;
                }
                
                if(!Utils.isNetworkAvailable(context)) {
                    Utils.makeEventToast(context, getString(R.string.no_valid_network), false);
                    return;
                }
                
                Utils.trackEvent(getApplicationContext(), Constants.GROUP_13,
                        Constants.SEND_FEEDBACK);
                
                if (mSession.isLogin()) {
                    content = "User[" + mSession.getUid() + "] send feedback : " + content;
                }
            }
        });
    }
}
