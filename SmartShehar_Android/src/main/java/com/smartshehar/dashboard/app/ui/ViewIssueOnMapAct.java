package com.smartshehar.dashboard.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.smartshehar.dashboard.app.CGlobals_db;
import com.smartshehar.dashboard.app.CMarker;
import com.smartshehar.dashboard.app.Constants_dp;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSApp;
import com.smartshehar.dashboard.app.SSLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import lib.app.util.CAddress;
import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;
import lib.app.util.Constants_lib_ss;
import lib.app.util.GeoHelper;
import lib.app.util.MyLocation;
import lib.app.util.SearchAddress_act;

public class ViewIssueOnMapAct extends FragmentActivity
        implements GoogleMap.OnMyLocationChangeListener,
        View.OnClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    TextView txtCount, tvLocation, txtfilterby;
    private static final int REQUEST_FITER_CODE = 1;
    protected GoogleMap mMap;
    boolean mIsMapFirstTime = false;
    public static final float DEFAULT_ZOOM = 15;
    public static final String TAG = "ViewIssueOnMapAct";
    JSONArray resultRow = null;
    LinearLayout llYourLoaction;
    ArrayList<CMarker> mLatLngList = new ArrayList<>();
    Connectivity mConnectivity;

    int mShowAllVal = 0, mMyIssueVal = 0;
    String PVVal = "", MVVal = "", RTVal = "", AUTVal = "", RDVal = "", CLVal = "",
            ENVal = "", SFVal = "", WEVal = "", OGVal = "", RSVal = "",
            mPostalCode = "", mFromDate, mToDate, closedVal = "0", resolvedVal = "0";
    ArrayList<Marker> mMarkerList = new ArrayList<>();
    String formatted_address;
    TileOverlay mOverlay;
    List<LatLng> mList = null;
    ImageView ivLocation, ivHelp;
    Location mCurrentLocation, tempLocation;
    Button btnWnatToHelp;
    ImageView ivFilter;
    boolean autoRefresh = true, inCall = false;
    private float currentZoom;
    GoogleApiClient mGoogleApiClient = null;
    LatLngBounds currenBounds;
    double nmaxLat = -999, nmaxLng = -999, sminLat = -999, sminLng = -999;
    boolean mTimeOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_issue_on_map);
        mGoogleApiClient = new GoogleApiClient.Builder(ViewIssueOnMapAct.this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(ViewIssueOnMapAct.this)
                .addOnConnectionFailedListener(ViewIssueOnMapAct.this)
                .build();
        mGoogleApiClient.connect();
        mConnectivity = new Connectivity();
        if (!Connectivity.checkConnected(ViewIssueOnMapAct.this)) {
            if (!mConnectivity.connectionError(ViewIssueOnMapAct.this,
                    getString(R.string.requires_internet))) {
                Log.d(TAG, "Internet Connection");
            }
        }
        if (mCurrentLocation == null) {
            mCurrentLocation = new Location("");

            mCurrentLocation.setLatitude(Double.valueOf(CGlobals_lib_ss.getInstance()
                    .getPersistentPreference(ViewIssueOnMapAct.this).getString(Constants_dp.PREF_GET_LAST_LAT,
                            String.valueOf(Constants_dp.MINCITYLAT))));
            mCurrentLocation.setLongitude(Double.valueOf(CGlobals_lib_ss.getInstance()
                    .getPersistentPreference(ViewIssueOnMapAct.this).getString(Constants_dp.PREF_GET_LAST_LNG,
                            String.valueOf(Constants_dp.MINCITYLON))));
        }
        CGlobals_db.getInstance(ViewIssueOnMapAct.this).
                turnGPSOn(ViewIssueOnMapAct.this, mGoogleApiClient);
        MyLocation myLocation = new MyLocation(
                SSApp.mRequestQueue, ViewIssueOnMapAct.this, mGoogleApiClient);
        myLocation.getLocation(this, onLocationResult);
        init();
        formatted_address = CGlobals_lib_ss.getInstance()
                .getPersistentPreference(ViewIssueOnMapAct.this).getString(Constants_dp.PREF_GET_LAST_ADDRESS, "");
        if (autoRefresh)
            tvLocation.setText(formatted_address);

        mPostalCode = "";
        setValues();
        setUpMap();

        llYourLoaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewIssueOnMapAct.this, SearchAddress_act.class);
                startActivityForResult(intent, Constants_lib_ss.FINDADDRESS_FROM);
                autoRefresh = false;
            }
        });
        ivHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CGlobals_db.showInformationalDialog(ViewIssueOnMapAct.this, getString(R.string.mapsummary));
            }
        });

        ivFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewIssueOnMapAct.this, ActIssueFilter.class);
                startActivityForResult(intent, REQUEST_FITER_CODE);
            }
        });
        btnWnatToHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CGlobals_db.showInformationalDialog(ViewIssueOnMapAct.this, getString(R.string.thanking));
            }
        });
        ivLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CGlobals_db.getInstance(ViewIssueOnMapAct.this).
                        turnGPSOn(ViewIssueOnMapAct.this, mGoogleApiClient);
                try {
                    tempLocation = CGlobals_db.getInstance(ViewIssueOnMapAct.this).getMyLocation(ViewIssueOnMapAct.this);
                    if (tempLocation != null)
                        mCurrentLocation = tempLocation;
                    autoRefresh = true;
                    GeoHelper geoHelper = new GeoHelper();
                    geoHelper.getAddress(ViewIssueOnMapAct.this, mCurrentLocation, onGeoHelperResult);
                    CGlobals_db.getInstance(ViewIssueOnMapAct.this).turnGPSOn(ViewIssueOnMapAct.this, mGoogleApiClient);
                    moveCamera();

                } catch (Exception e) {
                    Log.d(TAG, "llGpsMsg onClick " + e.toString());
                    e.printStackTrace();
                }
            }
        });
    }

    private void init() {
        currentZoom = DEFAULT_ZOOM;
        txtCount = (TextView) findViewById(R.id.txtCount);
        txtfilterby = (TextView) findViewById(R.id.txtfilterby);
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        ivFilter = (ImageView) findViewById(R.id.ivFilter);
        llYourLoaction = (LinearLayout) findViewById(R.id.llYourLoaction);
        ivLocation = (ImageView) findViewById(R.id.ivLocation);
        ivHelp = (ImageView) findViewById(R.id.ivHelp);
        btnWnatToHelp = (Button) findViewById(R.id.btnWnatToHelp);
    }

    private MyLocation.LocationResult onLocationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            try {
                if (location == null) {
                    Log.d(TAG, "No Location found");
                } else {
                    CGlobals_db.getInstance(ViewIssueOnMapAct.this).
                            setMyLocation(location, false, ViewIssueOnMapAct.this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void moveCamera() {
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mCurrentLocation.getLatitude(),
                            mCurrentLocation.getLongitude()), DEFAULT_ZOOM));
        }
    }

    private void rpFilterCategorywise() {
        try {
            inCall = true;
            final String url = Constants_dp.ISSUE_REPORT_URL;
            final StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (!response.trim().equals("-1")) {
//                                showIssueOnMap(response);
                              new ShowIssueOnMap().execute(response);

                            } else {
                                if (mLatLngList.size() > 0) {
                                    mLatLngList.clear();
                                }
                                if (mMarkerList.size() > 0 && mMap != null) {
                                    mMarkerList.clear();
                                    mMap.clear();
                                }
                                inCall = false;
                                txtCount.setText("No issue Found");
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    inCall = false;
                    if(!mTimeOut)
                    {
                        mTimeOut = true;
                        CGlobals_db.showInformationalDialog(ViewIssueOnMapAct.this, getString(R.string.time_out));
                    }
                    Log.d(TAG, "error is " + error.toString());
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("submitreport", "1");
                    if (mShowAllVal == 1)
                        params.put("showall", String.valueOf(mShowAllVal));
                    if (mMyIssueVal == 1)
                        params.put("myissue", String.valueOf(mMyIssueVal));
                    if (!TextUtils.isEmpty(mPostalCode))
                        params.put("postalcode", mPostalCode);
                    if (!TextUtils.isEmpty(mFromDate))
                        params.put("fromissuedate", mFromDate);
                    if (!TextUtils.isEmpty(mToDate))
                        params.put("toissuedate", mToDate);

                    if (!TextUtils.isEmpty(PVVal))
                        params.put("PV", PVVal);
                    if (!TextUtils.isEmpty(MVVal))
                        params.put("MV", MVVal);
                    if (!TextUtils.isEmpty(RTVal))
                        params.put("RT", RTVal);
                    if (!TextUtils.isEmpty(AUTVal))
                        params.put("AT", AUTVal);
                    if (!TextUtils.isEmpty(RDVal))
                        params.put("RD", RDVal);
                    if (!TextUtils.isEmpty(CLVal))
                        params.put("CL", CLVal);
                    if (!TextUtils.isEmpty(ENVal))
                        params.put("EN", ENVal);
                    if (!TextUtils.isEmpty(SFVal))
                        params.put("SF", SFVal);
                    if (!TextUtils.isEmpty(WEVal))
                        params.put("WE", WEVal);
                    if (!TextUtils.isEmpty(OGVal))
                        params.put("OG", OGVal);
                    if (!TextUtils.isEmpty(RSVal))
                        params.put("RS", RSVal);

                    if (nmaxLat != -999)
                        params.put("maxlat", String.format("%.9f", nmaxLat));
                    if (nmaxLng != -999)
                        params.put("maxlng", String.format("%.9f", nmaxLng));
                    if (sminLat != -999)
                        params.put("minlat", String.format("%.9f", sminLat));
                    if (sminLng != -999)
                        params.put("minlng", String.format("%.9f", sminLng));
                    if (!closedVal.equalsIgnoreCase("0"))
                        params.put("closed", closedVal);
                    if (!resolvedVal.equalsIgnoreCase("0"))
                        params.put("resolved", resolvedVal);
                    params = CGlobals_db.getInstance(ViewIssueOnMapAct.this).
                            getBasicMobileParams(params, url, ViewIssueOnMapAct.this);
                    return CGlobals_db.getInstance(ViewIssueOnMapAct.this).checkParams(params);
                }
            };
            CGlobals_db.getInstance(ViewIssueOnMapAct.this).getRequestQueue(ViewIssueOnMapAct.this).add(postRequest);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ERROR IS " + e.toString());
        }
    }

    private  class ShowIssueOnMap extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {
            String data = params[0];

            return data;
        }

        @Override
        protected void onPostExecute(String data) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            BitmapDescriptor icon;
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            if (mLatLngList.size() > 0) {
                mLatLngList.clear();
            }
            Marker marker;
            if (mMarkerList.size() > 0 && mMap != null) {
                mMarkerList.clear();
                mMap.clear();
            }
            LatLng mLatLng ;
            JSONObject jObj;
            String slat, slng, mIssueName, mComplaintTypeCode;//,mColour;
            double mLat, mLng;
            int count = 0;//, color,;
            CMarker mcLatLng;
            String mIssueId;
            if (mMap != null && !TextUtils.isEmpty(data)) {
                try {
                    mList = new ArrayList<>();
                    resultRow = new JSONArray(data);
                    txtfilterby.setVisibility(View.VISIBLE);
                    for (int j = 0; j < resultRow.length(); j++) {
                        jObj = resultRow.getJSONObject(j);
                        mIssueId = jObj.isNull("issue_id") ? "" : jObj.getString("issue_id");
                        mIssueName = jObj.isNull("issue_item_description") ? "" : jObj.getString("issue_item_description");
                        slat = jObj.isNull("lat") ? "" : jObj.getString("lat");
                        slng = jObj.isNull("lng") ? "" : jObj.getString("lng");
                        mComplaintTypeCode = jObj.isNull("issue_type_code") ? "" : jObj.getString("issue_type_code");
                        if (!TextUtils.isEmpty(slat) || !TextUtils.isEmpty(slng)) {
                            mLat = Double.parseDouble(slat);
                            mLng = Double.parseDouble(slng);
                            mLatLng = new LatLng(mLat, mLng);
                            mcLatLng = new CMarker(mLat, mLng, mComplaintTypeCode, mIssueName);
                            mLatLngList.add(mcLatLng);
                            mList.add(mLatLng);
                            switch (mComplaintTypeCode) {
                                case "PV":
                                    icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_parking);
                                    break;
                                case "MV":
                                    icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_moving);
                                    break;
                                case "RT":
                                    icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_pollution);
                                    break;
                                case "AT":
                                    icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_taxi);
                                    break;
                                case "RD":
                                    icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_road);
                                    break;
                                case "CL":
                                    icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_cleanliness);
                                    break;
                                case "WE":
                                    icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_infra);
                                    break;
                                case "EN":
                                    icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_hawkers);
                                    break;
                                case "OG":
                                    icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_greenery);
                                    break;
                                case "SF":
                                    icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_safety);
                                    break;
                                case "RS":
                                    icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_service);
                                    break;
                                default:
                                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
                                    break;
                            }
                            if (icon != null) {
                                marker = mMap.addMarker(new MarkerOptions().position(mLatLng).
                                        snippet(mIssueId).title(mIssueName).icon(icon));
                                mMarkerList.add(marker);
                            }
                        }
                    }
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    inCall = false;
                    if (currentZoom >= 12) {
                        showMarker();
                    }
                    if (currentZoom < 12) {
                        hideMarker();
                    }
                    for (Marker mrk : mMarkerList) {
                        builder.include(mrk.getPosition());
                    }
                    // LatLngBounds bounds = builder.build();
                    // int padding = 0;
                    //CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
