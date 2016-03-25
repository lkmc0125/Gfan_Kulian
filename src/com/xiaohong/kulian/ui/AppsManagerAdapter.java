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
package com.xiaohong.kulian.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.Session;
import com.xiaohong.kulian.common.ApiAsyncTask;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.download.DownloadManager;
import com.xiaohong.kulian.common.download.DownloadManager.Request;
import com.xiaohong.kulian.common.util.DBUtils;
import com.xiaohong.kulian.common.util.ImageUtils;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.vo.DownloadInfo;
import com.xiaohong.kulian.common.vo.DownloadItem;
import com.xiaohong.kulian.common.vo.UpgradeInfo;

/**
 * GfanClient ListView associating adapter<br>
 * It has lazyload feature, which load data on-demand.
 * 
 * @author andrew.wang
 * 
 */
public class AppsManagerAdapter extends BaseAdapter implements Observer, ApiRequestListener {

    static final int REFRESH = 0;
    static final int UPDATE_ITEM = 1;
    static final int REMOVE_ITEM = 2;
    static final int CHECK_DOWNLOAD_TITLE = 3;
    static final int CHECK_UPDATE_TITLE = 4;
    
    static final String ITEM_DOWNLOAD_TITLE = "download_title";
    static final String ITEM_INSTALLED_TITLE = "installed_title";
    static final String ITEM_UPDATE_ALL = "update_all";
    
    /** 分隔项 */
    static final int VIEW_TYPE_TITLE = 0;
    /** 下载任务区 */
    static final int VIEW_TYPE_DOWNLOADING = 1;
    /** 下载完成项目 */
    static final int VIEW_TYPE_DOWNLOADED = 2;
    /** 更新全部项目 */
    static final int VIEW_TYPE_UPDATE_ALL = 3;
    /** 已安装项目 */
    static final int VIEW_TYPE_INSTALLED = 4;
    
    // weight group download area
    static final int WEIGHT_DOWNLOAD_TITLE = 0;
    static final int WEIGHT_DOWNLOAD_DOWNLOADING = 1;
    static final int WEIGHT_DOWNLOAD_PENDING = 2;
    static final int WEIGHT_DOWNLOAD_COMPLETE = 3;
    // weight group installed area
    static final int WEIGHT_NORMAL_TITLE = 5;
    static final int WEIGHT_NORMAL_UPDATE_ALL = 6;
    static final int WEIGHT_NORMAL_UPDATE = 7;
    static final int WEIGHT_NORMAL_INSTALLED = 8;
    
    static final String DOWNLOAD_GROUP = "download_group";
    static final String INSTALLED_GROUP = "installed_group";
    static final String UPDATE_ALL_ITEM = "update_all_item";
    
    // 列表后台数据
    private ListOrderedMap mDataSource;
    private LayoutInflater mInflater;
    private Context mContext;
    private Session mSession;
    /* 可更新列表 */
    private HashMap<String, UpgradeInfo> mUpdateList;
    /* 下载列表 */
    private  HashMap<String, DownloadInfo> mDownloadingList;
    /* 下载管理器 */
    private DownloadManager mDownloadManager;
    
