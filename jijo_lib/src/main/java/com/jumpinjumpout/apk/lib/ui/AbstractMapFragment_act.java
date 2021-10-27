package com.jumpinjumpout.apk.lib.ui;


import android.Manifest;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.maps.android.PolyUtil;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.Constants_lib;
import com.jumpinjumpout.apk.lib.PinInfo;
import com.jumpinjumpout.apk.lib.R;
import com.jumpinjumpout.apk.lib.SSLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lib.app.util.CAddress;
import lib.app.util.Constants_lib_ss;
import lib.app.util.FetchAddressIntentService;
import lib.app.util.MyLocation;

public abstract class AbstractMapFragment_act extends AppCompatActivity
        implements GoogleMap.OnMyLocationChangeListener, SensorEventListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final float DEFAULT_ZOOM = 15;
    protected boolean mInDirectionMode = false;
    protected ProgressDialog mProgressDialog;
    protected HashMap<Marker, PinInfo> mPinHashMap = new HashMap<Marker, PinInfo>();

    protected boolean allowSelectionByPin = false;
    protected static final String TAG = "Abstract Map: ";
    protected final int ACTRESULT_FROM = 1;
    protected final int ACTRESULT_TO = 2;
    protected static final String FROM = "FROM", TO = "TO";
    protected GoogleMap mMap;
    protected Marker markerYou = null, markerFrom = null, markerTo = null;
    protected ImageView mivZoomMyLocation;
    public TextView mTvFrom, mTvTo;
    protected ImageView mIvNoConnection, mIvNoLocation;
    protected Button mButtonGo, mButtonStart, mButtonPause, mButtonResume, mButtonEndTrip,
            mButtonStartNow, mButtonCancelTrip;
    protected RelativeLayout mRlTripActive, mRlScheduledTrip;
    protected LinearLayout mRlFromTo;
    private boolean mAutoRefreshFromLocation;
    protected CAddress moFromAddress = new CAddress(),
            moToAddress = new CAddress();
    LocationRequest mLocationRequest;
    // Milliseconds per second
    protected static final int MILLISECONDS_PER_SECOND = 1000;
    // The fastest update frequency, in seconds
    protected static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    protected static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND
            * FASTEST_INTERVAL_IN_SECONDS;
    // Define an object that holds accuracy and frequency parameters
    protected String mDistance = "", mDuration = "";
    protected int iDuration;
    protected float mDeclination;
    protected float[] mRotationMatrix;
    private static boolean mIsMapFirstTime = false;
    protected static boolean mbFollowMe = false;
    public static boolean mMapIsTouched;
    protected LatLngBounds.Builder mBounds, mFriendsBounds, mZoomFitBounds;
    protected boolean doubleBackToExitPressedOnce;
    protected boolean mIsFromSelected = true;
    public Location mCurrentLocation = null;
    protected boolean mIsGettingDirections;
    protected String msTripPath = "";
    protected boolean isTripFrozen = false; // Do not change any input fields of
    // trip
    protected boolean mHasUserTouchedFrom = false;
    protected String msActiveMessage, msNoActiveMessage;
    protected List<LatLng> mLatLngTripPath; // Trip path polyline points

    private AddressResultReceiver mResultReceiver;
    protected String mAddressOutput;
    public GoogleApiClient mGoogleApiClient;
    private static final long INTERVAL = 1000 * 10;
    public RelativeLayout mLlMarkerLayout;

    public LinearLayout mLlFrom, mLlTo;
    protected ImageView mIvGreenPin, mIvRedPin;
    protected PolylineOptions mDirectionsPolyline;


    protected abstract void callDriver(JSONArray jsonArray);

    protected abstract void goFacebook();

    protected abstract void goTwitter();


    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

    } // onCreate

    protected void create() {
        SetupUI();
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        mResultReceiver = new AddressResultReceiver(new Handler());
        /*RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) mivZoomFit.getLayoutParams();
        p.setMargins(0, 0, 0, 0); // get rid of margins since shadow area is now the margin
        mivZoomFit.setLayoutParams(p);
        RelativeLayout.LayoutParams p1 = (RelativeLayout.LayoutParams) mivInviteUsers.getLayoutParams();
        p1.setMargins(0, 0, 0, 0); // get rid of margins since shadow area is now the margin
        mivInviteUsers.setLayoutParams(p1);*/
    }

    protected void SetupUI() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Getting route");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mLlFrom = (LinearLayout) findViewById(R.id.llFrom);
        mLlTo = (LinearLayout) findViewById(R.id.llTo);
        mivZoomMyLocation = (ImageView) findViewById(R.id.ivZoomMyLocation);
        mButtonGo = (Button) findViewById(R.id.btnGo);
        mButtonStart = (Button) findViewById(R.id.btnStart);
        mButtonPause = (Button) findViewById(R.id.btnPause);
        mButtonResume = (Button) findViewById(R.id.btnResume);
        mButtonStartNow = (Button) findViewById(R.id.btnStartNow);
        mButtonCancelTrip = (Button) findViewById(R.id.btnCancelTrip);
        mButtonEndTrip = (Button) findViewById(R.id.btnEndTrip);
        mTvFrom = (TextView) findViewById(R.id.tvFrom);
        mTvTo = (TextView) findViewById(R.id.tvTo);
        mLlMarkerLayout = (RelativeLayout) findViewById(R.id.lLlocationMarker);
        mRlFromTo = (LinearLayout) findViewById(R.id.rlFromTo);
        mRlTripActive = (RelativeLayout) findViewById(R.id.rlTripActive);
        mRlScheduledTrip = (RelativeLayout) findViewById(R.id.rlScheduledTrip);
        mIvNoConnection = (ImageView) findViewById(R.id.ivNoNetConnection);
        mIvNoLocation = (ImageView) findViewById(R.id.ivNoLocation);
        mIvGreenPin = (ImageView) findViewById(R.id.imageView1);
        mIvRedPin = (ImageView) findViewById(R.id.imageView2);

    } // SetuUI

    protected void init() {
        mBounds = new LatLngBounds.Builder();
        mFriendsBounds = new LatLngBounds.Builder();
        msActiveMessage = getString(R.string.activeUsers);
        msNoActiveMessage = getString(R.string.noActiveUsers);
        setUpMapIfNeeded();
        if (mMap != null) {
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    mapLoaded();
                }
            });
        }
    } // init

    protected void setLocation(final Location location) {
        if (!isAutoRefreshFromLocation()) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (location != null) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    moFromAddress.setLatitude(lat);
                    moFromAddress.setLongitude(lng);
                    if (mTvFrom != null && TextUtils.isEmpty(mTvFrom.getText())
                            && !isTripFrozen) {
                        moFromAddress = new CAddress();
                        moFromAddress.setLatitude(lat);
                        moFromAddress.setLongitude(lng);
                        mTvFrom.setHint(R.string.myLocation);
                        setAutoRefreshFromLocation(false);
                    }
                }
            }
        });
        startIntentServiceCurrentAddress(location);
    }

    public void setFrom(CAddress oFromAddress, boolean showMarker) {
        moFromAddress = oFromAddress;
        if (!showMarker) {
            mTvFrom.setText(oFromAddress.getAddress());
        } else {
            setFrom(oFromAddress.getAddress(), new LatLng(oFromAddress.getLatitude(), oFromAddress.getLongitude()));
        }
    }

    public void setFrom(String sFrom, LatLng latlng) {
        if (!isTripFrozen) {
            mTvFrom.setText(sFrom);
            if (latlng != null) {
                CAddress addr = new CAddress();
                addr.setAddress(sFrom);
                addr.setLatitude(latlng.latitude);
                addr.setLongitude(latlng.longitude);
                moFromAddress = addr;
                drawFromMarker(moFromAddress);
            } else {
                Toast.makeText(
                        AbstractMapFragment_act.this.getBaseContext(),
                        "Cannot get your current location\n. Please turn on network and gps locaion",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setTo(CAddress oToAddress, boolean showMarker) {
        moToAddress = oToAddress;
        if (!showMarker) {
            mTvTo.setText(oToAddress.getAddress());
        } else {
            setTo(oToAddress.getAddress(), new LatLng(oToAddress.getLatitude(), oToAddress.getLongitude()));
        }
    }

    public void setTo(String sto, LatLng latlng) {
        if (!isTripFrozen) {
            mTvTo.setText(sto);
            if (latlng != null) {
                drawToMarker(moToAddress);
            } else {
                Toast.makeText(
                        AbstractMapFragment_act.this.getBaseContext(),
                        "Cannot get your current location\n. Please turn on network and gps locaion",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onClickZoomMyLocation(View v) {
        onClickZoomMyLocation();
    }

    public void onClickTwitter(View v) {
        goTwitter();
    }


    public void onClickFacebook(View v) {
        goFacebook();
    }

    public void onClickZoomMyLocation() {
        Location location = CGlobals_lib.getInstance().getMyLocation(AbstractMapFragment_act.this);
        mCurrentLocation = CGlobals_lib.getInstance().isBetterLocation(location, mCurrentLocation) ? location
                : mCurrentLocation;
        if (mCurrentLocation != null) {
            mMap.animateCamera(CameraUpdateFactory
                    .newLatLng(new LatLng(mCurrentLocation
                            .getLatitude(), mCurrentLocation
                            .getLongitude())));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation
                    .getLatitude(), mCurrentLocation
                    .getLongitude()), 17));
            mIsFromSelected = false;
        }
    }

    @Override
    protected void onResume() {
        mButtonStart.setVisibility(View.GONE);
        mivZoomMyLocation.setVisibility(View.VISIBLE);
        try {
        } catch (Exception e) {
            SSLog.e(TAG, " onResume ", e);
        }
        if (showPath()) {
            drawFromMarker(moFromAddress);
            drawToMarker(moToAddress);
            plotPathFromPolyLine(msTripPath);
        }
        checkEverythingOk();
        super.onResume();
    } // onResume

    protected void onPause() {
        CGlobals_lib.getInstance()
                .writeRecentAddresses(getApplicationContext());
        clearMap();
        super.onPause();
    } // onPause

    protected void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (mMap == null) {
            mIsMapFirstTime = true;
            mMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();
            if (mMap != null) {
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mMap.setOnMyLocationChangeListener(this);
                if (allowSelectionByPin) {
                }
                Location location = CGlobals_lib.getInstance().getMyLocation(
                        this);
                if (location != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(), location
                                    .getLongitude()), DEFAULT_ZOOM));
                }
                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        mapLoaded();
                    }
                });
            }
        }
    } // setupMapIfNeeded

    public void drawFromMarker(CAddress addr) {

        try {
            if (!addr.hasLatitude() || !addr.hasLongitude()) {
                return;
            }
        } catch (Exception e) {

            e.printStackTrace();
            return;
        }
        if (markerFrom != null) {
            markerFrom.remove();
        }
        markerFrom = null;

        if (markerFrom == null) {
            markerFrom = mMap
                    .addMarker(new MarkerOptions()
                            .position(
                                    new LatLng(addr.getLatitude(), addr
                                            .getLongitude()))
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .alpha(0.8f).draggable(true)
                            .title(addr.getAddress()));
        } else {
            markerFrom.setPosition(new LatLng(moFromAddress.getLatitude(),
                    moFromAddress.getLongitude()));
        }
    } // drawFromMarker

    public void drawFromMarker(LatLng latLng) {

    }

    public void drawToMarker(CAddress addr) {
        if (!addr.hasLatitude() || !addr.hasLongitude()) {
            // Toast.makeText(AbstractMapFragment_act.this.getBaseContext(),
            // "No Destination point", Toast.LENGTH_SHORT).show();
            return;
        }
        if (markerTo != null) {
            markerTo.remove();
        }
        markerTo = null;
        if (markerTo == null) {
            markerTo = mMap.addMarker(new MarkerOptions()
                    .position(
                            new LatLng(moToAddress.getLatitude(), moToAddress
                                    .getLongitude()))
                    .icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .alpha(0.8f).title(addr.getAddress()));
        }
        markerTo.setPosition(new LatLng(moToAddress.getLatitude(), moToAddress
                .getLongitude()));

    } // drawToMarker

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onMyLocationChange(Location location) {
        if (location == null) {
            return;
        } else {
            setLocation(location);
        }
        mCurrentLocation = CGlobals_lib.getInstance().isBetterLocation(location, mCurrentLocation) ? location
                : mCurrentLocation;
        LatLng latLng = new LatLng(location.getLatitude(),
                location.getLongitude());
        CGlobals_lib.getInstance().setMyLocation(location, true);
        if (mIsMapFirstTime && isAutoRefreshFromLocation()) {
            mIsMapFirstTime = false;
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
        }
        gotGoogleMapLocation(location);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(mRotationMatrix,
                    event.values);
            float[] orientation = new float[3];
            SensorManager.getOrientation(mRotationMatrix, orientation);
            double bearing = Math.toDegrees(orientation[0]) + mDeclination;
            updateCamera(bearing);
        }
    }

    private void updateCamera(double bearing) {
        CameraPosition oldPos = mMap.getCameraPosition();
        CameraPosition.builder(oldPos).bearing((float) bearing).build();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // private void writeRecentAddresses() {
    // new Thread(new Runnable() {
    // public void run() {
    // if (CGlobals_lib.getInstance().masRecentAddresses == null) {
    // return;
    // }
    // StringBuilder sb = new StringBuilder();
    // String delim = "";
    // int iLen = CGlobals_lib.getInstance().masRecentAddresses.size();
    // if (iLen < 1)
    // return;
    // String s;
    // for (int i = 0; i < iLen; i++) {
    // s = CGlobals_lib.getInstance().masRecentAddresses.get(i);
    // if (!TextUtils.isEmpty(s)) {
    // sb.append(delim + s);
    // delim = ";";
    // }
    // }
    // CGlobals_lib.getInstance()
    // .getSharedPreferencesEditor(getApplicationContext())
    // .putString(Constants_lib.SELECTADDRESS, sb.toString());
    // CGlobals_lib.getInstance()
    // .getSharedPreferencesEditor(getApplicationContext())
    // .commit();
    // }
    // }).start();
    // } // writeRecentAddresses

    // protected void addRecentAddress(String addressline) {
    // if (CGlobals_lib.getInstance().masRecentAddresses == null)
    // CGlobals_lib.getInstance().masRecentAddresses = new ArrayList<String>();
    // CGlobals_lib.getInstance().masRecentAddresses.add(0, addressline);
    // int iLen = CGlobals_lib.getInstance().masRecentAddresses.size();
    // if (iLen > Constants_lib.MAXADDRESS)
    // CGlobals_lib.getInstance().masRecentAddresses.remove(iLen - 1);
    // // remove the same Station lower in the list
    // iLen = CGlobals_lib.getInstance().masRecentAddresses.size();
    // String sf;
    // for (int i = 1; i < iLen; i++) {
    // sf = CGlobals_lib.getInstance().masRecentAddresses.get(i);
    // if (sf.equalsIgnoreCase(addressline)) {
    // CGlobals_lib.getInstance().masRecentAddresses.remove(i);
    // break;
    // }
    // }
    // } // addRecentStop

    public void onDestroy() {
        super.onDestroy();

    }

    boolean setAddressLatLng(String fromTo, LatLng latlng) {
        boolean isFrom = fromTo.equals(FROM) ? true : false;
        if (isFrom) {
            if (latlng != null) {
                moFromAddress.setLatitude(latlng.latitude);
                moFromAddress.setLongitude(latlng.longitude);

            }
            if (!TextUtils.isEmpty(moFromAddress.getSubLocality1())) {
                // getFromAddr().setSubLocality1(getFromAddr().getSubLocality1());
            } else {
                moFromAddress.setSubLocality1("");
            }

            if (markerFrom == null) {
                markerFrom = mMap
                        .addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory
                                        .defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                .alpha(0.8f).title("Start"));
            }
            markerFrom.setPosition(new LatLng(moFromAddress.getLatitude(),
                    moFromAddress.getLongitude()));

        } else {
            if (latlng != null) {

                moToAddress.setLatitude(latlng.latitude);
                moToAddress.setLongitude(latlng.longitude);
            }
            if (!TextUtils.isEmpty(moToAddress.getSubLocality1())) {
                moToAddress.setSubLocality1(moToAddress.getSubLocality1());
            } else {
                moToAddress.setSubLocality1("");
            }
            if (markerTo == null) {
                markerTo = mMap
                        .addMarker(new MarkerOptions()

                                .icon(BitmapDescriptorFactory
                                        .defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                .alpha(0.8f).title("Start"));
            }
            markerTo.setPosition(new LatLng(moToAddress.getLatitude(),
                    moToAddress.getLongitude()));

            mBounds.include(latlng);

            // LatLngBounds adjustedBounds =
            // createBoundsWithMinDiagonal(mBounds.build());

            // mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mBounds.build(),
            // Constants_lib.MAP_PADDING));
        }
        return true;
    } // setAddressLatLng

    boolean isServiceRunning(Context context, String className) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(Integer.MAX_VALUE);

        if (!(serviceList.size() > 0)) {
            return false;
        }

        for (int i = 0; i < serviceList.size(); i++) {
            ActivityManager.RunningServiceInfo serviceInfo = serviceList
                    .get(i);
            ComponentName serviceName = serviceInfo.service;
            if (serviceName.getClassName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    void showFromTo() {
        mRlFromTo.setVisibility(View.VISIBLE);
    }

    public void resetTrip() {
        markerYou = null;
        markerFrom = null;
        markerTo = null;
        mTvTo.setText("");
    }

    void setConnectionStatus() {
        if (CGlobals_lib.getInstance().checkConnected(
                AbstractMapFragment_act.this)) {
            mIvNoConnection.setVisibility(View.GONE);
        } else {
            mIvNoConnection.setVisibility(View.VISIBLE);
        }

        /*if (CGlobals_lib.getInstance().getMyLocation(getApplicationContext()) == null) {
            mIvNoLocation.setVisibility(View.VISIBLE);
        } else {
            mIvNoLocation.setVisibility(View.GONE);
        }*/
    }

    protected void checkEverythingOk() {
        setConnectionStatus();
    }

    protected void hideViews() {
        mButtonGo.setVisibility(View.GONE);
        mButtonPause.setVisibility(View.GONE);
        mButtonResume.setVisibility(View.GONE);
        mButtonEndTrip.setVisibility(View.GONE);
        mRlFromTo.setVisibility(View.GONE);
        mRlTripActive.setVisibility(View.GONE);
        mRlScheduledTrip.setVisibility(View.GONE);
    }

    public LatLngBounds createBoundsWithMinDiagonal(LatLngBounds oldBounds) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        LatLng center = oldBounds.getCenter();
        LatLng norhtEast = move(center, 709, 709);
        LatLng southWest = move(center, -709, -709);
        builder.include(southWest);
        builder.include(norhtEast);
        return builder.build();
    }

    private static final double EARTHRADIUS = 6366198;

    private static LatLng move(LatLng startLL, double toNorth, double toEast) {
        double lonDiff = meterToLongitude(toEast, startLL.latitude);
        double latDiff = meterToLatitude(toNorth);
        return new LatLng(startLL.latitude + latDiff, startLL.longitude
                + lonDiff);
    }

    private static double meterToLongitude(double meterToEast, double latitude) {
        double latArc = Math.toRadians(latitude);
        double radius = Math.cos(latArc) * EARTHRADIUS;
        double rad = meterToEast / radius;
        return Math.toDegrees(rad);
    }

    private static double meterToLatitude(double meterToNorth) {
        double rad = meterToNorth / EARTHRADIUS;
        return Math.toDegrees(rad);
    }

    public Marker placeMarker(PinInfo pinInfo, boolean showInfoWindow, float markerColor) {

        Marker m = mMap.addMarker(new MarkerOptions()
                .position(pinInfo.getLatLng()).title(pinInfo.getTitle())
                .snippet(pinInfo.getSnippet()));
        if (markerColor == -1) {
            m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
        } else {
            m.setIcon(BitmapDescriptorFactory.defaultMarker(markerColor));
        }
        mPinHashMap.put(m, pinInfo);
        if (showInfoWindow) {
            m.showInfoWindow();
        }
        return m;

    } // placeMarker

    public void removeMarkers() {
        if (mPinHashMap == null)
            return;
        Log.d(TAG, "Size of HashMap : " + mPinHashMap.size());
        Iterator<Map.Entry<Marker, PinInfo>> it = mPinHashMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Marker, PinInfo> pairs = (Map.Entry<Marker, PinInfo>) it
                    .next();
            Marker m = (Marker) pairs.getKey();
            m.remove();
            System.out.println(pairs.getKey() + " = " + pairs.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
        mPinHashMap.clear();
        //  drawFromMarker(moFromAddress);
        // drawToMarker(moToAddress);
    }

    public static String getContactName(Context context, String phoneNo) {
        //ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNo));
        Cursor cursor = context.getContentResolver().query(uri,
                new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor
                    .getColumnIndex(PhoneLookup.DISPLAY_NAME));
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(this.getClass().getSimpleName(), "onConnectionFailed()");

    }

    @Override
    public void onConnected(Bundle arg0) {
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            return;
        }
        CGlobals_lib.getInstance().setMyLocation(location, true);
        mCurrentLocation = CGlobals_lib.getInstance().isBetterLocation(location, mCurrentLocation) ? location
                : mCurrentLocation;
        startIntentServiceCurrentAddress(mCurrentLocation);
        // CGlobals_lib.getInstance().sendUpdatePosition(getMyLocation(),
        // AbstractMapFragment_act.this);

    }

    public void clearMap() {
        // CGlobals_lib.getInstance()
        // .getSharedPreferencesEditor(getApplicationContext())
        // .putString(Constants_lib.TRIP_PATH, "");
        // CGlobals_lib.getInstance()
        // .getSharedPreferencesEditor(getApplicationContext())
        // .commit();
        if (CGlobals_lib.getInstance().mDirectionsPolyline != null) {
            CGlobals_lib.getInstance().mDirectionsPolyline = new PolylineOptions();
        }
        mPinHashMap.clear();
        mMap.clear();
        removeMarkers();
    }

    public float remainingDistance() {
        try {
            Location currentLocation = CGlobals_lib.getInstance().getMyLocation(getApplicationContext());
            Double lat = currentLocation.getLatitude();
            Double lng = currentLocation.getLongitude();
            List<LatLng> path = CGlobals_lib.getInstance().mDirectionsPolyline
                    .getPoints();
            float fCurrentDist = -1;
            float dist[] = new float[1];
            int len = path.size();
            LatLng latLng, prvLatLng;

            int nearestPointIdx = -1;
            for (int i = 0; i < len; i++) {
                latLng = path.get(i);
                Location.distanceBetween(lat, lng, latLng.latitude,
                        latLng.longitude, dist);
                if (fCurrentDist == -1 || dist[0] < fCurrentDist) {
                    fCurrentDist = dist[0];
                    nearestPointIdx = i;

                }
            }
            float fRemainingDistance = 0;
            for (int i = nearestPointIdx; i < len; i++) {
                latLng = path.get(i);
                if (i > 0) {
                    prvLatLng = path.get(i - 1);
                    Location.distanceBetween(prvLatLng.latitude,
                            prvLatLng.longitude, latLng.latitude,
                            latLng.longitude, dist);
                    fRemainingDistance += dist[0];
                }

            }

            return fRemainingDistance;
        } catch (Exception e) {
            return -1;
        }
    }


    MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            try {
                if (CGlobals_lib.getInstance() != null) {
                    CGlobals_lib.getInstance().setMyLocation(location);
                }
            } catch (Exception e) {
                SSLog.e(TAG, " locationResult ", e);
            }
        }
    };

    protected void plotPathFromPolyLine(String sPolyLine) {
        try {
            mLatLngTripPath = PolyUtil.decode(sPolyLine);
            CGlobals_lib.getInstance().mDirectionsPolyline = new PolylineOptions()
                    .addAll(mLatLngTripPath);
            CGlobals_lib.getInstance().mDirectionsPolyline.width(4);
            CGlobals_lib.getInstance().mDirectionsPolyline.color(Color.BLACK);
            mZoomFitBounds = new LatLngBounds.Builder();
            for (LatLng latLng : mLatLngTripPath) {
                mZoomFitBounds
                        .include(new LatLng(latLng.latitude, latLng.longitude));
            }

            mMap.addPolyline(CGlobals_lib.getInstance().mDirectionsPolyline);
        } catch (Exception e) {
            SSLog.e(TAG, "plotPathFromPolyLine:- ", e);
        }

    }

    protected void progressMessage(String msg) {
        if (!isFinishing()) {
            mProgressDialog.setMessage(msg);
            mProgressDialog.show();
        }
    }

    protected void progressCancel() {
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
            }
        }

    }

    public void slideToBottomHide(View view) {
        TranslateAnimation animate = new TranslateAnimation(0, 0, 0,
                view.getHeight());
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }

    public void slideToBottomShow(View view) {
        TranslateAnimation animate = new TranslateAnimation(0, 0, 0,
                view.getHeight());
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.VISIBLE);
    }

    public void setStatus(int iResourceId, int iStatusNo) {
        switch (iStatusNo) {
            case 1:
                // mTvStatus.setText(iResourceId);
                // mTvStatus.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void setStatus(String sStatus, int iStatusNo) {
        switch (iStatusNo) {
            case 1:
                /*mTvStatus.setText(sStatus);
                mTvStatus.setVisibility(View.VISIBLE);*/
                break;
        }
    }

    public void hideStatus(int iStatusNo) {

    }

    public abstract void onClickZoomActiveUsers(View v);

    public abstract void onClickZoomTrip(View v);


    public abstract void onClickZoomVehicle(View v);

    public abstract void onClickInviteUsers(View v);

    public abstract void onClickInformation(View v);

    public abstract void endTrip();

    // protected abstract void StartLocationService();
    // protected abstract void StopLocationService();
    protected abstract String getGeoCountryCode();

    protected abstract void gotGoogleMapLocation(Location location);

    public boolean isAutoRefreshFromLocation() {
        return mAutoRefreshFromLocation;
    }

    public void setAutoRefreshFromLocation(boolean mAutoRefreshFromLocation) {
        this.mAutoRefreshFromLocation = mAutoRefreshFromLocation;
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string or an error message sent from the
            // intent service.
            mAddressOutput = resultData
                    .getString(Constants_lib_ss.RESULT_DATA_KEY);

            // Show a toast message if an address was found.
            if (resultCode == Constants_lib.SUCCESS_RESULT) {
                if (!TextUtils.isEmpty(mAddressOutput)) {
                    //if (mTvFrom.getText().toString().equals(null)) {
                    moFromAddress = new CAddress(mAddressOutput, "", moFromAddress.getLatitude(), moFromAddress.getLongitude());
                    setFrom(moFromAddress, false);
                    moFromAddress.setAddress(mAddressOutput);
                    // }
                }


            }

        }
    }

    protected void startIntentServiceCurrentAddress(Location location) {

        try {
            if (isTripFrozen) {
                return;
            }
            Intent intent = new Intent(AbstractMapFragment_act.this,
                    FetchAddressIntentService.class);
            intent.putExtra(Constants_lib_ss.RECEIVER, mResultReceiver);
            intent.putExtra(Constants_lib_ss.LOCATION_DATA_EXTRA, location);
            startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // startIntentServiceCurrentAddress

    protected abstract boolean showPath();

    protected abstract void mapLoaded();

    @Override
    public void onConnectionSuspended(int i) {

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            SSLog.e(TAG, "Exception while downloading url - ", e);
            Toast.makeText(AbstractMapFragment_act.this, getString(R.string.failedToConnect), Toast.LENGTH_LONG).show();
        } finally {
            if (iStream != null) {
                iStream.close();
            }
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    public class GoogleDirection extends AsyncTask<String, Void, String> {
        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                // Fetching the data from web service
                data = AbstractMapFragment_act.this.downloadUrl(url[0]);
            } catch (Exception e) {
                mIsGettingDirections = false;
                SSLog.e(TAG, "Background Task ", e);
            }
            return data;
        }

        @Override
        protected void onPreExecute() {
            if (!isFinishing())
                mProgressDialog.show();
            mProgressDialog.setMessage(getString(R.string.gettingDirections));
            Log.d(TAG, "DownloadTask: PreExecute");
            super.onPreExecute();
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            JSONArray aoPoint = new JSONArray();
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray routesArray = jsonObject.getJSONArray("routes");
                for (int i = 0; i < routesArray.length(); i++) {
                    aoPoint = new JSONArray();
                    JSONObject route = routesArray.getJSONObject(i);
                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);

                    JSONObject durationObject = leg.getJSONObject("duration");
                    mDuration = durationObject.getString("text");
                    iDuration = durationObject.getInt("value");
                    iDuration = (int) Math.round((double) iDuration / 60.0);
                    JSONObject distanceObject = leg.getJSONObject("distance");
                    mDistance = distanceObject.getString("value");
                    JSONObject polyline = route.getJSONObject("overview_polyline");
                    msTripPath = polyline.getString("points");
                    String json = new Gson().toJson(leg);
                    CGlobals_lib.getInstance().getSharedPreferencesEditor(getApplicationContext())
                            .putString(Constants_lib.TRIP_JSON_OBJECT,
                                    json);
                    CGlobals_lib.getInstance().getSharedPreferencesEditor(getApplicationContext())
                            .putString(Constants_lib.TRIP_FROM_ADDRESS,
                                    leg.getString("start_address"));
                    CGlobals_lib.getInstance().getSharedPreferencesEditor(getApplicationContext())
                            .putString(Constants_lib.TRIP_TO_ADDRESS,
                                    leg.getString("end_address"));
                    CGlobals_lib.getInstance().getSharedPreferencesEditor(getApplicationContext())
                            .commit();
                    mLatLngTripPath = PolyUtil.decode(polyline.getString("points"));
                    mDirectionsPolyline = new PolylineOptions()
                            .addAll(mLatLngTripPath);
                    CGlobals_lib.getInstance().getSharedPreferencesEditor(getApplicationContext())
                            .putString(Constants_lib.TRIP_PATH, msTripPath);

                    CGlobals_lib.getInstance().getSharedPreferencesEditor(getApplicationContext())
                            .commit();

                    System.out.println("msTripPath Create    " + msTripPath);

                    String joinpath = CGlobals_lib.getInstance().getSharedPreferences(getApplicationContext())
                            .getString(Constants_lib.TRIP_PATH, "");

                    System.out.println("msTripPath join   " + joinpath);

                    mDirectionsPolyline.width(4);
                    mDirectionsPolyline.color(Color.BLACK);

                    mMap.addPolyline(mDirectionsPolyline);
                    mZoomFitBounds = new LatLngBounds.Builder();

                    for (LatLng latLng : mLatLngTripPath) {
                        try {
                            JSONObject oPoint = new JSONObject();
                            oPoint.put("lat", latLng.latitude);
                            oPoint.put("lng", latLng.longitude);
                            aoPoint.put(oPoint);
                            mZoomFitBounds.include(new LatLng(latLng.latitude,
                                    latLng.longitude));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                }
                callDriver(aoPoint);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(AbstractMapFragment_act.this.getBaseContext(),
                        "Failed to get directions, please try again",
                        Toast.LENGTH_SHORT).show();
                progressCancel();
            }
            mIsGettingDirections = false;
            // Invokes the thread for parsing the JSON data
            Log.d(TAG, "DownloadTask: DONE");

        }
    } // DownloadTask

} // AbstractMapFragmentActivity

