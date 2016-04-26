package com.xiaohong.kulian.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Session;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.util.TopBar;
import com.xiaohong.kulian.common.util.Utils;

public class BuyingEntryActivity extends Activity implements OnClickListener {

    private RelativeLayout layout_own_buy, layout_buy_for_other, layout_ask_buy;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buying_entry);
        initViews();
    }

    private void initViews() {
        initTopBar("购买上网时间");
        layout_own_buy = (RelativeLayout) this.findViewById(R.id.own_buy);
        layout_buy_for_other = (RelativeLayout) this.findViewById(R.id.buy_for_other);
//        layout_ask_buy = (RelativeLayout) this.findViewById(R.id.ask_buy);
        layout_own_buy.setOnClickListener((OnClickListener) this);
        layout_buy_for_other.setOnClickListener(this);
//        layout_ask_buy.setOnClickListener(this);
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
        switch (v.getId()) {
        case R.id.own_buy:
            Utils.gotoBuyCoinPage(BuyingEntryActivity.this);
            break;
        case R.id.buy_for_other:
            Utils.gotoBuyCoinPageForOther(BuyingEntryActivity.this);
            break;
//        case R.id.ask_buy:
//            break;
        default:
            break;
        }
    }
}
