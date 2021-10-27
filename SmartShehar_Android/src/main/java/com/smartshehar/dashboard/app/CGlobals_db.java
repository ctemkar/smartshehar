package com.smartshehar.dashboard.app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
import lib.app.util.UserInfo;


public class CGlobals_db {
    public static String mAddress = "";
    protected static Activity mActivity;
    public static final String TAG = "CGlobals_db: ";
    private static CGlobals_db instance;
    public static RequestQueue mVolleyRequestQueue;
    public static String msGmail;
    public static boolean mbRegistered;
    public Location mCurrentLocation, mPreviousLocation = null;
    public int miPreviousDistance;
    public static long lTotalTripms;

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
    public PolylineOptions mDirectionsPolyline;
    //saftey shield//
    public static LocalBroadcastManager mAcceptTripBroadcaster;
    public static final String PREFTRAVELDIST = "traveldistance";
    public static String PREFINWALKWITHME = "inwalkwithme";
    public static String PREFINWALKWITHME_EMAILS = "";
    public static boolean inWalkWithMe = false;
    public static String walkWithMeMode = "";
    public static DBHelperAutoTaxi mDBHelper;
    public DBHelperLocalAutoTaxi mDBHelperLocalAutoTaxi;
    public static final String DEFAULT_CITY = "MUMBAI";
    static String msCity;
    public static final String PREF_CITY = "city";
    public static final String PREFS_NAME = "ATFAppPrefsFile";
    public static final String PREFREGISTERED = "isRegistered";
    public static boolean bRegistered;
    public CallHome mCH;
    public static final int HAVEGPRS = 1;
    public static final int HAVEWIFI = 2;
    public static CTrip moLastTrip;
    public static String msVehicleType;
    public static final String PREF_VEHICLE = "veh";
    public static boolean mbInTrip, mbStartTripPressed;
    public static boolean mbGPSFix;
    public static int miTravelDistance;
    public static int miWaitTimeSecs = 0;    // Waiting time
    public static Location moLocStart;
    public static Location moLocDest;
    public int miDirectDistance;
    public static int miTripTime = 0;
    public static long mlTripTimeMs = 0L;
    public static long mlWaitTimeMs = 0L;    // Waiting time
    public static final String PREFINTRIP = "intrip";
    public static final String PREF_START_PRESSED = "startpressed";
    public static final String PREF_DIRECT_DIST = "directdistance";
    public static final String PREF_TRAVEL_DISTANCE = "traveldistance";
    public static final String PREF_WAIT_TIME = "waittime";
    public static final String PREF_TRIP_TIME = "triptime";
    public static final String PREF_SPEED = "speed";
    public static Time moTripStartTime;
    public static Time moTripDestTime;
    public boolean mbMeterShown;
    public static long mlStartTimeMs;
    public static final String CITY_MUMBAI = "MUMBAI";
    public static final int METERSTARTRS = 1;
    public static final int METERSTARTPAISE = 0;
    public static boolean showGPSDialog = true;
    public static Location mLocStart;
    public static Location mLocDest;
    public static Time mTripStartTime;
    public static int mTravelDistance;
    public static int mWaitTimeSecs = 0;
    public static int mDirectDistance;
    public static int mTripTime = 0;
    public static double mEstimatedTotalDayFare = 0;
    public static double mEstimatedToatalNightFare = 0;
    public static double mdNightFare = 0;
    public static boolean showDialogonce = false;

    //train app//
    public String msShowBusStop;
    public static final int REQUEST_CHECK_SETTINGS = 1001;

