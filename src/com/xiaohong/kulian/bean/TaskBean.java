package com.xiaohong.kulian.bean;

import android.os.Parcel;
import android.os.Parcelable;

/*
 * 方便Gson解析tasklist接口返回的数据
 */
public class TaskBean implements Parcelable {
    /**
     * 标记当前对象表示一个app
     */
    public static int ITEM_TYPE_APP = 1;
    /**
     * 标记当前对象表示一个web任务
     */
    public static int ITEM_TYPE_TASK = 2;
    /**
     * 标记当前对象表示一个web任务
     */
    public static int ITEM_TYPE_GZHTASK = 3;
    
    public enum TaskType {
        APP_TASK, // 下载app任务
        WEB_TASK, // web页任务
        WX_TASK   // 微信公众号任务
    }

    /**
     * 用于区分是公众号任务还是普通任务
     */
    private int mTaskType = 1;

    public int getTaskType() {
        return mTaskType;
    }
    public void setTaskType(int mTaskType) {
        this.mTaskType = mTaskType;
    }
    /**
     * 可领取
     */
    public static int STATUS_AVAILABLE = 1;
    /**
     * 已领取
     */
    public static int STATUS_GET = 2;
    /**
     * 已完成
     */
    public static int STATUS_DONE = 3;
    /**
     * 超时（领取但未完成）
     */
    public static int STATUS_TIMEOUT_WIHT_GET = 4;
    /**
     * 任务已结束（未领取）
     */
    public static int STATUS_TIMEOUT_WIHTOUT_GET = 5;

    private String click_url;// "http://120.193.39.115:8010/1/?"
    private int coin_num;// 300 金币
    private String desc;// "注册唯享客狂赚金币",
    private String logo_url;// "http://www.dspmind.com/upload/images/20160221/1456053750421216.png",
    private String name;// : "注册唯享客狂赚金币",
    private int task_id;// 140

    /**
     * 公众号任务特有的属性
     */
    private int id;// : 1,
    private String qr_code_url;// :
                               // "http://www.dspmind.com/upload/images/20160120/kuliancode.png ",
    private int remain_tasknum;// 694,
    private int remain_time;// 0,
    /**
     * 1 可领取 2 已领取 3 已完成 4 超时（领取但未完成） 5任务已结束（未领取）
     */
    private int task_status;// 1,
    private String weixin_id;// "wifikulian"

    public String getClick_url() {
        return click_url;
    }
    public int getCoin_num() {
        return coin_num;
    }
    public String getDesc() {
        return desc;
    }
    public String getLogo_url() {
        return logo_url;
    }
    public String getName() {
        return name;
    }
    public int getTask_id() {
        return task_id;
    }
    public int getId() {
        return id;
    }
    public String getQr_code_url() {
        return qr_code_url;
    }
    public void setRemain_tasknum(int remain_tasknum) {
        this.remain_tasknum = remain_tasknum;
    }
    public int getRemain_tasknum() {
        return remain_tasknum;
    }
    public int getRemain_time() {
        return remain_time;
    }
    public int getTask_status() {
        return task_status;
    }
    public String getWeixin_id() {
        return weixin_id;
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mTaskType);
        dest.writeInt(coin_num);
        dest.writeInt(task_id);
        dest.writeInt(id);
        dest.writeInt(remain_tasknum);
        dest.writeInt(remain_time);
        dest.writeInt(task_status);
        dest.writeString(click_url);
        dest.writeString(desc);
        dest.writeString(logo_url);
        dest.writeString(name);
        dest.writeString(qr_code_url);
        dest.writeString(weixin_id);
    }

    public static final Parcelable.Creator<TaskBean> CREATOR = new Parcelable.Creator<TaskBean>() {

        @Override
        public TaskBean createFromParcel(Parcel source) {
            TaskBean bean = new TaskBean();
            bean.mTaskType = source.readInt();
            bean.coin_num = source.readInt();
            bean.task_id = source.readInt();
            bean.id = source.readInt();
            bean.remain_tasknum = source.readInt();
            bean.remain_time = source.readInt();
            bean.task_status = source.readInt();
            bean.click_url = source.readString();
            bean.desc = source.readString();
            bean.logo_url = source.readString();
            bean.name = source.readString();
            bean.qr_code_url = source.readString();
            bean.weixin_id = source.readString();
            return bean;
        }

        @Override
        public TaskBean[] newArray(int size) {
            return new TaskBean[size];
        }

    };

}
