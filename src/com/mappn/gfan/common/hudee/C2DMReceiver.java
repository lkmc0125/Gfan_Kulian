package com.mappn.gfan.common.hudee;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.mappn.gfan.R;
import com.mappn.gfan.Session;
import com.mappn.gfan.common.download.DownloadManager;
import com.mappn.gfan.common.util.Utils;

public class C2DMReceiver extends BroadcastReceiver {
	public final static String APP_ID = "e28ccddf8b2048a0a06199e197e61efc";
	public final static String TYPE_APK = "apk";
	public final static String TYPE_URL = "url";
	public final static String TYPE_STRING = "string";
	
	@Override
	public void onReceive(Context ctx, Intent intent) {

        Utils.D(HudeeUtils.HUDEE_APP_ID + " receive intent: " + intent);
        if (Utils.sDebug) {
            HudeeUtils.writeLogToFile("[" + C2DMReceiver.class.getName() + "]"
                    + " receive intent: " + intent);
        }
		String action = intent.getAction();
		if (HudeeUtils.BIND_RESULT.equals(action)) {
			String error = intent.getStringExtra("error");
			String removed = intent.getStringExtra("unregistered");
			String devId = intent.getStringExtra("registration_id");
			if (removed != null) {
				// unregistered
				// should clear locally stored devId
			} else if (error != null) {
				// registration error occured
				// should retry later
			} else {
				// registration success
				// should store devId locally
				Utils.D(HudeeUtils.HUDEE_APP_ID + " get dev id:" + devId);
                if (Utils.sDebug) {
                    HudeeUtils.writeLogToFile("[" + C2DMReceiver.class.getName() + "]"
                            + " get dev id:" + devId);
                }
			}
		} else if ("com.hudee.pns.intent.MESSAGE".equals(action)) {
			String msg = Utils.getUTF8String(intent.getByteArrayExtra("msg"));
            Utils.D(HudeeUtils.HUDEE_APP_ID + " get msg:" + msg);
            if (Utils.sDebug) {
                HudeeUtils.writeLogToFile("[" + C2DMReceiver.class.getName() + "]" + " get msg:"
                        + msg);
            }
            HudeeUtils.acquireScreenOn(ctx);
			parserMsg(ctx, msg);
		}
	}

    private void parserMsg(Context ctx, String msg) {
        JSONObject json = null;
        String type = null;
        String url = null;
        String id = null;
        String name = null;
        try {
            json = new JSONObject(new JSONTokener(msg));
            type = json.getString("ctype");
            url = json.getString("content");
            id = json.getString("aid");
            name = json.getString("filename");
        } catch (JSONException e) {
            Utils.W("have JSONException when parse hudee message: " + msg, e);
        }
        if (HudeeUtils.TYPE.APK.equals(HudeeUtils.getLPNSType(type))) {
            startDownloadTask(ctx, type, id, name, url);
        } else if (HudeeUtils.TYPE.IMG.equals(HudeeUtils.getLPNSType(type))) {
            startBrowseImage(ctx, url);
        }
    }

	private void startDownloadTask(Context ctx, String type, String id, String name, String url) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setPackageName(id);
        request.setTitle(name);
        request.setIconUrl(String.valueOf(R.drawable.person_center_cloud));
        request.setSourceType(com.mappn.gfan.common.download.Constants.DOWNLOAD_FROM_CLOUD);
        Session.get(ctx).getDownloadManager().enqueue(request);
        Utils.makeEventToast(ctx, ctx.getString(R.string.get_push_msg),
                true);
	}
	
    private void startBrowseImage(Context ctx, String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setData(Uri.parse(url));
        ctx.startActivity(i);
    }
}
