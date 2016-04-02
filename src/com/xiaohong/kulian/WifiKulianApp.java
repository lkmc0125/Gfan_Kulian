package com.xiaohong.kulian;

import java.io.File;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class WifiKulianApp extends Application {
    private static final String TAG = "WifiKulianApp";

    @Override
    public void onCreate() {
        super.onCreate();
        String filePath = getPathDisk(getApplicationContext());
        File cacheDir = StorageUtils
                .getOwnCacheDirectory(getApplicationContext(), filePath);// 获取到缓存的目录地址
        Log.d(TAG, "cacheDir = " + cacheDir.getPath());
        // Create global configuration and initialize ImageLoader with this configuration
        ImageLoaderConfiguration config = new ImageLoaderConfiguration  
                .Builder(this)  
                .memoryCacheExtraOptions(480, 800) // max width, max height，即保存的每个缓存文件的最大长宽  
                .threadPoolSize(3)//线程池内加载的数量  
                .threadPriority(Thread.NORM_PRIORITY - 2)  
                .denyCacheImageMultipleSizesInMemory()  
                .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024)) // You can pass your own memory cache implementation/你可以通过自己的内存缓存实现  
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheSize(100 * 1024 * 1024)// 50 MiB
                .tasksProcessingOrder(QueueProcessingType.FIFO) 
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())  
                .imageDownloader(new BaseImageDownloader(this, 5 * 1000, 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)超时时间  
                .writeDebugLogs() // Remove for release app  
                .discCache(new UnlimitedDiskCache(cacheDir))
                
                .build();//开始构建  

        ImageLoader.getInstance().init(config);
    }
    public static String getPathDisk(Context context) {
        String filePath = Constants.IMAGE_LOADER_CACHE_DIR;
        try {
            try {
                // 判断是否存在SD
                if (Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
                    Log.d(TAG, "sdcard is exist");
                    filePath = Environment.getExternalStorageState() + "/" + filePath + "/";
                    File file = new File(filePath);
                    if(file.exists() == false) {
                        boolean result = file.mkdirs();
                        Log.d(TAG, "create dir result:" + result);
                    }else {
                        Log.d(TAG, filePath + " exist");
                    }
                } else {
                    filePath = context.getCacheDir().getPath() + filePath;
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "init fail");
                filePath = context.getCacheDir().getPath() + filePath;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "init fail:" + e.getMessage());
        }
        return filePath;
    }

}
