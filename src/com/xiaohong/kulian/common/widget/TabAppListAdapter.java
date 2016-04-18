/*
 * Copyright (C) 2016 Shanghai Xiaohong.Inc
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
package com.xiaohong.kulian.common.widget;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.Session;
import com.xiaohong.kulian.adapter.CommonAdapter;
import com.xiaohong.kulian.bean.AppBean;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.download.DownloadManager;
import com.xiaohong.kulian.common.download.DownloadManager.Request;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.vo.DownloadInfo;
import com.xiaohong.kulian.common.vo.DownloadItem;
import com.xiaohong.kulian.common.vo.UpgradeInfo;
import com.xiaohong.kulian.common.widget.AppListAdapter.LazyloadListener;
import com.xiaohong.kulian.ui.RegisterActivity;


/**
 * GfanClient ListView associating adapter<br>
 * It has lazyload feature, which load data on-demand.
 * 
 * @author andrew.wang
 * 
 */
public class TabAppListAdapter extends CommonAdapter implements Observer,
        ApiRequestListener {

    private ArrayList<AppBean> mDataSource;
    private LazyloadListener mLazyloadListener;

    private int mResource;
    private boolean mIsLazyLoad;
    private LayoutInflater mInflater;
    private Context mContext;
    private HashMap<String, DownloadInfo> mDownloadingTask;
    private ArrayList<String> mInstalledList;
    private DownloadManager mDownloadManager;
    private HashMap<String, AppBean> mDownloadExtraInfo;
    private HashMap<String, String> mIconCache;
    private HashMap<String, HashMap<String, Object>> mCheckedList;
    private HashMap<String, UpgradeInfo> mUpdateList;
    private String mPageType = Constants.GROUP_14;
    
    /**
     * A ImageLoader instance to load image from cache or network
     */
    private ImageLoader mImageLoader = ImageLoader.getInstance();  

    /**
     * Application list adapter<br>
     * 如果不希望这个子View显示，设置Key对应的Value为Null即可
     * 
     * @param context
     *            application context
     * @param data
     *            the datasource behind the listview
     * @param resource
     *            list item view layout resource
     * @param from
     *            the keys array of data source which you want to bind to the
     *            view
     * @param to
     *            array of according view id
     * @param hasGroup
     *            whether has place holder
     */
    public TabAppListAdapter(Context context, ArrayList<AppBean> data,
            int resource) {
        if (data == null) {
            mDataSource = new ArrayList<AppBean>();
        } else {
            mDataSource = data;
        }
        mContext = context;
        mResource = resource;
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCheckedList = new HashMap<String, HashMap<String, Object>>();
        mIconCache = new HashMap<String, String>();
    }

    /**
     * @return the mCheckedList
     */
    public HashMap<String, HashMap<String, Object>> getCheckedList() {
        return mCheckedList;
    }

    /**
     * 设置是否包含分隔项
     * 
     * @param flag
     *            默认是false, 不包含分隔符
     */
    public void setContainsPlaceHolder(boolean flag) {
    }

    /**
     * 设置分隔项的资源ID
     */
    public void setPlaceHolderResource(int id) {
    }

    /**
     * 用于统计Lable
     */
    public void setmPageType(String mPageType) {
        this.mPageType = mPageType;
    }

    /**
     * 产品列表，需要刷新产品状态
     */
    public void setProductList() {
        Session session = Session.get(mContext);
        session.addObserver(this);
        mDownloadManager = session.getDownloadManager();
        mInstalledList = session.getInstalledApps();
        mDownloadingTask = session.getDownloadingList();
        mUpdateList = session.getUpdateList();
        mDownloadExtraInfo = new HashMap<String, AppBean>();
    }

    /*
     * How many items are in the data set represented by this Adapter.
     */
    @Override
    public int getCount() {
        if (mDataSource == null) {
            return 0;
        }
        return mDataSource.size();
    }

    @Override
    public Object getItem(int position) {

        if (mDataSource != null && position < getCount()) {
            return mDataSource.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEmpty() {
        if (mDataSource == null || mDataSource.size() == 0) {
            return true;
        }
        return super.isEmpty();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    /*
     * Clear all the data
     */
    public void clearData() {
        if (mDataSource != null) {
            mDataSource.clear();
            notifyDataSetChanged();
        }
    }

    /**
     * Lazyload web data
     * 
     * @param newData
     */
    public void addData(ArrayList<AppBean> newData) {
        if (newData != null && newData.size() > 0) {
            mDataSource.addAll(getCount(), newData);
            notifyDataSetChanged();
        }
    }

    public void removeData(ArrayList<AppBean> oldData) {
        if (mDataSource != null) {
            mDataSource.remove(oldData);
            notifyDataSetChanged();
        }
    }

    public void removeData(int position) {
        if (mDataSource != null) {
            mDataSource.remove(position);
            notifyDataSetChanged();
        }
    }

    public void insertData(AppBean newData) {
        if (newData != null) {
            mDataSource.add(0, newData);
            notifyDataSetChanged();
        }
    }

    public void setLazyloadListener(LazyloadListener listener) {
        mIsLazyLoad = true;
        mLazyloadListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

//        Log.d("free", "getView size = " + mDataSource.size());
        // last 4 item trigger the lazyload event
        if (mIsLazyLoad && !mLazyloadListener.isEnd()
                && (position == getCount() - 4)) {
            // fix the multi-load situation
            synchronized (this) {
                if (mLazyloadListener.isLoadOver()) {
                    mLazyloadListener.loadMore();
                    Utils.trackEvent(mContext, mPageType,
                            Constants.PRODUCT_LAZY_LOAD);
                }
            }
        }

        // reach here when list is not at the end
        assert (position < getCount());
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(mResource, null);
            viewHolder = new ViewHolder();
            convertView = newView(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        bindView(position, viewHolder);
        showAllViews(viewHolder);
        /*if (mIsProductList && mDownloadingTask != null) {

            // 列表中存在下载列表的任务，更新状态
            AppBean item = mDataSource.get(position);
            String packageName = (String) item.getPackageName();
            if (mDownloadingTask.containsKey(packageName)) {
                DownloadInfo info = mDownloadingTask.get(packageName);
                // 下载过程中，刷新进度
                item.getStatusMap().put(Constants.KEY_PRODUCT_INFO, info.mProgress);
                item.getStatusMap().put(Constants.KEY_PRODUCT_DOWNLOAD, info.mProgressLevel);
            } else if (mInstalledList.contains(packageName)) {
                // 已经安装的应用
                if (mUpdateList.containsKey(packageName)) {
                    // 可以更新
                    item.getStatusMap().put(Constants.KEY_PRODUCT_DOWNLOAD,
                            Constants.STATUS_UPDATE);
                } else {
                    item.getStatusMap().put(Constants.KEY_PRODUCT_DOWNLOAD,
                            Constants.STATUS_INSTALLED);
                }
            } else {
                Object result = item.getStatusMap().get(Constants.KEY_PRODUCT_DOWNLOAD);
                if (result != null) {
                    int status = (Integer) result;
                    if (status == Constants.STATUS_PENDING) {
                        // 准备开始下载，无需处理
                    } else if (status != Constants.STATUS_DOWNLOADED) {
                        // 默认的状态是未安装
                        item.getStatusMap().put(Constants.KEY_PRODUCT_DOWNLOAD,
                                Constants.STATUS_NORMAL);
                    }
                }
            }
        }*/

        return convertView;
    }


    /*
     * bind the background data to the viewHolder
     */
    private void bindView(int position, ViewHolder viewHolder) {

        AppBean data = mDataSource.get(position);
        if (data == null) {
            return;
        }
        viewHolder.mAppNameView.setText(data.getAppName());
        viewHolder.mAppDescView.setText(data.getBriefSummary());
        viewHolder.mAppSizeView.setText(data.getAppSize());
        mImageLoader.displayImage(data.getAppLogo(), viewHolder.mAppIconView, Utils.sDisplayImageOptions);
        if(data.isIsInstalled()) {
            viewHolder.mActionView.setText(R.string.app_item_action_open);
        }
    }

    /*protected void setViewResource(View v, int position, int[] bitmaps) {
        if (v instanceof ImageView) {
            ImageView view = (ImageView) v;
            HashMap<String, Object> map = mDataSource.get(position);

            int flag = (Integer) map.get(String.valueOf(position));
            view.setImageResource(bitmaps[flag]);
        }
    }

    
     * Set the value for RatingBar
     
    private void setViewRating(RatingBar v, Object rating) {
        if (rating instanceof Integer) {
            float ratingLevel = ((Integer) rating) / (float) 10;
            v.setRating(ratingLevel);
        }
    }*/

    /*
     * Set text value for TextView
     */
    /*private void setViewText(int position, TextView v, Object text) {

        if (text instanceof byte[]) {

            v.setText(Utils.getUTF8String((byte[]) text));

        } else if (text instanceof CharSequence) {

            v.setText((CharSequence) text);

        } else if (text instanceof Integer) {

            // 应用状态指示器
            v.setTag(position);
            final int level = (Integer) text;
            Drawable indicatorDrawble = v.getCompoundDrawables()[1];
            indicatorDrawble.setLevel(level);
            if (Constants.STATUS_NORMAL == level) {
                // 未下载
                v.setText(mDataSource.get(position).getGiveCoin()+ "");

            } else if (Constants.STATUS_PENDING == level) {
                // 准备开始下载
                v.setText(mContext
                        .getString(R.string.download_status_downloading));

            } else if (Constants.STATUS_DOWNLOADED == level) {
                // 已经下载，未安装
                v.setText(mContext
                        .getString(R.string.download_status_downloaded));

            } else if (Constants.STATUS_INSTALLED == level) {
                // 已经安装
                v.setText(mContext
                        .getString(R.string.download_status_installed));

            } else if (Constants.STATUS_UPDATE == level) {

                // 有更新
                v.setText(mContext.getString(R.string.operation_update));

            } else {
                // 下载中
                v.setText((String) mDataSource.get(position).getPackageName());

            }
            // 为下载按钮绑定事件
            v.setOnClickListener(mDownloadListener);
        }
    }

    
     * Set drawable value for ImageView
     
    private void setViewImage(int position, ImageView v, Object obj) {

        Drawable oldDrawable = v.getDrawable();
        if (oldDrawable != null) {
            // clear the CALLBACK reference to prevent of OOM error
            oldDrawable.setCallback(null);
        }

        if (obj instanceof Drawable) {
            // here is one drawable object
            v.setImageDrawable((Drawable) obj);

        } else if (obj instanceof String) {
            // here is one remote object (URL)
            ImageUtils.download(mContext, (String) obj, v);
        } else if (obj instanceof Boolean) {

            if ((Boolean) obj) {
                v.setVisibility(View.VISIBLE);
            } else {
                v.setVisibility(View.INVISIBLE);
            }
        }
    }
*/
    private OnClickListener mDownloadListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            int position = (Integer) v.getTag();
            AppBean item = mDataSource.get(position);
            int status = (Integer) item.getStatusMap().get(Constants.KEY_PRODUCT_DOWNLOAD);
            int payType = (Integer) item.getStatusMap().get(Constants.KEY_PRODUCT_PAY_TYPE);
            if (Constants.STATUS_NORMAL == status
                    || Constants.STATUS_UPDATE == status) {

                if (Constants.PAY_TYPE_PAID == payType) {
                    if (Session.get(mContext).isLogin()) {
//                        Intent intent = new Intent(mContext,
//                                PreloadActivity.class);
//                        intent.putExtra(Constants.EXTRA_PRODUCT_ID,
//                                item.getAppId() + "");
//                        intent.putExtra(Constants.IS_BUY, true);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        mContext.startActivity(intent);
                    } else {
                        Intent loginIntent = new Intent(mContext,
                                RegisterActivity.class);
                        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(loginIntent);
                    }
                } else {

                    Utils.trackEvent(mContext, mPageType,
                            Constants.DIRECT_DOWNLOAD);

                    String pid = item.getAppId()+ "";
                    String iconUrl = item.getAppLogo();
                    String pkgName = item.getPackageName();
                    String apkUrl = item.getAppSource();
                    // 开始下载，避免用户多次点击
                    item.getStatusMap().put(Constants.KEY_PRODUCT_DOWNLOAD,
                            Constants.STATUS_PENDING);
                    mIconCache.put(pkgName, iconUrl);
                    DownloadItem info = new DownloadItem();
                    info.packageName = pkgName;
                    info.pId = pid;
                    info.url = apkUrl;
                    mDownloadExtraInfo.put(pid, item);
                    download(info);
                    notifyDataSetChanged();
                }

            } else if (Constants.STATUS_DOWNLOADED == status) {

                // 安装应用
                String packageName = item.getPackageName();
                String filePath = (String) item.getStatusMap().get(Constants.KEY_PRODUCT_INFO);
                DownloadInfo info = mDownloadingTask.get(packageName);
                if (info != null) {
                    Utils.installApk(mContext, new File(info.mFilePath));
                } else if (!TextUtils.isEmpty(filePath)) {
                    Utils.installApk(mContext, new File(filePath));
                }
            } else if (Constants.STATUS_INSTALLED == status) {

                // 已经安装，去产品详细页
//                String packageName = item.getPackageName();
//                Intent detailIntent = new Intent(mContext,
//                        PreloadActivity.class);
//                detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                detailIntent
//                        .putExtra(Constants.EXTRA_PACKAGE_NAME, packageName);
//                mContext.startActivity(detailIntent);
            }
        }
    };

    private void download(DownloadItem info) {
        AppBean downloadItem = mDownloadExtraInfo.get(info.pId);
        Request request = new Request(Uri.parse(info.url));
        request.setTitle(downloadItem.getAppName());
        request.setPackageName(info.packageName);
        request.setIconUrl(mIconCache.get(info.packageName));
        request.setSourceType(com.xiaohong.kulian.common.download.Constants.DOWNLOAD_FROM_MARKET);
        request.setMD5(info.fileMD5);
        mDownloadManager.enqueue(request);
        Utils.makeEventToast(mContext,
                mContext.getString(R.string.download_start), false);
    }

    @Override
    public void onSuccess(int method, Object obj) {
        switch (method) {

        default:
            break;
        }
    }

    @Override
    public void onError(int method, int statusCode) {
        Utils.makeEventToast(mContext,
                mContext.getString(R.string.alert_dialog_error), false);
    }

    /**
     * 下载状态更新，刷新列表状态
     */
    @SuppressWarnings("unchecked")
    @Override
    public void update(Observable arg0, Object arg1) {

        if (arg1 instanceof HashMap) {
            mDownloadingTask = (HashMap<String, DownloadInfo>) arg1;
            notifyDataSetChanged();
        } else if (arg1 instanceof Integer) {
            notifyDataSetChanged();
        }
    }
    
    /**
     * Init viewholder
     * @param viewHolder A holder to save the view from layout
     * @return
     */
    private View newView(ViewHolder viewHolder) {
        View view = mInflater.inflate(mResource, null);
        viewHolder.mAppIconView = (ImageView) view.findViewById(R.id.iv_logo);
        viewHolder.mAppNameView = (TextView) view.findViewById(R.id.tv_name);
        viewHolder.mAppDescView = (TextView) view.findViewById(R.id.tv_description);
        viewHolder.mAppSizeView = (TextView) view.findViewById(R.id.tv_size);
        viewHolder.mGoldView = (TextView) view.findViewById(R.id.tv_gold);
        viewHolder.mActionView = (TextView) view.findViewById(R.id.tv_action);
        viewHolder.mStatusView = (TextView) view.findViewById(R.id.tv_status);
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
   }
   
   private void showAllViews(ViewHolder viewHolder) {
       viewHolder.mAppIconView.setVisibility(View.VISIBLE);
       viewHolder.mAppNameView.setVisibility(View.VISIBLE);
       viewHolder.mAppDescView.setVisibility(View.VISIBLE);
       viewHolder.mAppSizeView.setVisibility(View.VISIBLE);
       viewHolder.mGoldView.setVisibility(View.VISIBLE);
       viewHolder.mActionView.setVisibility(View.VISIBLE); 
       viewHolder.mStatusView.setVisibility(View.GONE); 
   }

}