package com.mappn.gfan.ui;

import java.util.HashMap;

import android.os.Bundle;
import android.view.View;

import com.mappn.gfan.Constants;
import com.mappn.gfan.R;
import com.mappn.gfan.common.util.TopBar;
import com.mappn.gfan.common.widget.BaseActivity;

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
