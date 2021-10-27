package com.smartshehar.dashboard.app.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.smartshehar.dashboard.app.CFare;
import com.smartshehar.dashboard.app.CFareParams;
import com.smartshehar.dashboard.app.CGlobals_db;
import com.smartshehar.dashboard.app.CTrip;
import com.smartshehar.dashboard.app.DBHelperAutoTaxi;
import com.smartshehar.dashboard.app.LocationService_AutoTaxi;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.MyLocation;

public class ActMeter extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener { // implements SensorEventListener
    public static final String TAG = "ActMeter: ";

    TextView  mTvLocStatus, mTvDistTraveled, mTvFare, mTvNightFare,
            mTvFareTotal, mTvNightFareTotal, mTvTripTime, mTvWaitingTime, mTvSpeed, mTvStatus;
    //	EditText metRs, metPaise;
    Button mBtnStartTrip, mBtnCancelTrip;
    Spinner mSpinnerVehicleType;
    CFare moFare;
    private long mlStartMs, mlDestMs;
    private SharedPreferences mPref;
    SharedPreferences.Editor mEditor;
    private static final int ACTIVE_TEXT_SIZE = 32;
    private static final int INACTIVE_TEXT_SIZE = 24;
    private AlertDialog.Builder builder;
    private ArrayList<CFareParams> aoFareParams;
    GoogleApiClient mGoogleApiClient = null;
    CGlobals_lib_ss mApp = null;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setHomeButtonEnabled(true);

