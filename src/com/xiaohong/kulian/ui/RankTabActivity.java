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
package com.xiaohong.kulian.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.common.util.TopBar;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.widget.BaseTabActivity;

/**
 * the view is displaying for rank tab in home
 * 
 * @author cong.li
 * @date 2011-5-9
 * @since Version 0.7.0
 */

public class RankTabActivity extends BaseTabActivity implements OnTabChangeListener {

    private static final String TAG = "RankTabActivity";

    /** 排行榜100 */
    private static final int MAX_ITEMS = 100;
    private TabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //注册一个广播接收器，用于初始化tab的选取页
        registerReceivers();
        setContentView(R.layout.activity_rank);
//        getWindow().setBackgroundDrawableResource(android.R.color.darker_gray);
        initView();
//        initAdPager();  // banner
        Log.d(TAG, "onCreate");

    }

    /**
     * added by albert liu 2016/4/10
     */
    private void registerReceivers() {
        /**
         * Albert 2016/4/10注册查看所有的任务和推荐的广播
         */
        IntentFilter mCheckAllfilter = new IntentFilter();
        mCheckAllfilter.addAction(Constants.BROADCAST_CATEGORY_TASK);
        mCheckAllfilter.addAction(Constants.BROADCAST_CATEGORY_RCMD);
        registerReceiver(mCheckAllReceiver, mCheckAllfilter);
    }
    /**
     * added by albert liu 2016/4/10
     */
    private void unregisterReceiver() {
        unregisterReceiver(mCheckAllReceiver);
    }
    /**
     * added by albert liu 2016/4/10
     */
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterReceiver();
    }
    /**
     * added by albert liu 2016/4/10
     */
 // 打开推荐和任务详情
    private BroadcastReceiver mCheckAllReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.BROADCAST_CATEGORY_TASK)) {
                mTabHost.setCurrentTab(1);
            } else if (action.equals(Constants.BROADCAST_CATEGORY_RCMD)) {
                mTabHost.setCurrentTab(0);
            } 
        }
    };
    private void initView() {

        mTabHost = (TabHost) this.findViewById(android.R.id.tabhost);
        mTabHost.setup();
        //mTabHost.getTabWidget().setDividerDrawable(null);

        // do not show recommend tab
        /*Intent appIntent = new Intent(getApplicationContext(),
                ProductListActivity.class);
        appIntent.putExtra(Constants.EXTRA_CATEGORY, Constants.CATEGORY_RCMD);
        appIntent.putExtra(Constants.EXTRA_MAX_ITEMS, MAX_ITEMS);
        TabSpec tab1 = mTabHost
                .newTabSpec(Constants.CATEGORY_RCMD)
                .setIndicator(
                        Utils.createTabView(getApplicationContext(),
                                getString(R.string.rank_tab_rcmd)))
                .setContent(appIntent);
        mTabHost.addTab(tab1);*/

        Intent gameIntent = new Intent(getApplicationContext(),
                ProductListActivity.class);
        gameIntent.putExtra(Constants.EXTRA_CATEGORY, Constants.CATEGORY_RCMD);
        gameIntent.putExtra(Constants.EXTRA_MAX_ITEMS, MAX_ITEMS);
        TabSpec tab2 = mTabHost
                .newTabSpec(Constants.CATEGORY_APP)
                .setIndicator(
                        Utils.createMakeMoneyPageTabView(getApplicationContext(),
                                getString(R.string.rank_tab_app)))
                .setContent(gameIntent);
        mTabHost.addTab(tab2);

        // do not show game tab
        /*Intent bookIntent = new Intent(getApplicationContext(),
                ProductListActivity.class);
        bookIntent.putExtra(Constants.EXTRA_CATEGORY, Constants.CATEGORY_GAME);
        bookIntent.putExtra(Constants.EXTRA_MAX_ITEMS, MAX_ITEMS);
        TabSpec tab3 = mTabHost
                .newTabSpec(Constants.CATEGORY_GAME)
                .setIndicator(
                        Utils.createTabView(getApplicationContext(),
                                getString(R.string.rank_tab_game)))
                .setContent(bookIntent);
        mTabHost.addTab(tab3);*/

        Intent growIntent = new Intent(getApplicationContext(),
                TaskListActivity.class);
        growIntent.putExtra(Constants.EXTRA_CATEGORY, Constants.CATEGORY_TASK);
        growIntent.putExtra(Constants.EXTRA_MAX_ITEMS, MAX_ITEMS);
        TabSpec tab4 = mTabHost
                .newTabSpec(Constants.CATEGORY_TASK)
                .setIndicator(
                        Utils.createMakeMoneyPageTabView(getApplicationContext(),
                                getString(R.string.rank_tab_task)))
                .setContent(growIntent);
        mTabHost.addTab(tab4);
        mTabHost.setOnTabChangedListener(this);
        changeTabStyle();
    }

    private void changeTabStyle() {
        TabWidget tabWidget = mTabHost.getTabWidget();
        for (int i = 0; i < tabWidget.getChildCount(); i++) {
            View view = tabWidget.getChildAt(i);
            //view.getLayoutParams().height = 130;
            //view.getLayoutParams().width = 65;
            
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return getParent().onKeyDown(keyCode, event);
    }

    @Override
    public void onTabChanged(String tabId) {
        System.out.println("onTabChanged"+tabId);
        if (Constants.CATEGORY_APP.equals(tabId)) {
            Utils.trackEvent(getApplicationContext(), Constants.GROUP_6, Constants.CLICK_RANK_APP);
        } else if (Constants.CATEGORY_GAME.equals(tabId)) {
            Utils.trackEvent(getApplicationContext(), Constants.GROUP_6, Constants.CLICK_RANK_GAME);
        } else if (Constants.CATEGORY_EBOOK.equals(tabId)) {
            Utils.trackEvent(getApplicationContext(), Constants.GROUP_6, Constants.CLICK_RANK_BOOK);
        } else if (Constants.CATEGORY_GROW.equals(tabId)) {
            Utils.trackEvent(getApplicationContext(), Constants.GROUP_6, Constants.CLICK_RANK_POP);
        }
    }
    
    //to support banner
    private ViewPager mAdPager = null;//定义pager

    private int mCurrentAdPageIndex = 1 ;//用于记录当前page的页码

    private ImageView[] mAdIndicatorImageViews = null ;//用于标记当前是在哪个页面的图标，这里用五角星，高亮表示选中

    private Handler mUiHandler = null ; //用于实现timer

    private AdItemClickListener mAdItemClickListener = null ;//点击事件监听器

    private void initAdPager() {
        
        mUiHandler = new Handler();

        ViewGroup group =(ViewGroup) findViewById(R.id.viewGroup) ;

        mAdItemClickListener = new AdItemClickListener() ;

        //存放的是三张广告背景img1 img2 img3

        List<View> advPics = new ArrayList<View>() ;

        ImageView img1 = new ImageView(this) ;

        img1.setBackgroundResource(R.drawable.push) ;

        img1.setOnClickListener(mAdItemClickListener) ;

        img1.setTag("http://www.baidu.com") ;

        ImageView img2 = new ImageView(this) ;

        img2.setBackgroundResource(R.drawable.push) ;

        img2.setOnClickListener(mAdItemClickListener) ;

        img2.setTag("http://www.163.com") ;

       

        ImageView img3 = new ImageView(this) ;

        img3.setBackgroundResource(R.drawable.gallery) ;

        img3.setOnClickListener(mAdItemClickListener) ;

        img3.setTag("http://www.qq.com") ;

        // img1 img2 img3 循环滑动

        // img0 img1img2 img3 img4

        ImageView img0 = new ImageView(this) ;

        ImageView img4 = new ImageView(this) ;

        advPics.add(img0) ;
        advPics.add(img1) ;
        advPics.add(img2) ;
        advPics.add(img3) ;
        advPics.add(img4) ;

        // 对imageviews进行填充
        mAdIndicatorImageViews = new ImageView[advPics.size() - 2] ;

        // 小图标
        for (int i = 0; i < advPics.size() -2; i++) {
            ImageView imageView = new ImageView(this) ;
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(20,20);

            lp.setMargins(0,0, 40, 0);//modify 40 to a suitable value

            imageView.setLayoutParams(lp) ;
            mAdIndicatorImageViews[i] = imageView ;
            if (i == 0) {

                mAdIndicatorImageViews[i]
                       .setBackgroundResource(R.drawable.banner_ad_selected) ;

            } else {

                mAdIndicatorImageViews[i]
                       .setBackgroundResource(R.drawable.banner_ad_selected) ;

            }

            group.addView(mAdIndicatorImageViews[i]) ;

        }

        mAdPager =(ViewPager) findViewById(R.id.adv_pager) ;

        mAdPager.setOffscreenPageLimit(5);

        mAdPager.setAdapter(new AdvAdapter(advPics)) ;

        mAdPager.setOnPageChangeListener(new AdPageChangeListener()) ;

        mAdPager.setOnTouchListener(new OnTouchListener() {

            @Override

            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){

                    case MotionEvent.ACTION_DOWN:

                    case MotionEvent.ACTION_MOVE:

                        //Log.d(TAG,"onTouch down or move") ;

                       pauseShowNextAdPageDelay() ;

                        break ;

                    case MotionEvent.ACTION_UP:

                        showNextAdPageDelay() ;

                        //Log.d(TAG,"onTouch up") ;

                        break ;

                    default:

                        break ;

                }

                return false ;

            }

        }) ;

        mAdPager.setCurrentItem(mCurrentAdPageIndex, false) ;

        showNextAdPageDelay() ;

       

    }

   

    private Runnable mShowAdNextRunnable = new Runnable() {

        @Override

        public void run() {

            int index = ++mCurrentAdPageIndex % (mAdIndicatorImageViews.length + 1) ;

            if(index == 0) {

                mCurrentAdPageIndex = 1;

                index = 1;

            }

            mAdPager.setCurrentItem(index, false) ;

            showNextAdPageDelay() ;

        }

    } ;

   

    private void showNextAdPageDelay() {

        mUiHandler.postDelayed(mShowAdNextRunnable, 4500) ;

    }

   

    private void pauseShowNextAdPageDelay() {

        mUiHandler.removeCallbacks(mShowAdNextRunnable) ;

    }

   

    private class AdItemClickListener implements OnClickListener {
        @Override

        public void onClick(View v) {

            Object tag = v.getTag();

            Log.d(TAG, "onClick()") ;

            if (!(tag instanceof String)){

                Log.d(TAG, "tagis not a string") ;

                return ;

            }

            String url =(String) tag ;

            if ("".equals(url)) {

                Log.d(TAG, "urlis empty") ;

                return ;

            }

            openBrowserWithUrl(url) ;

        }

       

    }

   

    private void openBrowserWithUrl(String url) {

        Intent intent = new Intent(Intent.ACTION_VIEW) ;

        intent.setData(Uri.parse(url)) ;

        startActivity(intent) ;

    }

   

    private final class AdPageChangeListener implements OnPageChangeListener{

        boolean mIsAutoScrolled = false ;

       

        @Override

        public void onPageScrollStateChanged(int arg0) {

            Log.d(TAG, "onPageScrollStateChangedarg0 = " + arg0 + ", currentIdex = " + mAdPager.getCurrentItem());

            switch (arg0) {

                case ViewPager.SCROLL_STATE_DRAGGING:// 手势滑动

                    mIsAutoScrolled = false ;

                    break ;

                case ViewPager.SCROLL_STATE_SETTLING:// 界面切换

                    mIsAutoScrolled = true ;

                    break ;

                case ViewPager.SCROLL_STATE_IDLE:

                    if (mAdPager.getCurrentItem()== mAdPager.getAdapter()

                            .getCount() - 2&& !mIsAutoScrolled) {

                        mAdPager.setCurrentItem(1);

                        return ;

                    }

                    if (mAdPager.getCurrentItem()== 1 && !mIsAutoScrolled) {

                        mAdPager.setCurrentItem(mAdPager.getAdapter()

                                .getCount() -2) ;

                        return ;

                    }

                    break ;

                default:

                    break ;

            }

        }

       

        @Override

        public void onPageScrolled(int arg0, float arg1, int arg2) {

           

        }

       

        @Override

        public void onPageSelected(int arg0) {

            // why this function called frequently??
//            Log.d(TAG, "onPageSelected: " + arg0);

            for (int i = 0; i < mAdIndicatorImageViews.length; i++) {

                mAdIndicatorImageViews[i]

                       .setBackgroundResource(R.drawable.banner_ad_selected_not) ;

            }

            if(arg0 > mAdIndicatorImageViews.length) {

//                Log.d(TAG, "setCurrentItem" + 1);

                mAdPager.setCurrentItem(1,false) ;

                mAdIndicatorImageViews[1 - 1]

                       .setBackgroundResource(R.drawable.banner_ad_selected) ;

            }else if(arg0 < 1) {

                Log.d(TAG, "setCurrentItem" + mAdIndicatorImageViews.length);

                mAdPager.setCurrentItem(mAdIndicatorImageViews.length, false) ;

                mAdIndicatorImageViews[mAdIndicatorImageViews.length - 1]

                       .setBackgroundResource(R.drawable.banner_ad_selected) ;

            }else {

                Log.d(TAG, "setCurrentItem" + arg0);

                //mAdPager.setCurrentItem(arg0,false) ;

                mAdIndicatorImageViews[arg0 - 1]

                       .setBackgroundResource(R.drawable.banner_ad_selected) ;

            }

        }

       

    }

   

    private final class AdvAdapter extends PagerAdapter {

        private List<View> views = null ;

       

        public AdvAdapter(List<View> views) {

            this.views = views ;

        }

       

        @Override

        public void destroyItem(View arg0, int arg1, Object arg2) {

            ((ViewPager) arg0).removeView(views.get(arg1)) ;

        }

       

        @Override

        public void finishUpdate(View arg0) {

           

        }

       

        @Override

        public int getCount() {

            return views.size() ;

        }

       

        @Override

        public Object instantiateItem(View arg0, int arg1) {

            Log.d(TAG, "instantiateItem:" + arg1);

            //((ViewPager)arg0).removeView(views.get(arg1)) ;

            ((ViewPager) arg0).addView(views.get(arg1), 0) ;

            return views.get(arg1) ;

        }

       

        @Override

        public boolean isViewFromObject(View arg0, Object arg1) {

            return arg0 == arg1 ;

        }

       

        @Override

        public void restoreState(Parcelable arg0, ClassLoader arg1) {

           

        }

       

        @Override

        public Parcelable saveState() {

            return null ;

        }

       

        @Override

        public void startUpdate(View arg0) {

           

        }

    }
}
