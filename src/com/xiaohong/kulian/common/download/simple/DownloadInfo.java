package com.xiaohong.kulian.common.download.simple;

public class DownloadInfo {
    public String taskName;
    public String fileName;
    public String url;
    public String status; // "start"  "done"  "canceled"
    public Thread thread;
}
