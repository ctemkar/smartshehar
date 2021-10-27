package com.smartshehar.android.app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TabHost;
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
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.CallHome;
import lib.app.util.Constants_lib_ss;
import lib.app.util.DBHelperLocal;
import lib.app.util.UserInfo;


public class CGlobals_trains {
    private static CGlobals_trains instance;
    public static final int REQUEST_CHECK_SETTINGS = 1000;


    // Restrict the constructor from being instantiated
    private CGlobals_trains() {
    }

    public static synchronized CGlobals_trains getInstance() {
        if (instance == null) {
            instance = new CGlobals_trains();
        }
        return instance;
    }

    SharedPreferences.Editor mEditorVersionPersistent = null;
    private SharedPreferences mPrefsVersionPersistent = null;
    public static final String TAG = "SST: ";
    public static final String PREFS_NAME = "CBAppPrefsFile";
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

    public static final String VERSION = "v24";
    public static final String APPNAME = "trainapp";

    //	public static final String ALPHA = "/alpha";
    public static final String ALPHA = "/apps";

    public static final String AUTHORITY = "smartshehar.com";
    public static final String SITE = "http://smartshehar.com";
    public static final String SITE_DIRECTORY = SITE + ALPHA + "/" + APPNAME;
    public static final String SVR_DIRECTORY = SITE_DIRECTORY + "/svr/" + VERSION + "/";
    public static final String PHP_DIRECTORY = ALPHA + "/" + APPNAME + "/svr/" + VERSION + "/php/";
    public static final String PHP_PATH = SITE + PHP_DIRECTORY;
    public static String MEGABLOCK_URL = SITE_DIRECTORY + "/megablock.html";

    // "http://smartshehar.com/apps/trainapp/megablock.html";

    //"http://smartshehar.com/alpha/trainapp/svr/v24/php/";

    // production
    public static final int HAVEGPRS = 1;
    public static final int HAVEWIFI = 2;
    public static final int HAVECONNECTIONOTHER = 2;

    protected static final int FINDTAB = 0;
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

    public static final double INVALIDLAT = -999;
    public static final double INVALIDLON = -999;

    public static final int REQUEST_CODE = 11;

    public File msAppDir;
    public static boolean mbIsAdmin;
    public Station moStart, moDest;
    public static boolean showGPSDialog = true;

    public TabHost tabhost;
    public static boolean bRegistered;
    public static String msJourneyParams;
    public static String msRouteParams;
    public static String msMapParams;
    public static String fndStart; // for setting the start field from another
    // activity
    public Location mCurrentLocation, mOldLocation, mPreviousLocation;

    public ArrayList<String> malStopNames;
    // public ArrayList<StopInfo> malStopInfo = new ArrayList<StopInfo>();
    public ArrayList<Station> malFirstStations = new ArrayList<Station>();
    public ArrayList<Station> malLastStations = new ArrayList<Station>();
    public ProgressDialog mProgressDialog;
    public String msShowBusStop;

    public String msBusNo;
    public int miPreviousTab = FINDTAB;



    public boolean bStopsRead;
    public static DBHelperTrain mDBHelper;
    public DBHelperLocal mDBHelperLocal;
    private SQLiteDatabase mDB = null;
    public Stack<Integer> stackActivity;
    protected double mdDirectDistance;
    protected double mdTravelDistance;
    public static int mKmIncrement;
    public static float mBaseRate;
    public static float mRsIncrement;
    public static boolean bInTrip;
    public static Calendar oTripStartTime;
    public static Calendar oTripEndTime;
    public static Calendar oPrevTime;
    public static long lTotalTripms;
    public static final String gAnalyticsId = "UA-32381506-2";

    public static boolean mbAppInited;

    public static boolean mbJourneyHide;
    public static int miMapZoom;

    protected static Location mLocStart;
    protected static Location mLocDest;
    protected static Activity mActivity;
    public int miCurrentStationId;
    public int miTowardStationId = -1;
    public int miTrainId = -1;
    public PackageInfo packageInfo = null;
    public CallHome mCH;
    public String msProduct;
    public String msManufacturer;
    public String msCarrier;
    public String mAppNameCode;

    public static String mIMEI;
    public static String msGmail = null;
    public static String mLine1Number;
    public static Location mFakeLocation;
    public static String mAppNameShort;
    public static boolean mIsWorkHomeDefined = false;
    public static String msCurrentVersion;

