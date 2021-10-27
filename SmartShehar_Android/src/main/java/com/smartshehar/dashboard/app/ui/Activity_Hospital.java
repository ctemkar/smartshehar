package com.smartshehar.dashboard.app.ui;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.smartshehar.dashboard.app.CGlobals_db;
import com.smartshehar.dashboard.app.Constants_dp;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;

import java.util.List;
import java.util.Locale;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;

public class Activity_Hospital extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private Location mMostRecentLocation;
    private CGlobals_lib_ss mApp = null;
    ProgressDialog mProgressDialog;

    String msNearBy;
    TextView mTvStatus;
    Connectivity mConnectivity;
    GoogleApiClient mGoogleApiClient = null;
    public static String TAG = "Activity_Hospital";
    Snackbar snackbar;
    private CoordinatorLayout coordinatorLayout;
    private static final String URL = "file:///android_asset/www/nearby.html";
    private long lRadius = 0;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
///       	AnalyticsUtils.getInstance(this).trackPageView(getString(R.string.pageBeSafeDashboardLong)); 
        mApp = CGlobals_lib_ss.getInstance();
        mApp.init(this);
        mApp.getMyLocation();
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setHomeButtonEnabled(true);
        setContentView(R.layout.activity_hospital);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mGoogleApiClient = new GoogleApiClient.Builder(Activity_Hospital.this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(Activity_Hospital.this)
                .addOnConnectionFailedListener(Activity_Hospital.this)
                .build();
        mGoogleApiClient.connect();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            showSnackbar("You need to install Google Navigation to use this feature");
            Log.e(TAG, ex.getMessage());
        }
        if (!gps_enabled) {

            CGlobals_db.getInstance(Activity_Hospital.this).turnGPSOn(Activity_Hospital.this, mGoogleApiClient);
        }
        mConnectivity = new Connectivity();
        if (!Connectivity.checkConnected(Activity_Hospital.this)) {
            if (!mConnectivity.connectionError(Activity_Hospital.this)) {
                if (mConnectivity.isGPSEnable(Activity_Hospital.this)) {
                    Log.d(TAG, "Internet Connection");
                }
            }
        }
        msNearBy = getIntent().getStringExtra(Constants_dp.NEARBY);
        if (TextUtils.isEmpty(msNearBy))
            Activity_Hospital.this.finish();

        setTitle("Near by " + msNearBy);
        if (msNearBy.equals(Constants_dp.POLICE)) {
            lRadius = 0;
        }
        if (msNearBy.equals(Constants_dp.HOSPITAL)) {
            lRadius = 2500;
        }
        setupWebView();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CGlobals_db.REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    Log.d(TAG, "YES");

                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    Log.d(TAG, "NO");
                    CGlobals_db.showGPSDialog = false;
                    CGlobals_db.alertToTurnOnGps(Activity_Hospital.this);

                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Sets up the WebView object and loads the URL of the page
     **/
    private void setupWebView() {
        mMostRecentLocation = mApp.getMyLocation();
        @SuppressWarnings("unused")
        final String centerURL = "javascript:centerAt(" +
                mMostRecentLocation.getLatitude() + "," +
                mMostRecentLocation.getLongitude() + ")";
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        //Wait for the page to load then send the location information;
        webView.setWebViewClient(new WebViewClient() {
            /* On Android 1.1 shouldOverrideUrlLoading() will be called every time the user clicks a link,
             * but on Android 1.5 it will be called for every page load, even if it was caused by calling loadUrl()! */
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (url.startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_DIAL,
                            Uri.parse(url));
                    startActivity(intent);
                } else if (url.startsWith("google.navigation:")) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse(url)));
                    } catch (Exception e) {
                        showSnackbar("You need to install Google Navigation to use this feature");
                    }
                } else if (url.startsWith("http:") || url.startsWith("https:")) {
                    view.loadUrl(url);
                }
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
        });
        /** Allows JavaScript calls to access application resources **/
//        webView.addJavascriptInterface(new JavaScriptInterface(), "android");
        webView.addJavascriptInterface(new MyInterface(Activity_Hospital.this), "interface");
        webView.loadUrl(URL);
//	   webView.loadUrl("file:///android_assets/police.html");


    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG,"CONNECTED");
    }


    private class MyInterface {

        Context mContext;


        MyInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public String getNearbyType() {
            return msNearBy;
        }

        @JavascriptInterface
        public double getRadius() {
            return lRadius;
        }

        @JavascriptInterface
        public double getLatitude() {
            return mMostRecentLocation.getLatitude();
        }

        @JavascriptInterface
        public double getLongitude() {
            return mMostRecentLocation.getLongitude();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        if (CGlobals_lib_ss.haveNetworkConnection() == 0) {
            try {
                mTvStatus = (TextView) findViewById(R.id.tvStatus);
                mTvStatus.setText(R.string.no_internet);
            } catch (Exception e) {
                SSLog.e(" Emergency - Resume: ", "error ", e.getMessage());
            }
        }
        setupWebView();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_emergency);
    }

    String getAddress(Location location) {
        if (location == null)
            return "";
        try {
            Geocoder geo = new Geocoder(Activity_Hospital.this.getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1);
            if (addresses.isEmpty()) {
                return "";
            } else {
                if (addresses.size() > 0) {
                    String address = addresses.get(0).getAddressLine(0);
                    address += ", " + addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName();
                    return address;
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // getFromLocation() may sometimes fail
        }
        return "";
    }

    public void showSnackbar(String msg) {
        snackbar = Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }
} // Activity_BeSafe_Emergency





