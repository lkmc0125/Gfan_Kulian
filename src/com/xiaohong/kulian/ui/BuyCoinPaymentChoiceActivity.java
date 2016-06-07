package com.xiaohong.kulian.ui;

import org.json.JSONException;
import org.json.JSONObject;

import com.alipay.sdk.app.PayTask;
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
import com.xiaohong.kulian.common.util.PayResult;
import com.xiaohong.kulian.common.util.TopBar;
import com.xiaohong.kulian.common.util.Utils;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
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
                if (isChecked) {
                    checkBox_alipay.setChecked(false);
                    // if (mIsPaySupported) {
                    // mWechatPayTv.setEnabled(true);
                    // }
//                    mWechatPayTv.setVisibility(View.VISIBLE);
                    // } else {
                    // mWechatPayTv.setVisibility(View.GONE);
                }
            }
        });
        checkBox_alipay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkBox_wechat.setChecked(false);
                } else {

                }
            }
        });
        checkBox_alipay.setChecked(true);
        mWechatPayTv = (TextView) findViewById(R.id.payment_choice_pay_button);
//        mWechatPayTv.setVisibility(View.INVISIBLE);
//        mWechatPayTv.setEnabled(false);
        mWechatPayTv.setOnClickListener(this);

        mWxApi = WXAPIFactory.createWXAPI(this, Constants.APP_ID, false);
        mWxApi.registerApp(Constants.APP_ID);
//        mIsPaySupported = mWxApi.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
//        if (!mIsPaySupported) {
//            mWechatPayTv.setText("仅支持微信支付");
//        }

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
            if (checkBox_alipay.isChecked() == false) {
                doWechatPay();
            } else {
                doAliPay();
            }
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

    /**
     * 支付宝支付
     * 
     */
    private void doAliPay() {
        //get payInfo from server
        String url = "http://115.159.76.147:8590/cb/get_alipay_sign?phone_number=13418680969&goods_id=1";
        String ret = Utils.httpGet(url);
        String result = null;
        if (ret != null) {
            try {
                JSONObject obj1 = new JSONObject(ret);
                if (obj1.getInt("ret_code") == 0) {
                    result = obj1.getString("result");
                } else {
                    DialogUtils.showMessage(getApplicationContext(), "出错啦", obj1.getString("ret_msg"));
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }
        final String payInfo = result;
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                // 构造PayTask 对象
                PayTask alipay = new PayTask(BuyCoinPaymentChoiceActivity.this);
                // 调用支付接口，获取支付结果
                String result = alipay.pay(payInfo, true);
                return result;
            }

            protected void onPostExecute(String result) {
                PayResult payResult = new PayResult(result);
                /**
                 * 同步返回的结果必须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/doc2/
                 * detail.htm?spm=0.0.0.0.xdvAU6&treeId=59&articleId=103665&
                 * docType=1) 建议商户依赖异步通知
                 */
                String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                Log.d(TAG, "resultInfo = " + resultInfo);
                String resultStatus = payResult.getResultStatus();
                // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                if (TextUtils.equals(resultStatus, "9000")) {
                    Toast.makeText(BuyCoinPaymentChoiceActivity.this, 
                            "支付成功", Toast.LENGTH_SHORT).show();
                } else {
                    // 判断resultStatus 为非"9000"则代表可能支付失败
                    // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                    if (TextUtils.equals(resultStatus, "8000")) {
                        Toast.makeText(BuyCoinPaymentChoiceActivity.this, 
                                "支付结果确认中", Toast.LENGTH_SHORT).show();
                    } else {
                        // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                        Toast.makeText(BuyCoinPaymentChoiceActivity.this, 
                                "支付失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }.execute();
    }
}