        setContentView(R.layout.meter);
        mApp = CGlobals_lib_ss.getInstance();
        mApp.init(this);
        mApp.getMyLocation();
        String msCity = CGlobals_db.getCity(ActMeter.this);
        ab.setTitle("Meter for " + msCity);
        aoFareParams = new ArrayList<>();
        if (!TextUtils.isEmpty(msCity))
            CGlobals_db.mDBHelper = new DBHelperAutoTaxi(ActMeter.this);
        CGlobals_db.mDBHelper.openDataBase(mApp.mPackageInfo, ActMeter.this);
        aoFareParams = CGlobals_db.mDBHelper.getCityFareParameters(msCity);
        mGoogleApiClient = new GoogleApiClient.Builder(ActMeter.this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(ActMeter.this)
                .addOnConnectionFailedListener(ActMeter.this)
                .build();
        mGoogleApiClient.connect();

        Toast.makeText(ActMeter.this,"IN TRIP "+CGlobals_db.mbInTrip,Toast.LENGTH_LONG).show();
        try {
            CGlobals_db.moLastTrip.msMeterType = CFareParams.METERTYPEDIGITAL;

            mPref = getSharedPreferences(CGlobals_db.PREFS_NAME, 0);
            mEditor = mPref.edit();
            readPrefs();
            CGlobals_db.getInstance(ActMeter.this).init(ActMeter.this, CGlobals_db.msVehicleType);
            setupUI();
           /* try {
//                CGlobals_db.getInstance(ActMeter.this).mCH.userPing("SSD", getString(R.string.pageMeter));
            } catch (Exception e) {
                SSLog.e(TAG, " onCreate - ", e.getMessage());
            }*/
            CGlobals_db.getInstance(ActMeter.this).getMyLocation(ActMeter.this);
            if (CGlobals_db.mbInTrip) {
                if (CGlobals_db.moLastTrip.moFareParams == null) {
                    CGlobals_db.moLastTrip.moFareParams = new CFareParams();
                    for (int i = 0; i < aoFareParams.size(); i++) {
                        if (CGlobals_db.msVehicleType.equalsIgnoreCase(aoFareParams.get(i).msVehicleType)) {
                            CGlobals_db.moLastTrip.moFareParams.objClone(aoFareParams.get(i));

                        }
                    }
                }
                moFare = new CFare(CGlobals_db.moLastTrip.moFareParams, CFareParams.METERTYPEDIGITAL, 0, 0);
            } else {
                CGlobals_db.moLastTrip.moFareParams = new CFareParams();
                CGlobals_db.moLastTrip.moFareParams.objClone(aoFareParams.get(0));
                moFare = new CFare(CGlobals_db.moLastTrip.moFareParams, CFareParams.METERTYPEDIGITAL, 0, 0);
            }
        } catch (Exception e) {
//            Toast.makeText(ActMeter.this, "onCreate() " + e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
//			resetMeter();
    } // onCreate


    void setupUI() {
        mBtnStartTrip = (Button) findViewById(R.id.btnStartTrip);
        mBtnCancelTrip = (Button) findViewById(R.id.btnCancelTrip);
        mTvStatus = (TextView) findViewById(R.id.tvStatus);
        mTvDistTraveled = (TextView) findViewById(R.id.traveled);
        mTvTripTime = (TextView) findViewById(R.id.tvTripTime);
        mTvWaitingTime = (TextView) findViewById(R.id.tvWaitingTime);
        mTvSpeed = (TextView) findViewById(R.id.tvSpeed);
        mTvLocStatus = (TextView) findViewById(R.id.tvLocStatus);
        mTvFare = (TextView) findViewById(R.id.tvFare);
        mTvNightFare = (TextView) findViewById(R.id.tvNightFare);
        mTvFareTotal = (TextView) findViewById(R.id.tvFareTotal);
        mTvNightFareTotal = (TextView) findViewById(R.id.tvNightfareTotal);
        mTvStatus.setVisibility(View.GONE);
        mSpinnerVehicleType = (Spinner) findViewById(R.id.spinnerVehicleType);
        ArrayList<String> vehicleList = new ArrayList<>();
        for (int i = 0; i < aoFareParams.size(); i++) {
            vehicleList.add(aoFareParams.get(i).msVehicleTypeDescription);
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, vehicleList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerVehicleType.setAdapter(dataAdapter);
        mSpinnerVehicleType.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Object item = parent.getItemAtPosition(position);
//               	aoCFareParams = CGlobals_db.mDBHelper.getCityFareParameters(CGlobals_db.msCity);
                    if (item != null) {
                        Toast.makeText(ActMeter.this, item.toString(),
                                Toast.LENGTH_SHORT).show();
                        for (int i = 0; i < aoFareParams.size(); i++) {
                            if (item.toString().equals(aoFareParams.get(i).msVehicleTypeDescription)) {
                                CGlobals_db.moLastTrip.moFareParams.objClone(aoFareParams.get(i));
                                CGlobals_db.msVehicleType = CGlobals_db.moLastTrip.moFareParams.msVehicleType;
                                CGlobals_db.getInstance(ActMeter.this).setVehicle(CGlobals_db.msVehicleType, ActMeter.this);
                                if (CGlobals_db.mbInTrip) {
                                    setMeter(CGlobals_db.moLastTrip.msMeterType, CGlobals_db.mTravelDistance,
                                            CGlobals_db.mWaitTimeSecs);
                                } else {
                                    setMeter(CGlobals_db.moLastTrip.msMeterType, 0, 0);
                                }

                            }
                        }

                    }
                } catch (Exception e) {
                    //endTrip();
                    Toast.makeText(ActMeter.this, "Not supporting " + e.toString(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    ActMeter.this.finish();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(ActMeter.this, "Nothing selected",
                        Toast.LENGTH_SHORT).show();
            }
        });
        updateStartBtn();
        LocationManager locationManager =
                (LocationManager) ActMeter.this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d(TAG, "GPS is Enabled in your device");
//				Toast.makeText(this, "GPS is Enabled in your device", Toast.LENGTH_SHORT).show();
        } else {
            CGlobals_db.getInstance(ActMeter.this).turnGPSOn(ActMeter.this, mGoogleApiClient);
           /* CGlobals_db.getInstance(ActMeter.this).showGPSDisabledAlertToUser(ActMeter.this);*/
            return;
        }

        mBtnStartTrip.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!CGlobals_db.mbInTrip) { // Start Trip
                    resetMeter();
                    LocationManager locationManager =
                            (LocationManager) ActMeter.this.getSystemService(Context.LOCATION_SERVICE);
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Toast.makeText(ActMeter.this, "GPS is Enabled in your device", Toast.LENGTH_SHORT).show();
                    } else {
                        CGlobals_db.getInstance(ActMeter.this).turnGPSOn(ActMeter.this, mGoogleApiClient);
                        /*CGlobals_db.getInstance(ActMeter.this).showGPSDisabledAlertToUser(ActMeter.this);*/
                        return;
                    }
                    CGlobals_db.mbStartTripPressed = true;
                    writePrefs();
                    noFix();
                    stopLocationService_AutoTaxi("Start Button");
                    startService(new Intent(ActMeter.this, LocationService_AutoTaxi.class));
                    if (CGlobals_db.mbGPSFix)
                        gotFix();
                } else { // End Trip
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Yes button clicked
                                    endTrip();
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                            }
                        }
                    };

                    builder = new AlertDialog.Builder(ActMeter.this);
                    builder.setMessage(R.string.end_trip)
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener)
                            .show();
                }
            }

        });

        mBtnCancelTrip.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                cancelTrip();
            }
        });

        OnClickListener radio_listener = new OnClickListener() {

            public void onClick(View v) {
                // Perform action on clicks
                RadioButton rb = (RadioButton) v;
                String sSel = rb.getText().toString();
                Toast.makeText(ActMeter.this, sSel, Toast.LENGTH_SHORT).show();
                CGlobals_db.getInstance(ActMeter.this).setVehicle(sSel, ActMeter.this);
                if (CGlobals_db.mbInTrip)
                    setMeter(CGlobals_db.moLastTrip.msMeterType, CGlobals_db.mTravelDistance,
                            CGlobals_db.mWaitTimeSecs);
                else
                    setMeter(CGlobals_db.moLastTrip.msMeterType, 0, 0);
            }
        };
        RadioButton radioAuto = (RadioButton) findViewById(R.id.radioAuto);
        RadioButton radioTaxi = (RadioButton) findViewById(R.id.radioTaxi);

        radioAuto.setOnClickListener(radio_listener);
        radioTaxi.setOnClickListener(radio_listener);
    }


    protected void resetMeter() {
        CGlobals_db.mbInTrip = false;
        CGlobals_db.mbStartTripPressed = false;
        CGlobals_db.miTravelDistance = 0;
        CGlobals_db.getInstance(ActMeter.this).setVehicle(CGlobals_db.msVehicleType, ActMeter.this);
        stopLocationService_AutoTaxi("Reset Meter");
        mTvDistTraveled.setText(R.string.blank);
        mTvTripTime.setText(R.string.blank);
        mTvWaitingTime.setText(R.string.blank);
        mTvSpeed.setText(R.string.blank);
//		CFareParams cfp = CGlobals_db.moLastTrip.moFareParams;
//		metRs.setText(Integer.toString((int)(cfp.fMinimumFare)));
//		int iPs = (int)((cfp.fMinimumFare  - (int)cfp.fMinimumFare)*100);
        CGlobals_db.moLocStart = null;
        CGlobals_db.moLocDest = CGlobals_db.moLocStart;
        CGlobals_db.miTravelDistance = 0;
        CGlobals_db.getInstance(ActMeter.this).miDirectDistance = 0;
        CGlobals_db.miTripTime = 0;
        CGlobals_db.mlTripTimeMs = 0L;
        CGlobals_db.miWaitTimeSecs = 0;
        CGlobals_db.mlWaitTimeMs = 0L;
        CGlobals_db.mTripTime = 0;
        CGlobals_db.mlTripTimeMs = 0L;
        CGlobals_db.miWaitTimeSecs = 0;
        CGlobals_db.mlWaitTimeMs = 0L;
//		metPaise.setText(iPs == 0 ? "00" : Integer.toString(iPs));
        CGlobals_db.moLocStart = null;
        CGlobals_db.moLocDest = null;
        writePrefs();
        setMeter(CGlobals_db.moLastTrip.msMeterType, 0, 0);
        CGlobals_db.mLocStart = null;
        CGlobals_db.mLocDest = CGlobals_db.mLocStart;
        CGlobals_db.mTravelDistance = 0;
        CGlobals_db.mWaitTimeSecs = 0;
        CGlobals_db.mDirectDistance = 0;

    } // y
    protected void resetData() {
        CGlobals_db.mbInTrip = false;
        CGlobals_db.mbStartTripPressed = false;
        CGlobals_db.miTravelDistance = 0;
        CGlobals_db.getInstance(ActMeter.this).setVehicle(CGlobals_db.msVehicleType, ActMeter.this);
        mTvDistTraveled.setText(R.string.blank);
        mTvTripTime.setText(R.string.blank);
        mTvWaitingTime.setText(R.string.blank);
        mTvSpeed.setText(R.string.blank);
//		CFareParams cfp = CGlobals_db.moLastTrip.moFareParams;
//		metRs.setText(Integer.toString((int)(cfp.fMinimumFare)));
//		int iPs = (int)((cfp.fMinimumFare  - (int)cfp.fMinimumFare)*100);
        CGlobals_db.moLocStart = null;
        CGlobals_db.moLocDest = CGlobals_db.moLocStart;
        CGlobals_db.miTravelDistance = 0;
        CGlobals_db.getInstance(ActMeter.this).miDirectDistance = 0;
        CGlobals_db.miTripTime = 0;
        CGlobals_db.mlTripTimeMs = 0L;
        CGlobals_db.miWaitTimeSecs = 0;
        CGlobals_db.mlWaitTimeMs = 0L;
        CGlobals_db.mTripTime = 0;
        CGlobals_db.mlTripTimeMs = 0L;
        CGlobals_db.miWaitTimeSecs = 0;
        CGlobals_db.mlWaitTimeMs = 0L;
//		metPaise.setText(iPs == 0 ? "00" : Integer.toString(iPs));
        CGlobals_db.moLocStart = null;
        CGlobals_db.moLocDest = null;
        writePrefs();
        setMeter(CGlobals_db.moLastTrip.msMeterType, 0, 0);
        CGlobals_db.mLocStart = null;
        CGlobals_db.mLocDest = CGlobals_db.mLocStart;
        CGlobals_db.mTravelDistance = 0;
        CGlobals_db.mWaitTimeSecs = 0;
        CGlobals_db.mDirectDistance = 0;

    } // y

    private void writePrefs() {
        try {
            mEditor.putString(CGlobals_db.PREF_VEHICLE, CGlobals_db.msVehicleType);
            mEditor.putBoolean(CGlobals_db.PREFINTRIP, CGlobals_db.mbInTrip);
            mEditor.putBoolean(CGlobals_db.PREF_START_PRESSED, CGlobals_db.mbStartTripPressed);
            mEditor.putInt(CGlobals_db.PREF_DIRECT_DIST, CGlobals_db.mDirectDistance);
            mEditor.putInt(CGlobals_db.PREF_TRAVEL_DISTANCE, CGlobals_db.mTravelDistance);
            mEditor.putInt(CGlobals_db.PREF_DIRECT_DIST, CGlobals_db.mDirectDistance);
            mEditor.putInt(CGlobals_db.PREF_WAIT_TIME, CGlobals_db.mWaitTimeSecs);
            mEditor.putInt(CGlobals_db.PREF_TRIP_TIME, CGlobals_db.mTripTime);

            mEditor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readPrefs() {

        try {
            CGlobals_db.msVehicleType = mPref.getString(CGlobals_db.PREF_VEHICLE, CFareParams.DEFAULT_VEHICLE);
            if (TextUtils.isEmpty(CGlobals_db.msVehicleType))
                CGlobals_db.msVehicleType = CFareParams.DEFAULT_VEHICLE;
            CGlobals_db.mTravelDistance = mPref.getInt(CGlobals_db.PREF_TRAVEL_DISTANCE, 0);
            CGlobals_db.getInstance(ActMeter.this).mDirectDistance = mPref.getInt(CGlobals_db.PREF_DIRECT_DIST, 0);
            CGlobals_db.mbInTrip = mPref.getBoolean(CGlobals_db.PREFINTRIP, false);
            CGlobals_db.mbStartTripPressed = mPref.getBoolean(CGlobals_db.PREF_START_PRESSED, false);
            CGlobals_db.mWaitTimeSecs = mPref.getInt(CGlobals_db.PREF_WAIT_TIME, 0);
            CGlobals_db.mTripTime = mPref.getInt(CGlobals_db.PREF_TRIP_TIME, 0);
        } catch (Exception e) {
            SSLog.e(TAG, "getPrefs - ", e.getMessage());
        }


    }

    static double DIST_ERROR_MULTIPLE = 1.01; // Dist seems of by 10% prev-1.061;

    protected void setMeter(String sMeterType, int dDist, int iWaitSecs) {
        try {
            dDist = (int) (dDist * DIST_ERROR_MULTIPLE);
            // Fare calculation
            moFare = new CFare(CGlobals_db.moLastTrip.moFareParams, sMeterType,
                    dDist, iWaitSecs);
            Time tm = new Time();
            tm.setToNow();
            int hours = tm.hour;
            boolean mbNightFare = false;
            if (hours >= CGlobals_db.moLastTrip.moFareParams.fNightStart && hours <= CGlobals_db.moLastTrip.moFareParams.fNightEnd) {
                mbNightFare = true;
            }
//		int iPaise = Integer.parseInt(metPaise.getText().toString());
//		metPaise.setText(Integer.valueOf(iPaise).toString());
            mTvFare.setText(moFare.msFare);
            mTvNightFare.setText(moFare.msNightFare);
            mTvFareTotal.setText(moFare.msFareTotal);
            mTvNightFareTotal.setText(moFare.msNightFareTotal);
            if (mbNightFare) {
                mTvNightFare.setTextSize(ACTIVE_TEXT_SIZE);
                mTvNightFare.setTextColor(Color.MAGENTA);
                mTvNightFare.setTypeface(null, Typeface.BOLD);
                mTvNightFareTotal.setTextSize(ACTIVE_TEXT_SIZE);
                mTvNightFareTotal.setTextColor(Color.MAGENTA);
                mTvNightFareTotal.setTypeface(null, Typeface.BOLD);
                mTvFare.setTextSize(INACTIVE_TEXT_SIZE);
                mTvFare.setTextColor(Color.BLACK);
                mTvFareTotal.setTextColor(Color.BLACK);
            } else {
                mTvFare.setTextSize(ACTIVE_TEXT_SIZE);
                mTvFare.setTextColor(Color.MAGENTA);
                mTvFare.setTypeface(null, Typeface.BOLD);
                mTvNightFare.setTextColor(Color.BLACK);
                mTvFareTotal.setTextColor(Color.MAGENTA);
                mTvFareTotal.setTextSize(ACTIVE_TEXT_SIZE);
                mTvFareTotal.setTypeface(null, Typeface.BOLD);
                mTvNightFareTotal.setTextSize(INACTIVE_TEXT_SIZE);
                mTvNightFareTotal.setTextColor(Color.BLACK);
            }
        } catch (Exception e) {
//            Toast.makeText(ActMeter.this, "ActMeter in setMeter() " + e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    } // setMeter

    void init() {
        @SuppressWarnings("unused")

        boolean resetTrip;
        readPrefs();

        if (!isServiceRunning(ActMeter.this, getPackageName() + ".LocationService_AutoTaxi")) {
            if (CGlobals_db.mbInTrip || CGlobals_db.mbStartTripPressed) {
//                 Toast.makeText(ActMeter.this, getString(R.string.meterStoppedBySystem), Toast.LENGTH_SHORT).show();
                Log.d(TAG, getString(R.string.meterStoppedBySystem));

            }
            resetTrip = true;
        } else {
            if (CGlobals_db.mbInTrip || CGlobals_db.mbStartTripPressed) {
                resetTrip = false;
            } else {
                resetTrip = true;
            }
            Log.d(TAG,"resetTrip "+resetTrip);
        }

        updateStartBtn();
        writePrefs();

        RadioButton radioAuto = (RadioButton) findViewById(R.id.radioAuto);
        radioAuto.setChecked(true);
        if (CGlobals_db.msVehicleType.equalsIgnoreCase(CFareParams.TAXI) ||
                CGlobals_db.msVehicleType.equalsIgnoreCase(CFareParams.TAXIDISPLAY)) {
            RadioButton radioTaxi = (RadioButton) findViewById(R.id.radioTaxi);
            radioTaxi.setChecked(true);
        }
        // Restore preferences_ss

        if (CGlobals_db.mbStartTripPressed) {
            if (CGlobals_db.mbGPSFix)
                gotFix();
        }
        updateUI();
    } // init

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TextView tvLatLon = (TextView) findViewById(R.id.tvLatLon);
            Log.d(TAG, "Location is: " + CGlobals_db.getInstance(ActMeter.this).mCurrentLocation.getLatitude());
            if (CGlobals_db.getInstance(ActMeter.this).mCurrentLocation != null) {
                String sLatLon = "(" + (int) (CGlobals_db.getInstance(ActMeter.this).mCurrentLocation.getLatitude() * 10000) + ", " +
                        (int) (CGlobals_db.getInstance(ActMeter.this).mCurrentLocation.getLongitude() * 10000) + " - " +
                        CGlobals_db.getInstance(ActMeter.this).mCurrentLocation.getAccuracy();
                tvLatLon.setText(sLatLon);
            } else {
                tvLatLon.setText("Location not available");
            }
            if (!CGlobals_db.mbInTrip)
                gotFix();
            else
                updateUI();
        }
    };

    private static boolean mbGotFix = false;

    private void updateUI() {
        if (!CGlobals_db.mbInTrip) // || !CGlobals_db.getInstance(ActMeter.this).mbMeterShown)
            return;
        updateStartBtn();
        CGlobals_db.mWaitTimeSecs = mPref.getInt(CGlobals_db.PREF_WAIT_TIME, 0);
        CGlobals_db.mTravelDistance = mPref.getInt(CGlobals_db.PREF_TRAVEL_DISTANCE, 0);

        setMeter(CGlobals_db.moLastTrip.msMeterType, CGlobals_db.mTravelDistance, CGlobals_db.mWaitTimeSecs);
        try {
            /*Time nowTime = new Time();*/
           /* nowTime.setToNow();
            long iMinutesElapsed = nowTime.toMillis(false) - CGlobals_db.mTripStartTime.toMillis(false);
            iMinutesElapsed = (int) iMinutesElapsed / (60 * 1000);*/
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    CGlobals_db.mTripTime = mPref.getInt(CGlobals_db.PREF_TRIP_TIME, 0);
                    mTvTripTime.setText(CGlobals_db.ConvertToHMS(CGlobals_db.mTripTime * 1000));
                    mTvWaitingTime.setText(CGlobals_db.ConvertToHMS(CGlobals_db.mWaitTimeSecs * 1000));
                    int speed = mPref.getInt(CGlobals_db.PREF_SPEED, 0);
                    mTvSpeed.setText(Integer.toString(speed));
                    mTvDistTraveled.setText(CGlobals_db.showDistance(CGlobals_db.mTravelDistance * DIST_ERROR_MULTIPLE));
                }
            });
        } catch (Exception e) {
            SSLog.e(TAG, "updateUI - ", e.getMessage());
        }
    } // updateUI

    @Override
    public void onResume() {
        try {
            CGlobals_db.getInstance(ActMeter.this).mbMeterShown = true;
            registerReceiver(broadcastReceiver,
                    new IntentFilter(LocationService_AutoTaxi.BROADCAST_ACTION));
            try {
                init();
            } catch (Exception e) {
                SSLog.e(TAG, "onResume - ", e.toString());
                resetMeter();
            }
            if (new MyLocation().getLocation(ActMeter.this, onLocationResult)) {
                // Force the progress indicator to start
                Toast.makeText(ActMeter.this,
                        "Location found, updating location ...", Toast.LENGTH_LONG).show();
                // getRateBeerActivity().setProgress(true);
//			mTvStatus.setVisibility(View.GONE);

            }
        } catch (Exception e) {
/*            Toast.makeText(ActMeter.this, "onResume in ActMeter " + e.toString(), Toast.LENGTH_LONG).show();*/
            e.printStackTrace();
        }

        super.onResume();
    }

    private MyLocation.LocationResult onLocationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            if (location == null) {
                if (CGlobals_db.getInstance(ActMeter.this).mCurrentLocation != null) {
                    location = CGlobals_db.getInstance(ActMeter.this).mCurrentLocation;
                } else {
                    Log.d(TAG,"Cannot get nearest station \\n Turn GPS and network location in Setup");
                }
            }
            if (location != null && location.getProvider().equals("fake")) {
                Log.d(TAG,"Cannot get nearest station \\n Turn GPS and network location in Setup");
/*				Toast.makeText(ActTrainRoute.this,
                    "Cannot get nearest station \n Turn GPS and network location in Setup", Toast.LENGTH_LONG)
					.show();
*/
            }

        }
    };

    @Override
    public void onPause() {

        CGlobals_db.getInstance(ActMeter.this).mbMeterShown = false;
        unregisterReceiver(broadcastReceiver);
        writePrefs();
        super.onPause();
    } // onPause


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    boolean isServiceRunning(Context context, String className) {
        ActivityManager activityManager = (ActivityManager)
                context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList
                = activityManager.getRunningServices(Integer.MAX_VALUE);

        if (!(serviceList.size() > 0)) {
            return false;
        }

        for (int i = 0; i < serviceList.size(); i++) {
            ActivityManager.RunningServiceInfo serviceInfo = serviceList.get(i);
            ComponentName serviceName = serviceInfo.service;
            if (serviceName.getClassName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CGlobals_db.REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    Log.d(TAG, "YES");
//                    Toast.makeText(this, "Getting Location ... ", Toast.LENGTH_SHORT).show();

//                        ...
                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    Log.d(TAG, "NO");
                    CGlobals_db.showGPSDialog = false;
                    CGlobals_db.alertToTurnOnGps(ActMeter.this);

                    break;
                default:
                    break;
            }
        }
    }
    void startTrip() {
        try {
            resetData();
            CGlobals_db.mbInTrip = true;
            CGlobals_db.mbStartTripPressed = false;
            writePrefs();
            mTvLocStatus.setVisibility(View.GONE);
            Toast.makeText(ActMeter.this,
                    "Trip Started\nDistance Traveled is approx. Dist.\nCalculations depend on GPS availability and accuracy",
                    Toast.LENGTH_LONG).show();
            mBtnCancelTrip.setVisibility(View.GONE);

            String latlonStr = "_" + new DecimalFormat("##.00").format(CGlobals_db.mLocStart.getLatitude()) + "," +
                    new DecimalFormat("##.00").format(CGlobals_db.mLocStart.getLongitude()) + ":" +
                    new DecimalFormat("##.00").format(CGlobals_db.mLocDest.getLatitude()) + "," +
                    new DecimalFormat("##.00").format(CGlobals_db.mLocDest.getLongitude());
//            CGlobals_db.getInstance(ActMeter.this).mCH.userPing("SSD", getString(R.string.atTripStart) + latlonStr);


        } catch (Exception e) {
            SSLog.e(TAG, "tripStarted - ", e.getMessage());
        }

    }

    void endTrip() {
        try {
            mBtnStartTrip.setText(R.string.starttrip);
            Toast.makeText(ActMeter.this, "Trip Ended", Toast.LENGTH_SHORT).show();
            CGlobals_db.moTripDestTime = new Time();
            CGlobals_db.moTripDestTime.setToNow();
            mlDestMs = CGlobals_db.moTripDestTime.toMillis(false);
            mlStartMs = CGlobals_db.mTripStartTime.toMillis(false);
            final Location locStart = CGlobals_db.mLocStart;
            final Location locDest = CGlobals_db.mLocDest;
            stopLocationService_AutoTaxi("End Trip");
            final String latlonStr = "_" + new DecimalFormat("##.00").format(CGlobals_db.mLocStart.getLatitude()) + "," +
                    new DecimalFormat("##.00").format(CGlobals_db.mLocStart.getLongitude()) + ":" +
                    new DecimalFormat("##.00").format(CGlobals_db.mLocDest.getLatitude()) + "," +
                    new DecimalFormat("##.00").format(CGlobals_db.mLocDest.getLongitude());

            setMeter(CGlobals_db.moLastTrip.msMeterType, CGlobals_db.mTravelDistance, CGlobals_db.mWaitTimeSecs);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    try {
//                        CGlobals_db.getInstance(ActMeter.this).mCH.userPing("SSD", getString(R.string.atTripEnd) + latlonStr);
                        CGlobals_db.moLastTrip = new CTrip(CGlobals_db.moLastTrip.moFareParams,
                                CGlobals_db.moLastTrip.moFareParams.msVehicleType,
                                locStart.getLatitude(), locStart.getLongitude(),
                                locDest.getLatitude(), locDest.getLongitude(),
                                CGlobals_db.mTripStartTime.toMillis(false), mlDestMs,
                                moFare.mdFare, moFare.mdFare, CGlobals_db.mTravelDistance);
                        CTrip trip = CGlobals_db.getInstance(ActMeter.this).mDBHelperLocalAutoTaxi.saveTrip(CGlobals_db.moLastTrip);
                        if (trip != null) {
                            if (trip._id > -1) {
                                CGlobals_db.moLastTrip._id = trip._id;
                            } else {
                                int id = CGlobals_db.getInstance(ActMeter.this).mDBHelperLocalAutoTaxi.getTripId(mlStartMs);
                                if (id != -1)
                                    CGlobals_db.moLastTrip._id = id;
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(ActMeter.this, "Ene trip " + e.toString(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            }, 1);
        } catch (Exception e) {
            SSLog.e(TAG, "endTrip - ", e.getMessage());
        }
        // Check connection and run after connecting

        if (CGlobals_db.haveNetworkConnection(ActMeter.this) <= 0) {
            AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
            myAlertDialog.setTitle("Internet Connection Required");
            myAlertDialog.setMessage("To use this feature please start your Interent (Wifi/GPRS) connection and click OK");
            myAlertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    tripSummaryIntent();
                }
            });
            myAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    // do something when the Cancel button is clicked
                }
            });
            myAlertDialog.show();
        }
        if (CGlobals_db.haveNetworkConnection(ActMeter.this) > 0) {
            tripSummaryIntent();
        }
        CGlobals_db.mbInTrip = false;
        CGlobals_db.mbStartTripPressed = false;

    } // endTrip

    void cancelTrip() {
        mTvLocStatus.setVisibility(View.GONE);
        mBtnStartTrip.setVisibility(View.VISIBLE);
        mBtnStartTrip.setText(R.string.starttrip);
        Toast.makeText(ActMeter.this, "Trip Ended", Toast.LENGTH_SHORT).show();
        resetMeter();
        CGlobals_db.mbInTrip = false;
//        CGlobals_db.getInstance(ActMeter.this).mCH.userPing("SSD", getString(R.string.atTripCamceled));
        mBtnCancelTrip.setVisibility(View.GONE);
    } // cancelTrip

    void tripSummaryIntent() {
        Intent intent = new Intent(ActMeter.this, ActTripSummary.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        startActivity(intent);
//			resetMeter();
        finish();
    }

    void noFix() {
        mBtnStartTrip.setVisibility(View.GONE);
        mTvLocStatus.setVisibility(View.VISIBLE);
        mTvLocStatus.setText(R.string.gpsfix);
        mBtnCancelTrip.setVisibility(View.VISIBLE);
        mbGotFix = false;
    }

    void gotFix() {
        mbGotFix = true;
        updateStartBtn();

        CGlobals_db.mlStartTimeMs = SystemClock.uptimeMillis();

    }

    private void updateStartBtn() {
        if (!CGlobals_db.mbInTrip) {
            if (CGlobals_db.mbStartTripPressed) {
                mTvDistTraveled.setText(R.string.zerom);
                if (mbGotFix) {
                    mBtnStartTrip.setText(R.string.endtrip);
                    mTvLocStatus.setVisibility(View.GONE);
                    mBtnCancelTrip.setVisibility(View.GONE);
                    mBtnStartTrip.setVisibility(View.VISIBLE);
                    CGlobals_db.mbStartTripPressed = false;
                    startTrip();
                }
            } else { // Not in any form of trip
                mBtnStartTrip.setText(R.string.starttrip);
                mTvLocStatus.setVisibility(View.GONE);
                mBtnCancelTrip.setVisibility(View.GONE);
                mBtnStartTrip.setVisibility(View.VISIBLE);
            }
        } else { // In trip
            mBtnStartTrip.setText(R.string.endtrip);
            mTvLocStatus.setVisibility(View.GONE);
            mBtnCancelTrip.setVisibility(View.GONE);
            mBtnStartTrip.setVisibility(View.VISIBLE);
            CGlobals_db.mbStartTripPressed = false;
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        //EasyTracker.getInstance(this).activityStart(this);  // Add this method.
    }

    @Override
    public void onStop() {
        super.onStop();
        // EasyTracker.getInstance(this).activityStop(this);  // Add this method.
    }

    private void stopLocationService_AutoTaxi(String reason) {
        stopService(new Intent(ActMeter.this, LocationService_AutoTaxi.class));
        Toast.makeText(ActMeter.this, "Stopping location service - " + reason, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG,"CONNECTED");
    }


    @Override
    public void onBackPressed() {
        if (CGlobals_db.mbStartTripPressed || CGlobals_db.mbInTrip) {
            Toast.makeText(ActMeter.this, "Please press home  to switch to a different app", Toast.LENGTH_LONG).show();
        } else {
            super.onBackPressed();
        }

    }


} // ActMeter