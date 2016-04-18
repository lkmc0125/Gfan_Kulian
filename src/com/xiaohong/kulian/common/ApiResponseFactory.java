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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.Session;
import com.xiaohong.kulian.bean.AppDetailBean;
import com.xiaohong.kulian.bean.AppListBean;
import com.xiaohong.kulian.bean.GoodsListBean;
import com.xiaohong.kulian.bean.MessageListBean;
import com.xiaohong.kulian.bean.ReportResultBean;
import com.xiaohong.kulian.bean.TaskListBean;
import com.xiaohong.kulian.common.codec.binary.Base64;
import com.xiaohong.kulian.common.util.Crypter;
import com.xiaohong.kulian.common.util.DBUtils;
import com.xiaohong.kulian.common.util.SecurityUtil;
import com.xiaohong.kulian.common.util.StringUtils;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.util.XmlElement;
import com.xiaohong.kulian.common.vo.BuyLog;
import com.xiaohong.kulian.common.vo.CardsVerification;
import com.xiaohong.kulian.common.vo.CardsVerifications;
import com.xiaohong.kulian.common.vo.DownloadItem;
import com.xiaohong.kulian.common.vo.PayAndChargeLog;
import com.xiaohong.kulian.common.vo.PayAndChargeLogs;
import com.xiaohong.kulian.common.vo.ProductDetail;
import com.xiaohong.kulian.common.vo.SplashInfo;
import com.xiaohong.kulian.common.vo.UpdateInfo;
import com.xiaohong.kulian.common.vo.UpgradeInfo;

/**
 * API 响应结果解析工厂类，所有的API响应结果解析需要在此完成。
 * 
 * @author andrew
 * @date 2011-4-22
 * 
 */
public class ApiResponseFactory {

//    private static final String TAG = "ApiResponseFactory";

    /**
     * 解析市场API响应结果
     * 
     * @param action
     *            请求API方法
     * @param response
     *            HTTP Response
     * @return 解析后的结果（如果解析错误会返回Null）
     */
    public static Object getResponse(Context context, int action, HttpResponse response) {

        String inputBody = Utils.getStringResponse(response);
        if (TextUtils.isEmpty(inputBody)) {
            return null;
        }

        String requestMethod = "";
        Object result = null;
        Gson gson = new Gson();
        try {
            switch (action) {
            
            case MarketAPI.ACTION_REGISTER:

                // 注册
                requestMethod = "ACTION_REGISTER";
                result = parseLoginOrRegisterResult(context, inputBody);
                break;

            case MarketAPI.ACTION_LOGIN:

                // 登录
                requestMethod = "ACTION_LOGIN";
                result = parseLoginOrRegisterResult(context, inputBody);
                break;

            case MarketAPI.ACTION_GET_PRODUCT_DETAIL:
                
                // 获取应用详细
                requestMethod = "ACTION_GET_PRODUCT_DETAIL";
                result = gson.fromJson(inputBody, AppDetailBean.class);
                break;

            case MarketAPI.ACTION_GET_APP_LIST:
            {
                // 获取app列表
                requestMethod = "ACTION_GET_APP_LIST";
                result = gson.fromJson(inputBody, AppListBean.class);
                break;
            }

            case MarketAPI.ACTION_CHECK_NEW_VERSION:
                
                // 检查应用版本
                requestMethod = "ACTION_CHECK_NEW_VERSION";
                result = parseCheckNewVersion(context, inputBody);
                break;

            case MarketAPI.ACTION_CHECK_NEW_SPLASH:
                // 检查splash更新
                requestMethod = "ACTION_CHECK_NEW_SPLASH";
//                result = parseNewSplash(XmlElement.parseXml(in));
                break;
                
            case MarketAPI.ACTION_GET_SSID_LIST:
                requestMethod = "ACTION_GET_SSID_LIST";
                result = parseSSIDList(context, inputBody);
                break;

            case MarketAPI.ACTION_GET_TASK_LIST:
            case MarketAPI.ACTION_GET_GZH_TASK_LIST:
                result = gson.fromJson(inputBody, TaskListBean.class);
                break;

            case MarketAPI.ACTION_GET_MESSAGES:
                requestMethod = "ACTION_GET_MESSAGES";
                result = gson.fromJson(inputBody, MessageListBean.class);
                break;

            case MarketAPI.ACTION_SIGN_IN:
                requestMethod = "ACTION_SIGN_IN";
                result = parseSignIn(context, inputBody);
                break;

            case MarketAPI.ACTION_GET_GOODS_LIST:
                requestMethod = "ACTION_GET_GOODS_LIST";
                result = gson.fromJson(inputBody, GoodsListBean.class);
                break;
                
            case MarketAPI.ACTION_REPORT_APP_INSTALLED:
                requestMethod = "ACTION_REPORT_APP_INSTALLED";
                Log.d("free", "installed report  result str = " + inputBody);
                result = gson.fromJson(inputBody, ReportResultBean.class);
                Log.d("free", "installed report result = " + result);
                break;
                
            case MarketAPI.ACTION_REPORT_APP_LAUNCHED:
                requestMethod = "ACTION_REPORT_APP_LAUNCHED";
                Log.d("free", "launched result str = " + inputBody);
                result = gson.fromJson(inputBody, ReportResultBean.class);
                Log.d("free", "launched result = " + result);
                break;

            case MarketAPI.ACTION_REPORT_ORDER_PAY:
                requestMethod = "ACTION_REPORT_ORDER_PAY";
                Log.d("free", "report result str = " + inputBody);
                result = gson.fromJson(inputBody, ReportResultBean.class);
                Log.d("free", "report result = " + result);
                break;

            case MarketAPI.ACTION_ACCEPT_GZH_TASK:
                requestMethod = "ACTION_ACCEPT_GZH_TASK";
                Log.d("free", "accept task result str = " + inputBody);
//                result = gson.fromJson(inputBody, ReportResultBean.class);
//                Log.d("free", "report result = " + result);
                break;
            default:
                break;
            }

        } catch (Exception e) {
            Utils.D(requestMethod + " has Exception", e);
        }
        if (result != null) {
            Utils.D(requestMethod + "'s Response is : " + result.toString());
        } else {
            Utils.D(requestMethod + "'s Response is null");
        }
        return result;
    }

