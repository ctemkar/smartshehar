package com.jumpinjumpout.apk.user.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.PolyUtil;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.CTrip;
import com.jumpinjumpout.apk.lib.Constants_lib;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.lib.ui.AbstractMapFragment_act;
import com.jumpinjumpout.apk.user.CGlobals_user;
import com.jumpinjumpout.apk.user.CNotification;
import com.jumpinjumpout.apk.user.Constants_user;
import com.jumpinjumpout.apk.user.MyApplication;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lib.app.util.Connectivity;
import lib.app.util.MyLocation;

public abstract class TrackDriver_act extends AbstractMapFragment_act {
    protected static final String TAG = "TrackDriver: ";
    boolean isImage = false;
    CTrip moTrip;
    private int BOUNDS_PADDING = 50;
    Marker driverCurrentLocationMarker;
    private Handler handler = new Handler();
    private LatLng mDriverLatLng = null;
    String sSpeed = "", sDistance = "", msLastLocationTime = "",
            msLastActive = "";
    private int miTripId;
    private boolean isGettingTrip = false;
    Button mButtonJoin, mButtonCancel, mButtonJumpIn, mButtonJumpOut, mButtonShowTicket;
    TextView mTvTripAction;
    boolean hasJoinedTrip = false, hasTripEnded = false;
    private Circle myCircle;
    private long miDelayDriverPosition;
    private boolean mHasDriverMoved = false;
    private int isInJumpinMode = -1;
    private Handler handlerUpdateAddress = new Handler();
    private ArrayList<CNotification> maCNotification;
    public ImageView mIvPhoto;
    public LinearLayout changeColor;
    public TextView mTvInfoBandLine2, mTvInfoBandLine1;
    public ImageView mTvInfoBandLine3;
    public TextView mTvInfoBandLine4, mTvNoPassenger, mTvtrip;
    public LinearLayout mLlPassengerInfo, mLlPassenger;
    long lTimeDiff, lTimeDifflocation;
    String sLastActive = "";
    int msPassed = 0;
    ArrayList<CSpeed> aDriverSpeed = new ArrayList<CSpeed>();
    boolean showETA = false, showingETA = false;
    double dDistanceFromMe = -1;
    public TextView mTvMins, mTvTime, mTvkmph, mTvStatusAction, textkmAway;
    GoogleApiClient googleApiClient = null;
    String sNotificationValue = "0";
    private List<Address> addresses;
    protected Handler handlerJoinAddress = new Handler();
    private int joinAddressValue;
    Connectivity mConnectivity;
    private boolean israting = false;
    String comment = "";
    public boolean expanded = false;
    private float offset1, offset3, offset4, offset5, offset6, offset7, offset8;
    ViewGroup fabContainer;
    protected FloatingActionButton flZoomActiveUsers;
    protected FloatingActionButton mivZoomFit,
            mivInformation, mivZoomVehicle, mivTwitter, mivFacebook, famZoom, ivwalkwithme;
    private boolean hasDestination = false;
    protected final int NOTIRESULT = 10;
    String msTripType;
    private double bFromLat = Constants_user.INVALIDLAT, bFromLng = Constants_user.INVALIDLNG,
            bToLat = Constants_user.INVALIDLAT, bToLng = Constants_user.INVALIDLNG;
    private boolean isTicket = false;
    String msTicketId = "";
    double msTicketFare = 0;
    float dist;
    boolean getacabValue = false;
    boolean joinCancel = false;
    FrameLayout flTrackDriver, myLayout, flDriverInfo;
    String distanceFromMe = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_floating_action_menu_teackdriver);
        create();
        fabContainer = (ViewGroup) findViewById(R.id.fab_container);
        mivZoomFit = (FloatingActionButton) findViewById(R.id.ivZoomFit);
        mivInformation = (FloatingActionButton) findViewById(R.id.ivInformation);
        famZoom = (FloatingActionButton) findViewById(R.id.famZoom);
        mivTwitter = (FloatingActionButton) findViewById(R.id.ivTwitter);
        mivFacebook = (FloatingActionButton) findViewById(R.id.ivFacebook);
        flZoomActiveUsers = (FloatingActionButton) findViewById(R.id.ivZoomActiveUsers);
        mivZoomVehicle = (FloatingActionButton) findViewById(R.id.ivZoomVehicle);
        ivwalkwithme = (FloatingActionButton) findViewById(R.id.ivwalkwithme);
        mConnectivity = new Connectivity();
        hasJoinedTrip = MyApplication.getInstance().getPersistentPreference()
                .getBoolean(Constants_user.PREF_JOINED_TRIP, false);
        SetupUI();
        googleApiClient = new GoogleApiClient.Builder(TrackDriver_act.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(TrackDriver_act.this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
        flTrackDriver = (FrameLayout) findViewById(R.id.flTrackDriver);
        if (flTrackDriver == null) {
            myLayout = (FrameLayout) findViewById(R.id.flCustom1);
            View hiddenInfo = getLayoutInflater().inflate(
                    R.layout.track_driver_act, myLayout, false);
            myLayout.addView(hiddenInfo);
            getacabValue = MyApplication.getInstance().getPersistentPreference().
                    getBoolean(Constants_user.GET_A_CAB_VALUE, false);
            if (getacabValue) {
                flDriverInfo = (FrameLayout) findViewById(R.id.flPassengerInfo);
                View hiddenPassengerInfo = getLayoutInflater().inflate(
                        R.layout.getacabdriverdetails, flDriverInfo, false);
                flDriverInfo.addView(hiddenPassengerInfo);
                flDriverInfo.setVisibility(View.VISIBLE);
            } else {
                flDriverInfo = (FrameLayout) findViewById(R.id.flPassengerInfo);
                View hiddenPassengerInfo = getLayoutInflater().inflate(
                        R.layout.driverdetails, flDriverInfo, false);
                flDriverInfo.addView(hiddenPassengerInfo);
                flDriverInfo.setVisibility(View.VISIBLE);
                mIvPhoto = (ImageView) findViewById(R.id.ivPhoto);
                changeColor = (LinearLayout) findViewById(R.id.llWaitingPassenger);
                mTvInfoBandLine1 = (TextView) findViewById(R.id.tvInfoBandLine1);
                mTvInfoBandLine2 = (TextView) findViewById(R.id.tvInfoBandLine2);
                mTvInfoBandLine3 = (ImageView) findViewById(R.id.tvInfoBandLine3);
                mTvInfoBandLine4 = (TextView) findViewById(R.id.tvInfoBandLine4);
                mTvNoPassenger = (TextView) findViewById(R.id.tvNoPassenger);
                mLlPassengerInfo = (LinearLayout) findViewById(R.id.llPassengerInfo);
                mLlPassenger = (LinearLayout) findViewById(R.id.llPassenger);
                mTvTripAction = (TextView) findViewById(R.id.tvTripAction);
                mTvMins = (TextView) findViewById(R.id.tvMins);
                mTvTime = (TextView) findViewById(R.id.tvTime);
                textkmAway = (TextView) findViewById(R.id.textkmAway);
                mTvkmph = (TextView) findViewById(R.id.tvkmph);
                mTvStatusAction = (TextView) findViewById(R.id.tvStatusAction);
                mTvtrip = (TextView) findViewById(R.id.tvtrip);
                changeColor.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showThumbImageClick();
                    }
                });
            }
        }

        mButtonJoin = (Button) findViewById(R.id.btnJoin);
        mButtonCancel = (Button) findViewById(R.id.btnCancel);
        mButtonJumpIn = (Button) findViewById(R.id.btnJumpIn);
        mButtonJumpOut = (Button) findViewById(R.id.btnJumpOut);
        mButtonShowTicket = (Button) findViewById(R.id.btnShowTicket);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            msPassed = MyApplication.getInstance().getPersistentPreference().
                    getInt(Constants_user.PREF_TRIP_PASSOUT, 0);
            hasDestination = MyApplication.getInstance().getPersistentPreference().
                    getBoolean(Constants_user.HAS_DESTINATION, false);
            msTripType = extras.getString("notificationTripType");
        }
        if (hasDestination) {
            bFromLat = MyApplication.getInstance().getPersistentPreference().getFloat("FROMLAT", 0);
            bFromLng = MyApplication.getInstance().getPersistentPreference().getFloat("FROMLNG", 0);
            bToLat = MyApplication.getInstance().getPersistentPreference().getFloat("TOLAT", 0);
            bToLng = MyApplication.getInstance().getPersistentPreference().getFloat("TOLNG", 0);
        }
        mButtonJoin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    float results[] = new float[1];
                    if (!connectionError()) {
                        SSLog.d(TAG, "Join");

                        if (CGlobals_user.getInstance().getMyLocation(TrackDriver_act.this) != null) {
                            LatLng latlng = new LatLng(CGlobals_user.getInstance().getMyLocation(TrackDriver_act.this).getLatitude(),
                                    CGlobals_user.getInstance().getMyLocation(TrackDriver_act.this).getLongitude());
                            if (moTrip.getLatLng() != null && latlng != null) {
                                Location.distanceBetween(latlng.latitude,
                                        latlng.longitude, moTrip.getToLat(),
                                        moTrip.getToLng(), results);
                            }
                        }
                        if (TextUtils.isEmpty(moTrip.getMsTripStatusTrack())) {
                            Toast.makeText(getApplicationContext(), "Trip is no longer available, you cannot join it", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (hasTripEnded) {
                            Toast.makeText(getApplicationContext(),
                                    "Trip has ended, you cannot join it",
                                    Toast.LENGTH_SHORT).show();
                        } else if (moTrip.getMsTripStatusTrack().equals(Constants_user.TRIP_ACTION_PAUSE)) {
                            Toast.makeText(getApplicationContext(), "Sharing paused, " + getString(R.string.pleaseTryLater), Toast.LENGTH_LONG).show();

                        } else if (moTrip.getMiPassed() > 0) {
                            Toast.makeText(getApplicationContext(), "Driver has passed you,\nYou cannot join this trip at this time", Toast.LENGTH_LONG).show();
                        } else if (CGlobals_user.getInstance().getMyLocation(TrackDriver_act.this) == null) {
                            Toast.makeText(getApplicationContext(),
                                    "Please turn location on to join trip",
                                    Toast.LENGTH_SHORT).show();
                        } else if (mLatLngTripPath == null) {
                            Toast.makeText(getApplicationContext(),
                                    "Path not available", Toast.LENGTH_SHORT)
                                    .show();
                        } else if (!PolyUtil
                                .isLocationOnPath(
                                        new LatLng(CGlobals_user.getInstance()
                                                .getMyLocation(TrackDriver_act.this).getLatitude(),
                                                CGlobals_user.getInstance()
                                                        .getMyLocation(TrackDriver_act.this)
                                                        .getLongitude()),
                                        mLatLngTripPath,
                                        true,
                                        (double) (Constants_user.DEFAULT_WALKING_DISTANCE * 1000.0))) {
                            Toast.makeText(getApplicationContext(),
                                    "You are too far from this trip",
                                    Toast.LENGTH_SHORT).show();
                        } else if (!hasDestination) {
                            getHasDestinationCheck();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Sharer informed that you have joined",
                                    Toast.LENGTH_SHORT).show();
                            mButtonJoin.setVisibility(View.GONE);
                            mButtonCancel.setVisibility(View.VISIBLE);
                            new JoinTripTask(1).execute();
                            joinAddressValue = 1;
                            handlerJoinAddress.postDelayed(runnableUpdatePosition,
                                    Constants_user.DRIVER_UPDATE_INTERVAL);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mButtonCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                SSLog.d(TAG, "Back out of trip");
                setStatus(R.string.trackNotify, 1);
                cancelJoin();
                joinCancel = false;

            }
        });
        miTripId = MyApplication.getInstance().getPersistentPreference()
                .getInt(Constants_user.PREF_TRIP_ID, -2);
        getTrip();
        isTripFrozen = true;
        init();
        handler.postDelayed(runnableDriverPosition, 1);
        miDelayDriverPosition = Constants_user.DRIVER_UPDATE_INTERVAL;
        hideViews();
        mRlFromTo.setVisibility(View.GONE);
        if (hasJoinedTrip) {
            mButtonJoin.setVisibility(View.GONE);
            mButtonCancel.setVisibility(View.VISIBLE);
        }
        famZoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expanded = !expanded;
                if (expanded) {
                    expandFab();
                    mivTwitter.setVisibility(View.VISIBLE);
                    mivFacebook.setVisibility(View.VISIBLE);
                    ivwalkwithme.setVisibility(View.VISIBLE);
                } else {
                    collapseFab();
                    mivTwitter.setVisibility(View.GONE);
                    mivFacebook.setVisibility(View.GONE);
                    ivwalkwithme.setVisibility(View.GONE);
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

                offset6 = famZoom.getY() - mivFacebook.getY();
                mivFacebook.setTranslationY(offset6);

                offset7 = famZoom.getY() - mivTwitter.getY();
                mivTwitter.setTranslationY(offset7);

                offset8 = famZoom.getY() - ivwalkwithme.getY();
                ivwalkwithme.setTranslationY(offset8);
                return true;
            }
        });

        mivTwitter.setVisibility(View.GONE);
        mivFacebook.setVisibility(View.GONE);
        ivwalkwithme.setVisibility(View.GONE);
        CGlobals_lib.showGPSDialog = true;
        MyApplication.getInstance().getPersistentPreferenceEditor().putBoolean(Constants_user.PREF_NOTIFICATION_CLEAR_FLAG, true);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
    } // onCreate

    private void getHasDestinationCheck() {
        String json = new Gson().toJson(mLatLngTripPath);
        MyApplication.getInstance().getPersistentPreferenceEditor()
                .putString("LATLNG_TRIP_PATH", json);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
        Intent intent = new Intent(TrackDriver_act.this, NotificationDistance_act.class);
        startActivityForResult(intent, NOTIRESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NOTIRESULT) {
            if (resultCode == RESULT_OK) {
                bFromLat = data.getDoubleExtra("FROMLAT", Constants_user.INVALIDLAT);
                bFromLng = data.getDoubleExtra("FROMLNG", Constants_user.INVALIDLNG);
                bToLat = data.getDoubleExtra("TOLAT", Constants_user.INVALIDLAT);
                bToLng = data.getDoubleExtra("TOLNG", Constants_user.INVALIDLNG);
                Toast.makeText(getApplicationContext(),
                        "Sharer informed that you have joined",
                        Toast.LENGTH_SHORT).show();
                mButtonJoin.setVisibility(View.GONE);
                mButtonCancel.setVisibility(View.VISIBLE);
                new JoinTripTask(1).execute();
                joinAddressValue = 1;
                handlerJoinAddress.postDelayed(runnableUpdatePosition,
                        Constants_user.DRIVER_UPDATE_INTERVAL);
                joinCancel = true;
            }
            if (resultCode == RESULT_CANCELED) {
                joinCancel = false;
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
    }

    public void collapseFab() {
        famZoom.setImageResource(com.jumpinjumpout.apk.lib.R.mipmap.ic_openfloat);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(createCollapseAnimator(mivInformation, offset1));
        animatorSet.playTogether(createCollapseAnimator(flZoomActiveUsers, offset3));
        animatorSet.playTogether(createCollapseAnimator(mivZoomFit, offset4));
        animatorSet.playTogether(createCollapseAnimator(mivZoomVehicle, offset5));
        animatorSet.playTogether(createCollapseAnimator(mivFacebook, offset6));
        animatorSet.playTogether(createCollapseAnimator(mivTwitter, offset7));
        animatorSet.playTogether(createCollapseAnimator(ivwalkwithme, offset8));
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
        animatorSet.playTogether(createExpandAnimator(mivFacebook, offset6));
        animatorSet.playTogether(createExpandAnimator(mivTwitter, offset7));
        animatorSet.playTogether(createExpandAnimator(ivwalkwithme, offset8));
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

    private void driverInfomation() {
        String sDrive = MyApplication.getInstance().getPersistentPreference()
                .getString(Constants_user.CURRENT_TRACKED_TRIP, "");
        moTrip = new CTrip(sDrive, TrackDriver_act.this);
        final Dialog dialog = new Dialog(TrackDriver_act.this, R.style.DIALOG);
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

        misLastActive.setText(Html.fromHtml("<font color=\"red\"><strong>Trip Information</strong></font>"));
        misLastActive.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(moTrip.getFrom().trim())) {
            mimsFrom.setText("Start address: " + moTrip.getFrom());
            mimsFrom.setVisibility(View.VISIBLE);
        } else {
            mimsFrom.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(moTrip.getTo().trim())) {
            miTvDescription.setText(Html.fromHtml("Destination: " + moTrip.getTo()));
            miTvDescription.setVisibility(View.VISIBLE);
        } else {
            miTvDescription.setVisibility(View.GONE);
        }

        if (CGlobals_user.getInstance().getMyLocation(TrackDriver_act.this) != null) {
            LatLng latlng = new LatLng(CGlobals_user.getInstance().getMyLocation(TrackDriver_act.this).getLatitude(),
                    CGlobals_user.getInstance().getMyLocation(TrackDriver_act.this).getLongitude());
            String distanceFromMe = "";
            if (moTrip.getLatLng() != null && latlng != null) {
                float results[] = new float[1];
                Location.distanceBetween(moTrip.getLatLng().latitude,
                        moTrip.getLatLng().longitude, latlng.latitude,
                        latlng.longitude, results);
                try {
                    if (results[0] > 20) {
                        distanceFromMe = "About " + String.format("%.2f", results[0] / 1000) + " km. from you";
                    } else {
                        distanceFromMe = "Near you";
                    }
                } catch (Exception e) {
                    SSLog.e(TAG, "getView", e);
                }
            }

            if (!TextUtils.isEmpty(distanceFromMe.trim())) {
                miTvDriverDistance.setText(distanceFromMe);
                miTvDriverDistance.setVisibility(View.VISIBLE);
            } else {
                miTvDriverDistance.setVisibility(View.GONE);
            }
        }
        if (!TextUtils.isEmpty(moTrip.getStartLandmark().trim())) {
            miTvStartLandmark.setText(moTrip.getStartLandmark());
            miTvStartLandmark.setVisibility(View.VISIBLE);
        } else {
            miTvStartLandmark.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(moTrip.getTripNotes().trim())) {
            miTvTripNote.setText(moTrip.getTripNotes());
            miTvTripNote.setVisibility(View.VISIBLE);
        } else {
            miTvTripNote.setVisibility(View.GONE);
        }
        mimsName.setText(Html.fromHtml("<font color=\"red\"><strong>Vehicle Information</strong></font>"));
        mimsName.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(moTrip.getVehicleType().trim())) {
            miVehicleType.setText("Vehicle: " + moTrip.getVehicleColor() + " " + moTrip.getVehicleCompany() + " " +
                    moTrip.getVehicleNo() + " " + moTrip.getVehicleType());
            miVehicleType.setVisibility(View.VISIBLE);
        } else {
            miVehicleType.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(moTrip.getPhoneNo().trim())) {
            mimsPhoneno.setText("Driver Phone No: " + moTrip.getPhoneNo());
            mimsPhoneno.setVisibility(View.VISIBLE);
        } else {
            mimsPhoneno.setVisibility(View.GONE);
        }
        dialog.show();
    }

    private void populateTable(final String response) {
        runOnUiThread(new Runnable() {
            public void run() {
                ratingcomment(response);
            }
        });
    }

    private void ratingcomment(final String response) {
        try {
            israting = true;
            String sDrive = MyApplication.getInstance().getPersistentPreference()
                    .getString(Constants_user.CURRENT_TRACKED_TRIP, "");
            moTrip = new CTrip(sDrive, TrackDriver_act.this);
            final Dialog dialog = new Dialog(TrackDriver_act.this, R.style.DIALOG);
            dialog.setContentView(R.layout.rating_comment_dialog);
            dialog.setTitle(moTrip.getName());
            final RatingBar ratingBar1;
            final Button submit;
            final ImageView mProImage;
            ratingBar1 = (RatingBar) dialog.findViewById(R.id.ratingBar1);
            submit = (Button) dialog.findViewById(R.id.submit);
            mProImage = (ImageView) dialog.findViewById(R.id.ivPassengerImage1);
            float ratingber = MyApplication.getInstance().getPersistentPreference().getFloat("RATING", 0.0f);
            ratingBar1.setRating(ratingber);
            ratingBar1.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                public void onRatingChanged(RatingBar ratingBar, float rating,
                                            boolean fromUser) {
                    if (rating <= 3.0f) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(TrackDriver_act.this);
                        alertDialog.setTitle("Comment");
                        alertDialog.setMessage("Enter your comment");
                        final EditText input = new EditText(TrackDriver_act.this);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT);
                        input.setLayoutParams(lp);
                        alertDialog.setView(input);
                        alertDialog.setIcon(R.drawable.ic_comment);
                        alertDialog.setPositiveButton("YES",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        comment = input.getText().toString();
                                    }
                                });
                        alertDialog.setNegativeButton("NO",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                        if (!TrackDriver_act.this.isFinishing()) {
                            alertDialog.show();
                        }
                    } else {

                    }
                    MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("RATING", rating);
                    MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                }
            });

            if (!TextUtils.isEmpty(moTrip.getUserProfileImageFileName()) && !TextUtils.isEmpty(moTrip.getUserProfileImagePath())) {

                String url = Constants_user.GET_USER_PROFILE_IMAGE_FILE_NAME_URL + moTrip.getUserProfileImagePath() +
                        moTrip.getUserProfileImageFileName();
                ImageRequest request = new ImageRequest(url,
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap bitmap) {
                                if (Build.VERSION.SDK_INT < 11) {
                                    Toast.makeText(TrackDriver_act.this, getString(R.string.api11not), Toast.LENGTH_LONG).show();
                                    mProImage.setImageBitmap(bitmap);
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
                Bitmap bitmap = moTrip.getContactThnumbnail();
                if (bitmap != null) {
                    mProImage.setImageBitmap(bitmap);
                }
            }
            submit.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    float ratingber = MyApplication.getInstance().getPersistentPreference().getFloat("RATING", 0.0f);
                    sendratingcomment(comment, ratingber);
                    dialog.getContext();
                    dialog.cancel();
                }
            });
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            israting = false;
        }

    }

    private void sendratingcomment(final String comment, final float ratingber) {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.UPDATE_RATING_COMMENT_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response: UPDATE_RATING_COMMENT_URL - " + response.toString());
                Toast.makeText(TrackDriver_act.this, "Rating successful", Toast.LENGTH_LONG).show();
                israting = true;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    SSLog.e(TAG, " sendratingcomment: ", error);
                } catch (Exception e) {
                    SSLog.e(TAG, "sendratingcomment - ", e);
                }
                MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("RATING", 0.0f);
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                israting = false;
                Toast.makeText(TrackDriver_act.this, "Rating Failed", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                if (!TextUtils.isEmpty(comment)) {
                    params.put("comment", comment);
                }
                params.put("rating", String.valueOf(ratingber));
                params.put("tripid", Integer.toString(miTripId));
                params = CGlobals_user.getInstance().getAllMobileParams(params,
                        Constants_user.UPDATE_RATING_COMMENT_URL, TrackDriver_act.this);
                return CGlobals_user.getInstance().checkParams(params);
            }

        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    } // sendratingcomment

    private void showThumbImageClick() {
        try {
            String sDrive = MyApplication.getInstance().getPersistentPreference()
                    .getString(Constants_user.CURRENT_TRACKED_TRIP, "");
            moTrip = new CTrip(sDrive, TrackDriver_act.this);
            final Dialog dialog = new Dialog(TrackDriver_act.this, R.style.DIALOG);
            dialog.setContentView(R.layout.passengerinfo_image);
            dialog.setTitle(moTrip.getName());

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

            if (lTimeDiff < 1) {
                miinternetlogo.setBackgroundResource(R.drawable.btn_online);
                miinternet.setText("Online");
                miinternetlogo.setVisibility(View.VISIBLE);
                miinternet.setVisibility(View.VISIBLE);
            } else {
                miinternetlogo.setBackgroundResource(R.drawable.btn_offline);
                miinternet.setText("Last online " + lTimeDiff + " min. ago");
                miinternetlogo.setVisibility(View.VISIBLE);
                miinternet.setVisibility(View.VISIBLE);
            }
            if (lTimeDifflocation < 30) {
                milocationlogo.setBackgroundResource(R.drawable.btn_online);
                milocation.setText("Current location available");
                milocationlogo.setVisibility(View.VISIBLE);
                milocation.setVisibility(View.VISIBLE);
            } else {
                milocationlogo.setBackgroundResource(R.drawable.btn_offline);
                if (lTimeDifflocation < 60) {
                    milocation.setText("Location delayed " + lTimeDifflocation + " secs. ago");
                } else {
                    milocation.setText("Location delayed " + lTimeDifflocation / 60 + " min. ago");
                }
                milocationlogo.setVisibility(View.VISIBLE);
                milocation.setVisibility(View.VISIBLE);
            }
            mitoadd.setText(Html.fromHtml(moTrip.getPhoneNo()));
            mitoadd.setVisibility(View.GONE);
            mifromadd.setVisibility(View.GONE);
            mitripprogess.setText(Html.fromHtml(sDistance + " Km away"));
            mitripprogess.setVisibility(View.VISIBLE);


            if (!TextUtils.isEmpty(moTrip.getUserProfileImageFileName()) && !TextUtils.isEmpty(moTrip.getUserProfileImagePath())) {

                String url = Constants_user.GET_USER_PROFILE_IMAGE_FILE_NAME_URL + moTrip.getUserProfileImagePath() +
                        moTrip.getUserProfileImageFileName();
                ImageRequest request = new ImageRequest(url,
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap bitmap) {
                                if (Build.VERSION.SDK_INT < 11) {
                                    Toast.makeText(TrackDriver_act.this, getString(R.string.api11not), Toast.LENGTH_LONG).show();
                                    mProImage.setImageBitmap(bitmap);
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
                Bitmap bitmap = moTrip.getContactThnumbnail();
                if (bitmap != null) {
                    mProImage.setImageBitmap(bitmap);
                }
            }

            mCall.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + mitoadd.getText().toString()));
                    startActivity(callIntent);
                }
            });
            mWhatsApp.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendAppMsg(mitoadd.getText().toString());
                }
            });
            mSMS.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent smsVIntent = new Intent(Intent.ACTION_VIEW);
                    smsVIntent.setType("vnd.android-dir/mms-sms");
                    smsVIntent.putExtra("address", mitoadd.getText().toString());
                    try {
                        startActivity(smsVIntent);
                    } catch (Exception ex) {
                        Toast.makeText(TrackDriver_act.this, "Your sms has failed...",
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

    @Override
    protected void onPause() {
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
            }
        }
        MyLocation myLocation = new MyLocation(
                MyApplication.mVolleyRequestQueue);
        myLocation.getLocation(this, onLocationResult);
        miDelayDriverPosition = Constants_user.PASSIVE_INTERVAL;
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverTrack);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
        System.gc();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverTrack,
                new IntentFilter("com.jumpinjumpout.apk.user.ui.TrackDriver_act"));
        Intent sendableIntent = new Intent("com.jumpinjumpout.apk.user.ui.TrackDriver_act");
        LocalBroadcastManager.getInstance(this).
                sendBroadcast(sendableIntent);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        int dpValue = 110;
        float d = TrackDriver_act.this.getResources().getDisplayMetrics().density;
        int margin = (int) (dpValue * d);
        lp.setMargins(0, 0, 0, margin);
        findViewById(com.jumpinjumpout.apk.lib.R.id.map).setLayoutParams(lp);
        mLlMarkerLayout.setVisibility(View.GONE);
        progressMessage("Please wait ...");
        getTrip();
        driverCurrentLocationMarker = null;
        MyLocation myLocation = new MyLocation(
                MyApplication.mVolleyRequestQueue, TrackDriver_act.this, googleApiClient);
        CGlobals_user.getInstance().sendUpdatePosition(
                CGlobals_user.getInstance().getMyLocation(TrackDriver_act.this), getApplicationContext());

        myLocation.getLocation(this, onLocationResult);
        CGlobals_lib.getInstance().turnGPSOn1(TrackDriver_act.this, mGoogleApiClient);
        mivZoomVehicle.setVisibility(View.VISIBLE);
        findViewById(R.id.ivZoomActiveUsers).setVisibility(View.VISIBLE);
        findViewById(R.id.ivZoomFit).setVisibility(View.VISIBLE);
        miDelayDriverPosition = Constants_user.DRIVER_UPDATE_INTERVAL;
        if (hasJoinedTrip) {
        }
        if (hasJoinedTrip
                && MyApplication.getInstance().getPersistentPreference()
                .getBoolean(Constants_user.PREF_HAS_JUMPED_IN, false)) {
            findViewById(R.id.btnJumpIn).setVisibility(View.GONE);
            findViewById(R.id.btnCancel).setVisibility(View.GONE);
            findViewById(R.id.btnJumpOut).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.btnJumpOut).setVisibility(View.GONE);
        }
        checkJumpIn();
        if (!hasTripEnded && !isGettingTrip) {
            handler.postDelayed(runnableDriverPosition, 1);
        }
        populateListFromNotifications();
        isGettingTrip = true;

        if (!connectionError()) {
            getTrip();
        } else {
            Toast.makeText(
                    TrackDriver_act.this,
                    "Please start your Internet.",
                    Toast.LENGTH_SHORT).show();
        }
        if (!hasTripEnded && !hasJoinedTrip && !sNotificationValue.equals("1")) {
            Toast.makeText(
                    getApplicationContext(),
                    "You are now tracking this trip.\n You will get progress notificaitons",
                    Toast.LENGTH_SHORT).show();
        }

        getacabValue = MyApplication.getInstance().getPersistentPreference().
                getBoolean(Constants_user.GET_A_CAB_VALUE, false);
        if (getacabValue) {
            isGettingTrip = false;
            mButtonJoin.setVisibility(View.GONE);
            mButtonCancel.setVisibility(View.GONE);
            mButtonJumpIn.setVisibility(View.GONE);
            mButtonJumpOut.setVisibility(View.GONE);
            mButtonShowTicket.setVisibility(View.GONE);
        }
        MyApplication.getInstance().getPersistentPreferenceEditor().putBoolean(Constants_user.PREF_NOTIFICATION_CLEAR_FLAG, true);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
    }

    void getTrip() {
        progressMessage("Please wait ...");
        SimpleDateFormat df = new SimpleDateFormat(
                Constants_user.STANDARD_DATE_FORMAT, Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        progressCancel();
        String sUrl = Uri
                .parse(Constants_user.GET_TRIP_SAR_APK_URL)
                .buildUpon()
                .appendQueryParameter("appuserid",
                        Integer.toString(CGlobals_user.getInstance().getAppUserId()))
                .appendQueryParameter("email", CGlobals_user.msGmail)
                .appendQueryParameter("tripid", Integer.toString(miTripId))
                .appendQueryParameter("countrycode", CGlobals_user.msCountryCode)
                .appendQueryParameter("phoneno", CGlobals_user.msPhoneNo)
                .appendQueryParameter("clientdatetime",
                        df.format(cal.getTime())).build().toString();
        StringRequest req = new StringRequest(sUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            Log.d(TAG,
                                    "GET Trip Url: " + response.toString());


                            if (TextUtils.isEmpty(response)) {
                                progressCancel();
                                endTrip();
                                mTvNoPassenger.setText("Trip has Ended");
                                mTvNoPassenger.setVisibility(View.VISIBLE);
                                mLlPassengerInfo.setVisibility(View.GONE);
                                mivZoomFit.setVisibility(View.GONE);
                                flZoomActiveUsers.setVisibility(View.GONE);
                                mivZoomVehicle.setVisibility(View.GONE);
                                return;
                            }

                            if (response.toString().trim().equals("-1")) {
                                progressCancel();
                                Toast.makeText(
                                        getApplicationContext(),
                                        "Something went wrong. Cannot fetch this trip",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            CGlobals_lib.getInstance().isNewNotification = false;
                            moTrip = new CTrip(response, TrackDriver_act.this);
                            JSONObject jTrip = new JSONObject(response);
                            MyApplication.getInstance().getPersistentPreferenceEditor()
                                    .putString(Constants_user.CURRENT_TRACKED_TRIP, response);
                            MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                            if (!TextUtils.isEmpty(moTrip.getMsTripPath())) {
                                plotPathFromPolyLine(moTrip.getMsTripPath());
                            } else {
                                progressCancel();
                                Toast.makeText(
                                        getApplicationContext(),
                                        "Something went wrong. Cannot fetch this trip",
                                        Toast.LENGTH_SHORT).show();
                            }
                            moFromAddress.setLatitude(moTrip.getFromLat());
                            moFromAddress.setLongitude(moTrip.getFromLng());
                            moFromAddress.setAddress(moTrip.getFrom());
                            moToAddress.setLatitude(moTrip.getToLat());
                            moToAddress.setLongitude(moTrip.getToLng());
                            moToAddress.setAddress(moTrip.getTo());
                            drawFromMarker(moFromAddress);
                            drawToMarker(moToAddress);
                            mBounds = new LatLngBounds.Builder();
                            if (moFromAddress.hasLatitude()
                                    && moFromAddress.hasLongitude()
                                    && moToAddress.hasLatitude()
                                    && moToAddress.hasLongitude()) {
                                mBounds.include(
                                        new LatLng(moFromAddress.getLatitude(),
                                                moFromAddress.getLongitude()))
                                        .include(
                                                new LatLng(moToAddress
                                                        .getLatitude(),
                                                        moToAddress
                                                                .getLongitude()));
                                if (mDriverLatLng != null) {
                                    mBounds.include(mDriverLatLng);
                                }
                                try {
                                    if (mMap != null) {
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                                                mBounds.build(),
                                                Constants_user.MAP_PADDING));
                                    }
                                } catch (Exception e) {
                                    SSLog.e(TAG, "plotPath - ", e);
                                }
                            }
                            if (moTrip.getMsTripStatusTrack()
                                    .equals(Constants_user.TRIP_ACTION_CREATE)) {
                                setStatus(R.string.tripNotStarted, 1);
                            } else if (moTrip.getMsTripStatusTrack()
                                    .equals(Constants_user.TRIP_ACTION_BEGIN)
                                    || moTrip.getMsTripStatusTrack()
                                    .equals(Constants_user.TRIP_ACTION_PAUSE)
                                    || moTrip.getMsTripStatusTrack()
                                    .equals(Constants_user.TRIP_ACTION_RESUME)) {
                                if (mHasDriverMoved) {
                                    setStatus(R.string.tripInProgress, 1);
                                } else {
                                    setStatus(R.string.tripNotStarted, 1);
                                }
                            } else if (moTrip.getMsTripStatusTrack()
                                    .equals(Constants_user.TRIP_ACTION_ABORT)
                                    || moTrip.getMsTripStatusTrack()
                                    .equals(Constants_user.TRIP_ACTION_END)) {
                                setStatus(R.string.tripHasEnded, 1);
                                hasTripEnded = true;
                                if (hasTripEnded) {
                                    mButtonJoin.setVisibility(View.GONE);
                                }
                                endTrip();
                            }
                            getacabValue = MyApplication.getInstance().getPersistentPreference().
                                    getBoolean(Constants_user.GET_A_CAB_VALUE, false);
                            if (getacabValue) {
                                getaCabDriverDetails(response);
                            } else {
                                try {
                                    if (!TextUtils.isEmpty(moTrip.getUserProfileImageFileName()) && !TextUtils.isEmpty(moTrip.getUserProfileImagePath())) {
                                        String url = Constants_user.GET_USER_PROFILE_IMAGE_FILE_NAME_URL + moTrip.getUserProfileImagePath() +
                                                moTrip.getUserProfileImageFileName();
                                        ImageRequest request = new ImageRequest(url,
                                                new Response.Listener<Bitmap>() {
                                                    @Override
                                                    public void onResponse(Bitmap bitmap) {
                                                        if (Build.VERSION.SDK_INT < 11) {
                                                            Toast.makeText(TrackDriver_act.this, getString(R.string.api11not), Toast.LENGTH_LONG).show();
                                                            mIvPhoto.setImageBitmap(bitmap);
                                                        } else {
                                                            mIvPhoto.setVisibility(View.VISIBLE);
                                                            mIvPhoto.setImageBitmap(bitmap);
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
                                        Bitmap bitmap = moTrip.getContactThnumbnail();
                                        if (bitmap != null) {
                                            mIvPhoto.setImageBitmap(bitmap);
                                            mIvPhoto.setVisibility(View.VISIBLE);
                                        }
                                    }
                                    if (!TextUtils.isEmpty(moTrip.getName())) {
                                        mTvInfoBandLine4.setText(Html.fromHtml(moTrip.getHtmlName()));
                                        mTvInfoBandLine4.setVisibility(View.VISIBLE);
                                    }
                                    MyApplication.getInstance().getPersistentPreferenceEditor()
                                            .putString(Constants_user.PREF_MARATHI_LONG_DISTANCE_VEHICLE_CATEGORY, moTrip.getVehicle_Category());
                                    MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                                } catch (Exception e) {
                                    SSLog.e(TAG, "OnCreate - ", e);
                                }
                            }
                        } catch (Exception e) {
                            progressCancel();
                            SSLog.e(TAG, " getTrip() - " + response, e);
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Something went wrong with this trip. Aborting .....",
                                    Toast.LENGTH_LONG).show();

                            setStatus(R.string.tripHasEnded, 1);
                            hasTripEnded = true;
                            endTrip();
                            return;
                        }

                        handler.postDelayed(runnableDriverPosition,
                                miDelayDriverPosition);
                        progressCancel();
                        isGettingTrip = false;
                        progressCancel();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                mTvNoPassenger.setText("Waiting for connection");
                mTvNoPassenger.setVisibility(View.VISIBLE);
                mLlPassengerInfo.setVisibility(View.GONE);
                progressCancel();
                SSLog.e(TAG, " getTrip: onErrorResponse - ",
                        error.getMessage());
            }
        });
        MyApplication.getInstance().addVolleyRequest(req, false);
    } // getTrip

    void setCurrentLocation(LatLng latLng, int accuracy, double dSpeed,
                            String sLastLocationTime, double dDistanceFromMe) {
        String ttl = "";
        try {
            if (dSpeed != -1) {
                ttl = String.format(Locale.getDefault(), "%.2f km/hr ",
                        dSpeed * 3.6);
                sSpeed = String.format(Locale.getDefault(), "%.2f km/hr ",
                        dSpeed * 3.6);
            }
            if (dDistanceFromMe > -1) {
                ttl += String.format(Locale.getDefault(), "%.2f ",
                        dDistanceFromMe);
                sDistance = "<b>" + String.format(Locale.getDefault(), "%.2f ",
                        dDistanceFromMe) + "</b>";
            }
            if (!TextUtils.isEmpty(sLastLocationTime)) {
                ttl += sLastLocationTime;
                msLastLocationTime = sLastLocationTime;
            }
            popInfoBand(sSpeed, sDistance, msLastLocationTime
                    , lTimeDiff, lTimeDifflocation);
        } catch (Exception e) {
            SSLog.e(TAG, "setCurrentLocation - ", e);
        }
        if (myCircle == null) {
            CircleOptions circleOptions = new CircleOptions().center(latLng)
                    // set center
                    .fillColor(Color.TRANSPARENT).strokeColor(Color.BLUE)
                    .strokeWidth(5);
            myCircle = mMap.addCircle(circleOptions);
            if (accuracy != -1) {
                myCircle.setRadius(accuracy);
            }
        }
        if (driverCurrentLocationMarker == null) {
            driverCurrentLocationMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.track_driver_circle))
                    .alpha(0.8f).title(ttl));
        } else {
            driverCurrentLocationMarker.setPosition(new LatLng(latLng.latitude,
                    latLng.longitude));
            driverCurrentLocationMarker.setTitle(ttl);
            myCircle.setCenter(latLng);
            if (accuracy != -1) {
                myCircle.setRadius(accuracy);
            }
        }
    }

    public BroadcastReceiver mMessageReceiverTrack = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkInternetGPS();
            if (CGlobals_lib.getInstance().isNewNotification = true) {
                getTrip();
            }
            if (!hasTripEnded) {
                getTripStatus();
            }
        }
    };

    private Runnable runnableDriverPosition = new Runnable() {
        @Override
        public void run() {
            if (!hasTripEnded) {
                getTripStatus();
            }
        }
    };

    private void getTripStatus() {

        if (miTripId < 0) {
            Toast.makeText(TrackDriver_act.this, "Something went wrong, please go back and track this trip again", Toast.LENGTH_LONG).show();
            return;
        }
        checkJumpIn();
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.GET_TRIP_STATUS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String sResponse) {
                        double dSpeed = 0;
                        String sLastAccess = "";
                        JSONObject response;
                        Log.d("Response: ",
                                "GET_TRIP_STATUS_URL = " + sResponse);
                        try {
                            if (TextUtils.isEmpty(sResponse)) {
                                progressCancel();
                                endTrip();
                                mTvNoPassenger.setText("Trip has Ended");
                                mTvNoPassenger.setVisibility(View.VISIBLE);
                                mLlPassengerInfo.setVisibility(View.GONE);
                                mivZoomFit.setVisibility(View.GONE);
                                flZoomActiveUsers.setVisibility(View.GONE);
                                mivZoomVehicle.setVisibility(View.GONE);
                                return;
                            }
                            if (sResponse.toString().trim().equals("-1")) {
                                progressCancel();
                                endTrip();
                                mTvNoPassenger.setText("Trip has Ended");
                                mTvNoPassenger.setVisibility(View.VISIBLE);
                                mLlPassengerInfo.setVisibility(View.GONE);
                                mivZoomFit.setVisibility(View.GONE);
                                flZoomActiveUsers.setVisibility(View.GONE);
                                mivZoomVehicle.setVisibility(View.GONE);
                                return;
                            }
                            CGlobals_lib.getInstance().isNewNotification = false;
                            response = new JSONObject(sResponse);
                            MyApplication.getInstance().getPersistentPreferenceEditor()
                                    .putString(Constants_user.TRIP_STATUS_TRACKED_TRIP, sResponse);
                            moTrip = new CTrip(sResponse, getApplicationContext());
                            SimpleDateFormat df = new SimpleDateFormat(
                                    Constants_user.STANDARD_DATE_FORMAT, Locale
                                    .getDefault());
                            Date datetimeLastAccess = new Date();
                            Calendar cal = Calendar.getInstance();
                            mHasDriverMoved = (response
                                    .isNull("hasdriver_moved") ? false
                                    : (response.getInt("hasdriver_moved") > 0 ? true
                                    : false));
                            sLastAccess = response
                                    .isNull("clientaccessdatetime") ? ""
                                    : response
                                    .getString("clientaccessdatetime");
                            if (!TextUtils.isEmpty(sLastAccess)) {
                                try {
                                    datetimeLastAccess = df.parse(sLastAccess);
                                    lTimeDiff = (cal.getTimeInMillis() - datetimeLastAccess
                                            .getTime()) / 1000 / 60;
                                    if (lTimeDiff < 1) {
                                        msLastActive = "Online now";
                                    } else {
                                        msLastActive = "Last online "
                                                + lTimeDiff + " min. ago";
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (response.isNull("lat")
                                    || response.isNull("lng")) {
                                setStatus(R.string.driverPoistionNA, 1);
                            } else {
                                double lat = response.getDouble("lat");
                                double lng = response.getDouble("lng");
                                if (!response.isNull("lat")
                                        && !response.isNull("lng")) {
                                    mDriverLatLng = new LatLng(lat, lng);
                                }
                                dSpeed = response.isNull("speed") ? 0
                                        : response.getDouble("speed");
                                int iAccuracy = response.isNull("accuracy") ? -1
                                        : response.getInt("accuracy");
                                String sLocTime = response.isNull("loctime") ? ""
                                        : response.getString("loctime");
                                getacabValue = MyApplication.getInstance().getPersistentPreference().
                                        getBoolean(Constants_user.GET_A_CAB_VALUE, false);
                                if (getacabValue) {
                                } else {
                                    try {
                                        if (CGlobals_user.getInstance().getMyLocation(TrackDriver_act.this) != null) {
                                            float dist[] = new float[1];
                                            Location.distanceBetween(CGlobals_user
                                                    .getInstance().getMyLocation(TrackDriver_act.this)
                                                    .getLatitude(), CGlobals_user
                                                    .getInstance().getMyLocation(TrackDriver_act.this)
                                                    .getLongitude(), lat, lng, dist);
                                            if (dDistanceFromMe != Double
                                                    .parseDouble(String.format(
                                                            Locale.getDefault(),
                                                            "%.2f",
                                                            dist[0] / 1000.0))) {
                                                dDistanceFromMe = Double.parseDouble(String.format(
                                                        Locale.getDefault(),
                                                        "%.2f", dist[0] / 1000.0));
                                                updateSignal(dist[0]);
                                                mLlPassengerInfo.setVisibility(View.VISIBLE);
                                            }
                                            if (moTrip.hasJumpedOut() && !israting) {
                                                populateTable(sResponse);
                                            }
                                            if (moTrip.hasJoined() && !moTrip.hasJumpedIn()) {
                                                mTvtrip.setVisibility(View.GONE);
                                                mButtonShowTicket.setVisibility(View.VISIBLE);
                                            } else if (moTrip.hasJumpedIn() && !moTrip.hasJumpedOut()) {
                                                mTvtrip.setText("Enjoy your ride");
                                                mTvtrip.setVisibility(View.VISIBLE);
                                                mTvtrip.setTextSize(20);
                                                mLlPassenger.setVisibility(View.GONE);
                                            } else if (moTrip.hasJumpedIn() && moTrip.hasJumpedOut()) {
                                                mTvtrip.setText("Trip over. Receipt will be sent.");
                                                mTvtrip.setVisibility(View.VISIBLE);
                                                mTvNoPassenger.setText("Trip has Ended");
                                                mTvNoPassenger.setVisibility(View.VISIBLE);
                                                mLlPassengerInfo.setVisibility(View.GONE);
                                                mivZoomFit.setVisibility(View.GONE);
                                                flZoomActiveUsers.setVisibility(View.GONE);
                                                mivZoomVehicle.setVisibility(View.GONE);
                                                endTrip();
                                                return;
                                            }
                                        }
                                        if (!TextUtils.isEmpty(sLocTime)) {
                                            Date datetimeLastLocation = new Date();
                                            try {
                                                datetimeLastLocation = df
                                                        .parse(sLocTime);
                                                lTimeDifflocation = (cal
                                                        .getTimeInMillis() - datetimeLastLocation
                                                        .getTime()) / 1000;
                                                if (lTimeDifflocation < 30) {
                                                    sLastActive = "Location available";
                                                } else {
                                                    sLastActive = "Last location recd. "
                                                            + lTimeDifflocation
                                                            / 60
                                                            + " min. ago";
                                                }
                                            } catch (Exception e) {

                                                e.printStackTrace();
                                            }
                                        }
                                        String sTripStatus = response
                                                .isNull("trip_action") ? "N"
                                                : response.getString("trip_action");
                                        if (sTripStatus
                                                .equals(Constants_user.TRIP_ACTION_CREATE)) {
                                            setStatus(R.string.tripNotStarted, 1);
                                        } else if (sTripStatus
                                                .equals(Constants_user.TRIP_ACTION_BEGIN)
                                                || sTripStatus
                                                .equals(Constants_user.TRIP_ACTION_PAUSE)
                                                || sTripStatus
                                                .equals(Constants_user.TRIP_ACTION_RESUME)) {
                                            if (mHasDriverMoved) {
                                                setStatus(R.string.tripInProgress,
                                                        1);
                                            } else {
                                                setStatus(R.string.tripNotStarted,
                                                        1);
                                            }

                                        } else if (sTripStatus
                                                .equals(Constants_user.TRIP_ACTION_ABORT)
                                                || sTripStatus
                                                .equals(Constants_user.TRIP_ACTION_END)) {
                                            endTrip();
                                        }
                                    } catch (Exception e) {
                                        SSLog.e(TAG, "getTripStatus - ", e);
                                    }
                                    setCurrentLocation(new LatLng(lat, lng),
                                            iAccuracy, dSpeed, sLastActive,
                                            dDistanceFromMe);
                                }
                            }
                            if (!isGettingTrip && !hasTripEnded) {
                                handler.postDelayed(
                                        runnableDriverPosition,
                                        TrackDriver_act.this.miDelayDriverPosition);
                            }
                            if (moTrip.hasJoined()) {
                                mivTwitter.setVisibility(View.VISIBLE);
                                mivFacebook.setVisibility(View.VISIBLE);
                            } else {
                                mivTwitter.setVisibility(View.GONE);
                                mivFacebook.setVisibility(View.GONE);
                            }
                            statusShow();
                            driverBeReadyStatus();
                            driverMovedStatus(dSpeed, sLastAccess);
                            checkInternetGPS();

                        } catch (Exception e) {
                            SSLog.e(TAG, "getTripStatus - ", e);
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SSLog.e(TAG, "getTripStatus :-   ", error.getMessage());

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();

                params.put("tripid", String.valueOf(miTripId));

                params = CGlobals_user.getInstance().getBasicMobileParams(params,
                        Constants_user.GET_TRIP_STATUS_URL, TrackDriver_act.this);

                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";

                }

                String url1 = Constants_user.GET_TRIP_STATUS_URL;

                try {
                    String url = url1 + "?" + getParams.toString()
                            + "&verbose=Y";
                    System.out.println("url  " + url);

                } catch (Exception e) {
                    SSLog.e(TAG, "getTripStatus", e);
                }
                return CGlobals_user.getInstance().checkParams(params);

            }

        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);

    } // getTripStatus


    private void joinTrip(final int iJoinOrQuit, final String sAddress) {

        String sDrive = MyApplication.getInstance().getPersistentPreference()
                .getString(Constants_user.CURRENT_TRACKED_TRIP, "");
        moTrip = new CTrip(sDrive, TrackDriver_act.this);

        handlerUpdateAddress.postDelayed(runnableUpdateAddress, 0);
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.JOIN_TRIP_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response: JOIN_TRIP_URL - " + response.toString());
                Log.d("Response", response);
                getJoin(response);
                if (iJoinOrQuit > 0) {
                    MyApplication
                            .getInstance()
                            .getPersistentPreferenceEditor()
                            .putBoolean(
                                    Constants_user.PREF_JOINED_TRIP,
                                    true);
                    MyApplication
                            .getInstance()
                            .getPersistentPreferenceEditor()
                            .putBoolean(Constants_user.PREF_IN_TRIP,
                                    false);
                    MyApplication.getInstance()
                            .getPersistentPreferenceEditor().commit();
                    hasJoinedTrip = true;
                    checkJumpIn();
                    Toast.makeText(getApplicationContext(),
                            "You have now joined this trip",
                            Toast.LENGTH_LONG).show();
                    CGlobals_user.getInstance().setJumpIn(false);
                } else {
                    unJoinTrip();
                }
                MyApplication.getInstance()
                        .getPersistentPreferenceEditor().commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                String sErr = CGlobals_user.getInstance().getVolleyError(TrackDriver_act.this,
                        error);
                SSLog.e(TAG, sErr, error.getMessage());
                Toast.makeText(
                        getApplicationContext(),
                        "There was a problem joining this trip\nPlease check your internet connection and location",
                        Toast.LENGTH_LONG).show();
                unJoinTrip();
                try {
                    SSLog.e(TAG, " joinTrip: ", error);
                } catch (Exception e) {
                    SSLog.e(TAG, "joinTrip - ", e);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("triptype", CGlobals_user.getInstance().getTripType());
                params.put("tracknotification", Integer.toString(iJoinOrQuit));
                params.put("passengerdistance", String.valueOf(dist / 1000));
                params.put("tripid", Integer.toString(miTripId));
                params.put("joinaddress", sAddress);
                //params.put("triptype", moTrip.isTripCommercial());
                params = CGlobals_user.getInstance().getAllMobileParams(params,
                        Constants_user.JOIN_TRIP_URL, TrackDriver_act.this);
                return CGlobals_user.getInstance().checkParams(params);
            }

        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    } // joinTrip

    private void getJoin(String response) {
        if (TextUtils.isEmpty(response)) {
            return;
        }
        MyApplication.getInstance().getPersistentPreferenceEditor().putString("JOIN_TRIP_RESPONCE", response);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
        if (!isTicket) {
            showTripTicket();
        }
    }

    void cancelJoin() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                TrackDriver_act.this);
        // Setting Dialog Title
        alertDialog.setTitle("Back out of trip");
        // Setting Dialog Message
        alertDialog
                .setMessage("Are you sure? This may cause inconvenience to the sharer");
        // Setting Icon to Dialog
        // alertDialog.setIcon(R.drawable.delete);
        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(),
                                "Driver will be notified that you dropped out",
                                Toast.LENGTH_SHORT).show();
                        mTvNoPassenger.setText("Driver will be notified that you dropped out");
                        mTvNoPassenger.setVisibility(View.VISIBLE);
                        mLlPassengerInfo.setVisibility(View.GONE);
                        new JoinTripTask(0).execute();
                        joinAddressValue = 0;
                        handlerJoinAddress.postDelayed(runnableUpdatePosition,
                                Constants_user.DRIVER_UPDATE_INTERVAL);
                        if (!hasTripEnded) {
                            mButtonJoin.setVisibility(View.VISIBLE);
                        }
                        mButtonCancel.setVisibility(View.GONE);
                        MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("FROMLAT", (float) Constants_user.INVALIDLAT);
                        MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("FROMLNG", (float) Constants_user.INVALIDLNG);
                        MyApplication.getInstance().getPersistentPreferenceEditor().putString("PASSENGER_START_ADDRESS", "");
                        MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("TOLAT", (float) Constants_user.INVALIDLAT);
                        MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("TOLNG", (float) Constants_user.INVALIDLNG);
                        MyApplication.getInstance().getPersistentPreferenceEditor().putString("PASSENGER_DESTINATION_ADDRESS", "");
                        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                    }
                });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to invoke NO event
                        Toast.makeText(getApplicationContext(),
                                "Cancelling request to drop out",
                                Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });

        // Showing Alert Message
        if (!TrackDriver_act.this.isFinishing()) {
            alertDialog.show();
        }
    }

    private MyLocation.LocationResult onLocationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            if (location != null) {
                CGlobals_user.getInstance().setMyLocation(location, false);
                CGlobals_user.getInstance().sendUpdatePosition(
                        CGlobals_user.getInstance().getMyLocation(TrackDriver_act.this), getApplicationContext());
            }
        }
    };

    private void jumpOut() {
        sendJumpIn(0);
    }

    private void jumpInSuccessful() {
        CGlobals_user.getInstance().setJumpIn(true);
    }

    private void jumpOutSuccessful() {
        CGlobals_user.getInstance().setJumpIn(false);
        if (!hasTripEnded) {
            mButtonJoin.setVisibility(View.VISIBLE);
        }
        mButtonCancel.setVisibility(View.GONE);
        mButtonJumpOut.setVisibility(View.GONE);
        MyApplication.getInstance().getPersistentPreferenceEditor()
                .putBoolean(Constants_user.PREF_HAS_JUMPED_IN, false);
        MyApplication.getInstance().getPersistentPreferenceEditor()
                .putBoolean(Constants_user.PREF_HAS_JUMPED_OUT, true);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
        unJoinTrip();
    }

    private void sendJumpIn(final int iJumpIn) {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.JUMP_IN_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response: JUMP_IN_URL - " + response.toString());
                Log.d("Response", response);
                if (iJumpIn > 0) { // Jumpin
                    jumpInSuccessful();
                } else { // Jumpout
                    jumpOutSuccessful();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                jumpInCallFailed(error);
                SSLog.e(TAG, "sendJumpIn :-   ", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("triptype", CGlobals_user.getInstance().getTripType());
                if (iJumpIn > 0) {
                    params.put("jumpin", "1");
                } else {
                    params.put("jumpout", "1");
                }
                params.put("tripid", Integer.toString(miTripId));
                params = CGlobals_user.getInstance().getBasicMobileParams(params,
                        Constants_user.JUMP_IN_URL, TrackDriver_act.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "=" + entry.getValue());
                    delim = "&";
                }
                try {
                    String url = Constants_user.JUMP_IN_URL + "?" + getParams.toString() + "&verbose=Y";
                    System.out.println("url  " + url);
                } catch (Exception e) {
                    Log.e(TAG, e.getStackTrace().toString());
                }
                return CGlobals_user.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    } // joinTrip

    void jumpInCallFailed(VolleyError error) {
        try {
            SSLog.e(TAG, "JUMP_IN_URL - ", error.getMessage());
            Toast.makeText(getApplicationContext(),
                    "Request failed. Please try again", Toast.LENGTH_SHORT)
                    .show();
        } catch (Exception e) {
            SSLog.e(TAG, "JUMP_IN_URL - ", e);
        }
    }

    private void checkJumpIn() {
        if (!hasJoinedTrip) {
            return;
        }

        boolean jumpIn = false;
        float dist[] = new float[1];
        if (CGlobals_user.getInstance().getMyLocation(TrackDriver_act.this) != null
                && mDriverLatLng != null) {
            Location.distanceBetween(CGlobals_user.getInstance().getMyLocation(TrackDriver_act.this)
                            .getLatitude(), CGlobals_user.getInstance().getMyLocation(TrackDriver_act.this)
                            .getLongitude(), mDriverLatLng.latitude,
                    mDriverLatLng.longitude, dist);
            if (dist[0] <= Constants_user.JUMP_IN_DISTANCE) {
                jumpIn = true;
            }
        }
        if (jumpIn) {
            if (isInJumpinMode == -1) {
                mButtonJumpIn.setVisibility(View.GONE);
                isInJumpinMode = 1;
            }
        } else {
            mButtonJumpIn.setVisibility(View.GONE);
        }
    }

    private class GetAddressTask extends AsyncTask<Void, Void, Void> {
        Location location;

        public GetAddressTask(Location loc) {
            super();
            location = loc;
        }

        /**
         * Get a Geocoder instance, get the latitude and longitude look up the
         * address, and return it
         *
         * @return A string containing the address of the current location, or
         * an empty string if no address can be found, or an error
         * message
         * @params params One or more Location objects
         */
        @Override
        protected Void doInBackground(Void... params) {
            Geocoder geocoder = new Geocoder(TrackDriver_act.this,
                    Locale.getDefault());
            // Get the current location from the input parameter list
            // Create a list to contain the result address
            List<Address> addresses = null;
            try {
                /*
                 * Return 1 address.
				 */
                addresses = geocoder.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1);
            } catch (IOException e1) {
                Log.e("LocationSampleActivity",
                        "IO Exception in getFromLocation()");
                e1.printStackTrace();
                return null;
            } catch (IllegalArgumentException e2) {
                // Error message to post in the log
                String errorString = "Illegal arguments "
                        + Double.toString(location.getLatitude()) + " , "
                        + Double.toString(location.getLongitude())
                        + " passed to address service";
                Log.e("LocationSampleActivity", errorString);
                e2.printStackTrace();
                return null;
            }
            // If the reverse geocode returned an address
            if (addresses != null && addresses.size() > 0) {
                // Get the first address
                Address address = addresses.get(0);
                String.format(
                        "%s, %s, %s",
                        // If there's a street address, add it
                        address.getMaxAddressLineIndex() > 0 ? address
                                .getAddressLine(0) : "",
                        // Locality is usually a city
                        address.getLocality(),
                        // The country of the address
                        address.getCountryName());
                // Return the text
                return null;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Void result) {
        }
    } // GetAddressTask

    private Runnable runnableUpdateAddress = new Runnable() {
        @Override
        public void run() {
            Location location = CGlobals_user.getInstance().getMyLocation(TrackDriver_act.this);
            if (location != null) {
                new GetAddressTask(location).execute();
            }
        }
    };

    protected Runnable runnableUpdatePosition = new Runnable() {
        @Override
        public void run() {
            if (joinAddressValue == 0) {
                new JoinTripTask(0).execute();
            } else if (joinAddressValue == 1) {
                new JoinTripTask(1).execute();
            }
        }
    };

    private class JoinTripTask extends AsyncTask<Void, Void, Void> {
        int iJoinOrQuit;
        Location location;
        String sAddress;

        public JoinTripTask(int iJorQ) {
            super();
            iJoinOrQuit = iJorQ;
            location = CGlobals_user.getInstance().getMyLocation(TrackDriver_act.this);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Geocoder geocoder = new Geocoder(TrackDriver_act.this, Locale.ENGLISH);

            // Create a list to contain the result address
            try { // Return 1 address.
                addresses = geocoder.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1);
            } catch (IOException e1) {
                Log.e("LocationSampleActivity",
                        "IO Exception in JoinTripTask()");
                SSLog.e(TAG, "IO Exception in JoinTripTask()", e1);
                e1.printStackTrace();
                return null;
            } catch (IllegalArgumentException e2) {
                // Error message to post in the log
                String errorString = "Illegal arguments "
                        + Double.toString(location.getLatitude()) + " , "
                        + Double.toString(location.getLongitude())
                        + " passed to address service";
                Log.e("LocationSampleActivity", errorString);
                e2.printStackTrace();
                return null;
            } catch (Exception e) {
                SSLog.e(TAG, " JoinTripTask: doinBackground ", e);
            }
            // If the reverse geocode returned an address
            if (geocoder.isPresent()) {
                // Get the first address
                try {
                    Address address = addresses.get(0);
                /*
                 * Format the first line of address (if available), city, and
				 * country name.
				 */
                    String localityString = address.getSubLocality();
                    sAddress = String.format(
                            "%s",
                            // If there's a street address, add it
                            address.getMaxAddressLineIndex() > 1 ? address
                                    .getAddressLine(1) : localityString);
                } catch (Exception e) {
                    SSLog.e(TAG, "JoinTripTask:doinBackground ", e);
                }
                // ,
                // Locality is usually a city
                // address.getLocality());
                return null;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            float fTripDistance = 0;
            final float results[] = new float[1];
            int iStartNearestNode = -1;
            int iDestinationNearestNode = -1;
            iStartNearestNode = getNearestNode(new LatLng(bFromLat, bFromLng));
            iDestinationNearestNode = getNearestNode(new LatLng(bToLat, bToLng));
            for (int i = iStartNearestNode; i < iDestinationNearestNode; i++) {
                if (i > iStartNearestNode) {
                    Location.distanceBetween(mLatLngTripPath.get(i - 1).latitude, mLatLngTripPath.get(i - 1).longitude,
                            mLatLngTripPath.get(i).latitude, mLatLngTripPath.get(i).longitude, results);
                    fTripDistance += results[0];
                }
            }
            if (iStartNearestNode == -1 || iDestinationNearestNode == -1) {
                mTvtrip.setText(getString(R.string.notmatchnearestnode));
                mTvtrip.setTextSize(16);
                mTvtrip.setVisibility(View.VISIBLE);
                return;
            } else {
                dist = fTripDistance;
                MyApplication.getInstance().getPersistentPreferenceEditor()
                        .putFloat("TRICKET_DISTANCE", (dist / 1000));
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                joinTrip(iJoinOrQuit, sAddress);
            }
        }
    } // JoinTripTask

    public void onClickZoomActiveUsers(View v) {
        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        if (CGlobals_user.getInstance().getMyLocation(TrackDriver_act.this) != null) {
            bounds.include(new LatLng(CGlobals_user.getInstance().getMyLocation(TrackDriver_act.this)
                    .getLatitude(), CGlobals_user.getInstance().getMyLocation(TrackDriver_act.this)
                    .getLongitude()));
        }
        if (mDriverLatLng != null) {
            bounds.include(mDriverLatLng);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(),
                BOUNDS_PADDING));
    }

    public void onClickZoomTrip(View v) {
        try {
            if (mZoomFitBounds != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                        mZoomFitBounds.build(), BOUNDS_PADDING));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void goFacebook() {
        String sMessage = "";
        String sDrive = MyApplication.getInstance().getPersistentPreference()
                .getString(Constants_user.CURRENT_TRACKED_TRIP, "");
        moTrip = new CTrip(sDrive, TrackDriver_act.this);
        if (moTrip.isTripCommercial().equals(Constants_lib.TRIP_TYPE_COMMERCIAL)) {
            sMessage = "Hi All, I just found a shared cab going to " + moTrip.getTo() + " using Jump In Jump Out." +
                    " If you are looking for rides, they are available right now";
        } else {
            sMessage = "Hi All, I just found a shared ride using Jump In Jump Out." +
                    " If you are looking for rides, they may be available right now";
        }
        Intent intent = new Intent(TrackDriver_act.this, Facebook_act.class);
        intent.putExtra("INTENT_MESSAGE", sMessage);
        startActivity(intent);
    }

    protected void goTwitter() {
        String sMessage = "";
        sMessage = "Hey There, I just ditched my car & used a shared ride using Jump In Jump Out." +
                " Download the App to find yours";
        Intent intent = new Intent(TrackDriver_act.this, Twitter_act.class);
        intent.putExtra("INTENT_MESSAGE", sMessage);
        startActivity(intent);
    }

    public void onClickZoomVehicle(View v) {
        if (mDriverLatLng == null) {
            return;
        }
        CameraUpdate center = CameraUpdateFactory.newLatLng(mDriverLatLng);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
        mMap.moveCamera(center);
        mMap.animateCamera(zoom);
    }

    private void unJoinTrip() {
        if (!hasTripEnded) {
            mButtonJoin.setVisibility(View.VISIBLE);
        }
        mButtonCancel.setVisibility(View.GONE);
        mButtonJumpIn.setVisibility(View.GONE);
        MyApplication.getInstance().getPersistentPreferenceEditor()
                .putBoolean(Constants_user.PREF_JOINED_TRIP, false);
        MyApplication.getInstance().getPersistentPreferenceEditor()
                .putBoolean(Constants_user.PREF_IN_TRIP, false);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
        hasJoinedTrip = false;
    }

    public void endTrip() {
        joinCancel = false;
        jumpOut();
        setStatus(getString(R.string.tripHasEnded), 1);
        hasTripEnded = true;
        mButtonCancel.setVisibility(View.GONE);
        mButtonJumpOut.setVisibility(View.GONE);
        MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("RATING", 0.0f);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
        unJoinTrip();
        hideSignal();
    }

    protected Location getMyLocation() {
        return CGlobals_user.getInstance().getMyLocation(TrackDriver_act.this);
    }

    public void onMyLocationChange(Location location) {
        super.onMyLocationChange(location);
        CGlobals_user.getInstance().setMyLocation(location, true);
    }

    void popInfoBand(String sLine1, String sLine2, String sLine3, long sLine4, long sLine5) {
        mLlPassengerInfo.setVisibility(View.VISIBLE);
        changeColor.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(sLine2)) {
            mTvInfoBandLine1.setText(Html.fromHtml(sLine2));
            mTvInfoBandLine1.setVisibility(View.VISIBLE);
            textkmAway.setVisibility(View.VISIBLE);
        }
        if (sLine4 < 1 && sLine5 < 30) {
            mTvInfoBandLine3.setBackgroundResource(R.drawable.btn_online);
            mTvInfoBandLine3.setVisibility(View.VISIBLE);
        } else {
            mTvInfoBandLine3.setBackgroundResource(R.drawable.btn_offline);
            mTvInfoBandLine3.setVisibility(View.VISIBLE);
        }
    }

    private void driverBeReadyStatus() {
        if (moTrip.getMiPassed() > 0) {
            mTvInfoBandLine2.setText(moTrip.getMiPassed() > 0 ? " (Passed)" : "");
            mTvInfoBandLine2.setVisibility(View.VISIBLE);
            mTvInfoBandLine2.setTextColor(getResources().getColor(R.color.red));
        } else if (CGlobals_user.getInstance().getMyLocation(TrackDriver_act.this) != null) {
            LatLng latlng = new LatLng(CGlobals_user.getInstance().getMyLocation(TrackDriver_act.this).getLatitude(),
                    CGlobals_user.getInstance().getMyLocation(TrackDriver_act.this).getLongitude());
            String distanceFromMe = "";
            if (moTrip.getLatLng() != null && latlng != null) {
                float results[] = new float[1];
                Location.distanceBetween(moTrip.getLatLng().latitude,
                        moTrip.getLatLng().longitude, latlng.latitude,
                        latlng.longitude, results);
                try {
                    if (results[0] < Constants_user.NEAR_BE_READY_DISTANCE) {
                        distanceFromMe = "Be Ready!";
                        mTvInfoBandLine2.setText(distanceFromMe);
                        mTvInfoBandLine2.setVisibility(View.VISIBLE);
                    } else {
                        mTvInfoBandLine2.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    SSLog.e(TAG, "showDistance", e);
                }
            }
        }
    }

    private void driverMovedStatus(double speed, String lastAccess) {
        if (!TextUtils.isEmpty(moTrip.getMsTripStatusTrack())) {
            if (moTrip.getHasDriveMoved() && !showETA) {
                mTvTripAction.post(new Runnable() {
                    public void run() {
                        showETA = true;
                        mTvTripAction.setText(Html.fromHtml(moTrip.getMsTripMovedDriverTime() + "<br>Started Moving"));
                        mTvTripAction.setVisibility(View.VISIBLE);
                    }
                });
            } else if (showETA) {
                addSpeed(speed, lastAccess);
            } else {
                if (moTrip.getMsTripStatusTrack().equals(Constants_user.TRIP_ACTION_CREATE)) {
                    mTvTripAction.post(new Runnable() {
                        public void run() {
                            mTvTripAction.setText(Html.fromHtml(moTrip.getMsTripActionTime() + "<br>Created"));
                            mTvTripAction.setVisibility(View.VISIBLE);
                        }
                    });
                } else if (moTrip.getMsTripStatusTrack().equals(Constants_user.TRIP_ACTION_BEGIN)) {
                    mTvTripAction.post(new Runnable() {
                        public void run() {
                            mTvTripAction.setText(Html.fromHtml(moTrip.getMsTripActionTime() + "<br>Created"));
                            mTvTripAction.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }
    }

    private void addSpeed(double speed, String lastAccess) {
        double dTotalSpeed = 0.0, dAvgSpeed = 0.0;
        int i, j = 0, eTA = 0, eTA1 = 0;
        CSpeed cSpeed = new CSpeed(speed, lastAccess);
        aDriverSpeed.add(cSpeed);
        if (showETA) {
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
                if (dDistanceFromMe > 0) {
                    eTA1 = (int) ((dDistanceFromMe / dAvgSpeed) * 60);
                    if (eTA1 < 1) {
                        eTA = (int) (((dDistanceFromMe / dAvgSpeed) * 60) * 60);
                        mTvMins.setText("secs");
                    } else {
                        eTA = eTA1;
                        mTvMins.setText("mins");
                    }
                }
                mLlPassengerInfo.setVisibility(View.VISIBLE);
                mTvTripAction.setText(String.format(Locale.getDefault(), "%.1f",
                        dAvgSpeed));
                mTvTime.setText(String.valueOf(eTA));
                mTvMins.setVisibility(View.VISIBLE);
                mTvTime.setVisibility(View.VISIBLE);
                mTvkmph.setVisibility(View.VISIBLE);
                mTvTripAction.setVisibility(View.VISIBLE);
                showingETA = true;
            }
        }
    }


    private void statusShow() {
        try {
            if (!TextUtils.isEmpty(moTrip.getMsTripStatusTrack())) {
                if (moTrip.getMsTripStatusTrack().equals(Constants_user.TRIP_ACTION_PAUSE)) {
                    mTvStatusAction.post(new Runnable() {
                        public void run() {
                            mTvStatusAction.setText(Html.fromHtml(moTrip.getMsTripActionTime() + "<br>Paused"));
                            mTvStatusAction.setVisibility(View.VISIBLE);
                        }
                    });
                } else if (moTrip.getMsTripStatusTrack().equals(Constants_user.TRIP_ACTION_RESUME)) {
                    mTvStatusAction.post(new Runnable() {
                        public void run() {
                            mTvStatusAction.setText(Html.fromHtml(moTrip.getMsTripActionTime() + "<br>Resumed"));
                            mTvStatusAction.setVisibility(View.VISIBLE);
                        }
                    });
                } else if (moTrip.getMsTripStatusTrack().equals(Constants_user.TRIP_ACTION_END)) {
                    mTvStatusAction.post(new Runnable() {
                        public void run() {
                            mTvNoPassenger.setText("Trip has Ended");
                            mTvNoPassenger.setVisibility(View.VISIBLE);
                            mLlPassengerInfo.setVisibility(View.GONE);
                            mivZoomFit.setVisibility(View.GONE);
                            flZoomActiveUsers.setVisibility(View.GONE);
                            mivZoomVehicle.setVisibility(View.GONE);
                            joinCancel = false;
                        }
                    });
                }
            }
            if (moTrip.getMsTripStatusTrack().equals(Constants_user.TRIP_ACTION_END)) {
                mTvStatusAction.post(new Runnable() {
                    public void run() {
                        mTvNoPassenger.setText("Trip has Ended");
                        mTvNoPassenger.setVisibility(View.VISIBLE);
                        mLlPassengerInfo.setVisibility(View.GONE);
                        mivZoomFit.setVisibility(View.GONE);
                        flZoomActiveUsers.setVisibility(View.GONE);
                        mivZoomVehicle.setVisibility(View.GONE);
                        joinCancel = false;
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected String getGeoCountryCode() {
        return MyApplication.getInstance().getPersistentPreference()
                .getString(Constants_user.PREF_CURRENT_COUNTRY, "");
    }

    protected void gotGoogleMapLocation(Location location) {
    }

    private boolean connectionError() {
        if (MyApplication.getInstance().getConnectivity().checkConnected(TrackDriver_act.this)) {
            return false;
        } else {
            if (!MyApplication.getInstance().getConnectivity().connectionError(TrackDriver_act.this, getString(R.string.app_label))) {

            }
            return true;
        }
    }

    protected boolean showPath() {
        return true; // Always show the trip in track driver
    }

    @Override
    public void onConnectionSuspended(int arg0) {
    }

    @Override
    public void onClickInviteUsers(View v) {
    }

    public void onClickInformation(View v) {
        driverInfomation();
    }

    public void mapLoaded() {
    }

    void populateListFromNotifications() {
        try {
            String sNotificationValue = MyApplication.getInstance()
                    .getPersistentPreference()
                    .getString(Constants_user.PREF_NOTIFICATION_LIST_SAVED, "");
            ArrayList<String> arraySNotification = new ArrayList<String>();
            maCNotification = new ArrayList<CNotification>();
            if (TextUtils.isEmpty(sNotificationValue)) {
            } else {
                Type type = new TypeToken<ArrayList<String>>() {
                }.getType();
                arraySNotification = new Gson().fromJson(sNotificationValue, type);
                for (int i = 0; i < arraySNotification.size() && i < Constants_user.MAX_NOTIFICATIONS; i++) {
                    maCNotification.add(new CNotification(arraySNotification.get(i).toString(), TrackDriver_act.this));
                }
            }
        } catch (Exception e) {
            SSLog.e(TAG, "init", e);
        }
    }

    protected void hideSignal() {
    }

    @SuppressWarnings("deprecation")
    protected void updateSignal(double dDistanceFromMe) {
        changeColor.setVisibility(View.VISIBLE);
        if (dDistanceFromMe < 0) {
            return;
        }
        if (dDistanceFromMe < Constants_lib.DISTANCE_CLOSE) {
            mTvInfoBandLine1.setVisibility(View.VISIBLE);
            textkmAway.setVisibility(View.VISIBLE);
            int iTransparency = 255 - (int) ((dDistanceFromMe / Constants_lib.DISTANCE_CLOSE) * 128);
            changeColor.setBackgroundResource(R.drawable.red_background);
        } else if (dDistanceFromMe < Constants_lib.DISTANCE_NEAR) {
            mTvInfoBandLine1.setVisibility(View.VISIBLE);
            textkmAway.setVisibility(View.VISIBLE);
            int iTransparency = 255 - (int) ((dDistanceFromMe / Constants_lib.DISTANCE_NEAR) * 128);
            changeColor.setBackgroundResource(R.drawable.yellow_background);
        } else if (dDistanceFromMe < Constants_lib.DISTANCE_FAR) {
            mTvInfoBandLine1.setVisibility(View.VISIBLE);
            textkmAway.setVisibility(View.VISIBLE);
            int iTransparency = 255 - (int) ((dDistanceFromMe / Constants_lib.DISTANCE_FAR) * 128);
            if (dDistanceFromMe > Constants_lib.DISTANCE_FAR) {
                iTransparency = 35;
            }
            changeColor.setBackgroundResource(R.drawable.green_background);
        } else { // too far from me
            changeColor.setBackgroundResource(R.drawable.gray_background);
        }
    }

    @Override
    protected void callDriver(JSONArray jsonArray) {
    }

    protected void checkInternetGPS() {
        try {
            if (!mConnectivity.checkConnected(TrackDriver_act.this)) {
            }
            if (CGlobals_lib.getInstance().checkConnected(
                    TrackDriver_act.this)) {
                mIvNoConnection.setVisibility(View.GONE);
            } else {
                mIvNoConnection.setVisibility(View.VISIBLE);
            }
            Location location = CGlobals_lib.getInstance().getMyLocation(TrackDriver_act.this);
            if (location == null) {
                location = mCurrentLocation;
            }
            if (location == null) {
                return;
            }
            long diff = System.currentTimeMillis() - location.getTime();
            if (location == null || diff > Constants_lib.DRIVER_UPDATE_INTERVAL * 15) {
                MyLocation myLocation = new MyLocation(
                        CGlobals_lib.mVolleyRequestQueue, TrackDriver_act.this, mGoogleApiClient);
                myLocation.getLocation(this, onLocationResult);
                mIvNoLocation.setVisibility(View.VISIBLE);
            } else {
                mIvNoLocation.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickWalkWithMe(View view) {
        Intent intentWalkwithMe = new Intent(TrackDriver_act.this,
                ShareDriverLocationFriend.class);
        startActivity(intentWalkwithMe);
    }

    public void onClickShowTicket(View view) {
        showTripTicket();
    }

    private void showTripTicket() {
        try {
            String sDrive = MyApplication.getInstance().getPersistentPreference()
                    .getString(Constants_user.CURRENT_TRACKED_TRIP, "");
            moTrip = new CTrip(sDrive, TrackDriver_act.this);
            String res = MyApplication.getInstance().getPersistentPreference().getString("JOIN_TRIP_RESPONCE", "");
            JSONObject person;
            try {
                person = new JSONObject(res);
                msTicketId = person.isNull("ticket_id") ? "" : person
                        .getString("ticket_id");
                msTicketFare = person.isNull("trip_fare") ? 0.0 : person
                        .getDouble("trip_fare");
            } catch (Exception e) {
                SSLog.e(TAG, "showTripTicket", e);
            }
            isTicket = true;
            String msTicketStartAddress = MyApplication.getInstance().getPersistentPreference()
                    .getString("PASSENGER_START_ADDRESS", "");
            String msTicketDestinationAddress = MyApplication.getInstance().getPersistentPreference()
                    .getString("PASSENGER_DESTINATION_ADDRESS", "");
            float mDistance = MyApplication.getInstance().getPersistentPreference()
                    .getFloat("TRICKET_DISTANCE", 0);
            final Dialog dialog = new Dialog(TrackDriver_act.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            dialog.setContentView(R.layout.trip_ticket);
            dialog.setTitle("Ticket");
            final TextView tvTripStartAddress, tvTripDestinationAddress, tvTripDate,
                    tvCompanyName, tvBusType, tvTotalFare, tvTripDistance, tvTicketId, tvBusName, tvFare;

            tvBusName = (TextView) dialog.findViewById(R.id.tvBusName);
            tvTicketId = (TextView) dialog.findViewById(R.id.tvTicketId);
            tvTripStartAddress = (TextView) dialog.findViewById(R.id.tvTripStartAddress);
            tvTripDestinationAddress = (TextView) dialog.findViewById(R.id.tvTripDestinationAddress);
            tvTripDate = (TextView) dialog.findViewById(R.id.tvTripDate);
            tvCompanyName = (TextView) dialog.findViewById(R.id.tvCompanyName);
            tvBusType = (TextView) dialog.findViewById(R.id.tvBusType);
            tvTotalFare = (TextView) dialog.findViewById(R.id.tvTotalFare);
            tvTripDistance = (TextView) dialog.findViewById(R.id.tvTripDistance);

            tvBusName.setText(moTrip.getVehicleCompany());
            tvTripStartAddress.setText(msTicketStartAddress);
            tvTripDestinationAddress.setText(msTicketDestinationAddress);
            tvTripDate.setText(moTrip.getTripCreationTime());
            tvCompanyName.setText(moTrip.getCompanyName());
            tvBusType.setText(moTrip.getVehicleType());
            tvTotalFare.setText(String.valueOf((int) msTicketFare));
            MyApplication.getInstance().getPersistentPreferenceEditor()
                    .putString("TRIP_FARE_SHOW", String.valueOf((int) msTicketFare));
            MyApplication.getInstance().getPersistentPreferenceEditor().commit();
            tvTicketId.setText("Ticket id - " + msTicketId);
            tvTripDistance.setText("Distance: " + String.valueOf((int) mDistance) + " Km");
            dialog.show();
        } catch (Exception e) {
            SSLog.e(TAG, "showTripTicket", e);
        }
    }

    private void getaCabDriverDetails(String response) {
        TextView tvNoPassengerGetacab = (TextView) findViewById(R.id.tvNoPassengerGetacab);
        final ImageView ivPhotoGetacab = (ImageView) findViewById(R.id.ivPhotoGetacab);
        LinearLayout llPassengerInfoGetacab = (LinearLayout) findViewById(R.id.llPassengerInfoGetacab);
        TextView tvDriverNameGetacab = (TextView) findViewById(R.id.tvDriverNameGetacab);
        TextView tvTripStatusGetacab = (TextView) findViewById(R.id.tvTripStatusGetacab);
        TextView tvdistanceGetacab = (TextView) findViewById(R.id.tvdistanceGetacab);
        TextView tvTimeGetacab = (TextView) findViewById(R.id.tvTimeGetacab);
        if (TextUtils.isEmpty(response) || response.equals("-1")) {
            tvNoPassengerGetacab.setText("Trip has Ended");
            tvNoPassengerGetacab.setVisibility(View.VISIBLE);
            llPassengerInfoGetacab.setVisibility(View.GONE);
        }
        moTrip = new CTrip(response, TrackDriver_act.this);
        if (!TextUtils.isEmpty(moTrip.getUserProfileImageFileName()) && !TextUtils.isEmpty(moTrip.getUserProfileImagePath())) {
            String url = Constants_user.GET_USER_PROFILE_IMAGE_FILE_NAME_URL + moTrip.getUserProfileImagePath() +
                    moTrip.getUserProfileImageFileName();
            ImageRequest request = new ImageRequest(url,
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap bitmap) {
                            if (Build.VERSION.SDK_INT < 11) {
                                Toast.makeText(TrackDriver_act.this, getString(R.string.api11not), Toast.LENGTH_LONG).show();
                                ivPhotoGetacab.setImageBitmap(bitmap);
                            } else {
                                ivPhotoGetacab.setVisibility(View.VISIBLE);
                                ivPhotoGetacab.setImageBitmap(bitmap);
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
            Bitmap bitmap = moTrip.getContactThnumbnail();
            if (bitmap != null) {
                ivPhotoGetacab.setImageBitmap(bitmap);
                ivPhotoGetacab.setVisibility(View.VISIBLE);
            }
        }
        if (!TextUtils.isEmpty(moTrip.getName())) {
            tvDriverNameGetacab.setText(Html.fromHtml(moTrip.getHtmlName()));
            tvDriverNameGetacab.setVisibility(View.VISIBLE);
        }

        float results[] = new float[1];
        Location.distanceBetween(moTrip.getFromLat(),
                moTrip.getFromLng(), moTrip.getToLat(),
                moTrip.getToLng(), results);
        try {
            distanceFromMe = String.format("%.2f", results[0] / 1000);
            tvdistanceGetacab.setText(distanceFromMe);
        } catch (Exception e) {
            SSLog.e(TAG, "getView", e);
        }
    }

    protected int getNearestNode(LatLng latLng) {

        float results[] = new float[1];
        float fLeastDistance = -1;
        int len = mLatLngTripPath.size();
        int iNearestNode = -1;
        for (int i = 0; i < len; i++) {
            Location.distanceBetween(latLng.latitude, latLng.longitude,
                    mLatLngTripPath.get(i).latitude,
                    mLatLngTripPath.get(i).longitude, results);
            if (fLeastDistance == -1 || fLeastDistance > results[0]) {
                fLeastDistance = results[0];
                iNearestNode = i;
            }

        }
        return iNearestNode;
    }

} // TrackDriver_act

class CSpeed {
    double dSpeed = 0;
    String sLastTime = "";


    public CSpeed(double speed, String lastTime) {
        dSpeed = speed;
        sLastTime = lastTime;
    }

    public double getSpeed() {
        return dSpeed;
    }

    public Date getTime() {
        Date date = null;
        try {
            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date = sdf.parse(sLastTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

}

