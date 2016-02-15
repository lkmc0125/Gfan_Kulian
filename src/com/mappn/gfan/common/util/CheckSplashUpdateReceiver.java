package com.mappn.gfan.common.util;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.mappn.gfan.Session;
import com.mappn.gfan.common.ApiAsyncTask.ApiRequestListener;
import com.mappn.gfan.common.MarketAPI;
import com.mappn.gfan.common.download.Constants;
import com.mappn.gfan.common.download.DownloadManager;
import com.mappn.gfan.common.vo.SplashInfo;

public class CheckSplashUpdateReceiver extends BroadcastReceiver implements ApiRequestListener {

	private Context mContext;

	@Override
	public void onReceive(Context context, Intent intent) {
	    mContext = context;
		MarketAPI.checkNewSplash(context, this);
	}

	@Override
    public void onSuccess(int method, Object obj) {
        SplashInfo upgrades = (SplashInfo) obj;

        Session session = Session.get(mContext);

        if (upgrades != null && !TextUtils.isEmpty(upgrades.url)) {
            File oldSplash = new File(mContext.getCacheDir(), "splash.png");
            if (oldSplash.exists()) {
                oldSplash.delete();
            }

            DownloadManager dm = session.getDownloadManager();
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(upgrades.url));
            request.setMimeType(Constants.MIMETYPE_IMAGE);
            request.setDestination(DownloadManager.Impl.DESTINATION_CACHE_PARTITION);
            request.setShowRunningNotification(false);
            request.setTitle("splash.png");
            long id = dm.enqueue(request);

            session.setSplashId(id);
            session.setSplashTime(upgrades.timestamp);
        }
    }

	@Override
	public void onError(int method, int statusCode) {
	    Utils.D("market error when check upgrade info " + statusCode);
	}
}