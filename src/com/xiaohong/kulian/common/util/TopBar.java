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
package com.xiaohong.kulian.common.util;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.ui.FileManagerActivity;

/**
 * TopBar utility class
 * 
 * @author alex
 * @date 2011-1-1
 * @since Version 0.4.0
 * 
 */
public class TopBar {

    public static void createTopBar(final Context context, View[] views, int[] visibility,
            String title) {

        final int size = views.length;
        for (int i = 0; i < size; i++) {
            // set visibility
            View v = views[i];
            v.setVisibility(visibility[i]);

            if (View.GONE == visibility[i]) {
                // no need to assign values
                continue;
            }

            switch (v.getId()) {

            case R.id.top_bar_title:

                ((TextView) v).setText(title);
                break;

            case R.id.top_bar_files:

                // go to local file manager activity
                v.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        Utils.trackEvent(context, Constants.GROUP_7,
                                Constants.CLICK_FILE_MANAGER);
                        Intent intent = new Intent(context, FileManagerActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                });
                break;
            }
        }
    }

}