package com.xiaohong.kulian.bean;

public class ReportResultBean {

    private int coin_num;
    private int dec_coin_num;
    private int ret_code;
    private String ret_msg;

    public void setCoinNum(int coinNum) {
        this.coin_num = coinNum;
    }
    public int getCoinNum() {
        return coin_num;
    }

    public void setDecCoinNum(int decCoinNum) {
        this.dec_coin_num = decCoinNum;
    }
    public int getDecCoinNum() {
        return dec_coin_num;
    }

    public void setRetCode(int retCode) {
        this.ret_code = retCode;
    }
    public int getRetCode() {
        return ret_code;
    }

    public void setRetMsg(String retMsg) {
        this.ret_msg = retMsg;
    }
    public String getRetMsg() {
        return ret_msg;
    }

}