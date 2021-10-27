package lib.app.util;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class CGlobals_lib_ss {
    private static CGlobals_lib_ss instance;
    public static String msAppVersionName;
    private String msAndroidReleaseVersion;
    int miAndroidSdkVersion;
    public static int miAppVersionCode;
    public static boolean mbRegistered;
    public static boolean mbGPSFix;
    public boolean isLocationServiceStarted;
    public static Location moLocStart, moLocDest;
    public int miTravelDistance;
    public static boolean inDriverTrip;
    public boolean isNewNotification = false;
    public static String msInTrip;

    // Restrict the constructor from being instantiated
    private CGlobals_lib_ss() {
    }

    public static synchronized CGlobals_lib_ss getInstance() {
        if (instance == null) {
            instance = new CGlobals_lib_ss();
        }
        return instance;
    }

    private SharedPreferences mPrefsVersionPersistent = null;
    SharedPreferences.Editor mEditorVersionPersistent = null;
    public static RequestQueue mVolleyRequestQueue;
    public static final String TAG = "SST: ";
    public static final String PREFS_NAME = "CBAppPrefsFile";
    /*
    public static final double MINLATLONDIFF = .01;
    public static final String SMINLATLONDIFF = ".1";
    public static final double MINWALKINGDIST = 5000; // stops within 1.5 km
    public static final String AD_UNIT_ID = "a14f0d5d22cbec6";

    public static final String ALLSPEED = "All Speeds";
    public static final String FASTABBR = "F";
    public static final String FAST = "Fast";
    public static final String DOUBLEFASTABBR = "DF";
    public static final String DOUBLEFAST = "Double Fast";
    public static final String SLOWABBR = "S";
    public static final String SLOW = "Slow";

    public static final String DIRECTIONCODEUP = "U";
    public static final String DIRECTIONCODEDN = "D";
    public static final String DIRECTIONUP = "Up";
    public static final String DIRECTIONDOWN = "Down";
    public static final int TRAINSDIRECTIONTOSHOW = 20;

    public static final String PREFREGISTERED = "isRegistered";
*/
    public static final String VERSION = Constants_lib_ss.VERSION;
    public static final String APPNAME = "SmartsheharApp";

    //        public static final String ALPHA = "/alpha";
//	public static final String ALPHA = "/apps";
    public static final String ALPHA = Constants_lib_ss.ALPHA;
    public static final String AUTHORITY = "smartshehar.com";
    public static final String SITE = "https://smartshehar.com";
    public static final String PHP_DIRECTORY = ALPHA + "/" + APPNAME + "/svr/" + VERSION + "/php/";
    public static final String PHP_PATH = SITE + PHP_DIRECTORY;

    // "http://smartshehar.com/apps/trainapp/megablock.html";

    //"http://smartshehar.com/alpha/trainapp/svr/v24/php/";

    // production
    public static final int HAVEGPRS = 1;
    public static final int HAVEWIFI = 2;
    public static final int HAVECONNECTIONOTHER = 2;

    /*
    protected static final int ROUTETAB = 1;
    protected static final int JOURNEYTAB = 2;
    protected static final int MAPTAB = 3;
    protected static final int MAXDISTANCEFROMSTATION = 5000;
    // 19.475655, 72.737732
    // 18.895243, 73.087234
    public static final double MINCITYLAT = 18.895243;
    public static final double MINCITYLON = 72.737732;
    public static final double MAXCITYLAT = 19.475655;
    public static final double MAXCITYLON = 73.087234;

    public static final double INVALIDLON = -999;

    public static final int REQUEST_CODE = 11;
*/

// Location variables
    public static final String PROVIDER = "mProvider";
    public static final String ALTITUDE = "mAltitude";
    public static final String ACCURACY= "mAccuracy";
    public static final String LATITUDE = "mLatitude";
    public static final String LONGITUDE = "mLongitude";
    public static final String SPEED = "mSpeed";


    public static final double INVALIDLAT = -999;
    // activity
    public static Location mCurrentLocation;
    public Location mOldLocation;

    public ProgressDialog mProgressDialog;

    public static boolean mbAppInited;

    protected static Context mContext;
    public PackageInfo mPackageInfo = null;
    public String msProduct;
    public String msManufacturer;
    public String msCarrier;

    public static String mIMEI;
    public static String msGmail = null;

    public static UserInfo mUserInfo;
    //    public ArrayList<MegaBlock> maoMegaBlock;
    public ArrayList<CAddress> maoAddress;
    public static String msPhoneNo = null;
    public static String msCountryCode = null;
    public int miAppUserId;
    public PolylineOptions mDirectionsPolyline;
    public static boolean showGPSDialog = true;
    final public static int REQUEST_LOCATION_LIB = 1001;

    /*
        class TrainsAtStn {
            String startStn;
            String destStn;
            String myStn;
            String fastOrSlow;
            int startTime, destTime, myTime;
            int reachesLastStnAt;

            public TrainsAtStn(String fs, int fst, String ms, int mst, String ls, int lst, String fos) {
                startStn = fs;
                destStn = ls;
                myStn = ms;
                fastOrSlow = fos;
                startTime = fst;
                destTime = lst;
                myTime = mst;
            }
        }
    */
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

            // mRequestQueue =
            // Volley.newRequestQueue(sInstance.getApplicationContext());

            // Start the queue
            // mRequestQueue =
            // Volley.newRequestQueue(getApplicationContext());
        }

        return mVolleyRequestQueue;
    }

    public void addVolleyRequest(StringRequest postRequest,
                                 boolean clearBeforeQuery, Context context) {
        try {
            postRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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
            //SSLog.e(TAG, "mRequestQueue may not be initialized - ", e);
        }

    }

    public void init(Context context) {
        mContext = context;
        mbAppInited = false;
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            AccountManager manager = (AccountManager) mContext.getSystemService(Context.ACCOUNT_SERVICE);
            Account[] list = manager.getAccounts();
            msGmail = null;
            for (Account account : list) {
                if (account.type.equalsIgnoreCase("com.google")) {
                    msGmail = account.name;
                    break;
                }
            }
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(context)
                    .putString(Constants_lib_ss.KEY_PREF_EMAIL, msGmail);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(context).commit();
        } catch (Exception e) {
            SSLog_SS.e(TAG, "init - ", e, context);
        }
        try {
            TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            mIMEI = telephonyManager.getDeviceId();
            msCarrier = telephonyManager.getNetworkOperator();
            msProduct = Build.PRODUCT;
            msManufacturer = Build.MANUFACTURER;
            mbAppInited = true;
            mPackageInfo = getPackageInfo();
            msAndroidReleaseVersion = Build.VERSION.RELEASE;
            miAndroidSdkVersion = Build.VERSION.SDK_INT;
            miAppVersionCode = mPackageInfo.versionCode;
            msAppVersionName = mPackageInfo.versionName;
        } catch (Exception e) {
            SSLog_SS.e(TAG, "init" + e.toString());
        }
    }

    private PackageInfo getPackageInfo() {
        PackageInfo pi = null;
        try {
            pi = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pi;
    }

    // Update the location
    public Location getMyLocation() {
        LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        Location loc = null; // = mFakeLocation;
        List<String> providers = lm.getAllProviders();
        for (String provider : providers) {
            if (checkLocationPermission(mContext)) {
                loc = lm.getLastKnownLocation(provider);
                if (loc != null) {
                    if (isBetterLocation(loc, mCurrentLocation)) {
                        mCurrentLocation = loc;
                        if (mOldLocation != null) {
                            float[] res = new float[1];
                            Location.distanceBetween(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                                    mOldLocation.getLatitude(),
                                    mOldLocation.getLongitude(), res);
                            if (res[0] > 200 && !mCurrentLocation.getProvider().equals("fake"))
                                mOldLocation = mCurrentLocation;
                        } else {
                            if (!mCurrentLocation.getProvider().equals("fake"))
                                mOldLocation = mCurrentLocation;
                        }
                    }
                }
            }
        }

        if (loc == null) {
            if (mCurrentLocation != null)
                return mCurrentLocation;
        }
        return loc;

    }

    public boolean checkLocationPermission(Context context) {
//        try {
        return Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
    }

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    /**
     * Checks whether two providers are the same
     */
    private static boolean isBetterProvider(String provider1, String provider2) {

        return providerPrioriity(provider1) >= providerPrioriity(provider2);
    }

    private static int providerPrioriity(String provider) {
        if (provider.equalsIgnoreCase("gps"))
            return 1;
        else if (provider.equalsIgnoreCase("fused"))
            return 2;
        else if (provider.equalsIgnoreCase("network"))
            return 3;
        else return 4;
    }
    /*
     * public boolean inCityBoundingRect(StopInfo si) { ; boolean bInLat =
	 * false, bInLon = false; if(si.lat >= MINCITYLAT && si.lat <= MAXCITYLAT)
	 * bInLat = true; if(si.lon > MINCITYLON && si.lon < MAXCITYLON) bInLon =
	 * true; return bInLat && bInLon; }
	 */

    public static int haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        boolean haveConnected = false;
        ConnectivityManager cm;
        try {
            cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
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

    // Read App global preferences
    public void readPreferences() {
//		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
//		bRegistered = settings.getBoolean(PREFREGISTERED, false);
    }

    /**
     * Determines whether one Location reading is better than the current
     * Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new
     *                            one
     */
    public static boolean isBetterLocation(Location location,
                                           Location currentBestLocation) {
        if (currentBestLocation == null || location == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
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
        boolean isFromBetterProvider = isBetterProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and
        // accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate
                && isFromBetterProvider) {
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


    public ArrayList<CAddress> readRecentAddresses(Context context) {

        maoAddress = new ArrayList<>();

        String sAddress = getPersistentPreference(context).getString(
                Constants_lib_ss.PREF_RECENT_ADDRESSES, "");
        System.out.println("sAddress:-  " + sAddress);
        if (TextUtils.isEmpty(sAddress))
            return maoAddress;

        try {
            JSONArray jsArray = new JSONArray(sAddress);

            for (int x = 0; x < jsArray.length(); x++) {
                JSONObject oAddress = (JSONObject) jsArray.get(x);

                maoAddress.add(new CAddress(oAddress));

            }

        } catch (JSONException e) {

            //  SSLog.e(TAG, "readRecentAddresses - ", e);
            getPersistentPreferenceEditor(context).putString(
                    Constants_lib_ss.PREF_RECENT_ADDRESSES, "");
            getPersistentPreferenceEditor(context).commit();
        }

        return maoAddress;

    } // ReadRecentAddresses


    public void writeRecentAddresses(final Context context) {

        new Thread(new Runnable() {
            public void run() {
                if (maoAddress == null) {
                    return;
                }

                int iLen = maoAddress.size();
                if (iLen < 1)
                    return;
                JSONArray aJson = new JSONArray();
                for (int i = 0; i < iLen; i++) {
                    JSONObject oAddr = maoAddress.get(i).toJSon();
                    if (oAddr != null) {
                        aJson.put(maoAddress.get(i).toJSon());
                    }

                }
                if (aJson.length() > 0) {
                    getPersistentPreferenceEditor(context).putString(
                            Constants_lib_ss.PREF_RECENT_ADDRESSES,
                            aJson.toString());
                    getPersistentPreferenceEditor(context).commit();
                }
            }
        }).start();
    } // writeRecentAddressesString

    public SharedPreferences.Editor getPersistentPreferenceEditor(Context context) {
        if (mEditorVersionPersistent == null) {
            mEditorVersionPersistent = getPersistentPreference(context).edit();
        }
        return mEditorVersionPersistent;
    }

    public SharedPreferences getPersistentPreference(Context context) {
        if (mPrefsVersionPersistent == null) {
            mPrefsVersionPersistent = context.getApplicationContext()
                    .getSharedPreferences(
                            Constants_lib_ss.PREFS_VERSION_PERSISTENT,
                            Context.MODE_PRIVATE);
        }
        return mPrefsVersionPersistent;
    }
  /*  public SharedPreferences getSharedPreferences(Context mContext) {
        if (mPrefsVersionPersistent == null) {
            mPrefsVersionPersistent = mContext.getSharedPreferences(
                    Constants_lib_ss.PREFS_VERSION_PERSISTENT,
                    Context.MODE_PRIVATE);
        }
        return mPrefsVersionPersistent;
    }*/

  /*  public SharedPreferences.Editor getSharedPreferencesEditor(
            Context mContext) {
        if (mEditorVersionPersistent == null) {
            mEditorVersionPersistent = getSharedPreferences(mContext).edit();
        }
        return mEditorVersionPersistent;
    }*/

    public void addRecentAddress(CAddress oAddress) {
        if (oAddress == null) {
            return;
        }
        if (maoAddress == null)
            maoAddress = new ArrayList<>();
        int iLen = maoAddress.size();
        if (iLen > Constants_lib_ss.MAXADDRESS)
            maoAddress.remove(iLen - 1);
        // remove the same Station lower in the list
        iLen = maoAddress.size();
        CAddress sf;
        // boolean hasDupes = true;
        int i = 0;
        while (i < iLen) {
            sf = maoAddress.get(i);
            if (sf == null) {
                continue;
            }
            if (sf.getAddress().trim().equals(oAddress.getAddress().trim())) {
                maoAddress.remove(i);
                iLen = maoAddress.size();
            } else {
                i++;
            }
        }
        maoAddress.add(0, oAddress);

    } // addRecentAddress


    public Map<String, String> checkParams(Map<String, String> map) {
        for (Map.Entry<String, String> pairs : map.entrySet()) {
            if (pairs.getValue() == null) {
                map.put(pairs.getKey(), "");
                Log.d(TAG, pairs.getKey());
            }
        }
        return map;
    }

    public Map<String, String> getBasicMobileParamsShort(Map<String, String> params,
                                                         String url, Context context) {
        init(context);

        Location myLocation = null;
        try {
            myLocation = getMyLocation(context);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
//        myLocation = CGlobals_lib_ss.isBetterLocation(myLocation, mCurrentLocation) ? myLocation : mCurrentLocation;
        SimpleDateFormat df = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT,
                Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        params.put("i", Integer.toString(CGlobals_lib_ss.getInstance().getPersistentPreference(context).getInt(Constants_lib_ss.PREF_APPUSERID, -1)));
        params.put("e", msGmail);
        params.put("ct", df.format(cal.getTime()));

        if (myLocation != null) {
            //String address = getAddress(myLocation, context);
            Date locationDateTime = new Date(myLocation.getTime());
            params.put("p", myLocation.getProvider());
            params.put("l", String.format(Locale.getDefault(), "%.9f", myLocation.getLatitude()));
            params.put("o", String.format(Locale.getDefault(), "%.9f", myLocation.getLongitude()));
            params.put("a", Double.toString(myLocation.getAccuracy()));
            params.put("al", Double.toString(myLocation.getAltitude()));
            params.put("b", Double.toString(myLocation.getBearing()));
            params.put("s", Double.toString(myLocation.getSpeed()));
            params.put("lt", df.format(locationDateTime));
            //params.put("address", address);
        }
        String delim = "";
        StringBuilder getParams = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
            delim = "&";
        }
        try {
            url = url + "?" + getParams.toString();
            Log.d(TAG, "URL IS  " + url);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return params;
    }


    public Location getMyLocation(Context context) {
        Location location = getBestLocation(context);
        mCurrentLocation = CGlobals_lib_ss.isBetterLocation(location, mCurrentLocation) ? location
                : mCurrentLocation;
        /* Get saved location
        if (mCurrentLocation == null) {
            String jsonLocation = CGlobals_lib_ss.getInstance()
                    .getPersistentPreference(context)
                    .getString(Constants_lib_ss.PREF_MY_LOCATION, "");
            if (!TextUtils.isEmpty(jsonLocation)) try {
                mCurrentLocation = locationFromJsonString(jsonLocation);
            } catch (Exception e) {
                Log.e(TAG,
                        "getMyLocation: Could not convert jsonLocation to location object",
                        e);
            }
        }
*/
        if (mCurrentLocation != null) {
            setMyLocation(mCurrentLocation, false, context);
        }
        return mCurrentLocation;
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
        } catch (JSONException je) {
            Log.e(TAG, "locationFromJsonString", je);
        }
        return loc;
    }

    public Location getBestLocation(Context context) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void setMyLocation(Location location, boolean bForceLocation, final Context context) {
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
                            .putString(Constants_lib_ss.PREF_MY_LOCATION, jsonLocation);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(context)
                            .commit();
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, "setMyLocation: ", e);
        }
    }

    int getGPSCheckMilliSecsFromPrefs() {
        return 1000;
    }

    private static String locationToJsonString(Location loc) {
        Log.d(TAG, "loc " + loc);
        Log.d(TAG, "loc " + loc);
        return new Gson().toJson(loc);
    }

    public static String getPath() {
        return Constants_lib_ss.OFFENCE_IMAGES_URL;
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
                        try {
                            location = locationManager.getLastKnownLocation(provider);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "Cannot acces Provider " + provider);
            }
        }
        return location;
    }

    public Map<String, String> getAllMobileParams(Map<String, String> params,
                                                  String url, final String appNameCode, Context context) {
        init(context);
        Location myLocation = null;

        try {
            myLocation = getBestLocation(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SimpleDateFormat df = new SimpleDateFormat(
                Constants_lib_ss.STANDARD_DATE_FORMAT, Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        msPhoneNo = CGlobals_lib_ss.getInstance().getPersistentPreference(context)
                .getString(Constants_lib_ss.PREF_PHONENO, "");
        msCountryCode = CGlobals_lib_ss.getInstance().getPersistentPreference(context)
                .getString(Constants_lib_ss.PREF_COUNTRY_CODE, "");
        String regexStr = "^[0-9]*$";
        String phoneDigits = msPhoneNo.trim();
        params.put(
                "appuserid",
                Integer.toString(CGlobals_lib_ss.getInstance()
                        .getPersistentPreference(context)
                        .getInt(Constants_lib_ss.PREF_APPUSERID, -1)));
        if (!TextUtils.isEmpty(msPhoneNo)) {
            if (phoneDigits.length() > 0) {
                params.put("countrycode", msCountryCode.trim());
            }
            if (phoneDigits.length() == 10 && !phoneDigits.equals("-1") && phoneDigits.matches(regexStr)) {
                params.put("phoneno", phoneDigits);
            }
            params.put("clientdatetime", df.format(cal.getTime()));
        }
        if (myLocation != null) {
            Date locationDateTime = new Date(myLocation.getTime());
            params.put("provider", myLocation.getProvider());
            params.put("lat", String.format(Locale.getDefault(), "%.9f", myLocation.getLatitude()));
            params.put("lng", String.format(Locale.getDefault(), "%.9f", myLocation.getLongitude()));
            params.put("accuracy", Double.toString(myLocation.getAccuracy()));
            params.put("altitude", Double.toString(myLocation.getAltitude()));
            params.put("bearing", Double.toString(myLocation.getBearing()));
            params.put("speed", Double.toString(myLocation.getSpeed()));
            params.put("locationdatetime", df.format(locationDateTime));
            params.put("provider", myLocation.getProvider());
        }
        params.put("app", appNameCode);
        params.put("module", "SAR");
        params.put("version", CGlobals_lib_ss.msAppVersionName);
        params.put("android_release_version", msAndroidReleaseVersion);
        params.put("versioncode", String.valueOf(miAppVersionCode));
        params.put("android_sdk_version", Integer.toString(miAndroidSdkVersion));
        params.put("imei", CGlobals_lib_ss.mIMEI);
        params.put("email", CGlobals_lib_ss.msGmail);
        params.put("carrier", msCarrier);
        params.put("product", msProduct);
        params.put("manufacturer", msManufacturer);

        String delim = "";
        StringBuilder getParams = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
            delim = "&";

        }
        try {
            url = url + "?" + getParams.toString();
            System.out.println("url" + url);
        } catch (Exception e) {
            SSLog_SS.e(TAG, "getAllMobileParams - " + e);
        }
        return checkParams(params);
    } // getAllMobileParams

    public String getVolleyError(VolleyError error) {
        if (error instanceof NetworkError) {
            return "NetworkError: " + error.getMessage();
        } else if (error instanceof ServerError) {
            return "ServerError: " + error.getMessage();
        } else if (error instanceof AuthFailureError) {
            return "AuthFailureError: " + error.getMessage();
        } else if (error instanceof ParseError) {
            return "ParseError: " + error.getMessage();
        } /*else if (error instanceof NoConnectionError) {
            return "NoConnectionError: " + error.getMessage();
        }*/ else if (error instanceof TimeoutError) {
            return "TimeoutError: " + error.getMessage();
        } else {
            return error.getMessage();
        }
    }

    public int volleyError(VolleyError error) {
        if (error instanceof NetworkError) {
            return 1;
        } else if (error instanceof ServerError) {
            return 2;
        } else if (error instanceof AuthFailureError) {
            return 3;
        } else if (error instanceof ParseError) {
            return 4;
        } else if (error instanceof TimeoutError) {
            return 5;
        } else if (error instanceof NoConnectionError) {
            return 6;
        } else {
            return 0;
        }
    }

    public int countError1 = 1, countError2 = 1, countError3 = 1, countError4 = 1, countError5 = 1, countError6 = 1;

    public boolean checkerror(VolleyError error) {
        if (volleyError(error) == 1 && countError1 > 1) {
            return true;
        } else if (volleyError(error) == 2 && countError2 > 1) {
            return true;
        } else if (volleyError(error) == 3 && countError3 > 1) {
            return true;
        } else if (volleyError(error) == 4 && countError4 > 1) {
            return true;
        } else if (volleyError(error) == 5 && countError5 > 1) {
            return true;
        } else if (volleyError(error) == 6 && countError6 > 1) {
            return true;
        } else {
            if (volleyError(error) == 1) {
                countError1 = countError1 + 1;
            } else if (volleyError(error) == 2) {
                countError2 = countError2 + 1;
            } else if (volleyError(error) == 3) {
                countError3 = countError3 + 1;
            } else if (volleyError(error) == 4) {
                countError4 = countError4 + 1;
            } else if (volleyError(error) == 5) {
                countError5 = countError5 + 1;
            } else if (volleyError(error) == 6) {
                countError6 = countError6 + 1;
            }
            return false;
        }
    }


    public boolean checkConnected(Context context) {
        for (int i = 0; i < 2; i++) {
            try {
                if (isConnected(context)) {
                    return true;
                }
                Thread.sleep(Constants_lib_ss.INTERNET_CONNECTION_INTERVAL);
            } catch (Exception e) {
                SSLog_SS.e(TAG, "checkConnected ", e, context);
            }
        }
        return false;
    }

    private boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
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
                            if (showGPSDialog)
                                status.startResolutionForResult(
                                        activity, REQUEST_LOCATION_LIB);
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

    public Map<String, String> getMinMobileParams(Map<String, String> params,
                                                  String url, Context context) {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        params.put(
                "appuserid",
                Integer.toString(CGlobals_lib_ss.getInstance()
                        .getPersistentPreference(context)
                        .getInt(Constants_lib_ss.PREF_APPUSERID, -1)));
        params.put("email", CGlobals_lib_ss.msGmail);
        params.put("clientdatetime", df.format(cal.getTime()));
        String delim = "";
        StringBuilder getParams = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
            delim = "&";

        }
        try {
            url = url + "?" + getParams.toString();
            Log.i(TAG, "turnGpsOn - " + url);
        } catch (Exception e) {
            Log.e(TAG, Arrays.toString(e.getStackTrace()));
        }
        return checkParams(params);
    }

    public Map<String, String> getMinMobileParamsShort(Map<String, String> params,
                                                       String url, Context context) {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        params.put(
                "i",
                Integer.toString(CGlobals_lib_ss.getInstance()
                        .getPersistentPreference(context)
                        .getInt(Constants_lib_ss.PREF_APPUSERID, -1)));
        params.put("e", CGlobals_lib_ss.msGmail);
        params.put("ct", df.format(cal.getTime()));
        params.put("imei", mIMEI);
        String delim = "";
        StringBuilder getParams = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
            delim = "&";

        }
        try {
            url = url + "?" + getParams.toString();
            Log.i(TAG, "getMinMobileParamsShort - " + url);
        } catch (Exception e) {
            Log.e(TAG, Arrays.toString(e.getStackTrace()));
        }
        return checkParams(params);
    }

    public static ContactInfo getContactInfo(Context context,
                                             String sCountryCode, String sPhoneno, String sEmail) {
        //ContentResolver cr = mContext.getContentResolver();
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
        uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(sPhoneno.trim()));


        // Search first with Phone Lookup
        try {

            cursor = context.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME,
                    ContactsContract.PhoneLookup._ID}, null, null, null);

            if (cursor == null) {
                isNameFound = false;
            }
            if (cursor != null && cursor.getCount() <= 0) {
                isNameFound = false;
            }

            if (!isNameFound) {
                uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                        Uri.encode(sCountryCode.trim() + sPhoneno.trim()));
                cursor = context.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME,
                        ContactsContract.PhoneLookup._ID}, null, null, null);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                noRows = false;
                if (cursor.moveToFirst()) {
                    contactName = cursor.getString(cursor
                            .getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                    long id = cursor.getLong(cursor
                            .getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
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
                Uri uriEmail = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_FILTER_URI,
                        Uri.encode(sPhoneno.trim()));
                cursorEmail = context.getContentResolver().query(uriEmail, new String[]{
                        ContactsContract.CommonDataKinds.Email.DISPLAY_NAME, ContactsContract.CommonDataKinds.Email._ID}, null, null, null);

                if (cursorEmail != null && (cursorEmail.getCount() <= 0)) {
                    uriEmail = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_FILTER_URI,
                            Uri.encode(sEmail.trim()));
                    cursorEmail = context.getContentResolver().query(uriEmail, new String[]{
                            ContactsContract.CommonDataKinds.Email.DISPLAY_NAME, ContactsContract.CommonDataKinds.Email._ID}, null, null, null);
                }
                if (cursorEmail != null) {
                    if (cursorEmail.moveToFirst()) {
                        contactName = cursorEmail.getString(cursorEmail
                                .getColumnIndex(ContactsContract.CommonDataKinds.Email.DISPLAY_NAME));
                        long id = cursorEmail.getLong(cursorEmail
                                .getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email._ID));
                        photo = loadContactPhoto(context, id);
                    }
                }
                if (cursorEmail != null) {
                    cursorEmail.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (cursor != null) {
                cursor.close();
            }
            if (cursorEmail != null) {
                cursorEmail.close();
            }
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
            //ContentResolver cr = mContext.getContentResolver();
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

    // Check json for null not defined so it does not fail and throw error
    public boolean isNullNotDefined(JSONObject jo, String jkey) {
        if (!jo.has(jkey))
            return true;
        if (jo.isNull(jkey))
            return true;
        return false;
    }
}
