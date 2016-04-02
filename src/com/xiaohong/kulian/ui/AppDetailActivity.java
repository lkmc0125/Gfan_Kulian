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
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
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

public class AppDetailActivity extends Activity implements OnClickListener , Observer{
    private static final String TAG = "AppDetailActivity";
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
    
    private ArrayList<ImageView> mAppPicViews = new ArrayList<ImageView>();
    
    private TextView mAppActionView;
    
    private  DetailInfo mDetailInfo = null;
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
        
        mAppActionView.setOnClickListener(null);
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
            //TODO
            mAppVersionTv.setText("版本：v" + mDetailInfo.getAppversion());
            mAppCoinNumTv.setText("+" + mCoinNum);
            mAppDescView.setText(mDetailInfo.getAppsummary());
            
           
            HashMap<String, DownloadInfo> downloadingMap = mSession.getDownloadingList();
            Log.d(TAG, "downloadingMap size = " + downloadingMap.size());
            for(String pkg : downloadingMap.keySet()) {
                Log.d(TAG, "pkg = " + pkg);
            }
            if(Utils.isApkInstalled(getApplicationContext(), mDetailInfo.getPackagename())) {
                //已安装 显示打开
                mAppActionView.setText("打开");
                mAppActionView.setBackgroundColor(
                        getResources().getColor(R.color.open_button_background_color));
               
            }else if(new File(com.xiaohong.kulian.common.download.Constants.DEFAULT_MARKET_SUBDIR
                    + "/" + mDetailInfo.getAppname() + ".apk").exists()){
                mAppActionView.setText("安装");
                mAppActionView.setBackgroundColor(
                        getResources().getColor(R.color.install_button_background_color));
                
            }else if(mSession.getDownloadingList().get(mDetailInfo.getPackagename()) != null){
                //key is packageName
                //downloading
                DownloadInfo downloadInfo = mSession.getDownloadingList().get(mDetailInfo.getPackagename());
                Log.d(TAG, "progress:" + downloadInfo.mProgress);
                Log.d(TAG, "path:" + downloadInfo.mFilePath);
                //mSession.getDownloadManager().
                mDownloadId = downloadInfo.id;
                Log.d(TAG, "mDownloadId" + mDownloadId);
                if(downloadInfo.mFilePath != null && !downloadInfo.mFilePath.equals("")) {
                    mAppActionView.setText("安装");
                    mAppActionView.setBackgroundColor(
                            getResources().getColor(R.color.install_button_background_color));
                }else if(mIsDownloading == true) {
                    mAppActionView.setText("暂停");
                    mAppActionView.setBackgroundColor(
                            getResources().getColor(R.color.pause_button_background_color));
                }else {
                    mAppActionView.setText("继续");
                    mAppActionView.setBackgroundColor(
                            getResources().getColor(R.color.redownload_button_background_color));
                }
            } else {
                //do download
                mAppActionView.setText("下载安装赚金币(" + mDetailInfo.getAppsize() + ")");
               /* mAppActionView.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        startDownload(mDetailInfo);
                    }
                });*/
                
            }
            mAppActionView.setOnClickListener(AppDetailActivity.this);
            mImageLoader.displayImage(mDetailInfo.getApplogo(),
                    mAppIconView, Utils.sDisplayImageOptions, new ImageLoadingListener() {

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
            //最多显示5张
            if(size > 5) {
                size = 5;
            }
            for(int i = 0; i < size; i++) {
                mImageLoader.displayImage(pics.get(i), mAppPicViews.get(i), Utils.sDisplayImageOptions);
            }
        }

        @Override
        public void onError(int method, int statusCode) {
            Log.w(TAG, "get app detail fail");
        }

    }

