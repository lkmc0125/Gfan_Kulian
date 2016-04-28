package com.xiaohong.kulian.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Session;
import com.xiaohong.kulian.adapter.ConnectionAppGridAdapter;
import com.xiaohong.kulian.bean.AppBean;
import com.xiaohong.kulian.bean.AppListBean;
import com.xiaohong.kulian.bean.LoginResultBean;
import com.xiaohong.kulian.bean.MessageBean;
import com.xiaohong.kulian.bean.MessageListBean;
import com.xiaohong.kulian.bean.TaskBean;
import com.xiaohong.kulian.bean.TaskListBean;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.util.DialogUtils;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.util.WifiAdmin;
import com.xiaohong.kulian.common.util.WifiAuthentication;
import com.xiaohong.kulian.common.widget.AutoScrollTextViewH;
import com.xiaohong.kulian.common.widget.BaseActivity;
import com.xiaohong.kulian.common.widget.CustomDialog;
import com.xiaohong.kulian.common.widget.MarqueeTextView;
import com.xiaohong.kulian.common.widget.RoundImageView;

public class ConnectionActivity extends BaseActivity implements ApiRequestListener, OnClickListener {
    private static final String TAG = "ConnectionActivity"; 
   
    private WifiAuthentication mAuth;
    private WifiAdmin mWifiAdmin;
    private Integer mLoginRetryCount = 0;
    private String mCurrentSSID;
    private MyBroadcastReceiver mConnectionReceiver;
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
    private ArrayList<AppBean> appBeans;
    /**
     * 任务界面
     */
    private RoundImageView imageViewTaskImg;
    private TextView textViewTaskName;
    private TextView textViewTaskDsb;
    private TextView textViewTaskCoin;
    private TextView textViewTaskCheck;
    private String TaskImgUrl,TaskName,TaskDsb,TaskCoin;
    private TextView textViewAllTask;
    private TaskBean taskBean;
    private class MyBroadcastReceiver extends BroadcastReceiver {  
        @Override  
        public void onReceive(Context context, Intent intent) {  
            Log.v(TAG, "onReceive");
            checkWifiConnection();
        }  
    }
    private enum ConnectionStatus {
        DISCONNECTED,
        CONNECTED,
        HONGWIFI,
        HONGWIFI_AUTHED
    };
    private ConnectionStatus mConnectionStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_main_layout);
        initView();

        mWifiAdmin = new WifiAdmin(getApplicationContext());
        mAuth = new WifiAuthentication();
        mConnectionStatus = ConnectionStatus.DISCONNECTED;

        registerConnection();

        queryAppList();

        new Handler().postDelayed(new Runnable() {  
            public void run() {
                boolean open = mWifiAdmin.openWifi();
                Log.i(TAG, "wifi open:" + open);
                mWifiAdmin.startScan();
            }
        }, 500);
    }

    private void initView() {
        /**
         * 连接界面
         */
        mAuthBtn=(Button)findViewById(R.id.auth_button);
        mAuthBtn.setOnClickListener(this);
        mWifiStatusTitle=(TextView)findViewById(R.id.wifi_status_title);
        mWifiStatusDesc=(TextView)findViewById(R.id.wifi_status_desc);
        mWifiStatusIcon=(ImageView)findViewById(R.id.wifi_status_icon);
        mAutoScroolView=(AutoScrollTextViewH)findViewById(R.id.connection_current_activity_info_text);
        mAutoScroolView.setOnClickListener(this);
        /**
         * 签到界面
         */
        layoutSignIn=(RelativeLayout)findViewById(R.id.sign_in_layout);
        layoutSignIn.setOnClickListener(this);
        layoutBuyCoin=(RelativeLayout)findViewById(R.id.buy_layout);
        layoutBuyCoin.setOnClickListener(this);
        textView_coin_num=(TextView)findViewById(R.id.coin_text);
        textView_coin_num.setText("0");
        textView_signIn_status=(TextView)findViewById(R.id.sign_in_value_text);
        /**
         * 推荐应用界面
         */
        mGridView = (GridView)findViewById(R.id.connect_recommend_app_gridView_layout);
        textViewAllApp=(TextView)findViewById(R.id.connection_recommend_all_app_text);
        textViewAllApp.setOnClickListener(this);
        /**
         * 任务界面
         */
        imageViewTaskImg=(RoundImageView)findViewById(R.id.iconnection_recommend_task_logo);
        textViewTaskName=(TextView)findViewById(R.id.connection_recommend_task_name_text);
        textViewTaskDsb=(TextView)findViewById(R.id.tv_description);
        textViewTaskCoin=(TextView)findViewById(R.id.connection_recommend_task_coin_text);
        textViewTaskCheck=(TextView)findViewById(R.id.tv_action);
        textViewTaskCheck.setOnClickListener(this);
        textViewAllTask=(TextView)findViewById(R.id.connection_recommend_all_task_text);
        textViewAllTask.setOnClickListener(this);
        
        RelativeLayout humorLayout = (RelativeLayout)findViewById(R.id.connection_recommend_humor_layout);
        humorLayout.setOnClickListener(this);
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
                    decCoin();  // reportAuthenSuccess
                }
                mConnectionStatus = ConnectionStatus.HONGWIFI_AUTHED;
            }
        }
    }

    private void decCoin() {
        String url = MarketAPI.API_BASE_URL + "/dec_coin?phone_number="+mSession.getUserName();
        String ret = Utils.httpGet(url);
        if (ret != null) {
            try {
                JSONObject obj = new JSONObject(ret);
                if (obj.getInt("ret_code") == 0) {
                    DialogUtils.showMessage(this, "认证成功", "使用金币" + obj.getString("dec_coin_num") + "枚");
                    mSession.setCoinNum(obj.getInt("dec_coin_num"));
                    mSession.notifyCoinUpdated();
                } else if (obj.getInt("ret_code") == 3001) { // 3001 means already deduction coin today
                    Utils.makeEventToast(getApplicationContext(), obj.getString("ret_msg"), false);
                } else if (obj.getInt("ret_code") == 3002) { // 3002 means 使用上网时间包
                    int remainTime = obj.getInt("remain_time");
                    mSession.setRemainTime(remainTime);
                    mSession.setIsCountDown(true);
                    String leftTime = remainTime/(3600*24)+"天"+(remainTime%(3600*24))/3600+"时"+((remainTime%3600)/60)+"分";
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
        if (name.startsWith("hongwifi") || name.startsWith("ruijie") || name.endsWith("hongwifi")
                || (ssid.indexOf("小鸿") != -1)) {
            return true;
        } else {
            return false;
        }
    }

    private void authentication () {
        if (checkCoin()) {
            mAuth.appAuth();
        }
    }

    private void updateWifiStatusUI(ConnectionStatus status) {
        System.out.println("status:"+status);
        switch (status) {
        case DISCONNECTED:
        {
            mWifiStatusTitle.setText("未连接Wifi");
            mWifiStatusDesc.setText("点击按钮搜索小鸿免费Wifi");
            mAuthBtn.setText("搜索小鸿Wifi");
            mAuthBtn.setVisibility(View.VISIBLE);
            mWifiStatusIcon.getBackground().setAlpha(125);// 50%透明度
            break;
        }
        case CONNECTED:
        {
            mWifiStatusTitle.setText("已连接Wifi");
            mWifiStatusDesc.setText("已连接到 "+mCurrentSSID);
            if (getHongWifi() != null) {
                mAuthBtn.setText("切换到小鸿Wifi");
                mAuthBtn.setVisibility(View.VISIBLE);
            } else {
                mAuthBtn.setVisibility(View.INVISIBLE);
            }
            mWifiStatusIcon.getBackground().setAlpha(255); // 100%透明度
            break;
        }
        case HONGWIFI:
        {
            mWifiStatusTitle.setText("已连接Wifi");
            mWifiStatusDesc.setText("已连接到小鸿Wifi:"+mCurrentSSID);
            mAuthBtn.setText("认证上网");
            mAuthBtn.setVisibility(View.VISIBLE);
            mWifiStatusIcon.getBackground().setAlpha(255); // 100%透明度
            break;
        }
        case HONGWIFI_AUTHED:
        {
            mWifiStatusTitle.setText("已认证上网");
            mWifiStatusDesc.setText("已连接到小鸿Wifi："+mCurrentSSID);
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
            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(intent);
            return false;
        }
        String url = MarketAPI.API_BASE_URL + "/query_coin?phone_number="+mSession.getUserName();
        String ret = Utils.httpGet(url);
        if (ret != null) {
            try {
                JSONObject obj = new JSONObject(ret);
                // 3001 means already deduction coin today
                if (obj.getInt("ret_code") == 0 || obj.getInt("ret_code") == 3001) {
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
        .setNegativeButton("暂不购买", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        })
        .setPositiveButton("购买金币", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
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
        if (mSession.getUserName().length() > 0 && mSession.getPassword().length() > 0) {
            MarketAPI.login(getApplicationContext(), this, mSession.getUserName(), mSession.getPassword());
        } else if (mSession.isLogin() == false) {
            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(intent);
        }
    }

    private void checkWifiConnection() {
        ConnectivityManager nw = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = nw.getActiveNetworkInfo();
        if (netinfo != null && netinfo.isAvailable()) {
            if (!mSession.isLogin()) {
                checkLogin();
            }
        }

        ConnectivityManager conMan = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (State.CONNECTED != conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState()) {
            Log.i(TAG, "unconnect");
            wifiStatusChanged(null);
        } else {
            Log.i(TAG, "connected");
            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();
            Log.d("SSID", ssid);
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

    private void queryAppList() {
        MarketAPI.getAppList(getApplicationContext(), this, 1, Constants.CATEGORY_RCMD);
        MarketAPI.getTaskList(getApplicationContext(), this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(int method, Object obj) {
        switch (method) {
        case MarketAPI.ACTION_LOGIN:
        {
            Log.d(TAG, "login success");
            Utils.doPreloadAppAndTask(getApplicationContext());
            LoginResultBean result = (LoginResultBean) obj;
            if (result.getRetCode() == 0) {
                mSession.setLogin(true);
                mSession.setCoinNum(result.getCoinNum());
                mSession.setToken(result.getToken());
                textView_coin_num.setText(String.valueOf(result.getCoinNum()));
                mSession.setSignInToday(result.getIsSign());
                mSession.setIsCountDown(result.getShowCountdown());
                mSession.setRemainTime(result.getRemainTime());
                mSession.setRemainTime(60*60*24);  // for test
                if (mSession.getSignInToday()) {
                    textView_signIn_status.setText("今天已签到");                    
                }
                if (mSession.getMessages() == null) {
                    MarketAPI.getMessages(getApplicationContext(), this);
                }
                if (appBeans == null) {
                    queryAppList();
                }
            }
            break;
        }
        case MarketAPI.ACTION_GET_MESSAGES:
        {
            Log.d(TAG, "ACTION_GET_MESSAGES");
            MessageListBean messages = (MessageListBean)obj;
            if (messages!= null && messages.getMessageList() != null
                    && messages.getMessageList().size() > 0) {
                mSession.setMessages(messages);  
                String Message_value="";
                for(MessageBean s:mSession.getMessages().getMessageList()){
                    if(!Message_value.equals("")){
                        Message_value += "                    ";
                    }
                    Message_value += s.getMessageText()+"          ";
                }
                Log.d(TAG, "ACTION_GET_MESSAGES Message_value = " + Message_value);
                mAutoScroolView.setMessageBeans(mSession.getMessages().getMessageList());
                mAutoScroolView.setTexts(Message_value);
                mAutoScroolView.init(getWindowManager());
                mAutoScroolView.startScroll();
            }
            break;
        }
        case MarketAPI.ACTION_SIGN_IN:
        {
            HashMap<String, Object> result = (HashMap<String, Object>) obj;
            Integer ret_code = (Integer)result.get("ret_code");
            if (ret_code == 0) {
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
            } else if (ret_code == 3002) { // 今天已经签到领取过了
                textView_signIn_status.setText("今天已签到");
                Log.d(TAG, "Already sign in today, ret_code 3002");
                DialogUtils.showMessage(this, getString(R.string.sign_in_success), "今天已经领取过金币了，明天再来哦");
            } else {
                DialogUtils.showMessage(this, getString(R.string.sign_in_fail), result.get("ret_msg").toString());
            }
            break;
        }
        case MarketAPI.ACTION_GET_APP_LIST:
        {
            AppListBean appList = (AppListBean) obj;
            ArrayList<AppBean> list = appList.getApplist();
            appBeans = new ArrayList<AppBean>();
            if (list.size() > 0) {
                List<Map<String, Object>> data_list = new ArrayList<Map<String, Object>>();
                for (AppBean bean : list) {
                    if (Utils.isApkInstalled(getApplicationContext(),
                            bean.getPackageName()) ==  true) {
                        continue;
                    }
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("logo_url", bean.getAppLogo());
                    System.out.println("bean.getAppLogo()"+bean.getAppLogo());
                    map.put("name", bean.getAppName());
                    map.put("GiveCoin", "+"+bean.getGiveCoin());
                    data_list.add(map);
                    appBeans.add(bean);
                    if (data_list.size() >= 3) {
                        break;
                    }
                }
                showAppData(data_list);
            }
            break;
        }
        case MarketAPI.ACTION_GET_TASK_LIST:
            TaskListBean result = (TaskListBean) obj;
            if(result.getTasklist() != null) {
                Log.d(TAG, "size = " + result.getTasklist().size());
                TaskBean bean = new TaskBean();
                bean       = result.getTasklist().get(0);
                taskBean   = bean;
                TaskImgUrl = bean.getLogo_url();
                System.out.println("TaskImgUrl"+TaskImgUrl);
                TaskName   = bean.getName();
                TaskDsb    = bean.getDesc();
                TaskCoin   = "+" + String.valueOf(bean.getCoin_num());
                updateTaskLayout();
            } else {
                Log.d(TAG, "no data from server");
            }
            break;
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

    @Override
    public void onError(int method, int statusCode) {
        switch (method) {
        case MarketAPI.ACTION_LOGIN:
        {
            if (mLoginRetryCount++ < 5) {
                new Handler().postDelayed(new Runnable() {   
                    public void run() {
                        checkLogin();
                    }
                 }, 5*1000);
            } else {
                mLoginRetryCount = 0;
            }
            break;
        }
        case MarketAPI.ACTION_GET_APP_LIST:
            break;
        default:
            break;
        }
    }

    private ScanResult getHongWifi() {
        if (mWifiAdmin == null || mWifiAdmin.getWifiList() == null) {
            return null;
        }
        for (ScanResult scanResult : mWifiAdmin.getWifiList()) {
            if (isHongWifi(scanResult.SSID)) {
                String encryptType = mWifiAdmin.wifiEncryptType(scanResult.capabilities); 
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
        case R.id.auth_button:
        {
            switch (mConnectionStatus) {
            case DISCONNECTED:
            {
                Boolean ret = mWifiAdmin.openWifi();
                Log.d(TAG, "open wifi result:"+ret);
                break;
            }
            case CONNECTED:
            {
                ScanResult scanResult = getHongWifi();
                if (scanResult != null) {
                    mWifiAdmin.connectWifi(scanResult.SSID, "", mWifiAdmin.wifiEncryptType(scanResult.capabilities));
                    Log.d(TAG, "Trying to connect "+scanResult.SSID);
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
        //推荐应用详情点击事件
        case R.id.connection_recommend_all_app_text:
            Intent gameIntent = new Intent();
            gameIntent.setAction(Constants.BROADCAST_CATEGORY_RCMD);
            sendBroadcast(gameIntent);
            break;
        //推荐任务详情点击事件
        case R.id.connection_recommend_all_task_text:
            Intent growIntent = new Intent();
            growIntent.setAction(Constants.BROADCAST_CATEGORY_TASK);
            sendBroadcast(growIntent);
            break;
        //查看点击事件
        case R.id.tv_action:
            String clickUrl = taskBean.getClick_url();
            if (clickUrl != null && !clickUrl.equals("")) {
                Intent detailIntent = new Intent(getApplicationContext(), WebviewActivity.class);
                detailIntent.putExtra("extra.url", clickUrl);
                detailIntent.putExtra("extra.title", taskBean.getName());
                startActivity(detailIntent);
            } else {
                Log.w(TAG, "no click url");
                Intent intent = new Intent(getApplicationContext(), GzhTaskDetailActivity.class);
                intent.putExtra(Constants.EXTRA_TASK_BEAN, taskBean);
                startActivity(intent);
            }
            break;
        //签到点击事件
        case R.id.sign_in_layout:
            if (!mSession.isLogin()) {
                if (!autoLogin()) {
                    Intent intent_next = new Intent(getApplicationContext(), RegisterActivity.class);
                    startActivityForResult(intent_next, REQUEST_CODE);
                }
            } else {
                MarketAPI.signIn(getApplicationContext(), this);
            }
            break;
        //购买金币点击事件
        case R.id.buy_layout:
            Utils.gotoBuyingEntryPage(ConnectionActivity.this);
            break;
        //搞笑幽默点击事件
        case R.id.connection_recommend_humor_layout:
            Intent detailIntent = new Intent(getApplicationContext(), WebviewActivity.class);
            detailIntent.putExtra("extra.url", "http://xhaz.come11.com");
            detailIntent.putExtra("extra.title", "搞笑幽默");
            startActivity(detailIntent);
            break;
        case R.id.connection_current_activity_info_text:
            int positon=mAutoScroolView.getPosition();
            System.out.println("getTextWidth_First()"+positon);
            String url=mAutoScroolView.getMessageBeans().get(positon).getClickUrl();
            String Message=mAutoScroolView.getMessageBeans().get(positon).getMessageText();
            if(url!=null){
                Intent MessageIntent = new Intent(getApplicationContext(), WebviewActivity.class);
                MessageIntent.putExtra("extra.url", url);
                MessageIntent.putExtra("extra.title", Message);
                startActivity(MessageIntent);
            }
            break;
        default:
            break;
        }
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
    /**
     * 更新推荐任务界面
     */
    public void updateTaskLayout(){
        ImageLoader.getInstance().displayImage(TaskImgUrl, imageViewTaskImg);
        textViewTaskName.setText(TaskName);
        textViewTaskDsb.setText(TaskDsb);
        textViewTaskCoin.setText(TaskCoin);
    }
    
    class ConnectAppOnItemClick implements OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3)
        {
            AppBean item = (AppBean) appBeans.get(pos);
            String pid = item.getAppId() + "";
            Intent detailIntent = new Intent(getApplicationContext(),
                    AppDetailActivity.class);
            detailIntent.putExtra(Constants.EXTRA_PRODUCT_ID, pid);
            detailIntent.putExtra(Constants.EXTRA_CATEGORY, Constants.CATEGORY_RCMD);
            detailIntent.putExtra(Constants.EXTRA_COIN_NUM, item.getGiveCoin());
            startActivity(detailIntent);
        }
    }
    @Override
    protected void onResume() {
        if (!mSession.isLogin()) {
            textView_signIn_status.setText(R.string.person_account_sign_in_value);
        } else if (mSession.isLogin()) {
            textView_coin_num.setText(mSession.getCoinNum().toString());
            if (mSession.getSignInToday()) {
                textView_signIn_status.setText("今天已签到");
            } else {
                textView_signIn_status.setText(R.string.person_account_sign_in_value);
            }
        }

        if (mAutoScroolView.getMessageBeans() == null && mSession.getMessages() != null) {
            String Message_value="";
            for (MessageBean s:mSession.getMessages().getMessageList()){
                if(!Message_value.equals("")){
                    Message_value += "                    ";
                }
                Message_value += s.getMessageText()+"          ";
            }
            Log.d(TAG, "ACTION_GET_MESSAGES Message_value = " + Message_value);
            mAutoScroolView.setMessageBeans(mSession.getMessages().getMessageList());
            mAutoScroolView.setTexts(Message_value);
            mAutoScroolView.init(getWindowManager());
            mAutoScroolView.startScroll();
        }
        
        super.onResume();
    }
}
