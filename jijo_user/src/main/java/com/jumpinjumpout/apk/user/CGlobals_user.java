package com.jumpinjumpout.apk.user;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.user.ui.ShareTripTrackDriver_act;
import com.jumpinjumpout.apk.user.ui.TrackDriver_act;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import lib.app.util.CAddress;
import lib.app.util.UserInfo;

@SuppressWarnings("deprecation")
public class CGlobals_user {
    public static final String TAG = "CGlobals: ";
    private static CGlobals_user instance;
    private int nJoined = 0, nDroppedOut = 0;
    public boolean isCabCancelDriverNiotification = false;

    // Restrict the constructor from being instantiated
    private CGlobals_user() {
    }

    public static synchronized CGlobals_user getInstance() {
        if (instance == null) {
            instance = new CGlobals_user();
        }
        return instance;
    }

    public SSLog sslog;
    public static boolean mbIsAdmin;
    static String msCity;
    public static final String DEFAULT_CITY = "MUMBAI";
    public static boolean mbRegistered;
    private Location mCurrentLocation;
    public boolean bStopsRead;
    public Stack<Integer> stackActivity;
    protected double mdTravelDistance;
    public static String msTripAction = "";
    public static boolean mbAppInited;
    public PackageInfo mPackageInfo = null;
    public String msProduct;
    public String msManufacturer;
    public String msCarrier;
    public String mAppNameCode;
    String msAndroidReleaseVersion; // e.g. myVersion := "1.6"
    int miAndroidSdkVersion; // e.g. sdkVersion := 8;
    public static String mIMEI;
    public static String msGmail = null;
    public static String msPhoneNo = null;
    public static String msCountryCode = null;
    public static String mLine1Number;
    public static Location mFakeLocation;
    public static String mAppNameShort;
    String mCurrentVersion;
    public static String msAppVersionName;
    public static int miAppVersionCode;
    public UserInfo mUserInfo;
    public ArrayList<String> masRecentAddresses = new ArrayList<String>();
    private CAddress moFromAddr = new CAddress(""),
            moToAddr = new CAddress("");
    private static String msUserType = "";
    Map<String, String> mMobileParams;
    public int miAppUserId;
    public static boolean inWalkWithMe = false;
    public static String walkWithMeMode = "";
    public static final String PREFTRAVELDIST = "traveldistance";
    public static String PREFINWALKWITHME = "inwalkwithme";
    public static String PREFINWALKWITHME_EMAILS = "";
    public static int miTravelDistance;

    public String getEmailId() {
        return msGmail;
    }

    public int getAppUserId() {
        return MyApplication.getInstance()
                .getPersistentPreference()
                .getInt(Constants_user.PREF_APPUSERID, -1);
    }

    public boolean mExitApp = false;
    private double DRIVER_COST_PER_KM = 15;
    private double PASSENGER_COST_PER_KM = 8;

    public void init(Context act) {
        msPhoneNo = MyApplication.getInstance().getPersistentPreference()
                .getString(Constants_user.PREF_PHONENO, "");
        msCountryCode = MyApplication.getInstance().getPersistentPreference()
                .getString(Constants_user.PREF_COUNTRY_CODE, "");
        mbAppInited = false;
        bStopsRead = false;
        mdTravelDistance = 0;
        miTravelDistance = 0;
        readPreferences();
        if (mbAppInited)
            return;
        mAppNameShort = act.getString(R.string.appNameShort);
        mFakeLocation = new Location("fake"); // Dadar - Tilak Bridge
        double lat = 19.02078;
        double lon = 72.843168;
        mFakeLocation.setLatitude(lat);
        mFakeLocation.setLongitude(lon);
        mFakeLocation.setAccuracy(9999);
        TelephonyManager telephonyManager = (TelephonyManager) act
                .getSystemService(Context.TELEPHONY_SERVICE);
        mAppNameCode = act.getString(R.string.appNameShort);
        msAndroidReleaseVersion = Build.VERSION.RELEASE; // e.g.
        // myVersion
        // := "1.6"
        miAndroidSdkVersion = Build.VERSION.SDK_INT; // e.g.
        // sdkVersion :=
        // 8;

        try {
            mIMEI = telephonyManager.getDeviceId();
            msCarrier = telephonyManager.getNetworkOperator();
            mLine1Number = telephonyManager.getLine1Number();
            mLine1Number = telephonyManager.getSubscriberId();
        } catch (Exception e) {
            SSLog.e(TAG, "init: ", e);
        }
        msProduct = Build.PRODUCT;
        msManufacturer = Build.MANUFACTURER;
        mbAppInited = true;
        mPackageInfo = getPackageInfo(act);
        stackActivity = new Stack<Integer>();
        AccountManager manager = (AccountManager) act
                .getSystemService(Context.ACCOUNT_SERVICE);
        Account[] list = manager.getAccounts();
        msGmail = null;

        for (Account account : list) {
            if (account.type.equalsIgnoreCase("com.google")) {
                msGmail = account.name;
                break;
            }
        }
        mbIsAdmin = isAdmin(msGmail);
        msAppVersionName = mPackageInfo.versionName;
        miAppVersionCode = mPackageInfo.versionCode;
        String currentVersion = MyApplication.getInstance()
                .getPersistentPreference().getString("dbVersionInfo", "-1");
        if (!msAppVersionName.equals(currentVersion)) {

            MyApplication.getInstance().getPersistentPreferenceEditor()
                    .putString("dbVersionInfo", msAppVersionName);
            MyApplication.getInstance().getPersistentPreferenceEditor().commit();

            MyApplication.getInstance().getPersistentPreferenceEditor()
                    .putString("dbVersionInfo", currentVersion);

            MyApplication.getInstance().getPersistentPreferenceEditor()
                    .commit();

        }

        mCurrentVersion = MyApplication.getInstance().getPersistentPreference()
                .getString("dbVersionInfo", "-1");
        mUserInfo = new UserInfo(act,
                act.getString(R.string.app_label));
        // Is this the first use after download? then send to server as first
        // use
        if (!currentVersion.equals(mPackageInfo.versionCode)) { // First Use
        }

        readPreferences();
        mMobileParams = new HashMap<String, String>();
    }

