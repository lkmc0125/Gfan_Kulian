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
package com.xiaohong.kulian.common.util;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

import com.xiaohong.kulian.Session;
import com.xiaohong.kulian.common.download.DownloadProvider;


/**
 * aMarket Content Provider
 * 
 * @author andrew
 * @date    2011-4-25
 * @since  Version 0.7.0
 */
public class MarketProvider extends ContentProvider {

    /** The database that lies underneath this content provider */
    private SQLiteOpenHelper mOpenHelper = null;
    
    /** Database filename */
    private static final String DB_NAME = "market.db";
    /** Current database version */
    private static final int DB_VERSION = 70;
    /** Name of search history table in the database */
    public static final String TABLE_SEARCH_HISTORY = "search_history";
    /** Name of product table in the database */
    public static final String TABLE_PRODUCTS = "products";
    /** Name of update table in the database */
    public static final String TABLE_UPDATES = "updates";
    /** Name of buy table in the database */
    public static final String TABLE_BUY = "buy";
    /** Name of card table in the database */
    public static final String TABLE_CARD = "card";
    
    /** MIME type for the entire list */
    private static final String LIST_TYPE = "vnd.android.cursor.dir/";
    /** MIME type for an individual item */
    private static final String ITEM_TYPE = "vnd.android.cursor.item/";
    
    /** URI matcher used to recognize URIs sent by applications */
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    
    /** URI matcher constant for the URI of an search_history */
    private static final int SEARCH_HISTORY = 1;
    private static final int SEARCH_HISTORY_ID = 2;
    private static final int PRODUCTS = 3;
    private static final int UPDATE_PRODUCT = 4;
    private static final int BUY_PRODUCT = 5;
    private static final int CARD = 6;
    
    static {
        sURIMatcher.addURI("kulian", "search_history", SEARCH_HISTORY);
        sURIMatcher.addURI("kulian", "search_history/#", SEARCH_HISTORY_ID);
        sURIMatcher.addURI("kulian", "products", PRODUCTS);
        sURIMatcher.addURI("kulian", "updates", UPDATE_PRODUCT);
        sURIMatcher.addURI("kulian", "buy", BUY_PRODUCT);
        sURIMatcher.addURI("kulian", "card", CARD);
    }
    
    /**
     * The content:// URI to access search history
     */
    public static final Uri SEARCH_CONTENT_URI = Uri.parse("content://kulian/search_history");
    
    /**
     * The content:// URI to access cached products
     */
    public static final Uri PRODUCTS_CONTENT_URI = Uri.parse("content://kulian/products");
    
    /**
     * The content:// URI to access update products
     */
    public static final Uri UPDATE_CONTENT_URI = Uri.parse("content://kulian/updates");
    
    /**
     * The content:// URI to access purchased products
     */
    public static final Uri BUY_CONTENT_URI = Uri.parse("content://kulian/buy");
    
    /**
     * The content:// URI to access card
     */
    public static final Uri CARD_CONTENT_URI = Uri.parse("content://kulian/card");
    
    /** Table ID */
    public static final String COLUMN_ID = "_id";
    /** 搜索关键词 */
    public static final String COLUMN_SEARCH_KEY_WORD = "keyword";
    
    /** 产品表 */
    public static final String COLUMN_P_ID = "p_id";
    public static final String COLUMN_P_NAME = "p_name";
    public static final String COLUMN_P_PACKAGE_NAME = "p_package_name";
    public static final String COLUMN_P_CATEGORY = "p_category";
    public static final String COLUMN_P_PAYMENT_TYPE = "p_payment_type";
    public static final String COLUMN_P_PRICE = "p_price";
    public static final String COLUMN_P_SIZE = "p_size";
    public static final String COLUMN_P_ICON_URL = "p_icon_url";
    public static final String COLUMN_P_IS_RECOMMEND = "p_is_recommend";
    
    /** 可更新产品表（产品ID、包名、更新版本） */
    public static final String COLUMN_P_NEW_VERSION_NAME = "p_new_version_name"; 
    public static final String COLUMN_P_NEW_VERSION_CODE = "p_new_version_code";
    public static final String COLUMN_P_IGNORE = "p_update_ingore";
    
    public static final String COLUMN_CARD_NAME = "card_name";
    public static final String COLUMN_CARD_PAY_TYPE = "card_pay_type";
    public static final String COLUMN_CARD_ACCOUNTNUM = "card_account_num";
    public static final String COLUMN_CARD_PASSWORDNUM = "card_password_num";
    public static final String COLUMN_CARD_CREDIT = "card_credit";
    
    /**
     * This class encapsulates a SQL where clause and its parameters. It makes it possible for
     * shared methods (like {@link DownloadProvider#getWhereClause(Uri, String, String[], int)}) to
     * return both pieces of information, and provides some utility logic to ease piece-by-piece
     * construction of selections.
     */
    private static class SqlSelection {
        public StringBuilder mWhereClause = new StringBuilder();
        public List<String> mParameters = new ArrayList<String>();

