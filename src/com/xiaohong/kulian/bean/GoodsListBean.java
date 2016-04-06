package com.xiaohong.kulian.bean;

import java.util.ArrayList;

public class GoodsListBean {

    private ArrayList<GoodsBean> goods_list;
    private Integer ret_code;
    private String ret_msg;

    public ArrayList<GoodsBean> getGoodsList() {
        return goods_list;
    }
    
    public Integer getRetCode() {
        return ret_code;
    }
    
    public String getRegMsg() {
        return ret_msg;
    }
}
