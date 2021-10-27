package com.smartshehar.cabe.driver.ui;

import android.Manifest;
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
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.smartshehar.cabe.driver.Constants_CED;
import com.smartshehar.cabe.driver.PermissionUtil;
import com.smartshehar.cabe.driver.R;
import com.smartshehar.cabe.driver.RegistrationIntentService_CED;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;
import lib.app.util.Constants_lib_ss;
import lib.app.util.SSLog_SS;

/**
 * Created by ctemkar on 23/10/2015.
 * app first screen and send all mobile, account, network information to server
 */

public class SplashScreen_Driver_act extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static String TAG = "SplashScreen_Driver: ";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private CoordinatorLayout coordinatorLayout;
    Snackbar snackbar;
    String msGcmRegId;
    Connectivity mConnectivity;
    // for fused location
    Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private boolean isLocationSent = false;
    ProgressDialog mProgressDialog;
    private Handler handler = new Handler();
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private CircularProgressView mRegistrationProgressBar;
    private static final int INITIAL_REQUEST = 13;
    private boolean isVerifyDoneFlag = false;
    String sVerifyed_flag, sManuallyVerified;
    private boolean isLoginDoneFlag = false;
    int countError = 1;
    String sVerifiedflag = "-1", msPhoneNo, msCountryCode, sManually_Verified = "-1";
    private static final String[] INITIAL_PERMS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.GET_ACCOUNTS,
            android.Manifest.permission.READ_CONTACTS
    };

    private void requestAllPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_PHONE_STATE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.GET_ACCOUNTS)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_CONTACTS)) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(SplashScreen_Driver_act.this);
            builder1.setMessage("This app cannot work without the following permissions:\nLocation\nStorage\nPhone\nContacts\nAccounts");
            builder1.setCancelable(true);
            builder1.setPositiveButton("Grant permission",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
            AlertDialog alert11 = builder1.create();
            if (!SplashScreen_Driver_act.this.isFinishing()) {
                alert11.show();
            }
        } else {
            ActivityCompat.requestPermissions(this, INITIAL_PERMS, INITIAL_REQUEST);
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Remove notification bar
        buildGoogleApiClient();
        CGlobals_lib_ss.setMyLocation(CGlobals_lib_ss.mCurrentLocation, false, getApplicationContext());
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splashscreen_act);
        mConnectivity = new Connectivity();
        if (mConnectivity.connectionErrorSplashScreen(SplashScreen_Driver_act.this, getString(R.string.app_label))) {
            Log.d(TAG, "no internet Connection");
        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(SplashScreen_Driver_act.this);
                    builder1.setMessage("This app cannot work without the following permissions:\nLocation\nStorage\nPhone\nContacts\nAccounts");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton("Grant permission",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    requestAllPermission();
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert11 = builder1.create();
                    if (!SplashScreen_Driver_act.this.isFinishing()) {
                        alert11.show();
                    }
                } else {
                    havePermissions();
                }
            } else {
                havePermissions();
            }
        }
    } // onCreate

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case INITIAL_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (PermissionUtil.verifyPermissions(grantResults)) {
                    havePermissions();
                } else {
                    customDialog("Please allow permission");
                }
            }
        }
    }

    private void havePermissions() {
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .coordinatorLayout);
        handler.postDelayed(gotoDasboard, 5000);
        Log.d(TAG, "maxMemory : " + Runtime.getRuntime().maxMemory());
        Log.d(TAG, "totalMemory : " + Runtime.getRuntime().totalMemory());
        CGlobals_lib_ss.getInstance().init(getApplicationContext());
        SSLog_SS.setContext(getApplicationContext(), CGlobals_lib_ss.msGmail);
        CGlobals_lib_ss.getInstance().getMyLocation(SplashScreen_Driver_act.this);
        try {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            CGlobals_lib_ss.mIMEI = telephonyManager.getDeviceId();
            CGlobals_lib_ss.mbRegistered = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        mRegistrationProgressBar = (CircularProgressView) findViewById(R.id.registrationProgressBar);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                msGcmRegId = intent.getStringExtra(Constants_lib_ss.PREF_TOKEN_SAVED);
                mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(Constants_lib_ss.SENT_TOKEN_TO_SERVER, false);
                CGlobals_lib_ss.mbRegistered = CGlobals_lib_ss.getInstance().getPersistentPreference(SplashScreen_Driver_act.this)
                        .getBoolean(Constants_CED.PREF_REGISTERED, false);
                String sPhone = CGlobals_lib_ss.getInstance().getPersistentPreference(SplashScreen_Driver_act.this).getString(
                        Constants_CED.PREF_PHONENO, "");
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(SplashScreen_Driver_act.this).putString(
                        Constants_CED.PREF_REG_ID, msGcmRegId);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(SplashScreen_Driver_act.this).commit();
                sendUserAccess(msGcmRegId);
            }
        };
        if (CGlobals_lib_ss.mbRegistered) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.getWindow().setGravity(Gravity.BOTTOM);
            mProgressDialog.setMessage("Loading. Please wait ...");
        }
        CGlobals_lib_ss.getInstance().init(SplashScreen_Driver_act.this);
        if (checkPlayServices()) {
            Intent intent = new Intent(this, RegistrationIntentService_CED.class);
            startService(intent);
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

    }

    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
        if (mProgressDialog != null)
            if (mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
            }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isLocationSent = false;
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Constants_CED.REGISTRATION_COMPLETE));
        CGlobals_lib_ss.getInstance().countError1 = 1;
        CGlobals_lib_ss.getInstance().countError2 = 1;
        CGlobals_lib_ss.getInstance().countError3 = 1;
        CGlobals_lib_ss.getInstance().countError4 = 1;
        CGlobals_lib_ss.getInstance().countError5 = 1;
        CGlobals_lib_ss.getInstance().countError6 = 1;
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
        System.gc();
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(SplashScreen_Driver_act.this);
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

    public void sendUserAccess(final String sGcmReg) {
        final String url = Constants_CED.USER_ACCESS_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        userAccessSucess(response);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                userAccessFailure(error);
                if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                    Log.e(TAG, "");
                } else {
                    SSLog_SS.e(TAG, "sendUserAccess :-   ", error, SplashScreen_Driver_act.this);
                }

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                if (!TextUtils.isEmpty(sGcmReg)) {
                    params.put("gcmregid", sGcmReg);
                }
                params = CGlobals_lib_ss.getInstance().getAllMobileParams(params,
                        url, Constants_CED.APP_CODE, SplashScreen_Driver_act.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                String debugUrl;
                try {
                    debugUrl = url + "?" + getParams.toString() + "&verbose=Y";
                    Log.e(TAG, debugUrl);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "getPassengers map - ", e, SplashScreen_Driver_act.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, true, SplashScreen_Driver_act.this);

    } // sendUserAccess

    private void userAccessSucess(String response) {
        String sAppUserId = "-1";
        int iAppUserId = -1;
        try {
            if (response.trim().equals("-1")) {
                snackbar = Snackbar.make(coordinatorLayout, getString(R.string.driver_phone_no_not_registered), Snackbar.LENGTH_LONG);
                snackbar.show();
                Intent intent1 = new Intent(SplashScreen_Driver_act.this,
                        CabENumberVerify_act.class);
                startActivity(intent1);
                finish();
                return;
            }
            if (!response.trim().equals("-1")) {
                Log.d(TAG, "Useraccess_url - " + response);
                JSONObject jResponse = new JSONObject(response);
                Log.d(TAG, response);
                try {
                    sAppUserId = jResponse.isNull("appuser_id") ? "-1" : jResponse
                            .getString("appuser_id");
                    sVerifiedflag = jResponse.isNull("verified") ? "-1" : jResponse
                            .getString("verified");
                    sManually_Verified = jResponse.isNull("manually_verified") ? "-1" : jResponse
                            .getString("manually_verified");
                    msPhoneNo = jResponse.isNull("phoneno") ? "-1" : jResponse
                            .getString("phoneno");
                    msCountryCode = jResponse.isNull("country_code") ? "-1" : jResponse
                            .getString("country_code");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(SplashScreen_Driver_act.this)
                        .putString(Constants_lib_ss.PREF_PHONENO, msPhoneNo);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(SplashScreen_Driver_act.this)
                        .putString(Constants_lib_ss.PREF_COUNTRY_CODE, msCountryCode);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(SplashScreen_Driver_act.this)
                        .putString(Constants_CED.PREF_VERIFIED_FLAG, sVerifiedflag);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(SplashScreen_Driver_act.this)
                        .putString(Constants_CED.PREF_MANUALLY_VERIFIED_FLAG, sManually_Verified);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(SplashScreen_Driver_act.this).commit();
                if (!sAppUserId.equals("-1")) {
                    iAppUserId = Integer.parseInt(sAppUserId);
                }
            }
            if (!sAppUserId.trim().equals("-1")) { // registration probably
                // failed so we need to do//
                // it again
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(SplashScreen_Driver_act.this).
                        putInt(Constants_lib_ss.PREF_APPUSERID, iAppUserId);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(SplashScreen_Driver_act.this).commit();
                CGlobals_lib_ss.getInstance().miAppUserId = iAppUserId;
                startRightActivity();
            } else {
                sVerifyed_flag = CGlobals_lib_ss.getInstance().getPersistentPreference(SplashScreen_Driver_act.this)
                        .getString(Constants_CED.PREF_VERIFIED_FLAG, "");
                sManuallyVerified = CGlobals_lib_ss.getInstance().getPersistentPreference(SplashScreen_Driver_act.this)
                        .getString(Constants_CED.PREF_MANUALLY_VERIFIED_FLAG, "");
                isVerifyDoneFlag = CGlobals_lib_ss.getInstance().getPersistentPreference(SplashScreen_Driver_act.this)
                        .getBoolean(Constants_CED.PREF_CABE_NUMBER_VERIFY_DONE, false);
                isLoginDoneFlag = CGlobals_lib_ss.getInstance().getPersistentPreference(SplashScreen_Driver_act.this)
                        .getBoolean(Constants_CED.PREF_CABE_GOOGLE_FACEBOOK_LOGIN_DONE, false);
                if (!sVerifyed_flag.equals("1") || !isVerifyDoneFlag) {
                    if (sManuallyVerified.equals("1")) {
                        if (!isLoginDoneFlag) {
                            Intent intent1 = new Intent(SplashScreen_Driver_act.this,
                                    CabERegistration_act.class);
                            startActivity(intent1);
                            finish();
                        } else {
                            Intent intent1 = new Intent(SplashScreen_Driver_act.this,
                                    DashBoard_Driver_act.class);
                            startActivity(intent1);
                            finish();
                        }
                    } else {
                        Intent intent1 = new Intent(SplashScreen_Driver_act.this,
                                CabENumberVerify_act.class);
                        startActivity(intent1);
                        finish();
                    }
                } else if (!isLoginDoneFlag) {
                    Intent intent1 = new Intent(SplashScreen_Driver_act.this,
                            CabERegistration_act.class);
                    startActivity(intent1);
                    finish();
                } else {
                    Intent intent1 = new Intent(SplashScreen_Driver_act.this,
                            DashBoard_Driver_act.class);
                    startActivity(intent1);
                    finish();
                }
            }

        } catch (Exception e) {
            SSLog_SS.e(TAG, "sendUserAccess Response: " + response, e, SplashScreen_Driver_act.this);
            snackbar = Snackbar
                    .make(coordinatorLayout, "Bad data received, try after some time", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Cancel", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            snackbar.dismiss();
                        }
                    });
            snackbar.show();
            customDialog("Bad data received, try after some time");
        }

    } // userAccessSuccess

    private void userAccessFailure(VolleyError error) {
        cancelProgress();
        startRightActivity();
        try {
            SSLog_SS.e(TAG, "userAccessFailure :-   ", error, SplashScreen_Driver_act.this);
            CGlobals_lib_ss.getInstance().getVolleyError(error);
        } catch (Exception e) {
            cancelProgress();
            SSLog_SS.e(TAG, "sendUserAccess - Response.ErrorListener", e, SplashScreen_Driver_act.this);
            finish();
        }
    }

    void startRightActivity() {
        Intent intent;
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(SplashScreen_Driver_act.this).
                getInt(Constants_lib_ss.PREF_APPUSERID, -1) == -1
                && CGlobals_lib_ss.getInstance().getPersistentPreference(SplashScreen_Driver_act.this).getBoolean(
                Constants_CED.PREF_REGISTERED, false)) {
            snackbar = Snackbar
                    .make(coordinatorLayout, "Cannot verify credentials.\n" +
                            "Please check your connection and try again", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Cancel", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            snackbar.dismiss();
                        }
                    });
            snackbar.show();
            return;
        }

        try {
            getCountry();
        } catch (Exception e) {
            SSLog_SS.e(TAG, "sendUserAccess - Response.ErrorListener", e, SplashScreen_Driver_act.this);
        }
        String sPhone = CGlobals_lib_ss.getInstance().getPersistentPreference(SplashScreen_Driver_act.this).getString(
                Constants_CED.PREF_PHONENO, "");
        sVerifyed_flag = CGlobals_lib_ss.getInstance().getPersistentPreference(SplashScreen_Driver_act.this)
                .getString(Constants_CED.PREF_VERIFIED_FLAG, "");
        sManuallyVerified = CGlobals_lib_ss.getInstance().getPersistentPreference(SplashScreen_Driver_act.this)
                .getString(Constants_CED.PREF_MANUALLY_VERIFIED_FLAG, "");
        isVerifyDoneFlag = CGlobals_lib_ss.getInstance().getPersistentPreference(SplashScreen_Driver_act.this)
                .getBoolean(Constants_CED.PREF_CABE_NUMBER_VERIFY_DONE, false);
        isLoginDoneFlag = CGlobals_lib_ss.getInstance().getPersistentPreference(SplashScreen_Driver_act.this)
                .getBoolean(Constants_CED.PREF_CABE_GOOGLE_FACEBOOK_LOGIN_DONE, false);
        if (!TextUtils.isEmpty(sPhone)) {
            if ((sVerifyed_flag.equals("1") || sManuallyVerified.equals("1")) && isLoginDoneFlag) {
                intent = new Intent(SplashScreen_Driver_act.this, DashBoard_Driver_act.class);
                startActivity(intent);
                finish();
            } else {
                if (!sVerifyed_flag.equals("1") || !isVerifyDoneFlag) {
                    if (sManuallyVerified.equals("1")) {
                        if (!isLoginDoneFlag) {
                            Intent intent1 = new Intent(SplashScreen_Driver_act.this,
                                    CabERegistration_act.class);
                            startActivity(intent1);
                            finish();
                        } else {
                            Intent intent1 = new Intent(SplashScreen_Driver_act.this,
                                    DashBoard_Driver_act.class);
                            startActivity(intent1);
                            finish();
                        }
                    } else {
                        Intent intent1 = new Intent(SplashScreen_Driver_act.this,
                                CabENumberVerify_act.class);
                        startActivity(intent1);
                        finish();
                    }
                } else if (!isLoginDoneFlag) {
                    Intent intent5 = new Intent(SplashScreen_Driver_act.this,
                            CabERegistration_act.class);
                    startActivity(intent5);
                    finish();
                }
            }
        } else {
            if (!sVerifyed_flag.equals("1") || !isVerifyDoneFlag) {
                if (sManuallyVerified.equals("1")) {
                    if (!isLoginDoneFlag) {
                        Intent intent1 = new Intent(SplashScreen_Driver_act.this,
                                CabERegistration_act.class);
                        startActivity(intent1);
                        finish();
                    } else {
                        Intent intent1 = new Intent(SplashScreen_Driver_act.this,
                                DashBoard_Driver_act.class);
                        startActivity(intent1);
                        finish();
                    }
                } else {
                    Intent intent1 = new Intent(SplashScreen_Driver_act.this,
                            CabENumberVerify_act.class);
                    startActivity(intent1);
                    finish();
                }
            } else if (!isLoginDoneFlag) {
                Intent intent5 = new Intent(SplashScreen_Driver_act.this,
                        CabERegistration_act.class);
                startActivity(intent5);
                finish();
            } else {
                intent = new Intent(SplashScreen_Driver_act.this, DashBoard_Driver_act.class);
                startActivity(intent);
                finish();
            }
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
        Location location = CGlobals_lib_ss.getInstance().getMyLocation(SplashScreen_Driver_act.this);
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
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG,
                                "getCountry Response.ErrorListener - ",
                                error, SplashScreen_Driver_act.this);
                    }
                } catch (Exception e) {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG,
                                " getCountry Response.ErrorListener (2) - ",
                                e, SplashScreen_Driver_act.this);
                    }
                }
            }
        });
        try {
            CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, SplashScreen_Driver_act.this);
        } catch (Exception e) {
            SSLog_SS.e(TAG, "getActiveuser CGlobals.getInstance().mVolleyReq..", e, SplashScreen_Driver_act.this);
        }
    } // getCountry

    String getCountryCode(String response) {
        String cc = null;
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(response);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(cc)) {
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(SplashScreen_Driver_act.this)
                    .putString(Constants_CED.PREF_CURRENT_COUNTRY, cc);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(SplashScreen_Driver_act.this).commit();
        }
        return cc;

    }

    protected Runnable gotoDasboard = new Runnable() {
        @Override
        public void run() {
        }
    };

    private void customDialog(String message) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(SplashScreen_Driver_act.this);
        builder1.setMessage(message);
        builder1.setCancelable(true);
        builder1.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        AlertDialog alert11 = builder1.create();
        if (!SplashScreen_Driver_act.this.isFinishing()) {
            alert11.show();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(100); // Update location every second
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            CGlobals_lib_ss.setMyLocation(mLastLocation, true, getApplicationContext());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        CGlobals_lib_ss.setMyLocation(mLastLocation, true, getApplicationContext());
        if (!isLocationSent) {
            isLocationSent = true;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        buildGoogleApiClient();
    }

    synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }


} // ActivitySpashScreen