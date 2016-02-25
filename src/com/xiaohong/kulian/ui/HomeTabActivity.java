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
package com.xiaohong.kulian.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import org.apache.http.HttpHost;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.igexin.sdk.PushManager;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.common.HttpClientFactory;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.ResponseCacheManager;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.download.DownloadManager;
import com.xiaohong.kulian.common.util.CacheManager;
import com.xiaohong.kulian.common.util.DBUtils;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.vo.UpdateInfo;
import com.xiaohong.kulian.common.widget.BaseTabActivity;

/**
 * Home Tab Activity : 机锋市场的入口页面
 * 
 * @author Andrew
 * @date 2011-5-9
 * @since Version 0.7.0
 */
public class HomeTabActivity extends BaseTabActivity implements ApiRequestListener,
        OnTabChangeListener, Observer {

    // private static final String TAG = "HomeTabActivity";

    // exit menu dialog
    private static final int DIALOG_EXIT = 1;
    private static final int DIALOG_OPT_UPDATE = 2;
    private static final int DIALOG_FORCE_UPDATE = 3;

    // Tab id
    private static final String TAB_HOME = "home";
    private static final String TAB_CATEGORY = "category";
    private static final String TAB_HUMOR = "humor";
    private static final String TAB_MINE = "mine";
    private static final String TAB_RANK = "rank";
    private static final String TAB_APP = " app";

    private TabHost mTabHost;
    private ImageView mMover;
    private int mStartX;
    private boolean mIsAnimationReady;
    private int mUpdateCounter;

    // 检查用户是否切换CMWAP网络
    private BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            HttpHost proxy = Utils.detectProxy(getApplicationContext());
            if (proxy != null) {
                HttpClientFactory.get().getHttpClient().useProxyConnection(proxy);
            }
        }
    };
    
    // 检查用户是否进行了应用的（安装，卸载，更新）操作
    private BroadcastReceiver mInstallReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            final String packageName = intent.getData().getSchemeSpecificPart();

            if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {

                boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
                mSession.getDownloadManager().completeInstallation(packageName);
                if (replacing) {
                    // 升级
                    mSession.setUpgradeNumber(mSession.getUpgradeNumber() - 1);
                    DBUtils.removeUpgradable(getApplicationContext(), packageName);
                } else {
                    mSession.addInstalledApp(packageName);
                }
                mSession.getDownloadingList().remove(packageName);

            } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {

                mSession.removeInstalledApp(packageName);
                DBUtils.removeUpgradable(getApplicationContext(), packageName);
            }
        }
    };
    
    // 检查用户点击下载列表
    private BroadcastReceiver mIntentClickReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            mTabHost.setCurrentTabByTag(TAB_APP);
        }
    };
    
    // 检查机锋市场版本更新
    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.BROADCAST_FORCE_EXIT)) {
                // 强制退出机锋市场
//                exit();//jerry注释，取消强制退出
            } else if (action.equals(Constants.BROADCAST_REMIND_LATTER)) {
                // do nothing
            } else if (action.equals(Constants.BROADCAST_DOWNLOAD_OPT)) {
                // start download
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mSession
                        .getUpdateUri()));
                request.setPackageName(mSession.getPackageName());
                request.setTitle(mSession.getAppName());
                request.setShowRunningNotification(true);
                request.setSourceType(com.xiaohong.kulian.common.download.Constants.DOWNLOAD_FROM_OTA);
                request.setMimeType(com.xiaohong.kulian.common.download.Constants.MIMETYPE_APK);
                mSession.setUpdateID(mSession.getDownloadManager().enqueue(request));
            } else if (action.equals(Constants.BROADCAST_DOWNLOAD)) {
                // start download
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mSession
                        .getUpdateUri()));
                request.setPackageName(mSession.getPackageName());
                request.setTitle(mSession.getAppName());
                request.setShowRunningNotification(true);
                request.setSourceType(com.xiaohong.kulian.common.download.Constants.DOWNLOAD_FROM_OTA);
                request.setMimeType(com.xiaohong.kulian.common.download.Constants.MIMETYPE_APK);
                mSession.setUpdateID(mSession.getDownloadManager().enqueue(request));
                HomeTabActivity.this.finish();
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 接受更新提醒
        mSession.addObserver(this);
        registerReceivers();
        
        setContentView(R.layout.activity_home_tab_main);
        
        @SuppressWarnings("unchecked")
        HashMap<String, Object> item = (HashMap<String, Object>) getIntent().getSerializableExtra(
                Constants.EXTRA_HOME_DATA);
        initView(item);

        // getui
        PushManager.getInstance().initialize(this.getApplicationContext());

        // 网络正常，开始检查版本更新
        MarketAPI.checkUpdate(getApplicationContext(), this);
        checkNewSplash();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
        mIsAnimationReady = false;
    }
    
    private void registerReceivers() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkReceiver, filter);
        IntentFilter intentClickFilter = new IntentFilter(Constants.BROADCAST_CLICK_INTENT);
        registerReceiver(mIntentClickReceiver, intentClickFilter);
        
        IntentFilter appFilter = new IntentFilter();
        appFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        appFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        appFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        appFilter.addDataScheme("package");
        registerReceiver(mInstallReceiver, appFilter);
        
        IntentFilter updatefilter = new IntentFilter();
        updatefilter.addAction(Constants.BROADCAST_FORCE_EXIT);
        updatefilter.addAction(Constants.BROADCAST_REMIND_LATTER);
        updatefilter.addAction(Constants.BROADCAST_DOWNLOAD_OPT);
        updatefilter.addAction(Constants.BROADCAST_DOWNLOAD);
        registerReceiver(mUpdateReceiver, updatefilter);
    }
    
    private void unregisterReceiver() {
        unregisterReceiver(mNetworkReceiver);
        unregisterReceiver(mIntentClickReceiver);
        unregisterReceiver(mInstallReceiver);
        unregisterReceiver(mUpdateReceiver);
    }

    /*
     * 初始化Tab Host 包含四个Tab（首页、分类、排行、应用管理）
     */
    private void initView(HashMap<String, Object> item) {

        mTabHost = (TabHost) this.findViewById(android.R.id.tabhost);
        mTabHost.setup();

        Intent i = new Intent(this, ConnectionActivity.class);
        i.putExtra(Constants.EXTRA_HOME_DATA, item);
        TabSpec tab1 = mTabHost
                .newTabSpec(TAB_HOME)
                .setIndicator(
                        createTabView(getApplicationContext(), getString(R.string.main_tab_wifi),
                                R.drawable.main_tab_wifi_selector))
                .setContent(i);
        mTabHost.addTab(tab1);

        TabSpec tab2 = mTabHost
                .newTabSpec(TAB_RANK)
                .setIndicator(
                        createTabView(getApplicationContext(), getString(R.string.main_tab_earn),
                                R.drawable.main_tab_earn_selector))
                .setContent(new Intent(this, RankTabActivity.class));
        mTabHost.addTab(tab2);

        TabSpec tab3 = mTabHost
                .newTabSpec(TAB_HUMOR)
                .setIndicator(
                        createTabView(getApplicationContext(), getString(R.string.main_tab_humor),
                                R.drawable.main_tab_humor_selector))
                .setContent(new Intent(this, HumorActivity.class));
        mTabHost.addTab(tab3);

        TabSpec tab4 = mTabHost
                .newTabSpec(TAB_MINE)
                .setIndicator(
                        createTabView(getApplicationContext(), getString(R.string.main_tab_mine), 
                                R.drawable.main_tab_mine_selector))
                .setContent(new Intent(this, PersonalAccountActivity.class));
        mTabHost.addTab(tab4);

        mTabHost.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {

                if (!mIsAnimationReady) {
                    initTabAnimationParameter();
                }
                return true;
            }
        });
        mTabHost.setOnTabChangedListener(this);
    }

    @Override
    public void onTabChanged(String tabId) {

        final View tab = getTabHost().getCurrentTabView();
        final int endX = tab.getLeft();

        final TranslateAnimation animation = new TranslateAnimation(mStartX, endX, 0, 0);
        animation.setDuration(200);
        animation.setFillAfter(true);
        if (mMover == null) {
            initTabAnimationParameter();
        }
        mMover.startAnimation(animation);
        mStartX = endX;
    }

    /*
     * 初始化Tab Widget动画参数，获取每个Widget的宽度
     */
    private void initTabAnimationParameter() {
        mIsAnimationReady = true;
        int mWidth = mTabHost.getCurrentTabView().getWidth();
        int tabHeight = mTabHost.getCurrentTabView().getHeight();
        mMover = (ImageView) findViewById(R.id.iv_mover);
        mMover.setLayoutParams(new FrameLayout.LayoutParams(mWidth, tabHeight));
        
        if (!Utils.isNetworkAvailable(getApplicationContext())) {
            // 如果网络无法连接，直接显示APP管理页
            mTabHost.setCurrentTabByTag(TAB_APP);
        }
    }

    /*
     * 创建TabWidgetView
     */
    private View createTabView(Context context, String text, int imageResource) {

        View view = LayoutInflater.from(context).inflate(R.layout.activity_home_tab_view, null);

        ImageView bg;
        if (imageResource == -1) {
            bg = (ImageView) view.findViewById(R.id.tab_widget_icon);
            if (mSession.getUpgradeNumber() > 0) {
                drawUpdateCount(this, getResources(), bg, true);
            } else {
                bg.setImageResource(R.drawable.main_tab_app_manager_selector);
            }
        } else {
            bg = (ImageView) view.findViewById(R.id.tab_widget_icon);
            bg.setImageResource(imageResource);
        }
        TextView textview = (TextView) view.findViewById(R.id.tab_widget_content);
        textview.setText(text);
        return view;
    }

    /*
     * 更新应用可更新数
     */
    private void drawUpdateCount(Activity context, Resources res, ImageView view,
            boolean flag) {
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        Bitmap cornerRes = BitmapFactory.decodeResource(res, R.drawable.notify_update);
        Bitmap appBitmapNormal = 
                BitmapFactory.decodeResource(res, R.drawable.main_tab_app_unselect);
        Bitmap appBitmapPressed = BitmapFactory.decodeResource(res, R.drawable.main_tab_app_select);

        StateListDrawable stateDrawable = new StateListDrawable();
        int stateSelected = android.R.attr.state_selected;
        if (flag) {
            Bitmap cornerBitmap = drawText(dm, res, cornerRes, mSession.getUpgradeNumber());
            Bitmap newBitmapNormal = drawBitmap(dm, appBitmapNormal, cornerBitmap);
            Bitmap newBitmapPressed = drawBitmap(dm, appBitmapPressed, cornerBitmap);
            
            stateDrawable.addState(new int[] { -stateSelected }, new BitmapDrawable(res,
                    newBitmapNormal));
            stateDrawable.addState(new int[] { stateSelected }, new BitmapDrawable(res,
                    newBitmapPressed));
            
            view.setImageDrawable(stateDrawable);
        } else {
            
            view.setImageResource(R.drawable.main_tab_app_manager_selector);
        }
    }

    /*
     * 绘出背景图
     */
    private Bitmap drawBitmap(DisplayMetrics dm, Bitmap background, Bitmap corner) {
        Canvas canvas = new Canvas();
        final int height = background.getScaledHeight(dm);
        final int width = background.getScaledWidth(dm);
        Bitmap smallBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(smallBitmap);
        Paint textPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
        canvas.drawBitmap(background, 0, 0, textPainter);
        textPainter.setXfermode(new PorterDuffXfermode(Mode.SRC_OVER));
        canvas.drawBitmap(corner, width - corner.getScaledWidth(dm), 0, textPainter);
        canvas.save();
        return smallBitmap;
    }

    /*
     * 绘出更新数字
     */
    private Bitmap drawText(DisplayMetrics dm, Resources res, Bitmap bm, int num) {
        final int height = bm.getScaledHeight(dm);
        final int width = bm.getScaledWidth(dm);
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(bm, new Matrix(), new Paint());
        Paint textPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPainter.setColor(res.getColor(R.color.tab_app_num));
        textPainter.setTextSize(dm.scaledDensity * 12);
        textPainter.setTypeface(Typeface.DEFAULT_BOLD);
        float textWidth = textPainter.measureText(String.valueOf(num)) / 2;
        canvas.drawText(String.valueOf(num), width / 2 - textWidth, height / 2
                + (dm.scaledDensity * 6), textPainter);
        canvas.save();
        return newBitmap;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.common_menus, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        switch (item.getItemId()) {

        case R.id.menu_setting:
            Utils.trackEvent(getApplicationContext(), Constants.GROUP_8,
                    Constants.MENU_CLICK_SETTINGS);
            intent.setClass(getApplicationContext(),
                    ClientPreferenceActivity.class);
            startActivity(intent);
            break;
            
        case R.id.menu_exit:
            if (!isFinishing()) {
                showDialog(DIALOG_EXIT);
            }
            break;
            
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            if (!isFinishing()) {
                showDialog(DIALOG_EXIT);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    protected Dialog onCreateDialog(final int id) {
        switch (id) {
        case DIALOG_EXIT:
            return new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle(getString(R.string.exit_wifikulian))
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // 退出机锋市场
                            exit();
                        }
                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            try {
                                HomeTabActivity.this.dismissDialog(id);
                            } catch (IllegalArgumentException e) {
                            }
                        }
                    }).create();
        
        case DIALOG_OPT_UPDATE:
            
            String optVersionName = mSession.getUpdateVersionName();
            String optUpdateDesc = mSession.getUpdateVersionDesc();
            return new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle(R.string.find_new_version)
                    .setMessage(getString(R.string.update_prompt, optVersionName) + optUpdateDesc)
                    .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (checkDownload()) {
                                sendBroadcast(new Intent(
                                        Constants.BROADCAST_FORCE_EXIT));
                            } else {
                                sendBroadcast(new Intent(
                                        Constants.BROADCAST_DOWNLOAD_OPT));
                            }
                            removeDialog(id);
                        }
                    })
                    .setNegativeButton(R.string.btn_next_time, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            sendBroadcast(new Intent(Constants.BROADCAST_REMIND_LATTER));
                            removeDialog(id);
                        }
                    }).create();
            
        case DIALOG_FORCE_UPDATE:
            
            String forceVersionName = mSession.getUpdateVersionName();
            String forceUpdateDesc = mSession.getUpdateVersionDesc();
            return new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle(R.string.find_new_version)
                    .setMessage(
                            getString(R.string.update_prompt_stronger, forceVersionName)
                                    + forceUpdateDesc)
                    .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (checkDownload()) {
                                sendBroadcast(new Intent(
                                        Constants.BROADCAST_FORCE_EXIT));
                            } else {
                                sendBroadcast(new Intent(Constants.BROADCAST_DOWNLOAD));
                            }
                            removeDialog(id);
                        }
                    })
                    .setNegativeButton(R.string.btn_exit, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            sendBroadcast(new Intent(Constants.BROADCAST_FORCE_EXIT));
                            removeDialog(id);
                        }
                    }).create();

        default:
            break;
        }
        return super.onCreateDialog(id);
    }

    @Override
    public void update(Observable arg0, Object arg1) {

        if (arg1 instanceof Integer) {
            if (Constants.INFO_UPDATE == (Integer) arg1) {
                // 刷新可更新应用数
                int counter = mSession.getUpgradeNumber();
                if (counter > 0 && counter != mUpdateCounter) {
                    mUpdateCounter = counter;
                    View v = getTabHost().getTabWidget().getChildTabViewAt(3);
                    drawUpdateCount(this, getResources(),
                            (ImageView) (v.findViewById(R.id.tab_widget_icon)), true);
                } else if (counter == 0) {
                    View v = getTabHost().getTabWidget().getChildTabViewAt(3);
                    ((ImageView) v.findViewById(R.id.tab_widget_icon))
                            .setImageResource(R.drawable.main_tab_app_manager_selector);
                }
            }
        }
    }
    
    /*
     * 检查是否有新的Splash
     */
    private void checkNewSplash() {
        sendBroadcast(new Intent(Constants.BROADCAST_SPLASH_CHECK_UPGRADE));
    }
    
    @Override
    public void onSuccess(int method, Object obj) {
        if (method == MarketAPI.ACTION_CHECK_NEW_VERSION) {
            handleUpdate((UpdateInfo) obj);
        }
    }

    @Override
    public void onError(int method, int statusCode) {
        if (method == MarketAPI.ACTION_CHECK_NEW_VERSION) {
            Utils.D("check new version fail because of status : " + statusCode);
        }
    }
    
    /*
     * 检查机锋市场的更新
     */
    private void handleUpdate(UpdateInfo info) {
        int updateLevel = info.getUpdageLevel();
        if (Constants.NO_UPDATE == updateLevel) {
            // no update here
            mSession.setUpdateAvailable(false);
            return;
        }

        // update the info to local memory
        mSession.setUpdateInfo(info.getVersionName(), info.getVersionCode(), info.getDescription(),
                info.getApkUrl(), updateLevel);

        // 有可用升级
        if (Constants.FORCE_UPDATE == updateLevel) {
            showDialog(DIALOG_FORCE_UPDATE);
        } else if (Constants.SUGGEST_UPDATE == updateLevel) {
            showDialog(DIALOG_OPT_UPDATE);
        }
    }
    
    private boolean checkDownload() {
        Cursor cursor = mSession.getDownloadManager().query(
                new DownloadManager.Query().setFilterById(mSession.getUpdateId()));

        if (cursor != null && cursor.moveToFirst()) {
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.Impl.COLUMN_STATUS));
            if (DownloadManager.Impl.STATUS_SUCCESS == status) {
                String fileSrc = cursor.getString(cursor
                        .getColumnIndex(DownloadManager.Impl.COLUMN_DATA));
                Uri uri = Uri.parse(fileSrc);
                File file = new File(uri.getPath());
                if (!file.exists()) {
                    return false;
                }

                File root = new File(Environment.getExternalStorageDirectory(),
                        com.xiaohong.kulian.Constants.IMAGE_CACHE_DIR);
                root.mkdirs();
                File output = new File(root, "aMarket.apk");
                if (!output.exists()) {
                    try {
                        Utils.copyFile(new FileInputStream(file), new FileOutputStream(output));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        return false;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                Utils.installApk(getApplicationContext(), output);
                return true;
            }
        }
        return false;
    }
    
    /**
     * 退出机锋市场<br>
     * 进行必要的资源回收工作
     */
    private void exit() {
        // 清除所有API缓存
        ResponseCacheManager.getInstance().clear();
        // 清除所有图片缓存
        CacheManager.getInstance().clearFromMemory();
        // 清除所有观察者
        mSession.deleteObservers();
        if (mSession.isAutoClearCache()) {
            Utils.clearCache(getApplicationContext());
        }
        // 关闭HTTP资源
        HttpClientFactory.get().close();
        // 回收Session
        mSession.close();
        mSession = null;
        finish();
    }
}