    /*
     * 解析注册或者登录结果
     */
    private static HashMap<String, Object> parseLoginOrRegisterResult(Context context, String body) {
        if (body == null) {
            return null;
        }
        HashMap<String, Object> result = null;
        try {
            //{"ret_msg":"success","invite_code":"523851","ret_code":0,"coin_num":5572,"token":"JNGdT3H0dB7iwfr64OVRMOw7+P+0MBOFFwPGYsUUVzzh+zHeywhpjFe6L2aX6izX"}
            JSONObject jsonObj = new JSONObject(body);
            result = new HashMap<String, Object>();
            result.put("ret_code", Integer.valueOf(jsonObj.getString("ret_code")));
            result.put("ret_msg", jsonObj.getString("ret_msg"));
            if (jsonObj.getInt("ret_code") == 0) {
                result.put(Constants.KEY_COIN_NUM, Integer.valueOf(jsonObj.getString("coin_num")));
                if (jsonObj.has("is_sign")) {
                    result.put(Constants.KEY_SIGN_IN_TODAY, jsonObj.getString("is_sign")); 
                }
                String token = jsonObj.getString("token");
                Log.d("free", "token = " + token);
                result.put(Constants.KEY_TOKEN, token);
            }
        } catch (JSONException e) {
            Utils.D("have json exception when parse search result from bbs", e);
        }

        return result;
    }

    /**
     * 检查是否有新版本
     */
    private static Object parseCheckNewVersion(Context context, String body) {
        if (body == null) {
            return null;
        }
        try {
            JSONObject jsonObj = new JSONObject(body);
            if (jsonObj.getInt("ret_code") == 0) {
                UpdateInfo updateInfo = new UpdateInfo();
                updateInfo.setForce(jsonObj.getString("force").equals("YES"));
                updateInfo.setVersionCode(jsonObj.getInt("versionCode"));
                updateInfo.setDescription(jsonObj.getString("desc"));
                updateInfo.setApkUrl(jsonObj.getString("url"));
                return updateInfo;
            }
        } catch (JSONException e) {
            Utils.D("have json exception when parse new version info", e);
        }
        return null;
    }

    /*
     * 解析新的Splash页
     */
    private static SplashInfo parseNewSplash(XmlElement xmlDocument) {
        
        if (xmlDocument == null) {
            return null;
        }
        
        SplashInfo info = new SplashInfo();
        XmlElement url = xmlDocument.getChild(SplashInfo.URL, 0);
        if(url != null) {
            info.url = url.getText();
        }
        XmlElement time = xmlDocument.getChild(SplashInfo.TIMESTAMP, 0);
        if(time != null) {
            info.timestamp = Utils.getLong(time.getText());
        }
        return info;
    }

    private static ArrayList<String> parseSSIDList(Context context, String body) {
        if (body == null) {
            return null;
        }
        try {
            JSONObject jsonObj = new JSONObject(body);
            ArrayList<String> ssidArray = null;
            if (jsonObj.getInt("ret_code") == 0) {
                ssidArray = new ArrayList<String>();
                JSONArray array = jsonObj.getJSONArray("ssidlist");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    ssidArray.add(obj.getString("ssid"));
                }
                return ssidArray;
            }
        } catch (JSONException e) {
            Utils.D("have json exception when parse new version info", e);
        }
        return null;
    }

    /*
     * 解析签到
     */
    private static HashMap<String, Object> parseSignIn(Context context, String body) {
        if (body == null) {
            return null;
        }
        HashMap<String, Object> result = null;
        try {
            // { "add_coin_num" : 20, "coin_num" : 60, "ret_code" : 0, "ret_msg" : "success" } 
            JSONObject jsonObj = new JSONObject(body);
            result = new HashMap<String, Object>();
            result.put("ret_msg", jsonObj.getString("ret_msg"));
            result.put("ret_code", Integer.valueOf(jsonObj.getInt("ret_code")));
            if (jsonObj.getInt("ret_code") == 0) {
                result.put(Constants.KEY_ADD_COIN_NUM, Integer.valueOf(jsonObj.getString("add_coin_num")));
                result.put(Constants.KEY_COIN_NUM, Integer.valueOf(jsonObj.getString("coin_num")));
            }
        } catch (JSONException e) {
            Utils.D("have json exception when parse search result from bbs", e);
        }

        return result;
    }

    /*
     * 解析支付宝订单结果
     */
    private static JSONObject parseGetAlipayOrderInfo(String in) throws JSONException {
        byte[] data = Base64.decodeBase64(in);
        return new JSONObject(new String(new Crypter().decrypt(data,
                SecurityUtil.SECRET_KEY_HTTP_CHARGE_ALIPAY)));
    }
    
}