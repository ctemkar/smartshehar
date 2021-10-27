package com.jumpinjumpout.apk.user.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.ui.IconGenerator;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.CTrip;
import com.jumpinjumpout.apk.lib.Constants_lib;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.lib.ui.AbstractMapFragment_act;
import com.jumpinjumpout.apk.lib.ui.SearchAddress_act;
import com.jumpinjumpout.apk.user.CGlobals_user;
import com.jumpinjumpout.apk.user.Constants_user;
import com.jumpinjumpout.apk.user.MyApplication;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lib.app.util.CAddress;
import lib.app.util.MyLocation;

/**
 * Created by user pc on 17-07-2015.
 */
public class GetaCab_act extends AbstractMapFragment_act {
    protected final int ACTRESULT_INVITEFRIEND = 3;
    GoogleApiClient googleApiClient = null;
    private String sCountryCode = "", sFullName = "";
    public static String ShopLat;
    public static String ShopLong;
    private LatLng center;
    private TextView mTvSetLocation;
    private ImageView mIvGreenPin, mIvRedPin;
    boolean hasMovedMap = false;
    private Geocoder geocoder;
    private List<Address> addresses;
    private double slastlng, slastlat;
    CTrip getCab;
    private boolean bmcab = false;
    TextView tvShowMessage;
    Bitmap bmp = null;
    protected boolean isFromSelected = true;
    private int is_shared;
    private boolean isback = true;
    protected Handler handler = new Handler();
    protected Handler handlerFounderDriver = new Handler();
    String booking_time, planned_start_datetime, triptime, trip_end_notification_sent;
    int secs_to_confirm, driver_appuser_id, passenger_appuser_id, shared_cab, is_for_hire, allowstrangernotifications,
            cab_trip_id, hasdriver_started, iNoCabAva, trip_Accepted, trip_Rejected, cab_trip_idac;
    public ImageView mIvPhoto, mIvPassenger1;
    public FrameLayout mFlPassengerInfo;
    public LinearLayout mLlWaitingPassenger, mLlJoinPassenger;
    public TextView mTvJoinedCount, mtvJoinedCountNumber, mTvInfoBandLine2, mTvInfoBandLine1;
    public ImageView mTvInfoBandLine3;
    public TextView mTvInfoBandLine4, mTvTime, mTvMins;
    public LinearLayout mLlPassengerInfo;
    public TextView mTvJumpedInCount, mTvKmAway;
    private boolean isBookDriver = false;
    Marker marker;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.map_floating_action_menu_getacab);
        create();
        googleApiClient = new GoogleApiClient.Builder(GetaCab_act.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(GetaCab_act.this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
        MyLocation myLocation = new MyLocation(
                MyApplication.mVolleyRequestQueue, GetaCab_act.this, googleApiClient);
        myLocation.getLocation(this, onLocationResult);
        CGlobals_lib.getInstance().turnGPSOn1(GetaCab_act.this, mGoogleApiClient);
        init();
        mTvSetLocation = (TextView) findViewById(com.jumpinjumpout.apk.lib.R.id.tvSetLocation);
        mIvGreenPin = (ImageView) findViewById(com.jumpinjumpout.apk.lib.R.id.imageView1);
        mIvRedPin = (ImageView) findViewById(com.jumpinjumpout.apk.lib.R.id.imageView2);
        mFlPassengerInfo = (FrameLayout) findViewById(com.jumpinjumpout.apk.lib.R.id.flPassengerInfo);
        View hiddenPassengerInfo = getLayoutInflater().inflate(
                R.layout.getacab_list, mFlPassengerInfo, false);
        mFlPassengerInfo.addView(hiddenPassengerInfo);
        setAutoRefreshFromLocation(true);
        sCountryCode = getGeoCountryCode();
        mButtonGo.setVisibility(View.GONE);
        mButtonPause.setVisibility(View.GONE);
        mButtonResume.setVisibility(View.GONE);
        mButtonCancelTrip.setVisibility(View.GONE);
        mButtonEndTrip.setVisibility(View.GONE);
        mIvRedPin.setVisibility(View.GONE);
        tvShowMessage = (TextView) findViewById(R.id.tvShowMessage);
        Location loc = CGlobals_lib.getInstance().getMyLocation(this);
        if (loc != null) {
            setLocation(loc);
        }
        tvShowMessage.setVisibility(View.VISIBLE);
        stupMap();
        mButtonGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canceldialog();
            }
        });
        CGlobals_lib.showGPSDialog = true;
        findViewById(R.id.btnbookcab).setVisibility(View.GONE);
        CGlobals_user.getInstance().isCabCancelDriverNiotification = false;
    }

    public void onClickFrom(View v) {
        Intent i = new Intent(GetaCab_act.this,
                SearchAddress_act.class);
        i.putExtra("cc", getGeoCountryCode());
        startActivityForResult(i, ACTRESULT_FROM);
        if (!TextUtils.isEmpty(moFromAddress.getAddress())) {
            CGlobals_lib.getInstance().addRecentAddress(moFromAddress);
        }
    }

    protected Runnable runnablehasdriverAccepted = new Runnable() {
        @Override
        public void run() {

            hasDriverAccepted();
        }
    };

    protected Runnable runnablehasdrivercaricon = new Runnable() {
        @Override
        public void run() {
            getCabShow();
        }
    };

    protected Runnable runnablehasdriverStarted = new Runnable() {
        @Override
        public void run() {
            hasDriverStarted();
        }
    };

    protected Runnable runnableFoundDriver = new Runnable() {
        @Override
        public void run() {
            for (int i = 1; i <= 3; i++) {
                getFoundDriver(cab_trip_id);
            }
        }
    };

    public void onClickTo(View v) {
        Intent i = new Intent(GetaCab_act.this,
                SearchAddress_act.class);
        i.putExtra("cc", getGeoCountryCode());
        startActivityForResult(i, ACTRESULT_TO);
        if (!TextUtils.isEmpty(moFromAddress.getAddress())) {
            CGlobals_lib.getInstance().addRecentAddress(moFromAddress);
        }
    }

    protected boolean goDirections() {
        if (!moToAddress.hasLatitude() && !moToAddress.hasLongitude()) {
            Toast.makeText(GetaCab_act.this,
                    "Please enter your start and destination address",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        float results[] = new float[1];
        Location.distanceBetween(moFromAddress.getLatitude(), moFromAddress.getLongitude(),
                moToAddress.getLatitude(), moToAddress.getLongitude(), results);
        if (results[0] < Constants_lib.DISTANCE_SAME_FROM) {
            Toast.makeText(GetaCab_act.this,
                    "Your start and destination are too close (less than 500 m).\nPlease change your start or destination",
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if ((moFromAddress.hasLatitude() && moFromAddress
                .hasLongitude())
                && ((moToAddress.hasLatitude() && moToAddress
                .hasLongitude()) || !TextUtils
                .isEmpty(moToAddress.getAddress()))) {
            mIsGettingDirections = true;
            isTripFrozen = true;
            clearMap();
            drawFromMarker(moFromAddress);
            drawToMarker(moToAddress);
            mBounds.include(
                    new LatLng(moFromAddress.getLatitude(), moFromAddress
                            .getLongitude())).include(
                    new LatLng(moToAddress.getLatitude(), moToAddress
                            .getLongitude()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                    mBounds.build(), Constants_lib.MAP_PADDING));
            mInDirectionMode = true;
            SSLog.i(TAG, "Plotting directions");
            String url = GetaCab_act.this.getDirectionsUrl(
                    new LatLng(moFromAddress.getLatitude(), moFromAddress
                            .getLongitude()),
                    new LatLng(moToAddress.getLatitude(), moToAddress
                            .getLongitude()));
            mRlFromTo.setVisibility(View.GONE);
            String fromaddss, toaddss;
            IconGenerator tc = new IconGenerator(this);
            if (!TextUtils.isEmpty(moFromAddress.getSubLocality1())) {
                fromaddss = moFromAddress.getSubLocality1();
            } else {
                fromaddss = moFromAddress.getAddress().substring(0, 35);
                fromaddss = fromaddss + "...";
            }
            bmp = tc.makeIcon(fromaddss);
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(moFromAddress.getLatitude() + .001, moFromAddress.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromBitmap(bmp)));
            if (!TextUtils.isEmpty(moToAddress.getSubLocality1())) {
                toaddss = moToAddress.getSubLocality1();
            } else {
                toaddss = moToAddress.getAddress().substring(0, 35);
                toaddss = toaddss + "...";
            }
            bmp = tc.makeIcon(toaddss);
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(moToAddress.getLatitude() + .001, moToAddress.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromBitmap(bmp)));
            GoogleDirection downloadTask = new GoogleDirection();
            downloadTask.execute(url);
            return true;
        } else {
            Toast.makeText(GetaCab_act.this,
                    "Missing from or to. Cannot create trip",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    } // goDirections

    public String getDirectionsUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + ","
                + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Sensor enabled
        String sensor = "sensor=false";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&alternatives=true&" + sensor;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + parameters;
        return url;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String sAddr = "";
        double lat, lng;
        CAddress oAddr = null;
        if (requestCode == ACTRESULT_FROM) {
            if (resultCode == RESULT_OK) {
                sAddr = data.getStringExtra("street_address");
                lat = data.getDoubleExtra("lat", Constants_lib.INVALIDLAT);
                lng = data.getDoubleExtra("lng", Constants_lib.INVALIDLNG);
                oAddr = new CAddress();
                oAddr.setAddress(sAddr);
                oAddr.setLatitude(lat);
                oAddr.setLongitude(lng);
                moFromAddress = new CAddress(sAddr, "", lat, lng);
                setFrom(moFromAddress, false);
                drawFromMarker(moFromAddress);
                isFromSelected = true;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                                moFromAddress.getLatitude(), moFromAddress.getLongitude()),
                        DEFAULT_ZOOM));
            }
        }
        if (requestCode == ACTRESULT_TO) {
            if (resultCode == RESULT_OK) {
                sAddr = data.getStringExtra("street_address");
                lat = data.getDoubleExtra("lat", Constants_lib.INVALIDLAT);
                lng = data.getDoubleExtra("lng", Constants_lib.INVALIDLNG);
                oAddr = new CAddress();
                oAddr.setAddress(sAddr);
                oAddr.setLatitude(lat);
                oAddr.setLongitude(lng);
                moToAddress = new CAddress(sAddr, "", lat, lng);
                setTo(moToAddress, false);
                drawToMarker(moToAddress);
                isFromSelected = false;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                                moToAddress.getLatitude(), moToAddress.getLongitude()),
                        DEFAULT_ZOOM));
            }
        }
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
        if (requestCode == ACTRESULT_INVITEFRIEND) {
            if (resultCode == RESULT_OK) {

            }
        }

        if (!TextUtils.isEmpty(sAddr)) {
            CGlobals_lib.getInstance().addRecentAddress(oAddr);
        }
    }

    public void onClickBookCab(View view) {
        mButtonGo.setVisibility(View.VISIBLE);
        mButtonGo.setText("Cancel");
        createTrip();
        isBookDriver = true;
        findViewById(R.id.btnbookcab).setVisibility(View.GONE);
        tvShowMessage.setVisibility(View.VISIBLE);
        tvShowMessage.setText("Booking your cab");
    }

    public void createTrip() {
        final String url = Constants_user.REQUEST_CAB_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        SSLog.d("ResponseTripId  ", response);
                        bookCab(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                try {
                    SSLog.e(TAG, "createTrip - ", error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG, "createTrip - ", e);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tripdistance", mDistance);
                params.put("triptime", String.valueOf(iDuration));
                params.put("fromaddress", moFromAddress.getAddress());
                params.put("fromshortaddress", moFromAddress.getShortAddress());
                params.put("fromsublocality", moFromAddress.getSubLocality());
                params.put("toaddress", moToAddress.getAddress());
                params.put("toshortaddress", moToAddress.getShortAddress());
                params.put("tosublocality", moToAddress.getSubLocality1());
                if (moFromAddress.getSubLocality1() != null) {
                    params.put("fromsublocality",
                            moFromAddress.getSubLocality1());
                }
                params.put("fromlat",
                        Double.toString(moFromAddress.getLatitude()));
                params.put("fromlng",
                        Double.toString(moFromAddress.getLongitude()));
                params.put("tolat", Double.toString(moToAddress.getLatitude()));
                params.put("tolng", Double.toString(moToAddress.getLongitude()));
                params = CGlobals_lib.getInstance().getBasicMobileParams(params,
                        url, GetaCab_act.this);
                return CGlobals_lib.getInstance().checkParams(params);
            }
        };
        CGlobals_lib.getInstance().addVolleyRequest(postRequest, true, GetaCab_act.this);

    } // createTrip

    private void bookCab(String response) {
        if (TextUtils.isEmpty(response.trim())) {
            tvShowMessage.setText("Booking your cab");
        }
        if (response.trim().equals("-1")) {
            tvShowMessage.setText("Booking your cab");
        }
        tvShowMessage.setVisibility(View.VISIBLE);
        mLlTo.setVisibility(View.VISIBLE);
        try {
            JSONObject jResponse = new JSONObject(response);
            cab_trip_id = jResponse.isNull("cab_trip_id") ? -1
                    : jResponse.getInt("cab_trip_id");
            iNoCabAva = jResponse.isNull("nFoundDriver") ? -1
                    : jResponse.getInt("nFoundDriver");
            if (iNoCabAva == 0) {
                handler.postDelayed(runnableFoundDriver,
                        Constants_user.DRIVER_FOUND_CAB);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        isback = false;
        handler.postDelayed(runnablehasdriverAccepted,
                Constants_user.DRIVER_FOUND_CAB_HAS_DRIVER_AC);
    }

    private void getFoundDriver(final int cabTrip_id) {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.REQUEST_DRIVER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getAFoundDriver(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handler.postDelayed(runnableFoundDriver,
                        Constants_user.DRIVER_FOUND_CAB);
                /*Toast.makeText(
                        GetaCab_act.this.getBaseContext(),
                        getString(R.string.retry_internet),
                        Toast.LENGTH_LONG).show();*/
                SSLog.e(TAG, "getFoundDriver :-   ", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params.put("cabtripid", String.valueOf(cabTrip_id));
                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.REQUEST_DRIVER_URL);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_user.REQUEST_DRIVER_URL;
                try {
                    String url = url1 + "?" + getParams.toString();
                    System.out.println("url  " + url);
                } catch (Exception e) {
                    SSLog.e(TAG, "getFoundDriver", e);
                }
                return CGlobals_user.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    }

    private void getAFoundDriver(String response) {
        if (TextUtils.isEmpty(response)) {
            handler.postDelayed(runnableFoundDriver,
                    Constants_user.DRIVER_FOUND_CAB);
            return;
        }
        if (response.equals("-1")) {
            handler.postDelayed(runnableFoundDriver,
                    Constants_user.DRIVER_FOUND_CAB);
            return;
        }
        if (response.equals("0")) {
            handler.postDelayed(runnableFoundDriver,
                    Constants_user.DRIVER_FOUND_CAB);
            return;
        }
    }


    private void getCabShow() {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.GET_GET_FOR_HIRE_CAB_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getACab(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handler.postDelayed(runnablehasdrivercaricon,
                        Constants_user.DRIVER_CAB_ICONREQUEST);
                /*Toast.makeText(
                        GetaCab_act.this.getBaseContext(),
                        getString(R.string.retry_internet),
                        Toast.LENGTH_LONG).show();*/
                SSLog.e(TAG, "getCabShow :-   ", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params.put("lat", String.valueOf(moFromAddress.getLatitude()));
                params.put("lng", String.valueOf(moFromAddress.getLongitude()));
                params.put("tolat", String.valueOf(moToAddress.getLatitude()));
                params.put("tolng", String.valueOf(moToAddress.getLongitude()));
                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.GET_GET_FOR_HIRE_CAB_URL);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_user.GET_GET_FOR_HIRE_CAB_URL;
                try {
                    String url = url1 + "?" + getParams.toString();
                    System.out.println("url  " + url);
                } catch (Exception e) {
                    SSLog.e(TAG, "getCabShow", e);
                }
                return CGlobals_user.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    }

    private void getACab(String response) {
        if (TextUtils.isEmpty(response) || response.equals("-1")) {
            handler.postDelayed(runnablehasdrivercaricon,
                    Constants_user.DRIVER_CAB_ICONREQUEST);
            return;
        }
        try {
            JSONArray aJson = new JSONArray(response);
            getCab = new CTrip(response, GetaCab_act.this);
            for (int i = 0; i < aJson.length(); i++) {
                JSONObject person = (JSONObject) aJson.get(i);
                slastlat = person.getDouble("lat");
                slastlng = person.getDouble("lng");
                sFullName = person.getString("fullname");
                is_shared = person.getInt("is_shared");
                if (marker != null) {
                    marker.remove();
                }
                if (is_shared == -1) {
                    BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_yprivate);
                    marker = mMap.addMarker(new MarkerOptions()
                            .position(
                                    new LatLng(slastlat, slastlng))
                            .icon(icon)
                            .alpha(0.8f).draggable(true)
                            .title(sFullName));
                } else {
                    BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_sprivate);
                    marker = mMap.addMarker(new MarkerOptions()
                            .position(
                                    new LatLng(slastlat, slastlng))
                            .icon(icon)
                            .alpha(0.8f).draggable(true)
                            .title(sFullName));
                }
            }
        } catch (Exception e) {
            handler.postDelayed(runnablehasdrivercaricon,
                    Constants_user.DRIVER_CAB_ICONREQUEST);
            e.printStackTrace();
        }
        handler.postDelayed(runnablehasdrivercaricon,
                Constants_user.DRIVER_CAB_ICONREQUEST);
    }

    public BroadcastReceiver mMessageReceiverGetaCab = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (CGlobals_user.getInstance().isCabCancelDriverNiotification == true) {
                mFlPassengerInfo.setVisibility(View.GONE);
                CGlobals_user.getInstance().isCabCancelDriverNiotification = false;
                AlertDialog.Builder builder = new AlertDialog.Builder(GetaCab_act.this);
                builder.setMessage("sorry something wrong with this trip. please try again")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mFlPassengerInfo.setVisibility(View.VISIBLE);
                                mFlPassengerInfo = (FrameLayout) findViewById(com.jumpinjumpout.apk.lib.R.id.flPassengerInfo);
                                View hiddenPassengerInfo = getLayoutInflater().inflate(
                                        R.layout.getacab_list, mFlPassengerInfo, false);
                                mFlPassengerInfo.addView(hiddenPassengerInfo);
                                mButtonGo.setVisibility(View.GONE);
                                driverCancelCabTrip("1");
                                findViewById(R.id.btnbookcab).setVisibility(View.VISIBLE);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverGetaCab,
                new IntentFilter("com.jumpinjumpout.apk.user.ui.GetaCab_act"));
        Intent sendableIntent = new Intent("com.jumpinjumpout.apk.user.ui.GetaCab_act");
        LocalBroadcastManager.getInstance(this).
                sendBroadcast(sendableIntent);
        drawFromMarker(moFromAddress);
        drawToMarker(moToAddress);
        showTrip();
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        int dpValue = 95;
        float d = GetaCab_act.this.getResources().getDisplayMetrics().density;
        int margin = (int) (dpValue * d);
        lp.setMargins(0, 0, 0, margin);
        findViewById(com.jumpinjumpout.apk.lib.R.id.map).setLayoutParams(lp);
        mFlPassengerInfo.setVisibility(View.VISIBLE);
        checkInternetGPS();
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        this.getWindow().setAttributes(params);
        handler.postDelayed(runnablehasdriverStarted,
                Constants_user.DRIVER_FOUND_CAB_HAS_DRIVER_AC);
        handler.postDelayed(runnablehasdrivercaricon,
                Constants_user.DRIVER_CAB_ICONREQUEST);
    }

    private MyLocation.LocationResult onLocationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            CGlobals_lib.getInstance().setMyLocation(location, false);
            if (location != null && (moFromAddress.hasLatitude() && moFromAddress
                    .hasLongitude())) {
                CGlobals_user.getInstance().sendUpdatePosition(
                        CGlobals_user.getInstance().getMyLocation(GetaCab_act.this), getApplicationContext());
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
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            Location location = CGlobals_lib.getInstance().getMyLocation(GetaCab_act.this);
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
            mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

                @Override
                public void onCameraChange(CameraPosition arg0) {
                    if (!bmcab) {
                        center = mMap.getCameraPosition().target;
                        mMap.clear();
                        mLlMarkerLayout.setVisibility(View.VISIBLE);
                        try {
                            if (isFromSelected) {
                                mTvSetLocation.setText(" Set Start location ");
                                mIvRedPin.setVisibility(View.GONE);
                                mIvGreenPin.setVisibility(View.VISIBLE);
                                new GetLocationAsync(center.latitude, center.longitude, mTvFrom)
                                        .execute();
                                setTo(moToAddress.getAddress(),
                                        new LatLng(moToAddress.getLatitude(), moToAddress
                                                .getLongitude()));
                            } else {
                                mTvSetLocation.setText(" Set Destination location ");
                                mIvGreenPin.setVisibility(View.GONE);
                                mIvRedPin.setVisibility(View.VISIBLE);
                                if (hasMovedMap) {
                                    new GetLocationAsync(center.latitude, center.longitude, mTvTo)
                                            .execute();
                                    setFrom(moFromAddress.getAddress(),
                                            new LatLng(moFromAddress.getLatitude(), moFromAddress
                                                    .getLongitude()));
                                }
                            }
                            hasMovedMap = true;
                        } catch (Exception e) {
                            SSLog.e(TAG, "stupMap", e);
                        }
                    }
                }
            });

            mLlMarkerLayout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        double lat = center.latitude + .001;
                        if (isFromSelected) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                                            lat, center.longitude),
                                    DEFAULT_ZOOM));
                            isFromSelected = false;
                            mIvRedPin.setVisibility(View.VISIBLE);
                            mIvGreenPin.setVisibility(View.GONE);
                        } else {
                            isFromSelected = true;
                            mLlMarkerLayout.setVisibility(View.GONE);
                            bmcab = true;
                            tvShowMessage.setVisibility(View.GONE);
                            findViewById(R.id.btnbookcab).setVisibility(View.VISIBLE);
                            goDirections();
                        }
                    } catch (Exception e) {
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class GetLocationAsync extends AsyncTask<String, Void, String> {
        // boolean duplicateResponse;
        double x, y;
        StringBuilder str;
        TextView mTvAddress;

        public GetLocationAsync(double latitude, double longitude, TextView tvAddress) {
            x = latitude;
            y = longitude;
            mTvAddress = tvAddress;
        }

        @Override
        protected void onPreExecute() {
            mTvAddress.setHint(" Getting location ");
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                geocoder = new Geocoder(GetaCab_act.this, Locale.ENGLISH);
                addresses = geocoder.getFromLocation(x, y, 1);
                str = new StringBuilder();
                if (geocoder.isPresent()) {
                    Address returnAddress = addresses.get(0);
                    String localityString = returnAddress.getLocality();
                    String city = returnAddress.getCountryName();
                    String region_code = returnAddress.getCountryCode();
                    String zipcode = returnAddress.getPostalCode();
                    str.append(localityString + "");
                    str.append(city + "" + region_code + "");
                    str.append(zipcode + "");
                } else {
                }
            } catch (IOException e) {
                Log.e("tag", e.getMessage());
            } catch (Exception e) {
                Log.e("tag", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                CAddress addr = new CAddress(addresses.get(0));
                if (isFromSelected) {
                    moFromAddress = addr;
                    setFrom(moFromAddress, false);
                } else {
                    moToAddress = addr;
                    setTo(moToAddress, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
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
        return MyApplication.getInstance().getPersistentPreference()
                .getString(Constants_user.PREF_CURRENT_COUNTRY, "");
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

    protected void checkInternetGPS() {
        try {
            if (CGlobals_lib.getInstance().checkConnected(
                    GetaCab_act.this)) {
                mIvNoConnection.setVisibility(View.GONE);
            } else {
                mIvNoConnection.setVisibility(View.VISIBLE);
            }
            Location location = CGlobals_lib.getInstance().getMyLocation(getApplicationContext());
            if (location == null) {
                location = mCurrentLocation;
            }
            if (location == null) {
                return;
            }
            long diff = System.currentTimeMillis() - location.getTime();
            if (location == null || diff > Constants_lib.DRIVER_UPDATE_INTERVAL * 15) {
                MyLocation myLocation = new MyLocation(
                        CGlobals_lib.mVolleyRequestQueue, GetaCab_act.this, mGoogleApiClient);
                myLocation.getLocation(this, onLocationResult);
                mIvNoLocation.setVisibility(View.VISIBLE);
            } else {
                mIvNoLocation.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void showTrip() {
        Location location = CGlobals_lib.getInstance().getBestLocation(this, mCurrentLocation);
        mCurrentLocation = CGlobals_lib.getInstance().isBetterLocation(location, mCurrentLocation) ? location
                : mCurrentLocation;
        String jsonPoints = CGlobals_lib.getInstance().getSharedPreferences(getApplicationContext())
                .getString(Constants_lib.TRIP_PATH, "");
        if (!TextUtils.isEmpty(jsonPoints)) {
            mLatLngTripPath = PolyUtil.decode(jsonPoints);
            mDirectionsPolyline = new PolylineOptions()
                    .addAll(mLatLngTripPath);
            mDirectionsPolyline.width(4);
            mDirectionsPolyline.color(Color.BLACK);

            try {
                mMap.addPolyline(mDirectionsPolyline);
                drawFromMarker(moFromAddress);
                drawToMarker(moToAddress);
                mZoomFitBounds = new LatLngBounds.Builder();
                for (LatLng latLng : mLatLngTripPath) {
                    mZoomFitBounds.include(new LatLng(latLng.latitude,
                            latLng.longitude));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void callDriver(JSONArray jsonArray) {
        progressCancel();
        getCabShow();
    }

    private void hasDriverAccepted() {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.HAS_DRIVER_ACCEPTED_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getDriverInfo(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handler.postDelayed(runnablehasdriverAccepted,
                        Constants_user.DRIVER_FOUND_CAB_HAS_DRIVER_AC);
                /*Toast.makeText(
                        GetaCab_act.this.getBaseContext(),
                        getString(R.string.retry_internet),
                        Toast.LENGTH_LONG).show();*/
                SSLog.e(TAG, "hasDriverAccepted :-   ", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params.put("cabtripid", String.valueOf(cab_trip_id));
                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.HAS_DRIVER_ACCEPTED_URL);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_user.HAS_DRIVER_ACCEPTED_URL;
                try {
                    String url = url1 + "?" + getParams.toString();
                    System.out.println("url  " + url);
                } catch (Exception e) {
                    SSLog.e(TAG, "hasDriverAccepted", e);
                }

                return CGlobals_user.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    }

    private void getDriverInfo(String response) {
        if (TextUtils.isEmpty(response.trim())) {
            handler.postDelayed(runnablehasdriverAccepted,
                    Constants_user.DRIVER_FOUND_CAB_HAS_DRIVER_AC);
            return;
        }
        if (response.trim().equals("-1")) {
            handler.postDelayed(runnablehasdriverAccepted,
                    Constants_user.DRIVER_FOUND_CAB_HAS_DRIVER_AC);
            return;
        }
        if (isBookDriver) {
            getCab = new CTrip(response, GetaCab_act.this);
            try {
                JSONObject jResponse = new JSONObject(response);
                secs_to_confirm = jResponse.isNull("secs_to_confirm") ? 0
                        : jResponse.getInt("secs_to_confirm");
                driver_appuser_id = jResponse.isNull("driver_appuser_id") ? -1
                        : jResponse.getInt("driver_appuser_id");
                passenger_appuser_id = jResponse.isNull("passenger_appuser_id") ? -1
                        : jResponse.getInt("passenger_appuser_id");
                shared_cab = jResponse.isNull("shared_cab") ? -1
                        : jResponse.getInt("shared_cab");
                is_for_hire = jResponse.isNull("is_for_hire") ? -1
                        : jResponse.getInt("is_for_hire");
                allowstrangernotifications = jResponse.isNull("allowstrangernotifications") ? -1
                        : jResponse.getInt("allowstrangernotifications");
                booking_time = jResponse.isNull("booking_time") ? ""
                        : jResponse.getString("booking_time");
                cab_trip_idac = jResponse.isNull("cab_trip_id") ? -1
                        : jResponse.getInt("cab_trip_id");
                planned_start_datetime = jResponse.isNull("planned_start_datetime") ? ""
                        : jResponse.getString("planned_start_datetime");
                triptime = jResponse.isNull("triptime") ? ""
                        : jResponse.getString("triptime");
                hasdriver_started = jResponse.isNull("hasdriver_started") ? -1
                        : jResponse.getInt("hasdriver_started");
                trip_end_notification_sent = jResponse.isNull("trip_end_notification_sent") ? ""
                        : jResponse.getString("trip_end_notification_sent");
                trip_Accepted = jResponse.isNull("accepted") ? -1
                        : jResponse.getInt("accepted");
                trip_Rejected = jResponse.isNull("rejected") ? -1
                        : jResponse.getInt("rejected");
                if (trip_Rejected == 1) {
                    tvShowMessage.setText("Booking your cab");
                    handler.postDelayed(runnablehasdriverAccepted,
                            Constants_user.DRIVER_FOUND_CAB_HAS_DRIVER_AC);
                    handler.postDelayed(runnableFoundDriver,
                            Constants_user.DRIVER_FOUND_CAB);
                    getCabShow();
                    return;
                } else if (trip_Accepted == 0) {
                    tvShowMessage.setText("Booking your cab");
                    handler.postDelayed(runnablehasdriverAccepted,
                            Constants_user.DRIVER_FOUND_CAB_HAS_DRIVER_AC);
                    return;
                } else if (trip_Accepted == 1 && cab_trip_idac == cab_trip_id) {
                    mFlPassengerInfo.setVisibility(View.VISIBLE);
                    mFlPassengerInfo = (FrameLayout) findViewById(com.jumpinjumpout.apk.lib.R.id.flPassengerInfo);
                    View hiddenPassengerInfo = getLayoutInflater().inflate(
                            com.jumpinjumpout.apk.lib.R.layout.passengerdetails, mFlPassengerInfo, false);
                    mFlPassengerInfo.addView(hiddenPassengerInfo);
                    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT);
                    int dpValue = 95;
                    float d = GetaCab_act.this.getResources().getDisplayMetrics().density;
                    int margin = (int) (dpValue * d);
                    lp.setMargins(0, 0, 0, margin);
                    findViewById(com.jumpinjumpout.apk.lib.R.id.map).setLayoutParams(lp);
                    init1();
                    String sAddress = getCab.getFrom().substring(0, 25) + "...";
                    IconGenerator tc = new IconGenerator(this);
                    bmp = tc.makeIcon(sAddress);
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(getCab.getFromLat() + .001, getCab.getFromLng()))
                            .icon(BitmapDescriptorFactory.fromBitmap(bmp)));
                    markerFrom = mMap
                            .addMarker(new MarkerOptions()
                                    .position(
                                            new LatLng(getCab.getFromLat(), getCab
                                                    .getFromLng()))
                                    .icon(BitmapDescriptorFactory
                                            .defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                    .alpha(0.8f).draggable(true)
                                    .title(getCab.getFrom()));
                    findViewById(com.jumpinjumpout.apk.lib.R.id.llOneLine).setVisibility(View.GONE);
                    findViewById(com.jumpinjumpout.apk.lib.R.id.llTwoLine).setVisibility(View.GONE);
                    findViewById(com.jumpinjumpout.apk.lib.R.id.llThreeLine).setVisibility(View.GONE);
                    findViewById(com.jumpinjumpout.apk.lib.R.id.llPassengerInfo).setVisibility(View.VISIBLE);
                    findViewById(com.jumpinjumpout.apk.lib.R.id.llWaitingPassenger).setVisibility(View.VISIBLE);
                    findViewById(com.jumpinjumpout.apk.lib.R.id.ivPhoto).setVisibility(View.VISIBLE);
                    findViewById(com.jumpinjumpout.apk.lib.R.id.llJoinPassenger).setVisibility(View.VISIBLE);
                    findViewById(com.jumpinjumpout.apk.lib.R.id.tvKmAway).setVisibility(View.VISIBLE);
                    if (!TextUtils.isEmpty(getCab.getUserProfileImageFileName()) && !TextUtils.isEmpty(getCab.getUserProfileImagePath())) {
                        String url = Constants_user.GET_USER_PROFILE_IMAGE_FILE_NAME_URL + getCab.getUserProfileImagePath() +
                                getCab.getUserProfileImageFileName();
                        ImageRequest request = new ImageRequest(url,
                                new Response.Listener<Bitmap>() {
                                    @Override
                                    public void onResponse(Bitmap bitmap) {
                                        if (Build.VERSION.SDK_INT < 11) {
                                            Toast.makeText(GetaCab_act.this, getString(R.string.api11not), Toast.LENGTH_LONG).show();
                                        } else {
                                            mIvPhoto.setImageBitmap(bitmap);
                                            mIvPhoto.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }, 0, 0, null,
                                new Response.ErrorListener() {
                                    public void onErrorResponse(VolleyError error) {
                                        mIvPhoto.setImageResource(R.mipmap.ic_unknowuser);
                                    }
                                });
                        MyApplication.getInstance().addToRequestQueue(request);
                    }
                    mTvInfoBandLine4.setVisibility(View.VISIBLE);
                    mTvInfoBandLine1.setVisibility(View.VISIBLE);
                    mTvTime.setVisibility(View.VISIBLE);
                    mTvMins.setVisibility(View.GONE);
                    mtvJoinedCountNumber.setVisibility(View.VISIBLE);
                    mTvJoinedCount.setVisibility(View.VISIBLE);
                    mTvJumpedInCount.setVisibility(View.VISIBLE);
                    mTvInfoBandLine4.setText((TextUtils.isEmpty(getCab.getName()) ? ""
                            : getCab.getName()));
                    mTvInfoBandLine1.setText(Html.fromHtml(passengerTODriverDistance()));
                    mTvTime.setText((TextUtils.isEmpty(bookTime(booking_time)) ? ""
                            : bookTime(booking_time)));
                    //mtvJoinedCountNumber.setText(triptime);
                    // mTvJoinedCount.setText(Html.fromHtml("mins"));
                    mTvJumpedInCount.setText(Html.fromHtml("Trip distance <br>" + String.valueOf(getCab.getMiTripDistance() / 1000) + " Km"));

                    mLlWaitingPassenger.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showThumbImageClick();
                        }
                    });

                } else {
                    handler.postDelayed(runnablehasdriverAccepted,
                            Constants_user.DRIVER_FOUND_CAB_HAS_DRIVER_AC);
                }
            } catch (Exception e) {
                e.printStackTrace();
                handler.postDelayed(runnablehasdriverAccepted,
                        Constants_user.DRIVER_FOUND_CAB_HAS_DRIVER_AC);
            }

        }
    }

    private String bookTime(String passengerbooking_time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar c = Calendar.getInstance();
            c.setTime(sdf.parse(passengerbooking_time));
            SimpleDateFormat df = new SimpleDateFormat("HH:mm");
            String sTodayDate = df.format(c.getTime());
            return sTodayDate;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void onClickPassenger(View v) {
    }

    public void onClickDriverInfo(View view) {
        showThumbImageClick();
    }

    private void init1() {
        mIvPhoto = (ImageView) findViewById(com.jumpinjumpout.apk.lib.R.id.ivPhoto);
        mIvPassenger1 = (ImageView) findViewById(com.jumpinjumpout.apk.lib.R.id.ivPassenger1);
        mLlWaitingPassenger = (LinearLayout) findViewById(com.jumpinjumpout.apk.lib.R.id.llWaitingPassenger);
        mLlJoinPassenger = (LinearLayout) findViewById(com.jumpinjumpout.apk.lib.R.id.llJoinPassenger);
        mTvJoinedCount = (TextView) findViewById(com.jumpinjumpout.apk.lib.R.id.tvJoinedCount);
        mtvJoinedCountNumber = (TextView) findViewById(com.jumpinjumpout.apk.lib.R.id.tvJoinedCountNumber);
        mTvInfoBandLine1 = (TextView) findViewById(com.jumpinjumpout.apk.lib.R.id.tvInfoBandLine1);
        mTvInfoBandLine2 = (TextView) findViewById(com.jumpinjumpout.apk.lib.R.id.tvInfoBandLine2);
        mTvInfoBandLine3 = (ImageView) findViewById(com.jumpinjumpout.apk.lib.R.id.tvInfoBandLine3);
        mTvTime = (TextView) findViewById(com.jumpinjumpout.apk.lib.R.id.tvTime);
        mTvMins = (TextView) findViewById(com.jumpinjumpout.apk.lib.R.id.tvMins);
        mTvInfoBandLine4 = (TextView) findViewById(com.jumpinjumpout.apk.lib.R.id.tvInfoBandLine4);
        mTvKmAway = (TextView) findViewById(com.jumpinjumpout.apk.lib.R.id.tvKmAway);
        mLlPassengerInfo = (LinearLayout) findViewById(com.jumpinjumpout.apk.lib.R.id.llPassengerInfo);
        mTvJumpedInCount = (TextView) findViewById(com.jumpinjumpout.apk.lib.R.id.tvJumpedInCount);
    }

    public void showThumbImageClick() {
        try {
            String sptrip = CGlobals_lib.getInstance().getSharedPreferences(getApplicationContext()).
                    getString(Constants_lib.TRIP_TO_ADDRESS, "");
            final Dialog dialog = new Dialog(GetaCab_act.this, R.style.DIALOG);
            dialog.setContentView(R.layout.passengerinfo_image);
            dialog.setTitle(getCab.getName());
            final TextView mifromadd, mitoadd, milocation, miinternet,
                    mitripprogess;
            final ImageView mProImage;
            ImageButton mCall, mWhatsApp, mSMS;

            mProImage = (ImageView) dialog.findViewById(R.id.ivPassengerImage);
            miinternet = (TextView) dialog.findViewById(R.id.internet);
            milocation = (TextView) dialog.findViewById(R.id.clocation);
            mifromadd = (TextView) dialog.findViewById(R.id.tvName);
            mitoadd = (TextView) dialog.findViewById(R.id.tvPhoneNo);
            mitripprogess = (TextView) dialog.findViewById(R.id.tvKmAway);
            mCall = (ImageButton) dialog.findViewById(R.id.btnCall);
            mWhatsApp = (ImageButton) dialog.findViewById(R.id.btnWhatApp);
            mSMS = (ImageButton) dialog.findViewById(R.id.btnSMS);

            if (getCab.getlTimeDiff() < 1) {
                miinternet.setText("Online");
                miinternet.setVisibility(View.VISIBLE);
            } else {
                miinternet.setText("Last online " + getCab.getlTimeDiff() + " min. ago");
                miinternet.setVisibility(View.VISIBLE);
            }
            if (getCab.getlTimeDifflocation() < 30) {
                milocation.setText("Current location available");
                milocation.setVisibility(View.VISIBLE);
            } else {
                if (getCab.getlTimeDifflocation() < 60) {
                    milocation.setText("Location delayed " + getCab.getlTimeDifflocation() + " secs. ago");
                } else {
                    milocation.setText("Location delayed " + getCab.getlTimeDifflocation() / 60 + " min. ago");
                }
                milocation.setVisibility(View.VISIBLE);
            }
            mitoadd.setVisibility(View.GONE);
            mifromadd.setVisibility(View.GONE);
            mitripprogess.setText(passengerTODriverDistance() + " Km away");
            mitripprogess.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(getCab.getUserProfileImageFileName()) && !TextUtils.isEmpty(getCab.getUserProfileImagePath())) {
                String url = Constants_user.GET_USER_PROFILE_IMAGE_FILE_NAME_URL + getCab.getUserProfileImagePath() +
                        getCab.getUserProfileImageFileName();
                ImageRequest request = new ImageRequest(url,
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap bitmap) {
                                if (Build.VERSION.SDK_INT < 11) {
                                    Toast.makeText(GetaCab_act.this, getString(R.string.api11not), Toast.LENGTH_LONG).show();
                                } else {
                                    mProImage.setImageBitmap(bitmap);
                                }
                            }
                        }, 0, 0, null,
                        new Response.ErrorListener() {
                            public void onErrorResponse(VolleyError error) {
                                mProImage.setImageResource(R.mipmap.ic_unknowuser);
                            }
                        });
                MyApplication.getInstance().addToRequestQueue(request);
            } else {
                mProImage.setImageResource(R.mipmap.ic_unknowuser);
            }
            Bitmap bitmap = getCab.getContactThnumbnail();
            if (bitmap != null) {
                mProImage.setImageBitmap(bitmap);
            }
            mCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + getCab.getPhoneNo()));
                    startActivity(callIntent);
                }
            });
            mWhatsApp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendAppMsg(getCab.getPhoneNo());
                }
            });
            mSMS.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent smsVIntent = new Intent(Intent.ACTION_VIEW);
                    smsVIntent.setType("vnd.android-dir/mms-sms");
                    smsVIntent.putExtra("address", getCab.getPhoneNo());
                    try {
                        startActivity(smsVIntent);
                    } catch (Exception ex) {
                        Toast.makeText(GetaCab_act.this, "Your sms has failed...",
                                Toast.LENGTH_LONG).show();
                        ex.printStackTrace();
                    }
                }
            });
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendAppMsg(String phNo) {
        Uri uri = Uri.parse("smsto:" + phNo.trim());
        Intent intent = new Intent(Intent.ACTION_SEND, uri);
        intent.setType("text/plain");
        String text = "About my Jump in Jump out trip...";
        intent.setPackage("com.whatsapp");
        if (intent != null) {
            intent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(intent, text));
        } else {
            Toast.makeText(this, "App not found", Toast.LENGTH_SHORT).show();
        }
    }

    private String passengerTODriverDistance() {
        String distanceFromMe = "";
        if (CGlobals_lib.getInstance().getMyLocation(GetaCab_act.this) != null) {
            LatLng latlng = new LatLng(CGlobals_lib.getInstance().getMyLocation(GetaCab_act.this).getLatitude(),
                    CGlobals_lib.getInstance().getMyLocation(GetaCab_act.this).getLongitude());
            if (getCab.getFromLat() != -999 && getCab.getFromLng() != -999 && latlng != null) {
                float results[] = new float[1];
                Location.distanceBetween(getCab.getFromLat(),
                        getCab.getFromLng(), latlng.latitude,
                        latlng.longitude, results);
                try {
                    if (getCab.getMiPassed() == 1) {
                        distanceFromMe = getCab.getMiPassed() > 0 ? " (Passed)" : "";
                        mTvInfoBandLine2.setText(distanceFromMe);
                    } else if (results[0] < Constants_user.NEAR_BE_READY_DISTANCE) {
                        distanceFromMe = "Be Ready!";
                        mTvInfoBandLine2.setText(distanceFromMe);
                    }
                    distanceFromMe = String.format("%.2f", results[0] / 1000);
                    return distanceFromMe;
                } catch (Exception e) {
                    SSLog.e(TAG, "getView", e);
                }
            }
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        isBookDriver = false;
        if (!isback) {
            mMap.clear();
            canceldialog();
        } else {
            super.onBackPressed();
        }
    }

    private void canceldialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(GetaCab_act.this);
        alertDialog.setTitle("Are you sure do you cancel your trip?");
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                driverCancelCabTrip("0");
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        if (!GetaCab_act.this.isFinishing()) {
            alertDialog.show();
        }
    }

    private void driverCancelCabTrip(final String value) {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.CANCEL_CAB_TRIP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getCancelCabTrip(response, value);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String sErr = CGlobals_lib.getInstance().getVolleyError(error);
                Toast.makeText(GetaCab_act.this, sErr, Toast.LENGTH_SHORT).show();
                try {
                    SSLog.d(TAG,
                            "Failed to driverCancelCabTrip :-  "
                                    + error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG, "driverCancelCabTrip - " + sErr,
                            error.getMessage());
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("cabtripid", String.valueOf(cab_trip_id));
                params = CGlobals_user.getInstance().getMinMobileParams(params, Constants_user.CANCEL_CAB_TRIP_URL);
                return CGlobals_user.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    }

    private void getCancelCabTrip(String response, String value) {
        if (value.equals("0")) {
            GetaCab_act.this.finish();
        }
    }

    protected void goFacebook() {
    }

    protected void goTwitter() {
    }

    private void hasDriverStarted() {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.HAS_DRIVER_STARTED_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        userhasDriverStarted(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handler.postDelayed(runnablehasdriverStarted,
                        Constants_user.DRIVER_FOUND_CAB_HAS_DRIVER_AC);
                String sErr = CGlobals_lib.getInstance().getVolleyError(error);
                Toast.makeText(GetaCab_act.this, sErr, Toast.LENGTH_SHORT).show();
                try {
                    SSLog.d(TAG,
                            "Failed to hasDriverStarted :-  "
                                    + error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG, "hasDriverStarted - " + sErr,
                            error.getMessage());
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("cabtripid", String.valueOf(cab_trip_id));
                params = CGlobals_user.getInstance().getMinMobileParams(params, Constants_user.HAS_DRIVER_STARTED_URL);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_user.HAS_DRIVER_STARTED_URL;
                try {
                    String url = url1 + "?" + getParams.toString();
                    System.out.println("url  " + url);
                } catch (Exception e) {
                    SSLog.e(TAG, "hasDriverAccepted", e);
                }
                return CGlobals_user.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    }

    private void userhasDriverStarted(String response) {
        if (TextUtils.isEmpty(response.trim())) {
            handler.postDelayed(runnablehasdriverStarted,
                    Constants_user.DRIVER_FOUND_CAB_HAS_DRIVER_AC);
            return;
        }
        if (response.trim().equals("-1")) {
            handler.postDelayed(runnablehasdriverStarted,
                    Constants_user.DRIVER_FOUND_CAB_HAS_DRIVER_AC);
            return;
        }
        try {
            JSONObject jResponse = new JSONObject(response);
            int trip_id = jResponse.isNull("trip_id") ? -1
                    : jResponse.getInt("trip_id");
            int cab_trip_id_start = jResponse.isNull("cab_trip_id") ? -1
                    : jResponse.getInt("cab_trip_id");
            String trip_action = jResponse.isNull("trip_action") ? ""
                    : jResponse.getString("trip_action");
            if (trip_action.equals("B") && trip_id != -1 && cab_trip_id_start == cab_trip_id) {
                MyApplication.getInstance().getPersistentPreferenceEditor().
                        putInt(Constants_user.PREF_TRIP_ID, trip_id);
                MyApplication.getInstance().getPersistentPreferenceEditor().
                        putBoolean(Constants_user.HAS_DESTINATION, false);
                MyApplication.getInstance().getPersistentPreferenceEditor().
                        putBoolean(Constants_user.GET_A_CAB_VALUE, true);
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                Intent i = new Intent(GetaCab_act.this, GetACabTrackDriver_act.class);
                startActivity(i);
                finish();
            } else {
                handler.postDelayed(runnablehasdriverStarted,
                        Constants_user.DRIVER_FOUND_CAB_HAS_DRIVER_AC);
            }
        } catch (Exception e) {
            e.printStackTrace();
            handler.postDelayed(runnablehasdriverStarted,
                    Constants_user.DRIVER_FOUND_CAB_HAS_DRIVER_AC);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverGetaCab);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.gc();
    }
}
