package com.xiaohong.kulian.bean;

public class GoodsBean {
    private Integer goods_id;
    private Integer coin_num;
    private Integer money; // cent

    public void setGoodsId(Integer goodsId) {
        this.goods_id = goodsId;
    }
    public Integer getGoodsId() {
        return goods_id;
    }
//    public void setName(String name) {
//        this.name = name; 
//    }
//    public String getName() {
//        return name;
//    }
//    public void setPrice(Integer price) {
//        this.price = price;
//    }
//    public Integer getPrice() {
//        return price;
//    }
    public void setCoinNum(Integer coinNum) {
        this.coin_num = coinNum;
    }
    public Integer getCoinNum() {
        return coin_num;
    }
    public void setMoney(Integer money) {
        this.money = money;
    }
    public Integer getMoney() {
        return money;
    }
}
