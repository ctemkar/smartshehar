package com.smartshehar.android.app.ui;

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
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
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
import com.smartshehar.android.app.CDirection;
import com.smartshehar.android.app.CGlobals_trains;
import com.smartshehar.android.app.CTrainAtStation;
import com.smartshehar.android.app.Station;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSApp;
import com.smartshehar.dashboard.app.SSLog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.CTime;
import lib.app.util.MyLocation;
import lib.app.util.SSLog_SS;
import lib.app.util.ui.SSActivity;


public class ActFastInfo extends SSActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private final String TAG = "ActFastInfo: ";
    static final int TIME_DIALOG_ID = 0;
    private int miHour, miHourOfDay;
    private int miMin;

    private Location lastLocation = null;
    private Station moStartStation = null;
    private CGlobals_trains mApp = null;

    ArrayList<String> masRecentStations;
    ArrayList<String> masLines;
    Spinner mStartSpinner;
    ArrayAdapter<String> mStartSpinnerAdapter;
    ArrayList<String> maStation;
    ProgressDialog mProgressDialog;
    ArrayList<CTrainAtStation> maTrainsAtStation;

    Cursor stationListCursor;
    //    LinearLayout mllStartStation;
    private ImageButton mBtnMerge;
    private boolean mbTowardsMerge = false;
    Button mBtnTime, mBtnClear;
    TextView mtvDisplayTime;
    Button mBtnRecentStations;
    TextView mTvStatus;
    RelativeLayout mrlTowards;
    String msMergeId = "";
    String msTowardsStations;
    CTime moTm;
    private int miTowardsStationId = -1;

    private int REQUEST_START = 102;
    int iGroupIdx;

    // Time scrolled flag
    boolean mbAutoUpdate = true;
    private boolean mbExpandedTrains;

    private EditText mStartView;

    boolean mbICS = false;
    GoogleApiClient mGoogleApiClient = null;
    //    boolean displayTrainsTowardsFlag;
    boolean changedTimeManually;
    TextView tvGpsMsg;
    String provider;
    ScrollView svTrainsAtStations;
    LocationRequest mLocationRequest;
    private static final long INTERVAL = 1000 * 6;
    //LocationManager locationManager;
    // LocationListener locationListener;
    //	boolean mbAutoLocation = true;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle(getString(R.string.home_fastinfo));
        mApp = CGlobals_trains.getInstance();
        mApp.init(this);
        mApp.mCH.userPing(getString(R.string.pageFastInfo), "");
        mbAutoLocation = true;

        masLines = new ArrayList<>();
        maStation = new ArrayList<>();
        maStation = CGlobals_trains.mDBHelper.getStationNames();

        masRecentStations = getRecentStations();

        try {
            mProgressDialog = new ProgressDialog(ActFastInfo.this);
            mProgressDialog.setMessage("Getting Schedule");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        } catch (Exception e) {
            e.printStackTrace();
            SSLog_SS.e(TAG, "onCreate" + e.toString());
        }

        maTrainsAtStation = new ArrayList<>();

        setContentView(R.layout.actfastinfo);

        setupUI();
        mStartView.post(new Runnable() {
            @Override
            public void run() {
                mbExpandedTrains = false;
                mbAutoUpdate = true;
                if (mbAutoLocation) {
                    updateMyLocation(CGlobals_lib_ss.getInstance().getMyLocation(ActFastInfo.this));
                }
            }
        });
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(ActFastInfo.this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(ActFastInfo.this)
                .addOnConnectionFailedListener(ActFastInfo.this)
                .build();
        mGoogleApiClient.connect();

        startLocationUpdates();
        // Acquire a reference to the system Location Manager
       /*  locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
         locationListener = new LocationListener() {

            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                if (mbAutoUpdate && mbAutoLocation) {
                    mStartView.setText(location.getLatitude()+", "+location.getLongitude());
                    updateMyLocation(location);
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                SSLog_SS.i(TAG + " onStatusChanged - ", provider);
            }

            public void onProviderEnabled(String provider) {
                SSLog_SS.i(TAG + " onProviderEnabled - ", provider);
            }


            public void onProviderDisabled(String provider) {
                SSLog_SS.i(TAG + " onProviderDisabled - ", provider);
            }
        };

        // Register the listener with the Location Manager to receive location updates
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        } catch (Exception e) {
            SSLog_SS.e(TAG, "onCreate" + e.getMessage());
        }*/
// CellTowerLocation();
        setData();
    }

    @Override
    public void onResume() {
        if (mbAutoLocation) {
            setupLocationListeners();
            startLocationListeners();
            mApp.mCurrentLocation = getLocation();
        }
        keyboardShowHide(false);
        updateLocation();

        super.onResume();
    }

    private void updateLocation() {
        provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (!provider.contains("gps")) {
            tvGpsMsg.setVisibility(View.VISIBLE);
        } else {
            if (mbAutoUpdate && mbAutoLocation) {
                tvGpsMsg.setVisibility(View.GONE);
                Toast.makeText(this, "Getting Location ... ", Toast.LENGTH_SHORT).show();
                mStartView.post(new Runnable() {
                    @Override
                    public void run() {
                        mbAutoUpdate = true;
                        mbAutoLocation = true;
                        Location l = CGlobals_lib_ss.getInstance().getMyLocation(ActFastInfo.this);
                        refreshLocation(CGlobals_lib_ss.getInstance().getMyLocation(ActFastInfo.this));
                    }
                });
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    void setupUI() {
        if (mApp == null) {
            return;
        }
        svTrainsAtStations = (ScrollView) findViewById(R.id.svTrainsAtStations);
        mrlTowards = (RelativeLayout) findViewById(R.id.rlTowards);
        tvGpsMsg = (TextView) findViewById(R.id.tvGpsMsg);
        mTvStatus = (TextView) findViewById(R.id.locStatus);
        mLlRowTitle = (LinearLayout) findViewById(R.id.llRowTitle);

        mBtnMerge = (ImageButton) findViewById(R.id.btnMerge);
        mBtnMerge.setVisibility(View.VISIBLE);

        mBtnTime = (Button) findViewById(R.id.startTime);
        mtvDisplayTime = (TextView) findViewById(R.id.tvDisplayTime);
//		mtvDisplayTime.setText(currentTimeFormatted());
//		mBtnTime.setContentDescription(currentTimeFormatted());
        mBtnRecentStations = (Button) findViewById(R.id.btnRecentStations);

        mStartSpinner = (Spinner) findViewById(R.id.start_spinner);

        mStartSpinnerAdapter = new ArrayAdapter<>(this,
                R.layout.listitem, masRecentStations);
        mStartSpinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStartSpinner.setAdapter(mStartSpinnerAdapter);
        mBtnClear = (Button) findViewById(R.id.btnClear);

        stationListCursor = CGlobals_trains.mDBHelper.getStationNamesCursor("");

        mStartView = (EditText) findViewById(R.id.autocomplete_startstation);
        if (TextUtils.isEmpty(mStartView.getText().toString()))
            mbAutoUpdate = true;
        init();
//        mStartView.setAdapter(startAdapter);
        mLlRowTitle.setVisibility(View.GONE);
        mBtnMerge.setVisibility(View.VISIBLE);

        mStartView.invalidate();
        mStartView.clearFocus();

        mStartView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActFastInfo.this, SearchStationActivity.class);
                startActivityForResult(intent, REQUEST_START);
                mBtnTime.setVisibility(View.VISIBLE);
//                mStartView.setText("");
                mbAutoUpdate = false;
            }
        });


  /*      mllStartStation = (LinearLayout) findViewById(R.id.llStartStation);
        mllStartStation.setVisibility(LinearLayout.VISIBLE);*/

        mBtnRecentStations.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setStartStation(mStartView.getText().toString());
        // set current time
        mBtnTime.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onClick(View arg0) {
                mApp.mCH.userPing(getString(R.string.atChangeTime), "");
                keyboardShowHide(false);
                showDialog(TIME_DIALOG_ID);
            }
        });

        mBtnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mBtnTime.setVisibility(View.VISIBLE);
                mStartView.setText("");
                mbAutoUpdate = false;
            }
        });

        mBtnMerge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mbTowardsMerge = !mbTowardsMerge;
                if (mbTowardsMerge) {
                    mBtnMerge.setBackgroundResource(R.mipmap.tabduplicate32);
                    mApp.mCH.userPing(getString(R.string.atMerge), "");


                } else {
                    mBtnMerge.setBackgroundResource(R.mipmap.tabmerge32);
                    mApp.mCH.userPing(getString(R.string.atUnMerge), "");

                }
                setStartStation(mStartView.getText().toString());
            }
        });
        mBtnRecentStations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
