package com.smartshehar.dashboard.app.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.smartshehar.dashboard.app.CGlobals_db;
import com.smartshehar.dashboard.app.Constants_dp;
import com.smartshehar.dashboard.app.R;

import lib.app.util.Connectivity;

public class ActEstimatedCost extends AppCompatActivity {
	ProgressDialog mProgressDialog;
    Connectivity mConnectivity;
    public static  String TAG = "ActEstimatedCost";

	public static String URL = Constants_dp.SITE_DIRECTORY+"/rideshare.html?module=estimate&city=";
   public class GeoWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // When user clicks a hyperlink, load in the existing WebView
            view.loadUrl(url);
            return true;
        }
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
        	mProgressDialog.show();
          }
        @Override
        public void onPageFinished(WebView view, String url) {
             super.onPageFinished(view, url);
           try{
               mProgressDialog.dismiss();
               view.clearCache(true);}
           catch(Exception e)
           {
               e.printStackTrace();
           }
        }
    }
 

    public class GeoWebChromeClient extends WebChromeClient {
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin,
                GeolocationPermissions.Callback callback) {
            // Always grant permission since the app itself requires location
            // permission and the user has therefore already granted it
            callback.invoke(origin, true, false);
        }
    }
 
    WebView mWebView;
 
    /** Called when the activity is first created. */
    @SuppressLint("SetJavaScriptEnabled")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        mConnectivity = new Connectivity();
        if (!Connectivity.checkConnected(ActEstimatedCost.this)) {
            if (!mConnectivity.connectionError(ActEstimatedCost.this)) {
                if (mConnectivity.isGPSEnable(ActEstimatedCost.this)) {
                    Log.d(TAG, "Internet Connection");
                }
            }
        }
		ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setHomeButtonEnabled(true);
		ab.setTitle("Trip Cost - " + CGlobals_db.getCity(ActEstimatedCost.this));
		
        mWebView = (WebView) findViewById(R.id.webView1);
        // Brower niceties -- pinch / zoom, follow links in place

showMap();
    }
 private  void showMap(){
     mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
     mWebView.getSettings().setBuiltInZoomControls(true);
     mWebView.setWebViewClient(new GeoWebViewClient());
     // Below required for geolocation
     mWebView.getSettings().setJavaScriptEnabled(true);
     mWebView.getSettings().setGeolocationEnabled(true);
     mWebView.setWebChromeClient(new GeoWebChromeClient());
     SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
     String city = settings.getString("city", CGlobals_db.DEFAULT_CITY);
     mWebView.loadUrl(URL + city);
     mProgressDialog = new ProgressDialog(ActEstimatedCost.this);
     mProgressDialog.setMessage("Loading cost information\nPlease wait ...");
     mProgressDialog.setIndeterminate(false);
     mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 }
    @Override
    public void onBackPressed() {
        // Pop the browser back stack or exit the activity
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        }
        else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        showMap();
    }
}