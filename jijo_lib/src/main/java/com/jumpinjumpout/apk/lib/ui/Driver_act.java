package com.jumpinjumpout.apk.lib.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.ui.IconGenerator;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.CTrip;
import com.jumpinjumpout.apk.lib.Constants_lib;
import com.jumpinjumpout.apk.lib.PassengerAdapter;
import com.jumpinjumpout.apk.lib.R;
import com.jumpinjumpout.apk.lib.SSLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lib.app.util.CAddress;
import lib.app.util.Connectivity;
import lib.app.util.MyLocation;

public abstract class Driver_act extends AbstractMapFragment_act {
    protected static final String TAG = "Driver_act: ";
    private int BOUNDS_PADDING = 50;
    protected boolean isFromSelected = true;
    boolean hasMovedMap = false;
    private final int ACT_RESULT_TRIP_CREATED = 0x9;
    private final int ACT_RESULT_INVITE_USER = 10;
    protected ArrayList<Marker> mlActivePassengerMarkers = null;
    protected LatLngBounds.Builder mActiveUserBounds;
    protected boolean mIsFirstTime = true;
    public long miPassengerUpdateInterval = 0;
    PolylineOptions mDirectionsPolyline;
    private ArrayList<CTrip> maoRecentTrips = new ArrayList<CTrip>();
    public ImageView mIvPhoto, mIvPassenger1;
    public FrameLayout mFlPassengerInfo, mFlCreateTrip;
    public LinearLayout mLlWaitingPassenger, mLlJoinPassenger, rlFromTo;
    public TextView mTvJoinedCount, mtvJoinedCountNumber, mTvInfoBandLine2, mTvInfoBandLine1;
    public ImageView mTvInfoBandLine3;
    public TextView mTvInfoBandLine4, mTvTime, mTvMins;
    public LinearLayout mLlPassengerInfo;
    public TextView mTvJumpedInCount, mTvKmAway;
    private double mNearestLat, mNearestLng;
    protected int mLastInvited = 0;
    public String sCountryCode = "";
    public boolean flag_btnCreateTrip = false;
    JSONObject oDirectionPoints;
    private FrameLayout mfmLayout;
    Connectivity mConnectivity;
    ArrayList<CTrip> mResponseTrips;
    CTrip mResponsePassengerTrip;

    protected abstract int getAppUserId();

    protected abstract String getEmailId();

    protected abstract void StartLocationService();

    protected abstract void StopLocationService();

    protected abstract void sendTripAction(String sTripAction);

    protected abstract void inviteUsers();

    protected abstract void driverTripSummary();

    protected abstract boolean hasTripEnded(Location location, double lat,
                                            double lng);

    protected abstract String getTripUrl();

    protected abstract String getTripPathUrl();

    protected abstract String getTripType();

    protected abstract String profileImageUrl();

    protected abstract String passengertripsummayUrl();

    protected abstract void showThumbImageClick();

    protected abstract void showStartDialog(int iTripId);

    protected abstract void callLastInvitedTrip();

    protected abstract String getUpdatePositionUrl();

    protected abstract void setTripActionFloatingbuttonHide();

    protected abstract void perfClear();

    protected abstract boolean hasGetaCabTrip();

