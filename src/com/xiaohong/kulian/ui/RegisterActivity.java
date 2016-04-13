/*
 * Copyright (C) 2016 Shanghai Xiaohong.Inc
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.util.DialogUtils;
import com.xiaohong.kulian.common.util.TopBar;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.widget.BaseActivity;
public class RegisterActivity extends BaseActivity 
    implements OnClickListener, OnFocusChangeListener, ApiRequestListener {

    private static final String TAG = "RegisterActivity";
    private static final int DIALOG_PROGRESS = 0;

    // 用户不存在（用户名错误）
    private static final int ERROR_CODE_USERNAME_NOT_EXIST = 211;
    // 用户密码错误
    private static final int ERROR_CODE_PASSWORD_INVALID = 212;
    
    private EditText etUsername;
    private EditText etVerifyCode;
    private EditText etInviteCode;
    private SmsObserver mSmsObserver;
    //验证码倒计时
    private static TimeCount time;
    private long time_default=60000;
    private Button btnVerifyCode;
    private TextView license_tv;
    
    class SmsObserver extends ContentObserver {

        public SmsObserver(Context context, Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            // 每当有新短信到来时，使用我们获取短消息的方法
            getSmsFromPhone();
        }
    }

    public Handler smsHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            System.out.println("smsHandler 执行了.....");
        };
    };

    private Uri SMS_INBOX = Uri.parse("content://sms/");

    public void getSmsFromPhone() {
        ContentResolver cr = getContentResolver();
        String[] projection = new String[] { "body", "address", "person" };// "_id",
                                                                            // "address",
        // "person",, "date",
        // "type
        String where = " date >  " + (System.currentTimeMillis() - 10 * 60 * 1000);
        Cursor cur = null;
        try {  
            cur = cr.query(SMS_INBOX, projection, where, null, "date desc"); 
        } catch (Exception e) {
            e.printStackTrace();  
            return ;  
        }

        if (null == cur)
            return;
        if (cur.moveToNext()) {
            String number = cur.getString(cur.getColumnIndex("address"));// 手机号
            String name = cur.getString(cur.getColumnIndex("person"));// 联系人姓名列表
            String body = cur.getString(cur.getColumnIndex("body"));

            System.out.println(">>>>>>>>>>>>>>>>手机号：" + number);
            System.out.println(">>>>>>>>>>>>>>>>联系人姓名列表：" + name);
            System.out.println(">>>>>>>>>>>>>>>>短信的内容：" + body);

            // 【小鸿网络】您的验证码是8992。如非本人操作，请忽略本短信
            Pattern pattern = Pattern.compile("【小鸿网络】您的验证码是[0-9]{4}");
            Matcher matcher = pattern.matcher(body);
            if (matcher.find()) {
                String match = matcher.group();
                String res = match.substring(12, 16);// 获取短信中的验证码

                System.out.println(res);
                etVerifyCode.setText(res);
                // stop observer
                getContentResolver().unregisterContentObserver(mSmsObserver);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_layout);
        initView();
        mSmsObserver = new SmsObserver(this, smsHandler);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        etUsername = null;
        etVerifyCode = null;
        etInviteCode = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(Activity.RESULT_CANCELED);
            hideKeyBoard();
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initView() {

        initTopBar();
        etUsername = (EditText) findViewById(R.id.et_username);
        String userName = TextUtils.isEmpty(mSession.getUserName()) ? "" : mSession.getUserName();
        etUsername.setText(userName);
        etUsername.setOnFocusChangeListener(this);
        etUsername.requestFocus();
        etVerifyCode = (EditText) findViewById(R.id.et_verify_code);
        etVerifyCode.setOnFocusChangeListener(this);
        etInviteCode = (EditText) findViewById(R.id.et_invite_code);
        etInviteCode.setOnFocusChangeListener(this);
        
        if (!TextUtils.isEmpty(userName)) {
            etVerifyCode.requestFocus();
        }
        
        Button btnRegister = (Button) findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(this);
        btnVerifyCode = (Button) findViewById(R.id.btn_verify_code);
        btnVerifyCode.setOnClickListener(this);
        
        license_tv = (TextView) findViewById(R.id.license_tv);
        license_tv.setText(Html.fromHtml(getResources().getString(
                R.string.register_license_hint)));
        license_tv.setOnClickListener(this);
    }

    private void initTopBar() {

        TopBar.createTopBar(this, new View[] { findViewById(R.id.back_btn),
                findViewById(R.id.top_bar_title) }, new int[] { View.INVISIBLE,
                View.VISIBLE }, getString(R.string.register_title));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_register:
            register();
            break;
        case R.id.btn_verify_code:
            onClickVerifyCodeBtn();
            time = new TimeCount(time_default, 1000);
            time.start();
            break;
        case R.id.license_tv:
            Intent detailIntent = new Intent(getApplicationContext(), WebviewActivity.class);
            detailIntent.putExtra("extra.url", "file:///android_asset/license.html");
            detailIntent.putExtra("extra.title", "服务协议");
            startActivityForResult(detailIntent, 0);
            break;
        default:
            break;
        }
    }

    private void register() {
        
        // 检查用户输入
        if (!checkUserName() || !checkVerifyCode(etVerifyCode) || !checkInviteCode(etInviteCode)) {
            return;
        }
        
        if (!isFinishing()) {
            showDialog(DIALOG_PROGRESS);
        } else {
            // 如果当前页面已经关闭，不进行登录操作
            return;
        }
        String userName = etUsername.getText().toString();
        String password = userName.substring(5,11);
        String verifyCode = etVerifyCode.getText().toString();
        String inviteCode = etInviteCode.getText().toString();
        MarketAPI.register(getApplicationContext(), this, userName, password, verifyCode, inviteCode);
        
        Utils.trackEvent(getApplicationContext(), Constants.GROUP_9,
                Constants.LOGIN);
    }

    private void onClickVerifyCodeBtn() {
        if (checkUserName()) {
            // start observe sms
            getContentResolver().registerContentObserver(SMS_INBOX, true, mSmsObserver);

            String url = MarketAPI.API_BASE_URL+"/appverifycode?phone_number="+etUsername.getText().toString();
            if (Utils.isLeShiMobile()) {
                url += "&leshi=1";
            }
            String ret = Utils.httpGet(url);
            if (ret != null) {
                try {
                    JSONObject obj = new JSONObject(ret);
                    if (obj.getInt("ret_code") == 0) {
                        DialogUtils.showMessage(this, null, "验证码已通过短信发送");
                    } else {
                        DialogUtils.showMessage(this, "错误", obj.getString("ret_msg"));
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }                    
                Log.d(TAG, ret);
            } else {
                DialogUtils.showMessage(this, "网络错误", "请检查网络连接");
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
        case MarketAPI.ACTION_REGISTER:
            
            Utils.trackEvent(getApplicationContext(), Constants.GROUP_9,
                    Constants.LOGIN_SUCCESS);

            HashMap<String, Object> result = (HashMap<String, Object>) obj;
            String userName = etUsername.getText().toString();
            String password = etUsername.getText().toString().substring(5,11);
            mSession.setUserName(userName);
            mSession.setPassword(password);
            mSession.setCoinNum((Integer) result.get(Constants.KEY_COIN_NUM));
            mSession.setToken((String) result.get(Constants.KEY_TOKEN));
            if (result.containsKey(Constants.KEY_SIGN_IN_TODAY)) {
                mSession.setSignInToday(result.get(Constants.KEY_SIGN_IN_TODAY).toString().equals("true"));    
            } else {
                mSession.setSignInToday(false);
            }
            mSession.setLogin(true);
            // 隐藏登录框
            try{
                dismissDialog(DIALOG_PROGRESS);
            }catch (IllegalArgumentException e) {
            }
            hideKeyBoard();
            finish();
            break;
            
        default:
            break;
        }
    }

    @Override
    public void onError(int method, int statusCode) {

        switch (method) {
        case MarketAPI.ACTION_REGISTER:
            
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
            
        default:
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

        case R.id.et_verify_code:
            if (!flag) {
                checkVerifyCode(etVerifyCode);
            }
            break;

        case R.id.et_invite_code:
            if (!flag) {
                checkInviteCode(etInviteCode);
            }
            break;
        default:
            break;
        }
    }
    
    /*
     * 检查用户名合法性
     * 必须为手机号
     */
    private boolean checkUserName() {
        String input = etUsername.getText().toString();
        if (TextUtils.isEmpty(input)) {
            etUsername.setError(getDisplayText(R.string.error_username_empty));
            return false;
        } else {
            etUsername.setError(null);
        }
        int length = input.length();
        if (length != 11) {
            etUsername.setError(getDisplayText(R.string.error_username_length_invalid));
            return false;
        } else {
            etUsername.setError(null);
        }
        // todo: must be pure number
        return true;
    }

    /*
     * 检查邀请码合法性
     * 4位数字
     */
    private boolean checkVerifyCode(EditText input) {
        String verifyCode = input.getText().toString();
        if (TextUtils.isEmpty(verifyCode)) {
            input.setError(getDisplayText(R.string.error_verifycode_empty));
            return false;
        } else {
            input.setError(null);
        }
        int length = verifyCode.length();
        if (length != 4) {
            input.setError(getDisplayText(R.string.error_verifycode_length_invalid));
            return false;
        } else {
            input.setError(null);
        }
        // todo: must be pure number
        return true;
    }
    private CharSequence getDisplayText(int resId) {
        return Html.fromHtml("<font color='#2C78D4'>"+getString(resId)+"</font>");
    }
    /*
     * 检查邀请码合法性
     * 6位数字
     */
    private boolean checkInviteCode(EditText input) {
        String inviteCode = input.getText().toString();
        if (TextUtils.isEmpty(inviteCode)) { // 可不填
            return true;
        }
        int length = inviteCode.length();
        if (length != 6) {
            input.setError(getDisplayText(R.string.error_invitecode_length_invalid));
            return false;
        } else {
            input.setError(null);
        }
        // todo: must be pure number
        return true;
    }

    private void hideKeyBoard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);            
        if (imm.isActive()&&getCurrentFocus()!=null) {
            if (getCurrentFocus().getWindowToken()!=null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }
    
    /**
     * 验证码倒计时
     * @author Albert Liu
     *
     */
    class TimeCount extends CountDownTimer {
    public TimeCount(long millisInFuture, long countDownInterval) {
    super(millisInFuture, countDownInterval);//参数依次为总时长,和计时的时间间隔
    }
    @Override
    public void onFinish() {//计时完毕时触发
        btnVerifyCode.setText("获取验证码");
        btnVerifyCode.setBackgroundColor(Color.parseColor("#F5A623"));
        btnVerifyCode.setClickable(true);
    }
    @Override
    public void onTick(long millisUntilFinished){//计时过程显示
        btnVerifyCode.setText(
                            (millisUntilFinished%(1000*60))/1000+"秒以后重新获取");
        btnVerifyCode.setClickable(false);
        btnVerifyCode.setBackgroundColor(Color.GRAY);
    }
    }

}

