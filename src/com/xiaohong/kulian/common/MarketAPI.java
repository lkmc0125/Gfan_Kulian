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
package com.xiaohong.kulian.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.R.integer;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.Session;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.codec.binary.Base64;
import com.xiaohong.kulian.common.codec.digest.DigestUtils;
import com.xiaohong.kulian.common.util.SecurityUtil;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.vo.CardInfo;
import com.xiaohong.kulian.common.vo.UpgradeInfo;

/**
 * GfanMobile aMarket API utility class
 * 
 * @author andrew.wang
 * @date 2010-10-29
 * @since Version 0.4.0
 */
public class MarketAPI {

    /** 机锋市场API host地址 */
    public static final String API_BASE_URL = 
        // real host
      "http://api.kulian.com/";
        // test host
//        "http://117.79.80.22/";
    
    /** 机锋市场API地址 */
    public static final String API_HOST_JAVA = 
        API_BASE_URL + "market/api/";
//      "http://10.16.8.142:8080/market.kulian.com/api/";
    
    /** 用户中心API地址*/
    public static final String API_UCENTER_HOST = 
            API_BASE_URL
        // real host
      + "uc1/common/";
        // test host
//        + "uc/common/";
        // TEST 
//            "http://117.79.80.12/uc1/common/";
    
    public static final String BBS_SEARCH_API =
            "http://search.kulian.com/search/search/luntanAttJk";
//    "http://10.16.8.232:8080/search/search/luntanAttJk";
    
    // User Center URL HOST
    public static final String API_HOST_CLOUD = "http://passport.kulian.com/kulian_center/";
    
     // 机锋市场 API URLS
    /* package */static final String[] API_URLS = {
            // ACTION_LOGIN
            API_UCENTER_HOST + "login",
            // ACTION_REGISTER
            API_UCENTER_HOST + "register",
            // ACTION_GET_COMMENTS
            API_HOST_JAVA + "getComments",
            // ACTION_ADD_COMMENT
            API_HOST_JAVA + "addComment",
            // ACTION_ADD_RATING
            API_HOST_JAVA + "addRating",
            // ACTION_PURCHASE_PRODUCT
            API_BASE_URL + "sdk/pay/purchaseProduct",
            // ACTION_GET_CONSUMESUM
            API_BASE_URL + "sdk/pay/getConsumeSum",
            // ACTION_SYNC_BUYLOG
            API_HOST_JAVA + "syncBuyLog",
            // ACTION_GET_MYRATING
            API_HOST_JAVA + "getMyRating",
            // ACTION_GET_CONSUME_DETAIL
            API_BASE_URL + "sdk/pay/getConsumeDetail",
            // ACTION_GET_TOPIC
            API_HOST_JAVA + "getTopic",
            // ACTION_SEARCH
            API_HOST_JAVA + "search",
            // ACTION_GET_PRODUCTS
            API_HOST_JAVA + "getProducts",
            // ACTION_GET_RECOMMEND_PRODUCTS
            API_HOST_JAVA + "getRecommendProducts",
            // ACTION_GET_PRODUCT_DETAIL
//            API_HOST_JAVA + "getProductDetail",
            "http://livew.mobdsp.com/cb/appdetail",
            // ACTION_GET_DOWNLOAD_URL
            API_HOST_JAVA + "getDownloadUrl",
            // ACTION_GET_HOME_RECOMMEND
            API_HOST_JAVA + "getHomeRecommend",
            // ACTION_CHECK_NEW_VERSION
            API_HOST_JAVA + "checkNewVersion",
            // ACTION_GET_CATEGORY
            API_HOST_JAVA + "getCategory",
            // ACTION_CHECK_UPGRADE
            API_HOST_JAVA + "checkUpgrade",
            // ACTION_BIND_ACCOUNT
            API_HOST_CLOUD + "?mo=cloud_phone&do=addDev",
            // ACTION_GET_BALANCE
            API_UCENTER_HOST + "query_balance",
            // ACTION_GET_PAY_LOG
            API_BASE_URL + "sdk/pay/chargeConsumeLog",
            // ACTION_CHARGE
            API_BASE_URL + "pay/szf/servlet/rechargeRequest",
            // ACTION_SYNC_CARDINFO
            API_BASE_URL + "pay/szf/getCardConfigServlet",
            // ACTION_QUERY_CHARGE_BY_ORDERID
            API_BASE_URL + "pay/szf/sdk/queryServlet",
            // ACTION_QUERY_CHARGE
            API_UCENTER_HOST + "query_charge_log_list",
            // ACTION_SYNC_APPS
            API_HOST_JAVA + "syncApps",
            // ACTION_CHECK_NEW_SPLASH
            API_HOST_JAVA + "checkNewSplash",
            // ACTION_UNBIND
            API_HOST_CLOUD + "?mo=cloud_phone&do=delDev&uid=",
            // ACTION_GET_DETAIL
            API_HOST_JAVA + "getDetail",
            // ACTION_GET_ALIPAY_ORDER_INFO
            API_BASE_URL + "pay/szf/servlet/businessProcess.do",
            // ACTION_QUERY_ALIPAY_RESULT
            API_BASE_URL + "pay/szf/servlet/businessProcess.do",
            // ACTION_GET_SEARCH_KEYWORDS
            API_HOST_JAVA + "getKeywords",
            // ACTION_GET_TOP_RECOMMEND
            API_HOST_JAVA + "getTopRecommend",
            // ACTION_GET_RANK_BY_CATEGOR
//            API_HOST_JAVA + "getRankByCategory",
            "http://livew.mobdsp.com/cb/applist_page",
            // ACTION_GET_GROW_FAST
            API_HOST_JAVA + "getGrowFast",
            // ACTION_GET_ALL_CATEGORY
            API_HOST_JAVA + "getAllCategory",
            // ACTION_GET_REQUIRED
            API_HOST_JAVA + "getRequired",
            // ACTION_BBS_SEARCH
            BBS_SEARCH_API };
    
