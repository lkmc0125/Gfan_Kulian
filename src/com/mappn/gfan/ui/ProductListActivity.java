package com.mappn.gfan.ui;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.mappn.gfan.common.widget.AppListAdapter;
import com.mappn.gfan.common.widget.LazyloadListActivity;
import com.mappn.gfan.common.widget.LoadingDrawable;

public class ProductListActivity extends LazyloadListActivity implements ApiRequestListener,
        OnItemClickListener, OnClickListener {

    // Loading
    private FrameLayout mLoading;
    private ProgressBar mProgress;
    private TextView mNoData;
	private AppListAdapter mAdapter;
	private String mCategory;
	private int mSortType;
	private String mCategoryId; 
	private int mTotalSize;

    @Override
    public boolean doInitView(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if(intent != null) {
            
            mCategory = intent.getStringExtra(Constants.EXTRA_CATEGORY);
            if (TextUtils.isEmpty(mCategory)) {
                mSortType = intent.getIntExtra(Constants.EXTRA_SORT_TYPE, 1);
                mCategoryId = intent.getStringExtra(Constants.EXTRA_CATEGORY_ID);
            }
            setContentView(R.layout.common_list_view);
            
            int maxSize = intent.getIntExtra(Constants.EXTRA_MAX_ITEMS, 0);
            if (maxSize > 0) {
                mTotalSize = maxSize;
            }
            
            mList = (ListView) findViewById(android.R.id.list);
            mLoading = (FrameLayout) findViewById(R.id.loading);
            mProgress = (ProgressBar) mLoading.findViewById(R.id.progressbar);
            mProgress.setIndeterminateDrawable(new LoadingDrawable(getApplicationContext()));
            mProgress.setVisibility(View.VISIBLE);
            mNoData = (TextView) mLoading.findViewById(R.id.no_data);
            mNoData.setOnClickListener(this);
            mList.setEmptyView(mLoading);
            mList.setOnItemClickListener(this);
            
            lazyload();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void doLazyload() {

        if (Constants.ORDER_TYPE_INSTALLED_NUM == mSortType) {
            MarketAPI.getProducts(getApplicationContext(), this, getItemsPerPage(),
                    getStartIndex(), mSortType, mCategoryId);
        } else if (Constants.ORDER_TYPE_TIME == mSortType) {
            MarketAPI.getProducts(getApplicationContext(), this, getItemsPerPage(),
                    getStartIndex(), mSortType, mCategoryId);
        } else if (Constants.CATEGORY_GROW.equals(mCategory)) {
            MarketAPI.getGrowFast(getApplicationContext(), this, 
                            getStartIndex(), getItemsPerPage());
        } else {
            MarketAPI.getRankByCategory(getApplicationContext(), this, getStartIndex(),
                    getItemsPerPage(), mCategory);
        }
    }

    @Override
    public AppListAdapter doInitListAdapter() {
        mAdapter = new AppListAdapter(getApplicationContext(),
                null,
                R.layout.common_product_list_item, 
                new String[] { 
                    Constants.KEY_PRODUCT_ICON_URL,
                    Constants.KEY_PRODUCT_NAME, 
                    Constants.KEY_PRODUCT_AUTHOR,
                    Constants.KEY_PRODUCT_IS_STAR, 
                    Constants.KEY_PRODUCT_RATING,
                    Constants.KEY_PRODUCT_DOWNLOAD }, 
                new int[] { 
                    R.id.iv_logo, 
                    R.id.tv_name,
                    R.id.tv_description, 
                    R.id.iv_star,
                    R.id.rb_app_rating,
                    R.id.tv_download });
        mAdapter.setProductList();
        if (!TextUtils.isEmpty(mCategory)) {
            // 排行榜列表
            mAdapter.setRankList();
        }
        return mAdapter;
    }
    
    @Override
    protected int getItemCount() {
        return mTotalSize;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(int method, Object obj) {
        HashMap<String, Object> result = (HashMap<String, Object>) obj;
        
        if (mTotalSize <= 0) {
            mTotalSize = (Integer) result.get(Constants.KEY_TOTAL_SIZE);
        }
        
        mAdapter.addData((ArrayList<HashMap<String, Object>>) result
                .get(Constants.KEY_PRODUCT_LIST));
        setLoadResult(true);
    }

    @Override
    public void onError(int method, int statusCode) {
        if(statusCode == ApiAsyncTask.BUSSINESS_ERROR) {
            // 没有数据
        } else {
            // 超时
            mNoData.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.GONE);
        }
        setLoadResult(false);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Activity parent = getParent();
        if(parent != null) {
            return parent.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // 去产品详细页
        HashMap<String, Object> item = (HashMap<String, Object>) mAdapter.getItem(position);
        String pid = (String) item.get(Constants.KEY_PRODUCT_ID);
        Intent detailIntent = new Intent(getApplicationContext(), PreloadActivity.class);
        detailIntent.putExtra(Constants.EXTRA_PRODUCT_ID, pid);
        startActivity(detailIntent);
    }

    @Override
    public void onClick(View v) {
        // 重试
        mProgress.setVisibility(View.VISIBLE);
        mNoData.setVisibility(View.GONE);
        lazyload();
    }
}