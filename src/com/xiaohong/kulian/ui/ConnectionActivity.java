package com.xiaohong.kulian.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Session;
import com.xiaohong.kulian.bean.AppBean;
import com.xiaohong.kulian.bean.AppListBean;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.util.TopBar;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.util.WifiAdmin;
import com.xiaohong.kulian.common.util.WifiAuthentication;
import com.xiaohong.kulian.common.widget.BaseActivity;

public class ConnectionActivity extends BaseActivity implements ApiRequestListener, OnClickListener {
    private static final String TAG = "ConnectionActivity"; 
    private Button mAuthBtn;
    private WifiAuthentication mAuth;
    private WifiAdmin mWifiAdmin;
    private TextView mWifiStatusDesc;
    private ImageView mWifiStatusIcon;
    private Integer mLoginRetryCount;
    private String mCurrentSSID;
    private Session mSession;
    private MyBroadcastReceiver mConnectionReceiver;
    private GridView mGridView;
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
        queryAppList();
//        setContentView(R.layout.activity_connection);
//        initTopBar();
//        mLoginRetryCount = 0;
//        mAuth = new WifiAuthentication();
//        mSession = Session.get(getApplicationContext());
//        mConnectionStatus = ConnectionStatus.DISCONNECTED;
//        mWifiStatusDesc = (TextView)findViewById(R.id.wifi_status_desc);
//        mWifiStatusIcon = (ImageView)findViewById(R.id.wifi_status_icon);
//        mAuthBtn = (Button) findViewById(R.id.authenticationBtn);
//        mAuthBtn.setVisibility(View.INVISIBLE);
//        mAuthBtn.setOnClickListener(this);
//        registerConnection();
//
//        new Handler().postDelayed(new Runnable() {  
//            public void run() {
//                mWifiAdmin = new WifiAdmin(getApplicationContext());
//                boolean open = mWifiAdmin.openWifi();
//                Log.i(TAG, "wifi open:" + open);
//                mWifiAdmin.startScan();
////                checkWifiConnection();
//            }
//        }, 500);
    }

    private void initView() {
        mGridView = (GridView)findViewById(R.id.connect_recommend_app_gridView_layout);
    }
    
    private void checkNetwork() {
        String url = "http://115.159.3.16/cb/app_test";
        if (null == Utils.httpGet(url)) { // 网络不通
            Log.d(TAG, "network not availabel");
            mAuthBtn.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {  
                public void run() {
                    checkNetwork();
                }
            }, 5 * 1000);
        } else {
            Log.d(TAG, "network OK！");
            mAuthBtn.setVisibility(View.INVISIBLE);
            if (mConnectionStatus == ConnectionStatus.HONGWIFI) {
                mConnectionStatus = ConnectionStatus.HONGWIFI_AUTHED;
                // reportAuthenSuccess
                if (mSession.isLogin()) {
                    url = MarketAPI.API_BASE_URL + "/dec_coin?phone_number="+mSession.getUserName();
                    String ret = Utils.httpGet(url);
                    if (ret != null) {
                        try {
                            JSONObject obj = new JSONObject(ret);
                            if (obj.getInt("ret_code") == 0) {
                                Utils.makeEventToast(getApplicationContext(), "扣了"+obj.getString("dec_coin_num")+"金币", false);
                            } else if (obj.getInt("ret_code") == 3001) { // 3001 means already deduction coin today
                                Utils.makeEventToast(getApplicationContext(), obj.getString("ret_msg"), false);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
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
        switch (status) {
        case DISCONNECTED:
        {
            mWifiStatusDesc.setText("未连接到WIFI");
            mAuthBtn.setText("搜索小鸿Wifi");
            mAuthBtn.setVisibility(View.VISIBLE);
            Drawable img = getApplicationContext().getResources()
            .getDrawable(R.drawable.wifi_state_off);
            mWifiStatusIcon.setImageDrawable(img);
            break;
        }
        case CONNECTED:
        {
            mWifiStatusDesc.setText("已连接到Wifi:"+mCurrentSSID);
            mAuthBtn.setText("切换到小鸿Wifi");
            mAuthBtn.setVisibility(View.VISIBLE);
            Drawable img = getApplicationContext().getResources()
            .getDrawable(R.drawable.wifi_state_on);
            mWifiStatusIcon.setImageDrawable(img);
            break;
        }
        case HONGWIFI:
        {
            mWifiStatusDesc.setText("已连接到小鸿免费Wifi:"+mCurrentSSID);
            mAuthBtn.setText("认证上网");
            mAuthBtn.setVisibility(View.VISIBLE);
            Drawable img = getApplicationContext().getResources()
            .getDrawable(R.drawable.wifi_state_on);
            mWifiStatusIcon.setImageDrawable(img);
            break;
        }
        case HONGWIFI_AUTHED:
        {
            mWifiStatusDesc.setText("已连接到小鸿免费Wifi"+mCurrentSSID);
            mAuthBtn.setVisibility(View.INVISIBLE);
            Drawable img = getApplicationContext().getResources()
            .getDrawable(R.drawable.wifi_state_on);
            mWifiStatusIcon.setImageDrawable(img);
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
                // // 3001 means already deduction coin today
                if (obj.getInt("ret_code") == 0 || obj.getInt("ret_code") == 3001) {
                    return true;
                } else {
                    Utils.makeEventToast(getApplicationContext(), obj.getString("ret_msg"), false);
                    return false;
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return false;
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
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(int method, Object obj) {
        switch (method) {
        case MarketAPI.ACTION_LOGIN:
        {
            Log.d(TAG, "login success");
            HashMap<String, Object> result = (HashMap<String, Object>) obj;
            mSession.setCoinNum((Integer)result.get(Constants.KEY_COIN_NUM));
            mSession.setSignInToday(result.get(Constants.KEY_SIGN_IN_TODAY).equals("true")); 
            mSession.setLogin(true);

            if (mSession.getMessages() == null) {
                MarketAPI.getMessages(getApplicationContext(), this);
            }

            // 签到
            if (!mSession.getSignInToday()) {
                MarketAPI.signIn(getApplicationContext(), this);
            }
            break;
        }
        case MarketAPI.ACTION_GET_MESSAGES:
        {
            ArrayList<HashMap<String, String>> messages = (ArrayList<HashMap<String, String>>)obj;
            if (messages.size() > 0) {
                mSession.setMessages(messages);    
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
            } else if (ret_code == 3002) { // 今天已经签到领取过了
                Log.d(TAG, "Already sign in today, ret_code 3002");
            }
            break;
        }
        case MarketAPI.ACTION_GET_APP_LIST:
        {
            AppListBean appList = (AppListBean) obj;
            ArrayList<AppBean> list = appList.getApplist();
            if (list.size() > 0) {
                List<Map<String, Object>> data_list = new ArrayList<Map<String, Object>>();
                for (AppBean bean : list) {
                    if (Utils.isApkInstalled(getApplicationContext(),
                            bean.getPackageName()) ==  true) {
                        continue;
                    }
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("logo_url", bean.getAppLogo());
                    map.put("name", bean.getAppName());
                    data_list.add(map);
                    if (data_list.size() >= 3) {
                        break;
                    }
                }
                showAppData(data_list);
            }
            break;
        }
        default:
            break;
        }
    }

    private void showAppData(List<Map<String, Object>> data_list) {
        String [] from ={"logo_url", "name"};
        int [] to = {R.id.connection_recommend_app_image, R.id.connection_recommend_app_name_hint_text};
        // todo: need a adapter show get image from network and show it in imageview
        SimpleAdapter sim_adapter = new SimpleAdapter(this, data_list, R.layout.connect_third_part_grid_item, from, to);
        mGridView.setAdapter(sim_adapter);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.authenticationBtn:
        {
            switch (mConnectionStatus) {
            case DISCONNECTED:
            case CONNECTED:
            {
                for (ScanResult scanResult : mWifiAdmin.getWifiList()) {
                    if (isHongWifi(scanResult.SSID)) {
                        String encryptType = mWifiAdmin.wifiEncryptType(scanResult.capabilities); 
                        if (encryptType.length() > 0) { // only connect to no password wifi
                            continue;
                        }
                        mWifiAdmin.connectWifi(scanResult.SSID, "", encryptType);
                        Log.d(TAG, "Trying to connect "+scanResult.SSID);
                        break;
                    }
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
        default:
            break;
        }
    }
}
