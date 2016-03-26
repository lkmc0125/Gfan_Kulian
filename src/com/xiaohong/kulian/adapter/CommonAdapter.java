package com.xiaohong.kulian.adapter;

import com.xiaohong.kulian.common.widget.AppListAdapter.LazyloadListener;

import android.widget.BaseAdapter;

public abstract class CommonAdapter extends BaseAdapter {

    public CommonAdapter() {
        // TODO Auto-generated constructor stub
    }

    public abstract void setLazyloadListener(LazyloadListener listener);
}
