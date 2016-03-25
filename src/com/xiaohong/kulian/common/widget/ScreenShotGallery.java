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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * @author  andrew.wang
 * @date    2010-10-8
 * @since   Version 0.4.0
 */
public class ScreenShotGallery extends LinearLayout {

    private static final int INVALID_SCREEN = -1;
    private static final int TOUCH_STATE_REST = 0;
    private static final int TOUCH_STATE_SCROLLING = 1;
    /**
     * The velocity at which a fling gesture will cause us to snap to the next screen
     */
    private static final int SNAP_VELOCITY = 1000;
    
    private Scroller mScroller;
    // Distance a touch can wander before we think the user is scrolling in pixels
    private int mTouchSlop;
    // Minimum velocity to initiate a fling, as measured in pixels per second.
//    private int mMinimumVelocity;
    // Maximum velocity to initiate a fling, as measured in pixels per second.
    private int mMaximumVelocity;
    
    private float mLastMotionX;
    private float mLastMotionY;
    private int mCurrentScreen;
    private int mNextScreen = INVALID_SCREEN;
    private VelocityTracker mVelocityTracker;
    private int mTouchState = TOUCH_STATE_REST;
    private boolean mAllowLongPress;
    private GestureDetector mGestureDetector;
    
    public ScreenShotGallery(Context context) {
        this(context, null);
        requestDisallowInterceptTouchEvent(true);
    }
    
    public ScreenShotGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        Context localContext = getContext();
        this.mScroller = new Scroller(localContext);
        setFocusable(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setWillNotDraw(false);
        final ViewConfiguration configuration = ViewConfiguration.get(localContext);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

        mGestureDetector = new GestureDetector(localContext, new SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                float absX = Math.abs(velocityX);
                float absY = Math.abs(velocityY);
                if (absX > absY && absX > 400) {
                    if (velocityX > 0) {
                        ScreenShotGallery.this.scrollLeft();
                    } else {
                        ScreenShotGallery.this.scrollRight();
                    }
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            }
            
        });
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);

        final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        final int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == MeasureSpec.UNSPECIFIED 
                || heightSpecMode == MeasureSpec.UNSPECIFIED) {
            return;
        }
        
        final int count = getChildCount();
        if (count > 0) {
            
            final int width = widthSpecSize - getPaddingLeft() - getPaddingRight();
            final int height = heightSpecSize - getPaddingTop() - getPaddingBottom();
            
            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                
                int childWidthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                int childheightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
                
                child.measure(childWidthSpec, childheightSpec);
            }
        }
    }
    
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            super.scrollTo(mScroller.getCurrX(), getScrollY());
            postInvalidate();
        } else if (mNextScreen != INVALID_SCREEN) {
            mCurrentScreen = Math.max(0, Math.min(mNextScreen, getChildCount() - 1));
            mNextScreen = INVALID_SCREEN;
        }
    }
    
    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(true);
    }

    /*
     * This method JUST determines whether we want to intercept the motion.
     * If we return true, onTouchEvent will be called and we do the actual
     * scrolling there.
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        /*
         * Shortcut the most recurring case: the user is in the dragging
         * state and he is moving his finger.  We want to intercept this
         * motion.
         */
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
            return true;
        }

        final float x = ev.getX();
        final float y = ev.getY();
        
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                /*
                 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                 * whether the user has moved far enough from his original down touch.
                 */

                /*
                 * Locally do absolute value. mLastMotionX is set to the y value
                 * of the down event.
                 */
                final int xDiff = (int) Math.abs(x - mLastMotionX);
                final int yDiff = (int) Math.abs(y - mLastMotionY);

                final int touchSlop = mTouchSlop;
                boolean xMoved = xDiff > touchSlop;
                boolean yMoved = yDiff > touchSlop;
                
                if (xMoved || yMoved) {
                    
                    if (xMoved && (xDiff > yDiff)) {
                        // Scroll if the user moved far enough along the X axis
                        mTouchState = TOUCH_STATE_SCROLLING;
                    } 
//                    else if(yMoved) {
//                        // Do not scroll if the user moved far enough along the Y axis
//                        mTouchState = TOUCH_STATE_REST;
//                    }
//                    else {
//                        mTouchState = TOUCH_STATE_REST;
//                    }
                    // Either way, cancel any pending longpress
                    if (mAllowLongPress) {
                        mAllowLongPress = false;
                        // Try canceling the long press. It could also have been scheduled
                        // by a distant descendant, so use the mAllowLongPress flag to block
                        // everything
                        final View currentScreen = getChildAt(mCurrentScreen);
                        currentScreen.cancelLongPress();
                    }
                }
                break;

            case MotionEvent.ACTION_DOWN:
