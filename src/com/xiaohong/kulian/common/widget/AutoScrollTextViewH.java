package com.xiaohong.kulian.common.widget;

import android.content.Context ;
import android.graphics.Canvas ;
import android.graphics.Color ;
import android.graphics.Paint ;
import android.os.Handler;
import android.os.Parcel ;
import android.os.Parcelable ;
import android.util.AttributeSet ;
import android.util.Log ;
import android.view.Display ;
import android.view.WindowManager ;
import android.widget.ImageView ;
import android.widget.TextView ;

public class AutoScrollTextViewH extends TextView {
    public final static String TAG = "AutoScrollTextViewH" ;

    private float mViewWidth = 0f ;
    public boolean isStarting = false ;// 是否开始滚动
    private Paint mPaint = null ;// 绘图样式
    private String mText = "" ;
    private float mTextLength;//文字总长度
    private float mStepFirstLine = 2.3f ;//每次左移的步长,控制移动速度
    private float mXCoordinateFirstLine = 0f ;
    private float mYCoordinate;//文字的y坐标
    private Handler mHandler = null;
   
    
    public void setTexts(String text) {
        super.setText(text);
        mText = text;
        Log.d(TAG, "drawFirstLine mText = " + mText);
    }
    
    public AutoScrollTextViewH(Context context) {
        super(context) ;
    }
    
    public AutoScrollTextViewH(Context context, AttributeSet attrs) {
        super(context, attrs) ;
    }
    
    public AutoScrollTextViewH(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle) ;
    }
    
    /**
     * Init必须在setText后调用
     * @param windowManager
     */
    public void init(WindowManager windowManager) {
        mHandler = new Handler();
        mPaint = getPaint() ;
        mPaint.setColor(Color.WHITE) ;
        mViewWidth = getWidth() ;
        WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        if (mViewWidth == 0) {
            if (wm != null) {
                Display display = windowManager.getDefaultDisplay() ;
                mViewWidth = display.getWidth();
            }
        }
        mTextLength = mPaint.measureText(mText);
        mYCoordinate = getTextSize() + getPaddingTop() ;
        mXCoordinateFirstLine = mViewWidth;
        Log.d(TAG, "mTextLength = " + mTextLength);
        Log.d(TAG, "mYCoordinate = " + mYCoordinate);
        Log.d(TAG, "mXCoordinateFirstLine = " + mXCoordinateFirstLine);
    }
    
    public void init(WindowManager windowManager, int lineNum) {
        init(windowManager);
    }
    
    public void init(WindowManager windowManager, ImageView centerView) {
        init(windowManager);
    }
    
    public void init(WindowManager windowManager, TextView centerView) {
        init(windowManager);
    }
    
    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState() ;
        SavedState ss = new SavedState(superState) ;
        
        ss.mStepFirstLine = mStepFirstLine ;
        ss.mXCoordinateFirstLine = mXCoordinateFirstLine ;
        ss.isStarting = isStarting ;
        
        return ss ;
        
    }
    
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state) ;
            return ;
        }
        SavedState ss = (SavedState) state ;
        super.onRestoreInstanceState(ss.getSuperState()) ;
        
        mStepFirstLine = ss.mStepFirstLine ;
        mXCoordinateFirstLine = ss.mXCoordinateFirstLine ;
        isStarting = ss.isStarting ;
    }
    
    public static class SavedState extends BaseSavedState {
        public boolean isStarting = false ;
        public float mStepFirstLine = 0.0f ;
        public float mXCoordinateFirstLine = 0.0f ;
        
        SavedState(Parcelable superState) {
            super(superState) ;
        }
        
        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags) ;
            out.writeBooleanArray(new boolean[] { isStarting }) ;
            out.writeFloat(mStepFirstLine) ;
            out.writeFloat(mXCoordinateFirstLine) ;
        }
        
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            
            public SavedState[] newArray(int size) {
                return new SavedState[size] ;
            }
            
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in) ;
            }
        } ;
        
        private SavedState(Parcel in) {
            super(in) ;
            boolean[] b = new boolean[1] ;
            in.readBooleanArray(b) ;
            if (b != null && b.length > 0)
                isStarting = b[0] ;
            mStepFirstLine = in.readFloat() ;
            mXCoordinateFirstLine = in.readFloat() ;
        }
    }
    
    public void startScroll() {
        isStarting = true ;
        invalidate() ;
        Log.d(TAG, "startScroll");
    }
    
    public void stopScroll() {
        isStarting = false ;
        invalidate() ;
    }
    
    @Override
    public void onDraw(Canvas canvas) {
        if (!isStarting) {
            Log.d(TAG, "not started");
            return ;
        }
        //Log.d(TAG, "started then draw");
        drawFirstLine(canvas) ;
        invalidate() ;
    }

    private void drawFirstLine(Canvas canvas) {
        mXCoordinateFirstLine = mXCoordinateFirstLine - mStepFirstLine ;
        canvas.drawText(mText, mXCoordinateFirstLine, mYCoordinate,
                mPaint) ;
        //mStepFirstLine += 0.3 ;
        //最右边的文字已经显示到最左边，应该进行下一轮显示
        if (mXCoordinateFirstLine < -mTextLength) {
            mXCoordinateFirstLine = getWidth();
            Log.d(TAG, "reset coordinate");
        }        
        /*Log.d(TAG, "drawFirstLine mXCoordinateFirstLine = " + mXCoordinateFirstLine);
        Log.d(TAG, "drawFirstLine mStepFirstLine = " + mStepFirstLine);
        Log.d(TAG, "drawFirstLine mText = " + mText);*/
    }

}