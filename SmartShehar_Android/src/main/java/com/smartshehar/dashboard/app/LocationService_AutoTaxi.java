package com.smartshehar.dashboard.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import com.smartshehar.dashboard.app.ui.ActMeter;

import java.math.BigDecimal;


public class LocationService_AutoTaxi extends Service {
    private int NOTIFICATION = R.string.meter_running;
    private NotificationManager mNM;
    private int TIMER_DELAY = 1000;
    LocationManager mLocationManagerNetwork;
    LocationManager mLocationManagerGPS;
    LocationListener mLocationListenerNetwork;
    LocationListener mLocationListenerGPS;
    ////
    public static int miTravelDistance;
    public int miDirectDistance;
    public static long mlTripTimeMs = 0L;
    public static int miTripTime = 0;
    public static int miWaitTimeSecs = 0;
    public static long mlWaitTimeMs = 0L;
    public static Time moTripStartTime;
    public static Time moTripDestTime;
    public static Location moLocStart;
    public static Location moLocDest;

    //	private static final String TAG = "LocationService_AutoTaxi";
    public static final String BROADCAST_ACTION = "com.smartshehar.android.autotaximeterfare.app.showlocation";
    Intent mIntent;
    int nLocationGot;
    //	SharedPreferences mSettings;
    SharedPreferences.Editor mEditor;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mIntent = new Intent(BROADCAST_ACTION);
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
        SharedPreferences mPref = getSharedPreferences(CGlobals_db.PREFS_NAME, 0);
        mEditor = mPref.edit();

