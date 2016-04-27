package com.xiaohong.kulian.ui;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.common.util.TopBar;
import com.xiaohong.kulian.common.util.Utils;

public class BuyingEntryActivity extends Activity implements OnClickListener {

    private RelativeLayout layout_own_buy, layout_buy_for_other, layout_ask_buy;
    private TextView textView_buy_message;
    
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
        textView_buy_message=(TextView)this.findViewById(R.id.buy_message_push_coin_text);
        init_textview();
        
//        layout_ask_buy.setOnClickListener(this);
    }

    private void init_textview(){
        Resources resouces = getResources();
        String str = getResources().getString(R.string.buy_message_push_coin);
        SpannableString sps = new SpannableString(str);
//        Drawable d = getResources().getDrawable(R.drawable.activitybg);
//        d.setBounds(0, 0, 400, 50);
        int Index = str.indexOf(" ");
        System.out.println(Index);
        System.out.println(str);
        sps.setSpan(new AbsoluteSizeSpan(12,true), 0, Index, 
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sps.setSpan(new ForegroundColorSpan(resouces.getColor(R.color.white)), 
                0, Index, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  //设置前景色
        sps.setSpan(new BackgroundColorSpan(resouces.getColor(android.R.color.holo_red_light)), 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        sps.setSpan(new ImageSpan(d), 0, Index, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        textView_buy_message.setText(sps);
        textView_buy_message.setGravity(Gravity.CENTER_VERTICAL);
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
