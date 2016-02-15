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

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.mappn.gfan.Constants;
import com.mappn.gfan.R;
import com.mappn.gfan.common.ApiAsyncTask.ApiRequestListener;
import com.mappn.gfan.common.MarketAPI;
import com.mappn.gfan.common.util.DBUtils;
import com.mappn.gfan.common.util.TopBar;
import com.mappn.gfan.common.util.Utils;
import com.mappn.gfan.common.widget.BaseTabActivity;

/**
 * this view is displaying for search tab in home
 * 
 * @author cong.li
 * @date 2011-5-9
 * @since Version 0.7.0
 */

public class SearchActivity extends BaseTabActivity implements OnClickListener,
        OnFocusChangeListener, ApiRequestListener {
	
    // 定义TextView的Padding属性
    private static final int PADING_TOP_BOTTOM = 15;
    private static final int PADING_LEFT_RIGHT = 20;
//    private static final int PAD_RIGHT = 10;
//    private static final int PAD_BOTTOM = 10;

    // 定义TextView的Margin属性
    private static final int MARGIN_LEFT_RIGHT = 15;
    private static final int MARGIN_TOP_BOTTOM = 20;

    private static int[] sHotBackgound;
    private LinearLayout mKeywordsLayout;
    
	// Tab id
	private static final String TAB_PRODUCT_ID = "product";
	private static final String TAB_BBS_ID = "bbs";
	
	private TabHost mTabHost;
	private ImageButton searchBtn;
	private AutoCompleteTextView mAutoCompleteTextView;
	private ArrayAdapter<String> mSearchHistoryAdapter;
	private ArrayList<String> mHistory;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_layout);
		
		initTopBar();
		initView();
		initData();
	}
	
	private void initTopBar() {
        TopBar.createTopBar(getApplicationContext(), 
                new View[] { findViewById(R.id.top_bar_input),
                findViewById(R.id.top_bar_search) }, 
                new int[] { View.VISIBLE, View.VISIBLE }, "");
		 findViewById(R.id.top_bar_search).setOnClickListener(this);
	}

	private void initData() {
	    mHistory = DBUtils.querySearchHistory(getApplicationContext());
	    
		mSearchHistoryAdapter = new ArrayAdapter<String>(getApplicationContext(),
				R.layout.activity_search_autotext_list, mHistory);
		mAutoCompleteTextView.setAdapter(mSearchHistoryAdapter);
		mAutoCompleteTextView.setThreshold(1);
		
		MarketAPI.getSearchKeywords(getApplicationContext(), this);
	}
	
    private void initView() {

        searchBtn = (ImageButton) this.findViewById(R.id.top_bar_search);
        searchBtn.setOnClickListener(this);
        mAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.top_bar_input);
        mAutoCompleteTextView.setOnFocusChangeListener(this);
        
        sHotBackgound = new int[] { 
                R.drawable.keyword_bg_1, R.drawable.keyword_bg_1, R.drawable.keyword_bg_1,
                R.drawable.keyword_bg_2, R.drawable.keyword_bg_2, R.drawable.keyword_bg_2,
                R.drawable.keyword_bg_3, R.drawable.keyword_bg_3, R.drawable.keyword_bg_3, 
                R.drawable.keyword_bg_4, R.drawable.keyword_bg_4, R.drawable.keyword_bg_4, 
                R.drawable.keyword_bg_5, R.drawable.keyword_bg_5, R.drawable.keyword_bg_5 };
        mKeywordsLayout = (LinearLayout) findViewById(R.id.tab_content_linearLayout);
        
        initTabView();
    }
    
    private void initTabView() {

        mTabHost = (TabHost) this.findViewById(android.R.id.tabhost);
        mTabHost.setup();

        Intent marketIntent = new Intent(getApplicationContext(), SearchResultActivity.class);
        marketIntent.putExtra(Constants.EXTRA_SEARCH_TYPE, Constants.SEARCH_TYPE_MARKET);
        TabSpec tab1 = mTabHost
                .newTabSpec(TAB_PRODUCT_ID)
                .setIndicator(
                        Utils.createSearchTabView(getApplicationContext(),
                                getString(R.string.tab_product))).setContent(marketIntent);
        mTabHost.addTab(tab1);

        Intent bbsIntent = new Intent(getApplicationContext(), SearchResultActivity.class);
        bbsIntent.putExtra(Constants.EXTRA_SEARCH_TYPE, Constants.SEARCH_TYPE_BBS);
        TabSpec tab2 = mTabHost
                .newTabSpec(TAB_BBS_ID)
                .setIndicator(
                        Utils.createSearchTabView(getApplicationContext(),
                                getString(R.string.tab_bbs))).setContent(bbsIntent);
        mTabHost.addTab(tab2);
        
        mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
            
            @Override
            public void onTabChanged(String tabId) {
                
                if (TAB_BBS_ID.equals(tabId)) {
                    Utils.trackEvent(getApplicationContext(), Constants.GROUP_2,
                            Constants.CLICK_SEARCH_BBS);
                }
                doSearch();
            }
        });
    }
    
    private void showTabView() {
        resetCurrentActivity();
        updateMarketResultNumber(0);
        updateBbsResultNumber(0);
        mTabHost.setVisibility(View.VISIBLE);
        mKeywordsLayout.setVisibility(View.GONE);
    }
    
    /*
     * 初始化搜索热词VIew
     */
    private void initSearchKeywordsView(ArrayList<String> keywords) {
        
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        
        // the display screen width
        int displayWidth = getWindowManager().getDefaultDisplay().getWidth();
        
        float rowWidth = 0f;
        LinearLayout rowLayout = null;
        for (int i = 0, length = keywords.size(); i < length; i++) {
            String text = keywords.get(i);
            TextView keywordView = createTextView(i, text);

            float textWidth = keywordView.getPaint().measureText(text)
                    + PADING_LEFT_RIGHT * 2 + MARGIN_LEFT_RIGHT * 2;
            rowWidth += textWidth;
            if (rowLayout == null 
                    || rowWidth > displayWidth) {
                // 创建一个新的横向布局 或者 子View超过屏幕宽度，折行显示
                rowWidth = textWidth;
                rowLayout = getHorizontalLinearLayout(lp);
                mKeywordsLayout.addView(rowLayout);
            }
            rowLayout.addView(keywordView);
        }
    }
    
    /*
     * 生成新的水平方向的LinearLayout
     */
    private LinearLayout getHorizontalLinearLayout(LayoutParams lp) {
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setLayoutParams(lp);
        layout.setGravity(Gravity.CENTER);
        return layout;
    }

    /*
     * 创建一新的热词View
     */
    private TextView createTextView(int position, String text) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = MARGIN_LEFT_RIGHT;
        lp.rightMargin = MARGIN_LEFT_RIGHT;
        lp.topMargin = MARGIN_TOP_BOTTOM;
        lp.bottomMargin = MARGIN_TOP_BOTTOM;
        TextView view = new TextView(getApplicationContext());
        view.setText(text);
        view.setLayoutParams(lp);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setOnClickListener(this);
        view.setBackgroundResource(sHotBackgound[position]);
        view.setPadding(PADING_LEFT_RIGHT, PADING_TOP_BOTTOM, PADING_LEFT_RIGHT, PADING_TOP_BOTTOM);
        view.setTextAppearance(getApplicationContext(), R.style.hot_font);
        view.setFocusableInTouchMode(false);
        view.setGravity(Gravity.CENTER);
        return view;
    }
	
	@Override
    public void onClick(View v) {
	    
        switch (v.getId()) {
        
        case R.id.top_bar_search:
            // 点击搜索按钮
            Utils.trackEvent(getApplicationContext(), Constants.GROUP_2, Constants.CLICK_SEARCH);
            doSearch();
            break;
            
        default:
            // 点击关键词
            Utils.trackEvent(getApplicationContext(), Constants.GROUP_2,
                    Constants.CLICK_SEARCH_KEYWORDS);
            mAutoCompleteTextView.setText(((TextView) v).getText());
            doSearch();
            break;
        }
    }
	
	private void doSearch() {
	    
	    String content = mAutoCompleteTextView.getText().toString().trim();

        if (TextUtils.isEmpty(content)) {
            resetCurrentActivity();
            return;
        }

        if (!mTabHost.isShown()) {
            mTabHost.setVisibility(View.VISIBLE);
            mKeywordsLayout.setVisibility(View.GONE);
            // 开始搜索
            mTabHost.setCurrentTabByTag(TAB_PRODUCT_ID);
        }
        
        storeToAdapter(content);
        
        toogleInputMethod(false);

        SearchResultActivity resultActivity = (SearchResultActivity) getCurrentActivity();
        resultActivity.setSearchKeyword(content);
        resultActivity.lazyload();
	}
	
	private void resetCurrentActivity() {
	    SearchResultActivity resultActivity = (SearchResultActivity) getCurrentActivity();
        resultActivity.resetSearchResult();
	}

	/**
	 * 将搜索内容添加到数据源adapter
	 */
	private void storeToAdapter(String content) {
	    
        if (!mHistory.contains(content)) {
            mSearchHistoryAdapter.add(content);
            DBUtils.addSearchItem(getApplicationContext(), content);
            mHistory.add(content);
            mSearchHistoryAdapter.notifyDataSetChanged();
        }
		
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		switch (v.getId()) {
		case R.id.top_bar_input:
			toogleInputMethod(hasFocus);
			break;

		default:
			break;
		}
	}
	
    public void setKeyword(String text) {
        if (mAutoCompleteTextView != null) {
            mAutoCompleteTextView.setText(text);
        }
    }
	
    public String getKeyword() {
        if (mAutoCompleteTextView != null) {
            return mAutoCompleteTextView.getText().toString();
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(int method, Object obj) {
        switch (method) {
        
        case MarketAPI.ACTION_GET_SEARCH_KEYWORDS:
            initSearchKeywordsView((ArrayList<String>) obj);
            break;
        }
    }

    @Override
    public void onError(int method, int statusCode) {
        Utils.D("fetch keywords fail because of status " + statusCode);
    }
    
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (mTabHost != null && mTabHost.isShown()) {
            mAutoCompleteTextView.setText("");
            mTabHost.setCurrentTabByTag(TAB_PRODUCT_ID);
            mTabHost.requestFocus();
            mTabHost.setVisibility(View.GONE);
            resetCurrentActivity();
            mKeywordsLayout.setVisibility(View.VISIBLE);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    
    // toogle input method status
    private void toogleInputMethod(boolean flag) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (flag) {
            imm.showSoftInputFromInputMethod(mAutoCompleteTextView.getWindowToken(), 0);
            showTabView();
        } else {
            imm.hideSoftInputFromWindow(mAutoCompleteTextView.getWindowToken(), 0);
        }
    }
	
    
    /*package*/ void updateMarketResultNumber(int num) {
        
        if (mTabHost == null) {
            return;
        }
        
        TextView tv = (TextView) mTabHost.getTabWidget().getChildTabViewAt(0);
        if(num > 0) {
            tv.setText(getString(R.string.tab_product_result, String.valueOf(num)));
        } else {
            tv.setText(R.string.tab_product);
        }
    }
    
    /*package*/ void updateBbsResultNumber(int num) {
        
        if (mTabHost == null) {
            return;
        }
        
        TextView tv = (TextView) mTabHost.getTabWidget().getChildTabViewAt(1);
        if(num > 0) {
            tv.setText(getString(R.string.tab_bbs_result, String.valueOf(num)));
        } else {
            tv.setText(R.string.tab_bbs);
        }
    }
}