package in.bestbus.app;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TabHost;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.CallHome;
import lib.app.util.Constants_lib_ss;
import lib.app.util.DBHelperLocal;
import lib.app.util.SSLog_SS;
import lib.app.util.UserInfo;
// @ReportsCrashes(formKey = "dERZYThCRFo0MC01QkhST3ZRSFV6ZEE6MQ") 

public class CGlobals_BA {
    // Global variable
    public static final String TAG = "SSBus: ";
    public static final String PREFS_NAME = "CBAppPrefsFile";
    public static final double MINLATLONDIFF = .02;
    public static final String SMINLATLONDIFF = ".02";
    public static final double MINLATLONDIFFSTATION = .05;
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
    public static final int HAVEGPRS = 1;
    public static final int HAVEWIFI = 2;
    final public static int REQUEST_LOCATION = 1000;
    public static DBHelperBus mDBHelperBus;
    // 19.475655, 72.737732
    // 18.895243, 73.087234
    public static final double MINCITYLAT = 18.895243;
    public static final double MINCITYLON = 72.737732;
    public static final double MAXCITYLAT = 19.475655;
    public static final double MAXCITYLON = 73.087234;
    public static final double INVALIDLAT = -999;
    public static final double INVALIDLON = -999;
    public static final int REQUEST_CODE = 11;
    public static final String gAnalyticsId = "UA-32381506-2";
    // http://localhost/bestbuswk/assets/www/svr/sendbusinfo.php?
    // b=2L&lat=19.10234&lon=72.85&a=50&s=3&sn=hanumanroad&d=u
//		public static final String PHP_PATH = "http://192.168.2.144/sites/beta/svr/busapp";
//		public static final String PHP_PATH = "http://192.168.2.150/dropbox/sites/beta/svr/busapp";
    public static final String DRIVER = "driver";
    public static final double DEFAULT_RADIAL_DIST = 1000;
    public static final String VERSION = "v204";
    public static final String APPNAME = "busapp";
    //		public static final String ALPHA = "/alpha";
    public static final String ALPHA = "/apps";
    public static final String AUTHORITY = "smartshehar.com";
    public static final String SITE = "http://smartshehar.com";
    public static final String SITE_DIRECTORY = SITE + ALPHA + "/" + APPNAME;
    public static final String SVR_DIRECTORY = SITE_DIRECTORY + "/svr/" + VERSION + "/";
    public static final String PHP_DIRECTORY = ALPHA + "/" + APPNAME + "/svr/" + VERSION + "/php/";
    public static final String PHP_PATH = SITE + PHP_DIRECTORY;
    protected static final int FINDTAB = 0;
    protected static final int ROUTETAB = 1;
    protected static final int JOURNEYTAB = 2;
    protected static final int MAPTAB = 3;
    protected static final int MAXDISTANCEFROMSTATION = 5000;
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    public static boolean bInAdminMode;
    public static boolean bRegistered;
    public static String msJourneyParams;
    public static String msRouteParams;
    public static String msMapParams;
    public static String fndStart; // for setting the start field from another
    public static int mKmIncrement;
    public static float mBaseRate;
    public static float mRsIncrement;
    public static boolean bInTrip;
    public static Calendar oTripStartTime;
    public static Calendar oTripEndTime;
    public static Calendar oPrevTime;
    public static long lTotalTripms;
    public static boolean mbAppInited;
    public static boolean mbJourneyHide;
    public static int miMapZoom;
    public static String mIMEI;
    public static String msGmail = null;
    public static String mLine1Number;
    public static Location mFakeLocation;
    public static int iNearestStopIndex;
    public static RequestQueue mVolleyRequestQueue;
    protected static Location mLocStart;
    protected static Location mLocDest;
    private static CGlobals_BA instance;
    public File msAppDir;
    public ArrayList<CStop> maoStop = new ArrayList<CStop>();
    public ArrayList<CBus> maoBus = new ArrayList<CBus>();
    public Typeface typefaceHindi;
    public TabHost tabhost;
    // activity
    public Location mCurrentLocation, mOldLocation, mPreviousLocation;
    public ArrayList<String> malStopNames;
    public ArrayList<Station> malFirstStations = new ArrayList<Station>();
    public ArrayList<Station> malLastStations = new ArrayList<Station>();
    public ProgressDialog mProgressDialog;
    public String msShowBusStop;
    public String msBusNo;
    public String msBusLabel;
    public int miPreviousTab = FINDTAB;
    public CTrain moTrain;
    public boolean bStopsRead;
    public DBHelperLocal mDBHelperLocal;
    public Stack<Integer> stackActivity;
    public int miCurrentStationId;
    public int miTowardStationId = -1;
    public int miTrainId = -1;
    public PackageInfo mPackageInfo = null;
    public CallHome mCallHome;
    public String msProduct;
    public String msManufacturer;
    public String msCarrier;
    public String mAppNameCode;
    public UserInfo mUserInfo;
    public String msRouteCode;
    public double mdStopLat, mdStopLon;
    public CStop moStartStop, moDestStop;
    public double mdStartLat, mdStartLon, mdDestLat, mdDestLon;
    public String msBusDirection = "U";
    public boolean mbAutoRefreshLocation = true;
    public ArrayList<String> masRecentStops = null;
    protected double mdDirectDistance;
    protected double mdTravelDistance;
    protected Activity mActivity;
    String mAppVersion;
    SharedPreferences.Editor mEditorVersionPersistent = null;
    private SQLiteDatabase mDB = null, mDBLocal = null;
    private SharedPreferences mPrefsVersionPersistent = null;
    double lATLast = Constants_lib_ss.INVALIDLAT, lNGLast = Constants_lib_ss.INVALIDLNG;

