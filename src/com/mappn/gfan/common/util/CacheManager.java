/*
 * Copyright (C) 2010 mAPPn.Inc
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
package com.mappn.gfan.common.util;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Process;

import com.mappn.gfan.Constants;

/**
 * Cache Manager
 * 
 * For caching something like drawables, objects... 
 * 
 * @author andrew.wang
 *
 */
public class CacheManager {

	private static CacheManager mInstance;
	
	// the max cached icon in the SD-Card
	private static final int MAX_IMAGE_IN_SDCARD = 100;

	// cache in the memory
    private LinkedHashMap<String, Bitmap> mDrawableMap = new LinkedHashMap<String, Bitmap>();
    private SoftReference<LinkedHashMap<String, Bitmap>> mDrawableCache = 
            new SoftReference<LinkedHashMap<String, Bitmap>>(mDrawableMap);
    
    private ArrayList<String> mNewIcons = new ArrayList<String>();
	
    private CacheManager() {
//        loadIconsFromSDCard();
    }
	
    public static CacheManager getInstance() {
        if (mInstance == null) {
            mInstance = new CacheManager();
        }
        return mInstance;
    }
    
    /**
     *	Get the icon list from memory cache 
     */
	private LinkedHashMap<String, Bitmap> getMemoCache() {

		if (mDrawableCache == null) {
			mDrawableCache = new SoftReference<LinkedHashMap<String, Bitmap>>(mDrawableMap);
			return mDrawableMap;
		}

		return mDrawableCache.get();
	}
	
    /**
     * Check whether the drawable had been cached
     * 
     * @param key
     *            drawable uri
     * @return ture : exist false : not exist
     */
    public boolean existsDrawable(String key) {

    	// fetch the drawable from memory
		HashMap<String, Bitmap> memoCache = getMemoCache();
		if (memoCache.containsKey(StringUtils.getFileNameFromUrl(key))) return true;
        
        return false;
    }
	
    /**
     * 开始缓存图片 
     */
    public synchronized void cacheDrawable(String url, Bitmap image) {

        if (image == null) {
            return;
        }

        // put the icon in the memory
        LinkedHashMap<String, Bitmap> memoCache = getMemoCache();
        
        if (memoCache == null) {
            return;
        }
        String key = StringUtils.getFileNameFromUrl(url);
        if(memoCache.size() < MAX_IMAGE_IN_SDCARD) {
            memoCache.put(key, image);
            return;
        }
        
        // reach here means L1 is full, reorder current stack
        Iterator<String> iterator = memoCache.keySet().iterator();
        if (iterator.hasNext()) {
            // recycle the bitmap
            String oldBitmapUrl = (String) iterator.next();
            memoCache.remove(oldBitmapUrl);
            
            // reorder the new image into L1 cache stack
            memoCache.put(key, image);
        }
        return;
    }

