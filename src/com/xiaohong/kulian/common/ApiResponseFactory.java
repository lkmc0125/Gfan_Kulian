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

        InputStream in = null;
        String inputBody = null;
        if (MarketAPI.ACTION_GET_ALIPAY_ORDER_INFO == action
                || MarketAPI.ACTION_QUERY_ALIPAY_RESULT == action
                || MarketAPI.ACTION_GET_RANK_BY_CATEGORY == action
                || MarketAPI.ACTION_GET_PRODUCT_DETAIL == action
                ) {
            inputBody = Utils.getStringResponse(response);
            if (TextUtils.isEmpty(inputBody)) {
                return null;
            }
        } else {
            in = Utils.getInputStreamResponse(response);
            if (in == null) {
                return null;
            }
        }

        String requestMethod = "";
        Object result = null;
        try {
            switch (action) {
            
            case MarketAPI.ACTION_REGISTER:

                // 注册
                requestMethod = "ACTION_REGISTER";
                result = parseLoginOrRegisterResult(XmlElement.parseXml(in));
                break;

            case MarketAPI.ACTION_LOGIN:

                // 登录
                requestMethod = "ACTION_LOGIN";
                result = parseLoginOrRegisterResult(XmlElement.parseXml(in));
                break;
                
            case MarketAPI.ACTION_GET_COMMENTS:
                
                // 获取评论列表
                requestMethod = "ACTION_GET_COMMENTS";
                result = parseComments(XmlElement.parseXml(in));
                break;

            case MarketAPI.ACTION_GET_MYRATING:
                
                // 获取我的评级
                requestMethod = "ACTION_GET_MYRATING";
                result = parseMyRating(XmlElement.parseXml(in));
                break;

            case MarketAPI.ACTION_ADD_RATING:
                
                // 添加评级
                requestMethod = "ACTION_ADD_RATIONG";
                result = true;
                break;

            case MarketAPI.ACTION_ADD_COMMENT:
                
                // 添加评论
                requestMethod = "ACTION_ADD_COMMENT";
                result = true;
                break;

            case MarketAPI.ACTION_GET_PRODUCT_DETAIL:
                
                // 获取应用详细
                requestMethod = "ACTION_GET_PRODUCT_DETAIL";
                result = parseProductDetail2(context, inputBody);
                break;

            case MarketAPI.ACTION_GET_RANK_BY_CATEGORY:
            {
                // 获取排行列表
                requestMethod = "ACTION_GET_RANK_BY_CATEGORY";
                result = parseProductList(context, inputBody);
                break;
            }

            case MarketAPI.ACTION_GET_DETAIL:

                // 获取产品详细信息
                requestMethod = "ACTION_GET_DETAIL";
                result = parseProductDetail(XmlElement.parseXml(in));
                break;

            case MarketAPI.ACTION_SYNC_BUYLOG:

                // 获取消费记录
                requestMethod = "ACTION_SYNC_BUYLOG";
                result = parseSyncBuyLog(XmlElement.parseXml(in));
                break;

            case MarketAPI.ACTION_SYNC_APPS:
                
                // 提交安装应用信息
                requestMethod = "ACTION_SYNC_APPS";
                result = parseSyncApps(XmlElement.parseXml(in));
                break;

            case MarketAPI.ACTION_CHECK_NEW_VERSION:
                
                // 检查应用版本
                requestMethod = "ACTION_CHECK_NEW_VERSION";
                result = parseCheckNewVersion(XmlElement.parseXml(in));
                break;

            case MarketAPI.ACTION_PURCHASE_PRODUCT:
                
                // 购买应用
                requestMethod = "ACTION_PURCHASE_PRODUCT";
                result = true;
                break;
                
            case MarketAPI.ACTION_CHECK_UPGRADE:
                
                // 检查应用更新
                requestMethod = "ACTION_CHECK_UPGRADE";
                XmlElement r = null;
                result = parseUpgrade(context, r = XmlElement.parseXml(in));
                Log.i("test", "r:"+r);
                break;
                
            case MarketAPI.ACTION_CHECK_NEW_SPLASH:
                
                // 检查应用更新
                requestMethod = "ACTION_CHECK_NEW_SPLASH";
                result = parseNewSplash(XmlElement.parseXml(in));
                break;
                
            case MarketAPI.ACTION_GET_PAY_LOG:

                // 获取购买历史信息列表
                requestMethod = "ACTION_GET_PAY_LOG";
                result = parseGetPayLog(context, XmlElement.parseXml(in));
                break;
                
            case MarketAPI.ACTION_BIND_ACCOUNT:

                // 绑定用户手机
                requestMethod = "ACTION_BIND_ACCOUNT";
                result = true;
                break;
                
            case MarketAPI.ACTION_SYNC_CARDINFO:

                // 同步充值卡信息
                requestMethod = "ACTION_SYNC_CARDINFO";
                result = parseSyncCardinfo(context, XmlElement.parseXml(in));
                break;
           
            case MarketAPI.ACTION_CHARGE:
                
                // 查询充值结果
                requestMethod = "ACTION_CHARGE";
                result = parseChargeResult(XmlElement.parseXml(in));
                break;
                
            case MarketAPI.ACTION_QUERY_CHARGE_BY_ORDERID:
                
                // 查询充值结果
                requestMethod = "ACTION_QUERY_CHARGE_BY_ORDERID";
                result = parseQueryChargeResultByOderID(XmlElement.parseXml(in));
                break;
                
            case MarketAPI.ACTION_GET_BALANCE:
                
                // 查询余额
                requestMethod = "ACTION_GET_BALANCE";
                result = parseGetBalance(XmlElement.parseXml(in));
                break;
                
            case MarketAPI.ACTION_GET_ALIPAY_ORDER_INFO:
                
                // 解析支付宝订单结果
                requestMethod = "ACTION_GET_ALIPAY_ORDER_INFO";
                result = parseGetAlipayOrderInfo(inputBody);
                break;
                
            case MarketAPI.ACTION_QUERY_ALIPAY_RESULT:
                
                // 解析支付宝结果
                requestMethod = "ACTION_QUERY_ALIPAY_RESULT";
                result = parseGetAlipayOrderInfo(inputBody);
                break;
            
            case MarketAPI.ACTION_UNBIND:
            	
            	//解除绑定
            	result = true;
            	break;
                
            default:
                break;
            }

        } catch (XmlPullParserException e) {
            Utils.D(requestMethod + " has XmlPullParserException", e);
        } catch (IOException e) {
            Utils.D(requestMethod + " has IOException", e);
        } catch (JSONException e) {
            Utils.D(requestMethod + " has JSONException", e);
        }
        if (result != null) {
            Utils.D(requestMethod + "'s Response is : " + result.toString());
        } else {
            Utils.D(requestMethod + "'s Response is null");
        }
        return result;
    }

	/*
     * 获取所有分类列表
     */
    private static ArrayList<HashMap<String, Object>> parseAllCategory(XmlElement xmlDocument) {

        if (xmlDocument == null) {
            return null;
        }

        List<XmlElement> categorys = xmlDocument.getChildren(Constants.KEY_CATEGORY);
        ArrayList<HashMap<String, Object>> result = null;
        if (categorys != null) {
            result = new ArrayList<HashMap<String, Object>>();

            for (int i = 1; i < categorys.size(); i++) {
                XmlElement category = categorys.get(i);
                HashMap<String, Object> item = new HashMap<String, Object>();
                item.put(Constants.KEY_CATEGORY_NAME,
                        category.getAttribute(Constants.KEY_CATEGORY_NAME));
                item.put(Constants.KEY_APP_COUNT,
                        category.getAttribute(Constants.KEY_APP_COUNT));
                item.put(Constants.KEY_CATEGORY_ICON_URL,
                        category.getAttribute(Constants.KEY_CATEGORY_ICON_URL));
                
                String subCategoryText = category.getChild(Constants.KEY_SUB_CATEGORY, 0).getAttribute(
                        Constants.KEY_CATEGORY_NAME) + ", ";
                XmlElement category2 = category.getChild(Constants.KEY_SUB_CATEGORY, 1);
                if(category2 != null) {
                    subCategoryText +=  (category2.getAttribute(
                            Constants.KEY_CATEGORY_NAME) + ", ");
                }
                XmlElement category3 = category.getChild(Constants.KEY_SUB_CATEGORY, 2);
                if(category3 != null) {
                    subCategoryText += (category3.getAttribute(
                            Constants.KEY_CATEGORY_NAME) + ", ");
                }
                if (subCategoryText.length() > 0) {
                    subCategoryText = subCategoryText.substring(0, subCategoryText.length() - 2);
                }
                item.put(Constants.KEY_TOP_APP, subCategoryText);

                List<XmlElement> subCategorys = category.getChildren(Constants.KEY_SUB_CATEGORY);
                ArrayList<HashMap<String, Object>> subCategoryList = new ArrayList<HashMap<String, Object>>();
                for (XmlElement element : subCategorys) {
                    HashMap<String, Object> subCategory = new HashMap<String, Object>();
                    subCategory.put(Constants.KEY_CATEGORY_ID,
                            element.getAttribute(Constants.KEY_CATEGORY_ID));
                    subCategory.put(Constants.KEY_CATEGORY_NAME,
                            element.getAttribute(Constants.KEY_CATEGORY_NAME));
                    subCategory.put(Constants.KEY_APP_COUNT,
                            element.getAttribute(Constants.KEY_APP_COUNT));
                    subCategory.put(Constants.KEY_CATEGORY_ICON_URL,
                            element.getAttribute(Constants.KEY_CATEGORY_ICON_URL));
                    String app1 = element.getAttribute(Constants.KEY_APP_1);
                    String app2 = element.getAttribute(Constants.KEY_APP_2);
                    String app3 = element.getAttribute(Constants.KEY_APP_3);
                    String topApp = (TextUtils.isEmpty(app1) ? "" : app1 + ", ")
                            + (TextUtils.isEmpty(app2) ? "" : app2 + ", ")
                            + (TextUtils.isEmpty(app3) ? "" : app3 +  ", ");
                    if (topApp.length() > 0) {
                        topApp = topApp.substring(0, topApp.length() - 2);
                    }
                    subCategory.put(Constants.KEY_TOP_APP, topApp);
                    subCategoryList.add(subCategory);
                }
                item.put(Constants.KEY_SUB_CATEGORY, subCategoryList);
                result.add(item);
            }
            
            // 展开第一个一级列表
            XmlElement firstCategory = categorys.get(0);
            List<XmlElement> firstSubCategorys = firstCategory
                    .getChildren(Constants.KEY_SUB_CATEGORY);
            for (XmlElement element : firstSubCategorys) {
                HashMap<String, Object> item = new HashMap<String, Object>();
                item.put(Constants.KEY_CATEGORY_ID,
                            element.getAttribute(Constants.KEY_CATEGORY_ID));
                item.put(Constants.KEY_CATEGORY_NAME,
                        element.getAttribute(Constants.KEY_CATEGORY_NAME));
                item.put(Constants.KEY_APP_COUNT,
                        element.getAttribute(Constants.KEY_APP_COUNT));
                item.put(Constants.KEY_CATEGORY_ICON_URL,
                        element.getAttribute(Constants.KEY_CATEGORY_ICON_URL));
                String app1 = element.getAttribute(Constants.KEY_APP_1);
                String app2 = element.getAttribute(Constants.KEY_APP_2);
                String app3 = element.getAttribute(Constants.KEY_APP_3);
                String topApp = (TextUtils.isEmpty(app1) ? "" : app1 + ", ")
                        + (TextUtils.isEmpty(app2) ? "" : app2 + ", ")
                        + (TextUtils.isEmpty(app3) ? "" : app3 +  ", ");
                if (topApp.length() > 0) {
                    topApp = topApp.substring(0, topApp.length() - 2);
                }
                item.put(Constants.KEY_TOP_APP, topApp);
                result.add(item);
            }
        }
        return result;
    }
    
    private static Object parseProductDetail2(Context context, String body) {
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
     * 获取产品详细信息 
     */
    private static Object parseProductDetail(XmlElement xmlDocument) {

        if (xmlDocument == null) {
            return null;
        }

        XmlElement product = xmlDocument.getChild(Constants.KEY_PRODUCT, 0);
        ProductDetail result = null;

        if (product != null) {
            result = new ProductDetail();
            result.setPid(product.getAttribute(Constants.KEY_PRODUCT_ID));
            result.setProductType(product.getAttribute(Constants.KEY_PRODUCT_TYPE));
            result.setName(product.getAttribute(Constants.KEY_PRODUCT_NAME));
            result.setPrice(Utils.getInt(product.getAttribute(Constants.KEY_PRODUCT_PRICE)));
            result.setPayCategory(Utils.getInt(product.getAttribute(Constants.KEY_PRODUCT_PAY_TYPE)));
            result.setRating(Utils.getInt(product.getAttribute(Constants.KEY_PRODUCT_RATING)));
            result.setIconUrl(product.getAttribute(Constants.KEY_PRODUCT_ICON_URL));
            result.setIconUrlLdpi(product.getAttribute(Constants.KEY_PRODUCT_ICON_URL_LDPI));
            result.setShotDes(product.getAttribute(Constants.KEY_PRODUCT_SHORT_DESCRIPTION));
            result.setAppSize(Utils.getInt(product.getAttribute(Constants.KEY_PRODUCT_SIZE)));
            result.setSourceType(product.getAttribute(Constants.KEY_PRODUCT_SOURCE_TYPE));
            result.setPackageName(product.getAttribute(Constants.KEY_PRODUCT_PACKAGE_NAME));
            result.setVersionName(product.getAttribute(Constants.KEY_PRODUCT_VERSION_NAME));
            result.setVersionCode(Utils.getInt(product
                    .getAttribute(Constants.KEY_PRODUCT_VERSION_CODE)));
            result.setCommentsCount(Utils.getInt(product
                    .getAttribute(Constants.KEY_PRODUCT_COMMENTS_COUNT)));
            result.setRatingCount(Utils.getInt(product
                    .getAttribute(Constants.KEY_PRODUCT_RATING_COUNT)));
            result.setDownloadCount(Utils.getInt(product
                    .getAttribute(Constants.KEY_PRODUCT_DOWNLOAD_COUNT)));
            result.setLongDescription(product.getAttribute(Constants.KEY_PRODUCT_LONG_DESCRIPTION));
            result.setAuthorName(product.getAttribute(Constants.KEY_PRODUCT_AUTHOR));
            result.setPublishTime(Utils.getInt(product
                    .getAttribute(Constants.KEY_PRODUCT_PUBLISH_TIME)));
            final String[] screenShot = new String[5];
            screenShot[0] = product.getAttribute(Constants.KEY_PRODUCT_SCREENSHOT_1);
            screenShot[1] = product.getAttribute(Constants.KEY_PRODUCT_SCREENSHOT_2);
            screenShot[2] = product.getAttribute(Constants.KEY_PRODUCT_SCREENSHOT_3);
            screenShot[3] = product.getAttribute(Constants.KEY_PRODUCT_SCREENSHOT_4);
            screenShot[4] = product.getAttribute(Constants.KEY_PRODUCT_SCREENSHOT_5);
            result.setScreenshot(screenShot);
            final String[] screenShotLdpi = new String[5];
            screenShotLdpi[0] = product.getAttribute(Constants.KEY_PRODUCT_SCREENSHOT_LDPI_1);
            screenShotLdpi[1] = product.getAttribute(Constants.KEY_PRODUCT_SCREENSHOT_LDPI_2);
            screenShotLdpi[2] = product.getAttribute(Constants.KEY_PRODUCT_SCREENSHOT_LDPI_3);
            screenShotLdpi[3] = product.getAttribute(Constants.KEY_PRODUCT_SCREENSHOT_LDPI_4);
            screenShotLdpi[4] = product.getAttribute(Constants.KEY_PRODUCT_SCREENSHOT_LDPI_5);
            result.setScreenshotLdpi(screenShotLdpi);
            result.setUpReason(product.getAttribute(Constants.KEY_PRODUCT_UP_REASON));
            result.setUpTime(Utils.getLong(product.getAttribute(Constants.KEY_PRODUCT_UP_TIME)));
            result.setPermission(product.getAttribute(Constants.KEY_PRODUCT_PERMISSIONS));
        }
        return result;
    }
    
    /**
     * 解析同步应用
     */
    private static Object parseSyncApps(XmlElement xmlDocument) {

        if (xmlDocument == null) {
            return null;
        }
        UpdateInfo updateInfo = new UpdateInfo();

        updateInfo.setUpdageLevel(Integer.valueOf(xmlDocument.getChild(
                Constants.EXTRA_UPDATE_LEVEL, 0).getText()));
        updateInfo.setVersionCode(Integer.valueOf(xmlDocument.getChild(
                Constants.EXTRA_VERSION_CODE, 0).getText()));
        updateInfo.setVersionName(xmlDocument.getChild(
                Constants.EXTRA_VERSION_NAME, 0).getText());
        updateInfo.setDescription(xmlDocument.getChild(
                Constants.EXTRA_DESCRIPTION, 0).getText());
        updateInfo.setApkUrl(xmlDocument.getChild(Constants.EXTRA_URL, 0)
                .getText());

        return updateInfo;
    }

    /*
     * 解析我的评星结果
     */
    private static Object parseMyRating(XmlElement xmlDocument) {
        if (xmlDocument == null) {
            return null;
        }

        XmlElement element = xmlDocument.getChild(Constants.KEY_PRODUCT_RATING, 0);
        if (element != null) {
            return element.getAttribute(Constants.KEY_VALUE);
        }
        return null;
    }

    /*
     * 解析评论列表
     */
    private static Object parseComments(XmlElement xmlDocument) {
        if (xmlDocument == null) {
            return null;
        }
        HashMap<String, Object> result = null;
        XmlElement comments = xmlDocument.getChild(Constants.KEY_COMMENTS, 0);
        if (comments != null) {
            result = new HashMap<String, Object>();
            
            int totalSize = Utils.getInt(comments.getAttribute(Constants.KEY_TOTAL_SIZE));
            result.put(Constants.KEY_TOTAL_SIZE, totalSize);
            
            if (totalSize > 0) {
                ArrayList<HashMap<String, Object>> commentList = new ArrayList<HashMap<String, Object>>();
                List<XmlElement> children = comments.getChildren(Constants.KEY_COMMENT);
                for (XmlElement element : children) {
                    HashMap<String, Object> commentEntity = new HashMap<String, Object>();

                    commentEntity.put(Constants.KEY_COMMENT_ID,
                            element.getAttribute(Constants.KEY_COMMENT_ID));
                    commentEntity.put(Constants.KEY_COMMENT_AUTHOR,
                            element.getAttribute(Constants.KEY_COMMENT_AUTHOR));
                    commentEntity.put(Constants.KEY_COMMENT_BODY,
                            element.getAttribute(Constants.KEY_COMMENT_BODY));
                    long millis = Utils.getLong(element.getAttribute(Constants.KEY_COMMENT_DATE));
                    commentEntity.put(Constants.KEY_COMMENT_DATE, Utils.formatTime(millis));
                    commentList.add(commentEntity);
                }
                result.put(Constants.KEY_COMMENT_LIST, commentList);
            }
        }
        return result;
    }

    /*
     * 解析注册或者登录结果
     */
    private static HashMap<String, String> parseLoginOrRegisterResult(XmlElement xmlDocument) {

        if (xmlDocument == null) {
            return null;
        }

        HashMap<String, String> result = new HashMap<String, String>();
        result.put(Constants.KEY_USER_UID, xmlDocument.getChild(Constants.KEY_USER_UID, 0)
                .getText());
        result.put(Constants.KEY_USER_NAME, xmlDocument.getChild(Constants.KEY_USER_NAME, 0)
                .getText());
        result.put(Constants.KEY_USER_EMAIL, xmlDocument.getChild(Constants.KEY_USER_EMAIL, 0)
                .getText());
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
                JSONObject jsonBody = new JSONObject(body);
                productArray = new ArrayList<HashMap<String, Object>>();
                JSONArray array = jsonBody.getJSONArray("applist");

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

                    item.put(Constants.KEY_PRODUCT_NAME,
                            obj.getString("AppName"));
                    item.put(Constants.KEY_PRODUCT_AUTHOR,
                            "author");
                    item.put(
                            Constants.KEY_PRODUCT_SUB_CATEGORY,
                            "category");
                    // todo :: download url
                    item.put(Constants.KEY_PRODUCT_PAY_TYPE,
                            0);
                    item.put(Constants.KEY_PRODUCT_RATING,
                            100);
                    item.put(Constants.KEY_PRODUCT_SIZE,
                            obj.getString("AppSize"));
                    item.put(Constants.KEY_PRODUCT_ICON_URL,
                            obj.getString("AppLogo"));
                    item.put(Constants.KEY_PRODUCT_SHORT_DESCRIPTION,
                            obj.getString("BriefSummary"));
                    productArray.add(item);
                }
                result.put(Constants.KEY_PRODUCT_LIST, productArray);
            }
            
        } catch (JSONException e) {
            Utils.D("have json exception when parse search result from bbs", e);
        }
        return result;
    }
    
    /*
     * 检查可更新产品列表
     */
    private static String parseUpgrade(Context context, XmlElement xmlDocument) {

        if (xmlDocument == null) {
            return "";
        }

        XmlElement products = xmlDocument.getChild(Constants.KEY_PRODUCTS, 0);
        String count = "";
        if (products != null) {
            List<XmlElement> productList = products.getChildren(Constants.KEY_PRODUCT);
            if (productList == null) {
                // 没有可更新的应用
                return count;
            }
            ArrayList<UpgradeInfo> list = new ArrayList<UpgradeInfo>();
            for (XmlElement element : productList) {
                UpgradeInfo info = new UpgradeInfo();
                info.pid = element.getAttribute(Constants.KEY_PRODUCT_ID);
                info.pkgName = element.getAttribute(Constants.KEY_PRODUCT_PACKAGE_NAME);
                info.versionName = element.getAttribute(Constants.KEY_PRODUCT_VERSION_NAME);
                info.versionCode = Utils.getInt(element
                        .getAttribute(Constants.KEY_PRODUCT_VERSION_CODE));
                info.update = 0;
                list.add(info);
            }
            count = String.valueOf(DBUtils.addUpdateProduct(context, list));
        }
        return count;
    }

    /**
     * 获取同步购买列表
     */
    private static Object parseSyncBuyLog(XmlElement xmlDocument) {

        if (xmlDocument == null) {
            return null;
        }

        XmlElement products = xmlDocument.getChild(Constants.KEY_PRODUCTS, 0);
        if (products == null) {
            return null;
        }
        List<XmlElement> productList = products.getChildren(Constants.KEY_PRODUCT);
        if (productList == null) {
            return null;
        }
        List<BuyLog> result = new ArrayList<BuyLog>();
        for (int i = 0, length = productList.size(); i < length; i++) {
            XmlElement product = products.getChild(Constants.KEY_PRODUCT, i);
            BuyLog buyLog = new BuyLog();
            buyLog.pId = product.getAttribute(Constants.KEY_PRODUCT_ID);
            buyLog.packageName = product.getAttribute(Constants.PRODUCT_PACKAGENAME);
            result.add(buyLog);
        }
        return result;
    }

    /**
     * 检查是否有新版本
     */
    private static Object parseCheckNewVersion(XmlElement xmlDocument) {
        if (xmlDocument == null) {
            return null;
        }

        int level = Utils.getInt(xmlDocument.getChild(Constants.EXTRA_UPDATE_LEVEL, 0).getText());

        if (level == 0) {
            File root = new File(Environment.getExternalStorageDirectory(),
                    Constants.IMAGE_CACHE_DIR);
            root.mkdirs();
            File output = new File(root, "aMarket.apk");
            output.delete();
            return null;
        }
        
        UpdateInfo updateInfo = new UpdateInfo();
        updateInfo.setUpdageLevel(level);
        updateInfo.setVersionCode(Utils.getInt(xmlDocument
                .getChild(Constants.EXTRA_VERSION_CODE, 0).getText()));
        updateInfo.setVersionName(xmlDocument.getChild(Constants.EXTRA_VERSION_NAME, 0).getText());
        updateInfo.setDescription(xmlDocument.getChild(Constants.EXTRA_DESCRIPTION, 0).getText());
        updateInfo.setApkUrl(xmlDocument.getChild(Constants.EXTRA_URL, 0).getText());
        return updateInfo;
    }
    
    /*
     * 获取产品下载信息 
     */
    private static DownloadItem parseDownloadInfo(XmlElement xmlDocument) {

        if (xmlDocument == null) {
            return null;
        }

        DownloadItem item = null;
        XmlElement downloadInfo = xmlDocument.getChild(Constants.KEY_DOWNLOAD_INFO, 0);
        if (downloadInfo != null) {
            item = new DownloadItem();
            item.pId = downloadInfo.getAttribute(Constants.KEY_PRODUCT_ID);
            item.packageName = downloadInfo.getAttribute(Constants.KEY_PRODUCT_PACKAGE_NAME);
            item.url = downloadInfo.getAttribute(Constants.KEY_PRODUCT_DOWNLOAD_URI);
            item.fileMD5 = downloadInfo.getAttribute(Constants.KEY_PRODUCT_MD5);
        }
        return item;
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
    
    /*
     * 解析支付历史
     */
    private static PayAndChargeLogs parseGetPayLog(Context context,
            XmlElement xmlDocument) {

        if (xmlDocument == null) {
            return null;
        }
        PayAndChargeLogs result = null;
        XmlElement logs = xmlDocument.getChild(Constants.KEY_PAY_LOGS, 0);
        if (logs != null) {
            result = new PayAndChargeLogs();
            result.endPosition = Utils.getInt(logs.getAttribute(Constants.KEY_END_POSITION));
            result.totalSize = Utils.getInt(logs.getAttribute(Constants.KEY_TOTAL_SIZE));

            List<XmlElement> consumes = logs.getChildren(Constants.KEY_PAY_CONSUME);
            getPayAndChargeLog(consumes, result, Constants.KEY_PAY_CONSUME);
            List<XmlElement> charges = logs
                    .getChildren(Constants.KEY_PAY_CHARGE);
            getPayAndChargeLog(charges, result, Constants.KEY_PAY_CHARGE);

            List<XmlElement> buyApps = logs
                    .getChildren(Constants.KEY_PAY_BUY_APP);
            getPayAndChargeLog(buyApps, result, Constants.KEY_PAY_BUY_APP);
        }
        return result;
    }
    
    /*
     * 读取consume,charge,buy_app标签
     */
    private static void getPayAndChargeLog(List<XmlElement> tags, PayAndChargeLogs result,
            String flag) {
        if (tags != null && tags.size() > 0) {
            for (XmlElement tag : tags) {
                PayAndChargeLog log = new PayAndChargeLog();
                log.name = tag.getAttribute(Constants.KEY_PAY_FLAG);
                log.id = Utils.getInt(tag.getAttribute(Constants.KEY_PAY_ORDER_ID));
                log.desc = tag.getAttribute(Constants.KEY_PAY_DESCRIPTION);
                log.time = Utils.formatDate(Utils.getLong(tag.getAttribute(Constants.KEY_PAY_TIME)));
                log.payment = (int) Utils.getFloat(tag.getAttribute(Constants.KEY_PAY_MONEY));

                if (Constants.KEY_PAY_CONSUME.equals(flag)) {
                    log.type = PayAndChargeLog.TYPE_CONSUME;
                } else if (Constants.KEY_PAY_CHARGE.equals(flag)) {
                    log.type = PayAndChargeLog.TYPE_CHARGE;
                } else if (Constants.KEY_PAY_BUY_APP.equals(flag)) {
                    log.id = Utils.getInt(tag.getAttribute(Constants.KEY_PRODUCT_ID));
                    log.name = tag.getAttribute(Constants.KEY_PRODUCT_NAME);
                    log.iconUrl = tag.getAttribute(Constants.KEY_CATEGORY_ICON_URL);
                    log.type = PayAndChargeLog.TYPE_MARKET;
                    log.sourceType = Utils.getInt(tag
                            .getAttribute(Constants.KEY_PRODUCT_SOURCE_TYPE));
                }
                result.payAndChargeLogList.add(log);
            }
        }
    }
    
    /*
     * 同步充值卡信息
     */
    private static CardsVerifications parseSyncCardinfo(Context context, XmlElement xmlDocument) {

        if (xmlDocument == null) {
            return null;
        }
        CardsVerifications results = new CardsVerifications();
        results.version = Utils.getInt(xmlDocument.getAttribute(Constants.REMOTE_VERSION));

        List<XmlElement> cards = xmlDocument.getChildren(Constants.PAY_CARD);
        for (XmlElement card : cards) {
            CardsVerification subCard = new CardsVerification();
            subCard.name = card.getAttribute(Constants.KEY_USER_NAME);
            subCard.pay_type = card.getAttribute(Constants.PAY_TYPE);
            subCard.accountNum = Utils.getInt(card.getAttribute(Constants.ACCOUNT_LEN));
            subCard.passwordNum = Utils.getInt(card.getAttribute(Constants.PASSWORD_LEN));
            subCard.credit = card.getAttribute(Constants.PAY_CREDIT);
            results.cards.add(subCard);
        }
        return results;
    }
    
    /*
     * 解析充值结果
     */
    private static String parseChargeResult(XmlElement xmlDocument) {

        if (xmlDocument == null) {
            return null;
        }

        XmlElement result = xmlDocument.getChild(Constants.PAY_RESULT, 0);
        if (result != null) {
            return result.getAttribute(Constants.KEY_PAY_ORDER_ID);
        }
        return null;
    }
    
    /*
     * 解析充值结果(按订单号)
     */
    private static int parseQueryChargeResultByOderID(XmlElement xmlDocument) {

        if (xmlDocument == null) {
            return 0;
        }

        XmlElement result = xmlDocument.getChild(Constants.PAY_RESULT, 0);
        if (result != null) {
            return Utils.getInt(result.getAttribute(Constants.KEY_PAY_STATUS));
        }
        return 0;
    }
    
    /*
     * 解析查询余额
     */
    private static String parseGetBalance(XmlElement xmlDocument) {

        if (xmlDocument == null) {
            return null;
        }

        XmlElement result = xmlDocument.getChild(Constants.RESULT, 0);
        if (result != null) {
            return result.getText();
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