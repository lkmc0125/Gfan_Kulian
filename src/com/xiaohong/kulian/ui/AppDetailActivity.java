package com.xiaohong.kulian.ui;

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

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.bean.AppDetailBean;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.util.Utils;

public class AppDetailActivity extends Activity implements OnClickListener {
    private static final String TAG = "AppDetailActivity";

    private ImageView mAppIconView;
    private RelativeLayout mHeaderViewLayout;
    private ImageLoader mImageLoader;
    private LinearLayout mBackImageView;

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
            /*Log.d(TAG, "appDetail = " + appDetail);
            Log.d(TAG,
                    "appDetail.getDetailInfo() = " + appDetail.getDetailInfo());
            Log.d(TAG, "mImageLoader = " + mImageLoader);*/
            mImageLoader.displayImage(appDetail.getDetailInfo().getApplogo(),
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
