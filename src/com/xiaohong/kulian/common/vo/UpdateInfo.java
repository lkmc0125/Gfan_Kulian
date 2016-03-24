package com.xiaohong.kulian.common.vo;

public class UpdateInfo {
	int updageLevel;
	int versionCode;
	boolean force;
	String versionName;
	String description;
	String apkUrl;

	public int getUpdageLevel() {
		return updageLevel;
	}

	public void setUpdageLevel(int updageLevel) {
		this.updageLevel = updageLevel;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public void setForce(boolean force) {
	    this.force = force;
	}

	public boolean getForce() {
	    return force;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getApkUrl() {
		return apkUrl;
	}

	public void setApkUrl(String apkUrl) {
		this.apkUrl = apkUrl;
	}
}
