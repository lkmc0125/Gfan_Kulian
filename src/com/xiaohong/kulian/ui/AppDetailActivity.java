package com.xiaohong.kulian.ui;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import com.xiaohong.kulian.bean.AppDetailBean;
import com.xiaohong.kulian.bean.DetailInfo;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.util.Utils;

public class AppDetailActivity extends Activity implements OnClickListener {
    private static final String TAG = "AppDetailActivity";
    private int mCoinNum = 0;

    private ImageView mAppIconView;
    private RelativeLayout mHeaderViewLayout;
    private ImageLoader mImageLoader;
    private LinearLayout mBackImageView;
    
    private TextView mAppNameTv;
    private TextView mAppVersionTv;
    private TextView mAppCoinNumTv;
    private TextView mAppDescView;
    
    ArrayList<ImageView> mAppPicViews = new ArrayList<ImageView>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_detail);
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
        mBackImageView.setOnClickListener(this);
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
            DetailInfo detailInfo = appDetail.getDetailInfo();
            mAppNameTv.setText(detailInfo.getAppname());
            //TODO
            mAppVersionTv.setText("版本：v" + detailInfo.getAppversion());
            mAppCoinNumTv.setText("+" + mCoinNum);
            mAppDescView.setText(detailInfo.getAppsummary());
            
            /*Log.d(TAG, "appDetail = " + appDetail);
            Log.d(TAG,
                    "appDetail.getDetailInfo() = " + appDetail.getDetailInfo());
            Log.d(TAG, "mImageLoader = " + mImageLoader);*/
            mImageLoader.displayImage(detailInfo.getApplogo(),
                    mAppIconView, new ImageLoadingListener() {

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
                mImageLoader.displayImage(pics.get(i), mAppPicViews.get(i));
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

}
