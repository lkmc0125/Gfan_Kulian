package com.xiaohong.kulian.bean;

/*
 * 方便Gson解析tasklist接口返回的数据
 */
public class TaskBean {
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

    private String click_url;//"http://120.193.39.115:8010/1/?"
    private int coin_num;//300  金币
    private String desc;//"注册唯享客狂赚金币",
    private String logo_url;// "http://www.dspmind.com/upload/images/20160221/1456053750421216.png",
    private String name;//: "注册唯享客狂赚金币",
    private int task_id;//140
    
    /**
     * 公众号任务特有的属性
     */
    private int id;//: 1,
    private String qr_code_url;//: "http://www.dspmind.com/upload/images/20160120/kuliancode.png ",
    private int remain_tasknum;// 694,
    private int remain_time;// 0,
    /**
     * 1 可领取    2 已领取    3 已完成   4 超时（领取但未完成）   5任务已结束（未领取）
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
    
    

}
