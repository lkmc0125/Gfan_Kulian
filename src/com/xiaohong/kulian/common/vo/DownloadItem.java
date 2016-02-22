package com.xiaohong.kulian.common.vo;

public class DownloadItem {
	public String pId;
	public String packageName;
	public String url;
	public String fileMD5;
	public int sourceType;

	@Override
	public String toString() {
		String temp = pId + " " + url + " " + fileMD5;
		return temp;
	}
}
