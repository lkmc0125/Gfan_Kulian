package com.xiaohong.kulian;


import aga.fdf.grd.os.EarnPointsOrderInfo;
import aga.fdf.grd.os.EarnPointsOrderList;
import aga.fdf.grd.os.PointsReceiver;
import android.content.Context;

public class YoumiPointsReceiver extends PointsReceiver {

    @Override
    protected void onEarnPoints(Context context, EarnPointsOrderList orderList) {
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
