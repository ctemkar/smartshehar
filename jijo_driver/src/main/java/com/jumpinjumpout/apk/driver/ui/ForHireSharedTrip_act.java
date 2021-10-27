package com.jumpinjumpout.apk.driver.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.PhoneLookup;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jumpinjumpout.apk.driver.CGlobals_driver;
import com.jumpinjumpout.apk.driver.CSpeed;
import com.jumpinjumpout.apk.driver.Constants_driver;
import com.jumpinjumpout.apk.driver.LocationService_cab;
import com.jumpinjumpout.apk.driver.MyApplication;
import com.jumpinjumpout.apk.driver.R;
import com.jumpinjumpout.apk.driver.VehicleResult;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.CTrip;
import com.jumpinjumpout.apk.lib.Constants_lib;
import com.jumpinjumpout.apk.lib.DatabaseHandler;
import com.jumpinjumpout.apk.lib.PinInfo;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.lib.ui.Driver_act;
import com.jumpinjumpout.apk.lib.ui.SearchAddress_act;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lib.app.util.CAddress;
import lib.app.util.MyLocation;

public class ForHireSharedTrip_act extends Driver_act {
    protected static final String TAG = "ForHireSharedTrip_act: ";
    protected Handler handler = new Handler();
    private String lastUpdate;
    boolean isImage = false;
    CTrip cTripDriver;
    List<CTrip> mTrips;
    String plannedstartdatetime;
    ArrayList<CSpeed> aDriverSpeed = new ArrayList<CSpeed>();
    String getacabresponse, responsevalue = "";
    private ProgressDialog pDialog;
    public boolean expanded = false;
    private float offset1, offset3, offset4, offset5;
    ViewGroup fabContainer;
    protected FloatingActionButton flZoomActiveUsers;
    protected FloatingActionButton mivZoomFit,
            mivInformation, mivZoomVehicle, famZoom;
    DatabaseHandler db;
    private final int ACT_RESULT_SEAT_FARE = 19;
    String spVehicleid, spShiftid;
    int iHaveJoined = 0, iHaveJumpedIn = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_floating_action_menu_forhireshared);
        create();
        driverCreate();
        fabContainer = (ViewGroup) findViewById(R.id.fab_container);
        mivZoomFit = (FloatingActionButton) findViewById(R.id.ivZoomFit);
        mivInformation = (FloatingActionButton) findViewById(R.id.ivInformation);
        famZoom = (FloatingActionButton) findViewById(R.id.famZoom);
        flZoomActiveUsers = (FloatingActionButton) findViewById(R.id.ivZoomActiveUsers);
        mivZoomVehicle = (FloatingActionButton) findViewById(R.id.ivZoomVehicle);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
        mlActivePassengerMarkers = new ArrayList<Marker>();
        getSharedPreferencesEditor()
                .putString(Constants_driver.PREF_INVITE_USER_TO_APP, "");
        getSharedPreferencesEditor()
                .commit();
        db = new DatabaseHandler(this);
        LocationManager service = (LocationManager) getSystemService(Activity.LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (enabled) {
            recentTripCreate();
        }
        if (mMap == null) {
            setUpMapIfNeeded();
        }

        famZoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expanded = !expanded;
                if (expanded) {
                    expandFab();
                } else {
                    collapseFab();
                }
            }
        });

        fabContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                fabContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                offset1 = famZoom.getY() - mivInformation.getY();
                mivInformation.setTranslationY(offset1);

                offset3 = famZoom.getY() - flZoomActiveUsers.getY();
                flZoomActiveUsers.setTranslationY(offset3);

                offset4 = famZoom.getY() - mivZoomFit.getY();
                mivZoomFit.setTranslationY(offset4);

                offset5 = famZoom.getY() - mivZoomVehicle.getY();
                mivZoomVehicle.setTranslationY(offset5);
                return true;
            }
        });

        if (isInTrip()) {
            mivZoomFit.setVisibility(View.VISIBLE);
            isTripFrozen = true;
        } else {
            mivZoomFit.setVisibility(View.GONE);
        }
        CGlobals_lib.showGPSDialog = true;
    } // onCreate

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CGlobals_lib.REQUEST_LOCATION) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    Log.d(TAG, "YES");
                    recentTripCreate();
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
        if (requestCode == ACT_RESULT_SEAT_FARE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(ForHireSharedTrip_act.this, "Submit done!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void recentTripCreate() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            responsevalue = extras.getString("RESPONSEVALUE");
            if (extras.getString("SCHEDULETRIP") != null) {
                if (extras.getString("SCHEDULETRIP").equals("1")) {
                    Type type = new TypeToken<CAddress>() {
                    }.getType();
                    String startAddress = extras.getString("START");
                    String destAddress = extras.getString("DEST");
                    moFromAddress = new Gson().fromJson(startAddress, type);
                    moToAddress = new Gson().fromJson(destAddress, type);
                    mTvFrom.setText(moFromAddress.getAddress());
                    mTvTo.setText(moToAddress.getAddress());
                    pDialog = new ProgressDialog(ForHireSharedTrip_act.this);
                    pDialog.setMessage("Please wait...");
                    pDialog.setCancelable(false);
                    mIsDirectTrip = true;
                    goDirections();
                }
            } else if (extras.getString(Constants_driver.RECENT_TRIP) != null) {
                if (extras.getString(Constants_driver.RECENT_TRIP).equals("1")) {
                    Type type = new TypeToken<CAddress>() {
                    }.getType();
                    String fromAddress = extras.getString("FROM_RESENT");
                    String toAddress = extras.getString("TO_RESENT");
                    moFromAddress = new Gson().fromJson(fromAddress, type);
                    moToAddress = new Gson().fromJson(toAddress, type);
                    mTvFrom.setText(moFromAddress.getAddress());
                    mTvTo.setText(moToAddress.getAddress());
                    pDialog = new ProgressDialog(ForHireSharedTrip_act.this);
                    pDialog.setMessage("Please wait...");
                    pDialog.setCancelable(false);
                    mIsDirectTrip = true;
                    goDirections();
                }
            } else if (responsevalue != null) {
                if (responsevalue.equals("1")) {
                    getacabresponse = MyApplication.getInstance().getPersistentPreference().
                            getString(Constants_driver.PREF_SAVE_RESPONSE_PASSENGER, "");
                    Type type = new TypeToken<String>() {
                    }.getType();
                    String passengerinfo = new Gson().fromJson(getacabresponse, type);
                    cTripDriver = new CTrip(passengerinfo, ForHireSharedTrip_act.this);
                    //setFrom(cTripDriver.getFrom(), new LatLng(cTripDriver.getFromLat(), cTripDriver.getFromLng()));
                    setTo(cTripDriver.getTo(), new LatLng(cTripDriver.getToLat(), cTripDriver.getToLng()));
                    isFromSelected = false;
                    flag_btnCreateTrip = false;
                    moToAddress.setLatitude(cTripDriver.getToLat());
                    moToAddress.setLongitude(cTripDriver.getToLng());
                    moToAddress.setAddress(cTripDriver.getTo());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                                    cTripDriver.getToLat(), cTripDriver.getToLng()),
                            DEFAULT_ZOOM));
                    /*mIsDirectTrip = true;
                    goDirections();*/
                }
            }
        }
    }

    public void collapseFab() {
        famZoom.setImageResource(com.jumpinjumpout.apk.lib.R.mipmap.ic_openfloat);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(createCollapseAnimator(mivInformation, offset1));
        animatorSet.playTogether(createCollapseAnimator(flZoomActiveUsers, offset3));
        animatorSet.playTogether(createCollapseAnimator(mivZoomFit, offset4));
        animatorSet.playTogether(createCollapseAnimator(mivZoomVehicle, offset5));
        animatorSet.start();
        animateFab();
    }

    public void expandFab() {
        famZoom.setImageResource(com.jumpinjumpout.apk.lib.R.mipmap.ic_close);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(createExpandAnimator(mivInformation, offset1));
        animatorSet.playTogether(createExpandAnimator(flZoomActiveUsers, offset3));
        animatorSet.playTogether(createExpandAnimator(mivZoomFit, offset4));
        animatorSet.playTogether(createExpandAnimator(mivZoomVehicle, offset5));
        animatorSet.start();
        animateFab();
    }

    public static final String TRANSLATION_Y = "translationY";

    public Animator createCollapseAnimator(View view, float offset) {
        return ObjectAnimator.ofFloat(view, TRANSLATION_Y, 0, offset)
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    public Animator createExpandAnimator(View view, float offset) {
        return ObjectAnimator.ofFloat(view, TRANSLATION_Y, offset, 0)
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    public void animateFab() {
        Drawable drawable = famZoom.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }
    }

    protected void setupImageButtons() {
        if (isInTrip()) {
            mivZoomFit.setVisibility(View.VISIBLE);
            mivInformation.setVisibility(View.VISIBLE);
            mivZoomVehicle.setVisibility(View.GONE);
        } else {
            mtvJoinedCountNumber.setVisibility(View.GONE);
            mTvJoinedCount.setVisibility(View.GONE);
            mTvJumpedInCount.setVisibility(View.GONE);
            mivZoomFit.setVisibility(View.GONE);
            mivInformation.setVisibility(View.GONE);
            flZoomActiveUsers.setVisibility(View.GONE);
            mivZoomVehicle.setVisibility(View.GONE);
        }
    }

    protected void setTripActionFloatingbuttonHide() {
        mivZoomFit.setVisibility(View.GONE);
        flZoomActiveUsers.setVisibility(View.GONE);
        mivInformation.setVisibility(View.GONE);
        mivZoomVehicle.setVisibility(View.GONE);
    }

    public void onClickFrom(View v) {
        boolean isforhiredriver = MyApplication.getInstance().getPersistentPreference().
                getBoolean("IS_FOR_HIRE_DRIVER", false);
        if (isforhiredriver) {
            //Toast.makeText(ForHireSharedTrip_act.this, "Cannot change Start Address", Toast.LENGTH_LONG).show();
        } else {
            Intent i = new Intent(ForHireSharedTrip_act.this,
                    SearchAddress_act.class);
            i.putExtra("cc", sCountryCode);
            startActivityForResult(i, ACTRESULT_FROM);
            if (!TextUtils.isEmpty(moFromAddress.getAddress())) {
                CGlobals_lib.getInstance().addRecentAddress(moFromAddress);
            }
            mHasUserTouchedFrom = true;
        }
    }


    MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            CGlobals_driver.getInstance().setMyLocation(location, false);
            if (location != null
                    && (moFromAddress.hasLatitude() && moFromAddress
                    .hasLongitude())
                    && isInTrip()) {
                float results[] = new float[1];
                Location.distanceBetween(moToAddress.getLatitude(),
                        moToAddress.getLongitude(), location.getLatitude(),
                        location.getLongitude(), results);
            }
            CGlobals_driver.getInstance().sendUpdatePosition(mCurrentLocation, ForHireSharedTrip_act.this);
        }
    };

    @Override
    public void onClickInviteUsers(View v) {

    }

    protected void showActivePassengerMarkers(String result) {

        double nearestLat = Constants_lib.INVALIDLAT, nearestLng = Constants_lib.INVALIDLNG;
        try {
            if (!isInTrip()) {
                setTripAction(Constants_driver.TRIP_ACTION_END);
                resetDisplay();
            }
            if (result.trim().equals("0")) {
                String sMsg = "No waiting passengers";
                showStatusMessages(sMsg);
                SSLog.d(TAG, "No propsective passengers");
                resetDisplay();
                return;
            }
            if (responsevalue != null) {
                if (responsevalue.equals("1")) {
                    /*if (iHaveJoined > 0 && iHaveJumpedIn > 0) {
                        String sMsg = "No waiting passengers";
                        showStatusMessages(sMsg);
                        SSLog.d(TAG, "No propsective passengers");
                        resetDisplay();
                    }*/
                    return;
                }
            }
            if (result.trim().equals("-1")) {
                if (iHaveJoined > 0 && iHaveJumpedIn > 0) {
                    String sMsg = "No waiting passengers";
                    showStatusMessages(sMsg);
                    SSLog.d(TAG, "No propsective passengers");
                    resetDisplay();
                }
                return;
            }
            if (result.trim().isEmpty()) {
                if (iHaveJoined > 0 && iHaveJumpedIn > 0) {
                    String sMsg = "No waiting passengers";
                    showStatusMessages(sMsg);
                    SSLog.d(TAG, "No propsective passengers");
                    resetDisplay();
                }
                return;
            }
            // save all passenger data every time update to server
            CGlobals_lib.getInstance().getSharedPreferencesEditor(ForHireSharedTrip_act.this)
                    .putString(Constants_lib.PREF_SAVE_TRIP_CREATE_RESPONSE, result);
            CGlobals_lib.getInstance().getSharedPreferencesEditor(ForHireSharedTrip_act.this).commit();
            iHaveJoined = 0;
            iHaveJumpedIn = 0;
            String sMsg = "";
            showStatusMessages(sMsg);
            mFlPassengerInfo.setVisibility(View.VISIBLE);
            mLlPassengerInfo.setVisibility(View.VISIBLE);
            mLlPassengerInfo.bringToFront();
            JSONArray majActiveUsers = new JSONArray(result);

            mTrips = new ArrayList<CTrip>();

            int iDriverNearestNode = -1;
            JSONObject jPassenger = new JSONObject();
            int nPassengers = majActiveUsers.length();
            boolean isFriend = false;
            String sContactName;
            if (isInTrip()) {
                iDriverNearestNode = getNearestNode(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
            }
            int iNearestPassengerNode = -1, iNode = -1;
            Marker nearestMarker = null;
            mActiveUserBounds = new LatLngBounds.Builder();
            float LeastDistanceFromMe = -1;
            String nearestPerson = "";
            int len = mlActivePassengerMarkers.size();
            for (int i = 0; i < len; i++) {
                mlActivePassengerMarkers.get(i).remove();
            }
            mlActivePassengerMarkers.clear();
            removeMarkers();

            mTrips.clear();
            boolean foundNearestPassenger = false;
            for (int j = 0; j < nPassengers; j++) {
                sContactName = "";
                isFriend = false;
                jPassenger = majActiveUsers.getJSONObject(j);
                cTripDriver = new CTrip(majActiveUsers.getJSONObject(j)
                        .toString(), ForHireSharedTrip_act.this);
                mTrips.add(cTripDriver);

                int nInPath = jPassenger.isNull(Constants_driver.COL_IN_PATH) ? 0
                        : Integer.parseInt(jPassenger
                        .getString(Constants_lib.COL_IN_PATH));
                int isTracking = jPassenger
                        .isNull(Constants_lib.COL_IS_TRACKING) ? 0 : Integer
                        .parseInt(jPassenger
                                .getString(Constants_lib.COL_IS_TRACKING));


                lastUpdate = cTripDriver.getLastActive();

                if (cTripDriver.hasJoined()) {
                    if (!cTripDriver.hasJumpedIn()) {
                        iHaveJoined++;
                    }
                }
                if (cTripDriver.hasJumpedIn() && !cTripDriver.hasJumpedOut()) {
                    iHaveJumpedIn++;
                }
                String sPhoneNo = jPassenger.isNull(Constants_lib.COL_PHONE_NO) ? ""
                        : jPassenger.getString(Constants_lib.COL_PHONE_NO);
                double lat = jPassenger.isNull(Constants_lib.COL_LAT) ? -999
                        : jPassenger.getDouble(Constants_lib.COL_LAT);
                double lng = jPassenger.isNull(Constants_lib.COL_LNG) ? -999
                        : jPassenger.getDouble(Constants_lib.COL_LNG);
                String infoText = "";
                float results[] = new float[1];
                if (lat > -999 && lng > -999) {
                    mActiveUserBounds.include(new LatLng(lat, lng));
                }
                if (lat > -999 && lng > -999 && mCurrentLocation != null) {
                    Location.distanceBetween(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), lat,
                            lng, results);
                    infoText = String.format(Locale.getDefault(),
                            "%.2f km away", results[0] / 1000.0);
                }
                if (!TextUtils.isEmpty(sPhoneNo)) {
                    Uri uri = Uri.withAppendedPath(
                            PhoneLookup.CONTENT_FILTER_URI,
                            Uri.encode(sPhoneNo));

                    Cursor cur = getContentResolver().query(uri,
                            new String[]{PhoneLookup.DISPLAY_NAME}, null,
                            null, null);
                    cur.getColumnIndex(PhoneLookup.DISPLAY_NAME);
                    cur.getColumnIndex(PhoneLookup.TYPE);
                    final int idIndex = cur
                            .getColumnIndex(PhoneLookup.DISPLAY_NAME);

                    if (cur.moveToFirst()) {
                        cur.getString(idIndex);
                        isFriend = true;
                    } else {
                    }
                    cur.close();
                    if (!TextUtils.isEmpty(sPhoneNo)) {
                        sContactName = getContactName(getBaseContext(),
                                sPhoneNo);
                        if (sContactName != null) {
                            sContactName.substring(0, 1);
                            int nSecondPart = sContactName.indexOf(" ");
                            if (nSecondPart > 0
                                    && nSecondPart < sContactName.length() - 1) {
                                sContactName.substring(nSecondPart + 1).trim();
                            }
                        } else {
                            sContactName = "";
                        }

                    }
                }
                if (iHaveJoined > 0 || iHaveJumpedIn > 0) {
                    mLlJoinPassenger.setVisibility(View.VISIBLE);
                    mIvPassenger1.setVisibility(View.VISIBLE);
                    flZoomActiveUsers.setVisibility(View.VISIBLE);
                    if (iHaveJoined > 0) {
                        mTvJoinedCount.setText("Waiting");
                        mTvJoinedCount.setVisibility(View.VISIBLE);
                        mtvJoinedCountNumber.setVisibility(View.VISIBLE);
                        mtvJoinedCountNumber.setText(Integer.toString(iHaveJoined));
                    } else {
                        mTvJoinedCount.setVisibility(View.GONE);
                        mtvJoinedCountNumber.setVisibility(View.GONE);
                    }
                    if (iHaveJumpedIn > 0) {
                        mLlWaitingPassenger.setVisibility(View.VISIBLE);
                        mTvJumpedInCount.setVisibility(View.VISIBLE);

                        mTvJumpedInCount.setText(Html.fromHtml("<b><style='font-size:30px;  text-align:center;'>" + iHaveJumpedIn + "</b>  Jumped in"
                                + (iHaveJumpedIn > 1 ? "s" : "")));
                    } else {
                        mTvJumpedInCount.setVisibility(View.GONE);
                    }
                } else {
                    mTvInfoBandLine1.setVisibility(View.GONE);
                    mLlWaitingPassenger.setVisibility(View.GONE);
                    mTvInfoBandLine2.setVisibility(View.GONE);
                    mTvInfoBandLine3.setVisibility(View.GONE);
                    mTvInfoBandLine4.setVisibility(View.GONE);
                    mIvPassenger1.setVisibility(View.GONE);
                    mTvJoinedCount.setVisibility(View.GONE);
                    mtvJoinedCountNumber.setVisibility(View.GONE);
                    mTvJumpedInCount.setVisibility(View.GONE);
                }
                if (jPassenger.isNull(Constants_lib.COL_LAT)
                        || jPassenger.isNull(Constants_lib.COL_LNG)) {
                    continue;
                }
                if (cTripDriver.hasJoined() && !cTripDriver.hasJumpedIn()) {
                    if (LeastDistanceFromMe < 0
                            || results[0] < LeastDistanceFromMe) {
                        LeastDistanceFromMe = results[0];
                        nearestLat = jPassenger.getDouble(Constants_lib.COL_LAT);
                        nearestLng = jPassenger.getDouble(Constants_lib.COL_LNG);
                    }
                }

                mBounds.include(new LatLng(jPassenger
                        .getDouble(Constants_lib.COL_LAT), jPassenger
                        .getDouble(Constants_lib.COL_LNG)));


                PinInfo pininfo = new PinInfo(cTripDriver.getLatLng(), cTripDriver.getName()
                        + "\n" + infoText, "", cTripDriver.getTripStatusResource(),
                        cTripDriver.getMarkerText(), jPassenger);
                Marker m = null;
                if (cTripDriver.hasJoined() && !cTripDriver.hasJumpedIn()) {
                    if (j == 0) {
                        m = placeMarker(pininfo, false, BitmapDescriptorFactory.HUE_ORANGE);
                    } else {
                        m = placeMarker(pininfo, false, -1);

                    }
                }
                if (isInTrip() && cTripDriver.hasJoined() && !cTripDriver.hasJumpedIn()) {

                    iNode = getNearestNode(cTripDriver.getLatLng());
                    // Will have to check if driver moved
                    if (iNode >= iDriverNearestNode
                            && (iNode <= iNearestPassengerNode || iNearestPassengerNode == -1)) {
                        iNearestPassengerNode = iNode;
                        if (!foundNearestPassenger) {
                            nearestMarker = m;
                            if (iNearestPassengerNode != -1 && nearestMarker != null
                                    && iNearestPassengerNode >= iDriverNearestNode) {
                                mLlPassengerInfo.setVisibility(View.VISIBLE);
                                mLlPassengerInfo.bringToFront();
                                updateSignal(Double.parseDouble(passengerTODriverDistance()));
                                mTvInfoBandLine1.setVisibility(View.VISIBLE);
                                if (cTripDriver.getlTimeDiff() < 1 && cTripDriver.getlTimeDifflocation() < 30) {
                                    mTvInfoBandLine3.setBackgroundResource(R.drawable.btn_online);
                                } else {
                                    mTvInfoBandLine3.setBackgroundResource(R.drawable.btn_offline);
                                }
                                mTvInfoBandLine3.setVisibility(View.VISIBLE);
                                String sDistanceFromMe = "<b>" + CGlobals_lib.getInstance().getDistance(
                                        LeastDistanceFromMe) + "</b>";
                                mTvInfoBandLine1.setText(Html.fromHtml(passengerTODriverDistance()));
                                mTvInfoBandLine4.setText((TextUtils.isEmpty(cTripDriver.getName()) ? ""
                                        : cTripDriver.getName()));
                                mTvInfoBandLine4.setVisibility(View.VISIBLE);
                                setNearestPassengerLatLng(nearestLat, nearestLng);
                                if (!TextUtils.isEmpty(cTripDriver.getUserProfileImageFileName()) && !TextUtils.isEmpty(cTripDriver.getUserProfileImagePath())) {

                                    String url = Constants_driver.GET_USER_PROFILE_IMAGE_FILE_NAME_URL + cTripDriver.getUserProfileImagePath() +
                                            cTripDriver.getUserProfileImageFileName();

                                    CGlobals_lib.getInstance().getSharedPreferencesEditor(ForHireSharedTrip_act.this).
                                            putString(Constants_driver.PREF_CURRENT_PASSENGER_IMAGEURL, url);
                                    CGlobals_lib.getInstance().getSharedPreferencesEditor(ForHireSharedTrip_act.this).commit();

                                    ImageRequest request = new ImageRequest(url,
                                            new Response.Listener<Bitmap>() {
                                                @Override
                                                public void onResponse(Bitmap bitmap) {
                                                    if (Build.VERSION.SDK_INT < 11) {
                                                        Toast.makeText(ForHireSharedTrip_act.this, getString(R.string.api11not), Toast.LENGTH_LONG).show();
                                                    } else {
                                                        mIvPhoto.setImageBitmap(bitmap);
                                                        mIvPhoto.setVisibility(View.VISIBLE);
                                                        isImage = true;
                                                    }
                                                }
                                            }, 0, 0, null,
                                            new Response.ErrorListener() {
                                                public void onErrorResponse(VolleyError error) {
                                                    isImage = false;
                                                    CGlobals_lib.getInstance().getSharedPreferencesEditor(ForHireSharedTrip_act.this).
                                                            putString(Constants_driver.PREF_CURRENT_PASSENGER_IMAGEURL, "");
                                                    CGlobals_lib.getInstance().getSharedPreferencesEditor(ForHireSharedTrip_act.this).commit();
                                                }
                                            });
                                    MyApplication.getInstance().addToRequestQueue(request);
                                } else {
                                    isImage = false;
                                    CGlobals_lib.getInstance().getSharedPreferencesEditor(ForHireSharedTrip_act.this).
                                            putString(Constants_driver.PREF_CURRENT_PASSENGER_IMAGEURL, "");
                                    CGlobals_lib.getInstance().getSharedPreferencesEditor(ForHireSharedTrip_act.this).commit();
                                }
                                if (!isImage) {
                                    Bitmap bitmap = cTripDriver.getContactThnumbnail();
                                    if (bitmap != null) {
                                        mIvPhoto.setImageBitmap(bitmap);
                                        mIvPhoto.setVisibility(View.VISIBLE);
                                    }
                                    CGlobals_lib.getInstance().getSharedPreferencesEditor(ForHireSharedTrip_act.this).
                                            putString(Constants_driver.PREF_CURRENT_PASSENGER_IMAGEURL, "");
                                    CGlobals_lib.getInstance().getSharedPreferencesEditor(ForHireSharedTrip_act.this).commit();
                                }
                                mIvPhoto.setVisibility(View.VISIBLE);
                                mTvKmAway.setVisibility(View.VISIBLE); // 2015-06-20 16:31:44
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date date = new Date();
                                System.out.println(dateFormat.format(date));
                                addSpeed(mCurrentLocation.getSpeed(), dateFormat.format(date));

                                MyApplication.getInstance().getPersistentPreferenceEditor().
                                        putString(Constants_driver.PREF_CURRENT_PASSENGER_PHONENO, cTripDriver.getPhoneNo());
                                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                                foundNearestPassenger = true;
                            } else {
                                if (cTripDriver.hasJumpedIn() && !cTripDriver.hasJumpedOut()) {
                                    mLlPassengerInfo.setVisibility(View.VISIBLE);
                                    mLlPassengerInfo.bringToFront();
                                    mLlJoinPassenger.setVisibility(View.VISIBLE);
                                    if (iHaveJoined > 0) {
                                        mTvJoinedCount.setText("Waiting");
                                        mTvJoinedCount.setVisibility(View.VISIBLE);
                                        mtvJoinedCountNumber.setVisibility(View.VISIBLE);
                                        mtvJoinedCountNumber.setText(Integer.toString(iHaveJoined));
                                    } else {
                                        mTvJoinedCount.setVisibility(View.GONE);
                                        mtvJoinedCountNumber.setVisibility(View.GONE);
                                    }
                                    mIvPassenger1.setVisibility(View.VISIBLE);
                                    mTvJumpedInCount.setVisibility(View.VISIBLE);
                                    mLlWaitingPassenger.setVisibility(View.INVISIBLE);
                                    mIvPhoto.setVisibility(View.GONE);
                                    mTvKmAway.setVisibility(View.GONE);
                                    mTvInfoBandLine1.setVisibility(View.GONE);
                                    mTvInfoBandLine2.setVisibility(View.GONE);
                                    mTvInfoBandLine3.setVisibility(View.GONE);
                                    mTvTime.setVisibility(View.GONE);
                                    mTvMins.setVisibility(View.GONE);
                                    mTvInfoBandLine4.setVisibility(View.GONE);
                                } else {
                                    String sMsg4 = "No waiting passengers";
                                    showStatusMessages(sMsg4);
                                    mTvInfoBandLine1.setVisibility(View.GONE);
                                    mTvInfoBandLine2.setVisibility(View.GONE);
                                    mTvInfoBandLine3.setVisibility(View.GONE);
                                    mTvInfoBandLine4.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                }
            } // for loop

            if (iNearestPassengerNode != -1 && nearestMarker != null
                    && iNearestPassengerNode >= iDriverNearestNode) {

            } else {
                if (cTripDriver.hasJumpedIn() && !cTripDriver.hasJumpedOut()) {
                    mLlPassengerInfo.setVisibility(View.VISIBLE);
                    mLlPassengerInfo.bringToFront();
                    mLlJoinPassenger.setVisibility(View.VISIBLE);
                    if (iHaveJoined > 0) {
                        mTvJoinedCount.setText("Waiting");
                        mTvJoinedCount.setVisibility(View.VISIBLE);
                        mtvJoinedCountNumber.setVisibility(View.VISIBLE);
                        mtvJoinedCountNumber.setText(Integer.toString(iHaveJoined));
                    } else {
                        mTvJoinedCount.setVisibility(View.GONE);
                        mtvJoinedCountNumber.setVisibility(View.GONE);
                    }
                    mIvPassenger1.setVisibility(View.VISIBLE);
                    mTvJumpedInCount.setVisibility(View.VISIBLE);
                    mLlWaitingPassenger.setVisibility(View.INVISIBLE);
                    mIvPhoto.setVisibility(View.GONE);
                    mTvKmAway.setVisibility(View.GONE);
                    mTvInfoBandLine1.setVisibility(View.GONE);
                    mTvInfoBandLine2.setVisibility(View.GONE);
                    mTvInfoBandLine3.setVisibility(View.GONE);
                    mTvTime.setVisibility(View.GONE);
                    mTvMins.setVisibility(View.GONE);
                    mTvInfoBandLine4.setVisibility(View.GONE);
                } else {
                    String sMsg4 = "No waiting passengers";
                    showStatusMessages(sMsg4);
                    mLlWaitingPassenger.setVisibility(View.GONE);
                    mIvPhoto.setVisibility(View.GONE);
                    mTvKmAway.setVisibility(View.GONE);
                    mTvInfoBandLine1.setVisibility(View.GONE);
                    mTvInfoBandLine2.setVisibility(View.GONE);
                    mTvInfoBandLine3.setVisibility(View.GONE);
                    mTvTime.setVisibility(View.GONE);
                    mTvMins.setVisibility(View.GONE);
                    mTvInfoBandLine4.setVisibility(View.GONE);
                }

                for (int j = 0; j < nPassengers; j++) {
                    jPassenger = majActiveUsers.getJSONObject(j);
                    cTripDriver = new CTrip(majActiveUsers.getJSONObject(j)
                            .toString(), getApplicationContext());
                    if (cTripDriver.hasJumpedIn() && !cTripDriver.hasJumpedOut()) {
                        mLlPassengerInfo.setVisibility(View.VISIBLE);
                        mLlPassengerInfo.bringToFront();
                        mLlJoinPassenger.setVisibility(View.VISIBLE);
                        if (iHaveJoined > 0) {
                            mTvJoinedCount.setText("Waiting");
                            mTvJoinedCount.setVisibility(View.VISIBLE);
                            mtvJoinedCountNumber.setVisibility(View.VISIBLE);
                            mtvJoinedCountNumber.setText(Integer.toString(iHaveJoined));
                        } else {
                            mTvJoinedCount.setVisibility(View.GONE);
                            mtvJoinedCountNumber.setVisibility(View.GONE);
                        }
                        mIvPassenger1.setVisibility(View.VISIBLE);
                        mTvJumpedInCount.setVisibility(View.VISIBLE);
                        mLlWaitingPassenger.setVisibility(View.INVISIBLE);
                        mIvPhoto.setVisibility(View.GONE);
                        mTvKmAway.setVisibility(View.GONE);
                        mTvInfoBandLine1.setVisibility(View.GONE);
                        mTvInfoBandLine2.setVisibility(View.GONE);
                        mTvInfoBandLine3.setVisibility(View.GONE);
                        mTvTime.setVisibility(View.GONE);
                        mTvMins.setVisibility(View.GONE);
                        mTvInfoBandLine4.setVisibility(View.GONE);
                    }
                }

            }
            mbFollowMe = false;
            if (mIsFirstTime) {
                LatLngBounds oldBounds = mBounds.build();
                createBoundsWithMinDiagonal(oldBounds);
                mIsFirstTime = false;
            }

        } catch (Exception e) {
            SSLog.e(TAG, " showActivePassengerMarkers - ", e);
        }
    }

    @Override
    protected void showResponseThumbImageClick(final CTrip cTrip) {
        try {
            String distanceFromMe = "";
            String sptrip = CGlobals_lib.getInstance().getSharedPreferences(ForHireSharedTrip_act.this).
                    getString(Constants_lib.TRIP_TO_ADDRESS, "");
            final Dialog dialog = new Dialog(ForHireSharedTrip_act.this, R.style.DIALOG);
            dialog.setContentView(R.layout.passengerinfo_image);
            dialog.setTitle(cTrip.getName());

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

            if (cTrip.getlTimeDiff() < 1) {
                miinternet.setText("Online");
                miinternet.setVisibility(View.VISIBLE);
            } else {
                miinternet.setText("Last online " + cTrip.getlTimeDiff() + " min. ago");
                miinternet.setVisibility(View.VISIBLE);
            }
            if (cTrip.getlTimeDifflocation() < 30) {
                milocation.setText("Current location available");
                milocation.setVisibility(View.VISIBLE);
            } else {
                milocation.setText("Last location recd. " + cTrip.getlTimeDifflocation() / 60 + " min. ago");
                milocation.setVisibility(View.VISIBLE);
            }

            if (isInTrip()) {

                float results[] = new float[1];
                if (CGlobals_lib.getInstance().getMyLocation(ForHireSharedTrip_act.this) != null) {
                    LatLng latlng = new LatLng(CGlobals_lib.getInstance().getMyLocation(ForHireSharedTrip_act.this).getLatitude(),
                            CGlobals_lib.getInstance().getMyLocation(ForHireSharedTrip_act.this).getLongitude());
                    if (cTripDriver.getLatLng() != null && latlng != null) {
                        Location.distanceBetween(cTripDriver.getLatLng().latitude,
                                cTripDriver.getLatLng().longitude, latlng.latitude,
                                latlng.longitude, results);
                        distanceFromMe = String.format(Locale.getDefault(),
                                "%.2f km away", results[0] / 1000.0);
                    }
                }
                mitoadd.setVisibility(View.GONE);
                mifromadd.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(distanceFromMe)) {
                    mitripprogess.setText(distanceFromMe + " Km away");
                    mitripprogess.setVisibility(View.VISIBLE);
                }


                if (!TextUtils.isEmpty(cTrip.getUserProfileImageFileName()) && !TextUtils.isEmpty(cTrip.getUserProfileImagePath())) {

                    String url = Constants_driver.GET_USER_PROFILE_IMAGE_FILE_NAME_URL + cTrip.getUserProfileImagePath() +
                            cTrip.getUserProfileImageFileName();
                    ImageRequest request = new ImageRequest(url,
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap bitmap) {
                                    if (Build.VERSION.SDK_INT < 11) {
                                        Toast.makeText(ForHireSharedTrip_act.this, getString(R.string.api11not), Toast.LENGTH_LONG).show();
                                    } else {
                                        mProImage.setImageBitmap(bitmap);
                                        isImage = true;
                                    }
                                }
                            }, 0, 0, null,
                            new Response.ErrorListener() {
                                public void onErrorResponse(VolleyError error) {
                                    isImage = false;
                                }
                            });
                    MyApplication.getInstance().addToRequestQueue(request);
                }

                if (!isImage) {
                    Bitmap bitmap = cTrip.getContactThnumbnail();
                    if (bitmap != null) {
                        mProImage.setImageBitmap(bitmap);
                    }
                }

                mCall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:" + cTrip.getPhoneNo()));
                        startActivity(callIntent);
                    }
                });
                mWhatsApp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendAppMsg(cTrip.getPhoneNo());
                    }
                });
                mSMS.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent smsVIntent = new Intent(Intent.ACTION_VIEW);
                        smsVIntent.setType("vnd.android-dir/mms-sms");
                        smsVIntent.putExtra("address", cTrip.getPhoneNo());
                        try {
                            startActivity(smsVIntent);
                        } catch (Exception ex) {
                            Toast.makeText(ForHireSharedTrip_act.this, "Your sms has failed...",
                                    Toast.LENGTH_LONG).show();
                            ex.printStackTrace();
                        }
                    }
                });

            }

            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void setTripAction(String sTripStatus) {
        super.setTripAction(sTripStatus);

    }

    @Override
    protected void onResume() {
        setupImageButtons();
        MyLocation myLocation = new MyLocation(
                MyApplication.mVolleyRequestQueue, ForHireSharedTrip_act.this, mGoogleApiClient);
        myLocation.getLocation(this, locationResult);
        CGlobals_lib.getInstance().turnGPSOn1(ForHireSharedTrip_act.this, mGoogleApiClient);
        String sVDetails = MyApplication.getInstance().getPersistentPreference().getString("PERF_VEHICLE_DETAILS", "");
        if (!TextUtils.isEmpty(sVDetails)) {
            Type type = new TypeToken<VehicleResult>() {
            }.getType();
            VehicleResult vehicleResult = new Gson().fromJson(sVDetails, type);
            if (!TextUtils.isEmpty(vehicleResult.getVehicle_Category())) {
                if (vehicleResult.getVehicle_Category().equals(Constants_driver.VEHICLE_CATEGORY_LONG_DISTANCE_CAR)) {
                    mLlMarkerLayout.setVisibility(View.GONE);
                    mIvRedPin.setVisibility(View.GONE);
                    mIvGreenPin.setVisibility(View.GONE);
                    mTvSetLocation.setVisibility(View.GONE);
                    hasDriverMode = false;
                }
            }
        } else {
            mLlMarkerLayout.setVisibility(View.VISIBLE);
            mIvRedPin.setVisibility(View.VISIBLE);
            mIvGreenPin.setVisibility(View.VISIBLE);
            mTvSetLocation.setVisibility(View.VISIBLE);
            hasDriverMode = true;
        }

        CGlobals_driver.getInstance().sendUpdatePosition(
                mCurrentLocation, ForHireSharedTrip_act.this);
        if (isInTrip()) {
            if (!isLocationServiceRunning(LocationService_cab.class)) {
                StartLocationService();
            }
            String response = CGlobals_lib.getInstance().getSharedPreferences(ForHireSharedTrip_act.this)
                    .getString(Constants_lib.PREF_SAVE_TRIP_CREATE_RESPONSE, "");
            showActivePassengerMarkers(response);
        }
        super.onResume();
        mButtonStart.setVisibility(View.GONE);
        if (isInTrip()) {
            mivZoomFit.setVisibility(View.VISIBLE);
            mivInformation.setVisibility(View.VISIBLE);
        } else {
            mivZoomFit.setVisibility(View.GONE);
            mivInformation.setVisibility(View.GONE);
            flZoomActiveUsers.setVisibility(View.GONE);
        }
    }

    private boolean isLocationServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    protected String getGeoCountryCode() {
        return MyApplication.getInstance().getPreference()
                .getString(Constants_driver.PREF_CURRENT_COUNTRY, "");
    }


    protected boolean showPath() {
        return isInTrip();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
    }

    @Override
    public void endTrip() {
        setTripAction(Constants_lib.TRIP_ACTION_END);
        sendTripAction(Constants_lib.TRIP_ACTION_END);
        getSharedPreferencesEditor()
                .putString(Constants_driver.PREF_INVITE_USER_TO_APP, "");
        MyApplication.getInstance().getPersistentPreferenceEditor().putString("COMMERCIAL_VEHICLE_SEATFARE", "");
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
        CGlobals_lib.getInstance().getSharedPreferencesEditor(ForHireSharedTrip_act.this)
                .putString(Constants_lib.PREF_SAVE_TRIP_CREATE_RESPONSE, "");
        CGlobals_lib.getInstance().getSharedPreferencesEditor(ForHireSharedTrip_act.this).commit();
    }

    public void driverTripSummary() {
        final String url = Constants_driver.DRIVER_TRIP_SUMMARY_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getEndTripValue(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SSLog.e(TAG, " getActivePassengers - ",
                        error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tripid", String.valueOf(getTripId()));
                params = CGlobals_driver.getInstance().getBasicMobileParams(params,
                        url, ForHireSharedTrip_act.this);
                return CGlobals_driver.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    }

    public void getEndTripValue(String response) {
        float sCalculaTedtravelDistance = -1;
        JSONObject mjEndTripValue;
        try {
            mjEndTripValue = new JSONObject(response);
            sCalculaTedtravelDistance = mjEndTripValue.isNull("calculated_travel_distance") ?
                    -1 : mjEndTripValue.getInt("calculated_travel_distance");

        } catch (Exception e) {
            SSLog.e(TAG, "getEndTripValue", e);
        }
        if (sCalculaTedtravelDistance > 0) {
            mTvInfoBandLine4.setText("Distance traveled - " + String.format(Locale.getDefault(),
                    "%.2f km", sCalculaTedtravelDistance));
        } else {
            mTvInfoBandLine4.setText("You seem to be stationary");
        }

    }

    protected void StartLocationService() {
        stopService(new Intent(this, LocationService_cab.class));
        Intent serviceIntent = new Intent(this, LocationService_cab.class);
        serviceIntent.putExtra("lat", moToAddress.getLatitude());
        serviceIntent.putExtra("lng", moToAddress.getLongitude());
        serviceIntent.putExtra("tripid", getTripId());
        startService(serviceIntent);
    }

    protected void StopLocationService() {
        stopService(new Intent(this, LocationService_cab.class));
    }

    public void showStartDialog(int iTripId) {
        mIsGettingDirections = false;
        startTrip();
        progressCancel();

    }

    protected boolean hasTripEnded(Location location, double lat,
                                   double lng) {
        return false;

    }

    protected void inviteUsers() {
        String sVDetails = MyApplication.getInstance().getPersistentPreference().getString("PERF_VEHICLE_DETAILS", "");
        if (!TextUtils.isEmpty(sVDetails)) {
            Type type = new TypeToken<VehicleResult>() {
            }.getType();
            VehicleResult vehicleResult = new Gson().fromJson(sVDetails, type);
            if (!TextUtils.isEmpty(vehicleResult.getVehicle_Category())) {
                if (vehicleResult.getVehicle_Category().equals(Constants_driver.VEHICLE_CATEGORY_LONG_DISTANCE_CAR)) {
                    Intent intentInviteuser = new Intent(ForHireSharedTrip_act.this,
                            CheckSeatAndFare_act.class);
                    startActivityForResult(intentInviteuser, ACT_RESULT_SEAT_FARE);
                }
            }
        }
    }

    protected void sendTripAction(String sTripAction) {
        CGlobals_driver.getInstance().sendTripAction(sTripAction, ForHireSharedTrip_act.this);
    }

    public SharedPreferences.Editor getSharedPreferencesEditor() {
        return MyApplication.getInstance().getPersistentPreferenceEditor();
    }

    public void getInformation() {

        String sToAddress = CGlobals_lib.getInstance().getSharedPreferences(ForHireSharedTrip_act.this).
                getString(Constants_lib.TRIP_TO_ADDRESS, "");
        String sFromAddress = CGlobals_lib.getInstance().getSharedPreferences(ForHireSharedTrip_act.this)
                .getString(Constants_lib.TRIP_FROM_ADDRESS, "");

        String sResult = MyApplication.getInstance().getPersistentPreference().getString("PERF_VEHICLE_DETAILS", "");
        if (!TextUtils.isEmpty(sResult)) {
            Type type = new TypeToken<VehicleResult>() {
            }.getType();
            VehicleResult cTripInformation = new Gson().fromJson(sResult, type);

            final Dialog dialog = new Dialog(ForHireSharedTrip_act.this, R.style.DIALOG);
            dialog.setContentView(R.layout.active_user_more_information);
            dialog.setTitle("More Information ...");

            TextView misLastActive, mimsFrom, miTvDescription, mimsPhoneno, miTvDriverDistance, miTvStartLandmark,
                    miTvTripNote, mimsName, miVehicleType;

            misLastActive = (TextView) dialog.findViewById(R.id.tvsLastActive);
            miVehicleType = (TextView) dialog.findViewById(R.id.tvVehicleType);
            miTvDescription = (TextView) dialog.findViewById(R.id.tvTvDescription);
            mimsFrom = (TextView) dialog.findViewById(R.id.tvmsFrom);
            mimsName = (TextView) dialog.findViewById(R.id.tvmsName);
            mimsPhoneno = (TextView) dialog.findViewById(R.id.tvmsPhoneno);
            miTvDriverDistance = (TextView) dialog.findViewById(R.id.tvDriverDistance);
            miTvStartLandmark = (TextView) dialog.findViewById(R.id.tvStartLandmark);
            miTvTripNote = (TextView) dialog.findViewById(R.id.tvTripNote);
            mimsPhoneno.setVisibility(View.GONE);
            miTvDriverDistance.setVisibility(View.GONE);
            miTvStartLandmark.setVisibility(View.GONE);
            miTvTripNote.setVisibility(View.GONE);

            misLastActive.setText(Html.fromHtml("<font color=\"red\"><strong>Trip Information</strong></font>"));
            misLastActive.setVisibility(View.VISIBLE);

            if (isInTrip()) {
                mimsFrom.setText(Html.fromHtml("Start address: " + sFromAddress));
                miTvDescription.setText(Html.fromHtml("Destination: " + sToAddress));
                mimsFrom.setVisibility(View.VISIBLE);
                miTvDescription.setVisibility(View.VISIBLE);
            } else {
                miTvDescription.setVisibility(View.GONE);
                mimsFrom.setVisibility(View.GONE);
            }

            mimsName.setText(Html.fromHtml("<font color=\"red\"><strong>Vehicle Information</strong></font>"));
            mimsName.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(cTripInformation.getVehicleType().trim())) {
                miVehicleType.setText("Vehicle: " + cTripInformation.getVehicleColor() + " " + cTripInformation.getVehicleCompany() + " " +
                        cTripInformation.getVehicleNo() + " " + cTripInformation.getVehicleType());
                miVehicleType.setVisibility(View.VISIBLE);
            } else {
                miVehicleType.setVisibility(View.GONE);
            }
            dialog.show();
        }
    }


    protected String getTripUrl() {
        return Constants_driver.TRIP_CREATE_DRIVER_URL;
    }

    protected String getTripPathUrl() {
        return Constants_driver.TRIP_CREATE_TRIP_PATH_DRIVER_URL;
    }

    protected int getAppUserId() {
        return CGlobals_driver.getInstance().getAppUserId();
    }

    protected String getTripType() {
        return Constants_driver.TRIP_TYPE;
    }

    @Override
    protected String profileImageUrl() {
        return Constants_driver.GET_USER_PROFILE_IMAGE_FILE_NAME_URL;
    }

    @Override
    protected String passengertripsummayUrl() {
        return Constants_driver.JUMP_IN_URL;
    }

    protected String getEmailId() {
        return CGlobals_driver.getInstance().getEmailId();
    }

    public void showThumbImageClick() {

        try {
            final String phoneNo = MyApplication.getInstance().getPersistentPreference().

                    getString(Constants_driver.PREF_CURRENT_PASSENGER_PHONENO, "");

            String url = CGlobals_lib.getInstance().getSharedPreferences(ForHireSharedTrip_act.this).
                    getString(Constants_driver.PREF_CURRENT_PASSENGER_IMAGEURL, "");
            String sptrip = CGlobals_lib.getInstance().getSharedPreferences(getApplicationContext()).
                    getString(Constants_lib.TRIP_TO_ADDRESS, "");
            final Dialog dialog = new Dialog(ForHireSharedTrip_act.this, R.style.DIALOG);
            dialog.setContentView(R.layout.passengerinfo_image);
            dialog.setTitle(mTvInfoBandLine4.getText().toString());

            final TextView mifromadd, mitoadd, milocation, miinternet,
                    mitripprogess;
            final ImageView mProImage, miinternetlogo, milocationlogo;
            ImageButton mCall, mWhatsApp, mSMS;

            mProImage = (ImageView) dialog.findViewById(R.id.ivPassengerImage);
            miinternet = (TextView) dialog.findViewById(R.id.internet);
            miinternetlogo = (ImageView) dialog.findViewById(R.id.internetlogo);
            milocation = (TextView) dialog.findViewById(R.id.clocation);
            milocationlogo = (ImageView) dialog.findViewById(R.id.clocationlogo);
            mifromadd = (TextView) dialog.findViewById(R.id.tvName);
            mitoadd = (TextView) dialog.findViewById(R.id.tvPhoneNo);
            mitripprogess = (TextView) dialog.findViewById(R.id.tvKmAway);
            mCall = (ImageButton) dialog.findViewById(R.id.btnCall);
            mWhatsApp = (ImageButton) dialog.findViewById(R.id.btnWhatApp);
            mSMS = (ImageButton) dialog.findViewById(R.id.btnSMS);

            if (cTripDriver.getlTimeDiff() < 1) {
                miinternetlogo.setBackgroundResource(R.drawable.btn_online);
                miinternet.setText("Online");
                miinternetlogo.setVisibility(View.VISIBLE);
                miinternet.setVisibility(View.VISIBLE);
            } else {
                miinternetlogo.setBackgroundResource(R.drawable.btn_offline);
                miinternet.setText("Last online " + cTripDriver.getlTimeDiff() + " min. ago");
                miinternetlogo.setVisibility(View.VISIBLE);
                miinternet.setVisibility(View.VISIBLE);
            }
            if (cTripDriver.getlTimeDifflocation() < 30) {
                milocationlogo.setBackgroundResource(R.drawable.btn_online);
                milocation.setText("Current location available");
                milocationlogo.setVisibility(View.VISIBLE);
                milocation.setVisibility(View.VISIBLE);
            } else {
                milocationlogo.setBackgroundResource(R.drawable.btn_offline);
                milocation.setText("Last location recd. " + cTripDriver.getlTimeDifflocation() / 60 + " min. ago");
                milocationlogo.setVisibility(View.VISIBLE);
                milocation.setVisibility(View.VISIBLE);
            }

            if (isInTrip()) {
                mitoadd.setVisibility(View.GONE);
                mifromadd.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(mTvInfoBandLine1.getText().toString())) {
                    mitripprogess.setText(mTvInfoBandLine1.getText().toString() + " Km away");
                    mitripprogess.setVisibility(View.VISIBLE);
                }
                if (!TextUtils.isEmpty(url)) {

                    ImageRequest request = new ImageRequest(url,
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap bitmap) {
                                    if (Build.VERSION.SDK_INT < 11) {
                                        Toast.makeText(ForHireSharedTrip_act.this, getString(R.string.api11not), Toast.LENGTH_LONG).show();
                                    } else {
                                        mProImage.setImageBitmap(bitmap);
                                        isImage = true;
                                    }
                                }
                            }, 0, 0, null,
                            new Response.ErrorListener() {
                                public void onErrorResponse(VolleyError error) {
                                    isImage = false;
                                }
                            });
                    MyApplication.getInstance().addToRequestQueue(request);
                }

                if (!isImage) {
                    Bitmap bitmap = cTripDriver.getContactThnumbnail();
                    if (bitmap != null) {
                        mProImage.setImageBitmap(bitmap);
                    }
                }

                mCall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!TextUtils.isEmpty(phoneNo)) {
                            Intent callIntent = new Intent(Intent.ACTION_DIAL);
                            callIntent.setData(Uri.parse("tel:" + phoneNo));
                            startActivity(callIntent);
                        }
                    }
                });
                mWhatsApp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!TextUtils.isEmpty(phoneNo)) {
                            sendAppMsg(phoneNo);
                        }
                    }
                });
                mSMS.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!TextUtils.isEmpty(phoneNo)) {
                            Intent smsVIntent = new Intent(Intent.ACTION_VIEW);
                            smsVIntent.setType("vnd.android-dir/mms-sms");
                            smsVIntent.putExtra("address", phoneNo);
                            try {
                                startActivity(smsVIntent);
                            } catch (Exception ex) {
                                Toast.makeText(ForHireSharedTrip_act.this, "Your sms has failed...",
                                        Toast.LENGTH_LONG).show();
                                ex.printStackTrace();

                            }
                        }
                    }
                });

            }

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

    protected void callLastInvitedTrip() {
    }

    private String passengerTODriverDistance() {
        String distanceFromMe = "";
        if (CGlobals_lib.getInstance().getMyLocation(ForHireSharedTrip_act.this) != null) {
            LatLng latlng = new LatLng(CGlobals_lib.getInstance().getMyLocation(ForHireSharedTrip_act.this).getLatitude(),
                    CGlobals_lib.getInstance().getMyLocation(ForHireSharedTrip_act.this).getLongitude());

            if (cTripDriver.getLatLng() != null && latlng != null) {
                float results[] = new float[1];
                Location.distanceBetween(cTripDriver.getLatLng().latitude,
                        cTripDriver.getLatLng().longitude, latlng.latitude,
                        latlng.longitude, results);
                try {
                    if (cTripDriver.getMiPassed() == 1) {
                        mTvInfoBandLine2.setText(cTripDriver.getMiPassed() > 0 ? " (Passed)" : "");
                        mTvInfoBandLine2.setVisibility(View.VISIBLE);
                        mTvInfoBandLine2.setTextColor(getResources().getColor(R.color.red));
                    } else if (results[0] < Constants_driver.NEAR_BE_READY_DISTANCE) {
                        distanceFromMe = "Be Ready!";
                        mTvInfoBandLine2.setText(distanceFromMe);
                        mTvInfoBandLine2.setVisibility(View.VISIBLE);
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

    protected void startTrip() {
        progressMessage("create trip...");
        CGlobals_lib.getInstance().getSharedPreferencesEditor(ForHireSharedTrip_act.this)
                .putString(Constants_lib.PREF_SAVE_TRIP_CREATE_RESPONSE, "");
        CGlobals_lib.getInstance().getSharedPreferencesEditor(ForHireSharedTrip_act.this).commit();
        String sPlannedstartdatetime = CGlobals_lib.getInstance().getSharedPreferences(ForHireSharedTrip_act.this).
                getString("plannedstartdatetime", "");
        if (sPlannedstartdatetime != null) {
            plannedstartdatetime = sPlannedstartdatetime;
        }
        setTripAction(Constants_lib.TRIP_ACTION_BEGIN);
        CGlobals_driver.getInstance().sendTripActionWCallBack(plannedstartdatetime, Constants_driver.TRIP_ACTION_BEGIN,
                new CGlobals_driver.SendTripCallBackInterface() {

                    @Override
                    public void onRequestFinished(boolean success) {
                        StartLocationService();
                        setupImageButtons();
                        progressCancel();
                    }
                }, ForHireSharedTrip_act.this);
        MyApplication.getInstance().getPersistentPreferenceEditor().putBoolean(
                Constants_driver.PREF_IN_TRIP, true);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
        addRecentTrip(new CTrip(moFromAddress.getAddress(),
                moToAddress.getAddress(), moFromAddress.getLatitude(),
                moFromAddress.getLongitude(), moToAddress.getLatitude(),
                moToAddress.getLongitude()));

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(Constants_lib.PASSENGER_EVENT));
        miPassengerUpdateInterval = Constants_lib.PASSENGER_UPDATE_INTERVAL_ACTIVE;
        progressCancel();
    }

    protected String getUpdatePositionUrl() {
        return Constants_driver.UPDATE_POSITON_DRIVER_URL;
    }

    protected boolean isLoginflag() {
        return true;
    }

    private void addSpeed(double speed, String lastAccess) {
        double dTotalSpeed = 0.0, dAvgSpeed = 0.0;
        int i, j = 0;
        int eTA = 0, eTA1 = 0;
        CSpeed cSpeed = new CSpeed(speed, lastAccess);
        aDriverSpeed.add(cSpeed);
        Date lastDriverLocationTime = aDriverSpeed.get(aDriverSpeed.size() - 1).getTime();
        Date firstDriverLocationTime = aDriverSpeed.get(0).getTime();
        long dif = (lastDriverLocationTime.getTime() - firstDriverLocationTime.getTime()) / 1000;
        if (dif >= 30) {
            for (i = aDriverSpeed.size() - 1; i > 0; i--) {
                if (aDriverSpeed.get(i).getSpeed() > 0.83) {
                    dTotalSpeed += aDriverSpeed.get(i).getSpeed();
                    j++;
                }
            }
            dAvgSpeed = (dTotalSpeed / j) * 3.6;
            Log.d("AVGSpeed: ", String.valueOf(dAvgSpeed));

            float distance = Float.parseFloat(passengerTODriverDistance());
            if (distance > 0 && dAvgSpeed > 0.0) {
                eTA1 = (int) ((distance / dAvgSpeed) * 60);
                if (eTA1 < 1) {
                    eTA = (int) (((distance / dAvgSpeed) * 60) * 60);
                    mTvMins.setText("secs");
                } else {
                    eTA = eTA1;
                    mTvMins.setText("min.");
                }
                mTvTime.setText(String.valueOf(eTA));
                mTvMins.setVisibility(View.VISIBLE);
                mTvTime.setVisibility(View.VISIBLE);
            }
        }
    }

    protected void goFacebook() {
    }

    protected void goTwitter() {
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!isInTrip()) {
            String sVDetails = MyApplication.getInstance().getPersistentPreference().getString("PERF_VEHICLE_DETAILS", "");
            if (!TextUtils.isEmpty(sVDetails)) {
                Type type = new TypeToken<VehicleResult>() {
                }.getType();
                VehicleResult vehicleResult = new Gson().fromJson(sVDetails, type);
                if (vehicleResult.getVehicle_Category().equals(Constants_driver.VEHICLE_CATEGORY_LONG_DISTANCE_CAR)) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(ForHireSharedTrip_act.this);
                    alertDialog.setTitle("Are you sure you want to logout?");
                    alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            goLogout();
                        }
                    });
                    alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    if (!ForHireSharedTrip_act.this.isFinishing()) {
                        alertDialog.show();
                    }
                }
            }

            boolean isforhiredriver = MyApplication.getInstance().getPersistentPreference().
                    getBoolean("IS_FOR_HIRE_DRIVER", false);
            if (isforhiredriver) {
                MyApplication.getInstance().getPersistentPreferenceEditor().
                        putBoolean("IS_FOR_HIRE_DRIVER", false);
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                Intent intent = new Intent(ForHireSharedTrip_act.this, Dashboard_act.class);
                startActivity(intent);
                finish();
            }
        }
    }

    private void goLogout() {

        spShiftid = MyApplication.getInstance().getPersistentPreference()
                .getString(Constants_driver.PREF_SHIFT_ID, "");

        spVehicleid = MyApplication.getInstance().getPersistentPreference()
                .getString(Constants_driver.PREF_SHIFT_ID, "");

        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_driver.COMMERCIAL_DRIVER_END_SHIFT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getLogout(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String sErr = CGlobals_lib.getInstance().getVolleyError(error);
                Toast.makeText(ForHireSharedTrip_act.this, sErr, Toast.LENGTH_SHORT).show();
                try {
                    SSLog.d(TAG,
                            "Failed to mVehicleNoVerify :-  "
                                    + error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG, "mVehicleNoVerify - " + sErr,
                            error.getMessage());
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("shiftid", spShiftid);
                params.put("commercialvehicleid", spVehicleid);
                params = CGlobals_driver.getInstance().getMinAllMobileParams(params,
                        Constants_driver.COMMERCIAL_DRIVER_END_SHIFT_URL);
                return CGlobals_driver.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    }

    private void getLogout(String response) {
        if (response.trim().equals("-1") && TextUtils.isEmpty(response.trim())) {
            Toast.makeText(ForHireSharedTrip_act.this, "!server error", Toast.LENGTH_SHORT).show();
            return;
        }
        /*MyApplication.getInstance().getPersistentPreferenceEditor().putBoolean(Constants_driver.PREF_SET_SWITCH_FLAG, false);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();*/
        MyApplication.getInstance().getPersistentPreferenceEditor()
                .putBoolean(Constants_driver.PREF_LOGIN_LOGOUT_FLAG, false);
        MyApplication.getInstance().getPersistentPreferenceEditor().putString("PERF_VEHICLE_DETAILS", "");
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
        System.exit(0);
    }

    public void onClickSeatFare(View view) {
        Intent intent = new Intent(ForHireSharedTrip_act.this, CheckSeatAndFare_act.class);
        startActivity(intent);
    }

    protected void perfClear() {
        MyApplication.getInstance().getPersistentPreferenceEditor().putString("COMMERCIAL_VEHICLE_SEATFARE", "");
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
        CGlobals_lib.getInstance().getSharedPreferencesEditor(ForHireSharedTrip_act.this)
                .putString(Constants_lib.PREF_SAVE_TRIP_CREATE_RESPONSE, "");
        CGlobals_lib.getInstance().getSharedPreferencesEditor(ForHireSharedTrip_act.this).commit();
    }

    protected boolean hasBusDriver() {
        return false;
    }

    protected boolean hasGetaCabTrip() {
        boolean getac = false;
        if (responsevalue != null) {
            if (responsevalue.equals("1")) {
                getac = true;
            } else {
                getac = false;
            }
        }
        return getac;
    }


}
