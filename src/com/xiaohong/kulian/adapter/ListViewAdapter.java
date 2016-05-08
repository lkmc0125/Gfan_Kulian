package com.xiaohong.kulian.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import aga.fdf.grd.os.df.DiyAppNotify;
import aga.fdf.grd.os.df.AdExtraTaskStatus;
import aga.fdf.grd.os.df.AdTaskStatus;
import aga.fdf.grd.os.df.AppExtraTaskObject;
import aga.fdf.grd.os.df.AppExtraTaskObjectList;
import aga.fdf.grd.os.df.AppSummaryObject;
import aga.fdf.grd.os.df.DiyOfferWallManager;

import java.util.ArrayList;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.vo.CustomObject;

/**
 * 广告列表页Adapter
 *
 * @author youmi
 * @date 2015-4-24 上午9:12:48
 */
public class ListViewAdapter extends BaseAdapter implements DiyAppNotify {
    private static final String TAG = "ListViewAdapter";

    private Context mContext;

    private ArrayList<CustomObject> mCustomObjectArrayList;

    private SparseArray<ViewHolder> mViewHolderList = new SparseArray<ListViewAdapter.ViewHolder>();
    private LayoutInflater mInflater;
    /**
     * A ImageLoader instance to load image from cache or network
     */
    private ImageLoader mImageLoader = ImageLoader.getInstance();

