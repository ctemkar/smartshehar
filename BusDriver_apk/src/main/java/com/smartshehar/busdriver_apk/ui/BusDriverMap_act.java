package com.smartshehar.busdriver_apk.ui;

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
import android.os.Bundle;
import android.provider.ContactsContract;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.CTrip;
import com.jumpinjumpout.apk.lib.Constants_lib;
import com.jumpinjumpout.apk.lib.PinInfo;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.lib.ui.Driver_act;
import com.smartshehar.busdriver_apk.CGlobal_bd;
import com.smartshehar.busdriver_apk.CSpeed;
import com.smartshehar.busdriver_apk.Constants_bd;
import com.smartshehar.busdriver_apk.LocationService_bus;
import com.smartshehar.busdriver_apk.MyApplication;
import com.smartshehar.busdriver_apk.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lib.app.util.MyLocation;

public class BusDriverMap_act extends Driver_act {
    protected static final String TAG = "BusDriverMap_act: ";
    public boolean expanded = false;
    private float offset1, offset2;
    ViewGroup fabContainer;
    protected FloatingActionButton mivInformation, mivZoomVehicle, famZoom;
    float mdlat, mdLon;
    int tripid;
    String lastUpdate;
    boolean isImage = false;
    CTrip cTripDriver;
    List<CTrip> mTrips;
    private ProgressDialog pDialog;
    ArrayList<CSpeed> aDriverSpeed = new ArrayList<CSpeed>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_floating_action_menu_busdriver);
        create();
        driverCreate();
        fabContainer = (ViewGroup) findViewById(R.id.fab_container);
        mivInformation = (FloatingActionButton) findViewById(R.id.ivInformation);
        famZoom = (FloatingActionButton) findViewById(R.id.famZoom);
        mivZoomVehicle = (FloatingActionButton) findViewById(R.id.ivZoomVehicle);
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

                offset2 = mivInformation.getY() - mivZoomVehicle.getY();
                mivZoomVehicle.setTranslationY(offset2);
                return true;
            }
        });
        mRlFromTo.setVisibility(View.GONE);
        mLlMarkerLayout.setVisibility(View.GONE);
        mIvRedPin.setVisibility(View.GONE);
        mIvGreenPin.setVisibility(View.GONE);
        mTvSetLocation.setVisibility(View.GONE);
        mButtonGo.setVisibility(View.GONE);
        mButtonStart.setVisibility(View.GONE);
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
        String sSaddress = CGlobals_lib.getInstance().getSharedPreferences(BusDriverMap_act.this).
                getString(Constants_lib.TRIP_FROM_ADDRESS, "");
        float fSLat = CGlobal_bd.getInstance().getSharedPreferences(BusDriverMap_act.this)
                .getFloat(Constants_bd.PREF_BUS_START_LAT, 0);
        float fSLon = CGlobal_bd.getInstance().getSharedPreferences(BusDriverMap_act.this)
                .getFloat(Constants_bd.PREF_BUS_START_LON, 0);
        String sDaddress = CGlobals_lib.getInstance().getSharedPreferences(BusDriverMap_act.this).
                getString(Constants_lib.TRIP_TO_ADDRESS, "");
        float fDLat = CGlobal_bd.getInstance().getSharedPreferences(BusDriverMap_act.this)
                .getFloat(Constants_bd.PREF_BUS_DESTINATION_LAT, 0);
        float fDLon = CGlobal_bd.getInstance().getSharedPreferences(BusDriverMap_act.this)
                .getFloat(Constants_bd.PREF_BUS_DESTINATION_LON, 0);
        moFromAddress.setAddress(sSaddress);
        moFromAddress.setLatitude(fSLat);
        moFromAddress.setLongitude(fSLon);
        moToAddress.setAddress(sDaddress);
        moToAddress.setLatitude(fDLat);
        moToAddress.setLongitude(fDLon);
        mTvFrom.setText(moFromAddress.getAddress());
        mTvTo.setText(moToAddress.getAddress());
        pDialog = new ProgressDialog(BusDriverMap_act.this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        mIsDirectTrip = true;
        goDirections();
    }

    public void collapseFab() {
        famZoom.setImageResource(com.jumpinjumpout.apk.lib.R.mipmap.ic_openfloat);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(createCollapseAnimator(mivInformation, offset1));
        animatorSet.playTogether(createCollapseAnimator(mivZoomVehicle, offset2));
        animatorSet.start();
        animateFab();
    }

    public void expandFab() {
        famZoom.setImageResource(com.jumpinjumpout.apk.lib.R.mipmap.ic_close);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(createExpandAnimator(mivInformation, offset1));
        animatorSet.playTogether(createExpandAnimator(mivZoomVehicle, offset2));
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
            mivInformation.setVisibility(View.VISIBLE);
            mivZoomVehicle.setVisibility(View.GONE);
        } else {
            mtvJoinedCountNumber.setVisibility(View.GONE);
            mTvJoinedCount.setVisibility(View.GONE);
            mTvJumpedInCount.setVisibility(View.GONE);
            mivInformation.setVisibility(View.GONE);
            mivZoomVehicle.setVisibility(View.GONE);
        }
    }

    protected void setTripActionFloatingbuttonHide() {
        mivInformation.setVisibility(View.GONE);
        mivZoomVehicle.setVisibility(View.GONE);
    }

    @Override
    protected void perfClear() {
    }

    MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            CGlobal_bd.getInstance().setMyLocation(location, false, BusDriverMap_act.this);
            /*if (location != null
                    && (moFromAddress.hasLatitude() && moFromAddress
                    .hasLongitude())
                    && isInTrip()) {
                float results[] = new float[1];
                Location.distanceBetween(moToAddress.getLatitude(),
                        moToAddress.getLongitude(), location.getLatitude(),
                        location.getLongitude(), results);
            }*/
            CGlobal_bd.getInstance().sendUpdatePosition(getApplicationContext());
        }
    };

    @Override
    public void onClickInviteUsers(View v) {

    }

    protected void showActivePassengerMarkers(String result) {


        double nearestLat = Constants_lib.INVALIDLAT, nearestLng = Constants_lib.INVALIDLNG;
        try {
            int iHaveJoined = 0, iHaveJumpedIn = 0;
            if (!isInTrip()) {
                setTripAction(Constants_bd.TRIP_END);
                resetDisplay();
            }
            if (result.trim().equals("0")) {
                String sMsg = "No waiting passengers";
                showStatusMessages(sMsg);
                SSLog.d(TAG, "No propsective passengers");
                resetDisplay();
                return;
            } else {
                // save all passenger data every time update to server
                CGlobals_lib.getInstance().getSharedPreferencesEditor(BusDriverMap_act.this)
                        .putString(Constants_lib.PREF_SAVE_TRIP_CREATE_RESPONSE, result);
                CGlobals_lib.getInstance().getSharedPreferencesEditor(BusDriverMap_act.this).commit();
            }
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
                        .toString(), getApplicationContext());
                mTrips.add(cTripDriver);

                int nInPath = jPassenger.isNull(Constants_bd.COL_IN_PATH) ? 0
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
                            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                            Uri.encode(sPhoneNo));

                    Cursor cur = getContentResolver().query(uri,
                            new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null,
                            null, null);
                    cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                    cur.getColumnIndex(ContactsContract.PhoneLookup.TYPE);
                    final int idIndex = cur
                            .getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);

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
                               /* if (!TextUtils.isEmpty(cTripDriver.getUserProfileImageFileName()) && !TextUtils.isEmpty(cTripDriver.getUserProfileImagePath())) {

                                    String url = Constants_bd.GET_USER_PROFILE_IMAGE_FILE_NAME_URL + cTripDriver.getUserProfileImagePath() +
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
                                                        mIvPhoto.setRotation(cTripDriver.getMiImageRotation());
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
                                }*/
                                if (!isImage) {
                                    Bitmap bitmap = cTripDriver.getContactThnumbnail();
                                    if (bitmap != null) {
                                        mIvPhoto.setImageBitmap(bitmap);
                                        mIvPhoto.setVisibility(View.VISIBLE);
                                    }
                                    /*CGlobals_lib.getInstance().getSharedPreferencesEditor(BusDriverMap_act.this).
                                            putString(Constants_bd.PREF_CURRENT_PASSENGER_IMAGEURL, "");
                                    CGlobals_lib.getInstance().getSharedPreferencesEditor(BusDriverMap_act.this).commit();*/
                                }
                                mIvPhoto.setVisibility(View.VISIBLE);
                                mTvKmAway.setVisibility(View.VISIBLE); // 2015-06-20 16:31:44
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date date = new Date();
                                System.out.println(dateFormat.format(date));
                                addSpeed(mCurrentLocation.getSpeed(), dateFormat.format(date));

                                MyApplication.getInstance().getPersistentPreferenceEditor().
                                        putString(Constants_bd.PREF_CURRENT_PASSENGER_PHONENO, cTripDriver.getPhoneNo());
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
    }

    protected boolean hasBusDriver() {
        return true;
    }


    @Override
    protected void onResume() {
        tripid = CGlobal_bd.getInstance().getSharedPreferences(BusDriverMap_act.this)
                .getInt(Constants_bd.PREF_BUS_DRIVER_TRIP_ID, -1);
        setupImageButtons();
        MyLocation myLocation = new MyLocation(
                MyApplication.mVolleyRequestQueue, BusDriverMap_act.this, mGoogleApiClient);
        myLocation.getLocation(this, locationResult);
        CGlobals_lib.getInstance().turnGPSOn1(BusDriverMap_act.this, mGoogleApiClient);
        CGlobal_bd.getInstance().sendUpdatePosition(getApplicationContext());
        if (isInTrip()) {
            if (!isLocationServiceRunning(LocationService_bus.class)) {
                StartLocationService();
            }
            String response =  CGlobals_lib.getInstance().getSharedPreferences(BusDriverMap_act.this)
                    .getString(Constants_lib.PREF_SAVE_TRIP_CREATE_RESPONSE, "");
            showActivePassengerMarkers(response);
        }
        super.onResume();
        mButtonStart.setVisibility(View.GONE);
        if (isInTrip()) {
            mivInformation.setVisibility(View.VISIBLE);
        } else {
            mivInformation.setVisibility(View.GONE);
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
        CGlobals_lib.getInstance().getSharedPreferencesEditor(BusDriverMap_act.this)
                .putString(Constants_lib.PREF_SAVE_TRIP_CREATE_RESPONSE, "");
        CGlobals_lib.getInstance().getSharedPreferencesEditor(BusDriverMap_act.this).commit();
    }

    @Override
    protected String getGeoCountryCode() {
        return MyApplication.getInstance().getPreference()
                .getString(Constants_bd.PREF_CURRENT_COUNTRY, "");
    }

    public void driverTripSummary() {
    }

    protected void StartLocationService() {
        stopService(new Intent(this, LocationService_bus.class));
        Intent serviceIntent = new Intent(this, LocationService_bus.class);
        serviceIntent.putExtra("lat", moToAddress.getLatitude());
        serviceIntent.putExtra("lng", moToAddress.getLongitude());
        serviceIntent.putExtra("tripid", getTripId());
        startService(serviceIntent);
    }

    protected void StopLocationService() {
        stopService(new Intent(this, LocationService_bus.class));
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


    protected void sendTripAction(String sTripAction) {
        CGlobal_bd.getInstance().sendTripStatus(sTripAction, BusDriverMap_act.this);
    }

    @Override
    protected void inviteUsers() {

    }

    public void getInformation() {

        String sToAddress = CGlobals_lib.getInstance().getSharedPreferences(getApplicationContext()).
                getString(Constants_lib.TRIP_TO_ADDRESS, "");
        String sFromAddress = CGlobals_lib.getInstance().getSharedPreferences(getApplicationContext())
                .getString(Constants_lib.TRIP_FROM_ADDRESS, "");

        final Dialog dialog = new Dialog(BusDriverMap_act.this, R.style.DIALOG);
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
        mimsName.setVisibility(View.GONE);
        miVehicleType.setVisibility(View.GONE);
        dialog.show();
    }


    protected String getTripUrl() {
        return Constants_bd.TRIP_CREATE_DRIVER_URL;
    }

    protected String getTripPathUrl() {
        return Constants_bd.TRIP_CREATE_TRIP_PATH_DRIVER_URL;
    }

    protected int getAppUserId() {
        return CGlobal_bd.getInstance().getAppUserId();
    }

    protected String getTripType() {
        return Constants_bd.TRIP_TYPE;
    }

    @Override
    protected String profileImageUrl() {
        return null;
    }

    @Override
    protected String passengertripsummayUrl() {
        return null;
    }

    protected String getEmailId() {
        return null;
    }

    public void showThumbImageClick() {
        try {
            final String phoneNo = MyApplication.getInstance().getPersistentPreference().

                    getString(Constants_bd.PREF_CURRENT_PASSENGER_PHONENO, "");
            String sptrip = CGlobals_lib.getInstance().getSharedPreferences(getApplicationContext()).
                    getString(Constants_lib.TRIP_TO_ADDRESS, "");
            final Dialog dialog = new Dialog(BusDriverMap_act.this, R.style.DIALOG);
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
                /*if (!TextUtils.isEmpty(url)) {

                    ImageRequest request = new ImageRequest(url,
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap bitmap) {
                                    if (Build.VERSION.SDK_INT < 11) {
                                        Toast.makeText(BusDriverMap_act.this, getString(R.string.api11not), Toast.LENGTH_LONG).show();
                                    } else {
                                        mProImage.setRotation(cTripDriver.getMiImageRotation());
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
                }*/

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
                                Toast.makeText(BusDriverMap_act.this, "Your sms has failed...",
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
        String text = "About my SmartShehar Bus Driver trip...";
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
        if (CGlobals_lib.getInstance().getMyLocation(BusDriverMap_act.this) != null) {
            LatLng latlng = new LatLng(CGlobals_lib.getInstance().getMyLocation(BusDriverMap_act.this).getLatitude(),
                    CGlobals_lib.getInstance().getMyLocation(BusDriverMap_act.this).getLongitude());

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
                    } else if (results[0] < Constants_bd.NEAR_BE_READY_DISTANCE) {
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
        CGlobals_lib.getInstance().getSharedPreferencesEditor(BusDriverMap_act.this)
                .putString(Constants_lib.PREF_SAVE_TRIP_CREATE_RESPONSE, "");
        CGlobals_lib.getInstance().getSharedPreferencesEditor(BusDriverMap_act.this).commit();
        setTripAction(Constants_lib.TRIP_ACTION_BEGIN);
        CGlobal_bd.getInstance().sendTripStatus(Constants_bd.TRIP_BEGIN, BusDriverMap_act.this);
        StartLocationService();
        setupImageButtons();
        progressCancel();
        MyApplication.getInstance().getPersistentPreferenceEditor().putBoolean(
                Constants_bd.PREF_IN_TRIP, true);
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
                    mTvMins.setText("secs");
                } else {
                    eTA = eTA1;
                    mTvMins.setText("mins");
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
        if (!isLocationServiceRunning(LocationService_bus.class)) {
            endTrip();
            finish();
        } else {
            Toast.makeText(this, "Presss home to switch to a different app",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
