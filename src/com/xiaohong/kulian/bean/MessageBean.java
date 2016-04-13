package com.xiaohong.kulian.bean;

public class MessageBean {
    private String broadcast_item;
    private String click_url;
    
    public String getMessageText() {
        return broadcast_item.replace("\n", "");
    }
    public String getClickUrl() {
        return click_url;
    }
}
