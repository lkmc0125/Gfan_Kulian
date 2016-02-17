package com.mappn.gfan.common.util;

import org.apache.http.HttpResponse;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.gfan.sdk.statistics.Collector;
import com.gfan.sdk.statistics.Collector.IResponse;
import com.mappn.gfan.R;
import com.mappn.gfan.common.util.DialogUtil.InputDialogListener;
import com.mappn.gfan.ui.ConnectionActivity;
import com.mappn.gfan.ui.FileManagerActivity;

public class MenuUtil {
	// Menu dialog
	public static final int DIALOG_RESPONSE = 200;

	public static Dialog createResponseDialog(final Context context, int id) {
		return DialogUtil.createBigInputDialog(context, id, 
				R.string.title_response, new InputDialogListener() {

			@Override
			public void onInputDialogOK(int id, String value) {
				String content = context.getClass().getName() + ":" + value;
				if (!TextUtils.isEmpty(value)) {
					Collector.comment(context, content, new IResponse() {

						@Override
						public void onSuccess(HttpResponse arg0) {
							Utils.makeEventToast(context, 
									context.getString(R.string.thanks_response), false);
						}

						@Override
						public void onFailed(Exception arg0) {
							Utils.makeEventToast(context, 
									context.getString(R.string.thanks_response), false);
						}
					});
				} else {
					Utils.makeEventToast(context, 
							context.getString(R.string.content_no_empty), false);
				}
			}

			@Override
			public void onInputDialogCancel(int id) {

			}
		});
	}

    public static void onMenuSelectedResponse(Context context) {
        final Activity act = (Activity) context;
        if (!act.isFinishing()) {
        	//判断当前网络是否可用
            if (Utils.isNetworkAvailable(context)) {
                act.showDialog(MenuUtil.DIALOG_RESPONSE);
            } else {
                Utils.makeEventToast(context, context.getString(R.string.warning_netword_error),
                        false);
            }
        }
    }

	public static void onMenuSelectedDownload(Context context) {
		final Activity act = (Activity) context;
		Intent intent = new Intent(act, FileManagerActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		act.startActivity(intent);
	}

	public static void onMenuSelectedHome(Context context) {
		final Activity act = (Activity) context;
		Intent intent = new Intent(act, ConnectionActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		act.startActivity(intent);
	}
}
