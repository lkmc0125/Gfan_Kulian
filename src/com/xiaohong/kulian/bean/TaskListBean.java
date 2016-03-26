package com.xiaohong.kulian.bean;

import java.util.ArrayList;

/*
 * 方便Gson解析tasklist接口返回的数据
 */
public class TaskListBean {

    ArrayList<TaskBean> tasklist;

    public ArrayList<TaskBean> getTasklist() {
        return tasklist;
    }

}
