package com.jumpinjumpout.apk.driver;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

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
import com.google.gson.JsonObject;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.Constants_lib;
import com.jumpinjumpout.apk.lib.SSLog;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
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
public class CGlobals_driver {
    public static final String TAG = "CGlobals_driver: ";
    private static CGlobals_driver instance;
    private int nJoined = 0, nDroppedOut = 0;

    // Restrict the constructor from being instantiated
    private CGlobals_driver() {
    }

    public static synchronized CGlobals_driver getInstance() {
        if (instance == null) {
            instance = new CGlobals_driver();
        }
        return instance;
    }

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

    public String getEmailId() {
        return msGmail;
    }

    public int getAppUserId() {
        return miAppUserId;
    }

    public boolean mExitApp = false;
    private double DRIVER_COST_PER_KM = 15;
    private double PASSENGER_COST_PER_KM = 8;

    public void init(Context act) {
        msPhoneNo = MyApplication.getInstance().getPersistentPreference()
                .getString(Constants_driver.PREF_PHONENO, "");
        msCountryCode = MyApplication.getInstance().getPersistentPreference()
                .getString(Constants_driver.PREF_COUNTRY_CODE, "");

        mbAppInited = false;
        bStopsRead = false;
        mdTravelDistance = 0;
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
            e.printStackTrace();
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
            MyApplication.getInstance().getPreferenceEditor()
                    .putString("dbVersionInfo", msAppVersionName);
            MyApplication.getInstance().getPreferenceEditor().commit();

            MyApplication.getInstance().getPersistentPreferenceEditor()
                    .putString("dbVersionInfo", currentVersion);

            MyApplication.getInstance().getPersistentPreferenceEditor()
                    .commit();

        }

        mCurrentVersion = MyApplication.getInstance().getPreference()
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

