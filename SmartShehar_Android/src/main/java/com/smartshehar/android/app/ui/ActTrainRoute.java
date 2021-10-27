package com.smartshehar.android.app.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.smartshehar.android.app.CGlobals_trains;
import com.smartshehar.android.app.DBHelperTrain.Connection;
import com.smartshehar.android.app.DBHelperTrain.Route;
import com.smartshehar.android.app.Station;
import com.smartshehar.dashboard.app.PermissionUtil;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSApp;
import com.smartshehar.dashboard.app.ui.ActVerifyNumber;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.CTime;
import lib.app.util.MyLocation;
import lib.app.util.SSLog_SS;
import lib.app.util.ui.SSActivity;

public class ActTrainRoute extends SSActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private final static String TAG = "ActTrainRoute---- ";
    private final String SELECTRECENT = "Select Recent Station";


    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private int REQUEST_DEST = 101;
    private int REQUEST_START = 102;
    private final static String RECENT_TRIPS_TAG = "RecentTrips";
    private final static int MAX_RECENT_TRIPS = 10;
    private final static int MAX_TRAINS_TO_SHOW = 25;
    private final static int MINS_IN_DAY = 24 * 60;
    private final static int LONG_WAIT_MINS = 60;
    private final static String DIRECT_TABLE = "dircon";
    private String msStartStationName;
    private String msDestStationName;
    public ArrayList<Connection> maoConn;
    private ArrayList<Trip> maoRecentTrips;
    private ArrayList<String> masRecentTripsList;
    private String msFilterTrains = "Default";
    private Spanned msLeg1a, msLeg1b, msLeg2a, msLeg2b;
    private Spanned msChangeTrain;
    ProgressDialog mProgressDialog;
    private String msProgressMessage;
    private Location lastLocation = null;
    private Station moStartStation = null, moDestStation = null;
    private CGlobals_trains mApp = null;
    private int miHour, mHourOfDay;
    private int miMin;
    ArrayList<String> masLines;
    Button mBtnRecentTrips, mBtnReverseTrip, mBtnGoTrip;
    ImageView mBtnMore, mBtnSMS;
    ArrayList<String> aStation;
    Cursor startStationListCursor, destStationListCursor;
    //   LinearLayout mllStartStation;
    Button mBtnStartTime, mBtnClearStart, mBtnClearDest;
    TextView mtvShowTime;
    TextView mTvStatus; // , mTvBeta;
    static final int TIME_DIALOG_ID = 0;
    private int miStartStationId = 0, miDestStationId = 0;
    private CTime mTime;
    private String sMessage; // message for sms and email for sharing routes
    // Time scrolled flag
    boolean mbAutoUpdate = true;
    boolean mbAutoLocation = true;
    private boolean mbDirectTrainsOnly = false;
    private EditText mStartView, mDestView;
    // Motion detection
    private SensorManager mSensorManager;
    GoogleApiClient mGoogleApiClient = null;
    boolean changedTimeManually = false;
    ArrayList<String> masRecentStations;
    ArrayList<String> maStation;
    TextView txtGpsMsg;
    String provider;
    LocationRequest mLocationRequest;
    private static final long INTERVAL = 1000 * 6;
    private ScrollView trainhomeScrollView;
    private static final int INITIAL_REQUEST = 1341;
    private static final String[] INITIAL_PERMS = {
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.RECEIVE_SMS
    };
    private void showRequirementOfPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.SEND_SMS) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.RECEIVE_SMS))
            customDialog(getString(R.string.sms_permission));
    }
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, INITIAL_PERMS, INITIAL_REQUEST);
    }
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_train_route);
        if (Build.VERSION.SDK_INT >= 23) {
            if (!checkPermission())
                requestPermission();
            else startFunction();
        } else
            startFunction();
    } // onCreate

    private void startFunction()
    {
        mApp = CGlobals_trains.getInstance();
        mApp.init(this);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle("Route");
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        mbAutoUpdate = true;
        mbAutoLocation = true;// settings.getBoolean("checkBoxAutoLocation", true);
        Context context = getSupportActionBar().getThemedContext();
        ArrayAdapter<CharSequence> list =
                ArrayAdapter.createFromResource(context, R.array.route_action_list,
                        android.R.layout.simple_spinner_item);
        list.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (mApp.mCH == null) {
            mApp.init(ActTrainRoute.this);
        }
        if (mApp.mCH != null) {
            mApp.mCH.userPing(getString(R.string.pageRoute), "");
        }
        ActionBar.OnNavigationListener mNavigationCallback = new ActionBar.OnNavigationListener() {
            String[] strings = getResources().getStringArray(R.array.route_action_list);
            @Override
            public boolean onNavigationItemSelected(int position, long itemId) {
                mApp.mCH.userPing(getString(R.string.pageRoute),
                        getString(R.string.pageRoute,
                                getString(R.string.atFilterTrains) + position));
                msFilterTrains = strings[position];
                if (moStartStation != null && moDestStation != null) {
                    TableLayout tl = (TableLayout) findViewById(R.id.tblRoutes);
                    tl.removeAllViews();
                    findViewById(R.id.llRowTitle).setVisibility(View.GONE);
                    showRoutes(moStartStation, moDestStation, true);
                }
                return true;
            }
        };
        maStation = new ArrayList<>();
        maStation = CGlobals_trains.mDBHelper.getStationNames();
        masRecentStations = getRecentStations();
        ab.setListNavigationCallbacks(list, mNavigationCallback);

        trainhomeScrollView = (ScrollView) findViewById(R.id.trainhomeScrollView);
        mApp.mCH.userPing(getString(R.string.pageRoute), "");
        masLines = new ArrayList<>();
        masRecentTripsList = new ArrayList<>();
        maoRecentTrips = readRecentTrips();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        try {
            mProgressDialog = new ProgressDialog(ActTrainRoute.this);
            mProgressDialog.setMessage("Getting Train Connections");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        } catch (Exception e) {
            e.printStackTrace();
        }

        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(ActTrainRoute.this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(ActTrainRoute.this)
                .addOnConnectionFailedListener(ActTrainRoute.this)
                .build();
        mGoogleApiClient.connect();
        setupUI();

        mBtnStartTime = (Button) findViewById(R.id.startTime);
        resetToCurrentTime();
        mBtnStartTime.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onClick(View arg0) {
                mApp.mCH.userPing(getString(R.string.pageRoute), getString(R.string.atChangeTime));
                showKeyboard(false);
                showDialog(TIME_DIALOG_ID);
            }
        });
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            updateMyLocation(CGlobals_lib_ss.getInstance().getMyLocation(ActTrainRoute.this));
            miDestStationId = extras.getInt("destStationId");
            if (miDestStationId > 0) {
                moDestStation = CGlobals_trains.mDBHelper.getStationFromId(miDestStationId);
                mDestView.setText(moDestStation.msSearchStr);////////////
            }
        }
        init();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case INITIAL_REQUEST:
                // If request is cancelled, the result arrays are empty.
                if (PermissionUtil.verifyPermissions(grantResults)) {
                    startFunction();
                } else {
                    showRequirementOfPermission();
                }
                break;

        }
    }
    private boolean checkPermission() {
        if ( ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED
                ) {
            return false;
        } else {
            return true;
        }
    }

    private void customDialog(String message) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ActTrainRoute.this);
        builder1.setMessage(message);
        builder1.setCancelable(true);
        builder1.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission();
                    }
                });
        builder1.setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case TIME_DIALOG_ID:
                return new TimePickerDialog(this,
                        mTimeSetListener, mHourOfDay, miMin, false);
        }
        return null;
    }

    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    enableInput();
                    mHourOfDay = hourOfDay;
                    miMin = minute;
                    changedTimeManually = true;
                    updateDisplay();
                    showRoutes(moStartStation, moDestStation, true);
                    showKeyboard(false);
                }
            };

    private void updateDisplay() {
        String sAmPm = mHourOfDay >= 12 ? " pm " : " am ";
        miHour = mHourOfDay >= 13 ? mHourOfDay - 12 : mHourOfDay;
        mtvShowTime.setText(
                new StringBuilder()
                        .append(miHour).append(":")
                        .append(pad(miMin)).append(" ")
                        .append(sAmPm));
        showKeyboard(false);
    } // updateDisplay

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    } // pad

    private void resumeFunction()
    {
        enableInput();
        showKeyboard(false);
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        updateLocation();
    }
    @Override
    public void onResume() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission())
                resumeFunction();
        } else
            resumeFunction();
        super.onResume();
    } // onResume

    private void updateLocation() {
        provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (!provider.contains("gps")) {
            txtGpsMsg.setVisibility(View.VISIBLE);
        } else {
            txtGpsMsg.setVisibility(View.GONE);
        }
        if (mbAutoUpdate && mbAutoLocation) {
            Toast.makeText(this, "Getting Location ... ", Toast.LENGTH_SHORT).show();
            mbAutoUpdate = true;
            mbAutoLocation = true;
            Location location = mApp.getBestLocation(ActTrainRoute.this);
            updateMyLocation(location);
        }
    }

    public void init() {


        if (!changedTimeManually) {
            Calendar c = Calendar.getInstance();
            mHourOfDay = c.get(Calendar.HOUR_OF_DAY);
            miHour = c.get(Calendar.HOUR);
            miMin = c.get(Calendar.MINUTE);
            updateDisplay();
        }
        if (miStartStationId > 0 || miDestStationId > 0) {
            showRoutes(moStartStation, moDestStation, false);
            mbAutoUpdate = false;
        } else {
            mbAutoUpdate = true;
        }
        if (mbAutoUpdate) {
            refreshStartStation();
        }

    }

    @Override
    protected void onPause() {
        disableInput();
        writeRecentTrips();
        try {
            if (mProgressDialog != null)
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.cancel();
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    public void onDestroy() {
        super.onDestroy();
        try {
            if (mProgressDialog != null)
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.cancel();
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Motion Sensing
    private final SensorEventListener mSensorListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent se) {

        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    ArrayList<String> getRecentStations() {
        ArrayList<String> aRS = new ArrayList<>();
        String SRECENTSTATIONS = "Station List";
        SharedPreferences spRecentStations = getSharedPreferences(
                SRECENTSTATIONS, MODE_PRIVATE);

        String sRecentStations = spRecentStations.getString(SRECENTSTATIONS,
                null);
        SSLog_SS.i("TrainApp: TrainHome - ", sRecentStations);
        if (sRecentStations != null) {
            boolean bStopThere;
            String s;
            if (sRecentStations != null & sRecentStations.trim().length() > 0) {
                String as[] = sRecentStations.split(";");
                int iLen = as.length;
                for (int i = 0; i < iLen; i++) {
                    s = as[i];
                    if (s != null && !s.equalsIgnoreCase(SELECTRECENT)) {
                        bStopThere = false;

                        for (int j = 0; j < i; j++) {
                            // s2 = as[j];
                            if (s.equals(as[j])) {
                                bStopThere = true;
                                break;
                            }
                        }
                        try {
                            if (!bStopThere) {
                                SSLog_SS.i("TrainApp: Recent Station: ", s);
                                // if (oStop != null)
                                aRS.add(s);
                            }
                        } catch (Exception e) {
                            SSLog_SS.e("TrainApp: ", "getRecentStations - " +
                                    e.getMessage());
                            SSLog_SS.e("TrainApp: ", "getRecentStations - mApp - " +
                                    mApp == null ? " null" : " ");

                        }
                    }
                }
            }
        }
        for (String st : maStation)
            aRS.add(st);
        return aRS;
    } // getRecentStations
    // Motion Sensing

    void setupUI() {


        mTvStatus = (TextView) findViewById(R.id.tvStatus);

        LinearLayout mLlRowTitle = (LinearLayout) findViewById(R.id.llRowTitle);

        mBtnStartTime = (Button) findViewById(R.id.startTime);
        mtvShowTime = (TextView) findViewById(R.id.tvShowTime);
        resetToCurrentTime();
        mBtnRecentTrips = (Button) findViewById(R.id.btnRecentTrips);
        mBtnReverseTrip = (Button) findViewById(R.id.btnReverseTrip);
        mBtnGoTrip = (Button) findViewById(R.id.btnGoTrip);

        mBtnClearStart = (Button) findViewById(R.id.btnClear);
        mBtnClearDest = (Button) findViewById(R.id.btnClearDest);
        startStationListCursor = CGlobals_trains.mDBHelper.getStationNamesCursor("");
        destStationListCursor = CGlobals_trains.mDBHelper.getStationNamesCursor("");

        mStartView = (EditText) findViewById(R.id.autocomplete_startstation);
        mDestView = (EditText) findViewById(R.id.autocomplete_deststation);
        txtGpsMsg = (TextView) findViewById(R.id.txtGpsMsg);
        mLlRowTitle.setVisibility(View.GONE);

        mStartView.invalidate();
        mStartView.clearFocus();

        mBtnRecentTrips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mbAutoUpdate = false;
                if (masRecentTripsList != null && masRecentTripsList.size() > 0) {
                    new AlertDialog.Builder(ActTrainRoute.this)
                            .setTitle("Recent Trips")
                            .setAdapter(mRecentTripsAdapter, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(ActTrainRoute.this, masRecentTripsList.get(which),
                                            Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                    mApp.mCH.userPing(getString(R.string.pageRoute), getString(R.string.atRecentTrip));
                                    setStartDest(maoRecentTrips.get(which).moStartStation, maoRecentTrips.get(which).moDestStation);
                                }

                            }).create().show();
                } else
                    Toast.makeText(ActTrainRoute.this, "No Recent Trips",
                            Toast.LENGTH_LONG).show();

            }
        });


        mStartView.post(new Runnable() {
            @Override
            public void run() {
                mbAutoUpdate = true;
                if (mbAutoLocation) {
                    updateMyLocation(CGlobals_lib_ss.getInstance().getMyLocation(ActTrainRoute.this));
                }
            }
        });
        mStartView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActTrainRoute.this, SearchStationActivity.class);
                startActivityForResult(intent, REQUEST_START);
                mBtnStartTime.setVisibility(View.VISIBLE);
