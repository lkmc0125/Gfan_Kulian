package com.xiaohong.kulian.common.widget;

import com.xiaohong.kulian.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ProgressBar;

public class CustomProgressBar extends ProgressBar {

    public enum Status {
        INITIAL,
        PROCESSING,
        PAUSED,
        FINISHED,
        INSTALLED
    };

    private String mText;
    private Paint mPaint;
    private Rect mRect;
    private Context mContext;

    public void setStatus(Status status) {
        switch (status) {
        case INITIAL:
        {
            this.mPaint.setColor(getResources().getColor(R.color.white));
            int color = mContext.getResources().getColor(
                    R.color.download_button_blue_color);
            ClipDrawable d = new ClipDrawable(new ColorDrawable(color), Gravity.LEFT, ClipDrawable.HORIZONTAL);
            setProgressDrawable(d);
            setProgress(100);
            break;
        }
        case PROCESSING:
        {
            this.mPaint.setColor(getResources().getColor(R.color.white));
            int color = mContext.getResources().getColor(
                    R.color.download_button_blue_color);
            ClipDrawable d = new ClipDrawable(new ColorDrawable(color), Gravity.LEFT, ClipDrawable.HORIZONTAL);
            setProgressDrawable(d);
            setProgress(0);
            break;
        }
        case PAUSED:
        {
            this.mPaint.setColor(getResources().getColor(R.color.white));
            int color = mContext.getResources().getColor(
                    R.color.download_button_yellow_color);
            ClipDrawable d = new ClipDrawable(new ColorDrawable(color), Gravity.LEFT, ClipDrawable.HORIZONTAL);
            setProgressDrawable(d);
            setProgress(100);
            break;
        }
        case FINISHED:
        {
            this.mPaint.setColor(getResources().getColor(R.color.white));
            int color = mContext.getResources().getColor(
                    R.color.download_button_yellow_color);
            ClipDrawable d = new ClipDrawable(new ColorDrawable(color), Gravity.LEFT, ClipDrawable.HORIZONTAL);
            setProgressDrawable(d);
            setProgress(100);
            break;
        }
        case INSTALLED:
        {
            this.mPaint.setColor(getResources().getColor(R.color.white));
            int color = mContext.getResources().getColor(
                    R.color.download_button_green_color);
            ClipDrawable d = new ClipDrawable(new ColorDrawable(color), Gravity.LEFT, ClipDrawable.HORIZONTAL);
            setProgressDrawable(d);
            setProgress(100);
            break;
        }
        default:
            break;
        }
        invalidate();
        Log.d("free", "setStatus status = " + status);
    }

    public CustomProgressBar(Context context) {
        super(context);
        if (isInEditMode()) { return; }
        mContext = context;
        System.out.println("1");
        initText();
    }

    public CustomProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (isInEditMode()) { return; }
        mContext = context;
        System.out.println("2");
        initText();
    }

    public CustomProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) { return; }
        mContext = context;
        System.out.println("3");
        initText();
    }

    @Override
    public synchronized void setProgress(int progress) {
        super.setProgress(progress);
    }

    public void setText(String text) {
        mText = text;
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInEditMode()) { return; }
        this.mPaint.getTextBounds(mText, 0, mText.length(), mRect);
        int x = (getWidth() / 2) - mRect.centerX();
        int y = (getHeight() / 2) - mRect.centerY();
        canvas.drawText(mText, x, y, this.mPaint);
        Log.d("free", "CustomProgressBar onDraw progress:" + getProgress());
    }

    private void initText() {
        Log.d("free", "CustomProgressBar initText");
        this.mRect = new Rect();
        this.mPaint = new Paint();
        float ratio = getFontSizeRatio();
        this.mPaint.setTextSize(14 * ratio);
        this.mText = "";
    }

    private void setText(int progress) {
        int i = (progress * 100) / this.getMax();
        this.mText = String.valueOf(i) + "%";
    }

    private float getFontSizeRatio() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        float ratioWidth = (float) screenWidth / 320;
        return ratioWidth;
    }
}
