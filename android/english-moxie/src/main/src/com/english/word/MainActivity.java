package com.english.word;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class MainActivity extends Activity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        webView = new WebView(this);
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);          // 启用JS
        ws.setDomStorageEnabled(true);          // 启用localStorage（词库/记录持久化）
        ws.setDatabaseEnabled(true);            // 启用数据库存储
        ws.setAllowFileAccess(true);            // 允许访问本地文件
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        ws.setTextZoom(100); // 字号固定100%，不随系统字体缩放
        webView.setWebChromeClient(new android.webkit.WebChromeClient()); // 支持JS弹窗
        setContentView(webView);
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}