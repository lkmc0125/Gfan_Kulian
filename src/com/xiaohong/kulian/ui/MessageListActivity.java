package com.xiaohong.kulian.ui;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Session;
import com.xiaohong.kulian.adapter.MessageListAdapter;
import com.xiaohong.kulian.bean.MessageBean;
import com.xiaohong.kulian.bean.MessageListBean;
import com.xiaohong.kulian.common.util.TopBar;
import com.xiaohong.kulian.common.widget.BaseActivity;

public class MessageListActivity extends BaseActivity implements OnItemClickListener {
    private ListView mListView;
    private FrameLayout mLoading;
    private TextView mNoData;
    private MessageListAdapter mAdapter;
    private static final String TAG = "MessageListActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_common_layout);
        initTopBar();

        mListView = (ListView) findViewById(R.id.list);
        mLoading = (FrameLayout) findViewById(R.id.loading);
        mNoData = (TextView) mLoading.findViewById(R.id.no_data);
        mListView.setEmptyView(mLoading);
        mListView.setOnItemClickListener(this);
        Session session = Session.get(getApplicationContext());
        MessageListBean messages = session.getMessageList();
        if (messages != null && messages.getMessageList() != null && messages.getMessageList().size() > 0) {
            mAdapter = new MessageListAdapter(MessageListActivity.this);
            mListView.setAdapter(mAdapter);
            mAdapter.setData(messages.getMessageList());
            mAdapter.notifyDataSetChanged();
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
    
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        ArrayList<MessageBean> list = mAdapter.getData();
        if (list != null) {
            MessageBean item = list.get(position);
            String clickUrl = item.getClickUrl();
            if(clickUrl != null && !clickUrl.equals("")) {
                Intent detailIntent = new Intent(getApplicationContext(), WebviewActivity.class);
                detailIntent.putExtra("extra.url", clickUrl);
                detailIntent.putExtra("extra.title", item.getMessageText());
                startActivity(detailIntent);
            }
        } else {
            Log.w(TAG, "list is null");
        }
    }

}