//                mHandler.sendEmptyMessage(HomeActivity.PAUSE_AUTO_FLOW);
                // Remember location of down touch
                mLastMotionX = x;
                mLastMotionY = y;
                mAllowLongPress = true;
         

                /*
                 * If being flinged and user touches the screen, initiate drag;
                 * otherwise don't.  mScroller.isFinished should be false when
                 * being flinged.
                 */
                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                // Release the drag
                mTouchState = TOUCH_STATE_REST;
                mAllowLongPress = false;
                break;
        }

        /*
         * The only time we want to intercept motion events is if we are in the
         * drag mode.
         */
        return mTouchState != TOUCH_STATE_REST;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        
        // Give everything to the gesture detector
        boolean retValue = mGestureDetector.onTouchEvent(event);

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        
        final int action = event.getAction();
        final float x = event.getX();
        
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            /*
             * If being flinged and user touches, stop the fling. isFinished
             * will be false if being flinged.
             */
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }

            // Remember where the motion event started
            mLastMotionX = x;
            break;
        case MotionEvent.ACTION_MOVE:
            if (mTouchState == TOUCH_STATE_SCROLLING) {
                
                // Scroll to follow the motion event
                final int deltaX = (int) (mLastMotionX - x);
                mLastMotionX = x;

                if (deltaX < 0) {
                    if (getScrollX() > 0) {
                        scrollBy(Math.max(-getScrollX(), deltaX), 0);
                    }
                } else if (deltaX > 0) {
                    final int availableToScroll = getWidth() * getChildCount() -
                    getScrollX() - getWidth();
                    if (availableToScroll > 0) {
                        scrollBy(Math.min(availableToScroll, deltaX), 0);
                    }
                }
            }
            mTouchState = TOUCH_STATE_SCROLLING;
            break;
        case MotionEvent.ACTION_UP:
            if (mTouchState == TOUCH_STATE_SCROLLING) {
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int velocityX = (int) velocityTracker.getXVelocity();

                if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
                    // Fling hard enough to move left
                    snapToScreen(mCurrentScreen - 1);
                } else if (velocityX < -SNAP_VELOCITY && mCurrentScreen < getChildCount() - 1) {
                    // Fling hard enough to move right
                    snapToScreen(mCurrentScreen + 1);
                } else {
                    snapToDestination();
                }

                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
            }
            mTouchState = TOUCH_STATE_REST;
            break;
        case MotionEvent.ACTION_CANCEL:
            mTouchState = TOUCH_STATE_REST;
            break;
        }
        return retValue;
    }

    private void snapToDestination() {
        
        final int screenWidth = getWidth();
        final int whichScreen = (getScrollX() + (screenWidth / 2)) / screenWidth;

        snapToScreen(whichScreen);
    }

    public void snapToScreen(int whichScreen) {
        
        if (!mScroller.isFinished()) return;

        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        boolean changingScreens = whichScreen != mCurrentScreen;
        
        mNextScreen = whichScreen;
        if(mScrollListener != null) {
            mScrollListener.onChanged(mNextScreen);
        }
        
        View focusedChild = getFocusedChild();
        if (focusedChild != null && changingScreens && focusedChild == getChildAt(mCurrentScreen)) {
            focusedChild.clearFocus();
        }
        
        final int newX = whichScreen * getWidth();
        final int delta = newX - getScrollX();
        mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta) * 2);
        invalidate();
    }
    
    public void scrollLeft() {
        if (mNextScreen == INVALID_SCREEN && mCurrentScreen > 0 && mScroller.isFinished()) {
            snapToScreen(mCurrentScreen - 1);
        }
    }

    public void scrollRight() {
        if (mNextScreen == INVALID_SCREEN && mCurrentScreen < getChildCount() -1 &&
                mScroller.isFinished()) {
            snapToScreen(mCurrentScreen + 1);
        }
    }
    
    public void snapToNextScreen() {
        int next = mCurrentScreen + 1;
        if (next != INVALID_SCREEN && next < getChildCount() && mScroller.isFinished()) {
            snapToScreen(next);
        } else {
            snapToScreen(0);
        }
    }
    
    public void addChild(LinearLayout child) {
        addView(child);
    }
    
    private PageScrollListener mScrollListener;
    public void setPageScrollListener(PageScrollListener listener) {
        mScrollListener = listener;
    }
    
    public interface PageScrollListener {
        void onChanged(int pageIndex);
    }
    
    public void clear() {
        final int length = getChildCount();
        for (int i = 0; i < length; i++) {
            LinearLayout parent = (LinearLayout) getChildAt(i);
            final int imageViewLength = parent.getChildCount();
            for (int j = 0; j < imageViewLength; j++) {
                ImageView v = (ImageView) parent.getChildAt(j);
                Drawable d = v.getDrawable();
                if (d != null) {
                    Bitmap bmp = ((BitmapDrawable) d).getBitmap();
                    if (bmp != null) {
                        bmp.recycle();
                    }
                    d.setCallback(null);
                }
                v.setImageDrawable(null);
            }
        }
        removeAllViews();
    }

}