    private CGlobals_db(Context context) {
//        this.context = context;
        msGmail = null;
        mLocStart = getMyLocation(context);

        try {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
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


    public static synchronized CGlobals_db getInstance(Context context) {
        if (instance == null) {
            instance = new CGlobals_db(context);
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
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
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
        params.put("version", CGlobals_db.msAppVersionName);
        params.put("android_release_version", msAndroidReleaseVersion);
        params.put("versioncode", String.valueOf(miAppVersionCode));
        params.put("android_sdk_version", Integer.toString(miAndroidSdkVersion));
        params.put("imei", CGlobals_db.mIMEI);
        params.put("email", CGlobals_db.msGmail);
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

    public Map<String, String> getBasicMobileParamsShort(Map<String, String> params,
                                                         String url, Context context) {

        init(context);
        Location myLocation = getBestLocation(context);
        SimpleDateFormat df = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT,
                Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        params.put("i", Integer.toString(CGlobals_lib_ss.getInstance().getPersistentPreference(context).getInt(Constants_lib_ss.PREF_APPUSERID, -1)));
        params.put("e", msGmail);
        params.put("ct", df.format(cal.getTime()));

        if (myLocation != null) {
            String address = getAddress(myLocation, context);
            Date locationDateTime = new Date(myLocation.getTime());
            params.put("p", myLocation.getProvider());
            params.put("l", String.format("%.9f", myLocation.getLatitude()));
            params.put("o", String.format("%.9f", myLocation.getLongitude()));
            params.put("a", Double.toString(myLocation.getAccuracy()));
            params.put("al", Double.toString(myLocation.getAltitude()));
            params.put("b", Double.toString(myLocation.getBearing()));
            params.put("s", Double.toString(myLocation.getSpeed()));
            params.put("lt", df.format(locationDateTime));
            params.put("address", address);

        }
        params.put("app", mAppNameCode);
        params.put("module", "SAR");
        params.put("version", CGlobals_db.msAppVersionName);
        params.put("android_release_version", msAndroidReleaseVersion);
        params.put("versioncode", String.valueOf(miAppVersionCode));
        params.put("android_sdk_version", Integer.toString(miAndroidSdkVersion));
        params.put("imei", CGlobals_db.mIMEI);
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
                SSLog.e(TAG, "checkConnected ", e);
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

    // Show one time help information
    public void gotIt(final Context context, final String sPref, boolean alwaysShow, String sHelp1, String sHelp2) {
        try {
            if (!alwaysShow) {
                boolean showDialog = CGlobals_lib_ss.getInstance().getPersistentPreference(context).getBoolean(sPref, false);
                if (showDialog) {
                    return;
                }
            }
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
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(context).putBoolean(sPref, true);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(context).commit();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    SSLog.e(TAG,
                            "getMyLocation: Could not convert jsonLocation to location object",
                            e);
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
            loc = new Location(jo.getString( CGlobals_lib_ss.getInstance().));
            loc.setAccuracy((float) jo.getDouble("accuracy"));
            loc.setLatitude(jo.getDouble("latitude"));
            loc.setLongitude(jo.getDouble("longitude"));
            loc.setSpeed((float) jo.getDouble("speed"));
        } catch (JSONException je) {
            SSLog.e(TAG, "locationFromJsonString", je);
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
            SSLog.e(TAG, "setMyLocation: ", e);
        }
    }

    private String locationToJsonString(Location loc) {
        Log.d(TAG, "loc " + loc);
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

        try {
            msPhoneNo = CGlobals_lib_ss.getInstance().getPersistentPreference(context)
                    .getString(Constants_dp.PREF_PHONENO, "");
            msCountryCode = CGlobals_lib_ss.getInstance().getPersistentPreference(context)
                    .getString(Constants_dp.PREF_COUNTRY_CODE, "");
            msCity = CFareParams.DEFAULTCITY;
            mAppNameCode = context.getString(R.string.appNameShort);
            moLastTrip = new CTrip();
            msVehicleType = CFareParams.DEFAULT_VEHICLE;
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
            moLocStart = getMyLocation(context);
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
                    context.getString(R.string.app_label));
            // Is this the first use after download? then send to server as first
            // use
            if (!currentVersion.equals(mPackageInfo.versionCode)) { // First Use
                Log.d(TAG, "No package info");
            }

            readPreferences(context);
            mMobileParams = new HashMap<>();

            // Is this the first use after download? then send to server as first use
            if (mCH == null) {
                initdb(context);
                mCH = new CallHome(context, getMyLocation(context),
                        mPackageInfo, context.getString(R.string.appNameShort),
                        context.getString(R.string.appCode), mUserInfo);
            }
            if (currentVersion.equals(mPackageInfo.versionCode)) { // First Use
                mCH.userPing("SSD", context.getString(R.string.atFirstUse));
            }
        } catch (Exception e) {
            e.printStackTrace();
            SSLog.e(TAG,"init: ",e.toString());
        }
    }

    public void init(Activity activity, String sVeh) {
        mActivity = activity;
        miTravelDistance = 0;
        msVehicleType = CFareParams.DEFAULT_VEHICLE;
        moLastTrip = new CTrip();
        mbAppInited = false;
        msCity = CFareParams.DEFAULTCITY;
        moTripStartTime = new Time();
        moTripDestTime = new Time();
        mLocStart = getMyLocation(mActivity);
        moLocStart = getMyLocation(mActivity); ////////////
        fndStart = "";
        miTravelDistance = 0;
        setVehicle(sVeh, mActivity);
        mAcceptTripBroadcaster = LocalBroadcastManager.getInstance(activity);
        mbGPSFix = false;
/*		if (mbAppInited)
            return;
*/
        mbAppInited = true;
        mFakeLocation = new Location("fake");    // Dadar - Tilak Bridge
        double lat = 19.02078;
        double lon = 72.843168;
        mFakeLocation.setLatitude(lat);
        mFakeLocation.setLongitude(lon);


        TelephonyManager telephonyManager = (TelephonyManager) mActivity.getSystemService(Context.TELEPHONY_SERVICE);
        mAppNameCode = mActivity.getString(R.string.appNameShort);
        mIMEI = telephonyManager.getDeviceId();
        msCarrier = telephonyManager.getNetworkOperator();
        mLine1Number = telephonyManager.getLine1Number();
        mLine1Number = telephonyManager.getSubscriberId();
        msProduct = Build.PRODUCT;
        msManufacturer = Build.MANUFACTURER;


        mPackageInfo = getPackageInfo(mActivity);

        AccountManager manager = (AccountManager) mActivity.getSystemService(Context.ACCOUNT_SERVICE);
        Account[] list = manager.getAccounts();
        msGmail = null;

        for (Account account : list) {
            if (account.type.equalsIgnoreCase("com.google")) {
                msGmail = account.name;
                break;
            }
        }
        mCurrentVersion = mPackageInfo.versionName + mPackageInfo.versionCode;
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
//        String currentVersion = prefs.getString("dbVersionInfo", "-1");
        mUserInfo = new UserInfo(activity, activity.getString(R.string.appTitle));
        // Is this the first use after download? then send to server as first use
        if (mCH == null) {
            initdb(activity);
            mCH = new CallHome(activity, getMyLocation(mActivity),
                    mPackageInfo, activity.getString(R.string.appNameShort),
                    activity.getString(R.string.appCode), mUserInfo);
        }
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


    public static void showInformationalDialog(Context context, String msg) {
        try {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setTitle(context.getApplicationContext().
                    getString(R.string.appTitle));
            alertDialogBuilder.setMessage(msg);
            alertDialogBuilder.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int arg1) {
                            showDialogonce = false;
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setCancelable(false);
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            SSLog.e(TAG,"showInformationalDialog ",e);
        }

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
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void alertToTurnOnGps(Context context) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage("Start your GPS or Setup Network location to get your nearest station");
        alertDialogBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        showGPSDialog = true;
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    public static void setCity(String city, Context context) {
        msCity = city;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_CITY, city);
        editor.apply();
    }

    public static String getCity(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        msCity = prefs.getString(PREF_CITY, DEFAULT_CITY);
        return msCity;
    }

    public void setVehicle(String sVeh, Context context) {
        msVehicleType = sVeh;
        SharedPreferences pref = context.getSharedPreferences(CGlobals_db.PREFS_NAME, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PREF_VEHICLE, sVeh);
        editor.apply();
    }

    boolean initdb(Context context) {
        try {
            CGlobals_db.mDBHelper = new DBHelperAutoTaxi(context);
            CGlobals_db.mDBHelper.openDataBase(mPackageInfo, context);
            this.mDBHelperLocalAutoTaxi =
                    new DBHelperLocalAutoTaxi(context.getApplicationContext(), mPackageInfo, mAppNameCode, mUserInfo);
            this.mDBHelperLocalAutoTaxi.openDataBase(mPackageInfo, context, this);
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            CGlobals_db.msCity = settings.getString("city", DEFAULT_CITY);

        } catch (SQLException e) {
            SSLog.e(this.toString(), ".OnCreate: ", e.getMessage());
            return false;
        }
        return true;
    }

 /*   public void cleanUp() {
        if (mDB != null)
            mDB.close();

    }*/

    public static String showDistance(double di) {
        String sDist;
        if (di == -1)
            sDist = " N/A";
        else if (di == 0)
            sDist = "";
        else if (di >= 1000)
            sDist = Double.toString((int) di / 1000.00) + " km";
        else
            sDist = Integer.toString((int) di) + " m";
        return sDist;
    }

    public static String ConvertToHMS(int ms) {
        int sec = (ms / 1000) % 60;
        int min = (ms / (1000 * 60)) % 60;
        int hr = (ms / (1000 * 60 * 60)) % 24;
        String str = "-";
        if (hr > 0)
            str = String.format(Locale.UK, "%d hr %2d  m %2d s", hr, min, sec);
        else if (min > 0)
            str = String.format(Locale.ENGLISH, "%2d m %2d s", min, sec);
        else if (sec > 0)
            str = String.format(Locale.ENGLISH, "%2d s", sec);
        return str;
    }

    // Read App global preferences_ss
    public void readPreferences(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        bRegistered = settings.getBoolean(PREFREGISTERED, false);

    }

    // Save App global preferences_ss
  /*  public void savePreferences(Context context) {
        SharedPreferences settings = context.getSharedPreferences(CGlobals_db.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.commit();

    }*/

  /*  public boolean haveWiFi(Context context) {
        if (haveNetworkConnection(context) == HAVEWIFI)
            return true;
        return false;
    }*/

  /*  public boolean haveGPRS(Context context) {
        if (haveNetworkConnection(context) == HAVEGPRS)
            return true;
        return false;
    }*/

    public static int haveNetworkConnection(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        if (haveConnectedWifi)
            return HAVEWIFI;
        if (haveConnectedMobile)
            return HAVEGPRS;
        return 0;
    }

/*    public void showGPSDisabledAlertToUser(final Context context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder
                .setMessage(R.string.gps_enable)
                .setCancelable(false)
                .setPositiveButton(R.string.no_gps_message,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                context.startActivity(intent);
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
    }*/

    public static String getAddress(double latitude, double longitude, Context context) {
        StringBuilder result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                result.append(address.getAddressLine(0));
                if (!TextUtils.isEmpty(address.getAddressLine(1))) {
                    result.append("\n").append(address.getAddressLine(1));
                }
//                result.append(address.getSubLocality()).append("\n");
//                result.append(address.getThoroughfare()).append("\n");

//                result.append(address.getLocality()).append("\n");
//                result.append(address.getCountryName());
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }
        return result.toString();
    }


}
