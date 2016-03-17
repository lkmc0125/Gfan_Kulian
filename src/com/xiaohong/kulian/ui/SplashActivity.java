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
package com.xiaohong.kulian.ui;

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
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.common.HttpClientFactory;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.util.ImageUtils;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.widget.BaseActivity;
import com.xiaohong.kulian.common.widget.LoadingDrawable;

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
public class SplashActivity extends BaseActivity implements ApiRequestListener, SplashADListener {

    private static final int VALID = 1;
    private static final int LOAD = 2;
    private SplashAD splashAD;
    private ViewGroup container;
    public boolean canJump = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_layout);

        // 初始化加载页
//        initSplashBg();
        container = (ViewGroup) this.findViewById(R.id.splash_container);
        splashAD = new SplashAD(this, container, "1105244906", "9010002916167210", this);

        /**
         * 开屏广告现已增加新的接口，可以由开发者在代码中设置开屏的超时时长
         * SplashAD(Activity activity, ViewGroup container, String appId, String posId, SplashADListener adListener, int fetchDelay)
         * fetchDelay参数表示开屏的超时时间，单位为ms，取值范围[3000, 5000]。设置为0时表示使用广点通的默认开屏超时配置
         *
         * splashAD = new SplashAD(this, container, Constants.APPID, Constants.SplashPosID, this, 3000);可以设置超时时长为3000ms
         */

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
        
        mHandler.sendEmptyMessageDelayed(VALID, 800);
        
        // 检查用户是否使用CMWAP网络
        HttpHost proxy = Utils.detectProxy(getApplicationContext());
        if (proxy != null) {
            HttpClientFactory.get().getHttpClient().useProxyConnection(proxy);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case VALID:
                
                Intent i = new Intent(getApplicationContext(), HomeTabActivity.class);
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
//    private void initSplashBg() {
//
//        File splashFile = new File(getApplicationContext().getCacheDir(), "splash.png");
//
//        if (splashFile.exists()) {
//            Bitmap bmp = BitmapFactory.decodeFile(splashFile.getAbsolutePath());
//            if (bmp != null) {
//                setSplashBitmap(bmp);
//                return;
//            }
//        }
//        // 没有新的Splash页，使用默认图
//        setSplashBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.splash));
//        mSession.setSplashTime(0);
//    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(int method, Object obj) {
    }

    @Override
    public void onError(int method, int statusCode) {
    }
    
    /*
     * 设置Splash背景图
     */
//    private void setSplashBitmap(Bitmap bmp) {
//
//        // 针对不同分辨率的屏幕做Splash的适配
//        Bitmap scaledBitmap = ImageUtils.sacleBitmap(getApplicationContext(), bmp);
//        ImageView v = ((ImageView) findViewById(R.id.iv_splashBg));
//        if (scaledBitmap == null) {
//            v.setImageBitmap(bmp);
//        } else {
//            v.setImageBitmap(scaledBitmap);
//        }
//    }

    @Override
    public void onADPresent() {
      Log.i("AD_DEMO", "SplashADPresent");
    }

    @Override
    public void onADClicked() {
      Log.i("AD_DEMO", "SplashADClicked");
    }

    @Override
    public void onADDismissed() {
      Log.i("AD_DEMO", "SplashADDismissed");
      next();
    }

    @Override
    public void onNoAD(int errorCode) {
      Log.i("AD_DEMO", "LoadSplashADFail, eCode=" + errorCode);
      /** 如果加载广告失败，则直接跳转 */
      this.startActivity(new Intent(this, HomeTabActivity.class));
      this.finish();
    }

    /**
     * 设置一个变量来控制当前开屏页面是否可以跳转，当开屏广告为普链类广告时，点击会打开一个广告落地页，此时开发者还不能打开自己的App主页。当从广告落地页返回以后，
     * 才可以跳转到开发者自己的App主页；当开屏广告是App类广告时只会下载App。
     */
    private void next() {
      if (canJump) {
        this.startActivity(new Intent(this, HomeTabActivity.class));
        this.finish();
      } else {
        canJump = true;
      }
    }

    @Override
    protected void onPause() {
      super.onPause();
      canJump = false;
    }

    @Override
    protected void onResume() {
      super.onResume();
      if (canJump) {
        next();
      }
      canJump = true;
    }

    /** 开屏页最好禁止用户对返回按钮的控制 */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
      if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
        return true;
      }
      return super.onKeyDown(keyCode, event);
    }

}
