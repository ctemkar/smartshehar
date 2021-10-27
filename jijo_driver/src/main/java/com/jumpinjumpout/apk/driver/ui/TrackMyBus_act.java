package com.jumpinjumpout.apk.driver.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jumpinjumpout.apk.driver.CGlobals_driver;
import com.jumpinjumpout.apk.driver.Constants_driver;
import com.jumpinjumpout.apk.driver.MyApplication;
import com.jumpinjumpout.apk.driver.R;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.CTrip;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.lib.ui.AbstractMapFragment_act;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lib.app.util.MyLocation;

/**
 * Created by user pc on 04-12-2015.
 */
public class TrackMyBus_act extends AbstractMapFragment_act {

    private static String TAG = "TrackMyBus_act: ";
    ArrayList<CTrip> cTripArrayList;
    CTrip cTrip;
    GoogleApiClient googleApiClient = null;
    private TextView mTvSetLocation;
    public FrameLayout mFlPassengerInfo;
    private String sCountryCode = "";
    public static String ShopLat;
    public static String ShopLong;
    private Handler handlerUpdateAddress = new Handler();
    ArrayList<Marker> driverCurrentLocationMarkerArray = new ArrayList<Marker>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trackmybus_act);
        create();
        googleApiClient = new GoogleApiClient.Builder(TrackMyBus_act.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(TrackMyBus_act.this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
        MyLocation myLocation = new MyLocation(
                MyApplication.mVolleyRequestQueue, TrackMyBus_act.this, googleApiClient);
        myLocation.getLocation(this, onLocationResult);
        CGlobals_lib.getInstance().turnGPSOn1(TrackMyBus_act.this, mGoogleApiClient);
        init();
        mTvSetLocation = (TextView) findViewById(com.jumpinjumpout.apk.lib.R.id.tvSetLocation);
        mIvGreenPin = (ImageView) findViewById(com.jumpinjumpout.apk.lib.R.id.imageView1);
        mIvRedPin = (ImageView) findViewById(com.jumpinjumpout.apk.lib.R.id.imageView2);
        mFlPassengerInfo = (FrameLayout) findViewById(com.jumpinjumpout.apk.lib.R.id.flPassengerInfo);
        setAutoRefreshFromLocation(true);
        sCountryCode = getGeoCountryCode();
        mButtonGo.setVisibility(View.GONE);
        mButtonPause.setVisibility(View.GONE);
        mButtonResume.setVisibility(View.GONE);
        mButtonCancelTrip.setVisibility(View.GONE);
        mButtonEndTrip.setVisibility(View.GONE);
        mIvRedPin.setVisibility(View.GONE);
        Location loc = CGlobals_lib.getInstance().getMyLocation(this);
        if (loc != null) {
            setLocation(loc);
        }
        stupMap();
        CGlobals_lib.showGPSDialog = true;
    }

    private Runnable runnableDriverPosition = new Runnable() {
        @Override
        public void run() {
            getTrackMyBus();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mRlFromTo.setVisibility(View.GONE);
        mLlMarkerLayout.setVisibility(View.GONE);
        getTrackMyBus();
        handlerUpdateAddress.postDelayed(runnableDriverPosition, Constants_driver.MOVE_MARKER);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CGlobals_lib.REQUEST_LOCATION) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    Log.d(TAG, "YES");
                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    Log.d(TAG, "NO");
                    CGlobals_lib.showGPSDialog = false;
                    finish();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onClickZoomActiveUsers(View v) {

    }

    @Override
    public void onClickZoomTrip(View v) {

    }

    @Override
    public void onClickZoomVehicle(View v) {

    }

    @Override
    public void onClickInviteUsers(View v) {

    }

    @Override
    public void onClickInformation(View v) {

    }

    @Override
    public void endTrip() {

    }

    @Override
    protected String getGeoCountryCode() {
        return null;
    }

    @Override
    protected void gotGoogleMapLocation(Location location) {

    }

    @Override
    protected boolean showPath() {
        return false;
    }

    @Override
    protected void mapLoaded() {

    }

    @Override
    protected void callDriver(JSONArray jsonArray) {

    }

    @Override
    protected void goFacebook() {

    }

    @Override
    protected void goTwitter() {

    }

    // Create Trip History Function
    private void getTrackMyBus() {
        cTripArrayList = new ArrayList<CTrip>();
        // Send Data to Server Database Using Volley Library
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_driver.MANAGER_TRACK_TRIPS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getTrackMyAllBus(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handlerUpdateAddress.postDelayed(runnableDriverPosition, Constants_driver.MOVE_MARKER);
                try {
                    Toast.makeText(
                            TrackMyBus_act.this.getBaseContext(),
                            getString(R.string.retry_internet),
                            Toast.LENGTH_LONG).show();

                    SSLog.e(TAG, "getTripHistory:-   ",
                            error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG, "getTripHistory:-  ", e);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params = CGlobals_driver.getInstance().getMinAllMobileParams(params,
                        Constants_driver.MANAGER_TRACK_TRIPS_URL);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_driver.MANAGER_TRACK_TRIPS_URL;
                try {
                    String url = url1 + "?" + getParams.toString();
                    System.out.println("url  " + url);

                } catch (Exception e) {
                    SSLog.e(TAG, "getTripHistory", e);
                }
                return CGlobals_driver.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    } // sendUpdatePosition

    private void getTrackMyAllBus(String response) {
        if (TextUtils.isEmpty(response) || response.trim().equals("-1")) {
            handlerUpdateAddress.postDelayed(runnableDriverPosition, Constants_driver.MOVE_MARKER);
            return;
        }
        try {
            for (int i = 0; i < driverCurrentLocationMarkerArray.size(); i++) {
                driverCurrentLocationMarkerArray.get(i).remove();
            }
            JSONArray aJson = new JSONArray(response);
            for (int i = 0; i < aJson.length(); i++) {
                cTrip = new CTrip(aJson.getJSONObject(i)
                        .toString(), TrackMyBus_act.this);
                Marker driverCurrentLocationMarker = mMap.addMarker(new MarkerOptions()
                        .position(
                                new LatLng(cTrip.getLAT(), cTrip.getLNG()))
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.track_driver_circle))
                        .alpha(0.8f).draggable(true)
                        .title(cTrip.getName()));
                driverCurrentLocationMarkerArray.add(driverCurrentLocationMarker);
            }
            handlerUpdateAddress.postDelayed(runnableDriverPosition, Constants_driver.MOVE_MARKER);
        } catch (Exception e) {
            handlerUpdateAddress.postDelayed(runnableDriverPosition, Constants_driver.MOVE_MARKER);
            SSLog.e(TAG, "getTripHistory", e);
        }
    }

    private MyLocation.LocationResult onLocationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            CGlobals_lib.getInstance().setMyLocation(location, false);
            if (location != null && (moFromAddress.hasLatitude() && moFromAddress
                    .hasLongitude())) {
                CGlobals_driver.getInstance().sendUpdatePosition(
                        CGlobals_driver.getInstance().getMyLocation(TrackMyBus_act.this), TrackMyBus_act.this);
                startIntentServiceCurrentAddress(location);
            }
            try {
                CGlobals_lib.getInstance().setMyLocation(location, false);
                if (location != null
                        ) {
                    float results[] = new float[1];
                    Location.distanceBetween(moToAddress.getLatitude(),
                            moToAddress.getLongitude(), location.getLatitude(),
                            location.getLongitude(), results);
                }
            } catch (Exception e) {
                SSLog.e(TAG, "LocationResult", e);
            }
        }

        ;
    };

    private void stupMap() {
        try {
            LatLng latLong;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            Location location = CGlobals_lib.getInstance().getMyLocation(TrackMyBus_act.this);
            if (location != null) {
                latLong = new LatLng(location
                        .getLatitude(), location
                        .getLongitude());
                ShopLat = location.getLatitude() + "";
                ShopLong = location.getLongitude()
                        + "";
            } else {
                latLong = new LatLng(12.9667, 77.5667);
            }
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLong).zoom(15f).tilt(70).build();
            mMap.setMyLocationEnabled(true);
            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
            mMap.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