    private PackageInfo getPackageInfo(Context context) {
        PackageInfo pi = null;
        try {
            pi = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pi;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    int CHECKTIMES = 2;

    // Read App global preferences
    public void readPreferences() {
    }

    public boolean isBetterLocation(Location location,
                                    Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > Constants_user.TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -Constants_user.TWO_MINUTES;
        boolean isNewer = timeDelta > 0;
        // If it's been more than two minutes since the current location, use
        // the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be
            // worse
        } else if (isSignificantlyOlder) {
            return false;
        }
        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
                .getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and
        // accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate
                && isFromSameProvider) {
            return true;
        }
        return false;
    }

    public boolean isAdmin(String emailid) {
        String string = "ctemkar@gmail.com;priteshpanchigar@gmail.com;sultanakhanam2010@gmail.com";
        String[] splits = string.split(";");
        for (String email : splits) {
            if (email.equals(emailid)) {
                return true;
            }

        }
        return false;
    }

    /**
     * try to get the 'best' location selected from all providers
     */
    public Location getBestLocation(Context context) {
        Location gpslocation = getLocationByProvider(context, LocationManager.GPS_PROVIDER);
        Location networkLocation = getLocationByProvider(context, LocationManager.NETWORK_PROVIDER);
        // if we have only one location available, the choice is easy
        if (gpslocation == null) {
            Log.d(TAG, "No GPS Location available.");
            return networkLocation;
        }
        if (networkLocation == null) {
            Log.d(TAG, "No Network Location available");
            return gpslocation;
        }
        // a locationupdate is considered 'old' if its older than the configured
        // update interval. this means, we didn't get a
        // update from this provider since the last check
        long old = System.currentTimeMillis() - getGPSCheckMilliSecsFromPrefs();
        boolean gpsIsOld = (gpslocation.getTime() < old);
        boolean networkIsOld = (networkLocation.getTime() < old);
        // gps is current and available, gps is better than network
        if (!gpsIsOld) {
            Log.d(TAG, "Returning current GPS Location");
            return gpslocation;
        }
        // gps is old, we can't trust it. use network location
        if (!networkIsOld) {
            Log.d(TAG, "GPS is old, Network is current, returning network");
            return networkLocation;
        }
        // both are old return the newer of those two
        if (gpslocation.getTime() > networkLocation.getTime()) {
            Log.d(TAG, "Both are old, returning gps(newer)");
            return gpslocation;
        } else {
            Log.d(TAG, "Both are old, returning network(newer)");
            return networkLocation;
        }
    }

