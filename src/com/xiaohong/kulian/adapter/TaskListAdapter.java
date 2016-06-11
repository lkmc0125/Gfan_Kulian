
package com.xiaohong.kulian.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.adapter.TabAppListAdapter.LazyloadListener;
import com.xiaohong.kulian.bean.AppBean;
import com.xiaohong.kulian.bean.TaskBean;
import com.xiaohong.kulian.common.download.DownloadManager;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.vo.DownloadInfo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TaskListAdapter extends CommonAdapter {
    private static final String TAG = "TaskListAdapter";
    private Context mContext;
    private ArrayList<Object> mData;
    //用于标记列表中有多少个APP item
    private int mAppListSize = 0;
    /**
     * A ImageLoader instance to load image from cache or network
     */
    private ImageLoader mImageLoader = ImageLoader.getInstance();
    private final HashMap<String, DownloadInfo> mDownloadingTaskMap = new
            HashMap<String, DownloadInfo>();

    public TaskListAdapter(Context context) {
        mContext = context;
        mData = new ArrayList<Object>();
    }

    public synchronized void setTaskData(int type, ArrayList<Object> data) {
        Log.d(TAG, "setTaskData:" + data);
        if (type == TaskBean.ITEM_TYPE_WEB_TASK) {
            mData.addAll(0, data);
        } else {
            mData.addAll(data);
        }
    }
    
    /**
     * Add app list at the begining position
     * @param data
     */
    public synchronized void setAppData(ArrayList<Object> data) {
        Log.d(TAG, "setAppData:" + data);
        mData.addAll(data);
        mAppListSize = data.size();
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
        //Log.d(TAG, "getView pos = " + position);
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
        private ProgressBar mProgressBar;
        private TextView mAppSizeView; // 应用大小
        private int type;
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
        viewHolder.mActionView.setText(R.string.app_item_action_view);
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
            View view = inflater.inflate(R.layout.common_product_list_item, null);
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
            viewHolder.mProgressBar = (ProgressBar) view.findViewById(R.id.lvitem_pb_download);
            viewHolder.mAppSizeView = (TextView) view.findViewById(R.id.tv_size);
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
                holder.mStatusView.setVisibility(View.GONE);
                holder.mAppTitleView.setVisibility(View.GONE);
                holder.mProgressBar.setVisibility(View.GONE);
                holder.mAppSizeView.setVisibility(View.GONE);
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
            holder.mAppSizeView.setText(item.getAppSize());
            if (item.getAppLogo() != null) {
                mImageLoader.displayImage(item.getAppLogo(),
                        holder.mAppIconView, Utils.sDisplayImageOptions);
            } else {
                mImageLoader.displayImage("drawable://" + R.drawable.wechat,
                        holder.mAppIconView, Utils.sDisplayImageOptions);
            }
            holder.mGoldView.setText("+" + item.getGiveCoin());
            showActionViews(holder);
            holder.mStatusView.setVisibility(View.GONE);
            DownloadInfo downloadInfo = mDownloadingTaskMap.get(item.getPackageName());
            if(downloadInfo != null &&
                    DownloadManager.Impl.isStatusRunning(downloadInfo.mStatus)) {
                holder.mProgressBar.setVisibility(View.VISIBLE);
                holder.mProgressBar.setProgress(downloadInfo.mProgressNumber);
            }else {
                holder.mProgressBar.setVisibility(View.INVISIBLE);
            }
            
        }
    }

    public ArrayList<Object> getData() {
        return mData;
    }
    
    public void setDownloadingTaskMap(HashMap<String, DownloadInfo> map) {
        mDownloadingTaskMap.clear();
        mDownloadingTaskMap.putAll(map);
        Log.d(TAG, "setDownloadingTaskMap notifyDataSetChanged");
        notifyDataSetChanged();
    }

}
