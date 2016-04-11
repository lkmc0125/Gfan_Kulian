package com.xiaohong.kulian.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.Session;
import com.xiaohong.kulian.SessionManager;
import com.xiaohong.kulian.adapter.CommonAdapter;
import com.xiaohong.kulian.bean.AppBean;
import com.xiaohong.kulian.bean.AppListBean;
import com.xiaohong.kulian.common.ApiAsyncTask;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.widget.LazyloadListActivity;
import com.xiaohong.kulian.common.widget.LoadingDrawable;
import com.xiaohong.kulian.common.widget.TabAppListAdapter;

public class ProductListActivity extends LazyloadListActivity implements ApiRequestListener,
        OnItemClickListener, OnClickListener {

    private static final String TAG = "ProductListActivity"; 
    private FrameLayout mLoading;
    private ProgressBar mProgress;
    private TextView mNoData;
    private TabAppListAdapter mAdapter;
    private String mCategory;
    private boolean mIsEnd;


    private BroadcastReceiver mAppInstallReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String packageName = null;
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)
                    || intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
                packageName = intent.getData().getSchemeSpecificPart();
                ApplicationInfo applicationInfo = null;
                PackageManager packageManager = null;
                try {
                    packageManager = context.getPackageManager();
                    applicationInfo = packageManager.getApplicationInfo(packageName, 0);
                    String applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
                    Log.d(TAG, "installed [" + applicationName + "] pkg-name: " + applicationInfo.packageName);
//                    String appId = mDownloadAppInfoHashMap.get(applicationInfo.packageName);
//                    if (appId != null) {
                        Toast.makeText(context, "安装成功: " + applicationName, Toast.LENGTH_LONG).show();
//                        webView.loadUrl("javascript: appInstallFinished(" + appId + ")");
//                        mDownloadAppInfoHashMap.remove(applicationName);
//                    }
                      mSession.addInstalledApp(applicationInfo.packageName);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    };
	    
    private BroadcastReceiver mAppLanchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String packageName = null;
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_FIRST_LAUNCH)) {
                packageName = intent.getData().getSchemeSpecificPart();

                ApplicationInfo applicationInfo = null;
                PackageManager packageManager = null;
                try {
                    packageManager = context.getPackageManager();
                    applicationInfo = packageManager.getApplicationInfo(packageName, 0);
                    String applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
                    Log.d(TAG, "Lanched [" + applicationName + "] pkg-name: "   + applicationInfo.packageName);
                    Toast.makeText(context, "运行成功: " + applicationName, Toast.LENGTH_LONG).show();
                    mSession.reportAppLaunched(applicationInfo.packageName);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    };
	
    private void registerAppInstall() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addDataScheme("package");
        registerReceiver(mAppInstallReceiver, filter);
    }

    private void registerAppLanch() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_FIRST_LAUNCH);
        filter.addDataScheme("package");
        registerReceiver(mAppLanchReceiver, filter);
    }

    @Override
    public boolean doInitView(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent != null) {
            registerAppInstall();
            registerAppLanch();
            mCategory = intent.getStringExtra(Constants.EXTRA_CATEGORY);
            if (TextUtils.isEmpty(mCategory)) {
                intent.getIntExtra(Constants.EXTRA_SORT_TYPE, 1);
                intent.getStringExtra(Constants.EXTRA_CATEGORY_ID);
            }
            setContentView(R.layout.common_list_view);
            mIsEnd = false;
            mList = (ListView) findViewById(R.id.list);
            mLoading = (FrameLayout) findViewById(R.id.loading);
            mProgress = (ProgressBar) mLoading.findViewById(R.id.progressbar);
            mProgress.setIndeterminateDrawable(new LoadingDrawable(getApplicationContext()));
            mProgress.setVisibility(View.VISIBLE);
            mNoData = (TextView) mLoading.findViewById(R.id.no_data);
            mNoData.setOnClickListener(this);
            mList.setEmptyView(mLoading);
            mList.setOnItemClickListener(this);
            mAdapter = new TabAppListAdapter(getApplicationContext(),
                    null,
                    R.layout.common_product_list_item);
            
            lazyload();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void doLazyload() {
        ArrayList<AppBean> appList = Utils.getPreloadedAppList();
        if(appList != null && appList.size() > 0) {
            Log.d(TAG,"preloaded");
            mIsEnd = appList.size() < 10;
            mAdapter.addData(appList);
            setLoadResult(true);
            return;
        }
        Log.d(TAG,"no preloaded");
        MarketAPI.getAppList(getApplicationContext(), this, getStartPage(), mCategory);
    }

    @Override
    public CommonAdapter doInitListAdapter() {
        
        mAdapter.setProductList();
        return mAdapter;
    }
    
    @Override
    public boolean isEnd() {
        return mIsEnd;
    }
    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(int method, Object obj) {
 
        //HashMap<String, Object> result = (HashMap<String, Object>) obj;

        AppListBean appList = (AppListBean) obj;
        ArrayList<AppBean> list = appList.getApplist();
        mIsEnd = list.size() < 10;
        for(AppBean bean : list) {
            if(Utils.isApkInstalled(getApplicationContext(),
                    bean.getPackageName()) ==  true) {
                bean.setIsInstalled(true);
            }
        }
        mAdapter.addData(list);
        setLoadResult(true);
    }

    @Override
    public void onError(int method, int statusCode) {
        if(statusCode == ApiAsyncTask.BUSSINESS_ERROR) {
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
        if(parent != null) {
            return parent.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        AppBean item = (AppBean) mAdapter.getItem(position);
        // 去产品详细页
        String pid = item.getAppId() + "";
        Intent detailIntent = new Intent(getApplicationContext(),
                AppDetailActivity.class);
        detailIntent.putExtra(Constants.EXTRA_PRODUCT_ID, pid);
        detailIntent.putExtra(Constants.EXTRA_CATEGORY, mCategory);
        detailIntent.putExtra(Constants.EXTRA_COIN_NUM, item.getGiveCoin());
        startActivity(detailIntent);
        /*if (item.isIsInstalled()) {
            //打开已安装应用
            Utils.openApkByPackageName(getApplicationContext(),
                    item.getPackageName());
        } else {
            // 去产品详细页
            String pid = item.getAppId() + "";
            Intent detailIntent = new Intent(getApplicationContext(),
                    PreloadActivity.class);
            Intent detailIntent = new Intent(getApplicationContext(),
                    AppDetailActivity.class);
            detailIntent.putExtra(Constants.EXTRA_PRODUCT_ID, pid);
            detailIntent.putExtra(Constants.EXTRA_CATEGORY, mCategory);
            detailIntent.putExtra(Constants.EXTRA_COIN_NUM, item.getGiveCoin());
            startActivity(detailIntent);
        }*/
    }

    @Override
    public void onClick(View v) {
        // 重试
        mProgress.setVisibility(View.VISIBLE);
        mNoData.setVisibility(View.GONE);
        lazyload();
    }
}