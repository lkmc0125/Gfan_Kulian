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
package com.mappn.gfan.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.mappn.gfan.Session;
import com.mappn.gfan.common.vo.BuyLog;
import com.mappn.gfan.common.vo.CardsVerification;
import com.mappn.gfan.common.vo.CardsVerifications;
import com.mappn.gfan.common.vo.UpgradeInfo;

/**
 * 关于Content Provider的工具类
 * 
 * @author andrew
 * @date    2011-4-26
 * @since   Version 0.7.0
 */
public class DBUtils {

    /**
     * 清除所有搜索历史
     * @param context Application Context
     */
    public static void clearSearchHistory(Context context) {

        context.getContentResolver().delete(MarketProvider.SEARCH_CONTENT_URI, null, null);
    }
    
    /**
     * 添加搜索历史关键词
     * @param context Application Context
     * @param keyword 搜索关键词
     */
    public static Uri addSearchItem(Context context, String keyword) {

        ContentValues values = new ContentValues();
        values.put(MarketProvider.COLUMN_SEARCH_KEY_WORD, keyword);
        return context.getContentResolver().insert(MarketProvider.SEARCH_CONTENT_URI, values);
    }
    
    /**
     * 获得搜索历史关键词（最多20条数据）
     * @return 搜索关键词列表 ArrayList<String>
     */
    public static ArrayList<String> querySearchHistory(Context context) {

        ArrayList<String> result = new ArrayList<String>();
        Cursor cursor = context.getContentResolver().query(MarketProvider.SEARCH_CONTENT_URI, null,
                null, null,  MarketProvider.COLUMN_ID + " DESC");
        if (cursor != null) {
            final int fieldIndex = cursor.getColumnIndex(MarketProvider.COLUMN_SEARCH_KEY_WORD);
            int index = 0;
            while (cursor.moveToNext()) {

                if (index > 19) {
                    break;
                }
                result.add(cursor.getString(fieldIndex));
                index++;
            }
            cursor.close();
        }
        return result;
    }
    
	/**
	 * 添加购买记录
	 */
    public static void insertBuyLog(Context context, BuyLog log) {
        ContentValues values = new ContentValues();
        log.onAddToDatabase(values);
        context.getContentResolver().insert(MarketProvider.BUY_CONTENT_URI, values);
    }
	
	/**
	 * 查询是否已经购买
	 */
    public static boolean isBought(Context context, String pid) {
        Cursor c = context.getContentResolver().query(MarketProvider.BUY_CONTENT_URI,
                new String[] { MarketProvider.COLUMN_P_ID, MarketProvider.COLUMN_P_PACKAGE_NAME },
                MarketProvider.COLUMN_P_ID + " = '" + pid + "'", null, null);
        try {
            if (c != null && c.getCount() > 0) {
                return true;
            }
            return false;
        } finally {
            c.close();
        }
    }
    
    /**
     * 刷新本地已购买列表
     */
    public static void updateBuyedList(Context context, List<BuyLog> buyLogList) {
        final ContentResolver cr = context.getContentResolver();
        cr.delete(MarketProvider.BUY_CONTENT_URI, null, null);
        ContentValues values = null;
        for (BuyLog log : buyLogList) {
            values = new ContentValues();
            log.onAddToDatabase(values);
            cr.insert(MarketProvider.BUY_CONTENT_URI, values);
        }
    }
    
	/**
	 * 获取已经购买的应用列表
	 */
    public static List<BuyLog> getUpdateBuyedList(Context context) {
        List<BuyLog> buyLogList = new ArrayList<BuyLog>();
        BuyLog log = null;
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(MarketProvider.BUY_CONTENT_URI, null, null, null, null);
        if (c != null && c.getCount() > 0 && c.moveToFirst()) {
            do {
                log = new BuyLog();
                log.pId = c.getString(c.getColumnIndex(MarketProvider.COLUMN_P_ID));
                log.packageName = c.getString(c
                        .getColumnIndex(MarketProvider.COLUMN_P_PACKAGE_NAME));
                buyLogList.add(log);
            } while (c.moveToNext());
            c.close();
        }
        return buyLogList;
    }

	public static void updataCardsVerification(Context context, List<CardsVerification> cards) {
		final ContentResolver cr = context.getContentResolver();
		cr.delete(MarketProvider.CARD_CONTENT_URI, null, null);
		for (CardsVerification card : cards) {
			ContentValues values = new ContentValues();
			card.onAddToDatabase(values);
			cr.insert(MarketProvider.CARD_CONTENT_URI, values);
		}
	}
	
