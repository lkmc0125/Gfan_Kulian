package com.xiaohong.kulian.common.util;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.util.Log;
 
public class WifiAdmin {
 
    private static final String TAG = "WifiAdmin";
    private WifiManager mWifiManager;
    private WifiInfo mWifiInfo;
    private List<ScanResult> mWifiList = null;
    private List<WifiConfiguration> mWifiConfiguration;
    private WifiLock mWifiLock;
    private DhcpInfo dhcpInfo;

    public WifiAdmin(Context context) {
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiInfo = mWifiManager.getConnectionInfo();
    }

    public boolean openWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            Log.i(TAG, "setWifiEnabled.....");
            mWifiManager.setWifiEnabled(true);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Log.i(TAG, "setWifiEnabled.....end");
        }
        return mWifiManager.isWifiEnabled();
    }
 
    public void closeWifi() {
    	if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
    }
 
    public int checkState() {
        return mWifiManager.getWifiState();
    }
 
    public void acquireWifiLock() {
        mWifiLock.acquire();
    }
 
    public void releaseWifiLock() {
        if (mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }
 
    public void creatWifiLock() {
        mWifiLock = mWifiManager.createWifiLock("Test");
    }
 
    public List<WifiConfiguration> getConfiguration() {
        return mWifiConfiguration;
    }
 
    public void connectConfiguration(int index) {
        if (index > mWifiConfiguration.size()) {
            return;
        }
        mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId, true);
    }
 
    public void startScan() {
        boolean scan = mWifiManager.startScan();
        Log.i(TAG, "startScan result:" + scan);
        mWifiList = mWifiManager.getScanResults();
        mWifiConfiguration = mWifiManager.getConfiguredNetworks();
 
        if (mWifiList != null) {
            Log.i(TAG, "startScan result:" + mWifiList.size());
            for (int i = 0; i < mWifiList.size(); i++) {
                ScanResult result = mWifiList.get(i);
                Log.i(TAG, "startScan result[" + i + "]" + result.SSID + "," + result.BSSID);
            }
            Log.i(TAG, "startScan result end.");
        } else {
            Log.i(TAG, "startScan result is null.");
        }
 
    }

    public List<ScanResult> getWifiList() {
        return mWifiList;
    }
 
    public String wifiEncryptType(String capabilities) {
        ArrayList<String> encTypes = new ArrayList<String>();
        encTypes.add("WEP");
        encTypes.add("WPA");
        encTypes.add("WAPI");
        for (String type : encTypes) {
            if (capabilities.indexOf(type) != -1) {
                return type;
            }
        }
        return "";
    }
    
    public StringBuilder lookUpScan() {   
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < mWifiList.size(); i++) {
            stringBuilder.append("Index_" + new Integer(i + 1).toString() + ":");
            stringBuilder.append((mWifiList.get(i)).toString());
            stringBuilder.append("/n");
        }
        return stringBuilder;
    }
 
    public String getMacAddress() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }
 
    public String getBSSID() { // AP's mac-address
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }
 
    public DhcpInfo getDhcpInfo() {
        return dhcpInfo = mWifiManager.getDhcpInfo();
    }
 
    public int getIPAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    public int getNetworkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    public WifiInfo getWifiInfo() {
        mWifiInfo = mWifiManager.getConnectionInfo();
        return mWifiInfo;
    }

    public void addNetwork(WifiConfiguration wcg) {
        int wcgID = mWifiManager.addNetwork(wcg);
        boolean b = mWifiManager.enableNetwork(wcgID, true);
        System.out.println("addNetwork--" + wcgID);
        System.out.println("enableNetwork--" + b);
    }

    public void connectWifi(String ssid, String passwd, String encType) {
        Log.d(TAG, "Try to connect wifi");

        mWifiManager.setWifiEnabled(true);

        // 1.WIFICIPHER_NOPASS
        // 2.WIFICIPHER_WEP
        // 3.WIFICIPHER_WPA
        int type = 1;
        if (passwd.equals("")) {
            type = 1;
        } else if (encType.equals("WEP")) {
            type = 2;
        } else if (encType.equals("WPA")) {
            type = 3;
        }

        addNetwork(CreateWifiInfo(ssid, passwd, type));
    }
    public void disconnectWifi(int netId) {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }

//  type:
//  1.WIFICIPHER_NOPASS
//  2.WIFICIPHER_WEP
//  3.WIFICIPHER_WPA     
    public WifiConfiguration CreateWifiInfo(String SSID, String Password, int Type) {
        Log.i(TAG, "CreateWifiInfo SSID: " + SSID + " ,password: " + Password);
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
 
        WifiConfiguration tempConfig = this.IsExsits(SSID);
 
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        } else {
            Log.i(TAG, "IsExsits is null.");
        }
 
        if (Type == 1) // WIFICIPHER_NOPASS
        {
            Log.i(TAG, "Type =1.");
//            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//            config.wepTxKeyIndex = 0;
        }
        if (Type == 2) // WIFICIPHER_WEP
        {
            Log.i(TAG, "Type =2.");
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + Password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 3) // WIFICIPHER_WPA
        {
 
            Log.i(TAG, "Type =3.");
            config.preSharedKey = "\"" + Password + "\"";
 
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }
 
    private WifiConfiguration IsExsits(String SSID) {  
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        if (existingConfigs == null) {
        	return null;
        }
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }
}