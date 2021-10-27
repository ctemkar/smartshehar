package com.jumpinjumpout.apk.lib;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

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
import lib.app.util.UnitLocale;
import lib.app.util.UserInfo;


public class CGlobals_lib {

    public static final String TAG = "CGlobals_lib: ";
    private static CGlobals_lib instance;
    public static RequestQueue mVolleyRequestQueue;
    private boolean mHasVolleyConnection = false;
    public static boolean showGPSDialog = true;
    public boolean isNewNotification = false;

    // Restrict the constructor from being instantiated
    private CGlobals_lib() {
    }

    private SharedPreferences mPrefsVersionPersistent = null;
    SharedPreferences.Editor mEditorVersionPersistent = null;

    public static synchronized CGlobals_lib getInstance() {
        if (instance == null) {
            instance = new CGlobals_lib();
        }
        return instance;
    }

    public SSLog sslog;
    public static boolean mbIsAdmin;
    //static String msCity;
    // public static final String DEFAULT_CITY = "MUMBAI";
    private Location mCurrentLocation;
    public boolean bStopsRead;
    public Stack<Integer> stackActivity;
    protected double mdTravelDistance;
    public static String msInTrip;
    public static String msTripAction = "";
    public static boolean mbAppInited;
    public PackageInfo mPackageInfo = null;
    public String msProduct;
    public String msManufacturer;
    public String msCarrier;
    public String mAppNameCode;
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
    String msOsReleaseVersion; // e.g. myVersion := "1.6"
    int miOsSdkVersion; // e.g. sdkVersion := 8;
    public UserInfo mUserInfo;
    public ArrayList<CAddress> maoRecentAddress;
    public PolylineOptions mDirectionsPolyline;
    public int miTravelDistance;
    public static boolean mbGPSFix;
    public static Location moLocStart, moLocDest;
    public static boolean inDriverTrip;
    //private static String msUserType = "";
    Map<String, String> mMobileParams;
    public boolean isLocationServiceStarted;
    // public boolean mExitApp = false;
    // private double DRIVER_COST_PER_KM = 15;
    // private double PASSENGER_COST_PER_KM = 8;
    private static boolean isUpdatingPosition = false;
    public static final int HAVEGPRS = 1;
    public static final int HAVEWIFI = 2;
    public static final int HAVECONNECTIONOTHER = 2;
    public static final String PREFS_NAME = "CBAppPrefsFile";

    final public static int REQUEST_LOCATION = 1000;

