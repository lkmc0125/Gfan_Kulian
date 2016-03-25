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

import android.app.ActivityGroup;
import android.content.Context;
import android.os.Bundle;

import com.umeng.analytics.MobclickAgent;
import com.xiaohong.kulian.Session;
import com.xiaohong.kulian.common.ResponseCacheManager;

/**
 * 普通页面<br>
 * 
 * 一些基本的页面控制都在这里实现，比如SDK统计，Google统计，内存控制
 * 
 * @author andrew
 * @date    2011-4-20
 * @since Version 0.7.0
 *
 */
public class BaseActivity extends ActivityGroup {

    /** 应用Session */
    protected Session mSession;
    
    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        final Context context = getApplicationContext();
        mSession = Session.get(context);
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        final Context context = getApplicationContext();
        mSession = Session.get(context);
        MobclickAgent.onResume(this);
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onLowMemory()
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        ResponseCacheManager.getInstance().clear();
    }
    
}
