package com.smartshehar.busdriver_apk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.JsonObject;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.SSLog;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by user pc on 25-07-2015.
 */
public class CGlobal_bd {
    private static CGlobal_bd instance;
    public static RequestQueue mVolleyRequestQueue;
    public static final String TAG = "CGlobal_bd: ";
    private Location mCurrentLocation;
    public int miAppUserId;

    // Restrict the constructor from being instantiated
    private CGlobal_bd() {
    }

    public int getAppUserId() {
        return miAppUserId;
    }

    // private static long mlMSElapsed = 0;
    public static synchronized CGlobal_bd getInstance() {
        if (instance == null) {
            instance = new CGlobal_bd();
        }
        return instance;
    }

    private SharedPreferences mPrefsVersionPersistent = null;
    SharedPreferences.Editor mEditorVersionPersistent = null;

    public SharedPreferences getSharedPreferences(Context context) {
        if (mPrefsVersionPersistent == null) {
            mPrefsVersionPersistent = context.getSharedPreferences(
                    Constants_bd.PREFS_VERSION_PERSISTENT,
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

    public void dialogMessage(Context context) {
        new AlertDialog.Builder(context)
                .setMessage("Data not available..!")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void dialogMessageCustom(Context context, String sMassege) {
        new AlertDialog.Builder(context)
                .setMessage(sMassege)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public Map<String, String> checkParams(Map<String, String> map) {
        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> pairs = (Map.Entry<String, String>) it
                    .next();
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

            // mVolleyRequestQueue =
            // Volley.newRequestQueue(sInstance.getApplicationContext());

            // Start the queue
            // mVolleyRequestQueue =
            // Volley.newRequestQueue(getApplicationContext());
        }

        return mVolleyRequestQueue;
    }

    public void setMyLocation(Location location, boolean bForceLocation, final Context context) {
        if (location == null)
            return;
        if (bForceLocation) {
            mCurrentLocation = isBetterLocation(location, mCurrentLocation) ? location
                    : mCurrentLocation;
        } else {
            if (isBetterLocation(location, mCurrentLocation)) {
                mCurrentLocation = isBetterLocation(location, mCurrentLocation) ? location
                        : mCurrentLocation;
            }
        }
        final String jsonLocation = locationToJsonString(mCurrentLocation);
        new Thread(new Runnable() {
            public void run() {
                CGlobal_bd
                        .getInstance()
                        .getSharedPreferencesEditor(context)
                        .putString(Constants_bd.PREF_MY_LOCATION,
                                jsonLocation);
                CGlobal_bd.getInstance().getSharedPreferencesEditor(context)
                        .commit();
            }
        }).start();
    }

    private Location locationFromJsonString(String sJsonLoc) {
        Location loc = null;
        try {
            JSONObject jo = new JSONObject(sJsonLoc);
            loc = new Location(jo.getString("provider"));
            loc.setAccuracy((float) jo.getDouble("accuracy"));
            loc.setLatitude(jo.getDouble("latitude"));
            loc.setLongitude(jo.getDouble("longitude"));
            loc.setSpeed((float) jo.getDouble("speed"));
        } catch (Exception je) {
            SSLog.e(TAG, "locationFromJsonString", je);
        }
        return loc;
    }

    private String locationToJsonString(Location loc) {
        JsonObject jo = new JsonObject();
        jo.addProperty(CGlobals_lib_ss.LATITUDE, loc.getLatitude());
        jo.addProperty(CGlobals_lib_ss.LONGITUDE, loc.getLongitude());
        jo.addProperty(CGlobals_lib_ss.SPEED, loc.getSpeed());

        jo.addProperty(CGlobals_lib_ss.PROVIDER, loc.getProvider());
        jo.addProperty(CGlobals_lib_ss.ACCURACY, loc.getAccuracy());

        return jo.toString();

    }


    public Location getMyLocation(Context context) {
        Location location = getBestLocation(context);
        mCurrentLocation = isBetterLocation(location, mCurrentLocation) ? location
                : mCurrentLocation;
        if (mCurrentLocation == null) {
            String jsonLocation = CGlobal_bd.getInstance()
                    .getSharedPreferences(context)
                    .getString(Constants_bd.PREF_MY_LOCATION, "");
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

    boolean isProviderSupported(String provider) {
        return true;
    }

    private Location getLocationByProvider(Context context, String provider) {
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
                    // if(checkLocationPermission(context)) {
                    if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return null;
                    }
                    location = locationManager.getLastKnownLocation(provider);
                    // }
                }
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "Cannot acces Provider " + provider);
            }
        }
        return location;
    }

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

    int getGPSCheckMilliSecsFromPrefs() {
        return 1000;
    }

    public boolean isBetterLocation(Location location,
                                    Location currentBestLocation) {
        try {
            if (currentBestLocation == null) {
                // A new location is always better than no location
                return true;
            }

            // Check whether the new location fix is newer or older
            long timeDelta = location.getTime() - currentBestLocation.getTime();
            boolean isSignificantlyNewer = timeDelta > Constants_bd.TWO_MINUTES;
            boolean isSignificantlyOlder = timeDelta < -Constants_bd.TWO_MINUTES;
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
                Thread.sleep(500);
            } catch (InterruptedException e) {
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

    public boolean hasTripEnded(Location mCurrentLocation2, double toLat,
                                double toLng, Context context) {
        float results[] = new float[1];

        Location.distanceBetween(toLat, toLng, mCurrentLocation2.getLatitude(),
                mCurrentLocation2.getLongitude(), results);
        if (results[0] < Constants_bd.NEAR_DESTINATION_DISTANCE) {
            /*CGlobal_bd.getInstance().sendTripAction(
                    Constants_driver.TRIP_ACTION_END, context);*/

            return true;
        }
        return false;
    }

    public void sendUpdatePosition(final Context context) {
        final int tripid = CGlobal_bd.getInstance().getSharedPreferences(context)
                .getInt(Constants_bd.PREF_BUS_DRIVER_TRIP_ID, -1);
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_bd.UPDATE_POSITION_BUS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    SSLog.e(TAG, "callBusTrip", error.toString());
                } catch (Exception e) {
                    SSLog.e(TAG, "callBusTrip", e);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params.put("ti", String.valueOf(tripid));
                /*params.put("tripstatus", tripStatus);
                params.put("lat", String.valueOf(location.getLatitude()));
                params.put("lng", String.valueOf(location.getLongitude()));
                params.put("speed", String.valueOf(location.getSpeed()));
                params.put("td", getTripType(context));
                params.put("ta", isInTrip(context) ? "1" : "0");*/
                params = CGlobals_lib.getInstance().getBasicMobileParamsShort(params,
                        Constants_bd.UPDATE_POSITION_BUS_URL, context);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_bd.UPDATE_POSITION_BUS_URL;
                try {
                    String url = url1 + "?" + getParams.toString()
                            + "&verbose=Y";
                    System.out.println("url  " + url);

                } catch (Exception e) {
                    SSLog.e(TAG, "callBusTrip", e);
                }
                return CGlobal_bd.getInstance().checkParams(params);
            }
        };
        CGlobal_bd.getInstance().getRequestQueue(context).add(postRequest);
    }

    public void sendTripStatus(final String sTripStatus, final Context context) {
        final int tripid = CGlobal_bd.getInstance().getSharedPreferences(context)
                .getInt(Constants_bd.PREF_BUS_DRIVER_TRIP_ID, -1);
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_bd.UPDATE_TRIP_STATUS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());
                        MyApplication
                                .getInstance()
                                .getPersistentPreferenceEditor()
                                .putString(Constants_bd.PREF_TRIP_ACTION,
                                        sTripStatus);
                        if (sTripStatus
                                .equals(Constants_bd.TRIP_BEGIN)) {
                            MyApplication
                                    .getInstance()
                                    .getPersistentPreferenceEditor()
                                    .putBoolean(Constants_bd.PREF_IN_TRIP,
                                            true);
                        }
                        if (sTripStatus.equals(Constants_bd.TRIP_END)) {
                            MyApplication
                                    .getInstance()
                                    .getPersistentPreferenceEditor()
                                    .putBoolean(Constants_bd.PREF_IN_TRIP,
                                            false);
                        }
                        MyApplication.getInstance()
                                .getPersistentPreferenceEditor().commit();
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    SSLog.e(TAG, "callBusTrip", error.toString());
                } catch (Exception e) {
                    SSLog.e(TAG, "callBusTrip", e);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params.put("ti", String.valueOf(tripid));
                params.put("ts", sTripStatus);
                params = CGlobals_lib.getInstance().getBasicMobileParamsShort(params,
                        Constants_bd.UPDATE_TRIP_STATUS_URL, context);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_bd.UPDATE_TRIP_STATUS_URL;
                try {
                    String url = url1 + "?" + getParams.toString()
                            + "&verbose=Y";
                    System.out.println("url  " + url);

                } catch (Exception e) {
                    SSLog.e(TAG, "callBusTrip", e);
                }
                return CGlobal_bd.getInstance().checkParams(params);
            }
        };
        CGlobal_bd.getInstance().getRequestQueue(context).add(postRequest);
    }

}
