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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import aga.fdf.grd.os.df.AdExtraTaskStatus;
import aga.fdf.grd.os.df.AppExtraTaskObject;
import aga.fdf.grd.os.df.AppExtraTaskObjectList;
import aga.fdf.grd.os.df.AppSummaryDataInterface;
import aga.fdf.grd.os.df.AppSummaryObject;
import aga.fdf.grd.os.df.AppSummaryObjectList;
import aga.fdf.grd.os.df.DiyOfferWallManager;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Session;
import com.xiaohong.kulian.bean.AppBean;
import com.xiaohong.kulian.bean.AppListBean;
import com.xiaohong.kulian.bean.TaskBean;
import com.xiaohong.kulian.bean.TaskListBean;
import com.xiaohong.kulian.common.AndroidHttpClient;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.ui.BuyCoinActivity;
import com.xiaohong.kulian.ui.BuyCoinPaymentChoiceActivity;
import com.xiaohong.kulian.ui.BuyingEntryActivity;

/**
 * Common Utils for the application
 * 
 * @author andrew.wang
 * @date 2010-9-19
 * @since Version 0.4.0
 */
public class Utils {

    public static boolean sDebug;
    public static String sLogTag;

    private static final String TAG = "Utils";

    // UTF-8 encoding
    private static final String ENCODING_UTF8 = "UTF-8";

    private static WeakReference<Calendar> calendar;