//                mStartView.setThreshold(1000);
                mbAutoUpdate = false;
                if (masRecentStations.size() > 0) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            ActFastInfo.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            masRecentStations);
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            ActFastInfo.this);
                    builder.setTitle(R.string.selectRecentStation)
                            .setAdapter(
                                    adapter,
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                final int which) {
                                            mBtnRecentStations
                                                    .post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            setStartStation(masRecentStations
                                                                    .get(which));
                                                        }
                                                    });

                                            dialog.dismiss();
                                        }
                                    }).create().show();
                }
            }
        });

        tvGpsMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!provider.contains("gps")) { //if gps is disabled
                    try {
                        CGlobals_trains.getInstance().turnGPSOn(ActFastInfo.this, mGoogleApiClient);
                    } catch (Exception e) {
                        Log.d(TAG, "tvGpsMsg onClick " + e.toString());
                        e.printStackTrace();
                    }

                }
            }
        });
    } // setupUI ends


    private void refreshStartStation() {
        TableLayout tl = (TableLayout) findViewById(R.id.tblTrainsAtStation);
        tl.removeAllViews();
        mTvStatus.setVisibility(View.VISIBLE);
        mTvStatus.setText(R.string.waiting_location);
//        mStartView.dismissDropDown();
        mbExpandedTrains = false;
        setStartStation(mStartView.getText().toString());

    }


    private LinearLayout mLlRowTitle;

    void refreshLocation(Location location) {
        // Now get the places at this location, if this fragment is still bound
        // to an activity
        try {
            if (location == null)
                return;
            if (mbExpandedTrains)
                return;
            ActFastInfo.this.lastLocation = location;
            TableLayout tl = (TableLayout) findViewById(R.id.tblTrainsAtStation);
            tl.post(new Runnable() {
                @Override
                public void run() {
                    moStartStation = CGlobals_trains.mDBHelper.getNearestStation(
                            ActFastInfo.this.lastLocation.getLatitude(),
                            ActFastInfo.this.lastLocation.getLongitude(),
                            ActFastInfo.this);
                    if (moStartStation == null)
                        return;

                    if (mbAutoUpdate && mbAutoLocation) {

                        mStartView.setText(moStartStation.msSearchStr);
                        mStartView.setContentDescription(moStartStation.msSearchStr);
                        if (!mbExpandedTrains)
                            showDestStations(moStartStation, mbTowardsMerge);
                    }


                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            SSLog.e(TAG, "refreshLocation ", e.toString());
        }
    }

    // Show the Lines at this station (Lines at start)
    private void showDestStations(final Station oStation, boolean mbTowardsMerge) {
        try {
            if (oStation.msStationName == null)
                return;
            mTvStatus.setVisibility(View.GONE);
            mrlTowards.setVisibility(View.VISIBLE);
            mBtnMerge.setVisibility(View.VISIBLE);
            mLlRowTitle.setVisibility(View.GONE);

            LayoutInflater inflater = getLayoutInflater();

            TableLayout tl = (TableLayout) findViewById(R.id.tblTrainsAtStation);

            tl.removeAllViews();
            ArrayList<CDirection> aTowards;
            aTowards = CGlobals_trains.mDBHelper.getTrainDestinations(oStation, mbTowardsMerge);
            mTvStatus.setVisibility(View.GONE);
            if (aTowards == null) {
                mTvStatus.setVisibility(View.VISIBLE);
                mTvStatus.setText("Cannot find any trains from here");
                return;
            }

            for (final CDirection d : aTowards) {
                LinearLayout trd = (LinearLayout) inflater.inflate(
                        R.layout.towardstab, tl, false);
                TextView tvDir = (TextView) trd.findViewById(R.id.direction);
                final Button btnPlus = (Button) trd.findViewById(R.id.plus);
///			final Button btnMinus = (Button) trd.findViewById(R.id.minus);
                LinearLayout llDirectionRow = (LinearLayout) trd
                        .findViewById(R.id.llDirectionRow);

                tvDir.setText(d.msStation);
                tvDir.setContentDescription(d.msStation);
                msTowardsStations = d.msStation;
                if (oStation != null) {
                    miTowardsStationId = d.miTowardsStationId;
                    msMergeId = d.msMergeId;
                    miTowardsStationId = d.miTowardsStationId;
                }
                mStartView = (EditText) findViewById(R.id.autocomplete_startstation);
                View.OnClickListener filterTrains = new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        try {
//                            displayTrainsTowardsFlag = true;
                            mbExpandedTrains = true;
                            // String selTrain = "Expanding trains to " + d.msStation;
                            // Toast.makeText(getApplicationContext(), selTrain,
                            // Toast.LENGTH_SHORT).show();
                            btnPlus.setVisibility(LinearLayout.GONE);
//					btnMinus.setVisibility(LinearLayout.VISIBLE);
                            btnPlus.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (oStation != null) {
                                            miTowardsStationId = d.miTowardsStationId;
                                            msMergeId = d.msMergeId;
                                            miTowardsStationId = d.miTowardsStationId;
                                            mStartView = (EditText) findViewById(R.id.autocomplete_startstation);
                                            String station = mStartView.getText().toString();
                                            if (!TextUtils.isEmpty(station) && !TextUtils.isEmpty(oStation.msStationAbbr)) {
//                                                addRecentStation(station);
                                                mApp.mCH.userPing(getString(R.string.pageFastInfo), getString(R.string.atTIPlus) + " : " + oStation.msStationAbbr);
                                                showTrainsTowards(oStation, d.msStation, miTowardsStationId);
                                            } else {
                                                Toast.makeText(ActFastInfo.this, "Please refresh your location", Toast.LENGTH_LONG).show();
                                            }
                                        } else {
                                            Toast.makeText(ActFastInfo.this, "Please refresh your location", Toast.LENGTH_LONG).show();
                                        }
                                    } catch (Exception ex) {
                                        if (oStation != null) {

                                            String station = mStartView.getText().toString();
                                            if (!TextUtils.isEmpty(station) && !TextUtils.isEmpty(oStation.msStationAbbr)) {
//                                                addRecentStation(station);
                                                mApp.mCH.userPing(getString(R.string.pageFastInfo), getString(R.string.atTIPlus) + " : " + oStation.msStationAbbr);
                                                showTrainsTowards(oStation, d.msStation, miTowardsStationId);
                                            } else {
                                                Toast.makeText(ActFastInfo.this, "Please refresh your location", Toast.LENGTH_LONG).show();
                                            }
                                        } else {
                                            Toast.makeText(ActFastInfo.this, "Please refresh your location", Toast.LENGTH_LONG).show();
                                        }
                                        Log.d(TAG, "btnPlus.post" + ex.toString());
                                        SSLog_SS.e(TAG, "btnPlus.post" + ex);
                                    }

                                }
                            });
                        } catch (Exception e) {
                            Log.d(TAG, "View.OnClickListener filterTrains " + e.toString());
                            e.printStackTrace();
                        }
                    }
                };
                llDirectionRow.setOnClickListener(filterTrains);
                btnPlus.setOnClickListener(filterTrains);
                tl.addView(trd);
            }
        } catch (Exception e) {
            Log.d(TAG, "showDestStations " + e.toString());
            e.printStackTrace();
        }
    }

    // Show the trains at this station
    private void showTrainsTowards(Station moStart, String sTowardsStations, int iTowardsStationId) {

        if (moStart.msStationName == null)
            return;
        mTvStatus.setText("Updating trains .. ");
        mTvStatus.setContentDescription("Updating trains .. ");
        moStartStation = moStart;
        msTowardsStations = sTowardsStations;
        miTowardsStationId = iTowardsStationId;
        moTm = new CTime(miHourOfDay, miMin);
        new GetRoutesTask().execute("");


    } // showTrainsAtStation

    void displayTrainsTowards(ArrayList<CTrainAtStation> aTrainsAtStation) {
        int temp = 0;
        boolean setBackground = false;
        int mCurrentTime;
        mCurrentTime = miHourOfDay * 60 + miMin;

        TableLayout tl = (TableLayout) findViewById(R.id.tblTrainsAtStation);
        tl.removeAllViews();
        svTrainsAtStations.smoothScrollTo(0, 0);

        if (aTrainsAtStation == null) {
            mTvStatus.setText("No trains found at this time");
            mTvStatus.setVisibility(View.VISIBLE);
            return;
        }
        mLlRowTitle.setVisibility(View.VISIBLE);
        LayoutInflater inflater = getLayoutInflater();

        if (aTrainsAtStation.size() == 0) {
            Toast.makeText(ActFastInfo.this,
                    "Did not find any trains",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        iGroupIdx = -1;
        LinearLayout trd = (LinearLayout) inflater.inflate(
                R.layout.towardstab, tl, false);
        TextView tvDir = (TextView) trd.findViewById(R.id.direction);
        final Button btnPlus = (Button) trd.findViewById(R.id.plus);
        final Button btnMinus = (Button) trd.findViewById(R.id.minus);
        // LinearLayout llDirectionRow = (LinearLayout)
        // trd.findViewById(R.id.llDirectionRow);
        btnMinus.setVisibility(LinearLayout.GONE);
        btnPlus.setVisibility(LinearLayout.GONE);
        btnPlus.setText("BACK");
        btnPlus.setContentDescription("BACK");
        int ICSBUTTONCOLOR = 0xFFeee9e9;
        if (mbICS)
            btnPlus.getBackground().setColorFilter(ICSBUTTONCOLOR, PorterDuff.Mode.MULTIPLY);


        mBtnMerge.setVisibility(View.GONE);
        mrlTowards.setVisibility(View.GONE);
        tvDir.setText("Towards: " + msTowardsStations);
        tvDir.setContentDescription("Towards: " + msTowardsStations);
        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
/*
                String selTrain = "Showing all trains ...";
				Toast.makeText(getApplicationContext(), selTrain,
						Toast.LENGTH_SHORT).show();
*/
                btnPlus.setVisibility(LinearLayout.GONE);
                btnMinus.setVisibility(LinearLayout.VISIBLE);
                btnPlus.post(new Runnable() {
                    @Override
                    public void run() {
                        mbExpandedTrains = true;
                        mLlRowTitle.setVisibility(View.GONE);
                        mBtnMerge.setVisibility(View.VISIBLE);
                        showDestStations(moStartStation, mbTowardsMerge);
                    }
                });
            }
        });

        tl.addView(trd);
        int i = 1;
        int j = 0;
        for (final CTrainAtStation cb : aTrainsAtStation) {
            if (cb == null)
                continue;
            int MAXTRAINSTOSHOW = 40;
            if (i > MAXTRAINSTOSHOW && cb.miDiff > 70)
                break;
            Calendar c = Calendar.getInstance();
            int iDow = c.get(Calendar.DAY_OF_WEEK);
            boolean bSunday = iDow == Calendar.SUNDAY ? true : false;
            if ((cb.msSpl.equals("$") && !bSunday)
                    || (cb.msSpl.equals("LS") && bSunday))
                continue;
            if (cb.mbSundayOnly && !bSunday)
                continue;
            if (cb.mbNotOnSunday && bSunday)
                continue;

            // Create a new row to be added for each bus at station
            CardView tr = (CardView) inflater.inflate(
                    R.layout.traintowardsrow, tl, false);
            tr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String selTrain = "Selected Train - From: " + cb.msFirstStation
                            + " To " + cb.msLastStation;
                    Toast.makeText(getApplicationContext(), selTrain,
                            Toast.LENGTH_SHORT).show();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            mApp.mCH.userPing(getString(R.string.pageFastInfo),
                                    getString(R.string.atTITrip) + " : " + cb.miTrainId + " - " + miTowardsStationId);
                        }
                    }, 200);


                    Intent intent = new Intent(ActFastInfo.this,
                            ActTrainTrip.class);
                    intent.putExtra("trainId1", cb.miTrainId);
                    intent.putExtra("lineCode1", cb.msLine);
                    intent.putExtra("directOnly", true);
                    intent.putExtra("startStationId", -1);
                    intent.putExtra("destStationId", -1);

                    intent.putExtra("towardsStationId", miTowardsStationId);

                    intent.putExtra("towardsStations", msTowardsStations);
                    intent.putExtra("hr", miHour);
                    intent.putExtra("towardsMerge", mbTowardsMerge);
                    Calendar c = Calendar.getInstance();
                    intent.putExtra("min", c.get(Calendar.MINUTE));

                    startActivity(intent);
                }
            });
            TextView tvSpeed = (TextView) tr.findViewById(R.id.indicatorspeedcode);
            // tvSpeed.setTypeface(tf);
            TextView tvTrainTime = (TextView) tr.findViewById(R.id.traintime);
            if (mbICS)
                tvTrainTime.getBackground().setColorFilter(0xFFeee9e9, PorterDuff.Mode.MULTIPLY);
            TextView tvTrainStart = (TextView) tr.findViewById(R.id.firststation);
            TextView tvLine = (TextView) tr.findViewById(R.id.linecode);
            TextView tvPlatform = (TextView) tr.findViewById(R.id.platform);
            TextView tvCar = (TextView) tr.findViewById(R.id.car);
            ImageView tvSplCode = (ImageView) tr.findViewById(R.id.splcode);


            tvLine.setText(cb.msLine);
            tvLine.setContentDescription(cb.msLine);
