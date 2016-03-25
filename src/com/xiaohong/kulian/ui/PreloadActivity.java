/*
 * Copyright (C) 2016 Shanghai Xiaohong.Inc
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

import java.util.HashMap;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.common.ApiAsyncTask;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.vo.ProductDetail;
import com.xiaohong.kulian.common.widget.BaseActivity;
import com.xiaohong.kulian.common.widget.LoadingDrawable;

/**
 * 产品详细页预加载页面
 * 
 * @author andrew
 * @date    2011-4-19
 *
 */
public class PreloadActivity extends BaseActivity implements ApiRequestListener {

    private static final String ACTION_PID = "pid";
    private static final String ACTION_PACKAGENAME = "pkgName";
    
    private ProgressBar mProgress;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.loading);
        
        final Intent intent = getIntent();
        
        mProgress = (ProgressBar) findViewById(R.id.progressbar);
        mProgress.setIndeterminateDrawable(new LoadingDrawable(
                getApplicationContext()));
        mProgress.setVisibility(View.VISIBLE);

        // 通过产品ID来获取内容			
        String pId = intent.getStringExtra(Constants.EXTRA_PRODUCT_ID);
        String category = intent.getStringExtra(Constants.EXTRA_CATEGORY);
        MarketAPI.getProductDetailWithId(this, this, pId, category);
    }

    @Override
    public void onSuccess(int method, Object obj) {
        Intent intent = new Intent(getApplicationContext(), ProductDetailActivity.class);
        intent.putExtra(Constants.EXTRA_PRDUCT_DETAIL, (ProductDetail) obj);
        intent.putExtra(Constants.IS_BUY, getIntent().getBooleanExtra(Constants.IS_BUY, false));
        finish();
        startActivity(intent);
    }
    
    @Override
    public void onError(int method, int statusCode) {

        if (ApiAsyncTask.TIMEOUT_ERROR == statusCode) {
            // 网络异常
            Utils.makeEventToast(getApplicationContext(), getString(R.string.no_network),
                    false);
        } else {
            Utils.makeEventToast(getApplicationContext(), getString(R.string.lable_not_found),
                    false);
        }
        finish();
    }
}