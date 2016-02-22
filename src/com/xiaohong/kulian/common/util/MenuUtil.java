package com.xiaohong.kulian.common.util;

import org.apache.http.HttpResponse;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.common.util.DialogUtil.InputDialogListener;
import com.xiaohong.kulian.ui.ConnectionActivity;
import com.xiaohong.kulian.ui.FileManagerActivity;

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