    /** 登录 */
    public static final int ACTION_LOGIN = 0;
    /** 注册 */
    public static final int ACTION_REGISTER = 1;
    /** 获取评论 */
    public static final int ACTION_GET_COMMENTS = 2;
    /** 添加评论 */
    public static final int ACTION_ADD_COMMENT = 3;
    /** 添加星级 */
    public static final int ACTION_ADD_RATING = 4;
    /** 购买软件 */
    public static final int ACTION_PURCHASE_PRODUCT = 5;
    /** 获取消费额 */
    public static final int ACTION_GET_CONSUMESUM = 6;
    /** 获取消费记录 */
    public static final int ACTION_SYNC_BUYLOG = 7;
    /** 获取我的评级 */
    public static final int ACTION_GET_MYRATING = 8;
    /** 获取消费明细 */
    public static final int ACTION_GET_CONSUME_DETAIL = 9;
    /** 获取帖子 */
    public static final int ACTION_GET_TOPIC = 10;
    /** 搜索 */
    public static final int ACTION_SEARCH = 11;
    /** 获取商品 */
    public static final int ACTION_GET_PRODUCTS = 12;
    /** 获取专题推荐商品 */
    public static final int ACTION_GET_RECOMMEND_PRODUCTS = 13;
    /** 获取应用详细 */
    public static final int ACTION_GET_PRODUCT_DETAIL = 14;
    /** 获取应用下载链接 */
    public static final int ACTION_GET_DOWNLOAD_URL = 15;
    /** 获取首页推荐列表 */
    public static final int ACTION_GET_HOME_RECOMMEND = 16;
    /** 检查（机锋市场）更新 */
    public static final int ACTION_CHECK_NEW_VERSION = 17;
    /** 获取分类 */
    public static final int ACTION_GET_CATEGORY = 18;
    /** 检查（应用）更新 */
    public static final int ACTION_CHECK_UPGRADE = 19;
    /** 绑定账户（云推送） */
    public static final int ACTION_BIND_ACCOUNT = 20;
    /** 获取余额 */
    public static final int ACTION_GET_BALANCE = 21;
    /** 获取消费记录 */
    public static final int ACTION_GET_PAY_LOG = 22;
    /** 充值 */
    public static final int ACTION_CHARGE = 23;
    /** 同步卡信息 */
    public static final int ACTION_SYNC_CARDINFO = 24;
    /** 获取支付记录 */
    public static final int ACTION_QUERY_CHARGE_BY_ORDERID = 25;
    /** 获取支付记录 */
    public static final int ACTION_QUERY_CHARGE = 26;
    /** 获取用户安装应用列表 */
    public static final int ACTION_SYNC_APPS = 27;
    /** 检查SPLASH更新 */
    public static final int ACTION_CHECK_NEW_SPLASH = 28;
    /** 解除绑定（运推送） */
    public static final int ACTION_UNBIND = 29;
    /** 获取产品详细信息（通过包名） */
    public static final int ACTION_GET_DETAIL = 30;
    /** 获取支付宝订单信息 */
    public static final int ACTION_GET_ALIPAY_ORDER_INFO = 31;
    /** 获取支付宝充值结果 */
    public static final int ACTION_QUERY_ALIPAY_RESULT = 32;
    /** 获取搜索热词 */
    public static final int ACTION_GET_SEARCH_KEYWORDS = 33;
    /** 获取首页顶部推荐 */
    public static final int ACTION_GET_TOP_RECOMMEND = 34;
    /** 获取排行榜 */
    public static final int ACTION_GET_RANK_BY_CATEGORY = 35;
    /** 获取增长最快排行列表 */
    public static final int ACTION_GET_GROW_FAST = 36;
    /** 获取所有分类列表 */
    public static final int  ACTION_GET_ALL_CATEGORY = 37;
    /** 获取装机必备列表 */
    public static final int  ACTION_GET_REQUIRED = 38;
    /** BBS Search API */
    public static final int  ACTION_BBS_SEARCH = 39;
    
