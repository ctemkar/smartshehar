package com.smartshehar.cabe;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Constants_lib_ss;
import lib.app.util.SSLog_SS;
import lib.app.util.UserInfo;

@SuppressWarnings("deprecation")
public class CGlobals_CabE {
    public static final String TAG = "CGlobals: ";
    private static CGlobals_CabE instance;
    public boolean isNewNotification = false;

    // Restrict the constructor from being instantiated
    private CGlobals_CabE() {
        Log.d(TAG, "constructor CGlobals_CabE");
    }

    public static synchronized CGlobals_CabE getInstance() {
        if (instance == null) {
            instance = new CGlobals_CabE();
        }
        return instance;
    }

    public boolean isRatingNotSubmit;
    public static boolean mbIsAdmin;
    private Location mCurrentLocation;
    public boolean bStopsRead;
    public Stack<Integer> stackActivity;
    protected double mdTravelDistance;
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
    Map<String, String> mMobileParams;
    public static boolean inWalkWithMe = false;
    public static String walkWithMeMode = "";
    public static final String PREFTRAVELDIST = "traveldistance";
    public static String PREFINWALKWITHME = "inwalkwithme";
    public static String PREFINWALKWITHME_EMAILS = "";
    public static int miTravelDistance;
    public static final int HAVEGPRS = 1;
    public static final int HAVEWIFI = 2;
    public static final int HAVECONNECTIONOTHER = 2;
    public static final String PREFS_NAME = "CBAppPrefsFile";
    public static final int REQUEST_CHECK_SETTINGS = 1022;

