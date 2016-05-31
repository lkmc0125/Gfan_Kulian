package com.xiaohong.kulian.ui;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.bean.GoodsBean;
import com.xiaohong.kulian.bean.WeChatGoodsBean;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.util.DialogUtils;
import com.xiaohong.kulian.common.util.TopBar;
import com.xiaohong.kulian.common.util.Utils;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class BuyCoinPaymentChoiceActivity extends Activity implements OnClickListener, ApiRequestListener,
OnItemClickListener, OnFocusChangeListener {
    private static final String TAG = "BuyCoinPaymentChoiceActivity";
    private String TopBarTextValue="支付确认";
    private TextView textView_pay_time,textView_pay_money,textView_pay_account,textView_pay_remark;
    private CheckBox checkBox_wechat,checkBox_alipay;;
    private TextView mWechatPayTv;
    private IWXAPI mWxApi;
    private boolean mIsPaySupported;
    private ScrollView mContentScrollView;
    private TextView mRetryTv;
    private String pay_time="",pay_money="",pay_account="",pay_remark="";
    private GoodsBean goodsBean;
    private String mOtherAccount;
    private RelativeLayout layout_remark;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_payment_buy_coin);
        Intent intent = getIntent();
        if (intent.hasExtra("pay_account")) {
            pay_time=intent.getStringExtra("pay_time");
            pay_money=intent.getStringExtra("pay_money");
            pay_account=intent.getStringExtra("pay_account");//15181625496
            pay_remark=intent.getStringExtra("pay_remark");//3天
            goodsBean=(GoodsBean) intent.getSerializableExtra("GoodsBean");
            mOtherAccount=intent.getStringExtra("mOtherAccount");
            System.out.println("pay_time"+pay_time);
            System.out.println("pay_time"+pay_money);
            System.out.println("pay_time"+pay_account);
            System.out.println("pay_time"+pay_remark);
        }
        initViews();
        
    }
    private void initViews() {
        initTopBar(TopBarTextValue);
        textView_pay_time=(TextView)this.findViewById(R.id.payment_pay_time_value);
        textView_pay_money=(TextView)this.findViewById(R.id.payment_pay_money_value);
        textView_pay_account=(TextView)this.findViewById(R.id.payment_pay_acount_value);
        textView_pay_remark=(TextView)this.findViewById(R.id.payment_pay_remark_value);
        layout_remark=(RelativeLayout)this.findViewById(R.id.payment_pay_remark_layout);
        if(pay_time!=null){
            textView_pay_time.setText(pay_time);
        }
        if(pay_money!=null){
            textView_pay_money.setText(pay_money);
        }
        if(pay_account!=null){
            textView_pay_account.setText(pay_account);
        }
        if(pay_remark!=null){
            System.out.println("pay_remark"+pay_remark);
            if(pay_remark.equals("")){
                layout_remark.setVisibility(View.GONE);
            }else{
                layout_remark.setVisibility(View.VISIBLE);
            }
            textView_pay_remark.setText(pay_remark);
        }
        checkBox_wechat=(CheckBox)this.findViewById(R.id.payment_choice_wetchat_pay_checkbox);
        checkBox_alipay=(CheckBox)this.findViewById(R.id.payment_choice_alipay_pay_checkbox);
        checkBox_wechat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        checkBox_alipay.setChecked(false);
                        if (mIsPaySupported) {
                            mWechatPayTv.setEnabled(true);
                        }
                        mWechatPayTv.setVisibility(View.VISIBLE);
                    } else {
                        mWechatPayTv.setVisibility(View.GONE);
                    }
                    }
                    });
        checkBox_alipay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        checkBox_wechat.setChecked(false);
                    } else {
                            
                    }
                    }
                    });


        mWechatPayTv = (TextView) findViewById(R.id.payment_choice_pay_button);
        mWechatPayTv.setVisibility(View.INVISIBLE);
        mWechatPayTv.setEnabled(false);
        mWechatPayTv.setOnClickListener(this);

        mWxApi = WXAPIFactory.createWXAPI(this, Constants.APP_ID, false);
        mWxApi.registerApp(Constants.APP_ID);
        mIsPaySupported = mWxApi.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
        if (!mIsPaySupported) {
            mWechatPayTv.setText("仅支持微信支付");
        }

        mContentScrollView = (ScrollView) findViewById(R.id.content_scroll_view);
        mRetryTv = (TextView) findViewById(R.id.no_data);
        mRetryTv.setOnClickListener(this);
    }
    
    private void initTopBar(String title) {
        TopBar.createTopBar(this, 
                new View[] { findViewById(R.id.back_btn), findViewById(R.id.top_bar_title) },
                new int[] { View.VISIBLE, View.VISIBLE}, 
                title);
        ImageButton back = (ImageButton)findViewById(R.id.back_btn);
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onSuccess(int method, Object obj) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onError(int method, int statusCode) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
        case R.id.back_btn:
            finish();
            break;
        case R.id.payment_choice_pay_button:        
                doWechatPay();
            break;
        case R.id.no_data:
//            getGoodsList();
            break;
        default:
            break;
        }
        
    }
    
    /**
     * Wechat pay logic
     */
    private void doWechatPay() {
        final String url = "http://115.159.76.147:8390/cb/getprepayid?phone_number="
                + pay_account + "&type=1&goods_id=" + goodsBean.getGoodsId();
        Log.d(TAG, "doWechatPay url = " + url);
        final String goodsName = goodsBean.getName();
        final int goodsId = goodsBean.getGoodsId();
        final int goodsPrice = goodsBean.getPrice();
        final int goodsGift = goodsBean.getGiftCoin();
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
                            WeChatGoodsBean goodsBean = new WeChatGoodsBean();
                            goodsBean.setGoods_name(goodsName);
                            goodsBean.setGoods_id(goodsId);
                            goodsBean.setOut_trade_no(
                                    json.getString("out_trade_no"));
                            goodsBean.setGoods_gift(goodsGift);
                            goodsBean.setGoods_price(goodsPrice);
                            if(mOtherAccount != null&&!mOtherAccount.equals("")) {
                                goodsBean.setOther_account(mOtherAccount);
                            }else {
                                goodsBean.setOther_account("");
                            }
                            Gson gson = new Gson();
                            req.extData = gson.toJson(goodsBean);
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
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                mWechatPayTv.setEnabled(true);
            };
        }.execute();
    }


}