    public boolean checkConnected(Context context) {
        for (int i = 0; i < 2; i++) {
//			Log.i(TAG,
//					"Working... " + (i + 1) + "/5 @ "
//							+ SystemClock.elapsedRealtime());
            try {
                if (isConnected(context)) {
                    return true;
                }
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
        return false;
    }

    private boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

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
        boolean isSignificantlyNewer = timeDelta > Constants_driver.TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -Constants_driver.TWO_MINUTES;
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
        Location gpslocation = getLocationByProvider(LocationManager.GPS_PROVIDER, context);
        Location networkLocation = getLocationByProvider(LocationManager.NETWORK_PROVIDER, context);
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
    private Location getLocationByProvider(String provider, Context context) {
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
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    public AlertDialog.Builder buildNoConnectionDialog(Context c) {

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("No Internet connection.");
        builder.setMessage("Please activate your Internet connection\nThis module will not work without an Internet connection");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        return builder;
    }

    public Map<String, String> getAllMobileParams(Map<String, String> params,
                                                  String url, final Context context) {
        init(context);
        Location myLocation = getMyLocation(context);
        SimpleDateFormat df = new SimpleDateFormat(
                Constants_driver.STANDARD_DATE_FORMAT, Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        msPhoneNo = MyApplication.getInstance().getPersistentPreference()
                .getString(Constants_driver.PREF_PHONENO, "");
        msCountryCode = MyApplication.getInstance().getPersistentPreference()
                .getString(Constants_driver.PREF_COUNTRY_CODE, "");
        params.put(
                "appuserid",
                Integer.toString(MyApplication.getInstance()
                        .getPersistentPreference()
                        .getInt(Constants_driver.PREF_APPUSERID, -1)));
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
        params.put("version", CGlobals_driver.msAppVersionName);
        params.put("android_release_version", msAndroidReleaseVersion);
        params.put("versioncode", String.valueOf(miAppVersionCode));
        params.put("android_sdk_version", Integer.toString(miAndroidSdkVersion));
        params.put("imei", CGlobals_driver.mIMEI);
        params.put("email", CGlobals_driver.msGmail);
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
            url = url + "?" + getParams.toString() + "&verbose=Y";
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
                .getString(Constants_driver.PREF_PHONENO, "");
        msCountryCode = MyApplication.getInstance().getPersistentPreference()
                .getString(Constants_driver.PREF_COUNTRY_CODE, "");
        params.put("countrycode", msCountryCode.trim());
        params.put("phoneno", msPhoneNo.trim());
        params.put(
                "appuserid",
                Integer.toString(MyApplication.getInstance()
                        .getPersistentPreference()
                        .getInt(Constants_driver.PREF_APPUSERID, -1)));
        params.put("email", CGlobals_driver.msGmail);
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

        String delim = "";
        StringBuilder getParams = new StringBuilder();
        for (Entry<String, String> entry : params.entrySet()) {
            getParams.append(delim + entry.getKey() + "=" + entry.getValue());
            delim = "&";

        }
        try {
            url = url + "?" + getParams.toString() + "&verbose=Y";
            System.out.println("url  " + url);
        } catch (Exception e) {
            Log.e(TAG, e.getStackTrace().toString());
        }
        return checkParams(params);
    }

    public Map<String, String> getMinAllMobileParams(Map<String, String> params,
                                                     String url) {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        params.put("countrycode",
                MyApplication.getInstance().getPersistentPreference()
                        .getString(Constants_driver.PREF_COUNTRY_CODE, "").trim());
        params.put("phoneno",
                MyApplication.getInstance().getPersistentPreference()
                        .getString(Constants_driver.PREF_PHONENO, "").trim());

        params.put(
                "appuserid",
                Integer.toString(MyApplication.getInstance()
                        .getPersistentPreference()
                        .getInt(Constants_driver.PREF_APPUSERID, -1)));
        params.put("email", CGlobals_driver.msGmail);
        params.put("clientdatetime", df.format(cal.getTime()));
        String delim = "";
        StringBuilder getParams = new StringBuilder();
        for (Entry<String, String> entry : params.entrySet()) {
            getParams.append(delim + entry.getKey() + "=" + entry.getValue());
            delim = "&";
        }
        try {
            url = url + "?" + getParams.toString() + "&verbose=Y";

        } catch (Exception e) {
            Log.e(TAG, e.getStackTrace().toString());
        }
        return checkParams(params);
    }

    public void setMyLocation(Location location, boolean bForceLocation) {
        if (location == null)
            return;
        if (bForceLocation) {
            mCurrentLocation = location;
        } else {
            if (isBetterLocation(location, mCurrentLocation)) {
                mCurrentLocation = location;
            }
        }
        final String jsonLocation = locationToJsonString(mCurrentLocation);
        new Thread(new Runnable() {
            public void run() {
                MyApplication
                        .getInstance()
                        .getPersistentPreferenceEditor()
                        .putString(Constants_driver.PREF_MY_LOCATION,
                                jsonLocation);
                MyApplication.getInstance().getPersistentPreferenceEditor()
                        .commit();
            }
        }).start();
    }

    public Location getMyLocation(Context context) {
        if (mCurrentLocation == null) {
            Location location = getBestLocation(context);
            mCurrentLocation = location;
            if (mCurrentLocation == null) {
                String jsonLocation = MyApplication.getInstance()
                        .getPersistentPreference()
                        .getString(Constants_driver.PREF_MY_LOCATION, "");
                if (!TextUtils.isEmpty(jsonLocation)) {
                    try {
                        mCurrentLocation = locationFromJsonString(jsonLocation);
                    } catch (Exception e) {
                        SSLog.e(TAG,
                                "getMyLocation: Could not convert jsonLocation to location object",
                                e);
                        mCurrentLocation = null;

                    }
                }
            }
        }
        if (mCurrentLocation != null) {
            setMyLocation(mCurrentLocation, false);
        }
        return mCurrentLocation;
    }

    public void sendUpdatePosition(Location location, final Context context) {
        CGlobals_lib.getInstance().sendUpdatePosition(Constants_driver.UPDATE_POSITON_DRIVER_URL, location, context);
    }

    public String getVolleyError(VolleyError error, Context context) {
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

    public String getTripType() {
        return MyApplication
                .getInstance()
                .getPersistentPreference()
                .getString(Constants_driver.PREF_TRIP_TYPE, Constants_driver.TRIP_TYPE);
    }

    private String tripActionUrl = "";

    public void sendTripAction(final String sTripAction, final Context context) {

        final int tripid = CGlobals_lib.getInstance().getSharedPreferences(context)
                .getInt(Constants_lib.PREF_TRIP_ID_INT, 0);
        final String sCarInfo = MyApplication.getInstance().getPersistentPreference().getString(
                Constants_driver.PREF_CAR_INFO, "");
        final int cabtripiddriver = MyApplication.getInstance().getPersistentPreference().
                getInt("CAB_TRIP_ID_DRIVER", 0);
        msTripAction = sTripAction;
        tripActionUrl = Constants_driver.TRIP_ACTION_DRIVER_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                tripActionUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response: TRIP_ACTION_URL - " + response.toString());
                MyApplication
                        .getInstance()
                        .getPersistentPreferenceEditor()
                        .putString(Constants_driver.PREF_TRIP_ACTION,
                                sTripAction);
                if (sTripAction
                        .equals(Constants_driver.TRIP_ACTION_BEGIN)) {
                    MyApplication
                            .getInstance()
                            .getPersistentPreferenceEditor()
                            .putBoolean(Constants_driver.PREF_IN_TRIP,
                                    true);
                }
                if (msTripAction.equals(Constants_driver.TRIP_ACTION_END)) {
                    MyApplication
                            .getInstance()
                            .getPersistentPreferenceEditor()
                            .putBoolean(Constants_driver.PREF_IN_TRIP,
                                    false);
                }
                MyApplication
                        .getInstance()
                        .getPersistentPreferenceEditor()
                        .putString(Constants_driver.PREF_TRIP_TYPE,
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
                                .getBoolean(Constants_driver.PREF_NOTIFY_FRIENDS,
                                        true) ? "1" : "0");
                String sNotifyFriends = MyApplication.getInstance()
                        .getPersistentPreference()
                        .getString(Constants_driver.PREF_NOTIFY_FRIENDS_LIST, "");
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
                params.put("tripid", String.valueOf(tripid));
                params.put("notifyfriendslist", sbEmailList.toString());
                params.put("triptype", Constants_driver.TRIP_TYPE);
                params.put("tripaction", sTripAction);
                params.put("cabtripid", String.valueOf(cabtripiddriver));
                params = getAllMobileParams(params, tripActionUrl, context);
                return checkParams(params);
            }

        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    } // sendTripAction

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

    public Map<String, String> checkParams(Map<String, String> map) {
        Iterator<Entry<String, String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, String> pairs = (Entry<String, String>) it
                    .next();
            if (pairs.getValue() == null) {
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

    public boolean hasTripEnded(Location mCurrentLocation2, double toLat,
                                double toLng, Context context) {
        float results[] = new float[1];

        Location.distanceBetween(toLat, toLng, mCurrentLocation2.getLatitude(),
                mCurrentLocation2.getLongitude(), results);
        if (results[0] < Constants_driver.NEAR_DESTINATION_DISTANCE) {
            CGlobals_driver.getInstance().sendTripAction(
                    Constants_driver.TRIP_ACTION_END, context);

            return true;
        }
        return false;
    }

    public interface SendTripCallBackInterface {
        public void onRequestFinished(boolean success);
    }

    public void sendTripActionWCallBack(final String sPlannedstartdatetime, final String sTripAction,
                                        final SendTripCallBackInterface sendTripCallBack, final Context context) {

        final int tripid = CGlobals_lib.getInstance().getSharedPreferences(context)
                .getInt(Constants_lib.PREF_TRIP_ID_INT, 0);
        final String sCarInfo = MyApplication.getInstance().getPersistentPreference().getString(
                Constants_driver.PREF_CAR_INFO, "");
        final int cabtripiddriver = MyApplication.getInstance().getPersistentPreference().
                getInt("CAB_TRIP_ID_DRIVER", 0);
        msTripAction = sTripAction;
        tripActionUrl = Constants_driver.TRIP_ACTION_DRIVER_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                tripActionUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response: TRIP_ACTION_URL - " +
                        response.toString());
                MyApplication
                        .getInstance()
                        .getPersistentPreferenceEditor()
                        .putString(Constants_driver.PREF_TRIP_ACTION,
                                sTripAction);
                if (sTripAction
                        .equals(Constants_driver.TRIP_ACTION_BEGIN)) {
                    MyApplication
                            .getInstance()
                            .getPersistentPreferenceEditor()
                            .putBoolean(Constants_driver.PREF_IN_TRIP,
                                    true);
                }
                if (msTripAction.equals(Constants_driver.TRIP_ACTION_END)) {
                    MyApplication
                            .getInstance()
                            .getPersistentPreferenceEditor()
                            .putBoolean(Constants_driver.PREF_IN_TRIP,
                                    false);
                }

                MyApplication
                        .getInstance()
                        .getPersistentPreferenceEditor()
                        .putString(Constants_driver.PREF_TRIP_TYPE,
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
                                .getBoolean(Constants_driver.PREF_NOTIFY_FRIENDS,
                                        true) ? "1" : "0");
                String sNotifyFriends = MyApplication.getInstance()
                        .getPersistentPreference()
                        .getString(Constants_driver.PREF_NOTIFY_FRIENDS_LIST, "");
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
                params.put("tripid", String.valueOf(tripid));
                params.put("notifyfriendslist", sbEmailList.toString());
                params.put("triptype", Constants_driver.TRIP_TYPE);
                params.put("tripaction", sTripAction);
                params.put("cabtripid", String.valueOf(cabtripiddriver));
                params = getBasicMobileParams(params, tripActionUrl, context);
                if (!TextUtils.isEmpty(sPlannedstartdatetime)) {
                    params.put("plannedstartdatetime", sPlannedstartdatetime);
                } else {
                    params.put("plannedstartdatetime", params.get("clientdatetime"));
                }

                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "=" + entry.getValue());
                    delim = "&";

                }
                try {
                    String url1 = tripActionUrl + "?" + getParams.toString() + "&verbose=Y";
                    Log.d(TAG, url1);
                    SSLog.d(TAG, url1);
                    System.out.println("url1 : " + url1);

                } catch (Exception e) {
                    Log.e(TAG, e.getStackTrace().toString());
                }

                return checkParams(params);
            }

        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    } // sendTripAction

    /*boolean checkLocationPermission(Context context) {
        if (Build.VERSION.SDK_INT < 23)
            return true;
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }*/

} // CGlobals_driver