    /**
     * Application list adapter
     * 
     * @param context
     *            application context
     * @param data
     *            the datasource behind the listview
     * @param resource
     *            list item view layout resource
     * @param from
     *            the keys array of data source which you want to bind to the view
     * @param to
     *            array of according view id
     * @param hasGroup
     *            whether has place holder
     */
    public AppsManagerAdapter(Context context, ListOrderedMap data) {
        if (data == null) {
            mDataSource = new ListOrderedMap();
        } else {
            mDataSource = data;
        }
        mContext = context;
        mSession = Session.get(context);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSession.addObserver(this);
        mDownloadManager = mSession.getDownloadManager();
        mUpdateList = mSession.getUpdateList();
        mDownloadingList = mSession.getDownloadingList();
        init();
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
            return mDataSource.getValue(position);
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

    @Override
    public int getItemViewType(int position) {
        AppItem item = mDataSource.getValue(position);
        int viewType = item == null ? VIEW_TYPE_INSTALLED : item.mViewType;
        return viewType;
    }

    /*
     * Return the view types of the list adapter
     */
    @Override
    public int getViewTypeCount() {
        return 5;
    }

    @Override
    public boolean isEnabled(int position) {
        boolean result = !isPlaceHolder(position);
        return result;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // reach here when list is not at the end
        View v;
        int viewType = mDataSource.getValue(position).mViewType;
        if (convertView == null) {
            v = newView(position, parent, viewType);
        } else {
            v = convertView;
        }
        bindView(position, v, viewType);
        return v;
    }
    
    /*
     * Main UI handler
     */
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            
            switch (msg.what) {
            case REFRESH :
                
                refresh();
                break;
                
            case UPDATE_ITEM:
                
                AppItem item = (AppItem) msg.obj;
                mDataSource.put(item.mKey, item);
                checkToRemoveDownloadHeader();
                break;
                
            case REMOVE_ITEM:
                
                AppItem delItem = (AppItem) msg.obj;
                // 其它项目，直接删除
                mDataSource.remove(delItem.mKey);
                checkToRemoveDownloadHeader();
                break;
                
            case CHECK_DOWNLOAD_TITLE:
                
                // 检查是否需要添加下载区Title
                int downloadCounter = (Integer) msg.obj;
                AppItem group = mDataSource.getValue(DOWNLOAD_GROUP);
                if(downloadCounter > 0 && group == null) {
                    group = new AppItem();
                    group.mTitle = mContext.getString(R.string.all_downloads);
                    group.mInfo = null;
                    group.mViewType = VIEW_TYPE_TITLE;
                    group.mWeight = WEIGHT_DOWNLOAD_TITLE;
                    group.mKey = DOWNLOAD_GROUP; 
                    mDataSource.put(DOWNLOAD_GROUP, group);
                    
                } else if(downloadCounter == 0 && group != null){
                    
                    mDataSource.remove(DOWNLOAD_GROUP);
                }
                break;
                
            case CHECK_UPDATE_TITLE:

                // 检查全部更新项目
                int updateCounter = (Integer) msg.obj;
                AppItem updateAllItem = mDataSource.getValue(UPDATE_ALL_ITEM);
                if (updateCounter > 1 && updateAllItem == null) {
                    updateAllItem = new AppItem();
                    updateAllItem.mInfo = mContext.getString(R.string.download_all_update);
                    updateAllItem.mViewType = VIEW_TYPE_UPDATE_ALL;
                    updateAllItem.mWeight = WEIGHT_NORMAL_UPDATE_ALL;
                    updateAllItem.mKey = UPDATE_ALL_ITEM;
                    mDataSource.put(UPDATE_ALL_ITEM, updateAllItem);
                    
                } else if (updateCounter <= 1 && updateAllItem != null) {
                    
                    mDataSource.remove(UPDATE_ALL_ITEM);
                }
                break;
                
            default:
                break;
            }
        }
    };

    /*
     * Create new view object and cache some views associated with it
     */
    private View newView(int position, ViewGroup parent, int viewType) {
        
        View v = null;
        View[] holder = null;
        
        if (viewType == VIEW_TYPE_TITLE) {
            
            v = mInflater.inflate(R.layout.activity_apps_manager_list_separator, parent, false);
            holder = new View[2];
            holder[0] = v.findViewById(R.id.title);
            holder[1] = v.findViewById(R.id.info);

        } else if (viewType == VIEW_TYPE_DOWNLOADING) {

            v = mInflater.inflate(R.layout.activity_apps_manager_downloading_item, parent, false);
            holder = new View[5];
            holder[0] = v.findViewById(R.id.iv_logo);
            holder[1] = v.findViewById(R.id.tv_name);
            holder[2] = v.findViewById(R.id.progressbar);
            holder[3] = v.findViewById(R.id.info);
            holder[4] = v.findViewById(R.id.tv_operation);

        } else if (viewType == VIEW_TYPE_DOWNLOADED) {

            v = mInflater.inflate(R.layout.activity_apps_manager_uninstalled_item, parent, false);
            holder = new View[3];
            holder[0] = v.findViewById(R.id.app_icon);
            holder[1] = v.findViewById(R.id.app_name);
            holder[2] = v.findViewById(R.id.info);

        } else if(viewType == VIEW_TYPE_UPDATE_ALL) {
          
            v = mInflater.inflate(R.layout.activity_apps_manager_update_all_item, parent, false);
            holder = new View[1];
            holder[0] = v.findViewById(R.id.info);
            
        } else {

            v = mInflater.inflate(R.layout.activity_apps_manager_installed_item, parent, false);
            holder = new View[5];
            holder[0] = v.findViewById(R.id.iv_logo);
            holder[1] = v.findViewById(R.id.tv_name);
            holder[2] = v.findViewById(R.id.tv_current_version);
            holder[3] = v.findViewById(R.id.tv_update_version);
            holder[4] = v.findViewById(R.id.tv_operation);
        }
        v.setTag(holder);
        return v;
    }
    
    /*
     * bind the background data to the view
     */
    private void bindView(int position, View view, int viewType) {

        final AppItem item = (AppItem) mDataSource.getValue(position);
        if (item == null) {
            return;
        }
        final View[] holder = (View[]) view.getTag();
        if (viewType == VIEW_TYPE_TITLE) {

            bindTitleView(holder, item);

        } else if (viewType == VIEW_TYPE_DOWNLOADING) {

            bindDownloadingView(position, holder, item);
            
        } else if (viewType == VIEW_TYPE_DOWNLOADED) {

            bindUninstalledView(holder, item);
            
        } else if (viewType == VIEW_TYPE_UPDATE_ALL) {

            bindUpdateAllView(holder, item);
            
        } else {
            
            bindInstalledView(position, holder, item);
        }
    }
    
    /*
     * Bind views for downloading item
     */
    private void bindDownloadingView(int position, View[] holder, AppItem item) {

        if (item.mIcon instanceof Drawable) {
            setImageView((ImageView) holder[0], (Drawable) item.mIcon);
        } else if (item.mIcon instanceof String) {
            setImageView((ImageView) holder[0], (String) item.mIcon);
        } 

        setTextView((TextView) holder[1], item.mAppName);

        setProgressBar((ProgressBar) holder[2], item.mProgress);

        setTextView((TextView) holder[3], item.mInfo);

        setTextView(position, (TextView) holder[4], item.mWeight);
    }
    
    /*
     * Bind views for uninstalled item
     */
    private void bindUninstalledView(View[] holder, AppItem item) {

        if (item.mIcon instanceof Drawable) {
            setImageView((ImageView) holder[0], (Drawable) item.mIcon);
        } else if (item.mIcon instanceof String) {
            setImageView((ImageView) holder[0], (String) item.mIcon);
        }
        
        setTextView((TextView) holder[1], item.mAppName);
        
        setTextView((TextView) holder[2], item.mInfo);
    }
    
    /*
     * Bind views for update all item
     */
    private void bindUpdateAllView(View[] holder, AppItem item) {
        
        setTextView((TextView) holder[0], item.mInfo);
    }
    
    /*
     * Bind views for splitter item
     */
    private void bindTitleView(View[] holder, AppItem item) {
        
        // left info
        setTextView((TextView) holder[0], item.mTitle);
        
        // right info
        setTextView((TextView) holder[1], item.mInfo);
    }
    
    /*
     * Bind views for installed item
     */
    private void bindInstalledView(int position, View[] holder, AppItem item) {

        // set app icon
        setImageView((ImageView) holder[0], (Drawable) item.mIcon);

        // set app name
        setTextView((TextView) holder[1], item.mAppName);

        // set current version
        setTextView((TextView) holder[2], item.mCurrentVersionString, item.mNewVersionString);
        
        // set new version
//        setTextView((TextView) holder[3], item.mNewVersionString);

        // set uninstall button
        setTextView(position, (TextView) holder[4], item.mWeight);
    }
    
    /*
     * Utility for TextView 
     */
    private void setTextView(TextView v, CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            v.setVisibility(View.GONE);
        } else {
            v.setText(text);
            v.setVisibility(View.VISIBLE);
        }
    }
    
    private void setTextView(TextView v, CharSequence currentVersion, CharSequence newVersion) {

        if (!TextUtils.isEmpty(newVersion)) {
            v.setText(newVersion);
            v.setTextColor(mContext.getResources().getColor(R.color.info_font_light_orange));
            v.setVisibility(View.VISIBLE);
            return;
        }

        if (!TextUtils.isEmpty(currentVersion)) {
            v.setText(currentVersion);
            v.setTextAppearance(mContext, R.style.app_text_style3);
            v.setVisibility(View.VISIBLE);
            return;
        }

        v.setVisibility(View.GONE);
    }
    
    /*
     * Bind CompundDrawable TextView
     */
    private void setTextView(int position, TextView v, int weight) {
        
        if(WEIGHT_NORMAL_INSTALLED == weight) {
            v.setText(R.string.operation_uninstall);
            v.setCompoundDrawablesWithIntrinsicBounds(null,
                    mContext.getResources().getDrawable(R.drawable.btn_uninstall), null, null);
            v.setVisibility(View.VISIBLE);
        } else if(WEIGHT_NORMAL_UPDATE == weight) {
            
            AppItem item = mDataSource.getValue(position);
            if (TextUtils.isEmpty(item.mFilePath)) {
                v.setText(R.string.operation_update);
                v.setCompoundDrawablesWithIntrinsicBounds(null,
                        mContext.getResources().getDrawable(R.drawable.down_btn_10), null, null);
                v.setVisibility(View.VISIBLE);
            } else {
                // 更新下载完成
                v.setText(R.string.download_status_downloaded);
                v.setCompoundDrawablesWithIntrinsicBounds(null,
                        mContext.getResources().getDrawable(R.drawable.down_btn_9), null, null);
                v.setVisibility(View.VISIBLE);
            }
        } else if(WEIGHT_DOWNLOAD_DOWNLOADING == weight){
            v.setText(R.string.cancel_downloads);
            v.setCompoundDrawablesWithIntrinsicBounds(null, 
                    mContext.getResources().getDrawable(R.drawable.btn_cancel), null, null);
            v.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.INVISIBLE);
        }
        v.setTag(position);
        v.setOnClickListener(mOperationListener);
    }
    
    /*
     * Utility for ImageView(get rid of old drawable)
     */
    private static void setImageView(ImageView v, Drawable drawable) {
        Drawable old = v.getDrawable();
        if (old != null) {
            old.setCallback(null);
        }
        v.setImageDrawable(drawable);
        v.setVisibility(View.VISIBLE);
    }
    
    /*
     * Utility for ImageView(get rid of old drawable)
     */
    private void setImageView(ImageView v, String url) {
        Drawable old = v.getDrawable();
        if (old != null) {
            old.setCallback(null);
        }
        ImageUtils.download(mContext, url, v);
        v.setVisibility(View.VISIBLE);
    }
    
    /*
     * Utility for ProgressBar
     */
    private static void setProgressBar(ProgressBar v, int progress) {
        v.setProgress(progress);
        v.setVisibility(View.VISIBLE);
    }
    
    /*
     * Operation Listener
     */
    private OnClickListener mOperationListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int position = (Integer) v.getTag();
            AppItem item = mDataSource.getValue(position);
            if (item.mWeight == WEIGHT_NORMAL_INSTALLED) {
                // 卸载应用
                Utils.uninstallApk(mContext, item.mPackageName);
                Utils.trackEvent(mContext, Constants.GROUP_7,
                        Constants.CLICK_UNINSTALL);
            } else if (item.mWeight == WEIGHT_NORMAL_UPDATE) {
                
                if (!TextUtils.isEmpty(item.mFilePath)) {
                    Utils.installApk(mContext, new File(item.mFilePath));
                } else {
                    // 更新应用
//                    MarketAPI.getDownloadUrl(mContext, AppsManagerAdapter.this, item.mProductId,
//                            Constants.SOURCE_TYPE_KULIAN);
                    
                    Utils.trackEvent(mContext, Constants.GROUP_7,
                            Constants.CLICK_UPDATE);

                    // 开始更新单个项目
                    item.mWeight = WEIGHT_NORMAL_INSTALLED;
                    item.mViewType = VIEW_TYPE_INSTALLED;
                    item.mKey = item.mPackageName;
                    sendMessage(UPDATE_ITEM, item);
                }

            } else if (item.mWeight == WEIGHT_DOWNLOAD_DOWNLOADING) {
                // 取消下载
                mDownloadManager.cancelDownload(item.mId);
            }
            mHandler.sendEmptyMessage(REFRESH);
        }
    };

    /*
     * Identify whether current item is only a place holder
     */
    private boolean isPlaceHolder(int position) {
        if (mDataSource == null) {
            return false;
        }

        if (position >= mDataSource.size()) {
            return false;
        }

        AppItem item = mDataSource.getValue(position);
        return item.mViewType == VIEW_TYPE_TITLE;
    }

    /**
     * 初始化我的应用列表<br> 
     * 1 -- 获取用户安装的所有应用<br>
     * 2 -- 获取可更新的应用<br>
     * 3 -- 获取用户下载的应用（下载队列中、正在下载、下载完成但没有安装）<br>
     */
    private void init() {

        Thread refreshThread = new Thread() {

            @Override
            public void run() {
                loadInstalledApps();
                refreshUpdateApps();
                initDownloadingApps();
            }
        };
        refreshThread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        refreshThread.start();
        refreshThread = null;
    }
    
    private void onChanged(final int signal) {
        
        Thread initThread = new Thread() {

            @Override
            public void run() {
                if(signal == Constants.INFO_UPDATE) {
                    refreshUpdateApps();
                } else if(signal == Constants.INFO_REFRESH) {
                    refreshDownloadingApps();
                }
                mHandler.sendEmptyMessage(REFRESH);
            }
        };
        initThread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        initThread.start();
        initThread = null;
    }
    
    /*
     * 刷新可更新的应用
     */
    private void refreshUpdateApps() {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                if (mUpdateList != null) {
                    Collection<UpgradeInfo> values = mUpdateList.values();
                    for (UpgradeInfo info : values) {
                        AppItem item = mDataSource.getValue(info.pkgName);
                        if (item != null) {
                            item.mNewVersion = info.versionName;
                            item.mNewVersionString = mContext.getString(R.string.new_version,
                                    info.versionName);
                            item.mWeight = WEIGHT_NORMAL_UPDATE;
                            item.mProductId = info.pid;
                            item.mIsUpdate = true;
                            sendMessage(UPDATE_ITEM, item);
                        }
                    }
                }
            }
        });
    }
    
    /*
     * 更新下载任务
     */
    private void initDownloadingApps() {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                refreshDownloadingApps();
                mHandler.sendEmptyMessage(REFRESH);
            }
        });
    }
    
    private void checkToRemoveDownloadHeader() {
        // 当第二个项目不为下载项目时，说明需要移除下载区Title
        if (mDataSource != null && mDataSource.size() > 1) {
            AppItem item = mDataSource.getValue(1);
            if (item.mWeight > WEIGHT_DOWNLOAD_COMPLETE) {
                AppItem group = mDataSource.getValue(DOWNLOAD_GROUP);
                if(group != null) {
                    sendMessage(REMOVE_ITEM, group);
                }
            }
        }
    }

    /*
     * 加载已经安装的应用
     */
    private void loadInstalledApps() {

        List<PackageInfo> apps = Utils.getInstalledApps(mContext);
        final PackageManager pm = mContext.getPackageManager();
        
        // 加入已安装应用区域的Title
        AppItem groupTitle = new AppItem();
        groupTitle.mTitle = mContext.getString(R.string.all_apps);
        groupTitle.mInfo = mContext.getString(R.string.app_counter, apps.size());
        groupTitle.mViewType = VIEW_TYPE_TITLE;
        groupTitle.mWeight = WEIGHT_NORMAL_TITLE;
        // 保存已经安装的应用数量以便更新
        groupTitle.mData = apps.size();
        groupTitle.mKey = INSTALLED_GROUP;
        sendMessage(UPDATE_ITEM, groupTitle);
        
        // 加载已安装的应用列表项
        for (PackageInfo info : apps) {
            AppItem application = new AppItem();
            
            Drawable icon = null;
            try {
                icon = info.applicationInfo.loadIcon(pm);
            } catch (OutOfMemoryError err) {
                Utils.W("OutOfMemoryError when loading icon drawables from installed apps.");
            }
            application.mIcon = icon;
            application.mAppName = (String)info.applicationInfo.loadLabel(pm);
            String currentVersion = TextUtils.isEmpty(info.versionName) ? mContext
                    .getString(R.string.warning_unknown_version) : info.versionName;
            application.mCurrentVersion = currentVersion;
            application.mCurrentVersionString = mContext.getString(R.string.current_version, currentVersion);
            application.mPackageName = info.packageName;
            application.mViewType = VIEW_TYPE_INSTALLED;
            application.mKey = info.packageName;
            application.mWeight = WEIGHT_NORMAL_INSTALLED;
            sendMessage(UPDATE_ITEM, application);
        }
    }
    
    /*
     * 获取单个应用的信息
     */
    private static AppItem getApkInfo(Context context, String packageName) {
        final PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            AppItem application = new AppItem();
            Drawable icon = null;
            try {
                icon = info.applicationInfo.loadIcon(pm);
            } catch (OutOfMemoryError err) {
                Utils.W("OutOfMemoryError when loading icon drawables from installed apps.");
            }
            application.mIcon = icon;
            application.mAppName = (String) info.applicationInfo.loadLabel(pm);
            application.mCurrentVersion = info.versionName;
            application.mCurrentVersionString = context.getString(R.string.current_version,
                    info.versionName);
            application.mPackageName = info.packageName;
            application.mViewType = VIEW_TYPE_INSTALLED;
            application.mKey = info.packageName;
            application.mWeight = WEIGHT_NORMAL_INSTALLED;
            return application;
        } catch (NameNotFoundException e) {
            Utils.E("getApkInfo NameNotFoundException for " + packageName, e);
        }
        return null;
    }
    
    private void sendMessage(int what, Object obj) {
        Message msg = mHandler.obtainMessage(what);
        msg.obj = obj;
        mHandler.sendMessage(msg);
    }
    
    /*
     * 更新下载任务
     */
    private void refreshDownloadingApps() {
        
        if (mDownloadingList == null) {
            return;
        }
        
        Collection<DownloadInfo> taskList = mDownloadingList.values();
        int downloadCounter = 0;
        int updateCounter = mUpdateList.size();
        for (DownloadInfo info : taskList) {

            // 过期的项目(用户取消下载)，从列表中删除
            if (info.mStatus == DownloadManager.Impl.STATUS_CANCELED) {
                AppItem item = mDataSource.getValue(info.mPackageName);
                if (item != null) {
                    if (item.mIsUpdate) {
                        item.mViewType = VIEW_TYPE_INSTALLED;
                        item.mWeight = WEIGHT_NORMAL_UPDATE;
                        sendMessage(UPDATE_ITEM, item);
                    } else {
                        sendMessage(REMOVE_ITEM, item);
                    }
                }
                continue;
            }
            
            String packageName = info.mPackageName;
            AppItem item = mDataSource.getValue(packageName);
            if (item == null) {
                item = new AppItem();
                item.mAppName = info.mAppName;
                item.mPackageName = packageName;
                item.mKey = packageName;
                item.mIcon = info.mIconUrl;
            }
            item.mId = info.id;
            downloadCounter++;

            if (info.mStatus == DownloadManager.Impl.STATUS_SUCCESS) {
                // 下载成功的项目
                if (item.mIsUpdate) {
                    item.mViewType = VIEW_TYPE_INSTALLED;
                    item.mWeight = WEIGHT_NORMAL_UPDATE;
                    // 下载成功的更新项目不需要显示在下载区域
                    downloadCounter--;
                } else {
                    item.mViewType = VIEW_TYPE_DOWNLOADED;
                    item.mWeight = WEIGHT_DOWNLOAD_COMPLETE;
                }
                item.mFilePath = info.mFilePath;
                item.mInfo = mContext.getString(R.string.download_over);
            } else {
                // 下载过程中的项目
                item.mViewType = VIEW_TYPE_DOWNLOADING;
                item.mWeight = WEIGHT_DOWNLOAD_DOWNLOADING;
                item.mProgress = info.mProgressNumber;
                item.mInfo = Utils.calculateRemainBytes(mContext, info.mCurrentSize,
                        info.mTotalSize);
            }
            sendMessage(UPDATE_ITEM, item);

            if (mUpdateList.containsKey(item.mKey)) {
                updateCounter--;
            }
        }
        
        // 检查是否显示Download Title
        sendMessage(CHECK_DOWNLOAD_TITLE, downloadCounter);
        
        // 检查是否显示UpdateAll
        sendMessage(CHECK_UPDATE_TITLE, updateCounter);
    }
    
    /*package*/ void refresh() {
        notifyDataSetChanged();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void update(Observable arg0, Object arg1) {
        if (arg1 instanceof Integer) {
            onChanged((Integer) arg1);
        } else if (arg1 instanceof HashMap) {
            mDownloadingList = (HashMap<String, DownloadInfo>) arg1;
            onChanged(Constants.INFO_REFRESH);
        }
    }
    
    /* package */ void updateAll() {
        if (mUpdateList == null) {
            return;
        }
        
        for (String key : mUpdateList.keySet()) {
            
            if (mDownloadingList != null && mDownloadingList.containsKey(key)) {
                // 已经更新过的应用
                continue;
            }

            // 更新应用
            AppItem item = mDataSource.getValue(key);
//            MarketAPI.getDownloadUrl(mContext, AppsManagerAdapter.this, item.mProductId,
//                    Constants.SOURCE_TYPE_KULIAN);
            // 开始更新单个项目
            item.mWeight = WEIGHT_NORMAL_INSTALLED;
            item.mViewType = VIEW_TYPE_INSTALLED;
            item.mKey = item.mPackageName;
            sendMessage(UPDATE_ITEM, item);
        }
        mHandler.sendEmptyMessage(REFRESH);
    }

    @Override
    public void onSuccess(int method, Object obj) {
//        if (method == MarketAPI.ACTION_GET_DOWNLOAD_URL) {
//            DownloadItem info = (DownloadItem) obj;
//            AppItem item = mDataSource.getValue(info.packageName);
//            Request request = new Request(Uri.parse(info.url));
//            request.setTitle(item.mAppName);
//            request.setPackageName(info.packageName);
//            request.setSourceType(com.xiaohong.kulian.common.download.Constants.DOWNLOAD_FROM_MARKET);
//            request.setMD5(info.fileMD5);
//            item.mId = mDownloadManager.enqueue(request);
//            mUpdateList.remove(info.packageName);
//        }
    }
    
    @Override
    public void onError(int method, int statusCode) {
        if (statusCode == ApiAsyncTask.BUSSINESS_ERROR) {
            Utils.makeEventToast(mContext, mContext.getString(R.string.alert_no_download_url),
                    false);
        } else if (statusCode == ApiAsyncTask.TIMEOUT_ERROR) {
            Utils.makeEventToast(mContext, mContext.getString(R.string.no_data), false);
        }
    }
    
    /***
     * 更新已经安装的应用数
     */
    private void updateAppNumber() {
        List<PackageInfo> apps = Utils.getInstalledApps(mContext);
        AppItem item = mDataSource.getValue(INSTALLED_GROUP);
        item.mData = apps.size();
        item.mInfo = mContext.getString(R.string.app_counter, apps.size());
        sendMessage(UPDATE_ITEM, item);
    }
    
    /**
     * 卸载一个应用
     */
    /*package*/ void removedAppWithPackageName(String packageName) {
        AppItem item = mDataSource.getValue(packageName);
        if(item != null) {
            sendMessage(REMOVE_ITEM, item);
        }
        updateAppNumber();
        mHandler.sendEmptyMessage(REFRESH);
    }
    
    /**
     * 安装一个应用
     */
    /*package*/ void installAppWithPackageName(String packageName) {
        
        AppItem item = mDataSource.getValue(packageName);
        if (item != null) {
            if (item.mIsUpdate) {
                // 用户点击升级安装
                item.mIsUpdate = false;
                item.mCurrentVersionString = item.mNewVersionString;
                item.mNewVersionString = null;
                item.mViewType = VIEW_TYPE_INSTALLED;
                item.mWeight = WEIGHT_NORMAL_INSTALLED;
                sendMessage(UPDATE_ITEM, item);
            } else {
                // 用户全新安装
                AppItem app = getApkInfo(mContext, packageName);
                if (app != null) {
                    sendMessage(UPDATE_ITEM, app);
                } else {
                    sendMessage(REMOVE_ITEM, item);
                }
            }
        } else {
            // 用户全新安装
            AppItem app = getApkInfo(mContext, packageName);
            if (app != null) {
                sendMessage(UPDATE_ITEM, app);
            }
        }
        updateAppNumber();
        mHandler.sendEmptyMessage(REFRESH);
    }
    
    /* package */void delApp(int pos) {
        AppItem item = mDataSource.getValue(pos);
        if (item != null && !TextUtils.isEmpty(item.mFilePath)) {
            boolean result = new File(item.mFilePath).delete();
            if (result) {
                sendMessage(REMOVE_ITEM, item);
            }
        }
        mHandler.sendEmptyMessage(REFRESH);
    }
    
    /* package */void ignoreUpdate(int pos) {
        AppItem item = mDataSource.getValue(pos);
        item.mWeight = WEIGHT_NORMAL_INSTALLED;
        item.mNewVersionString = "";
        DBUtils.ignoreUpdate(mContext, item.mPackageName);
        
        mUpdateList.remove(item.mPackageName);
        mSession.setUpdateList(mUpdateList);
        mSession.setUpgradeNumber(mSession.getUpgradeNumber() - 1);
        
        sendMessage(UPDATE_ITEM, item);
        // 检查是否显示UpdateAll Title
        sendMessage(CHECK_UPDATE_TITLE, mSession.getUpgradeNumber());
        mHandler.sendEmptyMessage(REFRESH);
    }
    
    /* package */void close() {
        mSession.deleteObserver(this);
        mDataSource = null;
        mInflater = null;
        mContext = null;
        mSession = null;
        mUpdateList = null;
        mDownloadingList = null;
        mDownloadManager = null;
    }
    
    /**
     * 应用列表项<br>
     * 
     * 列表采用LinkedHashMap结构，Key是每个应用包名，Value是每个列表项，提高更新查找效率。<br>
     * 对于下载部分，由于存在包名重复的可能性，所以采用数据库ID作为包名。<br>
     * 
     * @author Andrew
     * @date 2011-5-18
     */
    public static class AppItem {
        /** 数据库ID */
        public long mId;
        /** Map Key */
        public String mKey;
        /** 产品ID(用于检查更新时，判断此应用是否存在于机锋市场) */
        public String mProductId;
        /** Title(Left信息) */
        public String mTitle;
        /** Info(Right信息) */
        public String mInfo;
        /** Info(Extra信息) */
        public String mInfo2;
        /** 应用名 */
        public String mAppName;
        /** 包名 */
        public String mPackageName;
        /** ICON(Drawable或者url) */
        public Object mIcon;
        /** 存储自定义数据 */
        public Object mData;
        /** 当然版本 */
        public String mCurrentVersion;
        /** 当然版本（装饰后字符串） */
        public String mCurrentVersionString;
        /** 升级版本 */
        public String mNewVersion;
        /**升级版本（装饰后字符串） */
        public String  mNewVersionString;
        /** 文件路径 */
        public String mFilePath;
        /** 列表项权重（用来排序） */
        public int mWeight;
        /** 下载项目的进度 */
        public int mProgress;
        /** 列表项的视图种类（本地已安装、下载中、下载完成...） */
        public int mViewType;
        /** 标识是否更新项目*/
        public boolean mIsUpdate;
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "[" + mAppName + "] weight " + mWeight;
        }
    }

    public static class ListOrderedMap {
        /** The content map */
        private HashMap<String, AppItem> map;
        private ArrayList<AppItem> list;
        private Comparator<AppItem> mSortComparator;
        private boolean isRefreshed;
        
        public ListOrderedMap() {
            map = new HashMap<String, AppItem>();
            mSortComparator = new Comparator<AppItem>() {
                @Override
                public int compare(AppItem object1, AppItem object2) {
                    int weight1 = object1.mWeight;
                    int weight2 = object2.mWeight;

                    return weight1 - weight2;
                }
            };
        }

        public AppItem put(String key, AppItem value) {
            synchronized (map) {
                isRefreshed = false;
                return map.put(key, value);
            }
        }

        public AppItem remove(String key) {
            synchronized (map) {
                isRefreshed = false;
                return map.remove(key);
            }
        }
        
        public void clear() {
            synchronized (map) {
                map.clear();
                list.clear();
            }
        }
        
        public AppItem getValue(int index) {
            synchronized (map) {
                while (!isRefreshed) {
                    refresh();
                }
                return list.get(index);
            }
        }
        
        public AppItem getValue(String key) {
            synchronized (map) {
                return map.get(key);
            }
        }
        
        private void refresh() {
            synchronized (map) {
                Collection<AppItem> values = map.values();
                if (values == null) {
                    return;
                }
                ArrayList<AppItem> tempList = new ArrayList<AppItem>(values);
                Collections.sort(tempList, mSortComparator);
                list = tempList;
                isRefreshed = true;
            }
        }
        
        public int size() {
            synchronized (map) {
                if (map == null) {
                    return 0;
                }
                return map.size();
            }
        }
    }
}