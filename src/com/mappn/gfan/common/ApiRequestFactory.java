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
package com.mappn.gfan.common;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.pm.PackageInfo;
import android.text.TextUtils;

import com.mappn.gfan.Session;
import com.mappn.gfan.common.codec.digest.DigestUtils;
import com.mappn.gfan.common.util.SecurityUtil;
import com.mappn.gfan.common.util.Utils;
import com.mappn.gfan.common.vo.UpgradeInfo;

/**
 * 这个类是获取API请求内容的工厂方法
 * 
 * @author andrew
 * @date    2011-4-21
 *
 */
public class ApiRequestFactory {

    private static ArrayList<Integer> S_XML_REQUESTS = new ArrayList<Integer>();
    private static ArrayList<Integer> S_JSON_REQUESTS = new ArrayList<Integer>();
    private static ArrayList<Integer> S_ENCRYPT_REQUESTS = new ArrayList<Integer>();
    private static ArrayList<Integer> S_ENCODE_FORM_REQUESTS = new ArrayList<Integer>(); 
    static {
        S_XML_REQUESTS.add(MarketAPI.ACTION_GET_HOME_RECOMMEND);
        S_XML_REQUESTS.add(MarketAPI.ACTION_GET_CATEGORY);
        S_XML_REQUESTS.add(MarketAPI.ACTION_GET_SEARCH_KEYWORDS);
        S_XML_REQUESTS.add(MarketAPI.ACTION_SEARCH);
        S_XML_REQUESTS.add(MarketAPI.ACTION_GET_TOP_RECOMMEND);
        S_XML_REQUESTS.add(MarketAPI.ACTION_CHECK_NEW_SPLASH);
        S_XML_REQUESTS.add(MarketAPI.ACTION_GET_RANK_BY_CATEGORY);
        S_XML_REQUESTS.add(MarketAPI.ACTION_GET_GROW_FAST);
        S_XML_REQUESTS.add(MarketAPI.ACTION_GET_DETAIL);
        S_XML_REQUESTS.add(MarketAPI.ACTION_GET_PRODUCT_DETAIL);
        S_XML_REQUESTS.add(MarketAPI.ACTION_GET_COMMENTS);
        S_XML_REQUESTS.add(MarketAPI.ACTION_GET_MYRATING);
        S_XML_REQUESTS.add(MarketAPI.ACTION_ADD_COMMENT);
        S_XML_REQUESTS.add(MarketAPI.ACTION_ADD_RATING);
        S_XML_REQUESTS.add(MarketAPI.ACTION_GET_ALL_CATEGORY);
        S_XML_REQUESTS.add(MarketAPI.ACTION_GET_PRODUCTS);
        S_XML_REQUESTS.add(MarketAPI.ACTION_GET_TOPIC);
        S_XML_REQUESTS.add(MarketAPI.ACTION_GET_RECOMMEND_PRODUCTS);
        S_XML_REQUESTS.add(MarketAPI.ACTION_GET_REQUIRED);
        S_XML_REQUESTS.add(MarketAPI.ACTION_GET_DOWNLOAD_URL);
        S_XML_REQUESTS.add(MarketAPI.ACTION_CHECK_UPGRADE);
        S_XML_REQUESTS.add(MarketAPI.ACTION_CHECK_NEW_VERSION);
        S_XML_REQUESTS.add(MarketAPI.ACTION_PURCHASE_PRODUCT);
        S_XML_REQUESTS.add(MarketAPI.ACTION_SYNC_CARDINFO);
        S_XML_REQUESTS.add(MarketAPI.ACTION_QUERY_CHARGE_BY_ORDERID);
        S_XML_REQUESTS.add(MarketAPI.ACTION_SYNC_BUYLOG);
        S_XML_REQUESTS.add(MarketAPI.ACTION_SYNC_APPS);
        
        // JSON
        S_JSON_REQUESTS.add(MarketAPI.ACTION_BIND_ACCOUNT);
        S_JSON_REQUESTS.add(MarketAPI.ACTION_GET_ALIPAY_ORDER_INFO);
        S_JSON_REQUESTS.add(MarketAPI.ACTION_QUERY_ALIPAY_RESULT);

        // encrypt
        S_ENCRYPT_REQUESTS.add(MarketAPI.ACTION_REGISTER);
        S_ENCRYPT_REQUESTS.add(MarketAPI.ACTION_LOGIN);
        S_ENCRYPT_REQUESTS.add(MarketAPI.ACTION_GET_PAY_LOG);
        S_ENCRYPT_REQUESTS.add(MarketAPI.ACTION_CHARGE);
        S_ENCRYPT_REQUESTS.add(MarketAPI.ACTION_GET_BALANCE);
        
        // pay
        S_ENCODE_FORM_REQUESTS.add(MarketAPI.ACTION_GET_ALIPAY_ORDER_INFO);
        S_ENCODE_FORM_REQUESTS.add(MarketAPI.ACTION_QUERY_ALIPAY_RESULT);
        S_ENCODE_FORM_REQUESTS.add(MarketAPI.ACTION_BBS_SEARCH);
    }
    
