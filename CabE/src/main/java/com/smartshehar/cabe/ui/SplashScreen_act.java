package com.smartshehar.cabe.ui;

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
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
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
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.smartshehar.cabe.Constants_CabE;
import com.smartshehar.cabe.MyApplication_CabE;
import com.smartshehar.cabe.PermissionUtil;
import com.smartshehar.cabe.R;
import com.smartshehar.cabe.RegistrationIntentService_CabE;

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

public class SplashScreen_act extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static String TAG = "ActSplashScreen: ";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    String msGcmRegId;
    MyApplication_CabE myApplication;
    ProgressDialog mProgressDialog;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private CircularProgressView mRegistrationProgressBar;
    private static final int INITIAL_REQUEST = 13;
    private boolean isVerifyDoneFlag = false;
    private boolean isLoginDoneFlag = false;
    //String sApporvedflag = "-1";
    Connectivity mConnectivity;
    private static final String[] INITIAL_PERMS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_PHONE_STATE,
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
                Manifest.permission.READ_PHONE_STATE)) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(SplashScreen_act.this);
            builder1.setMessage("This app cannot work without the following permissions:\nLocation\nStorage\nPhone");
            builder1.setCancelable(true);
            builder1.setPositiveButton("Grant permission",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
            AlertDialog alert11 = builder1.create();
            if (!SplashScreen_act.this.isFinishing()) {
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
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splashscreen_act);
        mConnectivity = new Connectivity();
        if (mConnectivity.connectionErrorSplashScreen(SplashScreen_act.this, getString(R.string.app_label))) {
            Log.d(TAG, "no internet Connection");
        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(SplashScreen_act.this);
                    builder1.setMessage("This app cannot work without the following permissions:\nLocation\nStorage\nPhone");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton("Grant permission",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    requestAllPermission();
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert11 = builder1.create();
                    if (!SplashScreen_act.this.isFinishing()) {
                        alert11.show();
                    }
                } else {
                    gotPermissions();
                }
            } else {
                gotPermissions();
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
                    gotPermissions();
                } else {
                    Toast.makeText(SplashScreen_act.this, "Please allow permission", Toast.LENGTH_LONG).show();
                    customDialog("Please allow permission");
                }
            }
        }
    }

    private void gotPermissions() {
        Log.d(TAG, "maxMemory : " + Runtime.getRuntime().maxMemory());
        Log.d(TAG, "totalMemory : " + Runtime.getRuntime().totalMemory());
        myApplication = MyApplication_CabE.getInstance();
        CGlobals_lib_ss.getInstance().init(getApplicationContext());
        SSLog_SS.setContext(getApplicationContext(), CGlobals_lib_ss.msGmail);
        CGlobals_lib_ss.getInstance().getMyLocation(SplashScreen_act.this);
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
                CGlobals_lib_ss.mbRegistered = MyApplication_CabE.getInstance().getPersistentPreference()
                        .getBoolean(Constants_CabE.PREF_REGISTERED, false);
                String sPhone = myApplication.getPersistentPreference().getString(
                        Constants_CabE.PREF_PHONENO, "");
                myApplication.getPersistentPreferenceEditor().putString(
                        Constants_CabE.PREF_REG_ID, msGcmRegId);
                myApplication.getPersistentPreferenceEditor().commit();
                if (sentToken) {
                    if (!CGlobals_lib_ss.mbRegistered || TextUtils.isEmpty(sPhone)) {
                        sendUserAccess(msGcmRegId);
                    } else {
                        sendUserAccess(
                                msGcmRegId);
                    }
                } else {
                    isVerifyDoneFlag = CGlobals_lib_ss.getInstance().getPersistentPreference(SplashScreen_act.this)
                            .getBoolean(Constants_CabE.PREF_CABE_NUMBER_VERIFY_DONE, false);
                    isLoginDoneFlag = CGlobals_lib_ss.getInstance().getPersistentPreference(SplashScreen_act.this)
                            .getBoolean(Constants_CabE.PREF_CABE_GOOGLE_FACEBOOK_LOGIN_DONE, false);
                    if (TextUtils.isEmpty(sPhone)) {
                        if (!isVerifyDoneFlag) {
                            Intent intent1 = new Intent(SplashScreen_act.this,
                                    CabENumberVerify_act.class);
                            startActivityForResult(intent1, 9);
                            finish();
                        } else if (!isLoginDoneFlag) {
                            Intent intent1 = new Intent(SplashScreen_act.this,
                                    CabERegistration_act.class);
                            startActivityForResult(intent1, 9);
                            finish();
                        }
                    } else {
                        if (isVerifyDoneFlag && isLoginDoneFlag) {
                            intent = new Intent(SplashScreen_act.this, CabEMain_act.class);
                            startActivity(intent);
                            finish();
                        } else {
                            if (!isVerifyDoneFlag) {
                                Intent intent1 = new Intent(SplashScreen_act.this,
                                        CabENumberVerify_act.class);
                                startActivityForResult(intent1, 9);
                                finish();
                            } else {
                                Intent intent1 = new Intent(SplashScreen_act.this,
                                        CabERegistration_act.class);
                                startActivityForResult(intent1, 9);
                                finish();
                            }
                        }
                    }
                }
            }
        };

        if (CGlobals_lib_ss.mbRegistered) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.getWindow().setGravity(Gravity.BOTTOM);
            mProgressDialog.setMessage("Loading. Please wait ...");
        }
        CGlobals_lib_ss.getInstance().init(SplashScreen_act.this);
        if (checkPlayServices()) {
            Intent intent = new Intent(this, RegistrationIntentService_CabE.class);
            startService(intent);
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

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
                new IntentFilter(Constants_CabE.REGISTRATION_COMPLETE));
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

    public void sendUserAccess(final String sGcmReg) {
        final String url = Constants_CabE.USER_ACCESS_URL;
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
                    SSLog_SS.e(TAG, "sendUserAccess :-   ", error, SplashScreen_act.this);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                if (!TextUtils.isEmpty(sGcmReg)) {
                    params.put("gcmregid", sGcmReg);
                }
                params.put("usertype", Constants_CabE.USER_TYPE);
                params = CGlobals_lib_ss.getInstance().getAllMobileParams(params,
                        url, Constants_CabE.APP_CODE, SplashScreen_act.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();

                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";

                }
                String debugUrl;
                try {
                    debugUrl = url + "?" + getParams.toString();
                    Log.e(TAG, debugUrl);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "getPassengers map - ", e, SplashScreen_act.this);
                }

                return CGlobals_lib_ss.getInstance().checkParams(params);
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
                isVerifyDoneFlag = CGlobals_lib_ss.getInstance().getPersistentPreference(SplashScreen_act.this)
                        .getBoolean(Constants_CabE.PREF_CABE_NUMBER_VERIFY_DONE, false);
                isLoginDoneFlag = CGlobals_lib_ss.getInstance().getPersistentPreference(SplashScreen_act.this)
                        .getBoolean(Constants_CabE.PREF_CABE_GOOGLE_FACEBOOK_LOGIN_DONE, false);
                if (!isVerifyDoneFlag) {
                    Intent intent1 = new Intent(SplashScreen_act.this,
                            CabENumberVerify_act.class);
                    startActivityForResult(intent1, 9);
                    finish();
                } else if (!isLoginDoneFlag) {
                    Intent intent1 = new Intent(SplashScreen_act.this,
                            CabERegistration_act.class);
                    startActivityForResult(intent1, 9);
                    finish();
                }
                return;
            }
            if (!response.trim().equals("-1")) {
                Log.d(TAG, "Useraccess_url - " + response);
                JSONObject jResponse = new JSONObject(response);
                Log.d(TAG, response);
                try {
                    sAppUserId = jResponse.isNull("appuser_id") ? "-1" : jResponse
                            .getString("appuser_id");
                    /*sApporvedflag = jResponse.isNull("verified") ? "-1" : jResponse
                            .getString("verified");*/
                } catch (JSONException e) {
                    e.printStackTrace();
                }
/*
                try {
                    sUnAppUserId = jResponse.isNull("un_appuser_id") ? "-1" : jResponse
                            .getString("un_appuser_id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
*/
                if (!sAppUserId.equals("-1")) {
                    iAppUserId = Integer.parseInt(sAppUserId);
                }
            }
            if (!sAppUserId.trim().equals("-1")) { // registration probably
                // failed so we need to do//
                // it again
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(SplashScreen_act.this).
                        putInt(Constants_lib_ss.PREF_APPUSERID, iAppUserId);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(SplashScreen_act.this).commit();
                CGlobals_lib_ss.getInstance().miAppUserId = iAppUserId;
                startRightActivity();
                SplashScreen_act.this.finish();
            } else {
                isVerifyDoneFlag = CGlobals_lib_ss.getInstance().getPersistentPreference(SplashScreen_act.this)
                        .getBoolean(Constants_CabE.PREF_CABE_NUMBER_VERIFY_DONE, false);
                isLoginDoneFlag = CGlobals_lib_ss.getInstance().getPersistentPreference(SplashScreen_act.this)
                        .getBoolean(Constants_CabE.PREF_CABE_GOOGLE_FACEBOOK_LOGIN_DONE, false);
                if (!isVerifyDoneFlag) {
                    /*if (sApporvedflag.equals("1")) {
                        Intent intent1 = new Intent(SplashScreen_act.this,
                                CabEMain_act.class);
                        startActivityForResult(intent1, 9);
                        finish();
                    } else {*/
                    Intent intent1 = new Intent(SplashScreen_act.this,
                            CabENumberVerify_act.class);
                    startActivityForResult(intent1, 9);
                    finish();
                    // }
                } else if (!isLoginDoneFlag) {
                    Intent intent1 = new Intent(SplashScreen_act.this,
                            CabERegistration_act.class);
                    startActivityForResult(intent1, 9);
                    finish();
                }
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG, "sendUserAccess Response: " + response, e, SplashScreen_act.this);
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
            SSLog_SS.e(TAG, "userAccessFailure :-   ", error, SplashScreen_act.this);
            CGlobals_lib_ss.getInstance().getVolleyError(error);
        } catch (Exception e) {
            cancelProgress();
            SSLog_SS.e(TAG, "sendUserAccess - Response.ErrorListener", e, SplashScreen_act.this);
            finish();
        }
    }

    MyApplication_CabE getMyApplication() {
        if (myApplication == null) {
            myApplication = MyApplication_CabE.getInstance();
        }
        return myApplication;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        CGlobals_lib_ss.mbRegistered = myApplication.getPersistentPreference()
                .getBoolean(Constants_CabE.PREF_REGISTERED, false);
        String sPhone = myApplication.getPersistentPreference().getString(
                Constants_CabE.PREF_PHONENO, "");
        if (!CGlobals_lib_ss.mbRegistered || TextUtils.isEmpty(sPhone)) {
            isVerifyDoneFlag = CGlobals_lib_ss.getInstance().getPersistentPreference(SplashScreen_act.this)
                    .getBoolean(Constants_CabE.PREF_CABE_NUMBER_VERIFY_DONE, false);
            isLoginDoneFlag = CGlobals_lib_ss.getInstance().getPersistentPreference(SplashScreen_act.this)
                    .getBoolean(Constants_CabE.PREF_CABE_GOOGLE_FACEBOOK_LOGIN_DONE, false);
            if (!isVerifyDoneFlag) {
                /*if (sApporvedflag.equals("1")) {
                    Intent intent1 = new Intent(SplashScreen_act.this,
                            CabEMain_act.class);
                    startActivityForResult(intent1, 9);
                    finish();
                } else {*/
                Intent intent1 = new Intent(SplashScreen_act.this,
                        CabENumberVerify_act.class);
                startActivityForResult(intent1, 9);
                finish();
                //  }
            } else if (!isLoginDoneFlag) {
                Intent intent1 = new Intent(SplashScreen_act.this,
                        CabERegistration_act.class);
                startActivityForResult(intent1, 9);
                finish();
            }
        } else {
            sendUserAccess(msGcmRegId);
        }
    }

    void startRightActivity() {
        Intent intent;
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(SplashScreen_act.this).
                getInt(Constants_lib_ss.PREF_APPUSERID, -1) == -1
                && myApplication.getPersistentPreference().getBoolean(
                Constants_CabE.PREF_REGISTERED, false)) {

            Toast.makeText(
                    SplashScreen_act.this.getBaseContext(),
                    "Cannot verify credentials.\nPlease check your connection and try again",
                    Toast.LENGTH_LONG).show();
            customDialog("Cannot verify credentials.\nPlease check your connection and try again");


            return;
        }

        try {
            getCountry();
        } catch (Exception e) {
            SSLog_SS.e(TAG, "sendUserAccess - Response.ErrorListener", e, SplashScreen_act.this);
        }
        String sPhone = myApplication.getPersistentPreference().getString(
                Constants_CabE.PREF_PHONENO, "");
        isVerifyDoneFlag = CGlobals_lib_ss.getInstance().getPersistentPreference(SplashScreen_act.this)
                .getBoolean(Constants_CabE.PREF_CABE_NUMBER_VERIFY_DONE, false);
        isLoginDoneFlag = CGlobals_lib_ss.getInstance().getPersistentPreference(SplashScreen_act.this)
                .getBoolean(Constants_CabE.PREF_CABE_GOOGLE_FACEBOOK_LOGIN_DONE, false);
        if (!TextUtils.isEmpty(sPhone)) {
            if (isVerifyDoneFlag && isLoginDoneFlag) {
                intent = new Intent(SplashScreen_act.this, CabEMain_act.class);
                startActivity(intent);
                cancelProgress();
                finish();
            } else {
                if (!isVerifyDoneFlag) {
                    /*if (sApporvedflag.equals("1")) {
                        Intent intent1 = new Intent(SplashScreen_act.this,
                                CabEMain_act.class);
                        startActivityForResult(intent1, 9);
                        finish();
                    } else {*/
                    Intent intent1 = new Intent(SplashScreen_act.this,
                            CabENumberVerify_act.class);
                    startActivityForResult(intent1, 9);
                    finish();
                    //}
                } else {
                    Intent intent5 = new Intent(SplashScreen_act.this,
                            CabERegistration_act.class);
                    startActivityForResult(intent5, 9);
                }
            }
        } else {
            if (!isVerifyDoneFlag) {
                /*if (sApporvedflag.equals("1")) {
                    Intent intent1 = new Intent(SplashScreen_act.this,
                            CabEMain_act.class);
                    startActivityForResult(intent1, 9);
                    finish();
                } else {*/
                Intent intent1 = new Intent(SplashScreen_act.this,
                        CabENumberVerify_act.class);
                startActivityForResult(intent1, 9);
                finish();
                // }
            } else if (!isLoginDoneFlag) {
                Intent intent5 = new Intent(SplashScreen_act.this,
                        CabERegistration_act.class);
                startActivityForResult(intent5, 9);
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
        Location location = CGlobals_lib_ss.getInstance().getMyLocation(SplashScreen_act.this);
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
                                error, SplashScreen_act.this);
                    }
                } catch (Exception e) {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG,
                                " getCountry Response.ErrorListener (2) - ",
                                e, SplashScreen_act.this);
                    }
                }
            }
        });
        try {
            MyApplication_CabE.getInstance().addVolleyRequest(postRequest, false);
        } catch (Exception e) {
            SSLog_SS.e(TAG, "getActiveuser CGlobals.getInstance().mVolleyReq..", e, SplashScreen_act.this);
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
            MyApplication_CabE.getInstance().getPersistentPreferenceEditor()
                    .putString(Constants_CabE.PREF_CURRENT_COUNTRY, cc);
            MyApplication_CabE.getInstance().getPersistentPreferenceEditor().commit();
        }
        return cc;

    }

    private void customDialog(String message) {
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