    /**
     * <p>
     * Get UTF8 bytes from a string
     * </p>
     * 
     * @param string
     *            String
     * @return UTF8 byte array, or null if failed to get UTF8 byte array
     */
    public static byte[] getUTF8Bytes(String string) {
        if (string == null)
            return new byte[0];

        try {
            return string.getBytes(ENCODING_UTF8);
        } catch (UnsupportedEncodingException e) {
            /*
             * If system doesn't support UTF-8, use another way
             */
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeUTF(string);
                byte[] jdata = bos.toByteArray();
                bos.close();
                dos.close();
                byte[] buff = new byte[jdata.length - 2];
                System.arraycopy(jdata, 2, buff, 0, buff.length);
                return buff;
            } catch (IOException ex) {
                return new byte[0];
            }
        }
    }

    /**
     * <p>
     * Get string in UTF-8 encoding
     * </p>
     * 
     * @param b
     *            byte array
     * @return string in utf-8 encoding, or empty if the byte array is not encoded with UTF-8
     */
    public static String getUTF8String(byte[] b) {
        if (b == null)
            return "";
        return getUTF8String(b, 0, b.length);
    }

    /**
     * <p>
     * Get string in UTF-8 encoding
     * </p>
     */
    public static String getUTF8String(byte[] b, int start, int length) {
        if (b == null) {
            return "";
        } else {
            try {
                return new String(b, start, length, ENCODING_UTF8);
            } catch (UnsupportedEncodingException e) {
                return "";
            }
        }
    }

    /**
     * <p>
     * Parse int value from string
     * </p>
     * 
     * @param value
     *            string
     * @return int value
     */
    public static int getInt(String value) {
        if (TextUtils.isEmpty(value)) {
            return 0;
        }

        try {
            return Integer.parseInt(value.trim(), 10);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * <p>
     * Parse float value from string
     * </p>
     * 
     * @param value
     *            string
     * @return float value
     */
    public static float getFloat(String value) {
        if (value == null)
            return 0f;

        try {
            return Float.parseFloat(value.trim());
        } catch (NumberFormatException e) {
            return 0f;
        }
    }

    /**
     * <p>
     * Parse long value from string
     * </p>
     * 
     * @param value
     *            string
     * @return long value
     */
    public static long getLong(String value) {
        if (value == null)
            return 0L;

        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public static void V(String msg) {
        if (sDebug) {
            Log.v(sLogTag, msg);
        }
    }

    public static void V(String msg, Throwable e) {
        if (sDebug) {
            Log.v(sLogTag, msg, e);
        }
    }

    public static void D(String msg) {
        if (sDebug) {
            Log.d(sLogTag, msg);
        }
    }

    public static void D(String msg, Throwable e) {
        if (sDebug) {
            Log.d(sLogTag, msg, e);
        }
    }

    public static void I(String msg) {
        if (sDebug) {
            Log.i(sLogTag, msg);
        }
    }

    public static void I(String msg, Throwable e) {
        if (sDebug) {
            Log.i(sLogTag, msg, e);
        }
    }

    public static void W(String msg) {
        if (sDebug) {
            Log.w(sLogTag, msg);
        }
    }

    public static void W(String msg, Throwable e) {
        if (sDebug) {
            Log.w(sLogTag, msg, e);
        }
    }

    public static void E(String msg) {
        if (sDebug) {
            Log.e(sLogTag, msg);
        }
    }

    public static void E(String msg, Throwable e) {
        if (sDebug) {
            Log.e(sLogTag, msg, e);
        }
    }

    public static String formatDate(long time) {
        if (calendar == null || calendar.get() == null) {
            calendar = new WeakReference<Calendar>(Calendar.getInstance());
        }
        Calendar target = calendar.get();
        target.setTimeInMillis(time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(target.getTime());
    }

    public static String getTodayDate() {
        if (calendar == null || calendar.get() == null) {
            calendar = new WeakReference<Calendar>(Calendar.getInstance());
        }
        Calendar today = calendar.get();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(today.getTime());
    }

    /**
     * Returns whether the network is available
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            Log.w(TAG, "couldn't get connectivity manager");
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0, length = info.length; i < length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns whether the network is roaming
     */
    public static boolean isNetworkRoaming(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            // Log.w(Constants.TAG, "couldn't get connectivity manager");
        } else {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE) {
            } else {
            }
        }
        return false;
    }

    // /**
    // * Get the decrypted HTTP response body<br>
    // *
    // * @return if response is empty or some error occured when decrypt process
    // * will return EMPTY string
    // */
    // public static String getDecryptedResponseBody(HttpEntity entity) {
    // byte[] response = SecurityUtil.decryptHttpEntity(entity);
    // return Utils.getUTF8String(response);
    // }
    //
    // private static GoogleAnalyticsTracker mTracker;
    //
    // public static void trackEvent(Context context, String... paras) {
    // if (paras == null || paras.length != 3) {
    // return;
    // }
    // if (mTracker == null) {
    // mTracker = GoogleAnalyticsTracker.getInstance();
    // mTracker.setProductVersion("GfanMobile",
    // String.valueOf(Session.get(context).getVersionCode()));
    // mTracker.start(com.xiaohong.kulian.Constants.GOOGLE_UID, context);
    // }
    // mTracker.trackEvent(paras[0], paras[0] + "_" + paras[1] + paras[2], "", 0);
    // Collector.setAppClickCount(String.format(com.xiaohong.kulian.Constants.STATISTICS_FORMAT,
    // paras[0], paras[1], paras[2]));
    // }

    // /**
    // * Show toast information
    // *
    // * @param context
    // * application context
    // * @param text
    // * the information which you want to show
    // * @return show toast dialog
    // */
    // public static void makeEventToast(Context context, String text, boolean isLongToast) {
    //
    // Toast toast = null;
    // if (isLongToast) {
    // toast = Toast.makeText(context, "", Toast.LENGTH_LONG);
    // } else {
    // toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
    // }
    // View v = LayoutInflater.from(context).inflate(R.layout.toast_view, null);
    // TextView textView = (TextView) v.findViewById(R.id.text);
    // textView.setText(text);
    // toast.setView(v);
    // toast.show();
    // }

    /**
     * 格式化时间（Format：yyyy-MM-dd HH:mm）
     * 
     * @param timeInMillis
     * @return
     */
    public static String formatTime(long timeInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new Date(timeInMillis));
    }

    // /**
    // * 从商家版Zip包中获取加密文件输入流
    // */
    // public static DecryptStream getDecryptStream(File file, String entryName) {
    // try {
    // ZipFile zipPackage = new ZipFile(file);
    // ZipEntry entry = zipPackage.getEntry(entryName);
    // if (entry == null) {
    // return null;
    // }
    // return new DecryptStream(zipPackage.getInputStream(entry));
    // } catch (IOException e) {
    // }
    // return null;
    // }

    // /**
    // * 从商家版Zip包中获取普通文件输入流
    // */
    // public static InputStream getNormalStream(File file, String entryName) {
    // try {
    // ZipFile zipPackage = new ZipFile(file);
    // ZipEntry entry = zipPackage.getEntry(entryName);
    // if (entry == null) {
    // return null;
    // }
    // return zipPackage.getInputStream(entry);
    // } catch (IOException e) {
    // }
    // return null;
    // }

    // /**
    // * 获取商家版加密后的APK文件，并拷贝到SD卡上（/sdcard/kulian/apk）
    // * @param root 商家版应用包文件
    // * @param entryName APK文件
    // * @return 拷贝后的文件
    // */
    // public static File getEncryptApk(File root, String entryName) {
    //
    // InputStream in = null;
    // FileOutputStream fos = null;
    // File outputFile = null;
    // try {
    // outputFile = new File(Environment.getExternalStorageDirectory() + "/kulian/apk",
    // entryName);
    // fos = new FileOutputStream(outputFile);
    // in = getDecryptStream(root, entryName);
    // if (in == null) {
    // return null;
    // }
    // copyFile(in, fos);
    // } catch (FileNotFoundException e) {
    // e.printStackTrace();
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // return outputFile;
    // }

    // /**
    // * 获取商家版APK对应的ICON文件，并拷贝到SD卡上（/sdcard/kulian/.cache）
    // * @param root 商家版应用包文件
    // * @param entryName ICON文件
    // * @return 拷贝后的文件
    // */
    // public static File getApkIcon(File root, String entryName) {
    // InputStream in = null;
    // FileOutputStream fos = null;
    // File outputFile = null;
    // try {
    // outputFile = new File(Environment.getExternalStorageDirectory() + "/kulian/.cache",
    // entryName);
    // fos = new FileOutputStream(outputFile);
    // in = getNormalStream(root, entryName);
    // if (in == null) {
    // return null;
    // }
    // copyFile(in, fos);
    // } catch (FileNotFoundException e) {
    // e.printStackTrace();
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // return outputFile;
    // }

    /**
     * 文件拷贝工具类
     * 
     * @param src
     *            源文件
     * @param dst
     *            目标文件
     * @throws IOException
     */
    public static void copyFile(InputStream in, FileOutputStream dst) throws IOException {
        byte[] buffer = new byte[8192];
        int len = 0;
        while ((len = in.read(buffer)) > 0) {
            dst.write(buffer, 0, len);
        }
        in.close();
        dst.close();
    }

    /**
     * 解析HTTP String Entity
     * 
     * @param response
     *            HTTP Response
     * @return 市场API返回的消息(String)
     */
    public static String getStringResponse(HttpResponse response) {
        HttpEntity entity = response.getEntity();
        try {
            return entity == null ? null : EntityUtils.toString(response.getEntity());
        } catch (ParseException e) {
            D("getStringResponse meet ParseException", e);
        } catch (IOException e) {
            D("getStringResponse meet IOException", e);
        }
        return null;
    }

    /**
     * 解析HTTP InputStream Entity
     * 
     * @param response
     *            HTTP Response
     * @return 市场API返回的消息(InputStream)
     */
    public static InputStream getInputStreamResponse(HttpResponse response) {
        HttpEntity entity = response.getEntity();
        try {
            if(entity == null) return null;
            return AndroidHttpClient.getUngzippedContent(entity);
        } catch (IllegalStateException e) {
            D("getInputStreamResponse meet IllegalStateException", e);
        } catch (IOException e) {
            D("getInputStreamResponse meet IOException", e);
        }
        return null;
    }

    /**
     * 界面切换动画
     * 
     * @return
     */
    public static LayoutAnimationController getLayoutAnimation() {
        AnimationSet set = new AnimationSet(true);

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(50);
        set.addAnimation(animation);

        animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        animation.setDuration(100);
        set.addAnimation(animation);

        LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);
        return controller;
    }

    /**
     * 创建Tab中的只包含TextView的View
     */
    public static View createTabView(Context context, String text) {
        TextView view = (TextView) LayoutInflater.from(context).inflate(R.layout.common_tab_view,
                null);
        view.setText(text);
        return view;
    }
    
    /**
     * 创建Tab中的只包含TextView的View
     */
    public static View createMakeMoneyPageTabView(Context context, String text) {
        View view =  LayoutInflater.from(context).inflate(R.layout.make_money_tab_view,
                null);
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(text);
        return view;
    }
    
    /**
     * 获取用户安装的应用列表
     */
    public static List<PackageInfo> getInstalledApps(Context context) {
        PackageManager pm = context.getPackageManager();
        final String ourPackageName =  Session.get(context).getPackageName();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        List<PackageInfo> apps = new ArrayList<PackageInfo>();
        for (PackageInfo info : packages) {
            // 只返回非系统级应用
            if (((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
                    && !ourPackageName.equals(info.packageName)) {
                apps.add(info);
            }
        }
        
        return apps;
    }

    /**
     * 获取用户本机所有应用程序
     */
    public static List<PackageInfo> getAllInstalledApps(Context context) {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        List<PackageInfo> apps = new ArrayList<PackageInfo>();
        ArrayList<String> installed = new ArrayList<String>();
        for (PackageInfo info : packages) {
            apps.add(info);
            installed.add(info.packageName);
        }
        Session.get(context).setInstalledApps(installed);
        return apps;
    }

    /**
     * 比较两个文件的签名是否一致
     */
    public static boolean compareFileWithSignature(String path1, String path2) {

        long start = System.currentTimeMillis();
        if (TextUtils.isEmpty(path1) || TextUtils.isEmpty(path2)) {
            return false;
        }

        String signature1 = getFileSignatureMd5(path1);
        String signature2 = getFileSignatureMd5(path2);

        V("compareFileWithSignature total time is " + (System.currentTimeMillis() - start));
        if (!TextUtils.isEmpty(signature1) && signature1.equals(signature2)) {
            return true;
        }
        return false;
    }

    /**
     * 获取应用签名MD5
     */
    public static String getFileSignatureMd5(String targetFile) {

        try {
            JarFile jarFile = new JarFile(targetFile);
            // 取RSA公钥
            JarEntry jarEntry = jarFile.getJarEntry("AndroidManifest.xml");

            if (jarEntry != null) {
                InputStream is = jarFile.getInputStream(jarEntry);
                byte[] buffer = new byte[8192];
                while (is.read(buffer) > 0) {
                    // do nothing
                }
                is.close();
                Certificate[] certs = jarEntry == null ? null : jarEntry.getCertificates();
                if (certs != null && certs.length > 0) {
                    String rsaPublicKey = String.valueOf(certs[0].getPublicKey());
                    return getMD5(rsaPublicKey);
                }
            }
        } catch (IOException e) {
            W("occur IOException when get file signature", e);
        }
        return "";
    }

    /**
     * Get MD5 Code
     */
    public static String getMD5(String text) {
        try {
            byte[] byteArray = text.getBytes("utf8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(byteArray, 0, byteArray.length);
            return convertToHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Convert byte array to Hex string
     */
    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    /**
     * Check whether the SD card is readable
     */
    public static boolean isSdcardReadable() {
        final String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)
                || Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Check whether the SD card is writable
     */
    public static boolean isSdcardWritable() {
        final String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Show toast information
     * 
     * @param context
     *            application context
     * @param text
     *            the information which you want to show
     * @return show toast dialog
     */
    public static void makeEventToast(Context context, String text, boolean isLongToast) {

        Toast toast = null;
        if (isLongToast) {
            toast = Toast.makeText(context, "", Toast.LENGTH_LONG);
        } else {
            toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        }
        View v = LayoutInflater.from(context).inflate(R.layout.toast_view, null);
        TextView textView = (TextView) v.findViewById(R.id.text);
        textView.setText(text);
        toast.setView(v);
        toast.show();
    }
    
    /**
     * 解析二维码地址
     */
    public static HashMap<String, String> parserUri(Uri uri) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        String paras[] = uri.getQuery().split("&");
        for (String s : paras) {
            if (s.indexOf("=") != -1) {
                String[] item = s.split("=");
                parameters.put(item[0], item[1]);
            } else {
                return null;
            }
        }
        return parameters;
    }
    
    /**
     * 检查默认Proxy
     */
    public static HttpHost detectProxy(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null 
                && ni.isAvailable() 
                && ni.getType() == ConnectivityManager.TYPE_MOBILE) {
            String proxyHost = android.net.Proxy.getDefaultHost();
            int port = android.net.Proxy.getDefaultPort();
            if (proxyHost != null) {
                return new HttpHost(proxyHost, port, "http");
            }
        }
        return null;
    }
    
    /**
     * Android 安装应用
     * 
     * @param context Application Context
     * @param filePath APK文件路径
     */
    public static void installApk(Context context, File file) {
        if (file.exists()) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            ((ContextWrapper) context).startActivity(i);
        } else {
            makeEventToast(context, context.getString(R.string.install_fail_file_not_exist), false);
        }
    }
    
    /**
     * 卸载应用
     * 
     * @param context
     *            应用上下文
     * @param pkgName
     *            包名
     */
    public static void uninstallApk(Context context, String pkgName) {
        Uri packageURI = Uri.parse("package:" + pkgName);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(uninstallIntent);
    }
    
    /**
     * 获取机锋市场下载的应用文件
     */
    public static ArrayList<HashMap<String, Object>> getLocalApks(Context context) {
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File root = new File(Environment.getExternalStorageDirectory(), Constants.ROOT_DIR);
            ArrayList<HashMap<String, Object>> apks = new ArrayList<HashMap<String, Object>>();
            getApkList(context, root, apks);
            return apks;
        }
        return null;
    }
    
    /*
     * 遍历Gfan APK文件
     */
    private static void getApkList(Context context, File root,
            ArrayList<HashMap<String, Object>> apkList) {

        int index = 0;
        File marketRoot = new File(root, "market");
        boolean hasMarket = false;
        if (marketRoot.exists()) {
            File[] children = marketRoot.listFiles();
            if (children.length > 0) {

                for (File child : children) {
                    if (!child.isDirectory()) {
                        if (child.getName().endsWith(".apk")) {
                            HashMap<String, Object> item = getApkInfo(context, child);
                            if (item != null) {
                                index++;
                                apkList.add(item);
                            }
                        }
                    }
                }
                if (index > 0) {
                    hasMarket = true;
                    HashMap<String, Object> group = new HashMap<String, Object>();
                    group.put(
                            Constants.KEY_PRODUCT_NAME,
                            context.getString(R.string.apk_title_market) + "("
                                    + marketRoot.getAbsolutePath() + ")");
                    group.put(Constants.KEY_PLACEHOLDER, true);
                    apkList.add(0, group);
                }
            }
        }

        File bbsRoot = new File(root, "bbs");
        boolean hasBbs = false;
        if (bbsRoot.exists()) {
            File[] children = bbsRoot.listFiles();
            if (children.length > 0) {

                int startPos = index;
                for (File child : children) {
                    if (!child.isDirectory()) {
                        if (child.getName().endsWith(".apk")) {
                            HashMap<String, Object> item = getApkInfo(context, child);
                            if (item != null) {
                                index++;
                                apkList.add(item);
                            }
                        }
                    }
                }

                if (index > startPos) {
                    hasBbs = true;
                    HashMap<String, Object> group = new HashMap<String, Object>();
                    group.put(Constants.KEY_PRODUCT_NAME, context.getString(R.string.apk_title_bbs)
                            + "(" + bbsRoot.getAbsolutePath() + ")");
                    group.put(Constants.KEY_PLACEHOLDER, true);
                    if (hasMarket) {
                        apkList.add(startPos + 1, group);
                    } else {
                        apkList.add(startPos, group);
                    }
                }
            }
        }
        
        File cloudRoot = new File(root, "cloud");
        if (cloudRoot.exists()) {
            File[] children = cloudRoot.listFiles();
            if (children.length > 0) {

                int startPos = index;
                for (File child : children) {
                    if (!child.isDirectory()) {
                        if (child.getName().endsWith(".apk")) {
                            HashMap<String, Object> item = getApkInfo(context, child);
                            if (item != null) {
                                index++;
                                apkList.add(item);
                            }
                        }
                    }
                }

                if (index > startPos) {
                    HashMap<String, Object> group = new HashMap<String, Object>();
                    group.put(Constants.KEY_PRODUCT_NAME, context.getString(R.string.apk_title_cloud)
                            + "(" + cloudRoot.getAbsolutePath() + ")");
                    group.put(Constants.KEY_PLACEHOLDER, true);
                    if (hasMarket && hasBbs) {
                        apkList.add(startPos + 2, group);
                    } else if (hasMarket | hasBbs) {
                        apkList.add(startPos + 1, group);
                    } else {
                        apkList.add(startPos, group);
                    }
                }
            }
        }
    }
    
    /*
     * 获取APK信息
     */
    public static HashMap<String, Object> getApkInfo(Context context, File file) {
        PackageManager pm = context.getPackageManager();
        String filePath = file.getAbsolutePath();
        PackageInfo info = pm.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
        if (info == null) {
            return null;
        }
        ApplicationInfo appInfo = info.applicationInfo;
        info.applicationInfo.sourceDir = filePath;
        info.applicationInfo.publicSourceDir = filePath;
        Drawable icon = pm.getApplicationIcon(appInfo);
        
        HashMap<String, Object> apk = new HashMap<String, Object>();
        apk.put(Constants.KEY_PRODUCT_ICON, icon);
        apk.put(Constants.KEY_PRODUCT_NAME, file.getName());
        apk.put(Constants.KEY_PRODUCT_INFO, filePath);
        apk.put(Constants.KEY_PRODUCT_DESCRIPTION, file.getAbsolutePath());
        apk.put(Constants.KEY_PRODUCT_PAY_TYPE, Constants.PAY_TYPE_FREE);
        apk.put(Constants.KEY_PLACEHOLDER, false);
        return apk;
    }
    
    /**
     * 删除安装包
     */
    public static boolean deleteFile(String file) {
        File realFile = new File(file);
        return realFile.delete();
    }
    
    /**
     * 计算下载进度字符串
     */
    public static String calculateRemainBytes(Context ctx, float current, float total) {

        float remain = total - current;
        remain = remain > 0 ? remain : 0;
        String text = "";
        final String megaBytes = "M";
        final String kiloBytes = "K";
        final String bytes = "B";
        if (remain > 1000000) {
            text = ctx.getString(R.string.download_remain_bytes,
                    String.format("%.02f", (remain / 1000000)), megaBytes);
        } else if (remain > 1000) {
            text = ctx.getString(R.string.download_remain_bytes,
                    String.format("%.02f", (remain / 1000)), kiloBytes);
        } else {
            text = ctx.getString(R.string.download_remain_bytes, (int) remain, bytes);
        }
        return text;
    }
    
    /**
     * 检查是否应该进行更新
     */
    public static boolean isNeedCheckUpgrade(Context context) {
        long currentTime = System.currentTimeMillis();
        long lastCheckTime = Session.get(context).getUpdataCheckTime();
        if (currentTime - lastCheckTime > 86400000) {
             // we only check update every 24 hours
            return true;
        }
        return false;
    }
    
    /**
     * 统计工具方法
     */
    public static void trackEvent(Context context, String... paras) {
        return;
    }
    
    public static String submitLogs() {
        Process mLogcatProc = null;
        BufferedReader reader = null;
        try {
            mLogcatProc = Runtime.getRuntime().exec(
                    new String[] { "logcat", "-d" , "机锋市场:v"});

            reader = new BufferedReader(new InputStreamReader(mLogcatProc.getInputStream()));

            String line;
            final StringBuilder log = new StringBuilder();
            String separator = System.getProperty("line.separator");

            while ((line = reader.readLine()) != null) {
                log.append(line);
                log.append(separator);
            }
            return log.toString();

            // do whatever you want with the log. I'd recommend using Intents to
            // create an email
        } catch (IOException e) {
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                }
        }
        return "";
    }

    public static void clearCache(Context context) {
        File file = Environment.getDownloadCacheDirectory();
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
        file = context.getCacheDir();
        files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
    }

    public static String httpGet(final String url) {
        if (url == null || url.length() == 0) {
            Log.e(TAG, "httpGet, url is null");
            return null;
        }

        HttpClient httpClient = getNewHttpClient();
        HttpGet httpGet = new HttpGet(url);

        try {
            HttpResponse resp = httpClient.execute(httpGet);
            if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                Log.e(TAG, "httpGet fail, status code = " + resp.getStatusLine().getStatusCode());
                return null;
            }

            return new String(EntityUtils.toByteArray(resp.getEntity()), "UTF-8");

        } catch (Exception e) {
            Log.e(TAG, "httpGet exception, e = " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public static byte[] httpPost(String url, String entity) {
        if (url == null || url.length() == 0) {
            Log.e(TAG, "httpPost, url is null");
            return null;
        }
        
        HttpClient httpClient = getNewHttpClient();
        
        HttpPost httpPost = new HttpPost(url);
        
        try {
            httpPost.setEntity(new StringEntity(entity));
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            
            HttpResponse resp = httpClient.execute(httpPost);
            if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                Log.e(TAG, "httpGet fail, status code = " + resp.getStatusLine().getStatusCode());
                return null;
            }

            return EntityUtils.toByteArray(resp.getEntity());
        } catch (Exception e) {
            Log.e(TAG, "httpPost exception, e = " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private static HttpClient getNewHttpClient() { 
        try { 
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType()); 
            trustStore.load(null, null); 

            SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore); 
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER); 

            HttpParams params = new BasicHttpParams(); 
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1); 
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8); 

            SchemeRegistry registry = new SchemeRegistry(); 
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80)); 
            registry.register(new Scheme("https", sf, 443)); 

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry); 

            return new DefaultHttpClient(ccm, params); 
        } catch (Exception e) { 
            return new DefaultHttpClient(); 
        } 
     }
    private static class SSLSocketFactoryEx extends SSLSocketFactory {      
        
        SSLContext sslContext = SSLContext.getInstance("TLS");      
          
        public SSLSocketFactoryEx(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {      
            super(truststore);      
          
            TrustManager tm = new X509TrustManager() {      
          
                public X509Certificate[] getAcceptedIssuers() {      
                    return null;      
                }      
          
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
                }  
            };      
          
            sslContext.init(null, new TrustManager[] { tm }, null);      
        }      
          
        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        } 
    }

    public static String getMobileInfo() {
        return "model:" + android.os.Build.MODEL + ",manufacturer:"
                + android.os.Build.MANUFACTURER + ",os:"
                + android.os.Build.VERSION.RELEASE;
    }

    public static boolean isLeShiMobile() {
        return (Utils.getMobileInfo().toLowerCase().indexOf("letv") != -1);
    }

    /**
     * Check if the apk is installed on the device
     * @param context A context
     * @param packageName The app's package name
     * @return return true if the app is installed
     *          otherwise return false
     */
    public static boolean isApkInstalled(Context context, 
            String packageName) {
        List<PackageInfo> list = getInstalledApps(context);
        for(PackageInfo p : list) {
            if(packageName != null && packageName.equals(p.packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the app is downloaded
     * @param appName App name without suffix(.apk)
     * @return true if the app is downloaded
     */
    public static boolean isApkDownloaded(String appName) {
//        File file =  new File(Environment.getExternalStorageDirectory(),
//                com.xiaohong.kulian.common.download.Constants.DEFAULT_MARKET_SUBDIR
//                        + "/" + appName + ".apk");
//        boolean downloaded = file.exists();
//        return downloaded;
        return false; // todo 需要先解决下载到一半，重启后不能检测安装包是否完整的问题
    }
    
    /**
     * Get the absolute path for a app name
     * @param appName
     * @return
     */
    public static String getDownloadedAppPath(String appName) {
        File file =  new File(Environment.getExternalStorageDirectory(),
                com.xiaohong.kulian.common.download.Constants.DEFAULT_MARKET_SUBDIR
                        + "/" + appName + ".apk");
        return file.getAbsolutePath();
    }
    
    /**
     * 
     * @param context
     * @param packageName
     * @return
     */
    public static boolean openApkByPackageName(Context context, 
            String packageName) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent();
        intent = packageManager.getLaunchIntentForPackage(packageName);
        if (intent == null) {
            return false;
        }
        context.startActivity(intent);
        return true;
    }

    /**
     * do blur for a bitmap
     * @param context
     * @param bitmap the bitmap to be blured
     * @return 
     */
    @SuppressLint("NewApi")
    public static Bitmap blurBitmap(Context context, Bitmap bitmap) {

        // Let's create an empty bitmap with the same size of the bitmap we want
        // to blur
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Config.ARGB_8888);

        // Instantiate a new Renderscript
        RenderScript rs = RenderScript.create(context);

        // Create an Intrinsic Blur Script using the Renderscript
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs,
                Element.U8_4(rs));

        // Create the Allocations (in/out) with the Renderscript and the in/out
        // bitmaps
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

        // Set the radius of the blur
        blurScript.setRadius(25.f);

        // Perform the Renderscript
        blurScript.setInput(allIn);
        blurScript.forEach(allOut);
        // Copy the final bitmap created by the out Allocation to the outBitmap
        allOut.copyTo(outBitmap);
        // recycle the original bitmap
        //bitmap.recycle();
        // After finishing everything, we destroy the Renderscript.
        rs.destroy();
        return outBitmap;
    }

    /**
     * A display option for ImageLoader
     */
    public static final  DisplayImageOptions sDisplayImageOptions = new DisplayImageOptions.Builder()
    // .showImageOnLoading(R.drawable.ic_stub) //加载图片时的图片
    // .showImageForEmptyUri(R.drawable.ic_e ynmnmpty) //没有图片资源时的默认图片
    // .showImageOnFail(R.drawable.ic_error) //加载失败时的图片
            .cacheInMemory(true) // 启用内存缓存
            .cacheOnDisk(true) // 启用外存缓存
            .considerExifParams(true) // 启用EXIF和JPEG图像格式
            .displayer(new RoundedBitmapDisplayer(20)) // 设置显示风格这里是圆角矩形
            .build();
    
    /**
     * A display option for ImageLoader
     */
    public static final  DisplayImageOptions sDisplayRoundImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true) // 启用内存缓存
            .cacheOnDisk(true) // 启用外存缓存
            .considerExifParams(true) // 启用EXIF和JPEG图像格式
            .build();

    /**
     * Go to BuyCoinActivity 
     * @param context A context which should be a Activity context
     */
    public static void gotoBuyCoinPage(Context context) {
        Intent intent = new Intent(context, BuyCoinActivity.class);
        context.startActivity(intent);
    }
    
    /**
     * Go to BuyCoinPaymentChoiceActivity 
     * @param context A context which should be a Activity context
     * modifyed by albert 2016/05/26
     */
    public static void gotoBuyCoinPaymentChoiceActivity(Context context) {
        Intent intent = new Intent(context, BuyCoinPaymentChoiceActivity.class);
        context.startActivity(intent);
    }
    
    public static final String KEY_OTHER_ACCOUNT = "other_account";
    /**
     * Go to BuyCoinActivity to buy coin for other
     * @param context A context which should be a Activity context
     */
    public static void gotoBuyCoinPageForOther(Context context) {
        Intent intent = new Intent(context, BuyCoinActivity.class);
        intent.putExtra(KEY_OTHER_ACCOUNT, true);
        context.startActivity(intent);
    }
    
    public static void gotoBuyingEntryPage(Context context) {
        Intent intent = new Intent(context, BuyingEntryActivity.class);
        context.startActivity(intent);
    }
    
    //do preload for make money page - app and task
    private static boolean sIsAppLoading = false;
    private static boolean sIsTaskLoading = false;
    private static int sStartPage = 1;
    private static LoadAppAndTaskApiResponseListener sLoadAppAndTaskApiResponseListener =
            null;
    
    /*
     * 预加载赚金币页面的数据
     */
    public static void doPreloadApp(Context context, AppSummaryDataInterface handler) {
        Log.d(TAG,"doPreloadApp sIsAppLoading = " + sIsAppLoading);
        if (sIsAppLoading == true) {
            return;
        }
        if (sYoumiData == null || sYoumiData.size() == 0) {
            sIsAppLoading = true;
            loadYoumiData(context, handler);
        }
    }

    public static void doPreloadTask(Context context, ApiRequestListener handler) {
        if (sIsTaskLoading == true) {
            Log.d(TAG, "already loaded");
            return;
        }
        if (sTaskList == null || sTaskList.size() == 0) {
            sIsTaskLoading = true;
            loadTask(context, handler);
        }
        if (sAppList == null || sAppList.size() == 0) {
            loadApp(context, handler);
        }
    }

    private synchronized static void loadApp(Context context, ApiRequestListener handler) {
        if (sLoadAppAndTaskApiResponseListener == null) {
            sLoadAppAndTaskApiResponseListener =
                    new LoadAppAndTaskApiResponseListener(context, handler);
        }
        MarketAPI.getAppList(context, sLoadAppAndTaskApiResponseListener, sStartPage, Constants.CATEGORY_RCMD);
    }

    public final static int AD_PER_NUMBER = 10;
    
    /**
     * 预加载有米数据
     */
    private static void loadYoumiData(Context context, final AppSummaryDataInterface handler) {

        // 异步加载方式
        // 请求类型，页码，请求数量，回调接口
        
        DiyOfferWallManager.getInstance(context).loadOfferWallAdList(DiyOfferWallManager.REQUEST_ALL,
                1, AD_PER_NUMBER, new AppSummaryDataInterface() {

                    /**
                     * 当成功获取到积分墙列表数据的时候，会回调这个方法（注意:本接口不在UI线程中执行，
                     * 所以请不要在本接口中进行UI线程方面的操作）
                     * 注意：列表数据有可能为空（比如：没有广告的时候），开发者处理之前，请先判断列表是否为空，大小是否大与0
                     */
                    @Override
                    public void onLoadAppSumDataSuccess(Context context,
                            AppSummaryObjectList adList) {
                        sYoumiData = adList;
                        sIsAppLoading = false;
                        handler.onLoadAppSumDataSuccess(context, adList);
                    }

                    /**
                     * 因为网络问题而导致请求失败时，会回调这个接口（注意:本接口不在UI线程中执行，
                     * 所以请不要在本接口中进行UI线程方面的操作）
                     */
                    @Override
                    public void onLoadAppSumDataFailed() {
                        handler.onLoadAppSumDataFailed();
                        sIsAppLoading = false;
                    }

                    /**
                     * 请求成功，但是返回有米错误代码时候，会回调这个接口（注意:本接口不在UI线程中执行，
                     * 所以请不要在本接口中进行UI线程方面的操作）
                     */
                    @Override
                    public void onLoadAppSumDataFailedWithErrorCode(
                            final int code) {
                        handler.onLoadAppSumDataFailedWithErrorCode(code);
                        sIsAppLoading = false;
                    }
                });
    }
    
    private synchronized static void loadTask(Context context, ApiRequestListener handler) {
        if(sLoadAppAndTaskApiResponseListener == null) {
            sLoadAppAndTaskApiResponseListener =
                    new LoadAppAndTaskApiResponseListener(context, handler);
        }
        MarketAPI.getTaskList(context, sLoadAppAndTaskApiResponseListener);
        MarketAPI.getGzhTaskList(context, sLoadAppAndTaskApiResponseListener);
    }

    private static AppSummaryObjectList sYoumiData = null;
    
    private static final ArrayList<AppBean> sAppList = new ArrayList<AppBean>(); 
    
    private static final ArrayList<TaskBean> sTaskList = new ArrayList<TaskBean>(); 
    
    private static final ArrayList<TaskBean> sGzhTaskList = new ArrayList<TaskBean>(); 
    
    public static AppSummaryObjectList getPredloadedYoumiData() {
        return sYoumiData;
    }
    
    public static void clearPreloadedYoumiData() {
        sIsAppLoading = false;
        sYoumiData = null;
    }
    
    public static void clearPreloadedTaskData() {
        sIsTaskLoading = false;
        sTaskList.clear();
        sGzhTaskList.clear();
        sAppList.clear();
    }
    
    public static ArrayList<AppBean> getPreloadedAppList() {
        return sAppList;
    }
    
    public static ArrayList<TaskBean> getPreloadedTaskList() {
        return sTaskList;
    }
    
    public static ArrayList<TaskBean> getPreloadedGzhTaskList() {
        return sGzhTaskList;
    }
    
    private static class LoadAppAndTaskApiResponseListener implements ApiRequestListener {
        private Context mContext;
        private ApiRequestListener mHandler;
        public LoadAppAndTaskApiResponseListener(Context context, ApiRequestListener handler) {
            mContext = context;
            mHandler = handler;
        }

        @Override
        public void onSuccess(int method, Object obj) {
            switch (method) {
                case MarketAPI.ACTION_GET_APP_LIST :
                    AppListBean appList = (AppListBean) obj;
                    sAppList.clear();
                    sAppList.addAll(appList.getApplist());
                    for (AppBean bean : sAppList) {
                        if (Utils.isApkInstalled(mContext, bean.getPackageName()) == true) {
                            bean.setIsInstalled(true);
                        } else if (Utils.isApkDownloaded(bean.getAppName())) {
                            /**
                             * only if the app is not installed , shall we check if it's downloaded
                             * 
                             */
                            bean.setDownloaded(true);
                        }
                    }
                    Log.d(TAG, "onSuccess sAppList size = " + sAppList.size());
                    break;
                case MarketAPI.ACTION_GET_TASK_LIST : {
                    TaskListBean result = (TaskListBean) obj;
                    if (result.getTasklist() != null) {
                        Log.d(TAG, "task size = " + result.getTasklist().size());
                        for (TaskBean item : result.getTasklist()) {
                            // set remain num to 1 for normal task
                            item.setRemain_tasknum(1);
                            item.setTaskType(TaskBean.ITEM_TYPE_WEB_TASK);
                        }
                    } else {
                        Log.d(TAG, "no data from server");
                    }
                    sTaskList.clear();
                    sTaskList.addAll(result.getTasklist());
                    sIsTaskLoading = false;
                    Log.d(TAG, "onSuccess sTaskList size = " + sTaskList.size());
                    mHandler.onSuccess(method, obj);
                    break;
                }
                case MarketAPI.ACTION_GET_GZH_TASK_LIST :
                    TaskListBean result = (TaskListBean) obj;
                    ArrayList<TaskBean> availableList = new ArrayList<TaskBean>();
                    ArrayList<TaskBean> finishedList = new ArrayList<TaskBean>();
                    int titlePos = 0;
                    if (result.getTasklist() != null) {
                        Log.d(TAG, " gzh size = "
                                + result.getTasklist().size());
                        for (int i = 0; i < result.getTasklist().size(); i++) {
                            TaskBean bean = result.getTasklist().get(i);
                            bean.setTaskType(TaskBean.ITEM_TYPE_GZH_TASK);
                            if (bean.getRemain_tasknum() == 0) {
                                finishedList.add(bean);
                            } else {
                                availableList.add(bean);
                            }
                        }
                        if (finishedList.size() > 0) {
                            availableList.addAll(finishedList);
                        }
                    }
                    sGzhTaskList.clear();
                    sGzhTaskList.addAll(availableList);
                    Log.d(TAG, "onSuccess sGzhTaskList size = " + sGzhTaskList.size());
                    mHandler.onSuccess(method, obj);
                    break;
                default :
                    break;
            }
        }
        @Override
        public void onError(int method, int statusCode) {
            switch (method) {
            case MarketAPI.ACTION_GET_TASK_LIST :
            case MarketAPI.ACTION_GET_GZH_TASK_LIST :
                sIsTaskLoading = false;
            default:
                break;
            }
        }
    }
    
    /**
     *
     高斯模糊
     */
    public static Bitmap doFastBlur(Bitmap sentBitmap, int radius, boolean canReuseInBitmap) {  
        
        // Stack Blur v1.0 from  
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html  
        //  
        // Java Author: Mario Klingemann <mario at quasimondo.com>  
        // http://incubator.quasimondo.com  
        // created Feburary 29, 2004  
        // Android port : Yahel Bouaziz <yahel at kayenko.com>  
        // http://www.kayenko.com  
        // ported april 5th, 2012  
  
        // This is a compromise between Gaussian Blur and Box blur  
        // It creates much better looking blurs than Box Blur, but is  
        // 7x faster than my Gaussian Blur implementation.  
        //  
        // I called it Stack Blur because this describes best how this  
        // filter works internally: it creates a kind of moving stack  
        // of colors whilst scanning through the image. Thereby it  
        // just has to add one new block of color to the right side  
        // of the stack and remove the leftmost color. The remaining  
        // colors on the topmost layer of the stack are either added on  
        // or reduced by one, depending on if they are on the right or  
        // on the left side of the stack.  
        //  
        // If you are using this algorithm in your code please add  
        // the following line:  
        //  
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>  
  
        Bitmap bitmap;  
        if (canReuseInBitmap) {  
            bitmap = sentBitmap;  
        } else {  
            bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);  
        }  
  
        if (radius < 1) {  
            return (null);  
        }  
  
        int w = bitmap.getWidth();  
        int h = bitmap.getHeight();  
  
        int[] pix = new int[w * h];  
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);  
  
        int wm = w - 1;  
        int hm = h - 1;  
        int wh = w * h;  
        int div = radius + radius + 1;  
  
        int r[] = new int[wh];  
        int g[] = new int[wh];  
        int b[] = new int[wh];  
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;  
        int vmin[] = new int[Math.max(w, h)];  
  
        int divsum = (div + 1) >> 1;  
        divsum *= divsum;  
        int dv[] = new int[256 * divsum];  
        for (i = 0; i < 256 * divsum; i++) {  
            dv[i] = (i / divsum);  
        }  
  
        yw = yi = 0;  
  
        int[][] stack = new int[div][3];  
        int stackpointer;  
        int stackstart;  
        int[] sir;  
        int rbs;  
        int r1 = radius + 1;  
        int routsum, goutsum, boutsum;  
        int rinsum, ginsum, binsum;  
  
        for (y = 0; y < h; y++) {  
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;  
            for (i = -radius; i <= radius; i++) {  
                p = pix[yi + Math.min(wm, Math.max(i, 0))];  
                sir = stack[i + radius];  
                sir[0] = (p & 0xff0000) >> 16;  
                sir[1] = (p & 0x00ff00) >> 8;  
                sir[2] = (p & 0x0000ff);  
                rbs = r1 - Math.abs(i);  
                rsum += sir[0] * rbs;  
                gsum += sir[1] * rbs;  
                bsum += sir[2] * rbs;  
                if (i > 0) {  
                    rinsum += sir[0];  
                    ginsum += sir[1];  
                    binsum += sir[2];  
                } else {  
                    routsum += sir[0];  
                    goutsum += sir[1];  
                    boutsum += sir[2];  
                }  
            }  
            stackpointer = radius;  
  
            for (x = 0; x < w; x++) {  
  
                r[yi] = dv[rsum];  
                g[yi] = dv[gsum];  
                b[yi] = dv[bsum];  
  
                rsum -= routsum;  
                gsum -= goutsum;  
                bsum -= boutsum;  
  
                stackstart = stackpointer - radius + div;  
                sir = stack[stackstart % div];  
  
                routsum -= sir[0];  
                goutsum -= sir[1];  
                boutsum -= sir[2];  
  
                if (y == 0) {  
                    vmin[x] = Math.min(x + radius + 1, wm);  
                }  
                p = pix[yw + vmin[x]];  
  
                sir[0] = (p & 0xff0000) >> 16;  
                sir[1] = (p & 0x00ff00) >> 8;  
                sir[2] = (p & 0x0000ff);  
  
                rinsum += sir[0];  
                ginsum += sir[1];  
                binsum += sir[2];  
  
                rsum += rinsum;  
                gsum += ginsum;  
                bsum += binsum;  
  
                stackpointer = (stackpointer + 1) % div;  
                sir = stack[(stackpointer) % div];  
  
                routsum += sir[0];  
                goutsum += sir[1];  
                boutsum += sir[2];  
  
                rinsum -= sir[0];  
                ginsum -= sir[1];  
                binsum -= sir[2];  
  
                yi++;  
            }  
            yw += w;  
        }  
        for (x = 0; x < w; x++) {  
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;  
            yp = -radius * w;  
            for (i = -radius; i <= radius; i++) {  
                yi = Math.max(0, yp) + x;  
  
                sir = stack[i + radius];  
  
                sir[0] = r[yi];  
                sir[1] = g[yi];  
                sir[2] = b[yi];  
  
                rbs = r1 - Math.abs(i);  
  
                rsum += r[yi] * rbs;  
                gsum += g[yi] * rbs;  
                bsum += b[yi] * rbs;  
  
                if (i > 0) {  
                    rinsum += sir[0];  
                    ginsum += sir[1];  
                    binsum += sir[2];  
                } else {  
                    routsum += sir[0];  
                    goutsum += sir[1];  
                    boutsum += sir[2];  
                }  
  
                if (i < hm) {  
                    yp += w;  
                }  
            }  
            yi = x;  
            stackpointer = radius;  
            for (y = 0; y < h; y++) {  
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )  
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];  
  
                rsum -= routsum;  
                gsum -= goutsum;  
                bsum -= boutsum;  
  
                stackstart = stackpointer - radius + div;  
                sir = stack[stackstart % div];  
  
                routsum -= sir[0];  
                goutsum -= sir[1];  
                boutsum -= sir[2];  
  
                if (x == 0) {  
                    vmin[y] = Math.min(y + r1, hm) * w;  
                }  
                p = x + vmin[y];  
  
                sir[0] = r[p];  
                sir[1] = g[p];  
                sir[2] = b[p];  
  
                rinsum += sir[0];  
                ginsum += sir[1];  
                binsum += sir[2];  
  
                rsum += rinsum;  
                gsum += ginsum;  
                bsum += binsum;  
  
                stackpointer = (stackpointer + 1) % div;  
                sir = stack[stackpointer];  
  
                routsum += sir[0];  
                goutsum += sir[1];  
                boutsum += sir[2];  
  
                rinsum -= sir[0];  
                ginsum -= sir[1];  
                binsum -= sir[2];  
  
                yi += w;  
            }  
        }  
  
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);  
  
        return (bitmap);  
    } 
    
    /**
     * get the package name for a app
     * @param context
     * @param appFullName
     * @return
     */
    public static String getPackageName(Context context, String appFullName) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(appFullName, PackageManager.GET_ACTIVITIES);
        ApplicationInfo appInfo = null;
        String packageName = null;
        if (info != null) {
            appInfo = info.applicationInfo;
            packageName = appInfo.packageName;
        }
        return packageName;
    }
    
    /**
     * This function will run in a background thread
     * @param packageName
     */
    public static void removeInstalledApkByPackageName(final String packageName) {
        if(packageName == null || packageName.equals("")) {
            Log.w(TAG, "removeInstalledApkByPackageName bad package name:" + packageName);
            return;
        }
        new AsyncTask<Void,Void,Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                //scan apk dir
                File file =  new File(Environment.getExternalStorageDirectory(),
                        com.xiaohong.kulian.common.download.Constants.DEFAULT_MARKET_SUBDIR);
                if(!file.exists()) {
                    Log.w(TAG, file.getAbsolutePath() + " doesnot exist");
                    return null;
                }
                File apkList [] = file.listFiles();
                for (File apk : apkList) {
                    ApkInfo apkInfo = ApkUtil.getApkInfo(apk.getAbsolutePath());
                    if (apkInfo != null && packageName.equals(apkInfo.getApkPackage())) {
                        Log.d(TAG, apkInfo.getApkName() + " removed");
                        deleteFile(apkInfo.getApkName());
                        return null;
                    }
                }
                return null;
            }
            
        }.execute();
    }
    
    public static void checkAppRunningStatus(final Context context, final String packageName) {
        if(packageName == null || packageName.equals("")) {
            Log.w(TAG, "checkAppRunningStatus bad package name:" + packageName);
            return;
        }
        new AsyncTask<Void,Void,Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                // 隔1秒检测一次
                for (int i = 0; i < 30; i++) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
                    boolean isAppRunning = false;
                    String[] activePackages;
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) { //  > Android 5.0
                        isAppRunning = true;
//                        UsageStatsManager usageStatsManager = (UsageStatsManager)context.getSystemService(Context.USAGE_STATS_SERVICE);
//                        long ts = System.currentTimeMillis();
//                        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, ts - 2000, ts);
//                        if (queryUsageStats == null || queryUsageStats.isEmpty()) {
//                            return null;
//                        }
//
//                        UsageStats recentStats = null;
//                        for (UsageStats usageStats : queryUsageStats) {
//                            if (recentStats == null || recentStats.getLastTimeUsed() < usageStats.getLastTimeUsed()) {
//                                recentStats = usageStats;
//                            }
//                        }
                        
//                        final List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
//                        for (ActivityManager.RunningAppProcessInfo processInfo : processInfos) {
//                            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
//                                for (String pkgName : processInfo.pkgList) {
//                                    if (pkgName.equals(packageName)) {
//                                        isAppRunning = true;
//                                        break;
//                                    }
//                                }
//                                if (isAppRunning) {
//                                    break;
//                                }
//                            }
//                        }
                    } else {
                        final List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(10);
                        for (RunningTaskInfo info : list) {
                            if (info.topActivity.getPackageName().equals(packageName)
                                    || info.baseActivity.getPackageName().equals(packageName)) {
                                isAppRunning = true;
                                break;
                            }
                        }
                    }

                    if (isAppRunning) {
                        Log.i(TAG, "checkAppRunningStatus: " + packageName + " is running!");
                        Session.get(context).reportAppLaunched(packageName);
                        return null;
                    }
                }
                return null;
            }
            
        }.execute();
    }
    
    /**
     * 获取指定广告的所有积分（正常完成的积分+可完成的追加任务积分）
     */
    public static int getTotalPoints(AppSummaryObject appSummaryObject) {
        int totalpoints = appSummaryObject.getPoints();
        AppExtraTaskObjectList tempList = appSummaryObject.getExtraTaskList();
        if (tempList != null && tempList.size() > 0) {
            for (int i = 0; i < tempList.size(); ++i) {
                AppExtraTaskObject extraTaskObject = tempList.get(i);
                if (extraTaskObject.getStatus() == AdExtraTaskStatus.NOT_START
                        || extraTaskObject.getStatus() == AdExtraTaskStatus.IN_PROGRESS) {
                    totalpoints += extraTaskObject.getPoints();
                }
            }
        }
        return totalpoints;
    }
}