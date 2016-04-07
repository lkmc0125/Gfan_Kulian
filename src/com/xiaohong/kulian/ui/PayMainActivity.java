package com.xiaohong.kulian.ui;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.bean.GoodsListBean;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.util.DialogUtils;
import com.xiaohong.kulian.common.util.TopBar;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.widget.BaseActivity;

public class PayMainActivity extends BaseActivity implements OnClickListener, ApiRequestListener {
    private IWXAPI mWxApi;
    private static final String TAG = "PayMainActivity";
    private Button mPayBtn;
    private EditText mCoinNumEt;
    private GoodsListBean mGoodsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_main);
        // initTopBar();
        initData();
        initViews();
    }

    private void initViews() {
        mPayBtn = (Button) findViewById(R.id.appay_btn);
        mCoinNumEt = (EditText) findViewById(R.id.coin_num_et);
        mPayBtn.setOnClickListener(this);
    }

    private void initData() {
        mWxApi = WXAPIFactory.createWXAPI(this, Constants.APP_ID, false);
        mWxApi.registerApp(Constants.APP_ID);

        boolean isPaySupported = mWxApi.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
        Toast.makeText(PayMainActivity.this, String.valueOf(isPaySupported), Toast.LENGTH_SHORT).show();

        getGoodsList();
    }

    void initTopBar() {
        TopBar.createTopBar(this, new View[] { findViewById(R.id.back_btn), findViewById(R.id.top_bar_title) },
                new int[] { View.VISIBLE, View.VISIBLE }, getString(R.string.feedback_title));
        ImageButton back = (ImageButton) findViewById(R.id.back_btn);
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
    }

    private void getGoodsList() {
        MarketAPI.getGoodsList(getApplicationContext(), this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
        case R.id.appay_btn:
            if (mCoinNumEt.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(), "亲，请输入要购买的金币数目", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "no input data");
                return;
            } else {
                doPay();
            }
            break;
        }

    }

    private void doPay() {
        final String url = "http://115.159.76.147:8390/cb/getprepayid?phone_number=13418680969&type=1&goods_id=1";
        Toast.makeText(PayMainActivity.this, "请稍候...", Toast.LENGTH_SHORT).show();
        new AsyncTask<Void, Void, Void>() {

            protected void onPreExecute() {
                mPayBtn.setEnabled(false);
            }

            @Override
            protected Void doInBackground(Void... params) {
                String ret = Utils.httpGet(url);
                if (ret != null) {
                    Log.e(TAG, "get server pay params:" + ret);
                    JSONObject json = null;
                    try {
                        json = new JSONObject(ret);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    try {
                        if (null != json && json.has("ret_code") && json.getInt("ret_code") == 0) {
                            PayReq req = new PayReq();

                            req.appId = json.getString("appid");
                            req.partnerId = json.getString("partnerId");
                            req.prepayId = json.getString("prepayId");
                            req.nonceStr = json.getString("nonceStr");
                            req.timeStamp = json.getString("timeStamp");
                            req.packageValue = json.getString("packageValue");
                            req.sign = json.getString("sign");
                            req.extData = "app data"; // optional
                            // Toast.makeText(PayMainActivity.this, "正在跳转到微信",
                            // Toast.LENGTH_SHORT).show();
                            mWxApi.sendReq(req);
                        } else {
                            DialogUtils.showMessage(getApplicationContext(), "获取支付信息失败", json.getString("retmsg"));
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                } else {
                    Log.d(TAG, "服务器连接失败");
                    // Toast.makeText(PayMainActivity.this, "无法连接到服务器，请稍后再试",
                    // Toast.LENGTH_SHORT).show();
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                mPayBtn.setEnabled(true);
            };
        }.execute();
    }

    @Override
    public void onSuccess(int method, Object obj) {
        switch (method) {
        case MarketAPI.ACTION_GET_GOODS_LIST:
            mGoodsList = (GoodsListBean) obj;
            break;
        default:
            break;
        }
    }

    @Override
    public void onError(int method, int statusCode) {

    }
}
