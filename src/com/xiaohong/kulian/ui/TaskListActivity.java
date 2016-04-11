package com.xiaohong.kulian.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.adapter.CommonAdapter;
import com.xiaohong.kulian.adapter.TaskListAdapter;
import com.xiaohong.kulian.bean.AppBean;
import com.xiaohong.kulian.bean.TaskBean;
import com.xiaohong.kulian.bean.TaskListBean;
import com.xiaohong.kulian.common.ApiAsyncTask;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.widget.LazyloadListActivity;
import com.xiaohong.kulian.common.widget.LoadingDrawable;

public class TaskListActivity extends LazyloadListActivity implements
        ApiRequestListener, OnItemClickListener, OnClickListener {

    private static final String TAG = "TaskListActivity";
    // Loading
    private FrameLayout mLoading;
    private ProgressBar mProgress;
    private TextView mNoData;
    private TaskListAdapter mAdapter;
    private String mCategory;
    private boolean mIsEnd;

    @Override
    public boolean doInitView(Bundle savedInstanceState) {
        Intent intent = getIntent();
        Log.d(TAG, "doInitView:" + intent);
        if (intent != null) {

            mCategory = intent.getStringExtra(Constants.EXTRA_CATEGORY);
            if (TextUtils.isEmpty(mCategory)) {
                intent.getIntExtra(Constants.EXTRA_SORT_TYPE, 1);
                intent
                        .getStringExtra(Constants.EXTRA_CATEGORY_ID);
            }
            setContentView(R.layout.common_list_view);
            mIsEnd = false;
            mList = (ListView) findViewById(R.id.list);
            mLoading = (FrameLayout) findViewById(R.id.loading);
            mProgress = (ProgressBar) mLoading.findViewById(R.id.progressbar);
            mProgress.setIndeterminateDrawable(new LoadingDrawable(
                    getApplicationContext()));
            mProgress.setVisibility(View.VISIBLE);
            mNoData = (TextView) mLoading.findViewById(R.id.no_data);
            mNoData.setOnClickListener(this);
            mList.setEmptyView(mLoading);
            mList.setOnItemClickListener(this);
//            mList.setDividerHeight(5);
//            mList.setDivider(getResources().getDrawable(R.drawable.divider_line));
            mAdapter = new TaskListAdapter(TaskListActivity.this);
            lazyload();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void doLazyload() {
        Log.d(TAG, "doInitView:mCategory=" + mCategory);
        if (Constants.CATEGORY_TASK.equals(mCategory)) {
            ArrayList<TaskBean> taskList = Utils.getPreloadedTaskList();
            boolean isLoaded = false;
            if(taskList != null && taskList.size() > 0) {
                Log.d(TAG,"preloaded task size = " + taskList.size());
                mAdapter.setData(TaskListAdapter.TYPE_NORMAL_TASK, taskList);
                isLoaded = true;
            }
            ArrayList<TaskBean> gzhTaskList = Utils.getPreloadedGzhTaskList();
            if(gzhTaskList != null && gzhTaskList.size() > 0) {
                Log.d(TAG,"preloaded gzh task size: " + gzhTaskList.size());
                mAdapter.setData(TaskListAdapter.TYPE_GZH_TAK, gzhTaskList);
                
                isLoaded = true;;
            }
            if(isLoaded == true) {
                Log.d(TAG,"set mIsEnd to true");
                mIsEnd = true;
                setLoadResult(true);
                return;
            }
            Log.d(TAG,"not preloaded task");
            MarketAPI.getTaskList(getApplicationContext(), this);
            MarketAPI.getGzhTaskList(getApplicationContext(), 
                    new GzhTaskListApiRequestListener());
        }
    }

    @Override
    public CommonAdapter doInitListAdapter() {
        return mAdapter;
    }

    @Override
    public boolean isEnd() {
        Log.d(TAG, "isEnd return " + mIsEnd);
        return mIsEnd;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(int method, Object obj) {
        Log.d(TAG, obj.toString());
        TaskListBean result = (TaskListBean) obj;
        if(result.getTasklist() != null) {
            Log.d(TAG, "size = " + result.getTasklist().size());
//            TaskBean bean = new TaskBean();
//            bean.setType(TaskBean.ITEM_TYPE_TITLE);
//            bean.setTitle(getResources().getString(R.string.title_task_todo));
            for(TaskBean item : result.getTasklist()) {
                //set remain num to 1 for normal task
                item.setRemain_tasknum(1);
                item.setTaskType(TaskListAdapter.TYPE_NORMAL_TASK);
            }
//            result.getTasklist().add(0, bean);
            mAdapter.setData(TaskListAdapter.TYPE_NORMAL_TASK,result.getTasklist());
            mAdapter.notifyDataSetChanged();
        }else {
            Log.d(TAG, "no data from server");
        }
        mIsEnd = true;
        setLoadResult(true);
    }

    @Override
    public void onError(int method, int statusCode) {
        if (statusCode == ApiAsyncTask.BUSSINESS_ERROR) {
            // 没有数据
        } else {
            // 超时
            mNoData.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.GONE);
        }
        setLoadResult(false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Activity parent = getParent();
        if (parent != null) {
            return parent.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        
        ArrayList<TaskBean> list = mAdapter.getData();
        if(list != null) {
            TaskBean item = list.get(position);
            String clickUrl = item.getClick_url();
            if(clickUrl != null && !clickUrl.equals("")) {
                openWebView(clickUrl, item.getName());
            }else {
                Log.w(TAG, "no click url");
                Intent intent = new Intent(TaskListActivity.this, GzhTaskDetailActivity.class);
                intent.putExtra(Constants.EXTRA_TASK_BEAN, item);
                startActivity(intent);
            }
            
        }else {
            Log.w(TAG, "list is null");
        }
        
        /*// 去产品详细页
        HashMap<String, Object> item = (HashMap<String, Object>) mAdapter
                .getItem(position);
        String pid = (String) item.get(Constants.KEY_PRODUCT_ID);
        Intent detailIntent = new Intent(getApplicationContext(),
                PreloadActivity.class);
        detailIntent.putExtra(Constants.EXTRA_PRODUCT_ID, pid);
        detailIntent.putExtra(Constants.EXTRA_CATEGORY, mCategory);
        startActivity(detailIntent);*/
    }

    @Override
    public void onClick(View v) {
        // 重试
        mProgress.setVisibility(View.VISIBLE);
        mNoData.setVisibility(View.GONE);
        lazyload();
    }

    private void openWebView(String url, String title) {
        Intent detailIntent = new Intent(getApplicationContext(), WebviewActivity.class);
        detailIntent.putExtra("extra.url", url);
        detailIntent.putExtra("extra.title", title);
        startActivity(detailIntent);
    }

    private class GzhTaskListApiRequestListener 
        implements ApiRequestListener {

        @Override
        public void onSuccess(int method, Object obj) {
            Log.d(TAG, obj.toString());
            TaskListBean result = (TaskListBean) obj;
            ArrayList<TaskBean> availableList = new ArrayList<TaskBean>();;
            ArrayList<TaskBean> finishedList = new ArrayList<TaskBean>();
            int titlePos = 0;
            if(result.getTasklist() != null) {
                Log.d(TAG, "ApiRequestListener size = " + result.getTasklist().size());
                for(int i = 0; i< result.getTasklist().size(); i++) {
                    TaskBean bean = result.getTasklist().get(i);
                    if (bean.getRemain_tasknum() == 0) {
                        finishedList.add(bean);                        
                    } else {
                        availableList.add(bean);
                    }
                }
                if (finishedList.size() > 0) {
                    availableList.addAll(finishedList);
                }
                Log.d(TAG, "onSuccess done");
                mAdapter.setData(TaskListAdapter.TYPE_GZH_TAK, availableList);
                mAdapter.notifyDataSetChanged();
            }else {
                Log.d(TAG, "no data from server");
            }
            mIsEnd = true;
            setLoadResult(true);
        }

        @Override
        public void onError(int method, int statusCode) {
            Log.d(TAG, "GzhTaskListApiRequestListener onError" + statusCode);
            
        }
        
    }

    @Override
    public void loadMore() {
        // TODO Auto-generated method stub
        
    }
}