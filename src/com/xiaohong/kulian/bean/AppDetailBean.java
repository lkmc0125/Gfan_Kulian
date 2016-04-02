package com.xiaohong.kulian.bean;

public class AppDetailBean {

    @JsonProperty("detail_info")
    private DetailInfo detail_info;
    @JsonProperty("ret_code")
    private int retCode;
    @JsonProperty("ret_msg")
    private String retMsg;
    public void setDetailInfo(DetailInfo detailInfo) {
         this.detail_info = detailInfo;
     }
     public DetailInfo getDetailInfo() {
         return detail_info;
     }

    public void setRetCode(int retCode) {
         this.retCode = retCode;
     }
     public int getRetCode() {
         return retCode;
     }

    public void setRetMsg(String retMsg) {
         this.retMsg = retMsg;
     }
     public String getRetMsg() {
         return retMsg;
     }

}