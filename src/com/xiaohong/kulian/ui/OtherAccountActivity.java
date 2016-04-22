package com.xiaohong.kulian.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.common.util.TopBar;
import com.xiaohong.kulian.common.util.Utils;

public class OtherAccountActivity extends Activity implements OnClickListener {

    private RelativeLayout layout_own_buy, layout_buy_for_other, layout_ask_buy;
    private Button btn_confirm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_account);
        initViews();
    }

    private void initViews() {
        initTopBar("帮人充值");
        btn_confirm = (Button) this.findViewById(R.id.btn_confirm);
        btn_confirm.setOnClickListener(this);
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
        case R.id.btn_confirm:
            Utils.gotoBuyCoinPage(OtherAccountActivity.this);
            break;
        default:
                break;
        }
    }

}
