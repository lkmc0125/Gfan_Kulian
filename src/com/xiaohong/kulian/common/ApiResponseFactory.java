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

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.Session;
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
                result = parseProductDetail(context, inputBody);
                break;

            case MarketAPI.ACTION_GET_APP_LIST:
            {
                // 获取app列表
                requestMethod = "ACTION_GET_APP_LIST";
                result = parseProductList(context, inputBody);
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

    private static Object parseProductDetail(Context context, String body) {
        if (body == null) {
            return null;
        }

        ProductDetail result = null;
        try {
            JSONObject jsonObj = new JSONObject(body);
            if (jsonObj.getInt("ret_code") == 0) {
                JSONObject product = jsonObj.getJSONObject("detail_info");

                if (product != null) {
                    result = new ProductDetail();
                    result.setPid(product.getString("AppId"));
//                    result.setProductType(product.getString(Constants.KEY_PRODUCT_TYPE));
                    result.setName(product.getString("AppName"));
//                    result.setPrice(Utils.getInt(product.getString(Constants.KEY_PRODUCT_PRICE)));
//                    result.setPayCategory(Utils.getInt(product.getString(Constants.KEY_PRODUCT_PAY_TYPE)));
//                    result.setRating(Utils.getInt(product.getString(Constants.KEY_PRODUCT_RATING)));
                    result.setIconUrl(product.getString("AppLogo"));
//                    result.setIconUrlLdpi(product.getString(Constants.KEY_PRODUCT_ICON_URL_LDPI));
                    result.setShotDes(product.getString("BriefSummary"));
                    result.setAppSize(Utils.getInt(product.getString("AppSize")));
//                    result.setSourceType(product.getString(Constants.KEY_PRODUCT_SOURCE_TYPE));
                    result.setPackageName(product.getString("PackageName"));
                    result.setVersionName(product.getString("AppVersion"));
                    result.setApkUrl(product.getString("AppSource"));
//                    result.setVersionCode(Utils.getInt(product
//                            .getString(Constants.KEY_PRODUCT_VERSION_CODE)));
//                    result.setCommentsCount(Utils.getInt(product
//                            .getString(Constants.KEY_PRODUCT_COMMENTS_COUNT)));
//                    result.setRatingCount(Utils.getInt(product
//                            .getString(Constants.KEY_PRODUCT_RATING_COUNT)));
//                    result.setDownloadCount(Utils.getInt(product
//                            .getString(Constants.KEY_PRODUCT_DOWNLOAD_COUNT)));
                    result.setLongDescription(product.getString("AppSummary"));
//                    result.setAuthorName(product.getString(Constants.KEY_PRODUCT_AUTHOR));
//                    result.setPublishTime(Utils.getInt(product
//                            .getString(Constants.KEY_PRODUCT_PUBLISH_TIME)));
//                    final String[] screenShot = new String[5];
//                    screenShot[0] = product.getString(Constants.KEY_PRODUCT_SCREENSHOT_1);
//                    screenShot[1] = product.getString(Constants.KEY_PRODUCT_SCREENSHOT_2);
//                    screenShot[2] = product.getString(Constants.KEY_PRODUCT_SCREENSHOT_3);
//                    screenShot[3] = product.getString(Constants.KEY_PRODUCT_SCREENSHOT_4);
//                    screenShot[4] = product.getString(Constants.KEY_PRODUCT_SCREENSHOT_5);

                    final ArrayList<String> screenShotList = new ArrayList<String>();
                    JSONArray array = product.getJSONArray("ImageSrcList");
                    for (int i = 0; i < array.length(); i++) {
                        screenShotList.add(array.getString(i));
                    }
                    if (screenShotList.size() > 0) {
                        String screenShotArray[] = (String[])screenShotList.toArray(new String[screenShotList.size()]);
                        result.setScreenshot(screenShotArray);
                    }
//                    result.setUpTime(Utils.getLong(product.getString(Constants.KEY_PRODUCT_UP_TIME)));
                }
            }
        } catch (JSONException e) {
          Utils.D("have json exception when parse search result from bbs", e);
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
            ArrayList<HashMap<String, Object>> productArray = null;
            result = new HashMap<String, Object>();
            result.put("ret_msg", jsonObj.getString("ret_msg"));
            if (jsonObj.getInt("ret_code") == 0) {
                result.put(Constants.KEY_COIN_NUM, jsonObj.getString("coin_num"));
            }
        } catch (JSONException e) {
            Utils.D("have json exception when parse search result from bbs", e);
        }

        return result;
    }

    private static HashMap<String, Object> parseProductList(Context context, String body) {
        if (body == null) {
            return null;
        }
        HashMap<String, Object> result = null;
        try {
            JSONObject jsonObj = new JSONObject(body);
            ArrayList<HashMap<String, Object>> productArray = null;
            result = new HashMap<String, Object>();
            // 获取已经安装的应用列表
            Session session = Session.get(context);
            ArrayList<String> installedApps = session.getInstalledApps();
            if (jsonObj.getInt("ret_code") == 0) {
                productArray = new ArrayList<HashMap<String, Object>>();
                JSONArray array = jsonObj.getJSONArray("applist");

                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    HashMap<String, Object> item = new HashMap<String, Object>();
                    item.put(Constants.KEY_PRODUCT_ID, obj.getString("AppId"));
                    String packageName = obj.getString("PackageName");
                    item.put(Constants.KEY_PRODUCT_PACKAGE_NAME, packageName);
                    item.put(Constants.KEY_PRODUCT_APK_URL, obj.getString("AppSource"));
                    int price = Utils.getInt(obj.getString("GiveCoin"));
                    String priceText = price == 0 ? context.getString(R.string.free) : context
                            .getString(R.string.coin_unit, price);
                    item.put(Constants.KEY_PRODUCT_PRICE, priceText);
                    // 忽略星标
                    item.put(Constants.KEY_PRODUCT_IS_STAR, false);
                    
                    if (installedApps.contains(packageName)) {
                        // 应用已经安装，显示已经安装的信息提示
                        item.put(Constants.KEY_PRODUCT_DOWNLOAD, Constants.STATUS_INSTALLED);
                    } else {
                        // 应用未安装，显示正常信息提示
                        item.put(Constants.KEY_PRODUCT_DOWNLOAD, Constants.STATUS_NORMAL);
                    }

                    item.put(Constants.KEY_PRODUCT_NAME, obj.getString("AppName"));
                    item.put(Constants.KEY_PRODUCT_AUTHOR, "author");
                    item.put(Constants.KEY_PRODUCT_SUB_CATEGORY, "category");
                    item.put(Constants.KEY_PRODUCT_PAY_TYPE, 0);
                    item.put(Constants.KEY_PRODUCT_RATING, 100);
                    item.put(Constants.KEY_PRODUCT_SIZE, obj.getString("AppSize"));
                    item.put(Constants.KEY_PRODUCT_ICON_URL, obj.getString("AppLogo"));
                    item.put(Constants.KEY_PRODUCT_SHORT_DESCRIPTION, obj.getString("BriefSummary"));
                    productArray.add(item);
                }
                result.put(Constants.KEY_PRODUCT_LIST, productArray);
            }
            
        } catch (JSONException e) {
            Utils.D("have json exception when parse app list", e);
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
                updateInfo.setForce(jsonObj.getBoolean("force"));
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
     * 解析支付宝订单结果
     */
    private static JSONObject parseGetAlipayOrderInfo(String in) throws JSONException {
        byte[] data = Base64.decodeBase64(in);
        return new JSONObject(new String(new Crypter().decrypt(data,
                SecurityUtil.SECRET_KEY_HTTP_CHARGE_ALIPAY)));
    }
    
}