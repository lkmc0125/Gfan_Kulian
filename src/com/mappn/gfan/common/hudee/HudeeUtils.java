package com.mappn.gfan.common.hudee;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.mappn.gfan.Constants;
import com.mappn.gfan.common.util.Utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

public class HudeeUtils {
    
	private static final String TAG = "HudeeUtils";

	/** 蝴蝶系统分配的APP ID */
    static final String HUDEE_APP_ID = "e28ccddf8b2048a0a06199e197e61efc";
    public static final String BIND_ACCOUNT = "com.hudee.pns.intent.REGISTER";
	public static final String UNBIND_ACCOUNT = "com.hudee.pns.intent.UNREGISTER";
	public static final String BIND_RESULT = "com.hudee.pns.intent.REGISTRATION";
	
    /**
     * 获取推送文件的类型
     */
	public static TYPE getLPNSType(String type) {
		if ("jpg".equalsIgnoreCase(type) 
				|| "png".equalsIgnoreCase(type) 
				|| "jpeg".equalsIgnoreCase(type) 
				|| "gif".equalsIgnoreCase(type)) {
			return TYPE.IMG;
		} else if ("apk".equalsIgnoreCase(type)) {
			return TYPE.APK;
		} else if ("url".equalsIgnoreCase(type)) {
			return TYPE.URL;
		} else if ("msg_authority".equalsIgnoreCase(type)) {
			return TYPE.MSG_AUTHORITY;
		} else {
			return TYPE.OTHER;
		}
	}

	static enum TYPE {
		URL, APK, IMG, OTHER, MSG_AUTHORITY
	}

	public static void writeLogToFile(String content) {
		Date date = new Date();
		date.setTime(System.currentTimeMillis());
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String result = "[" + format.format(date) + "]" + "[from Gfan]" + content;
		File f = new File(Environment.getExternalStorageDirectory(), "lpns.log");
		try {
			if (f.exists()) {
				Log.v(TAG, "file exists");
			} else {
				Log.v(TAG, "file dosen't exist, creating...");
				if (f.createNewFile()) {
					Log.v(TAG, "file create success!");
				} else {
					Log.v(TAG, "file create failed!");
				}
			}
			PrintWriter log = new PrintWriter(new FileWriter(f, true), true);
			log.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 绑定蝴蝶服务
	 */
	public static void registerLPNS(Context context, String deviceId) {
	    Utils.trackEvent(context, Constants.GROUP_11,
                Constants.OPEN_PUSH);
		Intent registrationIntent = new Intent(BIND_ACCOUNT);
		registrationIntent.putExtra("app", PendingIntent.getBroadcast(context, 0, new Intent(), 0)); // boilerplate
		registrationIntent.putExtra("appId", HUDEE_APP_ID);
		String devId = TextUtils.isEmpty(deviceId) ? null : deviceId;
		registrationIntent.putExtra("registration_id", devId);
		context.startService(registrationIntent);
	}
	
	/**
     * 解除绑定蝴蝶服务
     */
	public static void unregisterLPNS(Context context, String devID) {
	    Utils.trackEvent(context, Constants.GROUP_11,
                Constants.CLOSE_PUSH);
		Intent unregIntent = new Intent(UNBIND_ACCOUNT);
		unregIntent.putExtra("app", PendingIntent.getBroadcast(context, 0, new Intent(), 0));
		unregIntent.putExtra("appId", HUDEE_APP_ID);
		unregIntent.putExtra("registration_id", devID);
		context.startService(unregIntent);
	}

	public static void acquireScreenOn(Context context) {
		PowerManager mPM = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock mWakeLock = mPM.newWakeLock(PowerManager.FULL_WAKE_LOCK 
				| PowerManager.ACQUIRE_CAUSES_WAKEUP, "LPNS");
		mWakeLock.setReferenceCounted(false);
		mWakeLock.acquire();
		mWakeLock.release();
	}
}
