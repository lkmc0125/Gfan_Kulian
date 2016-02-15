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

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mappn.gfan.Constants;
import com.mappn.gfan.R;
import com.mappn.gfan.common.ApiAsyncTask.ApiRequestListener;
import com.mappn.gfan.common.MarketAPI;
import com.mappn.gfan.common.download.DownloadManager.Request;
import com.mappn.gfan.common.util.TopBar;
import com.mappn.gfan.common.util.Utils;
import com.mappn.gfan.common.vo.DownloadItem;
import com.mappn.gfan.common.widget.AppListAdapter;
import com.mappn.gfan.common.widget.BaseActivity;
import com.mappn.gfan.common.widget.LoadingDrawable;

/**
 * 装机必备页
 * 
 * @author cong.li
 * @date 2011-5-9
 * @since Version 0.7.0
 */
public class InstallNecessaryActivity extends BaseActivity implements OnClickListener,
        ApiRequestListener {

//    private static final String TAG = "InstallNecessaryActivty";

    private ListView mList;

    // loading
    private FrameLayout mLoading;
    private ProgressBar mProgress;
    private TextView mNoData;

    private AppListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easy_installation_layout);

        initTopBar();
        initView();
        initData();
    }
    
    private void initTopBar() {
        TopBar.createTopBar(getApplicationContext(),
                new View[] { findViewById(R.id.top_bar_title) }, 
                new int[] { View.VISIBLE },
                getString(R.string.sort_install_nessary_title));
    }

    private void initData() {
        mAdapter = doInitAdapter();
        mList.setAdapter(mAdapter);
        MarketAPI.getRequired(getApplicationContext(), this);
    }

    private void initView() {
        Button btnExit = (Button) this.findViewById(R.id.btn_exit);
        btnExit.setOnClickListener(this);
        Button btnDownload = (Button) this.findViewById(R.id.btn_download);
        btnDownload.setOnClickListener(this);
        mList = (ListView) this.findViewById(android.R.id.list);

        mLoading = (FrameLayout) this.findViewById(R.id.loading);
        mProgress = (ProgressBar) mLoading.findViewById(R.id.progressbar);
        mProgress.setIndeterminateDrawable(new LoadingDrawable(getApplicationContext()));
        mProgress.setVisibility(View.VISIBLE);
        mNoData = (TextView) mLoading.findViewById(R.id.no_data);
        mNoData.setOnClickListener(this);
        mList.setEmptyView(mLoading);
        mList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mList.setItemsCanFocus(false);
    }

    /**
     * 初始化适配器
     */
    private AppListAdapter doInitAdapter() {

        AppListAdapter adapter = new AppListAdapter(
                getApplicationContext(), 
                null,
                R.layout.activity_install_nessary_item, 
                new String[] { 
                    Constants.INSTALL_APP_LOGO,
                    Constants.INSTALL_APP_TITLE,
                    Constants.INSTALL_APP_DESCRIPTION,
                    Constants.INSTALL_APP_IS_CHECKED,
                    Constants.KEY_PRODUCT_IS_INSTALLED
                }, new int[] { 
                    R.id.iv_logo, 
                    R.id.tv_name, 
                    R.id.tv_description, 
                    R.id.cb_install,
                    R.id.tv_installed
                });
        adapter.setContainsPlaceHolder(true);
        adapter.setPlaceHolderResource(R.layout.activity_install_nessary_list_separator);
        return adapter;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_exit:
            finish();
            break;
            
        case R.id.btn_download:
            startDownload();
            break;
            
        case R.id.no_data:
            mNoData.setVisibility(View.GONE);
            mProgress.setVisibility(View.VISIBLE);
            MarketAPI.getRequired(getApplicationContext(), this);
            break;
            
        default:
            break;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(int method, Object obj) {
        switch (method) {
        case MarketAPI.ACTION_GET_REQUIRED:
            mAdapter.addData((ArrayList<HashMap<String, Object>>) obj);
            break;
            
        case MarketAPI.ACTION_GET_DOWNLOAD_URL:
            DownloadItem info = (DownloadItem) obj;
            Request request = new Request(Uri.parse(info.url));
            HashMap<String, Object> item = mAdapter.getCheckedList().get(info.pId);
            request.setTitle((String) item.get(Constants.INSTALL_APP_TITLE));
            request.setPackageName(info.packageName);
            request.setIconUrl((String) item.get(Constants.INSTALL_APP_LOGO));
            request.setSourceType(com.mappn.gfan.common.download.Constants.DOWNLOAD_FROM_MARKET);
            request.setMD5(info.fileMD5);
            mSession.getDownloadManager().enqueue(request);
            break;

        default:
            break;
        }
    }

    @Override
    public void onError(int method, int statusCode) {
        mNoData.setVisibility(View.VISIBLE);
        mProgress.setVisibility(View.GONE);
    }
    
    private void startDownload() {
        HashMap<String, HashMap<String, Object>> list = mAdapter.getCheckedList();
        if (list == null || list.size() == 0) {
            Utils.makeEventToast(getApplicationContext(), getString(R.string.warning_no_download),
                    false);
            return;
        }

        for (HashMap<String, Object> item : list.values()) {
            String id = (String) item.get(Constants.KEY_PRODUCT_ID);
            MarketAPI.getDownloadUrl(getApplicationContext(), this, id, Constants.SOURCE_TYPE_GFAN);
        }
        Utils.makeEventToast(getApplicationContext(), getString(R.string.download_start), false);
    }
}
