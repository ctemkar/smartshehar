package com.jumpinjumpout.apk.driver.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.webkit.WebView;

import com.jumpinjumpout.apk.driver.R;

public class Help_act extends Activity {

	WebView help_web;

	private ProgressDialog pDialog;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);

		help_web = (WebView) findViewById(R.id.help_web);

		pDialog = new ProgressDialog(this);
		pDialog.setMessage("Please wait...");
		pDialog.setCancelable(false);
		showpDialog();

		help_web.getSettings().setJavaScriptEnabled(true);

		help_web.loadUrl("http://jumpinjumpout.com/faq.html");

		hidepDialog();
	}

	private void showpDialog() {
		if (!pDialog.isShowing())
			pDialog.show();
	}

	private void hidepDialog() {
		if (pDialog.isShowing())
			pDialog.dismiss();
	}

}
