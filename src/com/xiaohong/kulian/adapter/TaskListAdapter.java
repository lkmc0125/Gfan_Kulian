
package com.xiaohong.kulian.adapter;

import java.util.ArrayList;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.adapter.TabAppListAdapter.LazyloadListener;
import com.xiaohong.kulian.bean.TaskBean;
import com.xiaohong.kulian.common.util.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class TaskListAdapter extends CommonAdapter {
    private static final String TAG = "TaskListAdapter";
    public static final int TYPE_NORMAL_TASK = 1;
    public static final int TYPE_GZH_TAK = 2;

    private Context mContext;
    private ArrayList<TaskBean> mData;
    /**
     * A ImageLoader instance to load image from cache or network
     */
    private ImageLoader mImageLoader = ImageLoader.getInstance();

    public TaskListAdapter(Context context) {
        mContext = context;
        mData = new ArrayList<TaskBean>();
    }

    public synchronized void setData(int type, ArrayList<TaskBean> data) {
        Log.d(TAG, "setData:" + data);
        if (type == TYPE_NORMAL_TASK) {
            mData.addAll(0, data);
        } else {
            mData.addAll(data);
        }
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
        if (convertView == null
                || mData.get(position).getType() != ((ViewHolder) convertView
                        .getTag()).type) {
            holder = new ViewHolder();
            convertView = newView(holder, mData.get(position).getType());
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
        private ImageView mAppIconView;//app 图标
        private TextView mAppDescView; // 任务描述
        private TextView mGoldView; //显示金币的view
        private TextView mActionView; //显示打开查看的view
        private TextView mStatusView;
        private TextView mAppTitleView;
        /**
         * 用于判断item是title还是一个task
         */
        private int type;
    }

    /**
     * 显示务已结束textview
     * 
     * @param viewHolder
     */
    private void showStatusViews(ViewHolder viewHolder) {
        // viewHolder.mAppIconView.setVisibility(View.VISIBLE);
        // viewHolder.mAppDescView.setVisibility(View.VISIBLE);
        viewHolder.mStatusView.setVisibility(View.VISIBLE);
    }

    @SuppressLint("NewApi")
    private void hideActionViews(ViewHolder viewHolder) {
        viewHolder.mGoldView.setVisibility(View.GONE);
        viewHolder.mActionView.setBackground(null);
        viewHolder.mActionView.setText("已结束");
    }
    
    @SuppressLint("NewApi")
    private void showActioViews(ViewHolder viewHolder) {
        viewHolder.mGoldView.setVisibility(View.VISIBLE);
//        Bitmap background = BitmapFactory.decodeResource(mContext.getResources(), 
//                R.drawable.task_action_view_border);
//        BitmapDrawable drawable = new BitmapDrawable(background);
//        viewHolder.mActionView.setBackground(drawable);
        viewHolder.mActionView.setText(R.string.app_item_action_view);
    }
    
    /**
     * 隐藏务已结束textview
     * 
     * @param viewHolder
     */
    private void hideStatusViews(ViewHolder viewHolder) {
        viewHolder.mStatusView.setVisibility(View.GONE);
        viewHolder.mAppTitleView.setVisibility(View.GONE);
    }

    /**
     * Init viewholder
     * 
     * @param viewHolder
     *            A holder to save the view from layout
     * @return
     */
    private View newView(ViewHolder viewHolder, int type) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        if (type == TaskBean.ITEM_TYPE_TASK) {
            View view = inflater.inflate(R.layout.task_list_item, null);
            viewHolder.mAppIconView = (ImageView) view
                    .findViewById(R.id.iv_logo);
            viewHolder.mAppDescView = (TextView) view
                    .findViewById(R.id.tv_description);
            viewHolder.mGoldView = (TextView) view.findViewById(R.id.tv_gold);
            viewHolder.mActionView = (TextView) view
                    .findViewById(R.id.tv_action);
            viewHolder.mStatusView = (TextView) view
                    .findViewById(R.id.tv_status);
            viewHolder.mAppTitleView = (TextView) view.findViewById(R.id.tv_name);
            viewHolder.type = TaskBean.ITEM_TYPE_TASK;
            view.setTag(viewHolder);
            return view;
        } else {
            View view = inflater.inflate(R.layout.task_list_item_title, null);
            viewHolder.mAppDescView = (TextView) view.findViewById(R.id.title);
            viewHolder.type = TaskBean.ITEM_TYPE_TITLE;
            view.setTag(viewHolder);
            return view;
        }
    }

    /**
     * 任务列表和app list共有一个layout
     * 任务列表没有title只有desc 也没有 statusview，所以要隐藏一部分view
     * @param position
     * @param holder
     */
    private void bindView(int position, ViewHolder holder) {
        int type = holder.type;
        TaskBean item = mData.get(position);
        if (type == TaskBean.ITEM_TYPE_TASK) {
            holder.mAppDescView.setText(item.getName());
            if (item.getLogo_url() != null) {
                mImageLoader.displayImage(item.getLogo_url(),
                        holder.mAppIconView, Utils.sDisplayImageOptions);
            } else {
                mImageLoader.displayImage("drawable://" + R.drawable.wechat,
                        holder.mAppIconView, Utils.sDisplayImageOptions);
            }
            holder.mGoldView.setText("+" + item.getCoin_num());
            hideStatusViews(holder);
            if(item.getRemain_tasknum() == 0) {
                hideActionViews(holder);
            }else {
                showActioViews(holder);
            }

        } else {
            holder.mAppDescView.setText(item.getTitle());
            
        }
    }

    public ArrayList<TaskBean> getData() {
        return mData;
    }

}