    String mAppVersion;
    public static UserInfo mUserInfo;
    public static ArrayList<MegaBlock> maoMegaBlock;
    public static String msAppVersionName;
    public static int miAppVersionCode;
    int miAndroidSdkVersion;
    String msAndroidReleaseVersion;
    public static RequestQueue mVolleyRequestQueue;
    public static String msBlockOn = "";


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

    public class MegaBlock {
        public String lineCode;
        public int iUp, iDn, iSlow, iFast;
        public String fromDt, toDt;
        public int fromStationId, toStationId;
        public int iRepeatDays;
        public String link;
        public String sMessage, sDisplay;
        public String fromstation, tostation;

        public MegaBlock() {

        }
    }

    class historyAct {
        int act;

        historyAct(int a) {
            act = a;
        }
    }


    public void init(Activity act) {
//		ACRA.init((Application)this);
//		loadInterstital(mActivity);
        try {
            mActivity = act;
            mbAppInited = false;
            maoMegaBlock = new ArrayList<MegaBlock>();
            bStopsRead = false;
            fndStart = "";
            msShowBusStop = "";
            mdTravelDistance = 0;
            readPreferences();
//		gAnalyticsId = activity.getString(R.string.ganalyticsid);
            SharedPreferences prefs;
            if (mbAppInited)
                return;
            mAppNameShort = mActivity.getString(R.string.appNameShort);
            mFakeLocation = new Location("fake");    // Dadar - Tilak Bridge
            double lat = 19.02078;
            double lon = 72.843168;
            mFakeLocation.setLatitude(lat);
            mFakeLocation.setLongitude(lon);
            mFakeLocation.setAccuracy(9999);

            TelephonyManager telephonyManager = (TelephonyManager) mActivity.getSystemService(Context.TELEPHONY_SERVICE);
            mAppNameCode = mActivity.getString(R.string.appNameShort);
            mIMEI = telephonyManager.getDeviceId();
            msCarrier = telephonyManager.getNetworkOperator();
            mLine1Number = telephonyManager.getLine1Number();
            mLine1Number = telephonyManager.getSubscriberId();
            msProduct = Build.PRODUCT;
            msManufacturer = Build.MANUFACTURER;
            mbAppInited = true;
            packageInfo = getPackageInfo();
            stackActivity = new Stack<Integer>();
            msJourneyParams = "";
            msRouteParams = "";
            msMapParams = "";
            AccountManager manager = (AccountManager) mActivity.getSystemService(Context.ACCOUNT_SERVICE);
            Account[] list = manager.getAccounts();
            msGmail = null;
            miAndroidSdkVersion = Build.VERSION.SDK_INT;
            msAndroidReleaseVersion = Build.VERSION.RELEASE; // e.g.
            for (Account account : list) {
                if (account.type.equalsIgnoreCase("com.google")) {
                    msGmail = account.name;
                    break;
                }
            }
            mbIsAdmin = isAdmin(msGmail);
            msAppVersionName = packageInfo.versionName;
            miAppVersionCode = packageInfo.versionCode;
            mAppVersion = packageInfo.versionName + packageInfo.versionCode;
            prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
            msCurrentVersion = prefs.getString("dbVersionInfo", "-1");
            mUserInfo = new UserInfo(mActivity, mActivity.getString(R.string.appTitle));
            Location mLoc = getMyLocation(act.getApplicationContext());
            if (mLoc != null)
                mCH = new CallHome(act, mLoc,
                        packageInfo, act.getString(R.string.appNameShort), act.getString(R.string.appCode),
                        mUserInfo,this.mDBHelperLocal);
            initdb(mActivity);
            readPreferences();
        } catch (Exception e) {
            e.printStackTrace();
            SSLog.e(TAG, "init: ", e);
        }
    }

    boolean initdb(Activity activity) {
        try {
            if (mDB == null) {
                this.mDBHelperLocal = new DBHelperLocal(activity, packageInfo,
                        activity.getString(R.string.appNameShort), mUserInfo);
                this.mDBHelperLocal.openDataBase(packageInfo, activity);
                mDBHelperLocal.getReadableDatabase();
                this.mDBHelper = new DBHelperTrain(activity.getApplicationContext());
                this.mDBHelper.openDataBase(packageInfo, activity, this);
                mDB = mDBHelper.getReadableDatabase();
            }


        } catch (SQLException e) {
            SSLog.e(this.toString(), ".OnCreate: ", e.getMessage());
            return false;
        }


        return true;
    }


