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

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.common.util.TopBar;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.widget.BaseTabActivity;

/**
 * the view is displaying for rank tab in home
 * 
 * @author cong.li
 * @date 2011-5-9
 * @since Version 0.7.0
 */

public class RankTabActivity extends BaseTabActivity implements OnTabChangeListener {

    /**排行榜100*/
    private static final int MAX_ITEMS = 100;
	private TabHost mTabHost;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rank);

		initTopBar();
		initView();
	}

	private void initView() {

		mTabHost = (TabHost) this.findViewById(android.R.id.tabhost);
		mTabHost.setup();
		mTabHost.getTabWidget().setDividerDrawable(R.drawable.tab_divider);

        Intent appIntent = new Intent(getApplicationContext(), ProductListActivity.class);
        appIntent.putExtra(Constants.EXTRA_CATEGORY, Constants.CATEGORY_RCMD);
        appIntent.putExtra(Constants.EXTRA_MAX_ITEMS, MAX_ITEMS);
		TabSpec tab1 = mTabHost
				.newTabSpec(Constants.CATEGORY_RCMD)
				.setIndicator(
						Utils.createTabView(getApplicationContext(),
								getString(R.string.rank_tab_rcmd)))
				.setContent(appIntent);
		//do not show recommend tab
		//mTabHost.addTab(tab1);
		
        Intent gameIntent = new Intent(getApplicationContext(), ProductListActivity.class);
        gameIntent.putExtra(Constants.EXTRA_CATEGORY, Constants.CATEGORY_APP);
        gameIntent.putExtra(Constants.EXTRA_MAX_ITEMS, MAX_ITEMS);
		TabSpec tab2 = mTabHost
				.newTabSpec(Constants.CATEGORY_APP)
				.setIndicator(
						Utils.createTabView(getApplicationContext(),
								getString(R.string.rank_tab_app)))
				.setContent(gameIntent);
		mTabHost.addTab(tab2);
		
        Intent bookIntent = new Intent(getApplicationContext(), ProductListActivity.class);
        bookIntent.putExtra(Constants.EXTRA_CATEGORY, Constants.CATEGORY_GAME);
        bookIntent.putExtra(Constants.EXTRA_MAX_ITEMS, MAX_ITEMS);
		TabSpec tab3 = mTabHost
				.newTabSpec(Constants.CATEGORY_GAME)
				.setIndicator(
						Utils.createTabView(getApplicationContext(),
								getString(R.string.rank_tab_game)))
				.setContent(bookIntent);
		//do not show game tab
		//mTabHost.addTab(tab3);
		
        Intent growIntent = new Intent(getApplicationContext(), ProductListActivity.class);
        growIntent.putExtra(Constants.EXTRA_CATEGORY, Constants.CATEGORY_TASK);
        growIntent.putExtra(Constants.EXTRA_MAX_ITEMS, MAX_ITEMS);
		TabSpec tab4 = mTabHost
				.newTabSpec(Constants.CATEGORY_TASK)
				.setIndicator(
						Utils.createTabView(getApplicationContext(),
								getString(R.string.rank_tab_task)))
				.setContent(growIntent);
		mTabHost.addTab(tab4);
		mTabHost.setOnTabChangedListener(this);
	}
	
	/**
	 * 初始化Topbar
	 */
	private void initTopBar() {
        TopBar.createTopBar(getApplicationContext(), 
                new View[] { findViewById(R.id.top_bar_title) }, 
                new int[] { View.VISIBLE }, getString(R.string.rank_top_title));
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return getParent().onKeyDown(keyCode, event);
    }

    @Override
    public void onTabChanged(String tabId) {
        if (Constants.CATEGORY_APP.equals(tabId)) {
            Utils.trackEvent(getApplicationContext(), Constants.GROUP_6, Constants.CLICK_RANK_APP);
        } else if (Constants.CATEGORY_GAME.equals(tabId)) {
            Utils.trackEvent(getApplicationContext(), Constants.GROUP_6, Constants.CLICK_RANK_GAME);
        } else if (Constants.CATEGORY_EBOOK.equals(tabId)) {
            Utils.trackEvent(getApplicationContext(), Constants.GROUP_6, Constants.CLICK_RANK_BOOK);
        } else if (Constants.CATEGORY_GROW.equals(tabId)) {
            Utils.trackEvent(getApplicationContext(), Constants.GROUP_6, Constants.CLICK_RANK_POP);
        }
    }
}
