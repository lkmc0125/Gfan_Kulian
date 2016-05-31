package com.xiaohong.kulian.ui;

import java.text.DecimalFormat;

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
import com.xiaohong.kulian.Session;
import com.xiaohong.kulian.adapter.BuyItemGridViewAdapter;
import com.xiaohong.kulian.bean.GoodsListBean;
import com.xiaohong.kulian.bean.WeChatGoodsBean;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.util.DialogUtils;
import com.xiaohong.kulian.common.util.PayResult;
import com.xiaohong.kulian.common.util.TopBar;
import com.xiaohong.kulian.common.util.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 购买金币的页面
 * 
 * @author free
 *
 */
public class BuyCoinActivity extends Activity implements OnClickListener, ApiRequestListener,
    OnItemClickListener, OnFocusChangeListener {
    private static final String TAG = "BuyCoinActivity";

    private IWXAPI mWxApi;

    private TextView mWechatPayTv;

    private GridView mGridView;
    private BuyItemGridViewAdapter mAdapter;
    private GoodsListBean mGoodsList;
    private Session mSession;
    private TextView mRetryTv;
    private boolean mIsPaySupported;
    private String mOtherAccount;
    private EditText userNameEditText;
    private LinearLayout otherAccountLayout, otherAccountLayout2;
    private boolean isBuyForOther = false;
    private String TopBarTextValue="购买上网时间";
    private TextView textView_remark;
    private ScrollView mContentScrollView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_coin);
        mSession = Session.get(getApplicationContext());
        Intent intent = getIntent();
        if (intent.hasExtra(com.xiaohong.kulian.common.util.Utils.KEY_OTHER_ACCOUNT)) {
            isBuyForOther = true;
            TopBarTextValue="帮人充值";
        }
        initViews();
        initData();
    }

    private void initViews() {
        initTopBar(TopBarTextValue);
        mGridView = (GridView) findViewById(R.id.buycoinitemgridview);
        if (isBuyForOther) {
            otherAccountLayout = (LinearLayout) findViewById(R.id.other_account_layout);
            otherAccountLayout.setVisibility(View.VISIBLE);
            otherAccountLayout2 = (LinearLayout) findViewById(R.id.other_account_layout2);
            otherAccountLayout2.setVisibility(View.VISIBLE);
            userNameEditText = (EditText) this.findViewById(R.id.et_username);
            userNameEditText.setOnFocusChangeListener(this);
            userNameEditText.requestFocus();
            textView_remark=(TextView)this.findViewById(R.id.person_account_other_account_remark_prompt_text);
            textView_remark.setVisibility(View.VISIBLE);
        }

        mWechatPayTv = (TextView) findViewById(R.id.payment_choice_info_button);
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
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
        case R.id.back_btn:
            finish();
            break;
        case R.id.payment_choice_info_button:
//            Utils.gotoBuyCoinPaymentChoiceActivity(BuyCoinActivity.this);
            Intent intent = new Intent(BuyCoinActivity.this, BuyCoinPaymentChoiceActivity.class);
            String remark=textView_remark==null?"":textView_remark.getText().toString();
            intent.putExtra("pay_account", mSession.getUserName());
            intent.putExtra("pay_time", mAdapter.getSelectedGoodsName());
            intent.putExtra("pay_money", getprice(mAdapter.getSelectedGoodsprice())+"元");
            intent.putExtra("pay_remark", remark);
            intent.putExtra("isBuyForOther", isBuyForOther);
            if (isBuyForOther == true && checkUserName() == true) {
                mOtherAccount = userNameEditText.getText().toString();
            }
            if(mOtherAccount != null) {
                intent.putExtra("mOtherAccount", mOtherAccount);
            }else{
                intent.putExtra("mOtherAccount", "");
            }
            
            Bundle bundle=new Bundle();
            bundle.putSerializable("GoodsBean", mAdapter.getSelectedGoodsBean());
            intent.putExtras(bundle);
            startActivity(intent);
//            mSession.getUserName() + "&type=1&goods_id=" + mAdapter.getSelectedGoodsId();
//            if (isBuyForOther == true && checkUserName() == true) {
//                mOtherAccount = userNameEditText.getText().toString();
//                doWechatPay();
//            } else if (isBuyForOther == false) {
//                doWechatPay();
//            }
            break;
        case R.id.no_data:
            getGoodsList();
            break;
        default:
            break;
        }
    }

    /**
     * Init wechat pay api
     */
    private void initData() {
        getGoodsList();
    }

    /**
     * Wechat pay logic
     */
    private void doWechatPay() {
        final String url = "http://115.159.76.147:8390/cb/getprepayid?phone_number="
                + mSession.getUserName() + "&type=1&goods_id=" + mAdapter.getSelectedGoodsId();
        Log.d(TAG, "doWechatPay url = " + url);
        final String goodsName = mAdapter.getSelectedGoodsName();
        final int goodsId = mAdapter.getSelectedGoodsId();
        final int goodsPrice = mAdapter.getSelectedGoodsprice();
        final int goodsGift = mAdapter.getSelectedGoodsGift();
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
                            if(mOtherAccount != null) {
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

    @Override
    public void onSuccess(int method, Object obj) {
        switch (method) {
            case MarketAPI.ACTION_GET_GOODS_LIST:
                mGoodsList = (GoodsListBean) obj;
                if (mGoodsList.getGoodsList() != null) {
                    mRetryTv.setVisibility(View.GONE);
                    mContentScrollView.setVisibility(View.VISIBLE);

                    mAdapter = new BuyItemGridViewAdapter(getApplicationContext(), mGoodsList.getGoodsList());
                    mGridView.setAdapter(mAdapter);
                    if (mIsPaySupported) {
                        mWechatPayTv.setEnabled(true);
                    }
                    mGridView.setOnItemClickListener(BuyCoinActivity.this);
                    mWechatPayTv.setVisibility(View.VISIBLE);
                } else {
                    mRetryTv.setVisibility(View.VISIBLE);
                    mContentScrollView.setVisibility(View.GONE);
                }
                break;
            case 10000:
                //TODO　change 10000 to a constants
                //get payInfo from server
                String payInfo = ""; 
                doAliPay(payInfo);
                break;
            default:
                break;
            }
    }

    @Override
    public void onError(int method, int statusCode) {
        mRetryTv.setVisibility(View.VISIBLE);
        mContentScrollView.setVisibility(View.GONE);
    }
    
    private void getGoodsList() {
        MarketAPI.getGoodsList(getApplicationContext(), this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
       mAdapter.setSelectedPos(position);
       mAdapter.notifyDataSetChanged();
        
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
        case R.id.et_username:
            if (!hasFocus) {
                if (checkUserName()) {
                    mOtherAccount = userNameEditText.getText().toString();
                } else {
                    mOtherAccount = null;
                }
            }
            break;
        default:
            break;
        }
    }

    private boolean checkUserName() {
        String input = userNameEditText.getText().toString();
        if (TextUtils.isEmpty(input)) {
            userNameEditText.setError(getDisplayText(R.string.error_username_empty));
            return false;
        } else {
            userNameEditText.setError(null);
        }
        int length = input.length();
        if (length != 11) {
            userNameEditText.setError(getDisplayText(R.string.error_username_length_invalid));
            return false;
        } else {
            userNameEditText.setError(null);
        }
        return true;
    }

    private CharSequence getDisplayText(int resId) {
        return Html.fromHtml("<font color='#2C78D4'>"+getString(resId)+"</font>");
    }
    private String getprice(int price){
        String str="";
        if (price % 100 == 0) {
            str=(price/100+"");
        } else {
            float price1 = (float)price/100.f;
            str=(new DecimalFormat("#0.00").format(price1));
        }
        return str;
    }
    
    /**
     * 当选择支付宝支付后调用此函数
     */
    private void onAliPaySelected() {
        //TODO call api to get order info
        //and do the other steps when request is successful
    }
    /**
     * 支付宝支付
     * 
     */
    public void doAliPay(final String payInfo) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                // 构造PayTask 对象
                PayTask alipay = new PayTask(BuyCoinActivity.this);
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
                    Toast.makeText(BuyCoinActivity.this, 
                            "支付成功", Toast.LENGTH_SHORT).show();
                } else {
                    // 判断resultStatus 为非"9000"则代表可能支付失败
                    // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                    if (TextUtils.equals(resultStatus, "8000")) {
                        Toast.makeText(BuyCoinActivity.this, 
                                "支付结果确认中", Toast.LENGTH_SHORT).show();

                    } else {
                        // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                        Toast.makeText(BuyCoinActivity.this, 
                                "支付失败", Toast.LENGTH_SHORT).show();

                    }
                }
            }
            
        }.execute();
    }
    
   
}
