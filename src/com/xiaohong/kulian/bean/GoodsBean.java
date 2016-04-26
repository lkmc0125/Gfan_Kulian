package com.xiaohong.kulian.bean;

public class GoodsBean {
    private int goods_id;
    private String name;
    private int price; // cent
    private int gift_coin;//奖励的金币

    public void setGoodsId(Integer goodsId) {
        this.goods_id = goodsId;
    }
    public int getGoodsId() {
        return goods_id;
    }
    public void setName(String name) {
        this.name = name; 
    }
    public String getName() {
        return name;
    }
    public void setPrice(int price) {
        this.price = price;
    }
    public int getPrice() {
        return price;
    }
    public int getGiftCoin() {
        return gift_coin;
    }
    public void setGiftCoin(int gift_coin) {
        this.gift_coin = gift_coin;
    }
    
    
}
