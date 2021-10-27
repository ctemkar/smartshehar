package com.jumpinjumpout.apk.driver.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.jumpinjumpout.apk.driver.CGlobals_driver;
import com.jumpinjumpout.apk.driver.Constants_driver;
import com.jumpinjumpout.apk.driver.MyApplication;
import com.jumpinjumpout.apk.driver.R;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.Constants_lib;
import com.jumpinjumpout.apk.lib.RegistrationIntentService;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.lib.ui.VerifyPhone_act;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


public class SplashScreen_act extends Activity {
    private static String TAG = "ActSplashScreen: ";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    String msGcmRegId;
    CGlobals_driver mApp;
    MyApplication myApplication;
    ProgressDialog mProgressDialog;
    int mIsCommercial;
    String msVehicleCategory;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ProgressBar mRegistrationProgressBar;
    private static final int INITIAL_REQUEST = 1337;
    private static final String[] INITIAL_PERMS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.CHANGE_NETWORK_STATE,
            android.Manifest.permission.GET_ACCOUNTS,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.RECEIVE_SMS,
            android.Manifest.permission.WAKE_LOCK,
            android.Manifest.permission.VIBRATE,
            android.Manifest.permission.ACCESS_WIFI_STATE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.READ_PHONE_STATE
    };

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splashscreen);
        if (Build.VERSION.SDK_INT >= 23) {
            if (!canAccessFINELocation() && !canAccessCOARSELocation()) {
                requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
            }else{
                handleMashmallow();
            }
        } else {
            handleMashmallow();
        }

    } // onCreate

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case INITIAL_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    handleMashmallow();
                } else {
                    Toast.makeText(SplashScreen_act.this, "Please allow permission", Toast.LENGTH_LONG).show();
                    customDialog("Please allow permission");

                }
                return;
            }
        }
    }

    private void handleMashmallow() {
        Log.d(TAG, "maxMemory : " + Runtime.getRuntime().maxMemory());
        Log.d(TAG, "totalMemory : " + Runtime.getRuntime().totalMemory());
        myApplication = MyApplication.getInstance();
        mApp = CGlobals_driver.getInstance();
        mApp.init(SplashScreen_act.this);
        SSLog.setContext(SplashScreen_act.this, CGlobals_driver.msGmail);
        mApp.getMyLocation(SplashScreen_act.this);
        /*if (!CGlobals_driver.getInstance().checkConnected(SplashScreen_act.this)) {
            mApp.buildNoConnectionDialog(this).show();
        }*/
        try {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            CGlobals_driver.mIMEI = telephonyManager.getDeviceId();
            CGlobals_driver.mbRegistered = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        mRegistrationProgressBar = (ProgressBar) findViewById(R.id.registrationProgressBar);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                msGcmRegId = intent.getStringExtra(Constants_lib.PREF_TOKEN_SAVED);
                mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(Constants_lib.SENT_TOKEN_TO_SERVER, false);
                CGlobals_driver.mbRegistered = myApplication.getPersistentPreference()
                        .getBoolean(Constants_driver.PREF_REGISTERED, false);
                String sPhone = myApplication.getPersistentPreference().getString(
                        Constants_driver.PREF_PHONENO, "");
                myApplication.getPersistentPreferenceEditor().putString(
                        Constants_driver.PREF_REG_ID, msGcmRegId);
                myApplication.getPersistentPreferenceEditor().commit();
                if (sentToken) {
                    if (!CGlobals_driver.mbRegistered || TextUtils.isEmpty(sPhone)) {
                        Intent intent1 = new Intent(SplashScreen_act.this,
                                VerifyPhone_act.class);
                        startActivityForResult(intent1, 9);
                        sendUserAccess(
                                msGcmRegId,
                                myApplication.getPersistentPreference().getString(
                                        Constants_driver.PREF_USER_TYPE, "U"));
                    } else {
                        sendUserAccess(
                                msGcmRegId,
                                myApplication.getPersistentPreference().getString(
                                        Constants_driver.PREF_USER_TYPE, "U"));
                    }
                } else {
                    if (TextUtils.isEmpty(sPhone)) {
                        Intent intent1 = new Intent(SplashScreen_act.this,
                                VerifyPhone_act.class);
                        startActivityForResult(intent1, 9);
                    } else {
                        intent = new Intent(SplashScreen_act.this, Dashboard_act.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        };

        if (CGlobals_driver.mbRegistered) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.getWindow().setGravity(Gravity.BOTTOM);
            mProgressDialog.setMessage("Loading. Please wait ...");
        }
        mApp = CGlobals_driver.getInstance();
        mApp.init(this);
       /* checkPlayServices();
        msGcmRegId = getRegistrationId(context);
        new AsyncLoad().execute();*/
        if (checkPlayServices()) {
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }


    private boolean canAccessFINELocation() {
        return (hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private boolean canAccessCOARSELocation() {
        return (hasPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION));
    }

    @SuppressLint("NewApi")
    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm));
    }

    public void onDestroy() {
        super.onDestroy();
        if (mProgressDialog != null)
            if (mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
            }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Constants_lib.REGISTRATION_COMPLETE));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
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

    public void sendUserAccess(final String sGcmReg, final String sUserType) {
        String encodedurl;
        final String url = Constants_driver.USER_ACCESS_DRIVER_URL;
        try {
            encodedurl = URLEncoder.encode(url, "UTF-8");
            Log.d("TEST", encodedurl);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_driver.USER_ACCESS_DRIVER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
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
                params.put("gcmregid", sGcmReg);
                params.put("triptype", sUserType);
                params = mApp.getAllMobileParams(params,
                        Constants_driver.USER_ACCESS_DRIVER_URL, SplashScreen_act.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();

                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";

                }
                return CGlobals_driver.getInstance().checkParams(params);
            }
        };
        getMyApplication().addVolleyRequest(postRequest, true);

    } // sendUserAccess

    private void userAccessSucess(String response) {

        String sAppUserId = "-1";

        int iAppUserId = -1;
        try {
            if (response.trim().equals("-1")) {
                Toast.makeText(SplashScreen_act.this, "Your device not registered", Toast.LENGTH_LONG).show();
                customDialog("Your device not registered");

            }
            if (!response.trim().equals("-1")) {

                Log.d(TAG, "Useraccess_url - " + response);
                JSONObject jResponse = new JSONObject(response);
                Log.d(TAG, response);
                sAppUserId = jResponse.isNull("appuser_id") ? "-1" : jResponse
                        .getString("appuser_id");
                if (!sAppUserId.trim().equals("-1")) {
                    mIsCommercial = jResponse.isNull("is_commercial") ? 0
                            : jResponse.getInt("is_commercial");
                    msVehicleCategory = jResponse.isNull("vehicle_category") ? ""
                            : jResponse.getString("vehicle_category");
                    int isAdmin = jResponse.isNull("is_admin") ? 0 : jResponse
                            .getInt("is_admin");
                    iAppUserId = Integer.parseInt(sAppUserId);
                    Log.d(TAG, " sAppUserId: " + sAppUserId);
                    Log.d(TAG, " iscommercial: " + mIsCommercial);
                    if (mIsCommercial > 0) {
                        getMyApplication().getPersistentPreferenceEditor()
                                .putString(Constants_driver.PREF_USER_TYPE,
                                        Constants_driver.TRIP_TYPE);
                    } else {
                        getMyApplication().getPersistentPreferenceEditor()
                                .putString(Constants_driver.PREF_USER_TYPE,
                                        Constants_driver.NORMAL_USER);
                    }
                    if (isAdmin > 0) {
                        getMyApplication().getPersistentPreferenceEditor()
                                .putBoolean(Constants_driver.PREF_IS_ADMIN, true);
                    } else {
                        getMyApplication().getPersistentPreferenceEditor()
                                .putBoolean(Constants_driver.PREF_IS_ADMIN, false);
                    }
                    if (!TextUtils.isEmpty(msVehicleCategory)) {
                        getMyApplication().getPersistentPreferenceEditor()
                                .putString(Constants_driver.PREF_VEHICLE_CATEGORY, msVehicleCategory);

                    }
                    if ((jResponse.isNull("show_cab_notifications") ? 0 : jResponse.getInt("show_cab_notifications")) == 1) {
                        getMyApplication().getPersistentPreferenceEditor()
                                .putBoolean(Constants_driver.PREF_CAB_ALERTS, true);
                    } else {
                        getMyApplication().getPersistentPreferenceEditor()
                                .putBoolean(Constants_driver.PREF_CAB_ALERTS, false);
                    }
                    if ((jResponse.isNull("receive_notifications") ? 0 : jResponse.getInt("receive_notifications")) == 1) {
                        getMyApplication().getPersistentPreferenceEditor()
                                .putBoolean(
                                        Constants_driver.PREF_RECEIVE_NOTIFICATIONS,
                                        true);
                    } else {
                        getMyApplication().getPersistentPreferenceEditor()
                                .putBoolean(
                                        Constants_driver.PREF_RECEIVE_NOTIFICATIONS,
                                        false);
                    }
                }
            }
            if (sAppUserId.trim().equals("-1")) { // registration probably

            } else {
                getMyApplication().getPersistentPreferenceEditor().putInt(
                        Constants_driver.PREF_APPUSERID, iAppUserId);
                if (mIsCommercial > 0) {
                    getMyApplication().getPersistentPreferenceEditor()
                            .putString(Constants_driver.PREF_TRIP_TYPE,
                                    Constants_driver.TRIP_TYPE);
                } else {
                    getMyApplication().getPersistentPreferenceEditor()
                            .putString(Constants_driver.PREF_TRIP_TYPE,
                                    Constants_driver.TRIP_TYPE);
                }

                getMyApplication().getPersistentPreferenceEditor().commit();
                mApp.miAppUserId = iAppUserId;
                startRightActivity();
            }
        } catch (Exception e) {
            SSLog.e(TAG, "sendUserAccess Response: " + response, e);
            Log.d("Response", response);
            Toast.makeText(getMyApplication(),
                    "Bad data received, try after some time", Toast.LENGTH_LONG)
                    .show();
            customDialog("Bad data received, try after some time");

        }

    } // userAccessSuccess

    private void userAccessFailure(VolleyError error) {
        cancelProgress();
        startRightActivity();
        try {
            SSLog.e(TAG, "userAccessFailure :-   ", error.getMessage());
            mApp.getVolleyError(error, SplashScreen_act.this);
        } catch (Exception e) {
            cancelProgress();
            Toast.makeText(
                    SplashScreen_act.this.getBaseContext(),
                    getString(R.string.retry_internet),
                    Toast.LENGTH_SHORT).show();
            customDialog(getString(R.string.retry_internet));
            SSLog.e(TAG, "sendUserAccess - Response.ErrorListener", e);

        }

    }

    MyApplication getMyApplication() {
        if (myApplication == null) {
            myApplication = MyApplication.getInstance();
        }
        return myApplication;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        CGlobals_driver.mbRegistered = myApplication.getPersistentPreference()
                .getBoolean(Constants_driver.PREF_REGISTERED, false);
        String sPhone = myApplication.getPersistentPreference().getString(
                Constants_driver.PREF_PHONENO, "");
        if (!CGlobals_driver.mbRegistered || TextUtils.isEmpty(sPhone)) {
            Intent intent = new Intent(SplashScreen_act.this,
                    VerifyPhone_act.class);
            startActivityForResult(intent, 9);

        } else {
            sendUserAccess(msGcmRegId, myApplication.getPersistentPreference()
                    .getString(Constants_driver.PREF_USER_TYPE, "U"));

        }

    }

    void startRightActivity() {
        Intent intent;
        if (myApplication.getPersistentPreference().getInt(
                Constants_driver.PREF_APPUSERID, -1) == -1
                && myApplication.getPersistentPreference().getBoolean(
                Constants_driver.PREF_REGISTERED, false)) {

            Toast.makeText(
                    SplashScreen_act.this.getBaseContext(),
                    "Cannot verify credentials.\nPlease check your connection and try again",
                    Toast.LENGTH_LONG).show();
            customDialog("Cannot verify credentials.\nPlease check your connection and try again");

        }

        try {
            getCountry();
        } catch (Exception e) {
            SSLog.e(TAG, "sendUserAccess - Response.ErrorListener", e);
        }
        if (CGlobals_lib.getInstance().isInTrip(SplashScreen_act.this)) {
            intent = new Intent(SplashScreen_act.this, ForHireSharedTrip_act.class);
            startActivity(intent);
            finish();
        } else {
            intent = new Intent(SplashScreen_act.this, Dashboard_act.class);
            startActivity(intent);
            finish();
        }

    }

    void cancelProgress() {
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
            }
        }
    }

    public void getCountry() {
        Location location = CGlobals_driver.getInstance().getMyLocation(SplashScreen_act.this);
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
            MyApplication.getInstance().addVolleyRequest(postRequest, false);
        } catch (Exception e) {
            SSLog.e(TAG, "getActiveuser CGlobals_driver.getInstance().mVolleyReq..", e);
        }
    } // getCountry

    String getCountryCode(String response) {
        String cc = null;
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(response);
            JSONArray addrComp = ((JSONArray) jsonObject.get("results"))
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(cc)) {
            MyApplication.getInstance().getPreferenceEditor()
                    .putString(Constants_driver.PREF_CURRENT_COUNTRY, cc);
            MyApplication.getInstance().getPreferenceEditor().commit();
        }
        return cc;
    }

    private void customDialog(String message){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(SplashScreen_act.this);
        builder1.setMessage(message);
        builder1.setCancelable(true);
        builder1.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        builder1.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        if (!SplashScreen_act.this.isFinishing()) {
            alert11.show();
        }
    }
} // ActivitySpashScreen