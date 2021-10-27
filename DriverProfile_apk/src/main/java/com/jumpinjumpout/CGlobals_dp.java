package com.jumpinjumpout;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.gson.Gson;
import com.jumpinjumpout.www.driverprofile.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.CallHome;
import lib.app.util.Constants_lib_ss;
import lib.app.util.DBHelperLocal;
import lib.app.util.UserInfo;

import static lib.app.util.CGlobals_lib_ss.ACCURACY;

//import com.jumpinjumpout.www.driverprofile.R;


public class CGlobals_dp {

    protected static Activity mActivity;
    public static final String TAG = "CGlobals_dp: ";
    private static CGlobals_dp instance;
    public static RequestQueue mVolleyRequestQueue;
    public static String msGmail;
    public static boolean mbRegistered;
    public Location mCurrentLocation;
    //    public static Context context;
    public static String msPhoneNo = null;
    public static String msCountryCode = null;
    public static boolean mbAppInited;
    public boolean bStopsRead;
    protected double mdTravelDistance;
    public static String mAppNameShort;
    public static Location mFakeLocation;
    public static String mAppNameCode;
    String msAndroidReleaseVersion;
    int miAndroidSdkVersion;
    public static String mIMEI;
    public String msCarrier;
    public static String mLine1Number;
    public String msProduct;
    public String msManufacturer;
    public PackageInfo mPackageInfo = null;
    public Stack<Integer> stackActivity;
    public static boolean mbIsAdmin;
    public static String msAppVersionName;
    public static String fndStart;
    public static int miAppVersionCode;
    String mCurrentVersion;
    public static UserInfo mUserInfo;
    Map<String, String> mMobileParams;

    public static final String PREFS_NAME = "ATFAppPrefsFile";
    public static final String PREFREGISTERED = "isRegistered";
    public static boolean bRegistered;
    public CallHome mCH;
    public static boolean mbGPSFix;
    public static Location moLocStart;


    public static Time moTripStartTime;
    public static Time moTripDestTime;
    public static boolean showGPSDialog = true;
    public static Location mLocStart;
    public DBHelper mDBHelper;
    public DBHelperLocal mDBHelperLocal;
    private SQLiteDatabase mDB = null;


    //train app//
    public String msShowBusStop;
    public static final int REQUEST_CHECK_SETTINGS = 1001;

