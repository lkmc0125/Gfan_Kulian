package com.xiaohong.kulian.ui;


import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.common.util.TopBar;
import com.xiaohong.kulian.common.widget.BaseActivity;

public class HumorActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_humor);
        initTopBar();

        WebView webView = (WebView)findViewById(R.id.humor_webview);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
    //  webView = new WebView(this);
        webView.setVerticalScrollBarEnabled(false);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setSupportZoom(false);
        webView.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) { //  重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
                    view.loadUrl(url);
                    return true;
                }
                });
        webView.loadUrl("http://xhaz.come11.com");
    }

    private void initTopBar() {
        TopBar.createTopBar(getApplicationContext(),
                new View[] { findViewById(R.id.top_bar_title) },
                new int[] { View.VISIBLE },
                getString(R.string.humor_title));
    }
    
}
