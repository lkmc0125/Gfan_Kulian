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

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.util.CustomDialog;
import com.xiaohong.kulian.common.widget.BaseActivity;

/**
 * this view is displaying for login success to personal center
 * 
 * @author cong.li
 * @date 2011-5-17
 */
public class PersonalAccountActivity extends BaseActivity implements android.view.View.OnClickListener,
        ApiRequestListener {

    private static final int ACCOUNT_REGIST = 0;
    private static final int REQUEST_CODE = 20;
    public static final int REGIST = 1;

    // 个人中心功能界面
    private RelativeLayout layout_task, layout_message, layout_question, layout_feedback, layout_account, layout_buy,
            layout_sign_in;
    private Intent intent_next;
    private TextView textView_login, textView_username, textView_signIn_status, textView_coin_num;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_account_fragment_layout);

        initView();
    }

    @Override
    protected void onResume() {

        if (!mSession.isLogin()) {
            textView_login.setText("登录");
            textView_username.setText("未登录");
            textView_signIn_status.setText(R.string.person_account_sign_in_value);
        } else if (mSession.isLogin()) {
            textView_login.setText("账号退出");
            textView_username.setText(mSession.getUserName());
            textView_coin_num.setText(mSession.getCoinNum().toString());
            if (mSession.getSignInToday()) {
                textView_signIn_status.setText("今天已经签到");
            } else {
                textView_signIn_status.setText(R.string.person_account_sign_in_value);
            }
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unregisterReceiver(mReceiver);
    }

    private void initView() {
        layout_sign_in = (RelativeLayout) this.findViewById(R.id.sign_in_layout);
        layout_task = (RelativeLayout) this.findViewById(R.id.person_account_my_task_value_layout);
        layout_message = (RelativeLayout) this.findViewById(R.id.person_account_message_center_value_layout);
        layout_question = (RelativeLayout) this.findViewById(R.id.person_account_normol_question_layout);
        layout_feedback = (RelativeLayout) this.findViewById(R.id.person_account_feedback_value_layout);
        layout_account = (RelativeLayout) this.findViewById(R.id.person_account_logout_value_layout);
        layout_buy = (RelativeLayout) this.findViewById(R.id.buy_layout);
        layout_sign_in.setOnClickListener(this);
        layout_task.setOnClickListener(this);
        layout_message.setOnClickListener(this);
        layout_question.setOnClickListener(this);
        layout_feedback.setOnClickListener(this);
        layout_account.setOnClickListener(this);
        layout_buy.setOnClickListener(this);
        textView_login = (TextView) this.findViewById(R.id.person_account_logout_value_text);
        textView_username = (TextView) this.findViewById(R.id.user_name_text);
        textView_signIn_status = (TextView) this.findViewById(R.id.sign_in_value_text);
        textView_coin_num = (TextView) this.findViewById(R.id.coin_text);
        textView_coin_num.setText("0");
        if (!mSession.isLogin()) {
            textView_login.setText("登录");
        } else if (mSession.isLogin()) {
            textView_login.setText("账号退出");
        }
    }

    @Override
    public void onSuccess(int method, Object obj) {
        switch (method) {
        case MarketAPI.ACTION_SIGN_IN: {
            HashMap<String, Object> result = (HashMap<String, Object>) obj;
            if ((Integer) result.get("ret_code") == 0) {
                mSession.setCoinNum((Integer) result.get(Constants.KEY_COIN_NUM));
                mSession.setSignInToday(true);
                textView_coin_num.setText(mSession.getCoinNum().toString());
                textView_signIn_status.setText("今天已签到");

                CustomDialog dialog = new CustomDialog.Builder(this).setTitle(getString(R.string.sign_in_success))
                        .setMessage("本次签到获得了" + result.get(Constants.KEY_ADD_COIN_NUM) + "个金币")
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
                dialog.show();
            } else if ((Integer) result.get("ret_code") == 3002) { // 今天已经签过到了

                mSession.setSignInToday(true);
                textView_signIn_status.setText("今天已签到");

                CustomDialog dialog = new CustomDialog.Builder(this).setTitle(getString(R.string.sign_in_success))
                        .setMessage("今天已经领取过金币了，明天再来哦")
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
                dialog.show();

            } else { // 签到失败
                CustomDialog dialog = new CustomDialog.Builder(this).setTitle(getString(R.string.sign_in_fail))
                        .setMessage(result.get("ret_msg").toString())
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
                dialog.show();
            }
            break;
        }
        case MarketAPI.ACTION_LOGIN: {
            HashMap<String, Object> result = (HashMap<String, Object>) obj;
            if ((Integer) result.get("ret_code") == 0) {
                textView_login.setText("账号退出");
                textView_username.setText(mSession.getUserName());
                mSession.setLogin(true);
                mSession.setCoinNum((Integer) result.get(Constants.KEY_COIN_NUM));
                textView_coin_num.setText(mSession.getCoinNum().toString());
                if (result.containsKey(Constants.KEY_SIGN_IN_TODAY)) {
                    mSession.setSignInToday(result.get(Constants.KEY_SIGN_IN_TODAY).equals("true"));
                }
                if (mSession.getSignInToday()) {
                    textView_signIn_status.setText("今天已签到");
                } else {
                    MarketAPI.signIn(getApplicationContext(), this);
                }
            }
            break;
        }
        default:
            break;
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            // 注销
            case REGIST:
                // ArrayList<HashMap<String, Object>> data = doInitFuncData();
                // mAdapter.changeDataSource(data);
                textView_login.setText("登录");
                textView_username.setText("未登录");
                break;
            }
        };
    };

    @Override
    public void onError(int method, int statusCode) {
        switch (method) {
        case MarketAPI.ACTION_SIGN_IN:
            CustomDialog dialog = new CustomDialog.Builder(this).setTitle(getString(R.string.sign_in_fail))
                    .setMessage("错误码: " + statusCode)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create();
            dialog.show();
            break;
        default:
            break;
        }
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        switch (id) {
        // 注销帐号
        case ACCOUNT_REGIST:
            return new CustomDialog.Builder(this).setMessage(getString(R.string.sure_to_regist))
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mSession.setLogin(false);
                            mSession.setUid(null);
                            mHandler.sendEmptyMessage(REGIST);
                            dialog.dismiss();
                        }
                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create();

        default:
            break;
        }
        return super.onCreateDialog(id);
    }

    private boolean autoLogin() {
        if (mSession.getUserName() != null && mSession.getUserName().length() > 0 && mSession.getPassword() != null
                && mSession.getPassword().length() > 0) {
            MarketAPI.login(getApplicationContext(), this, mSession.getUserName(), mSession.getPassword());
            return true;
        } else {
            return false;
        }
    }

    @Override
    /**
     * 个人中心点击事件按钮
     * author albert liu 2016-3-28
     * @param v
     */
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.sign_in_layout:
            if (!mSession.isLogin()) {
                if (!autoLogin()) {
                    intent_next = new Intent(getApplicationContext(), RegisterActivity.class);
                    startActivityForResult(intent_next, REQUEST_CODE);
                }
            } else {
                MarketAPI.signIn(getApplicationContext(), this);
            }
            break;
        case R.id.person_account_my_task_value_layout:

            break;
        case R.id.person_account_message_center_value_layout:
            Intent MessagesIntent = new Intent(getApplicationContext(), MessageListActivity.class);
            startActivityForResult(MessagesIntent, REQUEST_CODE);
            break;
        case R.id.person_account_normol_question_layout:
            Intent detailIntent = new Intent(getApplicationContext(), WebviewActivity.class);
            detailIntent.putExtra("extra.url", "file:///android_asset/FAQ.html");
            detailIntent.putExtra("extra.title", "常见问题");
            startActivityForResult(detailIntent, REQUEST_CODE);
            break;
        case R.id.person_account_feedback_value_layout:
            intent_next = new Intent();
            intent_next.setClass(getApplicationContext(), FeedBackActivity.class);
            startActivityForResult(intent_next, REQUEST_CODE);
            break;
        case R.id.person_account_logout_value_layout:
            if (!mSession.isLogin()) {
                intent_next = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivityForResult(intent_next, REQUEST_CODE);
            } else if (mSession.isLogin()) {
                showDialog(ACCOUNT_REGIST);
            }
            break;
        case R.id.buy_layout:
            Intent PayIntent = new Intent(getApplicationContext(), PayMainActivity.class);
            startActivity(PayIntent);
            break;
        default:
            break;
        }
    }
}
