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

import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.mappn.gfan.Constants;
import com.mappn.gfan.R;
import com.mappn.gfan.common.ApiAsyncTask;
import com.mappn.gfan.common.ApiAsyncTask.ApiRequestListener;
import com.mappn.gfan.common.MarketAPI;
import com.mappn.gfan.common.util.TopBar;
import com.mappn.gfan.common.util.Utils;
import com.mappn.gfan.common.widget.AppListAdapter;
import com.mappn.gfan.common.widget.BaseActivity;
import com.mappn.gfan.common.widget.LoadingDrawable;
import com.mappn.gfan.common.widget.TopRecommendAdapter;

/**
 * 首页
 * 
 * @author cong.li
 * @date 2011-5-9
 * @since Version 0.7.0
 */
public class HomeActivity extends BaseActivity implements OnClickListener, OnItemClickListener,
        ApiRequestListener {

//    private static final String TAG = "HomeActivity";
    
    /* 首页 */
    private static final int VIEW_TYPE_HOME = 0;
    /* 专题分类 */
    private static final int VIEW_TYPE_TOPIC = 1;
    /* 产品列表（应用，二级列表） */
    private static final int VIEW_TYPE_APP_PRODUCT_LIST = 2;
    
    private static final long ANIMATION_DURATION = 450;

    private Gallery mTopRecommendGallery;
    private ListView mRecommendList;
    private AppListAdapter mRecommendAdapter;
    private TopRecommendAdapter mTopRecommendAdapter;
    private FrameLayout mLoading;
    private ProgressBar mProgress;
    private TextView mNoData;
    private boolean isInSecondLevel;
    private int mCurrentViewType;
    private String mNavigationTitle;
    
    private ViewAnimator mViewAnimator;
    private TranslateAnimation sLeftOutAnimation;
    private TranslateAnimation sRightInAnimation;
    private TranslateAnimation sLeftInAnimation;
    private TranslateAnimation sRightOutAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_home_layout);

        @SuppressWarnings("unchecked")
        HashMap<String, Object> item = (HashMap<String, Object>) getIntent().getSerializableExtra(
                Constants.EXTRA_HOME_DATA);
        
        initTopBar();
        
        initView(item);
        
        initTranlateAnimation();
    }

    /*
     * 初始化Topbar
     */
    private void initTopBar() {

        TopBar.createTopBar(
                getApplicationContext(), 
                new View[] { findViewById(R.id.logo), findViewById(R.id.top_bar_search) }, 
                new int[] { View.VISIBLE, View.VISIBLE }, 
                "");
    }

    @SuppressWarnings("unchecked")
    private void initView(HashMap<String, Object> item) {

        mRecommendList = (ListView) findViewById(android.R.id.list);
        mLoading = (FrameLayout) findViewById(R.id.loading);
        mProgress = (ProgressBar) findViewById(R.id.progressbar);
        mProgress.setIndeterminateDrawable(new LoadingDrawable(getApplicationContext()));
        mProgress.setVisibility(View.VISIBLE);
        mNoData = (TextView) findViewById(R.id.no_data);
        mNoData.setOnClickListener(this);
        
        mInflater = LayoutInflater.from(getApplicationContext());
        mViewAnimator = (ViewAnimator) findViewById(R.id.va_hirachy);
        
        if (item != null) {
            handleTopContent((ArrayList<HashMap<String, Object>>) item
                    .get(Constants.EXTRA_HOME_DATA_TOP));
            handleBottomContent((ArrayList<HashMap<String, Object>>) item
                    .get(Constants.EXTRA_HOME_DATA_BOTTOM));
            
            if (mRecommendAdapter == null) {
                // 首页加载失败
                mRecommendList.removeAllViews();
                initData();
            }
        } else {
            initData();
        }
    }
    
    /*
     * 获取首页推荐内容
     */
    private void initData() {
        // reset the loading flag
        mLoadResult = 0;
        MarketAPI.getTopRecommend(getApplicationContext(), this);
        MarketAPI.getHomeRecommend(getApplicationContext(), this, 0, 50);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(int method, Object obj) {

        switch (method) {
        case MarketAPI.ACTION_GET_TOP_RECOMMEND:
            
            handleTopContent((ArrayList<HashMap<String, Object>>) obj);
            break;
            
        case MarketAPI.ACTION_GET_HOME_RECOMMEND:
            
            handleBottomContent((ArrayList<HashMap<String, Object>>) ((HashMap<String, Object>) obj)
                    .get(Constants.KEY_PRODUCT_LIST));
            break;
            
        case MarketAPI.ACTION_GET_RECOMMEND_PRODUCTS:
            
            mListAdapterLevel1.addData((ArrayList<HashMap<String, Object>>)
                        ((HashMap<String, Object>)obj).get(Constants.KEY_PRODUCT_LIST));
            break;
            
        default:
            break;
        }
    }

    @Override
    public void onError(int method, int statusCode) {
        
        switch (method) {
        case MarketAPI.ACTION_GET_TOP_RECOMMEND:
            
            synchronized (this) {
                mLoadResult++;
            }
            break;
            
        case MarketAPI.ACTION_GET_HOME_RECOMMEND:
            if (statusCode == ApiAsyncTask.TIMEOUT_ERROR) {
                // 超时
                mNoData.setVisibility(View.VISIBLE);
                mProgress.setVisibility(View.GONE);
            }
            break;
            
        case MarketAPI.ACTION_GET_RECOMMEND_PRODUCTS:
            if (statusCode == ApiAsyncTask.TIMEOUT_ERROR) {
                // 超时
                mLoadingNoData1.setVisibility(View.VISIBLE);
                mLoadingProgress1.setVisibility(View.GONE);
            }
            break;
            
        }
    }
    
    private int mLoadResult;
    
    private void handleTopContent(ArrayList<HashMap<String, Object>> result) {
        
        synchronized (this) {
            mLoadResult++;
        }
        
        if (result == null || result.size() == 0) {
            return;
        }
        
        mTopRecommendGallery = (Gallery) LayoutInflater.from(getApplicationContext()).inflate(
                R.layout.activity_home_gallery, null);
        mTopRecommendAdapter = new TopRecommendAdapter(getApplicationContext(), result);
        mTopRecommendGallery.setAdapter(mTopRecommendAdapter);
        mTopRecommendGallery.setOnItemClickListener(this);
        mRecommendList.addHeaderView(mTopRecommendGallery, null, false);
        mTopRecommendGallery.setSelection(1000 / 2 - 5);
        
        // 当顶部推荐延迟于列表推荐结果时
        if (mLoadResult == 2 && mRecommendAdapter != null) {
            mRecommendAdapter.setProductList();
            mRecommendList.setAdapter(mRecommendAdapter);
            mRecommendList.setOnItemClickListener(this);
            mRecommendList.setEmptyView(mLoading);
        }
    }
    
    private void handleBottomContent(ArrayList<HashMap<String, Object>> result) {
        
        synchronized (this) {
            mLoadResult++;
        }
        
        if(result == null || result.size() == 0) {
            return;
        }

        mRecommendAdapter = new AppListAdapter(
                getApplicationContext(),
                result,
                R.layout.common_product_list_item, 
                new String[] {
                        Constants.KEY_PRODUCT_ICON_URL_LDPI, 
                        Constants.KEY_PRODUCT_NAME,
                        Constants.KEY_PRODUCT_SUB_CATEGORY, 
                        Constants.KEY_PRODUCT_IS_STAR,
                        Constants.KEY_PRODUCT_SIZE, 
                        Constants.KEY_PRODUCT_DOWNLOAD }, 
                new int[] {
                        R.id.iv_logo, 
                        R.id.tv_name, 
                        R.id.tv_description, 
                        R.id.iv_star,
                        R.id.tv_size, 
                        R.id.tv_download });
        mRecommendAdapter.setmPageType(Constants.GROUP_4);
        if (mLoadResult == 2) {
            mRecommendAdapter.setProductList();
            mRecommendList.setAdapter(mRecommendAdapter);
            mRecommendList.setOnItemClickListener(this);
            mRecommendList.setEmptyView(mLoading);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        
        mViewAnimator.setInAnimation(sRightInAnimation);
        mViewAnimator.setOutAnimation(sLeftOutAnimation);
        
        switch (parent.getId()) {
        case R.id.gallery:
            
            int pos = (Integer) view.getTag();
            Utils.trackEvent(getApplicationContext(), Constants.GROUP_3,
                    Constants.CLICK_RECOMMEND_TOP + (pos + 1));
            
            HashMap<String, Object> topItem = (HashMap<String, Object>) 
                mTopRecommendAdapter.getItem(position);
            String type = (String)topItem.get(Constants.KEY_RECOMMEND_TYPE);
            if (Constants.KEY_CATEGORY.equals(type)) {
                // 分类项目，打开分类产品列表
                isInSecondLevel = true;
                initAppListView((String) topItem.get(Constants.KEY_ID));
                mViewAnimator.showNext();
                mCurrentViewType = VIEW_TYPE_APP_PRODUCT_LIST;
                
            } else if (Constants.KEY_TOPIC.equals(type)) {
                // 专题项目，打开专题列表
                isInSecondLevel = true;
                initTopicProducts();
                mCurrentTopicId = (String) topItem.get(Constants.KEY_ID);
                mNavigationTitle = (String) topItem.get(Constants.KEY_RECOMMEND_TITLE);
                MarketAPI.getRecommendProducts(getApplicationContext(), this, mCurrentTopicId, 100,
                        0);
                mViewAnimator.showNext();
                mCurrentViewType = VIEW_TYPE_TOPIC;
                updateNavigationTitle(true);
                
            } else if (Constants.KEY_PRODUCT.equals(type)) {
                // 产品项目，直接去详细页
                String pid = (String) topItem.get(Constants.KEY_ID);
                Intent detailIntent = new Intent(getApplicationContext(), PreloadActivity.class);
                detailIntent.putExtra(Constants.EXTRA_PRODUCT_ID, pid);
                startActivity(detailIntent);
                
            }
            break;

        case android.R.id.list:
            
            String pid = "";
            if (mCurrentViewType == VIEW_TYPE_TOPIC) {
                // 首页产品列表，去产品详细页
                HashMap<String, Object> item = (HashMap<String, Object>) mListAdapterLevel1
                        .getItem(position);
                pid = (String) item.get(Constants.KEY_PRODUCT_ID);
            } else {
                // 首页产品列表，去产品详细页
                HashMap<String, Object> item = (HashMap<String, Object>) mRecommendAdapter
                        .getItem(position - 1); // 考虑Header View所占位置
                pid = (String) item.get(Constants.KEY_PRODUCT_ID);
            }
            Intent detailIntent = new Intent(getApplicationContext(), PreloadActivity.class);
            detailIntent.putExtra(Constants.EXTRA_PRODUCT_ID, pid);
            startActivity(detailIntent);
            break;
            
        default:
            
            Utils.D("i am others position is " + position);
            break;
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            if (isInSecondLevel) {
                mViewAnimator.setInAnimation(sLeftInAnimation);
                mViewAnimator.setOutAnimation(sRightOutAnimation);
                mViewAnimator.showPrevious();
                mViewAnimator.removeViewAt(1);
                isInSecondLevel = false;
                mCurrentViewType = VIEW_TYPE_HOME;
                if (mTabHost != null) {
                    LocalActivityManager lam = getLocalActivityManager();
                    lam.removeAllActivities();
                }
                updateNavigationTitle(false);
                return true;
            }
        }
        return getParent().onKeyDown(keyCode, event);
    }
    
    /*
     * 更新导航栏标题
     */
    private void updateNavigationTitle(boolean isNextPage) {
        TextView titleView = (TextView) findViewById(R.id.top_bar_title);
        ImageView logo = (ImageView) findViewById(R.id.logo);
        if (isNextPage) {
            titleView.setText(mNavigationTitle);
            titleView
                    .setCompoundDrawablesWithIntrinsicBounds(R.drawable.topbar_navigation, 0, 0, 0);
            titleView.setVisibility(View.VISIBLE);
            logo.setVisibility(View.GONE);
        } else {
            titleView.setVisibility(View.GONE);
            logo.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * 初始化Translate动画
     */
    private void initTranlateAnimation() {
        
        // retrieve the screen width
        int displayWidth = getWindowManager().getDefaultDisplay().getWidth();
        
        sLeftOutAnimation = new TranslateAnimation(0, -displayWidth, 0, 0);
        sLeftOutAnimation.setDuration(ANIMATION_DURATION);
        
        sRightInAnimation = new TranslateAnimation(displayWidth, 0, 0, 0);
        sRightInAnimation.setDuration(ANIMATION_DURATION);
        
        sLeftInAnimation = new TranslateAnimation(-displayWidth, 0, 0, 0);
        sLeftInAnimation.setDuration(ANIMATION_DURATION);
        
        sRightOutAnimation = new TranslateAnimation(0, displayWidth, 0, 0);
        sRightOutAnimation.setDuration(ANIMATION_DURATION);
    }
    
    private TabHost mTabHost;
    private LayoutInflater mInflater;
    // Tab id
    private static final String TAB_POP = "pop";
    private static final String TAB_NEW = "new";
    /*
     * 初始化应用列表（包含最热[装机量]和最新列表）
     */
    private void initAppListView(String categoryId) {
        
        mTabHost = (TabHost) mInflater.inflate(R.layout.common_tab_host, mViewAnimator, false);
        mTabHost.setup(getLocalActivityManager());

        Intent popIntent = new Intent(getApplicationContext(), ProductListActivity.class);
        popIntent.putExtra(Constants.EXTRA_SORT_TYPE, Constants.ORDER_TYPE_INSTALLED_NUM);
        popIntent.putExtra(Constants.EXTRA_CATEGORY_ID, categoryId);
        TabSpec tab1 = mTabHost
                .newTabSpec(TAB_POP)
                .setIndicator(
                        Utils.createTabView(getApplicationContext(),
                                getString(R.string.sort_tab_pop)))
                .setContent(popIntent);
        mTabHost.addTab(tab1);

        Intent newIntent = new Intent(getApplicationContext(), ProductListActivity.class);
        newIntent.putExtra(Constants.EXTRA_SORT_TYPE, Constants.ORDER_TYPE_TIME);
        newIntent.putExtra(Constants.EXTRA_CATEGORY_ID, categoryId);
        TabSpec tab2 = mTabHost
                .newTabSpec(TAB_NEW)
                .setIndicator(
                        Utils.createTabView(getApplicationContext(),
                                getString(R.string.sort_tab_new)))
                .setContent(newIntent);
        mTabHost.addTab(tab2);
        
        mViewAnimator.addView(mTabHost);
    }
    
    /*专题ID*/
    private String mCurrentTopicId;
    private ListView mListViewLevel1;
    private FrameLayout mLoadingLevel1;
    private ProgressBar mLoadingProgress1;
    private TextView mLoadingNoData1;
    private AppListAdapter mListAdapterLevel1;
    
    /*
     * 初始化专题产品列表
     */
    private void initTopicProducts() {
        FrameLayout listViewLayout = (FrameLayout) LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.common_list_view, null, false);
        mListViewLevel1 = (ListView) listViewLayout.findViewById(android.R.id.list);
        mLoadingLevel1 = (FrameLayout) listViewLayout.findViewById(R.id.loading);
        mLoadingProgress1 = (ProgressBar) listViewLayout.findViewById(R.id.progressbar);
        mLoadingProgress1.setIndeterminateDrawable(new LoadingDrawable(getApplicationContext()));
        mLoadingProgress1.setVisibility(View.VISIBLE);
        mLoadingNoData1 = (TextView) listViewLayout.findViewById(R.id.no_data);
        mLoadingNoData1.setOnClickListener(this);
        mListAdapterLevel1 = new AppListAdapter(getApplicationContext(),
                null,
                R.layout.common_product_list_item, 
                new String[] { 
                    Constants.KEY_PRODUCT_ICON_URL_LDPI, 
                    Constants.KEY_PRODUCT_NAME,
                    Constants.KEY_PRODUCT_AUTHOR,
                    Constants.KEY_PRODUCT_RATING,
                    Constants.KEY_PRODUCT_DOWNLOAD }, 
                new int[] { 
                    R.id.iv_logo, 
                    R.id.tv_name,
                    R.id.tv_description, 
                    R.id.rb_app_rating, 
                    R.id.tv_download });
        mListAdapterLevel1.setProductList();
        mListViewLevel1.setAdapter(mListAdapterLevel1);
        mListViewLevel1.setEmptyView(mLoadingLevel1);
        mListViewLevel1.setOnItemClickListener(this);
        mViewAnimator.addView(listViewLayout);
    }

    @Override
    public void onClick(View v) {
        
        if(isInSecondLevel) {
            // 二级页面重试
            mLoadingProgress1.setVisibility(View.VISIBLE);
            mLoadingNoData1.setVisibility(View.GONE);
            MarketAPI.getRecommendProducts(getApplicationContext(), this, mCurrentTopicId, 100, 0);
        } else {
            // 首页重试
            mProgress.setVisibility(View.VISIBLE);
            mNoData.setVisibility(View.GONE);
            initData();
        }
    }

}