//				mtvShowTime.setText(resetToCurrentTime());
//                mStartView.setText("");
                mStartView.requestFocus();
                mbAutoUpdate = false;
            }
        });

        mDestView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActTrainRoute.this, SearchStationActivity.class);
                startActivityForResult(intent, REQUEST_DEST);
                mbAutoUpdate = false;
            }
        });


        mBtnClearStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mBtnStartTime.setVisibility(View.VISIBLE);
//				mtvShowTime.setText(resetToCurrentTime());
                mStartView.setText("");
//                mStartView.setThreshold(0);
                mStartView.requestFocus();
                mbAutoUpdate = false;
                showKeyboard(true);
            }
        });
        mBtnClearDest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mDestView.setText("");
                mbAutoUpdate = false;
                showKeyboard(true);
            }
        });

        mBtnReverseTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String sStart = mStartView.getText().toString();
                String sDest = mDestView.getText().toString();
                if (!TextUtils.isEmpty(sStart))
                    moDestStation = CGlobals_trains.mDBHelper.getStationFromSearchStr(sStart);
                if (!TextUtils.isEmpty(sDest))
                    moStartStation = CGlobals_trains.mDBHelper.getStationFromSearchStr(sDest);
                mDestView.setText(sStart);
                mStartView.setText(sDest);
                showRoutes(moStartStation, moDestStation, false);
                Toast.makeText(getApplicationContext(), "Reverse trip (start and destination stations)",
                        Toast.LENGTH_SHORT).show();
                mbAutoUpdate = false;
                mApp.mCH.userPing(getString(R.string.pageRoute), getString(R.string.atFlipTrip));


            }
        });
        mBtnGoTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showRoutes(moStartStation, moDestStation, false);
            }
        });
        mBtnGoTrip.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                showKeyboard(false);
            }
        });


        aStation = new ArrayList<>();
        aStation = CGlobals_trains.mDBHelper.getStationNames();

     /*   mllStartStation = (LinearLayout) findViewById(R.id.llStartStation);
        mllStartStation.setVisibility(LinearLayout.VISIBLE);*/

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        txtGpsMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!provider.contains("gps")) { //if gps is disabled
                    try {
                        CGlobals_trains.getInstance().turnGPSOn(ActTrainRoute.this, mGoogleApiClient);
                    } catch (Exception e) {
                        Log.d(TAG, "tvGpsMsg onClick " + e.toString());
                        e.printStackTrace();
                    }

                }
            }
        });
