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

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.mappn.gfan.Constants;
import com.mappn.gfan.R;
import com.mappn.gfan.common.util.TopBar;
import com.mappn.gfan.common.util.Utils;
import com.mappn.gfan.common.widget.BaseActivity;
import com.mappn.gfan.common.widget.LoadingDrawable;
import com.mappn.gfan.ui.AppsManagerAdapter.AppItem;

/**
 * 我的应用管理页
 * 
 * @author andrew
 * @date 2011-1-13
 * @since Version 0.5.1 Dev
 */
public class AppsManagerActivity extends BaseActivity implements OnItemClickListener {

    // private static final String TAG = "AppsManagerActivity";

    private final static int CONTEXT_MENU_DELETE_FILE = 0;
    private final static int CONTEXT_MENU_IGNORE_UPDATE = 1;
    
    private FrameLayout mLoading;
    private ProgressBar mProgress;
    private ListView mList;
    private AppsManagerAdapter mAdapter;

    private BroadcastReceiver mInstallReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            final String packageName = intent.getData().getSchemeSpecificPart();

            if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {

                mAdapter.installAppWithPackageName(packageName);

            } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {

                mAdapter.removedAppWithPackageName(packageName);

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps_manager_layout);
        doInitView(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(mInstallReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mInstallReceiver);
        mAdapter.close();
        mAdapter = null;
    }

    public boolean doInitView(Bundle savedInstanceState) {

        TopBar.createTopBar(getApplicationContext(), 
                new View[] { 
                    findViewById(R.id.top_bar_title),
                    findViewById(R.id.top_bar_files)
                }, 
                new int[] { 
                    View.VISIBLE, 
                    View.VISIBLE 
                },
                getString(R.string.app_manager_title));

        mLoading = (FrameLayout) findViewById(R.id.loading);
        mProgress = (ProgressBar) mLoading.findViewById(R.id.progressbar);
        mProgress.setIndeterminateDrawable(new LoadingDrawable(getApplicationContext()));
        mProgress.setVisibility(View.VISIBLE);
        
        mList = (ListView) findViewById(android.R.id.list);
        mList.setEmptyView(mLoading);
        mList.setOnItemClickListener(this);
        mList.setItemsCanFocus(true);
        mAdapter = doInitListAdapter();
        mList.setAdapter(mAdapter);
        registerForContextMenu(mList);

        return true;
    }

    public AppsManagerAdapter doInitListAdapter() {
        return new AppsManagerAdapter(getApplicationContext(), null);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        AppItem item = (AppItem) mAdapter.getItem(arg2);
        if (item.mViewType == AppsManagerAdapter.VIEW_TYPE_INSTALLED) {
            Utils.trackEvent(getApplicationContext(), Constants.GROUP_7,
                    Constants.OPEN_PRODUCT_DETAIL);
            // 已经安装的产品，点击去详细页
            Intent detailIntent = new Intent(getApplicationContext(), PreloadActivity.class);
            detailIntent.putExtra(Constants.EXTRA_PACKAGE_NAME, item.mPackageName);
            startActivity(detailIntent);
        } else if(item.mViewType == AppsManagerAdapter.VIEW_TYPE_DOWNLOADED) {
            // 已经下载的产品，点击安装
            Utils.installApk(getApplicationContext(), new File(item.mFilePath));
        } else if(item.mViewType == AppsManagerAdapter.VIEW_TYPE_UPDATE_ALL) {
            // 安装全部更新
            mAdapter.updateAll();
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return getParent().onKeyDown(keyCode, event);
    }
    
    private int mLongClickPos;
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) menuInfo;
        mLongClickPos = acmi.position;
        AppItem item = (AppItem) mAdapter.getItem(mLongClickPos);
        int viewType = item.mViewType;
        if (viewType == AppsManagerAdapter.VIEW_TYPE_DOWNLOADED) {
            
            menu.add(0, CONTEXT_MENU_DELETE_FILE, 0, R.string.delete);
        } else if (item.mWeight == AppsManagerAdapter.WEIGHT_NORMAL_UPDATE) {
            
            menu.add(0, CONTEXT_MENU_IGNORE_UPDATE, 0, R.string.ignore_update);
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case CONTEXT_MENU_DELETE_FILE:
            mAdapter.delApp(mLongClickPos);
            break;
            
        case CONTEXT_MENU_IGNORE_UPDATE:
            mAdapter.ignoreUpdate(mLongClickPos);
            break;
            
        default:
            return super.onContextItemSelected(item);
        }
        return true;
    }

}
