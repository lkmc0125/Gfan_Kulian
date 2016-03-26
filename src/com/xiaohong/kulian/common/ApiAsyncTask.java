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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.xiaohong.kulian.Session;
import com.xiaohong.kulian.common.util.Utils;

/**
 * 机锋市场API请求任务
 * 
 * @author andrew.wang
 * @date 2010-10-27
 * @since Version 0.4.0
 */
public class ApiAsyncTask extends AsyncTask<Void, Void, Object> {

	// 超时（网络）异常
	public static final int TIMEOUT_ERROR = 600;
	// 业务异常
	public static final int BUSSINESS_ERROR = 610;

	private AndroidHttpClient mClient;
	private int mReuqestAction;
	private ApiRequestListener mHandler;
	private Object mParameter;
	private Session mSession;
	private Context mContext;
	private ResponseCacheManager mResponseCache;

    ApiAsyncTask(Context context, int action, ApiRequestListener handler, Object param) {
        this.mContext = context;
        this.mSession = Session.get(context);
        this.mReuqestAction = action;
        this.mHandler = handler;
        this.mParameter = param;
        this.mResponseCache = ResponseCacheManager.getInstance();
        this.mClient = HttpClientFactory.get().getHttpClient();
    }

	@Override
	protected Object doInBackground(Void... params) {
	    
        if (!Utils.isNetworkAvailable(mContext)) {
            return TIMEOUT_ERROR;
        }
	    
	    String requestUrl =  MarketAPI.API_URLS[mReuqestAction];
	    
	    HttpEntity requestEntity = null;
	    try {
            requestEntity = ApiRequestFactory.getRequestEntity(mReuqestAction, mParameter);
        } catch (UnsupportedEncodingException e) {
            Utils.D("OPPS...This device not support UTF8 encoding.[should not happend]");
            return BUSSINESS_ERROR;
        }
	    
        Object result = null;
        //缓存唯一标识
        String cacheKey = "";
        if (!ApiRequestFactory.API_NO_CACHE_MAP.contains(mReuqestAction)) {
            final boolean isBodyEmpty = (requestEntity == null);
            if (isBodyEmpty) {
                // if no http entity then directly use the request url as cache key
                cacheKey = Utils.getMD5(requestUrl);
            } else {
                // we only cache string request
                if (requestEntity instanceof StringEntity) {
                    try {
                        cacheKey = Utils.getMD5(requestUrl + EntityUtils.toString(requestEntity));
                    } catch (ParseException e) {
                        Utils.W("have ParseException when get cache key", e);
                    } catch (IOException e) {
                        Utils.W("have IOException when get cache key", e);
                    }
                }
            }
            // fetch result from the cache
            result = mResponseCache.getResponse(cacheKey);
            if (result != null) {
                Utils.V("retrieve response from the cache");
                return result;
            }
        }
        
        HttpResponse response = null;
        HttpUriRequest request = null;
        try {
            request = ApiRequestFactory.getRequest(requestUrl, mReuqestAction, requestEntity, mSession); 
            
            response = mClient.execute(request);
            
            final int statusCode = response.getStatusLine().getStatusCode();
            Utils.D("requestUrl " + requestUrl + " statusCode: " + statusCode);

            if (HttpStatus.SC_OK != statusCode) {
                // 非正常返回
                return statusCode;
            }

            // fetch result from remote server
            result = ApiResponseFactory.getResponse(mContext, mReuqestAction, response);
            if (result != null && !ApiRequestFactory.API_NO_CACHE_MAP.contains(mReuqestAction)) {
                mResponseCache.putResponse(cacheKey, result);
            }
            // 处理API Response，如果解析出错，返回BUSSINESS_ERROR【610】
            return result == null ? BUSSINESS_ERROR : result;
            
        } catch (IOException e) {
            Utils.D("Market API encounter the IO exception[mostly is timeout exception]", e);
            return TIMEOUT_ERROR;
        } finally {
            // release the connection
            if (request != null) {
                request.abort();
            }
            if (response != null) {
                try {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        entity.consumeContent();
                    }
                } catch (IOException e) {
                    Utils.D("release low-level resource error");
                }
            }
        }
	}

	@Override
	protected void onPostExecute(Object response) {

        if (mHandler == null) {
            return;
        }
        if (mContext instanceof Activity 
                && ((Activity) mContext).isFinishing()) {
            // 页面已经被关闭，无须进行后续处理
            return;
        }
		if (response == null) {
			mHandler.onError(this.mReuqestAction, BUSSINESS_ERROR);
			return;
		} else if (response instanceof Integer) {
		    
		    Integer statusCode = (Integer) response;
            if (!handleCommonError(statusCode)) {
                mHandler.onError(this.mReuqestAction, (Integer) response);
                return;
            }
		}

		mHandler.onSuccess(this.mReuqestAction, response);
	}
	
    // 225 请求的数据不存在
    public static final int SC_DATA_NOT_EXIST = 225;
    // 232 非法回复内容
    public static final int SC_ILLEGAL_COMMENT = 232;
    // 421 请求头参数为空或参数不完整（User-Agent等）
    public static final int SC_ILLEGAL_USER_AGENT = 421;
    // 422 请求xml解析错误
    public static final int SC_XML_ERROR = 422;
    // 423 请求xml中参数缺失或参数类型错误
    public static final int SC_XML_PARAMS_ERROR = 423;
    // 427 请求解密或解码错误
    public static final int SC_ENCODE_ERROR = 427;
    // 520 DB访问或SQL执行出错
    public static final int SC_SERVER_DB_ERROR = 520;
    
    /**
     * 处理公用Http Status Code
     * @param statusCode Http Status Code
     * @return 此Code是否被处理（True：已经被处理）
     */
	private boolean handleCommonError(int statusCode) {
	    
        if (statusCode == 200) {
            return true;
        }
//	    if(statusCode >= TIMEOUT_ERROR) {
//	        Utils.makeEventToast(mContext, mContext.getString(R.string.notification_server_error),
//                    false);
//	    } else if(statusCode >= HttpStatus.SC_INTERNAL_SERVER_ERROR) {
//            Utils.makeEventToast(mContext, mContext.getString(R.string.notification_server_error),
//                    false);
//	    } else if(statusCode >= HttpStatus.SC_BAD_REQUEST) {
//	        Utils.makeEventToast(mContext, mContext.getString(R.string.notification_client_error),
//                    false);
//	    }
	    return false;
	}

	/**
	 * 市场API请求监听器
	 * 
	 * @author andrew.wang
	 * @date 2010-10-28
	 * @since Version 0.4.0
	 */
	public interface ApiRequestListener {

		/**
		 * The CALLBACK for success aMarket API HTTP response
		 * 
		 * @param response
		 *            the HTTP response
		 */
		void onSuccess(int method, Object obj);

		/**
		 * The CALLBACK for failure aMarket API HTTP response
		 * 
		 * @param statusCode
		 *            the HTTP response status code
		 */
		void onError(int method, int statusCode);
	}

}