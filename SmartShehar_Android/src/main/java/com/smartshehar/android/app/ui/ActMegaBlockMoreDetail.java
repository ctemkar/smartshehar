package com.smartshehar.android.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import com.smartshehar.dashboard.app.R;


public class ActMegaBlockMoreDetail extends AppCompatActivity {


    Intent intent;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.hide();

        setContentView(R.layout.act_mega_block_more_detail);
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        intent = getIntent();
        webView.loadUrl(intent.getStringExtra("link"));
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