//		keyboardShowHide(false);
    } // setupUI ends

    private void refreshStartStation() {
        mApp.mCurrentLocation = CGlobals_lib_ss.getInstance().getMyLocation(ActTrainRoute.this);
        if (!mbAutoUpdate)
            return;
        hideStatus();
        TableLayout tl = (TableLayout) findViewById(R.id.tblRoutes);
        tl.removeAllViews();

        mTvStatus.setVisibility(View.VISIBLE);
        mTvStatus.setText(R.string.waiting_location);

        if (mApp.mCurrentLocation != null && !mApp.mCurrentLocation.getProvider().equals("fake"))
            updateMyLocation(mApp.mCurrentLocation);

        if (new MyLocation().getLocation(ActTrainRoute.this, onLocationResult)) {
            Log.d(TAG, "Location available");
        } else {
            Toast.makeText(ActTrainRoute.this,
                    "Location not available - turn on GPS", Toast.LENGTH_LONG)
                    .show();
        }

    }

    private MyLocation.LocationResult onLocationResult = new MyLocation.LocationResult() {

        @Override
        public void gotLocation(Location location) {
            if (location == null) {
                if (mApp.mCurrentLocation != null) {
                    location = mApp.mCurrentLocation;
                } else {
                    // Location not found, show default or recent station
                    location =CGlobals_lib_ss.getInstance().getMyLocation(ActTrainRoute.this);
                }
            }
            if (location != null && location.getProvider().equals("fake")) {
                Log.d(TAG, "Cannot get nearest station \n Turn GPS and network location in Setup");
            }
            mApp.mCH.userPing(getString(R.string.pageRoute), getString(R.string.atTrainRouteLocation));
            CGlobals_lib_ss.setMyLocation(location, false, ActTrainRoute.this);
            updateMyLocation(location);


        }
    };
    private ArrayAdapter<String> mRecentTripsAdapter;

    @Override
    public void updateMyLocation(Location location) {
        // Now get the places at this location, if this fragment is still bound
        // to an activity

        if(location == null)
            return;
        try {
            if (!mbAutoUpdate || !mbAutoLocation)
                return;
            ActTrainRoute.this.lastLocation = CGlobals_lib_ss.getInstance().getMyLocation(ActTrainRoute.this);
//		mApp.mCurrentLocation = location;

            TableLayout tl = (TableLayout) findViewById(R.id.tblRoutes);
//		mTvStatus.setText(R.string.waiting_location);


            tl.post(new Runnable() {
                @Override
                public void run() {
                    if (ActTrainRoute.this.lastLocation != null)
                        moStartStation = CGlobals_trains.mDBHelper.getNearestStation(
                                ActTrainRoute.this.lastLocation.getLatitude(),
                                ActTrainRoute.this.lastLocation.getLongitude(),
                                ActTrainRoute.this);
                    if (moStartStation == null)
                        return;

                    EditText startView = (EditText) findViewById(R.id.autocomplete_startstation);
                    startView.setText(moStartStation.msSearchStr);

                    moDestStation = null;
                    setStartStation(moStartStation.msSearchStr);


                }
            });
        } catch (Exception e) {
            Toast.makeText(ActTrainRoute.this, "Start your GPS or Setup Network location to get your nearest station", Toast.LENGTH_LONG).show();
            Log.d(TAG, "updateMyLocation " + e.toString());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.train_route_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show();
            //			getActionBarHelper().setRefreshActionItemState(true);
            TableLayout tl = (TableLayout) findViewById(R.id.tblRoutes);
            tl.removeAllViews();
            findViewById(R.id.llRowTitle).setVisibility(View.GONE);

            init();
            //			setProgressBarIndeterminateVisibility (Boolean.TRUE);

        } else if (item.getItemId() == R.id.menu_location) {

            try {

                CGlobals_trains.getInstance().turnGPSOn(ActTrainRoute.this, mGoogleApiClient);
                mbAutoUpdate = true;
                mbAutoLocation = true;
                Location location = mApp.getBestLocation(ActTrainRoute.this);
                updateMyLocation(location);

            } catch (Exception e) {
                Log.d(TAG, "onOptionsItemSelected " + e.toString());
            }
        } else if (item.getItemId() == R.id.menu_fare) {
            ShowFare();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CGlobals_trains.REQUEST_CHECK_SETTINGS) {

            switch (resultCode) {
                case Activity.RESULT_OK: {
                    startLocationUpdates();


                    Log.d(TAG, "YES");
                    Toast.makeText(this, "Getting Location ... ", Toast.LENGTH_SHORT).show();
                    mbAutoUpdate = true;
                    mbAutoLocation = true;

                    break;
                }
                case Activity.RESULT_CANCELED: {
                    Log.d(TAG, "NO");
                    txtGpsMsg.setVisibility(View.VISIBLE);
                    break;
                }
                default:
                    break;
            }
        }
        if (requestCode == REQUEST_DEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Bundle extra = data.getBundleExtra("data");
                String station = extra.getString("station");

                setDestStation(station);
            }

        }
        if (requestCode == REQUEST_START && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Bundle extra = data.getBundleExtra("data");
                String station = extra.getString("station");
                setStartStation(station);
            }

        }
    }


    void ShowFare() {
        if (moStartStation != null && moDestStation != null) {
            Toast.makeText(this, "Showing fare", Toast.LENGTH_SHORT).show();
            Intent fare = new Intent(ActTrainRoute.this, ActFares.class);
            fare.putExtra("startstationid", moStartStation.miStationId);
            fare.putExtra("deststationid", moDestStation.miStationId);
            startActivity(fare);
        } else {
            Toast.makeText(this, "Please enter a start and destination " +
                    "station first to get the fare", Toast.LENGTH_SHORT).show();
        }
    }

    void resetToCurrentTime() {
        Calendar c = Calendar.getInstance();
        mHourOfDay = c.get(Calendar.HOUR_OF_DAY);
        miMin = c.get(Calendar.MINUTE);
        updateDisplay();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mbAutoUpdate)
        {
            CGlobals_lib_ss.setMyLocation(location, false, ActTrainRoute.this);
            updateMyLocation(location);}
    }

    class Trip {
        Station moStartStation, moDestStation;
        String msTrip;

        Trip(Station oStartStation, Station oDestStation) {
            moStartStation = oStartStation;
            moDestStation = oDestStation;

            init();
        }

        Trip(int iStartStationId, int iDestStationId) {
            moStartStation = new Station();
            moDestStation = new Station();
            moStartStation = CGlobals_trains.mDBHelper.getStationFromId(iStartStationId);
            moDestStation = CGlobals_trains.mDBHelper.getStationFromId(iDestStationId);
            init();
        }

        private void init() {
            msTrip = moStartStation.msSearchStr + "\n - " + moDestStation.msSearchStr;

        }
    }


    // Add a trip chosen by user
    void addRecentTrip(Station oStartStation, Station oDestStation) {
        if (maoRecentTrips == null)
            maoRecentTrips = new ArrayList<>();
        if (masRecentTripsList == null)
            masRecentTripsList = new ArrayList<>();
        maoRecentTrips.add(0, new Trip(oStartStation, oDestStation)); // add as first trip FIFO
        int iLen = maoRecentTrips.size();
        if (iLen > MAX_RECENT_TRIPS)
            maoRecentTrips.remove(iLen - 1);
        // remove the same Station lower in the list
        iLen = maoRecentTrips.size();
        Trip trip;
        for (int i = 1; i < iLen; i++) {
            trip = maoRecentTrips.get(i);
            if (trip.moStartStation.miStationId == oStartStation.miStationId &&
                    trip.moDestStation.miStationId == oDestStation.miStationId) {
                maoRecentTrips.remove(i);
                break;
            }
        }
        masRecentTripsList.clear();
        for (Trip tr : maoRecentTrips) {
            masRecentTripsList.add(tr.msTrip);
        }
/*		masRecentTripsList.add(" Trip: " + oStartStation.msSearchStr + " _ " +
                oDestStation.msSearchStr + " at " + new CTime(mHourOfDay, miMin).msTm); */
        mRecentTripsAdapter = new ArrayAdapter<>(this,
                R.layout.multiline_spinner_dropdown_item, masRecentTripsList);

    } // addRecentTrips

    private void writeRecentTrips() {
        new Thread(new Runnable() {
            public void run() {
                StringBuilder sb = new StringBuilder();
                String lineDelim = "";
                int iLen = maoRecentTrips.size();
                if (iLen < 1)
                    return;
                String s;
                for (int i = 0; i < iLen; i++) {
                    s = Integer.toString(maoRecentTrips.get(i).moStartStation.miStationId)
                            + ";" + Integer.toString(maoRecentTrips.get(i).moDestStation.miStationId);
                    if (!TextUtils.isEmpty(s)) {
                        sb.append(lineDelim).append(s);
                        lineDelim = "\n";
                    }
                }
                SharedPreferences spRecentTrips = getSharedPreferences(
                        RECENT_TRIPS_TAG, MODE_PRIVATE);
                SharedPreferences.Editor speRecentStopsEditor;
                speRecentStopsEditor = spRecentTrips.edit();

                speRecentStopsEditor.putString(RECENT_TRIPS_TAG, sb.toString());
                speRecentStopsEditor.apply();
            }
        }).start();

    } // writeRecentTrips

    ArrayList<Trip> readRecentTrips() {
        ArrayList<Trip> aRS = new ArrayList<>();
        SharedPreferences spRecentStations = getSharedPreferences(
                RECENT_TRIPS_TAG, MODE_PRIVATE);

        String sRecentTrips = spRecentStations.getString(RECENT_TRIPS_TAG,
                null);
        SSLog_SS.i(TAG, "readRecentTrips - " + sRecentTrips);
        if (sRecentTrips == null)
            return aRS;
        boolean bStopThere;
        String stationPair;
        String[] s;

        int iStart, iDest;
        if (sRecentTrips != null & sRecentTrips.trim().length() > 0) {
            String as[] = sRecentTrips.split("\n");
            for (String a : as) {
                stationPair = a;
                if (stationPair != null && !stationPair.equalsIgnoreCase(SELECTRECENT)) {
                    bStopThere = false;

                    try {
                        if (!bStopThere) {
                            SSLog_SS.i(TAG, "Recent Trip: " + stationPair);
                            s = stationPair.split(";");
                            iStart = Integer.valueOf(s[0]);
                            iDest = Integer.valueOf(s[1]);
                            aRS.add(new Trip(iStart, iDest));
                        }
                    } catch (Exception e) {
                        SSLog_SS.e(TAG, "getRecentTrip - " + e.getMessage());
                        cleanUp();
                    }
                }
            }
        }
        masRecentTripsList.clear();
        for (Trip tr : aRS) {
            masRecentTripsList.add(tr.msTrip);
        }
        mRecentTripsAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, masRecentTripsList);

        return aRS;
    } // readRecentTrips


    private void cleanUp() {
        try {
            if (mProgressDialog.isShowing())
                mProgressDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        if (mApp == null)
            SSLog_SS.e(TAG, "ActTrainRoute -" + " mApp is Null");
        else if (mApp.stackActivity == null)
            SSLog_SS.e(TAG, "ActTrainRoute -" + " stackActivity is Null");
        else {
            setResult(RESULT_CANCELED);
        }
        super.finish();
    }

    /////////////87145
    void setStartStation(String sStartStationFull) {
        if (mApp.mCurrentLocation == null || mApp.mCurrentLocation.getProvider().equals("fake")) {
            mTvStatus.setText("Start your GPS or Setup Network location to get your nearest station");
            mTvStatus.setVisibility(View.GONE);
        } else {
            mTvStatus.setVisibility(View.GONE);
        }

        Station retStn = CGlobals_trains.mDBHelper
                .getStationFromSearchStr(sStartStationFull);
        if (retStn != null) {
            miStartStationId = retStn.miStationId;
            moStartStation = retStn;

//            mStartView.setThreshold(1000);
            mStartView.setText(moStartStation.msSearchStr);
            mStartView.setText(sStartStationFull);
//            mStartView.dismissDropDown();

            if (!TextUtils.isEmpty(mStartView.getText().toString()) &&
                    !TextUtils.isEmpty(mDestView.getText().toString()))
                showRoutes(moStartStation, moDestStation, true);
        }
    }

    void setDestStation(String sStation) {
        Station retStn = CGlobals_trains.mDBHelper
                .getStationFromSearchStr(sStation);
        if (retStn != null) {
            moDestStation = retStn;

//            mDestView.setThreshold(1000);
            mDestView.setText(moDestStation.msSearchStr);
            mDestView.setText(sStation);
            mTvStatus.setVisibility(View.GONE);
//            mDestView.dismissDropDown();
            showRoutes(moStartStation, moDestStation, true);


        }
    }


    void showKeyboard(boolean bShow) {
        if (bShow) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        } else {
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    INPUT_METHOD_SERVICE);

            View cFocus = getCurrentFocus();
            if (imm != null && cFocus != null)
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            else
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }

    private void hideStatus() {
        mTvStatus.setText("");
        mTvStatus.setVisibility(View.GONE);

    }

    private class GetRoutesTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... sUrl) {
            try {

                Calendar cal = Calendar.getInstance();
                int iDow = cal.get(Calendar.DAY_OF_WEEK);
                int bSunday = iDow == Calendar.SUNDAY ? 1 : 2;
                if (maoConn.size() > 0)
                    maoConn.clear();
                ArrayList<Route> aoRoute = CGlobals_trains.mDBHelper.getDestinationRoutes(miStartStationId, miDestStationId,
                        new CTime(mHourOfDay, miMin), msFilterTrains);

                int incr = (int) (80.0 / aoRoute.size() / 3);
                int prog = 10;
                mbDirectTrainsOnly = true;
                StringBuilder sbProgressMessage = new StringBuilder();
                sbProgressMessage.append(msProgressMessage);
                for (Route route : aoRoute) {
                    if (route.miConn == 0)
                        sbProgressMessage.append("\n").append(route.msStartStationAbbr).append(" to ").append(route.msDestStationAbbr);
                    else {
                        mbDirectTrainsOnly = false;
                        sbProgressMessage.append("\n").append(route.msStartStationAbbr).append(" - ").append(route.msConnStationAbbr).append(" - ").append(route.msDestStationAbbr);
                    }
                }
                if (aoRoute.size() == 0) {
                    sbProgressMessage.setLength(0);
                    String msg = "No routes found: Try changing filter: " +
                            msFilterTrains + " or Start and Destination";
                    sbProgressMessage.append(msg);
                    return msg;
                }
                sbProgressMessage.append("\nAnalyzing Routes ...\n");
                String sProgressHeader = sbProgressMessage.toString();
                msProgressMessage = sProgressHeader;
                publishProgress(prog);
                ArrayList<Connection> aoConn;
                for (Route route : aoRoute) {
                    if (route.miConn == 0) {
                        aoConn = CGlobals_trains.mDBHelper.getDirect(DIRECT_TABLE,
                                route.miStartStationId, route.miDestStationId, mTime, bSunday, msFilterTrains);
                        prog += incr * 3;
                        if (aoConn != null) {
                            msProgressMessage = sProgressHeader + "Direct: (" + route.msLineCode + ")" + route.msStartStation
                                    + " to " + route.msDestStation + " (" + aoConn.size() + ")";
                        }
                        publishProgress(prog);
                    } else {
                        long count = CGlobals_trains.mDBHelper.startStationsTable(route.miStartStationId, route.miConnStationId, mTime, bSunday, msFilterTrains);
                        if (count == 0)
                            continue;
                        prog += incr;
                        msProgressMessage = sProgressHeader + "Routes: " + route.msStartStation
                                + " to " + route.msConnStation;
                        publishProgress(prog);
                        CGlobals_trains.mDBHelper.destStationsTable(route.miStartStationId, route.miDestStationId,
                                route.miConnStationId, mTime, bSunday, msFilterTrains);
                        prog += incr;
                        msProgressMessage = sProgressHeader + "Routes: " + route.msConnStation
                                + " to " + route.msDestStation;

                        publishProgress(prog);
                        aoConn = CGlobals_trains.mDBHelper.getConnections(route.miStartStationId, route.miDestStationId,
                                route.miConnStationId, route.msStartStation,
                                route.msDestStation, route.msConnStation, route.msStartStationAbbr,
                                route.msDestStationAbbr, route.msConnStationAbbr, mTime);
                        prog += incr;
                        msProgressMessage = sProgressHeader + "Results: " + route.msStartStation
                                + " to " + route.msDestStation;
                        publishProgress(prog);
                    }
                    publishProgress(prog);

                    if (aoConn != null) {
                        for (Connection c : aoConn) {

                            if (route.miConn == 0) {
                                c.msStartStation = route.msStartStation;
                                c.msDestStation = route.msDestStation;
                                c.msStartStationAbbr = route.msStartStationAbbr;
                                c.msDestStationAbbr = route.msDestStationAbbr;
                            }

                            maoConn.add(c);
                        }
                    }
                }
                Collections.sort(maoConn, new Comparator<Object>() {

                    public int compare(Object o1, Object o2) {
                        Connection c1 = (Connection) o1;
                        Connection c2 = (Connection) o2;
                        return (c1.miSortMins - c2.miSortMins);
                    }
                });
                msProgressMessage = sProgressHeader + "Final Step: Updating display ";
                publishProgress(90);
            } catch (Exception e) {
                SSLog_SS.e(TAG, "doInBackground - " + e.getMessage());
                cleanUp();
            }
            return null;
        } // doInBackground

        @Override
        protected void onPreExecute() {
            showKeyboard(false);
            maoConn = new ArrayList<>();

            try {
                if (!isFinishing())
                    mProgressDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            try {
                mProgressDialog.setProgress(progress[0]);
                mProgressDialog.setMessage(msProgressMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                publishProgress(90);
                updateRoutes(maoConn);
                publishProgress(100);
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
                mBtnGoTrip.requestFocus();
                showKeyboard(false);
                msProgressMessage = "";
                mTvStatus.setVisibility(View.GONE);
                if (result != null) {
                    Toast.makeText(ActTrainRoute.this, result,
                            Toast.LENGTH_LONG).show();

                }

            } catch (Exception e) {
                SSLog_SS.e(TAG, "ActTrainRoute: PostExecute - " + e.getMessage());
                cleanUp();
            }
            super.onPostExecute(result);
        }
    } // class GetRoutesTask


    private void showRoutes(Station startStation, Station destStation, boolean bSilent) {
        showKeyboard(false);
        msProgressMessage = "";
        if (startStation == null) {
            if (!bSilent) {
//                Toast.makeText(getApplicationContext(), "Invalid start station",
//                        Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Invalid start station");
            }
            return;
        }
        if (destStation == null) {
            boolean bFailed = true;
            String sDest = mDestView.getText().toString();
            if (!TextUtils.isEmpty(sDest))
                moDestStation = CGlobals_trains.mDBHelper.getStationFromSearchStr(sDest);
            if (moDestStation != null)
                destStation = moDestStation;
            if (bFailed && moDestStation == null) {
                if (!bSilent)
                    Toast.makeText(getApplicationContext(), "Please enter destination station",
                            Toast.LENGTH_SHORT).show();
                return;
            }

        }
        moStartStation = startStation;
        moDestStation = destStation;

        if (moStartStation.miStationId == moDestStation.miStationId) {
            if (!bSilent)
                Toast.makeText(getApplicationContext(), "Same Start and Destination station",
                        Toast.LENGTH_SHORT).show();
            return;
        }
        addRecentTrip(startStation, destStation);
        mApp.mCH.userPing(getString(R.string.pageRoute),
                getString(R.string.atShowRoute) + "_" +
                        moStartStation.miStationId + "_" + moDestStation.miStationId);

        msProgressMessage = startStation.msSearchStr + " to  " + destStation.msSearchStr + "\n";
        mProgressDialog.setMessage(msProgressMessage);

        miStartStationId = startStation.miStationId;
        miDestStationId = destStation.miStationId;
        msStartStationName = startStation.msSearchStr;
        msDestStationName = destStation.msSearchStr;
        mTime = new CTime(mHourOfDay, miMin);
        new GetRoutesTask().execute("");

    } // showRoutes

    private void updateRoutes(ArrayList<Connection> aoConn) {
        int temp = 0;
        int mCurrentTime;
        int mStartTime = 0;
        boolean setBackground = false;
        mCurrentTime = mHourOfDay * 60 + miMin;
        trainhomeScrollView.smoothScrollTo(0, 0);
        showKeyboard(false);
        mTvStatus.setVisibility(View.GONE);
        LayoutInflater inflater = getLayoutInflater();
        TableLayout tl = (TableLayout) findViewById(R.id.tblRoutes);
        tl.removeAllViews();


        TextView tvTrip = (TextView) findViewById(R.id.tvTrip);
        tvTrip.setText(msStartStationName + " - " + msDestStationName);
        int miPrevMins = -1;
        LinearLayout llRowTitle = (LinearLayout) findViewById(R.id.llRowTitle);

        if (aoConn.size() == 0) {
            mTvStatus.setVisibility(View.VISIBLE);
            mTvStatus.setText("Cannot find any trains\nTry changing the Destination Station (e.g. Kurla, Dadar) or the time");
            llRowTitle.setVisibility(View.GONE);

            return;
        }
        llRowTitle.setVisibility(View.VISIBLE);
        int nRowCount = 0;
        ArrayList<Integer> aTrainShown = new ArrayList<>();
        boolean bTrainShown;
        try {
            int j = 0;
            for (Connection conn : aoConn) {
                if (nRowCount > MAX_TRAINS_TO_SHOW)
                    break;
                bTrainShown = false;
                for (Integer iTrainId : aTrainShown) {
                    if (iTrainId.equals(conn.miTrainId1))
                        bTrainShown = true;
                    break;
                }
                if (!bTrainShown) {
                    aTrainShown.add(conn.miTrainId1);
                }

                if (msFilterTrains.equals("Fast"))
                    if (!conn.msConnSpeed1.equals("F") && !conn.msConnSpeed2.equals("F"))
                        continue;
                  /*  else if (msFilterTrains.equals("Slow"))
                        if (!conn.msConnSpeed1.equals("S") && (!conn.msConnSpeed2.equals("S") || TextUtils.isEmpty(conn.msConnSpeed2)))
                            continue;*/
                nRowCount++;

                final Connection currentConnection = conn;

                CardView tr = (CardView) inflater.inflate(R.layout.trainconnectionsrow, tl, false);
                TextView tvVia = (TextView) tr.findViewById(R.id.tvVia);
                if (mbDirectTrainsOnly)
                    tvVia.setVisibility(View.GONE);

                RelativeLayout rlLongWait = (RelativeLayout) tr.findViewById(R.id.rlLongWait);
                if ((miPrevMins != -1 && conn.miStartMin > miPrevMins + LONG_WAIT_MINS) ||    // This train long wait
                        (nRowCount == 1 && conn.miStartMin > mTime.miTimeMin + LONG_WAIT_MINS)) { // first train long wait
                    rlLongWait.setVisibility(View.VISIBLE);
                } else
                    rlLongWait.setVisibility(View.GONE);

                miPrevMins = conn.miStartMin;


                TextView tvStartTime = (TextView) tr.findViewById(R.id.startTime);
                TextView tvStartStnAbbr = (TextView) tr.findViewById(R.id.startStnAbbr);
                TextView tvConnSpeed1 = (TextView) tr.findViewById(R.id.connSpeed1);
                TextView tvLineCode1 = (TextView) tr.findViewById(R.id.linecode1);
                TextView tvConnTime1 = (TextView) tr.findViewById(R.id.connTime1);
                TextView tvConnStnAbbr1 = (TextView) tr.findViewById(R.id.connStnAbbr1);

                TextView tvConnTime2 = (TextView) tr.findViewById(R.id.connTime2);
                TextView tvConnStnAbbr2 = (TextView) tr.findViewById(R.id.connStnAbbr2);
                TextView tvConnSpeed2 = (TextView) tr.findViewById(R.id.connSpeed2);
                TextView tvLineCode2 = (TextView) tr.findViewById(R.id.linecode2);
                TextView tvDestStnAbbr = (TextView) tr.findViewById(R.id.destStnAbbr);
                TextView tvDestTime = (TextView) tr.findViewById(R.id.destTime);
                TextView tvPlatForm1 = (TextView) tr.findViewById(R.id.platform1);
                TextView tvPlatform2 = (TextView) tr.findViewById(R.id.platform2);
                TextView tvCars1 = (TextView) tr.findViewById(R.id.cars1);
                TextView tvCars2 = (TextView) tr.findViewById(R.id.cars2);
                ImageView tvExtraLine1 = (ImageView) tr.findViewById(R.id.tvExtraLine1);
                TextView tvExtraLine2 = (TextView) tr.findViewById(R.id.tvExtraLine2);

                LinearLayout dl2 = (LinearLayout) tr.findViewById(R.id.dl2);
                TextView tvFirstLast = (TextView) tr.findViewById(R.id.firstlast);
                LinearLayout ll2 = (LinearLayout) tr.findViewById(R.id.ll2);
                LinearLayout rlt = (LinearLayout) tr.findViewById(R.id.rlTotalTravelTime);
                TextView tvTotalTravelTime = (TextView) tr.findViewById(R.id.totalTravelTime);

                mBtnMore = (ImageView) tr.findViewById(R.id.btnMore);
                mBtnSMS = (ImageView) tr.findViewById(R.id.btnSMS);
                LinearLayout ll3 = (LinearLayout) tr.findViewById(R.id.ll3);
                if (CGlobals_trains.mbIsAdmin)
                    ll3.setVisibility(View.VISIBLE);
                TextView tvTrain1 = (TextView) tr.findViewById(R.id.train1);
                TextView tvTrain2 = (TextView) tr.findViewById(R.id.train2);
                tvTrain1.setText(conn.msTrainNo1);
                tvTrain2.setText(conn.msTrainNo2);
                tvStartTime.setText(conn.msStartTime);
                tvStartStnAbbr.setText(conn.msStartStationAbbr);
                tvConnSpeed1.setText(conn.msConnSpeed1);
                tvLineCode1.setText(conn.msLineCode1);
                tvDestTime.setText(conn.msDestTime);
                tvPlatForm1.setText(conn.msPlatform1);
                tvCars1.setText(Integer.toString(conn.miCar1));
                if (!TextUtils.isEmpty(conn.msSpl1)) {
                    tvExtraLine1.setVisibility(View.VISIBLE);
                    //tvExtraLine1.setText(conn.msSpl1);
                }
                if (!TextUtils.isEmpty(conn.msSpl2)) {
                    tvExtraLine2.setVisibility(View.VISIBLE);
                    //  tvExtraLine2.setText(conn.msSpl2);
                }


                if (conn.miConnMin2 > 0) { // Connecting trains
                    ll2.setVisibility(View.VISIBLE);
                    dl2.setVisibility(View.GONE);
                    rlt.setVisibility(View.VISIBLE);

                    tvConnStnAbbr1.setText(conn.msConnStationAbbr);
                    tvConnStnAbbr2.setText(conn.msConnStationAbbr);
                    tvConnTime1.setText(conn.msConnTime1);
                    tvConnSpeed2.setText(conn.msConnSpeed2);
                    tvLineCode2.setText(conn.msLineCode2);
                    tvDestStnAbbr.setText(conn.msDestStationAbbr);
                    tvConnTime2.setText(conn.msConnTime2);
                    if (conn.miConnMin2 - conn.miConnMin1 > LONG_WAIT_MINS)
                        tvConnTime2.setTextColor(Color.RED);
                    tvPlatform2.setText(conn.msPlatform2);
                    tvCars2.setText(Integer.toString(conn.miCar2));
                } else { // Direct trains
                    dl2.setVisibility(View.VISIBLE);
                    tvConnSpeed1.setText(conn.msConnSpeed1);
                    tvLineCode1.setText(conn.msLineCode1);
                    tvConnTime1.setText(conn.msDestTime);
                    tvConnStnAbbr1.setText(conn.msDestStationAbbr);
                    tvFirstLast.setText(conn.msFirstStation1 + " - " + conn.msLastStation1);
                    ll2.setVisibility(View.GONE);
                }
                int iTripTime = conn.miDestMin - conn.miStartMin;
                if (iTripTime < 0) {
                    int id, is;
                    id = conn.miDestMin > MINS_IN_DAY ? conn.miDestMin - MINS_IN_DAY : conn.miDestMin;
                    is = conn.miStartMin > MINS_IN_DAY ? conn.miStartMin - MINS_IN_DAY : conn.miStartMin;
                    iTripTime = id - is;
                }
                int iHr = iTripTime / 60;
                int iMin = iTripTime % 60;
                CTime tripTime = new CTime(iHr, iMin);
                tvTotalTravelTime.setText(tripTime.msHrMin);
                if (conn.mbDirect) {
                    tvVia.setText("Direct");
                } else {
                    tvVia.setText("Via " + conn.msConnStation);
                }
                if (!TextUtils.isEmpty(conn.msColour1))
                    tvLineCode1.setTextColor(Color.parseColor(conn.msColour1));
                if (!TextUtils.isEmpty(conn.msColour2))
                    tvLineCode2.setTextColor(Color.parseColor(conn.msColour2));

                //		dec.format(cb.dTrainTime));
                String sp1 = conn.msConnSpeed1;
                String sp2 = conn.msConnSpeed2;
                tvConnSpeed1.setTextColor(Color.parseColor("#228b22"));
                if (sp1.equals(CGlobals_trains.FASTABBR) || sp1.equals(CGlobals_trains.DOUBLEFASTABBR))
                    tvConnSpeed1.setTextColor(Color.RED);
                tvConnSpeed2.setTextColor(Color.parseColor("#228b22"));
                if (sp2.equals(CGlobals_trains.FASTABBR) || sp2.equals(CGlobals_trains.DOUBLEFASTABBR))
                    tvConnSpeed2.setTextColor(Color.RED);


                tr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        try {
                            mApp.mCH.userPing(getString(R.string.pageRoute), getString(R.string.atTrip));

                            Intent i = new Intent(ActTrainRoute.this, ActTrainTrip.class);
                            i.putExtra("trainId1", currentConnection.miTrainId1);
                            i.putExtra("trainNo1", currentConnection.msTrainNo1);
                            i.putExtra("lineCode1", currentConnection.msLineCode1);
                            i.putExtra("lineCode2", currentConnection.msLineCode2);
                            i.putExtra("trainId2", currentConnection.miTrainId2);
                            i.putExtra("trainNo2", currentConnection.msTrainNo2);
                            i.putExtra("startStationId", moStartStation.miStationId);
                            i.putExtra("startStationName", moStartStation.msStationName);
                            i.putExtra("startStationAbbr", moStartStation.msStationAbbr);
                            i.putExtra("destStationId", moDestStation.miStationId);
                            i.putExtra("destStationName", moDestStation.msStationName);
                            i.putExtra("destStationAbbr", moDestStation.msStationAbbr);
                            i.putExtra("connStationId", currentConnection.miConnStationId);
                            i.putExtra("connStationName", currentConnection.msConnStation);
                            i.putExtra("connStationNameAbbr", currentConnection.msConnStationAbbr);
                            i.putExtra("startTime", currentConnection.mdStartTime);
                            i.putExtra("connTime1", currentConnection.mdConnTime1);
                            i.putExtra("connTime2", currentConnection.mdConnTime2);
                            i.putExtra("destTime", currentConnection.mdDestTime);
                            i.putExtra("platform1", currentConnection.msPlatform1);
                            i.putExtra("platform2", currentConnection.msPlatform2);
                            i.putExtra("platformSide1", currentConnection.msPlatformSide1);
                            i.putExtra("platformSide2", currentConnection.msPlatformSide2);
                            i.putExtra("directOnly", mbDirectTrainsOnly);


                            startActivity(i);
                        } catch (Exception e) {
                            Log.d(TAG, "SET ON CLICK tr " + e.toString());
                        }
                    }
                });

                mBtnMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        try {
                            Context mContext = getApplicationContext();
                            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                            View layout = inflater.inflate(R.layout.route_more_dlg,
                                    (ViewGroup) findViewById(R.id.layout_root));
                            TextView tvFromTo = (TextView) layout.findViewById(R.id.tvFromTo);
                            TextView tvLeg1a = (TextView) layout.findViewById(R.id.tvLeg1a);
                            TextView tvLeg1b = (TextView) layout.findViewById(R.id.tvLeg1b);
                            TextView tvChangeTrain = (TextView) layout.findViewById(R.id.changeTrain);
                            TextView tvLeg2a = (TextView) layout.findViewById(R.id.tvLeg2a);
                            TextView tvLeg2b = (TextView) layout.findViewById(R.id.tvLeg2b);
                            mApp.mCH.userPing(getString(R.string.pageRoute), getString(R.string.atMore));


                            String sFromTo = moStartStation.msStationName + " (" + currentConnection.msLineCode1
                                    + ")" + " - " + moDestStation.msStationName;
                            if (mbDirectTrainsOnly || currentConnection.mdConnTime2 == 0)
                                sFromTo += " (" + currentConnection.msLineCode1 + ")";
                            else
                                sFromTo += " (" + currentConnection.msLineCode2 + ")";
                            tvFromTo.setText(sFromTo);
                            msLeg1a = Html.fromHtml("Board <b>" + (new CTime(currentConnection.miStartMin).msTm) + "</b> at " +
                                    moStartStation.msStationName +
                                    ", Plat. <b>" + currentConnection.msPlatform1 +
                                    "</b> to " + currentConnection.msLastStation1 + " (origin " + currentConnection.msFirstStation1 +
                                    (currentConnection.miCar1 > 0 ? ", " + currentConnection.miCar1 + " cars" : "") +
                                    (!TextUtils.isEmpty(currentConnection.msTrainNo1) ? ", " + currentConnection.msTrainNo1 : "") +
                                    ")");
                            String sPlatformSide = "";
                            if (!TextUtils.isEmpty(currentConnection.msPlatformSide1))
                                sPlatformSide = (currentConnection.msPlatformSide1.equals("R") ? "<b> on Right</b>" :
                                        (currentConnection.msPlatformSide1.equals("L") ? "<b> on Left</b>" :
                                                currentConnection.msPlatformSide1.equals("B") ? "<b> on Any Side</b>" : " "));
//						sPlatformSide += curConn.miCar1 > 0 ? " (<b>" + curConn.miCar1 + "</b> Cars)" : "";
                            if (mbDirectTrainsOnly || currentConnection.miConnMin1 <= 0)
                                msLeg1b = Html.fromHtml("Alight <b>" + (new CTime(currentConnection.miDestMin).msTm)
                                        + "</b> at " + msDestStationName + sPlatformSide);
                            else
                                msLeg1b = Html.fromHtml("Alight <b>" + (new CTime(currentConnection.miConnMin1).msTm) + "</b> at "
                                        + currentConnection.msConnStation + sPlatformSide);

                            msLeg2a = null;
                            msLeg2b = null;

                            if (!mbDirectTrainsOnly && currentConnection.mdConnTime2 > 0) {
                                String goToPlatform = TextUtils.isEmpty(currentConnection.msPlatform2) ? "" : " Plat. <b>"
                                        + currentConnection.msPlatform2 + "</b>";
                                String sChangeLine = currentConnection.msLineCode1.equals(currentConnection.msLineCode2) ? "" : ", Go to <b>" +
                                        currentConnection.msLineCode2 + "</b>";
                                if (!TextUtils.isEmpty(currentConnection.msConnStation)) {
                                    msChangeTrain = Html.fromHtml("At " + currentConnection.msConnStation + sChangeLine + goToPlatform);
                                }
                                msLeg2a = Html.fromHtml("Board <b>" + (new CTime(currentConnection.miConnMin2).msTm) + "</b> at " +
                                        currentConnection.msConnStation +
                                        ", Plat. <b>" + currentConnection.msPlatform2 +
                                        "</b> to " + currentConnection.msLastStation2 + " (origin " + currentConnection.msFirstStation2 +
                                        (currentConnection.miCar1 > 0 ? ", " + currentConnection.miCar2 + " cars" : "") +
                                        (!TextUtils.isEmpty(currentConnection.msTrainNo2) ? ", " + currentConnection.msTrainNo2 : "") +
                                        ")");

                                if (!TextUtils.isEmpty(currentConnection.msPlatformSide2))

                                    sPlatformSide = (currentConnection.msPlatformSide2.equals("L") ? "<b> on Left</b>" :
                                            currentConnection.msPlatformSide2.equals("R") ? "<b> on Right</b>" :
                                                    currentConnection.msPlatformSide2.equals("B") ? "<b> on Any Side</b>" : " ");

                                msLeg2b = Html.fromHtml("Alight <b>" +
                                        ((new CTime(currentConnection.miDestMin).msTm)) + "</b> at " +
                                        msDestStationName + sPlatformSide);
                            }
                            tvLeg1a.setText(msLeg1a);
                            tvLeg1b.setText(msLeg1b);
                            tvChangeTrain.setText(msChangeTrain);

                            if (mbDirectTrainsOnly || currentConnection.mdConnTime2 == 0) {
                                tvLeg2a.setVisibility(View.GONE);
                                tvLeg2b.setVisibility(View.GONE);
                                tvChangeTrain.setVisibility(View.GONE);
                            } else {
                                tvLeg2a.setVisibility(View.VISIBLE);
                                tvLeg2b.setVisibility(View.VISIBLE);
                                tvLeg2a.setText(msLeg2a);
                                tvLeg2b.setText(msLeg2b);
                            }

                            sMessage = sFromTo + " " + msLeg1a + " " +
                                    msLeg1b;
                            if (!TextUtils.isEmpty(msChangeTrain))
                                sMessage += " " + msChangeTrain;
                            if (!TextUtils.isEmpty(msLeg2a))
                                sMessage += " " + msLeg2a;
                            if (!TextUtils.isEmpty(msLeg2b))
                                sMessage += " " + msLeg2b;


                            new AlertDialog.Builder(ActTrainRoute.this)
                                    .setTitle("Route Details")

                                    .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    })
                                    .setView(layout).create().show();
                        } catch (Exception e) {
                            Log.d(TAG, "mBtnMore.setOnClickListener " + e.toString());
                        }

                    }
                });

                mBtnSMS.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        try {
                            if (moStartStation != null && moDestStation != null &&
                                    !TextUtils.isEmpty(moStartStation.msStationName) &&
                                    !TextUtils.isEmpty(moDestStation.msStationName)) {
                                mApp.mCH.userPing(getString(R.string.pageRoute), getString(R.string.atSendSMS));

                                String sFromTo = moStartStation.msStationName + " (" + currentConnection.msLineCode1
                                        + ")" + " - " + moDestStation.msStationName;
                                if (mbDirectTrainsOnly || currentConnection.mdConnTime2 == 0)
                                    sFromTo += " (" + currentConnection.msLineCode1 + ")";
                                else
                                    sFromTo += " (" + currentConnection.msLineCode2 + ")";
                                msLeg1a = Html.fromHtml("Board <b>" + (new CTime(currentConnection.miStartMin).msTm) + "</b> at " +
                                        moStartStation.msStationName +
                                        ", Plat. <b>" + currentConnection.msPlatform1 +
                                        "</b> to " + currentConnection.msLastStation1 + " (origin " + currentConnection.msFirstStation1 +
                                        (currentConnection.miCar1 > 0 ? ", " + currentConnection.miCar1 + " cars" : "") +
                                        (!TextUtils.isEmpty(currentConnection.msTrainNo1) ? ", " + currentConnection.msTrainNo1 : "") +
                                        ")");
                                String sPlatformSide = "";
                                if (!TextUtils.isEmpty(currentConnection.msPlatformSide1))
                                    sPlatformSide = (currentConnection.msPlatformSide1.equals("R") ? "<b> on Right</b>" :
                                            (currentConnection.msPlatformSide1.equals("L") ? "<b> on Left</b>" :
                                                    currentConnection.msPlatformSide1.equals("B") ? "<b> on Any Side</b>" : " "));
                                if (mbDirectTrainsOnly || currentConnection.miConnMin1 <= 0)
                                    msLeg1b = Html.fromHtml("Alight <b>" + (new CTime(currentConnection.miDestMin).msTm)
                                            + "</b> at " + msDestStationName + sPlatformSide);
                                else
                                    msLeg1b = Html.fromHtml("Alight <b>" + (new CTime(currentConnection.miConnMin1).msTm) + "</b> at "
                                            + currentConnection.msConnStation + sPlatformSide);
                                msLeg2a = null;
                                msLeg2b = null;
                                if (!mbDirectTrainsOnly && currentConnection.mdConnTime2 > 0) {
                                    String goToPlatform = TextUtils.isEmpty(currentConnection.msPlatform2) ? "" : " Plat. <b>"
                                            + currentConnection.msPlatform2 + "</b>";
                                    String sChangeLine = currentConnection.msLineCode1.equals(currentConnection.msLineCode2) ? "" : ", Go to <b>" +
                                            currentConnection.msLineCode2 + "</b>";
                                    if (!TextUtils.isEmpty(currentConnection.msConnStation)) {
                                        msChangeTrain = Html.fromHtml("At " + currentConnection.msConnStation + sChangeLine + goToPlatform);
                                    }
                                    msLeg2a = Html.fromHtml("Board <b>" + (new CTime(currentConnection.miConnMin2).msTm) + "</b> at " +
                                            currentConnection.msConnStation +
                                            ", Plat. <b>" + currentConnection.msPlatform2 +
                                            "</b> to " + currentConnection.msLastStation2 + " (origin " + currentConnection.msFirstStation2 +
                                            (currentConnection.miCar1 > 0 ? ", " + currentConnection.miCar2 + " cars" : "") +
                                            (!TextUtils.isEmpty(currentConnection.msTrainNo2) ? ", " + currentConnection.msTrainNo2 : "") +
                                            ")");

                                    if (!TextUtils.isEmpty(currentConnection.msPlatformSide2))
                                        sPlatformSide = (currentConnection.msPlatformSide2.equals("L") ? "<b> on Left</b>" :
                                                currentConnection.msPlatformSide2.equals("R") ? "<b> on Right</b>" :
                                                        currentConnection.msPlatformSide2.equals("B") ? "<b> on Any Side</b>" : " ");
                                    msLeg2b = Html.fromHtml("Alight <b>" +
                                            ((new CTime(currentConnection.miDestMin).msTm)) + "</b> at " +
                                            msDestStationName + sPlatformSide);
                                }


                                sMessage = sFromTo + " " + msLeg1a + " " +
                                        msLeg1b;
                                if (!TextUtils.isEmpty(msChangeTrain))
                                    sMessage += " " + msChangeTrain;
                                if (!TextUtils.isEmpty(msLeg2a))
                                    sMessage += " " + msLeg2a;
                                if (!TextUtils.isEmpty(msLeg2b))
                                    sMessage += " " + msLeg2b;

                                if (!TextUtils.isEmpty(sMessage)) {
                                    Intent i = new Intent(ActTrainRoute.this, ActSendSMS.class);
                                    i.putExtra("smstext", sMessage);
                                    startActivity(i);

                                } else {
                                    Toast.makeText(ActTrainRoute.this, "Please refresh the app", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(ActTrainRoute.this, "Please refresh your location", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception ex) {
                            SSLog_SS.e(TAG, "mBtnSMS.setOnClickListener " + ex);
                            Log.d(TAG, "mBtnSMS.setOnClickListener " + ex.toString());
                        }
                    }
                });

                if (mCurrentTime <= conn.miStartMin) {

                    if (!setBackground) {
                        int ROWODDCOLOR = 0xFFF0DDD5;
                        tr.setCardBackgroundColor(ROWODDCOLOR);
                        setBackground = true;
                        temp = j;
                        mStartTime = conn.miStartMin;
                    }
                }
                /*if(!setBackground)
                    tl.addView(tr);
                else{
                    if(mStartTime<conn.miStartMin)
                    {
                        Log.d(TAG,"NO TRAINS");
                    }
                    else
                    {

                    }
                }*/
                tl.addView(tr);
                j++;
            }
            final View child = tl.getChildAt(temp);
            trainhomeScrollView.post(new Runnable() {

                @Override
                public void run() {
                    if (child != null) {
                        trainhomeScrollView.smoothScrollTo(0, child.getTop());
                    }
                }
            });

        } catch (Exception e) {
            SSLog_SS.e("ActTrainRoute: ", "updateRoutes - " + e.getMessage());
            cleanUp();
        }

        if (nRowCount == 0) {
            mTvStatus.setVisibility(View.VISIBLE);
            mTvStatus.setText("Cannot find any trains\nTry changing the filter to Default");
            llRowTitle.setVisibility(View.GONE);
            return;
        }
        hideStatus();
    }

    private void setStartDest(Station oStartStation, Station oDestStation) {
        moStartStation = oStartStation;
        moDestStation = oDestStation;
        mStartView.setText(oStartStation.msSearchStr);
        mDestView.setText(oDestStation.msSearchStr);
        showRoutes(moStartStation, moDestStation, false);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.act_train_route);
        setupUI();
        try {
            if (mProgressDialog != null)
                mProgressDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void enableInput() {
        mStartView.setEnabled(true); // mStartView.setInputType(InputType.TYPE_NULL);
        mDestView.setEnabled(true); // mDestView.setInputType(InputType.TYPE_NULL);

    }

    void disableInput() {
        mStartView.setEnabled(false); // mStartView.setInputType(InputType.TYPE_NULL);
        mDestView.setEnabled(false); // mDestView.setInputType(InputType.TYPE_NULL);

    }

    /*@Override
    public void onStart() {
        super.onStart();
        //Get an Analytics tracker to report app starts & uncaught exceptions etc.
//		GoogleAnalytics.getInstance(this).reportActivityStart(this);
        init();
    }*/

    @Override
    protected void onStop() {
/*		mSensorManager.unregisterListener(mSensorListener); */
//		GoogleAnalytics.getInstance(this).reportActivityStop(this);
        try {
            writeRecentTrips();
        } catch (Exception e) {
            SSLog_SS.e("ActTrainRoute:", " onStop - " + e.getMessage());
            cleanUp();
        }
        super.onStop();
    }

    protected void startLocationUpdates() {
        MyLocation myLocation = new MyLocation(
                SSApp.mRequestQueue, ActTrainRoute.this, mGoogleApiClient);
        myLocation.getLocation(this, onLocationResult);

        if (ActivityCompat.checkSelfPermission(ActTrainRoute.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(ActTrainRoute.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (mGoogleApiClient.isConnected()) {
            PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            Log.d(TAG, "Location update started ..............: " + pendingResult.toString());
        }
    }


} // ActTrainRoute Activity
