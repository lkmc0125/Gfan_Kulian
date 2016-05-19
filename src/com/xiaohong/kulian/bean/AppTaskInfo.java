package com.xiaohong.kulian.bean;

public class AppTaskInfo {
    private int task_id;
    private String task_name;
    private int coin_num;
    private int task_status;  // 1 - 未开始 2 - 任务进行中 3 - 任务已完成 4 - 任务已过时
    
    public int getTaskId() {
        return task_id;
    }
    
    public String getTaskName() {
        return task_name;
    }
    
    public int getCoinNum() {
        return coin_num;
    }
    
    public int getTaskStatus() {
        return task_status;
    }
}