//                mMap.animateCamera(cu);

                    count = mMarkerList.size();
                    if (count < 0 || count == 0) {
                        txtCount.setText(getString(R.string.noissue));
                    } else
                        txtCount.setText(getString(R.string.total) + ": " + String.valueOf(count));


                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {

                            Bundle args = new Bundle();
                            args.putParcelable("location", marker.getPosition());
                            if (!TextUtils.isEmpty(marker.getSnippet()))
                                args.putString("issueid", marker.getSnippet());
                            if (!TextUtils.isEmpty(mPostalCode))
                                args.putString("postalcode", mPostalCode);
                            args.putBoolean("mFromMap", true);
                            Intent intent = new Intent(ViewIssueOnMapAct.this, ActListOfNearByIssues.class);
                            intent.putExtra("bundle", args);
                            startActivity(intent);
                            return false;
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    SSLog.e(TAG, "showIssueOnMap ", e);
                }
            }
        }
    }
    private void showIssueOnMap(final String data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                BitmapDescriptor icon;
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                if (mLatLngList.size() > 0) {
                    mLatLngList.clear();
                }
                Marker marker;
                if (mMarkerList.size() > 0 && mMap != null) {
                    mMarkerList.clear();
                    mMap.clear();
                }
                LatLng mLatLng ;
                JSONObject jObj;
                String slat, slng, mIssueName, mComplaintTypeCode;//,mColour;
                double mLat, mLng;
                int count = 0;//, color,;
                CMarker mcLatLng;
                String mIssueId;
                if (mMap != null && !TextUtils.isEmpty(data)) {
                    try {
                        mList = new ArrayList<>();
                        resultRow = new JSONArray(data);
                        txtfilterby.setVisibility(View.VISIBLE);
                        for (int j = 0; j < resultRow.length(); j++) {
                            jObj = resultRow.getJSONObject(j);
                            mIssueId = jObj.isNull("issue_id") ? "" : jObj.getString("issue_id");
                            mIssueName = jObj.isNull("issue_item_description") ? "" : jObj.getString("issue_item_description");
                            slat = jObj.isNull("lat") ? "" : jObj.getString("lat");
                            slng = jObj.isNull("lng") ? "" : jObj.getString("lng");
                            mComplaintTypeCode = jObj.isNull("issue_type_code") ? "" : jObj.getString("issue_type_code");
                            if (!TextUtils.isEmpty(slat) || !TextUtils.isEmpty(slng)) {
                                mLat = Double.parseDouble(slat);
                                mLng = Double.parseDouble(slng);
                                mLatLng = new LatLng(mLat, mLng);
                                mcLatLng = new CMarker(mLat, mLng, mComplaintTypeCode, mIssueName);
                                mLatLngList.add(mcLatLng);
                                mList.add(mLatLng);
                                switch (mComplaintTypeCode) {
                                    case "PV":
                                        icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_parking);
                                        break;
                                    case "MV":
                                        icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_moving);
                                        break;
                                    case "RT":
                                        icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_pollution);
                                        break;
                                    case "AT":
                                        icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_taxi);
                                        break;
                                    case "RD":
                                        icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_road);
                                        break;
                                    case "CL":
                                        icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_cleanliness);
                                        break;
                                    case "WE":
                                        icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_infra);
                                        break;
                                    case "EN":
                                        icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_hawkers);
                                        break;
                                    case "OG":
                                        icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_greenery);
                                        break;
                                    case "SF":
                                        icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_safety);
                                        break;
                                    case "RS":
                                        icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_service);
                                        break;
                                    default:
                                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
                                        break;
                                }
                                if (icon != null) {
                                    marker = mMap.addMarker(new MarkerOptions().position(mLatLng).
                                            snippet(mIssueId).title(mIssueName).icon(icon));
                                    mMarkerList.add(marker);
                                }
                            }
                        }
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        inCall = false;
                        if (currentZoom >= 12) {
                            showMarker();
                        }
                        if (currentZoom < 12) {
                            hideMarker();
                        }
                        for (Marker mrk : mMarkerList) {
                            builder.include(mrk.getPosition());
                        }
                        // LatLngBounds bounds = builder.build();
                        // int padding = 0;
                        //CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
