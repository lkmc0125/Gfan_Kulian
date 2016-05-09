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
package com.xiaohong.kulian;

import static com.xiaohong.kulian.SessionManager.P_CARD_VERSION;
import static com.xiaohong.kulian.SessionManager.P_CLEAR_CACHE;
import static com.xiaohong.kulian.SessionManager.P_CURRENT_VERSION;
import static com.xiaohong.kulian.SessionManager.P_DEFAULT_CHARGE_TYPE;
import static com.xiaohong.kulian.SessionManager.P_ISLOGIN;
import static com.xiaohong.kulian.SessionManager.P_LPNS_BINDED_DEVID;
import static com.xiaohong.kulian.SessionManager.P_LPNS_IS_BINDED;
import static com.xiaohong.kulian.SessionManager.P_MARKET_PASSWORD;
import static com.xiaohong.kulian.SessionManager.P_MARKET_USERNAME;
import static com.xiaohong.kulian.SessionManager.P_PRODUCT_UPDATE_CHECK_TIMESTAMP;
import static com.xiaohong.kulian.SessionManager.P_SCREEN_SIZE;
import static com.xiaohong.kulian.SessionManager.P_SPLASH_ID;
import static com.xiaohong.kulian.SessionManager.P_SPLASH_TIME;
import static com.xiaohong.kulian.SessionManager.P_UID;
import static com.xiaohong.kulian.SessionManager.P_UPDATE_AVAILABIE;
import static com.xiaohong.kulian.SessionManager.P_UPDATE_DESC;
import static com.xiaohong.kulian.SessionManager.P_UPDATE_ID;
import static com.xiaohong.kulian.SessionManager.P_UPDATE_LEVEL;
import static com.xiaohong.kulian.SessionManager.P_UPDATE_URI;
import static com.xiaohong.kulian.SessionManager.P_UPDATE_VERSION_CODE;
import static com.xiaohong.kulian.SessionManager.P_UPDATE_VERSION_NAME;
import static com.xiaohong.kulian.SessionManager.P_UPGRADE_NUM;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.bean.MessageListBean;
import com.xiaohong.kulian.bean.ReportResultBean;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.download.DownloadManager;
import com.xiaohong.kulian.common.download.DownloadManager.Impl;
import com.xiaohong.kulian.common.util.DBUtils;
import com.xiaohong.kulian.common.util.DialogUtils;
import com.xiaohong.kulian.common.util.MarketProvider;
import com.xiaohong.kulian.common.util.Pair;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.vo.DownloadInfo;
import com.xiaohong.kulian.common.vo.UpgradeInfo;

/**
 * 
 * The Client Session Object, contains some necessary
 * information.
 * 
 */
public class Session extends Observable {

    /** Log tag */
    private final static String TAG = "Session";
    
    /** Application Context */
    private Context mContext;
    
    /** The application debug mode */
    public boolean isDebug;

    /** The application uid */
    private String uid;

    /** The mobile device screen size */
    private String screenSize;

    /** The version of OS */
    private int osVersion;

    /** The Http User-Agent */
    private String userAgent;

    /** The user login status */
    private boolean isLogin;

    /** Indicate whether auto clear cache when user exit */
    private boolean isAutoClearCache;

    /** The channel id */
    private String cid;

    /** The SDK id */
    private String cpid;
    
    /** The Application Debug flag */
    private String debugType;

    /** The Application Version Code */
    private int versionCode;

    /** The Application package name */
    private String packageName;

    /** The Application version name */
    private String versionName;

    /** The Application version name */
    private String appName;

    /** The mobile IMEI code */
    private String imei;

    /** The mobile sim code */
    private String sim;
    
    /** The mobile mac address */
    private String macAddress;

    /**
     * The mobile model such as "Nexus One" Attention: some model type may have
     * illegal characters
     */
    private String model;

    /** The user-visible version string. E.g., "1.0" */
    private String buildVersion;

    /** User login name */
    private String userName;

    /** User login password */
    private String password;

    /** Indicate whether new version is available */
    private boolean isUpdateAvailable;

    /** The new version name */
    private String updateVersionName;

    /** The new version code */
    private int updateVersionCode;

    /** The new version description */
    private String updateVersionDesc;

    /** The new version update uri */
    private String updateUri;

    /** The new version update level(Force Update/Option Update) */
    private int updateLevel;
    
    /** The new version APK download task id*/
    private long updateId;

    /** The cloud service device bind flag */
    private boolean isDeviceBinded;

    /** The mobile device id */
    private String deviceId;

    /** FIXME The apps upgrade number */
    private int upgradeNumber;

    /** The apps update check time */
    private long updataCheckTime;

    /** The local card version */
    private int creditCardVersion;
    
    /** The current version */
    private int lastVersion;

