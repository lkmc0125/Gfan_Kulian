package com.xiaohong.kulian.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import aga.fdf.grd.os.PointsChangeNotify;
import aga.fdf.grd.os.PointsManager;
import aga.fdf.grd.os.df.AdExtraTaskStatus;
import aga.fdf.grd.os.df.AppExtraTaskObject;
import aga.fdf.grd.os.df.AppExtraTaskObjectList;
import aga.fdf.grd.os.df.AppSummaryDataInterface;
import aga.fdf.grd.os.df.AppSummaryObject;
import aga.fdf.grd.os.df.AppSummaryObjectList;
import aga.fdf.grd.os.df.DiyOfferWallManager;

import java.util.ArrayList;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.adapter.ListViewAdapter;
import com.xiaohong.kulian.common.util.BitmapDownloadListener;
import com.xiaohong.kulian.common.util.BitmapLoaderManager;
import com.xiaohong.kulian.common.vo.CustomObject;
import com.xiaohong.kulian.common.widget.BaseActivity;
import com.xiaohong.kulian.common.widget.RefreshLayout;

public class OfferWallAdListActivity extends BaseActivity
        implements
            PointsChangeNotify,
            OnItemClickListener,
            BitmapDownloadListener {

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
    private int mRequestType;

    /**
     * 请求页码
     */
    private int mPageIndex = 1;

    private ListViewAdapter mLvAdapter;

    private RefreshLayout mSwipeRefreshLayout; // 上下拉刷新组件

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // 获取广告的类型，由MainActivity传入值
        // 如果获取失败就默认加载的广告的类型为所有，即不限
        mRequestType = getIntent().getIntExtra("requestType",
                DiyOfferWallManager.REQUEST_ALL);

        // 初始化View
        initView();

        // （可选）注册积分余额变动监听-随时随地获得积分的变动情况
        PointsManager.getInstance(this).registerNotify(this);

        // （可选）注册广告下载安装监听-随时随地获得应用下载安装状态的变动情况
        DiyOfferWallManager.getInstance(this).registerListener(mLvAdapter);

        // 发起列表请求
        pull2Refresh4RequestList();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // （可选）注销积分余额监听-如果在onCreate中注册了，那这里必须得注销
        PointsManager.getInstance(this).unRegisterNotify(this);

        // （可选）注销下载安装监听-如果在onCreate中注册了，那这里必须得注销
        DiyOfferWallManager.getInstance(this).removeListener(mLvAdapter);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        Intent intent = new Intent(this, OfferWallAdDetailActivity.class);
        intent.putExtra("ad", mLvAdapter.getItem(position)
                .getAppSummaryObject());
        startActivity(intent);
    }

    /**
     * 初始化View
     */
    private void initView() {

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
        /*
         * mSwipeRefreshLayout .setColorScheme(Color.parseColor("#ff00ddff"),
         * Color.parseColor("#ff99cc00"), Color.parseColor ("#ffffbb33"),
         * Color.parseColor("#ffff4444"));
         */
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        ListView listView = (ListView) findViewById(R.id.lv_addata);
        mLvAdapter = new ListViewAdapter(this, null); // 这里先让列表为空，待加载到数据再显示出来
        listView.setAdapter(mLvAdapter);
        listView.setOnItemClickListener(this);
    }

    /**
     * 监听积分变动，可以在这里进行更新界面显示等操作
     */
    @Override
    public void onPointBalanceChange(float pointsBalance) {
        // updatePoints(pointsBalance);
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
                                Toast.makeText(OfferWallAdListActivity.this,
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
                                        OfferWallAdListActivity.this,
                                        String.format("请求错误，错误代码 ： %d， 请联系客服",
                                                code), Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
                    }
                });

        // // 同步加载方式
        // new Thread(new Runnable() {
        // @Override
        // public void run() {
        // AppSummaryObjectList adList = null;
        // try {
        // adList =
        // DiyOfferWallManager.getInstance(OfferWallAdListActivity.this)
        // .getOfferWallAdList(mRequestType, mPageIndex, AD_PER_NUMBER);
        // updateListView(adList);
        // if (adList != null) {
        // updateLimitInfo(adList.getInstallLimit(), adList.getInstallTimes());
        // }
        // } catch (NetworkException e) {
        // Log.e("YoumiSdk", "", e);
        // runOnUiThread(new Runnable() {
        // @Override
        // public void run() {
        // // 如果是请求第一页的时候，请求失败，就致列表为空
        // if (mPageIndex == 1) {
        // mLvAdapter.reset();
        // mLvAdapter.notifyDataSetChanged();
        // }
        // mSwipeRefreshLayout.setRefreshing(false);
        // mSwipeRefreshLayout.setPushRefreshing(false);
        // Toast.makeText(OfferWallAdListActivity.this, "请求失败，请检查网络～",
        // Toast.LENGTH_LONG).show();
        // }
        // });
        // } catch (final ErrorCodeException e) {
        // Log.e("YoumiSdk", "", e);
        // runOnUiThread(new Runnable() {
        // @Override
        // public void run() {
        // // 如果是请求第一页的时候，请求失败，就致列表为空
        // if (mPageIndex == 1) {
        // mLvAdapter.reset();
        // mLvAdapter.notifyDataSetChanged();
        // }
        //
        // mSwipeRefreshLayout.setRefreshing(false);
        // mSwipeRefreshLayout.setPushRefreshing(false);
        // Toast.makeText(OfferWallAdListActivity.this,
        // String.format("请求错误，错误代码 ： %d， 请联系客服", e
        // .getErrCode()),
        // Toast.LENGTH_LONG).show();
        // }
        // });
        // }
        // }
        // }).start();

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
                    Toast.makeText(OfferWallAdListActivity.this,
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
                            OfferWallAdListActivity.this,
                            String.format(
                                    "请求成功\n请求页码：%s\n请求数量：%s\n实际返回数量:%s\n当天新任务限制:%s\n已安装的新任务数:%s",
                                    adList.getPageIndex(),
                                    adList.getPerPageNumber(), adList.size(),
                                    adList.getInstallLimit(),
                                    adList.getInstallTimes()),
                            Toast.LENGTH_LONG).show();
                }
            });

            // 获取需要加载的图片url地址，然后加载
            String[] iconUrlArray = new String[customObjectArrayList.size()];
            for (int i = 0; i < customObjectArrayList.size(); i++) {
                iconUrlArray[i] = customObjectArrayList.get(i)
                        .getAppSummaryObject().getIconUrl();
            }

            // 线程池异步加载图片
            BitmapLoaderManager.loadBitmap(this, this, iconUrlArray);

        }
    }

    /**
     * 每当有一张图片加载完成的时候就会回调这个接口（本接口在非UI线程中调用）
     */
    @Override
    public void onLoadBitmap(String url, Bitmap bm) {
        try {
            if (mLvAdapter == null || mLvAdapter.getData() == null
                    || mLvAdapter.getData().isEmpty()) {
                return;
            }

            for (int i = 0; i < mLvAdapter.getData().size(); i++) { // 显示app_icon
                if (url.equals(mLvAdapter.getItem(i).getAppSummaryObject()
                        .getIconUrl())) {
                    mLvAdapter.getItem(i).setAppicon(bm);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLvAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
