package com.xiaohong.kulian.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.common.util.TopBar;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.util.WifiAuthentication;
import com.xiaohong.kulian.common.widget.BaseActivity;

public class ConnectionActivity extends BaseActivity {
    private static final String TAG = "ConnectionActivity"; 
    private Button mAuthBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        initTopBar();
        mAuthBtn = (Button) findViewById(R.id.authenticationBtn);
        mAuthBtn.setVisibility(View.INVISIBLE);
        mAuthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiAuthentication auth = new WifiAuthentication();
                auth.appAuth();
            }
        });
        checkNetwork();
    }

    private void initTopBar() {
        TopBar.createTopBar(getApplicationContext(),
                new View[] { findViewById(R.id.top_bar_title) },
                new int[] { View.VISIBLE },
                getString(R.string.connection_title));
    }

    private void checkNetwork () {
        String url = "http://115.159.3.16/cb/app_test"; // todo : add mobile para
        if (null == Utils.httpGet(url)) {
            Log.d(TAG, "network not availabel");
            mAuthBtn.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "network OK！");
            mAuthBtn.setVisibility(View.INVISIBLE);
        }
    }
}
