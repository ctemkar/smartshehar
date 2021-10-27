package com.jumpinjumpout.apk.user.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.CTrip;
import com.jumpinjumpout.apk.lib.Constants_lib;
import com.jumpinjumpout.apk.lib.PinInfo;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.lib.ui.Driver_act;
import com.jumpinjumpout.apk.lib.ui.SearchAddress_act;
import com.jumpinjumpout.apk.user.CGlobals_user;
import com.jumpinjumpout.apk.user.Constants_user;
import com.jumpinjumpout.apk.user.Contact_Results;
import com.jumpinjumpout.apk.user.LocationService_user;
import com.jumpinjumpout.apk.user.MyApplication;
import com.jumpinjumpout.apk.user.ViewTimedDialog;

import org.json.JSONArray;
import org.json.JSONException;
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
import lib.app.util.CSms;
import lib.app.util.MyLocation;


public class FriendPool_act extends Driver_act {

    protected static final String TAG = "FriendPool_act: ";
    boolean isImage = false;
    private final int ACT_RESULT_INVITE_USER = 10;
    private String lastUpdate;
    CTrip mPassengerTrip;
    List<CTrip> mTrips;
    ArrayList<CSpeed> aDriverSpeed = new ArrayList<CSpeed>();
    ArrayList<Contact_Results> contactResults = new ArrayList<Contact_Results>();
    StringBuilder sGistLastInvited;
    CSms mSms = null;
    private String mMemberName, mMemberEmail, mMemberPhoneNo, mCommunityName;
    private ProgressDialog pDialog;
    private String plannedstartdatetime = "";
    public boolean expanded = false;
    private float offset1, offset2, offset3, offset4, offset6, offset7, offset8;
    ViewGroup fabContainer;
    protected FloatingActionButton mivZoomFit, mivInviteUsers,
            mivInformation, mivTwitter, mivFacebook, famZoom, mivWalkWithMe, flZoomActiveUsers;
    int iHaveJoined = 0, iHaveJumpedIn = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_floating_action_menu_friendpool);
        create();
        driverCreate();
        if (mCurrentLocation == null) {
            double myCurrentLat = CGlobals_lib.getInstance().getSharedPreferences(FriendPool_act.this).
                    getFloat(Constants_lib.PREF_MYLOCATION_LAT, (float) Constants_lib.INVALIDLAT);
            double myCurrentLon = CGlobals_lib.getInstance().getSharedPreferences(FriendPool_act.this).
                    getFloat(Constants_lib.PREF_MYLOCATION_LON, (float) Constants_lib.INVALIDLNG);
            if (myCurrentLat != Constants_lib.INVALIDLAT && myCurrentLon != Constants_lib.INVALIDLNG) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                                myCurrentLat, myCurrentLon),
                        DEFAULT_ZOOM));
            }
        }
        fabContainer = (ViewGroup) findViewById(R.id.fab_container);
        mivZoomFit = (FloatingActionButton) findViewById(R.id.ivZoomFit);
        mivInviteUsers = (FloatingActionButton) findViewById(R.id.ivInviteUsers);
        mivInformation = (FloatingActionButton) findViewById(R.id.ivInformation);
        famZoom = (FloatingActionButton) findViewById(R.id.famZoom);
        mivTwitter = (FloatingActionButton) findViewById(R.id.ivTwitter);
        mivFacebook = (FloatingActionButton) findViewById(R.id.ivFacebook);
        mivWalkWithMe = (FloatingActionButton) findViewById(R.id.ivwalkwithme);
        flZoomActiveUsers = (FloatingActionButton) findViewById(R.id.ivZoomActiveUsers);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
        mlActivePassengerMarkers = new ArrayList<Marker>();
        LocationManager service = (LocationManager) getSystemService(Activity.LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (enabled) {
            recentTripCreate();
        }
        if (mMap == null) {
            setUpMapIfNeeded();
        }
        mMap.setOnMapLoadedCallback(new OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                if (isInTrip()) {
                    try {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                                mZoomFitBounds.build(), 50));
                    } catch (Exception e) {
                        SSLog.e(TAG, " onMapLoaded - ", e);
                    }
                }
            }
        });

        famZoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expanded = !expanded;
                if (expanded) {
                    expandFab();
                    if (isInTrip()) {
                        mivTwitter.setVisibility(View.VISIBLE);
                        mivFacebook.setVisibility(View.VISIBLE);
                        mivWalkWithMe.setVisibility(View.VISIBLE);
                        mivZoomFit.setVisibility(View.VISIBLE);
                        mivInviteUsers.setVisibility(View.VISIBLE);
                        mivInformation.setVisibility(View.VISIBLE);
                    }
                } else {
                    collapseFab();
                    mivTwitter.setVisibility(View.GONE);
                    mivFacebook.setVisibility(View.GONE);
                    mivWalkWithMe.setVisibility(View.GONE);
                    mivZoomFit.setVisibility(View.GONE);
                    mivInviteUsers.setVisibility(View.GONE);
                    flZoomActiveUsers.setVisibility(View.GONE);
                    mivInformation.setVisibility(View.GONE);
                }
            }
        });

        fabContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                fabContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                int offset = -1;
                offset1 = famZoom.getY() - mivInformation.getY();
                mivInformation.setTranslationY(offset1);

                offset2 = famZoom.getY() - mivInviteUsers.getY();
                mivInviteUsers.setTranslationY(offset2);

                offset3 = famZoom.getY() - mivZoomFit.getY();
                mivZoomFit.setTranslationY(offset3);

                offset4 = famZoom.getY() - flZoomActiveUsers.getY();
                flZoomActiveUsers.setTranslationY(offset4);

                offset6 = famZoom.getY() - mivFacebook.getY();
                mivFacebook.setTranslationY(offset6);

                offset7 = famZoom.getY() - mivTwitter.getY();
                mivTwitter.setTranslationY(offset7);

                offset8 = famZoom.getY() - mivWalkWithMe.getY();
                mivWalkWithMe.setTranslationY(offset8);
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
    }

    public void recentTripCreate() {
        if (!isInTrip()) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {// Quick trip - Work or home trip from Dashboard
                if (extras.getString("DEST") != null) {
                    if (extras.getString("DEST").equals("WORK")) {
                        showStatusMessages("Quick trip");
                        moToAddress = CGlobals_user.getInstance().getWorkAddress(FriendPool_act.this);
                        mTvTo.setText(moToAddress.getAddress());
                        mLastInvited = 1;
                        pDialog = new ProgressDialog(FriendPool_act.this);
                        pDialog.setMessage("Please wait...");
                        pDialog.setCancelable(false);
                        lastInvitedmember();
                    } else if (extras.getString("DEST").equals("HOME")) {
                        showStatusMessages("Quick trip");
                        moToAddress = CGlobals_user.getInstance().getHomeAddress(FriendPool_act.this);
                        mTvTo.setText(moToAddress.getAddress());
                        mLastInvited = 1;
                        pDialog = new ProgressDialog(FriendPool_act.this);
                        pDialog.setMessage("Please wait...");
                        pDialog.setCancelable(false);
                        lastInvitedmember();
                    }
                } else if (extras.getString("RESENTTRIP") != null) {
                    if (extras.getString("RESENTTRIP").equals("1")) {
                        showStatusMessages("Recent trip");
                        Type type = new TypeToken<CAddress>() {
                        }.getType();
                        String fromAddress = extras.getString("FROM_RESENT");
                        String toAddress = extras.getString("TO_RESENT");
                        moFromAddress = new Gson().fromJson(fromAddress, type);
                        moToAddress = new Gson().fromJson(toAddress, type);
                        mTvFrom.setText(moFromAddress.getAddress());
                        mTvTo.setText(moToAddress.getAddress());
                        mIsDirectTrip = true;
                        goDirections();
                    }
                } else if (extras.getString("SCHEDULETRIP") != null) {
                    if (extras.getString("SCHEDULETRIP").equals("1")) {
                        Type type = new TypeToken<CAddress>() {
                        }.getType();
                        String startAddress = extras.getString("START");
                        String destAddress = extras.getString("DESTINATION");
                        moFromAddress = new Gson().fromJson(startAddress, type);
                        moToAddress = new Gson().fromJson(destAddress, type);
                        mTvFrom.setText(moFromAddress.getAddress());
                        mTvTo.setText(moToAddress.getAddress());
                        pDialog = new ProgressDialog(FriendPool_act.this);
                        pDialog.setMessage("Please wait...");
                        pDialog.setCancelable(false);
                        mIsDirectTrip = true;
                        goDirections();
                    }
                }
            } else {
                showStatusMessages("Enter a destination to create a trip");
            }
        }
    }

    public void collapseFab() {
        famZoom.setImageResource(com.jumpinjumpout.apk.lib.R.mipmap.ic_openfloat);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(createCollapseAnimator(mivInformation, offset1));
        animatorSet.playTogether(createCollapseAnimator(mivInviteUsers, offset2));
        animatorSet.playTogether(createCollapseAnimator(mivZoomFit, offset3));
        animatorSet.playTogether(createCollapseAnimator(flZoomActiveUsers, offset4));
        animatorSet.playTogether(createCollapseAnimator(mivFacebook, offset6));
        animatorSet.playTogether(createCollapseAnimator(mivTwitter, offset7));
        animatorSet.playTogether(createCollapseAnimator(mivWalkWithMe, offset8));
        animatorSet.start();
        animateFab();
    }

    public void expandFab() {
        famZoom.setImageResource(com.jumpinjumpout.apk.lib.R.mipmap.ic_close);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(createExpandAnimator(mivInformation, offset1));
        animatorSet.playTogether(createExpandAnimator(mivInviteUsers, offset2));
        animatorSet.playTogether(createExpandAnimator(mivZoomFit, offset3));
        animatorSet.playTogether(createExpandAnimator(flZoomActiveUsers, offset4));
        animatorSet.playTogether(createExpandAnimator(mivFacebook, offset6));
        animatorSet.playTogether(createExpandAnimator(mivTwitter, offset7));
        animatorSet.playTogether(createExpandAnimator(mivWalkWithMe, offset8));
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
            mivInviteUsers.setVisibility(View.VISIBLE);
        } else {
            mtvJoinedCountNumber.setVisibility(View.GONE);
            mTvJoinedCount.setVisibility(View.GONE);
            mTvJumpedInCount.setVisibility(View.GONE);
            mivZoomFit.setVisibility(View.GONE);
            mivInformation.setVisibility(View.GONE);
            flZoomActiveUsers.setVisibility(View.GONE);
            mivInviteUsers.setVisibility(View.GONE);
            mivTwitter.setVisibility(View.GONE);
            mivFacebook.setVisibility(View.GONE);
            mivWalkWithMe.setVisibility(View.GONE);
        }
    }

    protected void setTripActionFloatingbuttonHide() {
        mivZoomFit.setVisibility(View.GONE);
        mivInviteUsers.setVisibility(View.GONE);
        flZoomActiveUsers.setVisibility(View.GONE);
        mivInformation.setVisibility(View.GONE);
    }

    protected String getUpdatePositionUrl() {
        return Constants_user.UPDATE_POSITION_URL;
    }

    @Override
    public void onPause() {
        super.onPause();
        System.gc();
    }

    protected void showActivePassengerMarkers(String result) {
        mFlPassengerInfo.setVisibility(View.VISIBLE);
        double nearestLat = Constants_lib.INVALIDLAT, nearestLng = Constants_lib.INVALIDLNG;
        try {
            if (!isInTrip()) {
                setTripAction(Constants_user.TRIP_ACTION_END);
                endTrip();
                resetDisplay();
                return;
            }
            if (result.trim().equals("0")) {
                try {
                    if (mIsDirectTrip) {
                        String sMsg1 = "Trip invitation (last invited) sent to";
                        String sMsg2 = sGistLastInvited.toString();
                        String sMsg3 = "No waiting passengers";
                        showStatusMessages(sMsg1, sMsg2, sMsg3);
                    } else {
                        String sMsg = "No waiting passengers";
                        showStatusMessages(sMsg);
                    }
                    SSLog.d(TAG, "No propsective passengers");
                    resetDisplay();

                } catch (Exception e) {
                    SSLog.e(TAG, " showActivePassengerMarkers - ", e);
                }
                return;
            }
            if (mCurrentLocation != null && moToAddress.hasLatitude()
                    && moToAddress.hasLongitude()) {
                if (hasTripEnded(mCurrentLocation,
                        moToAddress.getLatitude(), moToAddress.getLongitude())) {
                    endTrip();
                    String sMsg = "Trip has Ended";
                    showStatusMessages(sMsg);
                }
            }
            if (result.trim().equals("-1")) {
                try {
                    if (iHaveJoined > 0 && iHaveJumpedIn > 0) {
                        if (mIsDirectTrip) {
                            String sMsg1 = "Trip invitation (last invited) sent to";
                            String sMsg2 = sGistLastInvited.toString();
                            String sMsg3 = "No waiting passengers";
                            showStatusMessages(sMsg1, sMsg2, sMsg3);
                        } else {
                            String sMsg = "No waiting passengers";
                            showStatusMessages(sMsg);
                        }

                        SSLog.d(TAG, "No propsective passengers");
                        resetDisplay();
                    }
                } catch (Exception e) {
                    SSLog.e(TAG, " showActivePassengerMarkers - ", e);
                }
                return;
            }
            if (result.trim().isEmpty()) {
                try {
                    if (iHaveJoined > 0 && iHaveJumpedIn > 0) {
                        if (mIsDirectTrip) {
                            String sMsg1 = "Trip invitation (last invited) sent to";
                            String sMsg2 = sGistLastInvited.toString();
                            String sMsg3 = "No waiting passengers";
                            showStatusMessages(sMsg1, sMsg2, sMsg3);
                        } else {
                            String sMsg = "No waiting passengers";
                            showStatusMessages(sMsg);
                        }

                        SSLog.d(TAG, "No propsective passengers");
                        resetDisplay();
                    }
                } catch (Exception e) {
                    SSLog.e(TAG, " showActivePassengerMarkers - ", e);
                }
                return;
            }
            // save all passenger data every time update to server
            CGlobals_lib.getInstance().getSharedPreferencesEditor(FriendPool_act.this)
                    .putString(Constants_lib.PREF_SAVE_TRIP_CREATE_RESPONSE, result);
            CGlobals_lib.getInstance().getSharedPreferencesEditor(FriendPool_act.this).commit();
            iHaveJoined = 0;
            iHaveJumpedIn = 0;
            String sMsg = "";
            showStatusMessages(sMsg);
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
                if (mCurrentLocation != null) {
                    iDriverNearestNode = getNearestNode(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                }
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
                mPassengerTrip = new CTrip(majActiveUsers.getJSONObject(j)
                        .toString(), getApplicationContext());
                mTrips.add(mPassengerTrip);
                int nInPath = jPassenger.isNull(Constants_user.COL_IN_PATH) ? 0
                        : Integer.parseInt(jPassenger
                        .getString(Constants_lib.COL_IN_PATH));
                int isTracking = jPassenger
                        .isNull(Constants_lib.COL_IS_TRACKING) ? 0 : Integer
                        .parseInt(jPassenger
                                .getString(Constants_lib.COL_IS_TRACKING));
                lastUpdate = mPassengerTrip.getLastActive();
                if (mPassengerTrip.hasJoined()) {
                    if (!mPassengerTrip.hasJumpedIn()) {
                        iHaveJoined++;
                    }
                }
                if (mPassengerTrip.hasJumpedIn() && !mPassengerTrip.hasJumpedOut()) {
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
                        if (iHaveJoined == 0) {
                            mLlWaitingPassenger.setBackgroundResource(R.drawable.white_background);
                        }
                    } else {
                        mTvJumpedInCount.setVisibility(View.GONE);
                    }
                } else {
                    flZoomActiveUsers.setVisibility(View.GONE);
                    mLlWaitingPassenger.setVisibility(View.INVISIBLE);
                    mTvInfoBandLine1.setVisibility(View.GONE);
                    mTvInfoBandLine2.setVisibility(View.GONE);
                    mTvInfoBandLine3.setVisibility(View.GONE);
                    mTvTime.setVisibility(View.GONE);
                    mTvMins.setVisibility(View.GONE);
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
                if (mPassengerTrip.hasJoined() && !mPassengerTrip.hasJumpedIn()) {
                    if (LeastDistanceFromMe < 0
                            || results[0] < LeastDistanceFromMe) {
                        LeastDistanceFromMe = results[0];
                        nearestPerson = mPassengerTrip.getName();
                        nearestLat = jPassenger.getDouble(Constants_lib.COL_LAT);
                        nearestLng = jPassenger.getDouble(Constants_lib.COL_LNG);
                    }
                }
                mBounds.include(new LatLng(jPassenger
                        .getDouble(Constants_lib.COL_LAT), jPassenger
                        .getDouble(Constants_lib.COL_LNG)));
                PinInfo pininfo = new PinInfo(mPassengerTrip.getLatLng(), mPassengerTrip.getName()
                        + "\n" + infoText, "", mPassengerTrip.getTripStatusResource(),
                        mPassengerTrip.getMarkerText(), jPassenger);
                Marker m = null;
                if (mPassengerTrip.hasJoined() && !mPassengerTrip.hasJumpedIn()) {
                    if (j == 0) {
                        m = placeMarker(pininfo, false, BitmapDescriptorFactory.HUE_ORANGE);
                    } else {
                        m = placeMarker(pininfo, false, -1);
                    }
                }
                if (isInTrip() && mPassengerTrip.hasJoined() && !mPassengerTrip.hasJumpedIn()) {

                    iNode = getNearestNode(mPassengerTrip.getLatLng());
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
                                if (mPassengerTrip.getlTimeDiff() < 1 && mPassengerTrip.getlTimeDifflocation() < 30) {
                                    mTvInfoBandLine3.setBackgroundResource(R.drawable.btn_online);
                                } else {
                                    mTvInfoBandLine3.setBackgroundResource(R.drawable.btn_offline);
                                }
                                mTvInfoBandLine3.setVisibility(View.VISIBLE);
                                String sDistanceFromMe = "<b>" + CGlobals_lib.getInstance().getDistance(
                                        LeastDistanceFromMe) + "</b>";
                                mTvInfoBandLine1.setText(Html.fromHtml(passengerTODriverDistance()));
                                mTvInfoBandLine4.setText((TextUtils.isEmpty(mPassengerTrip.getName()) ? ""
                                        : mPassengerTrip.getName()));
                                mTvInfoBandLine4.setVisibility(View.VISIBLE);
                                setNearestPassengerLatLng(nearestLat, nearestLng);
                                if (!TextUtils.isEmpty(mPassengerTrip.getUserProfileImageFileName()) && !TextUtils.isEmpty(mPassengerTrip.getUserProfileImagePath())) {
                                    String url = Constants_user.GET_USER_PROFILE_IMAGE_FILE_NAME_URL + mPassengerTrip.getUserProfileImagePath() +
                                            mPassengerTrip.getUserProfileImageFileName();
                                    CGlobals_lib.getInstance().getSharedPreferencesEditor(FriendPool_act.this).
                                            putString(Constants_user.PREF_CURRENT_PASSENGER_IMAGEURL, url);
                                    CGlobals_lib.getInstance().getSharedPreferencesEditor(FriendPool_act.this).commit();
                                    ImageRequest request = new ImageRequest(url,
                                            new Response.Listener<Bitmap>() {
                                                @Override
                                                public void onResponse(Bitmap bitmap) {
                                                    if (Build.VERSION.SDK_INT < 11) {
                                                        Toast.makeText(FriendPool_act.this, getString(R.string.api11not), Toast.LENGTH_LONG).show();
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
                                                    CGlobals_lib.getInstance().getSharedPreferencesEditor(FriendPool_act.this).
                                                            putString(Constants_user.PREF_CURRENT_PASSENGER_IMAGEURL, "");
                                                    CGlobals_lib.getInstance().getSharedPreferencesEditor(FriendPool_act.this).commit();
                                                }
                                            });
                                    MyApplication.getInstance().addToRequestQueue(request);
                                } else {
                                    isImage = false;
                                    CGlobals_lib.getInstance().getSharedPreferencesEditor(FriendPool_act.this).
                                            putString(Constants_user.PREF_CURRENT_PASSENGER_IMAGEURL, "");
                                    CGlobals_lib.getInstance().getSharedPreferencesEditor(FriendPool_act.this).commit();
                                }
                                if (!isImage) {
                                    Bitmap bitmap = mPassengerTrip.getContactThnumbnail();
                                    if (bitmap != null) {
                                        mIvPhoto.setImageBitmap(bitmap);
                                        mIvPhoto.setVisibility(View.VISIBLE);
                                    }
                                    CGlobals_lib.getInstance().getSharedPreferencesEditor(FriendPool_act.this).
                                            putString(Constants_user.PREF_CURRENT_PASSENGER_IMAGEURL, "");
                                    CGlobals_lib.getInstance().getSharedPreferencesEditor(FriendPool_act.this).commit();
                                }
                                mIvPhoto.setVisibility(View.VISIBLE);
                                mTvKmAway.setVisibility(View.VISIBLE);
                                // 2015-06-20 16:31:44
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date date = new Date();
                                System.out.println(dateFormat.format(date));
                                addSpeed(mCurrentLocation.getSpeed(), dateFormat.format(date));
                                MyApplication.getInstance().getPersistentPreferenceEditor().
                                        putString(Constants_user.PREF_CURRENT_PASSENGER_PHONENO, mPassengerTrip.getPhoneNo());
                                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                                foundNearestPassenger = true;
                            } else {
                                if (mPassengerTrip.hasJumpedIn() && !mPassengerTrip.hasJumpedOut()) {
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
                                } else if (mIsDirectTrip) {
                                    String sMsg1 = "Trip invitation (last invited) sent to";
                                    String sMsg2 = sGistLastInvited.toString();
                                    String sMsg3 = "No waiting passengers";
                                    showStatusMessages(sMsg1, sMsg2, sMsg3);
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
                    }
                }
            } // for loop
            if (iNearestPassengerNode != -1 && nearestMarker != null
                    && iNearestPassengerNode >= iDriverNearestNode) {
            } else {
                if (mPassengerTrip.hasJumpedIn() && !mPassengerTrip.hasJumpedOut()) {
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
                } else if (mIsDirectTrip) {
                    String sMsg1 = "Trip invitation (last invited) sent to";
                    String sMsg2 = sGistLastInvited.toString();
                    String sMsg3 = "No waiting passengers";
                    showStatusMessages(sMsg1, sMsg2, sMsg3);
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
                for (int j = 0; j < nPassengers; j++) {
                    jPassenger = majActiveUsers.getJSONObject(j);
                    mPassengerTrip = new CTrip(majActiveUsers.getJSONObject(j)
                            .toString(), getApplicationContext());
                    if (mPassengerTrip.hasJumpedIn() && !mPassengerTrip.hasJumpedOut()) {
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

    public void onClickFrom(View v) {
        Intent i = new Intent(FriendPool_act.this,
                SearchAddress_act.class);
        i.putExtra("cc", sCountryCode);
        startActivityForResult(i, ACTRESULT_FROM);
        if (!TextUtils.isEmpty(moFromAddress.getAddress())) {
            CGlobals_lib.getInstance().addRecentAddress(moFromAddress);
        }
        mHasUserTouchedFrom = true;
    }

    @Override
    protected void onResume() {
        setupImageButtons();
        MyLocation myLocation = new MyLocation(
                MyApplication.mVolleyRequestQueue, FriendPool_act.this, mGoogleApiClient);
        myLocation.getLocation(FriendPool_act.this, locationResult);
        CGlobals_lib.getInstance().turnGPSOn1(FriendPool_act.this, mGoogleApiClient);
        CGlobals_user.getInstance().sendUpdatePosition(
                CGlobals_user.getInstance().getMyLocation(FriendPool_act.this), getApplicationContext());
        if (isInTrip()) {
            if (!isLocationServiceRunning(LocationService_user.class)) {
                StartLocationService();
            }
            String response = CGlobals_lib.getInstance().getSharedPreferences(FriendPool_act.this)
                    .getString(Constants_lib.PREF_SAVE_TRIP_CREATE_RESPONSE, "");
            showActivePassengerMarkers(response);
        }
        super.onResume();
        if (isInTrip()) {
            mivZoomFit.setVisibility(View.VISIBLE);
            mivInformation.setVisibility(View.VISIBLE);
        } else {
            mivTwitter.setVisibility(View.GONE);
            mivFacebook.setVisibility(View.GONE);
            mivWalkWithMe.setVisibility(View.GONE);
            mivZoomFit.setVisibility(View.GONE);
            mivInviteUsers.setVisibility(View.GONE);
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
        return MyApplication.getInstance().getPersistentPreference()
                .getString(Constants_user.PREF_CURRENT_COUNTRY, "");
    }

    protected boolean showPath() {
        return isInTrip();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
    }

    @Override
    public void endTrip() {
        mMap.clear();
        isTripFrozen = false;
        setTripAction(Constants_lib.TRIP_ACTION_END);
        sendTripAction(Constants_lib.TRIP_ACTION_END);
        MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("RATING", 0.0f);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
        CGlobals_lib.getInstance().getSharedPreferencesEditor(FriendPool_act.this).
                putString("plannedstartdatetime", "");
        CGlobals_lib.getInstance().getSharedPreferencesEditor(FriendPool_act.this)
                .putString(Constants_lib.PREF_SAVE_TRIP_CREATE_RESPONSE, "");
        CGlobals_lib.getInstance().getSharedPreferencesEditor(FriendPool_act.this).commit();
    }

    protected void StartLocationService() {
        try {
            stopService(new Intent(this, LocationService_user.class));
            Intent serviceIntent = new Intent(this, LocationService_user.class);
            serviceIntent.putExtra("lat", moToAddress.getLatitude());
            serviceIntent.putExtra("lng", moToAddress.getLongitude());
            serviceIntent.putExtra("tripid", getTripId());
            startService(serviceIntent);
        } catch (Exception e) {
            SSLog.e(TAG, "StartLocationService ", e);
        }
    }

    protected void StopLocationService() {
        stopService(new Intent(this, LocationService_user.class));
    }

    public void showStartDialog(int iTripId) {
        mIsGettingDirections = false;
        startTrip();
        progressCancel();
    }

    protected boolean hasTripEnded(Location location, double lat,
                                   double lng) {
        boolean hasTripEnded = CGlobals_user.getInstance().hasTripEnded(location, lat, lng, FriendPool_act.this);
        if (hasTripEnded) {
            mivInviteUsers.setVisibility(View.GONE);
            mivInformation.setVisibility(View.GONE);
            mIvPassenger1.setVisibility(View.GONE);
        }
        return CGlobals_user.getInstance().hasTripEnded(location, lat, lng, FriendPool_act.this);
    }

    protected void inviteUsers() {
        int iTripId = MyApplication.getInstance().getPersistentPreference()
                .getInt(Constants_user.PREF_TRIP_ID_INT, -1);
        mIsGettingDirections = false;
        Intent intentInviteuser = new Intent(FriendPool_act.this,
                InviteUserToApp.class);
        intentInviteuser.putExtra("TRIPID", iTripId);
        startActivityForResult(intentInviteuser, ACT_RESULT_INVITE_USER);
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
            }
        }
    }

    public void onClickInviteUsers(View v) {
        int iTripId = MyApplication.getInstance().getPersistentPreference()
                .getInt(Constants_user.PREF_TRIP_ID_INT, -1);
        mIsGettingDirections = false;
        Intent intentInviteuser = new Intent(FriendPool_act.this,
                InviteUserToApp.class);
        intentInviteuser.putExtra("TRIPID", iTripId);
        intentInviteuser.putExtra("OPENTRIP", "open");
        startActivityForResult(intentInviteuser, ACT_RESULT_INVITE_USER);
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
            }
        }
    }

    @Override
    protected void driverTripSummary() {
    }

    protected void sendTripAction(String sTripAction) {
        CGlobals_user.getInstance().sendTripAction(sTripAction, FriendPool_act.this);
    }

    public void getInformation() {
        String passengerDistanceJumpInOut = CGlobals_lib.getInstance().getSharedPreferences(FriendPool_act.this).
                getString(Constants_lib.PREF_PASSENGER_JUMPINOUT_DISTANCE, "");
        String sToAddress = CGlobals_lib.getInstance().getSharedPreferences(getApplicationContext()).
                getString(Constants_lib.TRIP_TO_ADDRESS, "");
        String sFromAddress = CGlobals_lib.getInstance().getSharedPreferences(getApplicationContext())
                .getString(Constants_lib.TRIP_FROM_ADDRESS, "");

        final Dialog dialog = new Dialog(FriendPool_act.this, R.style.DIALOG);
        dialog.setContentView(com.jumpinjumpout.apk.lib.R.layout.information_trip_list);
        dialog.setTitle("More Information ...");
        TextView miTvDriverDistance, miTvTripStatus, miTvStartLandmark, miTvTripNote,
                misLastActive, miVehicleType, miVehicleNo, miTvDescription, miVehicleCompany, miVehicleColor,
                mimsFrom, miCarDesc, mimsName, mimsPhoneno;

        misLastActive = (TextView) dialog.findViewById(com.jumpinjumpout.apk.lib.R.id.tvsLastActive);
        miVehicleType = (TextView) dialog.findViewById(com.jumpinjumpout.apk.lib.R.id.tvVehicleType);
        miVehicleNo = (TextView) dialog.findViewById(com.jumpinjumpout.apk.lib.R.id.tvVehicleNo);
        miTvDescription = (TextView) dialog.findViewById(com.jumpinjumpout.apk.lib.R.id.tvTvDescription);
        miVehicleCompany = (TextView) dialog.findViewById(com.jumpinjumpout.apk.lib.R.id.tvVehicleCompany);
        miVehicleColor = (TextView) dialog.findViewById(com.jumpinjumpout.apk.lib.R.id.tvVehicleColor);
        mimsFrom = (TextView) dialog.findViewById(com.jumpinjumpout.apk.lib.R.id.tvmsFrom);
        miCarDesc = (TextView) dialog.findViewById(com.jumpinjumpout.apk.lib.R.id.tvCarDesc);
        mimsName = (TextView) dialog.findViewById(com.jumpinjumpout.apk.lib.R.id.tvmsName);
        mimsPhoneno = (TextView) dialog.findViewById(com.jumpinjumpout.apk.lib.R.id.tvmsPhoneno);
        miTvDriverDistance = (TextView) dialog.findViewById(com.jumpinjumpout.apk.lib.R.id.tvDriverDistance);
        miTvTripStatus = (TextView) dialog.findViewById(com.jumpinjumpout.apk.lib.R.id.tvTripStatus);
        miTvStartLandmark = (TextView) dialog.findViewById(com.jumpinjumpout.apk.lib.R.id.tvStartLandmark);
        miTvTripNote = (TextView) dialog.findViewById(com.jumpinjumpout.apk.lib.R.id.tvTripNote);

        misLastActive.setVisibility(View.GONE);
        miVehicleType.setVisibility(View.GONE);
        miVehicleNo.setVisibility(View.GONE);
        miTvDescription.setVisibility(View.GONE);
        miVehicleCompany.setVisibility(View.GONE);
        miVehicleColor.setVisibility(View.GONE);
        mimsFrom.setVisibility(View.GONE);
        miCarDesc.setVisibility(View.GONE);
        mimsName.setVisibility(View.GONE);
        mimsPhoneno.setVisibility(View.GONE);
        miTvDriverDistance.setVisibility(View.GONE);
        miTvTripStatus.setVisibility(View.GONE);
        miTvStartLandmark.setVisibility(View.GONE);
        miTvTripNote.setVisibility(View.GONE);

        if (isInTrip()) {
            miTvDescription.setText(Html.fromHtml("Destination: " + sToAddress));
            miTvDescription.setVisibility(View.VISIBLE);
            mimsFrom.setText(Html.fromHtml("Start address: " + sFromAddress));
            mimsFrom.setVisibility(View.VISIBLE);
            misLastActive.setText("Online now");
            misLastActive.setVisibility(View.VISIBLE);
            miTvTripStatus.setText("Trip in progress.");
            miTvTripStatus.setVisibility(View.VISIBLE);
        } else {
            misLastActive.setVisibility(View.GONE);
            miTvDescription.setVisibility(View.GONE);
            mimsFrom.setVisibility(View.GONE);
            miTvTripStatus.setVisibility(View.GONE);
            miTvTripNote.setVisibility(View.GONE);
            miTvStartLandmark.setVisibility(View.GONE);
        }
        dialog.show();
    }


    public String getTripType() {
        return Constants_lib.TRIP_TYPE_USER;
    }

    protected int getAppUserId() {
        return CGlobals_user.getInstance().getAppUserId();
    }

    protected String getTripUrl() {
        return Constants_user.CREATE_TRIP_USER_URL;
    }

    protected String getTripPathUrl() {
        return Constants_user.CREATE_TRIP_PATH_USER_URL;
    }

    protected String getEmailId() {
        return CGlobals_user.getInstance().getEmailId();
    }

    public void showThumbImageClick() {
        try {

            final String phoneNo = MyApplication.getInstance().getPersistentPreference().

                    getString(Constants_user.PREF_CURRENT_PASSENGER_PHONENO, "");

            String url = CGlobals_lib.getInstance().getSharedPreferences(FriendPool_act.this).
                    getString(Constants_user.PREF_CURRENT_PASSENGER_IMAGEURL, "");

            String sptrip = CGlobals_lib.getInstance().getSharedPreferences(getApplicationContext()).
                    getString(Constants_lib.TRIP_TO_ADDRESS, "");
            final Dialog dialog = new Dialog(FriendPool_act.this, R.style.DIALOG);
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

            if (mPassengerTrip.getlTimeDiff() < 1) {
                miinternetlogo.setBackgroundResource(R.drawable.btn_online);
                miinternet.setText("Online");
                miinternetlogo.setVisibility(View.VISIBLE);
                miinternet.setVisibility(View.VISIBLE);
            } else {
                miinternetlogo.setBackgroundResource(R.drawable.btn_offline);
                miinternet.setText("Last online " + mPassengerTrip.getlTimeDiff() + " min. ago");
                miinternetlogo.setVisibility(View.VISIBLE);
                miinternet.setVisibility(View.VISIBLE);
            }
            if (mPassengerTrip.getlTimeDifflocation() < 30) {
                milocationlogo.setBackgroundResource(R.drawable.btn_online);
                milocation.setText("Current location available");
                milocationlogo.setVisibility(View.VISIBLE);
                milocation.setVisibility(View.VISIBLE);
            } else {
                milocationlogo.setBackgroundResource(R.drawable.btn_offline);
                if (mPassengerTrip.getlTimeDifflocation() < 60) {
                    milocation.setText("Location delayed " + mPassengerTrip.getlTimeDifflocation() + " secs. ago");
                } else {
                    milocation.setText("Location delayed " + mPassengerTrip.getlTimeDifflocation() / 60 + " min. ago");
                }
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
                                        Toast.makeText(FriendPool_act.this, getString(R.string.api11not), Toast.LENGTH_LONG).show();
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
                    Bitmap bitmap = mPassengerTrip.getContactThnumbnail();
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
                                Toast.makeText(FriendPool_act.this, "Your sms has failed...",
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

    protected void goFacebook() {
        String sMessage = "";
        String sToAddress = CGlobals_lib.getInstance().getSharedPreferences(getApplicationContext()).
                getString(Constants_lib.TRIP_TO_ADDRESS, "");
        String sFromAddress = CGlobals_lib.getInstance().getSharedPreferences(getApplicationContext())
                .getString(Constants_lib.TRIP_FROM_ADDRESS, "");
        sMessage = "Hi All, I just created a shared ride using Jump In Jump Out" +
                " from " + sFromAddress + " to " + sToAddress + ". If anyone needs a ride," +
                " look for my ride on JiJo and join me";
        Intent intent = new Intent(FriendPool_act.this, Facebook_act.class);
        intent.putExtra("INTENT_MESSAGE", sMessage);
        startActivity(intent);
    }

    protected void goTwitter() {
        String sMessage = "";
        String sToAddress = CGlobals_lib.getInstance().getSharedPreferences(getApplicationContext()).
                getString(Constants_lib.TRIP_TO_ADDRESS, "");
        sMessage = "Hey, I just created a shared trip using Jump In Jump Out";// + sToAddress;
        Intent intent = new Intent(FriendPool_act.this, Twitter_act.class);
        intent.putExtra("INTENT_MESSAGE", sMessage);
        startActivity(intent);
    }


    public void showResponseThumbImageClick(final CTrip cTrip) {
        try {
            String passengerDistanceJumpInOut = CGlobals_lib.getInstance().getSharedPreferences(FriendPool_act.this).
                    getString(Constants_lib.PREF_PASSENGER_JUMPINOUT_DISTANCE, "");
            String sptrip = CGlobals_lib.getInstance().getSharedPreferences(FriendPool_act.this).
                    getString(Constants_lib.TRIP_TO_ADDRESS, "");
            final Dialog dialog = new Dialog(FriendPool_act.this, R.style.DIALOG);
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
                if (cTrip.getlTimeDifflocation() < 60) {
                    milocation.setText("Location delayed " + cTrip.getlTimeDifflocation() + " secs. ago");
                } else {
                    milocation.setText("Location delayed " + cTrip.getlTimeDifflocation() / 60 + " min. ago");
                }
                milocation.setVisibility(View.VISIBLE);
            }
            if (isInTrip()) {
                mifromadd.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(passengerTODriverDistance())) {
                    mitripprogess.setText(passengerTODriverDistance() + " Km away");
                    mitripprogess.setVisibility(View.VISIBLE);
                }
                if (!TextUtils.isEmpty(cTrip.getUserProfileImageFileName()) && !TextUtils.isEmpty(cTrip.getUserProfileImagePath())) {

                    String url = Constants_user.GET_USER_PROFILE_IMAGE_FILE_NAME_URL + cTrip.getUserProfileImagePath() +
                            cTrip.getUserProfileImageFileName();
                    ImageRequest request = new ImageRequest(url,
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap bitmap) {
                                    if (Build.VERSION.SDK_INT < 11) {
                                        Toast.makeText(FriendPool_act.this, getString(R.string.api11not), Toast.LENGTH_LONG).show();
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
                            Toast.makeText(FriendPool_act.this, "Your sms has failed...",
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
        String distanceFromMe = "", distanceFromMe1 = "";
        if (CGlobals_lib.getInstance().getMyLocation(FriendPool_act.this) != null) {
            LatLng latlng = new LatLng(CGlobals_lib.getInstance().getMyLocation(FriendPool_act.this).getLatitude(),
                    CGlobals_lib.getInstance().getMyLocation(FriendPool_act.this).getLongitude());
            if (mPassengerTrip.getLatLng() != null && latlng != null) {
                float results[] = new float[1];
                Location.distanceBetween(mPassengerTrip.getLatLng().latitude,
                        mPassengerTrip.getLatLng().longitude, latlng.latitude,
                        latlng.longitude, results);
                try {
                    if (mPassengerTrip.getMiPassed() == 1) {
                        mTvInfoBandLine2.setText(mPassengerTrip.getMiPassed() > 0 ? " (Passed)" : "");
                        mTvInfoBandLine2.setVisibility(View.VISIBLE);
                        mTvInfoBandLine2.setTextColor(getResources().getColor(R.color.red));
                    } else if (results[0] < Constants_user.NEAR_BE_READY_DISTANCE) {
                        distanceFromMe1 = "Be Ready!";
                        mTvInfoBandLine2.setText(distanceFromMe1);
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
                    mTvMins.setText("sec.");
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

    protected void startTrip() {
        progressMessage("create trip...");
        MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("RATING", 0.0f);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
        CGlobals_lib.getInstance().getSharedPreferencesEditor(FriendPool_act.this)
                .putString(Constants_lib.PREF_SAVE_TRIP_CREATE_RESPONSE, "");
        CGlobals_lib.getInstance().getSharedPreferencesEditor(FriendPool_act.this).commit();
        if (isStartLater) {
            String sPlannedstartdatetime = CGlobals_lib.getInstance().getSharedPreferences(FriendPool_act.this).
                    getString("plannedstartdatetime", "");
            if (sPlannedstartdatetime != null) {
                plannedstartdatetime = sPlannedstartdatetime;
            }
        }
        setTripAction(Constants_lib.TRIP_ACTION_BEGIN);
        CGlobals_user.getInstance().sendTripActionWCallBack(plannedstartdatetime, Constants_user.TRIP_ACTION_BEGIN,
                new CGlobals_user.SendTripCallBackInterface() {

                    @Override
                    public void onRequestFinished(boolean success) {
                        invitetripmember();
                        StartLocationService();
                        setupImageButtons();
                    }
                }, FriendPool_act.this);
        MyApplication.getInstance().getPersistentPreferenceEditor().putBoolean(
                Constants_user.PREF_IN_TRIP, true);
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

    protected void callLastInvitedTrip() {
        MyApplication.getInstance().getPersistentPreferenceEditor()
                .putString(Constants_user.PREF_INVITE_USER_CURRENT_TO_APP, "");
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
    }

    private void invitetripmember() {
        // Send Data to Server Database Using Volley Library
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.INVITE_TRIP_MEMBER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());
                        noAppRows();
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                try {
                    SSLog.e(TAG, "invitetripmember :-   ",
                            error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG, "invitetripmember  -  ", e);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params.put("tripid", String.valueOf(getTripId()));
                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.INVITE_TRIP_MEMBER_URL);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_user.INVITE_TRIP_MEMBER_URL;
                try {
                    String url = url1 + "?" + getParams.toString()
                            + "&verbose=Y";
                    System.out.println("url  " + url);

                } catch (Exception e) {
                    SSLog.e(TAG, "invitetripmember", e);
                }
                return CGlobals_user.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    }

    private void noAppRows() {
        // Send Data to Server Database Using Volley Library
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.DO_NOT_HAVE_APP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());
                        sendSMS(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    SSLog.e(TAG, "noAppRows :-   ", error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG, "noAppRows -  ", e);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params.put("tripid", String.valueOf(getTripId()));
                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.DO_NOT_HAVE_APP_URL);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_user.DO_NOT_HAVE_APP_URL;
                try {
                    String url = url1 + "?" + getParams.toString()
                            + "&verbose=Y";
                    System.out.println("url  " + url);
                } catch (Exception e) {
                    SSLog.e(TAG, "noAppRows", e);
                }
                return CGlobals_user.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    }

    private void sendSMS(String response) {
        String member_PhoneNo;
        if (TextUtils.isEmpty(response) || response.trim().equals("-1")) {
            return;
        }
        try {
            JSONArray aJson = new JSONArray(response);
            for (int i = 0; i < aJson.length(); i++) {
                JSONObject person = (JSONObject) aJson.get(i);
                member_PhoneNo = person.isNull("member_phoneno") ? "" : person
                        .getString("member_phoneno");
                mSms = new CSms(FriendPool_act.this);
                mSms.sendSMS(member_PhoneNo,
                        FriendPool_act.this.getString(R.string.invitesms));
            }
        } catch (Exception e) {
            SSLog.e(TAG, "sendSMS", e);
        }
    }

    private void lastInvitedmember() {
        showpDialog();
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.LAST_INVITED_MEMBER_TRIP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());
                        lastInvitedmemberName(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidepDialog();
                try {
                    SSLog.e(TAG, "noAppRows :-   ", error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG, "noAppRows -  ", e);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.LAST_INVITED_MEMBER_TRIP_URL);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_user.LAST_INVITED_MEMBER_TRIP_URL;
                try {
                    String url = url1 + "?" + getParams.toString()
                            + "&verbose=Y";
                    System.out.println("url  " + url);
                } catch (Exception e) {
                    SSLog.e(TAG, "noAppRows", e);
                }
                return CGlobals_user.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    }

    private void lastInvitedmemberName(String response) {
        if (TextUtils.isEmpty(response) && response.equals("0")) {
            hidepDialog();
            return;
        }
        if (response.trim().equals("-1")) {
            String sMsg1 = "There is a problem connecting ot the system and getting your last invitees.";
            String sMsg2 = "You can continue creating a trip in the normal way by touching Go";
            showStatusMessages(sMsg1, sMsg2);
            Toast.makeText(FriendPool_act.this, getString(R.string.severprobleminviting), Toast.LENGTH_LONG).show();
            hidepDialog();
            return;
        }
        hidepDialog();
        try {
            JSONArray aJson = new JSONArray(response);
            for (int i = 0; i < aJson.length(); i++) {
                JSONObject person = (JSONObject) aJson.get(i);
                mMemberName = person.isNull("member_name") ? "" : person
                        .getString("member_name");
                mMemberEmail = person.isNull("member_email") ? "" : person
                        .getString("member_email");
                mMemberPhoneNo = person.isNull("member_phoneno") ? "" : person
                        .getString("member_phoneno");
                mCommunityName = person.isNull("community_name") ? "" : person
                        .getString("community_name");
                Contact_Results contactResult = new Contact_Results();
                contactResult.setContactName(mMemberName);
                contactResult.setContactEmail(mMemberEmail);
                contactResult.setContactPhone(mMemberPhoneNo);
                contactResult.setCommunityName(mCommunityName);
                contactResults.add(contactResult);
            }
            sGistLastInvited = new StringBuilder();
            String delim = "";
            for (int j = 0; j < contactResults.size(); j++) {
                if (TextUtils.isEmpty(contactResults.get(j).getContactName())) {
                    sGistLastInvited.append(delim + contactResults.get(j).getMCommunityName() + "(Group)");
                } else {
                    sGistLastInvited.append(delim + contactResults.get(j).getContactName());
                }
                delim = ",";
                if (j >= 2) {
                    sGistLastInvited.append(" ...");
                    break;
                }
            }
            String msg;
            if (TextUtils.isEmpty(sGistLastInvited)) {
                msg = "Cannot find a list of invitees from previous trip. Make sure you invite people to this trip";
                String sMsg1 = "Cannot find a list of invitees from previous trip.";
                String sMsg2 = "Make sure you invite people to this trip";
                showStatusMessages(sMsg1, sMsg2);
            } else {
                msg = "This will invite people you invited on your last trip";
                String sMsg1 = "Trip invitation (last invited) sent to";
                String sMsg2 = sGistLastInvited.toString();
                String sMsg3 = "No waiting passengers";
                showStatusMessages(sMsg1, sMsg2, sMsg3);
            }
            final ViewTimedDialog timedDialog = new ViewTimedDialog(this, "Quick trip",
                    msg,
                    "Do it!", "Cancel ", 10);
            timedDialog.show();
            timedDialog.setOnDialogListener(new ViewTimedDialog.DialogListener() {
                @Override
                public void onPositiveClick() {
                    try {
                        mIsDirectTrip = true;
                        goDirections();
                        timedDialog.dismissDialog();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNegativeClick() {
                    timedDialog.dismissDialog();
                }
            });

        } catch (JSONException e) {
            SSLog.e(TAG, "sendSMS", e);
            hidepDialog();
        } catch (Exception e) {
            SSLog.e(TAG, "sendSMS", e);
            hidepDialog();
        }
    }

    private void showpDialog() {
        try {
            if (!isFinishing()) {
                pDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hidepDialog() {
        try {
            if (pDialog != null) {
                if (pDialog.isShowing()) {
                    pDialog.cancel();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected String profileImageUrl() {
        return Constants_user.GET_USER_PROFILE_IMAGE_FILE_NAME_URL;
    }

    protected String passengertripsummayUrl() {
        return Constants_user.JUMP_IN_URL;
    }

    protected boolean isLoginflag() {
        return false;
    }

    public void onClickWalkWithMe(View view) {
        Intent intentWalkwithMe = new Intent(FriendPool_act.this,
                Act_WalkWithMe.class);
        startActivity(intentWalkwithMe);
    }

    protected void perfClear() {
    }

    protected boolean hasBusDriver() {
        return false;
    }

    protected boolean hasGetaCabTrip(){
        return false;
    }
}