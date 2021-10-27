package com.jumpinjumpout.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.jumpinjumpout.CGlobals_dp;
import com.jumpinjumpout.Constants_dp;
import com.jumpinjumpout.SSApp;
import com.jumpinjumpout.www.driverprofile.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import lib.app.util.SSLog_SS;


public class ActDriverProfileSplashScreen extends Activity {
    //    ProgressDialog mProgressDialog;
    CGlobals_dp cglobal;
    String msGcmRegId;
    SSApp mApp;
    private static String TAG = "SplashScreen";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PERMISSION_REQUEST_CODE = 1550;
    private static final String[] INITIAL_PERMS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);
//        int i= 5/0;

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission()) {
                startFunctioning();
            } else {
                requestPermission();
            }
        } else
            startFunctioning();
    }
    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(ActDriverProfileSplashScreen.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(ActDriverProfileSplashScreen.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(ActDriverProfileSplashScreen.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(ActDriverProfileSplashScreen.this,
                        Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(ActDriverProfileSplashScreen.this,
                        Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(ActDriverProfileSplashScreen.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(ActDriverProfileSplashScreen.this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                ) {
            return false;
        } else {
            return true;
        }
    }
    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_PHONE_STATE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_CONTACTS)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {

            AlertDialog.Builder builder1 = new AlertDialog.Builder(ActDriverProfileSplashScreen.this);
            builder1.setMessage(getString(R.string.permission_all));
            builder1.setCancelable(true);
            builder1.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            AlertDialog alert11 = builder1.create();
            if (!ActDriverProfileSplashScreen.this.isFinishing()) {
                alert11.show();
            }
        }
            ActivityCompat.requestPermissions(this, INITIAL_PERMS, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if ( verifyPermissions(grantResults)) {
                    startFunctioning();
                } else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(ActDriverProfileSplashScreen.this);
                    builder1.setMessage("Permission are required to function DriverProfile");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            });
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
                break;
        }
    }
    public static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) {
            return false;
        }
        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    private void startFunctioning() {
        mApp = (SSApp) ActDriverProfileSplashScreen.this.getApplication();
        msGcmRegId = getRegistrationId(this);
        cglobal = CGlobals_dp.getInstance(ActDriverProfileSplashScreen.this);
        cglobal.init(this);
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        CGlobals_dp.mIMEI = telephonyManager.getDeviceId();
        CGlobals_dp.mbRegistered = true;
        new AsyncLoad().execute();
    }

    private class AsyncLoad extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            // show your progress dialog
        }

        @Override
        protected String doInBackground(Void... voids) {
            // load your xml feed asynchronously
            mApp.init(ActDriverProfileSplashScreen.this);

            String msg = "";
            if (TextUtils.isEmpty(msGcmRegId)) {
                sendUserAccess(
                        msGcmRegId,
                        mApp.getPersistentPreference().getString(
                                Constants_dp.PREF_USER_TYPE, "U"));

            }
            return msg;
        }

        @Override
        protected void onPostExecute(String params) {
            // dismiss your dialog
            int iAppUserId = SSApp.getInstance().getPersistentPreference().getInt(Constants_dp.PREF_APPUSERID, -1);
            if (iAppUserId != -1) {
                Intent intent = new Intent(ActDriverProfileSplashScreen.this, ActDriverProfileRegistration.class);
                startActivity(intent);
                // close this activity
                finish();
            } else {
                AlertBox();
            }
        }
    }

    private void AlertBox() {
        final android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(ActDriverProfileSplashScreen.this);
        alertDialogBuilder.setMessage(R.string.noappuserid);
        alertDialogBuilder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    }
                });
        alertDialogBuilder.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void sendUserAccess(final String sGcmReg, final String sUserType) {
        String encodedurl = "";
        final String url = Constants_dp.USER_ACCESS_URL;

        try {
            encodedurl = URLEncoder.encode(url, "UTF-8");
            Log.d("TEST", encodedurl);
            StringRequest postRequest = new StringRequest(Request.Method.POST,
                    Constants_dp.USER_ACCESS_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "res -->" + response);
                            userAccessSucess(response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    userAccessFailure(error);

                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    if (!TextUtils.isEmpty(sGcmReg)) {
                        params.put("gcmregid", sGcmReg);
                    }
                    params.put("triptype", sUserType);
                    params = CGlobals_dp.getInstance(ActDriverProfileSplashScreen.this).getBasicMobileParams(params,
                            url, ActDriverProfileSplashScreen.this);
                    String delim = "";
                    StringBuilder getParams = new StringBuilder();
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        getParams.append(delim + entry.getKey() + "="
                                + entry.getValue());
                        delim = "&";
                    }
                    @SuppressWarnings("unused")
                    String debugUrl = "";
                    try {
                        debugUrl = url + "?" + getParams.toString() + "&verbose=Y";
                        Log.e(TAG, debugUrl);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return CGlobals_dp.getInstance(ActDriverProfileSplashScreen.this).checkParams(params);
                }
            };
            getMyApplication().addVolleyRequest(postRequest, true);
        } catch (Exception e) {
            e.printStackTrace();
            SSLog_SS.e(TAG, "sendUserAccess ", e, ActDriverProfileSplashScreen.this);
        }
    } // sendUserAccess

    private String getRegistrationId(Context context) {
        String registrationId = mApp.getPersistentPreference()
                .getString(Constants_dp.PREF_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        int registeredVersion = mApp.getPersistentPreference().getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        String registeredVersionName = mApp.getPersistentPreference()
                .getString(Constants_dp.PREF_PROPERTY_APP_VERSION_NAME, "");
        int currentVersionCode = mApp.mPackageInfo.versionCode;
        String currentVersionName = mApp.mPackageInfo.versionName;
        if (registeredVersion != currentVersionCode
                || !currentVersionName.equals(registeredVersionName)) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    SSApp getMyApplication() {
        if (mApp == null) {
            mApp = SSApp.getInstance();
        }
        return mApp;
    }

    private void userAccessSucess(String response) {
        String sAppUserId = "-1";
        int iAppUserId = -1;
        try {

            if (!response.trim().equals("-1")) {
                Log.d(TAG, "Useraccess_url - " + response);
                JSONObject jResponse = new JSONObject(response);
                Log.d(TAG, response);
                sAppUserId = jResponse.isNull("appuser_id") ? "-1" : jResponse
                        .getString("appuser_id");

                iAppUserId = Integer.parseInt(sAppUserId);
                SSApp.getInstance().getPersistentPreferenceEditor().putInt(Constants_dp.PREF_APPUSERID, iAppUserId);
                SSApp.getInstance().getPersistentPreferenceEditor().commit();
            }

        } catch (Exception e) {
            Log.d("Response", response);
            Toast.makeText(ActDriverProfileSplashScreen.this,
                    "Bad data received, try after some time", Toast.LENGTH_SHORT).show();
            finish();
        }
    } // userAccessSuccess
    private void userAccessFailure(VolleyError error) {
        startRightActivity();
        try {
            CGlobals_dp.getVolleyError(this, error);
        } catch (Exception e) {

            Toast.makeText(
                    ActDriverProfileSplashScreen.this.getBaseContext(),
                    "Cannot connect to the internet.\nPlease check your connection and try again",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    void startRightActivity() {
        Intent intent;
        if (mApp.getPersistentPreference().getInt(
                Constants_dp.PREF_APPUSERID, -1) == -1
                && mApp.getPersistentPreference().getBoolean(
                Constants_dp.PREF_REGISTERED, false)) {

            Toast.makeText(
                    ActDriverProfileSplashScreen.this.getBaseContext(),
                    "Cannot verify credentials.\nPlease check your connection and try again",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        try {
            getCountry();
        } catch (Exception e) {
        }
    }
    public void getCountry() {
        Location location = CGlobals_dp.getInstance(this).getMyLocation(this);
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
                SSLog_SS.e(TAG,"getCountry "+error);
            }
        });
        try {
            SSApp.getInstance().addVolleyRequest(postRequest, false);
        } catch (Exception e) {
            SSLog_SS.e(TAG,"getCountry "+e);
        }
    } // getCountry
    String getCountryCode(String response) {
        String cc = null;
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(response);
            JSONArray addrComp = ((JSONArray) jsonObject.get("contactResults"))
                    .getJSONObject(0).getJSONArray("address_components");
            JSONObject joAddrComp;
            String sType = null;
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
            SSApp.getInstance().getPersistentPreferenceEditor()
                    .putString(Constants_dp.PREF_CURRENT_COUNTRY, cc);
            SSApp.getInstance().getPersistentPreferenceEditor().commit();
        }
        return cc;

    }

}