package com.xiaohong.kulian.ui;

import java.util.ArrayList;

import aga.fdf.grd.os.PointsChangeNotify;
import aga.fdf.grd.os.PointsManager;
import aga.fdf.grd.os.df.AdExtraTaskStatus;
import aga.fdf.grd.os.df.AppExtraTaskObject;
import aga.fdf.grd.os.df.AppExtraTaskObjectList;
import aga.fdf.grd.os.df.AppSummaryDataInterface;
import aga.fdf.grd.os.df.AppSummaryObject;
import aga.fdf.grd.os.df.AppSummaryObjectList;
import aga.fdf.grd.os.df.DiyOfferWallManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.adapter.ListViewAdapter;
import com.xiaohong.kulian.common.download.DownloadManager;
import com.xiaohong.kulian.common.vo.CustomObject;
import com.xiaohong.kulian.common.widget.BaseActivity;
import com.xiaohong.kulian.common.widget.RefreshLayout;

public class ProductListActivity extends BaseActivity implements 
        OnItemClickListener, OnClickListener, PointsChangeNotify {

    private static final String TAG = "ProductListActivity"; 
    //private FrameLayout mLoading;
    //private ProgressBar mProgress;
    //private TextView mNoData;
    //private TabAppListAdapter mAdapter;
    private String mCategory;
    
    protected ListView mList;
    /**
     * 每页请求数量
     */
    private final static int AD_PER_NUMBER = 10;

    /**
     * 请求广告类型
     * <ul>
     * <li>{@link DiyOfferWallManager#REQUEST_ALL} : 请求所有，游戏先于应用展示</li>
     * <li>{@link DiyOfferWallManager#REQUEST_SPECIAL_SORT} : 请求所有，应用先于游戏展示</li>
     * <li>{@link DiyOfferWallManager#REQUEST_APP} : 只请求应用广告</li>
     * <li>{@link DiyOfferWallManager#REQUEST_GAME} : 只请求游戏广告</li>
     * <li>{@link DiyOfferWallManager#REQUEST_EXTRA_TASK} : 请求追加任务列表</li>
     * </ul>
     */
    private int mRequestType = DiyOfferWallManager.REQUEST_ALL;

    /**
     * 请求页码
     */
    private int mPageIndex = 1;

    private ListViewAdapter mLvAdapter;

    private RefreshLayout mSwipeRefreshLayout; // 上下拉刷新组件

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
//                    Toast.makeText(context, "运行成功: " + applicationName, Toast.LENGTH_LONG).show();
                    mSession.reportAppLaunched(applicationInfo.packageName);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void registerAppLanch() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_FIRST_LAUNCH);
        filter.addDataScheme("package");
        registerReceiver(mAppLanchReceiver, filter);
    }


    public void initViews() {
        Intent intent = getIntent();
        if (intent != null) {

            //registerAppLanch();
            mCategory = intent.getStringExtra(Constants.EXTRA_CATEGORY);
            // 获取广告的类型，由其他Activity传入值
            // 如果获取失败就默认加载的广告的类型为所有，即不限
            mRequestType = getIntent().getIntExtra("requestType",
                    DiyOfferWallManager.REQUEST_ALL);
            if (TextUtils.isEmpty(mCategory)) {
                intent.getIntExtra(Constants.EXTRA_SORT_TYPE, 1);
                intent.getStringExtra(Constants.EXTRA_CATEGORY_ID);
            }
           
            mSwipeRefreshLayout = (RefreshLayout) findViewById(R.id.sr_ad_list);
            mSwipeRefreshLayout
                    .setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

                        @Override
                        public void onRefresh() {
                            pull2Refresh4RequestList();
                        }
                    });
            mSwipeRefreshLayout
                    .setOnPushRefreshListener(new RefreshLayout.OnPushRefreshListener() {
                        @Override
                        public void onPushRefresh() {
                            push2Refresh4RequestList();
                        }
                    });

            mSwipeRefreshLayout.setProgressViewOffset(false, 0, (int) TypedValue
                    .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
                            .getDisplayMetrics()));
            mSwipeRefreshLayout.setColorSchemeResources(
                    android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);
            
            mList = (ListView) findViewById(R.id.lv_addata);
            //mLoading = (FrameLayout) findViewById(R.id.loading);
            //mProgress = (ProgressBar) mLoading.findViewById(R.id.progressbar);
            //mProgress.setIndeterminateDrawable(new LoadingDrawable(getApplicationContext()));
            //mProgress.setVisibility(View.VISIBLE);
            //mNoData = (TextView) mLoading.findViewById(R.id.no_data);
            //mNoData.setOnClickListener(this);
            //mList.setEmptyView(mLoading);
            mList.setOnItemClickListener(this);
            mLvAdapter = new ListViewAdapter(this, null); // 这里先让列表为空，待加载到数据再显示出来
            mList.setAdapter(mLvAdapter);
            mList.setOnItemClickListener(this);
            
           // （可选）注册积分余额变动监听-随时随地获得积分的变动情况
            PointsManager.getInstance(this).registerNotify(this);

            // （可选）注册广告下载安装监听-随时随地获得应用下载安装状态的变动情况
            DiyOfferWallManager.getInstance(this).registerListener(mLvAdapter);

            // 发起列表请求
            pull2Refresh4RequestList();
        } else {
        }
    }

    /*@Override
    public void doLazyload() {
        ArrayList<AppBean> appList = Utils.getPreloadedAppList();
        if(appList != null && appList.size() > 0) {
            Log.d(TAG,"app preloaded size = " + appList.size());
            mIsEnd = appList.size() < 10;
            //mAdapter.clearData();
            //mAdapter.addData(appList);
            //mAdapter.notifyDataSetChanged();
            setLoadResult(true);
            return;
        }
        try {
            throw new Exception("test");
        }catch(Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG,"no preloaded");
        //MarketAPI.getAppList(getApplicationContext(), this, getStartPage(), mCategory);
    }

    @Override
    public CommonAdapter doInitListAdapter() {
        
        //mLvAdapter.setProductList();
        return mLvAdapter;
    }
    
    @Override
    public boolean isEnd() {
        return mIsEnd;
    }*/
    
    /*@SuppressWarnings("unchecked")
    @Override
    public void onSuccess(int method, Object obj) {
        Log.d(TAG, "onSuccess product");
 
        //HashMap<String, Object> result = (HashMap<String, Object>) obj;

        AppListBean appList = (AppListBean) obj;
        ArrayList<AppBean> list = appList.getApplist();
        mIsEnd = list.size() < 10;
        for(AppBean bean : list) {
            if(Utils.isApkInstalled(getApplicationContext(),
                    bean.getPackageName()) ==  true) {
                bean.setIsInstalled(true);
            }else if(Utils.isApkDownloaded(bean.getAppName())) {
                *//**
                 * only if the app is not installed , shall we check if it's downloaded
                 * 
                 *//*
                bean.setDownloaded(true);
                
            }
        }
        //mAdapter.addData(list);
        setLoadResult(true);
    }

    @Override
    public void onError(int method, int statusCode) {
        if(statusCode == ApiAsyncTask.BUSSINESS_ERROR) {
            // 没有数据
        } else {
            // 超时
            //mNoData.setVisibility(View.VISIBLE);
            //mProgress.setVisibility(View.GONE);
        }
        setLoadResult(false);
    }*/
    
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

       /* AppBean item = (AppBean) mAdapter.getItem(position);
        // 去产品详细页
        String pid = item.getAppId() + "";
        Intent detailIntent = new Intent(getApplicationContext(),
                AppDetailActivity.class);
        detailIntent.putExtra(Constants.EXTRA_PRODUCT_ID, pid);
        detailIntent.putExtra(Constants.EXTRA_CATEGORY, mCategory);
        detailIntent.putExtra(Constants.EXTRA_COIN_NUM, item.getGiveCoin());
        detailIntent.putExtra(Constants.EXTRA_PACKAGE_NAME, item.getPackageName());
        startActivity(detailIntent);*/
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
        Intent intent = new Intent(this, OfferWallAdDetailActivity.class);
        intent.putExtra("ad", mLvAdapter.getItem(position)
                .getAppSummaryObject());
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        // 重试
        //mProgress.setVisibility(View.VISIBLE);
        //mNoData.setVisibility(View.GONE);
        //doLazyload();
    }


   /* @Override
    public void loadMore() {
        Log.d(TAG, "load more data");
        MarketAPI.getAppList(getApplicationContext(), this, getStartPage(), mCategory);
    }*/

    /*@Override
    public void onAppInstalled(String packageName) {
        //mAdapter.updateAppStatus(packageName , TabAppListAdapter.APP_STATUS_INSTALLED);
    }*/
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mSession.addOnAppInstalledListener(this);
        regReceiver();
        setContentView(R.layout.activity_app_list);
        initViews();
    }
    
    @Override
    protected void onDestroy() {
        //mSession.removeOnAppInstalledListener(this);
        unregReceiver();
        super.onDestroy();
    }
    
    private BroadcastReceiver mDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String filePath = intent.getStringExtra("FILENAME");
            Log.d(TAG, "mDownloadReceiver onReceive filePath = " + filePath);
            /*if (filePath != null && mAdapter != null) {
                String packageName = Utils.getPackageName(getApplicationContext(), filePath); 
                if(packageName != null) {
                    mAdapter.updateAppStatus(packageName, TabAppListAdapter.APP_STATUS_DOWNLOADED);
                }
            }*/
        }
    };
    
    private void regReceiver() {
        IntentFilter downloadFilter = new IntentFilter();
        downloadFilter.addAction(DownloadManager.BROADCAST_DOWNLOAD_APP_SUCCESS);
        registerReceiver(mDownloadReceiver, downloadFilter);
        registerAppLanch();
    }
    
    private void unregReceiver() {
        unregisterReceiver(mDownloadReceiver);
        unregisterReceiver(mAppLanchReceiver);
    }
    
    /**
     * 下拉更新广告列表
     */
    private void pull2Refresh4RequestList() {
        mPageIndex = 1;
        mLvAdapter.reset();
        mSwipeRefreshLayout.setRefreshing(true);
        requestList();
    }

    /**
     * 上拉更新广告列表
     */
    private void push2Refresh4RequestList() {
        ++mPageIndex;
        mSwipeRefreshLayout.setPushRefreshing(true);
        requestList();
    }
    
    /**
     * 发起列表请求
     */
    private void requestList() {

        // 获取指定类型 的广告，并更新listview，下面展示两种加载方式，开发者可选择适合自己的方式

        // 异步加载方式
        // 请求类型，页码，请求数量，回调接口
        DiyOfferWallManager.getInstance(this).loadOfferWallAdList(mRequestType,
                mPageIndex, AD_PER_NUMBER, new AppSummaryDataInterface() {

                    /**
                     * 当成功获取到积分墙列表数据的时候，会回调这个方法（注意:本接口不在UI线程中执行，
                     * 所以请不要在本接口中进行UI线程方面的操作）
                     * 注意：列表数据有可能为空（比如：没有广告的时候），开发者处理之前，请先判断列表是否为空，大小是否大与0
                     */
                    @Override
                    public void onLoadAppSumDataSuccess(Context context,
                            AppSummaryObjectList adList) {
                        updateListView(adList);
                        if (adList != null) {
                            updateLimitInfo(adList.getInstallLimit(),
                                    adList.getInstallTimes());
                        }
                    }

                    /**
                     * 因为网络问题而导致请求失败时，会回调这个接口（注意:本接口不在UI线程中执行，
                     * 所以请不要在本接口中进行UI线程方面的操作）
                     */
                    @Override
                    public void onLoadAppSumDataFailed() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 如果是请求第一页的时候，请求失败，就致列表为空
                                if (mPageIndex == 1) {
                                    mLvAdapter.reset();
                                    mLvAdapter.notifyDataSetChanged();
                                }
                                mSwipeRefreshLayout.setRefreshing(false);
                                mSwipeRefreshLayout.setPushRefreshing(false);
                                Toast.makeText(ProductListActivity.this,
                                        "请求失败，请检查网络～", Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
                    }

                    /**
                     * 请求成功，但是返回有米错误代码时候，会回调这个接口（注意:本接口不在UI线程中执行，
                     * 所以请不要在本接口中进行UI线程方面的操作）
                     */
                    @Override
                    public void onLoadAppSumDataFailedWithErrorCode(
                            final int code) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 如果是请求第一页的时候，请求失败，就致列表为空
                                if (mPageIndex == 1) {
                                    mLvAdapter.reset();
                                    mLvAdapter.notifyDataSetChanged();
                                }
                                mSwipeRefreshLayout.setRefreshing(false);
                                mSwipeRefreshLayout.setPushRefreshing(false);
                                Toast.makeText(
                                        ProductListActivity.this,
                                        String.format("请求错误，错误代码 ： %d， 请联系客服",
                                                code), Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
                    }
                });
    }
    
    /**
     * 更新用户当前可做的任务数
     * <p/>
     * 计算当前还可以做多少个处于《未完成状态》广告：安装上限-今天已经安装过的数量
     *
     * @param installLimit
     *            当天新任务安装限制
     * @param installTimes
     *            当天已经完成的新任务数量
     */
    private void updateLimitInfo(final int installLimit, final int installTimes) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                StringBuilder sb = new StringBuilder();
                sb.append("可做任务数：");
                sb.append(installLimit - installTimes);
                // updateLimitInfo(sb.toString());
            }
        });
    }

    /**
     * 更新listview
     *
     * @param adList
     */
    private void updateListView(final AppSummaryObjectList adList) {
        if (adList == null || adList.isEmpty()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(false);
                    mSwipeRefreshLayout.setPushRefreshing(false);
                    Toast.makeText(ProductListActivity.this,
                            "没有获取到更多的任务，晚点在来吧~", Toast.LENGTH_LONG).show();
                }
            });
        } else {

            ArrayList<CustomObject> customObjectArrayList = new ArrayList<CustomObject>();
            for (int k = 0; k < adList.size(); ++k) {

                // 如果请求的是追加任务的列表，demo将会把所有的追加任务独立为一个item项，因此需要把同一个appSummaryObject多次加入到列表中
                if (mRequestType == DiyOfferWallManager.REQUEST_EXTRA_TASK) {
                    // 下面是判断是否追加任务，如果是的话就会在写入一次列表
                    AppSummaryObject appSummaryObject = adList.get(k);
                    AppExtraTaskObjectList extraTaskObjectList = appSummaryObject
                            .getExtraTaskList();
                    for (int j = 0; j < extraTaskObjectList.size(); ++j) {
                        AppExtraTaskObject extraTaskObject = extraTaskObjectList
                                .get(j);
                        if (extraTaskObject.getStatus() == AdExtraTaskStatus.NOT_START
                                || extraTaskObject.getStatus() == AdExtraTaskStatus.IN_PROGRESS) {
                            CustomObject customObject = new CustomObject();
                            customObject.setAppSummaryObject(adList.get(k));
                            customObject.setAppicon(null);
                            customObject.setShowMultSameAd(true);
                            customObject.setShowExtraTaskIndex(j);
                            customObjectArrayList.add(customObject);
                        }
                    }
                } else {
                    CustomObject customObject = new CustomObject();
                    customObject.setAppSummaryObject(adList.get(k));
                    customObject.setAppicon(null);
                    customObjectArrayList.add(customObject);
                }
            }
            mLvAdapter.addData(customObjectArrayList);

            // 获取到数据之后向ui线程中handler发送更新view的信息（这里先显示文字信息，后续加载图片）
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(false);
                    mSwipeRefreshLayout.setPushRefreshing(false);
                    mLvAdapter.notifyDataSetChanged();
                    Toast.makeText(
                            ProductListActivity.this,
                            String.format(
                                    "请求成功\n请求页码：%s\n请求数量：%s\n实际返回数量:%s\n当天新任务限制:%s\n已安装的新任务数:%s",
                                    adList.getPageIndex(),
                                    adList.getPerPageNumber(), adList.size(),
                                    adList.getInstallLimit(),
                                    adList.getInstallTimes()),
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    //implemetns PointsChangeNotify
    @Override
    public void onPointBalanceChange(float arg0) {
        // TODO Auto-generated method stub
        
    }

}