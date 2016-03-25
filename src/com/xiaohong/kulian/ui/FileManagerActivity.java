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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.common.util.TopBar;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.widget.AppListAdapter;
import com.xiaohong.kulian.common.widget.BaseActivity;
import com.xiaohong.kulian.common.widget.LoadingDrawable;

/**
 * this view is displaying for manage application tab in home
 * @author cong.li
 * @date 2011-5-9
 * 
 */
public class FileManagerActivity extends BaseActivity implements OnItemClickListener {

    private static final int REFRESH_LOCAL_FILE = 0;
    private static final int REFRESH_LOCAL_NO_FILE = 1;
    private static final int CONTEXT_MENU_DELETE_FILE = 2;
    private static final int CONTEXT_MENU_DELETE_ALL_FILE = 3;
    
    private AppListAdapter mFileAdapter;
    private ListView mList;
    private TextView mNoData;
    private ProgressBar mProgress;
    private int mLongClickPos;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_common_layout);
		initTopBar();
		initView();
		initData();
	}
	
    @Override
    protected void onDestroy() {
        mSession.deleteObserver(mFileAdapter);
        super.onDestroy();
    }

    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            
            switch (msg.what) {

            case REFRESH_LOCAL_FILE:
                
                @SuppressWarnings("unchecked")
                ArrayList<HashMap<String, Object>> result = 
                    (ArrayList<HashMap<String, Object>>) msg.obj;
                mFileAdapter.addData(result);
                break;
            
            case REFRESH_LOCAL_NO_FILE:
                
                refreshNoFile();
                break;
                
            case CONTEXT_MENU_DELETE_ALL_FILE:
                
                mFileAdapter.clearData();
                refreshNoFile();
                break;

            default:
                break;
            }
        }
    };
    
    /*
     * 刷新无文件状态
     */
    private void refreshNoFile() {
        mNoData.setCompoundDrawablesWithIntrinsicBounds(null,
                getResources().getDrawable(R.drawable.no_apk), null, null);
        mNoData.setText(R.string.no_apk_file);
        mNoData.setCompoundDrawablePadding(10);
        mNoData.setVisibility(View.VISIBLE);
        mProgress.setVisibility(View.GONE);
    }

	private void initTopBar() {
		TopBar.createTopBar(getApplicationContext(),
				new View[] { findViewById(R.id.top_bar_title) },
				new int[] { View.VISIBLE },
				getString(R.string.manager_installed_title));
	}

	private void initView() {
	    
		mList = (ListView) findViewById(android.R.id.list);
		FrameLayout mLoading = (FrameLayout) findViewById(R.id.loading);
        mProgress = (ProgressBar) mLoading.findViewById(R.id.progressbar);
        mProgress.setIndeterminateDrawable(new LoadingDrawable(getApplicationContext()));
        mProgress.setVisibility(View.VISIBLE);
        mNoData = (TextView) mLoading.findViewById(R.id.no_data);
        
        mList.setEmptyView(mLoading);
        mList.setOnItemClickListener(this);
		mFileAdapter = doInitAdapter();
		mFileAdapter.setContainsPlaceHolder(true);
		mFileAdapter.setPlaceHolderResource(R.layout.common_list_separator);

		mList.setAdapter(mFileAdapter);
		registerForContextMenu(mList);
	}

	/**
	 * 初始化Adapter
	 */
    private AppListAdapter doInitAdapter() {
        
        AppListAdapter adapter = new AppListAdapter(getApplicationContext(), null,
                R.layout.activity_apps_manager_uninstalled_item,
                new String[] { 
                        Constants.KEY_PRODUCT_ICON, 
                        Constants.KEY_PRODUCT_NAME,
                        Constants.KEY_PRODUCT_DESCRIPTION }, 
                new int[] { 
                        R.id.app_icon, 
                        R.id.app_name,
                        R.id.info });
        return adapter;
    }
    
    private void initData() {
        new Thread() {
            @Override
            public void run() {
                // 检查本地SD卡
                ArrayList<HashMap<String, Object>> apks = Utils
                        .getLocalApks(getApplicationContext());
                if (apks == null || apks.size() <= 0) {
                    // 本地没有APK
                    mHandler.sendEmptyMessage(REFRESH_LOCAL_NO_FILE);
                    return;
                }
                Message msg = mHandler.obtainMessage();
                msg.what = REFRESH_LOCAL_FILE;
                msg.obj = apks;
                mHandler.sendMessage(msg);
            }
        }.start();
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) menuInfo;
        mLongClickPos = acmi.position;
        menu.add(0, CONTEXT_MENU_DELETE_FILE, 0, R.string.delete);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case CONTEXT_MENU_DELETE_FILE:
            @SuppressWarnings("unchecked")
            HashMap<String, Object> listItem = (HashMap<String, Object>) mFileAdapter
                    .getItem(mLongClickPos);
            String filePath = (String) listItem.get(Constants.KEY_PRODUCT_INFO);
            boolean result = Utils.deleteFile(filePath);
            if(result) {
                mFileAdapter.removeData(mLongClickPos);
                if (mFileAdapter.getCount() == 0) {
                    mHandler.sendEmptyMessage(REFRESH_LOCAL_NO_FILE);
                }
            } else {
                // 文件删除失败
                Utils.makeEventToast(getApplicationContext(),
                        getString(R.string.warining_delete_fail), false);
            }
            break;
        default:
            return super.onContextItemSelected(item);
        }
        return true;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, getString(R.string.menu_del_all)).setIcon(R.drawable.ic_menu_delete);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        delAllFile();
        return true;
    }
    
    /*
     * 删除所有文件
     */
    private void delAllFile() {

        new Thread() {
            public void run() {

                for (int i = 0, length = mFileAdapter.getCount(); i < length; i++) {
                    @SuppressWarnings("unchecked")
                    HashMap<String, Object> listItem = (HashMap<String, Object>) mFileAdapter
                            .getItem(i);
                    String filePath = (String) listItem.get(Constants.KEY_PRODUCT_INFO);
                    if (!TextUtils.isEmpty(filePath)) {
                        Utils.deleteFile(filePath);
                    }
                }
                mHandler.sendEmptyMessage(CONTEXT_MENU_DELETE_ALL_FILE);
            };
        }.start();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        @SuppressWarnings("unchecked")
        HashMap<String, Object> item = (HashMap<String, Object>) mFileAdapter.getItem(position);

        if (item != null) {
            String path = (String) item.get(Constants.KEY_PRODUCT_DESCRIPTION);

            if (!TextUtils.isEmpty(path)) {
                File file = new File(path);
                if (file.exists()) {
                    Utils.installApk(getApplicationContext(), new File(path));
                } else {
                    Utils.makeEventToast(getApplicationContext(),
                            getString(R.string.install_fail_file_not_exist), false);
                }
            }
        }
    }
}