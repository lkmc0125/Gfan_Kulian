package com.xiaohong.kulian.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.bean.TaskBean;

public class GzhTaskDetailActivity extends Activity implements OnClickListener {
    private static final String TAG = "GzhTaskDetailActivity";
    private TaskBean mTaskBean;

    private LinearLayout mBackLayout;

    private TextView mTaskNameTv;
    private TextView mTaskWeixinTv;
    private TextView mTaskCoinNumTv;
    private TextView mTaskGuide1Tv;
    private TextView mTaskGuide2Tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gzh_task_detail);
        Intent intent = getIntent();
        mTaskBean = intent.getParcelableExtra(Constants.EXTRA_TASK_BEAN);
        initViews();
    }

    private void initViews() {
        mTaskNameTv = (TextView) findViewById(R.id.task_name_tv);
        mTaskNameTv.setText(getTaskName());
        mTaskWeixinTv = (TextView) findViewById(R.id.task_weixin_id_tv);
        mTaskWeixinTv.setText(mTaskBean.getWeixin_id());
        mTaskCoinNumTv = (TextView) findViewById(R.id.task_coin_num_tv);
        mTaskCoinNumTv.setText("+" + mTaskBean.getCoin_num());
        mTaskGuide1Tv = (TextView) findViewById(R.id.task_guide_1_tv);
        mTaskGuide1Tv.setText(Html.fromHtml(getResources().getString(
                R.string.task_guide_1)));
        mTaskGuide2Tv = (TextView) findViewById(R.id.task_guide_2_tv);
        mTaskGuide2Tv.setText(Html.fromHtml(getResources().getString(
                R.string.task_guide_2)));
        mBackLayout = (LinearLayout) findViewById(R.id.back_layout);
        mBackLayout.setOnClickListener(this);

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.back_layout :
                finish();
                break;
            default :
                break;

        }

    }

    private String getTaskName() {
        String name = mTaskBean.getName();
        int startIndex = name.indexOf('\'');
        int endIndex = name.lastIndexOf('\'');
        if (startIndex == -1) {
            return name;
        } else if (endIndex == -1) {
            return name;
        }
        return name.substring(startIndex + 1, endIndex);
    }

}
