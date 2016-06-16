package com.xiaohong.kulian.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import aga.fdf.grd.os.df.AdForm;
import aga.fdf.grd.os.df.AdTaskStatus;
import aga.fdf.grd.os.df.AppSummaryDataInterface;
import aga.fdf.grd.os.df.AppSummaryObject;
import aga.fdf.grd.os.df.AppSummaryObjectList;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Session.OnCoinUpdatedListener;
import com.xiaohong.kulian.Session.OnLoginListener;
import com.xiaohong.kulian.adapter.ConnectionAppGridAdapter;
import com.xiaohong.kulian.bean.MessageBean;
import com.xiaohong.kulian.bean.MessageListBean;
import com.xiaohong.kulian.bean.TaskBean;
import com.xiaohong.kulian.bean.TaskListBean;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.util.DialogUtils;
import com.xiaohong.kulian.common.util.MySharedpreference;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.util.WifiAdmin;
import com.xiaohong.kulian.common.util.WifiAuthentication;
import com.xiaohong.kulian.common.widget.AutoScrollTextViewH;
import com.xiaohong.kulian.common.widget.BaseActivity;
import com.xiaohong.kulian.common.widget.CustomDialog;
import com.xiaohong.kulian.common.widget.RoundImageView;
import com.xiaohong.kulian.ui.guide.GuideActivity;
public class ConnectionActivity extends BaseActivity implements
        ApiRequestListener, OnClickListener, AppSummaryDataInterface,
        OnLoginListener, OnCoinUpdatedListener {
    private static final String TAG = "ConnectionActivity";

    private WifiAuthentication mAuth;
    private WifiAdmin mWifiAdmin;
    private Integer mLoginRetryCount = 0;
    private String mCurrentSSID;
    private MyBroadcastReceiver mConnectionReceiver;
    private boolean isLeftTimeDialogShown = false;
    /**
     * 连接状态页面
     */
    private Button mAuthBtn;
    private TextView mWifiStatusTitle, mWifiStatusDesc;
    private ImageView mWifiStatusIcon;
    private AutoScrollTextViewH mAutoScroolView;
    /**
     * 签到界面
     */
    private RelativeLayout layoutSignIn;
    private RelativeLayout layoutBuyCoin;
    private static final int REQUEST_CODE = 20;
    private TextView textView_coin_num;
    private TextView textView_signIn_status;
    /**
     * 推荐应用界面
     */
    private GridView mGridView;
    private ConnectionAppGridAdapter connectionAppGridAdapter;
    private TextView textViewAllApp;
    private AppSummaryObjectList mAdList;
    /**
     * 任务界面
     */
    private RoundImageView imageViewTaskImg;
    private TextView textViewTaskName;
    private TextView textViewTaskDsb;
    private TextView textViewTaskCoin;
    private TextView textViewTaskCheck;
    private String TaskImgUrl, TaskName, TaskDsb, TaskCoin;
    private RelativeLayout viewAllTask;
    private TaskBean taskBean;

    private UpdateLeftTimeThread mCountDownThread;
    /**
     * 帮助点击按钮
     */
    private ImageView imageView_help;
    
    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "onReceive");
            checkWifiConnection();
        }
    }

    private enum ConnectionStatus {
        DISCONNECTED, CONNECTED, HONGWIFI, HONGWIFI_AUTHED
    };

    private ConnectionStatus mConnectionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_main_layout);
        initView();
        registerConnection();
        mWifiAdmin = new WifiAdmin(getApplicationContext());
        mAuth = new WifiAuthentication(ConnectionActivity.this);
        mConnectionStatus = ConnectionStatus.DISCONNECTED;
        mSession.addOnCoinUpdateListener(this);
        mSession.addLoginListener(this);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                boolean open = mWifiAdmin.openWifi();
                Log.i(TAG, "wifi open:" + open);
                mWifiAdmin.startScan();
            }
        }, 500);
    }

    @Override
    protected void onDestroy() {
        mSession.removeLoginListener(this);
        mSession.removeOnCoinUpdateListener(this);
        super.onDestroy();
    }
    
    private void initView() {
        /**
         * 连接界面
         */
        mAuthBtn = (Button) findViewById(R.id.auth_button);
        mAuthBtn.setOnClickListener(this);
        mWifiStatusTitle = (TextView) findViewById(R.id.wifi_status_title);
        mWifiStatusDesc = (TextView) findViewById(R.id.wifi_status_desc);
        mWifiStatusIcon = (ImageView) findViewById(R.id.wifi_status_icon);
        mAutoScroolView = (AutoScrollTextViewH) findViewById(R.id.connection_current_activity_info_text);
        mAutoScroolView.setOnClickListener(this);
        /**
         * 签到界面
         */
        layoutSignIn = (RelativeLayout) findViewById(R.id.sign_in_layout);
        layoutSignIn.setOnClickListener(this);
        layoutBuyCoin = (RelativeLayout) findViewById(R.id.buy_layout);
        layoutBuyCoin.setOnClickListener(this);
        textView_coin_num = (TextView) findViewById(R.id.coin_text);
        textView_coin_num.setText("0");
        textView_signIn_status = (TextView) findViewById(R.id.sign_in_value_text);
        /**
         * 推荐应用界面
         */
        mGridView = (GridView) findViewById(R.id.connect_recommend_app_gridView_layout);
        textViewAllApp = (TextView) findViewById(R.id.connection_recommend_all_app_text);
        textViewAllApp.setOnClickListener(this);
        /**
         * 任务界面
         */
        imageViewTaskImg = (RoundImageView) findViewById(R.id.iconnection_recommend_task_logo);
        textViewTaskName = (TextView) findViewById(R.id.connection_recommend_task_name_text);
        textViewTaskDsb = (TextView) findViewById(R.id.tv_description);
        textViewTaskCoin = (TextView) findViewById(R.id.connection_recommend_task_coin_text);
        textViewTaskCheck = (TextView) findViewById(R.id.connection_recommend_task_tv_action);
        System.out.println("textViewTaskCheck" + textViewTaskCheck);
        textViewTaskCheck.setOnClickListener(this);
        viewAllTask = (RelativeLayout) findViewById(R.id.connection_recommend_task_layout);
        viewAllTask.setOnClickListener(this);

        RelativeLayout humorLayout = (RelativeLayout) findViewById(R.id.connection_recommend_humor_layout);
        humorLayout.setOnClickListener(this);
        /**
         * 帮助按钮
         */
        imageView_help=(ImageView)findViewById(R.id.help_info_icon);
        imageView_help.setOnClickListener(this);
    }

    // 检查外网是否通
    private void checkNetwork() {
        String url = "http://115.159.3.16/cb/app_test";
        String ret = Utils.httpGet(url);
        if (null == ret || ret.indexOf("success") == -1) { // 网络不通
            Log.d(TAG, "network not availabel");
            mAuthBtn.setVisibility(View.VISIBLE);

            // 外网不通，当前连接小鸿wifi，自动进行网络认证
            if (mConnectionStatus == ConnectionStatus.HONGWIFI) {
                authentication();
            }

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    checkNetwork();
                }
            }, 5 * 1000);// 5秒检查一次
        } else {
            Log.d(TAG, "network OK！");
            mAuthBtn.setVisibility(View.INVISIBLE);
            if (mConnectionStatus == ConnectionStatus.HONGWIFI) {
                if (mSession.isLogin()) {
                    decCoin(); // reportAuthenSuccess
                }
                mConnectionStatus = ConnectionStatus.HONGWIFI_AUTHED;
            }
        }
    }

    private void decCoin() {
        String url = MarketAPI.API_BASE_URL + "/dec_coin?phone_number="
                + mSession.getUserName();
        String ret = Utils.httpGet(url);
        if (ret != null) {
            try {
                JSONObject obj = new JSONObject(ret);
                if (obj.getInt("ret_code") == 0) {
                    DialogUtils.showMessage(this, "认证成功",
                            "使用金币" + obj.getString("dec_coin_num") + "枚");
                    mSession.setCoinNum(obj.getInt("coin_num"));
                    mSession.notifyCoinUpdated();
                } else if (obj.getInt("ret_code") == 3001) { // 3001 means
                                                             // already
                                                             // deduction coin
                                                             // today
                    Utils.makeEventToast(getApplicationContext(),
                            obj.getString("ret_msg"), false);
                } else if (obj.getInt("ret_code") == 3002
                        && isLeftTimeDialogShown == false) { // 3002 means
                                                             // 使用上网时间包
                    int remainTime = obj.getInt("remain_time");
                    mSession.setRemainTime(remainTime);
                    mSession.setIsCountDown(true);
                    isLeftTimeDialogShown = true;
                    String leftTime = remainTime / (3600 * 24) + "天"
                            + (remainTime % (3600 * 24)) / 3600 + "时"
                            + ((remainTime % 3600) / 60) + "分";
                    DialogUtils.showMessage(this, "认证成功", "上网时间还有" + leftTime);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isHongWifi(String ssid) {
        if (ssid == null) {
            return false;
        }
        String name = ssid.toLowerCase().replace("\"", "");
        if (name.startsWith("hongwifi") || name.startsWith("ruijie")
                || name.endsWith("hongwifi") || (ssid.indexOf("小鸿") != -1)) {
            return true;
        } else {
            return false;
        }
    }

    private void authentication() {
        if (checkCoin()) {
            mAuth.appAuth();
        }
    }

    private void updateWifiStatusUI(ConnectionStatus status) {
        System.out.println("status:" + status);
        switch (status) {
        case DISCONNECTED: {
            mWifiStatusTitle.setText("未连接Wifi");
            mWifiStatusDesc.setText("点击按钮搜索小鸿免费Wifi");
            mAuthBtn.setText("搜索小鸿Wifi");
            mAuthBtn.setVisibility(View.VISIBLE);
            mWifiStatusIcon.getBackground().setAlpha(125);// 50%透明度
            break;
        }
        case CONNECTED: {
            mWifiStatusTitle.setText("已连接Wifi");
            mWifiStatusDesc.setText("已连接到 " + mCurrentSSID);
            if (getHongWifi() != null) {
                mAuthBtn.setText("切换到小鸿Wifi");
                mAuthBtn.setVisibility(View.VISIBLE);
            } else {
                mAuthBtn.setVisibility(View.INVISIBLE);
            }
            mWifiStatusIcon.getBackground().setAlpha(255); // 100%透明度
            break;
        }
        case HONGWIFI: {
            mWifiStatusTitle.setText("已连接Wifi");
            mWifiStatusDesc.setText("已连接到小鸿Wifi:" + mCurrentSSID);
            mAuthBtn.setText("认证上网");
            mAuthBtn.setVisibility(View.VISIBLE);
            mWifiStatusIcon.getBackground().setAlpha(255); // 100%透明度
            break;
        }
        case HONGWIFI_AUTHED: {
            mWifiStatusTitle.setText("已认证上网");
            mWifiStatusDesc.setText("已连接到小鸿Wifi：" + mCurrentSSID);
            mAuthBtn.setVisibility(View.INVISIBLE);
            mWifiStatusIcon.getBackground().setAlpha(255);
            break;
        }
        default:
            mWifiStatusDesc.setText("正在搜索WIFI...");
            break;
        }
    }

    private boolean checkCoin() {
        if (mSession.isLogin() == false) {
            if (!autoLogin()) {
                Intent intent = new Intent(getApplicationContext(),
                        RegisterActivity.class);
                startActivity(intent);
                return false;
            }
        }
        String url = MarketAPI.API_BASE_URL + "/query_coin?phone_number="
                + mSession.getUserName();
        String ret = Utils.httpGet(url);
        if (ret != null) {
            try {
                JSONObject obj = new JSONObject(ret);
                // 3001 means already deduction coin today
                if (obj.getInt("ret_code") == 0
                        || obj.getInt("ret_code") == 3001) {
                    return true;
                } else {
                    // 金币不足，提示购买
                    showBuyCoinDialog(obj.getString("ret_msg"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void showBuyCoinDialog(String msg) {
        CustomDialog dialog = new CustomDialog.Builder(this)
                .setTitle("金币不足")
                .setMessage(msg)
                .setNegativeButton("暂不购买",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
                                dialog.dismiss();
                            }
                        })
                .setPositiveButton("购买上网时间",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
                                Utils.gotoBuyingEntryPage(ConnectionActivity.this);
                                dialog.dismiss();
                            }
                        }).create();
        dialog.show();
    }

    private void registerConnection() {
        if (mConnectionReceiver == null) {
            mConnectionReceiver = new MyBroadcastReceiver();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mConnectionReceiver, intentFilter);
    }

    private void checkLogin() {
        if (mSession.isLogin() == false && TextUtils.isEmpty(mSession.getUserName()) == false
                && TextUtils.isEmpty(mSession.getPassword()) == false) {
            mSession.login();
        } else if (mSession.isLogin() == false) {
            Intent intent = new Intent(getApplicationContext(),
                    RegisterActivity.class);
            startActivity(intent);
        }
    }

    private void checkWifiConnection() {
        ConnectivityManager nw = (ConnectivityManager) this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = nw.getActiveNetworkInfo();
        if (netinfo != null && netinfo.isAvailable()) {
            if (!mSession.isLogin()) {
                checkLogin();
            }
        }

        ConnectivityManager conMan = (ConnectivityManager) this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (State.CONNECTED != conMan.getNetworkInfo(
                ConnectivityManager.TYPE_WIFI).getState()) {
            Log.i(TAG, "unconnect");
            wifiStatusChanged(null);
        } else {
            Log.i(TAG, "connected");
            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();
            wifiStatusChanged(ssid);
        }
    }

    private void wifiStatusChanged(String ssid) {
        mCurrentSSID = ssid;
        if (ssid != null) {
            if (isHongWifi(ssid)) {
                mConnectionStatus = ConnectionStatus.HONGWIFI;
            } else {
                mConnectionStatus = ConnectionStatus.CONNECTED;
            }
            checkNetwork();
        } else {
            mConnectionStatus = ConnectionStatus.DISCONNECTED;
        }
        updateWifiStatusUI(mConnectionStatus);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(int method, Object obj) {
        switch (method) {

        case MarketAPI.ACTION_GET_MESSAGES: {
            Log.d(TAG, "ACTION_GET_MESSAGES");
            MessageListBean messages = (MessageListBean) obj;
            if (messages != null && messages.getMessageList() != null
                    && messages.getMessageList().size() > 0) {
                mSession.setMessages(messages);
                String Message_value = "";
                for (MessageBean s : mSession.getMessages().getMessageList()) {
                    if (!Message_value.equals("")) {
                        Message_value += "                    ";
                    }
                    Message_value += s.getMessageText() + "          ";
                }
                Log.d(TAG, "ACTION_GET_MESSAGES Message_value = "
                        + Message_value);
                mAutoScroolView.setMessageBeans(mSession.getMessages()
                        .getMessageList());
                mAutoScroolView.setTexts(Message_value);
                mAutoScroolView.init(getWindowManager());
                mAutoScroolView.startScroll();
            }
            break;
        }
        case MarketAPI.ACTION_SIGN_IN: {
            HashMap<String, Object> result = (HashMap<String, Object>) obj;
            Integer ret_code = (Integer) result.get("ret_code");
            if (ret_code == 0) {
                mSession.setCoinNum((Integer) result
                        .get(Constants.KEY_COIN_NUM));
                mSession.setSignInToday(true);
                textView_coin_num.setText(mSession.getCoinNum().toString());
                textView_signIn_status.setText("今天已签到");
                CustomDialog dialog = new CustomDialog.Builder(this)
                        .setTitle(getString(R.string.sign_in_success))
                        .setMessage(
                                "本次签到获得了"
                                        + result.get(Constants.KEY_ADD_COIN_NUM)
                                        + "个金币")
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                            int which) {
                                        dialog.dismiss();
                                    }
                                }).create();
                dialog.show();
            } else if (ret_code == 3002) { // 今天已经签到领取过了
                mSession.setSignInToday(true);
                Log.d(TAG, "Already sign in today, ret_code 3002");
                DialogUtils.showMessage(this,
                        getString(R.string.sign_in_success),
                        "今天已经签过到了，明天记得再来签到哦");
            } else {
                DialogUtils.showMessage(this, getString(R.string.sign_in_fail),
                        result.get("ret_msg").toString());
            }
            break;
        }
        case MarketAPI.ACTION_GET_TASK_LIST: {
            TaskListBean result = (TaskListBean) obj;
            if (result.getTasklist() != null) {
                Log.d(TAG, "size = " + result.getTasklist().size());
                TaskBean bean = new TaskBean();
                bean = result.getTasklist().get(0);
                taskBean = bean;
                updateTaskLayout(bean);
            } else {
                Log.d(TAG, "no data from server");
            }
            break;
        }
        default:
            break;
        }
    }

    @Override
    public void onError(int method, int statusCode) {
        switch (method) {
        case MarketAPI.ACTION_LOGIN: {
            if (mLoginRetryCount++ < 5) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        checkLogin();
                    }
                }, 5 * 1000);
            } else {
                mLoginRetryCount = 0;
            }
            break;
        }
        default:
            break;
        }
    }

    private void showAppData(List<Map<String, Object>> data_list) {
        connectionAppGridAdapter = new ConnectionAppGridAdapter(this, data_list);
        mGridView.setAdapter(connectionAppGridAdapter);
        mGridView.setOnItemClickListener(new ConnectAppOnItemClick());
        mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
    }

    private ScanResult getHongWifi() {
        if (mWifiAdmin == null || mWifiAdmin.getWifiList() == null) {
            return null;
        }
        for (ScanResult scanResult : mWifiAdmin.getWifiList()) {
            if (isHongWifi(scanResult.SSID)) {
                String encryptType = mWifiAdmin
                        .wifiEncryptType(scanResult.capabilities);
                if (encryptType.length() > 0) { // only connect to no password wifi
                    continue;
                }
                return scanResult;
            }
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.auth_button: {
            switch (mConnectionStatus) {
            case DISCONNECTED: {
                Boolean ret = mWifiAdmin.openWifi();
                Log.d(TAG, "open wifi result:" + ret);
                break;
            }
            case CONNECTED: {
                ScanResult scanResult = getHongWifi();
                if (scanResult != null) {
                    mWifiAdmin
                            .connectWifi(scanResult.SSID, "", mWifiAdmin
                                    .wifiEncryptType(scanResult.capabilities));
                    Log.d(TAG, "Trying to connect " + scanResult.SSID);
                }
                break;
            }
            case HONGWIFI:
                authentication();
                break;
            case HONGWIFI_AUTHED:
                break;           
            default:
                break;
            }
            break;
        }
        // 查看全部推荐应用
        case R.id.connection_recommend_all_app_text:
            MySharedpreference mySharedpreference = new MySharedpreference
            (this);
            mySharedpreference.saveRankTabActivity_FLAG(Constants.BROADCAST_CATEGORY_RCMD);
            Intent gameIntent = new Intent();
            gameIntent.setAction(Constants.BROADCAST_CATEGORY_RCMD);
            sendBroadcast(gameIntent);
            break;
        // 查看全部推荐任务
        case R.id.connection_recommend_task_layout:
            MySharedpreference mySharedpreference1 = new MySharedpreference
            (this);
            mySharedpreference1.saveRankTabActivity_FLAG(Constants.BROADCAST_CATEGORY_TASK);
            
            Intent growIntent = new Intent();
            growIntent.setAction(Constants.BROADCAST_CATEGORY_TASK);
            sendBroadcast(growIntent);
            break;
        // 推荐任务点击事件
        case R.id.connection_recommend_task_tv_action:
            if (taskBean != null) {
                String clickUrl = taskBean.getClick_url();
                if (clickUrl != null && !clickUrl.equals("")) {
                    Intent detailIntent = new Intent(getApplicationContext(),
                            WebviewActivity.class);
                    detailIntent.putExtra("extra.url", clickUrl);
                    detailIntent.putExtra("extra.title", taskBean.getName());
                    startActivity(detailIntent);
                } else {
                    Log.w(TAG, "no click url");
                    Intent intent = new Intent(getApplicationContext(),
                            GzhTaskDetailActivity.class);
                    intent.putExtra(Constants.EXTRA_TASK_BEAN, taskBean);
                    startActivity(intent);
                }
            }
            break;
        // 签到点击事件
        case R.id.sign_in_layout:
            if (!mSession.isLogin()) {
                if (!autoLogin()) {
                    Intent intent_next = new Intent(getApplicationContext(),
                            RegisterActivity.class);
                    startActivityForResult(intent_next, REQUEST_CODE);
                }
            } else {
                MarketAPI.signIn(getApplicationContext(), this);
            }
            break;
        // 购买金币点击事件
        case R.id.buy_layout:
            Utils.gotoBuyingEntryPage(ConnectionActivity.this);
            break;
        // 搞笑幽默点击事件
        case R.id.connection_recommend_humor_layout:
            Intent detailIntent = new Intent(getApplicationContext(),
                    WebviewActivity.class);
            detailIntent.putExtra("extra.url", "http://xhaz.come11.com");
            detailIntent.putExtra("extra.title", "搞笑幽默");
            startActivity(detailIntent);
            break;
        // 广播消息点击事件
        case R.id.connection_current_activity_info_text:
            if(mAutoScroolView == null) {
                Log.w(TAG, "mAutoScroolView is null");
                break;
            }
            int positon = mAutoScroolView.getPosition();
            System.out.println("getTextWidth_First()" + positon);
            ArrayList<MessageBean> messageBeanList = mAutoScroolView.getMessageBeans();
            if(messageBeanList == null) {
                Log.w(TAG, "messageBeanList is null");
                break;
            }
            MessageBean messageBean = messageBeanList.get(positon);
            if(messageBean == null) {
                Log.w(TAG, "messageBean is null");
                break;
            }
            String url = messageBean.getClickUrl();
            String Message = messageBean.getMessageText();
            if (url != null) {
                Intent MessageIntent = new Intent(getApplicationContext(),
                        WebviewActivity.class);
                MessageIntent.putExtra("extra.url", url);
                MessageIntent.putExtra("extra.title", Message);
                startActivity(MessageIntent);
            }
            break;
        //帮助页面点击事件
        case R.id.help_info_icon:
            Intent intent=new Intent(ConnectionActivity.this, 
                    GuideActivity.class);
            startActivity(intent);
            break;
        default:
            break;
        }
    }

    private boolean autoLogin() {
        return mSession.login();
    }

    /**
     * 更新推荐任务界面
     */
    public void updateTaskLayout(TaskBean bean) {
        TaskImgUrl = bean.getLogo_url();
        TaskName = bean.getName();
        TaskDsb = bean.getDesc();
        TaskCoin = "+" + String.valueOf(bean.getCoin_num());

        if (TaskImgUrl != null) {
            ImageLoader.getInstance()
                    .displayImage(TaskImgUrl, imageViewTaskImg);
        } else {
            ImageLoader.getInstance().displayImage(
                    "drawable://" + R.drawable.wechat, imageViewTaskImg,
                    Utils.sDisplayImageOptions);
        }
        ImageLoader.getInstance().displayImage(TaskImgUrl, imageViewTaskImg);
        textViewTaskName.setText(TaskName);
        textViewTaskDsb.setText(TaskDsb);
        textViewTaskCoin.setText(TaskCoin);
    }

    class ConnectAppOnItemClick implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
                long arg3) {
            Intent intent = new Intent(getApplicationContext(),
                    OfferWallAdDetailActivity.class);
            AppSummaryObject obj = (AppSummaryObject)((HashMap<String, Object>)connectionAppGridAdapter.getItem(pos)).get("AppSummaryObject");
            intent.putExtra("ad", obj);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {

//        onLoginStatusChanged();

        if (mAutoScroolView.getMessageBeans() == null
                && mSession.getMessages() != null) {
            String Message_value = "";
            for (MessageBean s : mSession.getMessages().getMessageList()) {
                if (!Message_value.equals("")) {
                    Message_value += "                    ";
                }
                Message_value += s.getMessageText() + "          ";
            }
            Log.d(TAG, "ACTION_GET_MESSAGES Message_value = " + Message_value);
            mAutoScroolView.setMessageBeans(mSession.getMessages()
                    .getMessageList());
            mAutoScroolView.setTexts(Message_value);
            mAutoScroolView.init(getWindowManager());
            mAutoScroolView.startScroll();
        }

        super.onResume();
    }

    private class UpdateLeftTimeThread extends Thread {
        @Override
        public void run() {
            while (true) {
                mSession.updateLeftTime(mSession.getLeftTime());
                try {
                    Thread.sleep(60 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mSession.getLeftTime().decOneMinutes();
            }
        }
    }

    @Override
    public void onLoadAppSumDataFailed() {
        Log.d(TAG, "onLoadAppSumDataFailed");
    }

    @Override
    public void onLoadAppSumDataFailedWithErrorCode(int code) {
        Log.d(TAG, "onLoadAppSumDataFailedWithErrorCode");
    }

    @Override
    public void onLoadAppSumDataSuccess(Context arg0,
            final AppSummaryObjectList adList) {

        Log.d(TAG, "onLoadAppSumDataSuccess");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdList = adList;
                int size = 0;
                if(mAdList != null) {
                    size = mAdList.size();
                }
                List<Map<String, Object>> data_list = new ArrayList<Map<String, Object>>();
                for (int i = 0; i < size; i++) {
                    if(mAdList == null) {
                        Log.w(TAG, "So strange mAdList is null");
                        break;
                    }
                    AppSummaryObject appObj = mAdList.get(i);
                    if (appObj.getAdTaskStatus() == AdTaskStatus.ALREADY_COMPLETE
                            || appObj.getAdForm() == AdForm.GO2WEB) {
                        continue;
                    }
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("logo_url", appObj.getIconUrl());
                    map.put("name", appObj.getAppName());
                    map.put("GiveCoin", "+" + Utils.getTotalPoints(appObj));
                    map.put("AppSummaryObject", appObj);
                    data_list.add(map);
                }
                // 只显示金币数最多的3个app
                Comparator comp = new Comparator() {  
                    public int compare(Object o1, Object o2) {  
                        Map<String, Object> p1 = (Map<String, Object>) o1;
                        Map<String, Object> p2 = (Map<String, Object>) o2;
                        String coinStr1 = (String) p1.get("GiveCoin");
                        String coinStr2 = (String) p2.get("GiveCoin");
                        //Log.d(TAG, "coinStr1 = " + coinStr1);
                        //Log.d(TAG, "coinStr2 = " + coinStr2);
                        int p1num = 0;
                        try {
                            p1num = Integer.parseInt(coinStr1);
                        }catch(NumberFormatException e) {
                            //Log.w(TAG, "NumberFormatException1:" + e.getMessage());
                            if(coinStr1.startsWith("+")) {
                                p1num = Integer.parseInt(coinStr1.substring(1));
                            }else if(coinStr1.startsWith("-")) {
                                p1num = -Integer.parseInt(coinStr1.substring(1));
                            }
                        }
                        int p2num = 0;
                        try {
                            p2num = Integer.parseInt(coinStr2);
                        }catch(NumberFormatException e) {
                            //Log.w(TAG, "NumberFormatException2:" + e.getMessage());
                            if(coinStr2.startsWith("+")) {
                                p2num = Integer.parseInt(coinStr2.substring(1));
                            }else if(coinStr2.startsWith("-")) {
                                p2num = -Integer.parseInt(coinStr2.substring(1));
                            }
                        }
                        if (p1num < p2num)
                            return 1;  
                        else if (p1num == p2num)  
                            return 0;
                        else if (p1num > p2num)  
                            return -1;  
                        return 0;  
                    }
                };
                Collections.sort(data_list, comp);
                for (int i = data_list.size(); i > 3; i--) {
                    data_list.remove(i-1);
                }
                showAppData(data_list);
            }
        });
    }

    @Override
    public void onLoginStatusChanged() {
        if(mSession == null) {
            Log.w(TAG, "onLoginStatusChanged mSession is null");
            return;
        }
        if (!mSession.isLogin()) {
            textView_signIn_status.setText(R.string.person_account_sign_in_value);
            textView_coin_num.setText("0");
        } else {
            textView_coin_num.setText(mSession.getCoinNum().toString());
            if (mSession.getSignInToday()) {
                textView_signIn_status.setText("今天已签到");
            } else {
                textView_signIn_status.setText(R.string.person_account_sign_in_value);
            }

            Log.d(TAG, "mAdList = " + mAdList);
            Log.d(TAG, "Utils.getPredloadedYoumiData() = " + Utils.getPredloadedYoumiData());
            if (mAdList == null) {
                if (Utils.getPredloadedYoumiData() == null) {
                    Utils.doPreloadApp(getApplicationContext(), this);
                }else {
                    mAdList = Utils.getPredloadedYoumiData();
                }
            }
            if (taskBean == null) {
                if (Utils.getPreloadedTaskList() == null || Utils.getPreloadedTaskList().size() == 0) {
                    Utils.doPreloadTask(getApplicationContext(), this);
                } else {
                    ArrayList<TaskBean> taskList = Utils.getPreloadedTaskList();
                    taskBean = taskList.get(0);
                    updateTaskLayout(taskBean);
                }
            }
            if (mSession.getMessages() == null) {
                MarketAPI.getMessages(getApplicationContext(), this);
            }
            if (mSession.getIsCountdown() && mCountDownThread == null) {
                mCountDownThread = new UpdateLeftTimeThread();
                mCountDownThread.start();
            }
        }
    }

    @Override
    public void onLoginFailed(int retCode, String retMsg) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onCoinUpdate(int newTotalCoinNum) {
        textView_coin_num.setText(newTotalCoinNum + "");
    }
}