//			tvTrainName.setText(cb.msTrainName);
//			tvTrainName.setContentDescription(cb.msTrainName);
            if (!TextUtils.isEmpty(cb.msColour))
                tvLine.setTextColor(Color.parseColor(cb.msColour));
            tvTrainTime.setText(cb.moTrainTime.msTm);
            tvTrainTime.setContentDescription(cb.moTrainTime.msTm);

            //		dec.format(cb.dTrainTime));
            tvTrainStart.setText(cb.msFirstStation + " - " + cb.msLastStation + "   " + cb.msTrainName);
            tvTrainStart.setContentDescription(cb.msFirstStation + " - " + cb.msLastStation);
//			tvTrainEnd.setText(cb.msLastStation);
            if (cb.miCar > 0) {
                tvCar.setText(" " + cb.miCar);
                tvCar.setContentDescription("Cars " + cb.miCar);
            }
            /////////////ladies special
            if (!TextUtils.isEmpty(cb.msSpl)) {
                if (cb.msSpl.equals("$")) {
//                    tvSplCode.setText("Sun");
//                    tvSplCode.setTextColor(Color.RED);
                    Log.d(TAG, "$");
                } else {
//                    tvSplCode.setText(cb.msSpl);
//                    tvSplCode.setTextColor(Color.BLUE);
                    tvSplCode.setVisibility(View.VISIBLE);
                }
            }
            String sp = cb.msSpeedAbbr.toUpperCase(Locale.UK);

            tvSpeed.setText(sp);
            tvSpeed.setTextColor(Color.parseColor("#228b22"));
            if (sp.equals(CGlobals_trains.FASTABBR) || sp.equals(CGlobals_trains.DOUBLEFASTABBR))
                tvSpeed.setTextColor(Color.RED);

            tvPlatform.setText(cb.msPlatform);

            if (mCurrentTime <= aTrainsAtStation.get(j).miMins) {

                if (!setBackground) {
                    int ROWODDCOLOR = 0xFFF0DDD5;
                    tr.setCardBackgroundColor(ROWODDCOLOR);
                    setBackground = true;
                    temp = j + 1;


                }
            }
            tl.addView(tr);
            i++;
            j++;
        } // for
        final View child = tl.getChildAt(temp);
        svTrainsAtStations.post(new Runnable() {

            @Override
            public void run() {
                if (child != null) {
                    svTrainsAtStations.smoothScrollTo(0, child.getTop());
                }
            }
        });


    } // displayTrainsTowards


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menufastinfo, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //			Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, ActTrainDashboard.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            ActFastInfo.this.finish();
            return true;
        } else if (item.getItemId() == R.id.menu_refresh) {
            Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show();
            setData();
        } else if (item.getItemId() == R.id.menu_locate) {

            CGlobals_trains.getInstance().turnGPSOn(ActFastInfo.this, mGoogleApiClient);

            mStartView.post(new Runnable() {
                @Override
                public void run() {
                    mbExpandedTrains = false;
                    mbAutoUpdate = true;
                    mbAutoLocation = true;
                    refreshLocation(CGlobals_lib_ss.getInstance().getMyLocation(ActFastInfo.this));
                }
            });

        }

        return super.onOptionsItemSelected(item);
    }

    private void setData() {
        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {

                mBtnTime.setVisibility(View.VISIBLE);
                if (!changedTimeManually) {
                    resetToCurrentTime();
                }
                if (mbExpandedTrains)//(displayTrainsTowardsFlag)
                {
                    new GetRoutesTask().execute("");

                } else {
                    setupUI();
                    refreshStartStation();
                    setProgressBarIndeterminateVisibility(Boolean.FALSE);
                }
            }
        }, 500);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        if (requestCode == REQUEST_START && resultCode == Activity.RESULT_OK) {

            if (data != null) {
                startLocationUpdates();
                Bundle extra = data.getBundleExtra("data");
//                byte[] ba = extra.getByteArray("originalByte");
                String station = extra.getString("station");

                setStartStation(station);
                mbExpandedTrains = false;
            }
        }
        if (requestCode == CGlobals_trains.REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    Log.d(TAG, "YES");
                    updateLocation();

                    mStartView.post(new Runnable() {
                        @Override
                        public void run() {
                            mbExpandedTrains = false;
                            mbAutoUpdate = true;
                            mbAutoLocation = true;
                            refreshLocation(CGlobals_lib_ss.getInstance().getMyLocation(ActFastInfo.this));
                            tvGpsMsg.setVisibility(View.GONE);

                        }
                    });
                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    Log.d(TAG, "NO");
                    CGlobals_trains.showGPSDialog = false;
                    tvGpsMsg.setVisibility(View.VISIBLE);


                    break;
                default:
                    break;
            }
        }

    }


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
            if (!TextUtils.isEmpty(sRecentStations) & sRecentStations.trim().length() > 0) {
                String as[] = sRecentStations.split(";");
                int iLen = as.length;
                for (int i = 0; i < iLen; i++) {
                    s = as[i];
                    String SELECTRECENT = "Select Station";
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

    @Override
    public void finish() {
        if (mApp == null)
            SSLog_SS.e("TrainApp: ", "TrainHome - " + "mApp is Null");
        else if (mApp.stackActivity == null)
            SSLog_SS.e("TrainApp: ", "TrainHome - " + "stackActivity is Null");
        else {
//            feRecentStations();
            setResult(RESULT_CANCELED);
            super.finish();
        }
    }


    void setStartStation(String sStartStationFull) {
        Station retStn = CGlobals_trains.mDBHelper
                .getStationFromSearchStr(sStartStationFull);
        if (retStn != null) {
            moStartStation = retStn;

//            mStartView.setThreshold(1000);
            mStartView.setText(moStartStation.msSearchStr);
            showDestStations(moStartStation, mbTowardsMerge);
//            addRecentStation(moStartStation.msSearchStr);
            mStartView.setText(sStartStationFull);
//			mStartView.setThreshold(0);
            mTvStatus.setVisibility(View.GONE);
//            mStartView.dismissDropDown();

//			keyboardShowHide(true);

        }
    }

    @Override
    public void onBackPressed() {
        if (mbExpandedTrains) {
            showDestStations(moStartStation, mbTowardsMerge);
            mbExpandedTrains = false;
//            displayTrainsTowardsFlag = false;
        } else
            super.onBackPressed();
    }

    void keyboardShowHide(boolean bShow) {
        if (bShow) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        } else {
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    INPUT_METHOD_SERVICE);
            View cFocus = getCurrentFocus();
            if (imm != null && cFocus != null)
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            else
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            mBtnRecentStations.requestFocus();


//		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
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
            SSLog_SS.e(TAG, "onDestroy" + e);
        }

    }

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

    @Override
    public void onLocationChanged(Location location) {
        if (mbAutoUpdate) {
            CGlobals_lib_ss.setMyLocation(location, false, ActFastInfo.this);
            updateMyLocation(location);
        }
    }


    private class GetRoutesTask extends AsyncTask<String, Integer, String> {
        ArrayList<CTrainAtStation> aTrainsAtStation;


        @Override
        protected String doInBackground(String... sUrl) {
            try {
                aTrainsAtStation = new ArrayList<>();
                aTrainsAtStation = CGlobals_trains.mDBHelper.getTrainsTowards(moStartStation, msTowardsStations,
                        msMergeId, mbTowardsMerge, miTowardsStationId, moTm);
                mProgressDialog.setTitle("Trains from " + moStartStation.msSearchStr +
                        " towards " + msTowardsStations);

            } catch (Exception e) {
                SSLog_SS.e(TAG, "doInBackground: " + e);
            }
            return "Success";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                if (!isFinishing())
                    mProgressDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
                SSLog_SS.e(TAG, "onPreExecute" + e);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            try {
                mProgressDialog.setProgress(progress[0]);
            } catch (Exception e) {
                e.printStackTrace();
                SSLog_SS.e(TAG, "onProgressUpdate" + e);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {

                displayTrainsTowards(aTrainsAtStation);
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
            } catch (Exception e) {
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
                SSLog_SS.e(TAG, "PostExecute - " + e.getMessage());
            }
            super.onPostExecute(result);
        }

    }

    public void updateMyLocation(Location loc) {
        mApp.mCurrentLocation = loc;

        if (loc == null)
            loc  = CGlobals_lib_ss.getInstance().getMyLocation(ActFastInfo.this);
        if (mbAutoLocation)
            refreshLocation(loc);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case TIME_DIALOG_ID:
//		    	disableInput();
                return new TimePickerDialog(this,
                        mTimeSetListener, miHourOfDay, miMin, false);
        }
        return null;
    }

    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//			        	enableInput();
                    miHourOfDay = hourOfDay;
                    miMin = minute;
                    changedTimeManually = true;
                    updateTimeDisplay();
                    if (mbExpandedTrains)
                        showTrainsTowards(moStartStation, msTowardsStations, miTowardsStationId);
                    keyboardShowHide(false);
                }
            };

    private void updateTimeDisplay() {
        String sAmPm = miHourOfDay >= 12 ? " pm " : " am ";
        miHour = miHourOfDay >= 13 ? miHourOfDay - 12 : miHourOfDay;
        miHour = miHour == 0 ? 12 : miHour;

        mtvDisplayTime.setText(
                new StringBuilder()
                        .append(miHour).append(":")
                        .append(pad(miMin)).append(" ")
                        .append(sAmPm));
/*	        if(mbExpandedTrains)
                showTrainsTowards(moStartStation,  msTowardsStations, msMergeId,
					miTowardsStationId, miHourOfDay, miMin);
*/
        keyboardShowHide(false);
    }

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }

    void resetToCurrentTime() {
        Calendar c = Calendar.getInstance();
        miHourOfDay = c.get(Calendar.HOUR_OF_DAY);
        miMin = c.get(Calendar.MINUTE);
        updateTimeDisplay();
    }

    public void init() {

        if (!changedTimeManually) {
            Calendar c = Calendar.getInstance();
            miHourOfDay = c.get(Calendar.HOUR_OF_DAY);
            miHour = c.get(Calendar.HOUR);
            miMin = c.get(Calendar.MINUTE);
            updateTimeDisplay();
        }
        mbAutoUpdate = true;

    }

    protected void startLocationUpdates() {
        MyLocation myLocation = new MyLocation(
                SSApp.mRequestQueue, ActFastInfo.this, mGoogleApiClient);
        myLocation.getLocation(this, onLocationResult);

        if (ActivityCompat.checkSelfPermission(ActFastInfo.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(ActFastInfo.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (mGoogleApiClient.isConnected()) {
            PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            Log.d(TAG, "Location update started ..............: " + pendingResult.toString());
        }
    }

    private MyLocation.LocationResult onLocationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {

            try {
                CGlobals_lib_ss.setMyLocation(location, false, ActFastInfo.this);
                updateMyLocation(location);
                //geoHelper.getAddress(ActFastInfo.this, location, onGeoHelperResult);
               /* showNearTrainStation(location);
                showNearBusStation(location);
                showFare();*/

            } catch (Exception e) {
                e.printStackTrace();
                SSLog_SS.e(TAG, "LocationResult", e, ActFastInfo.this);
            }
        }
    };
} // TrainHome Activity