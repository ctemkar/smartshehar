package com.smartshehar.cabe.driver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.smartshehar.cabe.driver.ui.CabEDriverForHire_act;
import com.smartshehar.cabe.driver.ui.SplashScreen_Driver_act;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.SSLog_SS;

/**
 * Created by jijo_soumen on 18/03/2016.
 * Driver isInTrip Location Service
 */
public class LocationService_cabe extends Service implements
        TextToSpeech.OnInitListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    protected Handler handler = new Handler();
    private NotificationManager mNM;
    LocationManager mLocationManagerNetwork;
    LocationManager mLocationManagerGPS;
    private TextToSpeech mTts;
    LocationListener mLocationListenerNetwork;
    LocationListener mLocationListenerGPS;
    public Location mCurrentLocation = null, mPreviousLocation = null;
    long mCurrentTime = -1;
    private static final String TAG = "LocationService_cabe: ";
    public static final String BROADCAST_ACTION = "com.smartshehar.cabe.driver.locationservice";
    Intent mIntent;
    int nLocationGot;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Notification mNotification;
    private static int miCurrentWaitingCount = 0, miCurrentJumpInCount = 0;
    float mdDistanceTraveled = 0;
    public double toLat, toLng;
    ArrayList<PassengerProspects> alCTrip;
    DatabaseHandler_CabE db;
    int miTripId;
    boolean iHasJumpOut = false;
    //ArrayList<String> alAddPassenger;
    //List<CTrip_CED> trips;
    //JSONObject jPassenger;

    public int getAppLabelId() {
        return R.string.app_label;
    }

    protected String servicetext() {
        return "CabE Driver";
    }

    public int inTripNotificationImage() {
        return R.mipmap.ic_launcher;
    }

    protected Intent getViewIntentClass() {
        return new Intent(this, CabEDriverForHire_act.class);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mCurrentLocation = CGlobals_lib_ss.getInstance().getMyLocation(this);
        mIntent = new Intent(BROADCAST_ACTION);
        mTts = new TextToSpeech(this, this);
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting. We put an icon in the
        // status bar.
        miCurrentWaitingCount = 0;
        miCurrentJumpInCount = 0;
        CGlobals_lib_ss.mbGPSFix = false;
        nLocationGot = 1;
        setupService();
        setupLocationListeners();
        startLocationListeners();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();

        handler.postDelayed(runnableUpdatePosition, 0);
        alCTrip = new ArrayList<>();
        CGlobals_lib_ss.getInstance().countError1 = 1;
        CGlobals_lib_ss.getInstance().countError2 = 1;
        CGlobals_lib_ss.getInstance().countError3 = 1;
        CGlobals_lib_ss.getInstance().countError4 = 1;
        CGlobals_lib_ss.getInstance().countError5 = 1;
        CGlobals_lib_ss.getInstance().countError6 = 1;
    } // onCreate

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        CGlobals_lib_ss.getInstance().isLocationServiceStarted = true;
        mGoogleApiClient.connect();
        if (intent == null) {
            return -1;
        }
        Bundle extras = intent.getExtras();
        if (extras != null) {
            toLat = extras.getDouble("lat");
            toLng = extras.getDouble("lng");
            miTripId = extras.getInt("tripid");
        }
        CGlobals_lib_ss.moLocStart = null;
        CGlobals_lib_ss.moLocDest = CGlobals_lib_ss.moLocStart;
        CGlobals_lib_ss.getInstance().miTravelDistance = 0;
        return START_STICKY;
    }

    void setupService() {
        try {
            setupNotification();
            startForeground(1337, mNotification);
        } catch (Exception e) {
            SSLog_SS.e(TAG, "setupService - ", e, this);
        }
    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
            mGoogleApiClient.disconnect();
        }
        CGlobals_lib_ss.moLocStart = null;
        CGlobals_lib_ss.moLocDest = null;
        mNM.cancel(getAppLabelId());
        CGlobals_lib_ss.inDriverTrip = false;
        sendUpdate(mCurrentLocation);
        CGlobals_lib_ss.getInstance().isLocationServiceStarted = false;
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
        super.onDestroy();
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

    void startLocationListeners() {
    }

    private void updateMyLocation(Location location) {

        // Location.distanceto
        try {
            if (location != null) {
                mCurrentLocation = CGlobals_lib_ss.isBetterLocation(location,
                        mCurrentLocation) ? location : mCurrentLocation;
            }

            CGlobals_CED.msTripAction = CGlobals_lib_ss.getInstance()
                    .getPersistentPreference(this)
                    .getString(Constants_CED.PREF_TRIP_ACTION, "");
            if (!CGlobals_CED.getInstance().isInTrip(this)) {
                stopSelf();
                miCurrentWaitingCount = 0;
                miCurrentJumpInCount = 0;
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG, "updateMyLocation - ", e, this);
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
            if (CGlobals_lib_ss
                    .isBetterLocation(location, mPreviousLocation)) {
                Log.i(TAG, "sendUpdate");
            }
        }
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
            // CGlobals.getInstance().sendUpdatePosition(location);
            mdDistanceTraveled =
                    CGlobals_lib_ss.getInstance().getPersistentPreference(this).
                            getFloat(Constants_CED.PREF_TRIP_DISTANCE_TRAVELED,
                                    (float) 0);
            mdDistanceTraveled = mdDistanceTraveled + results[0];
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(this).
                    putFloat(Constants_CED.PREF_TRIP_DISTANCE_TRAVELED, mdDistanceTraveled);
            mPreviousLocation = location;
            mCurrentTime = System.nanoTime();

        }
        for (PassengerProspects p : alCTrip) {
            if (!iHasJumpOut) {
                p.updateDistancePassenger(location);
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(this.getClass().getSimpleName(), "onConnectionFailed()");

    }

    @Override
    public void onConnected(Bundle arg0) {
        mLocationRequest = LocationRequest.create();
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
                Location l = CGlobals_lib_ss.getInstance().getMyLocation(LocationService_cabe.this);
                if (l != null) {
                   /* Toast.makeText(this, this.getString(R.string.tripActive_cabe),
                            Toast.LENGTH_LONG).show();*/
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                            this).setContentTitle(servicetext())
                            .setContentText("Your trip is now active")
                            .setSmallIcon(inTripNotificationImage())
                            .setContentIntent(viewPendingIntent);
                    mNotification = mBuilder.build();
                }
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG, " setupNotification - ", e, LocationService_cabe.this);
        }
    }

    protected Runnable runnableUpdatePosition = new Runnable() {
        @Override
        public void run() {
            runUpdatePosition();
        }
    };

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Change this to match your
            // locale
            mTts.setLanguage(Locale.US);
        } else {
            Log.i(TAG, "onInit");
        }
    }

    /*public void selectMissingLatLon() {
        db = new DatabaseHandler_CabE(this);
        ArrayList<MissingLocation_CabE> pPResultsList = db.getSentToServer();
        if (pPResultsList != null) {
            if (pPResultsList.size() > 0) {
                boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(LocationService_cabe.this);
                if (!isInternetCheck) {
                    CGlobals_CED.getInstance().sendMissingLatLon(pPResultsList, LocationService_cabe.this);
                }
            }
        }
    }*/

    @Override
    public void onConnectionSuspended(int arg0) {


    }

    public void sendUpdatePosition(Location location) {
        CGlobals_CED.getInstance().sendUpdatePosition(location, LocationService_cabe.this);
    }

    private void sendMessage(String message, int errorvalue) {
        Intent intent = new Intent(Constants_CED.PASSENGER_EVENT);
        intent.putExtra("response", message);
        intent.putExtra("errorvalue", errorvalue);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (CGlobals_lib_ss.isBetterLocation(location,
                    CGlobals_lib_ss.getInstance().getMyLocation(LocationService_cabe.this))) {
                CGlobals_lib_ss.setMyLocation(location, true, LocationService_cabe.this);
                updateMyLocation(location);
            }
        }
    };

    public void runUpdatePosition() {
        try {
            sendUpdatePosition(mCurrentLocation);
            sendUpdate(mCurrentLocation);
            final String sUserType = CGlobals_lib_ss.getInstance().getPersistentPreference(LocationService_cabe.this).
                    getString(Constants_CED.CAB_TRIP_USER_TYPE_DRIVER, "");
            if (sUserType.equals("P")) {
                sendSharedPassengerUpdate();
            } else {
                sendMessage("", 0);
            }
            //selectMissingLatLon();
        } catch (Exception e) {
            SSLog_SS.e(TAG, "runnableService", e, LocationService_cabe.this);
        }
        handler.postDelayed(runnableUpdatePosition,
                Constants_CED.DRIVER_UPDATE_INTERVAL);
    }

    public void sendSharedPassengerUpdate() {
        final int cabtripid = CGlobals_lib_ss.getInstance().getPersistentPreference(LocationService_cabe.this).
                getInt(Constants_CED.PREF_TRIP_ID_INT, -1);
        final String sUserType = CGlobals_lib_ss.getInstance().getPersistentPreference(LocationService_cabe.this).
                getString(Constants_CED.CAB_TRIP_USER_TYPE_DRIVER, "");

        final String url = Constants_CED.GET_SHARED_INFO_AFTER_TRIP_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (TextUtils.isEmpty(response) || response.equals("-1")) {
                            sendMessage("", 0);
                            return;
                        }
                        try {
                            sendMessage(response, 1);
                        } catch (Exception e) {
                            sendMessage("", 0);
                            e.printStackTrace();
                        }
                        // response
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                sendMessage("", 0);
                if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                    Log.e(TAG, "");
                } else {
                    SSLog_SS.e(TAG, "sendUserAccess :-   ", error, LocationService_cabe.this);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tripid", String.valueOf(cabtripid));
                switch (sUserType) {
                    case "A":
                        params.put("triptype", "A");
                        break;
                    case "K":
                        params.put("triptype", "K");
                        break;
                    case "S":
                        params.put("triptype", "S");
                        break;
                    case "W":
                        params.put("triptype", "W");
                        break;
                    case "P":
                        params.put("triptype", "P");
                        break;
                }
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        url, LocationService_cabe.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                try {
                    String url1 = url + "?" + getParams.toString();
                    System.out.println("url GET_SHARED_INFO  " + url1);
                } catch (Exception e) {
                    Log.e(TAG, Arrays.toString(e.getStackTrace()));
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, LocationService_cabe.this);
    }
}

class PassengerProspects {
    double dLat = 0.0;
    double dLng = 0.0;
    int sAppUserId = -1;
    double dDistance;

    public void setLAT(double dLat) {
        this.dLat = dLat;
    }

    public double getLAT() {
        return dLat;
    }

    public void setLNG(double dLng) {
        this.dLng = dLng;
    }

    public double getLNG() {
        return dLng;
    }

    public double getDistances() {
        return dDistance;
    }

    public void setAppUserId(int sAppUserId) {
        this.sAppUserId = sAppUserId;
    }

    public void updateDistancePassenger(Location location) {
        PassengerProspects passengerProspects = new PassengerProspects();
        float results[] = new float[1];
        if (location != null) {
            Location.distanceBetween(location.getLatitude(),
                    location.getLongitude(), passengerProspects.getLAT(),
                    passengerProspects.getLNG(), results);
            dDistance += results[0];
        }
    }
}
