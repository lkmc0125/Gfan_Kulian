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

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Gallery;

/**
 * 自定义的滑动速度Gallery
 * 
 * @author  Andrew
 * @date    2011-6-30
 *
 */
public class TopGallery extends Gallery {

    public TopGallery(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TopGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TopGallery(Context context) {
        super(context);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Gallery#onFling(android.view.MotionEvent, android.view.MotionEvent,
     * float, float)
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return super.onFling(e1, e2, velocityX / 2, velocityY);
    }

}
