package com.xiaohong.kulian.ui;

import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.util.TopBar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class BuyCoinPaymentChoiceActivity extends Activity implements OnClickListener, ApiRequestListener,
OnItemClickListener, OnFocusChangeListener {
    
    private String TopBarTextValue="支付确认";
    private TextView textView_pay_time,textView_pay_money,textView_pay_account,textView_pay_remark;
    private CheckBox checkBox_wechat,checkBox_alipay;;
    private TextView mWechatPayTv;
    private IWXAPI mWxApi;
    private boolean mIsPaySupported;
    private ScrollView mContentScrollView;
    private TextView mRetryTv;
    private String pay_time="",pay_money="",pay_account="",pay_remark="";
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
        checkBox_wechat=(CheckBox)this.findViewById(R.id.payment_choice_wetchat_pay_checkbox);
        checkBox_alipay=(CheckBox)this.findViewById(R.id.payment_choice_alipay_pay_checkbox);
        checkBox_wechat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        checkBox_alipay.setChecked(false);
                    } else {
                            
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
        // TODO Auto-generated method stub
        
    }

}
