/*
 * Copyright (C) 2010 mAPPn.Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mappn.gfan.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpHost;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.mappn.gfan.Constants;
import com.mappn.gfan.R;
import com.mappn.gfan.common.ApiAsyncTask.ApiRequestListener;
import com.mappn.gfan.common.HttpClientFactory;
import com.mappn.gfan.common.MarketAPI;
import com.mappn.gfan.common.util.ImageUtils;
import com.mappn.gfan.common.util.Utils;
import com.mappn.gfan.common.widget.BaseActivity;
import com.mappn.gfan.common.widget.LoadingDrawable;

/**
 * 
 * Splash Activity For GfanMobile
 * 
 * 优化处理过程，加快加载速度，只预加载首页数据
 * 
 * @author andrew.wang
 * @date 2010-11-22
 * @since Version 0.4.0
 */
public class SplashActivity extends BaseActivity implements ApiRequestListener {

    private static final int VALID = 1;
    private static final int LOAD = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_layout);
        
        // 初始化加载页
        initSplashBg();

        final ProgressBar mLoading = (ProgressBar) findViewById(R.id.splash_loading);
        mLoading.setIndeterminateDrawable(new LoadingDrawable(getApplicationContext(),
                LoadingDrawable.SIZE_SMALL, R.color.white, R.color.splash_notification_bg, 200));
        
        mHandler.sendEmptyMessage(LOAD);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
    /*
     * 预加载数据
     */
    private void preload() {

        // 加载屏幕大小
        mSession.setScreenSize(this);
        
        // 加载已经安装的应用列表
        mSession.getInstalledApps();

        if (!Utils.isNetworkAvailable(getApplicationContext())) {
            mPreloadResult = 2;
            mHandler.sendEmptyMessageDelayed(VALID, 800);
            return;
        }
        // 检查用户是否使用CMWAP网络
        HttpHost proxy = Utils.detectProxy(getApplicationContext());
        if (proxy != null) {
            HttpClientFactory.get().getHttpClient().useProxyConnection(proxy);
        }

        // 为首页预先加载内容
        MarketAPI.getTopRecommend(getApplicationContext(), this);
        MarketAPI.getHomeRecommend(getApplicationContext(), this, 0, 50);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case VALID:
                
                if (isFinishing() || mPreloadResult != 2) {
                    // wait for the preload result
                    return;
                }
                Intent i = new Intent(getApplicationContext(), HomeTabActivity.class);
                if (mContent != null) {
                    i.putExtra(Constants.EXTRA_HOME_DATA, mContent);
                }
                startActivity(i);
                finish();
                break;
            
            case LOAD:
                
                if (isFinishing()) {
                    return;
                }
                preload();
                break;
                
            default:
                break;
            }
        }
    };

    /*
     * 初始化Splash背景图
     */
    private void initSplashBg() {

        File splashFile = new File(getApplicationContext().getCacheDir(), "splash.png");

        if (splashFile.exists()) {
            Bitmap bmp = BitmapFactory.decodeFile(splashFile.getAbsolutePath());
            if (bmp != null) {
                setSplashBitmap(bmp);
                return;
            }
        }
        // 没有新的Splash页，使用默认图
        setSplashBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.splash));
        mSession.setSplashTime(0);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(int method, Object obj) {

        mPreloadResult++;

        if (method == MarketAPI.ACTION_GET_TOP_RECOMMEND) {

            handleTopRecommend(obj);

        } else if (method == MarketAPI.ACTION_GET_HOME_RECOMMEND) {

            handleHomeRecommend((HashMap<String, Object>) obj);

        }

        mHandler.sendEmptyMessage(VALID);
    }

    @Override
    public void onError(int method, int statusCode) {
        mPreloadResult++;
        mHandler.sendEmptyMessage(VALID);
    }
    
    /*
     * 设置Splash背景图
     */
    private void setSplashBitmap(Bitmap bmp) {

        // 针对不同分辨率的屏幕做Splash的适配
        Bitmap scaledBitmap = ImageUtils.sacleBitmap(getApplicationContext(), bmp);
        ImageView v = ((ImageView) findViewById(R.id.iv_splashBg));
        if (scaledBitmap == null) {
            v.setImageBitmap(bmp);
        } else {
            v.setImageBitmap(scaledBitmap);
        }
    }
    
    /*
     * 首页产品预加载结果标识
     */
    private int mPreloadResult;
    /*
     * 首页产品预加载结果内容
     */
    private HashMap<String, Object> mContent;
    
    /*
     * 解析首页的顶部推荐列表
     */
    @SuppressWarnings("unchecked")
    private void handleTopRecommend(Object obj) {
        ArrayList<HashMap<String, Object>> result = (ArrayList<HashMap<String, Object>>) obj;

        if (result == null || result.size() <= 0) {
            return;
        }
        if (mContent == null) {
            mContent = new HashMap<String, Object>();
        }
        mContent.put(Constants.EXTRA_HOME_DATA_TOP, result);
    }

    /*
     * 解析首页的推荐列表
     */
    @SuppressWarnings("unchecked")
    private void handleHomeRecommend(HashMap<String, Object> obj) {

        ArrayList<HashMap<String, Object>> result = (ArrayList<HashMap<String, Object>>) obj
                .get(Constants.KEY_PRODUCT_LIST);

        if (result == null || result.size() <= 0) {
            if (mContent != null) {
                mContent = null;
            }
            return;
        }
        if (mContent == null) {
            mContent = new HashMap<String, Object>();
        }
        mContent.put(Constants.EXTRA_HOME_DATA_BOTTOM, result);
    }

}
