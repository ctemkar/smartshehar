package com.smartshehar.cabe;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.smartshehar.cabe.ui.CabEMain_act;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.SSLog_SS;

/**
 * Created by jijo_soumen on 22/03/2016.
 * Passenger location service runs when ride is requested and driver is on the way
 */
public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "LocationService: ";
    public static final String BROADCAST_ACTION = "com.smartshehar.cabe.locationservice";
    public Location mCurrentLocation = null, mPreviousLocation = null;
    int nLocationGot;
    private GoogleApiClient mGoogleApiClient;
    Notification mNotification;
    long mCurrentTime = -1;
    protected Handler handler = new Handler();
    int mCabTripId;
    CTripCabE cTrip_cabE;
    Intent mIntent;
    LocationManager mLocationManagerNetwork, mLocationManagerGPS;
    LocationListener mLocationListenerNetwork, mLocationListenerGPS;
    int sendEveryThing = 1;
    String servicecode;

    @Override
    public void onCreate() {
        super.onCreate();
        mCurrentLocation = CGlobals_lib_ss.getInstance().getMyLocation(this);
        mIntent = new Intent(BROADCAST_ACTION);
        nLocationGot = 1;
        String res = CGlobals_lib_ss.getInstance().getPersistentPreference(this)
                .getString(Constants_CabE.CABE_REQUEST_RESPONSE, "");
        cTrip_cabE = new CTripCabE(res, LocationService.this);
        setupService();
        setupLocationListeners();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        handler.postDelayed(runnableUpdatePosition, 0);
        handler.postDelayed(runnableUpdatePositionLocation, 0);
        CGlobals_lib_ss.getInstance().countError1 = 1;
        CGlobals_lib_ss.getInstance().countError2 = 1;
        CGlobals_lib_ss.getInstance().countError3 = 1;
        CGlobals_lib_ss.getInstance().countError4 = 1;
        CGlobals_lib_ss.getInstance().countError5 = 1;
        CGlobals_lib_ss.getInstance().countError6 = 1;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CGlobals_lib_ss.getInstance().isLocationServiceStarted = true;
        mGoogleApiClient.connect();
        if (intent == null) {
            return -1;
        }
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mCabTripId = extras.getInt("tripid");
            servicecode = extras.getString("servicecode");
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(LocationService.this)
                    .putString("SERVICE_CODE", servicecode);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(LocationService.this).commit();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
            mGoogleApiClient.disconnect();
        }
        sendUpdate(mCurrentLocation);
        CGlobals_lib_ss.getInstance().isLocationServiceStarted = false;
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // Update location every second
        mLocationRequest.setFastestInterval(500); // Update location every
        // second
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, mLocationListener);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(this.getClass().getSimpleName(), "onConnectionFailed()");
    }

    void setupService() {
        try {
            setupNotification();
            startForeground(1337, mNotification);
        } catch (Exception e) {
            SSLog_SS.e(TAG, "setupService - ", e, LocationService.this);
        }
    }

    void setupLocationListeners() {
        // Acquire a reference to the system Location Manager
        mLocationManagerNetwork = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        // Define a listener that responds to location updates
        mLocationListenerNetwork = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location
                // provider.
                updateMyLocation(location);
            }
        };
        mLocationManagerGPS = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        mLocationListenerGPS = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location
                // provider.
                updateMyLocation(location);
            }
        };
    }

    protected Intent getViewIntentClass() {
        return new Intent(this, CabEMain_act.class);
    }

    protected String servicetext() {
        return "CabE Driver";
    }

    void setupNotification() {
        // Build intent for notification content
        try {
            Intent viewIntent = getViewIntentClass();
            viewIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            final PendingIntent viewPendingIntent = PendingIntent.getActivity(
                    this, 0, viewIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            getSystemService(NOTIFICATION_SERVICE);
            if (CGlobals_lib_ss.getInstance() != null) {
                Location l = CGlobals_lib_ss.getInstance().getMyLocation(LocationService.this);
                if (l != null) {
                    RemoteViews mContentView = new RemoteViews(getPackageName(), R.layout.notification);
                    mContentView.setImageViewResource(R.id.ivPersonImage, R.mipmap.ic_black_yellow);
                    mContentView.setImageViewResource(R.id.ivDriverImage, R.drawable.ic_driver);
                    mContentView.setTextViewText(R.id.ivDriverName, cTrip_cabE.getName());
                    mContentView.setTextViewText(R.id.ivVehicleNumber, cTrip_cabE.getVehicleNo());
                    // Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                            this).setTicker("Driver")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setAutoCancel(true)
                            .setContentIntent(viewPendingIntent);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mBuilder = mBuilder.setContent(mContentView);
                    } else {
                        mBuilder = mBuilder.setContentTitle(servicetext())
                                .setContentText("CabE Trip active")
                                .setSmallIcon(android.R.drawable.ic_menu_gallery);
                    }
                    mNotification = mBuilder.build();
                }
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG, " setupNotification - ", e, LocationService.this);
        }
    }

    private void updateMyLocation(Location location) {
        // Location.distanceto
        try {
            if (location != null) {
                mCurrentLocation = CGlobals_lib_ss.isBetterLocation(location,
                        mCurrentLocation) ? location : mCurrentLocation;
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG, "updateMyLocation - ", e, LocationService.this);
        }
        boolean isBetterLoc = CGlobals_lib_ss.isBetterLocation(location,
                mCurrentLocation);
        if (isBetterLoc) {
            mPreviousLocation = mCurrentLocation;
            CGlobals_lib_ss.setMyLocation(location, true, this);
            mCurrentLocation = CGlobals_lib_ss.isBetterLocation(location, mCurrentLocation) ? location
                    : mCurrentLocation;
            sendUpdate(location);
            nLocationGot++;
        }
    } // updateMyLocation

    public void sendUpdate(Location location) {
        if (location == null)
            return;
        float results[] = new float[1];
        boolean sendLocation;
        if (mPreviousLocation != null) {
            Location.distanceBetween(location.getLatitude(),
                    location.getLongitude(), mPreviousLocation.getLatitude(),
                    mPreviousLocation.getLongitude(), results);
        }
        sendLocation = mCurrentTime == -1
                || ((System.nanoTime() - mCurrentTime) / 1e9 > 200)
                || (mPreviousLocation == null || results[0] > 5 || location.getSpeed() > 0);
        if (sendLocation) {
            CGlobals_lib_ss.setMyLocation(location, true, this);
            mPreviousLocation = location;
            mCurrentTime = System.nanoTime();
        }
    }

    protected Runnable runnableUpdatePosition = new Runnable() {
        @Override
        public void run() {
            runUpdatePosition();
        }
    };

    protected Runnable runnableUpdatePositionLocation = new Runnable() {
        @Override
        public void run() {
            getrunUpdateLocation();
        }
    };

    public void getrunUpdateLocation() {
        try {
            boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(LocationService.this);
            if (!isInternetCheck) {
                runUpdateLocation();
            } else {
                sendMessage(Constants_CabE.VOLLEY_NETYWORK_ERROR, "");
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG, "runUpdatePosition", e, LocationService.this);
        }
        handler.postDelayed(runnableUpdatePositionLocation,
                Constants_CabE.DRIVER_UPDATE_LOCATION_INTERVAL_USER);
    }

    public void runUpdateLocation() {
        final int itrip_id = CGlobals_lib_ss.getInstance().getPersistentPreference(LocationService.this).
                getInt(Constants_CabE.PREF_TRIP_ID_PASSENGER, -1);
        final String url = Constants_CabE.GET_DRIVER_POSITION_SHORT_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        sendEveryThing = 0;
                        driverLocation(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                sendEveryThing = 1;
                sendMessage(Constants_CabE.VOLLEY_NETYWORK_ERROR, error.getMessage());
                if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                    Log.e(TAG, "");
                } else {
                    SSLog_SS.e(TAG, " getProspects failed:-  ",
                            error, LocationService.this);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tripid", String.valueOf(itrip_id));
                params.put("sendeverything", String.valueOf(sendEveryThing));
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        url, LocationService.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                try {
                    String url1 = url + "?" + getParams.toString();
                    Log.d(TAG, "url  " + url1);
                } catch (Exception e) {
                    Log.e(TAG, Arrays.toString(e.getStackTrace()));
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, LocationService.this);
    }

    public void driverLocation(String response) {
        sendMessage(2, response);
    }

    public void runUpdatePosition() {
        try {
            sendUpdate(mCurrentLocation);
            boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(LocationService.this);
            if (!isInternetCheck) {
                getDriverPosition();
            } else {
                sendMessage(Constants_CabE.VOLLEY_NETYWORK_ERROR, "");
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG, "runUpdatePosition", e, LocationService.this);
        }
        handler.postDelayed(runnableUpdatePosition,
                Constants_CabE.DRIVER_UPDATE_INTERVAL_USER);
    }

    private void sendMessage(int iStatus, String sResponse) {
        Intent intent = new Intent("com.smartshehar.cabe.ui.CabEMain_act");
        intent.putExtra("status", iStatus);
        intent.putExtra("response", sResponse);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (CGlobals_lib_ss.isBetterLocation(location,
                    CGlobals_lib_ss.getInstance().getMyLocation(LocationService.this))) {
                CGlobals_lib_ss.setMyLocation(location, true, LocationService.this);
                updateMyLocation(location);
            }
        }
    };

    private void getDriverPosition() {
        final int itrip_id = CGlobals_lib_ss.getInstance().getPersistentPreference(LocationService.this).
                getInt(Constants_CabE.PREF_TRIP_ID_PASSENGER, -1);
        servicecode = CGlobals_lib_ss.getInstance().getPersistentPreference(LocationService.this)
                .getString("SERVICE_CODE", "");
        final String url = Constants_CabE.GET_DRIVER_POSITION_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        driverPosition(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                sendMessage(Constants_CabE.VOLLEY_NETYWORK_ERROR, error.getMessage());
                if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                    Log.e(TAG, "");
                } else {
                    SSLog_SS.e(TAG, "getProspects failed:- ", error, LocationService.this);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                if (servicecode.equals(Constants_CabE.SERVICE_CODE_ST)) {
                    params.put("triptype", "P");
                } else {
                    params.put("triptype", "A");
                }
                params.put("tripid", String.valueOf(itrip_id));
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        url, LocationService.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                try {
                    String url1 = url + "?" + getParams.toString();
                    Log.d(TAG, "url  " + url1);
                } catch (Exception e) {
                    Log.e(TAG, Arrays.toString(e.getStackTrace()));
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, LocationService.this);
    }

    private void driverPosition(String response) {
        sendMessage(1, response);
    }
}
