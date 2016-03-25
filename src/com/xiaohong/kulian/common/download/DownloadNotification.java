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

package com.xiaohong.kulian.common.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Session;
import com.xiaohong.kulian.common.util.Utils;

/**
 * This class handles the updating of the Notification Manager for the
 * cases where there is an ongoing download. Once the download is complete
 * (be it successful or unsuccessful) it is no longer the responsibility
 * of this component to show the download in the notification manager.
 *
 */
class DownloadNotification {

    Context mContext;
    HashMap <String, NotificationItem> mNotifications;
    NotificationManager mNotificationManager;

    static final String LOGTAG = "DownloadNotification";
    
    static final String WHERE_RUNNING =
        "(" + DownloadManager.Impl.COLUMN_STATUS + " >= '100') AND (" +
        DownloadManager.Impl.COLUMN_STATUS + " <= '199') AND (" +
        DownloadManager.Impl.COLUMN_VISIBILITY + " IS NULL OR " +
        DownloadManager.Impl.COLUMN_VISIBILITY + " == '" + 
        DownloadManager.Impl.VISIBILITY_VISIBLE + "' OR " +
        DownloadManager.Impl.COLUMN_VISIBILITY +
            " == '" + DownloadManager.Impl.VISIBILITY_VISIBLE_NOTIFY_COMPLETED + "')";
    
    static final String WHERE_COMPLETED =
        DownloadManager.Impl.COLUMN_STATUS + " >= '200' AND " +
        DownloadManager.Impl.COLUMN_VISIBILITY +
            " == '" + DownloadManager.Impl.VISIBILITY_VISIBLE_NOTIFY_COMPLETED + "'";

    /**
     * This inner class is used to collate downloads that are owned by
     * the same application. This is so that only one notification line
     * item is used for all downloads of a given application.
     *
     */
    static class NotificationItem {
        // This first db _id for the download for the app
        int mId;
        // current downloaded bytes
        long mCurrentBytes = 0;
        // total size
        long mTotalBytes = 0;
        // the number of title
        int mTitleCount = 0;
        // App package name
        String mPackageName; 
        // download titles.
        String[] mTitles = new String[2];
        String mPausedText = null;

        /*
         * Add a second download to this notification item.
         */
        void addItem(String title, long currentBytes, long totalBytes) {
            mCurrentBytes += currentBytes;
            if (totalBytes <= 0 || mTotalBytes == -1) {
                mTotalBytes = -1;
            } else {
                mTotalBytes += totalBytes;
            }
            if (mTitleCount < 2) {
                mTitles[mTitleCount] = title;
            }
            mTitleCount++;
        }
    }

    /**
     * Constructor
     * @param ctx The context to use to obtain access to the
     *            Notification Service
     */
    DownloadNotification(Context ctx) {
        mContext = ctx;
        mNotifications = new HashMap<String, NotificationItem>();
        mNotificationManager = (NotificationManager)
        	mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }
    
    /*
     * Clear all notifications
     */
    public void clearAllNotification() {
    	if(mNotificationManager != null) {
    		mNotificationManager.cancelAll();
    	}
    }
    
    /*
     * Cancel notification use id 
     */
    public void cancelNotification(long id) {
    	if(mNotificationManager != null) {
			mNotificationManager.cancel((int) id);
    	}
    }
    
    /*
     * Update the notification ui.
     */
    public void updateNotification(Collection<DownloadInfo> downloads) {
        
        // Collate the notifications
        mNotifications.clear();
        
        for (DownloadInfo download : downloads) {
            
            if (isActiveAndVisible(download)) {
                // downloading items
                updateActivieNotification(download);
                
            } else if(isCompleteAndVisible(download)) {
                // downloaded items
                updateCompletedNotification(download);
                
            } else if(isCompleteAndInstall(download)) {
              
                // inatall OTA
                startInstallOta(download);
                
            } else {
                // others
            }
        }
        
        addActiviteNotifications();
    }
    