        public <T> void appendClause(String newClause, final T... parameters) {
            if (TextUtils.isEmpty(newClause)) {
                return;
            }
            if (mWhereClause.length() != 0) {
                mWhereClause.append(" AND ");
            }
            mWhereClause.append("(");
            mWhereClause.append(newClause);
            mWhereClause.append(")");
            if (parameters != null) {
                for (Object parameter : parameters) {
                    mParameters.add(parameter.toString());
                }
            }
        }

        public String getSelection() {
            return mWhereClause.toString();
        }

        public String[] getParameters() {
            String[] array = new String[mParameters.size()];
            return mParameters.toArray(array);
        }
    }
    
    /**
     * Creates and updated database on demand when opening it.
     * Helper class to create database the first time the provider is
     * initialized and upgrade it when a new version of the provider needs
     * an updated version of the database.
     */
    private final class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(final Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Utils.D("create the new database...");
            onUpgrade(db, 0, DB_VERSION);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            
            Utils.D("update the database...");
            if (oldVersion < newVersion) {
                // 删除0.7.0版本之前的设置
                Session.get(getContext()).setUpdataCheckTime(0);
                Session.get(getContext()).setUpgradeNumber(0);
                db.execSQL("DROP TABLE IF EXISTS installed");
            }
            createSearchHistoryTable(db);
            createProductTable(db);
            createUpdateTable(db);
            createPurchesdTable(db);
            createCardTable(db);
        }
        