    /** 上次更新splash的时间戳 */
    private long splashTime;

    /** 上次更新splash的id */
    private long splashId;

    /** 金币数 */
    private Integer coinNum;
    
    /** The application list which user has installed */
    private ArrayList<String> mInstalledApps;
    
//    /** 下载的应用ICON缓存 */
//    private HashMap<String, String> mIconUrlCache;
    
    /** Session Manager */
    private SessionManager mSessionManager;

    /** Download Manager */
    private DownloadManager mDownloadManager;

    /** The singleton instance */
    private static Session mInstance;
    
    /** 默认的支付方式 */
    private String mDefaultChargeType;

    /** xiaohong wifi name list */
    private ArrayList<String> ssidList;
    
    /** 广播和私人消息
     * 每个条目有text字段，可能有url字段
     *  */
    private MessageListBean messageList;

    /** 今天是否已经签到过 */
    private boolean signInToday;

    /** 上网时长剩余时间 */
    private int remainTime;
    
    private LeftTime mLeftTime = new LeftTime();

    /** 是否正在使用上网时长，是则开始倒计时 */
    private boolean isCountDown;
    
    /**
     * A cookie
     */
    private String mToken;

    private String channel; // 分发渠道（读取manifest文件UMENG配置）

    private final ReportApiRequestListener mReportApiRequestListener = new ReportApiRequestListener();
    
    private final ArrayList<OnCoinUpdatedListener> mOnCoinUpdatedListener = new ArrayList<OnCoinUpdatedListener>();
    
    private final ArrayList<OnAppInstalledListener> mOnAppInstalledListener = new ArrayList<OnAppInstalledListener>();
    
    
    private ArrayList<OnLeftTimeUpdateListener> mOnLeftTimeUpdateListeners 
        = new ArrayList<OnLeftTimeUpdateListener>();
    
    public static enum PersonalCenterStatus {
        SHOW_SIGN_IN,
        SHOW_LEFT_TIME
    }
    
    private PersonalCenterStatus mPersonalCenterStatus = PersonalCenterStatus.SHOW_SIGN_IN;
    

    public PersonalCenterStatus getPersonalCenterStatus() {
        return mPersonalCenterStatus;
    }

    /**
     * Set the status indicate what show, if status is PersonalCenterStatus.SHOW_SIGN_IN
     * then show sign in
     * @param personalCenterStatus
     */
    public void setPersonalCenterStatus(PersonalCenterStatus personalCenterStatus) {
        this.mPersonalCenterStatus = personalCenterStatus;
    }

    /**
     * default constructor
     * @param context
     */
    private Session(Context context) {
        
        synchronized (this) {
            mContext = context;
            
            mHandler.sendEmptyMessage(CURSOR_CREATED);
            
            osVersion = Build.VERSION.SDK_INT;
            buildVersion = Build.VERSION.RELEASE;
            try {
                model = URLEncoder.encode(Build.MODEL, "UTF-8");
            } catch (UnsupportedEncodingException e) {
            }
            mDownloadManager = new DownloadManager(context.getContentResolver(), getPackageName());

            readSettings();
        }
    }
    
    /*
     * 读取用户所有的设置
     */
    private void readSettings() {
        new Thread() {
            public void run() {
                mSessionManager = SessionManager.get(mContext);
                addObserver(mSessionManager);
                HashMap<String, Object> preference = mSessionManager.readPreference();
                uid = (String) preference.get(P_UID);
                screenSize = (String) preference.get(P_SCREEN_SIZE);
//                isLogin = (Boolean) preference.get(P_ISLOGIN);
                isAutoClearCache = (Boolean) preference.get(P_CLEAR_CACHE);
                userName = (String) preference.get(P_MARKET_USERNAME);
                password = (String) preference.get(P_MARKET_PASSWORD);
                upgradeNumber = (Integer) preference.get(P_UPGRADE_NUM);
                updataCheckTime = (Long) preference.get(P_PRODUCT_UPDATE_CHECK_TIMESTAMP);
                updateId = (Long) preference.get(P_UPDATE_ID);

                // cloud preferences
                deviceId = (String) preference.get(P_LPNS_BINDED_DEVID);
                isDeviceBinded = (Boolean) preference.get(P_LPNS_IS_BINDED);

                creditCardVersion = (Integer) preference.get(P_CARD_VERSION);
                lastVersion = (Integer) preference.get(P_CURRENT_VERSION);

                isUpdateAvailable = (Boolean) preference.get(P_UPDATE_AVAILABIE);
                updateVersionName = (String) preference.get(P_UPDATE_VERSION_NAME);
                updateVersionCode = (Integer) preference.get(P_UPDATE_VERSION_CODE);
                updateVersionDesc = (String) preference.get(P_UPDATE_DESC);
                updateUri = (String) preference.get(P_UPDATE_URI);
                updateLevel = (Integer) preference.get(P_UPDATE_LEVEL);

                splashId = (Long) preference.get(P_SPLASH_ID);
                splashTime = (Long) preference.get(P_SPLASH_TIME);

                mDefaultChargeType = (String) preference.get(P_DEFAULT_CHARGE_TYPE);
               
                getApplicationInfo();
            };
        }.start();
    }  

