
package com.xiaohong.kulian.adapter;

import java.util.ArrayList;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.adapter.TabAppListAdapter.LazyloadListener;
import com.xiaohong.kulian.bean.AppBean;
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
    private Context mContext;
    private ArrayList<Object> mData;
    /**
     * A ImageLoader instance to load image from cache or network
     */
    private ImageLoader mImageLoader = ImageLoader.getInstance();

    public TaskListAdapter(Context context) {
        mContext = context;
        mData = new ArrayList<Object>();
    }

    public synchronized void setData(int type, ArrayList<Object> data) {
        Log.d(TAG, "setData:" + data);
        if (type == TaskBean.ITEM_TYPE_APP_TASK) {
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
        Object item = mData.get(position);
        if (convertView == null
                || !(item instanceof TaskBean && ((ViewHolder) convertView.getTag()).type == TaskBean.ITEM_TYPE_WEB_TASK)
                || !(item instanceof AppBean && ((ViewHolder) convertView.getTag()).type == TaskBean.ITEM_TYPE_APP_TASK)
                ) {
            holder = new ViewHolder();

          convertView = newView(holder, item instanceof TaskBean ? TaskBean.ITEM_TYPE_WEB_TASK : TaskBean.ITEM_TYPE_APP_TASK);

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
    private void showActionViews(ViewHolder viewHolder) {
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
//        if (type != TaskBean.ITEM_TYPE_APP_TASK) {
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
            viewHolder.type = TaskBean.ITEM_TYPE_WEB_TASK;
            view.setTag(viewHolder);
            return view;
//        }
//        return null;
    }

    /**
     * 任务列表和app list共有一个layout
     * 任务列表没有title只有desc 也没有 statusview，所以要隐藏一部分view
     * @param position
     * @param holder
     */
    private void bindView(int position, ViewHolder holder) {
        int type = holder.type;
        Object obj = mData.get(position);
        
        if (obj instanceof TaskBean) {
            TaskBean item = (TaskBean)obj;
            if (type == TaskBean.ITEM_TYPE_WEB_TASK) {
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
                if (item.getRemain_tasknum() == 0) {
                    hideActionViews(holder);
                } else {
                    showActionViews(holder);
                }
            }   
        } else if (obj instanceof AppBean) {
            AppBean item = (AppBean)obj;
            holder.mAppTitleView.setText(item.getAppName());
            holder.mAppDescView.setText(item.getBriefSummary());
            if (item.getAppLogo() != null) {
                mImageLoader.displayImage(item.getAppLogo(),
                        holder.mAppIconView, Utils.sDisplayImageOptions);
            } else {
                mImageLoader.displayImage("drawable://" + R.drawable.wechat,
                        holder.mAppIconView, Utils.sDisplayImageOptions);
            }
            holder.mGoldView.setText("+" + item.getGiveCoin());
            showActionViews(holder);
        }
    }

    public ArrayList<Object> getData() {
        return mData;
    }

}