        miTravelDistance = 0;
        miDirectDistance = 0;
        mlTripTimeMs = 0L;
        miTripTime = 0;
        miWaitTimeSecs = 0;
        mlWaitTimeMs = 0L;
        CGlobals_db.mbGPSFix = false;
        CGlobals_db.mbInTrip = false;
        moTripStartTime = new Time();
        moTripDestTime = new Time();
        nLocationGot = 1;
        setupLocationListeners();
        startLocationListeners();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        moLocStart = CGlobals_db.getInstance(LocationService_AutoTaxi.this).getMyLocation(LocationService_AutoTaxi.this);
        CGlobals_db.mLocStart = moLocStart;
        moLocDest = moLocStart;
        miTravelDistance = 0;
        miDirectDistance = 0;
        miWaitTimeSecs = 0;
        mlWaitTimeMs = 0L;
        mEditor.putInt(CGlobals_db.PREF_TRAVEL_DISTANCE, 0);
        mEditor.putInt(CGlobals_db.PREF_DIRECT_DIST, 0);
        mEditor.putInt(CGlobals_db.PREF_WAIT_TIME, 0);
        mEditor.putInt(CGlobals_db.PREF_TRIP_TIME, 0);
        mEditor.commit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        customHandler.postDelayed(calculateWaitTime, TIMER_DELAY);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return ;
        }
            if (mLocationManagerNetwork != null && mLocationListenerNetwork != null)
                mLocationManagerNetwork.removeUpdates(mLocationListenerNetwork);
            if (mLocationManagerGPS != null && mLocationListenerGPS != null)
                mLocationManagerGPS.removeUpdates(mLocationListenerGPS);


        moLocStart = null;
        moLocDest = null;
        mNM.cancel(NOTIFICATION);
        customHandler.removeCallbacks(calculateWaitTime);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.meter_stopped, Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }


    void setupLocationListeners() {
        // Acquire a reference to the system Location Manager
        mLocationManagerNetwork = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        mLocationListenerNetwork = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location
                // provider.
                updateMyLocation(location);
            }

            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        mLocationManagerGPS = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocationListenerGPS = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location
                // provider.
                updateMyLocation(location);
            }

            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
    }

    void startLocationListeners() {
        Log.d("Location is: ","startLocationListeners()");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return ;
        }
            if (mLocationManagerNetwork != null && mLocationListenerNetwork != null)
                mLocationManagerNetwork.removeUpdates(mLocationListenerNetwork);
            if (mLocationManagerGPS != null && mLocationListenerGPS != null)
                mLocationManagerGPS.removeUpdates(mLocationListenerGPS);
            int GPS_MOVE = 1;
            int GPS_TIME = 150;
            assert mLocationManagerGPS != null;
            mLocationManagerGPS.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    GPS_TIME, GPS_MOVE, mLocationListenerGPS);

    }

    private void updateMyLocation(Location location) {
        Log.d("Location is: ","updateMyLocation(Location location)");
        // Update your current location
        float[] res = new float[1]; // To get the dist from the
        // Location.distanceto
        if (location != null) {
            if (location.getProvider().equals(LocationManager.GPS_PROVIDER) && !CGlobals_db.mbGPSFix) {
                CGlobals_db.mbGPSFix = true;
                sendBroadcast(mIntent);
            }
        }
        if (!CGlobals_db.getInstance(LocationService_AutoTaxi.this).isBetterLocation(location, moLocDest))
            return;
//		location = mApp.getBestLocation();
        CGlobals_db.getInstance(LocationService_AutoTaxi.this).mPreviousLocation = CGlobals_db.getInstance(LocationService_AutoTaxi.this).mCurrentLocation;
        CGlobals_db.getInstance(LocationService_AutoTaxi.this).mCurrentLocation = location;
        if (moLocStart != null) {
            if (moLocDest != null && CGlobals_db.mbInTrip) {
                assert location != null;
                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                        moLocDest.getLatitude(), moLocDest.getLongitude(), res);
                int MINIMUM_ACCURACY_REQUIRED = 150;
                int MIN_MOVE = 2;
                if (res[0] >= MIN_MOVE && location.getAccuracy() <= MINIMUM_ACCURACY_REQUIRED) {
                    moLocDest = location;
                    CGlobals_db.mLocDest = moLocDest;
                    CGlobals_db.getInstance(LocationService_AutoTaxi.this).miPreviousDistance = miTravelDistance;
                    miTravelDistance += res[0];
                    CGlobals_db.mTravelDistance = miTravelDistance;
                    mEditor.putInt(CGlobals_db.PREF_TRAVEL_DISTANCE, miTravelDistance);
                    Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                            moLocStart.getLatitude(), moLocStart.getLongitude(), res);
                    miDirectDistance = (int) res[0];
                    CGlobals_db.mDirectDistance = miDirectDistance;
                    mEditor.putInt(CGlobals_db.PREF_DIRECT_DIST, miDirectDistance);
                    mEditor.commit();
                    if (moLocStart != null)
                        moLocDest = location;
                    CGlobals_db.mLocDest = moLocDest;
                    if (CGlobals_db.mbGPSFix) {
                        Log.d("Location is: ","CGlobals_db.mbGPSFix");
                        showNotification();
                        sendBroadcast(mIntent);
                    }
                }
            }

        }
