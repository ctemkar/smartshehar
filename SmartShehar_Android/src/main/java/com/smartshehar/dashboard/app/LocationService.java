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
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.smartshehar.dashboard.app.ui.Act_WalkWithMe;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class LocationService extends Service {
    private int NOTIFICATION = R.string.safety_shield;
    Notification notification;
    private NotificationManager mNM;
    private  static final String TAG = "LocationService";

    LocationManager mLocationManagerNetwork;
    LocationManager mLocationManagerGPS;

    LocationListener mLocationListenerNetwork;
    LocationListener mLocationListenerGPS;
   
    //	private static final String TAG = "LocationService";
    public static final String BROADCAST_ACTION = "com.smartshehar.besafe.android.app.showlocation";
    Intent mIntent;
    int nLocationGot;


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

  
        SSApp.mbGPSFix = false;
        SSApp.mbInTrip = false;
        nLocationGot = 1;
        showNotification();
        setupLocationListeners();
        startLocationListeners();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        SSApp.moLocStart = null;
        SSApp.moLocDest = SSApp.moLocStart;
        CGlobals_db.getInstance(LocationService.this).miTravelDistance = 0;
        SharedPreferences mPref = getSharedPreferences(Constants_dp.PREFS_NAME, 0);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putInt(Constants_dp.PREFTRAVELDIST, 0);
        editor.commit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            return ;
        }
            if (mLocationManagerNetwork != null && mLocationListenerNetwork != null)
                mLocationManagerNetwork.removeUpdates(mLocationListenerNetwork);
            if (mLocationManagerGPS != null && mLocationListenerGPS != null)
                mLocationManagerGPS.removeUpdates(mLocationListenerGPS);


        SSApp.moLocStart = null;
        SSApp.moLocDest = null;
        mNM.cancel(NOTIFICATION);
        CGlobals_db.inWalkWithMe = false;
       sendLocation();

        // Tell the user we stopped.
//	        Toast.makeText(this, R.string.meter_stopped, Toast.LENGTH_SHORT).show();
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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return ;
        }
            if (mLocationManagerNetwork != null && mLocationListenerNetwork != null)
                mLocationManagerNetwork.removeUpdates(mLocationListenerNetwork);
            if (mLocationManagerGPS != null && mLocationListenerGPS != null)
                mLocationManagerGPS.removeUpdates(mLocationListenerGPS);
            int GPS_TIME = 5000;
            int GPS_MOVE = 10;
            mLocationManagerGPS.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    GPS_TIME, GPS_MOVE, mLocationListenerGPS);

    }

    private void updateMyLocation(Location location) {
        // Update your current location

        // Location.distanceto
        if (location != null) {
            sendBroadcast(mIntent);
        }
//		if(!CGlobals_db.getInstance(LocationService.this).isBetterLocation(location, SSApp.moLocDest))
//			return;
        CGlobals_db.getInstance(LocationService.this).mPreviousLocation = CGlobals_db.getInstance(LocationService.this).mCurrentLocation;
        CGlobals_db.getInstance(LocationService.this).mCurrentLocation = location;
        showNotification();
        sendLocation();
        sendBroadcast(mIntent);

        nLocationGot++;

    } // updateMyLocation

    @SuppressWarnings("deprecation")
    private void showNotification() {
        try {
            if (CGlobals_db.getInstance(LocationService.this).mCurrentLocation != null) {
                if (CGlobals_db.getInstance(LocationService.this) == null)
                    return;
                String sLoc;
                sLoc = " (" + Double.toString(CGlobals_db.getInstance(LocationService.this).mCurrentLocation.getLatitude()) + ", " +
                        Double.toString(CGlobals_db.getInstance(LocationService.this).mCurrentLocation.getLongitude()) + ")";
                String text = "Walk With Me " + sLoc;

                Intent intent = new Intent(this, Act_WalkWithMe.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.setAction("android.intent.action.VIEW");

                // The PendingIntent to launch our activity if the user selects this notification
                PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                        intent, 0);
                // Set the info for the views that show in the notification panel.
                NotificationCompat.Builder builder = new NotificationCompat.Builder(LocationService.this);
                builder.setAutoCancel(false);
                builder.setContentTitle(getText(R.string.location_service_label));
                builder.setContentText(text);
                builder.setSmallIcon(R.mipmap.ic_redlight);
                builder.setContentIntent(contentIntent);
                builder.setOngoing(true);
                builder.setNumber(100);
                builder.build();
                notification= builder.getNotification();


                // Send the notification.
                mNM.notify(NOTIFICATION, notification);
            }
        } catch (Exception e) {
            SSLog.e(TAG,"showNotification",e);
            Toast.makeText(LocationService.this, "Location service" + e.toString(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void sendLocation() {
        final String url = Constants_dp.SAFTEY_SHIELD_PHP_PATH + "walkwithme_updateposition.php";
        try {
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (!response.trim().equals("-1")) {
                                Log.d(TAG,"location sent");
                            } else {
                                Log.d(TAG,"location not sent");
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Log.d(TAG, "error is " + error.toString());
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
//                    Location location = CGlobals_db.getInstance(LocationService.this).getMyLocation(LocationService.this);
                    Calendar cal = Calendar.getInstance();

                    params.put("user_email", CGlobals_db.getInstance(LocationService.this).msGmail);
                    params.put("imei", SSApp.mIMEI);
                    params.put("mode", "U");
                    params.put("stopped", CGlobals_db.inWalkWithMe ? "N" : "Y");

                    params.put("y", Integer.toString(cal.get(Calendar.YEAR)));
                    params.put("o", Integer.toString(cal.get(Calendar.MONTH)));
                    params.put("d", Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
                    params.put("h", Integer.toString(cal.get(Calendar.HOUR_OF_DAY)));
                    params.put("m", Integer.toString(cal.get(Calendar.MINUTE)));
                    params.put("s", Integer.toString(cal.get(Calendar.SECOND)));


                    params = CGlobals_db.getInstance(LocationService.this).getBasicMobileParams(params,
                            url, LocationService.this);
                    Log.d(TAG, "PARAM IS " + params);
                    return CGlobals_db.getInstance(LocationService.this).checkParams(params);
                }
            };
            CGlobals_db.getInstance(LocationService.this).getRequestQueue(LocationService.this).add(postRequest);

        } catch (Exception e) {
            SSLog.e(TAG,"sendLocation",e);
            e.printStackTrace();
            Log.d(TAG, "ERROR IS " + e.toString());
        }

    }

}
