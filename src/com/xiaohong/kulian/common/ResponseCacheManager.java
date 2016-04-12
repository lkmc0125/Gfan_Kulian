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
package com.xiaohong.kulian.common;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.text.TextUtils;

/**
 * API响应缓存管理器
 * 
 * @author Andrew
 * @date 2011-5-11
 * 
 */
public class ResponseCacheManager {

    private static ResponseCacheManager mInstance;
    private static Object mLock = new Object();
    
    private HashMap<String, Object> mResponsePool;
    private SoftReference<HashMap<String, Object>> mResponseCache;

    private ResponseCacheManager() {
        mResponsePool = new HashMap<String, Object>();
        mResponseCache = new SoftReference<HashMap<String, Object>>(mResponsePool);
    }

    public static ResponseCacheManager getInstance() {
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new ResponseCacheManager();
            }
        }
        return mInstance;
    }

    /**
     * 从缓存中获取API访问结果
     */
    public Object getResponse(String key) {

        if (TextUtils.isEmpty(key))
            return null;
        
        if(mResponseCache == null) {
            return null;
        }
        
        return mResponseCache.get().get(key);
    }

    /**
     * 缓存API访问结果
     */
    public void putResponse(String key, Object value) {
        if(mResponseCache != null) {
            mResponseCache.get().put(key, value);
        }
    }
    
    /**
     * 清除所有API缓存
     */
    public void clear() {
        if (mResponseCache != null) {
            mResponseCache.clear();
            mResponseCache = null;
        }
        if (mResponsePool != null) {
            mResponsePool.clear();
            mResponsePool = null;
        }
        mInstance = null;
    }

}