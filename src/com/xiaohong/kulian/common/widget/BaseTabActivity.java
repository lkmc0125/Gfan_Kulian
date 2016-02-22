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
package com.xiaohong.kulian.common.widget;

import android.app.TabActivity;
import android.content.Context;
import android.os.Bundle;

import com.xiaohong.kulian.Session;
import com.xiaohong.kulian.common.ResponseCacheManager;

/**
 * An activity that contains and runs multiple embedded activities or views.
 */
public class BaseTabActivity extends TabActivity {
    
    /** 应用Session */
    protected Session mSession;
    
    /* (non-Javadoc)
     * @see android.app.ActivityGroup#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        final Context context = getApplicationContext();
        mSession = Session.get(context);
    }
    
    /* (non-Javadoc)
     * @see android.app.ActivityGroup#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        
        final Context context = getApplicationContext();
        mSession = Session.get(context);
    }

    /* (non-Javadoc)
     * @see android.app.ActivityGroup#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();
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