	public static CardsVerifications getAllCardsVerification(Context context) {
		CardsVerifications cards = new CardsVerifications();
		CardsVerification card = null;
		final ContentResolver cr = context.getContentResolver();
		String selection = null;
		String[] selectionArgs = null;
		Cursor c = cr.query(MarketProvider.CARD_CONTENT_URI, null, selection, selectionArgs, null);
		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				do {
					card = new CardsVerification();
                    card.name = c.getString(c.getColumnIndex(MarketProvider.COLUMN_CARD_NAME));
                    card.pay_type = c.getString(c
                            .getColumnIndex(MarketProvider.COLUMN_CARD_PAY_TYPE));
                    card.accountNum = c.getInt(c
                            .getColumnIndex(MarketProvider.COLUMN_CARD_ACCOUNTNUM));
                    card.passwordNum = c.getInt(c
                            .getColumnIndex(MarketProvider.COLUMN_CARD_PASSWORDNUM));
                    card.credit = c.getString(c.getColumnIndex(MarketProvider.COLUMN_CARD_CREDIT));
					cards.cards.add(card);
				} while (c.moveToNext());
			}
			c.close();
		}
		return cards;
	}

    /**
     * 添加可更新应用
     * @param context Application Context
     * @param item API返回的可更新应用
     */
    public static Uri addUpdateProduct(Context context, UpgradeInfo item) {
        ContentValues values = new ContentValues();
        values.put(MarketProvider.COLUMN_P_ID, item.pid);
        values.put(MarketProvider.COLUMN_P_PACKAGE_NAME, item.pkgName);
        values.put(MarketProvider.COLUMN_P_NEW_VERSION_NAME, item.versionName);
        values.put(MarketProvider.COLUMN_P_NEW_VERSION_CODE, item.versionCode);
        return context.getContentResolver().insert(MarketProvider.UPDATE_CONTENT_URI, values);
    }
    
    /**
     * 添加可更新应用
     * @param context Application Context
     * @param item API返回的可更新应用列表
     */
    public static int addUpdateProduct(Context context, ArrayList<UpgradeInfo> list) {

        Cursor cursor = context.getContentResolver().query(MarketProvider.UPDATE_CONTENT_URI, null,
                null, null, null);
        
        HashMap<String, UpgradeInfo> dbData = null;
        if(cursor != null) {
            dbData = new HashMap<String, UpgradeInfo>();
            for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                UpgradeInfo info = new UpgradeInfo();
                info.pkgName = cursor.getString(cursor
                        .getColumnIndex(MarketProvider.COLUMN_P_PACKAGE_NAME));
                info.versionCode = cursor.getInt(cursor
                        .getColumnIndex(MarketProvider.COLUMN_P_NEW_VERSION_CODE));
                info.update = cursor.getInt(cursor
                        .getColumnIndex(MarketProvider.COLUMN_P_IGNORE));
                dbData.put(info.pkgName, info);
            }
        }
        
        if (dbData != null) {
            for (UpgradeInfo info : list) {
                // 如果用户忽略了此版本的更新，不应该再次提醒
                if (dbData.containsKey(info.pkgName)) {
                    UpgradeInfo dbInfo = dbData.get(info.pkgName);
                    if (info.versionCode <= dbInfo.versionCode 
                            && dbInfo.update == 1) {
                        // 用户忽略了此更新
                        info.update = 1;
                    }
                }
            }
        }
        
        ContentValues[] values = new ContentValues[list.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = list.get(i).getContentValues();
        }
        
        context.getContentResolver().delete(MarketProvider.UPDATE_CONTENT_URI, null, null);
        int result = context.getContentResolver().bulkInsert(MarketProvider.UPDATE_CONTENT_URI,
                values);
        
        if (result > 0) {
            Session session = Session.get(context);
            session.setUpdateList();
        }
        cursor.close();
        return result;
    }
    
    /**
     * 已经更新，删除项目
     * @param context
     * @param packageName
     */
    public static void removeUpgradable(Context context, String packageName) {
        final ContentResolver cr = context.getContentResolver();
        String selection = MarketProvider.COLUMN_P_PACKAGE_NAME + " = ? ";
        String[] selectionArgs = new String[] { packageName };
        cr.delete(MarketProvider.UPDATE_CONTENT_URI, selection, selectionArgs);
    }
    
    /**
     * 忽略此版本的更新
     */
    public static int ignoreUpdate(Context context, String packageName) {
        ContentValues values = new ContentValues();
        String selection = MarketProvider.COLUMN_P_PACKAGE_NAME + " = ? ";
        String[] selectionArgs = new String[] { packageName };
        values.put(MarketProvider.COLUMN_P_IGNORE, 1);
        return context.getContentResolver().update(MarketProvider.UPDATE_CONTENT_URI, values,
                selection, selectionArgs);
    }

    /**
     * 查询可更新应用
     * @param context Application Context
     */
    public static HashMap<String, UpgradeInfo> queryUpdateProduct(Context context) {

        HashMap<String, UpgradeInfo> result = new HashMap<String, UpgradeInfo>();
        String selection = MarketProvider.COLUMN_P_IGNORE + " = ? ";
        String[] selectionArgs = new String[] { "0" };
        Cursor cursor = context.getContentResolver().query(MarketProvider.UPDATE_CONTENT_URI, null,
                selection, selectionArgs, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                UpgradeInfo info = new UpgradeInfo();
                info.pid = cursor.getString(cursor.getColumnIndex(MarketProvider.COLUMN_P_ID));
                info.pkgName = cursor.getString(cursor
                        .getColumnIndex(MarketProvider.COLUMN_P_PACKAGE_NAME));
                info.versionName = cursor.getString(cursor
                        .getColumnIndex(MarketProvider.COLUMN_P_NEW_VERSION_NAME));
                info.versionCode = cursor.getInt(cursor
                        .getColumnIndex(MarketProvider.COLUMN_P_NEW_VERSION_CODE));
                result.put(info.pkgName, info);
            }
            Session.get(context).setUpgradeNumber(cursor.getCount());
        }
        cursor.close();
        return result;
    }
    
    /**
     * 清除所有可更新记录
     * @param context Application Context
     */
    public static int clearUpdateProduct(Context context) {
        return context.getContentResolver().delete(MarketProvider.UPDATE_CONTENT_URI, null, null);
    }
}
