package com.jumpinjumpout.apk.user.ui;


import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.Constants_lib;
import com.jumpinjumpout.apk.lib.ContactsAutoComplete;
import com.jumpinjumpout.apk.lib.LocationService;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.user.CGeo;
import com.jumpinjumpout.apk.user.CGlobals_user;
import com.jumpinjumpout.apk.user.Constants_user;
import com.jumpinjumpout.apk.user.EmergencyContacts;
import com.jumpinjumpout.apk.user.MyApplication;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lib.app.util.AppRater;
import lib.app.util.Connectivity;


public class Act_WalkWithMe extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private static final String TAG = "Acitivity_WalkWithMe: ";
    GoogleApiClient mGoogleApiClient = null;

    private TextView mTvStatus;
    private Button btnStart;
    private EmergencyContacts emergencycontacts;
    private ContactsAutoComplete acEmail;
    private String sEmails = "";
    private Location mCurrentLocation = null;
    Connectivity mConnectivity;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConnectivity = new Connectivity();
        mGoogleApiClient = new GoogleApiClient.Builder(Act_WalkWithMe.this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(Act_WalkWithMe.this)
                .addOnConnectionFailedListener(Act_WalkWithMe.this)
                .build();
        mGoogleApiClient.connect();
        mCurrentLocation = CGlobals_user.getInstance().getBestLocation(Act_WalkWithMe.this);
        if (mCurrentLocation != null) {
            Toast.makeText(this, "Got Location!", Toast.LENGTH_SHORT).show();
        }
        CGlobals_user.getInstance().init(this);
        CGlobals_user.getInstance().getMyLocation(Act_WalkWithMe.this);
        ActionBar ab = getSupportActionBar();
        // ab.setHomeButtonEnabled(true);
        setContentView(R.layout.activity_walkwithme);
        if (!mConnectivity.checkConnected(Act_WalkWithMe.this)) {
            if (!mConnectivity.connectionError(Act_WalkWithMe.this, getString(R.string.app_label))) {
                if (mConnectivity.isGPSEnable(Act_WalkWithMe.this)) {
                    Log.d(TAG, "Internet Connection");
                }
            }
        }
        readPrefs();
        mTvStatus = (TextView) findViewById(R.id.tvStatus);
        btnStart = (Button) findViewById(R.id.btnStart);
        acEmail = (ContactsAutoComplete) findViewById(R.id.acEmail);
        if (!TextUtils.isEmpty(sEmails))
            acEmail.setText(sEmails);
        AppRater.app_launched(this, getString(R.string.pageDashboardLong), CGlobals_user.getInstance().mPackageInfo.packageName);
        emergencycontacts = new EmergencyContacts(this);
        PreferenceManager.getDefaultSharedPreferences(this);


        btnStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sEmails = acEmail.getText().toString();
                if (CGlobals_user.inWalkWithMe) {    // Stop WalkWithMe
                    CGlobals_user.inWalkWithMe = false;
                    btnStart.setText("Start");
                    stopService(new Intent(Act_WalkWithMe.this, LocationService.class));
                } else {    // Start WalkWithMe
                    if (sEmails.contains("@") && sEmails.contains(".")) {
                        CGlobals_user.inWalkWithMe = true;
                        btnStart.setText("Stop");
                        CGlobals_user.walkWithMeMode = "A";
                        stopService(new Intent(Act_WalkWithMe.this, LocationService.class));
                        startService(new Intent(Act_WalkWithMe.this, LocationService.class));
                        sendLocation();
                    } else {
                        Toast.makeText(Act_WalkWithMe.this, "Please enter valid email id/s", Toast.LENGTH_SHORT).show();
                    }
                }
                writePrefs();
                finish();
            }
        });

    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!CGlobals_user.inWalkWithMe)
                return;
            if (mCurrentLocation != null) {
                CGlobals_user.walkWithMeMode = "U";
                sendLocation();
            }
        }
    };

    private void sendLocation() {

        final int miTripId = CGlobals_lib.getInstance().getSharedPreferences(getApplicationContext())
                .getInt(Constants_lib.PREF_TRIP_ID_INT, -2);
        final String url = Constants_user.WALKWITHME_UPDATEPOSITION_URL;
        try {


            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (!response.trim().equals("-1")) {
                                if (CGlobals_user.walkWithMeMode.equals("A")) {
                                    Toast.makeText(Act_WalkWithMe.this,
                                            "Email sent to: " + sEmails +
                                                    " to 'Walk with you'\n They will see your position on a map",
                                            Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(Act_WalkWithMe.this, "Could not send request ", Toast.LENGTH_LONG).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Log.d(TAG, "error is " + error.toString());
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    Location location = CGlobals_user.getInstance().getMyLocation(Act_WalkWithMe.this);
                    Calendar cal = Calendar.getInstance();
                    params.put("user_email", emergencycontacts.msMyEmail);
                    params.put("email", emergencycontacts.msMyEmail);
                    params.put("friend_email", sEmails);
                    params.put("mode", CGlobals_user.walkWithMeMode);
                    params.put("stopped", CGlobals_user.inWalkWithMe ? "N" : "Y");
                    params.put("y", Integer.toString(cal.get(Calendar.YEAR)));
                    params.put("o", Integer.toString(cal.get(Calendar.MONTH)));
                    params.put("d", Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
                    params.put("h", Integer.toString(cal.get(Calendar.HOUR_OF_DAY)));
                    params.put("m", Integer.toString(cal.get(Calendar.MINUTE)));
                    params.put("s", Integer.toString(cal.get(Calendar.SECOND)));
                    SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(Act_WalkWithMe.this);
                    String msName = mSettings.getString(Constants_user.KEY_PREF_MY_NAME, "");
                    String msPhone = mSettings.getString(Constants_user.KEY_PREF_MY_NUMBER, "");
                    params.put("name", msName);
                    params.put("phone", msPhone);
                    params.put("tripid", String.valueOf(miTripId));
                    try {
                        if (location != null) {
                            CGeo geo = new CGeo(Act_WalkWithMe.this);
                            geo.getAddress(location);
                            String address = geo.mAddr.firstAddressString;

                            if (!TextUtils.isEmpty(address)) {
                                params.put("address", address);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace(); // getFromLocation() may sometimes fail
                    }
                    params = CGlobals_user.getInstance().getBasicMobileParams(params,
                            url, Act_WalkWithMe.this);
                    Log.d(TAG, "PARAM IS " + params);
                    return CGlobals_user.getInstance().checkParams(params);
                }
            };
            MyApplication.getInstance().getRequestQueue().add(postRequest);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ERROR IS " + e.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        keyboardShow(false);

        readPrefs();
        if (CGlobals_lib.haveNetworkConnection(Act_WalkWithMe.this) == 0) {
            try {
                mTvStatus = (TextView) findViewById(R.id.tvStatus);
                mTvStatus.setText(R.string.no_internet);
            } catch (Exception e) {
                SSLog.e(" Emergency - Resume: ", "error ", e.getMessage());
            }
        }
        registerReceiver(broadcastReceiver,
                new IntentFilter(LocationService.BROADCAST_ACTION));
        if (!isServiceRunning(Act_WalkWithMe.this, "com.jumpinjumpout.apk.user.LocationService_user")) {
            CGlobals_user.inWalkWithMe = false;
        }

        if (!CGlobals_user.inWalkWithMe) {
            stopService(new Intent(Act_WalkWithMe.this, LocationService.class));
            btnStart.setText("Start");
        } else
            btnStart.setText("Stop");
        writePrefs();
        super.onResume();
    }


    @Override
    public void onPause() {
        writePrefs();
        super.onPause();
    } // onPause

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_emergency);
    }


    boolean isServiceRunning(Context context, String className) {
        ActivityManager activityManager = (ActivityManager)
                context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList
                = activityManager.getRunningServices(Integer.MAX_VALUE);

        if (!(serviceList.size() > 0)) {
            return false;
        }

        for (int i = 0; i < serviceList.size(); i++) {
            ActivityManager.RunningServiceInfo serviceInfo = serviceList.get(i);
            ComponentName serviceName = serviceInfo.service;
            if (serviceName.getClassName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    private void writePrefs() {
        SharedPreferences mPref = getSharedPreferences(CGlobals_lib.PREFS_NAME, 0);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putBoolean(CGlobals_user.PREFINWALKWITHME, CGlobals_user.inWalkWithMe);
        editor.putInt(CGlobals_user.PREFTRAVELDIST, CGlobals_user.getInstance().miTravelDistance);
        editor.putString(CGlobals_user.PREFINWALKWITHME_EMAILS, sEmails);
        editor.commit();
    }

    private void readPrefs() {
        SharedPreferences pref = getSharedPreferences(CGlobals_lib.PREFS_NAME, 0);
        try {
            CGlobals_user.getInstance().miTravelDistance = pref.getInt(CGlobals_user.PREFTRAVELDIST, 0);
            CGlobals_user.inWalkWithMe = pref.getBoolean(CGlobals_user.PREFINWALKWITHME, false);
            sEmails = pref.getString(CGlobals_user.PREFINWALKWITHME_EMAILS, "");
        } catch (Exception e) {
            SSLog.e("ActMeter: getPrefs", "error", e.getMessage());
        }
    }

    void keyboardShow(boolean bShow) {
        if (bShow) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        } else {
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    INPUT_METHOD_SERVICE);
            View cFocus = getCurrentFocus();
            if (imm != null && cFocus != null)
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            else
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
    }
} // Activity_BeSafe_Emergency


