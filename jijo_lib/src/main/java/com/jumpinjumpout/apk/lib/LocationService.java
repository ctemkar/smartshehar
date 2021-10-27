package com.jumpinjumpout.apk.lib;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public abstract class LocationService extends Service implements
        TextToSpeech.OnInitListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    protected Handler handler = new Handler();
    private NotificationManager mNM;
    LocationManager mLocationManagerNetwork;
    LocationManager mLocationManagerGPS;
    private TextToSpeech mTts;
    LocationListener mLocationListenerNetwork;
    LocationListener mLocationListenerGPS;
    CGlobals_lib mApp;
    public Location mCurrentLocation = null, mPreviousLocation = null;
    long mCurrentTime = -1;
    private static final String TAG = "LocationService";
    public static final String BROADCAST_ACTION = "com.jumpinjumpout.apk.locationservice";
    Intent mIntent;
    int nLocationGot;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    Notification mNotification;
    private static int miCurrentWaitingCount = 0, miCurrentJumpInCount = 0;
    private float mdDistanceTraveled = 0;
    public double toLat, toLng;
    ArrayList<PassengerProspects> alCTrip;
    DatabaseHandler db;
    int miTripId;
    boolean iHasJumpOut = false;

    abstract protected void runUpdatePosition();

    abstract protected void runCheckNotification();

    abstract protected int getAppLabelId();

    abstract protected String passengerProspectsUrl();

    abstract protected String sendMissingLocationsUrl();

    abstract protected int inTripNotificationImage();

    abstract protected Intent getViewIntentClass();

    abstract protected String servicetext();

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mCurrentLocation = CGlobals_lib.getInstance().getMyLocation(this);
        mIntent = new Intent(BROADCAST_ACTION);
        mTts = new TextToSpeech(this, this);
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting. We put an icon in the
        // status bar.
        mApp = CGlobals_lib.getInstance();
        miCurrentWaitingCount = 0;
        miCurrentJumpInCount = 0;
        CGlobals_lib.mbGPSFix = false;
        nLocationGot = 1;
        setupService();
        setupLocationListeners();
        startLocationListeners();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();

        handler.postDelayed(runnableUpdatePosition, Constants_lib.DRIVER_UPDATE_INTERVAL_USER);
        handler.postDelayed(runnableCheckNotification, Constants_lib.DRIVER_CHECK_NOTIFICATION_USER);
        alCTrip = new ArrayList<PassengerProspects>();
    } // onCreate

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        CGlobals_lib.getInstance().isLocationServiceStarted = true;
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
        CGlobals_lib.moLocStart = null;
        CGlobals_lib.moLocDest = CGlobals_lib.moLocStart;
        CGlobals_lib.getInstance().miTravelDistance = 0;
        return START_STICKY;
    }

    void setupService() {
        try {
            setupNotification();
            startForeground(1337, mNotification);
        } catch (Exception e) {
            SSLog.e(TAG, "setupService - ", e);
        }
    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
            mGoogleApiClient.disconnect();
        }
        CGlobals_lib.moLocStart = null;
        CGlobals_lib.moLocDest = null;
        mNM.cancel(getAppLabelId());
        CGlobals_lib.inDriverTrip = false;
        sendUpdate(mCurrentLocation);
        CGlobals_lib.getInstance().isLocationServiceStarted = false;
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
                mCurrentLocation = CGlobals_lib.getInstance().isBetterLocation(location,
                        mCurrentLocation) ? location : mCurrentLocation;
            }

            CGlobals_lib.msTripAction = CGlobals_lib.getInstance()
                    .getSharedPreferences(this)
                    .getString(Constants_lib.PREF_TRIP_ACTION, "");
            if (!CGlobals_lib.getInstance().isInTrip(this)) {
                stopSelf();
                miCurrentWaitingCount = 0;
                miCurrentJumpInCount = 0;
            }
        } catch (Exception e) {
            SSLog.e(TAG, "updateMyLocation - ", e);
        }
        boolean isBetterLoc = CGlobals_lib.getInstance().isBetterLocation(location,
                mCurrentLocation);
        if (isBetterLoc) {
            mPreviousLocation = mCurrentLocation;
            CGlobals_lib.getInstance().setMyLocation(location, true);
            mCurrentLocation = CGlobals_lib.getInstance().isBetterLocation(location, mCurrentLocation) ? location
                    : mCurrentLocation;
            sendUpdate(location);
            nLocationGot++;
        }

        return;
    } // updateMyLocation

    public void sendUpdate(Location location) {
        if (location == null)
            return;
        float results[] = new float[1];
        boolean sendLocation = false;
        if (mPreviousLocation != null) {
            if (CGlobals_lib.getInstance()
                    .isBetterLocation(location, mPreviousLocation)) {
                sendLocation = true;
            }
        }
        if (mPreviousLocation != null && location != null) {
            Location.distanceBetween(location.getLatitude(),
                    location.getLongitude(), mPreviousLocation.getLatitude(),
                    mPreviousLocation.getLongitude(), results);

        }

        if (mCurrentTime == -1
                || ((System.nanoTime() - mCurrentTime) / 1e9 > 200)
                || (mPreviousLocation == null || results[0] > 5 || location.getSpeed() > 0)) {
            sendLocation = true;
        } else {
            sendLocation = false;
        }
        if (sendLocation) {
            if (location != null) {
                CGlobals_lib.getInstance().setMyLocation(location, true);
                // CGlobals.getInstance().sendUpdatePosition(location);
                mdDistanceTraveled =
                        CGlobals_lib.getInstance().getSharedPreferences(this).getFloat(Constants_lib.PREF_TRIP_DISTANCE_TRAVELED,
                                (float) 0);
                mdDistanceTraveled = mdDistanceTraveled + results[0];
                CGlobals_lib.getInstance().getSharedPreferencesEditor(this).putFloat(Constants_lib.PREF_TRIP_DISTANCE_TRAVELED, mdDistanceTraveled);
                sendUpdatePosition(location);
                mPreviousLocation = location;
                sendLocation = false;
            }
            mCurrentTime = System.nanoTime();

        }
        for (PassengerProspects p : alCTrip) {
            if (!iHasJumpOut) {
                p.updateDistancePassenger(location);
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(this.getClass().getSimpleName(), "onConnectionFailed()");

    }

    @Override
    public void onConnected(Bundle arg0) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // Update location every second
        mLocationRequest.setFastestInterval(500); // Update location every
        // second
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
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
            if (CGlobals_lib.getInstance() != null) {
                Location l = CGlobals_lib.getInstance().getMyLocation(LocationService.this);
                if (l != null) {
                    Toast.makeText(this, this.getString(R.string.tripActive),
                            Toast.LENGTH_LONG).show();
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                            this).setContentTitle(servicetext())
                            .setContentText("Your trip is now active")
                            .setSmallIcon(inTripNotificationImage())
                            .setContentIntent(viewPendingIntent);
                    mNotification = mBuilder.build();
                }
            }
        } catch (Exception e) {
            SSLog.e(TAG, " setupNotification - ", e);
        }
    }

    protected boolean isRunningrunnableUpdatePosition = false;

    protected Runnable runnableUpdatePosition = new Runnable() {
        @Override
        public void run() {
            if (isRunningrunnableUpdatePosition)
                return;
            isRunningrunnableUpdatePosition = true;
            runUpdatePosition();
        }
    };

    protected Runnable runnableCheckNotification = new Runnable() {
        @Override
        public void run() {
            runCheckNotification();
        }
    };

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Change this to match your
            // locale
            mTts.setLanguage(Locale.US);
        } else {
        }
    }

    boolean isGettingProspects = false;

    public void sendMissingLocations() {
        // write latest lat lng to table
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault());
            db = new DatabaseHandler(this);
            Date locationDateTime = new Date(mCurrentLocation.getTime());
            MissingLocation missingLocation = new MissingLocation(mCurrentLocation.getLatitude(),
                    mCurrentLocation.getLongitude(), mCurrentLocation.getAccuracy(),
                    mCurrentLocation.getAltitude(), mCurrentLocation.getBearing(),
                    mCurrentLocation.getSpeed(), df.format(locationDateTime),
                    mCurrentLocation.getProvider(), 0);
            db.addMissingLocation(missingLocation);
            // read last not sent to server row
            ArrayList<MissingLocation> pPResultsList = db.getSentToServer(miTripId);
            Gson gson = new Gson();
            final String json = gson.toJson(pPResultsList);
            if ((pPResultsList.size() - 1) == 0) {
                getProspects();
                return;
            }
            // check if max id is the same as this id
            // if not then write from last sent to server till less than max id through another php
            final String url = sendMissingLocationsUrl();
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                db.updateSentToServer(1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            // response
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    sendMessage(Constants_lib.VOLLEY_NETYWORK_ERROR, error.getMessage());
                    SSLog.e(TAG, " sendMissingLocations failed:-  ",
                            error.getMessage());
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("path", json);
                    params = CGlobals_lib.getInstance().getBasicMobileParams(params,
                            url, LocationService.this);
                    String delim = "";
                    StringBuilder getParams = new StringBuilder();
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        getParams.append(delim + entry.getKey() + "=" + entry.getValue());
                        delim = "&";
                    }
                    try {
                        String url1 = url + "?" + getParams.toString() + "&verbose=Y";
                        System.out.println("url  " + url1);
                    } catch (Exception e) {
                        Log.e(TAG, e.getStackTrace().toString());
                    }
                    return CGlobals_lib.getInstance().checkParams(params);
                }
            };
            CGlobals_lib.getInstance().addVolleyRequest(postRequest, false, LocationService.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // getPassengers

    public void getProspects() {
        // write latest lat lng to table

        if (isGettingProspects) {
            return;
        }
        // read last not sent to server row
        // check if max id is the same as this id
        // if not then write from last sent to server till less than max id through another php
        isGettingProspects = true;
        final String url = passengerProspectsUrl();
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        try {
                            db.updateSentToServer(1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        isGettingProspects = false;
                        prospects(response);
                        CGlobals_lib.getInstance().isNewNotification = false;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                sendMessage(Constants_lib.VOLLEY_NETYWORK_ERROR, error.getMessage());
                isGettingProspects = false;
                SSLog.e(TAG, " getProspects failed:-  ",
                        error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("ti", String.valueOf(miTripId));
                params.put("di", Double
                        .toString(Constants_lib.DEFAULT_WALKING_DISTANCE));
                params = CGlobals_lib.getInstance().getBasicMobileParamsShort(params,
                        url, LocationService.this);

                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "=" + entry.getValue());
                    delim = "&";

                }
                try {
                    String url1 = url + "?" + getParams.toString() + "&verbose=Y";
                    System.out.println("url  " + url1);
                } catch (Exception e) {
                    Log.e(TAG, e.getStackTrace().toString());
                }


                return CGlobals_lib.getInstance().checkParams(params);
            }
        };
        CGlobals_lib.getInstance().addVolleyRequest(postRequest, false, LocationService.this);
    } // getPassengers

    void prospects(String result) {
        CTrip item = null;
        int iAreWaiting = 0;
        int iHaveJumpedIn = 0;
        try {
            sendMessage(1, result);
            if (result.trim().equals("0")) {
                SSLog.d(TAG, "No propsective passengers");
                if (miCurrentWaitingCount > 0) {
                    String sSpeech = "1 passenger dropped out. No passengers.";
                    say(sSpeech);
                    miCurrentWaitingCount = 0;
                }

                return;
            }
            Log.d("Passeger pro Response: ", result);
            float results[] = new float[1];
            ArrayList<String> alAddPassenger = new ArrayList<String>();
            JSONArray majActiveUsers = new JSONArray(result);
            ;
            List<CTrip> trips = new ArrayList<CTrip>();
            ;
            JSONObject jPassenger = new JSONObject();
            int nPassengers = majActiveUsers.length();
            trips.clear();
            for (int j = 0; j < nPassengers; j++) {
                jPassenger = majActiveUsers.getJSONObject(j);
                item = new CTrip(majActiveUsers.getJSONObject(j)
                        .toString(), getApplicationContext());
                trips.add(item);
                Location.distanceBetween(mCurrentLocation.getLatitude(),
                        mCurrentLocation.getLongitude(), item.getLatLng().latitude,
                        item.getLatLng().longitude, results);
                if (item.hasJoined()) {
                    if (!item.hasJumpedIn()) {
                        iAreWaiting++;
                        alAddPassenger.add(String.valueOf(iAreWaiting));
                    }
                }
                if (item.hasJumpedIn() && !item.hasJumpedOut()) {
                    mPreviousLocation = CGlobals_lib.getInstance().getMyLocation(this);
                    PassengerProspects passengerProspects = new PassengerProspects();
                    passengerProspects.setLAT(mPreviousLocation.getLatitude());
                    passengerProspects.setLNG(mPreviousLocation.getLongitude());
                    passengerProspects.setAppUserId(item.getAppUserId());
                    alCTrip.add(passengerProspects);
                    iHasJumpOut = false;
                    iHaveJumpedIn++;
                } else if (item.hasJumpedOut()) {
                    iHasJumpOut = true;
                    Log.d("jumpout", "");
                }

            } // for loop
            String sSpeech = "";
            if (iAreWaiting != miCurrentWaitingCount) {
                int iDiff = iAreWaiting - miCurrentWaitingCount;
                int iDiffJumpin = iHaveJumpedIn - miCurrentJumpInCount;
                if (iDiff > 0) {
                    sSpeech = iDiff + "new passenger";

                    if (iAreWaiting > 1) {
                        sSpeech += ", " + iAreWaiting + getString(R.string.waiting);
                    }
                    say(sSpeech);
                } else {
                    if (iDiff + iDiffJumpin < 0) {
                        sSpeech = Math.abs(iDiff) + " passenger dropped out";
                        say(sSpeech);

                    } else {
                        if (iDiffJumpin > 0) {
                            if (iAreWaiting > 0) {
                                sSpeech += "Now, " + iAreWaiting + getString(R.string.waiting);
                                say(sSpeech);
                            }
                        }

                    }
                }
                if (iAreWaiting == 0) {
                    sSpeech = " No passengers waiting ";
                    say(sSpeech);
                }
            }
            String sDisSpeech;
            if (results[0] == 50) {
                sDisSpeech = "About " + String.format("%.2f", results[0] / 1000) + " km. from you";
                say(sDisSpeech);
            }
        } catch (Exception e) {
            SSLog.e(TAG, "prospects()", e);
        }
        miCurrentWaitingCount = iAreWaiting;
        miCurrentJumpInCount = iHaveJumpedIn;
        CGlobals_lib.getInstance().getSharedPreferencesEditor(this)
                .putInt(Constants_lib.PREF_CURRENT_WAITING_COUNT, miCurrentWaitingCount);
        CGlobals_lib.getInstance().getSharedPreferencesEditor(this)
                .putInt(Constants_lib.PREF_CURRENT_JUMP_COUNT, miCurrentJumpInCount);
        CGlobals_lib.getInstance().getSharedPreferencesEditor(this).commit();
    } // prospects()

    @SuppressWarnings("deprecation")
    void say(String sSay) {
        HashMap<String, String> hash = new HashMap<String, String>();
        hash.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
                String.valueOf(AudioManager.STREAM_NOTIFICATION));
        mTts.speak(sSay, TextToSpeech.QUEUE_ADD, hash);

    }

    @Override
    public void onConnectionSuspended(int arg0) {


    }

    abstract protected void sendUpdatePosition(Location location);

    public String getVolleyError(VolleyError error) {
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
            return this.getString(R.string.retry_internet);
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

    private void sendMessage(int iStatus, String sResponse) {
        PassengerProspects passengerProspects = new PassengerProspects();
        Intent intent = new Intent(Constants_lib.PASSENGER_EVENT);
        intent.putExtra("status", iStatus);
        intent.putExtra("response", sResponse);
        intent.putExtra("passengerDistance", passengerProspects.getDistances());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (CGlobals_lib.getInstance().isBetterLocation(location,
                    CGlobals_lib.getInstance().getMyLocation(LocationService.this))) {
                CGlobals_lib.getInstance().setMyLocation(location, true);
                updateMyLocation(location);
            }
        }
    };
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

    public int getAppUserId() {
        return sAppUserId;
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