    /**
     * 从缓存中获取图片 
     * @param url 图片的HTTP资源的地址
     */
    public Bitmap getDrawableFromCache(String url) {

        // retrieve bitmap from cache
        final String urlFileName = StringUtils.getFileNameFromUrl(url);
        HashMap<String, Bitmap> memoCache = getMemoCache();
        return memoCache.get(urlFileName);
    }
	
//	/**
//	 * 将缓存图片存入SD卡
//	 */
//    public void close() {
//
//        // check the sd-card status
//        if (!Utils.isSdcardWritable()) {
//            clearFromMemory();
//            Utils.D("save icons failed because sd card is not writable");
//            return;
//        }
//
//        new Thread() {
//
//            @Override
//            public void run() {
//                // make this thread in background priority
//                setPriority(Process.THREAD_PRIORITY_BACKGROUND);
//
//                File cacheDirectory = new File(Environment.getExternalStorageDirectory(),
//                        Constants.IMAGE_CACHE_DIR);
//                
//                if (!cacheDirectory.exists()) {
//                    Utils.V("cache dir not exist, make it immediately success");
//                    if(cacheDirectory.mkdirs()) {
//                        Utils.V("cache dir not exist, make it immediately success");
//                        
//                        HashMap<String, Bitmap> memoCache = getMemoCache();
//                        if (memoCache != null) {
//                            final int length = mNewIcons.size();
//                            try {
//                                for (int i = 0; i < length; i++) {
//                                    String key = mNewIcons.get(i);
//                                    final Bitmap bmp = memoCache.get(key);
//                                    if (bmp == null) {
//                                        continue;
//                                    }
//                                    FileOutputStream fos = null;
//                                    File file = new File(cacheDirectory, key);
//                                    if (file.exists()) {
//                                        continue;
//                                    }
//                                    fos = new FileOutputStream(file);
//                                    bmp.compress(CompressFormat.PNG, 100, fos);
//                                    fos.close();
//                                }
//                            } catch (FileNotFoundException e) {
//                                Utils.D("save icon fail because make file error", e);
//                            } catch (IOException e) {
//                                Utils.D("save icon fail because IOException", e);
//                            }
//                        }
//                    } else {
//                        Utils.V("cache dir not exist, make it immediately fail");
//                    }
//                }
//                // clear all the cache
//                clearFromMemory();
//            }
//        }.start();
//    }
	
//	/*
//	 * 从SD卡加载缓存图片
//	 */
//    private void loadIconsFromSDCard() {
//        
//        // check the sd-card status
//        if (!Utils.isSdcardReadable()) {
//            Utils.V("load icons failed because sd card is not readable");
//            return;
//        }
//        
//        new Thread() {
//            @Override
//            public void run() {
//                // make this thread in background priority
//                setPriority(Process.THREAD_PRIORITY_BACKGROUND);
//
//                File cacheDirectory = new File(Environment.getExternalStorageDirectory(),
//                        Constants.IMAGE_CACHE_DIR);
//                if (cacheDirectory.exists()) {
//                    File[] files = cacheDirectory.listFiles();
//                    int length = files.length;
//                    if (length > MAX_IMAGE_IN_SDCARD) {
//                        // 防止缓存文件冗余，清除超过最大缓存数的文件
//                        length = MAX_IMAGE_IN_SDCARD;
//                        for (int i = length, len = files.length; i < len; i++) {
//                            files[i].delete();
//                        }
//                    }
//                    
//                    HashMap<String, Bitmap> memoCache = getMemoCache();
//                    if (memoCache == null) {
//                        return;
//                    }
//                    synchronized (CacheManager.this) {
//                        try {
//                            for (int i = 0; i < length; i++) {
//                                final String fileName = files[i].getName();
//                                Bitmap bmp = BitmapFactory.decodeFile(files[i].getAbsolutePath());
//                                memoCache.put(fileName, bmp);
//                            }
//                        } catch (OutOfMemoryError e) {
//                            Utils.W("load icon from sd card occur OOM error");
//                        }
//                    }
//                }
//            }
//        }.start();
//    }
    
    /**
     * 当内存不足时清除所有缓存包括应用退出时
     */
    public void clearFromMemory() {
        if (mNewIcons != null) {
            mNewIcons.clear();
        }
        if (mDrawableMap != null) {
            mDrawableMap.clear();
            mDrawableMap = null;
        }
        if (mDrawableCache != null) {
            mDrawableCache.clear();
            mDrawableCache = null;
        }
        mInstance = null;
    }
    
    /**
     * 从磁盘清除缓存
     */
    public void clearFromFile() {
        Thread clearTask = new Thread() {
            @Override
            public void run() {
                File cacheDirectory = new File(Environment.getExternalStorageDirectory(),
                        Constants.IMAGE_CACHE_DIR);
                if (cacheDirectory.exists()) {
                    File[] files = cacheDirectory.listFiles();
                    for (File file : files) {
                        file.delete();
                    }
                }
            }
        };
        clearTask.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        clearTask.start();
    }
}
