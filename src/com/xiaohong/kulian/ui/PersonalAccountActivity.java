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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.util.TopBar;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.vo.PayAndChargeLog;
import com.xiaohong.kulian.common.vo.PayAndChargeLogs;
import com.xiaohong.kulian.common.widget.BaseActivity;
import com.xiaohong.kulian.common.widget.LoadingDrawable;

/**
 * this view is displaying for login success to personal center
 * 
 * @author cong.li
 * @date 2011-5-17
 */
public class PersonalAccountActivity extends BaseActivity implements
        android.view.View.OnClickListener, ApiRequestListener {

    private static final int ACCOUNT_REGIST = 0;
    private static final int REQUEST_CODE = 20;
    public static final int REGIST = 1;

    // 购买信息列表
//    private ListView mList;
    private FrameLayout mLoading;

    private PersonalAccountAdapter mAdapter;
    private ProgressBar mProgress;
    //是否正在云绑定
    private boolean isBinding;
    //是否已经登陆过
    private boolean isFirstAccess = true;
    //个人中心功能界面
    private RelativeLayout layout_task,layout_message,layout_question,layout_feedback,layout_account;
    private Intent intent_next;
    private TextView textView_login,textView_username;

//    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_account_fragment_layout);

//        initTopBar();
        initView();
    }
    
    @Override
    protected void onResume() {
//        if (mSession.isLogin() && isFirstAccess) {
//            mProgress.setVisibility(View.VISIBLE);
//        }
//    	System.out.println("mSession"+mSession.getSim());
//    	System.out.println("mSession"+mSession.getUserName());
    	if (!mSession.isLogin()) {
    		textView_login.setText("登录");
        } else if (mSession.isLogin()) {
        	textView_login.setText("账号退出");
        	textView_username.setText(mSession.getUserName());
        }
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        if(mSession.isLogin())
            isFirstAccess = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(mReceiver);
    }

