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

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.mappn.gfan.Constants;
import com.mappn.gfan.R;
import com.mappn.gfan.common.util.ImageUtils;

/**
 * 首页顶部推荐列表Adapter
 * 
 * @author Andrew
 * @date 2011-5-24
 * 
 */
public class TopRecommendAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<HashMap<String, Object>> mDataSource;

    public TopRecommendAdapter(Context context, ArrayList<HashMap<String, Object>> data) {
        mContext = context;
        mDataSource = data;
    }

    @Override
    public int getCount() {
        if (mDataSource == null || mDataSource.size() == 0) {
            return 0;
        }
        return 1000;
    }

    public void setData(ArrayList<HashMap<String, Object>> data) {
        if (mDataSource == null) {
            mDataSource = new ArrayList<HashMap<String, Object>>();
        }
        mDataSource.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public Object getItem(int position) {
        int length = mDataSource.size();
        position = position % length;
        if (position < 0)
            position = position + length;
        return mDataSource.get(position);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final int length = mDataSource.size();
        position = position % length;
        if (position < 0)
            position = position + length;

        ImageView v = null;
        if (convertView == null) {
            v = (ImageView)LayoutInflater.from(mContext).inflate(R.layout.home_galley_item, parent, false);
        } else {
            v = (ImageView) convertView;
        }
        Drawable old = v.getDrawable();
        if (old != null) {
            old.setCallback(null);
        }
        ImageUtils.downloadHomeTopDrawable(mContext,
                (String) mDataSource.get(position).get(Constants.KEY_RECOMMEND_ICON), v);
        v.setTag(position);
        return v;
    }
}