    // Restrict the constructor from being instantiated
    private CGlobals_BA() {
    }

    public static synchronized CGlobals_BA getInstance() {
        if (instance == null) {
            instance = new CGlobals_BA();
        }
        return instance;
    }

    /*
            @Override
            public void onCreate() {
                super.onCreate();
                mbAppInited = false;
                bStopsRead = false;
                fndStart = "";
                msShowBusStop = "";
                mdTravelDistance = 0;
                msBusDirection = "U";
                bInAdminMode = false;
                readPreferences();
            }

            @Override
            public void onTerminate() {
                // this.mDB.close();
                super.onTerminate();
            }
    */
    public void init(Activity activity) {
        String currentVersion;
//			gAnalyticsId = activity.getString(R.string.ganalyticsid);
        SharedPreferences prefs;
        if (mbAppInited)
            return;
        mbAutoRefreshLocation = true;
        typefaceHindi = Typeface.createFromAsset(activity.getAssets(), "fonts/DroidHindi.ttf");
        lATLast = CGlobals_lib_ss.getInstance().getPersistentPreference(activity).
                getFloat("LOCATION_LAST_LOCATION_LAT", (float) Constants_lib_ss.INVALIDLAT);
        lNGLast = CGlobals_lib_ss.getInstance().getPersistentPreference(activity).
                getFloat("LOCATION_LAST_LOCATION_LON", (float) Constants_lib_ss.INVALIDLNG);
        mFakeLocation = new Location("fake"); // Parle(E) - Matruchaya // Dadar - Tilak Bridge  19.0194351,72.8430694
        double lat = 19.01715;
        double lon = 72.84717;
        if (lATLast != Constants_lib_ss.INVALIDLAT && lNGLast != Constants_lib_ss.INVALIDLNG) {
            mFakeLocation.setLatitude(lATLast);
            mFakeLocation.setLongitude(lNGLast);
        } else {
            mFakeLocation.setLatitude(lat);
            mFakeLocation.setLongitude(lon);
        }

        mAppNameCode = activity.getString(R.string.appNameShort);
        try {
            TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
            mIMEI = telephonyManager.getDeviceId();
            msCarrier = telephonyManager.getNetworkOperator();
            mLine1Number = telephonyManager.getLine1Number();
            mLine1Number = telephonyManager.getSubscriberId();
        } catch (Exception e) {
            SSLog.e(TAG, "CGlobals_BA TelephonyManager :", e);
        }
        msProduct = Build.PRODUCT;
        msManufacturer = Build.MANUFACTURER;
        mActivity = activity;
        mbAppInited = true;
        mPackageInfo = getPackageInfo();
        stackActivity = new Stack<Integer>();
        msJourneyParams = "";
        msRouteParams = "";
        msMapParams = "";
        AccountManager manager = (AccountManager) activity.getSystemService(Context.ACCOUNT_SERVICE);
        Account[] list = manager.getAccounts();
        msGmail = null;

        for (Account account : list) {
            if (account.type.equalsIgnoreCase("com.google")) {
                msGmail = account.name;
                break;
            }
        }
        mAppVersion = mPackageInfo.versionName + mPackageInfo.versionCode;
        prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        currentVersion = prefs.getString("dbVersionInfo", "-1");
        mUserInfo = new UserInfo(activity, activity.getString(R.string.appTitle));
//		  	Activity act, Location loc, PackageInfo packageinfo, String appnameshort,
//			String appcode, UserInfo uinfo
        initdb(activity);
        readPreferences();
        mCallHome = new CallHome(activity, getMyLocation(activity),
                mPackageInfo, activity.getString(R.string.appNameShort),
                activity.getString(R.string.appCode),
                mUserInfo, this.mDBHelperLocal);
        if (!currentVersion.equals(mAppVersion)) { // First Use
            mCallHome.userPing(activity.getString(R.string.atFirstUse), "");
        }


    }

