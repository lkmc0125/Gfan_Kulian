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
package com.xiaohong.kulian.adapter;

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
import com.xiaohong.kulian.adapter.TabAppListAdapter.LazyloadListener;
import com.xiaohong.kulian.bean.AppBean;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.download.DownloadManager;
import com.xiaohong.kulian.common.download.DownloadManager.Request;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.vo.DownloadInfo;
import com.xiaohong.kulian.common.vo.DownloadItem;
import com.xiaohong.kulian.common.vo.UpgradeInfo;
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

    public static int APP_STATUS_DOWNLOADED = 0;
    public static int APP_STATUS_INSTALLED = 1;
    
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
     * Lazyload linstener If you want use the lazyload function, must implements
     * this interface
     */
    public interface LazyloadListener {

        /**
         * You should implements this method to justify whether should do
         * lazyload
         * 
         * @return
         */
        boolean isEnd();

        /**
         * Do something that process lazyload
         */
        void lazyload();

        /**
         * Indicate whether the loading process is over
         * 
         * @return
         */
        boolean isLoadOver();
        
        void loadMore();
    }

    
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

    public void reset() {
        mDataSource = null;
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
        viewHolder.mGoldView.setText("+"+data.getGiveCoin());
        mImageLoader.displayImage(data.getAppLogo(), viewHolder.mAppIconView, Utils.sDisplayImageOptions);
        if(data.isIsInstalled()) {
            viewHolder.mActionView.setText(R.string.app_item_action_open);
        } else if(data.isDownloaded()) {
            viewHolder.mActionView.setText(R.string.app_item_action_install);
        } else {
            viewHolder.mActionView.setText(R.string.app_item_action_view);
        }
    }

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
   
   /**
    * Update app's status to downladed or installed
    * @param packageName
    * @param status
    */
   public void updateAppStatus(String packageName, int status) {
       mDataSource.get(0);
       for(AppBean bean : mDataSource) {
           String beanPackageName = bean.getPackageName();
           if(beanPackageName != null && beanPackageName.equals(packageName)) {
               if(status == APP_STATUS_DOWNLOADED) {
                   bean.setDownloaded(true);
                   notifyDataSetChanged();
               }else if(status == APP_STATUS_INSTALLED) {
                   bean.setIsInstalled(true);
                   notifyDataSetChanged();
               }
               break;
           }
       }
       
   }
}