//	        if(SSApp.mLocStart == null) { // Wait for location twice
//	        	iFirstLocation += 1;
        if (nLocationGot == 2) {
//            Toast.makeText(LocationService_AutoTaxi.this,"in trip true",Toast.LENGTH_LONG ).show();
            moLocStart = location;
            CGlobals_db.mLocStart = moLocStart;
            moLocDest = location;
            CGlobals_db.mLocDest = moLocDest;
            CGlobals_db.mbInTrip = true;
            moTripStartTime.setToNow();
            CGlobals_db.mTripStartTime = moTripStartTime;
            moTripDestTime.setToNow();
            CGlobals_db.lTotalTripms = 0;

        }
        nLocationGot++;


    } // updateMyLocation

    @SuppressWarnings("deprecation")
    private void showNotification() {
        try {
        /*if(CGlobals_db.getInstance(LocationService_AutoTaxi.this) == null)
			mApp = CGlobals_db.getInstance(LocationService_AutoTaxi.this);*/

            CFare cf = new CFare(CGlobals_db.moLastTrip.moFareParams,
                    CFareParams.METERTYPEDIGITAL,
                    miTravelDistance, miWaitTimeSecs);
            double dFare = cf.mdFare;
            Time tm = new Time();
            tm.setToNow();
            int hours = tm.hour;
            ////////////////-------------------
            if (hours >= CGlobals_db.moLastTrip.moFareParams.fNightStart && hours <= CGlobals_db.moLastTrip.moFareParams.fNightEnd) {
                dFare = cf.mdNightFare;
            }
            BigDecimal bd = new BigDecimal(dFare);
            BigDecimal fare = bd.setScale(0, BigDecimal.ROUND_HALF_UP);
            CharSequence text = " " + (Integer.valueOf(miTravelDistance).toString() + " m., ") +
                    fare.toString();
            // Set the icon, scrolling text and timestamp
           if(!TextUtils.isEmpty(text))
           {

               Intent intent = new Intent(this, ActMeter.class);
               intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//			intent.setAction("android.intent.action.VIEW");

               // The PendingIntent to launch our activity if the user selects this notification
               PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                       intent, 0);
               // Set the info for the views that show in the notification panel.
               NotificationCompat.Builder builder = new NotificationCompat.Builder(LocationService_AutoTaxi.this);
               builder.setAutoCancel(false);
               builder.setContentTitle(getText(R.string.location_service_label_auto_taxi));
               builder.setContentText(text);
               builder.setSmallIcon(R.mipmap.ic_meter);
               builder.setContentIntent(contentIntent);
               builder.setOngoing(true);
               builder.setNumber(100);
               builder.build();
               Notification notification = builder.getNotification();

               // Send the notification.
               mNM.notify(NOTIFICATION, notification);
           }else{
//               showNotification();
               Toast.makeText(LocationService_AutoTaxi.this,"Meter stopped.",Toast.LENGTH_LONG ).show();
           }
        } catch (Exception e) {
//            Toast.makeText(LocationService_AutoTaxi.this, "LocationService_AutoTaxi " + e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private Handler customHandler = new Handler();
    long prvTimeInMilliseconds = 0L;
    int miPreviousDistance = 0; // to check movement

    //	long updatedTime = 0L;
    private Runnable calculateWaitTime = new Runnable() {
        public void run() {
            if (!CGlobals_db.mbInTrip) {
                customHandler.postDelayed(this, TIMER_DELAY);
                return;
            }
            prvTimeInMilliseconds = mlTripTimeMs;
            mlTripTimeMs = SystemClock.uptimeMillis() - CGlobals_db.mlStartTimeMs;
            int elapsedSecs = (int) ((mlTripTimeMs - prvTimeInMilliseconds) / 1000);

            miTripTime = (int) (mlTripTimeMs / 1000);
            CGlobals_db.mTripTime = miTripTime;
            if (elapsedSecs == 0)
                elapsedSecs = 1;
            double speed = ((miTravelDistance - miPreviousDistance)
                    / 1000.00) * (60 * 60 / elapsedSecs);

//					((timeInMilliseconds - prvTimeInMilliseconds) / 1000 / 3600.00);


            int MIN_TRAVEL_SPEED = 2;
            if (speed <= MIN_TRAVEL_SPEED) {
                miWaitTimeSecs = miWaitTimeSecs + elapsedSecs;
                CGlobals_db.mWaitTimeSecs = miWaitTimeSecs;
                mEditor.putInt(CGlobals_db.PREF_WAIT_TIME, miWaitTimeSecs);
                sendBroadcast(mIntent);
            }

            mEditor.putInt(CGlobals_db.PREF_SPEED, (int) speed);
            mEditor.putInt(CGlobals_db.PREF_TRAVEL_DISTANCE, miTravelDistance);
            mEditor.putInt(CGlobals_db.PREF_TRIP_TIME, miTripTime);
            mEditor.commit();
            miPreviousDistance = miTravelDistance;
            customHandler.postDelayed(this, TIMER_DELAY);

        }
    };
}
