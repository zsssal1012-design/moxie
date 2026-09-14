package com.yuwen.moxie;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import java.util.Locale;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * 语文默写 · 平板横屏 WebView 壳
 *
 * 职责：
 *  1) 强制横屏（manifest: sensorLandscape）+ 沉浸式全屏
 *  2) WebView 承载 assets/index.html（全部 UI 与逻辑在前端）
 *  3) JS 桥：题库/记录写本地文件（抗清数据）、震动、Toast、备份导出
 *  4) 发音不内置语音库，由前端联网合成（多源级联）
 */
public class MainActivity extends Activity {

    private WebView web;
    private File dataFile;
    private TextToSpeech tts;
    private boolean ttsReady = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        // 数据文件：应用专属外部目录，无需存储权限
        File dir = getExternalFilesDir(null);
        if (dir == null) dir = getFilesDir();
        if (!dir.exists()) dir.mkdirs();
        dataFile = new File(dir, "yw_data.json");

        web = new WebView(this);
        setContentView(web);
        setupWeb();
        web.loadUrl("file:///android_asset/index.html");
        hideBars();
        initTts();
    }

    /**
     * 本机 TTS：作为首选发音通道（离线、生僻词也能读）；
     * 若设备无中文语音，前端会自动改用在线发音源。
     */
    private void initTts() {
        try {
            tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                public void onInit(int status) {
                    if (status != TextToSpeech.SUCCESS || tts == null) return;
                    try {
                        int r = tts.setLanguage(Locale.CHINA);
                        if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
                            r = tts.setLanguage(Locale.SIMPLIFIED_CHINESE);
                        }
                        ttsReady = (r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED);
                    } catch (Exception ignored) {
                        ttsReady = false;
                    }
                }
            });
        } catch (Exception ignored) {
            ttsReady = false;
        }
    }

    private void setupWeb() {
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);                     // localStorage 兜底
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);     // 允许进词自动播报
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setTextZoom(100);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        if (Build.VERSION.SDK_INT >= 21) {
            s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        web.setWebViewClient(new WebViewClient());
        web.addJavascriptInterface(new Bridge(), "YwNative");
        web.setBackgroundColor(0xFFFFF6EC);
    }

    /** 隐藏状态栏 + 导航栏，沉浸全屏 */
    private void hideBars() {
        try {
            View d = getWindow().getDecorView();
            int f = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            if (Build.VERSION.SDK_INT >= 19) f |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            d.setSystemUiVisibility(f);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onWindowFocusChanged(boolean has) {
        super.onWindowFocusChanged(has);
        if (has) hideBars();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (web != null) web.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (web != null) web.onResume();
        hideBars();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try { if (tts != null) { tts.stop(); tts.shutdown(); tts = null; } } catch (Exception ignored) { }
        if (web != null) {
            web.destroy();
            web = null;
        }
    }

    /** 返回键交给前端：默写中先确认，否则回上一屏 */
    @Override
    public void onBackPressed() {
        if (web != null) {
            web.evaluateJavascript("window.ywBack?window.ywBack():history.back()", null);
        }
    }

    /** ============ 暴露给 JS 的原生能力 ============ */
    public class Bridge {

        /** 启动时读取本地数据，空则返回 "" */
        @JavascriptInterface
        public String load() {
            try {
                if (!dataFile.exists()) return "";
                InputStream in = new FileInputStream(dataFile);
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
                in.close();
                return new String(bo.toByteArray(), "UTF-8");
            } catch (Exception e) {
                return "";
            }
        }

        /** 保存全部数据（题库 / 记录 / 设置） */
        @JavascriptInterface
        public boolean save(String json) {
            if (json == null || json.length() == 0) return false;
            try {
                File tmp = new File(dataFile.getParentFile(), "yw_data.tmp");
                FileOutputStream o = new FileOutputStream(tmp);
                o.write(json.getBytes("UTF-8"));
                o.flush();
                o.getFD().sync();
                o.close();
                if (dataFile.exists()) dataFile.delete();
                return tmp.renameTo(dataFile);
            } catch (Exception e) {
                return false;
            }
        }

        /** 导出备份到公共 Download 目录，返回路径（失败返回 ""） */
        @JavascriptInterface
        public String export(String json) {
            try {
                File pub = new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        "语文默写_备份.json");
                FileOutputStream o = new FileOutputStream(pub);
                o.write(json.getBytes("UTF-8"));
                o.flush();
                o.close();
                return pub.getAbsolutePath();
            } catch (Exception e) {
                return "";
            }
        }

        /** 读取用户先前导出的备份文件 */
        @JavascriptInterface
        public String readBackup() {
            try {
                File pub = new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        "语文默写_备份.json");
                if (!pub.exists()) return "";
                InputStream in = new FileInputStream(pub);
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
                in.close();
                return new String(bo.toByteArray(), "UTF-8");
            } catch (Exception e) {
                return "";
            }
        }

        /** 本机 TTS 是否可用（前端据此选择发音通道） */
        @JavascriptInterface
        public boolean ttsOk() {
            return ttsReady && tts != null;
        }

        /**
         * 用本机 TTS 朗读 word，重复 times 次，语速 rate
         * 返回是否成功发起（未就绪时返回 false，前端改用在线发音）
         */
        @JavascriptInterface
        public boolean ttsSay(String word, float rate, int times) {
            if (!ttsReady || tts == null || word == null || word.length() == 0) return false;
            final String w = word;
            final float r = rate <= 0 ? 1f : rate;
            final int n = Math.max(1, Math.min(5, times));
            runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        tts.stop();
                        tts.setSpeechRate(r);
                        for (int i = 0; i < n; i++) {
                            tts.speak(w, TextToSpeech.QUEUE_ADD, null);
                        }
                    } catch (Exception ignored) {
                    }
                }
            });
            return true;
        }

        @JavascriptInterface
        public void ttsStop() {
            try { if (tts != null) tts.stop(); } catch (Exception ignored) { }
        }

        /** 翻页轻震 */
        @JavascriptInterface
        public void buzz(int ms) {
            try {
                Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                if (v != null && v.hasVibrator()) v.vibrate(ms <= 0 ? 12 : ms);
            } catch (Exception ignored) {
            }
        }

        @JavascriptInterface
        public void toast(String msg) {
            final String m = msg;
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, m, Toast.LENGTH_SHORT).show();
                }
            });
        }

        /** 跳转系统 TTS 设置（联网发音正常时用不到，留作排障入口） */
        @JavascriptInterface
        public void openTtsSettings() {
            try {
                // 用字符串常量，兼容各版本 SDK（Settings.ACTION_TTS_SETTINGS 并非所有 API 都有）
                startActivity(new Intent("com.android.settings.TTS_SETTINGS"));
            } catch (Exception ignored) {
            }
        }

        @JavascriptInterface
        public String version() {
            return "1.0 / api" + Build.VERSION.SDK_INT;
        }
    }
}