    boolean initdb(Activity activity) {
        try {
            this.mDBHelperBus = new DBHelperBus(activity.getApplicationContext());
            this.mDBHelperBus.openDataBase(mPackageInfo, activity, this);
            mDB = mDBHelperBus.getReadableDatabase();
            this.mDBHelperLocal = new DBHelperLocal(activity, mPackageInfo,
                    activity.getString(R.string.appNameShort), mUserInfo);
            this.mDBHelperLocal.openDataBase(mPackageInfo, activity);
            mDBLocal = mDBHelperLocal.getReadableDatabase();
        } catch (SQLException e) {
            SSLog_SS.e(this.toString(), ".OnCreate: " + e.getMessage());
            return false;
        }
        return true;
    }

    public void cleanUp() {
        if (mDB != null)
            mDB.close();
        if (mDBLocal != null)
            mDBLocal.close();

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

    ;

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
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        LocationManager lm = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
        Location loc = null;
        List<String> providers = lm.getAllProviders();
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
        if (mCurrentLocation == null)
            mCurrentLocation = mFakeLocation;
        return mCurrentLocation;
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

    public String showDistance(double di) {
        if (di == -1)
            return " N/A";
        if (di > 1000)
            return Double.toString((int) di / 1000.00) + " km";
        else
            return Integer.toString((int) di) + " m";
    }

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

    public int haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
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

    // Read App global preferences
    public void readPreferences() {
//			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
//			bRegistered = settings.getBoolean(PREFREGISTERED, false);

    }

    // Save App global preferences
    public void savePreferences() {
//			SharedPreferences settings = getSharedPreferences(SSTApp.PREFS_NAME, 0);
//			SharedPreferences.Editor editor = settings.edit();

//			editor.commit();

    }

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
        boolean isMoreAccurate = accuracyDelta <= 0;
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

    protected void showGPSDisabledAlertToUser(Activity activity) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
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

    public boolean inCityBoundingRect(CStop si) {
        ;
        boolean bInLat = false, bInLon = false;
        if (si.lat >= MINCITYLAT && si.lat <= MAXCITYLAT)
            bInLat = true;
        if (si.lon > MINCITYLON && si.lon < MAXCITYLON)
            bInLon = true;
        return bInLat && bInLon;
    }

    public int dow(int iCDow) {
        return iCDow == 1 ? 7 : iCDow - 1;
    }

    public Map<String, String> checkParams(Map<String, String> map) {
        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> pairs = (Map.Entry<String, String>) it
                    .next();
            if (pairs.getValue() == null || TextUtils.isEmpty(pairs.getValue())) {
                map.put(pairs.getKey(), "");
                SSLog_SS.d(TAG, pairs.getKey());
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
            mVolleyRequestQueue = Volley
                    .newRequestQueue(context);
        }

        return mVolleyRequestQueue;
    }

    public SharedPreferences getPersistentPreference(Context context) {
        if (mPrefsVersionPersistent == null) {
            mPrefsVersionPersistent = context.getApplicationContext()
                    .getSharedPreferences(
                            Constants_bus.PREFS_VERSION_PERSISTENT,
                            Context.MODE_PRIVATE);
        }
        return mPrefsVersionPersistent;
    }

    public SharedPreferences.Editor getPersistentPreferenceEditor(Context context) {
        if (mEditorVersionPersistent == null) {
            mEditorVersionPersistent = getPersistentPreference(context).edit();
        }
        return mEditorVersionPersistent;
    }

    class historyAct {
        int act;

        historyAct(int a) {
            act = a;
        }
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


} // CGlobals_BA