    @SuppressLint("MissingPermission")
    public void init(Context context) {
        msPhoneNo = getSharedPreferences(context).getString(
                Constants_lib.PREF_PHONENO, "");
        msCountryCode = getSharedPreferences(context).getString(
                Constants_lib.PREF_COUNTRY_CODE, "");

        mbAppInited = false;
        bStopsRead = false;
        mdTravelDistance = 0;
        readPreferences();
        if (mbAppInited)
            return;

        mAppNameShort = context.getString(R.string.appNameShort);
        mFakeLocation = new Location("fake"); // Dadar - Tilak Bridge
        double lat = 19.02078;
        double lon = 72.843168;
        mFakeLocation.setLatitude(lat);
        mFakeLocation.setLongitude(lon);
        mFakeLocation.setAccuracy(9999);

        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        mAppNameCode = context.getString(R.string.appNameShort);
        mIMEI = telephonyManager.getDeviceId();
        msCarrier = telephonyManager.getNetworkOperator();
        mLine1Number = telephonyManager.getLine1Number();
        mLine1Number = telephonyManager.getSubscriberId();
        msProduct = Build.PRODUCT;
        msManufacturer = Build.MANUFACTURER;
        mbAppInited = true;
        mPackageInfo = getPackageInfo(context);
        stackActivity = new Stack<Integer>();
        AccountManager manager = (AccountManager) context
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
        String currentVersion = getSharedPreferences(context).getString(
                "dbVersionInfo", "-1");
        if (!msAppVersionName.equals(currentVersion)) {

            getSharedPreferencesEditor(context).putString("dbVersionInfo",
                    msAppVersionName);
            getSharedPreferencesEditor(context).commit();

        }
        msOsReleaseVersion = Build.VERSION.RELEASE; // e.g. myVersion
        // := "1.6"
        miOsSdkVersion = Build.VERSION.SDK_INT; // e.g. sdkVersion :=
        // 8;
        mCurrentVersion = getSharedPreferences(context).getString("dbVersionInfo",
                "-1");
        mUserInfo = new UserInfo(context, "jijo_lib");
        // Is this the first use after download? then send to server as first
        // use
        /*if (!currentVersion.equals(mPackageInfo.versionCode)) { // First Use
        }*/

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
            try {
                if (isConnected(context)) {
                    return true;
                }
                Thread.sleep(Constants_lib.INTERNET_CONNECTION_INTERVAL);
            } catch (Exception e) {
                SSLog.e(TAG, "checkConnected ", e);
            }
        }
        return false;
    }

    private boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        @SuppressLint("MissingPermission") NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    // Read App global preferences
    public void readPreferences() {
    }

    ;

    /**
     * Determines whether one Location reading is better than the current
     * Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new
     *                            one
     */
    public boolean isBetterLocation(Location location,
                                    Location currentBestLocation) {
        try {
            if (currentBestLocation == null) {
                // A new location is always better than no location
                return true;
            }

            // Check whether the new location fix is newer or older
            long timeDelta = location.getTime() - currentBestLocation.getTime();
            boolean isSignificantlyNewer = timeDelta > Constants_lib.TWO_MINUTES;
            boolean isSignificantlyOlder = timeDelta < -Constants_lib.TWO_MINUTES;
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
        } catch (Exception e) {
            e.printStackTrace();
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
    public Location getBestLocation(Context context, Location location) {
        Location gpslocation = getLocationByProvider(
                LocationManager.GPS_PROVIDER, context);
        Location networkLocation = getLocationByProvider(
                LocationManager.NETWORK_PROVIDER, context);
        // if we have only one location available, the choice is easy
        if (gpslocation == null) {
            Log.d(TAG, "No GPS Location available.");
            return isBetterLocation(networkLocation, location) ? networkLocation : location;

        }
        if (networkLocation == null) {
            Log.d(TAG, "No Network Location available");
            return isBetterLocation(gpslocation, location) ? gpslocation : location;
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
            return isBetterLocation(gpslocation, location) ? gpslocation : location;
        }
        // gps is old, we can't trust it. use network location
        if (!networkIsOld) {
            Log.d(TAG, "GPS is old, Network is current, returning network");
            return isBetterLocation(networkLocation, location) ? networkLocation : location;
        }
        // both are old return the newer of those two
        if (gpslocation.getTime() > networkLocation.getTime()) {
            Log.d(TAG, "Both are old, returning gps(newer)");
            return isBetterLocation(gpslocation, location) ? gpslocation : location;
        } else {
            Log.d(TAG, "Both are old, returning network(newer)");
            return isBetterLocation(networkLocation, location) ? networkLocation : location;
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

    public ArrayList<CAddress> readRecentAddresses(Context context) {

        maoRecentAddress = new ArrayList<CAddress>();

        String sAddress = getSharedPreferences(context).getString(
                Constants_lib.PREF_RECENT_ADDRESSES, "");
        if (!TextUtils.isEmpty(sAddress)) {
            try {
                JSONArray jsArray = new JSONArray(sAddress);
                for (int x = 0; x < jsArray.length(); x++) {
                    JSONObject oAddress = (JSONObject) jsArray.get(x);
                    maoRecentAddress.add(new CAddress(oAddress));
                }

            } catch (Exception e) {
                SSLog.e(TAG, "readRecentAddresses - ", e);
                getSharedPreferencesEditor(context).putString(
                        Constants_lib.PREF_RECENT_ADDRESSES, "");
                getSharedPreferencesEditor(context).commit();
            }
        }

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String sResultHome = pref.getString("homeAddress", "");
        String sResultWork = pref.getString("workAddress", "");

        if (!TextUtils.isEmpty(sResultWork)) {
            Type type = new TypeToken<CAddress>() {
            }.getType();

            try {
                CAddress caWorkAddress = new Gson().fromJson(sResultWork, type);
                addRecentAddress(caWorkAddress);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
        if (!TextUtils.isEmpty(sResultHome)) {
            Type type = new TypeToken<CAddress>() {
            }.getType();

            try {
                CAddress caHomeAddress = new Gson().fromJson(sResultHome, type);
                addRecentAddress(caHomeAddress);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
        return maoRecentAddress;

    } // ReadRecentAddresses

    public Map<String, String> getBasicMobileParams(Map<String, String> params,
                                                    String url, Context context) {
        Location myLocation = getMyLocation(context);
        init(context);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        msPhoneNo = getSharedPreferences(context).getString(
                Constants_lib.PREF_PHONENO, "");
        msCountryCode = getSharedPreferences(context).getString(
                Constants_lib.PREF_COUNTRY_CODE, "");
        params.put(
                "appuserid",
                Integer.toString(getSharedPreferences(context).getInt(
                        Constants_lib.PREF_APPUSERID, -1)));
        params.put("email", CGlobals_lib.msGmail);
        params.put("clientdatetime", df.format(cal.getTime()));
        if (myLocation != null) {
            String sProvider = myLocation.getProvider();
            if (sProvider.equals("fused")) {
                sProvider = "F";
            } else if (sProvider.equals("network")) {
                sProvider = "N";
            } else if (sProvider.equals("gps")) {
                sProvider = "G";
            }
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
            url = url + "?" + getParams.toString() + "&verbose=Y";
            System.out.println("getBasicMobileParams url:- " + url);
        } catch (Exception e) {
            SSLog.e(TAG, "getBasicMobileParams ", e);
        }
        return checkParams(params);
    }

    public Map<String, String> getBasicMobileParamsShort(Map<String, String> params,
                                                         String url, Context context) {
        Location myLocation = getMyLocation(context);
        init(context);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        msPhoneNo = getSharedPreferences(context).getString(
                Constants_lib.PREF_PHONENO, "");
        msCountryCode = getSharedPreferences(context).getString(
                Constants_lib.PREF_COUNTRY_CODE, "");
        params.put(
                "i",
                Integer.toString(getSharedPreferences(context).getInt(
                        Constants_lib.PREF_APPUSERID, -1)));
        params.put("e", CGlobals_lib.msGmail);
        params.put("ct", df.format(cal.getTime()));
        if (myLocation != null) {
            String sProvider = myLocation.getProvider();
            if (sProvider.equals("fused")) {
                sProvider = "F";
            } else if (sProvider.equals("network")) {
                sProvider = "N";
            } else if (sProvider.equals("gps")) {
                sProvider = "G";
            }
            Date locationDateTime = new Date(myLocation.getTime());
            params.put("p", sProvider);
            params.put("l", String.format("%.9f", myLocation.getLatitude()));
            params.put("o", String.format("%.9f", myLocation.getLongitude()));
            params.put("a", Double.toString(myLocation.getAccuracy()));
            params.put("al", Double.toString(myLocation.getAltitude()));
            params.put("b", Double.toString(myLocation.getBearing()));
            params.put("s", Double.toString(myLocation.getSpeed()));
            params.put("lt", df.format(locationDateTime));
        }
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

    public void setMyLocation(Location location) {
        if (location == null)
            return;
        mCurrentLocation = isBetterLocation(location, mCurrentLocation) ? location
                : mCurrentLocation;
    }

    public void setMyLocation(Location location, boolean bForceLocation) {
        if (location == null)
            return;
        if (bForceLocation) {
            mCurrentLocation = isBetterLocation(location, mCurrentLocation) ? location
                    : mCurrentLocation;
        } else {
            mCurrentLocation = isBetterLocation(location, mCurrentLocation) ? location
                    : mCurrentLocation;
        }
    }

    public Location getMyLocation(Context context) {
        if (mCurrentLocation != null) {
            return mCurrentLocation;
        } else {
            return getBestLocation(context, mCurrentLocation);
        }
    }

    public boolean isInTrip(Context context) {
        boolean bIsInTrip = false;
        try {

            msTripAction = CGlobals_lib.getInstance().getSharedPreferences(context)
                    .getString(Constants_lib.PREF_TRIP_ACTION,
                            Constants_lib.TRIP_ACTION_NONE);
            boolean isTripActive = getSharedPreferences(context).getBoolean(
                    Constants_lib.PREF_IN_TRIP, false);
            if (!isTripActive || (msTripAction.equals(Constants_lib.TRIP_ACTION_END)
                    || msTripAction.equals(Constants_lib.TRIP_ACTION_ABORT))) {
                bIsInTrip = false;
            } else {
                bIsInTrip = true;
            }

        } catch (Exception e) {
            SSLog.e(TAG, " isInTrip ", e);
        }
        return bIsInTrip;
    }

    public String getDistanceText(float dist) {
        String sDistance = "";
        if (dist < 1000) {
            sDistance = (int) dist + " m";
        } else {
            sDistance = String.format("%.2f", dist / 1000.0) + " km";
        }
        return sDistance;
    }

    public String getVolleyError(VolleyError error) {
        if (error instanceof NetworkError) {
            return "Net error. Please check your connection and try again";
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

    public String getTripType(Context context) {
        return getSharedPreferences(context).getString(
                Constants_lib.PREF_TRIP_TYPE, Constants_lib.DRIVER);
    }

    public void sendUpdatePosition(final String sUpdatePositionUrl, Location location, final Context context) {
        if (TextUtils.isEmpty(sUpdatePositionUrl)) {
            return;
        }
        if (isUpdatingPosition) {
        }
        isUpdatingPosition = true;

        StringRequest postRequest = new StringRequest(Request.Method.POST,
                sUpdatePositionUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (TextUtils.isEmpty(response)
                                || response.equals(null)) { // write soumen
                            return;
                        }
                        Log.d("Response: ", "UPDATE_POSITION_URL - " +
                                response.toString());
                        // response
                        isUpdatingPosition = false;
                        mHasVolleyConnection = true;
                        SSLog.d("Response CGlobals_driver: sebdUpdatePosition - ",
                                response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                isUpdatingPosition = false;
                mHasVolleyConnection = false;
                try {
                    SSLog.e(TAG, "sendUpdatePosition - ",
                            error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG, "sendUpdatePosition - ", e);
                    String sError = getVolleyError(error);
                    if (sError.equals(context
                            .getString(R.string.retry_internet))) {
                        SSLog.e(TAG, "sendUpdatePosition - ", e);

                    }
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("td", getTripType(context));
                params.put("ta", isInTrip(context) ? "1" : "0");
                params = getBasicMobileParamsShort(params,
                        sUpdatePositionUrl, context);
                return checkParams(params);
            }
        };
        try {
            addVolleyRequest(postRequest, false, context);
        } catch (Exception e) {
            isUpdatingPosition = false;
            SSLog.e(TAG, "mVolleyRequestQueue may not be initialized - ", e);
        }
    } // sendUpdatePosition

    public boolean hasVolleyConnection() {
        return mHasVolleyConnection;
    }

    public void writeRecentAddresses(final Context context) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    if (maoRecentAddress == null) {
                        return;
                    }

                    int iLen = maoRecentAddress.size();
                    if (iLen < 1)
                        return;
                    JSONArray aJson = new JSONArray();
                    for (int i = 0; i < iLen; i++) {
                        JSONObject oAddr = maoRecentAddress.get(i).toJSon();
                        if (oAddr != null) {
                            aJson.put(maoRecentAddress.get(i).toJSon());
                        }

                    }
                    if (aJson.length() > 0) {
                        getSharedPreferencesEditor(context).putString(
                                Constants_lib.PREF_RECENT_ADDRESSES,
                                aJson.toString());
                        getSharedPreferencesEditor(context).commit();
                    }
                } catch (Exception e) {
                    SSLog.e(TAG, "writeRecentAddresses: ", e);
                }
            }
        }).start();
    } // writeRecentAddressesString

    public void addRecentAddress(CAddress oAddress) {
        if (oAddress == null) {
            return;
        }
        if (maoRecentAddress == null)
            maoRecentAddress = new ArrayList<CAddress>();
        int iLen = maoRecentAddress.size();
        if (iLen > Constants_lib.MAXADDRESS)
            maoRecentAddress.remove(iLen - 1);
        // remove the same Station lower in the list
        iLen = maoRecentAddress.size();
        CAddress sf;
        int i = 0;
        while (i < iLen) {
            sf = maoRecentAddress.get(i);
            if (sf == null || oAddress == null) {
                continue;
            }
            if (sf.getAddress().trim().equals(oAddress.getAddress().trim())) {
                maoRecentAddress.remove(i);
                iLen = maoRecentAddress.size();
            } else {
                i++;
            }
        }
        maoRecentAddress.add(0, oAddress);
    } // addRecentAddress

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

    public SharedPreferences getSharedPreferences(Context context) {
        if (mPrefsVersionPersistent == null) {
            mPrefsVersionPersistent = context.getSharedPreferences(
                    Constants_lib.PREFS_VERSION_PERSISTENT,
                    Context.MODE_PRIVATE);
        }
        return mPrefsVersionPersistent;
    }

    public SharedPreferences.Editor getSharedPreferencesEditor(
            Context context) {
        if (mEditorVersionPersistent == null) {
            mEditorVersionPersistent = getSharedPreferences(context).edit();
        }
        return mEditorVersionPersistent;
    }

    public void addVolleyRequest(StringRequest postRequest,
                                 boolean clearBeforeQuery, Context context) {
        try {
            postRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            if (clearBeforeQuery) {
                getRequestQueue(context).cancelAll(
                        new RequestQueue.RequestFilter() {
                            @Override
                            public boolean apply(Request<?> request) {
                                return true;
                            }
                        });
            }
            getRequestQueue(context).add(postRequest);
        } catch (Exception e) {
            SSLog.e(TAG, "mVolleyRequestQueue may not be initialized - ", e);
        }

    }

    public void addVolleyRequest(JsonObjectRequest getRequest, Context context) {
        try {
            getRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            getRequestQueue(context).add(getRequest);
        } catch (Exception e) {
            SSLog.e(TAG, "mVolleyRequestQueue may not be initialized - ", e);
        }
    }

    public void addVolleyRequest(ImageRequest postRequest,
                                 boolean clearBeforeQuery, Context context) {
        try {
            postRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            if (clearBeforeQuery) {
                getRequestQueue(context).cancelAll(
                        new RequestQueue.RequestFilter() {
                            @Override
                            public boolean apply(Request<?> request) {
                                return true;
                            }
                        });
            }
            getRequestQueue(context).add(postRequest);
        } catch (Exception e) {
            SSLog.e(TAG, "mVolleyRequestQueue may not be initialized - ", e);
        }
    }

    public RequestQueue getRequestQueue(Context context) {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (mVolleyRequestQueue == null) {

            // Instantiate the cache
            Cache cache = new DiskBasedCache(context.getCacheDir(), 1024 * 1024); // 1MB
            // cap

            // Set up the network to use HttpURLConnection as the HTTP client.
            Network network = new BasicNetwork(new HurlStack());

            // Instantiate the RequestQueue with the cache and network.
            mVolleyRequestQueue = new RequestQueue(cache, network);
            mVolleyRequestQueue.start();

            // mVolleyRequestQueue =
            // Volley.newRequestQueue(sInstance.getApplicationContext());

            // Start the queue
            // mVolleyRequestQueue =
            // Volley.newRequestQueue(getApplicationContext());
        }

        return mVolleyRequestQueue;
    }

    public String getContactName(Context context, String phoneNo) {
        //ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNo));
        Cursor cursor = context.getContentResolver().query(uri,
                new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = "";
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor
                    .getColumnIndex(PhoneLookup.DISPLAY_NAME));
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return contactName;
    } // getContactName

    public static ContactInfo getContactInfo(Context context,
                                             String sCountryCode, String sPhoneno, String sEmail) {
        //ContentResolver cr = context.getContentResolver();
        sCountryCode = sCountryCode.trim();
        sPhoneno = sPhoneno.trim();
        sEmail = sEmail.trim();
        String contactName = "";
        Bitmap photo = null;
        boolean noRows = true;
        boolean isNameFound = true;
        Uri uri;
        Cursor cursor = null;
        if (TextUtils.isEmpty(sPhoneno)) {
            return null;
        }
        uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(sPhoneno.trim()));


        // Search first with Phone Lookup
        try {

            cursor = context.getContentResolver().query(uri, new String[]{PhoneLookup.DISPLAY_NAME,
                    PhoneLookup._ID}, null, null, null);

            if (cursor == null) {
                isNameFound = false;
            }
            if (cursor.getCount() <= 0) {
                isNameFound = false;
            }

            if (!isNameFound) {
                uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                        Uri.encode(sCountryCode.trim() + sPhoneno.trim()));
                cursor = context.getContentResolver().query(uri, new String[]{PhoneLookup.DISPLAY_NAME,
                        PhoneLookup._ID}, null, null, null);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                noRows = false;
                if (cursor.moveToFirst()) {
                    contactName = cursor.getString(cursor
                            .getColumnIndex(PhoneLookup.DISPLAY_NAME));
                    long id = cursor.getLong(cursor
                            .getColumnIndexOrThrow(PhoneLookup._ID));
                    photo = loadContactPhoto(context, id);

                }
            }

            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        // If Phone lookup does not work search with Email
        Cursor cursorEmail = null;
        try {
            if (noRows) {
                Uri uriEmail = Uri.withAppendedPath(CommonDataKinds.Email.CONTENT_FILTER_URI,
                        Uri.encode(sPhoneno.trim()));
                cursorEmail = context.getContentResolver().query(uriEmail, new String[]{
                        CommonDataKinds.Email.DISPLAY_NAME, CommonDataKinds.Email._ID}, null, null, null);

                if (cursorEmail.getCount() <= 0 || cursorEmail == null) {
                    uriEmail = Uri.withAppendedPath(CommonDataKinds.Email.CONTENT_FILTER_URI,
                            Uri.encode(sEmail.trim()));
                    cursorEmail = context.getContentResolver().query(uriEmail, new String[]{
                            CommonDataKinds.Email.DISPLAY_NAME, CommonDataKinds.Email._ID}, null, null, null);
                }
                if (cursorEmail != null) {
                    if (cursorEmail.moveToFirst()) {
                        contactName = cursorEmail.getString(cursorEmail
                                .getColumnIndex(CommonDataKinds.Email.DISPLAY_NAME));
                        long id = cursorEmail.getLong(cursorEmail
                                .getColumnIndexOrThrow(CommonDataKinds.Email._ID));
                        photo = loadContactPhoto(context, id);
                    }
                }
                cursorEmail.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            cursor.close();
            cursorEmail.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        if (TextUtils.isEmpty(contactName)) {
            return null;
        } else {
            return new ContactInfo(contactName, photo);
        }
    } // getContactName

    public static Bitmap loadContactPhoto(Context context, long id) {
        try {
            //ContentResolver cr = context.getContentResolver();
            Uri uri = ContentUris.withAppendedId(
                    ContactsContract.Contacts.CONTENT_URI, id);
            InputStream input;
            input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), uri);
            if (input == null) {
                return null;
            }
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, String> getMinMobileParams(int iAppUserId, String sEmail, Map<String, String> params,
                                                  String url) {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        params.put("appuserid", Integer.toString(iAppUserId));
        params.put("email", sEmail);
        params.put("clientdatetime", df.format(cal.getTime()));
        String delim = "";
        StringBuilder getParams = new StringBuilder();
        for (Entry<String, String> entry : params.entrySet()) {
            getParams.append(delim + entry.getKey() + "=" + entry.getValue());
            delim = "&";

        }
        try {
            url = url + "?" + getParams.toString() + "&verbose=Y";
            System.out.println("getMinMobileParams url:- " + url);
        } catch (Exception e) {
            Log.e(TAG, e.getStackTrace().toString());
        }
        return checkParams(params);
    }

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
            }
        });

    }

    public String getDistance(float dist) {
        String sDistance = "";
        if (UnitLocale.getDefault() == UnitLocale.Imperial) {
            sDistance = String.format("%.2f", dist / 1600.0);
        } else {
            sDistance = String.format("%.2f", dist / 1000.0);
        }
        return sDistance;
    }

    public void turnGPSOn(final Activity activity, GoogleApiClient googleApiClient) {

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        //**************************
        builder.setAlwaysShow(true); //this is the key ingredient
        //**************************

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    activity, REQUEST_LOCATION);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });

    }

    public void turnGPSOn1(final Activity activity, GoogleApiClient googleApiClient) {

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        //**************************
        builder.setAlwaysShow(true); //this is the key ingredient
        //**************************

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            if (showGPSDialog)
                                status.startResolutionForResult(
                                        activity, REQUEST_LOCATION);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });

    }

    public ArrayList<CTrip> readRecentTrip(Context context) {
        ArrayList<CTrip> aoRecentTrips = new ArrayList<CTrip>();
        String sRecentTrips = CGlobals_lib.getInstance().getSharedPreferences(context)
                .getString(Constants_lib.PREF_RECENT_TRIP, "");
        System.out.println("sRecentTrips:-  " + sRecentTrips);
        if (TextUtils.isEmpty(sRecentTrips))
            return aoRecentTrips;

        try {
            JSONArray jsArray = new JSONArray(sRecentTrips);

            for (int x = 0; x < jsArray.length(); x++) {
                JSONObject oTrip = (JSONObject) jsArray.get(x);

                aoRecentTrips.add(new CTrip(oTrip));

            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return aoRecentTrips;

    } // ReadRecentAddresses

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

    public static int haveNetworkConnection(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        boolean haveConnected = false;
        ConnectivityManager cm = null;
        try {
            cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] netInfo = cm.getAllNetworkInfo();
            for (NetworkInfo ni : netInfo) {
                if (ni.isConnected())
                    haveConnected = true;
                if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                    if (ni.isConnected())
                        haveConnectedWifi = true;
                if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                    if (ni.isConnected())
                        haveConnectedMobile = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "CGloblas_lib_ss in LocationService " + e.toString());
        }


        if (haveConnectedWifi)
            return HAVEWIFI;
        if (haveConnectedMobile)
            return HAVEGPRS;
        if (haveConnected)
            return HAVECONNECTIONOTHER;
        return 0;
    }

} // CGlobals
