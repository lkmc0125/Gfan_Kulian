/*
 * Copyright (C) 2011 mAPPn.Inc
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
import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.vo.ProductDetail;
import com.xiaohong.kulian.common.widget.AppListAdapter;
import com.xiaohong.kulian.common.widget.LazyloadListActivity;
import com.xiaohong.kulian.common.widget.LoadingDrawable;

/**
 * 产品评论页<br>
 * 用户可以对产品进行各种讨论、评星，用以展示讨论列表。
 * 
 * @author andrew
 * @date 2011-3-22
 * 
 */
public class ProductCommentActivity extends LazyloadListActivity implements ApiRequestListener {

    private static final int REFRESH_RATING = 1;

    private AppListAdapter mCommentAdapter;
    private ProductDetail mProduct;
    private int mTotalSize;
    private RatingBar mMyRating;
    private TextView mRanking;
    private boolean mHasRating;
    private boolean mIsRating;
    private long lastRatingTime;
    private RelativeLayout mRatingLayout;
    private RelativeLayout mInfoLayout;
    private EditText mComment;
    private Button mSend;
    private LinearLayout mCommentView;

    @Override
    public boolean doInitView(Bundle savedInstanceState) {

        setContentView(R.layout.market_activity_comment_list);

        Utils.trackEvent(getApplicationContext(), Constants.GROUP_12,
                Constants.DETAIL_CLICK_COMMENT);
        
        final Intent intent = getIntent();
        mProduct = (ProductDetail) intent.getSerializableExtra(Constants.EXTRA_PRDUCT_DETAIL);

        initViews();
        
        doLazyload();
        return true;
    }

    @Override
    public void doLazyload() {
        MarketAPI.getComments(this, this, mProduct.getPid(), getItemsPerPage(), getStartIndex());
    }

