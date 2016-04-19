package com.xiaohong.kulian.bean;

import java.util.HashMap;

public class AppBean {

    private int AppId;//: 154,
    private String AppLargeLogo;//: "http://www.dspmind.com/upload/images/20160119/1453203085543641.png",
    private String AppLogo;//: "http://www.dspmind.com/upload/images/20160119/1453203079489024.png",
    private String AppName;//: "唯品会",
    private String AppSize;//: "24M",
    private String AppSource;//: "http://www.dspmind.com/upload/files/20160119/4/weipinhui_413.apk",
    private String BriefSummary;//: "唯品会，一家专门做特卖的网站！每天10点上新，全场1折起特卖。",
    private String Clickurl;//: "http://www.mobdsp.com/cb/c?k=154&m1=[M_MAC]&m2=[M_IDFA]&m3=[M_IMEI]&o=",
    private int GiveCoin;//: 90,
    private String PackageName;//: "com.achievo.vipshop"
    
    /**
     * mStatusMap不是json的一部分，而是用于存储当前应用的一些状态信息：比如是否下载等
     * KEY_PRODUCT_INFO KEY_PRODUCT_DOWNLOAD
     */
    private HashMap<String, Object> mStatusMap = new HashMap<String, Object>();

    /**
     * 标记用户是否已安装app,默认值为false
     */
    private boolean mIsInstalled = false;
    
    /**
     * 标记app是否已被我们下载，默认值为false
     */
    private boolean mIsDownloaded = false;
    
    public boolean isDownloaded() {
        return mIsDownloaded;
    }
    
    public void setDownloaded(boolean mIsDownloaded) {
        this.mIsDownloaded = mIsDownloaded;
    }
    
    /**
     * @return the isInstalled
     */
    public boolean isIsInstalled() {
        return mIsInstalled;
    }
    /**
     * @param isInstalled the isInstalled to set
     */
    public void setIsInstalled(boolean isInstalled) {
        mIsInstalled = isInstalled;
    }
    
    
    
    public HashMap<String, Object> getStatusMap() {
        return mStatusMap;
    }
    public int getAppId() {
        return AppId;
    }
    public String getAppLargeLogo() {
        return AppLargeLogo;
    }
    public String getAppLogo() {
        return AppLogo;
    }
    public String getAppName() {
        return AppName;
    }
    public String getAppSize() {
        return AppSize;
    }
    public String getAppSource() {
        return AppSource;
    }
    public String getBriefSummary() {
        return BriefSummary;
    }
    public String getClickurl() {
        return Clickurl;
    }
    public int getGiveCoin() {
        return GiveCoin;
    }
    public String getPackageName() {
        return PackageName;
    }
    

}
