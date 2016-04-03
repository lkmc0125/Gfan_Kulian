package com.xiaohong.kulian.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Session;
import com.xiaohong.kulian.common.util.TopBar;
import com.xiaohong.kulian.common.widget.BaseActivity;

public class MessagesActivity extends BaseActivity {
    private ListView mListView;
    private ArrayAdapter<String> adapter;
    private FrameLayout mLoading;
    private TextView mNoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_common_layout);
        initTopBar();

        mListView = (ListView) findViewById(R.id.list);
        mLoading = (FrameLayout) findViewById(R.id.loading);
        mNoData = (TextView) mLoading.findViewById(R.id.no_data);
        mListView.setEmptyView(mLoading);
        ArrayList<String> data = new ArrayList<String>();
        Session session = Session.get(getApplicationContext());
        ArrayList<HashMap<String, String>> messages = session.getMessages();
        if (messages != null && messages.size() > 0) {
            for (HashMap<String, String> item : messages) {
                data.add(item.get("text"));
            }
            adapter = new ArrayAdapter<String>(MessagesActivity.this,
                    android.R.layout.simple_list_item_1, data);
            mListView.setAdapter(adapter);
        } else {
            mNoData.setVisibility(View.VISIBLE);
            // mProgress.setVisibility(View.GONE);
        }
    }

    private void initTopBar() {

        TopBar.createTopBar(this, new View[] { findViewById(R.id.back_btn),
                findViewById(R.id.top_bar_title) }, new int[] { View.VISIBLE,
                View.VISIBLE }, getString(R.string.account_messages_title));
        ImageButton back = (ImageButton) findViewById(R.id.back_btn);
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
    }
}
