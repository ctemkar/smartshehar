package com.smartshehar.cabe.ui;


import android.annotation.SuppressLint;
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
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.smartshehar.cabe.CGeo;
import com.smartshehar.cabe.CGlobals_CabE;
import com.smartshehar.cabe.Constants_CabE;
import com.smartshehar.cabe.ContactsAutoComplete;
import com.smartshehar.cabe.LocationService;
import com.smartshehar.cabe.MyApplication_CabE;
import com.smartshehar.cabe.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lib.app.util.AppRater;
import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;
import lib.app.util.Constants_lib_ss;
import lib.app.util.SSLog_SS;

/**
 * Created by Soumen on 09-10-2015.
 * this class share my location to email
 */

public class WalkWithMe_act extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private static final String TAG = "WalkWithMe_act: ";
    GoogleApiClient mGoogleApiClient = null;
    private Button btnStart;
    private ContactsAutoComplete acEmail;
    private String sEmails = "";
    private Location mCurrentLocation = null;
    Connectivity mConnectivity;
    int miTripId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConnectivity = new Connectivity();
        mGoogleApiClient = new GoogleApiClient.Builder(WalkWithMe_act.this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(WalkWithMe_act.this)
                .addOnConnectionFailedListener(WalkWithMe_act.this)
                .build();
        mGoogleApiClient.connect();
        mCurrentLocation = CGlobals_lib_ss.getInstance().getBestLocation(WalkWithMe_act.this);
        if (mCurrentLocation != null) {
            Toast.makeText(this, "Got Location!", Toast.LENGTH_SHORT).show();
        }
        CGlobals_lib_ss.getInstance().init(this);
        CGlobals_lib_ss.getInstance().getMyLocation(WalkWithMe_act.this);
        /*ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);*/
        setContentView(R.layout.activity_walkwithme);
        if (!Connectivity.checkConnected(WalkWithMe_act.this)) {
            if (!mConnectivity.connectionError(WalkWithMe_act.this, getString(R.string.app_label))) {
                if (mConnectivity.isGPSEnable(WalkWithMe_act.this)) {
                    Log.d(TAG, "Internet Connection");
                }
            }
        }
        readPrefs();
        btnStart = (Button) findViewById(R.id.btnStart);
        acEmail = (ContactsAutoComplete) findViewById(R.id.acEmail);
        if (!TextUtils.isEmpty(sEmails))
            acEmail.setText(sEmails);
        AppRater.app_launched(this, getString(R.string.pageDashboardLong),
                CGlobals_lib_ss.getInstance().mPackageInfo.packageName);
        PreferenceManager.getDefaultSharedPreferences(this);


        btnStart.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            public void onClick(View v) {
                sEmails = acEmail.getText().toString();
                if (CGlobals_CabE.inWalkWithMe) {    // Stop WalkWithMe
                    CGlobals_CabE.inWalkWithMe = false;
                    btnStart.setText("Start");
                    stopService(new Intent(WalkWithMe_act.this, LocationService.class));
                } else {    // Start WalkWithMe
                    if (sEmails.contains("@") && sEmails.contains(".")) {
                        CGlobals_CabE.inWalkWithMe = true;
                        btnStart.setText("Stop");
                        CGlobals_CabE.walkWithMeMode = "A";
                        stopService(new Intent(WalkWithMe_act.this, LocationService.class));
                        startService(new Intent(WalkWithMe_act.this, LocationService.class));
                        sendLocation();
                    } else {
                        Toast.makeText(WalkWithMe_act.this, "Please enter valid email id/s", Toast.LENGTH_SHORT).show();
                    }
                }
                writePrefs();
                stopService(new Intent(WalkWithMe_act.this, LocationService.class));
                finish();
            }
        });

    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!CGlobals_CabE.inWalkWithMe)
                return;
            if (mCurrentLocation != null) {
                CGlobals_CabE.walkWithMeMode = "U";
                sendLocation();
            }
        }
    };

    private void sendLocation() {
        miTripId = CGlobals_lib_ss.getInstance().getPersistentPreference(WalkWithMe_act.this).
                getInt(Constants_CabE.PREF_TRIP_ID_PASSENGER, -1);
        final String url = Constants_CabE.WALKWITHME_UPDATEPOSITION_URL;
        try {
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (!response.trim().equals("-1")) {
                                if (CGlobals_CabE.walkWithMeMode.equals("A")) {
                                    Toast.makeText(WalkWithMe_act.this,
                                            "Email sent to: " + sEmails +
                                                    " to 'Walk with you'\n They will see your position on a map",
                                            Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(WalkWithMe_act.this, "Could not send request ", Toast.LENGTH_LONG).show();
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
                    Location location = CGlobals_CabE.getInstance().getMyLocation(WalkWithMe_act.this);
                    Calendar cal = Calendar.getInstance();
                    params.put("user_email", CGlobals_lib_ss.getInstance().getPersistentPreference(WalkWithMe_act.this)
                            .getString(Constants_lib_ss.KEY_PREF_EMAIL, ""));
                    params.put("email", CGlobals_lib_ss.getInstance().getPersistentPreference(WalkWithMe_act.this)
                            .getString(Constants_lib_ss.KEY_PREF_EMAIL, ""));
                    params.put("friend_email", sEmails);
                    params.put("mode", CGlobals_CabE.walkWithMeMode);
                    params.put("stopped", CGlobals_CabE.inWalkWithMe ? "N" : "Y");
                    params.put("y", Integer.toString(cal.get(Calendar.YEAR)));
                    params.put("o", Integer.toString(cal.get(Calendar.MONTH)));
                    params.put("d", Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
                    params.put("h", Integer.toString(cal.get(Calendar.HOUR_OF_DAY)));
                    params.put("m", Integer.toString(cal.get(Calendar.MINUTE)));
                    params.put("s", Integer.toString(cal.get(Calendar.SECOND)));
                    SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(WalkWithMe_act.this);
                    String msName = mSettings.getString(Constants_CabE.KEY_PREF_MY_NAME, "");
                    String msPhone = mSettings.getString(Constants_CabE.KEY_PREF_MY_NUMBER, "");
                    params.put("name", msName);
                    params.put("phone", msPhone);
                    params.put("tripid", String.valueOf(miTripId));
                    try {
                        if (location != null) {
                            CGeo geo = new CGeo(WalkWithMe_act.this);
                            geo.getAddress(location);
                            String address = geo.mAddr.firstAddressString;

                            if (!TextUtils.isEmpty(address)) {
                                params.put("address", address);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace(); // getFromLocation() may sometimes fail
                    }
                    params = CGlobals_lib_ss.getInstance().getAllMobileParams(params,
                            url, Constants_CabE.APP_CODE, WalkWithMe_act.this);
                    Log.d(TAG, "PARAM IS " + params);
                    return CGlobals_lib_ss.getInstance().checkParams(params);
                }
            };
            MyApplication_CabE.getInstance().getRequestQueue().add(postRequest);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ERROR IS " + e.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        keyboardShow(false);

        readPrefs();
        if (CGlobals_CabE.haveNetworkConnection(WalkWithMe_act.this) == 0) {
            Log.e(TAG, "Error Network Connection");
        }
        registerReceiver(broadcastReceiver,
                new IntentFilter(LocationService.BROADCAST_ACTION));
        if (!isServiceRunning(WalkWithMe_act.this, "com.smartshehar.cabe.LocationService")) {
            CGlobals_CabE.inWalkWithMe = false;
        }

        if (!CGlobals_CabE.inWalkWithMe) {
            stopService(new Intent(WalkWithMe_act.this, LocationService.class));
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
        SharedPreferences mPref = getSharedPreferences(CGlobals_CabE.PREFS_NAME, 0);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putBoolean(CGlobals_CabE.PREFINWALKWITHME, CGlobals_CabE.inWalkWithMe);
        editor.putInt(CGlobals_CabE.PREFTRAVELDIST, CGlobals_CabE.miTravelDistance);
        editor.putString(CGlobals_CabE.PREFINWALKWITHME_EMAILS, sEmails);
        editor.apply();
    }

    private void readPrefs() {
        SharedPreferences pref = getSharedPreferences(CGlobals_CabE.PREFS_NAME, 0);
        try {
            CGlobals_CabE.miTravelDistance = pref.getInt(CGlobals_CabE.PREFTRAVELDIST, 0);
            CGlobals_CabE.inWalkWithMe = pref.getBoolean(CGlobals_CabE.PREFINWALKWITHME, false);
            sEmails = pref.getString(CGlobals_CabE.PREFINWALKWITHME_EMAILS, "");
        } catch (Exception e) {
            SSLog_SS.e("ActMeter: getPrefs", "error", e, WalkWithMe_act.this);
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


