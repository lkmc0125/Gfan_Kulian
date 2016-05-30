package com.xiaohong.kulian.common.download.simple;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
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
    public static final String DEFAULT_MARKET_SUBDIR = "kulian/market";
    private static final String TAG = "SimpleDownloadManager";
    private Timer mTimer;
    private Context mContext;
    private CopyOnWriteArrayList<String> mDownloadIds;
    /**
     * This map is used to find id by download id
     */
    private HashMap<String, Long> mUrlIdsMap = new HashMap<String, Long>();
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
        Log.d(TAG, "addOnDownloadStatusChangedListener");
        if(!mOnDownloadStatusChangedListenerList.contains(listener)) {
            mOnDownloadStatusChangedListenerList.add(listener);
        }else {
            Log.d(TAG, "listener is in the list");
        }
        
    }
    
    public void removeOnDownloadStatusChangedListener(
            OnDownloadStatusChangedListener listener) {
        Log.d(TAG, "removeOnDownloadStatusChangedListener");
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
        Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).mkdir();

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
        mUrlIdsMap.put(url, downloadId);
        startTimer();
        Log.d(TAG, "downloadApk downloadId = " + downloadId);
        return downloadId;
    }

    /**
     * Stop a download by id
     * @param id
     */
    @SuppressLint("NewApi")
    public void stopDownloadApk(long id) {
        mDownloadManager.remove(id);
        mDownloadIds.remove(id);
        Collection<Long> ids = mUrlIdsMap.values();
        ids.remove(id);
    }
    
    /**
     * Stop a download by a url
     * @param url
     */
    @SuppressLint("NewApi")
    public void stopDownloadApk(String url) {
        Long id = mUrlIdsMap.get(url);
        if(id != null) {
            mDownloadManager.remove(id);
            mUrlIdsMap.remove(url);
            mDownloadIds.remove(id);
        }
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
                        "kulian",
                        "market" + "/" + appName + ".apk");
        /*File file =  new File("kulian",
                "market" + "/" + appName + ".apk");*/

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
                downloadId + ", progress = " + progress + 
                ", listener size=" + mOnDownloadStatusChangedListenerList.size());
        for(OnDownloadStatusChangedListener listener : mOnDownloadStatusChangedListenerList) {
            Log.d(TAG, "listener = " + listener);
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
    
    /**
     * Get download id by url
     * @param url
     * @return
     */
    public long getDownloadId(String url) {
        Long id = mUrlIdsMap.get(url);
        if(id == null) {
            return -1;
        }else {
            return id;
        }
    }
}
