/*
 * Copyright (C) 2010 mAPPn.Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xiaohong.kulian.ui;

import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.util.TopBar;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.widget.BaseActivity;

/**
 * 登录页
 * @author Andrew
 * @date    2011-5-25
 *
 */
public class LoginActivity extends BaseActivity 
	implements OnClickListener, OnFocusChangeListener, ApiRequestListener {

//	private static final String TAG = "LoginActivity";
	
	private static final int DIALOG_PROGRESS = 0;

	// 用户不存在（用户名错误）
	private static final int ERROR_CODE_USERNAME_NOT_EXIST = 211;
	// 用户密码错误
	private static final int ERROR_CODE_PASSWORD_INVALID = 212;
	
	private EditText etUsername;
	private EditText etPassword;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_layout);
		initView();
	}
	
    @Override
    protected void onDestroy() {
        super.onDestroy();
        etUsername = null;
        etPassword = null;
    }

    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setResult(Activity.RESULT_CANCELED);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void initView() {
	    
	       // top bar
        TopBar.createTopBar(this, 
                new View[] { findViewById(R.id.top_bar_title) },
                new int[] { View.VISIBLE }, 
                getString(R.string.login));
        
		etUsername = (EditText) findViewById(R.id.et_username);
        String userName = TextUtils.isEmpty(mSession.getUserName()) ? "" : mSession.getUserName();
        etUsername.setText(userName);
		etUsername.setOnFocusChangeListener(this);
		etUsername.requestFocus();
		etPassword = (EditText) findViewById(R.id.et_verify_code);
		etPassword.setOnFocusChangeListener(this);
		
        if (!TextUtils.isEmpty(userName)) {
            etPassword.requestFocus();
        }
		
        Button btnLogin = (Button) findViewById(R.id.btn_login);
		btnLogin.setOnClickListener(this);
//		TextView btnRegister = (TextView) findViewById(R.id.btn_register);
//		CharSequence text = btnRegister.getText();
//		SpannableString spanable = new SpannableString(text);
//		spanable.setSpan(new UnderlineSpan(), text.length() - 4, text.length(), 0);
//		btnRegister.setText(spanable);
//		btnRegister.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_login:
			login();
			break;
		case R.id.btn_verify_code:
			onClickVerifyCodeBtn();
			break;
		}
	}

	/*
	 * Do login process
	 */
	private void login() {
	    
	    // 检查用户输入
        if (!checkUserName() || !checkPassword(etPassword)) {
            return;
        }
	    
        if (!isFinishing()) {
            showDialog(DIALOG_PROGRESS);
        } else {
            // 如果当前页面已经关闭，不进行登录操作
            return;
        }
		String userName = etUsername.getText().toString();
		String password = etPassword.getText().toString();
		MarketAPI.login(getApplicationContext(), this, userName, password);
		
		Utils.trackEvent(getApplicationContext(), Constants.GROUP_9,
                Constants.LOGIN);
	}

	private void onClickVerifyCodeBtn() {

	}

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);

        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        switch (id) {
        case DIALOG_PROGRESS:
            ProgressDialog mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage(getString(R.string.singin));
            return mProgressDialog;

        default:
            return super.onCreateDialog(id);
        }
    }

	@SuppressWarnings("unchecked")
    @Override
	public void onSuccess(int method, Object obj) {
		
	    switch (method) {
        case MarketAPI.ACTION_LOGIN:
            
            Utils.trackEvent(getApplicationContext(), Constants.GROUP_9,
                    Constants.LOGIN_SUCCESS);
            HashMap<String, String> result = (HashMap<String, String>) obj;
            mSession.setUid(result.get(Constants.KEY_USER_UID));
            mSession.setUserName(result.get(Constants.KEY_USER_NAME));
            // 同步用户购买记录
            MarketAPI.syncBuyLog(getApplicationContext(), this);
            break;
            
        case MarketAPI.ACTION_SYNC_BUYLOG:
            
            syncBuyLogOver(true);
            break;
        default:
            break;
        }
	}

	@Override
    public void onError(int method, int statusCode) {

	    switch (method) {
        case MarketAPI.ACTION_LOGIN:
            
            // 隐藏登录框
            try{
                dismissDialog(DIALOG_PROGRESS);
            }catch (IllegalArgumentException e) {
            }
            
            String msg = null;
            if(statusCode == ERROR_CODE_USERNAME_NOT_EXIST) {
                msg = getString(R.string.error_login_username);
            } else if(statusCode == ERROR_CODE_PASSWORD_INVALID) {
                msg = getString(R.string.error_login_password);
            } else {
                msg = getString(R.string.error_login_other);
            }
            Utils.makeEventToast(getApplicationContext(), msg, false);
            break;
            
        case MarketAPI.ACTION_SYNC_BUYLOG:
            
            // 同步购买记录失败
            syncBuyLogOver(false);
            break;

        default:
            break;
        }
    }
	
	/*
	 * 同步购买记录完成，无论成功或者失败，登录操作都结束。
	 * 登录状态中，下次开启应用的时候同步购买记录。
	 */
	private void syncBuyLogOver(boolean isSuccess) {
	    
        mSession.setLogin(true);
        Utils.makeEventToast(getApplicationContext(), getString(R.string.login_success), false);
	    setResult(Activity.RESULT_OK);
        try{
            dismissDialog(DIALOG_PROGRESS);
        }catch (IllegalArgumentException e) {
        }
        finish();
	}

    @Override
    public void onFocusChange(View v, boolean flag) {
        switch (v.getId()) {
        case R.id.et_username:

            if (!flag) {
                checkUserName();
            }
            break;

        case R.id.et_verify_code:

            if (!flag) {
                checkPassword(etPassword);
            }
            break;

        default:
            break;
        }
    }
    
    /*
     * 检查用户名合法性
     * 1 不能为空
     * 2 长度在 3 - 16 个字符之间
     */
    private boolean checkUserName() {
        String input = etUsername.getText().toString();
        if (TextUtils.isEmpty(input)) {
            etUsername.setError(getString(R.string.error_username_empty));
            return false;
        } else {
            etUsername.setError(null);
        }
        int length = input.length();
        if (length < 3 || length > 16) {
            etUsername.setError(getString(R.string.error_username_length_invalid));
            return false;
        } else {
            etUsername.setError(null);
        }
        return true;
    }
    
    /*
     * 检查用户密码合法性
     * 1 不能为空
     * 2 长度在1 - 32 个字符之间
     */
    private boolean checkPassword(EditText input) {
        String passwod = input.getText().toString();
        if (TextUtils.isEmpty(passwod)) {
            input.setError(getString(R.string.error_password_empty));
            return false;
        } else {
            input.setError(null);
        }
        int length = passwod.length();
        if (length > 32) {
            input.setError(getString(R.string.error_password_length_invalid));
            return false;
        } else {
            input.setError(null);
        }
        return true;
    }
}