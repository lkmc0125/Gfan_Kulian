package com.mappn.gfan.common.vo;

import java.io.Serializable;

public class SplashInfo implements Serializable{
	
    /**  Serializable ID  */
    private static final long serialVersionUID = 4970809950944283716L;

    /** API FIELD[URL] */
    public static final String URL = "url";
    
    /** API FIELD[TIME] */
    public static final String TIMESTAMP = "time";
    
    /**
     * Splash 更新时间戳
     */
    public long timestamp;
	
    /**
     * Splash Image 下载链接
     */
    public String url;

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SplashInfo [" + "timestamp : " + timestamp + " url : " + url + "]";
    }
}