    public ListViewAdapter(Context context,
            ArrayList<CustomObject> mCustomObjectArrayList) {
        this.mContext = context;
        this.mCustomObjectArrayList = mCustomObjectArrayList;
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addData(ArrayList<CustomObject> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        if (mCustomObjectArrayList == null) {
            mCustomObjectArrayList = new ArrayList<CustomObject>();
        }
        mCustomObjectArrayList.addAll(list);
    }

    public ArrayList<CustomObject> getData() {
        return mCustomObjectArrayList;
    }

    public void reset() {
        mCustomObjectArrayList = null;
    }

    @Override
    public int getCount() {
        return mCustomObjectArrayList == null ? 0 : mCustomObjectArrayList
                .size();
    }

    @Override
    public CustomObject getItem(int position) {
        if (mCustomObjectArrayList == null || mCustomObjectArrayList.isEmpty()) {
            return null;
        }
        return mCustomObjectArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = newView(holder);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        bindView(position, holder);
        showAllViews(holder);

        return convertView;

    }

    /*private static class ViewHolder {

        int id;

        ImageView app_icon;

        TextView app_name;

        TextView app_adSlogan;

        TextView app_adpoints;

        TextView app_status;

        ProgressBar app_download_progress;

        Button app_download_btn;
    }*/
    
    /*
     * bind the background data to the viewHolder
     */
    private void bindView(int position, ViewHolder holder) {

        CustomObject item = getItem(position);
        if (item == null) {
            Log.e(TAG, "item is null");
            return;
        }
        final AppSummaryObject appSummaryObject = item.getAppSummaryObject();
        if (appSummaryObject == null) {
            Log.e(TAG, "appSummaryObject is null");
            return;
        }
        holder.mId = appSummaryObject.getAdId();
        mViewHolderList.put(appSummaryObject.getAdId(), holder);

        // 设置广告图标
        if (item.getAppicon() == null) {
            ImageLoader.getInstance().displayImage(item.getAppSummaryObject().getIconUrl(),
                    holder.mAppIconView, Utils.sDisplayImageOptions);
            mImageLoader.displayImage(appSummaryObject.getIconUrl(), holder.mAppIconView, Utils.sDisplayImageOptions);
            
            //holder.app_icon.setVisibility(View.INVISIBLE);
        } else {
            holder.mAppIconView.setImageBitmap(item.getAppicon());
            holder.mAppIconView.setVisibility(View.VISIBLE);
        }

        // 设置广告名字
        holder.mAppNameView.setText(appSummaryObject.getAppName());

        // 设置广告语
        holder.mAppDescView.setText(appSummaryObject.getAdSlogan());
        
        //设置app大小
        holder.mAppSizeView.setText(appSummaryObject.getAppSize());
        if(appSummaryObject.getAppSize() == null || appSummaryObject.getAppSize().equals("")) {
            Log.d(TAG, "######################");
            Log.d(TAG, "ad form:" + appSummaryObject.getAdForm());
            Log.d(TAG, "ad name:" + appSummaryObject.getAppName());
            Log.d(TAG, "ad url:" + appSummaryObject.getUrl());
            Log.d(TAG, "######################");
        }

        String action_type = "";

        // 设置按钮是"打开"还是"下载安装"
        final boolean isPackageExist = Utils.isApkInstalled(mContext,
                appSummaryObject.getPackageName());
        holder.mActionView.setText(isPackageExist ? R.string.app_item_action_open : R.string.app_item_action_view);

        // 设置广告的状态、广告语
        switch (appSummaryObject.getAdTaskStatus()) {

        // 未完成
            case AdTaskStatus.NOT_COMPLETE :
                //now we do not have status view
               /* holder.app_status.setText("未完成");
                holder.app_status.setTextColor(mContext.getResources()
                        .getColor(R.color.black));*/
                holder.mStatusView.setVisibility(View.VISIBLE);
                holder.mStatusView.setText("未完成");
                holder.mStatusView.setTextColor(mContext.getResources()
                        .getColor(R.color.black));

                // 这里将演示将正常任务的积分和追加任务的积分加起来，然后展示给用户，开发者可以参考这里使用
                String textformat = "<html><body>"
                        + action_type
                        + "+<b><font color=\"#FF9F05\">"
                        + getTotalPoints(getItem(position)
                                .getAppSummaryObject())
                        + "</b>积分</body></html>";
                holder.mGoldView.setText("+" + getTotalPoints(getItem(position)
                        .getAppSummaryObject()));
                holder.mGoldView.setVisibility(View.VISIBLE);

                break;

            // 已完成
            case AdTaskStatus.ALREADY_COMPLETE :
                /*holder.app_status.setText("已完成");
                holder.app_status.setTextColor(mContext.getResources()
                        .getColor(R.color.green_color));*/
                holder.mStatusView.setVisibility(View.VISIBLE);
                holder.mStatusView.setText("已完成");
                holder.mStatusView.setTextColor(mContext.getResources()
                        .getColor(R.color.green_color));

                holder.mGoldView.setVisibility(View.GONE);

                break;

            // 有追加任务
            case AdTaskStatus.HAS_EXTRA_TASK :
                // 如果是同一个广告需要展示多次的话，就标识这个item是专门请求追加任务的item，需要特殊处理
                if (getItem(position).isShowMultSameAd()) {
                    AppExtraTaskObjectList extraTaskList = getItem(position)
                            .getAppSummaryObject().getExtraTaskList();
                    if (extraTaskList != null && extraTaskList.size() > 0) {
                        AppExtraTaskObject extraTaskObject = extraTaskList
                                .get(getItem(position).getShowExtraTaskIndex());
                        if (extraTaskObject.getStatus() == AdExtraTaskStatus.NOT_START) {
                            /*holder.app_status.setText("任务等待中");
                            holder.app_status.setTextColor(Color
                                    .parseColor("#BFBFBF"));*/
                            holder.mStatusView.setVisibility(View.VISIBLE);
                            holder.mStatusView.setText("任务等待中");
                            holder.mStatusView.setTextColor(Color
                                    .parseColor("#BFBFBF"));

                            holder.mGoldView.setText("完成+"
                                    + extraTaskObject.getPoints() + "积分");
                            holder.mGoldView.setTextColor(Color
                                    .parseColor("#C0C0C0"));

                        } else if (extraTaskObject.getStatus() == AdExtraTaskStatus.IN_PROGRESS) {
                           /* holder.app_status.setText("任务进行中");
                            holder.app_status.setTextColor(Color
                                    .parseColor("#8256D9"));*/
                            holder.mStatusView.setVisibility(View.VISIBLE);
                            holder.mStatusView.setText("任务进行中");
                            holder.mStatusView.setTextColor(Color
                                    .parseColor("#8256D9"));

                            holder.mGoldView.setTextColor(Color
                                    .parseColor("#399A00"));
                            String textformat1 = "<html><body>+<b><font color=\"#BE0028\">"
                                    + extraTaskObject.getPoints()
                                    + "</b>积分</body></html>";
                            holder.mGoldView.setText(Html
                                    .fromHtml(textformat1));
                            holder.mGoldView.setVisibility(View.VISIBLE);

                        }
                        holder.mAppDescView
                                .setText(extraTaskObject.getAdText());
                    }
                }

                // 到这里就标识是其他广告列表请求
                else {

                    AppExtraTaskObjectList extraTaskList = getItem(position)
                            .getAppSummaryObject().getExtraTaskList();

                    if (extraTaskList != null && extraTaskList.size() > 0) {

                        for (int i = 0; i < extraTaskList.size(); ++i) {
                            AppExtraTaskObject extraTaskObject = extraTaskList
                                    .get(i);
                            if (extraTaskObject.getStatus() == AdExtraTaskStatus.NOT_START
                                    || extraTaskObject.getStatus() == AdExtraTaskStatus.IN_PROGRESS) {

                                /*holder.app_status.setText("追加奖励");
                                holder.app_status
                                        .setTextColor(mContext.getResources()
                                                .getColor(R.color.black));*/
                                holder.mStatusView.setVisibility(View.VISIBLE);
                                holder.mStatusView.setText("追加奖励");
                                holder.mStatusView.setTextColor(mContext.getResources()
                                        .getColor(R.color.black));

                                String textformat1 = "<html><body>+<b><font color=\"#BE0028\">"
                                        + extraTaskObject.getPoints()
                                        + "</b>积分</body></html>";
                                holder.mGoldView.setText(Html
                                        .fromHtml(textformat1));
                                holder.mGoldView.setVisibility(View.VISIBLE);

                                holder.mAppDescView.setText(extraTaskObject
                                        .getAdText());
                                break;
                            }
                        }
                    }
                }

                break;
            default :
                break;
        }

        holder.mActionView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                DiyOfferWallManager.getInstance(mContext).openOrDownloadApp(
                        (Activity) mContext, appSummaryObject);
            }
        });
    }
    /**
     * Init viewholder
     * @param viewHolder A holder to save the view from layout
     * @return
     */
    private View newView(ViewHolder viewHolder) {
        View view = mInflater.inflate(R.layout.common_product_list_item, null);
        viewHolder.mAppIconView = (ImageView) view.findViewById(R.id.iv_logo);
        viewHolder.mAppNameView = (TextView) view.findViewById(R.id.tv_name);
        viewHolder.mAppDescView = (TextView) view.findViewById(R.id.tv_description);
        viewHolder.mAppSizeView = (TextView) view.findViewById(R.id.tv_size);
        viewHolder.mGoldView = (TextView) view.findViewById(R.id.tv_gold);
        viewHolder.mActionView = (TextView) view.findViewById(R.id.tv_action);
        viewHolder.mStatusView = (TextView) view.findViewById(R.id.tv_status);
        viewHolder.mDownloadProgressView = (ProgressBar) view.findViewById(R.id.lvitem_pb_download);
        view.setTag(viewHolder);
        return view;
    }

   private class ViewHolder {
       private ImageView mAppIconView;
       private TextView mAppNameView;
       private TextView mAppDescView;
       private TextView mAppSizeView;
       private TextView mGoldView;
       private TextView mActionView;
       private TextView mStatusView;
       private ProgressBar mDownloadProgressView;
       private int mId; // an id to mark an item
   }
   
   private void showAllViews(ViewHolder viewHolder) {
       viewHolder.mAppIconView.setVisibility(View.VISIBLE);
       viewHolder.mAppNameView.setVisibility(View.VISIBLE);
       viewHolder.mAppDescView.setVisibility(View.VISIBLE);
       viewHolder.mAppSizeView.setVisibility(View.VISIBLE);
       viewHolder.mGoldView.setVisibility(View.VISIBLE);
       viewHolder.mActionView.setVisibility(View.VISIBLE); 
       //viewHolder.mStatusView.setVisibility(View.GONE); 
       viewHolder.mDownloadProgressView.setVisibility(View.GONE);
   }

    /**
     * 如果任务未完成就获取指定广告的所有积分（正常完成的积分+可完成的追加任务积分）
     */
    private int getTotalPoints(AppSummaryObject appSummaryObject) {
        int totalpoints = appSummaryObject.getPoints();
        AppExtraTaskObjectList tempList = appSummaryObject.getExtraTaskList();
        if (tempList != null && tempList.size() > 0) {
            for (int i = 0; i < tempList.size(); ++i) {
                AppExtraTaskObject extraTaskObject = tempList.get(i);
                if (extraTaskObject.getStatus() == AdExtraTaskStatus.NOT_START
                        || extraTaskObject.getStatus() == AdExtraTaskStatus.IN_PROGRESS) {
                    totalpoints += extraTaskObject.getPoints();
                }
            }
        }
        return totalpoints;
    }

    @Override
    public void onDownloadStart(int id) {

    }

    @Override
    public void onDownloadProgressUpdate(int id, long contentLength,
            long completeLength, int percent, long speedBytesPerS) {
        try {
            ViewHolder viewHolder = mViewHolderList.get(id);
            if (viewHolder == null || viewHolder.mId != id) {
                return;
            }

            viewHolder.mDownloadProgressView.setProgress(percent);
            viewHolder.mDownloadProgressView.setVisibility(View.VISIBLE);
            viewHolder.mActionView.setEnabled(false);
            viewHolder.mActionView.setText("正在下载");

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDownloadSuccess(int id) {
        try {
            ViewHolder viewHolder = mViewHolderList.get(id);
            if (viewHolder == null || viewHolder.mId != id) {
                return;
            }
            /*viewHolder.app_download_progress.setProgress(0);
            viewHolder.app_download_progress.setVisibility(View.GONE);
            viewHolder.app_status.setText("下载成功,请安装!");*/

            viewHolder.mDownloadProgressView.setProgress(0);
            viewHolder.mDownloadProgressView.setVisibility(View.GONE);
            viewHolder.mActionView.setEnabled(true);
            viewHolder.mActionView.setText("安装");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDownloadFailed(int id) {
        try {
            ViewHolder viewHolder = mViewHolderList.get(id);
            if (viewHolder == null || viewHolder.mId != id) {
                return;
            }
            /*viewHolder.app_download_progress.setProgress(0);
            viewHolder.app_download_progress.setVisibility(View.GONE);
            viewHolder.app_status.setText("下载失败,请重试!");*/

            viewHolder.mDownloadProgressView.setProgress(0);
            viewHolder.mDownloadProgressView.setVisibility(View.GONE);
            viewHolder.mActionView.setEnabled(true);
            viewHolder.mActionView.setText("下载失败");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInstallSuccess(int id) {
        try {
            ViewHolder viewHolder = mViewHolderList.get(id);
            if (viewHolder == null || viewHolder.mId != id) {
                return;
            }
            /*viewHolder.app_download_progress.setProgress(0);
            viewHolder.app_download_progress.setVisibility(View.GONE);
            viewHolder.app_status.setText("安装成功!");*/

            viewHolder.mDownloadProgressView.setProgress(0);
            viewHolder.mDownloadProgressView.setVisibility(View.GONE);
            viewHolder.mActionView.setEnabled(true);
            viewHolder.mActionView.setText("打开");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
