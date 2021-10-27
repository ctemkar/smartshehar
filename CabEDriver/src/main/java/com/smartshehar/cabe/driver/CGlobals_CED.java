package com.smartshehar.cabe.driver;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Constants_lib_ss;
import lib.app.util.SSLog_SS;
import lib.app.util.UserInfo;

@SuppressWarnings("deprecation")
public class CGlobals_CED {
    public static final String TAG = "CGlobals: ";
    private static CGlobals_CED instance;

    // Restrict the constructor from being instantiated
    private CGlobals_CED() {
    }

    public static synchronized CGlobals_CED getInstance() {
        if (instance == null) {
            instance = new CGlobals_CED();
        }
        return instance;
    }

    public static String msTripAction = "";
    public static int SERVICE_DB = 1;
    public static int SERVICE_CMD = 2;
    public static int SERVICE_CURRENT = -1;
    private DatabaseHandler_CabE db;

    public static boolean mbIsAdmin;
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
    public static int miTravelDistance;

    public void init(Context act) {
        msPhoneNo = MyApplication_CED.getInstance().getPersistentPreference()
                .getString(Constants_CED.PREF_PHONENO, "");
        msCountryCode = MyApplication_CED.getInstance().getPersistentPreference()
                .getString(Constants_CED.PREF_COUNTRY_CODE, "");
        mbAppInited = false;
        bStopsRead = false;
        mdTravelDistance = 0;
        miTravelDistance = 0;
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
        String currentVersion = MyApplication_CED.getInstance()
                .getPersistentPreference().getString("dbVersionInfo", "-1");
        if (!msAppVersionName.equals(currentVersion)) {

            MyApplication_CED.getInstance().getPersistentPreferenceEditor()
                    .putString("dbVersionInfo", msAppVersionName);
            MyApplication_CED.getInstance().getPersistentPreferenceEditor().commit();

            MyApplication_CED.getInstance().getPersistentPreferenceEditor()
                    .putString("dbVersionInfo", currentVersion);

            MyApplication_CED.getInstance().getPersistentPreferenceEditor()
                    .commit();

        }
        mCurrentVersion = MyApplication_CED.getInstance().getPersistentPreference()
                .getString("dbVersionInfo", "-1");
        mUserInfo = new UserInfo(act,
                act.getString(R.string.app_label));
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

    public static boolean isNullNotDefined(JSONObject jo, String jkey) {
        return !jo.has(jkey) || jo.isNull(jkey);
    }

    public void sendUpdatePosition(final Location location, final Context context) {
        final String url = Constants_CED.UPDATE_POSITON_DRIVER_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("null")) { // write soumen
                            return;
                        } else if (TextUtils.isEmpty(response)) { // write soumen
                            return;
                        }
                        Log.d("Response: ", "UPDATE_POSITION_URL - " +
                                response);
                        SSLog_SS.d("Response CGlobals_driver: sebdUpdatePosition - ",
                                response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                try {
                    Location mCurrentLocation = CGlobals_lib_ss.getInstance().getMyLocation(context);
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault());
                    Calendar cal = Calendar.getInstance();
                    String sDateTime = df.format(cal.getTime());
                    db = new DatabaseHandler_CabE(context);
                    Date locationDateTime = new Date(mCurrentLocation.getTime());
                    MissingLocation_CabE missingLocation = new MissingLocation_CabE(sDateTime, mCurrentLocation.getLatitude(),
                            mCurrentLocation.getLongitude(), mCurrentLocation.getAccuracy(),
                            mCurrentLocation.getAltitude(), mCurrentLocation.getBearing(),
                            mCurrentLocation.getSpeed(), df.format(locationDateTime),
                            mCurrentLocation.getProvider(), 0);
                    db.addMissingLocation(missingLocation);
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG, "sendUpdatePosition - ",
                                error, context);
                    }
                } catch (Exception e) {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG, "sendUpdatePosition - ", e, context);
                    }
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params = CGlobals_lib_ss.getInstance().getBasicMobileParamsShort(params,
                        url, context);
                CGlobals_lib_ss.setMyLocation(location, false, context);
                return checkParams(params);
            }
        };
        try {
            CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, context);
        } catch (Exception e) {
            SSLog_SS.e(TAG, "mVolleyRequestQueue may not be initialized - ", e, context);
        }
    } // sendUpdatePosition

    public void sendMissingLatLon(final ArrayList<MissingLocation_CabE> pPResultsList, final Context context) {
        Gson gson = new Gson();
        final String json = gson.toJson(pPResultsList);
        final String url = Constants_CED.MISSING_LOCATION_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            db.deleteMissingLocation(pPResultsList.get(0).getCurrent_Datetime());
                        } catch (Exception e) {
                            SSLog_SS.e(TAG, " MissingLoc:- ", e, context);
                        }
                        // response
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                    Log.e(TAG, "");
                } else {
                    SSLog_SS.e(TAG, " sendMissingLocations failed:-  ",
                            error, context);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("path", json);
                params = CGlobals_lib_ss.getInstance().getBasicMobileParamsShort(params,
                        url, context);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                try {
                    String url1 = url + "?" + getParams.toString();
                    System.out.println("url  " + url1);
                } catch (Exception e) {
                    Log.e(TAG, Arrays.toString(e.getStackTrace()));
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, context);
    }

    private String getVolleyError(VolleyError error) {
        if (error instanceof NetworkError) {
            return "NetworkError: " + error.getMessage();
        } else if (error instanceof ServerError) {
            return "ServerError: " + error.getMessage();
        } else if (error instanceof AuthFailureError) {
            return "AuthFailureError: " + error.getMessage();
        } else if (error instanceof ParseError) {
            return "ParseError: " + error.getMessage();
        } else if (error instanceof TimeoutError) {
            return "TimeoutError: " + error.getMessage();
        } else {
            return error.getMessage();
        }
    }

    private String tripActionUrl = "";

    /*public void sendTripAction(final String sTripAction, final Context context) {
        final int tripid = CGlobals_lib_ss.getInstance().getPersistentPreference(context)
                .getInt(Constants_CED.PREF_TRIP_ID_INT, -1);
        final String sUserType = CGlobals_lib_ss.getInstance().getPersistentPreference(context).
                getString(Constants_CED.CAB_TRIP_USER_TYPE_DRIVER, "");
        tripActionUrl = Constants_CED.TRIP_ACTION_DRIVER_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                tripActionUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response: TRIP_ACTION_URL - " + response);
                int setBusyCreateTripDriver = CGlobals_lib_ss.getInstance().getPersistentPreference(context)
                        .getInt(Constants_CED.PERF_SET_BUSY_CREATE_TRIP_DRIVER, 0);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                String sDateTime = df.format(cal.getTime());
                if (setBusyCreateTripDriver == 2 && sTripAction.equals(Constants_CED.TRIP_ACTION_END)) {
                    CGlobals_CED.getInstance().sendFlag(2, sDateTime, context);
                } else if (sTripAction.equals(Constants_CED.TRIP_ACTION_END)) {
                    CGlobals_CED.getInstance().sendFlag(1, sDateTime, context);
                }

                CGlobals_lib_ss
                        .getInstance()
                        .getPersistentPreferenceEditor(context)
                        .putString(Constants_CED.PREF_TRIP_ACTION,
                                sTripAction);
                if (sTripAction
                        .equals(Constants_CED.TRIP_ACTION_BEGIN)) {
                    CGlobals_lib_ss
                            .getInstance()
                            .getPersistentPreferenceEditor(context)
                            .putBoolean(Constants_CED.PREF_IN_TRIP,
                                    true);
                }
                if (msTripAction.equals(Constants_CED.TRIP_ACTION_END)) {
                    CGlobals_lib_ss
                            .getInstance()
                            .getPersistentPreferenceEditor(context)
                            .putBoolean(Constants_CED.PREF_IN_TRIP,
                                    false);
                }
                CGlobals_lib_ss.getInstance()
                        .getPersistentPreferenceEditor(context).commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                String sDateTime = df.format(cal.getTime());
                Location location = CGlobals_lib_ss.getInstance().getMyLocation(context);
                int appuserid = CGlobals_lib_ss.getInstance().getPersistentPreference(context)
                        .getInt(Constants_lib_ss.PREF_APPUSERID, -1);
                db = new DatabaseHandler_CabE(context);
                db.addTripAction(location, tripid, appuserid, sUserType, sTripAction,
                        CGlobals_lib_ss.msGmail, sDateTime);
                int setBusyCreateTripDriver = CGlobals_lib_ss.getInstance().getPersistentPreference(context)
                        .getInt(Constants_CED.PERF_SET_BUSY_CREATE_TRIP_DRIVER, 0);
                if (setBusyCreateTripDriver == 2 && sTripAction.equals(Constants_CED.TRIP_ACTION_END)) {
                    CGlobals_CED.getInstance().sendFlag(2, sDateTime, context);
                } else if (sTripAction.equals(Constants_CED.TRIP_ACTION_END)) {
                    CGlobals_CED.getInstance().sendFlag(1, sDateTime, context);
                }
                // error
                SSLog_SS.e(TAG, "sendTripAction :-   ", error, context);

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tripid", String.valueOf(tripid));
                params.put("triptype", sUserType);
                params.put("tripaction", sTripAction);
                params = CGlobals_lib_ss.getInstance().getBasicMobileParamsShort(params, tripActionUrl, context);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                try {
                    String url1 = tripActionUrl + "?" + getParams.toString() + "&verbose=Y";
                    Log.d(TAG, url1);
                    SSLog_SS.d(TAG, url1);
                    System.out.println("url1 : " + url1);
                } catch (Exception e) {
                    Log.e(TAG, Arrays.toString(e.getStackTrace()));
                }
                return checkParams(params);
            }

        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, context);
    }*/ // sendTripAction

    public Map<String, String> checkParams(Map<String, String> map) {
        for (Entry<String, String> pairs : map.entrySet()) {
            if (pairs.getValue() == null || TextUtils.isEmpty(pairs.getValue())) {
                map.put(pairs.getKey(), "");
            }
        }
        return map;
    }

    public boolean isInTrip(Context context) {
        boolean bIsInTrip = false;
        try {

            msTripAction = CGlobals_lib_ss.getInstance().getPersistentPreference(context)
                    .getString(Constants_CED.PREF_TRIP_ACTION,
                            Constants_CED.TRIP_ACTION_NONE);
            boolean isTripActive = CGlobals_lib_ss.getInstance().getPersistentPreference(context).getBoolean(
                    Constants_CED.PREF_IN_TRIP, false);
            bIsInTrip = !(!isTripActive || (msTripAction.equals(Constants_CED.TRIP_ACTION_END)
                    || msTripAction.equals(Constants_CED.TRIP_ACTION_ABORT)));

        } catch (Exception e) {
            SSLog_SS.e(TAG, " isInTrip ", e, context);
        }
        return bIsInTrip;
    }

    /*public void sendFlag(final int flagValue, final String saveDateTime, final Context context) {
        final String url = Constants_CED.SET_FOR_HIRE_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("response" + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                String sDateTime = df.format(cal.getTime());
                int appuserid = CGlobals_lib_ss.getInstance().getPersistentPreference(context)
                        .getInt(Constants_lib_ss.PREF_APPUSERID, -1);
                db = new DatabaseHandler_CabE(context);
                db.addFlagSetDriver(String.valueOf(flagValue), sDateTime, appuserid, CGlobals_lib_ss.msGmail,
                        CGlobals_lib_ss.mIMEI);
                SSLog_SS.e(TAG, "sendFlag :-   ",
                        error, context);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("isforhire", String.valueOf(flagValue));
                params.put("fhdt", saveDateTime);
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params, url,
                        context);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                String debugUrl;
                try {
                    debugUrl = url + "?" + getParams.toString() + "&verbose=Y";
                    Log.e(TAG, debugUrl);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "getPassengers map - ", e, context);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, context);
    } */// sendForHire

   /* public void goLogout(final Context context) {
        final String spShiftid = CGlobals_lib_ss.getInstance().getPersistentPreference(context)
                .getString(Constants_CED.PREF_SHIFT_ID, "");
        final String spVehicleid = CGlobals_lib_ss.getInstance().getPersistentPreference(context)
                .getString(Constants_CED.PREF_VEHICLE_ID, "");
        final String url = Constants_CED.DRIVER_END_SHIFT_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String sErr = CGlobals_lib_ss.getInstance().getVolleyError(error);
                try {
                    SSLog_SS.d(TAG,
                            "Failed to mVehicleNoVerify :-  "
                                    + error.getMessage());
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "mVehicleNoVerify - " + sErr,
                            e, context);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("shiftid", spShiftid);
                params.put("vehicleid", spVehicleid);
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        url, context);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                String debugUrl;
                try {
                    debugUrl = url + "?" + getParams.toString() + "&verbose=Y";
                    Log.e(TAG, debugUrl);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "getPassengers map - ", e, context);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, context);
    }*/

} // CGlobals

