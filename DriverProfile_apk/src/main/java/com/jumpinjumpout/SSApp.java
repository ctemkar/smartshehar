package com.jumpinjumpout;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
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
import com.jumpinjumpout.www.driverprofile.R;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.util.List;

import lib.app.util.CallHome;
import lib.app.util.UserInfo;

@ReportsCrashes(
        formUri = "https://www.cabebooking.com/alpha/cabeapp/v1/svr/php/acra/acra.php",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)
public class SSApp extends Application {
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
    private PowerManager.WakeLock wakeLock;
    private OnScreenOffReceiver onScreenOffReceiver;


    public static final String TAG = "SSC";
    /*private static final int HAVEGPRS = 1;
    private static final int HAVEWIFI = 2;
    public static final int REQUEST_CODE = 11;
    public static final String PHP_PATH = "http://www.smartshehar.com/svr";*/
    public UserInfo mUserInfo;
    SharedPreferences.Editor mEditorVersionPersistent = null;
    private SharedPreferences /*mPrefs = null,*/ mPrefsVersionPersistent = null;
    public static RequestQueue mVolleyRequestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        registerKioskModeScreenOffReceiver();
        startKioskService();
    }

    private void registerKioskModeScreenOffReceiver() {
        // register screen off receiver
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        onScreenOffReceiver = new OnScreenOffReceiver();
        registerReceiver(onScreenOffReceiver, filter);
    }

    public PowerManager.WakeLock getWakeLock() {
        if (wakeLock == null) {
            // lazy loading: first call, create wakeLock via PowerManager.
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "wakeup");
        }
        return wakeLock;
    }

    private void startKioskService() { // ... and this method
        startService(new Intent(this, KioskService.class));
    }

    @Override
    public void onTerminate() {
        // this.mDB.close();
        super.onTerminate();
    }


    public void init(Activity activity) {
        ACRA.init(this);
        String currentVersion;
//		gAnalyticsId = activity.getString(R.string.ganalyticsid);
        SharedPreferences prefs;
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
//        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
//        currentVersion = prefs.getString("dbVersionInfo", "-1");
        mActivity = activity;
        mIfAppInited = true;
        mPackageInfo = getPackageInfo();
        mUserInfo = new UserInfo(activity, getString(R.string.appTitle));

        if (ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
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

        ACRA.getErrorReporter().putCustomData("useremail", msGmail);
        ACRA.getErrorReporter().putCustomData("email", msGmail);
        Log.d("MyApplication", "onCreate()");
        sInstance = this;


        String mAppVersion = mPackageInfo.versionName + mPackageInfo.versionCode;
        prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        currentVersion = prefs.getString("dbVersionInfo", "-1");

        mCH = new CallHome(activity, getMyLocation(),
                mPackageInfo, getString(R.string.appNameShort),
                getString(R.string.appCode), mUserInfo);
        if (!currentVersion.equals(mAppVersion)) { // First Use
            mCH.userPing(getString(R.string.atFirstUse), "First Use");
        }
        readPreferences();
    }

    public static synchronized SSApp getInstance() {
        return sInstance;
    }

    public SharedPreferences.Editor getPersistentPreferenceEditor() {
        if (mEditorVersionPersistent == null) {
            mEditorVersionPersistent = getPersistentPreference().edit();
        }
        return mEditorVersionPersistent;
    }

    public SharedPreferences getPersistentPreference() {
        if (mPrefsVersionPersistent == null) {
            mPrefsVersionPersistent = getApplicationContext()
                    .getSharedPreferences(
                            Constants_dp.PREFS_VERSION_PERSISTENT,
                            Context.MODE_PRIVATE);
        }
        return mPrefsVersionPersistent;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
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
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location loc;
        List<String> providers = lm.getAllProviders();
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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


    // Read App global preferences
    public void readPreferences() {
//		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
//		bRegistered = settings.getBoolean(PREFREGISTERED, false);

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
            e.printStackTrace();
            //	SSLog.e(TAG, "mVolleyRequestQueue may not be initialized - ", e);
        }
    }


    public RequestQueue getRequestQueue() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (mVolleyRequestQueue == null) {

            // Instantiate the cache
            Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB
            // cap

            // Set up the network to use HttpURLConnection as the HTTP client.
            Network network = new BasicNetwork(new HurlStack());

            // Instantiate the RequestQueue with the cache and network.
            mVolleyRequestQueue = new RequestQueue(cache, network);
            mVolleyRequestQueue.start();
            mVolleyRequestQueue = Volley
                    .newRequestQueue(getApplicationContext());

            // mVolleyRequestQueue =
            // Volley.newRequestQueue(sInstance.getApplicationContext());

            // Start the queue
            // mVolleyRequestQueue =
            // Volley.newRequestQueue(getApplicationContext());
        }

        return mVolleyRequestQueue;
    }

} // SSTApp