    private void startInstallOta(DownloadInfo download) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(),
                    com.xiaohong.kulian.Constants.IMAGE_CACHE_DIR);
            root.mkdirs();
            File output = new File(root, "aMarket.apk");
            Utils.copyFile(new FileInputStream(new File(download.mFileName)), 
                    new FileOutputStream(output));
            Utils.installApk(mContext, output);
            Session.get(mContext).getDownloadManager().hideDownload(download.mId);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /*
     * update the ongoing notification item
     */
    private void updateActivieNotification(DownloadInfo download) {
        
        String packageName = download.mPackage;
        long max = download.mTotalBytes;
        long progress = download.mCurrentBytes;
        long id = download.mId;
        String title = download.mTitle;
        if (TextUtils.isEmpty(title)) {
            Utils.D("don't get any title information");
            title = mContext.getResources().getString(R.string.download_unknown_title);
        }

        NotificationItem item;
        if (mNotifications.containsKey(packageName)) {
            item = mNotifications.get(packageName);
            item.addItem(title, progress, max);
            Utils.D("just update the notification which already exist and title is " + title);
        } else {
            item = new NotificationItem();
            item.mId = (int) id;
            item.addItem(title, progress, max);
            mNotifications.put(packageName, item);
            Utils.D("just create one new notification and title is " + title);
        }

        // This item paused by user
        if (download.mStatus == DownloadManager.Impl.STATUS_PAUSED_BY_APP
                && item.mPausedText == null) {
            item.mPausedText = mContext.getResources().getString(
                    R.string.notification_paused_by_app);
        }
    }
    
    /*
     * Add notification items
     */
    private void addActiviteNotifications() {

        // Add the notifications
        for (NotificationItem item : mNotifications.values()) {
            // Build the notification object
            Notification n = new Notification();

            boolean hasPausedText = (item.mPausedText != null);
            int iconResource = android.R.drawable.stat_sys_download;
            if (hasPausedText) {
                iconResource = android.R.drawable.stat_sys_warning;
            }
            n.icon = iconResource;

            n.flags |= Notification.FLAG_ONGOING_EVENT;

            // Build the RemoteView object
            RemoteViews expandedView = new RemoteViews("com.xiaohong.kulian",
                    R.layout.status_bar_ongoing_event_progress_bar);
            StringBuilder title = new StringBuilder(item.mTitles[0]);
            if (item.mTitleCount > 1) {
                title.append(mContext.getString(R.string.notification_filename_separator));
                title.append(item.mTitles[1]);
                n.number = item.mTitleCount;
                if (item.mTitleCount > 2) {
                    title.append(mContext.getString(R.string.notification_filename_extras,
                            new Object[] { Integer.valueOf(item.mTitleCount - 2) }));
                }
            }
            expandedView.setTextViewText(R.id.title, title);

            if (hasPausedText) {
                expandedView.setViewVisibility(R.id.progress_bar, View.GONE);
                expandedView.setTextViewText(R.id.paused_text, item.mPausedText);
            } else {
                expandedView.setViewVisibility(R.id.paused_text, View.GONE);
                expandedView.setProgressBar(R.id.progress_bar, (int) item.mTotalBytes,
                        (int) item.mCurrentBytes, item.mTotalBytes == -1);
            }
            expandedView.setTextViewText(R.id.progress_text,
                    getDownloadingText(item.mTotalBytes, item.mCurrentBytes));
            expandedView.setImageViewResource(R.id.appIcon, iconResource);
            n.contentView = expandedView;

            Intent intent = new Intent(Constants.ACTION_LIST);
            intent.setClassName("com.xiaohong.kulian", DownloadReceiver.class.getName());
            intent.setData(ContentUris.withAppendedId(DownloadManager.Impl.CONTENT_URI, item.mId));
            intent.putExtra("multiple", item.mTitleCount > 1);

            n.contentIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
            mNotificationManager.notify((int) item.mId, n);
        }
    }

    /*
     * Update the completed notification item                                                                                
     */
    private void updateCompletedNotification(DownloadInfo download) {

        // Add the notifications
        Notification n = new Notification();
        n.icon = android.R.drawable.stat_sys_download_done;

        long id = download.mId;
        String title = download.mTitle;
        if (TextUtils.isEmpty(title)) {
            title = mContext.getResources().getString(R.string.download_unknown_title);
        }
        Uri contentUri = ContentUris.withAppendedId(DownloadManager.Impl.CONTENT_URI, id);
        String caption;
        Intent intent = new Intent(Constants.ACTION_OPEN);
        if (DownloadManager.Impl.isStatusError(download.mStatus)) {
            // download have some troubles, when user click this notification direct goto the
            // product's details page
            caption = handleErrorMessage(download.mStatus);
            intent.putExtra(DownloadManager.Impl.COLUMN_STATUS,
                    DownloadManager.Impl.STATUS_UNKNOWN_ERROR);
            Session.get(mContext).getDownloadingList().remove(download.mPackageName);
            Session.get(mContext).updateDownloading();
        } else {
            // download success
            intent.putExtra(DownloadManager.Impl.COLUMN_STATUS,
                    DownloadManager.Impl.STATUS_SUCCESS);
            caption = mContext.getResources().getString(R.string.notification_download_complete);
        }
        intent.setClassName("com.xiaohong.kulian", DownloadReceiver.class.getName());
        intent.setData(contentUri);

        n.when = download.mLastMod;
        n.setLatestEventInfo(mContext, title, caption,
                PendingIntent.getBroadcast(mContext, 0, intent, 0));

        // make this item invisible after click event
        intent = new Intent(Constants.ACTION_HIDE);
        intent.setClassName("com.xiaohong.kulian", DownloadReceiver.class.getName());
        intent.setData(contentUri);
        n.deleteIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);

        // update the download status
        mNotificationManager.notify((int) download.mId, n);
    }
    
    private String handleErrorMessage(int status) {
        if (DownloadManager.Impl.STATUS_BAD_REQUEST == status) {
            return mContext.getString(R.string.download_alert_url);
        } else if (DownloadManager.Impl.STATUS_NOT_ACCEPTABLE == status) {
            return mContext.getString(R.string.download_error_file_type);
        } else if (DownloadManager.Impl.STATUS_LENGTH_REQUIRED == status
                || DownloadManager.Impl.STATUS_PRECONDITION_FAILED == status
                || DownloadManager.Impl.STATUS_UNKNOWN_ERROR == status) {
            return  mContext.getString(R.string.download_alert_service);
        } else if (DownloadManager.Impl.STATUS_FILE_ALREADY_EXISTS_ERROR == status
                || DownloadManager.Impl.STATUS_FILE_ERROR == status) {
            return mContext.getString(R.string.download_alert_client);
        } else if (DownloadManager.Impl.STATUS_CANCELED == status) {
            return mContext.getString(R.string.download_alert_cancel);
        } else if (DownloadManager.Impl.STATUS_UNHANDLED_REDIRECT == status
                || DownloadManager.Impl.STATUS_UNHANDLED_HTTP_CODE == status
                || DownloadManager.Impl.STATUS_HTTP_EXCEPTION == status
                || DownloadManager.Impl.STATUS_HTTP_DATA_ERROR == status
                || DownloadManager.Impl.STATUS_TOO_MANY_REDIRECTS == status) {
           return mContext.getString(R.string.download_alert_network);
        } else if (DownloadManager.Impl.STATUS_DEVICE_NOT_FOUND_ERROR == status) {
            return mContext.getString(R.string.download_alert_no_sdcard);
        } else if (DownloadManager.Impl.STATUS_INSUFFICIENT_SPACE_ERROR == status) {
            return mContext.getString(R.string.download_alert_no_space);
        }  else {
            return mContext.getString(R.string.download_error);
        }
    }

    private boolean isActiveAndVisible(DownloadInfo download) {
        return 100 <= download.mStatus && download.mStatus < 200
                && download.mVisibility != DownloadManager.Impl.VISIBILITY_HIDDEN;
    }

    private boolean isCompleteAndVisible(DownloadInfo download) {
        return download.mStatus >= 200
                && download.mVisibility == DownloadManager.Impl.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;
    }
    
    private boolean isCompleteAndInstall(DownloadInfo download) {
        return download.mStatus == 200
                && download.mSource == Constants.DOWNLOAD_FROM_OTA
                && download.mVisibility == DownloadManager.Impl.VISIBILITY_VISIBLE
                && Constants.MIMETYPE_APK.equals(download.mMimeType);
    }

    /*
     * Helper function to build the downloading text.
     */
    private String getDownloadingText(long totalBytes, long currentBytes) {
        if (totalBytes <= 0) {
            return "";
        }
        long progress = currentBytes * 100 / totalBytes;
        StringBuilder sb = new StringBuilder();
        sb.append(progress);
        sb.append('%');
        return sb.toString();
    }

}