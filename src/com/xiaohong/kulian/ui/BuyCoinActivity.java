package com.xiaohong.kulian.ui;

import org.json.JSONException;
import org.json.JSONObject;

import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.common.util.DialogUtils;
import com.xiaohong.kulian.common.util.Utils;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 购买金币的页面
 * 
 * @author free
 *
 */
public class BuyCoinActivity extends Activity implements OnClickListener {
    private static final String TAG = "BuyCoinActivity";

    private IWXAPI mWxApi;

    private ImageButton mBackBtn;
    private TextView mWechatPayTv;
    private RelativeLayout mOneyuanLayout;
    private RelativeLayout mFiveyuanLayout;
    private RelativeLayout mTenyuanLayout;
    private RelativeLayout mThirtyyuanLayout;

    private TextView mOnehoundredTv;
    private TextView mOnetv;
    private TextView mOneUnitTv;
    private TextView mFivehoundredTv;
    private TextView mFiveTv;
    private TextView mFiveUnittv;

    private TextView mOnethousandTv;
    private TextView mTentv;
    private TextView mTenUnitTv;

    private TextView mThreethousandTv;
    private TextView mThirtyTv;
    private TextView mThirtyUnittv;

    // the default item is one yuan
    private int mMoney = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_coin);
        initData();
        initViews();
    }

    private void initViews() {
        mBackBtn = (ImageButton) findViewById(R.id.back_btn);
        mWechatPayTv = (TextView) findViewById(R.id.wechatpaytv);
        mOneyuanLayout = (RelativeLayout) findViewById(R.id.oneyuanlayout);
        mFiveyuanLayout = (RelativeLayout) findViewById(R.id.fiveyuanlayout);
        mTenyuanLayout = (RelativeLayout) findViewById(R.id.tenyuanlayout);
        mThirtyyuanLayout = (RelativeLayout) findViewById(R.id.thirtyyuanlayout);

        mOnehoundredTv = (TextView) findViewById(R.id.onehundredtv);
        mOnetv = (TextView) findViewById(R.id.onetv);
        mOneUnitTv = (TextView) findViewById(R.id.oneyuanunit);

        mFivehoundredTv = (TextView) findViewById(R.id.fivehundredtv);
        mFiveTv = (TextView) findViewById(R.id.fivetv);
        mFiveUnittv = (TextView) findViewById(R.id.fiveyuanunit);

        mOnethousandTv = (TextView) findViewById(R.id.onethousandtv);
        mTentv = (TextView) findViewById(R.id.tentv);
        mTenUnitTv = (TextView) findViewById(R.id.tenyuanunit);

        mThreethousandTv = (TextView) findViewById(R.id.threethousandtv);
        mThirtyTv = (TextView) findViewById(R.id.thirtytv);
        mThirtyUnittv = (TextView) findViewById(R.id.thirtyyuanunit);

        mBackBtn.setOnClickListener(this);
        mWechatPayTv.setOnClickListener(this);
        mOneyuanLayout.setOnClickListener(this);
        mFiveyuanLayout.setOnClickListener(this);
        mTenyuanLayout.setOnClickListener(this);
        mThirtyyuanLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.back_btn :
                finish();
                break;
            case R.id.wechatpaytv :
                doWechatPay();
                break;
            case R.id.oneyuanlayout :
                doSelectOneyuan();
                break;
            case R.id.fiveyuanlayout :
                doSelectFiveyuan();
                break;
            case R.id.tenyuanlayout :
                doSelectTenyuan();
                break;
            case R.id.thirtyyuanlayout :
                doSelectThirtyyuan();
                break;
            default :
                break;
        }
    }

    private void doSelectOneyuan() {
        mMoney = 1;
        mOneyuanLayout.setBackgroundResource(R.drawable.coincheckedbg);
        mFiveyuanLayout.setBackgroundResource(R.drawable.coindefaultbg);
        mTenyuanLayout.setBackgroundResource(R.drawable.coindefaultbg);
        mThirtyyuanLayout
                .setBackgroundResource(R.drawable.coindefaultbg);
        
        mOnehoundredTv.setTextColor(getResources().getColor(R.color.buy_coin_item_selected_color));
        mOnetv.setTextColor(getResources().getColor(R.color.buy_coin_item_selected_color));
        mOneUnitTv.setTextColor(getResources().getColor(R.color.buy_coin_item_selected_color));
        
        mFivehoundredTv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_black_color));
        mFiveTv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_blue_color));
        mFiveUnittv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_black_color));
                         
        mOnethousandTv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_black_color));
        mTentv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_blue_color));
        mTenUnitTv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_black_color));
                         
        mThreethousandTv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_black_color));
        mThirtyTv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_blue_color));
        mThirtyUnittv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_black_color));
    }

    private void doSelectFiveyuan() {
        mMoney = 5;
        mOneyuanLayout.setBackgroundResource(R.drawable.coindefaultbg);
        mFiveyuanLayout.setBackgroundResource(R.drawable.coincheckedbg);
        mTenyuanLayout.setBackgroundResource(R.drawable.coindefaultbg);
        mThirtyyuanLayout
                .setBackgroundResource(R.drawable.coindefaultbg);
        
        mOnehoundredTv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_black_color));
        mOnetv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_blue_color));
        mOneUnitTv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_black_color));
        
        mFivehoundredTv.setTextColor(getResources().getColor(R.color.buy_coin_item_selected_color));
        mFiveTv.setTextColor(getResources().getColor(R.color.buy_coin_item_selected_color));
        mFiveUnittv.setTextColor(getResources().getColor(R.color.buy_coin_item_selected_color));
                         
        mOnethousandTv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_black_color));
        mTentv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_blue_color));
        mTenUnitTv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_black_color));
                         
        mThreethousandTv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_black_color));
        mThirtyTv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_blue_color));
        mThirtyUnittv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_black_color));
        
    }

    private void doSelectTenyuan() {
        mMoney = 10;
        mOneyuanLayout.setBackgroundResource(R.drawable.coindefaultbg);
        mFiveyuanLayout.setBackgroundResource(R.drawable.coindefaultbg);
        mTenyuanLayout.setBackgroundResource(R.drawable.coincheckedbg);
        mThirtyyuanLayout
                .setBackgroundResource(R.drawable.coindefaultbg);
        
        mOnehoundredTv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_black_color));
        mOnetv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_blue_color));
        mOneUnitTv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_black_color));
        
        mFivehoundredTv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_black_color));
        mFiveTv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_blue_color));
        mFiveUnittv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_black_color));
                         
        mOnethousandTv.setTextColor(getResources().getColor(R.color.buy_coin_item_selected_color));
        mTentv.setTextColor(getResources().getColor(R.color.buy_coin_item_selected_color));
        mTenUnitTv.setTextColor(getResources().getColor(R.color.buy_coin_item_selected_color));
                         
        mThreethousandTv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_black_color));
        mThirtyTv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_blue_color));
        mThirtyUnittv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_black_color));
    }

    private void doSelectThirtyyuan() {
        mMoney = 30;
        mOneyuanLayout.setBackgroundResource(R.drawable.coindefaultbg);
        mFiveyuanLayout.setBackgroundResource(R.drawable.coindefaultbg);
        mTenyuanLayout.setBackgroundResource(R.drawable.coindefaultbg);
        mThirtyyuanLayout
                .setBackgroundResource(R.drawable.coincheckedbg);
        
        mOnehoundredTv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_black_color));
        mOnetv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_blue_color));
        mOneUnitTv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_black_color));
        
        mFivehoundredTv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_black_color));
        mFiveTv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_blue_color));
        mFiveUnittv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_black_color));

        mOnethousandTv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_black_color));
        mTentv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_blue_color));
        mTenUnitTv.setTextColor(getResources().getColor(R.color.buy_coin_item_un_selected_black_color));
                         
        mThreethousandTv.setTextColor(getResources().getColor(R.color.buy_coin_item_selected_color));
        mThirtyTv.setTextColor(getResources().getColor(R.color.buy_coin_item_selected_color));
        mThirtyUnittv.setTextColor(getResources().getColor(R.color.buy_coin_item_selected_color));
    }

    /**
     * Init wechat pay api
     */
    private void initData() {
        mWxApi = WXAPIFactory.createWXAPI(this, Constants.APP_ID, false);
        mWxApi.registerApp(Constants.APP_ID);

        boolean isPaySupported = mWxApi.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
        Toast.makeText(getApplicationContext(), String.valueOf(isPaySupported),
                Toast.LENGTH_SHORT).show();

        // getGoodsList();
    }

    /**
     * Wechat pay logic
     */
    private void doWechatPay() {
        final String url = "http://115.159.76.147:8390/cb/getprepayid?phone_number=13418680969&type=1&goods_id=1";
        Toast.makeText(getApplicationContext(), "请稍候...", Toast.LENGTH_SHORT)
                .show();
        new AsyncTask<Void, Void, Void>() {

            protected void onPreExecute() {
                mWechatPayTv.setEnabled(false);
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
                        Log.e(TAG, "create json object fail:" + e.getMessage());
                        e.printStackTrace();
                    }
                    try {
                        if (null != json && json.has("ret_code")
                                && json.getInt("ret_code") == 0) {
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
                            DialogUtils.showMessage(getApplicationContext(),
                                    "获取支付信息失败", json.getString("retmsg"));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG,
                                "when do pay Jsonexception:" + e.getMessage());
                        e.printStackTrace();
                    }

                } else {
                    Log.d(TAG, "connect server failed");
                    // Toast.makeText(PayMainActivity.this, "无法连接到服务器，请稍后再试",
                    // Toast.LENGTH_SHORT).show();
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                mWechatPayTv.setEnabled(true);
            };
        }.execute();
    }

}
