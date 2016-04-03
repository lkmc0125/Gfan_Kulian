package com.xiaohong.kulian.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Session;
import com.xiaohong.kulian.bean.AppDetailBean;
import com.xiaohong.kulian.bean.DetailInfo;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.download.DownloadManager;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.vo.DownloadInfo;

public class AppDetailActivity extends Activity
        implements
            OnClickListener,
            Observer {
    private static final String TAG = "AppDetailActivity";
    
    private static final String TEXT_CONTINUE = "继续";
    private static final String TEXT_OPEN = "打开";
    private static final String TEXT_INSTALL = "安装";
    
    /**
     * 已安装等待打开
     */
    private static final int STATUS_WAITING_OPEN = 1;
    
    /**下载完毕等待安装
     */
    private static final int STATUS_WAITING_INSTALL = 2;
    
    /**
     * 从未下载过等待开始下载
     */
    private static final int STATUS_WAITING_DOWNLOAD = 3;
    
    /**
     * 下载中
     */
    private static final int STATUS_DOWNLOADING = 4;
    
    /**
     * 暂停中
     */
    private static final int STATUS_PAUSE = 5;
    
    private int mStatus;
    private String mFilePath;
    
    private int mCoinNum = 0;
    private Session mSession;

    private ImageView mAppIconView;
    private RelativeLayout mHeaderViewLayout;
    private ImageLoader mImageLoader;
    private LinearLayout mBackImageView;

    private TextView mAppNameTv;
    private TextView mAppVersionTv;
    private TextView mAppCoinNumTv;
    private TextView mAppDescView;

    private FrameLayout mAppActionLayout;
    private ImageView mAppActionIv;
    private LinearLayout mRootLayout;

    private ArrayList<ImageView> mAppPicViews = new ArrayList<ImageView>();

    private TextView mAppActionView;

    private DetailInfo mDetailInfo = null;
    private long mDownloadId = -1;
    private boolean mIsDownloading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_detail);
        mSession = Session.get(getApplicationContext());
        mSession.addObserver(this);
        initViews();
        String appId = getIntent().getStringExtra(Constants.EXTRA_PRODUCT_ID);
        String category = getIntent().getStringExtra(Constants.EXTRA_CATEGORY);
        if (appId == null || category == null) {
            Log.d(TAG, "invalid appId or category");
            finish();
            return;
        }
        mCoinNum = getIntent().getIntExtra(Constants.EXTRA_COIN_NUM, 0);
        mImageLoader = ImageLoader.getInstance();
        MarketAPI.getProductDetailWithId(getApplicationContext(),
                new AppDetailApiRequestListener(), appId, category);
    }

    @SuppressLint("NewApi")
    private void initViews() {
        mAppIconView = (ImageView) findViewById(R.id.app_icon);
        mAppIconView.setBackground(null);
        mHeaderViewLayout = (RelativeLayout) findViewById(R.id.app_deail_header_layout);
        mBackImageView = (LinearLayout) findViewById(R.id.back_layout);
        mAppNameTv = (TextView) findViewById(R.id.app_name_tv);
        mAppVersionTv = (TextView) findViewById(R.id.app_version_tv);
        mAppCoinNumTv = (TextView) findViewById(R.id.app_coin_num_tv);
        mAppDescView = (TextView) findViewById(R.id.app_desc_tv);
        mAppPicViews.add((ImageView) findViewById(R.id.app_desc_pic1));
        mAppPicViews.add((ImageView) findViewById(R.id.app_desc_pic2));
        mAppPicViews.add((ImageView) findViewById(R.id.app_desc_pic3));
        mAppPicViews.add((ImageView) findViewById(R.id.app_desc_pic4));
        mAppPicViews.add((ImageView) findViewById(R.id.app_desc_pic5));
        mAppActionView = (TextView) findViewById(R.id.app_action_tv);

        mAppActionLayout = (FrameLayout) findViewById(R.id.app_action_layout);
        mAppActionIv = (ImageView) findViewById(R.id.app_action_iv);
        
        mRootLayout = (LinearLayout) findViewById(R.id.app_detail_root_layout);
        mRootLayout.setVisibility(View.INVISIBLE);

        mAppActionView.setOnClickListener(this);
        mBackImageView.setOnClickListener(this);
        mAppActionLayout.setOnClickListener(this);
        mAppActionIv.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        mSession.deleteObserver(this);
        super.onDestroy();
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
            return Utils.blurBitmap(getApplicationContext(), mBitmap);
        }

        @SuppressLint("NewApi")
        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            mDstView.setBackground(new BitmapDrawable(result));
        }
    }

    private class AppDetailApiRequestListener implements ApiRequestListener {

        @Override
        public void onSuccess(int method, Object obj) {
            AppDetailBean appDetail = (AppDetailBean) obj;
            mDetailInfo = appDetail.getDetailInfo();
            mAppNameTv.setText(mDetailInfo.getAppname());
            // TODO
            mAppVersionTv.setText("版本：v" + mDetailInfo.getAppversion());
            mAppCoinNumTv.setText("+" + mCoinNum);
            Log.d(TAG, "app summary = " + mDetailInfo.getAppsummary());
            //mAppDescView.setText(mDetailInfo.getAppsummary());
            String desc =  mDetailInfo.getAppsummary();
            Log.d(TAG, "app desc = " + desc);
            mAppDescView.setText(desc);

            HashMap<String, DownloadInfo> downloadingMap = mSession
                    .getDownloadingList();
            Log.d(TAG, "downloadingMap size = " + downloadingMap.size());
            for (String pkg : downloadingMap.keySet()) {
                Log.d(TAG, "pkg = " + pkg);
            }
            if (Utils.isApkInstalled(getApplicationContext(),
                    mDetailInfo.getPackagename())) {
                // 已安装 显示打开
                mStatus = STATUS_WAITING_OPEN;
                showOpenView();
            } else if (new File(
                    com.xiaohong.kulian.common.download.Constants.DEFAULT_MARKET_SUBDIR
                            + "/" + mDetailInfo.getAppname() + ".apk").exists()) {
                mStatus = STATUS_WAITING_INSTALL;
                mFilePath = com.xiaohong.kulian.common.download.Constants.DEFAULT_MARKET_SUBDIR
                        + "/" + mDetailInfo.getAppname() + ".apk";
                showInstallView();

            } else if (mSession.getDownloadingList().get(
                    mDetailInfo.getPackagename()) != null) {
                // key is packageName
                // downloading
                DownloadInfo downloadInfo = mSession.getDownloadingList().get(
                        mDetailInfo.getPackagename());
                Log.d(TAG, "progress:" + downloadInfo.mProgress);
                Log.d(TAG, "path:" + downloadInfo.mFilePath);
                Log.d(TAG, "mIsDownloading:" + mIsDownloading);
                // mSession.getDownloadManager().
                mDownloadId = downloadInfo.id;
                Log.d(TAG, "mDownloadId-" + mDownloadId);
                if (downloadInfo.mFilePath != null
                        && !downloadInfo.mFilePath.equals("")) {
                    mStatus = STATUS_WAITING_INSTALL;
                    mFilePath = downloadInfo.mFilePath;
                    showInstallView();
                } else if (mIsDownloading == true) {
                    mStatus = STATUS_DOWNLOADING;
                    showDowloadingView(downloadInfo);
                } else {
                    mStatus = STATUS_PAUSE;
                    showContinueView(downloadInfo);
                }
            } else {
                // do download
                mStatus = STATUS_WAITING_DOWNLOAD;
                showDownloadView();

            }
            mAppActionView.setOnClickListener(AppDetailActivity.this);
            mImageLoader.displayImage(mDetailInfo.getApplogo(), mAppIconView,
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
                            mAppIconView.setImageBitmap(arg2);
                            new GaussBlurTask(arg2, mHeaderViewLayout)
                                    .execute();

                        }

                        @Override
                        public void onLoadingCancelled(String arg0, View arg1) {
                            // TODO Auto-generated method stub

                        }
                    });
            List<String> pics = mDetailInfo.getImagesrclist();
            int size = pics.size();
            // 最多显示5张
            if (size > 5) {
                size = 5;
            }
            for (int i = 0; i < size; i++) {
                mImageLoader.displayImage(pics.get(i), mAppPicViews.get(i),
                        Utils.sDisplayImageOptions);
            }
            mRootLayout.setVisibility(View.VISIBLE);
        }

        @Override
        public void onError(int method, int statusCode) {
            Log.w(TAG, "get app detail fail");
        }

    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick");
        int id = v.getId();
        switch (id) {
            case R.id.back_layout :
                finish();
                break;
            case R.id.app_action_layout :
            case R.id.app_action_iv:
            case R.id.app_action_tv:
                handleActionTvClicked();
                break;

            default :
                break;
        }

    }

    @Override
    public void update(Observable observable, Object data) {

        if(mDetailInfo == null) {
            Log.w(TAG, "update mDetailInfo is null");
            return;
        }
        if (data instanceof HashMap) {
            HashMap<String, DownloadInfo> mDownloadingTask = (HashMap<String, DownloadInfo>) data;
            DownloadInfo info = mDownloadingTask.get(mDetailInfo
                    .getPackagename());
            if (info != null) {
                Log.d(TAG, "download progress update:" + info.mProgress);
                if (info.mStatus == DownloadManager.Impl.STATUS_SUCCESS) {
                    // 已经下载成功
                    mAppActionView.setText("安装");
                    mAppActionView.setBackgroundColor(getResources().getColor(
                            R.color.install_button_background_color));
                    // mProduct.setFilePath(info.mFilePath);
                } else if (DownloadManager.Impl.isStatusError(info.mStatus)) {
                    // 下载失败

                } else if(info.mProgress != null){
                    // 下载中
                    showDowloadingView(info);
                }
            } else {

            }
        }

    }

    private void handleActionTvClicked() {
        String tvStr = (String) mAppActionView.getText();
        DownloadInfo downloadInfo = mSession.getDownloadingList().get(
                mDetailInfo.getPackagename());
        Log.d(TAG, "handleActionTvClicked mStatus = " + mStatus);
        if(mStatus == STATUS_WAITING_INSTALL) {
            //安装
            Utils.installApk(getApplicationContext(), new File(mFilePath));
        }else if(mStatus == STATUS_WAITING_OPEN) {
            //打开
            Utils.openApkByPackageName(getApplicationContext(),
                    mDetailInfo.getPackagename());
        }else if(mStatus == STATUS_PAUSE) {
            //继续
            Log.d(TAG, "continue downloading");
            mIsDownloading = true;
            mStatus = STATUS_DOWNLOADING;
            showDowloadingView(downloadInfo);
            mSession.getDownloadManager().resumeDownload(mDownloadId);
            
        }else if(mStatus == STATUS_DOWNLOADING) {
            //暂停
            Log.d(TAG, "goto pause status");
            mIsDownloading = false;
            mStatus = STATUS_PAUSE;
            showContinueView(downloadInfo);
            mSession.getDownloadManager().pauseDownload(mDownloadId);
            
        }else {
            //点击后开始下载
            Log.d(TAG, "begin download");
            mIsDownloading = true;
            mStatus = STATUS_DOWNLOADING;
            showDowloadingView(downloadInfo);
            mDownloadId = startDownload(mDetailInfo);
            
        }
        
        /*if (Utils.isApkInstalled(getApplicationContext(),
                mDetailInfo.getPackagename())) {
            // 已安装 显示打开
            mAppActionView.setText("打开");
            mAppActionView.setBackgroundColor(getResources().getColor(
                    R.color.open_button_background_color));
            Utils.openApkByPackageName(getApplicationContext(),
                    mDetailInfo.getPackagename());
        } else if (new File(
                com.xiaohong.kulian.common.download.Constants.DEFAULT_MARKET_SUBDIR
                        + "/" + mDetailInfo.getAppname() + ".apk").exists()) {
            Utils.installApk(
                    getApplicationContext(),
                    new File(
                            com.xiaohong.kulian.common.download.Constants.DEFAULT_MARKET_SUBDIR
                                    + "/" + mDetailInfo.getAppname() + ".apk"));

        } else if (mSession.getDownloadingList().get(
                mDetailInfo.getPackagename()) != null) {
            // key is packageName
            // downloading
            DownloadInfo downloadInfo = mSession.getDownloadingList().get(
                    mDetailInfo.getPackagename());
            Log.d(TAG, "progress:" + downloadInfo.mProgress);
            Log.d(TAG, "path:" + downloadInfo.mFilePath);
            // mSession.getDownloadManager().
            mDownloadId = downloadInfo.id;
            Log.d(TAG, "mDownloadId:" + mDownloadId);
            if (downloadInfo.mFilePath != null
                    && !downloadInfo.mFilePath.equals("")) {
                showInstallView();
                Utils.installApk(getApplicationContext(), new File(
                        downloadInfo.mFilePath));
            } else if("继续".equals(mAppActionView.getText())) {
                Log.d(TAG, "continue downloading");
                mIsDownloading = true;
                mSession.getDownloadManager().resumeDownload(mDownloadId);
                showDowloadingView(downloadInfo);
            }else if (mIsDownloading == true) {
                mIsDownloading = false;
                mSession.getDownloadManager().pauseDownload(mDownloadId);
                showContinueView(downloadInfo);
            } else {
                mIsDownloading = true;
                mSession.getDownloadManager().resumeDownload(mDownloadId);
                showDowloadingView(downloadInfo);
            }
        } else {
            // do download
            mDownloadId = startDownload(mDetailInfo);
            mIsDownloading = true;

        }*/
    }

    /**
     * 开始下载任务
     */
    private long startDownload(DetailInfo detailInfo) {
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(detailInfo.getAppsource()));
        request.setPackageName(detailInfo.getPackagename());
        request.setTitle(detailInfo.getAppname());
        request.setIconUrl(detailInfo.getApplogo());
        // request.setMD5(info.fileMD5);
        request.setSourceType(com.xiaohong.kulian.common.download.Constants.DOWNLOAD_FROM_MARKET);
        long id = mSession.getDownloadManager().enqueue(request);
        Utils.makeEventToast(getApplicationContext(),
                getString(R.string.alert_start_download), false);
        return id;
    }

    /**
     * 为了让下载、继续这个文本框具有进度条的效果 使用一个FrameLayout里面一个ImageView和一个TextView
     * 通过改变ImageView的宽度来达到进度条的效果
     */
    private void changeAppActionIvWidth(int percent) {
        int total = mAppActionLayout.getWidth();
        int current = percent * total / 100;
        android.view.ViewGroup.LayoutParams lp = mAppActionIv.getLayoutParams();
        lp.width = current;
        mAppActionIv.setLayoutParams(lp);
    }

    private int progressStr2Int(String progress) {
        if (progress == null) {
            return 0;
        }
        String str = progress.substring(0, progress.length() - 1);
        int result = 0;
        try {
            result = Integer.valueOf(str);
        } catch (NumberFormatException e) {
            Log.d(TAG, "progressStr2Int number format exception:" + progress);
        }
        return result;
    }

    /**
     * 之前从来没有下载过，提示下载
     */
    private void showDownloadView() {
        mAppActionView.setText("下载安装赚金币(" + mDetailInfo.getAppsize() + ")");
        // test
        changeAppActionIvWidth(100);
    }

    /**
     * 正在下载，会不断更新进度条
     */
    private void showDowloadingView(DownloadInfo downloadInfo) {
        String progress = "0%";
        if(downloadInfo != null) {
            progress = downloadInfo.mProgress == null ? "0%" : downloadInfo.mProgress;
        }
        mAppActionView.setText("正在下载（" + progress + "）");
        mAppActionView.setTextColor(Color.BLUE);
        mAppActionIv.setBackgroundColor(getResources().getColor(
                R.color.pause_button_background_color));
        if(downloadInfo != null) {
            changeAppActionIvWidth(progressStr2Int(downloadInfo.mProgress));
        }else {
            changeAppActionIvWidth(0);
        }
        
    }

    /**
     * 已安装，显示打开
     */
    private void showOpenView() {
        mAppActionView.setText(TEXT_OPEN);
        mAppActionIv.setBackgroundColor(getResources().getColor(
                R.color.open_button_background_color));
        changeAppActionIvWidth(100);
    }

    /**
     * 下载被暂停，显示继续
     */
    private void showContinueView(DownloadInfo downloadInfo) {
       /* if(downloadInfo == null) {
            Log.w(TAG, "showContinueView downloadInfo is null");
            return;
        }*/
        Log.d(TAG, "showContinueView");
        mAppActionView.setText(TEXT_CONTINUE);
        if(downloadInfo == null || progressStr2Int(downloadInfo.mProgress) < 50) {
            mAppActionView.setTextColor(getResources().
                    getColor(R.color.redownload_button_background_color));
        }else {
            mAppActionView.setTextColor(Color.WHITE);
        }
        
        mAppActionIv.setBackgroundColor(getResources().getColor(
                R.color.redownload_button_background_color));
        if(downloadInfo == null) {
            changeAppActionIvWidth(0);
        }else {
            changeAppActionIvWidth(progressStr2Int(downloadInfo.mProgress));
        }
        
    }

    /**
     * 下载已完成等待安装
     */
    private void showInstallView() {
        mAppActionView.setText(TEXT_INSTALL);
        mAppActionIv.setBackgroundColor(getResources().getColor(
                R.color.install_button_background_color));
        changeAppActionIvWidth(100);
    }

}
