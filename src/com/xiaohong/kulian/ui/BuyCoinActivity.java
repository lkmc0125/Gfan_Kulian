package com.xiaohong.kulian.ui;

import org.json.JSONException;
import org.json.JSONObject;

import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Session;
import com.xiaohong.kulian.adapter.BuyItemGridViewAdapter;
import com.xiaohong.kulian.bean.GoodsListBean;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.util.DialogUtils;
import com.xiaohong.kulian.common.util.TopBar;
import com.xiaohong.kulian.common.util.Utils;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_coin);
        mSession = Session.get(getApplicationContext());
        Intent intent = getIntent();
        if (intent.hasExtra(com.xiaohong.kulian.common.util.Utils.KEY_OTHER_ACCOUNT)) {
            isBuyForOther = true;
        }
        initViews();
        initData();
    }

    private void initViews() {
        initTopBar("购买金币");
        mGridView = (GridView) findViewById(R.id.buycoinitemgridview);
        if (isBuyForOther) {
            otherAccountLayout = (LinearLayout) findViewById(R.id.other_account_layout);
            otherAccountLayout.setVisibility(View.VISIBLE);
            otherAccountLayout2 = (LinearLayout) findViewById(R.id.other_account_layout2);
            otherAccountLayout2.setVisibility(View.VISIBLE);
        }

        userNameEditText = (EditText) this.findViewById(R.id.et_username);
        userNameEditText.setOnFocusChangeListener(this);
        userNameEditText.requestFocus();

        mWechatPayTv = (TextView) findViewById(R.id.wechatpaytv);
        mWechatPayTv.setVisibility(View.INVISIBLE);
        mWechatPayTv.setEnabled(false);
        mWechatPayTv.setOnClickListener(this);

        mWxApi = WXAPIFactory.createWXAPI(this, Constants.APP_ID, false);
        mWxApi.registerApp(Constants.APP_ID);
        mIsPaySupported = mWxApi.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
        if (!mIsPaySupported) {
            mWechatPayTv.setText("仅支持微信支付");
        }

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
        case R.id.wechatpaytv:
            if ((isBuyForOther == true && checkUserName() == true)
                    || isBuyForOther == false) {
                doWechatPay();
            }
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
                            req.extData = "{\"goods_name\":\"" + goodsName
                                    +"\", \"goods_id\":" + goodsId
                                    +", \"out_trade_no\":\"" + json.getString("out_trade_no") + "\"";
                            if(mOtherAccount != null) {
                                req.extData += ", \"other_account\":\"" + mOtherAccount + "\"";
                            }else {
                                req.extData += ", \"other_account\":\"" + "\"";
                            }
                            req.extData += "}";
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

    @Override
    public void onSuccess(int method, Object obj) {
        switch (method) {
            case MarketAPI.ACTION_GET_GOODS_LIST:
                mGoodsList = (GoodsListBean) obj;
                if (mGoodsList.getGoodsList() != null) {
                    mRetryTv.setVisibility(View.GONE);
                    mAdapter = new BuyItemGridViewAdapter(getApplicationContext(), mGoodsList.getGoodsList());
                    mGridView.setAdapter(mAdapter);
                    if (mIsPaySupported) {
                        mWechatPayTv.setEnabled(true);                        
                    }
                    mGridView.setOnItemClickListener(BuyCoinActivity.this);
                    mWechatPayTv.setVisibility(View.VISIBLE);
                } else {
                    mRetryTv.setVisibility(View.VISIBLE);
                }
                break;
            default:
                break;
            }
    }

    @Override
    public void onError(int method, int statusCode) {
        mRetryTv.setVisibility(View.VISIBLE);
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
}