    private CGlobals_dp(Context context) {
//        this.context = context;
        msGmail = null;
        mLocStart = getMyLocation(context);

        try {
            AccountManager manager = (AccountManager) context
                    .getSystemService(Context.ACCOUNT_SERVICE);
            Account[] list = manager.getAccounts();
            for (Account account : list) {
                if (account.type.equalsIgnoreCase("com.google")) {
                    msGmail = account.name;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static synchronized CGlobals_dp getInstance(Context context) {
        if (instance == null) {
            instance = new CGlobals_dp(context);
        }
        return instance;
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
        }

        return mVolleyRequestQueue;
    }

    public Location getBestLocation(Context context) {
        Location gpslocation = getLocationByProvider(
                LocationManager.GPS_PROVIDER, context);
        Location networkLocation = getLocationByProvider(
                LocationManager.NETWORK_PROVIDER, context);
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
        if (context != null) {
            LocationManager locationManager = (LocationManager) context
                    .getApplicationContext().getSystemService(
                            Context.LOCATION_SERVICE);
            try {
                if (locationManager.isProviderEnabled(provider)) {
                    if (checkLocationPermission(context)) {
                        location = locationManager.getLastKnownLocation(provider);
                    }
                }
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "Cannot acces Provider " + provider);
            }
        }
        return location;
    }

    public boolean checkLocationPermission(Context context) {

        if (Build.VERSION.SDK_INT < 23)
            return true;
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    int getGPSCheckMilliSecsFromPrefs() {
        return 1000;
    }

    public static String getAddress(Location location, Context context) {
        if (location == null)
            return "";
        try {
            Geocoder geo = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1);
            if (addresses.isEmpty()) {
                return "";
            } else {
                if (addresses.size() > 0) {
                    String address = addresses.get(0).getAddressLine(0);
                    address += ", " + addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName();
                    return address;
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // getFromLocation() may sometimes fail
        }
        return "";
    }

    public Map<String, String> getBasicMobileParams(Map<String, String> params,
                                                    String url, Context context) {

        init(context);
        Location myLocation = getBestLocation(context);
        SimpleDateFormat df = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT,
                Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        params.put("appuserid", Integer.toString(CGlobals_lib_ss.getInstance().getPersistentPreference(context).getInt(Constants_lib_ss.PREF_APPUSERID, -1)));
        params.put("email", msGmail);
        params.put("clientdatetime", df.format(cal.getTime()));

        if (myLocation != null) {
            String address = getAddress(myLocation, context);
            Date locationDateTime = new Date(myLocation.getTime());
            params.put("provider", myLocation.getProvider());
            params.put("lat", String.format("%.9f", myLocation.getLatitude()));
            params.put("lng", String.format("%.9f", myLocation.getLongitude()));
            params.put("accuracy", Double.toString(myLocation.getAccuracy()));
            params.put("altitude", Double.toString(myLocation.getAltitude()));
            params.put("bearing", Double.toString(myLocation.getBearing()));
            params.put("speed", Double.toString(myLocation.getSpeed()));
            params.put("locationdatetime", df.format(locationDateTime));
            params.put("address", address);

        }
        params.put("app", mAppNameCode);
        params.put("module", "SAR");
        params.put("version", CGlobals_dp.msAppVersionName);
        params.put("android_release_version", msAndroidReleaseVersion);
        params.put("versioncode", String.valueOf(miAppVersionCode));
        params.put("android_sdk_version", Integer.toString(miAndroidSdkVersion));
        params.put("imei", CGlobals_dp.mIMEI);
        params.put("email", CGlobals_dp.msGmail);
        params.put("carrier", msCarrier);
        params.put("product", msProduct);
        params.put("manufacturer", msManufacturer);

        String delim = "";
        StringBuilder getParams = new StringBuilder();
        for (Entry<String, String> entry : params.entrySet()) {
            getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
            delim = "&";

        }
        try {
            url = url + "?" + getParams.toString() + "&verbose=Y";
            Log.d(TAG, "URL IS  " + url);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return params;
    }
    public Map<String, String> checkParams(Map<String, String> map) {
        for (Entry<String, String> pairs : map.entrySet()) {
            if (pairs.getValue() == null) {
                map.put(pairs.getKey(), "");
                Log.d(TAG, pairs.getKey());
            }
        }
        return map;
    }

    public boolean checkConnected(Context context) {
        for (int i = 0; i < 2; i++) {

            try {
                if (isConnected(context)) {
                    return true;
                }
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
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


    public static String getVolleyError(Context context, VolleyError error) {

        if (error instanceof NetworkError) {
            return context.getString(R.string.retry_internet);
        } else if (error instanceof ServerError) {
            Log.d(TAG, error.toString());
        } else if (error instanceof AuthFailureError) {
            Log.d(TAG, error.toString());
        } else if (error instanceof ParseError) {
            Log.d(TAG, error.toString());
        } else if (error instanceof NoConnectionError) {
            Log.d(TAG, error.toString());
        } else if (error instanceof TimeoutError) {
            Log.d(TAG, error.toString());
        } else {
            return error.getMessage();
        }
        return "";
    }


    public Location getMyLocation(Context context) {
        Location location = getBestLocation(context);
        mCurrentLocation = CGlobals_lib_ss.getInstance().isBetterLocation(location, mCurrentLocation) ? location
                : mCurrentLocation;
        if (mCurrentLocation == null) {
            String jsonLocation = CGlobals_lib_ss.getInstance()
                    .getPersistentPreference(context)
                    .getString(Constants_dp.PREF_MY_LOCATION, "");
            if (!TextUtils.isEmpty(jsonLocation)) {
                try {
                    mCurrentLocation = locationFromJsonString(jsonLocation);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (mCurrentLocation != null) {
            setMyLocation(mCurrentLocation, false, context);
        }
        return mCurrentLocation;
    }

    ///
    private Location locationFromJsonString(String sJsonLoc) {
        Location loc = null;
        try {
            JSONObject jo = new JSONObject(sJsonLoc);
            loc = new Location(jo.getString(CGlobals_lib_ss.PROVIDER));
            loc.setAccuracy((float) jo.getDouble(CGlobals_lib_ss.ACCURACY));
            loc.setLatitude(jo.getDouble(CGlobals_lib_ss.LATITUDE));
            loc.setLongitude(jo.getDouble(CGlobals_lib_ss.LONGITUDE));
            loc.setSpeed((float) jo.getDouble(CGlobals_lib_ss.SPEED));
        } catch (JSONException je) {
            je.printStackTrace();
        }
        return loc;
    }

    public void setMyLocation(Location location, boolean bForceLocation, final Context context) {
        try {
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
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(context)
                            .putString(Constants_dp.PREF_MY_LOCATION, jsonLocation);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(context)
                            .commit();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
//            SSLog.e(TAG, "setMyLocation: ", e);
        }
    }

    private String locationToJsonString(Location loc) {
        Log.d(TAG, "loc " + loc);
        String location = new Gson().toJson(mCurrentLocation);

        return location;

    }

    public boolean isBetterLocation(Location location,
                                    Location currentBestLocation) {
        if (currentBestLocation == null || location == null) {
            // A new location is always better than no location
            return true;
        }
        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > Constants_dp.TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -Constants_dp.TWO_MINUTES;
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

    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public void init(Context context) {
        mActivity = (Activity) context;
        msPhoneNo = CGlobals_lib_ss.getInstance().getPersistentPreference(context)
                .getString(Constants_dp.PREF_PHONENO, "");
        msCountryCode = CGlobals_lib_ss.getInstance().getPersistentPreference(context)
                .getString(Constants_dp.PREF_COUNTRY_CODE, "");

        mAppNameCode = context.getString(R.string.appNameShort);


        mbAppInited = false;
        bStopsRead = false;
        mbGPSFix = false;
        mdTravelDistance = 0;
        moTripStartTime = new Time();
        moTripDestTime = new Time();
        mLocStart = getMyLocation(context);
        fndStart = "";
        msShowBusStop = "";
        mdTravelDistance = 0;
        moLocStart = getMyLocation(context); ///////////////
        readPreferences(context);
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
        msAndroidReleaseVersion = Build.VERSION.RELEASE; // e.g.
        // myVersion
        // := "1.6"
        miAndroidSdkVersion = Build.VERSION.SDK_INT; // e.g.
        // sdkVersion :=
        // 8;


        mIMEI = telephonyManager.getDeviceId();
        msCarrier = telephonyManager.getNetworkOperator();
        mLine1Number = telephonyManager.getLine1Number();
        mLine1Number = telephonyManager.getSubscriberId();
        msProduct = Build.PRODUCT;
        msManufacturer = Build.MANUFACTURER;
        mbAppInited = true;
        mPackageInfo = getPackageInfo(context);
        stackActivity = new Stack<>();
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
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
        } catch (Exception e) {
            e.printStackTrace();
        }


        mbIsAdmin = isAdmin(msGmail);
        msAppVersionName = mPackageInfo.versionName;
        miAppVersionCode = mPackageInfo.versionCode;
        String currentVersion = CGlobals_lib_ss.getInstance()
                .getPersistentPreference(context).getString("dbVersionInfo", "-1");
        if (!msAppVersionName.equals(currentVersion)) {

            CGlobals_lib_ss.getInstance()
                    .getPersistentPreferenceEditor(context)
                    .putString("dbVersionInfo", msAppVersionName);
            CGlobals_lib_ss.getInstance()
                    .getPersistentPreferenceEditor(context).commit();

            CGlobals_lib_ss.getInstance()
                    .getPersistentPreferenceEditor(context).putString("dbVersionInfo", currentVersion);

            CGlobals_lib_ss.getInstance()
                    .getPersistentPreferenceEditor(context).commit();

        }

        mCurrentVersion = CGlobals_lib_ss.getInstance()
                .getPersistentPreference(context).getString("dbVersionInfo", "-1");
        mUserInfo = new UserInfo(context,
                context.getString(R.string.appTitle));
        // Is this the first use after download? then send to server as first
        // use
        if (!currentVersion.equals(mPackageInfo.versionCode)) { // First Use
            Log.d(TAG, "No package info");
        }

        readPreferences(context);
        mMobileParams = new HashMap<>();

        // Is this the first use after download? then send to server as first use

        initdb(mActivity);


    }

    boolean initdb(Activity activity) {
        try {
            if (mDB == null) {

               /* this.dataBaseHandler = new DataBaseHandler(activity.getApplicationContext());
                this.dataBaseHandler.openDataBase(mPackageInfo, activity, this);
                mDB = dataBaseHandler.getReadableDatabase();*/
                mDBHelper = new DBHelper(activity.getApplicationContext());
            }


        } catch (SQLException e) {
            Log.e(this.toString(), ".OnCreate: " + e.getMessage());
            return false;
        }


        return true;
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

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {

                            if (showGPSDialog)
                                status.startResolutionForResult(
                                        activity, REQUEST_CHECK_SETTINGS);
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


    // Read App global preferences_ss
    public void readPreferences(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        bRegistered = settings.getBoolean(PREFREGISTERED, false);

    }

}