    public int isFilterApps() {
        return mSessionManager.isFilterApps();
    }

    public static Session get(Context context) {
        if (mInstance == null) {
            mInstance = new Session(context);
        }
        return mInstance;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
        super.setChanged();
        super.notifyObservers(new Pair<String, Object>(P_UID, uid));
    }

    public void setRemainTime(int remainTime) {
        Log.d(TAG, "setRemainTime remainTime = " + remainTime);
        //debug set remainTime to a fixed value
        //remainTime = 50000;
        this.remainTime = remainTime;
        mLeftTime.setRemainTime(remainTime);
    }

    public int getRemainTime() {
        return remainTime;
    }
    
    /**
     * 获取格式化的剩余时间
     * @return
     */
    public LeftTime getLeftTime() {
        return mLeftTime;
    }

    public void setIsCountDown(boolean isCountDown) {
        this.isCountDown = isCountDown;
    }
    
    public  boolean getIsCountdown() {
        return isCountDown;
    }

    public MessageListBean getMessages() {
        return messageList;
    }

    public void setMessages(MessageListBean messageList) {
        this.messageList = messageList;
    }

    public String getScreenSize() {
        return screenSize;
    }

    public void setScreenSize(Activity activity) {

        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        this.screenSize = dm.widthPixels < dm.heightPixels ? dm.widthPixels + "#" + dm.heightPixels
                : dm.heightPixels + "#" + dm.widthPixels;
        super.setChanged();
        super.notifyObservers(new Pair<String, Object>(P_SCREEN_SIZE, screenSize));
    }

    public int getOsVersion() {
        return osVersion;
    }

    public String getJavaApiUserAgent() {
        if (TextUtils.isEmpty(userAgent)) {
            StringBuilder buf = new StringBuilder();
            final String splash = "/";
            buf.append(getModel()).append(splash).append(getBuildVersion()).append(splash)
                    .append(mContext.getString(R.string.app_name_en)).append(splash)
                    .append(getVersionName()).append(splash).append(getCid()).append(splash)
                    // 2011/3/7 add mac address for Analytics
                    .append(getIMEI()).append(splash).append(getSim()).append(splash).append(getMac());
            return buf.toString();
        }
        return userAgent;
    }

    public String getUCenterApiUserAgent() {
        return "packageName=com.xiaohong.kulian,appName=Kulian,channelID=9";
    }

    private void getApplicationInfo() {

        final PackageManager pm = (PackageManager) mContext.getPackageManager();
        try {
            final PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), 0);
            versionName = pi.versionName;
            versionCode = pi.versionCode;

            final ApplicationInfo ai = pm.getApplicationInfo(mContext.getPackageName(),
                    PackageManager.GET_META_DATA);
            cid = ai.metaData.get("kulian_cid").toString();
            cpid = ai.metaData.get("kulian_cpid").toString();
            debugType = ai.metaData.get("kulian_debug").toString();

            channel = ai.metaData.getString("UMENG_CHANNEL");
            
            if ("1".equals(debugType)) {
                // developer mode
                isDebug = true;
            } else if ("0".equals(debugType)) {
                // release mode
                isDebug = false;
            }
            Utils.sDebug = isDebug;

            appName = String.valueOf(ai.loadLabel(pm));
            Utils.sLogTag = appName;
            packageName = mContext.getPackageName();