        /*
         * 创建搜索历史表
         */
        private void createSearchHistoryTable(SQLiteDatabase db) {
            try {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEARCH_HISTORY);
                db.execSQL("CREATE TABLE " + TABLE_SEARCH_HISTORY + "(" + 
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
                        COLUMN_SEARCH_KEY_WORD + " TEXT);");
            } catch (SQLException ex) {
                Utils.D("couldn't create " + TABLE_SEARCH_HISTORY + " table in market database");
                throw ex;
            }
        }
        
        /*
         * 创建产品缓存表
         */
        private void createProductTable(SQLiteDatabase db) {
            try {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
                db.execSQL("CREATE TABLE " + TABLE_PRODUCTS + "(" + 
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
                        COLUMN_P_ID + " TEXT, " +
                        COLUMN_P_NAME + " TEXT, " +
                        COLUMN_P_PACKAGE_NAME + " TEXT, " +
                        COLUMN_P_CATEGORY + " TEXT, " +
                        COLUMN_P_PAYMENT_TYPE + " TEXT, " +
                        COLUMN_P_SIZE + " TEXT, " +
                        COLUMN_P_PRICE + " TEXT, " +
                        COLUMN_P_ICON_URL + " TEXT, " +
                        COLUMN_P_IS_RECOMMEND + " TEXT);");
            } catch (SQLException ex) {
                Utils.D("couldn't create " + TABLE_PRODUCTS + " table in market database");
                throw ex;
            }
        }
        
        /*
         * 创建更新产品表
         */
        private void createUpdateTable(SQLiteDatabase db) {
            try {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_UPDATES);
                db.execSQL("CREATE TABLE " + TABLE_UPDATES + "(" + 
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
                        COLUMN_P_ID + " TEXT, " +
                        COLUMN_P_PACKAGE_NAME + " TEXT, " +
                        COLUMN_P_NEW_VERSION_NAME + " TEXT, " +
                        COLUMN_P_NEW_VERSION_CODE + " TEXT, " +
                        COLUMN_P_IGNORE + " INTEGER);");
            } catch (SQLException ex) {
                Utils.D("couldn't create " + TABLE_UPDATES + " table in market database");
                throw ex;
            }
        }
        
        /*
         * 创建产品购买历史表
         */
        private void createPurchesdTable(SQLiteDatabase db) {
            try {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUY);
                db.execSQL("CREATE TABLE " + TABLE_BUY + " (" + 
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + 
                        COLUMN_P_ID + " TEXT ," + 
                        COLUMN_P_PACKAGE_NAME + " TEXT);");
            } catch (SQLException ex) {
                Utils.D("couldn't create " + TABLE_BUY + " table in market database");
                throw ex;
            }
        }
        
        /*
         * 创建充值卡表
         */
        private void createCardTable(SQLiteDatabase db) {
            try {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARD);
                db.execSQL("CREATE TABLE " + TABLE_CARD + " (" + 
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + 
                        COLUMN_CARD_NAME + " TEXT," + 
                        COLUMN_CARD_PAY_TYPE + " TEXT," + 
                        COLUMN_CARD_ACCOUNTNUM + " INTEGER," + 
                        COLUMN_CARD_PASSWORDNUM + " INTEGER," + 
                        COLUMN_CARD_CREDIT + " TEXT);");
            } catch (SQLException ex) {
                Utils.D("couldn't create " + TABLE_CARD + " table in market database");
                throw ex;
            }
        }
    }
    
    /* (non-Javadoc)
     * @see android.content.ContentProvider#onCreate()
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }
    
    /* (non-Javadoc)
     * @see android.content.ContentProvider#getType(android.net.Uri)
     */
    @Override
    public String getType(Uri uri) {
        
        int match = sURIMatcher.match(uri);
        switch (match) {
        case SEARCH_HISTORY:
            
            return LIST_TYPE + TABLE_SEARCH_HISTORY;
            
        case SEARCH_HISTORY_ID:
            
            return ITEM_TYPE + TABLE_SEARCH_HISTORY;
        
        case PRODUCTS:
            
            return LIST_TYPE + TABLE_PRODUCTS;
            
        case UPDATE_PRODUCT:
            
            return LIST_TYPE + TABLE_UPDATES;
            
        case BUY_PRODUCT:
            
            return LIST_TYPE + TABLE_BUY;
            
        case CARD:
            
            return LIST_TYPE + TABLE_CARD;
            
        default:
            break;
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        int match = sURIMatcher.match(uri);

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final String table = getTableFromUri(uri);
        long rowID = db.insert(table, null, values);
        if (rowID == -1) {
            Utils.D("couldn't insert into " + table + " database");
            return null;
        }

        Uri inserResult = ContentUris.withAppendedId(uri, rowID);
        notifyContentChanged(uri, match);
        return inserResult;
    }
    
    /* (non-Javadoc)
     * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
     */
    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {

        int match = sURIMatcher.match(uri);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final String table = getTableFromUri(uri);
        SqlSelection selection = getWhereClause(uri, where, whereArgs);
        int count = db.delete(table, selection.getSelection(), selection.getParameters());

        if (count == 0) {
            Utils.D("couldn't delete URI " + uri);
            return count;
        }

        notifyContentChanged(uri, match);
        return count;
    }
    
    /* (non-Javadoc)
     * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        int match = sURIMatcher.match(uri);
        if (match == -1) {
            Utils.D("updating unknown URI: " + uri);
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        final String table = getTableFromUri(uri);
        return db.update(table, values, selection, selectionArgs);
    }
    
    /* (non-Javadoc)
     * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        int match = sURIMatcher.match(uri);
        if (match == -1) {
            Utils.D("querying unknown URI: " + uri);
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SqlSelection fullSelection = getWhereClause(uri, selection, selectionArgs);

        logVerboseQueryInfo(projection, selection, selectionArgs, sortOrder, db);

        final String table = getTableFromUri(uri);
        Cursor ret = db.query(table, projection, fullSelection.getSelection(),
                fullSelection.getParameters(), null, null, sortOrder);

        if (ret == null) {
            Utils.D("query failed in market database");
        }
        return ret;
    }
    
    /**
     * Notify of a change through both URIs
     */
    private void notifyContentChanged(final Uri uri, int uriMatch) {
        getContext().getContentResolver().notifyChange(uri, null);
    }
    
//    /**
//     * 从URI中获取ID
//     */
//    private static String getIdFromUri(final Uri uri) {
//        return uri.getPathSegments().get(1);
//    }
    
    /**
     * 从URI中获取表名
     * @param uri 目标Uri
     * @return 操作目标的表名
     */
    private static String getTableFromUri(final Uri uri) {
        return uri.getPathSegments().get(0);
    }
    
    /**
     * 获取SQL条件的工具方法
     * @param uri Content URI
     * @param where 条件
     * @param whereArgs 参数
     * @param uriMatch 类型
     * @return 合成的SqlSelection对象
     */
    private static SqlSelection getWhereClause(final Uri uri, final String where,
            final String[] whereArgs) {
        SqlSelection selection = new SqlSelection();
        selection.appendClause(where, whereArgs);
//        if (uriMatch == SEARCH_HISTORY_ID) {
//            selection.appendClause(COLUMN_ID + " = ?", getIdFromUri(uri));
//        } else if(uriMatch == PRODUCTS) {
//            selection.appendClause(COLUMN_ID + " = ?", getIdFromUri(uri));
//        }
        return selection;
    }
    
    /**
     * 打印 【查询SQL】 详细信息
     */
    private static void logVerboseQueryInfo(String[] projection, final String selection,
            final String[] selectionArgs, final String sort, SQLiteDatabase db) {
        StringBuilder sb = new StringBuilder();
        sb.append("starting query, database is ");
        if (db != null) {
            sb.append("not ");
        }
        sb.append("null; ");
        if (projection == null) {
            sb.append("projection is null; ");
        } else if (projection.length == 0) {
            sb.append("projection is empty; ");
        } else {
            for (int i = 0; i < projection.length; ++i) {
                sb.append("projection[");
                sb.append(i);
                sb.append("] is ");
                sb.append(projection[i]);
                sb.append("; ");
            }
        }
        sb.append("selection is ");
        sb.append(selection);
        sb.append("; ");
        if (selectionArgs == null) {
            sb.append("selectionArgs is null; ");
        } else if (selectionArgs.length == 0) {
            sb.append("selectionArgs is empty; ");
        } else {
            for (int i = 0; i < selectionArgs.length; ++i) {
                sb.append("selectionArgs[");
                sb.append(i);
                sb.append("] is ");
                sb.append(selectionArgs[i]);
                sb.append("; ");
            }
        }
        sb.append("sort is ");
        sb.append(sort);
        sb.append(".");
        Utils.D(sb.toString());
    }

}