    /**
     * Get Search Keywords API<br>
     * Default size is 10.
     */
    public static void getSearchKeywords(Context context, ApiRequestListener handler) {

        final HashMap<String, Object> params = new HashMap<String, Object>(1);

        params.put("size", 15);

        new ApiAsyncTask(context, 
                ACTION_GET_SEARCH_KEYWORDS, handler, params).execute();
    }
    
    public static void getSearchFromBBS(Context context, ApiRequestListener handler,
            String keyword, int start, int size) {

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new BasicNameValuePair("searchWord", keyword));
        params.add(new BasicNameValuePair("startPosition", String.valueOf(start)));
        params.add(new BasicNameValuePair("size", String.valueOf(size)));
        
        new ApiAsyncTask(context, 
                ACTION_BBS_SEARCH, handler, params).execute();
    }
    
    /**
     * 装机必备API<br>
     */
    public static void getRequired(Context context, ApiRequestListener handler) {
        
        final HashMap<String, Object> params = new HashMap<String, Object>(3);

        Session session = Session.get(context);
        
        params.put("platform", session.getOsVersion());
        params.put("screen_size", session.getScreenSize());
        params.put("match_type", session.isFilterApps());
        
        new ApiAsyncTask(context, 
                ACTION_GET_REQUIRED, handler, params).execute();
    }
    
    /**
     * Get Home Page Top Recommends API<br>
     * Default size is 10.
     */
    public static void getTopRecommend(Context context, ApiRequestListener handler) {

        final HashMap<String, Object> params = new HashMap<String, Object>(3);

        Session session = Session.get(context);
        
        params.put("platform", session.getOsVersion());
        params.put("screen_size", session.getScreenSize());
        params.put("match_type", session.isFilterApps());

        new ApiAsyncTask(context, 
                ACTION_GET_TOP_RECOMMEND, handler, params).execute();
    }
    
    /**
     * Get All Category API<br>
     */
    public static void getAllCategory(Context context, ApiRequestListener handler) {

        final HashMap<String, Object> params = new HashMap<String, Object>(3);

        Session session = Session.get(context);
        
        params.put("platform", session.getOsVersion());
        params.put("screen_size", session.getScreenSize());
        params.put("match_type", session.isFilterApps());

        new ApiAsyncTask(context, 
                ACTION_GET_ALL_CATEGORY, handler, params).execute();
    }
    
	/**
	 * Register API<br>
	 * Do the register process, UserName, Password, Email must be provided.<br>
	 */
	public static void register(Context context, ApiRequestListener handler,
			String username, String password, String email) {

		final HashMap<String, Object> params = new HashMap<String, Object>(3);

		params.put("username", username);
		params.put("password", password);
		params.put("email", email);

		new ApiAsyncTask(context, ACTION_REGISTER, handler, params).execute();
	}
	
	   /**
     * Login API<br>
     * Do the login process, UserName, Password must be provided.<br>
     */
    public static void login(Context context, ApiRequestListener handler,
            String username, String password) {

        final HashMap<String, Object> params = new HashMap<String, Object>(2);

        params.put("username", username);
        params.put("password", password);

        new ApiAsyncTask(context, 
                ACTION_LOGIN, handler, params).execute();
    }

	/**
	 * Get Home Recommend API<br>
	 * 首页推荐列表（包含编辑推荐部分和算法生成部分）<br>
	 */
	public static void getHomeRecommend(Context context,
			ApiRequestListener handler, int startPosition, int size) {

		Session session = Session.get(context);

		final HashMap<String, Object> params = new HashMap<String, Object>(5);
		params.put("size", size);
		params.put("start_position", startPosition);
		params.put("platform", session.getOsVersion());
		params.put("screen_size", session.getScreenSize());
		params.put("match_type", session.isFilterApps());

		new ApiAsyncTask(context,
				ACTION_GET_HOME_RECOMMEND, handler, params).execute();
	}
	
	/**
     * Get Rank By Category API<br>
     * 首页排行列表<br>
     */
    public static void getRankByCategory(Context context,
            ApiRequestListener handler, int startPosition, int size, String category) {

        Session session = Session.get(context);

        HashMap<String, String> categoryMap = new HashMap<String, String>(3);
        categoryMap.put(Constants.CATEGORY_RCMD, "1");
        categoryMap.put(Constants.CATEGORY_APP, "2");
        categoryMap.put(Constants.CATEGORY_GAME, "3");
        
        final HashMap<String, Object> params = new HashMap<String, Object>(3);
        params.put("apptype", categoryMap.get(category));
        params.put("page", 1);
        params.put("phone_number", "13800138000");
        
        new ApiAsyncTask(context,
                ACTION_GET_RANK_BY_CATEGORY, handler, params).execute();
    }
    
    /**
     * Get Grow Fast API<br>
     * 增长最快排行列表<br>
     */
    public static void getGrowFast(Context context,
            ApiRequestListener handler, int startPosition, int size) {

        Session session = Session.get(context);

        final HashMap<String, Object> params = new HashMap<String, Object>(5);
        params.put("size", size);
        params.put("start_position", startPosition);
        params.put("platform", session.getOsVersion());
        params.put("screen_size", session.getScreenSize());
        params.put("match_type", session.isFilterApps());

        new ApiAsyncTask(context,
                ACTION_GET_GROW_FAST, handler, params).execute();
    }

	/**
	 * Bind user account with cloud authority
	 */
	public static void bindAccount(Context context, ApiRequestListener handler) {

		Session mSession = Session.get(context);

		final HashMap<String, Object> params = new HashMap<String, Object>(4);
		params.put("uid", mSession.getUid());
		params.put("devid", mSession.getDeviceId());
		params.put("imei", mSession.getIMEI());
		params.put("phonemodel", mSession.getModel());
		// use new hudee system
		params.put("version", 2);
		new ApiAsyncTask(context, ACTION_BIND_ACCOUNT, handler, params)
				.execute();
	}

	/**
	 * Unbind user account with cloud authority
	 */
	public static void unbindAccount(Context context, ApiRequestListener handler) {

		new ApiAsyncTask(context, ACTION_UNBIND, handler, null).execute();
	}

	/**
	 * 获取专题推荐商品列表
	 */
	public static void getRecommendProducts(Context context,
			ApiRequestListener handler, String type, int size, int startPosition) {

		Session mSession = Session.get(context);

		final HashMap<String, Object> params = new HashMap<String, Object>(6);
		params.put("type", type);
		params.put("size", size);
		params.put("start_position", startPosition);
		params.put("platform", mSession.getOsVersion());
		params.put("screen_size", mSession.getScreenSize());
		params.put("match_type", mSession.isFilterApps());

		new ApiAsyncTask(context, ACTION_GET_RECOMMEND_PRODUCTS, handler,
				params).execute();
	}

	/**
	 * 获取应用分类列表
	 */
	public static void getCategory(Context context, ApiRequestListener handler,
			String categoryCode) {

		Session mSession = Session.get(context);

		final HashMap<String, Object> params = new HashMap<String, Object>(5);
		params.put("local_version", -1);
		params.put("category_cord", categoryCode);
		params.put("platform", mSession.getOsVersion());
		params.put("screen_size", mSession.getScreenSize());
		params.put("match_type", mSession.isFilterApps());

		new ApiAsyncTask(context, ACTION_GET_CATEGORY, handler, params)
				.execute();
	}

	/**
	 * 获取应用列表
	 */
	public static void getProducts(Context context, ApiRequestListener handler,
			int size, int startPosition, int orderBy, String categoryId) {

		Session mSession = Session.get(context);

		final HashMap<String, Object> params = new HashMap<String, Object>(6);
//		params.put("size", size);
//		params.put("start_position", startPosition);
//		params.put("platform", mSession.getOsVersion());
//		params.put("screen_size", mSession.getScreenSize());
//		params.put("orderby", orderBy);
//		params.put("category_id", categoryId);
//		params.put("match_type", mSession.isFilterApps());
		params.put("apptype", 1);
		params.put("page", 1);
		params.put("phone_number", "13800138000");
		new ApiAsyncTask(context, ACTION_GET_PRODUCTS, handler, params)
				.execute();
	}

	/**
	 * 获取评论
	 */
	public static void getComments(Context context, ApiRequestListener handler,
			String pId, int size, int startPosition) {

		final HashMap<String, Object> params = new HashMap<String, Object>(3);
		params.put("p_id", pId);
		params.put("size", size);
		params.put("start_position", startPosition);

		new ApiAsyncTask(context, ACTION_GET_COMMENTS, handler, params)
				.execute();
	}

	/**
	 * 充值
	 */
	public static void charge(Context context, ApiRequestListener handler,
			String password, String type, CardInfo card) {

		Session mSession = Session.get(context);

		final HashMap<String, Object> params = new HashMap<String, Object>(7);
		params.put("user_id", mSession.getUid());
        params.put("password",
                SecurityUtil.encryptPassword(password, String.valueOf(mSession.getUid())));
		params.put("type", type);
		params.put("pay_type", card.payType);
		params.put("card_account", card.cardAccount);
		params.put("card_password", card.cardPassword);
		params.put("card_credit", card.cardCredit);

		new ApiAsyncTask(context, MarketAPI.ACTION_CHARGE, handler, params)
				.execute();
	}

	/**
	 * 添加评论
	 */
    public static void addComment(Context context, ApiRequestListener handler, String pId,
            String comment) {

        Session mSession = Session.get(context);

        String passwordEnc = SecurityUtil.encryptPassword(mSession.getPassword(),
                mSession.getUserName());
        String verifyCodeEnc = Utils.getUTF8String(Base64.encodeBase64(DigestUtils.md5(String
                .valueOf(mSession.getUserName()) + String.valueOf(pId) + passwordEnc)));

        final HashMap<String, Object> params = new HashMap<String, Object>(3);
        params.put("p_id", pId);
        params.put("uid", mSession.getUid());
        params.put("comment", comment);
        params.put("username", mSession.getUserName());
        params.put("password", passwordEnc);
        params.put("verify_code", verifyCodeEnc);

        new ApiAsyncTask(context, ACTION_ADD_COMMENT, handler, params).execute();
    }

	/**
	 * 添加评级
	 */
    public static void addRating(Context context, ApiRequestListener handler, 
            String pId, int rating) {

        Session mSession = Session.get(context);

        String passwordEnc = SecurityUtil.encryptPassword(mSession.getPassword(),
                mSession.getUserName());
        String verifyCodeEnc = Utils.getUTF8String(Base64.encodeBase64(
                DigestUtils.md5(String.valueOf(mSession.getUserName()) 
                        + String.valueOf(pId) + passwordEnc)));
        final HashMap<String, Object> params = new HashMap<String, Object>(6);
        params.put("p_id", pId);
        params.put("uid", mSession.getUid());
        params.put("rating", rating);
        params.put("username", mSession.getUserName());
        params.put("password", passwordEnc);
        params.put("verify_code", verifyCodeEnc);

        new ApiAsyncTask(context, ACTION_ADD_RATING, handler, params).execute();
    }

	/**
	 * 购买商品
	 */
	public static void purchaseProduct(Context context,
			ApiRequestListener handler, String pId, String password) {

		Session mSession = Session.get(context);

		String passwordEnc = SecurityUtil.encryptPassword(password,
				mSession.getUserName());
        String verifyCodeEnc = Utils.getUTF8String(Base64.encodeBase64(DigestUtils.md5(String
                .valueOf(mSession.getUserName()) + String.valueOf(pId) + passwordEnc)));

		final HashMap<String, Object> params = new HashMap<String, Object>(4);
		params.put("pid", pId);
		params.put("username", mSession.getUserName());
		params.put("password", passwordEnc);
		 params.put("verify_code", verifyCodeEnc);

		new ApiAsyncTask(context, MarketAPI.ACTION_PURCHASE_PRODUCT, handler,
				params).execute();
	}

	/**
	 * 获取下载链接
	 */
	public static void getDownloadUrl(Context context,
			ApiRequestListener handler, String pId, String sourceType) {

		Session mSession = Session.get(context);

		final HashMap<String, Object> params = new HashMap<String, Object>(3);
		params.put("p_id", pId);
		params.put("uid", mSession.getUid());
		params.put("source_type", sourceType);

		new ApiAsyncTask(context, MarketAPI.ACTION_GET_DOWNLOAD_URL, handler,
				params).execute();
	}

	/**
	 * 搜索
	 */
	public static void search(Context context, ApiRequestListener handler,
			int size, int startPosition, int orderBy, String keyword) {

		Session session = Session.get(context);

		final HashMap<String, Object> params = new HashMap<String, Object>(7);
		params.put("size", size);
		params.put("start_position", startPosition);
		params.put("platform", session.getOsVersion());
		params.put("screen_size", session.getScreenSize());
		params.put("orderby", orderBy);
		params.put("keyword", keyword);
		params.put("match_type", session.isFilterApps());

		new ApiAsyncTask(context, ACTION_SEARCH, handler, params).execute();
	}

	/**
	 * 获取商品详细信息
	 */
	public static void getProductDetailWithId(Context context,
			ApiRequestListener handler, String pId, String category) {

        HashMap<String, String> categoryMap = new HashMap<String, String>(3);
        categoryMap.put(Constants.CATEGORY_RCMD, "1");
        categoryMap.put(Constants.CATEGORY_APP, "2");
        categoryMap.put(Constants.CATEGORY_GAME, "3");

		final HashMap<String, Object> params = new HashMap<String, Object>(3);
		params.put("appid", pId);
		params.put("apptype", categoryMap.get(category));
        params.put("phone_number", "13800138000");
		new ApiAsyncTask(context, ACTION_GET_PRODUCT_DETAIL, handler, params)
				.execute();
	}

	/**
	 * 获取商品详细信息(包名)
	 */
	public static void getProductDetailWithPackageName(Context context,
			ApiRequestListener handler, int localVersion, String packageName) {

		final HashMap<String, Object> params = new HashMap<String, Object>(3);
		params.put("local_version", localVersion);
		params.put("packagename", packageName);

		new ApiAsyncTask(context, MarketAPI.ACTION_GET_DETAIL, handler, params)
				.execute();
	}

	/**
	 * 获取消费总额
	 */
	public static void getConsumeSum(Context context,
			ApiRequestListener handler, String uId) {

		final HashMap<String, Object> params = new HashMap<String, Object>(1);
		params.put("uid", uId);

		new ApiAsyncTask(context, ACTION_GET_CONSUMESUM, handler, params)
				.execute();
	}

	/**
	 * 同步用户购买记录
	 */
	public static void syncBuyLog(Context context, ApiRequestListener handler) {

		Session mSession = Session.get(context);

		final HashMap<String, Object> params = new HashMap<String, Object>(1);
		params.put("uid", mSession.getUid());

		new ApiAsyncTask(context, ACTION_SYNC_BUYLOG, handler, params)
				.execute();
	}

	/**
	 * 获取我的评级
	 */
	public static void getMyRating(Context context, ApiRequestListener handler,
			String pId) {

		Session mSession = Session.get(context);

		final HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("uid", mSession.getUid());
		params.put("p_id", pId);

		new ApiAsyncTask(context, ACTION_GET_MYRATING, handler, params)
				.execute();
	}

	/**
	 * 查询充值结果
	 */
	public static void queryChargeResult(Context context,
			ApiRequestListener handler, String orderId) {

		final HashMap<String, Object> params = new HashMap<String, Object>(1);
		params.put("order_id", orderId);

		new ApiAsyncTask(context, ACTION_QUERY_CHARGE_BY_ORDERID, handler,
				params).execute();
	}

	/**
	 * 同步充值卡信息
	 */
	public static void syncCardInfo(Context context, ApiRequestListener handler) {

		Session mSession = Session.get(context);

		final HashMap<String, Object> params = new HashMap<String, Object>(1);
		params.put("local_version", mSession.getCreditCardVersion());

		new ApiAsyncTask(context, ACTION_SYNC_CARDINFO, handler, params)
				.execute();
	}

	/**
	 * 获取消费明细
	 */
	public static void getConsumeDetail(Context context,
			ApiRequestListener handler, String uid, String type) {

		final HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("uid", uid);
		params.put("type", type);

		new ApiAsyncTask(context, ACTION_GET_CONSUME_DETAIL, handler, params)
				.execute();
	}

	/**
	 * 获取专题列表
	 */
	public static void getTopic(Context context, ApiRequestListener handler) {

		Session mSession = Session.get(context);

		final HashMap<String, Object> params = new HashMap<String, Object>(4);
		params.put("platform", mSession.getOsVersion());
		params.put("screen_size", mSession.getScreenSize());
		params.put("match_type", mSession.isFilterApps());

		new ApiAsyncTask(context, ACTION_GET_TOPIC, handler, params).execute();
	}

	/**
	 * 检查更新（机锋市场）
	 */
	public static void checkUpdate(Context context, ApiRequestListener handler) {

		Session mSession = Session.get(context);

		final HashMap<String, Object> params = new HashMap<String, Object>(4);
		params.put("package_name", mSession.getPackageName());
		params.put("version_code", mSession.getVersionCode());
		params.put("sdk_id", mSession.getCpid());
		params.put("type", mSession.getDebugType());

		new ApiAsyncTask(context, ACTION_CHECK_NEW_VERSION, handler, params)
				.execute();
	}

	 /**
	 * 检查更新（应用）
	 */
    public static void checkUpgrade(final Context context) {

        final HashMap<String, Object> params = new HashMap<String, Object>(1);
        params.put("upgradeList", Utils.getInstalledApps(context));

        new ApiAsyncTask(context, ACTION_CHECK_UPGRADE, new ApiRequestListener() {
            @Override
            public void onSuccess(int method, Object obj) {
                // do nothing
            }

            @Override
            public void onError(int method, int statusCode) {
                // do nothing
                Utils.D("check upgrade fail : " + statusCode);
            }
        }, params).execute();
    }

	/**
	 * 查询余额
	 * */
	public static void getBalance(Context context, ApiRequestListener handler) {
		Session mSession = Session.get(context);

		final HashMap<String, Object> params = new HashMap<String, Object>(1);
		params.put("uid", mSession.getUid());

		new ApiAsyncTask(context, ACTION_GET_BALANCE, handler, params)
				.execute();
	}

	/**
	 * 查询充值支付记录
	 * */
	public static void getPayLog(Context context, ApiRequestListener handler,
			int newStartPosition, int size) {
		Session mSession = Session.get(context);

		final HashMap<String, Object> params = new HashMap<String, Object>(3);
		params.put("uid", mSession.getUid());
		params.put("start_position", newStartPosition);
		params.put("size", size);

		new ApiAsyncTask(context, ACTION_GET_PAY_LOG, handler, params)
				.execute();
	}

	/**
	 * 检查是否有新splash需要下载
	 * */
	public static void checkNewSplash(Context context,
			ApiRequestListener handler) {

		Session mSession = Session.get(context);

		final HashMap<String, Object> params = new HashMap<String, Object>(4);
		params.put("package_name", mSession.getPackageName());
		params.put("version_code", mSession.getVersionCode());
		params.put("sdk_id", mSession.getCpid());
		params.put("time", mSession.getSplashTime());

		new ApiAsyncTask(context, ACTION_CHECK_NEW_SPLASH, handler, params)
				.execute();
	}

	/**
	 * 获取支付宝订单信息
	 * */
	public static void getAliPayOrder(Context context,
			ApiRequestListener handler, int money, String productName,
			String productDesc) {
		Session mSession = Session.get(context);

		final HashMap<String, Object> params = new HashMap<String, Object>(4);
		params.put("uid", Utils.getInt(mSession.getUid()));
		params.put("money", money);
		params.put("productName", productName);
		params.put("productDesc", productDesc);

		new ApiAsyncTask(context, ACTION_GET_ALIPAY_ORDER_INFO, handler, params)
				.execute();
	}

	/**
	 * 查询支付宝充值结果
	 */
	public static void queryAliPayResult(Context context,
			ApiRequestListener handler, String orderId) {
		final HashMap<String, Object> params = new HashMap<String, Object>(1);
		params.put("orderNo", orderId);

		new ApiAsyncTask(context, ACTION_QUERY_ALIPAY_RESULT, handler, params)
				.execute();
	}

	/**
	 * 提交所有应用 
	 */
    public static void submitAllInstalledApps(final Context context) {

        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        ArrayList<UpgradeInfo> appList = new ArrayList<UpgradeInfo>();
        for (PackageInfo info : packages) {
            UpgradeInfo app = new UpgradeInfo();
            app.name = String.valueOf(info.applicationInfo.loadLabel(pm));
            app.versionName = info.versionName;
            app.versionCode = info.versionCode;
            app.pkgName = info.packageName;
            appList.add(app);
        }
        final HashMap<String, Object> params = new HashMap<String, Object>(1);
        params.put("appList", appList);
        new ApiAsyncTask(context, MarketAPI.ACTION_SYNC_APPS, null, params).execute();
    }

}