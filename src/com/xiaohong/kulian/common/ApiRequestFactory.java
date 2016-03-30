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
import org.apache.http.util.EntityUtils;

import android.content.pm.PackageInfo;
import android.text.TextUtils;

import com.xiaohong.kulian.Session;
import com.xiaohong.kulian.common.codec.digest.DigestUtils;
import com.xiaohong.kulian.common.util.SecurityUtil;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.vo.UpgradeInfo;

/**
 * 这个类是获取API请求内容的工厂方法
 * 
 * @author andrew
 * @date    2011-4-21
 *
 */
public class ApiRequestFactory {
    
    // 不需要进行缓存的API
    public static ArrayList<Integer> API_NO_CACHE_MAP = new ArrayList<Integer>();
    static {
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_CHECK_NEW_SPLASH);
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_REGISTER);
        API_NO_CACHE_MAP.add(MarketAPI.ACTION_LOGIN);
    }
    
    /**
     * 获取Market API HttpReqeust 
     */
    public static HttpUriRequest getRequest(String url, int action, HttpEntity entity,
            Session session) throws IOException {

        String requestString = url+"?"+EntityUtils.toString(entity);
        HttpGet request = new HttpGet(requestString);
        return request;
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
        return getGetRequest(params);
    }
    
    private static StringEntity getGetRequest(Object params) throws UnsupportedEncodingException {
        String paraString = generateGetParameters(params);
        return new StringEntity(paraString, HTTP.UTF_8);
    }
    
    @SuppressWarnings("unchecked")
    private static String generateGetParameters(Object params) {
        if (params == null) {
            return "";
        }

        HashMap<String, Object> requestParams;
        if (params instanceof HashMap) {
            requestParams = (HashMap<String, Object>) params;
        } else {
            return "";
        }

        final Iterator<String> keySet = requestParams.keySet().iterator();
        String parameters = "";
        while (keySet.hasNext()) {
            final String key = keySet.next();
            if (parameters.length() > 0) {
                parameters = parameters + "&";
            }
            parameters = parameters + key + "=" + requestParams.get(key);
        }

        return parameters;
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
