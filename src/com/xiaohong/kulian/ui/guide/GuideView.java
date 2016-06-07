package com.xiaohong.kulian.ui.guide;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.xiaohong.kulian.Session;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.TabWidget;

/**
 * Indicator
 * 
 * @author free
 *
 */
public class GuideView extends View implements OnPageChangeListener {
    private static final String TAG = "GuideView";
    private static final String STATUS_BAR_HEIGHT_RES_NAME = "status_bar_height";
    private static final String NAV_BAR_HEIGHT_RES_NAME = "navigation_bar_height";
    private static final String NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME = "navigation_bar_height_landscape";
    private static final String NAV_BAR_WIDTH_RES_NAME = "navigation_bar_width";
    private static final String SHOW_NAV_BAR_RES_NAME = "config_showNavigationBar";

    private static final int DOT_SPACE_DIVIDE = 3;
    /**
     * The relative size of height
     */
    private static final float DOT_Y_POSITION = 0.95f;

    private List<SinglePage> mGuideContent;

    private int mPosition;
    private float mOffset;

    private boolean mDrawDot;

    private List<SingleElement> mDotList;
    private int mDotXStart;
    private int mDotXPlus;
    private int mDotY;
    private SingleElement mSelectedDot;
    private Session mSession = null;

    /**
     * This class could not instantiation from XML
     * 
     * @param context
     * @param guideContent
     * @param dotSelected
     * @param dotDefault
     */
    public GuideView(Activity activity, List<SinglePage> guideContent,
            boolean drawDot, Bitmap dotDefault, Bitmap dotSelected) {
        super(activity);
        mSession = Session.get(activity.getApplicationContext());
        mGuideContent = guideContent;
        mDrawDot = drawDot;

        // Prepare dot element, if we have only one page, do not show dot
        if (guideContent != null && guideContent.size() > 1 && drawDot) {
            mDotList = new ArrayList<SingleElement>();

            // Just get a rough screen width/height
            DisplayMetrics dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
            Rect frame = new Rect();
            activity.getWindow().getDecorView()
                    .getWindowVisibleDisplayFrame(frame);
            /* int statusBarHeight = frame.top; */
            int statusBarHeight = getStatusBarHeight(activity.getResources());
            int mScreenWidth = dm.widthPixels;
            int mScreenHeight = dm.heightPixels - statusBarHeight;

            mDotXStart = mScreenWidth / DOT_SPACE_DIVIDE
                    - dotDefault.getWidth() / 2;
            mDotXPlus = mScreenWidth / DOT_SPACE_DIVIDE
                    / (guideContent.size() - 1);

            TabWidget tabWidget = mSession.getMainPageTabWidget();
            if (tabWidget != null) {

                // 依据tab的高度设置y坐标
                mDotY = mScreenHeight 
                        - tabWidget.getHeight() / 2;
                //Log.d(TAG, "mDotY = " + mDotY);
                //Log.d(TAG, "statusBarHeight = " + statusBarHeight);
                //Log.d(TAG, "tabWidget.getHeight() = " + tabWidget.getHeight());
            } else {
                mDotY = (int) (mScreenHeight * DOT_Y_POSITION);
            }

            for (int i = 0; i < guideContent.size(); i++) {
                SingleElement e = new SingleElement(mDotXStart + mDotXPlus * i,
                        mDotY, mDotXStart + mDotXPlus * i, mDotY, 1.0f, 1.0f,
                        dotDefault);
                mDotList.add(e);
            }
            mSelectedDot = new SingleElement(
                    mDotXStart + mDotXPlus * mPosition, mDotY, mDotXStart
                            + mDotXPlus * mPosition, mDotY, 1.0f, 1.0f,
                    dotSelected);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw dot
        if (mDrawDot) {

            // Draw default dot
            for (int i = 0; i < mDotList.size(); i++) {
                SingleElement e = mDotList.get(i);
                drawElement(canvas, e);
            }

            // Draw selected dot
            int x = mDotXStart + mDotXPlus * mPosition;
            mSelectedDot.xStart = x;
            mSelectedDot.xEnd = x;
            drawElement(canvas, mSelectedDot);
        }

        SinglePage singlePage = mGuideContent.get(mPosition);
        if (singlePage.mElementsList == null) {
            // No stuff
            super.onDraw(canvas);
            return;
        }

        // Draw custom stuff
        for (int i = 0; i < singlePage.mElementsList.size(); i++) {
            SingleElement e = singlePage.mElementsList.get(i);
            drawElement(canvas, e);
        }
        super.onDraw(canvas);
    }

    private void drawElement(Canvas canvas, SingleElement e) {
        Bitmap bitmap = e.contentBitmap;
        Matrix m = e.m;
        Paint p = e.p;

        float dx = e.xStart + (e.xEnd - e.xStart) * mOffset;
        float dy = e.yStart + (e.yEnd - e.yStart) * mOffset;
        float alpha = e.alphaStart + (e.alphaEnd - e.alphaStart) * mOffset;

        m.setTranslate(dx, dy);
        p.setAlpha((int) (0xFF * alpha));

        canvas.drawBitmap(bitmap, m, p);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPageScrolled(int index, float offset, int offsetPixel) {
        mPosition = index;
        mOffset = offset;
        invalidate();
    }

    @Override
    public void onPageSelected(int index) {
        // TODO Auto-generated method stub

    }

    @TargetApi(14)
    private int getNavigationBarHeight(Context context) {
        Resources res = context.getResources();
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (hasNavBar(context)) {
                String key;
                boolean inPortrait = (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
                if (inPortrait) {
                    key = NAV_BAR_HEIGHT_RES_NAME;
                } else {
                    key = NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME;
                }
                return getInternalDimensionSize(res, key);
            }
        }
        return result;
    }

    // 通过此方法获取navigation bar的宽度
    @TargetApi(14)
    private int getNavigationBarWidth(Context context) {
        Resources res = context.getResources();
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (hasNavBar(context)) {
                return getInternalDimensionSize(res, NAV_BAR_WIDTH_RES_NAME);
            }
        }
        return result;
    }

    // 通过此方法判断是否存在navigation bar
    @TargetApi(14)
    private boolean hasNavBar(Context context) {
        Resources res = context.getResources();
        int resourceId = res.getIdentifier(SHOW_NAV_BAR_RES_NAME, "bool",
                "android");
        if (resourceId != 0) {
            boolean hasNav = res.getBoolean(resourceId);
            // 查看是否有通过系统属性来控制navigation bar。
            if ("1".equals(getNavBarOverride())) {
                hasNav = false;
            } else if ("0".equals(getNavBarOverride())) {
                hasNav = true;
            }
            return hasNav;
        } else {
            // 可通过此方法来查看设备是否存在物理按键(menu,back,home键)。
            return !ViewConfiguration.get(context).hasPermanentMenuKey();
        }
    }

    // 通过此方法获取资源对应的像素值
    private int getInternalDimensionSize(Resources res, String key) {
        int result = 0;
        int resourceId = res.getIdentifier(key, "dimen", "android");
        if (resourceId > 0) {
            result = res.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    // 安卓系统允许修改系统的属性来控制navigation bar的显示和隐藏，此方法用来判断是否有修改过相关属性。
    // (修改系统文件，在build.prop最后加入qemu.hw.mainkeys=1即可隐藏navigation bar)
    // 相关属性模拟器中有使用。
    // 当返回值等于"1"表示隐藏navigation bar，等于"0"表示显示navigation bar。
    @TargetApi(19)
    private String getNavBarOverride() {
        String isNavBarOverride = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                Class c = Class.forName("android.os.SystemProperties");
                Method m = c.getDeclaredMethod("get", String.class);
                m.setAccessible(true);
                isNavBarOverride = (String) m.invoke(null, "qemu.hw.mainkeys");
            } catch (Throwable e) {
                isNavBarOverride = null;
            }
        }
        return isNavBarOverride;
    }

    private int getStatusBarHeight(Resources res) {
        return getInternalDimensionSize(res, STATUS_BAR_HEIGHT_RES_NAME);
    }

}
