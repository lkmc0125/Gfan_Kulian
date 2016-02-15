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
package com.mappn.gfan.ui;

import java.io.File;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.mappn.gfan.Constants;
import com.mappn.gfan.R;
import com.mappn.gfan.common.ApiAsyncTask.ApiRequestListener;
import com.mappn.gfan.common.MarketAPI;
import com.mappn.gfan.common.download.DownloadManager;
import com.mappn.gfan.common.util.DBUtils;
import com.mappn.gfan.common.util.DialogUtil;
import com.mappn.gfan.common.util.ImageUtils;
import com.mappn.gfan.common.util.Utils;
import com.mappn.gfan.common.vo.BuyLog;
import com.mappn.gfan.common.vo.DownloadInfo;
import com.mappn.gfan.common.vo.DownloadItem;
import com.mappn.gfan.common.vo.ProductDetail;
import com.mappn.gfan.common.widget.BaseTabActivity;

/**
 * 商品详细页 <br>
 * 1 - 通过包名访问<br>
 * 2 - 通过产品ID访问(Google Market)<br>
 * 
 * @author andrew
 * @date 2011-3-14
 * @since Version 0.6.2
 */
public class ProductDetailActivity extends BaseTabActivity implements ApiRequestListener, Observer {

	private ProductDetail mProduct;
	private TabHost mTabHost;
	private ImageButton mDownloadButton;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_product_detail);
		
		final Intent intent = getIntent();
		mProduct = (ProductDetail) intent.getSerializableExtra(Constants.EXTRA_PRDUCT_DETAIL);
        initTopBar(mProduct);
        initTab(mProduct);
        boolean isBuy = intent.getBooleanExtra(Constants.IS_BUY, false);
        if (isBuy) {
            showDialog(DIALOG_PURCHASE);
        }
        mSession.addObserver(this);
	}
	
    @Override
    protected void onStart() {
        super.onStart();
        Utils.trackEvent(getApplicationContext(), Constants.GROUP_12,
                Constants.OPEN_PRODUCT_DETAIL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSession.deleteObserver(this);
    }

    /*
	 * 初始化TopBar
	 */
	private void initTopBar(ProductDetail product) {
		mDownloadButton = (ImageButton) findViewById(R.id.btn_download);
		mDownloadButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				download();
			}
		});
		
		final ImageView ivIcon = (ImageView) findViewById(R.id.iv_icon);
        ImageUtils.download(getApplicationContext(), product.getIconUrl(), ivIcon);
		final TextView tvAppName = (TextView) findViewById(R.id.tv_app_name);
		tvAppName.setText(product.getName());
		final TextView tvAppAuthor = (TextView) findViewById(R.id.tv_app_author);
		tvAppAuthor.setText(product.getAuthorName());
		final TextView tvAppRatings = (TextView) findViewById(R.id.tv_app_rating_num);
		tvAppRatings.setText(getString(R.string.gfan_comments,
				product.getRatingCount()));
		
		// 评星
        final RatingBar rbAppRating = (RatingBar) findViewById(R.id.rb_app_rating);
        float ratingLevel = product.getRating() / (float) 10;
        rbAppRating.setRating(ratingLevel);
		
        final TextView rbAppStatus = (TextView) findViewById(R.id.tv_status);
        HashMap<String, DownloadInfo> list = mSession.getDownloadingList();
        if (list.containsKey(product.getPackageName())) {
            
            DownloadInfo info = list.get(product.getPackageName()); 
            if(DownloadManager.Impl.isStatusInformational(info.mStatus)) {
                // 正在下载
                mDownloadButton.setEnabled(false);
                rbAppStatus.setText(R.string.download_status_downloading);
            } else if (info.mStatus == DownloadManager.Impl.STATUS_SUCCESS) {
                // 下载完成
                mDownloadButton.setEnabled(true);
                mProduct.setFilePath(info.mFilePath);
                rbAppStatus.setText(R.string.download_status_downloaded);
                mDownloadButton.setBackgroundResource(R.drawable.btn_install);
            } else {
                initAppInfo(mProduct);
            }
        } else {
            initAppInfo(mProduct);
        }
	}
	
	/*
	 * 初始化应用的状态信息（价格以及下载状态）
	 */
    private void initAppInfo(ProductDetail product) {

        final TextView rbAppStatus = (TextView) findViewById(R.id.tv_status);
        int payCategory = product.getPayCategory();

        // 正常模式
        PackageManager pm = getPackageManager();
        PackageInfo info = null;
        try {
            info = pm.getPackageInfo(product.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            // do nothing
        }
        if (info != null) {
            int versionCode = info.versionCode;
            if (product.getVersionCode() > versionCode) {
                // 有更新
                mDownloadButton.setEnabled(true);
                mDownloadButton.setImageResource(R.drawable.btn_dowload);
                rbAppStatus.setText(R.string.has_update);
            } else {
                // 已安装
                mDownloadButton.setEnabled(false);
                rbAppStatus.setText(R.string.has_installed);
            }
        } else {
            // 未安装
            if (Constants.PAY_TYPE_FREE == payCategory) {
                rbAppStatus.setText(R.string.free);
            } else if (Constants.PAY_TYPE_PAID == payCategory) {
                rbAppStatus.setText(getString(R.string.duihuanquan_unit, product.getPrice()));
            }
        }
    }

	/*
	 * 初始化Tab页面
	 */
	private void initTab(ProductDetail product) {

	    mTabHost = getTabHost();
	    mTabHost.setup();
	    
		TabSpec tab1 = mTabHost.newTabSpec(getString(R.string.lable_description));
		tab1.setIndicator(createTabView(getApplicationContext(),
				getString(R.string.lable_description)));
		Intent i1 = new Intent(this, ProductInfoActivity.class);
		i1.putExtra(Constants.EXTRA_PRDUCT_DETAIL, product);
		tab1.setContent(i1);
		mTabHost.addTab(tab1);
		
		TabSpec tab2 = mTabHost.newTabSpec(getString(R.string.comment_lab));
		int commentCount = product.getCommentsCount() > 0 ? product.getCommentsCount() : 0;
		tab2.setIndicator(createTabView(getApplicationContext(),
				getString(R.string.comment_lab, commentCount)));
		Intent i2 = new Intent(this, ProductCommentActivity.class);
		i2.putExtra(Constants.EXTRA_PRDUCT_DETAIL, product);
		tab2.setContent(i2);
		mTabHost.addTab(tab2);
        if (Constants.SOURCE_TYPE_GOOGLE.equals(product.getSourceType())) {
            mTabHost.getTabWidget().setEnabled(false);
        }
        mTabHost.setCurrentTab(0);
	}
	
    /**
     * 更新评论数 
     */
    /*package*/  void changeCommentCount(int num) {
        TextView indicator = (TextView) mTabHost.getTabWidget().getChildTabViewAt(1);
        indicator.setText(getString(R.string.comment_lab, num));
    }
    
	private static View createTabView(final Context context, final String text) {
		TextView view = (TextView) LayoutInflater.from(context).inflate(
				R.layout.common_tab_view, null);
		view.setText(text);
		return view;
	}

	public void onSuccess(int method, Object obj) {
		switch (method) {
		case MarketAPI.ACTION_GET_DOWNLOAD_URL:
			DownloadItem info = (DownloadItem) obj;
			startDownload(info);
			mDownloadButton.setEnabled(true);
			finish();
			break;

		case MarketAPI.ACTION_PURCHASE_PRODUCT:
			// 购买成功，同步购买记录
			BuyLog buyLog = new BuyLog();
			buyLog.pId = mProduct.getPid();
			buyLog.packageName = mProduct.getPackageName();
			DBUtils.insertBuyLog(getApplicationContext(), buyLog);
			MarketAPI.getDownloadUrl(this, this, mProduct.getPid(), mProduct.getSourceType());
			break;
		}
	}
	
	public void onError(int method, int statusCode) {
		switch (method) {
		case MarketAPI.ACTION_GET_DOWNLOAD_URL:
			mDownloadButton.setEnabled(true);
            Utils.makeEventToast(getApplicationContext(),
                    getString(R.string.alert_no_download_url), false);
			break;
		case MarketAPI.ACTION_PURCHASE_PRODUCT:
			if (219 == statusCode) {
				// NoEnoughCredit
				if (!isFinishing()) {
					showDialog(DIALOG_NO_BALANCE);
				}
            } else if (212 == statusCode) {
			    // 密码错误
			    Utils.makeEventToast(getApplicationContext(),
                        getString(R.string.hint_purchase_password_error), false);
			} else {
				// other error
				Utils.makeEventToast(getApplicationContext(),
						getString(R.string.hint_purchase_failed), false);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 开始下载任务
	 */
    private void startDownload(DownloadItem info) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(info.url));
        request.setPackageName(mProduct.getPackageName());
        request.setTitle(mProduct.getName());
        request.setIconUrl(mProduct.getIconUrl());
        request.setMD5(info.fileMD5);
        request.setSourceType(com.mappn.gfan.common.download.Constants.DOWNLOAD_FROM_MARKET);
        mSession.getDownloadManager().enqueue(request);
        Utils.makeEventToast(getApplicationContext(), getString(R.string.alert_start_download),
                false);
    }

	/**
	 * 开始下载任务
	 */
	public void download() {
	    
	    Utils.trackEvent(getApplicationContext(), Constants.GROUP_12,
                Constants.DETAIL_DOWNLOAD);

		if(Constants.PAY_TYPE_PAID == mProduct.getPayCategory()) {
			// 收费应用
			if (mSession.isLogin()) {
				if (!DBUtils.isBought(getApplicationContext(), mProduct.getPid())) {
					if (!isFinishing()) {
						showDialog(DIALOG_PURCHASE);
						return;
					}
				}
			} else {
				// 登录
				Intent loginIntent = new Intent(getApplicationContext(),
						LoginActivity.class);
				startActivity(loginIntent);
				return;
			}
		}

        if (TextUtils.isEmpty(mProduct.getFilePath())) {
            HashMap<String, DownloadInfo> list = mSession.getDownloadingList();
            if (list.containsKey(mProduct.getPackageName())) {
                // 下载中
                Utils.makeEventToast(getApplicationContext(),
                        getString(R.string.warning_comment_later), false);
                return;
            } else {
                // 开始下载
                MarketAPI.getDownloadUrl(getApplicationContext(), ProductDetailActivity.this,
                        mProduct.getPid(), mProduct.getSourceType());
                mDownloadButton.setEnabled(false);
            }
        } else {
            // 下载完成
            Utils.installApk(getApplicationContext(), new File(mProduct.getFilePath()));
        }
	}

	private final static int DIALOG_PURCHASE = 1;
	private final static int DIALOG_NO_BALANCE = 2;

    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_PURCHASE:
            return DialogUtil.newEnsurePurchaseDialog(this, id,
                    getString(R.string.hint_purchase, mProduct.getPrice()));
        case DIALOG_NO_BALANCE:
            return DialogUtil.newInsufficientBalanceDialog(this, id,
                    getString(R.string.warning_no_balance));
        default:
            break;
        }
        return null;
    }
	
	/**
	 * 购买商品
	 */
	public void purchaseProduct(String password) {
	    MarketAPI.purchaseProduct(this, this, mProduct.getPid(), password);
	}
	
	/**
	 * 前往充值页
	 */
    public void gotoDepositPage() {
        final String type = mSession.getDefaultChargeType();
        if (type == null) {
            final Intent intent = new Intent(getApplicationContext(), ChargeTypeListActivity.class);
            intent.putExtra("payment", mProduct.getPrice());
            startActivity(intent);
        } else {
            final Intent intent = new Intent(getApplicationContext(), PayMainActivity.class);
            intent.putExtra("type", type);
            intent.putExtra("payment", mProduct.getPrice());
            startActivity(intent);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void update(Observable arg0, Object arg1) {
        if (arg1 instanceof HashMap) {
            HashMap<String, DownloadInfo> mDownloadingTask = (HashMap<String, DownloadInfo>) arg1;
            DownloadInfo info = mDownloadingTask.get(mProduct.getPackageName());
            if (info != null) {
                final TextView rbAppStatus = (TextView) findViewById(R.id.tv_status);
                if (info.mStatus == DownloadManager.Impl.STATUS_SUCCESS) {
                    // 已经下载成功
                    mDownloadButton.setEnabled(true);
                    mDownloadButton.setBackgroundResource(R.drawable.btn_install);
                    rbAppStatus.setText(R.string.download_status_downloaded);
                    mProduct.setFilePath(info.mFilePath);
                } else if(DownloadManager.Impl.isStatusError(info.mStatus)) {
                    // 下载失败
                    initAppInfo(mProduct);
                }
            } else {
                initAppInfo(mProduct);
            }
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuItem item = menu.add(0, 0, 0, getString(R.string.alert_safe));
        item.setIcon(R.drawable.ic_safe);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case 0:
            Intent i = new Intent(getApplicationContext(), SafeActivity.class);
            startActivity(i);
            return true;
        }
        return false;
    }
}