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
package com.xiaohong.kulian;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.xiaohong.kulian.common.util.Pair;
import com.xiaohong.kulian.common.util.SecurityUtil;

/**
 * SeesionManager for GfanMobile
 * 
 * @author  andrew.wang
 * @date    2010-9-27
 * @since   Version 0.5.0
 *
 */
public class SessionManager implements Observer {

    public static final String P_ISLOGIN = "pref.isLogin";
    public static final String P_MARKET_USERNAME = "pref.market.username";
    public static final String P_MARKET_PASSWORD = "pref.market.password";
    public static final String P_REMEMBER_PASSWORD = "pref.remember.password";
    public static final String P_UID = "pref.uid";
    public static final String P_SCREEN_SIZE = "pref.screen.size";
    public static final String P_OS_VERSION = "pref.os.version";
    public static final String P_CARD_VERSION = "pref.card.version";
    public static final String P_CATEGORY_VERSION = "pref.category.version";
    public static final String P_UPGRADE_NUM = "pref.upgrade.num";
    public static final String P_CLEAR_CACHE = "auto_clear_cache";
    public static final String P_USER_COOKIES = "pref.cookies";
    public static final String P_NO_APP_FILTER = "no_app_filter";

    public static final String P_UPDATE_AVAILABIE = "pref.update.available";
    public static final String P_UPDATE_VERSION_CODE = "pref.update.version.code";
    public static final String P_UPDATE_DESC = "pref.update.desc";
    public static final String P_UPDATE_URI = "pref.update.uri";
    public static final String P_UPDATE_VERSION_NAME = "pref.update.version.name";
    public static final String P_UPDATE_LEVEL = "pref.update.level";
    public static final String P_PRODUCT_UPDATE_CHECK_TIMESTAMP = "pref.product.update.timestamp";
    public static final String P_UPDATE_ID = "pref.update.id";
    
    public static final String P_LPNS_BINDED_DEVID = "pref.lpns.binded.devid";
	public static final String P_LPNS_IS_BINDED = "pref.lpns.is.binded";
	public static final String P_DEFAULT_CHARGE_TYPE = "pref.charge.defaultChargeType";
	
    // splash info
	public static final String P_SPLASH_TIME = "pref.splash.time";
	public static final String P_SPLASH_ID = "pref.splash.id";
	
	// version name
	public static final String P_CURRENT_VERSION = "pref.current.version";
	
    private static SessionManager mInstance;
    
    private SharedPreferences mPreference;
    private Context mContext;
	private LinkedList<Pair<String, Object>> mUpdateQueue = new LinkedList<Pair<String, Object>>();
	private Thread mCurrentUpdateThread;
    
    private SessionManager(Context context) {
        synchronized (this) {
            mContext = context;
            if (mPreference == null) {
                mPreference = PreferenceManager.getDefaultSharedPreferences(mContext);
            }
        }
    }
    
    public static SessionManager get(Context context) {
        if (mInstance == null) {
            mInstance = new SessionManager(context);
        }
        return mInstance;
    }
    
    private static final Method sApplyMethod = findApplyMethod();

    private static Method findApplyMethod() {
        try {
			Class<Editor> cls = SharedPreferences.Editor.class;
            return cls.getMethod("apply");
        } catch (NoSuchMethodException unused) {
            // fall through
        }
        return null;
    }

    /** Use this method to modify preference */
    public static void apply(SharedPreferences.Editor editor) {
        if (sApplyMethod != null) {
            try {
                sApplyMethod.invoke(editor);
                return;
            } catch (InvocationTargetException unused) {
                // fall through
            } catch (IllegalAccessException unused) {
                // fall through
            }
        }
        editor.commit();
    }
    
	public int isFilterApps() {
		boolean isFilter = mPreference.getBoolean(P_NO_APP_FILTER, false);
		return isFilter ? 0 : 1;
	}
    
    /**
     * Release all resources
     */
    public void close() {
        mPreference = null;
        mInstance = null;
    }
    
    private boolean isPreferenceNull() {
        if(mPreference == null) 
            return true;
        return false;
    }

	@SuppressWarnings("unchecked")
    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof Pair) {
            synchronized (mUpdateQueue) {
                if (data != null) {
                    mUpdateQueue.add((Pair<String, Object>) data);
                }
            }
            writePreferenceSlowly();
        }
    }
	
