package com.xiaohong.kulian.wxapi;

import com.google.gson.Gson;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelpay.PayResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Session;
import com.xiaohong.kulian.bean.ReportResultBean;
import com.xiaohong.kulian.bean.WeChatGoodsBean;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.util.DialogUtils;
import com.xiaohong.kulian.common.util.TopBar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler,
        OnClickListener, ApiRequestListener {

    private static final String TAG = "MicroMsg.SDKSample.WXPayEntryActivity";

    private IWXAPI api;
    private TextView confirmBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_result);
        initView();
        Log.d(TAG, "onCreate");
        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);
        api.handleIntent(getIntent(), this);
    }

    private void initView() {
        initTopBar();
        confirmBtn = (TextView) findViewById(R.id.confirm_button);
        confirmBtn.setOnClickListener(this);
        confirmBtn.setEnabled(false);
    }

    private void initTopBar() {

        TopBar.createTopBar(this, new View[] { findViewById(R.id.back_btn),
                findViewById(R.id.top_bar_title) }, new int[] { View.INVISIBLE,
                View.VISIBLE }, getString(R.string.title_pay));
//        ImageButton back = (ImageButton) findViewById(R.id.back_btn);
//        back.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                finish();
//            }
//        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
        Log.d(TAG, "onReq: " + req);
    }

    @Override
    public void onResp(BaseResp resp) {
        Log.d(TAG, "onPayFinish, errCode = " + resp.errCode);
        if (resp.getClass() == PayResp.class) {
            PayResp payResp = (PayResp) resp;
            if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
                String msg = "";
                switch (resp.errCode) {
                case 0:
                    msg = "支付成功";
                    break;
                case -1:
                    msg = "支付失败";
                    break;
                case -2:
                    msg = "用户取消支付";
                    break;
                default:
                    break;
                }

                WeChatGoodsBean bean = new Gson().fromJson(payResp.extData, WeChatGoodsBean.class);

                TextView tradeNoTv = (TextView) findViewById(R.id.trade_no);
                tradeNoTv.setText("订单号："+bean.getOutTradeNo());

                TextView goodsNameTv = (TextView) findViewById(R.id.goods_name);                
                goodsNameTv.setText("购买商品：" + bean.getGoodsName());

                TextView payResultTv = (TextView) findViewById(R.id.pay_result);
                payResultTv.setText(msg);

                // report pay success
                if (resp.errCode == 0) {
                    Session session = Session.get(this);
                    if(bean.getOther_account() != null && !bean.getOther_account().equals("")) {
                        //this case is that buy coin for other
                        MarketAPI.reportOrderPay(getApplicationContext(), this, bean.getGoodsId(), 
                                bean.getOutTradeNo(), bean.getOther_account());
                    }else {
                        //this case is that buy coin for self
                        MarketAPI.reportOrderPay(getApplicationContext(), this, bean.getGoodsId(), 
                                bean.getOutTradeNo(), session.getUserName());
                    }
                    findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
                } else {
                    confirmBtn.setEnabled(true);
                }
            }
        } else {
            DialogUtils.showMessage(this, "出错啦", "支付结果异常");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.confirm_button:
            finish();
            break;
        default:
            break;
        }
    }

    @Override
    public void onSuccess(int method, Object obj) {
        switch (method) {
        case MarketAPI.ACTION_REPORT_ORDER_PAY:
            ReportResultBean bean = (ReportResultBean)obj;
            if (bean.getRetCode() == 0) {
                DialogUtils.showMessage(this, "购买成功", "您获得了"+String.valueOf(bean.getAddedCoinNum())+"个金币");
                Session session = Session.get(getApplicationContext()); 
                session.setRemainTime(bean.getRemainTime());
                session.notifyCoinUpdated(bean.getAddedCoinNum());
            }
            confirmBtn.setEnabled(true);
            findViewById(R.id.progressbar).setVisibility(View.GONE);
            break;
        default:
            break;
        }
    }

    @Override
    public void onError(int method, int statusCode) {
        switch (method) {
        case MarketAPI.ACTION_REPORT_ORDER_PAY:
            DialogUtils.showMessage(this, "出错啦", "支付结果同步失败，错误码"+String.valueOf(statusCode)+"，请联系客服");
            confirmBtn.setEnabled(true);
            findViewById(R.id.progressbar).setVisibility(View.GONE);
            break;
        default:
            break;
        }
    }
}
