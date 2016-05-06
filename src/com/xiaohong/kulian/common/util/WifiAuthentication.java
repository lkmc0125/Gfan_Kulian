package com.xiaohong.kulian.common.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.ruijie.wmc.open.ClientHelper;
import com.ruijie.wmc.open.JsonUtil;

import android.content.Context;
import android.util.Log;

public class WifiAuthentication {
    private final String TAG = "WifiAuthentication";
    private Context mContext;

    private WifiAuthentication() {
    }

    public WifiAuthentication(Context context) {
        mContext = context;
    }

    public void appAuth() {
        new Thread(authTask).start();
    }

    Runnable authTask = new Runnable() {
        @Override
        public void run() {
            try {
                sendKangKaiAuthRequest();
                sendShenZhouAuthRequest();
//                sendHillStoneAuthRequest();
                sendRuijieAuthRequest();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void sendKangKaiAuthRequest() throws Exception {
        Log.d(TAG, "sendKangKaiAuthRequest");
        String url = "http://182.254.140.228/portaltt/Logon.html";
        Utils.httpGet(url);
    }

    private void sendShenZhouAuthRequest() throws Exception {
        Log.d(TAG, "sendShenZhouAuthRequest");
        try {
            String url = "http://www.baidu.com";
            String redictURL = getRedirectUrl(url);
            if (redictURL == null) {
                Log.d(TAG, "url not redirected");
                return;
            }
            String ip = getUrlPara(redictURL, "ip");
            String gw = getUrlPara(redictURL, "gw");

            String authUrl = "http://" + gw
                    + ":8800/dcmecloud/interface/RestHttpAuth.php?har={\"ip\":\"" + ip
                    + "\",\"tool\":\"onekey\"}";
            HttpURLConnection conn = (HttpURLConnection) new URL(authUrl).openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(5000);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(), "utf-8"));
            String line = "";
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
/*  山石未用
    private void sendHillStoneAuthRequest() throws Exception {
        Log.d(TAG, "sendHillStoneAuthRequest");
        try {
            String authUrl = "http://www.wifiopenapiauth.com/";
            HttpURLConnection conn = (HttpURLConnection) new URL(authUrl).openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(5000);
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            String line = "";
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/
    private void sendRuijieAuthRequest() throws Exception {
        Log.d(TAG, "sendRuijieAuthRequest");
        try {
            String content = getWebContent("http://www.baidu.com");
            if (content == null || content.length() == 0) {
                Log.d(TAG, "url not redirected");
                return;
            }
            String redictURL = content.replaceAll("<script>self.location.href='","");
            redictURL = redictURL.replaceAll("'</script>","");
            String ip       = getUrlPara(redictURL, "ip");
            String userId   = getUrlPara(redictURL, "id");
            String mac      = getUrlPara(redictURL, "mac");
            String username = mac;
            String serialno = getUrlPara(redictURL, "serialno");

            if (ip == null || userId == null || mac == null || serialno == null) {
                Log.d(TAG, "ruijie redirect para error");
                return;
            }

            HashMap<String,String> params = new HashMap<String,String>();
            params.put("method", "auth.userOnlineWithDecrypt");
            params.put("ip", ip);
            params.put("userId", userId);
            params.put("username", username);
            params.put("mac", mac);
            params.put("serialno", serialno);
    
            String resultJson = ClientHelper.sendRequest("http://wmc.ruijieyun.com/open/service","Q1OF5SB3HIVD","7F4981MBZXBOSV53VVX5FJ2V","614634",JsonUtil.toJsonString(params));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static String stringToMD5(String string) {
        byte[] hash;  
        try {  
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));  
        } catch (NoSuchAlgorithmException e) {  
            e.printStackTrace();  
            return null;  
        } catch (UnsupportedEncodingException e) {  
            e.printStackTrace();  
            return null;  
        }  
      
        StringBuilder hex = new StringBuilder(hash.length * 2);  
        for (byte b : hash) {  
            if ((b & 0xFF) < 0x10)  
                hex.append("0");  
            hex.append(Integer.toHexString(b & 0xFF));  
        }  
      
        return hex.toString();  
    }  
    
    private String getUrlPara(String url, String key) {
        String params = url.substring(url.indexOf("?") + 1);
        Pattern pattern = Pattern.compile("(^|&)" + key + "=([^&]*)(&|$)");
        Matcher m = pattern.matcher(params);
        while (m.find()) {
            String value = m.group(2);
            return value;
        }
        return null;
    }

    private String getRedirectUrl(String path) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(path)
                .openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.setConnectTimeout(5000);
        return conn.getHeaderField("Location");
    }
    
    private String getWebContent(String urlStr) {
        try
        {
//            return "<script>self.location.href='http://112.124.31.88/auth/servlet/authServlet?s=f202313df1f99f2af542a9073a50811c&mac=9807f5c39b0245c82c04859938fb2d0e&port=29316c3960ee95d8&url=709db9dc9ce334aaea3d4fa878826efd5d8c15802016713e&ip=81b0dc1f1e4c5effd8c557e77a2986ae&id=8e47f33396baf6de&serialno=db1ebcf32ebd0305e016e2afc5767922'</script>";
//
            URL url = new URL(urlStr);  
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();  
            conn.setDoInput(true);  
            conn.setConnectTimeout(10000);  
            conn.setRequestMethod("GET");
            conn.setRequestProperty("accept", "*/*");  
            String location = conn.getRequestProperty("location");  
            int resCode = conn.getResponseCode();  
            conn.connect();  
            InputStream stream = conn.getInputStream();  
            byte[] data=new byte[102400];  
            int length=stream.read(data);  
            String str=new String(data,0,length);   
            conn.disconnect();
            stream.close();
            return str;
        }  
        catch(Exception ee)  
        {  
            System.out.print("ee:"+ee.getMessage());   
        }
        return null;  
    }
}
