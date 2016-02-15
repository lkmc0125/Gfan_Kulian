package com.mappn.gfan.common.vo;

import java.io.Serializable;

public class ProductDetail implements Serializable {
    private static final long serialVersionUID = 9027701566317108365L;
    private String pid;
    private String productType;
    private String name;
    private int price;
    private int payCategory;
    private int rating;
    private String iconUrl;
    private String iconUrlLdpi;
    private String shotDes;
    private int appSize;
    private String sourceType;
    private long publishTime;
    private String versionName;
    private String authorName;
    private int downloadCount;
    private int ratingCount;
    private String[] screenshot;
    private String[] screenshotLdpi;
    private String longDescription;
    private int commentsCount;
    private int versionCode;
    private String packageName;
    private String upReason;
    private long upTime;
    private String permission;
    private String mFilePath;

    public ProductDetail() {
        screenshot = new String[5];
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("ProductDetail[");
        sb.append(name);
        sb.append("]");
        return sb.toString();
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getPayCategory() {
        return payCategory;
    }

    public void setPayCategory(int payCategory) {
        this.payCategory = payCategory;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getShotDes() {
        return shotDes;
    }

    public void setShotDes(String shotDes) {
        this.shotDes = shotDes;
    }

    public int getAppSize() {
        return appSize;
    }

    public void setAppSize(int appSize) {
        this.appSize = appSize;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public long getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(long publishTime) {
        this.publishTime = publishTime;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public String[] getScreenshot() {
        return screenshot;
    }

    public void setScreenshot(String[] screenshot) {
        this.screenshot = screenshot;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getUpReason() {
        return upReason;
    }

    public void setUpReason(String upReason) {
        this.upReason = upReason;
    }

    public long getUpTime() {
        return upTime;
    }

    public void setUpTime(long upTime) {
        this.upTime = upTime;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String[] getScreenshotLdpi() {
        return screenshotLdpi;
    }

    public void setScreenshotLdpi(String[] screenshotLdpi) {
        this.screenshotLdpi = screenshotLdpi;
    }
    
    public String getIconUrlLdpi() {
        return iconUrlLdpi;
    }

    public void setIconUrlLdpi(String iconUrlLdpi) {
        this.iconUrlLdpi = iconUrlLdpi;
    }
    
    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String mFilePath) {
        this.mFilePath = mFilePath;
    }
}