            TelephonyManager telMgr = (TelephonyManager) mContext
                    .getSystemService(Context.TELEPHONY_SERVICE);
            imei = telMgr.getDeviceId();
            sim = telMgr.getSimSerialNumber();
        } catch (NameNotFoundException e) {
            Log.d(TAG, "met some error when get application info");
        }
    }

    public String getCid() {
        if (TextUtils.isEmpty(cid)) {
            getApplicationInfo();
        }
        return cid;
    }

    public String getCpid() {
        if (TextUtils.isEmpty(cpid)) {
            getApplicationInfo();
        }
        return cpid;
    }

    public String getVersionName() {
        if (TextUtils.isEmpty(versionName)) {
            getApplicationInfo();
        }
        return versionName;
    }

    public int getVersionCode() {
        if (versionCode <= 0) {
            getApplicationInfo();
        }
        return versionCode;
    }

    public String getChannel() {
        if (TextUtils.isEmpty(channel)) {
            getApplicationInfo();
        }
        return channel;
    }

    public String getIMEI() {
        if (TextUtils.isEmpty(imei)) {
            getApplicationInfo();
        }
        return imei;
    }

    public String getPackageName() {
        if (TextUtils.isEmpty(packageName)) {
            getApplicationInfo();
        }
        return packageName;
    }

    public String getSim() {
        if (TextUtils.isEmpty(sim)) {
            getApplicationInfo();
        }
        return sim;
    }

    public String getMac() {
        if (TextUtils.isEmpty(macAddress)) {
            WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifi.getConnectionInfo();
            macAddress = info.getMacAddress();
        }
        return macAddress;
    }

    public boolean isLogin() {
        return isLogin;
    }

    public void setLogin(boolean isLogin) {

        // there is no need to update for [same] value
        if (this.isLogin == isLogin) {
            return;
        }

        this.isLogin = isLogin;
        super.setChanged();
        super.notifyObservers(new Pair<String, Object>(P_ISLOGIN, isLogin));
    }

    public void setCoinNum(Integer coinNum) {
        this.coinNum = coinNum;
    }
    
    public Integer getCoinNum() {
        return coinNum;
    }
    
    public void setSignInToday(boolean signIn) {
        this.signInToday = signIn;
    }

    public boolean getSignInToday() {
        return signInToday;
    }
    
    public String getUserName() {
        if (userName == null) {
            return "";
        }
        return userName;
    }

    public void setUserName(String userName) {

        this.userName = userName;
        super.setChanged();
        super.notifyObservers(new Pair<String, Object>(P_MARKET_USERNAME, userName));
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {

        this.password = password;
        super.setChanged();
        super.notifyObservers(new Pair<String, Object>(P_MARKET_PASSWORD, password));
    }

    public String getAppName() {
        return appName;
    }

    public boolean isUpdateAvailable() {
        return isUpdateAvailable;
    }
    
    public void setUpdateAvailable(boolean flag) {
        this.isUpdateAvailable = flag;
        super.setChanged();
        super.notifyObservers(new Pair<String, Object>(P_UPDATE_AVAILABIE, flag));
    }

    public String getUpdateVersionName() {
        return updateVersionName;
    }

    public int getUpdateVersionCode() {
        return updateVersionCode;
    }

    public String getUpdateVersionDesc() {
        return updateVersionDesc;
    }

    public String getUpdateUri() {
        return updateUri;
    }

    public int getUpdateLevel() {
        return updateLevel;
    }

    public void setUpdateInfo(int versionCode, String description, String url) {
        
        this.isUpdateAvailable = true;
        this.updateVersionCode = versionCode;
        this.updateVersionDesc = description;
        this.updateUri = url;
        super.setChanged();
        super.notifyObservers(new Pair<String, Object>(P_UPDATE_AVAILABIE, true));
        super.setChanged();
        super.notifyObservers(new Pair<String, Object>(P_UPDATE_VERSION_CODE, versionCode));
        super.setChanged();
        super.notifyObservers(new Pair<String, Object>(P_UPDATE_DESC, description));
        super.setChanged();
        super.notifyObservers(new Pair<String, Object>(P_UPDATE_URI, url));
    }

    public long getUpdateId() {
        return updateId;
    }

    public void setUpdateID(long updateId) {
        
        if(this.updateId == updateId) {
            return;
        }
        
        this.updateId = updateId;
        super.setChanged();
        super.notifyObservers(new Pair<String, Object>(P_UPDATE_ID, updateId));
    }

    public boolean isAutoClearCache() {
        return isAutoClearCache;
    }

    public String getModel() {
        return model;
    }

    public String getBuildVersion() {
        return buildVersion;
    }

    public DownloadManager getDownloadManager() {
        if (mDownloadManager == null) {
            mDownloadManager = new DownloadManager(mContext.getContentResolver(), getPackageName());
        }
        return mDownloadManager;
    }

    public boolean isDeviceBinded() {
        return isDeviceBinded;
    }

    public void setDeviceBinded(boolean isDeviceBinded) {

        // there is no need to update for [same] value
        if (this.isDeviceBinded == isDeviceBinded) {
            return;
        }

        this.isDeviceBinded = isDeviceBinded;
        super.setChanged();
        super.notifyObservers(new Pair<String, Object>(P_LPNS_IS_BINDED, isDeviceBinded));
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {

        this.deviceId = deviceId;
        super.setChanged();
        super.notifyObservers(new Pair<String, Object>(P_LPNS_BINDED_DEVID, deviceId));
    }

    public int getUpgradeNumber() {
        return upgradeNumber;
    }

    public void setUpgradeNumber(int upgradeNumber) {

        // there is no need to update for [same] value
        if (this.upgradeNumber == upgradeNumber) {
            return;
        }
        this.upgradeNumber = upgradeNumber;
        super.setChanged();
        super.notifyObservers(new Pair<String, Object>(P_UPGRADE_NUM, upgradeNumber));
        mHandler.sendEmptyMessage(CURSOR_UPDATE);
    }

    public long getUpdataCheckTime() {
        return updataCheckTime;
    }

    public void setUpdataCheckTime(long updataCheckTime) {

        // there is no need to update for [same] value
        if (this.updataCheckTime == updataCheckTime) {
            return;
        }
        this.updataCheckTime = updataCheckTime;
        super.setChanged();
        super.notifyObservers(new Pair<String, Object>(P_PRODUCT_UPDATE_CHECK_TIMESTAMP,
                updataCheckTime));
    }

    public int getCreditCardVersion() {
        return creditCardVersion;
    }

    public void setCreditCardVersion(int creditCardVersion) {

        // there is no need to update for [same] value
        if (this.creditCardVersion == creditCardVersion) {
            return;
        }

        this.creditCardVersion = creditCardVersion;
        super.setChanged();
        super.notifyObservers(new Pair<String, Object>(P_CARD_VERSION, creditCardVersion));
    }

    public String getDebugType() {
        return debugType;
    }

    public void close() {
        mSessionManager.writePreferenceQuickly();
        mDownloadingCursor.unregisterContentObserver(mCursorObserver);
        mDownloadingCursor.close();
        mInstance = null;
    }

    public ArrayList<String> getInstalledApps() {

        if (mInstalledApps == null) {
            Utils.getAllInstalledApps(mContext);
        }
        return mInstalledApps;
    }

    public void addInstalledApp(String packageName) {

        reportAppInstalled(packageName);
        if (mInstalledApps == null) {
            Utils.getAllInstalledApps(mContext);
        }
        mInstalledApps.add(packageName);
        mHandler.sendEmptyMessage(CURSOR_UPDATE);
    }
    
    public void removeInstalledApp(String packageName) {
        if (mInstalledApps == null) {
            Utils.getAllInstalledApps(mContext);
        }
        if (mInstalledApps != null) {
            mInstalledApps.remove(packageName);           
        }
        mHandler.sendEmptyMessage(CURSOR_UPDATE);
    }

    public void setInstalledApps(ArrayList<String> mInstalledApps) {
        this.mInstalledApps = mInstalledApps;
    }

    public long getSplashTime() {
        return splashTime;
    }

    public void setSplashTime(long splashTime) {
        this.splashTime = splashTime;

        super.setChanged();
        super.notifyObservers(new Pair<String, Object>(P_SPLASH_TIME, splashTime));
    }
    
    public int getLastVersion() {
        return lastVersion;
    }

    public void setLastVersion(int currentVersion) {

        if (currentVersion == this.lastVersion) {
            return;
        }
        clearData();
        this.lastVersion = currentVersion;
        super.setChanged();
        super.notifyObservers(new Pair<String, Object>(P_CURRENT_VERSION, currentVersion));
    }
    
    /**
     * 清除上一个版本数据
     */
    public void clearData() {
        setDeviceId("");
        setDeviceBinded(false);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        sp.edit().clear().commit();
    }

    public long getSplashId() {
        return splashId;
    }

    public void setSplashId(long splashId) {
        this.splashId = splashId;

        super.setChanged();
        super.notifyObservers(new Pair<String, Object>(P_SPLASH_ID, splashId));
    }

    public String getDefaultChargeType() {
    	return mDefaultChargeType;
	}

	public void setDefaultChargeType(String type) {
		mDefaultChargeType = type;
		super.setChanged();
        super.notifyObservers(new Pair<String, Object>(P_DEFAULT_CHARGE_TYPE, type));
	}
	
	/** 创建下载数据结果集*/
	private static final int CURSOR_CREATED = 0;
	/** 更新下载数据结果集*/
	private static final int CURSOR_CHANGED = 1;
	/** 产品更新 */
	private static final int CURSOR_UPDATE = 2;
	/** 下载列表更新 */
	private static final int UPDATE_LIST = 3;
	
	private static final int ADD_COIN = 4;
	
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {

            case CURSOR_CREATED:

                mDownloadingList = new HashMap<String, DownloadInfo>();
                startQuery();
                break;

            case CURSOR_CHANGED:

                if (mDownloadingCursor == null) {
                    return;
                }
                mDownloadingCursor.requery();
                synchronized (this) {
                    refreshDownloadApp(mDownloadingCursor);
                }
                break;

            case CURSOR_UPDATE:

                setChanged();
                notifyObservers(Constants.INFO_UPDATE);
                break;
                
            case UPDATE_LIST:
                
                setChanged();
                notifyObservers(mDownloadingList);
                break;

            case ADD_COIN:
                if (msg.arg1 > 0) { // add coin num
                    sendAddCoinRequest(msg.arg1);
                }
                break;
            default:
                break;
            }
        }
    };
	
    private HashMap<String, DownloadInfo> mDownloadingList;
    /** The application list which user can update */
    private HashMap<String, UpgradeInfo> mUpdateApps = new HashMap<String, UpgradeInfo>();
    private Cursor mDownloadingCursor;

    public HashMap<String, DownloadInfo> getDownloadingList() {
        return mDownloadingList;
    }
    
    public HashMap<String, UpgradeInfo> getUpdateList() {
        return mUpdateApps;
    }
    
    public void setUpdateList() {
        mUpdateApps = DBUtils.queryUpdateProduct(mContext);
        mHandler.sendEmptyMessage(CURSOR_UPDATE);
    }
    
    public void setUpdateList(HashMap<String, UpgradeInfo> list) {
        mUpdateApps = list;
        mHandler.sendEmptyMessage(CURSOR_UPDATE);
    }

    public ArrayList<String> getSSIDList() {
        return ssidList;
    }

    public void setSSIDList(ArrayList<String> ssidList) {
        this.ssidList = ssidList;
    }

    private ContentObserver mCursorObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange) {
            mHandler.sendEmptyMessage(CURSOR_CHANGED);
        }
    };
    
    /**
     * 提醒下载列表更新
     */
    public void updateDownloading() {
        mHandler.sendEmptyMessage(UPDATE_LIST);
    }
    
    private void startQuery() {
        DbStatusRefreshTask refreshTask = new DbStatusRefreshTask(mContext.getContentResolver());
        refreshTask.startQuery(DbStatusRefreshTask.DOWNLOAD, null,
                DownloadManager.Impl.CONTENT_URI, null, "(((" + DownloadManager.Impl.COLUMN_STATUS
                        + " >= '190' AND " + DownloadManager.Impl.COLUMN_STATUS + " <= '200') OR "
                        + DownloadManager.Impl.COLUMN_STATUS + " = '"
                        + DownloadManager.Impl.STATUS_CANCELED + "') AND "
                        + DownloadManager.Impl.COLUMN_DESTINATION + " = '"
                        + DownloadManager.Impl.DESTINATION_EXTERNAL + "' AND "
                        + Impl.COLUMN_MIME_TYPE + " = '"
                        + com.xiaohong.kulian.common.download.Constants.MIMETYPE_APK + "')", null, null);

        String selection = MarketProvider.COLUMN_P_IGNORE + "=?";
        String[] selectionArgs = new String[] { "0" };
        refreshTask.startQuery(DbStatusRefreshTask.UPDATE, null, MarketProvider.UPDATE_CONTENT_URI,
                null, selection, selectionArgs, null);
    }
    
    /*
     * 刷新正在下载中的应用
     */
    void refreshDownloadApp(Cursor cursor) {

        // 绑定观察者
        if (mDownloadingCursor == null) {
            mDownloadingCursor = cursor;
            cursor.registerContentObserver(mCursorObserver);
        }

        if (cursor.getCount() > 0) {
            // 检索有结果
            mDownloadingList = new HashMap<String, DownloadInfo>();
        } else {
            return;
        }

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

            String packageName = cursor.getString(cursor
                    .getColumnIndex(DownloadManager.Impl.COLUMN_PACKAGE_NAME));

            DownloadInfo infoItem = new DownloadInfo();
            infoItem.id = cursor.getInt(cursor.getColumnIndex(DownloadManager.Impl.COLUMN_ID));
            infoItem.mPackageName = packageName;
            infoItem.mAppName = cursor.getString(cursor
                    .getColumnIndex(DownloadManager.Impl.COLUMN_TITLE));
            
            int source = cursor.getInt(cursor
                    .getColumnIndex(DownloadManager.Impl.COLUMN_SOURCE));
            infoItem.mIconUrl = cursor.getString(cursor
                        .getColumnIndex(DownloadManager.Impl.COLUMN_NOTIFICATION_EXTRAS));

            infoItem.mStatus = cursor.getInt(cursor
                    .getColumnIndex(DownloadManager.Impl.COLUMN_STATUS));
            mDownloadingList.put(packageName, infoItem);

            if (DownloadManager.Impl.isStatusRunning(infoItem.mStatus)) {
                // downloading progress
                long currentBytes = cursor.getInt(cursor
                        .getColumnIndex(DownloadManager.Impl.COLUMN_CURRENT_BYTES));
                long totalBytes = cursor.getInt(cursor
                        .getColumnIndex(DownloadManager.Impl.COLUMN_TOTAL_BYTES));
                infoItem.mTotalSize = totalBytes;
                infoItem.mCurrentSize = currentBytes;
                int progress = (int) ((float) currentBytes / (float) totalBytes * 100);
                infoItem.mProgress = progress + "%";
                infoItem.mProgressNumber = progress;
                // 下载分成8个级别，用于显示下载进度动画
                int progressLevel = (int) (progress / 14) + 2;
                infoItem.mProgressLevel = progressLevel > 8 ? 8 : progressLevel;
            } else if (DownloadManager.Impl.isStatusPending(infoItem.mStatus)) {
                // 下载等待中
                infoItem.mProgressLevel = Constants.STATUS_PENDING;
            } else if (infoItem.mStatus == DownloadManager.Impl.STATUS_SUCCESS) {
                // download success
                infoItem.mProgressLevel = Constants.STATUS_DOWNLOADED;
                infoItem.mFilePath = cursor.getString(cursor
                        .getColumnIndex(DownloadManager.Impl.COLUMN_DATA));

                // 检查文件完整性，如果不存在，删除此条记录
                if (!new File(infoItem.mFilePath).exists()) {
                    mDownloadingList.remove(packageName);
                }
            } else if (infoItem.mStatus == DownloadManager.Impl.STATUS_CANCELED) {
                // 用户取消下载，恢复原始状态
                Utils.D("session cancel download " + infoItem.mAppName + " " + infoItem.mStatus);
                infoItem.mProgressLevel = Constants.STATUS_NORMAL;
                mDownloadManager.remove(infoItem.id);
            }
        }
        setChanged();
        notifyObservers(mDownloadingList);
    }

    /*
     * 刷新可更新的应用
     */
    void refreshUpdateApp(Cursor cursor) {
        mUpdateApps = new HashMap<String, UpgradeInfo>();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                UpgradeInfo info = new UpgradeInfo();
                info.pid = cursor.getString(cursor.getColumnIndex(MarketProvider.COLUMN_P_ID));
                info.pkgName = cursor.getString(cursor
                        .getColumnIndex(MarketProvider.COLUMN_P_PACKAGE_NAME));
                info.versionName = cursor.getString(cursor
                        .getColumnIndex(MarketProvider.COLUMN_P_NEW_VERSION_NAME));
                info.versionCode = cursor.getInt(cursor
                        .getColumnIndex(MarketProvider.COLUMN_P_NEW_VERSION_CODE));
                mUpdateApps.put(info.pkgName, info);
            }
            cursor.close();
        }
    }
    
    /**
     * 本地数据库刷新检查
     *
     */
    private class DbStatusRefreshTask extends AsyncQueryHandler {

        private final static int DOWNLOAD = 0;
        private final static int UPDATE = 1;
        
        public DbStatusRefreshTask(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            
            switch (token) {
            case DOWNLOAD:
                refreshDownloadApp(cursor);
                break;
                
            case UPDATE:
                refreshUpdateApp(cursor);
                break;

            default:
                break;
            }
        }
    }
    
    public void setToken(String token) {
        mToken = token;
    }
    
    public String getToken() {
        return mToken;
    }
    
    public void requestAddCoin(int coinNum) {
        Message msg = new Message();
        msg.what = ADD_COIN;
        msg.arg1 = coinNum;
        mHandler.sendMessage(msg);
    }
    
    private void sendAddCoinRequest(int coinNum) {
        MarketAPI.requestAddCoin(mContext, mReportApiRequestListener, coinNum);
    }
    
    private void reportAppInstalled(String packagename) {
//        DownloadInfo info = mDownloadingList.get(packagename);
//        if(info != null) {
            MarketAPI.reportAppInstalled(mContext, mReportApiRequestListener, packagename);
            notifyAppInstalled(packagename);
            Utils.removeInstalledApkByPackageName(packagename);
//        }
    }
    
    public void reportAppLaunched(String packagename) {
//        DownloadInfo info = mDownloadingList.get(packagename);
//        if(info != null) {
            MarketAPI.reportAppLaunched(mContext, mReportApiRequestListener, packagename);
//        }
    }
    
    private class ReportApiRequestListener implements ApiRequestListener {

        @Override
        public void onSuccess(int method, Object obj) {
            
            switch(method) {
                case MarketAPI.ACTION_REPORT_APP_INSTALLED:
                {
                    ReportResultBean result = (ReportResultBean) obj;
                    //should move add logic to notifyCoinUpdated function
                    //coinNum += result.getAddedCoinNum();
                    notifyCoinUpdated(result.getAddedCoinNum());
                    break;
                }
                case MarketAPI.ACTION_REPORT_APP_LAUNCHED:
                {
                    ReportResultBean result = (ReportResultBean) obj;
                    //coinNum += result.getAddedCoinNum();
                    notifyCoinUpdated(result.getAddedCoinNum());
                    break;
                }
                case MarketAPI.ACTION_REQ_ADD_COIN:
                {
                    ReportResultBean result = (ReportResultBean) obj;
                    notifyCoinUpdated(result.getAddedCoinNum());
                    break;
                }
            }
        }

        @Override
        public void onError(int method, int statusCode) {
            // TODO Auto-generated method stub
            
        }
        
    }

    // 正在运行的
    private List<String> getRunningProcess() {
        ActivityManager am = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        // 获取正在运行的应用
        List<RunningAppProcessInfo> runList = am.getRunningAppProcesses();
        ArrayList<String> runPackageList = new ArrayList<String>();
        for (RunningAppProcessInfo info : runList) {
            runPackageList.add(info.processName);
        }
        return runPackageList;
    }
    
    private volatile boolean mNeedStopThread = false;

    private class QueryRunningAppThread extends Thread {
        @Override
        public void run() {
            while(true) {
                if(mNeedStopThread == true) {
                    break;
                }
                List<String> list = getRunningProcess();
                
                for(String pkg : list) {
                    if(mDownloadingList.get(pkg) != null) {
                        reportAppLaunched(pkg);
                    }
                }
            }
        }
    }
    
    /**
     * 用于通知金币数量发声变化，个人主页需要更新
     * @author free
     *
     */
    public static interface OnCoinUpdatedListener {
        public void onCoinUpdate(int newTotalCoinNum);
    }
    
    public void addOnCoinUpdateListener(OnCoinUpdatedListener listener) {
        mOnCoinUpdatedListener.add(listener);
    }
    
    public void removeOnCoinUpdateListener(OnCoinUpdatedListener listener) {
        mOnCoinUpdatedListener.remove(listener);
    }
    
    /**
     * This function should only be called by ConnectionActivity
     */
    public void notifyCoinUpdated() {
        for(OnCoinUpdatedListener listener : mOnCoinUpdatedListener) {
            listener.onCoinUpdate(coinNum);
        }
    }
    
    public void notifyCoinUpdated(int added_coin) {
        Log.d(TAG, "notifyCoinUpdated added_coin = " + added_coin);
        if(added_coin > 0) {
            DialogUtils.showMessage(mContext, "金币奖励", "您获得了" + added_coin + "个金币");
        }
        coinNum = coinNum + added_coin;
        for(OnCoinUpdatedListener listener : mOnCoinUpdatedListener) {
            listener.onCoinUpdate(coinNum);
        }
    }
    
    /**
     * 用于通知app已安装完毕
     * @author free
     *
     */
    public static interface OnAppInstalledListener {
        /**
         * 
         * @param packageName App's package name
         */
        public void onAppInstalled(String packageName);
    }
    
    public void addOnAppInstalledListener(OnAppInstalledListener listener) {
        mOnAppInstalledListener.add(listener);
    }
    
    public void removeOnAppInstalledListener(OnAppInstalledListener listener) {
        mOnAppInstalledListener.remove(listener);
    }
    
    private void notifyAppInstalled(String packageName) {
        for(OnAppInstalledListener listener : mOnAppInstalledListener) {
            listener.onAppInstalled(packageName);
        }
    }

    /**
     * 注册倒计时更新listener
     * @param listener
     */
    public void registerOnLeftTimeUpdateListener(OnLeftTimeUpdateListener listener) {
        mOnLeftTimeUpdateListeners.add(listener);
    }
    
    /**
     * 
     * @param listener
     */
    public void removeOnLeftTimeUpdateListener(OnLeftTimeUpdateListener listener) {
        mOnLeftTimeUpdateListeners.remove(listener);
    }
    
    /**
     * 通知倒计时更新
     * @param leftTime
     */
    public void updateLeftTime(LeftTime leftTime) {
        for(OnLeftTimeUpdateListener listener : mOnLeftTimeUpdateListeners) {
            listener.onLeftTimeUpdate(leftTime);
        }
    }
    
    /**
     * 用于倒计时
     * @author free
     *
     */
    public static interface OnLeftTimeUpdateListener {
        public void onLeftTimeUpdate(LeftTime leftTime);
    }
    
    public static class LeftTime {
        private int mRemainTime = 0;

        public void setRemainTime(int remainTime) {
            mRemainTime = remainTime;
        }
        public int getRemainTime() {
            return mRemainTime;
        }
        public int getDays() {
            return mRemainTime / (3600 * 24);
        }
        public int getHours() {
            return (mRemainTime % (3600 * 24)) / 3600;
        }
        public int getMinutes() {
            return ((mRemainTime % 3600) / 60);
        }
        public boolean decOneMinutes() {
            if (mRemainTime > 60) {
                mRemainTime -= 60;
                return true;
            } else if (mRemainTime > 0) {
                mRemainTime = 0;
                return true;
            } else {
                return false;
            }
        }
    }
}