package com.xiaohong.kulian.bean;

public class GoodsBean {
    private Integer goods_id;
    private String name;
    private Integer price; // cent

    public void setGoodsId(Integer goodsId) {
        this.goods_id = goodsId;
    }
    public Integer getGoodsId() {
        return goods_id;
    }
    public void setName(String name) {
        this.name = name; 
    }
    public String getName() {
        return name;
    }
    public void setPrice(Integer price) {
        this.price = price;
    }
    public Integer getPrice() {
        return price;
    }
}