    // justify the G-Header
    private static ArrayList<Integer> UCENTER_API = new ArrayList<Integer>();
    static {
        UCENTER_API.add(MarketAPI.ACTION_REGISTER);
        UCENTER_API.add(MarketAPI.ACTION_LOGIN);
        UCENTER_API.add(MarketAPI.ACTION_GET_BALANCE);
        UCENTER_API.add(MarketAPI.ACTION_QUERY_CHARGE);
        UCENTER_API.add(MarketAPI.ACTION_PURCHASE_PRODUCT);
        UCENTER_API.add(MarketAPI.ACTION_GET_CONSUMESUM);
        UCENTER_API.add(MarketAPI.ACTION_GET_CONSUME_DETAIL);
        UCENTER_API.add(MarketAPI.ACTION_GET_PAY_LOG);
        UCENTER_API.add(MarketAPI.ACTION_CHARGE);
        UCENTER_API.add(MarketAPI.ACTION_SYNC_CARDINFO);
        UCENTER_API.add(MarketAPI.ACTION_QUERY_CHARGE_BY_ORDERID);
    }
    
    // 不需要进行缓存的API
    public static ArrayList<Integer> API_NO_CACHE_MAP = new ArrayList<Integer>();
    static {
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_GET_HOME_RECOMMEND);
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_GET_TOP_RECOMMEND);
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_CHECK_NEW_SPLASH);
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_REGISTER);
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_LOGIN);
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_GET_BALANCE);
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_QUERY_CHARGE);
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_PURCHASE_PRODUCT);
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_GET_CONSUMESUM);
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_GET_CONSUME_DETAIL);
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_GET_PAY_LOG);
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_CHARGE);
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_SYNC_CARDINFO);
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_QUERY_CHARGE_BY_ORDERID);
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_BIND_ACCOUNT);
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_UNBIND);
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_ADD_COMMENT);
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_ADD_RATING);
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_GET_COMMENTS);
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_GET_MYRATING);
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_GET_REQUIRED);
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_GET_ALIPAY_ORDER_INFO);
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_QUERY_ALIPAY_RESULT);
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_GET_DOWNLOAD_URL);
    }
    
    /**
     * 获取Market API HttpReqeust 
     */
    public static HttpUriRequest getRequest(String url, int action, HttpEntity entity,
            Session session) throws IOException {

        if (MarketAPI.ACTION_UNBIND == action
                || MarketAPI.ACTION_GET_RANK_BY_CATEGORY == action) {
            HttpGet request = new HttpGet(url + session.getUid());
            return request;
        } else if (UCENTER_API.contains(action)) {
            HttpPost request = new HttpPost(url);
            // update the User-Agent
            request.setHeader("User-Agent", session.getUCenterApiUserAgent());
            request.setEntity(entity);
            return request;
        } else if (S_XML_REQUESTS.contains(action)) {
            HttpPost request = new HttpPost(url);
            // update the g-header
            request.setHeader("G-Header", session.getJavaApiUserAgent());
            request.addHeader("Accept-Encoding", "gzip");
            request.setEntity(AndroidHttpClient.getCompressedEntity(entity.getContent()));
            return request;
        } else {
            // for BBS search API
            HttpPost request = new HttpPost(url);
            request.setEntity(entity);
            return request;
        }
    }
    
    /**
     * 获取Market API HTTP 请求内容
     * 
     * @param action 请求的API Code
     * @param params 请求参数
     * @return 处理完成的请求内容
     * @throws UnsupportedEncodingException 假如不支持UTF8编码方式会抛出此异常
     */
    public static HttpEntity getRequestEntity(int action, Object params)
            throws UnsupportedEncodingException {

        if (S_XML_REQUESTS.contains(action)) {
            // 普通的XML请求内容
            return getXmlRequest(params);
        } else if (S_ENCODE_FORM_REQUESTS.contains(action)) {
            // URL encode form 请求内容
            return getFormRequest(action, params);
        } else if (S_JSON_REQUESTS.contains(action)) {
            // 普通的JSON请求内容
            return getJsonRequest(action, params);
        } else if (S_ENCRYPT_REQUESTS.contains(action)) {
            // 加密的请求内容
            return getEncryptRequest(action, params);
        } else {
            // 不需要请求内容
            return null;
        }
    }
    
    /**
     * 获取标准的XML请求内容，采用utf8编码方式
     * @return XML请求内容
     * @throws UnsupportedEncodingException 假如不支持UTF8编码方式会抛出此异常
     */
    private static StringEntity getXmlRequest(Object params) throws UnsupportedEncodingException {
        String body = generateXmlRequestBody(params);
        Utils.D("generate XML request body is : " + body);
        return new StringEntity(body, HTTP.UTF_8);
    }
    
    /**
     * 获取标准的JSON请求内容，采用utf8编码方式
     * @return JSON请求内容
     * @throws UnsupportedEncodingException 假如不支持UTF8编码方式会抛出此异常
     */
    private static StringEntity getJsonRequest(int action, Object params) throws UnsupportedEncodingException {
        String body = generateJsonRequestBody(params);
        Utils.D("generate JSON request body is : " + body);
        return new StringEntity(body, HTTP.UTF_8);
    }
    
    /**
     * 获取加密后的请求内容
     * @return ByteArrayEntity请求内容 
     */
    private static ByteArrayEntity getEncryptRequest(int action, Object params) {
        String body = generateXmlRequestBody(params);
        Utils.D("generate request body before encryption  is : " + body);

        // 加密处理
        if (action == MarketAPI.ACTION_CHARGE) {
            final byte[] encyptedBody = SecurityUtil.encryptHttpChargeBody(body);
            return new ByteArrayEntity(encyptedBody);
        } else {
            final byte[] encyptedBody = SecurityUtil.encryptHttpBody(body);
            return new ByteArrayEntity(encyptedBody);
        }
    }
    
    /**
     * 获取标准的表单请求内容，采用utf8编码方式
     * @return UrlEncodedFormEntity请求内容
     * @throws UnsupportedEncodingException 假如不支持UTF8编码方式会抛出此异常
     */
    @SuppressWarnings("unchecked")
    private static UrlEncodedFormEntity getFormRequest(int action, Object params)
            throws UnsupportedEncodingException {

        if (action == MarketAPI.ACTION_GET_ALIPAY_ORDER_INFO
                || action == MarketAPI.ACTION_QUERY_ALIPAY_RESULT) {
            String body = generateJsonRequestBody(params);
            Utils.D("generate JSON request body is : " + body);
            final byte[] encyptedBody = SecurityUtil.encryptHttpChargePalipayBody(body);
            String dataenc = new String(encyptedBody, HTTP.UTF_8);
            String cno = "03";
            String actionMethod = null;
            if (MarketAPI.ACTION_GET_ALIPAY_ORDER_INFO == action) {
                // 获取支付宝订单信息
                actionMethod = "addAlipayOrder";
            } else {
                // 查询支付宝充值结果
                actionMethod = "queryAlipayOrderIsSuccess";
            }
            final ArrayList<NameValuePair> postParams = new ArrayList<NameValuePair>(4);
            postParams.add(new BasicNameValuePair("action", actionMethod));
            postParams.add(new BasicNameValuePair("data", dataenc));
            postParams.add(new BasicNameValuePair("cno", cno));
            postParams.add(new BasicNameValuePair("sign", DigestUtils.md5Hex("action="
                    + actionMethod + "&data=" + dataenc + "&cno=" + cno
                    + SecurityUtil.KEY_HTTP_CHARGE_ALIPAY)));
            return new UrlEncodedFormEntity(postParams, HTTP.UTF_8);
        } else if (params instanceof ArrayList) {
            return new UrlEncodedFormEntity((ArrayList<NameValuePair>) params, HTTP.UTF_8);
        }
        return null;
    }
    
    /**
     * Generate the API XML request body
     */
    @SuppressWarnings("unchecked")
    private static String generateXmlRequestBody(Object params) {

        if (params == null) {
            return "<request version=\"2\"></request>";
        }

        HashMap<String, Object> requestParams;
        if (params instanceof HashMap) {
            requestParams = (HashMap<String, Object>) params;
        } else {
            return "<request version=\"2\"></request>";
        }

        final StringBuilder buf = new StringBuilder();

        // TODO: add local_version parameter if exist
        // 2010/12/29 update version to 2 to get comments from bbs
        buf.append("<request version=\"2\"");
        if (requestParams.containsKey("local_version")) {
            buf.append(" local_version=\"" + requestParams.get("local_version") + "\" ");
            requestParams.remove("local_version");
        }
        buf.append(">");

        // add parameter node
        final Iterator<String> keySet = requestParams.keySet().iterator();
        while (keySet.hasNext()) {
            final String key = keySet.next();

            if ("upgradeList".equals(key)) {
                buf.append("<products>");
                List<PackageInfo> productsList = (List<PackageInfo>) requestParams.get(key);
                for (PackageInfo info : productsList) {
                    buf.append("<product package_name=\"").append(info.packageName);
                    buf.append("\" version_code=\"").append(info.versionCode).append("\"/>");
                }
                buf.append("</products>");
                continue;
            } else if ("appList".equals(key)) {
                buf.append("<apps>");
                List<UpgradeInfo> productsList = (List<UpgradeInfo>) requestParams.get(key);
                for (UpgradeInfo info : productsList) {
                    buf.append("<app package_name=\"").append(info.pkgName);
                    buf.append("\" version_code=\"").append(info.versionCode);
                    buf.append("\" version_name=\"").append(info.versionName);
                    buf.append("\" app_name=\"").append(wrapText(info.name));
//                    buf.append("\" md5=\"").append(info.md5);
                    buf.append("\"/>");
                }
                buf.append("</apps>");
                continue;
            }

            buf.append("<").append(key).append(">");
            buf.append(requestParams.get(key));
            buf.append("</").append(key).append(">");
        }

        // add the enclosing quote
        buf.append("</request>");
        return buf.toString();
    }
    
    /**
     * Generate the API JSON request body 
     */
    @SuppressWarnings("unchecked")
    private static String generateJsonRequestBody(Object params) {

        if (params == null) {
            return "";
        }

        HashMap<String, Object> requestParams;
        if (params instanceof HashMap) {
            requestParams = (HashMap<String, Object>) params;
        } else {
            return "";
        }

        // add parameter node
        final Iterator<String> keySet = requestParams.keySet().iterator();
        JSONObject jsonObject = new JSONObject();
        try {
            while (keySet.hasNext()) {
                final String key = keySet.next();
                jsonObject.put(key, requestParams.get(key));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
        return jsonObject.toString();
    }
    
    private static final String[] REPLACE = { "&", "&amp;", "\"", "&quot;", "'", "&apos;", "<",
            "&lt;", ">", "&gt;" };
    
    private static String wrapText(String input) {

        if (!TextUtils.isEmpty(input)) {
            for (int i = 0, length = REPLACE.length; i < length; i += 2) {
                input = input.replace(REPLACE[i], REPLACE[i + 1]);
            }
            return input;
        } else {
            return "";
        }
    }
    
}
