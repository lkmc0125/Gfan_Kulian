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
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;

import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.common.util.MySharedpreference;
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

    private static final String TAG = "RankTabActivity";

    /** 排行榜100 */
    private static final int MAX_ITEMS = 100;
    private TabHost mTabHost;
    private String action="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //注册一个广播接收器，用于初始化tab的选取页
//        registerReceivers();
        setContentView(R.layout.activity_rank);
        registerReceivers();
//        getWindow().setBackgroundDrawableResource(android.R.color.darker_gray);
        initView();
//        initAdPager();  // banner
        Log.d(TAG, "onCreate");
    }

    /**
     * added by albert liu 2016/4/10
     */
    private void registerReceivers() {
        /**
         * Albert 2016/4/10注册查看所有的任务和推荐的广播
         */
        IntentFilter mCheckAllfilter = new IntentFilter();
        mCheckAllfilter.addAction(Constants.BROADCAST_CATEGORY_TASK);
        mCheckAllfilter.addAction(Constants.BROADCAST_CATEGORY_RCMD);
        registerReceiver(mCheckAllReceiver, mCheckAllfilter);
    }
    /**
     * added by albert liu 2016/4/10
     */
    private void unregisterReceiver() {
        unregisterReceiver(mCheckAllReceiver);
    }
    /**
     * added by albert liu 2016/4/10
     */
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterReceiver();
    }
    /**
     * added by albert liu 2016/4/10
     */
 // 打开推荐和任务详情
    private BroadcastReceiver mCheckAllReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            System.out.println("mCheckAllReceiver"+intent.getAction());
            if (action.equals(Constants.BROADCAST_CATEGORY_TASK)) {
                mTabHost.setCurrentTab(1);
            } else if (action.equals(Constants.BROADCAST_CATEGORY_RCMD)) {
                mTabHost.setCurrentTab(0);
            } 
        }
    };
    private void initView() {

        mTabHost = (TabHost) this.findViewById(android.R.id.tabhost);
        mTabHost.setup();

        Intent gameIntent = new Intent(getApplicationContext(),
                ProductListActivity.class);
        gameIntent.putExtra(Constants.EXTRA_CATEGORY, Constants.CATEGORY_RCMD);
        gameIntent.putExtra(Constants.EXTRA_MAX_ITEMS, MAX_ITEMS);
        TabSpec tab2 = mTabHost
                .newTabSpec(Constants.CATEGORY_APP)
                .setIndicator(
                        Utils.createMakeMoneyPageTabView(getApplicationContext(),
                                getString(R.string.rank_tab_app)))
                .setContent(gameIntent);
        mTabHost.addTab(tab2);

        Intent growIntent = new Intent(getApplicationContext(),
                TaskListActivity.class);
        growIntent.putExtra(Constants.EXTRA_CATEGORY, Constants.CATEGORY_TASK);
        growIntent.putExtra(Constants.EXTRA_MAX_ITEMS, MAX_ITEMS);
        TabSpec tab4 = mTabHost
                .newTabSpec(Constants.CATEGORY_TASK)
                .setIndicator(
                        Utils.createMakeMoneyPageTabView(getApplicationContext(),
                                getString(R.string.rank_tab_task)))
                .setContent(growIntent);
        mTabHost.addTab(tab4);
        mTabHost.setOnTabChangedListener(this);
        changeTabStyle();
        MySharedpreference mySharedpreference = new MySharedpreference
                (this);
Map<String, Object> user=new HashMap<String, Object>();
user=mySharedpreference.getMessage();
action=user.get("type").toString();
        if (action.equals(Constants.BROADCAST_CATEGORY_TASK)) {
            mTabHost.setCurrentTab(1);
        } else if (action.equals(Constants.BROADCAST_CATEGORY_RCMD)) {
            mTabHost.setCurrentTab(0);
        } 
    }

    private void changeTabStyle() {
        TabWidget tabWidget = mTabHost.getTabWidget();
        for (int i = 0; i < tabWidget.getChildCount(); i++) {
            View view = tabWidget.getChildAt(i);
            //view.getLayoutParams().height = 130;
            //view.getLayoutParams().width = 65;
            
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return getParent().onKeyDown(keyCode, event);
    }

    @Override
    public void onTabChanged(String tabId) {
        System.out.println("onTabChanged"+tabId);
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
