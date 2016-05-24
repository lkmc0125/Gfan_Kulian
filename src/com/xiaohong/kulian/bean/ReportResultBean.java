package com.xiaohong.kulian.bean;

public class ReportResultBean {

    private int coin_num;
    private int added_coin;
    private int refund_coin; // 如果当天认证扣了金币，购买时间后会退还当天扣的金币
    private int remain_time;
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

    public int getRefundCoinNum() {
        return refund_coin;
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

    public int getRemainTime() {
        return remain_time;
    }

    public String toString() {
        return "ReportResultBean[coin_num=" + coin_num + ", added_coin = "
                + added_coin + ", ret_code = " + ret_code + ", ret_msg = " + ret_msg + "]";
                
    }

}