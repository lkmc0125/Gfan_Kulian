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
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
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
 * 注册页
 * @author Andrew
 * @date    2011-5-25
 *
 */
public class RegisterActivity extends BaseActivity 
	implements OnClickListener, OnFocusChangeListener, ApiRequestListener {
	
	private EditText etUsername;
	private EditText etEmail;
	private EditText etPassword;
	private EditText etPassword2;
	
	// dialog ID
	private static final int DIALOG_REGISTERING = 0;
	
	// 用户名不合法
	private static final int ERROR_CODE_USERNAME_INVALID = 213;
	//  用户名已存在
	private static final int ERROR_CODE_USERNAME_EXIST = 214;
	// 注册email格式有误
	private static final int ERROR_CODE_EMAIL_INVALID_FORMAT = 215;
	// 注册email已存在
	private static final int ERROR_CODE_EMAIL_EXIST = 216;
	// 注册密码不合法
	private static final int ERROR_CODE_PASSWORD_INVALID = 217;
	
	public static final Pattern EMAIL_ADDRESS_PATTERN = 
		Pattern.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-]{1,256}" 
				+ "\\@" + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" 
				+ "(\\." + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_register_layout);
		
		initView();
	}
	
    @Override
    protected void onDestroy() {
        super.onDestroy();
        etUsername = null;
        etEmail = null;
        etPassword = null;
        etPassword2 = null;
    }

    private void initView() {
		// top bar
        TopBar.createTopBar(this, 
                new View[] { findViewById(R.id.top_bar_title) },
                new int[] { View.VISIBLE }, 
                getString(R.string.register));

		etUsername = (EditText) findViewById(R.id.et_username);
		etUsername.setOnFocusChangeListener(this);
		etUsername.requestFocus();
		etEmail = (EditText) findViewById(R.id.et_email);
        etEmail.setOnFocusChangeListener(this);
		etPassword = (EditText) findViewById(R.id.et_password);
		etPassword.setOnFocusChangeListener(this);
		etPassword2 = (EditText) findViewById(R.id.et_confirm_password);
		etPassword2.setOnFocusChangeListener(this);
		
		Button btnRegister = (Button) findViewById(R.id.btn_register);
		btnRegister.setOnClickListener(this);
		TextView login = (TextView) findViewById(R.id.btn_cancel);
        
		// make the underline style
		CharSequence text = login.getText();
        SpannableString spanable = new SpannableString(text);
        spanable.setSpan(new UnderlineSpan(), text.length() - 4, text.length(), 0);
        login.setText(spanable);
		login.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		
		case R.id.btn_register:
			onClickRegister();
			break;
			
		case R.id.btn_cancel:
		    
		    Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
		    finish();
		    startActivity(loginIntent);
		    break;
		}
	}
	
    @Override
    public void onFocusChange(View v, boolean flag) {
        
        switch (v.getId()) {
        case R.id.et_username:
            
            if (!flag) {
                checkUserName();
            }
            break;
            
        case R.id.et_email:
            
            if (!flag) {
                checkEmail();
            }
            break;
            
        case R.id.et_password:
            
            if (!flag) {
                checkPassword(etPassword);
            }
            break;
            
        case R.id.et_confirm_password:

            if (!flag) {
                checkPassword(etPassword2);
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
     * 检查用户邮箱合法性
     * 1 不能为空
     * 2 长度在6 - 40 个字符之间
     * 3 符合邮箱格式
     */
    private boolean checkEmail() {
        String input = etEmail.getText().toString();
        if (TextUtils.isEmpty(input)) {
            etEmail.setError(getString(R.string.error_email_empty));
            return false;
        } else {
            etEmail.setError(null);
        }
        int length = input.length();
        if (length < 6 || length > 40) {
            etEmail.setError(getString(R.string.error_email_length_invalid));
            return false;
        } else {
            etEmail.setError(null);
        }
        if(!EMAIL_ADDRESS_PATTERN.matcher(input).find()) {
            etEmail.setError(getString(R.string.error_email_format_invalid));
            return false;
        } else {
            etEmail.setError(null);
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
    
    /*
     * 检查两次密码相同
     */
    private boolean checkPasswordSame() {
        String psw1 = etPassword.getText().toString();
        String psw2 = etPassword2.getText().toString();
        if (!psw1.equals(psw2)) {
            etPassword2.setError(getString(R.string.error_password_not_same));
            return false;
        } else {
            etPassword2.setError(null);
            return true;
        }
    }

    /*
     * 点击注册按钮 
     */
    private void onClickRegister() {
        // 验证成功，进行注册操作
        if(checkUserName() 
                && checkEmail()
                && checkPassword(etPassword)
                && checkPassword(etPassword2)
                && checkPasswordSame()) {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();
            String email = etEmail.getText().toString();
            MarketAPI.register(getApplicationContext(), this, username, password, email);
            Utils.trackEvent(getApplicationContext(), Constants.GROUP_10,
                    Constants.REGISTER);
            if (!isFinishing()) {
                showDialog(DIALOG_REGISTERING);
            }
        }
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

        if (id == DIALOG_REGISTERING) {
            ProgressDialog mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage(getString(R.string.registering));
            return mProgressDialog;
        }
        return super.onCreateDialog(id);
    }

	@SuppressWarnings("unchecked")
    @Override
	public void onSuccess(int method, Object obj) {
        try {
            dismissDialog(DIALOG_REGISTERING);
        } catch (IllegalArgumentException e) {
        }
        Utils.trackEvent(getApplicationContext(), Constants.GROUP_10,
                Constants.REGISTER_SUCCESS);
        Utils.makeEventToast(getApplicationContext(), getString(R.string.register_ok), false);
        HashMap<String, String> result = (HashMap<String, String>) obj;
        mSession.setUid(result.get(Constants.KEY_USER_UID));
        mSession.setUserName(result.get(Constants.KEY_USER_NAME));
        setResult(Activity.RESULT_OK);
        mSession.setLogin(true);
        finish();
	}

	@Override
    public void onError(int method, int statusCode) {
        try {
            dismissDialog(DIALOG_REGISTERING);
        } catch (IllegalArgumentException e) {
        }
        String msg = null;
        switch (statusCode) {
        case ERROR_CODE_USERNAME_INVALID:
            msg = getString(R.string.error_username_invalid);
            break;
        case ERROR_CODE_USERNAME_EXIST:
            msg = getString(R.string.error_username_exist);
            break;
        case ERROR_CODE_EMAIL_INVALID_FORMAT:
            msg = getString(R.string.error_email_invalid);
            break;
        case ERROR_CODE_EMAIL_EXIST:
            msg = getString(R.string.error_email_exist);
            break;
        case ERROR_CODE_PASSWORD_INVALID:
            msg = getString(R.string.error_password_invalid);
            break;
        default:
            msg = getString(R.string.error_other);
            break;
        }
        Utils.makeEventToast(getApplicationContext(), msg, false);
    }
}
