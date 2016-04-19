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
import com.xiaohong.kulian.bean.ReportResultBean;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.download.DownloadManager;
import com.xiaohong.kulian.common.util.DialogUtils;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.vo.DownloadInfo;
import com.xiaohong.kulian.common.widget.CustomProgressBar;

public class AppDetailActivity extends Activity
        implements
            ApiRequestListener,
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

    private RelativeLayout mRootLayout;

    private CustomProgressBar mProgressBar;
    
    private ArrayList<ImageView> mAppPicViews = new ArrayList<ImageView>();

    private DetailInfo mDetailInfo = null;
    private long mDownloadId = -1;
    private boolean mIsDownloading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_detail);
        mSession = Session.get(getApplicationContext());
        //mSession.addObserver(this);
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
        mHeaderViewLayout = (RelativeLayout) findViewById(R.id.header_blur_layout);
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
        
        mProgressBar = (CustomProgressBar) findViewById(R.id.download_progress_bar);

        mRootLayout = (RelativeLayout) findViewById(R.id.app_detail_root_layout);
        mRootLayout.setVisibility(View.INVISIBLE);

        mProgressBar.setOnClickListener(this);
        mBackImageView.setOnClickListener(this);
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
            return Utils.doFastBlur(mBitmap, 25, false);
            //return Utils.blurBitmap(getApplicationContext(), mBitmap);
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
            mSession.addObserver(AppDetailActivity.this);
            AppDetailBean appDetail = (AppDetailBean) obj;
            mDetailInfo = appDetail.getDetailInfo();
            mAppNameTv.setText(mDetailInfo.getAppname());
            // TODO
            mAppVersionTv.setText("版本：v" + mDetailInfo.getAppversion());
            mAppCoinNumTv.setText("+" + mCoinNum);
            Log.d(TAG, "app summary = " + mDetailInfo.getAppsummary());
            //mAppDescView.setText(mDetailInfo.getAppsummary());
            String desc =  mDetailInfo.getAppsummary();
            desc = desc.replace("\\n", "\n");
            Log.d(TAG, "app desc = " + desc);
            mAppDescView.setText(desc);

            HashMap<String, DownloadInfo> downloadingMap = mSession
                    .getDownloadingList();
            Log.d(TAG, "downloadingMap size = " + downloadingMap.size());
            /*for (String pkg : downloadingMap.keySet()) {
                Log.d(TAG, "pkg = " + pkg);
            }*/
            if (Utils.isApkInstalled(getApplicationContext(),
                    mDetailInfo.getPackagename())) {
                // 已安装 显示打开
                mStatus = STATUS_WAITING_OPEN;
                showOpenView();
            } else if (Utils.isApkDownloaded(mDetailInfo.getAppname())) {
                mStatus = STATUS_WAITING_INSTALL;
                mFilePath = Utils.getDownloadedAppPath(mDetailInfo.getAppname());
                Log.d(TAG, "downloaded mFilePath = " + mFilePath);
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
                mDownloadId = downloadInfo.id;
                Log.d(TAG, "mDownloadId-" + mDownloadId);
                if (downloadInfo.mFilePath != null
                        && !downloadInfo.mFilePath.equals("")) {
                    mStatus = STATUS_WAITING_INSTALL;
                    mFilePath = downloadInfo.mFilePath;
                    showInstallView();
                } else /*if (mIsDownloading == true)*/ {
                    mStatus = STATUS_DOWNLOADING;
                    mIsDownloading = true;
                    mProgressBar.setStatus(CustomProgressBar.Status.PROCESSING);
                    showDownloadingView(downloadInfo);
                }/* else {
                    mStatus = STATUS_PAUSE;
                    showContinueView(downloadInfo);
                }*/
            } else {
                // do download
                mStatus = STATUS_WAITING_DOWNLOAD;
                showDownloadView();
            }

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

                            new GaussBlurTask(getImageTopPart(arg2), mHeaderViewLayout)
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

    public static Bitmap getImageTopPart(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = w/5;
        return Bitmap.createBitmap(bitmap, w/4, 0, w/2, h, null, false);
    }
    
    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick");
        int id = v.getId();
        switch (id) {
            case R.id.back_layout :
                finish();
                break;
            case R.id.download_progress_bar:
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
            DownloadInfo info = mDownloadingTask.get(mDetailInfo.getPackagename());
            if (info != null) {
                Log.d(TAG, "download progress update:" + info.mProgress);
                if (info.mStatus == DownloadManager.Impl.STATUS_SUCCESS) {
                    // 已经下载成功
                    mProgressBar.setText("安装");
                    mProgressBar.setStatus(CustomProgressBar.Status.FINISHED);
                    // mProduct.setFilePath(info.mFilePath);
                } else if (DownloadManager.Impl.isStatusError(info.mStatus)) {
                    // 下载失败

                } else if(info.mProgress != null){
                    // 下载中
                    showDownloadingView(info);
                }
            } else {

            }
        }
    }

    private void handleActionTvClicked() {
        DownloadInfo downloadInfo = mSession.getDownloadingList().get(
                mDetailInfo.getPackagename());
        Log.d(TAG, "handleActionTvClicked mStatus = " + mStatus);
        if(mStatus == STATUS_WAITING_INSTALL) {
            //安装
            Utils.installApk(getApplicationContext(), new File(mFilePath));
        }else if(mStatus == STATUS_WAITING_OPEN) {
            //打开
            MarketAPI.reportAppLaunched(getApplicationContext(), this, mDetailInfo.getPackagename());
            Utils.openApkByPackageName(getApplicationContext(),
                    mDetailInfo.getPackagename());
        }else if(mStatus == STATUS_PAUSE) {
            //继续
            Log.d(TAG, "continue downloading");
            mSession.addObserver(this);
            mIsDownloading = true;
            mStatus = STATUS_DOWNLOADING;
            mProgressBar.setStatus(CustomProgressBar.Status.PROCESSING);
            showDownloadingView(downloadInfo);
            mSession.getDownloadManager().resumeDownload(mDownloadId);
            
        }else if(mStatus == STATUS_DOWNLOADING) {
            //暂停
//            mSession.deleteObserver(this);
//            Log.d(TAG, "goto pause status");
//            mIsDownloading = false;
//            mStatus = STATUS_PAUSE;
//            showContinueView(downloadInfo);
//            mSession.getDownloadManager().pauseDownload(mDownloadId);

        }else {
            //点击后开始下载
            Log.d(TAG, "begin download");
            mSession.addObserver(this);
            mIsDownloading = true;
            mStatus = STATUS_DOWNLOADING;
            mProgressBar.setStatus(CustomProgressBar.Status.PROCESSING);
            showDownloadingView(downloadInfo);
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
                showDownloadingView(downloadInfo);
            }else if (mIsDownloading == true) {
                mIsDownloading = false;
                mSession.getDownloadManager().pauseDownload(mDownloadId);
                showContinueView(downloadInfo);
            } else {
                mIsDownloading = true;
                mSession.getDownloadManager().resumeDownload(mDownloadId);
                showDownloadingView(downloadInfo);
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
        mProgressBar.setText("下载安装赚金币(" + mDetailInfo.getAppsize() + ")");
        mProgressBar.setStatus(CustomProgressBar.Status.INITIAL);
    }

    /**
     * 正在下载，会不断更新进度条
     */
    private void showDownloadingView(DownloadInfo downloadInfo) {
        String progress = "0%";
        if(downloadInfo != null) {
            progress = downloadInfo.mProgress == null ? "0%" : downloadInfo.mProgress;
            if(!"100%".equals(progress)) {
                mProgressBar.setProgress(progressStr2Int(downloadInfo.mProgress));
                mProgressBar.setText("正在下载（" + progress + "）");
            }else {
             // 已经下载成功
                mProgressBar.setText("安装");
                mProgressBar.setStatus(CustomProgressBar.Status.FINISHED);
            }
            
        }else {
            mProgressBar.setText("正在下载（" + progress + "）");
        }
        
    }

    /**
     * 已安装，显示打开
     */
    private void showOpenView() {
        mProgressBar.setText(TEXT_OPEN);
        mProgressBar.setStatus(CustomProgressBar.Status.INSTALLED);
    }

    /**
     * 下载被暂停，显示继续
     */
    private void showContinueView(DownloadInfo downloadInfo) {
        Log.d(TAG, "showContinueView");
        mProgressBar.setText(TEXT_CONTINUE);
        mProgressBar.setStatus(CustomProgressBar.Status.PAUSED);
    }

    /**
     * 下载已完成等待安装
     */
    private void showInstallView() {
        mProgressBar.setText(TEXT_INSTALL);
        mProgressBar.setStatus(CustomProgressBar.Status.FINISHED);
    }

    @Override
    public void onSuccess(int method, Object obj) {
        switch (method) {
        case MarketAPI.ACTION_REPORT_APP_LAUNCHED: {
            ReportResultBean result = (ReportResultBean) obj;
            if (result.getAddedCoinNum() > 0) {
                DialogUtils.showMessage(AppDetailActivity.this, "金币奖励", "您获得了"+result.getAddedCoinNum()+"个金币");
                mSession.setCoinNum(result.getCoinNum());
                mSession.notifyCoinUpdated();
            }
            break;
        }
        default:
            break;
        }
    }

    @Override
    public void onError(int method, int statusCode) {
        // TODO Auto-generated method stub
        
    }

}

