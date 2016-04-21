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
import android.util.Log;

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

    public static final String API_BASE_URL = "http://livew.mobdsp.com/cb";

    // API URLS
    static final String[] API_URLS = {
            // ACTION_CHECK_NEW_VERSION
            API_BASE_URL + "/klappversion",
            // ACTION_LOGIN
            API_BASE_URL + "/applogin",
            // ACTION_REGISTER
            API_BASE_URL + "/appregister",
            // ACTION_GET_APP_LIST
            API_BASE_URL + "/applist_page",
            // ACTION_GET_PRODUCT_DETAIL
            API_BASE_URL + "/appdetail",
            // ACTION_CHECK_NEW_SPLASH
            API_BASE_URL + "/checkNewSplash",
            // ACTION_GET_SSID_LIST
            API_BASE_URL + "/get_ssidlist",
            // ACTION_GET_TASK_LIST
            API_BASE_URL + "/get_tasklist",
            // ACTION_GET_GZH_TASKLIST
            API_BASE_URL + "/get_gzhtasklist",
            // ACTION_GET_MESSAGES
            API_BASE_URL + "/app_broadcast",
            // ACTION_SIGN_IN
            API_BASE_URL + "/user_sign",
            // ACTION_GET_GOODS_LIST
            API_BASE_URL + "/get_goodslist", 
            // ACTION_REPORT_APP_INSTALLED
            API_BASE_URL + "/download_report", 
            // ACTION_REPORT_APP_LAUNCHED
            API_BASE_URL + "/applanch_report", 
            // ACTION_REPORT_ORDER_PAY
            API_BASE_URL + "/pay_report",
            // ACTION_ACCEPT_GZH_TASK
            API_BASE_URL + "/accept_gzh_task"
            };

    /** 检查更新 */
    public static final int ACTION_CHECK_NEW_VERSION = 0;
    /** 登录 */
    public static final int ACTION_LOGIN = 1;
    /** 注册 */
    public static final int ACTION_REGISTER = 2;
    /** 获取app列表 */
    public static final int ACTION_GET_APP_LIST = 3;
    /** 获取应用详细 */
    public static final int ACTION_GET_PRODUCT_DETAIL = 4;
    /** 检查SPLASH更新 */
    public static final int ACTION_CHECK_NEW_SPLASH = 5;
    /** 获取SSID列表 */
    public static final int ACTION_GET_SSID_LIST = 6;
    /** 获取任务列表 */
    public static final int ACTION_GET_TASK_LIST = 7;
    /** 获取公众号任务列表 */
    public static final int ACTION_GET_GZH_TASK_LIST = 8;
    /** 获取广播和个人消息 */
    public static final int ACTION_GET_MESSAGES = 9;
    /** 签到 */
    public static final int ACTION_SIGN_IN = 10;
    /** 获取商品列表 */
    public static final int ACTION_GET_GOODS_LIST = 11;
    /** 报告app安装完毕  */
    public static final int ACTION_REPORT_APP_INSTALLED = 12;
    /** 报告app已启动 */
    public static final int ACTION_REPORT_APP_LAUNCHED = 13;
    /** 报告订单支付成功 */
    public static final int ACTION_REPORT_ORDER_PAY = 14;
    /** 领取公众号任务 */
    public static final int ACTION_ACCEPT_GZH_TASK = 15;

    /**
     * Register API<br>
     * Do the register process, UserName, Password, Email must be provided.<br>
     */
    public static void register(Context context, ApiRequestListener handler, String username, String password,
            String verifyCode, String inviteCode) {

        final HashMap<String, Object> params = new HashMap<String, Object>(3);

        params.put("phone_number", username);
        params.put("passwd", Utils.getMD5(password));
        params.put("verify_code", verifyCode);
        if (inviteCode != null && inviteCode.length() > 0) {
            params.put("invite_code", inviteCode);
        }
        if (Utils.isLeShiMobile()) {
            params.put("leshi", 1);
        }

        new ApiAsyncTask(context, ACTION_REGISTER, handler, params).execute();
    }

    /**
     * Login API<br>
     * Do the login process, UserName, Password must be provided.<br>
     */
    public static void login(Context context, ApiRequestListener handler, String username, String password) {

        final HashMap<String, Object> params = new HashMap<String, Object>(2);

        params.put("phone_number", username);
        params.put("passwd", Utils.getMD5(password));

        new ApiAsyncTask(context, ACTION_LOGIN, handler, params).execute();
    }

    /**
     * 获取软件列表
     */
    public static void getAppList(Context context, ApiRequestListener handler, int page, String category) {

        Session session = Session.get(context);

        HashMap<String, String> categoryMap = new HashMap<String, String>(3);
        categoryMap.put(Constants.CATEGORY_RCMD, "1");
        categoryMap.put(Constants.CATEGORY_APP, "2");
        categoryMap.put(Constants.CATEGORY_GAME, "3");

        final HashMap<String, Object> params = new HashMap<String, Object>(3);
        params.put("apptype", categoryMap.get(category));
        params.put("page", page);
        params.put("phone_number", session.getUserName());

        new ApiAsyncTask(context, ACTION_GET_APP_LIST, handler, params).execute();
    }

    /**
     * 获取商品详细信息
     */
    public static void getProductDetailWithId(Context context, ApiRequestListener handler, String pId, String category) {

        Session session = Session.get(context);
        HashMap<String, String> categoryMap = new HashMap<String, String>(3);
        categoryMap.put(Constants.CATEGORY_RCMD, "1");
        categoryMap.put(Constants.CATEGORY_APP, "2");
        categoryMap.put(Constants.CATEGORY_GAME, "3");

        final HashMap<String, Object> params = new HashMap<String, Object>(3);
        params.put("appid", pId);
        params.put("apptype", categoryMap.get(category));
        params.put("phone_number", session.getUserName());
        new ApiAsyncTask(context, ACTION_GET_PRODUCT_DETAIL, handler, params).execute();
    }

    /**
     * 检查更新
     */
    public static void checkUpdate(Context context, ApiRequestListener handler) {

        Session session = Session.get(context);

        final HashMap<String, Object> params = new HashMap<String, Object>(4);
        params.put("version_code", session.getVersionCode());
        params.put("channel", session.getChannel());
        new ApiAsyncTask(context, ACTION_CHECK_NEW_VERSION, handler, params).execute();
    }

    /**
     * 检查是否有新splash需要下载
     * */
    public static void checkNewSplash(Context context, ApiRequestListener handler) {

        Session session = Session.get(context);

        final HashMap<String, Object> params = new HashMap<String, Object>(4);
        params.put("package_name", session.getPackageName());
        params.put("version_code", session.getVersionCode());
        params.put("sdk_id", session.getCpid());
        params.put("time", session.getSplashTime());

        new ApiAsyncTask(context, ACTION_CHECK_NEW_SPLASH, handler, params).execute();
    }

    /**
     * 获取wifi列表
     */
    public static void getSSIDList(Context context, ApiRequestListener handler) {
        Session session = Session.get(context);
        final HashMap<String, Object> params = new HashMap<String, Object>(1);
        params.put("phone_number", session.getUserName());

        new ApiAsyncTask(context, ACTION_GET_SSID_LIST, handler, params).execute();
    }

    /**
     * 获取任务列表
     * 
     * @param context
     * @param handler
     * @param page
     * @param category
     */
    public static void getTaskList(Context context, ApiRequestListener handler) {
        Session session = Session.get(context);
        final HashMap<String, Object> params = new HashMap<String, Object>(1);
        params.put("phone_number", session.getUserName());

        new ApiAsyncTask(context, ACTION_GET_TASK_LIST, handler, params).execute();
    }

    /**
     * 获取公众号任务列表
     * 
     * @param context
     * @param handler
     * @param page
     * @param category
     */
    public static void getGzhTaskList(Context context, ApiRequestListener handler) {
        Session session = Session.get(context);
        final HashMap<String, Object> params = new HashMap<String, Object>(1);
        String phoneNumber = session.getUserName();
        if (phoneNumber == null || phoneNumber.equals("")) {
            phoneNumber = "18980928189";
        }
        params.put("phone_number", phoneNumber);
        Log.d("free", "getGzhTaskList");
        new ApiAsyncTask(context, ACTION_GET_GZH_TASK_LIST, handler, params).execute();
    }

    public static void acceptGzhTask(Context context, ApiRequestListener handler, int taskId) {
        Session session = Session.get(context);
        final HashMap<String, Object> params = new HashMap<String, Object>(1);
        String phoneNumber = session.getUserName();
        params.put("phone_number", phoneNumber);
        params.put("task_id", String.valueOf(taskId));
        new ApiAsyncTask(context, ACTION_ACCEPT_GZH_TASK, handler, params).execute();
    }

    public static void getMessages(Context context, ApiRequestListener handler) {
        Session session = Session.get(context);
        final HashMap<String, Object> params = new HashMap<String, Object>(1);
        params.put("phone_number", session.getUserName());

        new ApiAsyncTask(context, ACTION_GET_MESSAGES, handler, params).execute();
    }

    /**
     * 签到
     */
    public static void signIn(Context context, ApiRequestListener handler) {
        Session session = Session.get(context);
        final HashMap<String, Object> params = new HashMap<String, Object>(1);
        params.put("phone_number", session.getUserName());

        new ApiAsyncTask(context, ACTION_SIGN_IN, handler, params).execute();
    }
    
    /**
     * 获取商品列表
     */
    public static void getGoodsList(Context context, ApiRequestListener handler) {
        Session session = Session.get(context);
        final HashMap<String, Object> params = new HashMap<String, Object>(1);
        params.put("phone_number", session.getUserName());

        new ApiAsyncTask(context, ACTION_GET_GOODS_LIST, handler, params).execute();
    }
    
    /**
     * 报告app已完成安装
     * @param context
     * @param handler
     */
    public static void reportAppInstalled(Context context, ApiRequestListener handler, int appId) {
        Session session = Session.get(context);
        final HashMap<String, Object> params = new HashMap<String, Object>(1);
        params.put("appid", appId + "");
        params.put("phone_number", session.getUserName());
        params.put("imei", session.getIMEI());

        new ApiAsyncTask(context, ACTION_REPORT_APP_INSTALLED, handler, params).execute();
    }
    
    /**
     * 报告app已完成安装
     * @param context
     * @param handler
     */
    public static void reportAppInstalled(Context context, ApiRequestListener handler, String packageName) {
        Session session = Session.get(context);
        final HashMap<String, Object> params = new HashMap<String, Object>(1);
        params.put("pkgname", packageName);
        params.put("phone_number", session.getUserName());
        params.put("imei", session.getIMEI());

        new ApiAsyncTask(context, ACTION_REPORT_APP_INSTALLED, handler, params).execute();
    }
    
    /**
     * 报告APP已启动
     * @param context
     * @param handler
     */
    public static void reportAppLaunched(Context context, ApiRequestListener handler, String packageName) {
        Session session = Session.get(context);
        final HashMap<String, Object> params = new HashMap<String, Object>(1);
        params.put("phone_number", session.getUserName());
        params.put("imei", session.getIMEI());
        params.put("pkgname", packageName);

        new ApiAsyncTask(context, ACTION_REPORT_APP_LAUNCHED, handler, params).execute();
    }
    
    /**
     * 报告APP已启动
     * @param context
     * @param handler
     */
    public static void reportAppLaunched(Context context, ApiRequestListener handler, int appId) {
        Session session = Session.get(context);
        final HashMap<String, Object> params = new HashMap<String, Object>(1);
        params.put("phone_number", session.getUserName());
        params.put("imei", session.getIMEI());
        params.put("appid", appId + "");

        new ApiAsyncTask(context, ACTION_REPORT_APP_LAUNCHED, handler, params).execute();
    }

    /**
     * 报告订单支付成功
        realBuyer 用于代买，表示实际买主
     */
    public static void reportOrderPay(Context context, ApiRequestListener handler, int goodsId, String outTradeNo, String realBuyer) {
        Session session = Session.get(context);
        final HashMap<String, Object> params = new HashMap<String, Object>(1);
        params.put("self_phone", session.getUserName());
        params.put("other_phone", realBuyer);
        params.put("out_trade_no", outTradeNo);
        params.put("goods_id", Integer.toString(goodsId));
        params.put("imei", session.getIMEI());
        new ApiAsyncTask(context, ACTION_REPORT_ORDER_PAY, handler, params).execute();
    }
}