    public void init(Context act) {
        msPhoneNo = MyApplication_CabE.getInstance().getPersistentPreference()
                .getString(Constants_CabE.PREF_PHONENO, "");
        msCountryCode = MyApplication_CabE.getInstance().getPersistentPreference()
                .getString(Constants_CabE.PREF_COUNTRY_CODE, "");
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
            Log.e(TAG, "init: " + e);
        }
        msProduct = Build.PRODUCT;
        msManufacturer = Build.MANUFACTURER;
        mbAppInited = true;
        mPackageInfo = getPackageInfo(act);
        stackActivity = new Stack<>();
        AccountManager manager = (AccountManager) act
                .getSystemService(Context.ACCOUNT_SERVICE);
        if (ActivityCompat.checkSelfPermission(act, android.Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Account[] list = manager.getAccounts();
        msGmail = null;

        for (Account account : list) {
            if (account.type.equalsIgnoreCase("com.google")) {
                msGmail = account.name;
                break;
            }
        }
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(act)
                .putString(Constants_lib_ss.KEY_PREF_EMAIL, msGmail);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(act).commit();
        mbIsAdmin = isAdmin(msGmail);
        msAppVersionName = mPackageInfo.versionName;
        miAppVersionCode = mPackageInfo.versionCode;
        String currentVersion = MyApplication_CabE.getInstance()
                .getPersistentPreference().getString("dbVersionInfo", "-1");
        if (!msAppVersionName.equals(currentVersion)) {

            MyApplication_CabE.getInstance().getPersistentPreferenceEditor()
                    .putString("dbVersionInfo", msAppVersionName);
            MyApplication_CabE.getInstance().getPersistentPreferenceEditor().commit();

            MyApplication_CabE.getInstance().getPersistentPreferenceEditor()
                    .putString("dbVersionInfo", currentVersion);

            MyApplication_CabE.getInstance().getPersistentPreferenceEditor()
                    .commit();

        }

        mCurrentVersion = MyApplication_CabE.getInstance().getPersistentPreference()
                .getString("dbVersionInfo", "-1");
        mUserInfo = new UserInfo(act,
                act.getString(R.string.app_label));
        // Is this the first use after download? then send to server as first
        // use
      /*  if (!currentVersion.equals(mPackageInfo.versionCode)) { // First Use
        }
*/
        readPreferences();
        mMobileParams = new HashMap<>();
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
        boolean isSignificantlyNewer = timeDelta > Constants_CabE.TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -Constants_CabE.TWO_MINUTES;
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
        if (!isProviderSupported()) {
            return null;
        }
        if (context != null) {
            LocationManager locationManager = (LocationManager) context
                    .getApplicationContext().getSystemService(
                            Context.LOCATION_SERVICE);
            try {
                if (locationManager.isProviderEnabled(provider)) {
                    if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    boolean isProviderSupported() {
        return true;
    }

    public void setMyLocation(Location location, boolean bForceLocation) {
        if (location == null)
            return;
        if (bForceLocation) {
            mCurrentLocation = CGlobals_lib_ss.isBetterLocation(location, mCurrentLocation) ? location
                    : mCurrentLocation;
        } else {
            if (isBetterLocation(location, mCurrentLocation)) {
                mCurrentLocation = CGlobals_lib_ss.isBetterLocation(location, mCurrentLocation) ? location
                        : mCurrentLocation;
            }
        }
        final String jsonLocation = locationToJsonString(mCurrentLocation);
        new Thread(new Runnable() {
            public void run() {
                MyApplication_CabE
                        .getInstance()
                        .getPersistentPreferenceEditor()
                        .putString(Constants_CabE.PREF_MY_LOCATION,
                                jsonLocation);
                MyApplication_CabE.getInstance().getPersistentPreferenceEditor()
                        .commit();
            }
        }).start();
    }

    public Location getMyLocation(Context context) {
        Location location = getBestLocation(context);
        mCurrentLocation = CGlobals_lib_ss.isBetterLocation(location, mCurrentLocation) ? location
                : mCurrentLocation;
        if (mCurrentLocation == null) {
            String jsonLocation = MyApplication_CabE.getInstance()
                    .getPersistentPreference()
                    .getString(Constants_CabE.PREF_MY_LOCATION, "");
            if (!TextUtils.isEmpty(jsonLocation)) {
                try {
                    mCurrentLocation = locationFromJsonString(jsonLocation);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "getMyLocation: ", e, context);
                }
            }
        }

        if (mCurrentLocation != null) {
            setMyLocation(mCurrentLocation, false);
        }
        return mCurrentLocation;
    }

   /* public void setFromAddr(CAddress addr) {
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
    }*/

    public Map<String, String> checkParams(Map<String, String> map) {
        for (Entry<String, String> pairs : map.entrySet()) {
            if (pairs.getValue() == null || TextUtils.isEmpty(pairs.getValue())) {
                map.put(pairs.getKey(), "");
                Log.d(TAG, pairs.getKey());
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
            loc = new Location(jo.getString(CGlobals_lib_ss.PROVIDER));
            loc.setAccuracy((float) jo.getDouble(CGlobals_lib_ss.ACCURACY));
            loc.setLatitude(jo.getDouble(CGlobals_lib_ss.LATITUDE));
            loc.setLongitude(jo.getDouble(CGlobals_lib_ss.LONGITUDE));
            loc.setSpeed((float) jo.getDouble(CGlobals_lib_ss.SPEED));
        } catch (Exception je) {
            Log.e(TAG, "locationFromJsonString" + je);
        }
        return loc;
    }

    // Show one time help information
   /* public void gotIt(Context context, final String sPref, String sHelp1, String sHelp2) {
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
                MyApplication_CabE.getInstance().getPersistentPreferenceEditor().putBoolean(sPref, true);
            }
        });
    }*/

    /*public CAddress getHomeAddress(Context context) {
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
    }*/

    /*public CAddress getWorkAddress(Context context) {

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
    }*/

    public static boolean isNullNotDefined(JSONObject jo, String jkey) {
        return !jo.has(jkey) || jo.isNull(jkey);
    }

    public static int haveNetworkConnection(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        boolean haveConnected = false;
        ConnectivityManager cm;
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

    public void settingsrequest(final Activity activity, GoogleApiClient googleApiClient) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
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
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
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

} // CGlobals

