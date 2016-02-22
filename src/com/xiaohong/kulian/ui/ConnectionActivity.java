package com.xiaohong.kulian.ui;

import java.util.HashMap;

import android.os.Bundle;
import android.view.View;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.common.util.TopBar;
import com.xiaohong.kulian.common.widget.BaseActivity;

public class ConnectionActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        initTopBar();
    }

    private void initTopBar() {
        TopBar.createTopBar(getApplicationContext(),
                new View[] { findViewById(R.id.top_bar_title) },
                new int[] { View.VISIBLE },
                getString(R.string.connection_title));
    }
}
