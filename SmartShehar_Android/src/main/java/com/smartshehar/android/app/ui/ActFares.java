package com.smartshehar.android.app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
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
import com.smartshehar.android.app.DBHelperTrain.Fare;
import com.smartshehar.android.app.Station;
import com.smartshehar.android.app.TrainFaresAdapter;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSApp;

import java.util.ArrayList;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.MyLocation;
import lib.app.util.SSLog_SS;


public class ActFares extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private final int LINE = 1;
    private final int STATION = 2;
    private int REQUEST_DEST = 101;
    private int REQUEST_START = 102;
    private Location lastLocation = null;
    private Station moStartStation = null, moDestStation = null;
    private CGlobals_trains mApp = null;

    ArrayList<String> masRecentStations;
    ArrayList<String> masLines;
    Spinner mStartSpinner;
    ArrayAdapter<String> mStartSpinnerAdapter;
    ArrayList<String> maStation;
    ProgressDialog mProgressDialog;

    Cursor mStationListCursor, mStationListDestCursor;
    TrainFaresAdapter mFareAdapter;
    //  LinearLayout mllStartStation;
    Button mBtnClearFrom, mBtnClearTo;
    Button mBtnRecentStations;
    TextView txtGpsOffMsg;
    String provider;
    boolean mbAutoUpdate = true;
    private boolean mbExpandedTrains;
    private EditText mStartView, mDestView;
    boolean isSortByLine = true;
    private static String TAG = "ActFares:---- ";
    GoogleApiClient mGoogleApiClient = null;
    protected static boolean mbAutoLocation;
    LocationRequest mLocationRequest;
    private static final long INTERVAL = 1000 * 6;

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
        ab.setTitle("Fares");

        mApp = CGlobals_trains.getInstance();
        mApp.init(this);
        mApp.mCH.userPing(getString(R.string.pageFare), "");
        Bundle extras = getIntent().getExtras();
        masLines = new ArrayList<>();
        maStation = new ArrayList<>();
        maStation = CGlobals_trains.mDBHelper.getStationNames();

        masRecentStations = getRecentStations();
        try {
            mProgressDialog = new ProgressDialog(ActFares.this);
            mProgressDialog.setMessage("Getting Schedule");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        } catch (Exception e) {
            e.printStackTrace();
            SSLog_SS.e(TAG, "onCreate" + e);
        }


        setContentView(R.layout.act_trainfare);

        setupUI();
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(ActFares.this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(ActFares.this)
                .addOnConnectionFailedListener(ActFares.this)
                .build();
        mGoogleApiClient.connect();
        startLocationUpdates();
        mbAutoLocation = true;/*settings.getBoolean("checkBoxAutoLocation", true);*/
        if (extras != null) {
            int iStartStationId = extras.getInt("startstationid");
            int iDestStationId = extras.getInt("deststationid");
            if (iStartStationId > 0) {
                moStartStation = CGlobals_trains.mDBHelper.getStationFromId(iStartStationId);
            }
            if (iDestStationId > 0) {
                moDestStation = CGlobals_trains.mDBHelper.getStationFromId(iDestStationId);
            }
            if (moStartStation != null && moDestStation != null) {
                mbAutoUpdate = false;
                mStartView.setText(moStartStation.msSearchStr);

                mDestView.setText(moDestStation.msSearchStr);
                findViewById(R.id.farelist).setVisibility(View.GONE);
                findViewById(R.id.llHeader).setVisibility(View.GONE);
                findViewById(R.id.tlFare).setVisibility(View.VISIBLE);
                showFare(iStartStationId, iDestStationId, LINE);
            } else {
                mStartView.post(new Runnable() {
                    @Override
                    public void run() {
                        mbAutoUpdate = true;
//                        refreshLocation1(mApp.getMyLocation(ActFares.this));
                    }
                });
            }
        }

    }


    @Override
    public void onStart() {
        super.onStart();
//      GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
//      GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    public void onResume() {
        keyboardShowHide(false);
        turnOnGps();
        super.onResume();
    }

    private void turnOnGps() {
        provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (!provider.contains("gps")) {
            txtGpsOffMsg.setVisibility(View.VISIBLE);
        } else {
            txtGpsOffMsg.setVisibility(View.GONE);

        }
        if (mbAutoUpdate)
            refreshStartStation();

    }

    void setupUI() {


        txtGpsOffMsg = (TextView) findViewById(R.id.txtGpsOffMsg);
//        mrlTowards = (RelativeLayout) findViewById(R.id.rlTowards);
        mBtnRecentStations = (Button) findViewById(R.id.btnRecentStations);
        mStartSpinner = (Spinner) findViewById(R.id.start_spinner);
//		mDestSpinner = (Spinner) findViewById(R.id.dest_spinner);
        mStartSpinnerAdapter = new ArrayAdapter<>(this,
                R.layout.listitem, masRecentStations);
        mStartSpinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStartSpinner.setAdapter(mStartSpinnerAdapter);
        mBtnClearFrom = (Button) findViewById(R.id.btnClear);
        mBtnClearTo = (Button) findViewById(R.id.btnClearDest);
        mStationListCursor = CGlobals_trains.mDBHelper.getStationNamesCursor("");
        mStationListDestCursor = CGlobals_trains.mDBHelper.getStationNamesCursor("");

        mStartView = (EditText) findViewById(R.id.autocomplete_startstation);
        mDestView = (EditText) findViewById(R.id.autocomplete_deststation);

//        mStartView.setAdapter(startAdapter);
        mStartView.invalidate();
        mStartView.clearFocus();
//        mDestView.setAdapter(destAdapter);
        mDestView.invalidate();
        mDestView.clearFocus();
        txtGpsOffMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!provider.contains("gps")) { //if gps is disabled
                    try {
                        CGlobals_trains.getInstance().turnGPSOn(ActFares.this, mGoogleApiClient);
                    } catch (Exception e) {
                        Log.d(TAG, "tvGpsMsg onClick " + e.toString());
                        e.printStackTrace();
                    }

                }
            }
        });
        mStartView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mbAutoUpdate = false;
                Intent intent = new Intent(ActFares.this, SearchStationActivity.class);
                startActivityForResult(intent, REQUEST_START);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


            }
        });

        mDestView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActFares.this, SearchStationActivity.class);
                startActivityForResult(intent, REQUEST_DEST);

                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                mbAutoUpdate = false;
            }
        });

        mBtnClearFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mStartView.setText("");
                mbAutoUpdate = false;
            }
        });

        mBtnClearTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mDestView.setText("");
                findViewById(R.id.farelist).setVisibility(View.VISIBLE);
                findViewById(R.id.llHeader).setVisibility(View.VISIBLE);
                findViewById(R.id.tlFare).setVisibility(View.GONE);
                mbAutoUpdate = false;
            }
        });


       /* mllStartStation = (LinearLayout) findViewById(R.id.llStartStation);
        mllStartStation.setVisibility(LinearLayout.VISIBLE);*/

        mBtnRecentStations.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    } // setupUI ends

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Bundle extra = data.getBundleExtra("data");
                String station = extra.getString("station");
                setDestStation(station);
            }

        }
        if (requestCode == REQUEST_START && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                mbAutoUpdate = false;
                Bundle extra = data.getBundleExtra("data");
//                byte[] ba = extra.getByteArray("originalByte");
                String station = extra.getString("station");
                setStartStation(station);
            }

        }
        if (requestCode == CGlobals_trains.REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    Log.d(TAG, "YES");
                    startLocationUpdates();
                /*    turnOnGps();*/
                    mStartView.post(new Runnable() {
                        @Override
                        public void run() {
                            mbAutoUpdate = true;
                            //  refreshLocation1(mApp.getMyLocation(ActFares.this));
                            txtGpsOffMsg.setVisibility(View.GONE);

                        }
                    });
                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    Log.d(TAG, "NO");
                    CGlobals_trains.showGPSDialog = false;
                    txtGpsOffMsg.setVisibility(View.VISIBLE);


                    break;
                default:
                    break;
            }
        }
    }

    private void refreshStartStation() {
        mbExpandedTrains = false;

        mApp.mCurrentLocation = CGlobals_lib_ss.getInstance().getMyLocation(ActFares.this);
        if (mApp.mCurrentLocation != null)
            refreshLocation(mApp.mCurrentLocation);
        else {
            if (new MyLocation().getLocation(ActFares.this, onLocationResult)) {
                Log.d(TAG,
                        "Location available");
            } else {
                Toast.makeText(ActFares.this,
                        "Location not available - turn on GPS", Toast.LENGTH_LONG)
                        .show();

            }
        }
    }

    private MyLocation.LocationResult onLocationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            if (location == null) {
                if (mApp.mCurrentLocation != null && mApp != null) {
                    location = mApp.mCurrentLocation;
                } else {
                    // Location not found, show default or recent station

                    location = CGlobals_lib_ss.getInstance().getMyLocation(ActFares.this);
                }
            }
            CGlobals_lib_ss.setMyLocation(location, false, ActFares.this);
            refreshLocation(location);
            mApp.mCurrentLocation = location;
        }
    };

    void refreshLocation(Location location) {
        // Now get the places at this location, if this fragment is still bound
        // to an activity

        if (location == null)
            location = CGlobals_lib_ss.getInstance().getMyLocation(ActFares.this);
        if(location == null)
            return;
        try {
            ActFares.this.lastLocation = location;
            final Location finalLocation = location;
            mBtnClearFrom.post(new Runnable() {
                @Override
                public void run() {
                    moStartStation = CGlobals_trains.mDBHelper.getNearestStation(
                            finalLocation.getLatitude(),
                            finalLocation.getLongitude(),
                            ActFares.this);
                    if (moStartStation == null)
                        return;
                    if (moDestStation != null)
                        showFare(moStartStation.miStationId, moDestStation.miStationId, isSortByLine ? LINE : STATION);
                    else {
                        showFares(moStartStation, isSortByLine ? LINE : STATION);
                    }

                    TextView tvStartStation = (TextView) findViewById(R.id.startStation);
                    tvStartStation.setText(moStartStation.msSearchStr);
                    tvStartStation.setContentDescription(moStartStation.msSearchStr);
                    EditText startView = (EditText) findViewById(R.id.autocomplete_startstation);
                    if (mbAutoUpdate && mbAutoLocation)
                        startView.setText(moStartStation.msSearchStr);
                    startView.setContentDescription(moStartStation.msSearchStr);
                    // set current time

                    mBtnRecentStations.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
//                        mStartView.setThreshold(1000);
                            if (masRecentStations.size() > 0) {
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                        ActFares.this,
                                        android.R.layout.simple_spinner_dropdown_item,
                                        masRecentStations);
                                AlertDialog.Builder builder = new AlertDialog.Builder(
                                        ActFares.this);
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
                }
            });

        } catch (Exception e) {
            Toast.makeText(ActFares.this, "Start your GPS or Setup Network location to get your nearest station", Toast.LENGTH_LONG).show();
            Log.d(TAG, "refreshLocation " + e.toString());
        }


    }

    private void showFare(int iStartStationId, int iDestStationId, int sortOrder) {
        if (mApp == null || CGlobals_trains.mDBHelper == null) {
            assert mApp != null;
            mApp.init(this);
        }
        ArrayList<Fare> aoFare = CGlobals_trains.mDBHelper.getFare(iStartStationId, iDestStationId, sortOrder);
        TextView tv;
        RelativeLayout rl;
        LayoutInflater inflater = getLayoutInflater();
        TableLayout tl = (TableLayout) findViewById(R.id.tlFare);
        tl.removeAllViews();
        for (final Fare fare : aoFare) {
            LinearLayout tr = (LinearLayout) inflater.inflate(
                    R.layout.farerow_vertical, tl, false);
            if (fare.msLineCode.equals("MET") || fare.msLineCode.equals("MON")) {
                tv = (TextView) tr.findViewById(R.id.lblSecond);
                tv.setText("Token");
                tv = (TextView) tr.findViewById(R.id.lblFirst);
                tv.setText("Smart card");
            }
            TextView tvFare = (TextView) tr.findViewById(R.id.tvFare);
            tvFare.setText(fare.miFare > 0 ? Integer.toString(fare.miFare) : "-");
            TextView tvFare1st = (TextView) tr.findViewById(R.id.tvFare1st);
            tvFare1st.setText(fare.miFare1st > 0 ? Integer.toString(fare.miFare1st) : "-");
            if (fare.miFarePass1m > 0 || fare.miFarePass1m1st > 0) {
                tv = (TextView) tr.findViewById(R.id.tv1mLabel);
                tv.setVisibility(View.VISIBLE);
                tv = (TextView) tr.findViewById(R.id.tvFarePass1m);
                tv.setText(fare.miFarePass1m > 0 ? Integer.toString(fare.miFarePass1m) : "-");
                tv.setVisibility(View.VISIBLE);
                tv = (TextView) tr.findViewById(R.id.tvFarePass1m1st);
                tv.setText(fare.miFarePass1m1st > 0 ? Integer.toString(fare.miFarePass1m1st) : "-");
                tv.setVisibility(View.VISIBLE);
            }
            if (fare.miFarePass3m > 0 || fare.miFarePass3m1st > 0) {
                tv = (TextView) tr.findViewById(R.id.tv3mLabel);
                tv.setVisibility(View.VISIBLE);
                tv = (TextView) tr.findViewById(R.id.tvFarePass3m);
                tv.setText(fare.miFarePass3m > 0 ? Integer.toString(fare.miFarePass3m) : "-");
                tv.setVisibility(View.VISIBLE);
                tv = (TextView) tr.findViewById(R.id.tvFarePass3m1st);
                tv.setText(fare.miFarePass3m1st > 0 ? Integer.toString(fare.miFarePass3m1st) : "-");
                tv.setVisibility(View.VISIBLE);
            }
            if (fare.miFarePass6m > 0 || fare.miFarePass6m1st > 0) {
                tv = (TextView) tr.findViewById(R.id.tv6mLabel);
                tv.setVisibility(View.VISIBLE);
                tv = (TextView) tr.findViewById(R.id.tvFarePass6m);
                tv.setText(fare.miFarePass6m > 0 ? Integer.toString(fare.miFarePass6m) : "-");
                tv.setVisibility(View.VISIBLE);
                tv = (TextView) tr.findViewById(R.id.tvFarePass6m1st);
                tv.setText(fare.miFarePass6m1st > 0 ? Integer.toString(fare.miFarePass6m1st) : "-");
                tv.setVisibility(View.VISIBLE);
            }
            if (fare.miFarePass1y > 0 || fare.miFarePass1y1st > 0) {
                rl = (RelativeLayout) tr.findViewById(R.id.rl1yLabel);
                rl.setVisibility(View.VISIBLE);
                tv = (TextView) tr.findViewById(R.id.tvFarePass1y);
                tv.setText(fare.miFarePass1y > 0 ? Integer.toString(fare.miFarePass1y) : "-");
                tv = (TextView) tr.findViewById(R.id.tvFarePass1y1st);
                tv.setText(fare.miFarePass1y1st > 0 ? Integer.toString(fare.miFarePass1y1st) : "-");
            }
            tv = (TextView) tr.findViewById(R.id.tvVia);
            if (TextUtils.isEmpty(fare.msVia) || fare.msVia.equals("-")) {
                tv.setText("Direct (" + fare.msLineCode + ")");
            } else {
                tv.setText("via " + fare.msVia);
            }
            tl.addView(tr);
            View ruler = new View(this);
            ruler.setBackgroundColor(0xFF00FF00);
            tl.addView(ruler,
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
        }
        if (aoFare.size() == 0) {
            try {
                tv = new TextView(ActFares.this);
                tv.setTextSize(24);
                tv.setPadding(5, 5, 5, 5);
                final String subject = "Fare information for: " + moStartStation.msSearchStr + " to " + moDestStation.msSearchStr;
                tv.setText(Html.fromHtml("Fare information for this route not available<br>Help other SmartShehar.com users by emailing us this info <a href=\"mailto:userfeedback@smartshehar.com?subject=" + subject + "\">userfeedback@smartshehar.com</a>"));
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        String[] TO = {"userfeedback@smartshehar.com"};
                        emailIntent.setData(Uri.parse("mailto:"));
                        emailIntent.setType("plain/text");
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                        startActivity(emailIntent);
                    }
                });
                tl.addView(tv);
            } catch (Exception e) {

                Log.d(TAG, "setMovementMethod " + e.toString());
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void showFares(Station oStation, int sortOrder) {
        Log.d(TAG, "showFares");
        if (mApp == null || CGlobals_trains.mDBHelper == null) {
            assert mApp != null;
            mApp.init(this);
        }
        ArrayList<Fare> maFares = CGlobals_trains.mDBHelper.getFareCursor(oStation.miStationId, sortOrder);
        ListView listContent = (ListView) findViewById(R.id.farelist);
        if (maFares.size() > 0) {
            mFareAdapter = new TrainFaresAdapter(ActFares.this, maFares);
            listContent.setAdapter(mFareAdapter);
            listContent.setVisibility(View.VISIBLE);
            findViewById(R.id.llHeader).setVisibility(View.VISIBLE);
            findViewById(R.id.tlFare).setVisibility(View.GONE);
        } else {
            Toast.makeText(ActFares.this, "Empty list", Toast.LENGTH_LONG).show();
            listContent.setVisibility(View.GONE);
            findViewById(R.id.llHeader).setVisibility(View.GONE);
            findViewById(R.id.tlFare).setVisibility(View.VISIBLE);
            try {
                TextView tv = new TextView(ActFares.this);
                tv.setTextSize(24);
                tv.setPadding(5, 5, 5, 5);
                final String subject = "Fare information for: " + oStation.msSearchStr;
                tv.setText(Html.fromHtml("Fare information for this route not available<br>Help other SmartShehar.com users by emailing us this info <a href=\"mailto:userfeedback@smartshehar.com?subject=" + subject + "\">userfeedback@smartshehar.com</a>"));
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        String[] TO = {"userfeedback@smartshehar.com"};
                        emailIntent.setData(Uri.parse("mailto:"));
                        emailIntent.setType("plain/text");
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                        startActivity(emailIntent);
                    }
                });
                TableLayout tl = (TableLayout) findViewById(R.id.tlFare);
                tl.removeAllViews();
                tl.addView(tv);
            } catch (Exception e) {

                Log.d(TAG, "setMovementMethod " + e.toString());
                e.printStackTrace();
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
            if (sRecentStations.trim().length() > 0) {
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
//            writeRecentStations();
            setResult(RESULT_CANCELED);
            super.finish();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_fare, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_locate) {
            CGlobals_trains.getInstance().turnGPSOn(ActFares.this, mGoogleApiClient);
            mStartView.post(new Runnable() {
                @Override
                public void run() {
                    mbAutoUpdate = true;
                    mbAutoLocation = true;
                    refreshLocation(CGlobals_lib_ss.getInstance().getMyLocation(ActFares.this));
                }
            });

        }

        return super.onOptionsItemSelected(item);
    }

    void setStartStation(String sStartStationFull) {
        Station retStn = CGlobals_trains.mDBHelper
                .getStationFromSearchStr(sStartStationFull);
        if (retStn != null) {
            moStartStation = retStn;
//            mStartView.setThreshold(1000);
            mStartView.setText(moStartStation.msSearchStr);


            if (moDestStation != null) {
                showFare(moStartStation.miStationId, moDestStation.miStationId, isSortByLine ? LINE : STATION);
                findViewById(R.id.farelist).setVisibility(View.GONE);
                findViewById(R.id.llHeader).setVisibility(View.GONE);
                findViewById(R.id.tlFare).setVisibility(View.VISIBLE);
            } else {
                showFares(moStartStation, isSortByLine ? LINE : STATION);

            }
//            addRecentStation(moStartStation.msSearchStr);
            mStartView.setText(sStartStationFull);
//            mStartView.dismissDropDown();
        }
    } // setStartStation

    void setDestStation(String sStationFull) {
        try {
            Station retStn = CGlobals_trains.mDBHelper
                    .getStationFromSearchStr(sStationFull);
            if (retStn != null) {
                moDestStation = retStn;
//                mDestView.setThreshold(1000);
                mDestView.setText(moDestStation.msSearchStr);
                findViewById(R.id.farelist).setVisibility(View.GONE);
                findViewById(R.id.llHeader).setVisibility(View.GONE);
                findViewById(R.id.tlFare).setVisibility(View.VISIBLE);
                showFare(moStartStation.miStationId, moDestStation.miStationId, isSortByLine ? LINE : STATION);
//                addRecentStation(moDestStation.msSearchStr);
                mDestView.setText(sStationFull);


            }
        } catch (Exception e) {
            Log.d(TAG, "Exception e " + e.toString());
        }
    } // setDestStation

    @Override
    public void onBackPressed() {
        if (mbExpandedTrains) {
            mbExpandedTrains = false;
        } else
            super.onBackPressed();
    }

    void keyboardShowHide(boolean bShow) {
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
            mBtnRecentStations.requestFocus();


//		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the tracker when it is no longer needed.
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
        if (mbAutoUpdate)
        {CGlobals_lib_ss.setMyLocation(location, false, ActFares.this);
            refreshLocation(location);}
    }

    protected void startLocationUpdates() {
        MyLocation myLocation = new MyLocation(
                SSApp.mRequestQueue, ActFares.this, mGoogleApiClient);
        myLocation.getLocation(this, onLocationResult);

        if (ActivityCompat.checkSelfPermission(ActFares.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(ActFares.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (mGoogleApiClient.isConnected()) {
            PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            Log.d(TAG, "Location update started ..............: " + pendingResult.toString());
        }
    }
}
