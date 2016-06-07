package com.xiaohong.kulian.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

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
import com.xiaohong.kulian.bean.AppListBean;
import com.xiaohong.kulian.bean.TaskBean;
import com.xiaohong.kulian.bean.TaskListBean;
import com.xiaohong.kulian.common.ApiAsyncTask;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.vo.DownloadInfo;
import com.xiaohong.kulian.common.widget.LazyloadListActivity;
import com.xiaohong.kulian.common.widget.LoadingDrawable;

public class TaskListActivity extends LazyloadListActivity implements
        ApiRequestListener, OnItemClickListener, OnClickListener,
        Observer{

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

        ArrayList<AppBean> appList = Utils.getPreloadedAppList();
        if (appList != null && appList.size() > 0) {
            Log.d(TAG,"app preloaded size = " + appList.size());
            mIsEnd = appList.size() < 10;
            ArrayList<Object> array = new ArrayList<Object>();
            for (int i = 0; i < appList.size(); i++) {
                array.add((Object)appList.get(i));
            }
            mAdapter.setAppData(array);
        } else {
            MarketAPI.getAppList(getApplicationContext(), this, getStartPage(), Constants.CATEGORY_RCMD);
        }

        ArrayList<TaskBean> taskList = Utils.getPreloadedTaskList();
        boolean isLoaded = false;
        if (taskList != null && taskList.size() > 0) {
            Log.d(TAG,"preloaded task size = " + taskList.size());
            ArrayList<Object> array = new ArrayList<Object>();
            for (int i = 0; i < taskList.size(); i++) {
                array.add((Object)taskList.get(i));
            }
            mAdapter.setTaskData(TaskBean.ITEM_TYPE_WEB_TASK, array);
            isLoaded = true;
        }
        ArrayList<TaskBean> gzhTaskList = Utils.getPreloadedGzhTaskList();
        if (gzhTaskList != null && gzhTaskList.size() > 0) {
            Log.d(TAG,"preloaded gzh task size: " + gzhTaskList.size());
            ArrayList<Object> array = new ArrayList<Object>();
            for (int i = 0; i < gzhTaskList.size(); i++) {
                array.add((Object)gzhTaskList.get(i));
            }
            mAdapter.setTaskData(TaskBean.ITEM_TYPE_GZH_TASK, array);
            isLoaded = true;
        }
        if (isLoaded == true) {
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
        switch (method) {
        case MarketAPI.ACTION_GET_APP_LIST :
        {
            AppListBean appList = (AppListBean) obj;
            ArrayList<Object> array = new ArrayList<Object>();
            for (AppBean bean : appList.getApplist()) {
                if (Utils.isApkInstalled(getApplicationContext(), bean.getPackageName()) == true) {
                    bean.setIsInstalled(true);
                } else if (Utils.isApkDownloaded(bean.getAppName())) {
                    /**
                     * only if the app is not installed , shall we check if it's downloaded
                     * 
                     */
                    bean.setDownloaded(true);
                }
                array.add((Object)bean);
            }
            mAdapter.setAppData(array);
            mAdapter.notifyDataSetChanged();
            break;
        }
        case MarketAPI.ACTION_GET_TASK_LIST:
        {
            Log.d(TAG, obj.toString());
            TaskListBean result = (TaskListBean) obj;
            if (result.getTasklist() != null) {
                Log.d(TAG, "size = " + result.getTasklist().size());
                for (TaskBean item : result.getTasklist()) {
                    //set remain num to 1 for normal task
                    item.setRemain_tasknum(1);
                    item.setTaskType(TaskBean.ITEM_TYPE_WEB_TASK);
                }
                ArrayList<Object> array = new ArrayList<Object>();
                for (int i = 0; i < result.getTasklist().size(); i++) {
                    array.add((Object)(result.getTasklist().get(i)));
                }
                mAdapter.setTaskData(TaskBean.ITEM_TYPE_WEB_TASK, array);
                mAdapter.notifyDataSetChanged();
            } else {
                Log.d(TAG, "no data from server");
            }
            mIsEnd = true;
            setLoadResult(true);
            break;
        }
        default:
            break;
        }
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        ArrayList<Object> list = mAdapter.getData();
        if (list != null) {
            Object obj = list.get(position);
            if (obj instanceof TaskBean) {
                TaskBean item = (TaskBean)obj; 
                String clickUrl = item.getClick_url();
                if (clickUrl != null && !clickUrl.equals("")) {
                    openWebView(clickUrl, item.getName());
                } else {
                    if (!mSession.isLogin()) {
                        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(TaskListActivity.this, GzhTaskDetailActivity.class);
                        intent.putExtra(Constants.EXTRA_TASK_BEAN, item);
                        startActivity(intent);
                    }
                }
            } else if (obj instanceof AppBean) {
                AppBean item = (AppBean) mAdapter.getItem(position);
                // 去产品详细页
                String pid = item.getAppId() + "";
                Intent detailIntent = new Intent(getApplicationContext(),
                        AppDetailActivity.class);
                detailIntent.putExtra(Constants.EXTRA_PRODUCT_ID, pid);
                detailIntent.putExtra(Constants.EXTRA_CATEGORY, Constants.CATEGORY_RCMD);
                detailIntent.putExtra(Constants.EXTRA_COIN_NUM, item.getGiveCoin());
                detailIntent.putExtra(Constants.EXTRA_PACKAGE_NAME, item.getPackageName());
                startActivity(detailIntent);
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

    private class GzhTaskListApiRequestListener implements ApiRequestListener {

        @Override
        public void onSuccess(int method, Object obj) {
            switch (method) {
            case MarketAPI.ACTION_GET_GZH_TASK_LIST:
                Log.d(TAG, obj.toString());
                TaskListBean result = (TaskListBean) obj;
                ArrayList<TaskBean> availableList = new ArrayList<TaskBean>();;
                ArrayList<TaskBean> finishedList = new ArrayList<TaskBean>();
                if (result.getTasklist() != null) {
                    Log.d(TAG, "ApiRequestListener size = " + result.getTasklist().size());
                    for (int i = 0; i< result.getTasklist().size(); i++) {
                        TaskBean bean = result.getTasklist().get(i);
                        bean.setTaskType(TaskBean.ITEM_TYPE_GZH_TASK);
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
                    ArrayList<Object> array = new ArrayList<Object>();
                    for (int i = 0; i < availableList.size(); i++) {
                        array.add((Object)availableList.get(i));
                    }
                    mAdapter.setTaskData(TaskBean.ITEM_TYPE_GZH_TASK, array);
                    mAdapter.notifyDataSetChanged();
                } else {
                    Log.d(TAG, "no data from server");
                }
                mIsEnd = true;
                setLoadResult(true);
                break;
            default:
                break;
            }
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

    @Override
    protected void onPause() {
        mSession.deleteObserver(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSession.addObserver(this);
    }

    /**
     * Implements Observer
     */
    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof HashMap) {
            HashMap<String, DownloadInfo> downloadingTask = 
                    (HashMap<String, DownloadInfo>) data;
            //The map's key is package name and value is DownloadInfo
            mAdapter.setDownloadingTaskMap(downloadingTask);
        }
        
        
    }
}