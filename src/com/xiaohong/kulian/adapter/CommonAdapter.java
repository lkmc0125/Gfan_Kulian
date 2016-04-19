package com.xiaohong.kulian.adapter;

import com.xiaohong.kulian.adapter.TabAppListAdapter.LazyloadListener;

import android.widget.BaseAdapter;

public abstract class CommonAdapter extends BaseAdapter {

    public CommonAdapter() {
        // TODO Auto-generated constructor stub
    }

    public abstract void setLazyloadListener(LazyloadListener listener);
}
