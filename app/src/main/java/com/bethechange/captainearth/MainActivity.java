package com.bethechange.captainearth;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Context contextPop;
    private WebView webViewPop;
    private AlertDialog builder;

    private String url = "https://www.whatismybrowser.com/detect/what-is-my-user-agent/";

    private WebView webView;
    private String userAgent; // = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // to continue loading a given URL in the current WebView.
                // needed to handle redirects.
                return false;
            }
        });
        // Set "X-Requested-With" null in the first request
        //Map<String, String> extraHeaders = new HashMap<String, String>();
        //extraHeaders.put("X-Requested-With", "");
        //webView.loadUrl(url, extraHeaders);
        webView.loadUrl(url);

        WebSettings webSettings = webView.getSettings();
        // Set User Agent
        //userAgent = System.getProperty("http.agent");
        // the upper line sometimes causes "403: disallowed user agent error"
        userAgent = "";
        try {
        webSettings.setUserAgentString(userAgent + "Your App Info/Version");
        } catch (Exception e) {
        }

        // Enable Cookies
        CookieManager.getInstance().setAcceptCookie(true);
        if(android.os.Build.VERSION.SDK_INT >= 21)
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        // WebView Tweaks
        webSettings.setJavaScriptEnabled(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setUseWideViewPort(true);
        webSettings.setSaveFormData(true);
        webSettings.setEnableSmoothTransition(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        // Handle Popups
        webView.setWebChromeClient(new CustomChromeClient());
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true);
        contextPop = this.getApplicationContext();
    }

    @Override
    public void onBackPressed() {

        if(webView.canGoBack()) {
            webView.goBack();
        }
        else {
            //super.onBackPressed();
            // Terminate the app
            finishAffinity();
            System.exit(0);
        }
    }

    class CustomChromeClient extends WebChromeClient {

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog,
                                      boolean isUserGesture, Message resultMsg) {

            webViewPop = new WebView(contextPop);
            webViewPop.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // to continue loading a given URL in the current WebView.
                    // needed to handle redirects.
                    return false;
                }
            });

            // Enable Cookies
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            if(android.os.Build.VERSION.SDK_INT >= 21) {
                cookieManager.setAcceptThirdPartyCookies(webViewPop, true);
                cookieManager.setAcceptThirdPartyCookies(webView, true);
            }

            WebSettings popSettings = webViewPop.getSettings();
            // WebView tweaks for popups
            webViewPop.setVerticalScrollBarEnabled(false);
            webViewPop.setHorizontalScrollBarEnabled(false);
            popSettings.setJavaScriptEnabled(true);
            popSettings.setSaveFormData(true);
            popSettings.setEnableSmoothTransition(true);
            // Set User Agent
            popSettings.setUserAgentString(userAgent);
            // to support content re-layout for redirects
            popSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

            // handle new popups
            webViewPop.setWebChromeClient(new CustomChromeClient());

            // set the WebView as the AlertDialog.Builder’s view
            builder = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT).create();
            builder.setTitle("");
            builder.setView(webViewPop);

            builder.setButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    webViewPop.destroy();
                    dialog.dismiss();
                }
            });

            builder.show();
            builder.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(webViewPop);
            resultMsg.sendToTarget();

            return true;
        }

        @Override
        public void onCloseWindow(WebView window) {
            //Toast.makeText(contextPop,"onCloseWindow called",Toast.LENGTH_SHORT).show();
            try {
                webViewPop.destroy();
            } catch (Exception e) {
                Log.d("Webview Destroy Error: ", e.getStackTrace().toString());
            }

            try {
                builder.dismiss();
            } catch (Exception e) {
                Log.d("Builder Dismiss Error: ", e.getStackTrace().toString());
            }

        }
    }
}
