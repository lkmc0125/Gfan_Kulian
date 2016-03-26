package com.xiaohong.kulian.adapter;

import java.util.ArrayList;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.bean.TaskListBean;
import com.xiaohong.kulian.common.widget.AppListAdapter.LazyloadListener;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class TaskListAdapter extends CommonAdapter {
    private static final String TAG = "TaskListAdapter";
    private Context mContext;
    private TaskListBean mData;

    public TaskListAdapter(Context context) {
        mContext = context;
        mData = new TaskListBean();
    }
    
    public void setData(TaskListBean data) {
        Log.d(TAG, "setData:" + data);
        mData = data;
    }

    @Override
    public int getCount() {
        if(mData == null) {
            Log.d(TAG, "getCount:" + 0);
            return 0;
        }
        ArrayList list = mData.getTasklist();
        if(list == null) {
            Log.d(TAG, "getCount:" + 0);
            return 0;
        }else {
            Log.d(TAG, "getCount:" + list.size());
            return list.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return  mData.getTasklist().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        Log.d(TAG, "getView pos = " + position);
        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.common_product_list_item, null);
            holder = new ViewHolder();
            holder.mAppIconView = (ImageView) convertView.findViewById(R.id.iv_logo);
            holder.mAppDesc = (TextView) convertView.findViewById(R.id.tv_name);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.mAppDesc.setText(mData.getTasklist().get(position).getName());
        holder.mAppDesc.setVisibility(View.VISIBLE);
        return convertView;
    }

    @Override
    public void setLazyloadListener(LazyloadListener listener) {
        // TODO Auto-generated method stub
        
    }
    
    private class ViewHolder {
        private ImageView mAppIconView;
        private TextView mAppDesc;
        
        
    }

}
