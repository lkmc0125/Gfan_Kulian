package com.xiaohong.kulian.bean;

public class ReportResultBean {

    private int coin_num;
    private int added_coin;
    private int ret_code;
    private String ret_msg;

    public void setCoinNum(int coinNum) {
        this.coin_num = coinNum;
    }
    public int getCoinNum() {
        return coin_num;
    }

    public void setAddedCoinNum(int decCoinNum) {
        this.added_coin = decCoinNum;
    }
    public int getAddedCoinNum() {
        return added_coin;
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
    
    public String toString() {
        return "ReportResultBean[coin_num=" + coin_num + ", added_coin = "
                + added_coin + ", ret_code = " + ret_code + ", ret_msg = " + ret_msg + "]";
                
    }

}