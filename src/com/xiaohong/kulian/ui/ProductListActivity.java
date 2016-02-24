package com.xiaohong.kulian.ui;

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

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.common.ApiAsyncTask;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.widget.AppListAdapter;
import com.xiaohong.kulian.common.widget.LazyloadListActivity;
import com.xiaohong.kulian.common.widget.LoadingDrawable;

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
	private boolean mIsEnd;

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
            mIsEnd = false;
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
        MarketAPI.getRankByCategory(getApplicationContext(), this, getStartPage(), mCategory);
    }

    @Override
    public AppListAdapter doInitListAdapter() {
        mAdapter = new AppListAdapter(getApplicationContext(),
                null,
                R.layout.common_product_list_item, 
                new String[] { 
                    Constants.KEY_PRODUCT_ICON_URL,
                    Constants.KEY_PRODUCT_NAME, 
                    Constants.KEY_PRODUCT_SHORT_DESCRIPTION,
                    Constants.KEY_PRODUCT_IS_STAR, 
//                    Constants.KEY_PRODUCT_RATING,
                    Constants.KEY_PRODUCT_DOWNLOAD }, 
                new int[] { 
                    R.id.iv_logo, 
                    R.id.tv_name,
                    R.id.tv_description, 
                    R.id.iv_star,
//                    R.id.rb_app_rating,
                    R.id.tv_download });
        mAdapter.setProductList();
        if (!TextUtils.isEmpty(mCategory)) {
            // 排行榜列表
            mAdapter.setRankList();
        }
        return mAdapter;
    }
    
    @Override
    public boolean isEnd() {
        return mIsEnd;
    }
    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(int method, Object obj) {
        HashMap<String, Object> result = (HashMap<String, Object>) obj;

        ArrayList<HashMap<String, Object>> appList = (ArrayList<HashMap<String, Object>>) result
        .get(Constants.KEY_PRODUCT_LIST);
        mIsEnd = appList.size() < 10;
        mAdapter.addData(appList);
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
        detailIntent.putExtra(Constants.EXTRA_CATEGORY, mCategory);
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