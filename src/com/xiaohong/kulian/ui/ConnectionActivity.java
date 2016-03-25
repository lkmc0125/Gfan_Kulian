package com.xiaohong.kulian.ui;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Session;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.util.TopBar;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.util.WifiAuthentication;
import com.xiaohong.kulian.common.widget.BaseActivity;

public class ConnectionActivity extends BaseActivity {
    private static final String TAG = "ConnectionActivity"; 
    private Button mAuthBtn;
    private WifiAuthentication mAuth;
    private Session mSession;
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
        setContentView(R.layout.activity_connection);
        initTopBar();
        mAuth = new WifiAuthentication();
        mSession = Session.get(getApplicationContext());
        mConnectionStatus = ConnectionStatus.DISCONNECTED;

        mAuthBtn = (Button) findViewById(R.id.authenticationBtn);
        mAuthBtn.setVisibility(View.INVISIBLE);
        mAuthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authentication();
            }
        });
        
        checkWifiConnection();
    }

    private void initTopBar() {
        TopBar.createTopBar(getApplicationContext(),
                new View[] { findViewById(R.id.top_bar_title) },
                new int[] { View.VISIBLE },
                getString(R.string.connection_title));
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

    private boolean isHongWifi(String ssid) {
        if (ssid == null) {
            return false;
        }
        return ssid.toLowerCase().startsWith("hongwifi")
                || ssid.toLowerCase().startsWith("ruijie")
                || ssid.toLowerCase().endsWith("hongwifi")
                || (ssid.indexOf("小鸿") != -1);
    }

    private void authentication () {
        if (checkCoin()) {
            mAuth.appAuth();
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

    private void checkWifiConnection() {
        ConnectivityManager nw = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = nw.getActiveNetworkInfo();
        if (netinfo != null && netinfo.isAvailable()) {
//  checkLogin()
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
    }
}
