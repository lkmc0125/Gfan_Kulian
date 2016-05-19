package com.xiaohong.kulian.bean;

import java.util.ArrayList;
import java.util.List;

public class DetailInfo {

    @JsonProperty("AppCategory")
    private int AppCategory;
    @JsonProperty("AppId")
    private int AppId;
    @JsonProperty("AppLogo")
    private String AppLogo;
    @JsonProperty("AppName")
    private String AppName;
    @JsonProperty("AppPrice")
    private String AppPrice;
    @JsonProperty("AppSize")
    private String AppSize;
    @JsonProperty("AppSource")
    private String AppSource;
    @JsonProperty("AppSummary")
    private String AppSummary;
    @JsonProperty("AppVersion")
    private String AppVersion;
    @JsonProperty("BriefSummary")
    private String BriefSummary;
    @JsonProperty("ImageCount")
    private int ImageCount;
    @JsonProperty("ImageIsVertical")
    private int ImageIsVertical;
    @JsonProperty("ImageSrcList")
    private List<String> ImageSrcList;
    @JsonProperty("PackageName")
    private String PackageName;

    private ArrayList<AppTaskInfo> task_list;

    public void setAppcategory(int appcategory) {
        this.AppCategory = appcategory;
    }

    public int getAppcategory() {
        return AppCategory;
    }

    public void setAppid(int appid) {
        this.AppId = appid;
    }

    public int getAppid() {
        return AppId;
    }

    public void setApplogo(String applogo) {
        this.AppLogo = applogo;
    }

    public String getApplogo() {
        return AppLogo;
    }

    public void setAppname(String appname) {
        this.AppName = appname;
    }

    public String getAppname() {
        return AppName;
    }

    public void setAppprice(String appprice) {
        this.AppPrice = appprice;
    }

    public String getAppprice() {
        return AppPrice;
    }

    public void setAppsize(String appsize) {
        this.AppSize = appsize;
    }

    public String getAppsize() {
        return AppSize;
    }

    public void setAppsource(String appsource) {
        this.AppSource = appsource;
    }

    public String getAppsource() {
        return AppSource;
    }

    public void setAppsummary(String appsummary) {
        this.AppSummary = appsummary;
    }

    public String getAppsummary() {
        return AppSummary;
    }

    public void setAppversion(String appversion) {
        this.AppVersion = appversion;
    }

    public String getAppversion() {
        return AppVersion;
    }

    public void setBriefsummary(String briefsummary) {
        this.BriefSummary = briefsummary;
    }

    public String getBriefsummary() {
        return BriefSummary;
    }

    public void setImagecount(int imagecount) {
        this.ImageCount = imagecount;
    }

    public int getImagecount() {
        return ImageCount;
    }

    public void setImageisvertical(int imageisvertical) {
        this.ImageIsVertical = imageisvertical;
    }

    public int getImageisvertical() {
        return ImageIsVertical;
    }

    public void setImagesrclist(List<String> imagesrclist) {
        this.ImageSrcList = imagesrclist;
    }

    public List<String> getImagesrclist() {
        return ImageSrcList;
    }

    public void setPackagename(String packagename) {
        this.PackageName = packagename;
    }

    public String getPackagename() {
        return PackageName;
    }

    public ArrayList<AppTaskInfo> getTaskList() {
        return task_list;
    }
}
