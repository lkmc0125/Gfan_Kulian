package com.xiaohong.kulian.common.download.simple;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

/**
 * A simple download manager to download apk, it's singleton
 * It's suggested that you pass a application context to construct a SimpleDownloadManager
 * The client can register a OnDownloadStatusChangedListener to observer the download status
 * @author free
 *
 */
public class SimpleDownloadManager {
    private static final String TAG = "SimpleDownloadManager";
    private Timer mTimer;
    private Context mContext;
    private CopyOnWriteArrayList<String> mDownloadIds;
    private HashMap<String, String> mDownloadFinishedMap; // key:id, value:apk
                                                          // path
    private ArrayList<OnDownloadStatusChangedListener> mOnDownloadStatusChangedListenerList
        = new ArrayList<OnDownloadStatusChangedListener>();

    /**
     * A listener to notify a item's status is changed
     * include download progress is updated
     * download completed successfully and fail
     * @author free
     *
     */
    public static interface OnDownloadStatusChangedListener {
        public void onDownloadStatusChanged(long downloadId, int status);
        public void onProgressUpdate(long downloadId, int progress);
    }
    
    public synchronized String getDownloadPath(String downloadId) {
        return mDownloadFinishedMap.get(downloadId);
    }
    
    private static SimpleDownloadManager sInstance = null;
    
    public static synchronized SimpleDownloadManager 
        getInstance(Context context) {
        if(sInstance == null) {
            sInstance = new SimpleDownloadManager(context);
        }
        return sInstance;
    }
    
    public void addOnDownloadStatusChangedListener(
            OnDownloadStatusChangedListener listener) {
        mOnDownloadStatusChangedListenerList.add(listener);
    }
    
    public void removeOnDownloadStatusChangedListener(
            OnDownloadStatusChangedListener listener) {
        mOnDownloadStatusChangedListenerList.remove(listener);
    }

    private SimpleDownloadManager(Context context) {
        mContext = context;
        mDownloadIds = new CopyOnWriteArrayList<String>();
        mDownloadFinishedMap = new HashMap<String, String>();
        mDownloadManager = (DownloadManager) mContext
                .getSystemService(Context.DOWNLOAD_SERVICE);
    }

    @SuppressLint("NewApi")
    public synchronized long downloadApk(String url, String appName)
            throws MalformedURLException {
        Uri uri = Uri.parse(url);
        Environment.getExternalStoragePublicDirectory(SimpleDownloadConstants.DEFAULT_MARKET_SUBDIR).mkdir();

        Request request = new DownloadManager.Request(uri)
                .setAllowedNetworkTypes(
                        DownloadManager.Request.NETWORK_MOBILE
                                | DownloadManager.Request.NETWORK_WIFI)
                .setAllowedOverRoaming(false)
                .setTitle(appName)
                .setDestinationInExternalPublicDir(
                        "kulian",
                        "market" + "/" + appName + ".apk");
        // request.allowScanningByMediaScanner();

        long downloadId = mDownloadManager.enqueue(request);
        mDownloadIds.add(Long.toString(downloadId));
        startTimer();
        return downloadId;
    }
    
    @SuppressLint("NewApi")
    public synchronized long downloadFile(String url, String appName)
            throws MalformedURLException {
        Uri uri = Uri.parse(url);
        Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).mkdir();

        Request request = new DownloadManager.Request(uri)
                .setAllowedNetworkTypes(
                        DownloadManager.Request.NETWORK_MOBILE
                                | DownloadManager.Request.NETWORK_WIFI)
                .setAllowedOverRoaming(false)
                .setTitle(appName)
                .setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        "_" + (int) (Math.random() * 100000) + ".apk");

        long downloadId = mDownloadManager.enqueue(request);
        mDownloadIds.add(Long.toString(downloadId));
        startTimer();
        return downloadId;
    }

    private DownloadManager mDownloadManager = null;
    @SuppressLint("NewApi")
    private synchronized void queryDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        try {
            for (String downloadIdStr : mDownloadIds) {
                Long downloadId = Long.parseLong(downloadIdStr);
                query.setFilterById(downloadId);
                Cursor c = mDownloadManager.query(query);
                if (c != null && c.moveToFirst()) {
                    int status = c.getInt(c
                            .getColumnIndex(DownloadManager.COLUMN_STATUS));
                    int titleIdx = c
                            .getColumnIndex(DownloadManager.COLUMN_TITLE);
                    int fileSizeIdx = c
                            .getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                    int bytesDLIdx = c
                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                    int pathIdx = c
                            .getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);

                    String title = c.getString(titleIdx);
                    String path = c.getString(pathIdx);
                    int fileSize = c.getInt(fileSizeIdx);
                    int bytesDL = c.getInt(bytesDLIdx);
                    int progress = (int) ((float) bytesDL / (float) fileSize * 100);

                    switch (status) {
                        case DownloadManager.STATUS_PAUSED :
                             Log.v(TAG, "STATUS_PAUSED");
                        case DownloadManager.STATUS_PENDING :
                             Log.v(TAG, "STATUS_PENDING");
                        case DownloadManager.STATUS_RUNNING :
                             Log.v(TAG, "STATUS_RUNNING");
                            break;
                        case DownloadManager.STATUS_SUCCESSFUL :
                            Log.v(TAG, title + " STATUS_SUCCESSFUL");
                            mDownloadFinishedMap.put(
                                    String.valueOf(downloadId), path);
                            boolean result = mDownloadIds.remove(String.valueOf(downloadId));
                            Log.d(TAG, "remove result = " + result);
                            break;
                        case DownloadManager.STATUS_FAILED :
                            Log.v(TAG, "STATUS_FAILED");
                            mDownloadManager.remove(downloadId); 
                            break;
                        default :
                            break;
                    }
                    c.close();
                    if(status == DownloadManager.STATUS_RUNNING) {
                        notifyProgressUpdate(downloadId, progress);
                    }else {
                        notifyDowloadStatusChanged(downloadId, status);
                    }
                }
            }//end for
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    public synchronized void cancelDownload(long downloadId) {
        Log.d(TAG, "cancelDownload");
        mDownloadManager.remove(downloadId);
        mDownloadIds.remove(String.valueOf(downloadId));
    }

    private void startTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {

                @Override
                public void run() {

                    if (mDownloadIds.size() == 0) {
                        mTimer.cancel();
                        mTimer = null;
                        return;
                    }
                    queryDownloadStatus();

                }
            }, 1000, 1000);
        }
    }
    
    private void notifyProgressUpdate(long downloadId, int progress) {
        Log.d(TAG, "notifyProgressUpdate downloadId = " + 
                downloadId + ", progress = " + progress);
        for(OnDownloadStatusChangedListener listener : mOnDownloadStatusChangedListenerList) {
            listener.onProgressUpdate(downloadId, progress);
        }
    }
    
    private void notifyDowloadStatusChanged(long downloadId, int status) {
        Log.d(TAG, "notifyDowloadStatusChanged downloadId = " + 
                downloadId + ", status = " + status);
        for(OnDownloadStatusChangedListener listener : mOnDownloadStatusChangedListenerList) {
            listener.onDownloadStatusChanged(downloadId, status);
        }
    }
}