    /**
     * get the last known location from a specific provider (network/gps)
     */
    private Location getLocationByProvider(Context context, String provider) {
        Location location = null;
        if (!isProviderSupported(provider)) {
            return null;
        }
        if (context != null) {
            LocationManager locationManager = (LocationManager) context
                    .getApplicationContext().getSystemService(
                            Context.LOCATION_SERVICE);
            try {
                if (locationManager.isProviderEnabled(provider)) {
                    if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return null;
                    }
                    location = locationManager.getLastKnownLocation(provider);
                }
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "Cannot acces Provider " + provider);
            }
        }
        return location;
    }

    int getGPSCheckMilliSecsFromPrefs() {
        return 1000;
    }

    boolean isProviderSupported(String provider) {
        return true;
    }

    public void showGPSDisabledAlertToUser(final Activity activity) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                activity);
        alertDialogBuilder
                .setMessage(R.string.gps_enable)
                .setCancelable(false)
                .setPositiveButton(R.string.no_gps_message,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);

                                activity.startActivityForResult(
                                        callGPSSettingIntent,
                                        Constants_user.REQUEST_CODE);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public Map<String, String> getAllMobileParams(Map<String, String> params,
                                                  String url, Context context) {
        init(MyApplication.getInstance().getApplicationContext());
        Location myLocation = getMyLocation(context);
        SimpleDateFormat df = new SimpleDateFormat(
                Constants_user.STANDARD_DATE_FORMAT, Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        msPhoneNo = MyApplication.getInstance().getPersistentPreference()
                .getString(Constants_user.PREF_PHONENO, "");
        msCountryCode = MyApplication.getInstance().getPersistentPreference()
                .getString(Constants_user.PREF_COUNTRY_CODE, "");
        params.put(
                "appuserid",
                Integer.toString(MyApplication.getInstance()
                        .getPersistentPreference()
                        .getInt(Constants_user.PREF_APPUSERID, -1)));
        params.put("countrycode", msCountryCode.trim());
        params.put("phoneno", msPhoneNo.trim());
        params.put("clientdatetime", df.format(cal.getTime()));
        if (myLocation != null) {
            Date locationDateTime = new Date(myLocation.getTime());
            params.put("provider", myLocation.getProvider());
            params.put("lat", String.format("%.9f", myLocation.getLatitude()));
            params.put("lng", String.format("%.9f", myLocation.getLongitude()));
            params.put("accuracy", Double.toString(myLocation.getAccuracy()));
            params.put("altitude", Double.toString(myLocation.getAltitude()));
            params.put("bearing", Double.toString(myLocation.getBearing()));
            params.put("speed", Double.toString(myLocation.getSpeed()));
            params.put("locationdatetime", df.format(locationDateTime));
            params.put("provider", myLocation.getProvider());
        }
        params.put("app", mAppNameCode);
        params.put("module", "SAR");
        params.put("version", CGlobals_user.msAppVersionName);
        params.put("android_release_version", msAndroidReleaseVersion);
        params.put("versioncode", String.valueOf(miAppVersionCode));
        params.put("android_sdk_version", Integer.toString(miAndroidSdkVersion));
        params.put("imei", CGlobals_user.mIMEI);
        params.put("email", CGlobals_user.msGmail);
        params.put("carrier", msCarrier);
        params.put("product", msProduct);
        params.put("manufacturer", msManufacturer);

        String delim = "";
        StringBuilder getParams = new StringBuilder();
        for (Entry<String, String> entry : params.entrySet()) {
            getParams.append(delim + entry.getKey() + "=" + entry.getValue());
            delim = "&";

        }
        try {
            url = url + "?" + getParams.toString();
        } catch (Exception e) {
            SSLog.e(TAG, "getAllMobileParams - ", e);
        }
        return checkParams(params);
    } // getAllMobileParams

    public Map<String, String> getBasicMobileParams(Map<String, String> params,
                                                    String url, Context context) {
        Location myLocation = getMyLocation(context);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        msPhoneNo = MyApplication.getInstance().getPersistentPreference()
                .getString(Constants_user.PREF_PHONENO, "");
        msCountryCode = MyApplication.getInstance().getPersistentPreference()
                .getString(Constants_user.PREF_COUNTRY_CODE, "");
        String sProvider = myLocation.getProvider();
        if (sProvider.equals("fused")) {
            sProvider = "F";
        } else if (sProvider.equals("network")) {
            sProvider = "N";
        } else if (sProvider.equals("gps")) {
            sProvider = "G";
        }
        params.put(
                "appuserid",
                Integer.toString(MyApplication.getInstance()
                        .getPersistentPreference()
                        .getInt(Constants_user.PREF_APPUSERID, -1)));
        params.put("email", CGlobals_user.msGmail);
        params.put("clientdatetime", df.format(cal.getTime()));
        if (myLocation != null) {
            Date locationDateTime = new Date(myLocation.getTime());
            params.put("provider", sProvider);
            params.put("lat", String.format("%.9f", myLocation.getLatitude()));
            params.put("lng", String.format("%.9f", myLocation.getLongitude()));
            params.put("accuracy", Double.toString(myLocation.getAccuracy()));
            params.put("altitude", Double.toString(myLocation.getAltitude()));
            params.put("bearing", Double.toString(myLocation.getBearing()));
            params.put("speed", Double.toString(myLocation.getSpeed()));
            params.put("locationdatetime", df.format(locationDateTime));

        }

        String delim = "";
        StringBuilder getParams = new StringBuilder();
        for (Entry<String, String> entry : params.entrySet()) {
            getParams.append(delim + entry.getKey() + "=" + entry.getValue());
            delim = "&";

        }
        try {
            url = url + "?" + getParams.toString();
            System.out.println("url  " + url);
        } catch (Exception e) {
            Log.e(TAG, e.getStackTrace().toString());
        }
        return checkParams(params);
    }

    public Map<String, String> getMinMobileParams(Map<String, String> params,
                                                  String url) {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        params.put(
                "appuserid",
                Integer.toString(MyApplication.getInstance()
                        .getPersistentPreference()
                        .getInt(Constants_user.PREF_APPUSERID, -1)));
        params.put("email", CGlobals_user.msGmail);
        params.put("clientdatetime", df.format(cal.getTime()));
        String delim = "";
        StringBuilder getParams = new StringBuilder();
        for (Entry<String, String> entry : params.entrySet()) {
            getParams.append(delim + entry.getKey() + "=" + entry.getValue());
            delim = "&";

        }
        try {
            url = url + "?" + getParams.toString();
        } catch (Exception e) {
            Log.e(TAG, e.getStackTrace().toString());
        }
        return checkParams(params);
    }

    public void setMyLocation(Location location, boolean bForceLocation) {
        if (location == null)
            return;
        if (bForceLocation) {
            mCurrentLocation = CGlobals_lib.getInstance().isBetterLocation(location, mCurrentLocation) ? location
                    : mCurrentLocation;
        } else {
            if (isBetterLocation(location, mCurrentLocation)) {
                mCurrentLocation = CGlobals_lib.getInstance().isBetterLocation(location, mCurrentLocation) ? location
                        : mCurrentLocation;
            }
        }
        final String jsonLocation = locationToJsonString(mCurrentLocation);
        new Thread(new Runnable() {
            public void run() {
                MyApplication
                        .getInstance()
                        .getPersistentPreferenceEditor()
                        .putString(Constants_user.PREF_MY_LOCATION,
                                jsonLocation);
                MyApplication.getInstance().getPersistentPreferenceEditor()
                        .commit();
            }
        }).start();
    }

    public Location getMyLocation(Context context) {
        Location location = getBestLocation(context);
        mCurrentLocation = CGlobals_lib.getInstance().isBetterLocation(location, mCurrentLocation) ? location
                : mCurrentLocation;
        if (mCurrentLocation == null) {
            String jsonLocation = MyApplication.getInstance()
                    .getPersistentPreference()
                    .getString(Constants_user.PREF_MY_LOCATION, "");
            if (!TextUtils.isEmpty(jsonLocation)) {
                try {
                    mCurrentLocation = locationFromJsonString(jsonLocation);
                } catch (Exception e) {
                    SSLog.e(TAG,
                            "getMyLocation: Could not convert jsonLocation to location object",
                            e);
                }
            }
        }

        if (mCurrentLocation != null) {
            setMyLocation(mCurrentLocation, false);
        }
        return mCurrentLocation;
    }


    public void sendUpdatePosition(Location location, final Context context) {
        CGlobals_lib.getInstance().sendUpdatePosition(Constants_user.UPDATE_POSITION_URL, location, context);
    }

    public String getVolleyError(Context context, VolleyError error) {
        // int statusCode = error.networkResponse.statusCode;
        // NetworkResponse response = error.networkResponse;

        // Log.d("testerror",""+statusCode+" "+response.data);
        // Handle your error types accordingly.For Timeout & No connection
        // error, you can show 'retry' button.
        // For AuthFailure, you can re login with user credentials.
        // For ClientError, 400 & 401, Errors happening on client side when
        // sending api request.
        // In this case you can check how client is forming the api and debug
        // accordingly.
        // For ServerError 5xx, you can do retry or handle accordingly.
        if (error instanceof NetworkError) {
            return context.getString(R.string.retry_internet);
        } else if (error instanceof ServerError) {
        } else if (error instanceof AuthFailureError) {
        } else if (error instanceof ParseError) {
        } else if (error instanceof NoConnectionError) {
        } else if (error instanceof TimeoutError) {
        } else {
            return error.getMessage();
        }
        return "";
    }

    public void setUserType(String sUserType) {
        msUserType = sUserType;
    }

    public String getUserType() {
        return msUserType;
    }

    /*
     * public void setTripType(String sTripType) { msTripType = sTripType;
     * MyApplication
     * .getInstance().getPersitentPreferenceEditor().putString(Constants
     * .PREF_TRIP_TYPE, sTripType);
     * MyApplication.getInstance().getPersitentPreferenceEditor().commit();
     *
     * }
     */
    public String getTripType() {
        return MyApplication
                .getInstance()
                .getPersistentPreference()
                .getString(Constants_user.PREF_TRIP_TYPE, Constants_user.TRIP_TYPE_USER);
    }

    public void setDisplayTripType(String sTripType) {
        MyApplication.getInstance().getPersistentPreferenceEditor()
                .putString(Constants_user.PREF_DISPLAY_TRIP_TYPE, sTripType);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
    }

    public String getDisplayTripType() {
        return MyApplication
                .getInstance()
                .getPersistentPreference()
                .getString(Constants_user.PREF_DISPLAY_TRIP_TYPE,
                        Constants_user.TRIP_TYPE_USER);
    }

    private String tripActionUrl = "";

    public void sendTripAction(final String sTripAction, final Context context) {
        msTripAction = sTripAction;
        tripActionUrl = Constants_user.TRIP_ACTION_USER_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                tripActionUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response: TRIP_ACTION_URL - " +
                        response.toString());
                MyApplication
                        .getInstance()
                        .getPersistentPreferenceEditor()
                        .putString(Constants_user.PREF_TRIP_ACTION,
                                sTripAction);
                if (sTripAction
                        .equals(Constants_user.TRIP_ACTION_BEGIN)) {
                    MyApplication
                            .getInstance()
                            .getPersistentPreferenceEditor()
                            .putBoolean(Constants_user.PREF_IN_TRIP,
                                    true);
                }
                if (msTripAction.equals(Constants_user.TRIP_ACTION_END)) {
                    MyApplication
                            .getInstance()
                            .getPersistentPreferenceEditor()
                            .putBoolean(Constants_user.PREF_IN_TRIP,
                                    false);
                }
                MyApplication
                        .getInstance()
                        .getPersistentPreferenceEditor()
                        .putString(Constants_user.PREF_TRIP_TYPE,
                                getTripType());
                MyApplication.getInstance()
                        .getPersistentPreferenceEditor().commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                SSLog.e(TAG, "sendTripAction :-   ", error.getMessage());

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(
                        "notifyfriends",
                        MyApplication
                                .getInstance()
                                .getPersistentPreference()
                                .getBoolean(Constants_user.PREF_NOTIFY_FRIENDS,
                                        true) ? "1" : "0");
                String sNotifyFriends = MyApplication.getInstance()
                        .getPersistentPreference()
                        .getString(Constants_user.PREF_NOTIFY_FRIENDS_LIST, "");
                StringBuilder sbEmailList = new StringBuilder("");
                if (!TextUtils.isEmpty(sNotifyFriends)) {
                    String emails[] = sNotifyFriends.split("<");
                    for (String str : emails) {
                        if (str.contains(">")) {
                            sbEmailList.append(
                                    str.substring(0, str.indexOf(">"))).append(
                                    ",");
                        }
                    }
                }
                params.put("notifyfriendslist", sbEmailList.toString());
                params.put("triptype", Constants_user.TRIP_TYPE_USER);
                params.put("tripaction", sTripAction);
                params = getAllMobileParams(params, tripActionUrl, context);
                return checkParams(params);
            }

        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    } // sendTripAction

    // get Address object from address string
    public CAddress getAddress(String address) {
        return geoCode("http://maps.google.com/maps/api/geocode/json?address="
                + address.replaceAll(" ", "%20") + "&sensor=false");
    }

    // get Address object from lat, lng
    public CAddress getAddress(LatLng latlng) {
        return geoCode("http://maps.google.com/maps/api/geocode/json?latlng="
                + latlng.latitude + "," + latlng.longitude + "&sensor=false");
    }

    public CAddress geoCode(String query) {
        JSONObject jsonObject = null;
        Double lng = Double.valueOf(0);
        Double lat = Double.valueOf(0);
        CAddress addr = null;
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(query);

        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                InputStream stream = entity.getContent();
                int b;
                while ((b = stream.read()) != -1) {
                    stringBuilder.append((char) b);
                }
                try {
                    jsonObject = new JSONObject(stringBuilder.toString());
                    addr = new CAddress("");
                    JSONObject joResult = ((JSONArray) jsonObject
                            .get("results")).getJSONObject(0);
                    addr.setAddressLine0(joResult
                            .getString("formatted_address"));
                    JSONArray addrComp = ((JSONArray) jsonObject.get("results"))
                            .getJSONObject(0)
                            .getJSONArray("address_components");
                    JSONObject joAddrComp;
                    String sType;
                    int len = addrComp.length();
                    for (int i = 0; i < len; i++) {
                        joAddrComp = (JSONObject) addrComp.get(i);
                        sType = joAddrComp.get("types").toString();

                        if (sType.contains("street_number")) {
                            addr.setStreetNumber(joAddrComp
                                    .isNull("short_name") ? "" : joAddrComp
                                    .getString("short_name"));
                        }
                        if (sType.contains("route")) {
                            addr.setRoute(joAddrComp.isNull("short_name") ? ""
                                    : joAddrComp.getString("short_name"));
                        }
                        if (sType.contains("postal_code")) {
                            addr.setPostalCode(joAddrComp
                                    .getString("short_name"));
                        }
                        if (sType.contains("sublocality_level_1")) {
                            addr.setSubLocality1(joAddrComp
                                    .getString("short_name"));
                        }
                        if (sType.contains("sublocality_level_2")) {
                            addr.setSubLocality2(joAddrComp
                                    .getString("short_name"));
                        }
                        if (sType.contains("country")) {
                            addr.setCountryCode(joAddrComp
                                    .getString("short_name"));
                        }
                        Log.d(TAG,
                                sType + ": "
                                        + joAddrComp.getString("short_name"));
                    } // process address components
                    addr.setCountrySpecificComponents();
                    String locality = ((JSONArray) ((JSONObject) addrComp
                            .get(0)).get("types")).getString(0);
                    if (locality.compareTo("locality") == 0) {
                        locality = ((JSONObject) addrComp.get(0))
                                .getString("long_name");
                        addr.setLocality(locality);
                    }
                    if (locality.compareTo("locality") == 0) {
                        locality = ((JSONObject) addrComp.get(0))
                                .getString("long_name");
                        addr.setLocality(locality);
                    }
                    String adminArea = ((JSONArray) ((JSONObject) addrComp
                            .get(2)).get("types")).getString(0);
                    if (adminArea.compareTo("administrative_area_level_1") == 0) {
                        adminArea = ((JSONObject) addrComp.get(2))
                                .getString("long_name");
                        addr.setAdminArea(adminArea);
                    }
                    String country = ((JSONArray) ((JSONObject) addrComp.get(3))
                            .get("types")).getString(0);
                    if (country.compareTo("country") == 0) {
                        country = ((JSONObject) addrComp.get(3))
                                .getString("long_name");
                        addr.setCountryName(country);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    lng = ((JSONArray) jsonObject.get("results"))
                            .getJSONObject(0).getJSONObject("geometry")
                            .getJSONObject("location").getDouble("lng");

                    lat = ((JSONArray) jsonObject.get("results"))
                            .getJSONObject(0).getJSONObject("geometry")
                            .getJSONObject("location").getDouble("lat");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                addr.setLatitude(lat);
                addr.setLongitude(lng);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return addr;

    }

    // goeCode - Creates and address object from a latlng ofr string address
    public CAddress geoCode(String address, LatLng latlng) {

        String query;
        JSONObject jsonObject = null;
        Double lng = Double.valueOf(0);
        Double lat = Double.valueOf(0);

        if (latlng == null) {
            query = "http://maps.google.com/maps/api/geocode/json?address="
                    + address.replaceAll(" ", "%20") + "&sensor=false";
        } else {
            query = "http://maps.google.com/maps/api/geocode/json?latlng="
                    + latlng.latitude + "," + latlng.longitude
                    + "&sensor=false";
        }
        CAddress addr = null;
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(query);

        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                InputStream stream = entity.getContent();
                int b;
                while ((b = stream.read()) != -1) {
                    stringBuilder.append((char) b);
                }
                try {
                    jsonObject = new JSONObject(stringBuilder.toString());
                    addr = new CAddress();
                    JSONObject joResult = ((JSONArray) jsonObject
                            .get("results")).getJSONObject(0);
                    addr.setAddressLine0(joResult
                            .getString("formatted_address"));
                    JSONArray addrComp = ((JSONArray) jsonObject.get("results"))
                            .getJSONObject(0)
                            .getJSONArray("address_components");
                    JSONObject joAddrComp;
                    String sType;
                    int len = addrComp.length();
                    for (int i = 0; i < len; i++) {
                        joAddrComp = (JSONObject) addrComp.get(i);
                        sType = joAddrComp.get("types").toString();

                        if (sType.contains("street_number")) {
                            addr.setStreetNumber(joAddrComp
                                    .isNull("short_name") ? "" : joAddrComp
                                    .getString("short_name"));
                        }
                        if (sType.contains("route")) {
                            addr.setRoute(joAddrComp.isNull("short_name") ? ""
                                    : joAddrComp.getString("short_name"));
                        }
                        if (sType.contains("postal_code")) {
                            addr.setPostalCode(joAddrComp
                                    .getString("short_name"));
                        }
                        if (sType.contains("sublocality_level_1")) {
                            addr.setSubLocality1(joAddrComp
                                    .getString("short_name"));
                        }
                        if (sType.contains("sublocality_level_2")) {
                            addr.setSubLocality2(joAddrComp
                                    .getString("short_name"));
                        }
                        if (sType.contains("country")) {
                            addr.setCountryCode(joAddrComp
                                    .getString("short_name"));
                        }

                        Log.d(TAG,
                                sType + ": "
                                        + joAddrComp.getString("short_name"));

                    } // process address components
                    addr.setCountrySpecificComponents();
                    String locality = ((JSONArray) ((JSONObject) addrComp
                            .get(0)).get("types")).getString(0);
                    if (locality.compareTo("locality") == 0) {
                        locality = ((JSONObject) addrComp.get(0))
                                .getString("long_name");
                        addr.setLocality(locality);
                    }
                    if (locality.compareTo("locality") == 0) {
                        locality = ((JSONObject) addrComp.get(0))
                                .getString("long_name");
                        addr.setLocality(locality);
                    }
                    String adminArea = ((JSONArray) ((JSONObject) addrComp
                            .get(2)).get("types")).getString(0);
                    if (adminArea.compareTo("administrative_area_level_1") == 0) {
                        adminArea = ((JSONObject) addrComp.get(2))
                                .getString("long_name");
                        addr.setAdminArea(adminArea);
                    }
                    String country = ((JSONArray) ((JSONObject) addrComp.get(3))
                            .get("types")).getString(0);
                    if (country.compareTo("country") == 0) {
                        country = ((JSONObject) addrComp.get(3))
                                .getString("long_name");
                        addr.setCountryName(country);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {

                    lng = ((JSONArray) jsonObject.get("results"))
                            .getJSONObject(0).getJSONObject("geometry")
                            .getJSONObject("location").getDouble("lng");

                    lat = ((JSONArray) jsonObject.get("results"))
                            .getJSONObject(0).getJSONObject("geometry")
                            .getJSONObject("location").getDouble("lat");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                addr.setLatitude(lat);
                addr.setLongitude(lng);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return addr;

    }

    public void sendFlag(final String flagName, final boolean flagValue, final Context context) {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.SET_TRIP_FLAG_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("TAG", flagName + ": " + response.toString());
                Log.d(TAG, (flagValue ? " Set " : " Cleared "));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error

                SSLog.e(TAG, "sendFlag :-   " + flagName,
                        error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("flagname", flagName);
                params.put("flagvalue", flagValue ? "1" : "0");
                params.put("triptype", getTripType());
                params = getAllMobileParams(params, Constants_user.SET_TRIP_FLAG_URL, context);
                return checkParams(params);
            }

        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    } // sendForHire

    public void setJumpIn(boolean jumpIn) {
        MyApplication.getInstance().getPersistentPreferenceEditor()
                .putBoolean(Constants_user.PREF_JUMPIN, jumpIn);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
    }

    public static CAddress getAddressListFromLocation(double lat, double lng)
            throws ClientProtocolException, IOException, JSONException {
        CAddress addr = null;

        String address = String
                .format(Locale.ENGLISH,
                        "http://maps.googleapis.com/maps/api/geocode/json?latlng=%1$f,%2$f&sensor=true&language="
                                + Locale.getDefault().getCountry(), lat, lng);
        HttpGet httpGet = new HttpGet(address);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        response = client.execute(httpGet);
        HttpEntity entity = response.getEntity();
        InputStream stream = entity.getContent();
        int b;
        while ((b = stream.read()) != -1) {
            stringBuilder.append((char) b);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject = new JSONObject(stringBuilder.toString());

        if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
            addr = new CAddress();
            JSONObject joResult = ((JSONArray) jsonObject.get("results"))
                    .getJSONObject(0);
            addr.setAddressLine0(joResult.getString("formatted_address"));
            JSONArray addrComp = ((JSONArray) jsonObject.get("results"))
                    .getJSONObject(0).getJSONArray("address_components");
            JSONObject joAddrComp;
            String sType;
            int len = addrComp.length();
            try {
                for (int i = 0; i < len; i++) {

                    joAddrComp = (JSONObject) addrComp.get(i);
                    sType = joAddrComp.get("types").toString();

                    if (sType.contains("street_number")) {
                        addr.setStreetNumber(joAddrComp.isNull("short_name") ? ""
                                : joAddrComp.getString("short_name"));
                    }
                    if (sType.contains("route")) {
                        addr.setRoute(joAddrComp.isNull("short_name") ? ""
                                : joAddrComp.getString("short_name"));
                    }
                    if (sType.contains("postal_code")) {
                        addr.setPostalCode(joAddrComp.getString("short_name"));
                    }
                    if (sType.contains("sublocality_level_1")) {
                        addr.setSubLocality1(joAddrComp.getString("short_name"));
                    }
                    if (sType.contains("sublocality_level_2")) {
                        addr.setSubLocality2(joAddrComp.getString("short_name"));
                    }
                    if (sType.contains("country")) {
                        addr.setCountryCode(joAddrComp.getString("short_name"));
                    }

                    Log.d(TAG,
                            sType + ": " + joAddrComp.getString("short_name"));

                }
            } catch (Exception e) {
                SSLog.e(TAG,
                        " getAddressListFromLocation - loop for components - ",
                        e);
            }
            addr.setCountrySpecificComponents();
            String locality = ((JSONArray) ((JSONObject) addrComp.get(0))
                    .get("types")).getString(0);
            if (locality.compareTo("locality") == 0) {
                locality = ((JSONObject) addrComp.get(0))
                        .getString("long_name");
                addr.setLocality(locality);
            }
            if (locality.compareTo("locality") == 0) {
                locality = ((JSONObject) addrComp.get(0))
                        .getString("long_name");
                addr.setLocality(locality);
            }
            String adminArea = ((JSONArray) ((JSONObject) addrComp.get(2))
                    .get("types")).getString(0);
            if (adminArea.compareTo("administrative_area_level_1") == 0) {
                adminArea = ((JSONObject) addrComp.get(2))
                        .getString("long_name");
                addr.setAdminArea(adminArea);
            }
            String country = ((JSONArray) ((JSONObject) addrComp.get(3))
                    .get("types")).getString(0);
            if (country.compareTo("country") == 0) {
                country = ((JSONObject) addrComp.get(3)).getString("long_name");
                addr.setCountryName(country);
            }

        }
        addr.setLatitude(lat);
        addr.setLongitude(lng);
        return addr;
    } // getAddressListFromLocation

    public void setFromAddr(CAddress addr) {
        if (moFromAddr == null) {
            moFromAddr = new CAddress();
        }
        moFromAddr = addr;
    }

    public void setToAddr(CAddress addr) {
        if (moToAddr == null) {
            moToAddr = new CAddress();
        }
        moToAddr = addr;
    }

    public int runRightActivity(Context context) {
        Intent intent = null;
        boolean hasJoinedTrip = MyApplication.getInstance()
                .getPersistentPreference()
                .getBoolean(Constants_user.PREF_JOINED_TRIP, false);
        if (!hasJoinedTrip && !CGlobals_lib.getInstance().isInTrip(context)) {
            return 0;
        }
        if (hasJoinedTrip) {
            MyApplication.getInstance().getPersistentPreferenceEditor().
                    putBoolean(Constants_user.GET_A_CAB_VALUE, false);
            MyApplication.getInstance().getPersistentPreferenceEditor().commit();
            intent = new Intent(context, ShareTripTrackDriver_act.class);
            context.startActivity(intent);
        } else {
            if (CGlobals_lib.getInstance().isInTrip(context)) {
                String sTripType = MyApplication.getInstance()
                        .getPersistentPreference()
                        .getString(Constants_user.PREF_TRIP_TYPE, "");
                String sTripAction = MyApplication.getInstance()
                        .getPersistentPreference()
                        .getString(Constants_user.PREF_TRIP_ACTION, "N");
                if (sTripType.equals(Constants_user.TRIP_TYPE_USER)
                        || sTripType.equals(Constants_user.COMMERCIAL)) {
                    sendTripAction(sTripAction, context);
                }
            } else {

            }

        }
        return 1;
    } // runRightActivity

    public Map<String, String> checkParams(Map<String, String> map) {
        Iterator<Entry<String, String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, String> pairs = (Entry<String, String>) it
                    .next();
            if (pairs.getValue() == null || TextUtils.isEmpty(pairs.getValue())) {
                map.put(pairs.getKey(), "");
                SSLog.d(TAG, pairs.getKey());
            }
        }
        return map;
    }

    private String locationToJsonString(Location loc) {
        JsonObject jo = new JsonObject();
        jo.addProperty("latitude", loc.getLatitude());
        jo.addProperty("longitude", loc.getLongitude());
        jo.addProperty("speed", loc.getSpeed());

        jo.addProperty("provider", loc.getProvider());
        jo.addProperty("accuracy", loc.getAccuracy());

        return jo.toString();

    }

    private Location locationFromJsonString(String sJsonLoc) {
        Location loc = null;
        try {
            JSONObject jo = new JSONObject(sJsonLoc);
            loc = new Location(jo.getString("provider"));
            loc.setAccuracy((float) jo.getDouble("accuracy"));
            loc.setLatitude(jo.getDouble("latitude"));
            loc.setLongitude(jo.getDouble("longitude"));
            loc.setSpeed((float) jo.getDouble("speed"));
        } catch (Exception je) {
            SSLog.e(TAG, "locationFromJsonString", je);
        }
        return loc;
    }

    public static String getPhoneNumber() {

        msPhoneNo = MyApplication.getInstance().getPersistentPreference()
                .getString(Constants_user.PREF_PHONENO, "");
        return msPhoneNo.trim();

    }

    public boolean hasTripEnded(Location currentLocation, double toLat,
                                double toLng, Context context) {
        float results[] = new float[1];
        currentLocation = getMyLocation(context);
        Location.distanceBetween(toLat, toLng, currentLocation.getLatitude(),
                currentLocation.getLongitude(), results);
        if (results[0] < Constants_user.NEAR_DESTINATION_DISTANCE) {
            CGlobals_user.getInstance().sendTripAction(
                    Constants_user.TRIP_ACTION_END, context);


            return true;
        }
        return false;
    }

    // Show one time help information
    public void gotIt(Context context, final String sPref, String sHelp1, String sHelp2) {
        final Dialog gotItDialog = new Dialog(context, R.style.GotItDialogTheme);
        gotItDialog.setContentView(R.layout.got_it);
        gotItDialog.show();
        TextView tvHelp1 = ((TextView) gotItDialog.findViewById(R.id.tvHelpLine1));
        tvHelp1.setText(sHelp1);
        TextView tvHelp2 = ((TextView) gotItDialog.findViewById(R.id.tvHelpLine2));
        tvHelp2.setText(sHelp2);
        Button close_btn = (Button) gotItDialog.findViewById(R.id.buttonGotIt);
        close_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                gotItDialog.dismiss();
                MyApplication.getInstance().getPersistentPreferenceEditor().putBoolean(sPref, true);
            }
        });
    }

    public CAddress getHomeAddress(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String sResultHome = "";
        CAddress caHomeAddress = null;
        try {
            sResultHome = pref.getString("homeAddress", "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(sResultHome)) {
            Type type = new TypeToken<CAddress>() {
            }.getType();
            caHomeAddress = new Gson().fromJson(sResultHome, type);
        }
        return caHomeAddress;
    }

    public CAddress getWorkAddress(Context context) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String sResultWork = "";
        CAddress caWorkAddress = null;
        try {
            sResultWork = pref.getString("workAddress", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(sResultWork)) {
            Type type = new TypeToken<CAddress>() {
            }.getType();
            caWorkAddress = new Gson().fromJson(sResultWork, type);
        }
        return caWorkAddress;
    }

    public interface SendTripCallBackInterface {

        public void onRequestFinished(boolean success);
    }

    public void sendTripActionWCallBack(final String sPlannedstartdatetime, final String sTripAction,
                                        final SendTripCallBackInterface sendTripCallBack, final Context context) {

        final String sCarInfo = MyApplication.getInstance().getPersistentPreference().getString(
                Constants_user.PREF_CAR_INFO, "");
        msTripAction = sTripAction;
        tripActionUrl = Constants_user.TRIP_ACTION_USER_URL;
        // if (getTripType().equals(Constants.COMMERCIAL)) {
        // tripActionUrl = Constants.TRIP_ACTION_COMMERCIAL_URL;
        // }
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                tripActionUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response: TRIP_ACTION_URL - " +
                        response.toString());
                MyApplication
                        .getInstance()
                        .getPersistentPreferenceEditor()
                        .putString(Constants_user.PREF_TRIP_ACTION,
                                sTripAction);
                if (sTripAction
                        .equals(Constants_user.TRIP_ACTION_BEGIN)) {
                    MyApplication
                            .getInstance()
                            .getPersistentPreferenceEditor()
                            .putBoolean(Constants_user.PREF_IN_TRIP,
                                    true);
                }
                if (msTripAction.equals(Constants_user.TRIP_ACTION_END)) {
                    MyApplication
                            .getInstance()
                            .getPersistentPreferenceEditor()
                            .putBoolean(Constants_user.PREF_IN_TRIP,
                                    false);
                }

                MyApplication
                        .getInstance()
                        .getPersistentPreferenceEditor()
                        .putString(Constants_user.PREF_TRIP_TYPE,
                                getTripType());
                MyApplication.getInstance()
                        .getPersistentPreferenceEditor().commit();
                sendTripCallBack.onRequestFinished(true);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error

                SSLog.e(TAG, "sendTripAction :-   ", error.getMessage());

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(
                        "notifyfriends",
                        MyApplication
                                .getInstance()
                                .getPersistentPreference()
                                .getBoolean(Constants_user.PREF_NOTIFY_FRIENDS,
                                        true) ? "1" : "0");
                String sNotifyFriends = MyApplication.getInstance()
                        .getPersistentPreference()
                        .getString(Constants_user.PREF_NOTIFY_FRIENDS_LIST, "");
                StringBuilder sbEmailList = new StringBuilder("");
                if (!TextUtils.isEmpty(sNotifyFriends)) {
                    String emails[] = sNotifyFriends.split("<");
                    for (String str : emails) {
                        if (str.contains(">")) {
                            sbEmailList.append(
                                    str.substring(0, str.indexOf(">"))).append(
                                    ",");
                        }
                    }
                }
                params.put("carinfo", sCarInfo);
                params.put("notifyfriendslist", sbEmailList.toString());
                params.put("triptype", Constants_user.TRIP_TYPE_USER);
                params.put("tripaction", sTripAction);
                params = getBasicMobileParams(params, tripActionUrl, context);
                if (!TextUtils.isEmpty(sPlannedstartdatetime)) {
                    params.put("plannedstartdatetime", sPlannedstartdatetime);
                } else {
                    params.put("plannedstartdatetime", params.get("clientdatetime"));
                }
                return checkParams(params);
            }

        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    } // sendTripAction

   /* boolean checkLocationPermission(Context context) {
        if (Build.VERSION.SDK_INT < 23)
            return true;
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }*/

} // CGlobals

