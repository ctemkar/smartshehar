package com.smartshehar.dashboard.app.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.smartshehar.android.app.CGlobals_trains;
import com.smartshehar.chat.gcm.Config;
import com.smartshehar.dashboard.app.CGlobals_db;
import com.smartshehar.dashboard.app.Constants_dp;
import com.smartshehar.dashboard.app.GcmIntentService;
import com.smartshehar.dashboard.app.PermissionUtil;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.RegistrationIntentService;
import com.smartshehar.dashboard.app.SSApp;
import com.smartshehar.dashboard.app.SSLog;
import com.smartshehar.dashboard.app.SendDraftDataService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import in.bestbus.app.CGlobals_BA;
import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Constants_lib_ss;

public class SmartShehar_SplashsScreen_act extends Activity {
    private static String TAG = "SmartShehar_SplashsScreen_act";
    String msGcmRegId;
    ProgressDialog mProgressDialog;
    CGlobals_db cglobal;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private static final int INITIAL_REQUEST = 1337;
    Snackbar snackbar;
    private CoordinatorLayout coordinatorLayout;
    private static final String[] INITIAL_PERMS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.READ_CONTACTS
    };

    private void showRequirementOfPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_PHONE_STATE) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CONTACTS))
            customDialog(getString(R.string.location_read_contact));

    }
    private void requestPermission()
    {
        ActivityCompat.requestPermissions(this, INITIAL_PERMS, INITIAL_REQUEST);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        if (Build.VERSION.SDK_INT >= 23) {
            if (!checkPermission())
                requestPermission();
            else handleMashmallow();
        } else
            handleMashmallow();
    }

    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                ) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case INITIAL_REQUEST:
                // If request is cancelled, the result arrays are empty.
                if (PermissionUtil.verifyPermissions(grantResults)) {
                    handleMashmallow();
                } else {
                    showRequirementOfPermission();
                }
                break;

        }
    }

    private boolean checkExternalPermission() {

        String permission = "android.permission.READ_PHONE_STATE";
        int res = checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private void handleMashmallow() {
        try {
            if (!TextUtils.isEmpty(msGcmRegId)) {
                subscribeToGlobalTopic();
            }
            CGlobals_BA.getInstance().init(SmartShehar_SplashsScreen_act.this);
            CGlobals_trains.getInstance().init(SmartShehar_SplashsScreen_act.this);
            CGlobals_db.getInstance(SmartShehar_SplashsScreen_act.this).init(SmartShehar_SplashsScreen_act.this);
            startService(new Intent(this, SendDraftDataService.class));
            cglobal = CGlobals_db.getInstance(SmartShehar_SplashsScreen_act.this);
            if (checkExternalPermission()) {
                try {
                    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    CGlobals_db.mIMEI = telephonyManager.getDeviceId();
                } catch (Exception e) {
                    e.printStackTrace();
                    SSLog.e(TAG, "handleMashmallow ", e.toString());
                }
            }
            KeyguardManager myKM = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
            if (myKM.inKeyguardRestrictedInputMode()) {
                finish();
            }
            CGlobals_db.mbRegistered = true;
            if (CGlobals_db.mbRegistered) {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.getWindow().setGravity(Gravity.BOTTOM);
                mProgressDialog.setMessage("Loading... Please wait...");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
            }
            mRegistrationBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                        // gcm successfully registered
                        // now subscribe to `global` topic to receive app wide notifications
                        subscribeToGlobalTopic();
                    }
                    msGcmRegId = intent.getStringExtra(Constants_dp.PREF_TOKEN_SAVED);
                    boolean sentToken = CGlobals_lib_ss.getInstance()
                            .getPersistentPreference(SmartShehar_SplashsScreen_act.this).getBoolean(Constants_dp.SENT_TOKEN_TO_SERVER, false);
                    CGlobals_lib_ss.getInstance()
                            .getPersistentPreferenceEditor(SmartShehar_SplashsScreen_act.this).putString(
                            Constants_dp.PREF_REG_ID, msGcmRegId).commit();

                    if (sentToken) {
                        sendUserAccess(
                                msGcmRegId,
                                CGlobals_lib_ss.getInstance()
                                        .getPersistentPreference(SmartShehar_SplashsScreen_act.this).getString(
                                        Constants_dp.PREF_USER_TYPE, "U"));
                    }
                    userVerification();
                }
            };
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(mRegistrationBroadcastReceiver, filter);

            if (checkPlayServices()) {
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            } else {
                Log.i(TAG, "No valid Google Play Services APK found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            SSLog.e(TAG, "handleMashmallow() ", e.toString());
        }
    }

    private void userVerification() {
        Intent intent;
        // dismiss your dialog

        if (CGlobals_lib_ss.getInstance()
                .getPersistentPreference(SmartShehar_SplashsScreen_act.this).getBoolean(Constants_lib_ss.PREF_SKIPPED, false)) {
            intent = new Intent(SmartShehar_SplashsScreen_act.this, SmartShehar_Dashboard_act.class);
            startActivity(intent);
            finish();
        } else {
            if (!CGlobals_lib_ss.getInstance()
                    .getPersistentPreference(SmartShehar_SplashsScreen_act.this).getBoolean(Constants_lib_ss.PREF_REGISTERED, false)) {
                intent = new Intent(SmartShehar_SplashsScreen_act.this, ActVerifyNumber.class);
                startActivityForResult(intent, Constants_lib_ss.Request_Code);
            } else {
                if (CGlobals_db.mbStartTripPressed || CGlobals_db.mbInTrip) {
                    intent = new Intent(SmartShehar_SplashsScreen_act.this, ActMeter.class);
                } else {
                    intent = new Intent(SmartShehar_SplashsScreen_act.this, SmartShehar_Dashboard_act.class);
                }
                startActivity(intent);
                finish();
            }
        }
    }

    private void customDialog(String message) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(SmartShehar_SplashsScreen_act.this);
        builder1.setMessage(message);
        builder1.setCancelable(true);
        builder1.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                       requestPermission();
                    }
                });
        builder1.setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                       finish();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.activity_splash_screen, menu);
        return true;
    }


    public void sendUserAccess(final String sGcmReg, final String sUserType) {
        String encodedurl;
        final String url = Constants_dp.USER_ACCESS_URL;

        try {
            encodedurl = URLEncoder.encode(url, "UTF-8");
            Log.d("TEST", encodedurl);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_dp.USER_ACCESS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("ASMITA", "res " + response);
                        userAccessSucess(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                userAccessFailure(error);
                SSLog.e(TAG, "sendUserAccess :-   ", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                if (!TextUtils.isEmpty(sGcmReg)) {
                    params.put("gcmregid", sGcmReg);
                }
                params.put("triptype", sUserType);
                params = CGlobals_db.getInstance(SmartShehar_SplashsScreen_act.this).getBasicMobileParams(params,
                        url, SmartShehar_SplashsScreen_act.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();

                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }

                @SuppressWarnings("unused")
                String debugUrl;
                try {
                    debugUrl = url + "?" + getParams.toString() + "&verbose=Y";
                    Log.e(TAG, debugUrl);
                } catch (Exception e) {
                    SSLog.e(TAG, "getPassengers map - ", e);
                }

                return CGlobals_db.getInstance(SmartShehar_SplashsScreen_act.this).checkParams(params);
            }
        };
        SSApp.getInstance().addVolleyRequest(postRequest, true);

    } // sendUserAccess


    private void userAccessSucess(String response) {
        String sAppUserId, sAppUsageId, sShowNotification;
        int iAppUserId, iAppUsageId, iShowNotification;
        try {
            if (!response.trim().equals("-1")) {
                Log.d(TAG, "Useraccess_url - " + response);
                JSONObject jResponse = new JSONObject(response);
                Log.d(TAG, response);

                sAppUserId = jResponse.isNull("appuser_id") ? "-1" : jResponse
                        .getString("appuser_id");
                iAppUserId = Integer.parseInt(sAppUserId);

                sAppUsageId = jResponse.isNull("appusage_id") ? "-1" : jResponse
                        .getString("appusage_id");
                iAppUsageId = Integer.parseInt(sAppUsageId);

                sShowNotification = jResponse.isNull("show_notification") ? "1" : jResponse
                        .getString("show_notification");
                iShowNotification = Integer.parseInt(sShowNotification);

                SSApp.getInstance()
                        .getPreferenceEditor().putInt(Constants_lib_ss.PREF_APPUSERID, iAppUserId).commit();
                CGlobals_lib_ss.getInstance()
                        .getPersistentPreferenceEditor(SmartShehar_SplashsScreen_act.this).putInt(Constants_lib_ss.PREF_APPUSERID, iAppUserId).commit();

                CGlobals_lib_ss.getInstance()
                        .getPersistentPreferenceEditor(SmartShehar_SplashsScreen_act.this).putInt(Constants_lib_ss.PREF_APPUSAGEID, iAppUsageId).commit();

                CGlobals_lib_ss.getInstance()
                        .getPersistentPreferenceEditor(SmartShehar_SplashsScreen_act.this).putInt(Constants_dp.PREF_SHOW_NOTIFICATION, iShowNotification).commit();
            }
        } catch (Exception e) {
            SSLog.e(TAG, "sendUserAccess Response: " + response, e);
            Log.d("Response", response);
            showSnackbar("Bad data received, try after some time");
            finish();
        }
    } // userAccessSuccess

    void startRightActivity() {

        if (CGlobals_lib_ss.getInstance()
                .getPersistentPreference(SmartShehar_SplashsScreen_act.this).getInt(
                        Constants_lib_ss.PREF_APPUSERID, -1) == -1
                && CGlobals_lib_ss.getInstance()
                .getPersistentPreference(SmartShehar_SplashsScreen_act.this).getBoolean(
                        Constants_dp.PREF_REGISTERED, false)) {

            showSnackbar("Cannot verify credentials.\nPlease check your connection and try again");
            finish();

            return;
        }

        try {
            getCountry();
        } catch (Exception e) {
            SSLog.e(TAG, "sendUserAccess - Response.ErrorListener", e);
        }

//        intent = new Intent(SplashScreen_act.this, Dashboard_act.class);
//        startActivity(intent);
//        finish();
    }

    public void getCountry() {
        Location location = CGlobals_db.getInstance(this).getMyLocation(this);
        if (location == null) {

            return;
        }
        final String url = "http://maps.google.com/maps/api/geocode/json?latlng="
                + location.getLatitude()
                + ","
                + location.getLongitude()
                + "&sensor=false";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getCountryCode(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // handler.postDelayed(runnableActivePassengers,
                // Constants..PASSENGER_UPDATE_INTERVAL);
                try {
                    SSLog.e(TAG,
                            "getCountry Response.ErrorListener - ",
                            error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG,
                            " getCountry Response.ErrorListener (2) - ",
                            e);
                }
            }

        });
        try {
            SSApp.getInstance().addVolleyRequest(postRequest, false);
        } catch (Exception e) {
            SSLog.e(TAG, "getActiveuser CGlobals.getInstance().mVolleyReq..", e);
        }
    } // getCountry

    String getCountryCode(String response) {
        String cc = null;
        JSONObject jsonObject;
        // String addr = null;
        try {
            jsonObject = new JSONObject(response);
            // JSONObject joResult = ((JSONArray) jsonObject.get("contactResults"))
            // .getJSONObject(0);
            JSONArray addrComp = ((JSONArray) jsonObject.get("contactResults"))
                    .getJSONObject(0).getJSONArray("address_components");
            JSONObject joAddrComp;
            String sType;
            int len = addrComp.length();
            for (int i = 0; i < len; i++) {
                joAddrComp = (JSONObject) addrComp.get(i);
                sType = joAddrComp.get("types").toString();

                if (sType.contains("country")) {
                    cc = joAddrComp.getString("short_name");
                }

            }
        } catch (JSONException e) {

            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(cc)) {
            CGlobals_lib_ss.getInstance()
                    .getPersistentPreferenceEditor(SmartShehar_SplashsScreen_act.this).putString(Constants_dp.PREF_CURRENT_COUNTRY, cc).commit();
        }
        return cc;

    }

    private void userAccessFailure(VolleyError error) {
        cancelProgress();
        startRightActivity();
        try {
            // String volleyError = error.toString();
            SSLog.e(TAG, "userAccessFailure :-   ", error.getMessage());
            CGlobals_db.getVolleyError(this, error);
        } catch (Exception e) {
            cancelProgress();
            showSnackbar("Cannot connect to the internet.\nPlease check your connection and try again");
            SSLog.e(TAG, "sendUserAccess - Response.ErrorListener", e);
            finish();
        }

    }

    void cancelProgress() {
        try {
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            SSLog.e(TAG, "cancelProgress", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (CGlobals_lib_ss.getInstance()
                .getPersistentPreference(SmartShehar_SplashsScreen_act.this).getBoolean(Constants_lib_ss.PREF_SKIPPED, false)) {
            Intent intent = new Intent(SmartShehar_SplashsScreen_act.this, SmartShehar_Dashboard_act.class);
            startActivity(intent);
            sendUserAccess(
                    msGcmRegId,
                    CGlobals_lib_ss.getInstance()
                            .getPersistentPreference(SmartShehar_SplashsScreen_act.this).getString(
                            Constants_dp.PREF_USER_TYPE, "U"));
            finish();
        } else {
            CGlobals_lib_ss.getInstance()
                    .getPersistentPreference(SmartShehar_SplashsScreen_act.this).getBoolean(Constants_lib_ss.PREF_REGISTERED, false);
            String sPhone = CGlobals_lib_ss.getInstance()
                    .getPersistentPreference(SmartShehar_SplashsScreen_act.this).getString(Constants_lib_ss.PREF_PHONENO, "");
            if (!CGlobals_db.mbRegistered || TextUtils.isEmpty(sPhone)) {
                Intent intent = new Intent(SmartShehar_SplashsScreen_act.this,
                        ActVerifyNumber.class);
                startActivityForResult(intent, 9);

            } else {
                getAppUserDetails();
                Intent intent = new Intent(SmartShehar_SplashsScreen_act.this, SmartShehar_Dashboard_act.class);
                startActivity(intent);
                sendUserAccess(
                        msGcmRegId,
                        CGlobals_lib_ss.getInstance()
                                .getPersistentPreference(SmartShehar_SplashsScreen_act.this).getString(
                                Constants_dp.PREF_USER_TYPE, "U"));
                finish();
            }
        }

    }

    private void sendMobileNoToServer() {
        try {
            final String mobileno = CGlobals_lib_ss.getInstance()
                    .getPersistentPreference(SmartShehar_SplashsScreen_act.this).
                            getString(Constants_lib_ss.PREF_PHONENO, "");
            final String url = Constants_dp.UPDATE_APPUSER_DETAILS_URL;
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (!response.trim().equals("-1")) {
                                Log.d(TAG, "" + response);
                            } else {
                                showSnackbar("Verification failed!");
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    SSLog.e(TAG, "sendMobileNoToServer()", error.toString());
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    //
                    String user_Name = CGlobals_lib_ss.getInstance()
                            .getPersistentPreference(SmartShehar_SplashsScreen_act.this).getString(Constants_dp.PREF_USER_NAME, "");
                    String user_Full_Name = CGlobals_lib_ss.getInstance()
                            .getPersistentPreference(SmartShehar_SplashsScreen_act.this).getString(Constants_dp.PREF_USER_FULL_NAME, "");
                    String user_Age = CGlobals_lib_ss.getInstance()
                            .getPersistentPreference(SmartShehar_SplashsScreen_act.this).getString(Constants_dp.PREF_USER_AGE, "");
                    String user_Gender = CGlobals_lib_ss.getInstance()
                            .getPersistentPreference(SmartShehar_SplashsScreen_act.this).getString(Constants_dp.PREF_USER_GENDER, "");

                    if (!TextUtils.isEmpty(user_Name))
                        params.put("username", String.valueOf(user_Name));
                    if (!TextUtils.isEmpty(mobileno))
                        params.put("phoneno", String.valueOf(mobileno));
                    if (!TextUtils.isEmpty(user_Gender) && !TextUtils.isEmpty("GENDER"))
                        params.put("sex", String.valueOf(user_Gender));
                    if (!TextUtils.isEmpty(user_Full_Name))
                        params.put("fullname", String.valueOf(user_Full_Name));
                    if (!TextUtils.isEmpty(user_Age))
                        params.put("age", String.valueOf(user_Age));

                    params = CGlobals_db.getInstance(SmartShehar_SplashsScreen_act.this).getBasicMobileParams(params,
                            url, SmartShehar_SplashsScreen_act.this);

                    Log.d(TAG, "PARAM IS " + params);
                    return CGlobals_db.getInstance(SmartShehar_SplashsScreen_act.this).checkParams(params);
                }
            };
            CGlobals_db.getInstance(SmartShehar_SplashsScreen_act.this).getRequestQueue(SmartShehar_SplashsScreen_act.this).add(postRequest);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ERROR IS " + e.toString());
        }
    }

    private void getAppUserDetails() {
//        boolean status = false;
        try {

            mProgressDialog.setMessage("Connecting..."); //
//            mProgressDialog.show();
            final String url = Constants_dp.GET_APPUSER_DETAILS_URL;

            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (!response.trim().equals("-1")) {
                                parseJson(response);
                            } else {
                                showSnackbar("Failed!");
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    SSLog.e(TAG, "getAppUserDetails ", error.toString());
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params = CGlobals_db.getInstance(SmartShehar_SplashsScreen_act.this).getBasicMobileParams(params,
                            url, SmartShehar_SplashsScreen_act.this);
                    return CGlobals_db.getInstance(SmartShehar_SplashsScreen_act.this).checkParams(params);
                }
            };
            CGlobals_db.getInstance(SmartShehar_SplashsScreen_act.this).getRequestQueue(SmartShehar_SplashsScreen_act.this).add(postRequest);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ERROR IS " + e.toString());
        }
//        return false;
    }

    private void parseJson(String data) {
        String username, fullname, age,
                sex, phoneno;

        JSONObject jobj;
        try {
            jobj = new JSONObject(data);
            username = jobj.isNull("username") ? "" : jobj.getString("username");
            fullname = jobj.isNull("fullname") ? "" : jobj.getString("fullname");
            age = jobj.isNull("age") ? "" : jobj.getString("age");
            sex = jobj.isNull("sex") ? "" : jobj.getString("sex");
            phoneno = jobj.isNull("phoneno") ? "" : jobj.getString("phoneno");
            CGlobals_lib_ss.getInstance()
                    .getPersistentPreferenceEditor(SmartShehar_SplashsScreen_act.this).putString(Constants_dp.PREF_USER_NAME, username).commit();
            CGlobals_lib_ss.getInstance()
                    .getPersistentPreferenceEditor(SmartShehar_SplashsScreen_act.this).putString(Constants_dp.PREF_USER_FULL_NAME, fullname).commit();
            CGlobals_lib_ss.getInstance()
                    .getPersistentPreferenceEditor(SmartShehar_SplashsScreen_act.this).putString(Constants_dp.PREF_USER_AGE, age).commit();
            CGlobals_lib_ss.getInstance()
                    .getPersistentPreferenceEditor(SmartShehar_SplashsScreen_act.this).putString(Constants_dp.PREF_USER_GENDER, sex).commit();
            if (!TextUtils.isEmpty(phoneno))
                CGlobals_lib_ss.getInstance()
                        .getPersistentPreferenceEditor(SmartShehar_SplashsScreen_act.this).putString(Constants_lib_ss.PREF_PHONENO,
                        phoneno).commit();
            sendMobileNoToServer();

        } catch (JSONException e) {
            SSLog.e(TAG, "parseJson", e);
            e.printStackTrace();
        }
    }


    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Constants_dp.REGISTRATION_COMPLETE));
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        this.getWindow().setAttributes(params);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    public void showSnackbar(String msg) {
        snackbar = Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    // subscribing to global topic
    private void subscribeToGlobalTopic() {
        Intent intent = new Intent(this, GcmIntentService.class);
        intent.putExtra(GcmIntentService.KEY, GcmIntentService.SUBSCRIBE);
        intent.putExtra(GcmIntentService.TOPIC, Config.TOPIC_GLOBAL);
        startService(intent);
    }


    @Override
    protected void onStop() {
        try {
            if (mRegistrationBroadcastReceiver != null)
                unregisterReceiver(mRegistrationBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
            SSLog.e(TAG, "onStop ", e.toString());
        }
        super.onStop();
    }
} // Activity_SplashScreen