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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LevelListDrawable;

import com.xiaohong.kulian.R;

/**
 * The indicator drawable for GfanMobile Home Page
 * 
 * @author  andrew.wang
 * @date    2010-11-17
 * @since   Version 0.4.0
 * 
 */
public class IndicatorDrawable extends LevelListDrawable {

    private Context mContext;
    private int mIndicatorNum;
    private Canvas mCanvas;
    private int mPointSize;
    private int mWidth;
    private int mStepWidth;
    private Paint mPainter;
    private boolean mIsWhite;
    
    public IndicatorDrawable(Context context, int num, boolean isWhite) {
        mContext = context;
        mIndicatorNum = num;
        mCanvas = new Canvas();
        mPointSize = context.getResources().getDimensionPixelSize(R.dimen.indicator_size);
        mStepWidth = context.getResources().getDimensionPixelSize(R.dimen.indicator_step_width);
        mWidth = mStepWidth * num;
        mPainter = new Paint();
        mIsWhite = isWhite;
        initDrawable();
    }
    
    private void initDrawable() {
        
        Bitmap normalPoint = 
            BitmapFactory.decodeResource(mContext.getResources(), R.drawable.indicator_normal);
        Bitmap highlightPoint = null;
        if (mIsWhite) {
            highlightPoint = BitmapFactory.decodeResource(mContext.getResources(),
                    R.drawable.indicator_highlight_white);
        } else {
            highlightPoint = BitmapFactory.decodeResource(mContext.getResources(),
                    R.drawable.indicator_highlight);
        }
        
        int index = 0;
        while(index < mIndicatorNum) {
            Bitmap level = null;
            try {
                level = Bitmap.createBitmap(mWidth, mPointSize, Bitmap.Config.ARGB_4444);
            } catch (OutOfMemoryError error) {
            }
            if (level == null) {
                continue;
            }
            mCanvas.setBitmap(level);
            mCanvas.drawColor(Color.TRANSPARENT);
            
            int startPositionX = mPointSize / 2;
            int startPositionY = mPointSize / 2;
            int pointIndex = 0;
            while (pointIndex < mIndicatorNum) {
                if(index == pointIndex) {
                    mCanvas.drawBitmap(highlightPoint, startPositionX, startPositionY, mPainter);
                } else {
                    mCanvas.drawBitmap(normalPoint, startPositionX, startPositionY, mPainter);
                }
                startPositionX += mStepWidth;
                pointIndex++;
            }
            mCanvas.save();
            BitmapDrawable bmp = new BitmapDrawable(level);
            this.addLevel(index, index, bmp);
            index++;
        }
    }
}