    @Override
    public AppListAdapter doInitListAdapter() {
        return mCommentAdapter = new AppListAdapter(
                getApplicationContext(), 
                null,
                R.layout.market_list_item_comment, 
                new String[] { 
                    Constants.KEY_COMMENT_AUTHOR,
                    Constants.KEY_COMMENT_DATE, 
                    Constants.KEY_COMMENT_BODY }, 
                new int[] {
                    R.id.tv_author, 
                    R.id.tv_time, 
                    R.id.tv_comment });
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case REFRESH_RATING:
                refreshRatingView();
                break;
            default:
                break;
            }
        }
    };

    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(int method, Object obj) {

        if (isFinishing()) {
            return;
        }
        switch (method) {
        case MarketAPI.ACTION_GET_COMMENTS:

            if (obj instanceof HashMap) {
                HashMap<String, Object> result = (HashMap<String, Object>) obj;

                mTotalSize = (Integer) result.get(Constants.KEY_TOTAL_SIZE);

                if (mTotalSize > 0) {
                    // 成功获取评论列表
                    mCommentAdapter.addData((ArrayList<HashMap<String, Object>>) result
                            .get(Constants.KEY_COMMENT_LIST));
                } else {
                    // 暂无评论
                    HashMap<String, Object> noData = new HashMap<String, Object>();
                    noData.put(Constants.KEY_COMMENT_BODY, getString(R.string.hint_no_comments));
                    mCommentAdapter.addData(noData);
                }
                setLoadResult(true);
            }
            break;
        case MarketAPI.ACTION_GET_MYRATING:
            mHandler.sendEmptyMessage(REFRESH_RATING);
            int rating = Utils.getInt((String) obj);
            if (rating > 0) {
                mMyRating.setRating(rating);
            }
            break;
        case MarketAPI.ACTION_ADD_RATING:
            mRanking.setText(R.string.lable_ranking_over);
            break;
        case MarketAPI.ACTION_ADD_COMMENT:
            mSend.setEnabled(true);
            addMyComment();
            mComment.setText("");
            Utils.makeEventToast(getApplicationContext(), getString(R.string.alert_post_ok), false);
            break;
        }
    }

    /*
     * 显示我的评论信息
     */
    private void addMyComment() {
        String commentBody = mComment.getText().toString();
        HashMap<String, Object> comment = new HashMap<String, Object>();
        comment.put(Constants.KEY_COMMENT_BODY, commentBody);
        comment.put(Constants.KEY_COMMENT_AUTHOR, mSession.getUserName());
        comment.put(Constants.KEY_COMMENT_DATE, Utils.formatTime(System.currentTimeMillis()));

        if (mTotalSize <= 0) {
            // 第一条评论
            mCommentAdapter.clearData();
        }
        mCommentAdapter.insertData(comment);
        ((ProductDetailActivity) getParent()).changeCommentCount(++mTotalSize);
    }

    @Override
    public void onError(int method, int statusCode) {
        switch (method) {
        case MarketAPI.ACTION_GET_COMMENTS:
            setLoadResult(false);
            Utils.makeEventToast(getApplicationContext(), getString(R.string.no_network), false);
            break;
        case MarketAPI.ACTION_GET_MYRATING:
            mHasRating = false;
            break;
        case MarketAPI.ACTION_ADD_COMMENT:
            mSend.setEnabled(true);
            handlePostError(statusCode);
            break;
        default:
            break;
        }
        Log.d("error", "status code " + statusCode);
    }

    /*
     * 处理评论的异常信息
     */
    private void handlePostError(int statusCode) {
        switch (statusCode) {
        case 232:
            // 非法回复内容
            Utils.makeEventToast(getApplicationContext(), getString(R.string.alert_unsafe_word),
                    false);
            break;
        case 225:
            // 没有对应的帖子
            Utils.makeEventToast(getApplicationContext(),
                    getString(R.string.alert_product_not_exist), false);
            break;
        case 233:
            // 帐号被禁言
            Utils.makeEventToast(getApplicationContext(), getString(R.string.alert_user_forbidden),
                    false);
            break;
        default:
            // 网络问题
            Utils.makeEventToast(getApplicationContext(), getString(R.string.alert_post_error),
                    false);
            break;
        }
    }

    @Override
    protected int getItemCount() {
        return mTotalSize;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHeaderFooterView();
    }

    // 管理用戶状态
    private void refreshHeaderFooterView() {
        if (mSession.isLogin()) {
            // 用户已经登录
            if (mSession.getInstalledApps().contains(mProduct.getPackageName())) {
                // 用户已经安装此应用
                mInfoLayout.setVisibility(View.GONE);
                mRatingLayout.setVisibility(View.VISIBLE);
            } else {
                // 用户未安装此应用，提示下载并安装
                mInfoLayout.setVisibility(View.VISIBLE);
                mRatingLayout.setVisibility(View.GONE);
                ImageView icon = (ImageView) mInfoLayout.findViewById(R.id.iv_icon);
                icon.setImageResource(R.drawable.down_btn_9);
                TextView info = (TextView) mInfoLayout.findViewById(R.id.tv_info);
                info.setText(R.string.lable_not_install);
            }
            if (!mHasRating) {
                mHasRating = true;
                MarketAPI.getMyRating(this, this, mProduct.getPid());
            }
            mCommentView.setVisibility(View.VISIBLE);
        } else {
            // 用户没有登录，提示用户登录
            mInfoLayout.setVisibility(View.VISIBLE);
            mRatingLayout.setVisibility(View.GONE);
            ImageView icon = (ImageView) mInfoLayout.findViewById(R.id.iv_icon);
            icon.setImageResource(R.drawable.login_icon);
            TextView info = (TextView) mInfoLayout.findViewById(R.id.tv_info);
            info.setText(R.string.lable_not_login);
            mCommentView.setVisibility(View.GONE);
        }
    }

    // 刷新评分
    private void refreshRatingView() {
        if (mIsRating) {
            // 用户安装过此应用，已经卸载
            mInfoLayout.setVisibility(View.GONE);
            mRatingLayout.setVisibility(View.VISIBLE);
        }
        // 可以修改评分状态
        lastRatingTime = 1;
    }

    /*
     * 初始化View 判断用户是否登录，并显示没有登录的提示信息
     */
    private void initViews() {

        FrameLayout mLoading = (FrameLayout) findViewById(R.id.loading);
        ProgressBar mProgress = (ProgressBar) mLoading.findViewById(R.id.progressbar);
        mProgress.setIndeterminateDrawable(new LoadingDrawable(getApplicationContext()));
        mProgress.setVisibility(View.VISIBLE);
        
        mList = (ListView) findViewById(android.R.id.list);
        mList.setEmptyView(mLoading);

        FrameLayout rl = (FrameLayout) LayoutInflater.from(getApplicationContext()).inflate(
                R.layout.product_rating, mList, false);
        mRatingLayout = (RelativeLayout) rl.findViewById(R.id.rating_view);
        mInfoLayout = (RelativeLayout) rl.findViewById(R.id.info_view);

        mInfoLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSession.isLogin()) {
                    // 安装应用
                    ProductDetailActivity parent = (ProductDetailActivity) getParent();
                    parent.download();
                } else {
                    // 登录
                     Intent loginIntent = new Intent(getApplicationContext(),
                     LoginActivity.class);
                     startActivity(loginIntent);
                }
            }
        });

        mMyRating = (RatingBar) rl.findViewById(R.id.rb_myrating);
        mRanking = (TextView) rl.findViewById(R.id.tv_ranking);
        mMyRating.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, final float rating, boolean fromUser) {

                int ranking = (int) rating;
                switch (ranking) {
                case 1:
                    mRanking.setText(R.string.lable_ranking_1);
                    break;
                case 2:
                    mRanking.setText(R.string.lable_ranking_2);
                    break;
                case 3:
                    mRanking.setText(R.string.lable_ranking_3);
                    break;
                case 4:
                    mRanking.setText(R.string.lable_ranking_4);
                    break;
                case 5:
                    mRanking.setText(R.string.lable_ranking_5);
                    break;

                default:
                    break;
                }

                long currentTime = System.currentTimeMillis();
                if ((lastRatingTime != 0) && (currentTime - lastRatingTime > 2000)) {
                    // 防止用户过于频繁提交数据
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MarketAPI.addRating(getApplicationContext(),
                                    ProductCommentActivity.this, mProduct.getPid(), (int) rating);
                        }
                    }, 2000);

                    lastRatingTime = currentTime;
                }
            }
        });

        mList.addHeaderView(rl, null, false);

        mCommentView = (LinearLayout) findViewById(R.id.product_comment);
        mComment = (EditText) findViewById(R.id.et_comment);
        mSend = (Button) findViewById(R.id.ib_send);
        mSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String body = mComment.getText().toString();
                if (TextUtils.isEmpty(body)) {
                    // 不能发表空内容
                    Utils.makeEventToast(getApplicationContext(),
                            getString(R.string.alert_post_not_null), false);
                } else {
                    mSend.setEnabled(false);
                    MarketAPI.addComment(getApplicationContext(), ProductCommentActivity.this,
                            mProduct.getPid(), body.trim());
                    Utils.trackEvent(getApplicationContext(), Constants.GROUP_12,
                            Constants.DETAIL_POST_COMMENT);
                }
            }
        });
        refreshHeaderFooterView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCommentAdapter = null;
        mProduct = null;
        mMyRating = null;
        mRanking = null;
        mRatingLayout = null;
        mInfoLayout = null;
        mComment = null;
        mSend = null;
        mCommentView = null;
    }
}