//    private void logTaskQueue() {
//        if (mUpdateQueue != null) {
//            Log.d("hibenate", " task queue is ");
//            int length = mUpdateQueue.size();
//            for (int i = 0; i < length; i++) {
//                Pair p = mUpdateQueue.get(i);
//                Log.d("hibenate", "key " + p.first + " value " + p.second);
//            }
//        }
//    }
	
	/*
	 * Do Hibernation slowly
	 */
	private void writePreferenceSlowly() {
		if (mCurrentUpdateThread != null) {
			if (mCurrentUpdateThread.isAlive()) {
				// the update thread is still running, 
				// so no need to start a new one
				return;
			}
		}
		
		// update the seesion value back to preference
		// ATTENTION: some more value will be add to the queue while current task is running
		mCurrentUpdateThread = new Thread() {
			
			@Override
			public void run() {
				
				try {
					// sleep 10secs to wait some concurrent task be 
					// inserted into the task queue
					sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				writePreference();
			}
			
		};
		mCurrentUpdateThread.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		mCurrentUpdateThread.start();
	}
	
	/*
	 * Do Hibernation immediately
	 */
	public void writePreferenceQuickly() {
		
		// update the seesion value back to preference
		// ATTENTION: some more value will be add to the queue while current task is running
		mCurrentUpdateThread = new Thread() {
			
			@Override
			public void run() {
				writePreference();
			}
			
		};
		mCurrentUpdateThread.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		mCurrentUpdateThread.start();
	}
	
	/**
	 * Write session value back to preference
	 */
	private void writePreference() {
		
		Editor editor = mPreference.edit();

		synchronized (mUpdateQueue) {
			while (!mUpdateQueue.isEmpty()) {

				// remove already unused reference from the task queue
				Pair<String, Object> updateItem = mUpdateQueue.remove();

				// the preference key
				final String key = (String) updateItem.first;

				if (P_UID.equals(key) || P_MARKET_USERNAME.equals(key)
						|| P_MARKET_PASSWORD.equals(key)) {
					editor.putString(key, SecurityUtil.encrypt(
					        String.valueOf(updateItem.second)));
				} else if (P_ISLOGIN.equals(key)
						|| P_LPNS_IS_BINDED.equals(key)
						|| P_UPDATE_AVAILABIE.equals(key)) {
					editor.putBoolean(key, (Boolean) updateItem.second);
				} else if (P_SCREEN_SIZE.equals(key)
						|| P_OS_VERSION.equals(key)
						|| P_LPNS_BINDED_DEVID.equals(key)
						|| P_UPDATE_DESC.equals(key)
						|| P_UPDATE_URI.equals(key)
						|| P_UPDATE_VERSION_NAME.equals(key)
						|| P_DEFAULT_CHARGE_TYPE.equals(key)) {
					editor.putString(key, (String) updateItem.second);
				} else if (P_UPDATE_VERSION_CODE.equals(key)
						|| P_UPDATE_LEVEL.equals(key)
						|| P_UPGRADE_NUM.equals(key)
						|| P_CARD_VERSION.equals(key)
						|| P_CURRENT_VERSION.equals(key)) {
					editor.putInt(key, (Integer) updateItem.second);
				} else if (P_PRODUCT_UPDATE_CHECK_TIMESTAMP.equals(key)
						|| P_SPLASH_TIME.equals(key)
						|| P_SPLASH_ID.equals(key)
						|| P_UPDATE_ID.equals(key)) {
					editor.putLong(key, (Long) updateItem.second);
				}
			}
		}
		// update the preference
		apply(editor);
	}
	
	public HashMap<String, Object> readPreference() {
		
		if (isPreferenceNull()) {
            return null;
        }

		HashMap<String, Object> data = new HashMap<String, Object>();
		String uidString = mPreference.getString(P_UID, null);
		String uid;
		if (uidString == null)
			uid = "";
		else {
			uid = SecurityUtil.decrypt(uidString);
		}
		data.put(P_UID, uid);
		data.put(P_SCREEN_SIZE, mPreference.getString(P_SCREEN_SIZE, "320#480"));
		data.put(P_OS_VERSION, mPreference.getInt(P_OS_VERSION, 0));
//		data.put(P_ISLOGIN, mPreference.getBoolean(P_ISLOGIN, false));
		String username = mPreference.getString(P_MARKET_USERNAME, "");
		if (username == null) {
			username = "";
		} else {
			username = SecurityUtil.decrypt(username);
		}
		data.put(P_MARKET_USERNAME, username);
		String password = mPreference.getString(P_MARKET_PASSWORD, null);
		if(password == null) {
			password = "";
		} else {
			password = SecurityUtil.decrypt(password);
		}
		data.put(P_MARKET_PASSWORD, password);
		data.put(P_CLEAR_CACHE, mPreference.getBoolean(P_CLEAR_CACHE, false));
		data.put(P_CARD_VERSION, mPreference.getInt(P_CARD_VERSION, -1));
		
		// cloud preference
		data.put(P_LPNS_IS_BINDED, mPreference.getBoolean(P_LPNS_IS_BINDED, false));
		data.put(P_LPNS_BINDED_DEVID, mPreference.getString(P_LPNS_BINDED_DEVID, ""));
		
		// update info
		data.put(P_UPDATE_AVAILABIE, mPreference.getBoolean(P_UPDATE_AVAILABIE, false));
		data.put(P_UPDATE_VERSION_CODE, mPreference.getInt(P_UPDATE_VERSION_CODE, -1));
		data.put(P_UPDATE_LEVEL, mPreference.getInt(P_UPDATE_LEVEL, -1));
		data.put(P_UPGRADE_NUM, mPreference.getInt(P_UPGRADE_NUM, 0));
		data.put(P_PRODUCT_UPDATE_CHECK_TIMESTAMP, mPreference.getLong(P_PRODUCT_UPDATE_CHECK_TIMESTAMP, -1));
		data.put(P_UPDATE_DESC, mPreference.getString(P_UPDATE_DESC, ""));
		data.put(P_UPDATE_URI, mPreference.getString(P_UPDATE_URI, ""));
		data.put(P_UPDATE_VERSION_NAME, mPreference.getString(P_UPDATE_VERSION_NAME, ""));
		data.put(P_UPDATE_ID, mPreference.getLong(P_UPDATE_ID, -1));
		
		//splash info
		data.put(P_SPLASH_ID, mPreference.getLong(P_SPLASH_ID, -1L));
		data.put(P_SPLASH_TIME, mPreference.getLong(P_SPLASH_TIME, 0L));
		
		// current version
		data.put(P_CURRENT_VERSION, mPreference.getInt(P_CURRENT_VERSION, -1));
		
		//charge
		data.put(P_DEFAULT_CHARGE_TYPE, mPreference.getString(P_DEFAULT_CHARGE_TYPE, null));
		
		return data;
	}
	
}
