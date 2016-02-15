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
package com.mappn.gfan.common.widget;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mappn.gfan.R;
import com.mappn.gfan.common.widget.AppListAdapter.LazyloadListener;

/**
 * Lazyload ListView Activity
 * 
 * @author andrew.wang
 * @date 2010-9-26
 * @since Version 0.4.0
 */
public abstract class LazyloadListActivity extends BaseActivity implements LazyloadListener,
        OnClickListener {

    // private final static String TAG = "LazyloadListActivity";

    // 每页加载项目数
    private final static int ITEMS_PER_PAGE = 20;

    private int mStartIndex = 0;
    private int mEndIndex = ITEMS_PER_PAGE - 1;

    // 异步任务处理结束，可以开始新的异步任务
    private boolean mIsLoadOver = true;

    // ListView Object
    protected ListView mList;

    // ListView footer view
    private FrameLayout mFooterView;
    private ProgressBar mFooterLoading;
    private TextView mFooterNoData;

    // item per page
    private int mItemsPerPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (doInitView(savedInstanceState)) {
            mItemsPerPage = this.getItemsPerPage();
            initListView();
        }
    }

    /**
     * 子类必须实现这个方法初始化Layout Layout中必须有一个id为@android:id/list的ListView
     * 
     */
    public abstract boolean doInitView(Bundle savedInstanceState);

    /**
     * 子类必须实现这个方法以实现延迟加载
     */
    public abstract void doLazyload();

    /**
     * 子类必须实现这个方法以初始化ListAdapter<br>
     * 1 初始化数据集 2 初始化HeaderView或者FooterView
     */
    public abstract AppListAdapter doInitListAdapter();

    /**
     * 子类如果需要添加HeaderView或者FooterView需要重写这个方法
     */
    protected void doInitHeaderViewOrFooterView() {
    }

    /**
     * 返回每页加载项目数，默认值为每页10条，如果不满足需求需要重写这个方法
     */
    protected int getItemsPerPage() {
        return ITEMS_PER_PAGE;
    }

    /**
     * 子类实现这个方法以通知总项目数
     */
    protected int getItemCount() {
        return 0;
    }

    @Override
    public boolean isEnd() {
        return mStartIndex >= getItemCount();
    }

    @Override
    public boolean isLoadOver() {
        return mIsLoadOver;
    }

    @Override
    public void lazyload() {

        if (!mIsLoadOver) {
            return;
        }

        mIsLoadOver = false;
        doLazyload();
    }

    /**
     * 子类必须通过此方法通知异步任务结果
     */
    public void setLoadResult(final boolean isLoadSuccess) {
        mIsLoadOver = true;
        if (isLoadSuccess) {
            // 加载成功
            mStartIndex = (mEndIndex + 1);
            mEndIndex += mItemsPerPage;
            mFooterLoading.setVisibility(View.VISIBLE);
            mFooterNoData.setVisibility(View.GONE);

            // 2011/2/16 fix bug : can't touch main UI in background thread when load request error
            // 没有更多数据时，移除FooterView
            if (isEnd()) {
                mList.removeFooterView(mFooterView);
            }

        } else {
            mFooterLoading.setVisibility(View.GONE);
            mFooterNoData.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 返回分页起始下标
     */
    public int getStartIndex() {
        return mStartIndex;
    }

    /**
     * 返回分页终止下标
     */
    public int getEndIndex() {
        return mEndIndex;
    }
    
    /**
     * 重置ListAdapter
     */
    public void reset() {
        mStartIndex = 0;
        mEndIndex = ITEMS_PER_PAGE - 1;
        AppListAdapter adapter = (AppListAdapter) ((HeaderViewListAdapter) mList.getAdapter())
                .getWrappedAdapter();
        adapter.clearData();
    }

    private void initListView() {
        AppListAdapter adapter = doInitListAdapter();
        adapter.setLazyloadListener(this);
        doInitHeaderViewOrFooterView();
        // add Loading FooterView if this lazy load activity
        // make the footer view can't be selected
        mList.addFooterView(createFooterView(), null, false);
        mList.setAdapter(adapter);
    }

    // Create the footer LAZYLOAD view
    private View createFooterView() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mFooterView = (FrameLayout) inflater.inflate(R.layout.loading, mList, false);
        mFooterView.setBackgroundResource(R.drawable.list_item_bg_selector);
        mFooterLoading = (ProgressBar) mFooterView.findViewById(R.id.progressbar);
        mFooterLoading.setIndeterminateDrawable(new LoadingDrawable(getApplicationContext()));
        mFooterLoading.setVisibility(View.VISIBLE);
        mFooterNoData = (TextView) mFooterView.findViewById(R.id.no_data);
        mFooterNoData.setOnClickListener(this);
        mFooterNoData.setVisibility(View.GONE);
        return mFooterView;
    }

    @Override
    public void onClick(View v) {
        mFooterLoading.setVisibility(View.VISIBLE);
        mFooterNoData.setVisibility(View.GONE);
        doLazyload();
    }
    
}
