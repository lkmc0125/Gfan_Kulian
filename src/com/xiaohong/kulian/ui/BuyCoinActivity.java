package com.xiaohong.kulian.ui;

import java.text.DecimalFormat;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Session;
import com.xiaohong.kulian.adapter.BuyItemGridViewAdapter;
import com.xiaohong.kulian.bean.GoodsListBean;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.util.TopBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * 购买金币的页面
 * 
 * @author free
 *
 */
public class BuyCoinActivity extends Activity implements OnClickListener, ApiRequestListener,
    OnItemClickListener, OnFocusChangeListener {
    private static final String TAG = "BuyCoinActivity";

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

    @Override
    public void onSuccess(int method, Object obj) {
        switch (method) {
            case MarketAPI.ACTION_GET_GOODS_LIST:
            {
                mGoodsList = (GoodsListBean) obj;
                if (mGoodsList.getGoodsList() != null) {
                    mRetryTv.setVisibility(View.GONE);
                    mContentScrollView.setVisibility(View.VISIBLE);

                    mAdapter = new BuyItemGridViewAdapter(getApplicationContext(), mGoodsList.getGoodsList());
                    mGridView.setAdapter(mAdapter);
                    mGridView.setOnItemClickListener(BuyCoinActivity.this);
                    mWechatPayTv.setEnabled(true);
                    mWechatPayTv.setVisibility(View.VISIBLE);

                } else {
                    mRetryTv.setVisibility(View.VISIBLE);
                    mContentScrollView.setVisibility(View.GONE);
                }
                break;
            }
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
}
