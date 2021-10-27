package com.smartshehar.android.app.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import com.smartshehar.android.app.CGlobals_trains;
import com.smartshehar.dashboard.app.R;


public class ActTrainMap<ActFind> extends AppCompatActivity {

    CGlobals_trains mApp;

    private WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar ab = getSupportActionBar();
        ab.hide();

        mApp = CGlobals_trains.getInstance();
        mApp.init(this);
///      	AnalyticsUtils.getInstance(this).trackPageView(getString(R.string.pageMapLong));
        mApp.mCH.userPing(getString(R.string.pageMap), "");
        setContentView(R.layout.railmap);
        webView = (WebView) findViewById(R.id.wvrailmap);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
//			webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/www/images/mumbairailmap.png");
    }
    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void finish() {
        super.finish();
    }
    @Override
    public void onStart() {
        super.onStart();
        //Get an Analytics tracker to report app starts & uncaught exceptions etc.
//		GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }
    @Override
    public void onStop() {
        super.onStop();
        //Stop the analytics tracking
//		GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }
} // TrainRoute Activity