    @Override
    public void onClick(View v) {
        Log.d("free", "onClick");
        int id = v.getId();
        switch (id) {
            case R.id.back_layout :
                finish();
                break;
            case R.id.app_action_tv:
                handleActionTvClicked();
                break;
            
            default :
                break;
        }

    }

    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof HashMap) {
            HashMap<String, DownloadInfo> mDownloadingTask = (HashMap<String, DownloadInfo>) data;
            DownloadInfo info = mDownloadingTask.get(mDetailInfo.getPackagename());
            if (info != null) {
                if (info.mStatus == DownloadManager.Impl.STATUS_SUCCESS) {
                    // 已经下载成功
                    mAppActionView.setText("安装");
                    mAppActionView.setBackgroundColor(
                            getResources().getColor(R.color.install_button_background_color));
                    //mProduct.setFilePath(info.mFilePath);
                } else if(DownloadManager.Impl.isStatusError(info.mStatus)) {
                    // 下载失败
                    
                }
            } else {
                
            }
        }
        
    }
    
    private void handleActionTvClicked() {
        if(Utils.isApkInstalled(getApplicationContext(), mDetailInfo.getPackagename())) {
            //已安装 显示打开
            mAppActionView.setText("打开");
            mAppActionView.setBackgroundColor(
                    getResources().getColor(R.color.open_button_background_color));
            Utils.openApkByPackageName(getApplicationContext(),
                    mDetailInfo.getPackagename());
        }else if(new File(com.xiaohong.kulian.common.download.Constants.DEFAULT_MARKET_SUBDIR
                + "/" + mDetailInfo.getAppname() + ".apk").exists()){
            Utils.installApk(getApplicationContext(), new File(com.xiaohong.kulian.common.download.Constants.DEFAULT_MARKET_SUBDIR
                + "/" + mDetailInfo.getAppname() + ".apk"));
            
        }else if(mSession.getDownloadingList().get(mDetailInfo.getPackagename()) != null){
            //key is packageName
            //downloading
            DownloadInfo downloadInfo = mSession.getDownloadingList().get(mDetailInfo.getPackagename());
            Log.d(TAG, "progress:" + downloadInfo.mProgress);
            Log.d(TAG, "path:" + downloadInfo.mFilePath);
            //mSession.getDownloadManager().
            mDownloadId = downloadInfo.id;
            Log.d(TAG, "mDownloadId:" + mDownloadId);
            if(downloadInfo.mFilePath != null && !downloadInfo.mFilePath.equals("")) {
                mAppActionView.setText("安装");
                mAppActionView.setBackgroundColor(
                        getResources().getColor(R.color.install_button_background_color));
                Utils.installApk(getApplicationContext(), new File(downloadInfo.mFilePath));
            }else if(mIsDownloading == true) {
                mIsDownloading = false;
                mSession.getDownloadManager().pauseDownload(mDownloadId);
                mAppActionView.setText("继续");
                mAppActionView.setBackgroundColor(
                        getResources().getColor(R.color.redownload_button_background_color));
            }else {
                mIsDownloading = true;
                mSession.getDownloadManager().resumeDownload(mDownloadId);
                mAppActionView.setText("暂停");
                mAppActionView.setBackgroundColor(
                        getResources().getColor(R.color.pause_button_background_color));
            }
        } else {
            //do download
            mDownloadId = startDownload(mDetailInfo);
            mIsDownloading = true;
            
        }
    }
    
    /**
     * 开始下载任务
     */
    private long startDownload(DetailInfo detailInfo) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(detailInfo.getAppsource()));
        request.setPackageName(detailInfo.getPackagename());
        request.setTitle(detailInfo.getAppname());
        request.setIconUrl(detailInfo.getApplogo());
        //request.setMD5(info.fileMD5);
        request.setSourceType(com.xiaohong.kulian.common.download.Constants.DOWNLOAD_FROM_MARKET);
        long id = mSession.getDownloadManager().enqueue(request);
        Utils.makeEventToast(getApplicationContext(), getString(R.string.alert_start_download),
                false);
        return id;
    }

}
