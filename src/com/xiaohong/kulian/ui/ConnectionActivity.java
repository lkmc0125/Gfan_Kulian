package com.xiaohong.kulian.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.common.util.TopBar;
import com.xiaohong.kulian.common.util.WifiAuthentication;
import com.xiaohong.kulian.common.widget.BaseActivity;

public class ConnectionActivity extends BaseActivity {
    private static final String TAG = "ConnectionActivity"; 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        initTopBar();

        Button authBtn = (Button) findViewById(R.id.authenticationBtn);
        authBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                WifiAuthentication auth = new WifiAuthentication();
                auth.appAuth();
            }
        });
    }

    private void initTopBar() {
        TopBar.createTopBar(getApplicationContext(),
                new View[] { findViewById(R.id.top_bar_title) },
                new int[] { View.VISIBLE },
                getString(R.string.connection_title));
    }
}
