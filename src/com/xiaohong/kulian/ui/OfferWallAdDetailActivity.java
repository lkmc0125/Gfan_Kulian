package com.xiaohong.kulian.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.adapter.GridViewAdapter;
import com.xiaohong.kulian.adapter.ListViewAdapter_TaskDesc;
import com.xiaohong.kulian.common.util.BitmapDownloadListener;
import com.xiaohong.kulian.common.util.BitmapLoaderManager;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.vo.TaskDescObject;
import com.xiaohong.kulian.common.widget.BaseActivity;
import com.xiaohong.kulian.common.widget.CustomProgressBar;

import aga.fdf.grd.os.PointsChangeNotify;
import aga.fdf.grd.os.PointsManager;
import aga.fdf.grd.os.df.AdDownloadStatus;
import aga.fdf.grd.os.df.DiyAppNotify;
import aga.fdf.grd.os.df.AdExtraTaskStatus;
import aga.fdf.grd.os.df.AdTaskStatus;
import aga.fdf.grd.os.df.AppDetailDataInterface;
import aga.fdf.grd.os.df.AppDetailObject;
import aga.fdf.grd.os.df.AppExtraTaskObject;
import aga.fdf.grd.os.df.AppExtraTaskObjectList;
import aga.fdf.grd.os.df.AppSummaryObject;
import aga.fdf.grd.os.df.DiyOfferWallManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OfferWallAdDetailActivity extends BaseActivity
        implements
            OnClickListener,
            BitmapDownloadListener,
            DiyAppNotify,
            PointsChangeNotify {

    public AppSummaryObject appSumObject;

    private ImageView appIcon;

    private TextView rewardCount;

    private TextView appName;

    private TextView appVersion;
    
    private TextView appCoinNumTv;

    private TextView appDesc;

    private GridView gridView;

    private ListView listView;

    private GridViewAdapter gvAdapter;

    private ListViewAdapter_TaskDesc lvAdapter;

    private AppDetailObject appDetailObject;

    private ArrayList<Bitmap> bmLists;

    /**
     * 任务描述列表的数据列表
     */
    private ArrayList<TaskDescObject> mTaskDescList;

    private boolean isPackageExist = false;

    private final static Handler handler = new Handler();
    
    /**
     * 应用详情图片
     */
    
    private ArrayList<ImageView> mAppPicViews = new ArrayList<ImageView>();
    private ImageLoader mImageLoader;
    private RelativeLayout mHeaderViewLayout;
    /**
     * 下载条
     */
    private CustomProgressBar mProgressBar;
    /**
     * 返回
     */
    private LinearLayout back_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_offerwall_detail);

        mImageLoader = ImageLoader.getInstance();
        // 检查传入的Intent是否合法，不合法就直接finish
        Object obj = getIntent().getSerializableExtra("ad");
        if (obj == null || !(obj instanceof AppSummaryObject)) {
            this.finish();
            return;
        }
        appSumObject = (AppSummaryObject) obj;

        // 检查这个应用是否已经存在于手机中
        isPackageExist = Utils.isApkInstalled(this,
                appSumObject.getPackageName());

        // 初始化View
        initView();

        // （可选）注册积分余额变动监听-随时随地获得积分的变动情况
        PointsManager.getInstance(this).registerNotify(this);

        // （可选）注册广告下载安装监听-随时随地获得应用下载安装状态的变动情况
        DiyOfferWallManager.getInstance(this).registerListener(this);

        // 获取广告的详细数据
        requestDetailData();
        if (appDetailObject != null) {

            DiyOfferWallManager.getInstance(this).openOrDownloadApp(
                    this, appDetailObject);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // （可选）注销积分余额监听-如果在onCreate中注册了，那这里必须得注销
        PointsManager.getInstance(this).unRegisterNotify(this);

        // （可选）注销下载安装监听-如果在onCreate中注册了，那这里必须得注销
        DiyOfferWallManager.getInstance(this).removeListener(this);

    }

    private void initView() {

        mHeaderViewLayout = (RelativeLayout) findViewById(R.id.header_blur_layout);
        back_layout=(LinearLayout)findViewById(R.id.back_layout);
        back_layout.setOnClickListener(this);

        /**
         * modifyed by albert 2016/5/7 
         */
        mProgressBar = (CustomProgressBar) findViewById(R.id.download_progress_bar);
        mProgressBar.setOnClickListener(this);
        appIcon = (ImageView) findViewById(R.id.app_icon);
        appName = (TextView) findViewById(R.id.app_name_tv);
        appVersion = (TextView) findViewById(R.id.app_version_tv);
        appCoinNumTv = (TextView) findViewById(R.id.app_coin_num_tv);
        appDesc = (TextView) findViewById(R.id.app_desc_tv);
        gridView = (GridView) findViewById(R.id.detailpage_gridView);
        gvAdapter = new GridViewAdapter(OfferWallAdDetailActivity.this, null);
        gridView.setAdapter(gvAdapter);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));

        listView = (ListView) findViewById(R.id.detailpage_listview);
        listView.setEnabled(false);
        lvAdapter = new ListViewAdapter_TaskDesc(
                OfferWallAdDetailActivity.this, null);
        listView.setAdapter(lvAdapter);
    }

    /**
     * 获取广告的详细数据,下面展示两种加载方式，开发者可选择适合自己的方式
     */
    private void requestDetailData() {

//        mSwipeRefreshLayout.setRefreshing(true);
        // 异步加载方式
        DiyOfferWallManager.getInstance(this).loadAppDetailData(appSumObject,
                new AppDetailDataInterface() {

                    /**
                     * 当成功请求到数据的时候，会回调本方法（注意:本接口不在UI线程中执行，
                     * 所以请不要在本接口中进行UI线程方面的操作） 注意：广告详细数据有可能为空，开发者处理之前，请先判断是否为空
                     */
                    @Override
                    public void onLoadAppDetailDataSuccess(Context context,
                            AppDetailObject appDetailObject) {
                        updateView(appDetailObject);
                    }

                    /**
                     * 请求成功，但是返回有米错误代码时候，会回调这个接口
                     */
                    @Override
                    public void onLoadAppDetailDataFailedWithErrorCode(int code) {
                        notifyRequestFailed("请求错误，错误代码 ： %d， 请联系客服", code);
                    }

                    /**
                     * 因为网络问题而导致请求失败时，会回调这个接口（注意:本接口不在UI线程中执行，
                     * 所以请不要在本接口中进行UI线程方面的操作）
                     */
                    @Override
                    public void onLoadAppDetailDataFailed() {
                        notifyRequestFailed("请求失败，请检查网络");
                    }
                });

        // // 同步加载方式
        // new Thread(new Runnable() {
        // @Override
        // public void run() {
        // try {
        // AppDetailObject data =
        // DiyOfferWallManager.getInstance(OfferWallAdDetailActivity
        // .this).getAppDetailData(appSumObject);
        // updateView(data);
        // } catch (NetworkException e) {
        // Log.e("YoumiSdk", "", e);
        // notifyRequestFailed("请求失败，请检查网络");
        // } catch (ErrorCodeException e) {
        // Log.e("YoumiSdk", "", e);
        // notifyRequestFailed("请求错误，错误代码 ： %d， 请联系客服", e.getErrCode());
        // }
        // }
        // }).start();
    }

    private void notifyRequestFailed(final String format, final Object... args) {
        handler.post(new Runnable() {

            @Override
            public void run() {
//                mSwipeRefreshLayout.setRefreshing(false);
                new AlertDialog.Builder(OfferWallAdDetailActivity.this)
                        .setTitle("请求失败")
                        .setMessage(String.format(format, args)).create()
                        .show();
            }
        });
    }

    /**
     * 更新按钮状态
     */
    private void updateOpenOrDownloadButtonStatus(int status) {
        System.out.println("updateOpenOrDownloadButtonStatus"+status);
        switch (status) {
            
            case AdTaskStatus.NOT_COMPLETE : // 未完成

                if (isPackageExist) {
                    mProgressBar.setText("打开");
                    mProgressBar.setStatus(CustomProgressBar.Status.INSTALLED);
                } else {
                    mProgressBar.setText("下载安装(" + appSumObject.getAppSize() + ")");
                    mProgressBar.setStatus(CustomProgressBar.Status.INITIAL);
                }

                break;
            case AdTaskStatus.HAS_EXTRA_TASK : // 有追加任务

                boolean isExtraTaskCanDo = false; // 标记追加任务现在是否可以进行
                for (int i = 0; i < appSumObject.getExtraTaskList().size(); ++i) {
                    if (AdExtraTaskStatus.IN_PROGRESS == appSumObject
                            .getExtraTaskList().get(i).getStatus()) {
                        isExtraTaskCanDo = true;
                        break;
                    }
                }

                if (isPackageExist) {
                    mProgressBar.setText(isExtraTaskCanDo
                            ? "任务未完成，打开体验"
                                    : "任务等待中");
                    mProgressBar.setStatus(CustomProgressBar.Status.INSTALLED);
                } else {
                    mProgressBar.setText("下载安装(" + appSumObject.getAppSize() + ")");
                    mProgressBar.setStatus(CustomProgressBar.Status.INITIAL);
                }                
                break;
            case AdTaskStatus.ALREADY_COMPLETE : // 已完成
                if (isPackageExist) {
                    mProgressBar.setText("打开");
                    mProgressBar.setStatus(CustomProgressBar.Status.INSTALLED);
                } else {
                    mProgressBar.setText("重新安装");
                    mProgressBar.setStatus(CustomProgressBar.Status.INITIAL);
                }                
                break;
            default :
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.download_progress_bar:
                if (appDetailObject != null) {

                    DiyOfferWallManager.getInstance(this).openOrDownloadApp(
                            this, appDetailObject);
                }
                break;
            case R.id.back_layout:
                OfferWallAdDetailActivity.this.finish();

            default :
                break;
        }
    }

    /**
     * 构造任务描述列表
     *
     * @return
     */
    private void generateDestList() {
        if (mTaskDescList == null) {
            mTaskDescList = new ArrayList<TaskDescObject>();
        }

        // 1、将正常的任务加入到描述列表中
        int status = 0;
        if (appDetailObject.getAdTaskStatus() == AdTaskStatus.ALREADY_COMPLETE
                || appDetailObject.getAdTaskStatus() == AdTaskStatus.HAS_EXTRA_TASK) {
            status = AdExtraTaskStatus.COMPLETE; // 标记任务已经完成
        } else if (appDetailObject.getAdTaskStatus() == AdTaskStatus.NOT_COMPLETE) {
            status = AdExtraTaskStatus.IN_PROGRESS; // 标记任务可以进行
        }
        TaskDescObject normalTask = new TaskDescObject(status,
                appDetailObject.getTaskSteps(), appDetailObject.getPoints());
        mTaskDescList.add(normalTask);

        // 2、将追加任务加入到描述列表中
        AppExtraTaskObjectList extraTastkList = appDetailObject
                .getExtraTaskList();
        if (extraTastkList != null && extraTastkList.size() > 0) {
            for (int i = 0; i < extraTastkList.size(); ++i) {
                AppExtraTaskObject extraTaskObject = extraTastkList.get(i);
                TaskDescObject temp = new TaskDescObject(
                        extraTaskObject.getStatus(),
                        extraTaskObject.getAdText(),
                        extraTaskObject.getPoints());
                mTaskDescList.add(temp);
            }
        }
    }

    /**
     * 更新视图
     *
     * @param appDetailObject
     */
    private void updateView(final AppDetailObject detailData) {

        if (detailData != null) {
            this.appDetailObject = detailData;

            // 这里生成一下描述列表
            generateDestList();

            // 当获取到数据的时候，先设置默认图片，等后续图片下载完毕之后在更新
            bmLists = new ArrayList<Bitmap>();
            for (int i = 0; i < appDetailObject.getScreenShotUrls().length; i++) {
                bmLists.add(BitmapFactory.decodeResource(getResources(),
                        R.drawable.icon_default));
            }

            // 在主线程中更新数据
            handler.post(new Runnable() {

                @Override
                public void run() {
                    update_image();
                    appName.setText(appDetailObject.getAppName());
                    appCoinNumTv.setText("+" + Utils.getTotalPoints(appSumObject));
                    appVersion.setText(appDetailObject.getVersionName());;
                    appDesc.setText(appDetailObject.getDescription());
                    updateOpenOrDownloadButtonStatus(appDetailObject.getAdTaskStatus());
                    updateGridView(bmLists);
                    updateListView(mTaskDescList);
//                    mSwipeRefreshLayout.setRefreshing(false);
//                    rewardCount.setText("今天已有 "
//                            + appDetailObject.getRewardsCount() + " 个用户获得奖励");
                }
            });

            // 构造需要加载的图片url数组，当图片加载完毕的时候更新显示
            // 1、传入图标url
            int ssUrlsLength = appDetailObject.getScreenShotUrls().length;
            String[] imageUrlArray = new String[ssUrlsLength + 1];
            imageUrlArray[0] = appDetailObject.getIconUrl();

            // 2、传入截图url
            if (appDetailObject.getScreenShotUrls() != null) {
                System.arraycopy(appDetailObject.getScreenShotUrls(), 0,
                        imageUrlArray, 1, ssUrlsLength); // 传入截图地址
            }

            // 线程池异步加载图片
            BitmapLoaderManager.loadBitmap(this, this, imageUrlArray);
        }
    }

    @Override
    public void onLoadBitmap(String url, final Bitmap bm) {
        try {
            if (url.equals(appDetailObject.getIconUrl())) { // 显示app图标
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        appIcon.setImageBitmap(bm);
                    }
                });
            }
            for (int i = 0; i < appDetailObject.getScreenShotUrls().length; i++) { // 显示app截图
                if (url.equals(appDetailObject.getScreenShotUrls()[i])) {
                    bmLists.set(i, bm);
                    gvAdapter.setData(bmLists);
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            gvAdapter.notifyDataSetChanged();
                        }
                    });
                    break;
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    /**
     * 更新gridview显示app截图
     *
     * @param bmLists
     */
    private void updateGridView(ArrayList<Bitmap> bmLists) {
        if (bmLists != null && !bmLists.isEmpty()) {
            int colWidth = getResources().getDisplayMetrics().widthPixels / 2;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    bmLists.size() * colWidth, LayoutParams.WRAP_CONTENT);
            gridView.setLayoutParams(params);
            gridView.setColumnWidth(colWidth);
            gridView.setHorizontalSpacing(6);
            gridView.setStretchMode(GridView.NO_STRETCH);
            gridView.setNumColumns(bmLists.size());
            gridView.setVisibility(View.VISIBLE);
            gvAdapter.setData(bmLists);
            gvAdapter.notifyDataSetChanged();
        } else {
            gridView.setVisibility(View.GONE);
        }
    }

    /**
     * 更新listview显示任务描述
     *
     * @param bmLists
     */
    private void updateListView(List<TaskDescObject> list) {
        if (list != null && !list.isEmpty()) {
            listView.setVisibility(View.VISIBLE);
            lvAdapter.setData(list);
            lvAdapter.notifyDataSetChanged();
        } else {
            listView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDownloadStart(int id) {
        try {
            if (appDetailObject == null) {
                return;
            }

            if (appDetailObject.getAdId() != id) {
                return;
            }
            this.mProgressBar.setProgress(0);
            this.mProgressBar.setVisibility(View.VISIBLE);
            this.mProgressBar.setStatus(CustomProgressBar.Status.INITIAL);
            this.mProgressBar.setText("下载安装(" + appSumObject.getAppSize() + ")");
        } catch (Throwable e) {
            Log.d("Youmi", "", e);
        }
    }

    @Override
    public void onDownloadFailed(int id) {
        try {
            if (appDetailObject == null) {
                return;
            }

            if (appDetailObject.getAdId() != id) {
                return;
            }
            this.mProgressBar.setProgress(0);
            this.mProgressBar.setVisibility(View.VISIBLE);
            this.mProgressBar.setEnabled(true);
            this.mProgressBar.setText("下载失败,请稍候重试!");
        } catch (Throwable e) {
            Log.d("Youmi", "", e);
        }
    }

    @Override
    public void onDownloadSuccess(int id) {
        try {
            if (appDetailObject == null) {
                return;
            }

            if (appDetailObject.getAdId() != id) {
                return;
            }
            this.mProgressBar.setProgress(0);
            this.mProgressBar.setVisibility(View.VISIBLE);
            this.mProgressBar.setStatus(CustomProgressBar.Status.FINISHED);
            this.mProgressBar.setText("安装");
        } catch (Throwable e) {
            Log.d("Youmi", "", e);
        }
    }

    @Override
    public void onDownloadProgressUpdate(int id, long contentLength,
            long completeLength, int percent, long speedBytesPerS) {
        try {
            if (appDetailObject == null) {
                return;
            }

            if (appDetailObject.getAdId() != id) {
                return;
            }
            this.mProgressBar.setVisibility(View.VISIBLE);
            this.mProgressBar.setProgress(percent);
            this.mProgressBar.setText(getDisplayText(String.format(
                    "正在下载（%d%%）", percent)).toString());
            mProgressBar.setBackgroundColor(Color.parseColor("802f83e9"));
            mProgressBar.setStatus(CustomProgressBar.Status.PROCESSING);
        } catch (Throwable e) {
            Log.d("Youmi", "", e);
        }

    }
    private CharSequence getDisplayText(String str) {
        return Html.fromHtml("<font color='#2C78D4'>"+str+"</font>");
    }

    @Override
    public void onInstallSuccess(int id) {
        try {
            if (appDetailObject == null) {
                return;
            }

            if (appDetailObject.getAdId() != id) {
                return;
            }
            this.mProgressBar.setVisibility(View.VISIBLE);
            this.mProgressBar.setProgress(0);
            this.mProgressBar.setText("打开");
            this.mProgressBar.setStatus(CustomProgressBar.Status.INSTALLED);
        } catch (Throwable e) {
            Log.d("Youmi", "", e);
        }
    }

    @Override
    public void onPointBalanceChange(float arg0) {
        // TODO Auto-generated method stub

    }

    public void update_image(){
        mImageLoader.displayImage(appDetailObject.getIconUrl(), appIcon,
                Utils.sDisplayImageOptions, new ImageLoadingListener() {

                    @Override
                    public void onLoadingStarted(String arg0, View arg1) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onLoadingFailed(String arg0, View arg1,
                            FailReason arg2) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onLoadingComplete(String arg0, View arg1,
                            Bitmap arg2) {
                        appIcon.setImageBitmap(arg2);

                        new GaussBlurTask(getImageTopPart(arg2), mHeaderViewLayout)
                                .execute();
                    }

                    @Override
                    public void onLoadingCancelled(String arg0, View arg1) {
                        // TODO Auto-generated method stub

                    }
                });
        /*String[] pics = appDetailObject.getScreenShotUrls();
        int size = pics.length;
        // 最多显示5张
        if (size > 5) {
            size = 5;
        }
        System.out.println("appDetailObject.getScreenShotUrls()"+Arrays.toString(appDetailObject.getScreenShotUrls()));
        for (int i = 0; i < size; i++) {
            mImageLoader.displayImage(pics[i], mAppPicViews.get(i),
                    Utils.sDisplayImageOptions);
            mAppPicViews.get(i).setVisibility(View.VISIBLE);
        }*/
    }
    
    public static Bitmap getImageTopPart(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = w/5;
        return Bitmap.createBitmap(bitmap, w/4, 0, w/2, h, null, false);
    }
    
    private class GaussBlurTask extends AsyncTask<Void, Void, Bitmap> {

        private Bitmap mBitmap;
        private View mDstView;

        public GaussBlurTask(Bitmap bitmap, View dstView) {
            mBitmap = bitmap;
            mDstView = dstView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            return Utils.doFastBlur(mBitmap, 25, false);
            //return Utils.blurBitmap(getApplicationContext(), mBitmap);
        }

        @SuppressLint("NewApi")
        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mDstView.setBackground(new BitmapDrawable(result));
            } else {
                mDstView.setBackgroundDrawable(new BitmapDrawable(result));
            }
        }
    }

}
