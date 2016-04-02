package com.xiaohong.kulian.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Session;
import com.xiaohong.kulian.bean.AppDetailBean;
import com.xiaohong.kulian.bean.DetailInfo;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.util.DBUtils;
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
            final DetailInfo detailInfo = appDetail.getDetailInfo();
            mAppNameTv.setText(detailInfo.getAppname());
            //TODO
            mAppVersionTv.setText("版本：v" + detailInfo.getAppversion());
            mAppCoinNumTv.setText("+" + mCoinNum);
            mAppDescView.setText(detailInfo.getAppsummary());
            
           
            if(Utils.isApkInstalled(getApplicationContext(), detailInfo.getPackagename())) {
                //已安装 显示打开
                mAppActionView.setText("打开");
                mAppActionView.setBackgroundColor(
                        getResources().getColor(R.color.open_button_background_color));
                mAppActionView.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        Utils.openApkByPackageName(getApplicationContext(),
                                detailInfo.getPackagename());
                    }
                });
            }else {
                
            }
            mImageLoader.displayImage(detailInfo.getApplogo(),
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
            List<String> pics = detailInfo.getImagesrclist();
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
            default :
                break;
        }

    }

    @Override
    public void update(Observable observable, Object data) {
        // TODO Auto-generated method stub
        
    }
    
    /**
     * 开始下载任务
     */
    public void download(DetailInfo detailInfo) {
        
        Utils.trackEvent(getApplicationContext(), Constants.GROUP_12,
                Constants.DETAIL_DOWNLOAD);

        /*if(Constants.PAY_TYPE_PAID == mProduct.getPayCategory()) {
            // 收费应用
            if (mSession.isLogin()) {
                if (!DBUtils.isBought(getApplicationContext(), detailInfo.getAppid() + "")) {
                    if (!isFinishing()) {
                        //showDialog(DIALOG_PURCHASE);
                        return;
                    }
                }
            } else {
                // 登录
                Intent loginIntent = new Intent(getApplicationContext(),
                        RegisterActivity.class);
                startActivity(loginIntent);
                return;
            }
        }*/

       /* if (TextUtils.isEmpty(mProduct.getFilePath())) {
            HashMap<String, DownloadInfo> list = mSession.getDownloadingList();
            if (list.containsKey(mProduct.getPackageName())) {
                // 下载中
                Utils.makeEventToast(getApplicationContext(),
                        getString(R.string.warning_comment_later), false);
                return;
            } else {
                // 开始下载
                MarketAPI.getDownloadUrl(getApplicationContext(), ProductDetailActivity.this,
                        mProduct.getPid(), mProduct.getSourceType());
                //mDownloadButton.setEnabled(false);
            }
        } else {
            // 下载完成
            //Utils.installApk(getApplicationContext(), new File(mProduct.getFilePath()));
        }*/
    }

}
