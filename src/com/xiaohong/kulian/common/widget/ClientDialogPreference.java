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


import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import com.xiaohong.kulian.common.util.CacheManager;
import com.xiaohong.kulian.common.util.DBUtils;
import com.xiaohong.kulian.common.util.Utils;


/**
 * Dialog Preference Component for Gfan Mobile project 
 * 
 * @author andrew.wang
 * @date    2010-9-3
 *
 */
public class ClientDialogPreference extends DialogPreference {

    public ClientDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        final String key = getKey();
        switch (which) {
        case DialogInterface.BUTTON_POSITIVE:
            
            if ("manual_clear_cache".equals(key)) {
                // clear the cache
                Utils.clearCache(getContext());
                CacheManager.getInstance().clearFromMemory();
                CacheManager.getInstance().clearFromFile();
            } else if ("manual_clear_search_history".equals(key)) {
                DBUtils.clearSearchHistory(getContext());
            }
            
            break;
        case DialogInterface.BUTTON_NEGATIVE:
            // do nothing
            break;
        default:
            break;
        }
    }
}