//                mMap.animateCamera(cu);

                        count = mMarkerList.size();
                        if (count < 0 || count == 0) {
                            txtCount.setText(getString(R.string.noissue));
                        } else
                            txtCount.setText(getString(R.string.total) + ": " + String.valueOf(count));


                        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {

                                Bundle args = new Bundle();
                                args.putParcelable("location", marker.getPosition());
                                if (!TextUtils.isEmpty(marker.getSnippet()))
                                    args.putString("issueid", marker.getSnippet());
                                if (!TextUtils.isEmpty(mPostalCode))
                                    args.putString("postalcode", mPostalCode);
                                args.putBoolean("mFromMap", true);
                                Intent intent = new Intent(ViewIssueOnMapAct.this, ActListOfNearByIssues.class);
                                intent.putExtra("bundle", args);
                                startActivity(intent);
                                return false;
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        SSLog.e(TAG, "showIssueOnMap ", e);
                    }
                }
            }
        });

    }

    private void showMarker() {
        Marker marker;
        if (mOverlay != null)
            mOverlay.remove();
        if (mMap != null) {
            if (mMarkerList.size() > 0) {
                for (int m = 0; m < mMarkerList.size(); m++) {
                    marker = mMarkerList.get(m);
                    marker.setVisible(true);
                }
            }
        }
    }

    private void hideMarker() {
        addHeatMap(mList);
        Marker marker;
        if (mMap != null) {
            if (mMarkerList.size() > 0) {
                for (int m = 0; m < mMarkerList.size(); m++) {
                    marker = mMarkerList.get(m);
                    marker.setVisible(false);
                }
            }
        }

    }

    private void addHeatMap(List<LatLng> list) {
        try {
            int[] colors = {
                    Color.rgb(0, 191, 255), // blue
                    Color.rgb(255, 0, 0)    // red
            };

            float[] startPoints = {
                    0.2f, 1f
            };
            Gradient gradient = new Gradient(colors, startPoints);
            HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                    .data(list)
                    .gradient(gradient)
                    .build();
            mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
            Log.d(TAG, "" + mOverlay);
            mProvider.setOpacity(0.7);
            mOverlay.clearTileCache();
        } catch (Exception e) {
//                Toast.makeText(ViewIssueOnMapAct.this, "error is " + e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            SSLog.e(TAG, "showIssueOnMap ", e);
        }
    }

    protected void setUpMap() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (mMap == null) {

            mIsMapFirstTime = true;
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(
                    R.id.frIssuMap)).getMap();

            if (mMap != null) {
                if (ActivityCompat.checkSelfPermission(ViewIssueOnMapAct.this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(ViewIssueOnMapAct.this,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                                PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mMap.setOnMyLocationChangeListener(this);
                tempLocation = CGlobals_db.getInstance(ViewIssueOnMapAct.this).getMyLocation(ViewIssueOnMapAct.this);
                if (tempLocation != null)
                    mCurrentLocation = tempLocation;
                if (mCurrentLocation != null)
                    moveCamera();
                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        mapLoaded();
                    }
                });
                mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition position) {

                        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                        currentZoom = position.zoom;
                        currenBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                        nmaxLat = currenBounds.northeast.latitude;
                        nmaxLng = currenBounds.northeast.longitude;
                        sminLat = currenBounds.southwest.latitude;
                        sminLng = currenBounds.southwest.longitude;
                        Log.d(TAG, "" + position.zoom);

                        final LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                        final LatLng ne = bounds.northeast;
                        final LatLng sw = bounds.southwest;
                        Timer t = null;
                        if (t != null) {
                            t.purge();
                            t.cancel();
                        }
                        t = new Timer();
                        final Timer finalT = t;
                        t.schedule(new TimerTask() {
                            public void run() {
                                double ne1 = 0;
                                double ne2 = 0;
                                double sw1 = 0;
                                double sw2 = 0;
                                if (ne1 != ne.latitude && ne2 != ne.longitude && sw1 != sw.latitude && sw2 != sw.longitude) {
                                    Log.d(TAG, "Refreshing data");

                                    rpFilterCategorywise();
                                    finalT.cancel();
                                }
                                finalT.cancel(); // also just top the timer thread, otherwise, you may receive a crash report
                            }
                        }, 1000);

                    }
                });
            }
        }
    }

    private void mapLoaded() {
        tempLocation = CGlobals_db.getInstance(ViewIssueOnMapAct.this).getMyLocation(ViewIssueOnMapAct.this);
        if (tempLocation != null)
            mCurrentLocation = tempLocation;
        if (mCurrentLocation != null) {
            mPostalCode = "";
            GeoHelper geoHelper = new GeoHelper();
            geoHelper.getAddress(ViewIssueOnMapAct.this, mCurrentLocation, onGeoHelperResult);
            String json = new Gson().toJson(mCurrentLocation);
            CGlobals_lib_ss.getInstance()
                    .getPersistentPreferenceEditor(ViewIssueOnMapAct.this).
                    putString(Constants_dp.PREF_MY_LOCATION, json);
        }
    }

    private GeoHelper.GeoHelperResult onGeoHelperResult = new GeoHelper.GeoHelperResult() {
        @Override
        public void gotAddress(CAddress addr) {
            if (addr!=null) {
                if (addr.hasLatitude() || addr.hasLongitude()) {
                    if (!TextUtils.isEmpty(addr.getAddress())) {
                        CGlobals_db.mAddress = addr.getAddress();
                        CGlobals_lib_ss.getInstance()
                                .getPersistentPreferenceEditor(ViewIssueOnMapAct.this).putString(Constants_dp.PREF_GET_LAST_ADDRESS, addr.getAddress()).commit();
                        if (autoRefresh && !TextUtils.isEmpty(addr.getAddress()))
                            tvLocation.setText(addr.getAddress());
                    }
                } else {
                    formatted_address = CGlobals_lib_ss.getInstance()
                            .getPersistentPreference(ViewIssueOnMapAct.this).getString(Constants_dp.PREF_GET_LAST_ADDRESS, "");
                    if (!TextUtils.isEmpty(formatted_address))
                        tvLocation.setText(formatted_address);
                    Toast.makeText(ViewIssueOnMapAct.this, "Connection is failed. Please try again later.", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        MyLocation myLocation = new MyLocation(
                SSApp.mRequestQueue, ViewIssueOnMapAct.this, mGoogleApiClient);
        myLocation.getLocation(this, onLocationResult);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {


    }

    @Override
    public void onMyLocationChange(Location location) {
        mCurrentLocation = location;
        GeoHelper geoHelper = new GeoHelper();
        geoHelper.getAddress(ViewIssueOnMapAct.this, mCurrentLocation, onGeoHelperResult);
        String json = new Gson().toJson(mCurrentLocation);
        CGlobals_lib_ss.getInstance()
                .getPersistentPreferenceEditor(ViewIssueOnMapAct.this).
                putString(Constants_dp.PREF_MY_LOCATION, json);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String sAddr;
        CAddress oAddr;
        if (requestCode == Constants_lib_ss.FINDADDRESS_FROM) {

            if (resultCode == Activity.RESULT_OK) {
                sAddr = data.getStringExtra("add");
                Type type = new TypeToken<CAddress>() {
                }.getType();
                oAddr = new Gson().fromJson(sAddr, type);

                if (oAddr.getLatitude() != Constants_lib_ss.INVALIDLAT
                        && oAddr.getLongitude() != Constants_lib_ss.INVALIDLNG) {

                    if (sAddr.equals("")) {
                        Toast.makeText(ViewIssueOnMapAct.this, "Please check your internet connection", Toast.LENGTH_LONG).show();

                    }
                    tvLocation.setText(oAddr.getAddress());
                    mPostalCode = "";
                    if (mMap != null) {
                        if (mCurrentLocation == null)
                            mCurrentLocation = new Location("");
                        mCurrentLocation.setLatitude(oAddr.getLatitude());
                        mCurrentLocation.setLongitude(oAddr.getLongitude());
                        moveCamera();

                    }
                }
            }
        }
        if (requestCode == CGlobals_db.REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Location location = CGlobals_db.getInstance(ViewIssueOnMapAct.this).getMyLocation(ViewIssueOnMapAct.this);
                    mCurrentLocation = CGlobals_db.getInstance(ViewIssueOnMapAct.this).isBetterLocation(mCurrentLocation, location) ? mCurrentLocation : location;
                    moveCamera();
                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    Log.d(TAG, "NO");
                    mCurrentLocation = new Location("");

                    mCurrentLocation.setLatitude(Double.valueOf(CGlobals_lib_ss.getInstance()
                            .getPersistentPreference(ViewIssueOnMapAct.this).getString(Constants_dp.PREF_GET_LAST_LAT,
                                    String.valueOf(Constants_dp.MINCITYLAT))));
                    mCurrentLocation.setLongitude(Double.valueOf(CGlobals_lib_ss.getInstance()
                            .getPersistentPreference(ViewIssueOnMapAct.this).getString(Constants_dp.PREF_GET_LAST_LNG,
                                    String.valueOf(Constants_dp.MINCITYLON))));

                    break;
                default:

                    break;
            }
        }
        if (requestCode == REQUEST_FITER_CODE) {
            mPostalCode = CGlobals_lib_ss.getInstance().getPersistentPreference(ViewIssueOnMapAct.this).
                    getString(Constants_dp.PREF_FILTER_POSTAL_CODE, "");
            setValues();
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            rpFilterCategorywise();
            if (!TextUtils.isEmpty(mPostalCode)) {
                Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
                List<Address> address ;

                try {
                    address = geoCoder.getFromLocationName(mPostalCode + ",India", 10);
                    if (address != null)
                        if (address.size() > 0) {
                            Address first = address.get(0);
                            double lat = first.getLatitude();
                            double lon = first.getLongitude();
//                                String add = first.getAddressLine(0);
                            tvLocation.setText(mPostalCode + ",India");
                            mCurrentLocation.setLatitude(lat);
                            mCurrentLocation.setLongitude(lon);
                            if (mMap != null) {
                                autoRefresh = false;
                                moveCamera();
                            }
                        }
                } catch (IOException e) {
                    SSLog.e(TAG, "onActivityResult", e);
                    e.printStackTrace();
                }

            }
        }
    }

    private void setValues() {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    SimpleDateFormat ft;
                    Date date;
                    String filterby = "";
                    mFromDate = CGlobals_lib_ss.getInstance().getPersistentPreference(ViewIssueOnMapAct.this).
                            getString(Constants_dp.PREF_FILTER_FROM_DATE, "");
                    mToDate = CGlobals_lib_ss.getInstance().getPersistentPreference(ViewIssueOnMapAct.this).
                            getString(Constants_dp.PREF_FILTER_TO_DATE, "");
                    if (CGlobals_lib_ss.getInstance().getPersistentPreference(ViewIssueOnMapAct.this).
                            getBoolean(Constants_dp.PREF_FILTER_CLOSED, false)) {
                        closedVal = "1";
                    } else closedVal = "0";

                    if (CGlobals_lib_ss.getInstance().getPersistentPreference(ViewIssueOnMapAct.this).
                            getBoolean(Constants_dp.PREF_FILTER_RESOLVED, false)) {
                        resolvedVal = "1";
                    } else resolvedVal = "0";

                    if (CGlobals_lib_ss.getInstance().getPersistentPreference(ViewIssueOnMapAct.this).
                            getBoolean(Constants_dp.PREF_FILTER_MY_COMPLAINTS, false)) {
                        mMyIssueVal = 1;
                    } else mMyIssueVal = 0;

                    if (CGlobals_lib_ss.getInstance().getPersistentPreference(ViewIssueOnMapAct.this).
                            getBoolean(Constants_dp.PREF_FILTER_ALL, true)) {
                        mShowAllVal = 1;
                    } else mShowAllVal = 0;

                    if (CGlobals_lib_ss.getInstance().getPersistentPreference(ViewIssueOnMapAct.this).
                            getBoolean(Constants_dp.PREF_FILTER_PV, true)) {
                        PVVal = getString(R.string.PVVal);
                    } else PVVal = "";
                    if (CGlobals_lib_ss.getInstance().getPersistentPreference(ViewIssueOnMapAct.this).
                            getBoolean(Constants_dp.PREF_FILTER_MV, true)) {
                        MVVal = getString(R.string.MVVal);
                    } else MVVal = "";
                    if (CGlobals_lib_ss.getInstance().getPersistentPreference(ViewIssueOnMapAct.this).
                            getBoolean(Constants_dp.PREF_FILTER_RT, true)) {
                        RTVal = getString(R.string.RTVal);
                    } else RTVal = "";
                    if (CGlobals_lib_ss.getInstance().getPersistentPreference(ViewIssueOnMapAct.this).
                            getBoolean(Constants_dp.PREF_FILTER_AUT, true)) {
                        AUTVal = getString(R.string.AUTVal);
                    } else AUTVal = "";
                    if (CGlobals_lib_ss.getInstance().getPersistentPreference(ViewIssueOnMapAct.this).
                            getBoolean(Constants_dp.PREF_FILTER_RD, true)) {
                        RDVal = getString(R.string.RDVal);
                    } else RDVal = "";
                    if (CGlobals_lib_ss.getInstance().getPersistentPreference(ViewIssueOnMapAct.this).
                            getBoolean(Constants_dp.PREF_FILTER_CL, true)) {
                        CLVal = getString(R.string.CLVal);
                    } else CLVal = "";
                    if (CGlobals_lib_ss.getInstance().getPersistentPreference(ViewIssueOnMapAct.this).
                            getBoolean(Constants_dp.PREF_FILTER_EN, true)) {
                        ENVal = getString(R.string.ENVal);
                    } else ENVal = "";
                    if (CGlobals_lib_ss.getInstance().getPersistentPreference(ViewIssueOnMapAct.this).
                            getBoolean(Constants_dp.PREF_FILTER_SF, true)) {
                        SFVal = getString(R.string.SFVal);
                    } else SFVal = "";
                    if (CGlobals_lib_ss.getInstance().getPersistentPreference(ViewIssueOnMapAct.this).
                            getBoolean(Constants_dp.PREF_FILTER_WE, true)) {
                        WEVal = getString(R.string.WEVal);
                    } else WEVal = "";
                    if (CGlobals_lib_ss.getInstance().getPersistentPreference(ViewIssueOnMapAct.this).
                            getBoolean(Constants_dp.PREF_FILTER_OG, true)) {
                        OGVal = getString(R.string.OGVal);
                    } else OGVal = "";
                    if (CGlobals_lib_ss.getInstance().getPersistentPreference(ViewIssueOnMapAct.this).
                            getBoolean(Constants_dp.PREF_FILTER_RS, true)) {
                        RSVal = getString(R.string.RSVal);
                    } else RSVal = "";

                    if (TextUtils.isEmpty(mFromDate) && TextUtils.isEmpty(mToDate) &&
                            TextUtils.isEmpty(mPostalCode) && closedVal.equals("0") &&
                            resolvedVal.equals("0") && (mShowAllVal == 1 || (mShowAllVal == 0 && mMyIssueVal == 0 &&
                            TextUtils.isEmpty(PVVal) && TextUtils.isEmpty(MVVal) && TextUtils.isEmpty(RTVal) &&
                            TextUtils.isEmpty(AUTVal) && TextUtils.isEmpty(RDVal) && TextUtils.isEmpty(CLVal) &&
                            TextUtils.isEmpty(CLVal) && TextUtils.isEmpty(ENVal) && TextUtils.isEmpty(SFVal) &&
                            TextUtils.isEmpty(WEVal) && TextUtils.isEmpty(OGVal) && TextUtils.isEmpty(RSVal)
                    ))) {
                        txtfilterby.setText(R.string.filterbynone);
                    } else {
                        if (!TextUtils.isEmpty(mPostalCode)) {
                            filterby = filterby + getString(R.string.postal) + " " + mPostalCode + " ,";
                        }
                        if (!TextUtils.isEmpty(mFromDate)) {
                            filterby = filterby + getString(R.string.date) + " from " + mFromDate;
                            if (!TextUtils.isEmpty(mToDate)) {
                                filterby = filterby + " to " + mToDate + " ,";
                            } else {
                                filterby = filterby + " to till the date ,";
                            }
                        }
                        if (!TextUtils.isEmpty(PVVal))
                            filterby = filterby + getString(R.string.ParkingViolation) + ",";
                        if (!TextUtils.isEmpty(MVVal))
                            filterby = filterby + getString(R.string.MovingViolation) + ",";
                        if (!TextUtils.isEmpty(RTVal))
                            filterby = filterby + getString(R.string.RegularTrafficProblem) + ",";
                        if (!TextUtils.isEmpty(AUTVal))
                            filterby = filterby + getString(R.string.AutoRickshawTaxi) + ",";
                        if (!TextUtils.isEmpty(RDVal))
                            filterby = filterby + getString(R.string.Roads) + ",";
                        if (!TextUtils.isEmpty(CLVal))
                            filterby = filterby + getString(R.string.Cleanliness) + ",";
                        if (!TextUtils.isEmpty(ENVal))
                            filterby = filterby + getString(R.string.HawkersEncroachment) + ",";
                        if (!TextUtils.isEmpty(SFVal))
                            filterby = filterby + getString(R.string.SafetyIssues) + ",";
                        if (!TextUtils.isEmpty(WEVal))
                            filterby = filterby + getString(R.string.WaterElectricity) + ",";
                        if (!TextUtils.isEmpty(OGVal))
                            filterby = filterby + getString(R.string.OpenSpacesGreenery) + ",";
                        if (!TextUtils.isEmpty(RSVal))
                            filterby = filterby + getString(R.string.RequestaService) + ",";
                        if (closedVal.equals("1")) {
                            filterby = filterby + getString(R.string.closedissue) + ",";
                        }
                        if (resolvedVal.equals("1")) {
                            filterby = filterby + getString(R.string.resolvedissue);
                        }
                        txtfilterby.setText("Filter by " + filterby);
                    }

                    if (!TextUtils.isEmpty(mToDate)) {
                        ft = new SimpleDateFormat("dd-MM-yyyy");
                        date = ft.parse(mToDate);
                        ft = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        mToDate = ft.format(date) + " 23:59:59";
                    } else {
                        final Calendar c = Calendar.getInstance();
                        int year = c.get(Calendar.YEAR);
                        int month = c.get(Calendar.MONTH);
                        int day = c.get(Calendar.DAY_OF_MONTH);
                        mToDate = String.valueOf(year) + "-" + (month + 1) + "-" + day + " 23:59:59";
                    }
                    if (!TextUtils.isEmpty(mFromDate)) {
                        ft = new SimpleDateFormat("dd-MM-yyyy");
                        date = ft.parse(mFromDate);
                        ft = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        mFromDate = ft.format(date) + " 00:00:00";
                    } else {
                        mToDate = "";
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                    SSLog.e(TAG, "onResume ", e.toString());
                }
            }
        });

    }
}