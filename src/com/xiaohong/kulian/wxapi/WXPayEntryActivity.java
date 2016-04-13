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
        Log.d(TAG, "onCreate");
        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);
        api.handleIntent(getIntent(), this);

        initView();
    }

    private void initView() {
        initTopBar();
        confirmBtn = (TextView) findViewById(R.id.confirm_button);
        confirmBtn.setOnClickListener(this);
    }

    private void initTopBar() {

        TopBar.createTopBar(this, new View[] { findViewById(R.id.back_btn),
                findViewById(R.id.top_bar_title) }, new int[] { View.VISIBLE,
                View.VISIBLE }, getString(R.string.title_pay));
        ImageButton back = (ImageButton) findViewById(R.id.back_btn);
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
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

                TextView goodsNameTv = (TextView) findViewById(R.id.goods_name);
                Gson gson = new Gson();
                WeChatGoodsBean bean = gson
                        .fromJson(payResp.extData, WeChatGoodsBean.class);
                goodsNameTv.setText("购买商品: " + bean.getGoodsName());

                TextView payResultTv = (TextView) findViewById(R.id.pay_result);
                payResultTv.setText(msg);
                
                // report apy sucssess
                if (resp.errCode == 0) {
                    Session session = Session.get(this);
                    MarketAPI.reportOrderPay(this, this, bean.getGoodsId(), bean.getOutTradeNo(), session.getUserName());
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
            // Session.get(getApplicationContext()).notifyCoinUpdated(bean.getAdded_coin());
            break;
        default:
            break;
        }
    }

    @Override
    public void onError(int method, int statusCode) {
        switch (method) {
        case MarketAPI.ACTION_REPORT_ORDER_PAY:
            DialogUtils.showMessage(this, "出错啦", "支付结果同步失败，请联系客服");
            break;
        default:
            break;
        }
    }
}
