package com.xiaohong.kulian.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.common.util.TopBar;
import com.xiaohong.kulian.common.util.Utils;

public class OtherAccountActivity extends Activity implements OnClickListener, OnFocusChangeListener {

    private EditText userNameEditText;
    private Button confirmBtn;
    private String userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_account);
        initViews();
    }

    private void initViews() {
        initTopBar("帮人充值");
        confirmBtn = (Button) this.findViewById(R.id.btn_confirm);
        confirmBtn.setOnClickListener(this);
        userNameEditText = (EditText) this.findViewById(R.id.et_username);
        userNameEditText.setOnFocusChangeListener(this);
        userNameEditText.requestFocus();
    }

    private void initTopBar(String title) {
        TopBar.createTopBar(this, 
                new View[] { findViewById(R.id.back_btn), findViewById(R.id.top_bar_title) },
                new int[] { View.VISIBLE, View.VISIBLE}, 
                title);
        ImageButton back = (ImageButton)findViewById(R.id.back_btn);
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_confirm:
        {
            if (checkUserName()) {
                userName = userNameEditText.getText().toString();
                Utils.gotoBuyCoinPage(OtherAccountActivity.this);                
            }
            break;
        }
        default:
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
        case R.id.et_username:
            if (!hasFocus) {
                if (checkUserName()) {
                    userName = userNameEditText.getText().toString();
                } else {
                    userName = null;
                }
            }
            break;
        default:
            break;
        }
    }

    private boolean checkUserName() {
        String input = userNameEditText.getText().toString();
        if (TextUtils.isEmpty(input)) {
            userNameEditText.setError(getDisplayText(R.string.error_username_empty));
            return false;
        } else {
            userNameEditText.setError(null);
        }
        int length = input.length();
        if (length != 11) {
            userNameEditText.setError(getDisplayText(R.string.error_username_length_invalid));
            return false;
        } else {
            userNameEditText.setError(null);
        }
        return true;
    }

    private CharSequence getDisplayText(int resId) {
        return Html.fromHtml("<font color='#2C78D4'>"+getString(resId)+"</font>");
    }
}
