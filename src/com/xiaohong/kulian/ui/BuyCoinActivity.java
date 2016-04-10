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
import com.xiaohong.kulian.common.util.Utils;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 购买金币的页面
 * 
 * @author free
 *
 */
public class BuyCoinActivity extends Activity implements OnClickListener, ApiRequestListener,
    OnItemClickListener{
    private static final String TAG = "BuyCoinActivity";

    private IWXAPI mWxApi;

    private ImageButton mBackBtn;
    private TextView mWechatPayTv;

    private GridView mGridView;
    private BuyItemGridViewAdapter mAdapter;
    private GoodsListBean mGoodsList;
    private Session mSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_coin);
        mSession = Session.get(getApplicationContext());
        initViews();
        initData();
    }

    private void initViews() {
        mBackBtn = (ImageButton) findViewById(R.id.back_btn);
        mWechatPayTv = (TextView) findViewById(R.id.wechatpaytv);
        mGridView = (GridView) findViewById(R.id.buycoinitemgridview);
       
        mWechatPayTv.setEnabled(false);
        
        mBackBtn.setOnClickListener(this);
        mWechatPayTv.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        /*MarketAPI.reportAppInstalled(getApplicationContext(), null, 154);
        MarketAPI.reportAppLaunched(getApplicationContext(), null, 154);*/
        switch (id) {
            case R.id.back_btn :
                finish();
                break;
            case R.id.wechatpaytv :
                doWechatPay();
                break;
            default :
                break;
        }
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

        getGoodsList();
    }

    /**
     * Wechat pay logic
     */
    private void doWechatPay() {
        final String url = "http://115.159.76.147:8390/cb/getprepayid?phone_number="
                + mSession.getUserName() + "&type=1&goods_id=" + mAdapter.getSelectedGoodsId();
        Log.d(TAG, "doWechatPay url = " + url);
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
                            /*req.extData = "app data"; // optional
*/                            // Toast.makeText(PayMainActivity.this, "正在跳转到微信",
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

    @Override
    public void onSuccess(int method, Object obj) {
        switch (method) {
            case MarketAPI.ACTION_GET_GOODS_LIST:
                mGoodsList = (GoodsListBean) obj;
                mAdapter = new BuyItemGridViewAdapter(getApplicationContext(), mGoodsList.getGoodsList());
                mGridView.setAdapter(mAdapter);
                mWechatPayTv.setEnabled(true);
                mGridView.setOnItemClickListener(BuyCoinActivity.this);

                break;
            default:
                break;
            }
        
    }

    @Override
    public void onError(int method, int statusCode) {
        // TODO Auto-generated method stub
        
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

}