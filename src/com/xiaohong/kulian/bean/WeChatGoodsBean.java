package com.xiaohong.kulian.bean;

public class WeChatGoodsBean {

    public String getGoodsName() {
        return goods_name;
    }

    public int getGoodsId() {
        return goods_id;
    }

    public String getOutTradeNo() {
        return out_trade_no;
    }
    
    

    public void setGoods_name(String goods_name) {
        this.goods_name = goods_name;
    }

    public void setGoods_id(int goods_id) {
        this.goods_id = goods_id;
    }

    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }
    

    public String getOther_account() {
        return other_account;
    }

    public void setOther_account(String other_account) {
        this.other_account = other_account;
    }


    private int    goods_id;
    private String goods_name;
    private String out_trade_no;
    private String other_account;
}
