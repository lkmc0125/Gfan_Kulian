package com.xiaohong.kulian.bean;

public class WeChatGoodsBean {

    public String getGoodsName() {
        return goods_name;
    }

    public int getGoodsId() {
        return goods_id;
    }

    public long getOutTradeNo() {
        return out_trade_no;
    }
    
    

    public void setGoods_name(String goods_name) {
        this.goods_name = goods_name;
    }

    public void setGoods_id(int goods_id) {
        this.goods_id = goods_id;
    }

    public void setOut_trade_no(long out_trade_no) {
        this.out_trade_no = out_trade_no;
    }



    private String goods_name;
    private int goods_id;
    private long out_trade_no;
}
