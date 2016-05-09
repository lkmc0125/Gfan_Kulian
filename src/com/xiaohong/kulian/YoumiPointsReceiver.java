package com.xiaohong.kulian;


import aga.fdf.grd.os.EarnPointsOrderInfo;
import aga.fdf.grd.os.EarnPointsOrderList;
import aga.fdf.grd.os.PointsReceiver;
import android.content.Context;
import android.util.Log;

public class YoumiPointsReceiver extends PointsReceiver {
    private final static String TAG = "YoumiPointsReceiver";

    @Override
    protected void onEarnPoints(Context context, EarnPointsOrderList orderList) {
        Log.d(TAG, "onEarnPoints");
        int coinNum = 0;
        for (int i = 0; i < orderList.size(); i++) {
            EarnPointsOrderInfo order = orderList.get(i);
            coinNum += order.getPoints();
        }
        if (coinNum > 0) {
            Session.get(context).requestAddCoin(coinNum);
        }
    }

    @Override
    protected void onViewPoints(Context arg0) {
        // TODO Auto-generated method stub

    }

}

