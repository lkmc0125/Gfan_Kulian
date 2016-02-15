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

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mappn.gfan.Constants;
import com.mappn.gfan.R;
import com.mappn.gfan.common.util.ImageUtils;
import com.mappn.gfan.common.vo.ProductDetail;
import com.mappn.gfan.common.widget.IndicatorDrawable;

/**
 * 软件截屏页
 * 用以展示产品的截屏图片。
 * @author andrew
 * @date    2011-3-22
 *
 */
public class ScreenshotActivity extends Activity {

    private ImageView mScreenShot;
    private ProductDetail mProduct;
    private int mIndex;
    private ArrayList<String> mScreenShots;
    private GestureDetector mGestureDetector;
    private LinearLayout mIndicator;
    private ImageView mIndicatorDrawable;
    private Button mClose;
    private Animation inAnim;
    private Animation outAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.market_activity_screenshot);

        initViews();
    }
    
    private Handler mHandler = new Handler() { };

    private void initViews() {

        final Intent intent = getIntent();

        mProduct = (ProductDetail) intent.getSerializableExtra(Constants.EXTRA_PRDUCT_DETAIL);
        mIndex = intent.getIntExtra(Constants.EXTRA_SCREENSHOT_ID, 0);
        String[] tempArray = mProduct.getScreenshot();
        mScreenShots = new ArrayList<String>();
        for (String url : tempArray) {
            if (TextUtils.isEmpty(url)) {
                continue;
            }
            mScreenShots.add(url);
        }
        
        mIndicator = (LinearLayout) findViewById(R.id.layout_done);
        mIndicatorDrawable = (ImageView) findViewById(R.id.indicator);
        IndicatorDrawable id = new IndicatorDrawable(getApplicationContext(), mScreenShots.size(), true);
        mIndicatorDrawable.setImageDrawable(id);
        mScreenShot = (ImageView) findViewById(R.id.iv_show);

        mClose = (Button) findViewById(R.id.btn_done);
        mClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        inAnim = AnimationUtils.loadAnimation(this, R.anim.show_in);
        outAnim = AnimationUtils.loadAnimation(this, R.anim.show_out);
        
        displayScreenShot(mIndex);

        mGestureDetector = new GestureDetector(getApplicationContext(),
                new SimpleOnGestureListener() {
                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                            float velocityY) {

                        float absX = Math.abs(velocityX);
                        float absY = Math.abs(velocityY);
                        if (absX > absY && absX > 500) {
                            if (velocityX > 0) {
                                // left
                                showPrevious();
                            } else {
                                // right
                                showNext();
                            }
                            displayIndicator();
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        displayIndicator();
                        return false;
                    }
                });
    }
    
    private Runnable mHideIndicator = new Runnable() {
        
        @Override
        public void run() {
            mIndicator.startAnimation(outAnim);
            mIndicator.setVisibility(View.GONE);
        }
    };
    
    private void displayIndicator() {
        if (mIndicator.isShown()) {
            mHandler.removeCallbacks(mHideIndicator);
        } else {
            mIndicator.startAnimation(inAnim);
            mIndicator.setVisibility(View.VISIBLE);
        }
        mHandler.postDelayed(mHideIndicator, 1500);
    }
    
    /*
     * Display the screen shot image
     */
    private void displayScreenShot(int index) {
        Drawable oldDrawable = mScreenShot.getDrawable();
        Drawable oldBackground = mScreenShot.getBackground();
        if (oldDrawable != null) {
            oldDrawable.setCallback(null);
        }
        if (oldBackground != null) {
            oldBackground.setCallback(null);
        }
        mIndicatorDrawable.setImageLevel(index);
        ImageUtils
                .downloadScreenShot(getApplicationContext(), mScreenShots.get(index), mScreenShot);
        displayIndicator();
    }
    
    private void showPrevious() {
        if (mIndex > 0) {
            displayScreenShot(--mIndex);
            mIndicatorDrawable.setImageLevel(mIndex);
        }
    }

    private void showNext() {
        if (mIndex < (mScreenShots.size() - 1)) {
            displayScreenShot(++mIndex);
            mIndicatorDrawable.setImageLevel(mIndex);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

}
