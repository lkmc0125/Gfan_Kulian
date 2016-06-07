package com.xiaohong.kulian.ui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.bean.TaskBean;
import com.xiaohong.kulian.common.util.TopBar;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.widget.BaseActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.DownloadListener;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class WebviewActivity extends BaseActivity {
    private WebView webView;
    private class MyWebViewDownLoadListener implements DownloadListener{   
        @Override  
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,   
                                    long contentLength) {              
            Log.i("tag", "url="+url);
            Log.i("tag", "userAgent="+userAgent);
            Log.i("tag", "contentDisposition="+contentDisposition);            
            Log.i("tag", "mimetype="+mimetype);
            Log.i("tag", "contentLength="+contentLength);   
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);   
            startActivity(intent);              
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        String url = intent.getStringExtra("extra.url");
        String title = intent.getStringExtra("extra.title");

        setContentView(R.layout.activity_webview);
        initTopBar(title);

        webView = (WebView)findViewById(R.id.webview);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setVerticalScrollBarEnabled(false);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setSupportZoom(false);
        webView.setDownloadListener(new MyWebViewDownLoadListener()); 
        webView.setWebViewClient(new WebViewClient() {
            // 重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                "kuliangzhtask://taskid=1"
                if (url.startsWith("kuliangzhtask://")) {
                    String taskid = url.replaceAll("kuliangzhtask://taskid=","");
                    if (TextUtils.isEmpty(taskid) == false) {
                        ArrayList<TaskBean> taskList = Utils.getPreloadedGzhTaskList();
                        if (taskList != null) {
                            for (int position = 0; position < taskList.size(); position++) {
                                Object obj = taskList.get(position);
                                TaskBean item = (TaskBean)obj;
                                if (item.getId() == Integer.parseInt(taskid)) {
                                    Intent intent = new Intent(WebviewActivity.this, GzhTaskDetailActivity.class);
                                    intent.putExtra(Constants.EXTRA_TASK_BEAN, item);
                                    startActivity(intent);
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    view.loadUrl(url);    
                }
                return true;
            }
        });
        webView.loadUrl(url);
    }

    private void initTopBar(String title) {

        TopBar.createTopBar(this, 
                new View[] { findViewById(R.id.back_btn), findViewById(R.id.top_bar_title) },
                new int[] { View.VISIBLE, View.VISIBLE}, 
                title);
        ImageButton back = (ImageButton)findViewById(R.id.back_btn);
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
      if (keyCode == KeyEvent.KEYCODE_BACK) {
          if (webView.canGoBack()) {
              webView.goBack();
          } else {
              finish();
          }
          return true;
      }
      return super.onKeyDown(keyCode, event);
    }
}
