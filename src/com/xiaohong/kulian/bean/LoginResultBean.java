package com.xiaohong.kulian.bean;

public class LoginResultBean {

//        "coin_num" : 5132,
//        "invite_code" : "523851",
//        "is_sign" : "false",
//        "remain_time" : 0,
//        "ret_code" : 0,
//        "ret_msg" : "success",
//        "show_countdown" : 0,
//        "token" : "NRwYmsPZwnARFJNjCuHjXew7+P+0MBOFRRzlqu2ap3JnMDFyC88lJdlz0EZ+nbpLfFNFfA3xf/c="

    private int coin_num;
    private int ret_code;
    private String ret_msg;
    private String invite_code;
    private int remain_time;  // 购买的上网时长的剩余时间
    private String token;
    private String show_countdown;
    private String is_sign; // 今天是否已签到
    private String phone_number;
    
    public int getCoinNum() {
        return coin_num;
    }

    public int getRetCode() {
        return ret_code;
    }

    public String getRetMsg() {
        return ret_msg;
    }

    public String getPhoneNumber() {
        return phone_number;
    }

    public String getInviteCode() {
        return invite_code;
    }

    public int getRemainTime() {
        return remain_time;  // 购买的上网时长的剩余时间
    }

    public String getToken() {
        return token;
    }

    public boolean getShowCountdown() {
        return (show_countdown != null && show_countdown.equals("true"));
    }

    public boolean getIsSign() {
        return (is_sign != null && is_sign.equals("true")); // 今天是否已签到
    }
}