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

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mappn.gfan.Constants;
import com.mappn.gfan.R;
import com.mappn.gfan.common.util.TopBar;
import com.mappn.gfan.common.widget.AppListAdapter;
import com.mappn.gfan.common.widget.BaseActivity;
import com.mappn.gfan.common.widget.LoadingDrawable;

/**
 * @author andrew
 * @date    2011-4-21
 *
 */
public class SafeActivity extends BaseActivity implements OnItemClickListener {

    private AppListAdapter mAdapter;
    private ListView mList;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_common_layout); 
        
        // init top bar
        TopBar.createTopBar(this, 
                new View[] { 
                    findViewById(R.id.top_bar_title) 
                    }, 
                new int[] {  
                    View.VISIBLE 
                    }, 
                getString(R.string.alert_safe));

        FrameLayout mLoading = (FrameLayout) findViewById(R.id.loading);
        ProgressBar mProgress = (ProgressBar) mLoading.findViewById(R.id.progressbar);
        mProgress.setIndeterminateDrawable(new LoadingDrawable(getApplicationContext()));
        mProgress.setVisibility(View.VISIBLE);

        mList = (ListView) findViewById(android.R.id.list);
        mList.setEmptyView(mLoading);
        mList.setOnItemClickListener(this);
        mList.addHeaderView(createHeaderView(), null, false);
        
        doInitList();
    }

    private void doInitList() {
        ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> item1 = new HashMap<String, Object>();
        item1.put(Constants.KEY_PRODUCT_ICON_URL, getResources().getDrawable(R.drawable.icon_360));
        item1.put(Constants.KEY_PRODUCT_NAME, "360手机安全卫士");
        item1.put(Constants.KEY_PRODUCT_INFO, "全方位的手机安全和隐私保护。");
        items.add(item1);
        HashMap<String, Object> item2 = new HashMap<String, Object>();
        item2.put(Constants.KEY_PRODUCT_ICON_URL,
                getResources().getDrawable(R.drawable.icon_kingsoft));
        item2.put(Constants.KEY_PRODUCT_NAME, "金山手机卫士");
        item2.put(Constants.KEY_PRODUCT_INFO, "防骚扰、防病毒、隐私保护、查健康。");
        items.add(item2);
        HashMap<String, Object> item3 = new HashMap<String, Object>();
        item3.put(Constants.KEY_PRODUCT_ICON_URL,
                getResources().getDrawable(R.drawable.icon_qq));
        item3.put(Constants.KEY_PRODUCT_NAME, "QQ手机管家");
        item3.put(Constants.KEY_PRODUCT_INFO, "独具卡巴双核查杀引擎，专业保护手机安全。");
        items.add(item3);
        
        mAdapter = new AppListAdapter(getApplicationContext(), 
                items,
                R.layout.list_item_safe_product, 
                new String[] { 
                    Constants.KEY_PRODUCT_ICON_URL,
                    Constants.KEY_PRODUCT_NAME, 
                    Constants.KEY_PRODUCT_INFO }, 
                new int[] { 
                    R.id.app_icon, 
                    R.id.app_name,
                    R.id.app_description });
        
        mList.setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
        Intent intent = new Intent(getApplicationContext(), PreloadActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if(pos == 1) {
            intent.putExtra(Constants.EXTRA_PRODUCT_ID, String.valueOf(45040));
        } else if(pos == 2) {
            intent.putExtra(Constants.EXTRA_PRODUCT_ID, String.valueOf(99207));
        } else if(pos == 3) {
            intent.putExtra(Constants.EXTRA_PRODUCT_ID, String.valueOf(21363));
        }
        intent.putExtra(Constants.EXTRA_SORT_TYPE, Constants.SOURCE_TYPE_GFAN);
        startActivity(intent);
    }
    
    private View createHeaderView() {
        TextView tv = (TextView) LayoutInflater.from(getApplicationContext()).inflate(
                R.layout.listview_header_view, mList, false);
        tv.setText("机锋市场的应用经过以下厂商检测，保证安全、无毒，请放心下载。");
        return tv;
    }
    
}
