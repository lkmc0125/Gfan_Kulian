package com.mappn.gfan.ui;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.mappn.gfan.Constants;
import com.mappn.gfan.R;
import com.mappn.gfan.common.ApiAsyncTask.ApiRequestListener;
import com.mappn.gfan.common.MarketAPI;
import com.mappn.gfan.common.util.TopBar;
import com.mappn.gfan.common.util.Utils;
import com.mappn.gfan.common.widget.AppListAdapter;
import com.mappn.gfan.common.widget.BaseActivity;
import com.mappn.gfan.common.widget.LoadingDrawable;

public class CategoryActivity extends BaseActivity implements OnItemClickListener,
        OnClickListener, ApiRequestListener {

    /* 一级分类 */
    private static final int VIEW_TYPE_CATEGORY = 1;
    /* 二级分类 */
    private static final int VIEW_TYPE_SUBCATEGORY = 2;
    /* 产品列表（应用，二级列表） */
    private static final int VIEW_TYPE_APP_PRODUCT_LIST = 3;
    /* 产品列表（其它，三级列表）*/
    private static final int VIEW_TYPE_OTHERS_PRODUCT_LIST = 4;
    /* 专题列表（二级列表）*/
    private static final int VIEW_TYPE_TOPIC_CATEGORY = 5;
    /* 专题产品列表（三级列表）*/
    private static final int VIEW_TYPE_TOPIC_PRODUCTS = 6;
    /* 当前的ViewType */
    private int mCurrentViewType = VIEW_TYPE_CATEGORY;
    
    // 动画时间
    private static final long ANIMATION_DURATION = 450;
    
    /* 标题（一级、二级、三级）*/
    private String mTopLevelTitle;
    private String mSecondLevelTitle;
    private String mThirdLevelTitle;

    private ViewAnimator mViewAnimator;
    private ListView mlistView;
    private AppListAdapter mAdapter;
    private FrameLayout mLoading;
    private ProgressBar mProgress;
    private TextView mNoData;

    // Head ImageView
    private ImageView leftHeader;
    private ImageView rightHeader;
    // Tab id
    private static final String TAB_POP = "pop";
    private static final String TAB_NEW = "new";

    private TabHost mTabHost;
    
    private ListView mListViewLevel1;
    private AppListAdapter mListAdapterLevel1;
    private FrameLayout mLoadingLevel1;
    private ProgressBar mLoadingProgress1;
    private TextView mLoadingNoData1;
    private ListView mListViewLevel2;
    private AppListAdapter mListAdapterLevel2;
    private FrameLayout mLoadingLevel2;
    private ProgressBar mLoadingProgress2;
    private TextView mLoadingNoData2;
    
    private int mCurrentLevel;
    // 专题ID
    private String mTopicId;

    private LayoutInflater mInflater;
    // 屏幕宽度
    private int width;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_layout);
        // 获取屏幕宽度
        width = getWindowManager().getDefaultDisplay().getWidth();
        mInflater = LayoutInflater.from(getApplicationContext());
        initAnimation();
        initTopBar();
        initView();
        MarketAPI.getAllCategory(getApplicationContext(), this);
    }
    
    private void initView() {
        
        mLoading = (FrameLayout) findViewById(R.id.loading);
        mProgress = (ProgressBar) mLoading.findViewById(R.id.progressbar);
        mProgress.setIndeterminateDrawable(new LoadingDrawable(getApplicationContext()));
        mProgress.setVisibility(View.VISIBLE);
        mNoData = (TextView) mLoading.findViewById(R.id.no_data);
        mNoData.setOnClickListener(this);
        
        mlistView = (ListView) findViewById(android.R.id.list);
        
        View header = LayoutInflater.from(getApplicationContext()).inflate(
                R.layout.activity_category_header, null);
        leftHeader = (ImageView) header.findViewById(R.id.iv_header_left);
        leftHeader.setOnClickListener(this);
        rightHeader = (ImageView) header.findViewById(R.id.iv_header_right);
        rightHeader.setOnClickListener(this);
        mlistView.addHeaderView(header, null, false);
        mlistView.setEmptyView(mLoading);
        mlistView.setOnItemClickListener(this);
        mlistView.setAdapter(initAdapter());

        mViewAnimator = (ViewAnimator) this.findViewById(R.id.va_hirachy);
    }

    /**
     * 初始化Topbar
     */
    private void initTopBar() {
        mTopLevelTitle = getString(R.string.sort_top_title);
        TopBar.createTopBar(getApplicationContext(), 
                new View[] { findViewById(R.id.top_bar_title), findViewById(R.id.top_bar_search) }, 
                new int[] { View.VISIBLE, View.VISIBLE }, mTopLevelTitle);
    }

    /*
     * 初始化数据Adapter
     */
    private ListAdapter initAdapter() {
        return mAdapter = new AppListAdapter(getApplicationContext(), null,
                R.layout.activity_category_list_item, 
                new String[] { 
                    Constants.KEY_CATEGORY_ICON_URL,
                    Constants.KEY_CATEGORY_NAME,
                    Constants.KEY_TOP_APP,
                    Constants.KEY_APP_COUNT }, 
                new int[] { 
                    R.id.iv_icon, 
                    R.id.tv_category_name,
                    R.id.tv_category_description, 
                    R.id.tv_app_num });
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        
        mViewAnimator.setOutAnimation(sLeftOutAnimation);
        mViewAnimator.setInAnimation(sRighttInAnimation);
        
        HashMap<String, Object> data = null;
        if(mCurrentViewType == VIEW_TYPE_CATEGORY) {
            // 一级列表有HeaderView
            data = (HashMap<String, Object>) mAdapter.getItem(position - 1);
            mSecondLevelTitle = (String) data.get(Constants.KEY_CATEGORY_NAME);
            updateNavigationTitle(mSecondLevelTitle, true);
            
            String categoryId = (String) data.get(Constants.KEY_CATEGORY_ID);
            if (!TextUtils.isEmpty(categoryId)) {
                // 应用分类的产品列表
                initAppListView(categoryId);
                mCurrentViewType = VIEW_TYPE_APP_PRODUCT_LIST;
            } else {
                // 游戏或者电子书的二级分类列表
                initListView((ArrayList<HashMap<String, Object>>) data
                        .get(Constants.KEY_SUB_CATEGORY));
                mCurrentViewType = VIEW_TYPE_SUBCATEGORY;
            }
            
        } else if(mCurrentViewType == VIEW_TYPE_SUBCATEGORY) {
            
            data = (HashMap<String, Object>) mListAdapterLevel1.getItem(position);
            mThirdLevelTitle =  (String) data.get(Constants.KEY_CATEGORY_NAME);
            updateNavigationTitle(mThirdLevelTitle, true);
            String categoryId = (String) data.get(Constants.KEY_CATEGORY_ID);
            // 应用分类的产品列表
            initAppListView(categoryId);
            mCurrentViewType = VIEW_TYPE_OTHERS_PRODUCT_LIST;
            
        } else if(mCurrentViewType == VIEW_TYPE_OTHERS_PRODUCT_LIST
                || mCurrentViewType == VIEW_TYPE_TOPIC_CATEGORY) {
            
            // 二级分类 OR 专题列表
            data = (HashMap<String, Object>) mListAdapterLevel1.getItem(position);
            mThirdLevelTitle = (String) data.get(Constants.KEY_CATEGORY_NAME);
            updateNavigationTitle(mThirdLevelTitle, true);
            
            String categoryId = (String) data.get(Constants.KEY_CATEGORY_ID);
            if (!TextUtils.isEmpty(categoryId)) {
                // 游戏或者电子书的产品列表
                
                Utils.trackEvent(getApplicationContext(), Constants.GROUP_5,
                        Constants.CLICK_CATEGORY_ITEM + mThirdLevelTitle);
                
                initAppListView(categoryId);
                mCurrentViewType = VIEW_TYPE_OTHERS_PRODUCT_LIST;
            } else {
                // 专题的产品列表
                
                Utils.trackEvent(getApplicationContext(), Constants.GROUP_5,
                        Constants.CLICK_SUB_TOPIC + mThirdLevelTitle);
                
                mTopicId = (String)data.get(Constants.KEY_ID);
                initTopicProducts();
                MarketAPI.getRecommendProducts(getApplicationContext(), this, mTopicId, 100, 0);
                mCurrentViewType = VIEW_TYPE_TOPIC_PRODUCTS;
            }
            
        } else if(mCurrentViewType == VIEW_TYPE_TOPIC_PRODUCTS) {
            
            // 专题产品列表，去产品详细页
            data = (HashMap<String, Object>) mListAdapterLevel2.getItem(position);
            String packageName = (String) data.get(Constants.KEY_PRODUCT_PACKAGE_NAME);
            Intent detailIntent = new Intent(getApplicationContext(), PreloadActivity.class);
            detailIntent.putExtra(Constants.EXTRA_PACKAGE_NAME, packageName);
            startActivity(detailIntent);
            return;
        }
        mViewAnimator.showNext();
        mCurrentLevel++;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
        case R.id.iv_header_left:
            
            Utils.trackEvent(getApplicationContext(), Constants.GROUP_5,
                    Constants.ENTRY);
            Intent intent = new Intent(getApplicationContext(), InstallNecessaryActivity.class);
            startActivity(intent);
            break;
            
        case R.id.iv_header_right:
            
            Utils.trackEvent(getApplicationContext(), Constants.GROUP_5,
                    Constants.CLICK_TOPIC_ENTRY);
            
            // 进入专题二级分类列表
            mViewAnimator.setOutAnimation(sLeftOutAnimation);
            mViewAnimator.setInAnimation(sRighttInAnimation);
            initListView(null);
            mSecondLevelTitle = getString(R.string.category_title_genius);
            updateNavigationTitle(mSecondLevelTitle, true);
            MarketAPI.getTopic(getApplicationContext(), this);
            mViewAnimator.showNext();
            mCurrentLevel++;
            mCurrentViewType = VIEW_TYPE_TOPIC_CATEGORY;
            break;
            
        case R.id.no_data:
            
            if (mCurrentViewType == VIEW_TYPE_CATEGORY) {
                mNoData.setVisibility(View.GONE);
                mProgress.setVisibility(View.VISIBLE);
                MarketAPI.getAllCategory(getApplicationContext(), this);
            } else if (mCurrentViewType == VIEW_TYPE_TOPIC_PRODUCTS) {
                mLoadingNoData2.setVisibility(View.GONE);
                mLoadingProgress2.setVisibility(View.VISIBLE);
                MarketAPI.getRecommendProducts(getApplicationContext(), this, mTopicId, 100, 0);
            } else {
                mLoadingNoData1.setVisibility(View.GONE);
                mLoadingProgress1.setVisibility(View.VISIBLE);
                MarketAPI.getTopic(getApplicationContext(), this);
            }
            break;
            
        default:
            break;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(int method, Object obj) {
        if (method == MarketAPI.ACTION_GET_ALL_CATEGORY) {
            mAdapter.addData((ArrayList<HashMap<String, Object>>) obj);
        } else if (method == MarketAPI.ACTION_GET_TOPIC) {
            mListAdapterLevel1.addData((ArrayList<HashMap<String, Object>>) obj);
        } else if(method == MarketAPI.ACTION_GET_RECOMMEND_PRODUCTS) {
            mListAdapterLevel2.addData((ArrayList<HashMap<String, Object>>)
                    ((HashMap<String, Object>)obj).get(Constants.KEY_PRODUCT_LIST));
        }
    }

    @Override
    public void onError(int method, int statusCode) {

        if (method == MarketAPI.ACTION_GET_ALL_CATEGORY) {
            mNoData.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.GONE);
        } else if (method == MarketAPI.ACTION_GET_TOPIC) {
            mLoadingNoData1.setVisibility(View.VISIBLE);
            mLoadingProgress1.setVisibility(View.GONE);
        } else if (method == MarketAPI.ACTION_GET_RECOMMEND_PRODUCTS) {
            mLoadingNoData2.setVisibility(View.VISIBLE);
            mLoadingProgress2.setVisibility(View.GONE);
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode && mCurrentLevel > 0) {

            mViewAnimator.setOutAnimation(sRightOutAnimation);
            mViewAnimator.setInAnimation(sLeftInAnimation);

            if (mCurrentViewType == VIEW_TYPE_OTHERS_PRODUCT_LIST) {
                // 返回到二级目录
                updateNavigationTitle(mSecondLevelTitle, true);
                mCurrentViewType = VIEW_TYPE_SUBCATEGORY;
            } else if (mCurrentViewType == VIEW_TYPE_TOPIC_PRODUCTS) {
                // 返回到二级目录
                updateNavigationTitle(mSecondLevelTitle, true);
                mCurrentViewType = VIEW_TYPE_TOPIC_CATEGORY;
            } else if (mCurrentViewType == VIEW_TYPE_SUBCATEGORY
                    || mCurrentViewType == VIEW_TYPE_APP_PRODUCT_LIST
                    || mCurrentViewType == VIEW_TYPE_TOPIC_CATEGORY) {
                // 返回到顶级目录
                updateNavigationTitle(mTopLevelTitle, false);
                mCurrentViewType = VIEW_TYPE_CATEGORY;
            }
            if (mTabHost != null) {
                LocalActivityManager lam = getLocalActivityManager();
                lam.removeAllActivities();
            }
            mViewAnimator.showPrevious();
            int viewIndex = mCurrentLevel--;
            View v = mViewAnimator.getChildAt(viewIndex);
            if (v != null) {
                mViewAnimator.removeViewAt(viewIndex);
            }
            return true;
        }
        return getParent().onKeyDown(keyCode, event);
    }
    
    /*
     * 更新导航栏标题
     */
    private void updateNavigationTitle(String title, boolean isDeep) {
        TextView titleView = (TextView) findViewById(R.id.top_bar_title);
        if(isDeep) {
            titleView.setText(title);
            titleView
                    .setCompoundDrawablesWithIntrinsicBounds(R.drawable.topbar_navigation, 0, 0, 0);
        } else {
            titleView.setText(title);
            titleView
            .setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }
    
    /* 分级列表动画 */
    private static TranslateAnimation sLeftOutAnimation;
    private static TranslateAnimation sLeftInAnimation;
    private static TranslateAnimation sRightOutAnimation;
    private static TranslateAnimation sRighttInAnimation;
    /*
     * 初始化分级列表移动动画
     */
    private void initAnimation() {
        sLeftOutAnimation = new TranslateAnimation(0, -width, 0, 0);
        sRighttInAnimation = new TranslateAnimation(width, 0, 0, 0);
        sLeftInAnimation = new TranslateAnimation(-width, 0, 0, 0);
        sRightOutAnimation = new TranslateAnimation(0, width, 0, 0);
        sLeftOutAnimation.setDuration(ANIMATION_DURATION);
        sRighttInAnimation.setDuration(ANIMATION_DURATION);
        sLeftInAnimation.setDuration(ANIMATION_DURATION);
        sRightOutAnimation.setDuration(ANIMATION_DURATION);
    }
    
    /*
     * 初始化子分类列表（二级分类）或者 专题列表
     */
    private void initListView(ArrayList<HashMap<String, Object>> data) {
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
                data,
                R.layout.activity_category_list_item, 
                new String[] { 
                        Constants.KEY_CATEGORY_ICON_URL,
                        Constants.KEY_CATEGORY_NAME,
                        Constants.KEY_TOP_APP,
                        Constants.KEY_APP_COUNT }, 
                        new int[] { 
                        R.id.iv_icon, 
                        R.id.tv_category_name,
                        R.id.tv_category_description, 
                        R.id.tv_app_num });
        mListViewLevel1.setAdapter(mListAdapterLevel1);
        mListViewLevel1.setEmptyView(mLoadingLevel1);
        mListViewLevel1.setOnItemClickListener(this);
        mViewAnimator.addView(listViewLayout);
    }
    
    /*
     * 初始化专题产品列表，此时处于第三层级
     */
    private void initTopicProducts() {
        FrameLayout listViewLayout = (FrameLayout) LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.common_list_view, null, false);
        mListViewLevel2 = (ListView) listViewLayout.findViewById(android.R.id.list);
        mLoadingLevel2 = (FrameLayout) listViewLayout.findViewById(R.id.loading);
        mLoadingProgress2 = (ProgressBar) listViewLayout.findViewById(R.id.progressbar);
        mLoadingProgress2.setIndeterminateDrawable(new LoadingDrawable(getApplicationContext()));
        mLoadingProgress2.setVisibility(View.VISIBLE);
        mLoadingNoData2 = (TextView) listViewLayout.findViewById(R.id.no_data);
        mLoadingNoData2.setOnClickListener(this);
        mListAdapterLevel2 = new AppListAdapter(getApplicationContext(),
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
        mListAdapterLevel2.setProductList();
        mListViewLevel2.setAdapter(mListAdapterLevel2);
        mListViewLevel2.setEmptyView(mLoadingLevel2);
        mListViewLevel2.setOnItemClickListener(this);
        mViewAnimator.addView(listViewLayout);
    }
    
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
        
        mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
            
            @Override
            public void onTabChanged(String tabId) {
                if (TAB_NEW.equals(tabId)) {
                    Utils.trackEvent(getApplicationContext(), Constants.GROUP_5,
                            Constants.CLICK_SUB_CATEGORY_NEW_TAB);
                }
            }
        });
        
        mViewAnimator.addView(mTabHost);
    }
    
}