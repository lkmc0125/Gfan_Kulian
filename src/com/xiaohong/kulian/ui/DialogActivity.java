package com.xiaohong.kulian.ui;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.common.util.DialogUtils;
import com.xiaohong.kulian.common.widget.CustomDialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * 用于显示Dialog - 之前DialogUtils里的方法需要一个Activity context
 * @author free
 *
 */
public class DialogActivity extends Activity {
    private static final String TAG = "DialogActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String title = intent.getStringExtra(DialogUtils.KEY_DIALOG_TITLE);
        String message = intent.getStringExtra(DialogUtils.KEY_DIALOG_MESSAGE);
        Log.d(TAG, "title = " + title + ", message = " + message);
        showMessage(this, title, message);
    }

    private void showMessage(Context context, String title, String message) {
        CustomDialog dialog = new CustomDialog.Builder(context).setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        finish();
                    }
                }).create();
        dialog.show();
    }

}
