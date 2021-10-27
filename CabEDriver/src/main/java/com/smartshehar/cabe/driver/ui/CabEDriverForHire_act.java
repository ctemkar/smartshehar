package com.smartshehar.cabe.driver.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
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
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.PolyUtil;
import com.smartshehar.cabe.driver.CGlobals_CED;
import com.smartshehar.cabe.driver.CTrip_CED;
import com.smartshehar.cabe.driver.Constants_CED;
import com.smartshehar.cabe.driver.DatabaseHandler_CabE;
import com.smartshehar.cabe.driver.Fixed_Address;
import com.smartshehar.cabe.driver.LocationService_cabe;
import com.smartshehar.cabe.driver.MyApplication_CED;
import com.smartshehar.cabe.driver.PassengerAdapter_Jump;
import com.smartshehar.cabe.driver.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import lib.app.util.CAddress;
import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;
import lib.app.util.Constants_lib_ss;
import lib.app.util.MyLocation;
import lib.app.util.SSLog_SS;
import lib.app.util.ui.AbstractMapFragment_lib_act;

/**
 * Created by jijo_soumen on 17/03/2016.
 * When driver has passenger in car then start trip
 */
public class CabEDriverForHire_act extends AbstractMapFragment_lib_act {

    private static String TAG = "CabEDriverForHire_act: ";
    GoogleApiClient googleApiClient = null;
    CoordinatorLayout coordinatorLayout;
    Snackbar snackbar;
    //LinearLayout llPassengerInformation;
    //public FrameLayout mFlPassengerInfo;
    //public static String ShopLat, ShopLong;
    protected boolean isFromSelected = true;
    Bitmap bmp = null;
    CTrip_CED moTripDriver;
    JSONObject oDirectionPoints;
    public long miPassengerUpdateInterval = 0;
    Button btnEndTrip, btnPauseSharingTrip, btnResumeTrip;
    //TextView tvTotalDistance;
    // Button btnCallPassenger, CallHelpCenter;
    ImageView btnCheckMapTrack, ivPassengerJump;
    String sCostValue = "", sDistance = "";
    int setBusyCreateTripDriver;
    Connectivity mConnectivity;
    DatabaseHandler_CabE db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cabedriverforhire_act);
        create();
        mConnectivity = new Connectivity();
        int iTripId = -1;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                iTripId = extras.getInt("tripid");
            }
        }
        if (iTripId > 0) {
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this)
                    .putInt(Constants_CED.PREF_TRIP_ID_INT, iTripId);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this)
                    .commit();
        }
        googleApiClient = new GoogleApiClient.Builder(CabEDriverForHire_act.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(CabEDriverForHire_act.this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
        MyLocation myLocation = new MyLocation(
                MyApplication_CED.mVolleyRequestQueue, CabEDriverForHire_act.this, googleApiClient);
        myLocation.getLocation(this, onLocationResult);
        init();
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .coordinatorLayout);
        setAutoRefreshFromLocation(true);
        Location loc = CGlobals_lib_ss.getInstance().getMyLocation(CabEDriverForHire_act.this);
        if (loc != null && !isInTrip()) {
            setLocation(loc);
            startIntentServiceCurrentAddress(loc);
        }
        btnPauseSharingTrip = (Button) findViewById(R.id.btnPauseSharingTrip);
        btnResumeTrip = (Button) findViewById(R.id.btnResumeTrip);
        btnEndTrip = (Button) findViewById(R.id.btnEndTrip);
        ivPassengerJump = (ImageView) findViewById(R.id.ivPassengerJump);
        btnEndTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRlFromTo.setVisibility(View.GONE);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this)
                        .putBoolean("ONE_GO_DIRECT", false);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this)
                        .putBoolean(Constants_CED.PREF_IS_CHECK_BOOK, false);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).commit();
                AlertDialog.Builder builder = new AlertDialog.Builder(CabEDriverForHire_act.this);
                builder.setMessage("Are you sure end this trip?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                setBusyCreateTripDriver = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                                        .getInt(Constants_CED.PERF_SET_BUSY_CREATE_TRIP_DRIVER, 0);
                                if (setBusyCreateTripDriver == 1) {
                                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this)
                                            .putInt(Constants_CED.PREF_SET_SWITCH_FLAG, 1);
                                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).commit();
                                } else if (setBusyCreateTripDriver == 2) {
                                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this)
                                            .putInt(Constants_CED.PREF_SET_SWITCH_FLAG, 2);
                                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).commit();
                                }
                                String sServiceCodeBookingDriver = CGlobals_lib_ss.getInstance().
                                        getPersistentPreference(CabEDriverForHire_act.this)
                                        .getString(Constants_CED.PERF_SERVICE_CODE_BOOKING_DRIVER,
                                                "");
                                if (sServiceCodeBookingDriver.equals(Constants_CED.SERVICE_CODE_BY) ||
                                        sServiceCodeBookingDriver.equals(Constants_CED.SERVICE_CODE_CC)
                                        || sServiceCodeBookingDriver.equals(Constants_CED.SERVICE_CODE_FT)) {
                                    if (setBusyCreateTripDriver == 1) {
                                        updateTripCost();
                                    } else {
                                        Intent intent33 = new Intent(CabEDriverForHire_act.this, DashBoard_Driver_act.class);
                                        startActivity(intent33);
                                        finish();
                                    }
                                } else {
                                    Intent intent33 = new Intent(CabEDriverForHire_act.this, DashBoard_Driver_act.class);
                                    startActivity(intent33);
                                    finish();
                                }
                                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this)
                                        .putString(Constants_lib_ss.TRIP_PATH, "");
                                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).
                                        putString("plannedstartdatetime", "");
                                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).commit();
                                CGlobals_lib_ss.msInTrip = "";
                                setTripAction(Constants_CED.TRIP_ACTION_END);
                                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                                        Locale.getDefault());
                                Calendar cal = Calendar.getInstance();
                                String sDateTime = df.format(cal.getTime());
                                Location location = CGlobals_lib_ss.getInstance().getMyLocation(CabEDriverForHire_act.this);
                                int appuserid = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                                        .getInt(Constants_lib_ss.PREF_APPUSERID, -1);
                                int tripid = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                                        .getInt(Constants_CED.PREF_TRIP_ID_INT, -1);
                                String sUserType = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this).
                                        getString(Constants_CED.CAB_TRIP_USER_TYPE_DRIVER, "");
                                db = new DatabaseHandler_CabE(CabEDriverForHire_act.this);
                                Log.i(TAG, "addTripAction: " + sDateTime);
                                db.addTripAction(location, tripid, appuserid, sUserType, Constants_CED.TRIP_ACTION_END,
                                        CGlobals_lib_ss.msGmail, sDateTime);
                                int setBusyCreateTripDriver = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                                        .getInt(Constants_CED.PERF_SET_BUSY_CREATE_TRIP_DRIVER, 0);
                                Log.i(TAG, "addFlagSetDriver: " + sDateTime);
                                if (setBusyCreateTripDriver == 2) {
                                    db.addFlagSetDriver(String.valueOf(2), sDateTime, appuserid, CGlobals_lib_ss.msGmail,
                                            CGlobals_lib_ss.mIMEI);
                                } else {
                                    db.addFlagSetDriver(String.valueOf(1), sDateTime, appuserid, CGlobals_lib_ss.msGmail,
                                            CGlobals_lib_ss.mIMEI);
                                }
                                mMap.clear();
                                isTripFrozen = false;
                                StopLocationService();
                                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this)
                                        .putString(Constants_CED.PREF_CAB_E_TRIP_PATH_POLYLINE, "");
                                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).commit();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

                try {
                    String response = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this).
                            getString("RECEIVER_RESPONSE", "");
                    int errorvalue = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this).
                            getInt("RECEIVER_ERROR_VALUE", 0);
                    if (errorvalue == 1) {
                        if (TextUtils.isEmpty(response) || response.equals("-1")) {
                            return;
                        }
                        mPassengerTripArray = new ArrayList<>();
                        JSONArray majActivePassenger = new JSONArray(response);
                        for (int i = 0; i < majActivePassenger.length(); i++) {
                            mCTPassengerTrip = new CTrip_CED(majActivePassenger.getJSONObject(i)
                                    .toString(), getApplicationContext());
                            SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                                    Locale.getDefault());
                            Calendar cal1 = Calendar.getInstance();
                            String sDateTime1 = df1.format(cal1.getTime());
                            int appuserid1 = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                                    .getInt(Constants_lib_ss.PREF_APPUSERID, -1);
                            int tripid1 = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                                    .getInt(Constants_CED.PREF_TRIP_ID_INT, -1);
                            Location location1 = CGlobals_lib_ss.getInstance().getMyLocation(CabEDriverForHire_act.this);
                            db = new DatabaseHandler_CabE(CabEDriverForHire_act.this);
                            db.addJumpInOutTrip(location1, appuserid1, tripid1, mCTPassengerTrip.getPassenger_Appuser_Id(),
                                    CGlobals_lib_ss.msGmail, sDateTime1, "jumpout");
                        }
                    }
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "Jumpout: ", e, CabEDriverForHire_act.this);
                }
            }
        });
        if (!isInTrip()) {
            startTrip();
        }
        btnCheckMapTrack = (ImageView) findViewById(R.id.btnCheckMapTrack);
        btnCheckMapTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String res = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                        .getString(Constants_CED.PREF_CAB_E_TRIP_PATH_POLYLINE, "");
                if (!TextUtils.isEmpty(res)) {
                    moTrip = new CTrip_CED(res, CabEDriverForHire_act.this);
                    String toLat = String.valueOf(moTrip.getToLat());
                    String toLng = String.valueOf(moTrip.getToLng());
                    if (!TextUtils.isEmpty(toLat) && !TextUtils.isEmpty(toLng)) {
                        Intent intentNav = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("google.navigation:q=" + toLat + "," + toLng));
                        startActivity(intentNav);
                    }
                } else {
                    String response = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this).
                            getString(Constants_CED.PREF_CURRENT_TRIP, "");
                    moTrip = new CTrip_CED(response, CabEDriverForHire_act.this);
                    String toLat = String.valueOf(moTrip.getToLat());
                    String toLng = String.valueOf(moTrip.getToLng());
                    if (!TextUtils.isEmpty(toLat) && !TextUtils.isEmpty(toLng)) {
                        Intent intentNav = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("google.navigation:q=" + toLat + "," + toLng));
                        startActivity(intentNav);
                    }
                }
            }
        });
        setTripAction(Constants_CED.TRIP_ACTION_BEGIN);
        btnPauseSharingTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTripAction(Constants_CED.TRIP_ACTION_PAUSE);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                String sDateTime = df.format(cal.getTime());
                Location location = CGlobals_lib_ss.getInstance().getMyLocation(CabEDriverForHire_act.this);
                int appuserid = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                        .getInt(Constants_lib_ss.PREF_APPUSERID, -1);
                int tripid = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                        .getInt(Constants_CED.PREF_TRIP_ID_INT, -1);
                String sUserType = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this).
                        getString(Constants_CED.CAB_TRIP_USER_TYPE_DRIVER, "");
                db = new DatabaseHandler_CabE(CabEDriverForHire_act.this);
                db.addTripAction(location, tripid, appuserid, sUserType, Constants_CED.TRIP_ACTION_PAUSE,
                        CGlobals_lib_ss.msGmail, sDateTime);
            }
        });
        btnResumeTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTripAction(Constants_CED.TRIP_ACTION_RESUME);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                String sDateTime = df.format(cal.getTime());
                Location location = CGlobals_lib_ss.getInstance().getMyLocation(CabEDriverForHire_act.this);
                int appuserid = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                        .getInt(Constants_lib_ss.PREF_APPUSERID, -1);
                int tripid = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                        .getInt(Constants_CED.PREF_TRIP_ID_INT, -1);
                String sUserType = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this).
                        getString(Constants_CED.CAB_TRIP_USER_TYPE_DRIVER, "");
                db = new DatabaseHandler_CabE(CabEDriverForHire_act.this);
                db.addTripAction(location, tripid, appuserid, sUserType, Constants_CED.TRIP_ACTION_RESUME,
                        CGlobals_lib_ss.msGmail, sDateTime);
            }
        });
    }

    protected void setTripAction(String sTripStatus) {
        final String sUserType = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this).
                getString(Constants_CED.CAB_TRIP_USER_TYPE_DRIVER, "");
        mRlFromTo.setVisibility(View.GONE);
        mLlMarkerLayout.setVisibility(View.GONE);
        hideViews();
        if (sTripStatus.equals(Constants_CED.TRIP_ACTION_NONE)
                && isInTrip()) {
            sTripStatus = Constants_CED.TRIP_ACTION_BEGIN;
        }
        switch (sTripStatus) {
            case Constants_CED.TRIP_ACTION_CREATE:

                if (sUserType.equals(Constants_CED.TRIP_TYPE_SHARE_DRIVER)) {
                    btnPauseSharingTrip.setVisibility(View.GONE);
                    btnResumeTrip.setVisibility(View.GONE);
                } else {
                    btnPauseSharingTrip.setVisibility(View.GONE);
                    btnResumeTrip.setVisibility(View.GONE);
                }

                mRlFromTo.setVisibility(View.GONE);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).putBoolean(
                        Constants_CED.PREF_IN_TRIP, true);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).commit();
            case Constants_CED.TRIP_ACTION_END:

                if (sUserType.equals(Constants_CED.TRIP_TYPE_SHARE_DRIVER)) {
                    btnPauseSharingTrip.setVisibility(View.GONE);
                    btnResumeTrip.setVisibility(View.GONE);
                } else {
                    btnPauseSharingTrip.setVisibility(View.GONE);
                    btnResumeTrip.setVisibility(View.GONE);
                }

                mRlFromTo.setVisibility(View.GONE);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).putBoolean(
                        Constants_CED.PREF_IN_TRIP, false);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).commit();
            case Constants_CED.TRIP_ACTION_ABORT:

                if (sUserType.equals(Constants_CED.TRIP_TYPE_SHARE_DRIVER)) {
                    btnPauseSharingTrip.setVisibility(View.GONE);
                    btnResumeTrip.setVisibility(View.GONE);
                } else {
                    btnPauseSharingTrip.setVisibility(View.GONE);
                    btnResumeTrip.setVisibility(View.GONE);
                }

                mRlFromTo.setVisibility(View.GONE);
                mLlMarkerLayout.setVisibility(View.VISIBLE);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this)
                        .putBoolean(Constants_CED.PREF_IN_TRIP, false);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this)
                        .commit();
                StopLocationService();
                btnEndTrip.setVisibility(View.GONE);
                break;
            case Constants_CED.TRIP_ACTION_BEGIN:

                if (sUserType.equals(Constants_CED.TRIP_TYPE_SHARE_DRIVER)) {
                    btnPauseSharingTrip.setVisibility(View.VISIBLE);
                    btnResumeTrip.setVisibility(View.GONE);
                } else {
                    btnPauseSharingTrip.setVisibility(View.GONE);
                    btnResumeTrip.setVisibility(View.GONE);
                }

                mRlFromTo.setVisibility(View.GONE);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).putBoolean(
                        Constants_CED.PREF_IN_TRIP, true);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).commit();
                break;

            case Constants_CED.TRIP_ACTION_RESUME:

                if (sUserType.equals(Constants_CED.TRIP_TYPE_SHARE_DRIVER)) {
                    btnPauseSharingTrip.setVisibility(View.VISIBLE);
                    btnResumeTrip.setVisibility(View.GONE);
                } else {
                    btnPauseSharingTrip.setVisibility(View.GONE);
                    btnResumeTrip.setVisibility(View.GONE);
                }

                mRlFromTo.setVisibility(View.GONE);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).putBoolean(
                        Constants_CED.PREF_IN_TRIP, true);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).commit();
                break;

            case Constants_CED.TRIP_ACTION_PAUSE:

                if (sUserType.equals(Constants_CED.TRIP_TYPE_SHARE_DRIVER)) {
                    btnPauseSharingTrip.setVisibility(View.GONE);
                    btnResumeTrip.setVisibility(View.VISIBLE);
                } else {
                    btnPauseSharingTrip.setVisibility(View.GONE);
                    btnResumeTrip.setVisibility(View.GONE);
                }

                mRlFromTo.setVisibility(View.GONE);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).putBoolean(
                        Constants_CED.PREF_IN_TRIP, true);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).commit();
                break;
        }
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this)
                .putString(Constants_CED.PREF_TRIP_ACTION, sTripStatus);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).commit();
    } // setTripAction

    @Override
    protected void onResume() {
        init();
        super.onResume();
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        this.getWindow().setAttributes(params);
        // MyService Broadcast
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageBroadcastReceiverDashBoard,
                new IntentFilter(Constants_CED.ERVICE_DRIVER_ALL_PHP));
        Intent intentResponseService = new Intent(Constants_CED.ERVICE_DRIVER_ALL_PHP);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentResponseService);

        if (isInTrip()) {
            if (!isLocationServiceRunning(LocationService_cabe.class)) {
                StartLocationService();
            }
            mIvRedPin.setVisibility(View.GONE);
        }
        /*String trippathpoly = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                .getString(Constants_CED.PREF_CAB_E_TRIP_PATH_POLYLINE, "");
        if (!TextUtils.isEmpty(trippathpoly)) {
            plotPathPolyLine(trippathpoly);
        }*/
        mRlFromTo.setVisibility(View.GONE);
        mLlMarkerLayout.setVisibility(View.GONE);
        mIvRedPin.setVisibility(View.GONE);
        String sTripAction = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                .getString(Constants_CED.PREF_TRIP_ACTION, "");
        setTripAction(sTripAction);
        setBusyCreateTripDriver = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                .getInt(Constants_CED.PERF_SET_BUSY_CREATE_TRIP_DRIVER, 0);
        if (setBusyCreateTripDriver != 2) {
            if (ivPassengerJump != null) {
                ivPassengerJump.setVisibility(View.GONE);
            }
        }
        showTrip();
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(this).
                putBoolean(Constants_CED.PREF_NOTIFICATION_CLEAR_FLAG, true);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(this).commit();
    }

    @Override
    public void onBackPressed() {
        snackbar = Snackbar.make(coordinatorLayout, "Presss home to switch to a different app", Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private boolean isLocationServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        miPassengerUpdateInterval = Constants_CED.PASSIVE_INTERVAL;
        bmp = null;
        if (mMap != null) {
            mMap.clear();
        }
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageBroadcastReceiverDashBoard);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bmp != null && !bmp.isRecycled()) {
            bmp.recycle();
            bmp = null;
        }
        System.gc();
    }

    protected boolean isInTrip() {
        return CGlobals_CED.getInstance().isInTrip(CabEDriverForHire_act.this);
    }

    public void onClickFrom(View v) {

    }

    public void onClickTo(View v) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CGlobals_lib_ss.REQUEST_LOCATION_LIB) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    Log.d(TAG, "YES");
                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    Log.d(TAG, "NO");
                    CGlobals_lib_ss.showGPSDialog = true;
                    break;
                default:
                    break;
            }
        }
    }

    private MyLocation.LocationResult onLocationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            try {
                CGlobals_lib_ss.setMyLocation(location, false, CabEDriverForHire_act.this);
            } catch (Exception e) {
                SSLog_SS.e(TAG, "LocationResult", e, CabEDriverForHire_act.this);
            }
        }
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
            Location location = CGlobals_lib_ss.getInstance().getMyLocation(CabEDriverForHire_act.this);
            if (location != null) {
                latLong = new LatLng(location
                        .getLatitude(), location
                        .getLongitude());
            } else {
                latLong = new LatLng(12.9667, 77.5667);
            }
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLong).zoom(15f).tilt(20).build();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            if (location != null) {
                mMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));
            }
            mMap.clear();
        } catch (Exception e) {
            SSLog_SS.e(TAG, "stupMap: ", e, CabEDriverForHire_act.this);
        }
    }

    @Override
    protected void callDriver(JSONArray jsonArray) {
        try {
            progressCancel();
            mMap.setPadding(0, 180, 0, 300);
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                    mZoomFitBounds.build(), 15));
            oDirectionPoints = new JSONObject();
            oDirectionPoints.put("path", jsonArray);
        } catch (JSONException e) {
            SSLog_SS.e(TAG, "callDriver: ", e, CabEDriverForHire_act.this);
        }
    }

    public int getTripId() {
        return CGlobals_lib_ss.getInstance().getPersistentPreference(getApplicationContext())
                .getInt(Constants_CED.PREF_TRIP_ID_INT, -1);
    }

    protected void StopLocationService() {
        stopService(new Intent(CabEDriverForHire_act.this, LocationService_cabe.class));
    }

    protected void startTrip() {
        progressMessage("Starting trip...");
        setTripAction(Constants_CED.TRIP_ACTION_BEGIN);

        if (!isLocationServiceRunning(LocationService_cabe.class)) {
            StartLocationService();
        }
        btnEndTrip.setVisibility(View.VISIBLE);
        mLlMarkerLayout.setVisibility(View.GONE);
        mIvRedPin.setVisibility(View.GONE);
        int tripid = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                .getInt(Constants_CED.PREF_TRIP_ID_INT, -1);
        int appuserid = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                .getInt(Constants_lib_ss.PREF_APPUSERID, -1);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        String sDateTime = df.format(cal.getTime());
        db = new DatabaseHandler_CabE(CabEDriverForHire_act.this);
        Log.i(TAG, "addGetTripPath: " + sDateTime);
        db.addGetTripPath(appuserid, tripid, CGlobals_lib_ss.msGmail, sDateTime, CGlobals_lib_ss.mIMEI);
        progressCancel();
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).putBoolean(
                Constants_CED.PREF_IN_TRIP, true);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).commit();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(Constants_CED.PASSENGER_EVENT));
        miPassengerUpdateInterval = Constants_CED.PASSENGER_UPDATE_INTERVAL_ACTIVE;
        progressCancel();
        Location location = CGlobals_lib_ss.getInstance().getMyLocation(CabEDriverForHire_act.this);
        String sUserType = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this).
                getString(Constants_CED.CAB_TRIP_USER_TYPE_DRIVER, "");
        Log.i(TAG, "addTripAction: " + sDateTime);
        db.addTripAction(location, tripid, appuserid, sUserType, Constants_CED.TRIP_ACTION_BEGIN,
                CGlobals_lib_ss.msGmail, sDateTime);
    }

    protected void StartLocationService() {
        stopService(new Intent(this, LocationService_cabe.class));
        Intent serviceIntent = new Intent(this, LocationService_cabe.class);
        if (moToAddress.hasLongitude() && moToAddress.hasLatitude()) {
            serviceIntent.putExtra("lat", moToAddress.getLatitude());
            serviceIntent.putExtra("lng", moToAddress.getLongitude());
        }
        serviceIntent.putExtra("tripid", getTripId());
        startService(serviceIntent);
    }

    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isInTrip()) {
                doubleBackToExitPressedOnce = false;
            }
            int errorvalue = intent.getIntExtra("errorvalue", 0);
            String response = intent.getStringExtra("response");
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).
                    putString("RECEIVER_RESPONSE", response);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).
                    putInt("RECEIVER_ERROR_VALUE", errorvalue);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).commit();
            new getCabEDriverForHire().execute("");
        }
    };

    public BroadcastReceiver mMessageBroadcastReceiverDashBoard = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String response = intent.getStringExtra("newresponse");
            int errorvalue = intent.getIntExtra("newerrorvalue", 0);
            if (errorvalue == 3) {
                showTripPathLine(response);
            }
        }
    };

    private class getCabEDriverForHire extends AsyncTask<String, Integer, String> {
        boolean isInternetCheck = false, isLocationCheck = false;
        String response;
        CTrip_CED cTrip_cedDriver;
        Location location;

        @Override
        protected String doInBackground(String... params) {
            response = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this).
                    getString(Constants_CED.PREF_CURRENT_TRIP, "");
            try {
                if (TextUtils.isEmpty(response)) {
                    return null;
                }
                if (response.equals("-1")) {
                    return null;
                }

                cTrip_cedDriver = new CTrip_CED(response, CabEDriverForHire_act.this);
                isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEDriverForHire_act.this);
                location = CGlobals_lib_ss.getInstance().getMyLocation(getApplicationContext());
                if (location == null) {
                    location = mCurrentLocation;
                }
                if (location == null) {
                    return null;
                }
                long diff = System.currentTimeMillis() - location.getTime();
                isLocationCheck = diff <= Constants_lib_ss.DRIVER_UPDATE_INTERVAL * 10;
                /*if (isInTrip()) {
                    boolean isBeginWithInternet = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                            .getBoolean(Constants_CED.PREF_TRIP_ACTION_WITH_INTERNET, true);
                    if (!isBeginWithInternet) {
                        CGlobals_CED.getInstance().sendTripActionSaneTime(Constants_CED.TRIP_ACTION_BEGIN, CabEDriverForHire_act.this);
                    }
                }*/
            } catch (Exception e) {
                SSLog_SS.e(TAG, "getCabEMainDisplay: ", e, CabEDriverForHire_act.this);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (!isInternetCheck) {
                    mIvNoConnection.setVisibility(View.GONE);
                } else {
                    mIvNoConnection.setVisibility(View.VISIBLE);
                }
                if (!isLocationCheck) {
                    mIvNoLocation.setVisibility(View.VISIBLE);
                } else {
                    mIvNoLocation.setVisibility(View.GONE);
                }
                // tvTotalDistance.setText("");
               /* btnCallPassenger.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!TextUtils.isEmpty(moTripDriver.getPhoneNo())) {
                            Intent callIntent = new Intent(Intent.ACTION_DIAL);
                            callIntent.setData(Uri.parse("tel:" + cTrip_cedDriver.getPhoneNo()));
                            if (ActivityCompat.checkSelfPermission(CabEDriverForHire_act.this,
                                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            startActivity(callIntent);
                        }
                    }
                });
                CallHelpCenter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar = Snackbar.make(coordinatorLayout, "Call  Help Center", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                });*/
                if (!isInternetCheck) {
                    if (location.getLatitude() != 0.0 && location.getLongitude() != 0.0) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),
                                DEFAULT_ZOOM));
                    }
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG, "DriverForHire: ", e, CabEDriverForHire_act.this);
            }
            super.onPostExecute(result);
        }
    }

    @Override
    protected String getGeoCountryCode() {
        return MyApplication_CED.getInstance().getPersistentPreference()
                .getString(Constants_CED.PREF_CURRENT_COUNTRY, "");
    }

    @Override
    protected void gotGoogleMapLocation(Location location) {
        mCurrentLocation = CGlobals_lib_ss.isBetterLocation(location, mCurrentLocation) ? location
                : mCurrentLocation;
        if (mCurrentLocation != null) {
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).
                    putFloat(Constants_CED.PREF_MYLOCATION_LAT, (float) mCurrentLocation.getLatitude());
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).
                    putFloat(Constants_CED.PREF_MYLOCATION_LON, (float) mCurrentLocation.getLongitude());
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).commit();
        }
        if (isAutoRefreshFromLocation() && !isInTrip()) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                            location.getLatitude(), location.getLongitude()),
                    DEFAULT_ZOOM));
            setAutoRefreshFromLocation(false);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void mapReady() {
        stupMap();
        String passengerinfo = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this).
                getString(Constants_CED.PREF_CURRENT_TRIP, "");
        moTripDriver = new CTrip_CED(passengerinfo, CabEDriverForHire_act.this);
        isFromSelected = false;
        mRlFromTo.setVisibility(View.GONE);
        mLlMarkerLayout.setVisibility(View.GONE);
        if (isInTrip()) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                    new IntentFilter(Constants_CED.PASSENGER_EVENT));
            miPassengerUpdateInterval = Constants_CED.PASSENGER_UPDATE_INTERVAL_ACTIVE;
        } else {
            miPassengerUpdateInterval = Constants_CED.PASSENGER_UPDATE_INTERVAL_PASSIVE;
        }
        String sTripStatus = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                .getString(Constants_CED.PREF_TRIP_ACTION,
                        Constants_CED.TRIP_ACTION_NONE);
        setTripAction(sTripStatus);

        CGlobals_CED.msTripAction = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                .getString(Constants_CED.PREF_TRIP_ACTION,
                        Constants_CED.TRIP_ACTION_NONE);
        isTripFrozen = isInTrip();
        MyLocation myLocation = new MyLocation(
                MyApplication_CED.mVolleyRequestQueue, CabEDriverForHire_act.this, mGoogleApiClient);
        myLocation.getLocation(this, onLocationResult);
        CGlobals_lib_ss.getInstance().turnGPSOn1(CabEDriverForHire_act.this, mGoogleApiClient);
        if (isInTrip()) {
            if (!isLocationServiceRunning(LocationService_cabe.class)) {
                StartLocationService();
            }
           /* llPassengerInformation = (LinearLayout) findViewById(R.id.llPassengerInformation);
            if (llPassengerInformation == null) {
                mFlPassengerInfo = (FrameLayout) findViewById(R.id.flPassengerInfodriver);
                View hiddenPassengerInfo = getLayoutInflater().inflate(
                        R.layout.passengerinformation_item, mFlPassengerInfo, false);
                mFlPassengerInfo.addView(hiddenPassengerInfo);
                setBusyCreateTripDriver = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                        .getInt(Constants_CED.PERF_SET_BUSY_CREATE_TRIP_DRIVER, 0);
                if (setBusyCreateTripDriver == 1) {
                    llPassengerInformation = (LinearLayout) findViewById(R.id.llPassengerInformation);
                    if (llPassengerInformation != null) {
                        llPassengerInformation.setVisibility(View.GONE);
                    }
                }
                mFlPassengerInfo.setVisibility(View.VISIBLE);
                initPassenger();
            }*/
            new getCabEDriverForHire().execute("");
        }
        // checkInternetGPS();
        if (isInTrip()) {
            mLlMarkerLayout.setVisibility(View.GONE);
            btnEndTrip.setVisibility(View.VISIBLE);
        } else {
            btnEndTrip.setVisibility(View.GONE);
        }
        boolean isOneGoDirect = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                .getBoolean("ONE_GO_DIRECT", false);
        int setflag = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                .getInt(Constants_CED.PREF_SET_SWITCH_FLAG, -1);
        if (!isOneGoDirect) {
            if (setflag != 0) {
                goDirections();
            }
        }
    }

    private void updateTripCost() {
        try {
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this)
                    .putBoolean(Constants_CED.PREF_UPDATE_TRIP_COST_DISTANCE_DIALOG, false);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).commit();
            final Dialog dialog = new Dialog(CabEDriverForHire_act.this, R.style.AppCompatAlertDialogStyle);
            dialog.setContentView(R.layout.update_trip_cost);
            dialog.setCancelable(false);
            final EditText etCostValue, etDistance;
            final TextView tvCostSubmit;
            etCostValue = (EditText) dialog.findViewById(R.id.etCostValue);
            etDistance = (EditText) dialog.findViewById(R.id.etDistance);
            tvCostSubmit = (TextView) dialog.findViewById(R.id.tvCostSubmit);
            tvCostSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sCostValue = etCostValue.getText().toString();
                    sDistance = etDistance.getText().toString();
                    if (!TextUtils.isEmpty(sCostValue) && !TextUtils.isEmpty(sDistance)) {
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this)
                                .putBoolean(Constants_CED.PREF_UPDATE_TRIP_COST_DISTANCE_DIALOG, true);
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this)
                                .putString(Constants_CED.PREF_TOTAL_TRIP_COST, sCostValue);
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this)
                                .putString(Constants_CED.PREF_TOTAL_TRIP_DISTANCE, sDistance);
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).commit();
                        int tripid = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                                .getInt(Constants_CED.PREF_TRIP_ID_INT, -1);
                        String sUserType = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this).
                                getString(Constants_CED.CAB_TRIP_USER_TYPE_DRIVER, "");
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                                Locale.getDefault());
                        Calendar cal = Calendar.getInstance();
                        String sDateTime = df.format(cal.getTime());
                        int appuserid = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                                .getInt(Constants_lib_ss.PREF_APPUSERID, -1);
                        db = new DatabaseHandler_CabE(CabEDriverForHire_act.this);
                        db.addTripCostDistance(appuserid, tripid, sUserType, CGlobals_lib_ss.msGmail,
                                sDateTime, CGlobals_lib_ss.mIMEI,
                                sCostValue, sDistance);
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this)
                                .putInt(Constants_CED.PERF_SET_BUSY_CREATE_TRIP_DRIVER, 0);
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this)
                                .putBoolean(Constants_CED.PREF_TRIP_COST_DISTANCE_SUBMIT, true);
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).
                                putString(Constants_CED.CAB_TRIP_USER_TYPE_DRIVER, "");
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).commit();
                        Toast.makeText(CabEDriverForHire_act.this, "Trip has ended", Toast.LENGTH_SHORT).show();
                        Intent intent33 = new Intent(CabEDriverForHire_act.this, DashBoard_Driver_act.class);
                        startActivity(intent33);
                        finish();
                        dialog.getContext();
                        dialog.cancel();
                    } else {
                        customDialog("Please enter Trip cost and distance");
                    }
                }
            });
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*private void updateTripCost_CallServer(final String sCostValue, final String sDistance) {
        final int tripid = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                .getInt(Constants_CED.PREF_TRIP_ID_INT, -1);
        final String sUserType = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this).
                getString(Constants_CED.CAB_TRIP_USER_TYPE_DRIVER, "");
        final String url = Constants_CED.UPDATE_TRIP_COST_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        SSLog_SS.d("updateTripCost  ", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                String sDateTime = df.format(cal.getTime());
                int appuserid = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                        .getInt(Constants_lib_ss.PREF_APPUSERID, -1);
                db = new DatabaseHandler_CabE(CabEDriverForHire_act.this);
                db.addTripCostDistance(appuserid, tripid, sUserType, CGlobals_lib_ss.msGmail,
                        sDateTime, CGlobals_lib_ss.mIMEI,
                        sCostValue, sDistance);
                try {
                    SSLog_SS.e(TAG, "updateTripCost - ", error, CabEDriverForHire_act.this);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "updateTripCost - ", e, CabEDriverForHire_act.this);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tripid", String.valueOf(tripid));
                params.put("tripcost", sCostValue);
                params.put("tripdistance", sDistance);
                params.put("triptype", sUserType);
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params, url, CabEDriverForHire_act.this);
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }

        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, true, CabEDriverForHire_act.this);
    }*/

    private void customDialog(String message) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(CabEDriverForHire_act.this);
        builder1.setMessage(message);
        builder1.setCancelable(true);
        builder1.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert11 = builder1.create();
        if (!CabEDriverForHire_act.this.isFinishing()) {
            alert11.show();
        }
    }

    /*public void getTripPath() {
        final int tripid = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                .getInt(Constants_CED.PREF_TRIP_ID_INT, -1);
        final String url = Constants_CED.GET_TRIP_PATH_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        showTripPathLine(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                int appuserid = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                        .getInt(Constants_lib_ss.PREF_APPUSERID, -1);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                String sDateTime = df.format(cal.getTime());
                db = new DatabaseHandler_CabE(CabEDriverForHire_act.this);
                db.addGetTripPath(appuserid, tripid, CGlobals_lib_ss.msGmail, sDateTime, CGlobals_lib_ss.mIMEI);
                SSLog_SS.e(TAG, "sendUserAccess :-   ", error, CabEDriverForHire_act.this);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tripid", String.valueOf(tripid));
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        url, CabEDriverForHire_act.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                String debugUrl;
                try {
                    debugUrl = url + "?" + getParams.toString() + "&verbose=Y";
                    Log.e(TAG, debugUrl);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "getPassengers map - ", e, CabEDriverForHire_act.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, true, CabEDriverForHire_act.this);
    }*/

    CTrip_CED moTrip;

    private void showTripPathLine(String response) {
        if (TextUtils.isEmpty(response) || response.equals("-1")) {
            return;
        }
        plotPathPolyLine(response);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this)
                .putString(Constants_CED.PREF_CAB_E_TRIP_PATH_POLYLINE, response);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).commit();
    }

    protected void plotPathPolyLine(String response) {
        try {
            moTrip = new CTrip_CED(response, CabEDriverForHire_act.this);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this)
                    .putString(Constants_lib_ss.TRIP_PATH, moTrip.getMsTripPath());
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).commit();
            mLatLngTripPath = PolyUtil.decode(moTrip.getMsTripPath());
            CGlobals_lib_ss.getInstance().mDirectionsPolyline = new PolylineOptions()
                    .addAll(mLatLngTripPath);
            CGlobals_lib_ss.getInstance().mDirectionsPolyline.width(8);
            CGlobals_lib_ss.getInstance().mDirectionsPolyline.color(Color.BLACK);
            mZoomFitBounds = new LatLngBounds.Builder();
            for (LatLng latLng : mLatLngTripPath) {
                mZoomFitBounds
                        .include(new LatLng(latLng.latitude, latLng.longitude));
            }
            setBusyCreateTripDriver = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                    .getInt(Constants_CED.PERF_SET_BUSY_CREATE_TRIP_DRIVER, 0);
            if (setBusyCreateTripDriver == 0) {
                mMap.addPolyline(CGlobals_lib_ss.getInstance().mDirectionsPolyline);
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(moTrip.getFromLat(), moTrip.getFromLng()))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(moTrip.getToLat(), moTrip.getToLng()))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }
        } catch (Exception e) {
            Log.e(TAG, "plotPathFromPolyLine:- " + e);
        }

    }

    Fixed_Address cTripCabEFrom, cTripCabETo;

    protected boolean goDirections() {
        setBusyCreateTripDriver = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                .getInt(Constants_CED.PERF_SET_BUSY_CREATE_TRIP_DRIVER, 0);
        if (setBusyCreateTripDriver == 2) {
            String sFromAdd = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                    .getString(Constants_CED.PREF_DRIVER_FIXED_ADDRESS_FROM, "");
            String sToAdd = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                    .getString(Constants_CED.PREF_DRIVER_FIXED_ADDRESS_TO, "");
            Type type = new TypeToken<Fixed_Address>() {
            }.getType();
            if (moFromAddress == null) {
                moFromAddress = new CAddress();
            }
            if (moToAddress == null) {
                moToAddress = new CAddress();
            }
            cTripCabEFrom = new Gson().fromJson(sFromAdd, type);
            cTripCabETo = new Gson().fromJson(sToAdd, type);
            moFromAddress.setAddress(cTripCabEFrom.getFormatted_Address());
            moFromAddress.setLatitude(cTripCabEFrom.getLatitude());
            moFromAddress.setLongitude(cTripCabEFrom.getLongitude());
            moToAddress.setAddress(cTripCabETo.getFormatted_Address());
            moToAddress.setLatitude(cTripCabETo.getLatitude());
            moToAddress.setLongitude(cTripCabETo.getLongitude());
        } else {
            try {
                String response = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this).
                        getString(Constants_CED.PREF_CURRENT_TRIP, "");
                if (!TextUtils.isEmpty(response)) {
                    moTripDriver = new CTrip_CED(response, CabEDriverForHire_act.this);
                    moFromAddress.setAddress(moTripDriver.getFrom());
                    moFromAddress.setLatitude(moTripDriver.getFromLat());
                    moFromAddress.setLongitude(moTripDriver.getFromLng());
                    moToAddress.setAddress(moTripDriver.getTo());
                    moToAddress.setLatitude(moTripDriver.getToLat());
                    moToAddress.setLongitude(moTripDriver.getToLng());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!moFromAddress.hasLatitude() || !moFromAddress.hasLongitude() || !moToAddress.hasLatitude() || !moToAddress.hasLongitude()) {
            return false;
        }
        if ((moFromAddress.hasLatitude() && moFromAddress
                .hasLongitude())
                && ((moToAddress.hasLatitude() && moToAddress
                .hasLongitude()))) {
            clearMap();
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this)
                    .putBoolean("ONE_GO_DIRECT", true);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEDriverForHire_act.this).commit();
            drawFromMarkerCab(moFromAddress);
            drawToMarkerCab(moToAddress);
            mBounds.include(
                    new LatLng(moFromAddress.getLatitude(), moFromAddress
                            .getLongitude())).include(
                    new LatLng(moToAddress.getLatitude(), moToAddress
                            .getLongitude()));
            /*mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                    mBounds.build(), Constants_lib_ss.MAP_PADDING));*/
            SSLog_SS.i(TAG, "Plotting directions");
            String url = CabEDriverForHire_act.this.getDirectionsUrl(
                    new LatLng(moFromAddress.getLatitude(), moFromAddress
                            .getLongitude()),
                    new LatLng(moToAddress.getLatitude(), moToAddress
                            .getLongitude()));
            GoogleDirection downloadTask = new GoogleDirection();
            downloadTask.execute(url);
            return true;
        } else {
            Toast.makeText(CabEDriverForHire_act.this,
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
        return "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + parameters;
    }

    public void drawFromMarkerCab(CAddress addr) {
        try {
            if (!addr.hasLatitude() || !addr.hasLongitude()) {
                return;
            }
            if (markerFrom != null) {
                markerFrom.remove();
            }
            markerFrom = null;
            markerFrom = mMap
                    .addMarker(new MarkerOptions()
                            .position(
                                    new LatLng(addr.getLatitude(), addr
                                            .getLongitude()))
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_start_location)));
        } catch (Exception e) {
            SSLog_SS.e(TAG, "drawFromMarkerCab", e, CabEDriverForHire_act.this);
        }
    } // drawFromMarker

    public void drawToMarkerCab(CAddress addr) {
        try {
            if (!addr.hasLatitude() || !addr.hasLongitude()) {
                return;
            }
            if (markerTo != null) {
                markerTo.remove();
            }
            markerTo = null;
            markerTo = mMap.addMarker(new MarkerOptions()
                    .position(
                            new LatLng(moToAddress.getLatitude(), moToAddress
                                    .getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_end_location)));
        } catch (Exception e) {
            SSLog_SS.e(TAG, "drawToMarkerCab", e, CabEDriverForHire_act.this);
        }
    } // drawToMarker

    ArrayList<CTrip_CED> mPassengerTripArray;
    CTrip_CED mCTPassengerTrip;

    public void onClickPassengerJump(View v) {
        try {
            String response = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this).
                    getString("RECEIVER_RESPONSE", "");
            int errorvalue = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this).
                    getInt("RECEIVER_ERROR_VALUE", 0);
            if (errorvalue == 1) {
                if (TextUtils.isEmpty(response)) {
                    return;
                }
                if (response.equals("-1")) {
                    return;
                }
                final Dialog dialog = new Dialog(CabEDriverForHire_act.this);
                dialog.setContentView(R.layout.trip_passenger_info);
                dialog.setTitle("Passenger");
                final ListView mlvPassengerList;
                mlvPassengerList = (ListView) dialog.findViewById(R.id.lvPassengerListinfo);
                mPassengerTripArray = new ArrayList<>();
                JSONArray majActivePassenger = new JSONArray(response);
                for (int i = 0; i < majActivePassenger.length(); i++) {
                    mCTPassengerTrip = new CTrip_CED(majActivePassenger.getJSONObject(i)
                            .toString(), getApplicationContext());
                    mPassengerTripArray.add(mCTPassengerTrip);
                }
                mlvPassengerList.setAdapter(new PassengerAdapter_Jump(CabEDriverForHire_act.this, mPassengerTripArray));
                dialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*public void callJumpInJumpOutUrl(final int passenger_appuser_id) {
        final int tripid = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                .getInt(Constants_CED.PREF_TRIP_ID_INT, -1);
        final String url = Constants_CED.JUMP_IN_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                String sDateTime = df.format(cal.getTime());
                int appuserid = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                        .getInt(Constants_lib_ss.PREF_APPUSERID, -1);
                Location location = CGlobals_lib_ss.getInstance().getMyLocation(CabEDriverForHire_act.this);
                DatabaseHandler_CabE db = new DatabaseHandler_CabE(CabEDriverForHire_act.this);
                db.addJumpInOutTrip(location, appuserid, tripid, passenger_appuser_id,
                        CGlobals_lib_ss.msGmail, sDateTime, "jumpout");
                SSLog_SS.e(TAG, "sendUserAccess :-   ", error, CabEDriverForHire_act.this);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tripid", String.valueOf(tripid));
                params.put("passengerappuserid", String.valueOf(passenger_appuser_id));
                params.put("jumpout", "1");
                params = CGlobals_lib_ss.getInstance().getBasicMobileParamsShort(params,
                        url, CabEDriverForHire_act.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                String debugUrl;
                try {
                    debugUrl = url + "?" + getParams.toString() + "&verbose=Y";
                    Log.e(TAG, debugUrl);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "getPassengers map - ", e, CabEDriverForHire_act.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, true, CabEDriverForHire_act.this);
    }*/

    void showTrip() {
        try {
            String jsonPoints = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEDriverForHire_act.this)
                    .getString(Constants_lib_ss.TRIP_PATH, "");
            if (!TextUtils.isEmpty(jsonPoints)) {
                mLatLngTripPath = PolyUtil.decode(jsonPoints);
                mDirectionsPolyline = new PolylineOptions()
                        .addAll(mLatLngTripPath);
                mDirectionsPolyline.width(4);
                mDirectionsPolyline.color(Color.BLACK);
                mMap.addPolyline(mDirectionsPolyline);
                drawFromMarker(moFromAddress);
                drawToMarker(moToAddress);
                mZoomFitBounds = new LatLngBounds.Builder();
                for (LatLng latLng : mLatLngTripPath) {
                    mZoomFitBounds.include(new LatLng(latLng.latitude,
                            latLng.longitude));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "showTrip:- " + e);
        }
    }
}