/*    private void initTopBar() {
        TopBar.createTopBar(getApplicationContext(),
                new View[] { findViewById(R.id.top_bar_title) },
                new int[] { View.VISIBLE },
                getString(R.string.person_account_title));
    }*/

    private void initView() {

//        mList = (ListView) this.findViewById(android.R.id.list);
    	layout_task=(RelativeLayout)this.findViewById(R.id.person_account_my_task_value_layout);
    	layout_message=(RelativeLayout)this.findViewById(R.id.person_account_message_center_value_layout);
    	layout_question=(RelativeLayout)this.findViewById(R.id.person_account_normol_question_layout);
    	layout_feedback=(RelativeLayout)this.findViewById(R.id.person_account_feedback_value_layout);
    	layout_account=(RelativeLayout)this.findViewById(R.id.person_account_logout_value_layout);
    	layout_task.setOnClickListener(this);
    	layout_message.setOnClickListener(this);
    	layout_question.setOnClickListener(this);
    	layout_feedback.setOnClickListener(this);
    	layout_account.setOnClickListener(this);
    	textView_login=(TextView)this.findViewById(R.id.person_account_logout_value_text);
    	textView_username=(TextView)this.findViewById(R.id.user_name_text);
    	if (!mSession.isLogin()) {
    		textView_login.setText("登录");
        } else if (mSession.isLogin()) {
        	textView_login.setText("账号退出");
        }

        /*mLoading = (FrameLayout) findViewById(R.id.loading);
        mProgress = (ProgressBar) mLoading.findViewById(R.id.progressbar);
        mProgress.setIndeterminateDrawable(new LoadingDrawable(
                getApplicationContext()));*/
        
        mAdapter = doInitPayAdapter();
//        mList.setAdapter(mAdapter);
//        mList.setItemsCanFocus(false);
//        mList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
//        
//        mList.setOnItemClickListener(this);
    }
    
    public boolean getCurrentBindStatue(){
        return isBinding;
    }
    
    /*
     * 初始化支付信息列表
     */
    private PersonalAccountAdapter doInitPayAdapter() {

        return new PersonalAccountAdapter(this,
                doInitFuncData(),
                R.layout.activity_personal_account_header_item, new String[] {
                        Constants.ACCOUNT_ICON, Constants.ACCOUNT_TITLE,
                        Constants.ACCOUNT_DESC, Constants.ACCOUNT_TIME,
                        Constants.ACCOUNT_DOWNLOAD, Constants.ACCOUNT_ARROW },
                new int[] { R.id.iv_icon, R.id.tv_name, R.id.tv_description,
                        R.id.tv_time, R.id.cb_operation, R.id.iv_arrow },
                mHandler);
    }

    /*
     * 初始化功能菜单栏数据
     * 
     * @return
     */
    private ArrayList<HashMap<String, Object>> doInitFuncData() {
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

        int[] icons = new int[] {
                R.drawable.person_center_logo,
//                R.drawable.person_center_payment,
                R.drawable.person_center_logo,
                R.drawable.person_center_logo,
                R.drawable.person_center_logo
                };
        String[] titles = new String[] {
                getString(R.string.account_logo_title),
//                getString(R.string.account_payment_title),
                getString(R.string.account_feedback_title),
                getString(R.string.account_pay_title),
                getString(R.string.account_about_title)
                };

        String pkName = this.getPackageName();
        String versionName = "";
        try {
            versionName = this.getPackageManager().getPackageInfo(pkName, 0).versionName;
        } catch (NameNotFoundException e) {
        }
        
        String[] descs = new String[] {
                getString(R.string.account_logo_desc),
//                getString(R.string.account_payment_desc),
                "有问题就反馈",
                "买买买",
                "WIFI酷连 v"+versionName};

        for (int i = 0; i < icons.length; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put(Constants.ACCOUNT_ICON, icons[i]);
            map.put(Constants.ACCOUNT_TITLE, titles[i]);
            map.put(Constants.ACCOUNT_DESC, descs[i]);
//            map.put(Constants.ACCOUNT_DOWNLOAD,R.drawable.cloud_off);
            map.put(Constants.ACCOUNT_ARROW, R.drawable.more_indicator);
            map.put(Constants.ACCOUNT_TYPE, Constants.FLAG_HEADER_ITEM);
            data.add(map);
        }
        return data;
    }

    /*
     * 将对象PayAndChargeLogs转换成所需的数据格式
     */
    private ArrayList<HashMap<String, Object>> transferDataType(PayAndChargeLogs logs) {

        ArrayList<HashMap<String, Object>> data = null;
        ArrayList<PayAndChargeLog> logList = logs.payAndChargeLogList;
        if (logs != null && logList.size() > 0) {
            data = new ArrayList<HashMap<String, Object>>(logs.totalSize + 1);
            HashMap<String, Object> group = new HashMap<String, Object>();
            group.put(Constants.ACCOUNT_TYPE, Constants.FLAG_GROUP_ITEM);
            group.put(Constants.ACCOUNT_TIME, String.format(
                    this.getString(R.string.account_payed_count),
                    logs.totalSize));
            group.put(Constants.ACCOUNT_TITLE,
                    this.getString(R.string.account_payed_history));
            group.put(Constants.KEY_PLACEHOLDER, true);
            data.add(group);
            for (PayAndChargeLog log : logList) {
                HashMap<String, Object> item = new HashMap<String, Object>();
                String url = log.iconUrl;
                item.put(Constants.ACCOUNT_ICON, url);
                item.put(Constants.ACCOUNT_TITLE, log.name);
                item.put(Constants.ACCOUNT_DESC, String.format(
                        getString(R.string.kulian_money), log.payment));
                item.put(Constants.ACCOUNT_TIME, log.time + " "
                        + getString(R.string.account_payed));
                item.put(Constants.ACCOUNT_TYPE, log.type);
                data.add(item);
            }
        }
        return data;
    }

    @Override
    public void onSuccess(int method, Object obj) {
        switch (method) {
            
        default:
            break;
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            // 注销
            case REGIST:
//                ArrayList<HashMap<String, Object>> data = doInitFuncData();
//                mAdapter.changeDataSource(data);
            	textView_login.setText("登录");
            	textView_username.setText("未登录");
                break;
            }
        };
    };

    @Override
    public void onError(int method, int statusCode) {
        switch (method) {

        default:
            break;
        }
    }

    /*@Override
    public void onItemClick(AdapterView<?> parent, View view, int position,    long id) {
        switch (position) {
        case 0: {
            if (!mSession.isLogin()) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            } else if (mSession.isLogin()) {
                showDialog(ACCOUNT_REGIST);
            }
            break;
        }
        case 1: {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), FeedBackActivity.class);
            startActivity(intent);
            break;
        }
        case 2: {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), PayMainActivity.class);
            startActivity(intent);
            break;
        }
        case 3:
            break;
        default:
            break;
        }
    }
*/
    @Override
    protected Dialog onCreateDialog(final int id) {
        switch (id) {
        // 注销帐号
        case ACCOUNT_REGIST:
            return new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle(getString(R.string.sure_to_regist))
                    .setPositiveButton(R.string.yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    mSession.setLogin(false);
                                    mSession.setUid(null);
                                    isFirstAccess = true;
                                    mHandler.sendEmptyMessage(REGIST);
                                }
                            })
                    .setNegativeButton(R.string.no,
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    PersonalAccountActivity.this
                                            .dismissDialog(id);
                                    mAdapter.notifyDataSetChanged();
                                }
                            }).create();

        default:
            break;
        }
        return super.onCreateDialog(id);
    }

	@Override
	/**
	 * 个人中心点击事件按钮
	 * author albert liu 2016-3-28
	 * @param v
	 */
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.person_account_my_task_value_layout:
			
			break;
		case R.id.person_account_message_center_value_layout:
			
			break;
		case R.id.person_account_normol_question_layout:
	        Intent detailIntent = new Intent(getApplicationContext(), WebviewActivity.class);
	        detailIntent.putExtra("extra.url", "file:///android_asset/FAQ.html");
	        detailIntent.putExtra("extra.title", "常见问题");
	        startActivity(detailIntent);
			break;
		case R.id.person_account_feedback_value_layout:
			intent_next= new Intent();
			intent_next.setClass(getApplicationContext(), FeedBackActivity.class);
            startActivity(intent_next);
			break;
		case R.id.person_account_logout_value_layout:			
			if (!mSession.isLogin()) {
				intent_next = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivityForResult(intent_next, REQUEST_CODE);
            } else if (mSession.isLogin()) {
                showDialog(ACCOUNT_REGIST);
            }
            break;

		default:
			break;
		}
		
	}
}
