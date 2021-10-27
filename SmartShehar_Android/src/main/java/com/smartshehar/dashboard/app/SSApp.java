package com.smartshehar.dashboard.app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.FacebookSdk;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.smartshehar.chat.gcm.MyPreferenceManager;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import lib.app.util.CallHome;
import lib.app.util.UserInfo;


@ReportsCrashes(
        formUri = "http://www.smartshehar.com/alpha/smartsheharapp/v17/svr/php/acra/acra_ss_report.php",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)
      /*  mode = ReportingInteractionMode.DIALOG,
        resDialogText = R.string.crash_toast_text)*/
public class SSApp extends Application {
    private MyPreferenceManager pref;
    private static boolean mIfAppInited;
    public String msProduct;
    public String msManufacturer;
    public String msCarrier;
    public String mAppNameCode;
    private static SSApp sInstance;
    public static String mIMEI;
    public static String mGmail = null;
    public static String mLine1Number;
    public static Location mFakeLocation;
    private Activity mActivity;
    public CallHome mCH;
    public PackageInfo mPackageInfo;
    public Location mCurrentLocation;
    private Location mOldLocation;
    public ProgressDialog mProgressDialog;
    public static final String TAG = "SSC";
    private static final int HAVEGPRS = 1;
    private static final int HAVEWIFI = 2;
    public static final String PHP_PATH = "http://www.smartshehar.com/svr";
    public UserInfo mUserInfo;
    public static int appuserid;
    SharedPreferences.Editor mEditorVersionPersistent = null;
    private SharedPreferences mPrefsVersionPersistent = null;
    public static RequestQueue mRequestQueue;
    public static boolean mbGPSFix;
    public static boolean mbInTrip;
    public static Location moLocStart;
    public static Location moLocDest;
    public static GoogleAnalytics analytics;
    public static Tracker tracker;


    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AccountManager manager = (AccountManager) this
                .getSystemService(Context.ACCOUNT_SERVICE);
        Account[] list = manager.getAccounts();
        String msGmail = null;

        for (Account account : list) {
            if (account.type.equalsIgnoreCase("com.google")) {
                msGmail = account.name;
                mGmail = msGmail;

                break;
            }
        }

        ACRA.getErrorReporter().putCustomData("useremail", msGmail);
        ACRA.getErrorReporter().putCustomData("email", msGmail);
        Log.d("MyApplication", "onCreate()");
        Log.d("MyApplication", "onCreate()");
        sInstance = this;
        gettingHashKey();
        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);
        tracker = analytics.newTracker("UA-32381506-2"); // Replace with actual tracker id
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);
    }

    @Override
    public void onTerminate() {

        super.onTerminate();
    }


    public void init(Activity activity) {
        //     String currentVersion;
//		gAnalyticsId = activity.getString(R.string.ganalyticsid);
//        SharedPreferences prefs;
        if (mIfAppInited)
            return;

        mFakeLocation = new Location("fake");    // Dadar - Tilak Bridge
        double lat = 19.02078;
        double lon = 72.843168;
        mFakeLocation.setLatitude(lat);
        mFakeLocation.setLongitude(lon);

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mAppNameCode = getString(R.string.appNameShort);
        mIMEI = telephonyManager.getDeviceId();
        msCarrier = telephonyManager.getNetworkOperator();
        mLine1Number = telephonyManager.getLine1Number();
        mLine1Number = telephonyManager.getSubscriberId();
        msProduct = Build.PRODUCT;
        msManufacturer = Build.MANUFACTURER;
        mActivity = activity;
        mIfAppInited = true;
        mPackageInfo = getPackageInfo();
        mUserInfo = new UserInfo(activity, getString(R.string.appTitle));


        AccountManager manager = (AccountManager) this
                .getSystemService(Context.ACCOUNT_SERVICE);
        Account[] list = manager.getAccounts();
        String msGmail = null;

        for (Account account : list) {
            if (account.type.equalsIgnoreCase("com.google")) {
                msGmail = account.name;
                break;
            }
        }
        mGmail = msGmail;
        ACRA.getErrorReporter().putCustomData("useremail", msGmail);
        ACRA.getErrorReporter().putCustomData("email", msGmail);
        Log.d("MyApplication", "onCreate()");
        sInstance = this;


        //    String mAppVersion = mPackageInfo.versionName + mPackageInfo.versionCode;
//        prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        //    currentVersion = prefs.getString("dbVersionInfo", "-1");

        mCH = new CallHome(activity, getMyLocation(),
                mPackageInfo, getString(R.string.appNameShort),
                getString(R.string.appCode), mUserInfo);

    }

    public void gettingHashKey(){

        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.smartshehar.dashboard.app",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

    }

    public static synchronized SSApp getInstance() {
        return sInstance;
    }

    public SharedPreferences.Editor getPreferenceEditor() {
        if (mEditorVersionPersistent == null) {
            mEditorVersionPersistent = getPreference().edit();
        }
        return mEditorVersionPersistent;
    }

    public SharedPreferences getPreference() {
        if (mPrefsVersionPersistent == null) {
            mPrefsVersionPersistent = getApplicationContext()
                    .getSharedPreferences(
                            Constants_dp.PREF_APP_PREFERENCES,
                            Context.MODE_PRIVATE);
        }
        return mPrefsVersionPersistent;
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


    // Update the location
    public Location getMyLocation() {
      /*  if (ActivityCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            return null;
        }*/
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location loc;
        List<String> providers = lm.getAllProviders();
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            loc = lm.getLastKnownLocation(provider);
            if (loc != null) {
                if (isBetterLocation(loc, mCurrentLocation)) {
                    mCurrentLocation = loc;
                    if (mOldLocation != null && mCurrentLocation != null) {
                        float[] res = new float[1];
                        Location.distanceBetween(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                                mOldLocation.getLatitude(),
                                mOldLocation.getLongitude(), res);
                        if (res[0] > 200 && !mCurrentLocation.getProvider().equals("fake"))
                            mOldLocation = mCurrentLocation;
                    } else {
                        if (mCurrentLocation != null && !mCurrentLocation.getProvider().equals("fake"))
                            mOldLocation = mCurrentLocation;
                    }
                }
            }
        }
        return mCurrentLocation;
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


    public int haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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


    protected boolean isBetterLocation(Location location,
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


    public void addVolleyRequest(StringRequest postRequest,
                                 boolean clearBeforeQuery) {
        try {
            postRequest.setRetryPolicy(new DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 2,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            if (clearBeforeQuery) {
                getRequestQueue().cancelAll(new RequestQueue.RequestFilter() {
                    @Override
                    public boolean apply(Request<?> request) {
                        return true;
                    }
                });
            }
            getRequestQueue().add(postRequest);
        } catch (Exception e) {
            SSLog.e(TAG, "mRequestQueue may not be initialized - ", e);
        }
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }


    public RequestQueue getRequestQueue() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (mRequestQueue == null) {

            // Instantiate the cache
            Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB
            // cap

            // Set up the network to use HttpURLConnection as the HTTP client.
            Network network = new BasicNetwork(new HurlStack());

            // Instantiate the RequestQueue with the cache and network.
            mRequestQueue = new RequestQueue(cache, network);
            mRequestQueue.start();
            mRequestQueue = Volley
                    .newRequestQueue(getApplicationContext());

        }

        return mRequestQueue;
    }
    public MyPreferenceManager getPrefManager() {
        if (pref == null) {
            pref = new MyPreferenceManager(this);
        }

        return pref;
    }
} // SSTApp