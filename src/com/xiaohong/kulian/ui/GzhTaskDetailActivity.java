package com.xiaohong.kulian.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.bean.TaskBean;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.widget.CustomDialog;

public class GzhTaskDetailActivity extends Activity implements OnClickListener, ApiRequestListener {
    private static final String TAG = "GzhTaskDetailActivity";
    private TaskBean mTaskBean;

    private ImageButton mBackBtn;
    private TextView mCopyBtn;
    private TextView mTaskNameTv;
    private TextView mTaskDescTv;
    private TextView mTaskWeixinTv;
    private TextView mTaskCoinNumTv;
    private TextView mTaskGuide1Tv;
    private TextView mTaskGuide2Tv;
    private RelativeLayout mTaskStatus;
    private TextView mTaskStatusTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gzh_task_detail);
        Intent intent = getIntent();
        mTaskBean = intent.getParcelableExtra(Constants.EXTRA_TASK_BEAN);
        if (mTaskBean == null) {
            Log.d(TAG, "task bean not found.");
            finish();
        }
        initViews();
    }

    private void initViews() {
        mTaskNameTv = (TextView) findViewById(R.id.task_name_tv);
        mTaskNameTv.setText(getTaskName());
        mTaskDescTv = (TextView) findViewById(R.id.task_desc_tv);
        mTaskDescTv.setText(mTaskBean.getDesc());        
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
        mBackBtn = (ImageButton) findViewById(R.id.back_btn);
        mBackBtn.setOnClickListener(this);
        mCopyBtn = (TextView) findViewById(R.id.task_copy_tv);
        mCopyBtn.setOnClickListener(this);
        mTaskStatus = (RelativeLayout) findViewById(R.id.status);
        mTaskStatusTv = (TextView) findViewById(R.id.task_status_tv);
        
        // task status: 1 可领取    2 已领取    3 已完成   4 超时（领取但未完成）   5任务已结束（未领取）
        switch (mTaskBean.getTask_status()) {
        case 1:
        case 2:
            break;
        case 3:
            mTaskStatus.setVisibility(View.VISIBLE);
            break;
        case 4:
            break;
        case 5:
            mTaskStatus.setVisibility(View.VISIBLE);
            mTaskStatusTv.setText("任务已结束");
            break;
        default:
            break;
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.back_btn :
                finish();
                break;
            case R.id.task_copy_tv:
                copyWeixinId();
                MarketAPI.acceptGzhTask(getApplicationContext(), this, mTaskBean.getId());
                break;
            default :
                break;
        }
    }

    private void copyWeixinId() {
        Log.d(TAG, "copy to clipboard");
        ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        cbm.setPrimaryClip(ClipData.newPlainText(null, mTaskBean.getWeixin_id()));
        CustomDialog dialog = new CustomDialog.Builder(this).setMessage("公众号id已复制到剪贴板")
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
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

    @Override
    public void onSuccess(int method, Object obj) {
        switch (method) {
        case MarketAPI.ACTION_ACCEPT_GZH_TASK:
            break;
        default:
            break;
        }
    }

    @Override
    public void onError(int method, int statusCode) {
        switch (method) {
        case MarketAPI.ACTION_ACCEPT_GZH_TASK:
            break;
        default:
            break;
        }
    }

}
