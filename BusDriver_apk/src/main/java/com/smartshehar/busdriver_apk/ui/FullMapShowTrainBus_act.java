package com.smartshehar.busdriver_apk.ui;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.jumpinjumpout.apk.lib.SSLog;
import com.smartshehar.busdriver_apk.CGlobal_bd;
import com.smartshehar.busdriver_apk.Constants_bd;
import com.smartshehar.busdriver_apk.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by user pc on 21-07-2015.
 */
public class FullMapShowTrainBus_act extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleMap.OnMyLocationChangeListener,
        GoogleApiClient.OnConnectionFailedListener {

    private static String TAG = "FullMap_T_B: ";
    GoogleApiClient mGoogleApiClient = null;
    protected GoogleMap mMap;
    public static final float DEFAULT_ZOOM = 15;
    Location mCurrentLocation;
    protected Handler handler = new Handler();
    float mdlat, mdLon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullmapshow_act);
        mGoogleApiClient = new GoogleApiClient.Builder(FullMapShowTrainBus_act.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(FullMapShowTrainBus_act.this)
                .addOnConnectionFailedListener(FullMapShowTrainBus_act.this)
                .build();
        mGoogleApiClient.connect();
        mCurrentLocation = CGlobal_bd.getInstance().getMyLocation(FullMapShowTrainBus_act.this);
        setUpMapIfNeeded();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.postDelayed(runnableUpdatePosition, Constants_bd.DRIVER_UPDATE_INTERVAL);
    }

    protected Runnable runnableUpdatePosition = new Runnable() {
        @Override
        public void run() {
            mCurrentLocation = CGlobal_bd.getInstance().getBestLocation(FullMapShowTrainBus_act.this);
            mdlat = CGlobal_bd.getInstance().getSharedPreferences(FullMapShowTrainBus_act.this)
                    .getFloat(Constants_bd.PREF_BUS_DESTINATION_LAT, 0);
            mdLon = CGlobal_bd.getInstance().getSharedPreferences(FullMapShowTrainBus_act.this)
                    .getFloat(Constants_bd.PREF_BUS_DESTINATION_LON, 0);
            float results[] = new float[1];
            Location.distanceBetween(mdlat, mdLon, mCurrentLocation.getLatitude(),
                    mCurrentLocation.getLongitude(), results);
            try {
                if (results[0] > 20) {
                    callBusTrip(mCurrentLocation, Constants_bd.TRIP_BEGIN);
                } else {
                    callBusTrip(mCurrentLocation, Constants_bd.TRIP_END);
                }
            } catch (Exception e) {
                SSLog.e(TAG, "getView", e);
            }
        }
    };

    protected void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mMap.setOnMyLocationChangeListener(FullMapShowTrainBus_act.this);

                Location location = CGlobal_bd.getInstance().getMyLocation(FullMapShowTrainBus_act.this);
                if (location != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(), location
                                    .getLongitude()), DEFAULT_ZOOM));
                }
                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        mCurrentLocation = CGlobal_bd.getInstance().getBestLocation(FullMapShowTrainBus_act.this);
                        mdlat = CGlobal_bd.getInstance().getSharedPreferences(FullMapShowTrainBus_act.this)
                                .getFloat(Constants_bd.PREF_BUS_DESTINATION_LAT, 0);
                        mdLon = CGlobal_bd.getInstance().getSharedPreferences(FullMapShowTrainBus_act.this)
                                .getFloat(Constants_bd.PREF_BUS_DESTINATION_LON, 0);
                        float results[] = new float[1];
                        Location.distanceBetween(mdlat, mdLon, mCurrentLocation.getLatitude(),
                                mCurrentLocation.getLongitude(), results);
                        try {
                            if (results[0] > 20) {
                                callBusTrip(mCurrentLocation, Constants_bd.TRIP_BEGIN);
                            } else {
                                callBusTrip(mCurrentLocation, Constants_bd.TRIP_END);
                            }
                        } catch (Exception e) {
                            SSLog.e(TAG, "getView", e);
                        }
                    }
                });
            }
        }
    } // setupMapIfNeeded

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public void onMyLocationChange(Location location) {
        mCurrentLocation = CGlobal_bd.getInstance().isBetterLocation(location, mCurrentLocation) ? location
                : mCurrentLocation;
        CGlobal_bd.getInstance().setMyLocation(location, true, FullMapShowTrainBus_act.this);
        handler.postDelayed(runnableUpdatePosition, Constants_bd.DRIVER_UPDATE_INTERVAL);
    }

    private void callBusTrip(final Location mcurrentLocation, final String tripStatus) {
        final int tripid = CGlobal_bd.getInstance().getSharedPreferences(FullMapShowTrainBus_act.this)
                .getInt(Constants_bd.PREF_BUS_DRIVER_TRIP_ID, -1);
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_bd.UPDATE_BUS_LOCATION_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    handler.postDelayed(runnableUpdatePosition, Constants_bd.DRIVER_UPDATE_INTERVAL);
                    SSLog.e(TAG, "callBusTrip", error.toString());
                } catch (Exception e) {
                    handler.postDelayed(runnableUpdatePosition, Constants_bd.DRIVER_UPDATE_INTERVAL);
                    SSLog.e(TAG, "callBusTrip", e);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params.put("tripid", String.valueOf(tripid));
                params.put("tripstatus", tripStatus);
                params.put("lat", String.valueOf(mcurrentLocation.getLatitude()));
                params.put("lng", String.valueOf(mcurrentLocation.getLongitude()));
                params.put("speed", String.valueOf(mcurrentLocation.getSpeed()));
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_bd.UPDATE_BUS_LOCATION_URL;
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
        CGlobal_bd.getInstance().getRequestQueue(FullMapShowTrainBus_act.this).add(postRequest);
    }  //callBusTrip

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FullMapShowTrainBus_act.this);
        alertDialog.setCancelable(false);
        alertDialog.setTitle("End Your Bus trip?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                callBusTrip(mCurrentLocation, Constants_bd.TRIP_END);
                finish();
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }
}
