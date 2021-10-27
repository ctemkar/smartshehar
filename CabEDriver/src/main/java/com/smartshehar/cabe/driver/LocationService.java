package com.smartshehar.cabe.driver;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
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
import com.smartshehar.cabe.driver.ui.CabEMainDriver_act;
import com.smartshehar.cabe.driver.ui.DashBoard_Driver_act;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.SSLog_SS;

/**
 * Created by jijo_soumen on 22/03/2016.
 * Runs when driver is for hire
 */
public class LocationService extends Service implements TextToSpeech.OnInitListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "LocationService: ";
    public static final String BROADCAST_ACTION = "com.smartshehar.cabe.driver.locationservice";
    Intent mIntent;
    public Location mCurrentLocation = null, mPreviousLocation = null;
    int nLocationGot;
    private GoogleApiClient mGoogleApiClient;
    Notification mNotification;
    LocationManager mLocationManagerNetwork;
    LocationListener mLocationListenerNetwork;
    LocationManager mLocationManagerGPS;
    LocationListener mLocationListenerGPS;
    long mCurrentTime = -1;
    LocationRequest mLocationRequest;
    protected boolean isRunningrunnableUpdatePosition = false;
    protected Handler handler = new Handler();
    String flag_Service = "";
    private TextToSpeech mTts;
    //ToneGenerator toneG;
    private MediaPlayer mMediaPlayer;
    int iBeepSoundTime = 1000;
    boolean isGotCheckIfBookResponse = false;
    //private static final int NOTIFY_ME_ID = 1337;
    //String sCostValue = "", sDistance = "";

    @Override
    public void onCreate() {
        super.onCreate();
        mCurrentLocation = CGlobals_lib_ss.getInstance().getMyLocation(this);
        mIntent = new Intent(BROADCAST_ACTION);
        nLocationGot = 1;
        setupService();
        setupLocationListeners();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        handler.postDelayed(runnableUpdatePosition, 0);
        handler.postDelayed(runnableLoginRequested, 0);
        mTts = new TextToSpeech(this, this);
        //toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
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
            flag_Service = extras.getString(Constants_CED.SERVICE_FLAG);
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
        //toneG.stopTone();

        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = CGlobals_lib_ss.isBetterLocation(location, mCurrentLocation) ? location
                : mCurrentLocation;
        CGlobals_lib_ss.setMyLocation(mCurrentLocation, false, LocationService.this);
    }

    @Override
    public void onConnected(Bundle bundle) {
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
        if (CGlobals_CED.SERVICE_CURRENT == CGlobals_CED.SERVICE_DB) {
            return new Intent(this, DashBoard_Driver_act.class);
        } else if (CGlobals_CED.SERVICE_CURRENT == CGlobals_CED.SERVICE_CMD) {
            return new Intent(this, CabEMainDriver_act.class);
        } else {
            return null;
        }
    }

    protected String servicetext() {
        return "CabE Driver";
    }

    public int inTripNotificationImage() {
        return R.mipmap.ic_launcher;
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
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                            this).setContentTitle(servicetext())
                            .setContentText("Your CabE is now active")
                            .setSmallIcon(inTripNotificationImage())
                            .setContentIntent(viewPendingIntent);
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
            CGlobals_CED.msTripAction = CGlobals_lib_ss.getInstance()
                    .getPersistentPreference(this)
                    .getString(Constants_CED.PREF_TRIP_ACTION, "");
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

    public void sendUpdatePosition(Location location) {
        CGlobals_CED.getInstance().sendUpdatePosition(location, LocationService.this);
    }

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

    protected Runnable runnableLoginRequested = new Runnable() {
        @Override
        public void run() {
            boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(LocationService.this);
            if (!isInternetCheck) {
                checkIfLoginRequested();
            } else {
                sendMessage("", 0);
            }
            handler.postDelayed(runnableLoginRequested, Constants_CED.LOGIN_REQUEST);
        }
    };

    protected Runnable runnableUpdatePosition = new Runnable() {
        @Override
        public void run() {
            if (isRunningrunnableUpdatePosition)
                return;
            isRunningrunnableUpdatePosition = true;
            // run continue driver update location in dashboard and start cancel page.
            runUpdatePosition();
            isRunningrunnableUpdatePosition = false;
            handler.postDelayed(runnableUpdatePosition,
                    Constants_CED.DRIVER_UPDATE_INTERVAL);
        }
    };

    public void runUpdatePosition() {
        try {
            sendUpdatePosition(mCurrentLocation);
            boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(LocationService.this);
            sendUpdate(mCurrentLocation);
            if (CGlobals_CED.SERVICE_CURRENT == CGlobals_CED.SERVICE_DB) {
                boolean isCostDis_Submit = CGlobals_lib_ss.getInstance().getPersistentPreference(LocationService.this)
                        .getBoolean(Constants_CED.PREF_TRIP_COST_DISTANCE_SUBMIT, true);
                if (isCostDis_Submit) {
                    if (!isInternetCheck) {
                        checkIfBooked();
                    } else {
                        sendMessage("", 0);
                    }
                }
            }
            if (CGlobals_CED.SERVICE_CURRENT == CGlobals_CED.SERVICE_CMD) {
                if (!isInternetCheck) {
                    checkPassengerCancelTrip();
                } else {
                    sendMessage("", 0);
                }
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG, "runnableService", e, LocationService.this);
        }

    }

    private void sendMessage(String message, int errorvalue) {
        if (CGlobals_CED.SERVICE_CURRENT == CGlobals_CED.SERVICE_DB) {
            Intent intent = new Intent(Constants_CED.LOCATION_SERVICE_FOR_HIRE);
            intent.putExtra("response", message);
            intent.putExtra("errorvalue", errorvalue);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
        if (CGlobals_CED.SERVICE_CURRENT == CGlobals_CED.SERVICE_CMD) {
            Intent intent = new Intent(Constants_CED.LOCATION_SERVICE_DRIVER_MAIN);
            intent.putExtra("response", message);
            intent.putExtra("errorvalue", errorvalue);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
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

    private void checkIfBooked() {
        final String url = Constants_CED.CHECK_IF_BOOKED_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        checkIfBookedResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                sendMessage("", 0);
                String sErr = CGlobals_lib_ss.getInstance().getVolleyError(error);
                try {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.d(TAG,
                                "Failed to mVehicleNoVerify :-  "
                                        + error.getMessage());
                    }
                } catch (Exception e) {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG, "mVehicleNoVerify - " + sErr,
                                error, LocationService.this);
                    }
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params = CGlobals_lib_ss.getInstance().getBasicMobileParamsShort(params,
                        url, LocationService.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                String debugUrl;
                try {
                    debugUrl = url + "?" + getParams.toString();
                    Log.i(TAG, debugUrl);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "getPassengers map - ", e, LocationService.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, LocationService.this);
    }

    private void checkIfBookedResponse(String response) {
        if (TextUtils.isEmpty(response) || response.equals("-1")) {
            sendMessage("", 0);
            return;
        }
        try {
            if (isGotCheckIfBookResponse) {
                return;
            }
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(LocationService.this)
                    .putString(Constants_CED.PREF_CHECK_BOOK_RESPONSE, response);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(LocationService.this)
                    .putBoolean(Constants_CED.PREF_IS_CHECK_BOOK, true);
            Log.d("Pref is set", "true");
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(LocationService.this).commit();
            String sSpeech = "Passenger Waiting. Please pick ride";
            say(sSpeech);
            Runnable r = new Runnable() {
                public void run() {
                    long start = System.currentTimeMillis();
                    long end = start + iBeepSoundTime;
                    while (System.currentTimeMillis() < end) {
                        playAudio();
                        try {
                            Thread.sleep(1000);
                        } catch (Exception x) {
                            Log.d(TAG, String.valueOf(x));
                        }
                    }
                }
            };
            Thread t = new Thread(r);
            t.start();
            Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(2000);
            KeyguardManager myKM = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
            boolean isPhoneLocked = myKM.inKeyguardRestrictedInputMode();
            if (!isScreenOn(this) && isPhoneLocked) {
                PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
                boolean isScreenOn = pm.isScreenOn();
                if (!isScreenOn) {
                    PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "MyLock");
                    wl.acquire(10000);
                    PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyCpuLock");
                    wl_cpu.acquire(10000);
                }
            }
            isGotCheckIfBookResponse = true;
            sendMessage(response, 1);
        } catch (Exception e) {
            sendMessage("", 0);
            e.printStackTrace();
        }
    }

    boolean isScreenOn(Context context) {
        // If you use less than API20:
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return powerManager.isScreenOn();
    }

    private void playAudio() {
        try {
            // http://www.soundjay.com/beep-sounds-1.html lots of free beeps here
            mMediaPlayer = MediaPlayer.create(this, R.raw.beep09);
            mMediaPlayer.setLooping(false);
            mMediaPlayer.start();
        } catch (Exception e) {
            Log.e("beep", "error: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("deprecation")
    void say(String sSay) {
        HashMap<String, String> hash = new HashMap<>();
        hash.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
                String.valueOf(AudioManager.STREAM_NOTIFICATION));
        mTts.speak(sSay, TextToSpeech.QUEUE_ADD, hash);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Change this to match your
            // locale
            mTts.setLanguage(Locale.US);
        }
    }


    private void checkIfLoginRequested() {
        final String url = Constants_CED.CHECK_IF_LOGIN_REQUESTED_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        checkIfLoginRequestedResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                sendMessage("", 0);
                String sErr = CGlobals_lib_ss.getInstance().getVolleyError(error);
                if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                    Log.e(TAG, "");
                } else {
                    SSLog_SS.e(TAG, "mVehicleNoVerify - " + sErr, error, LocationService.this);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params = CGlobals_lib_ss.getInstance().getBasicMobileParamsShort(params,
                        url, LocationService.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                String debugUrl;
                try {
                    debugUrl = url + "?" + getParams.toString();
                    Log.i(TAG, debugUrl);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "getPassengers map - ", e, LocationService.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, LocationService.this);
    }

    private void checkIfLoginRequestedResponse(String response) {
        if (TextUtils.isEmpty(response) || response.equals("-1")) {
            sendMessage("", 0);
            return;
        }
        try {
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(LocationService.this)
                    .putString(Constants_CED.PREF_CHECK_LOGIN_REQUEST_RESPONSE, response);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(LocationService.this).commit();
            sendMessage(response, 2);
        } catch (Exception e) {
            sendMessage("", 0);
            SSLog_SS.e(TAG, "checkIfLogin: ", e, LocationService.this);
        }
    }

    private void checkPassengerCancelTrip() {
        final int cabtripid = CGlobals_lib_ss.getInstance().getPersistentPreference(LocationService.this).
                getInt(Constants_CED.PREF_TRIP_ID_INT, -1);
        final String sUserType = CGlobals_lib_ss.getInstance().getPersistentPreference(LocationService.this).
                getString(Constants_CED.CAB_TRIP_USER_TYPE_DRIVER, "");
        /*final int setBusyCreateTripDriver = CGlobals_lib_ss.getInstance().getPersistentPreference(LocationService.this)
                .getInt(Constants_CED.PERF_SET_BUSY_CREATE_TRIP_DRIVER, 0);*/
        final String url = Constants_CED.GET_CAB_TRIP_STATUS_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (TextUtils.isEmpty(response) || response.equals("-1") || response.equals("0")) {
                            sendMessage("", 0);
                            return;
                        }
                        try {
                            sendMessage(response, 1);
                        } catch (Exception e) {
                            sendMessage("", 0);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                sendMessage("", 0);
                String sErr = CGlobals_lib_ss.getInstance().getVolleyError(error);
                try {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.d(TAG,
                                "Failed to mVehicleNoVerify :-  "
                                        + error.getMessage());
                    }
                } catch (Exception e) {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG, "mVehicleNoVerify - " + sErr,
                                error, LocationService.this);
                    }
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
                        url, LocationService.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                String url1 = url;
                try {
                    String url = url1 + "?" + getParams.toString();
                    System.out.println("url  " + url);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "hasDriverAccepted", e, LocationService.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, LocationService.this);
    }

    /*private void updateTripCost_CallServer(final String sCostValue, final String sDistance) {
        final int tripid = CGlobals_lib_ss.getInstance().getPersistentPreference(LocationService.this)
                .getInt(Constants_CED.PREF_TRIP_ID_INT, -1);
        final String sUserType = CGlobals_lib_ss.getInstance().getPersistentPreference(LocationService.this).
                getString(Constants_CED.CAB_TRIP_USER_TYPE_DRIVER, "");
        *//*final int setBusyCreateTripDriver = CGlobals_lib_ss.getInstance().getPersistentPreference(LocationService.this)
                .getInt(Constants_CED.PERF_SET_BUSY_CREATE_TRIP_DRIVER, 0);*//*
        final String url = Constants_CED.UPDATE_TRIP_COST_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        SSLog_SS.d("updateTripCost  ", response);
                        if (response.equals("-1")) {
                            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(LocationService.this)
                                    .putBoolean(Constants_CED.PREF_TRIP_COST_DISTANCE_SUBMIT, true);
                            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(LocationService.this).commit();
                            return;
                        }
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(LocationService.this)
                                .putBoolean(Constants_CED.PREF_TRIP_COST_DISTANCE_SUBMIT, true);
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(LocationService.this).commit();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                try {
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(LocationService.this)
                            .putBoolean(Constants_CED.PREF_TRIP_COST_DISTANCE_SUBMIT, false);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(LocationService.this).commit();
                    SSLog_SS.e(TAG, "updateTripCost - ", error, LocationService.this);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "updateTripCost - ", e, LocationService.this);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tripid", String.valueOf(tripid));
                params.put("tripcost", sCostValue);
                params.put("tripdistance", sDistance);
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
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params, url, LocationService.this);
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }

        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, true, LocationService.this);
    }*/

}
