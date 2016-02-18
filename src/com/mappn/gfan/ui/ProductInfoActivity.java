/*
 * Copyright (C) 2011 mAPPn.Inc
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

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mappn.gfan.Constants;
import com.mappn.gfan.R;
import com.mappn.gfan.common.util.ImageUtils;
import com.mappn.gfan.common.util.StringUtils;
import com.mappn.gfan.common.util.Utils;
import com.mappn.gfan.common.vo.ProductDetail;
import com.mappn.gfan.common.widget.BaseActivity;
import com.mappn.gfan.common.widget.ScreenShotGallery;

/**
 * 产品详细信息页<br>
 * 用以展示应用所有的详细信息。<br>
 * @author andrew
 * @date    2011-3-22
 *
 */
public class ProductInfoActivity extends BaseActivity {

    private ScreenShotGallery mGallery;
    private ProductDetail mProduct;
    private boolean mIsShortDescription = true;
    private boolean isInit = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        mProduct = (ProductDetail) intent
                .getSerializableExtra(Constants.EXTRA_PRDUCT_DETAIL);
        setContentView(R.layout.product_detail);
        mGallery = (ScreenShotGallery) findViewById(R.id.gallery);
        initProductInfo(mProduct);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGallery.clear();
        mGallery = null;
    }

    /**
     * 计算描述信息是否过长
     */
    private boolean mesureDescription(TextView shortView, TextView longView) {
        final int shortHeight = shortView.getHeight();
        final int longHeight = longView.getHeight();
        if (longHeight > shortHeight) {
            shortView.setVisibility(View.VISIBLE);
            longView.setVisibility(View.GONE);
            return true;
        }
        shortView.setVisibility(View.GONE);
        longView.setVisibility(View.VISIBLE);
        return false;
    }
    
    /**
     * 更改按钮【更多】的文本
     */
    private void toogleMoreButton(Button btn) {
        
        String text = (String)btn.getText();
        String moreText = getString(R.string.label_more);
        String lessText = getString(R.string.label_less);
        if(moreText.equals(text)) {
            btn.setText(lessText);
        } else {
            btn.setText(moreText);
        }
    }
    
    /**
     * 初始化产品详细信息
     */
    private void initProductInfo(ProductDetail product) {
        final FrameLayout frame = (FrameLayout) findViewById(R.id.app_description);
        ViewTreeObserver vto = frame.getViewTreeObserver();

        final TextView appShortDescription = (TextView) findViewById(R.id.app_description1);
        appShortDescription.setText(product.getLongDescription());
        final TextView appLongDescription = (TextView) findViewById(R.id.app_description2);
        appLongDescription.setText(product.getLongDescription());
        
        final ImageView moreLine = (ImageView) findViewById(R.id.iv_more);
        final Button more = (Button) findViewById(R.id.btn_more);
        
        vto.addOnPreDrawListener(new OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (isInit)
                    return true;
                if (mesureDescription(appShortDescription, appLongDescription)) {
                    moreLine.setVisibility(View.VISIBLE);
                    more.setVisibility(View.VISIBLE);
                }
                isInit = true;
                return true;
            }
        });
        
        more.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mIsShortDescription) {
                    Utils.trackEvent(getApplicationContext(), Constants.GROUP_12,
                            Constants.DETAIL_CLICK_MORE);
                    appShortDescription.setVisibility(View.GONE);
                    appLongDescription.setVisibility(View.VISIBLE);
                } else {
                    appShortDescription.setVisibility(View.VISIBLE);
                    appLongDescription.setVisibility(View.GONE);
                }
                toogleMoreButton(more);
                mIsShortDescription = !mIsShortDescription;
            }
        });
        
        TextView appSize = (TextView) findViewById(R.id.app_size);
        appSize.setText(StringUtils.formatSize(product.getAppSize()));
        TextView appVersion = (TextView) findViewById(R.id.app_version);
        appVersion.setText(String.valueOf(product.getVersionName()));
        TextView appDownload = (TextView) findViewById(R.id.app_download);
        appDownload.setText(StringUtils.getDownloadInterval(product.getDownloadCount()));

        initGallery(product);
    }
    
    /*
     * 初始化Gallery控件
     */
    private void initGallery(ProductDetail product) {
        ArrayList<String> mUrls = new ArrayList<String>();
        String[] screenUrl = product.getScreenshot();//Ldpi();
        for (String url : screenUrl) {
            if (TextUtils.isEmpty(url)) {
                continue;
            }
            mUrls.add(url);
        }
        if(mUrls.size() > 0) {
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());

            LinearLayout child = null;

            final int length = mUrls.size();
            for (int i = 0; i < length; i++) {
                if (i == 0 || i == 2 || i == 4) {
                    child = (LinearLayout) inflater.inflate(R.layout.gallery_frame, mGallery, false);
                    ImageView screen1 = (ImageView) child.findViewById(R.id.iv_screen1);
                    screen1.setTag(i);
                    screen1.setOnClickListener(mScreenShotClickListener);
                    String urls = mUrls.get(i);
                    ImageUtils.downloadDeatilScreenshot(getApplicationContext(), urls, screen1);
                    if (i == (length - 1)) {
                        ImageView screen2 = (ImageView) child.findViewById(R.id.iv_screen2);
                        screen2.setVisibility(View.GONE);
                        mGallery.addChild(child);
                    }
                } else {
                    ImageView screen2 = (ImageView) child.findViewById(R.id.iv_screen2);
                    ImageUtils.downloadDeatilScreenshot(getApplicationContext(), mUrls.get(i),
                            screen2);
                    screen2.setTag(i);
                    screen2.setOnClickListener(mScreenShotClickListener);
                    mGallery.addChild(child);
                }
            }
        }
    }
    
    private OnClickListener mScreenShotClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Utils.trackEvent(getApplicationContext(), Constants.GROUP_12,
                    Constants.DETAIL_CLICK_SNAPSHOT);
            Integer pos = (Integer) v.getTag();
            Intent screenShotIntent = new Intent(getApplicationContext(), ScreenshotActivity.class);
            screenShotIntent.putExtra(Constants.EXTRA_PRDUCT_DETAIL, mProduct);
            screenShotIntent.putExtra(Constants.EXTRA_SCREENSHOT_ID, pos);
            startActivity(screenShotIntent);
        }
    };
}