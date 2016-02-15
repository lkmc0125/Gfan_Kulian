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
import java.util.HashMap;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mappn.gfan.Constants;
import com.mappn.gfan.R;
import com.mappn.gfan.common.ApiAsyncTask;
import com.mappn.gfan.common.ApiAsyncTask.ApiRequestListener;
import com.mappn.gfan.common.MarketAPI;
import com.mappn.gfan.common.download.DownloadManager.Request;
import com.mappn.gfan.common.util.Utils;
import com.mappn.gfan.common.widget.AppListAdapter;
import com.mappn.gfan.common.widget.LazyloadListActivity;
import com.mappn.gfan.common.widget.LoadingDrawable;

/**
 * this view is displaying for product in search view
 * 
 * @author cong.li
 * @date 2011-5-9
 * @since Version 0.7.0
 */

public class SearchResultActivity extends LazyloadListActivity implements ApiRequestListener,
        OnClickListener, OnItemClickListener {

    // Loading
    private FrameLayout mLoading;
    private ProgressBar mProgress;
    private TextView mNoData;
    
    private int mTotalSize;
    private int mEndPosition;
    private AppListAdapter mAdapter;
    private int mSearchType;
    private String mKeywords;

    @Override
    public boolean doInitView(Bundle savedInstanceState) {
        setContentView(R.layout.common_list_view);
        
        // 获得搜索类型（市场/社区）
        mSearchType = getIntent().getIntExtra(Constants.EXTRA_SEARCH_TYPE, 0);
        
        mLoading = (FrameLayout) findViewById(R.id.loading);
        mProgress = (ProgressBar) mLoading.findViewById(R.id.progressbar);
        mProgress.setIndeterminateDrawable(new LoadingDrawable(getApplicationContext()));
        mProgress.setVisibility(View.VISIBLE);
        mNoData = (TextView) mLoading.findViewById(R.id.no_data);
        mNoData.setOnClickListener(this);
        
        switchHintStatus(STATUS_INIT);
        
        mList = (ListView) findViewById(android.R.id.list);
        mList.setVisibility(View.GONE);
        mList.setEmptyView(mLoading);
        mList.setOnItemClickListener(this);
        
        return true;
    }

    @Override
    public void doLazyload() {
        if (mSearchType == Constants.SEARCH_TYPE_MARKET) {
            // 搜索市场结果
            MarketAPI.search(getApplicationContext(), this, getItemsPerPage(), getStartIndex(), 0,
                    mKeywords);
        } else {
            // 搜索社区结果
            MarketAPI.getSearchFromBBS(getApplicationContext(), this, mKeywords, getStartIndex(),
                    getItemsPerPage());
        }
        switchHintStatus(STATUS_LOADING);
    }

    @Override
    public AppListAdapter doInitListAdapter() {
        if (mSearchType == Constants.SEARCH_TYPE_MARKET) {
            mAdapter = new AppListAdapter(
                    getApplicationContext(), 
                    null,
                    R.layout.search_result_list_item, 
                    new String[] { 
                        Constants.KEY_PRODUCT_ICON_URL, 
                        Constants.KEY_PRODUCT_NAME,
                        Constants.KEY_PRODUCT_AUTHOR,
                        Constants.KEY_PRODUCT_PRICE, 
                        Constants.KEY_PRODUCT_RATING,
                        Constants.KEY_PRODUCT_DOWNLOAD,
                        Constants.KEY_PRODUCT_SOURCE_TYPE }, 
                        new int[] { 
                        R.id.iv_logo, 
                        R.id.tv_name, 
                        R.id.tv_description, 
                        R.id.tv_info, 
                        R.id.rb_app_rating,
                        R.id.tv_download,
                        R.id.tv_source});
            mAdapter.setProductList();
        } else {
            mAdapter = new AppListAdapter(
                    getApplicationContext(),
                    null, 
                    R.layout.activity_search_bbs_item,
                    new String[] { Constants.SEARCH_RESULT_TITLE },
                    new int[] { R.id.tv_name });
            mAdapter.setContainsPlaceHolder(true);
            mAdapter.setPlaceHolderResource(R.layout.activity_install_nessary_list_separator);
        }
        return mAdapter;
    }
    

    private static final int STATUS_INIT = 0;
    private static final int STATUS_LOADING = 1;
    private static final int STATUS_NODATA = 2;
    private static final int STATUS_RETRY = 3;

    /*
     * 切换提示状态
     */
    private void switchHintStatus(int status) {

        switch (status) {
        case STATUS_INIT:
            mNoData.setClickable(false);
            mNoData.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            mNoData.setText(R.string.warning_no_input);
            mProgress.setVisibility(View.GONE);
            mNoData.setVisibility(View.VISIBLE);
            break;

        case STATUS_LOADING:
            mNoData.setClickable(false);
            mProgress.setVisibility(View.VISIBLE);
            mNoData.setVisibility(View.GONE);
            break;
            
        case STATUS_NODATA:
            mNoData.setClickable(false);
            mNoData.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            mNoData.setText(R.string.search_no_result);
            mProgress.setVisibility(View.GONE);
            mNoData.setVisibility(View.VISIBLE);
            break;
            
        case STATUS_RETRY:
            mNoData.setClickable(true);
            mNoData.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.btn_retry, 0, 0);
            mNoData.setText(R.string.no_data);
            mProgress.setVisibility(View.GONE);
            mNoData.setVisibility(View.VISIBLE);
            break;

        default:
            break;
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(int method, Object obj) {
        
        switch (method) {
            
        case MarketAPI.ACTION_SEARCH:
            
            mList.setVisibility(View.VISIBLE);
            HashMap<String, Object> products = (HashMap<String, Object>) obj;
            mTotalSize = (Integer) products.get(Constants.KEY_TOTAL_SIZE);
            mEndPosition = (Integer) products.get(Constants.KEY_END_POSITION);

            if (mTotalSize > 0) {
                ArrayList<HashMap<String, Object>> productList = 
                        (ArrayList<HashMap<String, Object>>) products.get(Constants.KEY_PRODUCT_LIST);
                mAdapter.addData(productList);
                ((SearchActivity)getParent()).updateMarketResultNumber(mTotalSize);
            } else {
                switchHintStatus(STATUS_NODATA);
            }
            setLoadResult(true);
            break;
            
        case MarketAPI.ACTION_BBS_SEARCH:
            
            mList.setVisibility(View.VISIBLE);
            HashMap<String, Object> result = (HashMap<String, Object>) obj;
            mTotalSize = (Integer) result.get(Constants.KEY_TOTAL_SIZE);
            mEndPosition = (Integer) result.get(Constants.KEY_END_POSITION);
            
            if(mTotalSize > 0) {
                ArrayList<HashMap<String, Object>> data = (ArrayList<HashMap<String, Object>>) (result
                        .get(Constants.KEY_JK_LIST));
                mAdapter.addData(data);
                ((SearchActivity)getParent()).updateBbsResultNumber(mTotalSize);
            } else {
                switchHintStatus(STATUS_NODATA);
            }
            setLoadResult(true);
            break;
            
        default:
            break;
        }
    }

    @Override
    public void onError(int method, int statusCode) {
        
        if (statusCode == ApiAsyncTask.BUSSINESS_ERROR) {
            switchHintStatus(STATUS_NODATA);
        } else if (statusCode == ApiAsyncTask.TIMEOUT_ERROR) {
            switchHintStatus(STATUS_RETRY);
        }
        setLoadResult(false);
    }

    @Override
    protected int getItemCount() {
        return mTotalSize;
    }
    
    @Override
    public int getEndIndex() {
        return mEndPosition;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            resetSearchResult();
        }
        return getParent().onKeyDown(keyCode, event);
    }
    
    public void resetSearchResult() {
        switchHintStatus(STATUS_INIT);
        reset();
    }
    
    /**
     * 设置搜索关键字，为父页面搜索设置
     */
    public void setSearchKeyword(String keyword) {
        mKeywords = keyword;
        resetSearchResult();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        @SuppressWarnings("unchecked")
        HashMap<String, Object> item = (HashMap<String, Object>) mAdapter.getItem(position);
        if (mSearchType == Constants.SEARCH_TYPE_MARKET) {
            String pid = (String) item.get(Constants.KEY_PRODUCT_ID);
            Intent detailIntent = new Intent(getApplicationContext(), PreloadActivity.class);
            detailIntent.putExtra(Constants.EXTRA_PRODUCT_ID, pid);
            detailIntent.putExtra(Constants.EXTRA_SOURCE_TYPE,
                    (String) item.get(Constants.KEY_PRODUCT_SOURCE_TYPE));
            startActivity(detailIntent);
        } else {
            String url = (String) item.get(Constants.KEY_DOWN_URL);
            String fileName = (String) item.get(Constants.SEARCH_RESULT_TITLE);
            Request request = new Request(Uri.parse(url));
            request.setTitle(fileName);
            request.setPackageName(fileName);
            request.setSourceType(com.mappn.gfan.common.download.Constants.DOWNLOAD_FROM_BBS);
            mSession.getDownloadManager().enqueue(request);
            Utils.makeEventToast(getApplicationContext(),
                    getString(R.string.start_download_bbs_apk), false);
            Utils.trackEvent(getApplicationContext(), Constants.GROUP_2,
                    Constants.CLICK_SEARCH_BBS_APK);
        }
    }
}