    public Map<String, String> getBasicMobileParams(Map<String, String> params,
                                                    String url, Context context) {

        init((Activity) context);
        Location myLocation = getBestLocation(context);
        SimpleDateFormat df = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT,
                Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        params.put("appuserid", Integer.toString(CGlobals_lib_ss.getInstance()
                .getPersistentPreference(context).getInt(Constants_lib_ss.PREF_APPUSERID, -1)));
        params.put("appusageid", Integer.toString(CGlobals_lib_ss.getInstance()
                .getPersistentPreference(context).getInt(Constants_lib_ss.PREF_APPUSAGEID, -1)));
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
        params.put("version", CGlobals_trains.msAppVersionName);
        params.put("android_release_version", msAndroidReleaseVersion);
        params.put("versioncode", String.valueOf(miAppVersionCode));
        params.put("android_sdk_version", Integer.toString(miAndroidSdkVersion));
        params.put("imei", CGlobals_trains.mIMEI);
        params.put("email", CGlobals_trains.msGmail);
        params.put("module", "SAR");
        params.put("carrier", msCarrier);
        params.put("product", msProduct);
        params.put("manufacturer", msManufacturer);

        String delim = "";
        StringBuilder getParams = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            getParams.append(delim + entry.getKey() + "=" + entry.getValue());
            delim = "&";

        }
        try {
            url = url + "?" + getParams.toString() + "&verbose=Y";
            Log.d(TAG, "URL IS  " + url);
        } catch (Exception e) {
            Log.e(TAG, e.getStackTrace().toString());
        }
        return params;
    }

    String getAddress(Location location, Context context) {
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

    int getGPSCheckMilliSecsFromPrefs() {
        return 1000;
    }

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

    public void cleanUp() {
        if (mDB != null)
            mDB.close();

    }

    private PackageInfo getPackageInfo() {
        PackageInfo pi = null;
        try {
            pi = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pi;
    }

    public CharSequence readAsset(String asset, Activity activity) {
        BufferedReader in = null;

        try {
            in = new BufferedReader(new InputStreamReader(activity.getAssets()
                    .open(asset)));
            String line;
            StringBuilder buffer = new StringBuilder();
            while ((line = in.readLine()) != null) {
                buffer.append(line).append('\n');
            }
            return buffer;
        } catch (IOException e) {
            return "";
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                return "";
            }

        }
    }

    // Update the location
    public Location getMyLocation(Context context) {
        LocationManager lm = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
        Location loc = null; // = mFakeLocation;
        List<String> providers = lm.getProviders(true);
        for (String provider : providers) {
            loc = lm.getLastKnownLocation(provider);
            if (loc != null) {
                if (isBetterLocation(loc, mCurrentLocation)) {
                    mCurrentLocation = loc;
                    if (mOldLocation != null && mCurrentLocation != null) {
                        float[] res = new float[1];
                        Location.distanceBetween(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                                mOldLocation.getLatitude(),
                                mOldLocation.getLongitude(), res);
                        if (res[0] > 200 && !mCurrentLocation.getProvider().toString().equals("fake"))
                            mOldLocation = mCurrentLocation;
                    } else {
                        if (mCurrentLocation != null && !mCurrentLocation.getProvider().toString().equals("fake"))
                            mOldLocation = mCurrentLocation;
                    }
                }
            }
        }
        if (loc == null) {
            if (mCurrentLocation != null) {
                getInstance().setLocationPref(mCurrentLocation,context);
                return mCurrentLocation;
            } else {
//                double lon = 72.843168
                String latitude = CGlobals_lib_ss.getInstance()
                        .getPersistentPreference(context).getString(Constants_trains.LAST_LAT, "19.020789");
                String longitude = CGlobals_lib_ss.getInstance()
                        .getPersistentPreference(context).getString(Constants_trains.LAST_LNG, "72.843168");
                double lat = Double.valueOf(latitude);
                double lng = Double.valueOf(longitude);

                mFakeLocation.setLatitude(lat);
                mFakeLocation.setLongitude(lng);
                return mFakeLocation;
            }
        } else {
            return loc;
        }
    }

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public ArrayList<Station> readStation(Activity activity) {
        // String[] mStation = readresourcefiles.readAsset("data/stations.txt",
        // activity).toString().split("\n");

        ArrayList<String> mStation = new ArrayList<String>();

        final ArrayList<Station> malStation = new ArrayList<Station>();
        String[] saLine;
        int timenxt = -1;
        int stnno = -1;
        double lat = INVALIDLAT;
        double lon = INVALIDLON;
        String lin = "", stn, stnabbr;
        boolean bLastFound = false;
        for (String s : mStation) {
            saLine = s.split(",");
            timenxt = -1;
            stnno = -1;
            lat = INVALIDLAT;
            lon = INVALIDLON;
            lin = "";
            stn = "";
            stnabbr = "";
            if (s.trim().length() > 0) {
                try {
                    if (saLine[0].trim().length() > 0)
                        stnno = Integer.parseInt(saLine[0].trim());
                    if (saLine[3].trim().length() > 0)
                        lat = Double.parseDouble(saLine[3].trim());
                    if (saLine[4].trim().length() > 0)
                        lon = Double.parseDouble(saLine[4].trim());
                    if (saLine[5].trim().length() > 0)
                        timenxt = Integer.parseInt(saLine[5].trim());
                    lin = saLine[1].trim();
                    stn = saLine[2].trim();
                    stnabbr = saLine[6].trim();
                    malStation.add(new Station(stnno, lin, stn, lat, lon,
                            timenxt, stnabbr));
                    if (stnno == 1) {
                        malFirstStations.add(new Station(stnno, lin, stn, lat,
                                lon, timenxt, stnabbr));
                    }
                    bLastFound = false;
                    for (Station ls : malLastStations) {
                        if (ls.msLin.equalsIgnoreCase(lin)) {
                            ls.miStationNo = stnno;
                            ls.msLin = lin;
                            ls.msStationName = stn;
                            ls.mdLat = lat;
                            ls.mdLon = lon;
                            ls.miTimeToNxt = timenxt;
                            bLastFound = true;
                        }
                    }
                    if (!bLastFound)
                        malLastStations.add(new Station(stnno, lin, stn, lat,
                                lon, timenxt, stnabbr));

                } catch (Exception e) {
                    Log.e("Route: ", s);
                }
            }
        }
        return malStation;
    }

    TextView changeBusColor(TextView tv) {
        String sBus = tv.getText().toString().replace(" ", "");
        // TextView tv = new TextView(this) ;
        tv.setText(sBus.replace("ExpACExp", "ACExp"));

        if (sBus.contains("ACExp"))
            tv.setTextColor(Color.BLUE);
        else if (sBus.contains("L"))
            tv.setTextColor(Color.RED);
        else if (sBus.contains("C"))
            tv.setTextColor(Color.MAGENTA);

        else
            tv.setTextColor(Color.BLACK);

        return null;

    }

    public String showDistance(double di) {
        if (di == -1)
            return " N/A";
        if (di > 1000)
            return Double.toString((int) di / 1000.00) + " km";
        else
            return Integer.toString((int) di) + " m";
    }

	/*
     * public boolean inCityBoundingRect(StopInfo si) { ; boolean bInLat =
	 * false, bInLon = false; if(si.lat >= MINCITYLAT && si.lat <= MAXCITYLAT)
	 * bInLat = true; if(si.lon > MINCITYLON && si.lon < MAXCITYLON) bInLon =
	 * true; return bInLat && bInLon; }
	 */

    public boolean haveWiFi() {
        if (haveNetworkConnection() == HAVEWIFI)
            return true;
        return false;
    }

    public boolean haveGPRS() {
        if (haveNetworkConnection() == HAVEGPRS)
            return true;
        return false;
    }

    public static int haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        boolean haveConnected = false;

        try{

           ConnectivityManager cm = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
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
       }catch (Exception e)
       {
           Log.d(TAG,"haveNetworkConnection: "+e.toString());
           SSLog.e(TAG,"haveNetworkConnection: ",e);
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

    // Save App global preferences
    public void savePreferences() {
//		SharedPreferences settings = getSharedPreferences(SSTApp.PREFS_NAME, 0);
//		SharedPreferences.Editor editor = settings.edit();

//		editor.commit();

    }

    ;

    // http://localhost/bestbuswk/assets/www/svr/sendbusinfo.php?
    // b=2L&lat=19.10234&lon=72.85&a=50&s=3&sn=hanumanroad&d=u
    private static final String sendUrl = "http://www.mesn.org/bestbus/svr/sendbusinfo.php?b=";

    void sendLocation(final Location location, final Integer iNearestStopNo,
                      final String sNearestStop, final char mcDir) {

        if (location != null) {
            if (mPreviousLocation != null)
                if (location == mPreviousLocation
                        || ((int) (location.getLatitude() * 1e6) == (int) (mPreviousLocation
                        .getLatitude() * 1e6))
                        && (int) (location.getLongitude() * 1e6) == (int) (mPreviousLocation
                        .getLongitude() * 1e6))
                    return;
        }
        if (mPreviousLocation != null)
            if (isBetterLocation(mPreviousLocation, location))
                return;
        mPreviousLocation = location;
        Thread t = new Thread() {
            public void run() {
                try {
                    assert location != null;
                    String sUrl = sendUrl + msBusNo.replace(" ", "") + "&lat="
                            + Double.toString(location.getLatitude()) + "&lon="
                            + Double.toString(location.getLongitude()) + "&a="
                            + Double.toString(location.getAccuracy()) + "&s="
                            + Integer.toString(iNearestStopNo) + "&sn="
                            + sNearestStop + "&d=" + mcDir + "&m=j";
                    URL url = new URL(sUrl);
                    URLConnection conn = url.openConnection();
                    // Get the response
                    @SuppressWarnings("unused")
                    BufferedReader rd = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                } catch (Exception e) {
                    SSLog.e("ActJourney:", " sendLocation - ", e.getMessage());
                }
            }
        };
        t.start();
    } // sendLocation

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
        if (currentBestLocation == null) {
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

    protected void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
        alertDialogBuilder
                .setMessage(R.string.gps_enable)
                .setCancelable(false)
                .setPositiveButton(R.string.no_gps_message,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                mActivity.startActivityForResult(
                                        callGPSSettingIntent, REQUEST_CODE);
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

    public boolean inCityBoundingRect(Station moStation) {
        // TODO Auto-generated method stub
        return true;
    }

    public boolean isAdmin(String emailid) {
        String string = "ctemkar@gmail.com;priteshpanchigar@gmail.com;asmita18madhav@gmail.com";
        String[] splits = string.split(";");
        for (String email : splits) {
            if (email.equals(emailid)) {
                return true;
            }

        }
        return false;
    }

    /**
     * Function to display simple Alert Dialog
     *
     * @param context - application context
     * @param title   - alert dialog title
     * @param message - alert message
     * @param status  - success/failure (used to set icon)
     */
    @SuppressWarnings("deprecation")
    public static void showAlertDialog(Context context, String title, String message, Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting alert dialog icon
//        alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);

        // Setting OK Button
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    public Map<String, String> checkParams(Map<String, String> map) {
        for (Map.Entry<String, String> pairs : map.entrySet()) {
            if (pairs.getValue() == null) {
                map.put(pairs.getKey(), "");
                Log.d(TAG, pairs.getKey());
            }
        }
        return map;
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

            // mRequestQueue =
            // Volley.newRequestQueue(sInstance.getApplicationContext());

            // Start the queue
            // mRequestQueue =
            // Volley.newRequestQueue(getApplicationContext());
        }

        return mVolleyRequestQueue;
    }

    /*public SharedPreferences getPersistentPreference() {
        if (mPrefsVersionPersistent == null) {
            mPrefsVersionPersistent = mActivity
                    .getSharedPreferences(
                            Constants_trains.PREFS_VERSION_PERSISTENT,
                            Context.MODE_PRIVATE);
        }
        return mPrefsVersionPersistent;
    }
    public SharedPreferences.Editor getPersistentPreferenceEditor() {
        if (mEditorVersionPersistent == null) {
            mEditorVersionPersistent = getPersistentPreference().edit();
        }
        return mEditorVersionPersistent;
    }*/
    /*public void addVolleyRequest(StringRequest postRequest,
                                 boolean clearBeforeQuery) {
		try {
			postRequest.setRetryPolicy(new DefaultRetryPolicy(
					DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 2,
					DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
			if (clearBeforeQuery) {
				getRequestQueue(mActivity).cancelAll(new RequestQueue.RequestFilter() {
					@Override
					public boolean apply(Request<?> request) {
						return true;
					}
				});
			}
			getRequestQueue(mActivity).add(postRequest);
		} catch (Exception e) {
			SSLog.e(TAG, "mRequestQueue may not be initialized - ", e);
		}
	}*/
    public static String getVolleyError(Context context, VolleyError error) {
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

    // to check gps is on or off.
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
                final LocationSettingsStates states = result.getLocationSettingsStates();
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
//                            if (showGPSDialog)
                            status.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                            e.printStackTrace();
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



    public void setLocationPref(Location location,Context context) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        CGlobals_lib_ss.getInstance()
                .getPersistentPreferenceEditor(context).putString(Constants_trains.LAST_LAT, String.valueOf(lat));
        CGlobals_lib_ss.getInstance()
                .getPersistentPreferenceEditor(context).commit();
        CGlobals_lib_ss.getInstance()
                .getPersistentPreferenceEditor(context).putString(Constants_trains.LAST_LNG, String.valueOf(lng));
        CGlobals_lib_ss.getInstance()
                .getPersistentPreferenceEditor(context).commit();
    }
}
