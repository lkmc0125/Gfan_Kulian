
package com.xiaohong.kulian.adapter;

import java.util.ArrayList;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.bean.MessageBean;
import com.xiaohong.kulian.bean.TaskBean;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.widget.AppListAdapter.LazyloadListener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class MessageListAdapter extends CommonAdapter {
    private static final String TAG = "MessageListAdapter";

    private Context mContext;
    private ArrayList<MessageBean> mData;

    public MessageListAdapter(Context context) {
        mContext = context;
        mData = new ArrayList<MessageBean>();
    }

    public synchronized void setData(ArrayList<MessageBean> data) {
        Log.d(TAG, "setData:" + data);
        mData.addAll(data);
    }

    @Override
    public int getCount() {
        if (mData == null) {
            // Log.d(TAG, "getCount:" + 0);
            return 0;
        }
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        // Log.d(TAG, "getView pos = " + position);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = newView(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        bindView(position, holder);
        return convertView;
    }

    @Override
    public void setLazyloadListener(LazyloadListener listener) {
        // TODO Auto-generated method stub

    }

    private class ViewHolder {
        private TextView mMessageView; // content
        private TextView mActionView;
    }

    @SuppressLint("NewApi")
    private void hideActionViews(ViewHolder viewHolder) {
        viewHolder.mActionView.setVisibility(View.GONE);
    }

    @SuppressLint("NewApi")
    private void showActioViews(ViewHolder viewHolder) {
        viewHolder.mActionView.setVisibility(View.VISIBLE);
    }

    private View newView(ViewHolder viewHolder) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.message_list_item, null);
        viewHolder.mActionView = (TextView) view
                .findViewById(R.id.tv_action);
        viewHolder.mMessageView = (TextView) view.findViewById(R.id.tv_name);
        view.setTag(viewHolder);
        return view;
    }

    private void bindView(int position, ViewHolder holder) {
        MessageBean item = mData.get(position);
        holder.mMessageView.setText(item.getMessageText());
        if(item.getClickUrl() == null) {
            hideActionViews(holder);
        }else {
            showActioViews(holder);
        }
    }

    public ArrayList<MessageBean> getData() {
        return mData;
    }
}
