package com.smartshehar.dashboard.app.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.smartshehar.dashboard.app.Constants_dp;
import com.smartshehar.dashboard.app.R;


public class ActDashboardReport extends AppCompatActivity {


    private WebView webView;
    ProgressDialog mProgressDialog;
    String url = Constants_dp.DASHBOARD_REPROT_URL;
    Intent intent;
    String newUrl;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_dashboard_report);
        intent = getIntent();
        newUrl = intent.getStringExtra("url");
        if(!TextUtils.isEmpty(newUrl))
            url = newUrl;

        mProgressDialog = new ProgressDialog(ActDashboardReport.this);
        mProgressDialog.setMessage("Loading... Please wait...");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //Get webview

        webView = (WebView) findViewById(R.id.webView);
        webView.clearCache(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setGeolocationEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSupportZoom(true);
        startWebView(url);

    }

    /*code to come back to the previous page*/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }/*code to come back to the previous page*/

    private void startWebView(String url) {

        //Create new webview Client to show progress dialog
        //When opening a url or click on link

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {


                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mProgressDialog.show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                try {
                    mProgressDialog.dismiss();
                    view.clearCache(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        // Javascript inabled on webview
        webView.getSettings().setJavaScriptEnabled(true);


        webView.loadUrl(url);


    }

    // Open previous opened link from history on webview when back button pressed

    @Override
    // Detect when the back button is pressed
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            // Let the system handle the back button
            super.onBackPressed();
        }
    }

}