    public static String ShopLat;
    public static String ShopLong;
    private LatLng center;
    private Geocoder geocoder;
    private List<android.location.Address> addresses;
    protected TextView mTvSetLocation;
    private Animation slideup, slidedown;
    boolean startDialogFlag = false;
    Bitmap bmp = null;
    public boolean mIsDirectTrip = false;
    public boolean hasDriverMode = false;
    public boolean isStartLater = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    } // onCreate

    protected void driverCreate() {
        mConnectivity = new Connectivity();
        CGlobals_lib.getInstance().maoRecentAddress = CGlobals_lib.getInstance()
                .readRecentAddresses(this);

        mTvSetLocation = (TextView) findViewById(R.id.tvSetLocation);
        mButtonPause = (Button) findViewById(R.id.btnPause);
        mButtonResume = (Button) findViewById(R.id.btnResume);
        mButtonCancelTrip = (Button) findViewById(R.id.btnCancelTrip);
        mButtonEndTrip = (Button) findViewById(R.id.btnEndTrip);
        mFlPassengerInfo = (FrameLayout) findViewById(R.id.flPassengerInfo);

        View hiddenPassengerInfo = getLayoutInflater().inflate(
                R.layout.passengerdetails, mFlPassengerInfo, false);
        mFlPassengerInfo.addView(hiddenPassengerInfo);

        mFlCreateTrip = (FrameLayout) findViewById(R.id.flCreateTrip);
        View vFlCreateTrip = getLayoutInflater().inflate(
                R.layout.starttrip_now_later, mFlCreateTrip, false);
        mFlCreateTrip.addView(vFlCreateTrip);

        slideup = AnimationUtils.loadAnimation(Driver_act.this, R.anim.slide_up);
        slidedown = AnimationUtils.loadAnimation(Driver_act.this, R.anim.slide_down);
        slidedown.setAnimationListener(animationListener);

        init();
        sCountryCode = getGeoCountryCode();
        setAutoRefreshFromLocation(true);
        mIvPhoto = (ImageView) findViewById(R.id.ivPhoto);

        mIvPassenger1 = (ImageView) findViewById(R.id.ivPassenger1);
        mLlWaitingPassenger = (LinearLayout) findViewById(R.id.llWaitingPassenger);
        mLlJoinPassenger = (LinearLayout) findViewById(R.id.llJoinPassenger);
        mTvJoinedCount = (TextView) findViewById(R.id.tvJoinedCount);
        mtvJoinedCountNumber = (TextView) findViewById(R.id.tvJoinedCountNumber);

        mTvInfoBandLine1 = (TextView) findViewById(R.id.tvInfoBandLine1);
        mTvInfoBandLine2 = (TextView) findViewById(R.id.tvInfoBandLine2);
        mTvInfoBandLine3 = (ImageView) findViewById(R.id.tvInfoBandLine3);
        mTvTime = (TextView) findViewById(R.id.tvTime);
        mTvMins = (TextView) findViewById(R.id.tvMins);

        mTvInfoBandLine4 = (TextView) findViewById(R.id.tvInfoBandLine4);
        mTvKmAway = (TextView) findViewById(R.id.tvKmAway);
        mfmLayout = (FrameLayout) findViewById(R.id.fmLayout);
        mLlPassengerInfo = (LinearLayout) findViewById(R.id.llPassengerInfo);
        mTvJumpedInCount = (TextView) findViewById(R.id.tvJumpedInCount);
        mlActivePassengerMarkers = new ArrayList<Marker>();
        Location loc = CGlobals_lib.getInstance().getMyLocation(this);
        if (loc != null && !isInTrip()) {
            setLocation(loc);
        }
        if (isInTrip()) {
            showTrip();
        }

        mButtonGo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    if (!moToAddress.hasLatitude() && !moToAddress.hasLongitude()) {
                        Toast.makeText(Driver_act.this,
                                "Please enter your start and destination address",
                                Toast.LENGTH_SHORT).show();
                    }
                    flag_btnCreateTrip = true;
                    float results[] = new float[1];
                    Location.distanceBetween(moFromAddress.getLatitude(), moFromAddress.getLongitude(),
                            moToAddress.getLatitude(), moToAddress.getLongitude(), results);
                    if (results[0] < Constants_lib.DISTANCE_SAME_FROM) {
                        Toast.makeText(Driver_act.this,
                                "Your start and destination are too close (less than 500 m).\nPlease change your start or destination",
                                Toast.LENGTH_SHORT).show();
                    } else if ((moFromAddress.hasLatitude() && moFromAddress
                            .hasLongitude())
                            && ((moToAddress.hasLatitude() && moToAddress
                            .hasLongitude()) || !TextUtils
                            .isEmpty(moToAddress.getAddress()))) {
                        goDirections();
                    } else {
                        Toast.makeText(Driver_act.this,
                                "Missing from or to. Cannot create trip",
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mButtonPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                setTripAction(Constants_lib.TRIP_ACTION_PAUSE);
                sendTripAction(
                        Constants_lib.TRIP_ACTION_PAUSE);

            }
        });
        mButtonResume.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                setTripAction(Constants_lib.TRIP_ACTION_RESUME);
                sendTripAction(
                        Constants_lib.TRIP_ACTION_RESUME);
            }
        });
        mButtonStartNow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                goDirections();
            }
        });
        mButtonCancelTrip.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                setTripAction(Constants_lib.TRIP_ACTION_ABORT);
                sendTripAction(
                        Constants_lib.TRIP_ACTION_ABORT);
            }
        });

        mButtonEndTrip.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                CGlobals_lib.getInstance().getSharedPreferencesEditor(Driver_act.this).
                        putString("plannedstartdatetime", "");
                CGlobals_lib.getInstance().getSharedPreferencesEditor(Driver_act.this).commit();
                CGlobals_lib.msInTrip = "";
                setTripAction(Constants_lib.TRIP_ACTION_END);
                driverTripSummary();
                // Delay the moving of trip to done tables so that summary is reported
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendTripAction(Constants_lib.TRIP_ACTION_END);
                    }
                }, 5);
                resetDisplay();
                String sMsg = "Trip has Ended";
                showStatusMessages(sMsg);
                callLastInvitedTrip();
                mMap.clear();
                isTripFrozen = false;
                mButtonStart.setVisibility(View.GONE);
                perfClear();
            }
        });

        mLlWaitingPassenger.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showThumbImageClick();
            }
        });
        if (!isInTrip())
            stupMap();
    }


    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        miPassengerUpdateInterval = Constants_lib.PASSIVE_INTERVAL;
        bmp = null;
        mMap.clear();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    private Animation.AnimationListener animationListener = new Animation.AnimationListener() {

        @Override
        public void onAnimationEnd(Animation animation) {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            int dpValue = 0;
            float d = Driver_act.this.getResources().getDisplayMetrics().density;
            int margin = (int) (dpValue * d);
            lp.setMargins(0, 0, 0, margin);
            findViewById(R.id.map).setLayoutParams(lp);
            mFlCreateTrip.setVisibility(View.GONE);
            startDialogFlag = false;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }
    };

    public void clickStartLater(View v) {
        isStartLater = true;
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    public void clickStartNow(View v) {
        CGlobals_lib.getInstance().getSharedPreferencesEditor(Driver_act.this).
                putString("plannedstartdatetime", "");
        CGlobals_lib.getInstance().getSharedPreferencesEditor(Driver_act.this).commit();
        isStartLater = false;
        if (oDirectionPoints.toString() != null) {
            createTrip(oDirectionPoints.toString());
        } else {
            showStatusMessages("Create trip failed, please try again");
            Toast.makeText(Driver_act.this, "Create trip failed, please try again", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onResume() {
        init();
        updateBand();
        if (isInTrip()) {
            isTripFrozen = true;
            showTrip();
            String sFromAddressMarker = CGlobals_lib.getInstance().getSharedPreferences(getApplicationContext()).
                    getString(Constants_lib.TRIP_JSON_OBJECT, "");
            Type type = new TypeToken<CAddress>() {
            }.getType();
            CAddress cJsonAddress = new Gson().fromJson(sFromAddressMarker, type);
            drawFromMarker(cJsonAddress);
        } else {
            isTripFrozen = false;
        }
        String sTripStatus = CGlobals_lib.getInstance().getSharedPreferences(getApplicationContext())
                .getString(Constants_lib.PREF_TRIP_ACTION,
                        Constants_lib.TRIP_ACTION_NONE);
        setTripAction(sTripStatus);

        CGlobals_lib.msTripAction = CGlobals_lib.getInstance().getSharedPreferences(getApplicationContext())
                .getString(Constants_lib.PREF_TRIP_ACTION,
                        Constants_lib.TRIP_ACTION_NONE);

        if (TextUtils.isEmpty(CGlobals_lib.msTripAction)) {
            setTripAction(Constants_lib.TRIP_ACTION_NONE);
        } else {
            setTripAction(CGlobals_lib.msTripAction);
        }
        if (isInTrip()) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                    new IntentFilter(Constants_lib.PASSENGER_EVENT));
            miPassengerUpdateInterval = Constants_lib.PASSENGER_UPDATE_INTERVAL_ACTIVE;
        } else {
            miPassengerUpdateInterval = Constants_lib.PASSENGER_UPDATE_INTERVAL_PASSIVE;
        }
        Location loc = CGlobals_lib.getInstance().getMyLocation(this);
        if (loc != null && !CGlobals_lib.inDriverTrip) {
            mBounds.include(new LatLng(loc.getLatitude(), loc.getLongitude()));
        }
        msTripPath = CGlobals_lib.getInstance().getSharedPreferences(getApplicationContext())
                .getString(Constants_lib.TRIP_PATH, "");
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        this.getWindow().setAttributes(params);
        super.onResume();
        mButtonStart.setVisibility(View.GONE);
    } // onResumed

    private void updateBand() {
        if (isInTrip()) {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            int dpValue = 95;
            float d = Driver_act.this.getResources().getDisplayMetrics().density;
            int margin = (int) (dpValue * d);
            lp.setMargins(0, 0, 0, margin);
            findViewById(R.id.map).setLayoutParams(lp);
            mFlPassengerInfo.setVisibility(View.VISIBLE);
        } else {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            int dpValue = 0;
            float d = Driver_act.this.getResources().getDisplayMetrics().density;
            int margin = (int) (dpValue * d);
            lp.setMargins(0, 0, 0, margin);
            findViewById(R.id.map).setLayoutParams(lp);
            mFlPassengerInfo.setVisibility(View.GONE);
        }
    }


    @Override
    public void onBackPressed() {
        if (startDialogFlag) {
            mRlFromTo.setVisibility(View.VISIBLE);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            int dpValue = 0;
            float d = Driver_act.this.getResources().getDisplayMetrics().density;
            int margin = (int) (dpValue * d);
            lp.setMargins(0, 0, 0, margin);
            findViewById(R.id.map).setLayoutParams(lp);
            mFlCreateTrip.setVisibility(View.GONE);
            mMap.clear();
            isFromSelected = false;
            if (!isFromSelected) {
                mTvSetLocation.setText(" Set Destination location ");
                mIvGreenPin.setVisibility(View.GONE);
                mIvRedPin.setVisibility(View.VISIBLE);
                setFrom(moFromAddress.getAddress(),
                        new LatLng(moFromAddress.getLatitude(), moFromAddress
                                .getLongitude()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                                moToAddress.getLatitude(), moToAddress.getLongitude()),
                        DEFAULT_ZOOM));
            }
            startDialogFlag = false;
        } else if (!isInTrip()) {
            super.onBackPressed();
            return;
        }
        Toast.makeText(this, "Presss home to switch to a different app",
                Toast.LENGTH_SHORT).show();
    }

    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkInternetGPS();
            String message = intent.getStringExtra("message");
            Log.d("receiver", "Got message: " + message);
            updateBand();
            checkEverythingOk();
            if (!isInTrip()) {
                doubleBackToExitPressedOnce = false;
            }
            int iStatus = intent.getIntExtra("status", 0);
            String response = intent.getStringExtra("response");
            double passengerDistanceJumpInOut = intent.getDoubleExtra("passengerDistance", 0.0);

            if (iStatus > 0) {
                CGlobals_lib.getInstance().getSharedPreferencesEditor(Driver_act.this)
                        .putString(Constants_lib.PREF_SAVE_TRIP_CREATE_RESPONSE, response);
                CGlobals_lib.getInstance().getSharedPreferencesEditor(Driver_act.this).
                        putString(Constants_lib.PREF_PASSENGER_JUMPINOUT_DISTANCE, String.valueOf(passengerDistanceJumpInOut));
                CGlobals_lib.getInstance().getSharedPreferencesEditor(Driver_act.this).commit();
                if (TextUtils.isEmpty(response)) {
                    String resultres = CGlobals_lib.getInstance().getSharedPreferences(Driver_act.this)
                            .getString(Constants_lib.PREF_SAVE_TRIP_CREATE_RESPONSE, "");
                    if (!TextUtils.isEmpty(resultres)) {
                        showActivePassengerMarkers(resultres);
                    } else {
                        showActivePassengerMarkers(response);
                    }
                } else {
                    showActivePassengerMarkers(response);
                }
                Log.i("iStatus", "Internet gone");
            } else {
                Log.i("iStatus", "Internet on");
            }
        }
    };

    protected abstract void showActivePassengerMarkers(String response);

    protected void callDriver(JSONArray aoPoint) {
        try {
            oDirectionPoints = new JSONObject();
            oDirectionPoints.put("path", aoPoint);
            if (oDirectionPoints.toString() != null) {
                if (mIsDirectTrip) {
                    createTrip(oDirectionPoints.toString());
                } else {
                    progressCancel();
                    mLlMarkerLayout.setVisibility(View.GONE);
                    mFlCreateTrip.startAnimation(slideup);
                    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT);
                    int dpValue = 130;
                    float d = Driver_act.this.getResources().getDisplayMetrics().density;
                    int margin = (int) (dpValue * d);
                    lp.setMargins(0, 0, 0, margin);
                    findViewById(R.id.map).setLayoutParams(lp);
                    mFlCreateTrip.setVisibility(View.VISIBLE);
                    startDialogFlag = true;
                }
            } else {
                showStatusMessages("Create trip failed, please try again");
                Toast.makeText(Driver_act.this, "Create trip failed, please try again", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class TimePickerFragment extends DialogFragment implements
            TimePickerDialog.OnTimeSetListener {
        private Calendar startDateTime;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            DialogFragment dialogFragment = new DialogFragment();
            dialogFragment.setCancelable(false);
            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            String sTime = String.format(Locale.getDefault(), "%d:%02d",
                    hourOfDay, minute);

            startDateTime = Calendar.getInstance();
            startDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            startDateTime.set(Calendar.MINUTE, minute);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault());
            CGlobals_lib.getInstance().getSharedPreferencesEditor(getActivity()).
                    putString("plannedstartdatetime", df.format(startDateTime.getTime()));
            CGlobals_lib.getInstance().getSharedPreferencesEditor(getActivity()).commit();
            if (((Driver_act) getActivity()).oDirectionPoints.toString() != null) {
                ((Driver_act) getActivity()).createTrip(((Driver_act) getActivity()).oDirectionPoints.toString());
            } else {
                ((Driver_act) getActivity()).showStatusMessages("Create trip failed, please try again");
                Toast.makeText(getActivity(), "Create trip failed, please try again", Toast.LENGTH_SHORT).show();
            }

        }
    }

    protected boolean goDirections() {
        if (!moToAddress.hasLatitude() && !moToAddress.hasLongitude()) {
            Toast.makeText(Driver_act.this,
                    "Please enter your start and destination address",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        flag_btnCreateTrip = true;
        float results[] = new float[1];
        Location.distanceBetween(moFromAddress.getLatitude(), moFromAddress.getLongitude(),
                moToAddress.getLatitude(), moToAddress.getLongitude(), results);
        if (results[0] < Constants_lib.DISTANCE_SAME_FROM) {
            Toast.makeText(Driver_act.this,
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
            saveFromTo();
            mBounds.include(
                    new LatLng(moFromAddress.getLatitude(), moFromAddress
                            .getLongitude())).include(
                    new LatLng(moToAddress.getLatitude(), moToAddress
                            .getLongitude()));
            try {
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                        mBounds.build(), Constants_lib.MAP_PADDING));
            } catch (Exception e) {
                e.printStackTrace();
            }
            mInDirectionMode = true;
            SSLog.i(TAG, "Plotting directions");

            String url = Driver_act.this.getDirectionsUrl(
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
                if (moFromAddress.getAddress().length() < 20) {
                    fromaddss = moFromAddress.getAddress();
                } else {
                    fromaddss = moFromAddress.getAddress().substring(0, 20);
                    fromaddss = fromaddss + "...";
                }
            }
            bmp = tc.makeIcon(fromaddss);
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(moFromAddress.getLatitude() + .001, moFromAddress.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromBitmap(bmp)));
            if (!TextUtils.isEmpty(moToAddress.getSubLocality1())) {
                toaddss = moToAddress.getSubLocality1();
            } else {
                if (moToAddress.getAddress().length() < 20) {
                    toaddss = moToAddress.getAddress();
                } else {
                    toaddss = moToAddress.getAddress().substring(0, 20);
                    toaddss = toaddss + "...";
                }
            }
            bmp = tc.makeIcon(toaddss);
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(moToAddress.getLatitude() + .001, moToAddress.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromBitmap(bmp)));

            GoogleDirection downloadTask = new GoogleDirection();
            // Start downloading json data from Google Directions API
            downloadTask.execute(url);
            return true;
        } else {
            Toast.makeText(Driver_act.this,
                    "Missing from or to. Cannot create trip",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    } // goDirections

    /**
     * A method to download json data from url
     */

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

    public void onClickZoomActiveUsers(View v) {
        Location location = CGlobals_lib.getInstance().getMyLocation(Driver_act.this);
        mCurrentLocation = CGlobals_lib.getInstance().isBetterLocation(location, mCurrentLocation) ? location
                : mCurrentLocation;
        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        if (CGlobals_lib.getInstance().getMyLocation(Driver_act.this) != null) {
            bounds.include(new LatLng(CGlobals_lib.getInstance().getMyLocation(Driver_act.this)
                    .getLatitude(), CGlobals_lib.getInstance().getMyLocation(Driver_act.this)
                    .getLongitude()));
        } else {
            bounds.include(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
        }
        if (mNearestLat != Constants_lib.INVALIDLAT && mNearestLng != Constants_lib.INVALIDLNG) {
            bounds.include(new LatLng(mNearestLat, mNearestLng));
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(),
                BOUNDS_PADDING));
    }

    public void setNearestPassengerLatLng(double nearestLat, double nearestLng) {
        this.mNearestLat = nearestLat;
        this.mNearestLng = nearestLng;
    }

    public void onClickZoomTrip(View v) {
        try {
            if (mZoomFitBounds != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                        mZoomFitBounds.build(), 40));

            }
        } catch (Exception e) {
            SSLog.e(TAG, "onClickZoomTrip", e);
        }
    }


    public void onClickPassenger(View v) {

        final Dialog dialog = new Dialog(Driver_act.this);
        dialog.setContentView(R.layout.trip_passenger_info);
        dialog.setTitle("Passenger");

        float LeastDistanceFromMe = -1;
        final ListView mlvPassengerList;
        String response = CGlobals_lib.getInstance().getSharedPreferences(Driver_act.this)
                .getString(Constants_lib.PREF_SAVE_TRIP_CREATE_RESPONSE, "");
        try {
            JSONArray majActiveUsers = new JSONArray(response);
            mResponseTrips = new ArrayList<CTrip>();
            JSONObject jPassenger = new JSONObject();
            int nPassengers = majActiveUsers.length();
            for (int j = 0; j < nPassengers; j++) {
                jPassenger = majActiveUsers.getJSONObject(j);
                mResponsePassengerTrip = new CTrip(majActiveUsers.getJSONObject(j)
                        .toString(), getApplicationContext());
                mResponseTrips.add(mResponsePassengerTrip);

            }

            mlvPassengerList = (ListView) dialog.findViewById(R.id.lvPassengerListinfo);
            mlvPassengerList.setAdapter(new PassengerAdapter(Driver_act.this, mResponseTrips, profileImageUrl(), passengertripsummayUrl(), dialog));
            dialog.show();

            mlvPassengerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    CTrip value = mResponseTrips.get(i);
                    showResponseThumbImageClick(value);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract void showResponseThumbImageClick(CTrip cTrip);

    protected abstract void getInformation();

    public void onClickInformation(View v) {

        getInformation();

    }

    public void onClickZoomVehicle(View v) {
    }

    // gets the nearest node on the polyline based on lat, lng passed
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

    protected void setTripAction(String sTripStatus) {
        mButtonGo.setVisibility(View.GONE);
        mButtonPause.setVisibility(View.GONE);
        mButtonResume.setVisibility(View.GONE);
        mButtonEndTrip.setVisibility(View.GONE);
        mRlFromTo.setVisibility(View.GONE);
        mRlTripActive.setVisibility(View.GONE);
        mRlScheduledTrip.setVisibility(View.GONE);
        mLlMarkerLayout.setVisibility(View.GONE);
        hideViews();
        if (sTripStatus.equals(Constants_lib.TRIP_ACTION_NONE)
                && isInTrip()) {
            sTripStatus = Constants_lib.TRIP_ACTION_BEGIN;
        }
        if (sTripStatus.equals(Constants_lib.TRIP_ACTION_CREATE)
                || sTripStatus.equals(Constants_lib.TRIP_ACTION_NONE)
                || sTripStatus.equals(Constants_lib.TRIP_ACTION_END)
                || sTripStatus.equals(Constants_lib.TRIP_ACTION_ABORT)) {
            mRlFromTo.setVisibility(View.VISIBLE);
            mLlMarkerLayout.setVisibility(View.VISIBLE);
            CGlobals_lib.getInstance().getSharedPreferencesEditor(getApplicationContext())
                    .putBoolean(Constants_lib.PREF_IN_TRIP, false);
            CGlobals_lib.getInstance().getSharedPreferencesEditor(getApplicationContext())
                    .commit();
            setTripActionFloatingbuttonHide();
            StopLocationService();
        } else if (sTripStatus.equals(Constants_lib.TRIP_ACTION_BEGIN)
                || sTripStatus.equals(Constants_lib.TRIP_ACTION_RESUME)) {
            mButtonPause.setVisibility(View.VISIBLE);
            mRlTripActive.setVisibility(View.VISIBLE);
        } else if (sTripStatus.equals(Constants_lib.TRIP_ACTION_SCHEDULE)
                || sTripStatus.equals(Constants_lib.TRIP_ACTION_LATER)) {
            mRlScheduledTrip.setVisibility(View.VISIBLE);
        } else if (sTripStatus.equals(Constants_lib.TRIP_ACTION_PAUSE)) {
            mRlTripActive.setVisibility(View.VISIBLE);
            mButtonResume.setVisibility(View.VISIBLE);
            mButtonEndTrip.setVisibility(View.VISIBLE);
        }
        CGlobals_lib.getInstance().getSharedPreferencesEditor(getApplicationContext())
                .putString(Constants_lib.PREF_TRIP_ACTION, sTripStatus);
        CGlobals_lib.getInstance().getSharedPreferencesEditor(getApplicationContext()).commit();
    } // setTripAction


    void showTrip() {
        if (!setFromTo()) {
            Toast.makeText(getApplicationContext(),
                    "Sorry Something went wrong with this trip.",
                    Toast.LENGTH_SHORT).show();
            setTripAction(Constants_lib.TRIP_ACTION_ABORT);
            return;

        }

        Location location = CGlobals_lib.getInstance().getBestLocation(this, mCurrentLocation);
        mCurrentLocation = CGlobals_lib.getInstance().isBetterLocation(location, mCurrentLocation) ? location
                : mCurrentLocation;
        if (mCurrentLocation != null && moToAddress.hasLatitude()
                && moToAddress.hasLongitude()) {
            if (hasTripEnded(mCurrentLocation,
                    moToAddress.getLatitude(), moToAddress.getLongitude())) {
                endTrip();
            }
        }
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
                setFromTo();
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


    protected Location getMyLocation() {
        return mCurrentLocation;
    }

    private void writeRecentTrip() {
        new Thread(new Runnable() {
            public void run() {
                if (maoRecentTrips == null) {
                    return;
                }

                int iLen = maoRecentTrips.size();
                if (iLen < 1)
                    return;

                JSONArray aJson = new JSONArray();
                for (int i = 0; i < iLen; i++) {
                    aJson.put(maoRecentTrips.get(i).toJSon());

                }

                CGlobals_lib.getInstance().getSharedPreferencesEditor(getApplicationContext())
                        .putString(Constants_lib.PREF_RECENT_TRIP,
                                aJson.toString());
                CGlobals_lib.getInstance().getSharedPreferencesEditor(getApplicationContext())
                        .commit();
            }
        }).start();
    } // writeRecentAddresses

    protected void addRecentTrip(CTrip oTrip) {
        maoRecentTrips = CGlobals_lib.getInstance().readRecentTrip(Driver_act.this);
        maoRecentTrips.add(0, oTrip);
        int iLen = maoRecentTrips.size();
        if (iLen > Constants_lib.MAXTRIPS)
            maoRecentTrips.remove(iLen - 1);
        // remove the same Station lower in the list
        iLen = maoRecentTrips.size();
        CTrip sf;
        for (int i = 1; i < iLen; i++) {
            sf = maoRecentTrips.get(i);
            if (sf.sameAs(oTrip)) {
                maoRecentTrips.remove(i);
                break;
            }
        }
        writeRecentTrip();
    } // addRecentStop


    protected void gotGoogleMapLocation(Location location) {
        mCurrentLocation = CGlobals_lib.getInstance().isBetterLocation(location, mCurrentLocation) ? location
                : mCurrentLocation;
        if (mCurrentLocation != null) {
            CGlobals_lib.getInstance().getSharedPreferencesEditor(Driver_act.this).
                    putFloat(Constants_lib.PREF_MYLOCATION_LAT, (float) mCurrentLocation.getLatitude());
            CGlobals_lib.getInstance().getSharedPreferencesEditor(Driver_act.this).
                    putFloat(Constants_lib.PREF_MYLOCATION_LON, (float) mCurrentLocation.getLongitude());
            CGlobals_lib.getInstance().getSharedPreferencesEditor(Driver_act.this).commit();
        }
        if (isAutoRefreshFromLocation() && !isInTrip()) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                            location.getLatitude(), location.getLongitude()),
                    DEFAULT_ZOOM));
            setAutoRefreshFromLocation(false);
        }

    }

    protected boolean isInTrip() {
        return CGlobals_lib.getInstance().isInTrip(getApplicationContext());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String sAddrmarathi = "", sAddrEng = "", sAddr = "";
        double lat, lng;
        CAddress oAddr = null;
        boolean textToSpeechInstalled = true;
        if (requestCode == ACT_RESULT_TRIP_CREATED) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
            } else {
                textToSpeechInstalled = false;
            }
            if (!textToSpeechInstalled) {
                Intent install = new Intent();
                install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(install);
            }
        }
        if (requestCode == ACTRESULT_TO || requestCode == ACTRESULT_FROM) {
            if (resultCode == RESULT_OK) {
                flag_btnCreateTrip = true;
                sAddrEng = data.getStringExtra("street_address");
                sAddr = sAddrEng;
                lat = data.getDoubleExtra("lat", Constants_lib.INVALIDLAT);
                lng = data.getDoubleExtra("lng", Constants_lib.INVALIDLNG);

                if (lat != Constants_lib.INVALIDLAT
                        && lng != Constants_lib.INVALIDLNG) {
                    oAddr = new CAddress();
                    oAddr.setAddress(sAddr);
                    oAddr.setLatitude(lat);
                    oAddr.setLongitude(lng);
                    if (requestCode == ACTRESULT_FROM) {
                        moFromAddress = new CAddress(sAddr, "", lat, lng);
                        setFrom(moFromAddress, false);
                        isFromSelected = true;
                        flag_btnCreateTrip = false;
                    } else {
                        moToAddress = new CAddress(sAddr, "", lat, lng);
                        setTo(moToAddress, false);
                        isFromSelected = false;
                        flag_btnCreateTrip = false;
                    }
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                                    oAddr.getLatitude(), oAddr.getLongitude()),
                            DEFAULT_ZOOM));
                }
            }
            if (resultCode == RESULT_CANCELED) {
                // Write your code if there's no result
            }
        }
        if (!TextUtils.isEmpty(sAddr)) {
            CGlobals_lib.getInstance().addRecentAddress(oAddr);
        }

        if (requestCode == ACT_RESULT_INVITE_USER) {
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "DONE Invited User oR Open Trip");
            }
            if (resultCode == RESULT_CANCELED) {
                Log.i(TAG, "Failed Invited User oR Open Trip");
            }
        }
    } // onActivityResult


    boolean setFromTo() {

        String sFrom = CGlobals_lib.getInstance().getSharedPreferences(getApplicationContext())
                .getString(Constants_lib.PREF_FROM_ADDRESS, "");
        try {
            JSONObject jo = new JSONObject(sFrom);
            moFromAddress = new CAddress(jo);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        String sTo = CGlobals_lib.getInstance().getSharedPreferences(getApplicationContext())
                .getString(Constants_lib.PREF_TO_ADDRESS, "");
        try {
            JSONObject jo = new JSONObject(sTo);
            moToAddress = new CAddress(jo);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }

    void saveFromTo() {
        try {
            CGlobals_lib.getInstance().getSharedPreferencesEditor(getApplicationContext())
                    .putString(Constants_lib.PREF_FROM_ADDRESS, moFromAddress.toJSon().toString());
            CGlobals_lib.getInstance().getSharedPreferencesEditor(getApplicationContext())
                    .putString(Constants_lib.PREF_TO_ADDRESS, moToAddress.toJSon().toString());
            CGlobals_lib.getInstance().getSharedPreferencesEditor(getApplicationContext()).commit();
        } catch (Exception e) {
            SSLog.e(TAG, "saveFromTo - ", e);
        }

    }

    public void createTrip(final String jsonPath) {
        progressMessage("create trip...");
        mFlCreateTrip.startAnimation(slidedown);
        final String url = getTripUrl();
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        SSLog.d("ResponseTripId  ", response);
                        createTripResponse(response, jsonPath);
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                try {
                    SSLog.e(TAG, "createTrip - ", error.getMessage());
                    Toast.makeText(Driver_act.this.getBaseContext(),
                            "Create trip failed, please try again",
                            Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    SSLog.e(TAG, "createTrip - ", e);
                    Toast.makeText(Driver_act.this.getBaseContext(),
                            "Create trip failed, please try again",
                            Toast.LENGTH_SHORT).show();
                }
                progressCancel();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tripaction", Constants_lib.TRIP_ACTION_CREATE);
                params.put("triptype", getTripType());
                params.put("tripdistance", mDistance);
                params.put("triptime", String.valueOf(iDuration));
                params.put("fromaddress", moFromAddress.getAddress());
                params.put("fromshortaddress", moFromAddress.getShortAddress());
                params.put("fromsublocality", moFromAddress.getSubLocality());
                params.put("toaddress", moToAddress.getAddress());
                params.put("toshortaddress", moToAddress.getShortAddress());
                params.put("tosublocality", moToAddress.getSubLocality1());
                if (moFromAddress.getSubLocality1() != null) {
                    params.put("fromsublocality", moFromAddress.getSubLocality1());
                }
                params.put("fromlat", Double.toString(moFromAddress.getLatitude()));
                params.put("fromlng", Double.toString(moFromAddress.getLongitude()));
                params.put("tolat", Double.toString(moToAddress.getLatitude()));
                params.put("tolng", Double.toString(moToAddress.getLongitude()));
                params.put("trip_directions_polyline",
                        msTripPath.replace("\\", "\\\\"));
                Log.d(TAG, "tripPath: " + msTripPath.length());
                Log.d(TAG,
                        "after put: "
                                + params.get("trip_directions_polyline")
                                .length());


                params.put("lastinvited", String.valueOf(mLastInvited));

                params = CGlobals_lib.getInstance().getBasicMobileParams(params,
                        url, getApplicationContext());

                return CGlobals_lib.getInstance().checkParams(params);
            }

        };
        CGlobals_lib.getInstance().addVolleyRequest(postRequest, true, getApplicationContext());

    } // createTrip


    protected void createTripResponse(String response, String jsonPath) {
        int iTripId;
        if (TextUtils.isEmpty(response) || response.equals("-1")) {
            Toast.makeText(
                    getApplicationContext(),
                    getApplicationContext().getString(R.string.tripSetupFailed)
                            + getApplicationContext().getString(
                            R.string.pleaseTryAgain), Toast.LENGTH_LONG)
                    .show();
            progressCancel();
            return;
        } else {
            System.out.println("response createTripResponse   " + response);
            try {
                JSONObject oJson = new JSONObject(response);
                iTripId = oJson.isNull("trip_id") ? -1 : Integer.parseInt(oJson
                        .getString("trip_id"));
                CGlobals_lib.getInstance().getSharedPreferencesEditor(getApplicationContext())
                        .putInt(Constants_lib.PREF_TRIP_ID_INT, iTripId);
                CGlobals_lib.getInstance().getSharedPreferencesEditor(getApplicationContext())
                        .commit();

                int iTripFound = oJson.isNull("tripFound") ? 0 : Integer
                        .parseInt(oJson.getString("tripFound"));
                /*if (iTripFound == 0) {*/
                createTripPath(iTripId, jsonPath);

                /*} else {
                    showStartDialog(iTripId);
                }*/

                if (!mIsDirectTrip && !hasDriverMode) {
                    inviteUsers();
                }
                progressCancel();
                showStatusMessages("Checking for waiting passengers");
            } catch (Exception e) {
                SSLog.e(TAG, "createTripResponse -  ", e);
                Toast.makeText(getApplicationContext(),
                        "Server error. Please try again...", Toast.LENGTH_SHORT)
                        .show();
                progressCancel();
            }

        }
    } // createTripResponse

    public int getTripId() {
        return CGlobals_lib.getInstance().getSharedPreferences(getApplicationContext()).getInt(Constants_lib.PREF_TRIP_ID_INT, 0);
    }

    protected void createTripPath(final int iTripId, final String jsonPath) {
        progressMessage("create trip...");
        final String url = getTripPathUrl();
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        SSLog.d("ResponseTripId  ", response);
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                                Locale.getDefault());
                        Calendar cal = Calendar.getInstance();
                        CGlobals_lib.getInstance().getSharedPreferencesEditor(Driver_act.this)
                                .putString(Constants_lib.PREF_SCHEDULE_TRIP_CREATE, df.format(cal.getTime()));
                        CGlobals_lib.getInstance().getSharedPreferencesEditor(Driver_act.this).commit();
                        if (response.equals("-1")) {
                            cancelTripForNotCreatePath();
                            return;
                        }
                        showStartDialog(iTripId);
                        progressCancel();
                        showStatusMessages("Checking for waiting passengers");
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
                progressCancel();
                cancelTripForNotCreatePath();
                Toast.makeText(Driver_act.this, getString(R.string.couldNotCreateTrip), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("tripid", String.valueOf(iTripId));
                params.put("path", jsonPath);
                params = CGlobals_lib.getInstance().getMinMobileParams(getAppUserId(), getEmailId(), params, url);

                return CGlobals_lib.getInstance().checkParams(params);
            }

        };
        CGlobals_lib.getInstance().addVolleyRequest(postRequest, true, getApplicationContext());

    } // createTripPath

    public void resetDisplay() {
        mIvPassenger1.setVisibility(View.GONE);
        mTvJoinedCount.setVisibility(View.GONE);
        mtvJoinedCountNumber.setVisibility(View.GONE);
        mTvJumpedInCount.setVisibility(View.GONE);
        mLlWaitingPassenger.setVisibility(View.GONE);
        mLlJoinPassenger.setVisibility(View.GONE);
        mTvInfoBandLine1.setVisibility(View.GONE);
        mTvInfoBandLine2.setVisibility(View.GONE);
        mTvInfoBandLine3.setVisibility(View.GONE);
        mTvTime.setVisibility(View.GONE);
        mTvMins.setVisibility(View.GONE);
        mTvInfoBandLine4.setVisibility(View.GONE);
    }

    public void mapLoaded() {
        if (isInTrip()) {
            try {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                        mZoomFitBounds.build(), 50));
            } catch (Exception e) {

                SSLog.e(TAG, " onMapLoaded - ", e);
            }
        }

    }

    @SuppressWarnings("deprecation")
    protected void updateSignal(double dDistanceFromMe) {
        dDistanceFromMe = dDistanceFromMe * 1000;
        mLlWaitingPassenger.setVisibility(View.VISIBLE);
        if (dDistanceFromMe < 0) {
            return;
        }

        if (dDistanceFromMe < Constants_lib.DISTANCE_CLOSE) {
            mTvInfoBandLine1.setVisibility(View.VISIBLE);
            int iTransparency = 255 - (int) ((dDistanceFromMe / Constants_lib.DISTANCE_CLOSE) * 128);
            mLlWaitingPassenger.setBackgroundResource(R.drawable.red_background);
        } else if (dDistanceFromMe < Constants_lib.DISTANCE_NEAR) {
            mTvInfoBandLine1.setVisibility(View.VISIBLE);
            int iTransparency = 255 - (int) ((dDistanceFromMe / Constants_lib.DISTANCE_NEAR) * 128);
            mLlWaitingPassenger.setBackgroundResource(R.drawable.yellow_background);
        } else if (dDistanceFromMe < Constants_lib.DISTANCE_FAR) {
            mTvInfoBandLine1.setVisibility(View.VISIBLE);
            int iTransparency = 255 - (int) ((dDistanceFromMe / Constants_lib.DISTANCE_FAR) * 128);
            if (dDistanceFromMe > Constants_lib.DISTANCE_FAR) {
                iTransparency = 35;
            }
            mLlWaitingPassenger.setBackgroundResource(R.drawable.green_background);
        } else { // too far from me
            mLlWaitingPassenger.setBackgroundResource(R.drawable.gray_background);
        }
    }

    public void showStatusMessages(String msg) {
        findViewById(R.id.llOneLine).bringToFront();
        ((TextView) findViewById(R.id.tvOneLine1)).setText(msg);
    }

    public void showStatusMessages(String msg1, String msg2) {
        findViewById(R.id.llTwoLine).bringToFront();
        ((TextView) findViewById(R.id.tvTwoLine1)).setText(msg1);
        ((TextView) findViewById(R.id.tvTwoLine2)).setText(msg2);
    }

    public void showStatusMessages(String msg1, String msg2, String msg3) {
        findViewById(R.id.llThreeLine).bringToFront();
        ((TextView) findViewById(R.id.tvThreeLine1)).setText(msg1);
        ((TextView) findViewById(R.id.tvThreeLine2)).setText(msg2);
        ((TextView) findViewById(R.id.tvThreeLine3)).setText(msg3);
    }

    private void stupMap() {
        try {
            LatLng latLong;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            Location location = CGlobals_lib.getInstance().getMyLocation(Driver_act.this);
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
            // Clears all the existing markers
            mMap.clear();

            mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

                @Override
                public void onCameraChange(CameraPosition arg0) {
                    if (!isInTrip() && !flag_btnCreateTrip && !startDialogFlag) {
                        center = mMap.getCameraPosition().target;
                        LatLng latLng1 = new LatLng(center.latitude,
                                center.longitude);
                        mMap.clear();

                        mLlMarkerLayout.setVisibility(View.VISIBLE);
                        try {
                            if (isFromSelected) {
                                mTvSetLocation.setText(" Set Start location ");
                                mIvRedPin.setVisibility(View.GONE);
                                mIvGreenPin.setVisibility(View.VISIBLE);
                                flag_btnCreateTrip = true;
                                new GetLocationAsync(center.latitude, center.longitude,
                                        mTvFrom, true)
                                        .execute();

                                setTo(moToAddress.getAddress(),
                                        new LatLng(moToAddress.getLatitude(), moToAddress
                                                .getLongitude()));
                                Log.i("setTo", String.valueOf(moToAddress.getLatitude() + moToAddress.getLongitude()));
                            } else {
                                mTvSetLocation.setText(" Set Destination location ");
                                mIvGreenPin.setVisibility(View.GONE);
                                mIvRedPin.setVisibility(View.VISIBLE);
                                if (hasMovedMap) {
                                    new GetLocationAsync(center.latitude, center.longitude,
                                            mTvTo, false)
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
                    flag_btnCreateTrip = false;
                }
            });

            mLlMarkerLayout.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        double lat = center.latitude + .001;
                        LatLng latLng1 = new LatLng(center.latitude,
                                center.longitude);
                        if (hasGetaCabTrip()) {
                            mIsDirectTrip = true;
                            goDirections();
                        } else {
                            if (isFromSelected) {
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                                                lat, center.longitude),
                                        DEFAULT_ZOOM));
                                isFromSelected = false;
                                mIvRedPin.setVisibility(View.VISIBLE);
                                mIvGreenPin.setVisibility(View.GONE);
                            } else {
                                isFromSelected = true;
                                goDirections();
                            }
                        }
                    } catch (Exception e) {
                        Log.e("goDirections", e.toString());
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class GetLocationAsync extends AsyncTask<String, Void, String> {
        double x, y;
        StringBuilder str;
        TextView mTvAddress;
        boolean bFromSelected;

        public GetLocationAsync(double latitude, double longitude, TextView tvAddress,
                                boolean fromSelected) {
            x = latitude;
            y = longitude;
            mTvAddress = tvAddress;
            bFromSelected = fromSelected;
        }

        @Override
        protected void onPreExecute() {
            mTvAddress.setHint(" Getting location ");
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                geocoder = new Geocoder(Driver_act.this, Locale.ENGLISH);
                addresses = geocoder.getFromLocation(x, y, 1);
                str = new StringBuilder();
                if (geocoder.isPresent()) {
                    android.location.Address returnAddress = addresses.get(0);

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
                if (bFromSelected) {
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

    public void onClickTo(View v) {
        Intent i = new Intent(Driver_act.this,
                SearchAddress_act.class);
        i.putExtra("cc", sCountryCode);
        startActivityForResult(i, ACTRESULT_TO);
        if (!TextUtils.isEmpty(moFromAddress.getAddress())) {
            CGlobals_lib.getInstance().addRecentAddress(moFromAddress);
        }
    }

    protected void checkInternetGPS() {
        try {
            if (!mConnectivity.checkConnected(Driver_act.this)) {
            }
            if (CGlobals_lib.getInstance().checkConnected(
                    Driver_act.this)) {
                mIvNoConnection.setVisibility(View.GONE);
            } else {
                mIvNoConnection.setVisibility(View.VISIBLE);
            }
            Location location = CGlobals_lib.getInstance().getMyLocation(Driver_act.this);
            if (location == null) {
                location = mCurrentLocation;
            }
            if (location == null) {
                return;
            }
            long diff = System.currentTimeMillis() - location.getTime();
            if (location == null || diff > Constants_lib.DRIVER_UPDATE_INTERVAL * 15) {
                MyLocation myLocation = new MyLocation(
                        CGlobals_lib.mVolleyRequestQueue, Driver_act.this, mGoogleApiClient);
                myLocation.getLocation(this, locationResult);
                mIvNoLocation.setVisibility(View.VISIBLE);
            } else {
                mIvNoLocation.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {

            try {
                CGlobals_lib.getInstance().setMyLocation(location, false);
                if (location != null
                        && (moFromAddress.hasLatitude() && moFromAddress
                        .hasLongitude())
                        && isInTrip()) {
                    float results[] = new float[1];
                    Location.distanceBetween(moToAddress.getLatitude(),
                            moToAddress.getLongitude(), location.getLatitude(),
                            location.getLongitude(), results);
                    if (results[0] < Constants_lib.NEAR_DESTINATION_DISTANCE) {
                        setTripAction(Constants_lib.TRIP_ACTION_END);
                    }
                }
                CGlobals_lib.getInstance().sendUpdatePosition(getUpdatePositionUrl(), location, getApplicationContext());
            } catch (Exception e) {
                SSLog.e(TAG, "LocationResult", e);
            }

        }
    };

    public void cancelTripForNotCreatePath() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(
                Driver_act.this);
        builder1.setMessage("Could not create trip (Connection problems?), please try again");
        builder1.setCancelable(true);
        builder1.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        setTripAction(Constants_lib.TRIP_ACTION_END);
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        if (!Driver_act.this.isFinishing()) {
            alert11.show();
        }
    }


} // Driver_act
