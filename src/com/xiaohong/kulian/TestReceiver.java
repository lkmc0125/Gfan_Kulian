package com.xiaohong.kulian;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class TestReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "收到一条消息", Toast.LENGTH_SHORT).show();
        Session.get(context).requestAddCoin(10);
    }

}
