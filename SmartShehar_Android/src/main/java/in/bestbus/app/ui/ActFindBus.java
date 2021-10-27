package in.bestbus.app.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import in.bestbus.app.ActFindBus_adapter;
import in.bestbus.app.CBus;
import in.bestbus.app.CGlobals_BA;
import in.bestbus.app.CStop;
import in.bestbus.app.Constants_bus;
import in.bestbus.app.NavItem;
import in.bestbus.app.Station;
import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;
import lib.app.util.SSLog_SS;
import lib.app.util.ui.SSActivity;

public class ActFindBus extends SSActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 3;
    private final static String RECENT_TRIPS_TAG = "RecentTrips";
    private final static int MAX_RECENT_TRIPS = 10;
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    private final String TAG = "ActFindBus: ";
    private final String SELECTRECENT = "Select Stop";
    private final String SELECTSTOPS = "Stop List";
    private final int MAXRECENTSTOPS = 5;
    private final int ACTRESULT_START = 1;
    private final int ACTRESULT_DEST = 2;
    CGlobals_BA mApp;
    ProgressBar mProgressBar;
    Location myLocation;
    int miProgress = 0;
    ArrayList<CBus> maBusesAtStop;
    ArrayAdapter<String> mStartSpinnerAdapter;
    Cursor startListCursor, destListCursor;
    ArrayList<String> maStation = null; //, maStop = null;
    CurrentLayoutData mCurrentLayoutData;
    ImageView mBtnClearDest;
    ImageView mBtnStopsNearStart;
    Button mBtnReverseTrip, mBtnGoTrip;
    ImageView mBtnRecentTrips;
    ListView tblBusesAtStop;
    GoogleApiClient mGoogleApiClient = null;
    int iCurrentStopCode;
    private int iCurrentStopId = -1, mDoW;
    private ArrayList<Trip> maoRecentTrips;
    private ArrayList<String> masRecentTripsList;
    private ArrayAdapter<String> mRecentTripsAdapter;
    //		Button mBtnRecentStops;
    private TextView mTvBusesToStation, mTvLandmarkList, tvBusNoList, tvBusStop;
    private AutoCompleteTextView mStartView, mDestView;
    private LocationRequest mLocationRequest;
    double lon = 0.0, lat = 0.0;
    String sLastAccess = "";
    double busCurrentLat, busCurrentLng, speed;
    String busCLable, tripstatus, sDirection;
    int fromStopSerial, toStopSerial;
    double iMyDistanceUp, iMyDistanceDown;
    Connectivity mConnectivity;

    //Action menu bar
    ArrayList<NavItem> mNavItems = new ArrayList<NavItem>();
    ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    RelativeLayout mDrawerPane;

    /*private InterstitialAd interstitial;
        private AdView adView; */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_findbus);
        try {
            mConnectivity = new Connectivity();
            tblBusesAtStop = (ListView) findViewById(R.id.tblBuseAtStop);
            LayoutInflater inflater = this.getLayoutInflater();
            View header = inflater.inflate(R.layout.actfindbus_list, tblBusesAtStop, false);
            tblBusesAtStop.addHeaderView(header, null, false);
            mGoogleApiClient = new GoogleApiClient.Builder(ActFindBus.this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(ActFindBus.this)
                    .addOnConnectionFailedListener(ActFindBus.this)
                    .build();
            mGoogleApiClient.connect();
            ActionBar ab = getSupportActionBar();
            ab.setTitle("  Bus Stop");
            ab.setDisplayShowHomeEnabled(false);
            ab.setDisplayHomeAsUpEnabled(true);
            setupLocationListeners();
            mLocationRequest = LocationRequest.create();
            // Use high accuracy
            mLocationRequest.setPriority(
                    LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            // Set the update interval to 5 seconds
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            // Set the fastest update interval to 1 second
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
            mApp = CGlobals_BA.getInstance();
            mApp.init(this);
///      	AnalyticsUtils.getInstance(this).trackPageView(getString(R.string.pageMapLong));
            mApp.mCallHome.userPing(getString(R.string.pageFindBus), "");
            maBusesAtStop = new ArrayList<CBus>();
            mApp.mbAutoRefreshLocation = true;
            masRecentTripsList = new ArrayList<String>();
            maoRecentTrips = new ArrayList<Trip>();
            maoRecentTrips = readRecentTrips();
            mApp.getMyLocation(ActFindBus.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        /*adView.resume();*/
        // maBusesAtStop = new ArrayList<CBus>();
        try {
            startLocationListeners();
            mCurrentLayoutData = new CurrentLayoutData();
            setupUI();
            mProgressBar.setIndeterminate(false);
            mProgressBar.setMax(100);
            if ((maStation == null || maStation.size() == 0))
                maStation = mApp.mDBHelperBus.getStationNames();
//		if (maStop == null || maStop.size() == 0)
//		maStop = mApp.mDBHelper.getStopNames();
            mApp.masRecentStops = readRecentStops();
            if (mApp.mbAutoRefreshLocation)
                updateMyLocation(mApp.getMyLocation(ActFindBus.this));

            if (mApp.moStartStop != null &&
                    mApp.moStartStop.miStopId == mCurrentLayoutData.miStartStopId) {
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(10);
                //displayBuses(mCurrentLayoutData.aBusesAtStop);
                if (TextUtils.isEmpty(mCurrentLayoutData.sBusesToStation))
                    new getBusesToNearestStation().execute("");
                else
                    mTvBusesToStation.setText(mCurrentLayoutData.sBusesToStation);
                new getBusFrequencies().execute("");

                mProgressBar.setVisibility(View.GONE);
            } else {
                iCurrentStopId = -1;
                refreshStartStop();
            }
            enableInput();
            keyboardShowHide(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    // onResume

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menufragfind, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_location) {
            /*Toast.makeText(this, "Getting location...", Toast.LENGTH_SHORT).show();*/
            iCurrentStopId = -1;
            mApp.mbAutoRefreshLocation = true;
            Location location = mApp.getMyLocation(ActFindBus.this);
            updateMyLocation(location);
            CGlobals_lib_ss.getInstance().turnGPSOn1(ActFindBus.this, mGoogleApiClient);
        } else if (item.getItemId() == R.id.menu_refresh) {
            try {
                iCurrentStopId = -1;
                mApp.mbAutoRefreshLocation = false;
                displayBuses(mCurrentLayoutData.aBusesAtStop);
                String start = mApp.moStartStop.msStopNameDetail;
                int iStopcode = mApp.moStartStop.miStopCode;
                if (!TextUtils.isEmpty(start)) {
                    setStartStop(iStopcode); //get stop id
                    mDestView.setText("");
                    mApp.moDestStop = null;
                }
                Toast.makeText(this, "Refreshing ...", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                SSLog.e(TAG, "Refreshing: ", e);
            }
        }
        return super.onOptionsItemSelected(item);
    } // onOptions

    @Override
    public void onPause() {
        writeRecentStops();
        writeRecentTrips();
        stopLocationListeners();
        //adView.pause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        // adView.destroy();
        super.onDestroy();
        //saveperf();
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        // EasyTracker.getInstance(this).activityStart(this);
        super.onStart();
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, ActFindBus.this);
        }
        mGoogleApiClient.disconnect();
        //  EasyTracker.getInstance(this).activityStop(this);
        super.onStop();
        //saveperf();
    }

    public void updateMyLocation(Location location) {
        // Now get the places at this location, if this fragment is still bound
        // to an activity
        if (!mApp.mbAutoRefreshLocation || location == null)
            return;
        float[] res = new float[1];
        if (myLocation != null) {
            Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                    myLocation.getLatitude(), myLocation.getLongitude(), res);
//			if((int)res[0] < 50)
//				return;
        }

        myLocation = location;
//		if (!mApp.isBetterLocation(location, mApp.mCurrentLocation))
//			return;
        tblBusesAtStop.post(new Runnable() {
            @Override
            public void run() {
                Location location = mApp.getMyLocation(ActFindBus.this);
                if (location != null)
                    mApp.moStartStop = mApp.mDBHelperBus.getNearestStop(location.getLatitude(),
                            location.getLongitude());
                if (mApp.moStartStop == null)
                    return;
                setStartStop(mApp.moStartStop.miStopCode);  //get stop id
            }
        });
    }

    private void displayBuses(ArrayList<CBus> aBusesAtStop) {
        try {
            LayoutInflater inflater = this.getLayoutInflater();
            String sCurStopName = "";
            for (final CBus oBus : aBusesAtStop) {
                if (!oBus.msStopName.equals(sCurStopName)) {
                    LinearLayout trstop = (LinearLayout) inflater.inflate(
                            R.layout.stoprow, tblBusesAtStop, false);
                    TextView tvStopName = (TextView) trstop
                            .findViewById(R.id.tvStopName);
                    tvStopName.setText(aBusesAtStop.get(0).msStopName);
                    sCurStopName = oBus.msStopName;
                }
            }
            ArrayList<CBus> cbusarr = new ArrayList<CBus>();
            for (CBus oBus : aBusesAtStop) {
                cbusarr.add(oBus);
            }
            if (cbusarr.size() > 0) {
                tblBusesAtStop.setAdapter(new ActFindBus_adapter(ActFindBus.this, cbusarr, iCurrentStopCode));
            }
        } catch (Exception e) {
            SSLog.e(TAG, "displayBuses: ", e);
        }
    }

    private void refreshStartStop() {
        Location location = mApp.getMyLocation(ActFindBus.this);
        if (mApp.moStartStop != null)
            setStartStop(mApp.moStartStop.miStopCode); //get stop id
        else if (location != null) {
            if (mApp.mbAutoRefreshLocation)
                updateMyLocation(location);
        }
        mStartView.dismissDropDown();
    }

    void enableInput() {
        mStartView.setEnabled(true); // mStartView.setInputType(InputType.TYPE_NULL);
//        mDestView.setEnabled(true); // mDestView.setInputType(InputType.TYPE_NULL);

    }

    void disableInput() {
        mStartView.setEnabled(false); // mStartView.setInputType(InputType.TYPE_NULL);
//        mDestView.setEnabled(false); // mDestView.setInputType(InputType.TYPE_NULL);

    }

    public void keyboardShowHide(boolean bShow) {
        if (bShow) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        } else {
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            View cFocus = getCurrentFocus();
            if (imm != null && cFocus != null)
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            else
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        }
    }

    void setStartStop(int iStop_Code) {
        CStop retStop = mApp.mDBHelperBus.getStopFromSearchStr(iStop_Code);
        if (retStop != null) {
            mStartView.setText("");
            mApp.moStartStop = retStop;
            mStartView.setThreshold(1000);
            mStartView.setText(mApp.moStartStop.msStopNameDetail +
                    String.format(" (%.2f)", mApp.moStartStop.mdDist / 1000));
            iCurrentStopCode = mApp.moStartStop.miStopCode;
            mTvLandmarkList.setText(mApp.moStartStop.msLandmarkList);
            showBusesAtStop(mApp.moStartStop.miStopId);
            addRecentStop(mApp.moStartStop.msStopNameDetail);
            mStartView.dismissDropDown();

        }
    }

    void setDestStop(int iStopCode) {
        CStop retStop = mApp.mDBHelperBus.getStopFromSearchStr(iStopCode);
        if (retStop != null) {
            mApp.moDestStop = retStop;
//			mDestView.setThreshold(1000);
            mDestView.setText(mApp.moDestStop.msStopNameDetail);
//			mDestView.dismissDropDown();
            showRoutes(mApp.moStartStop, mApp.moDestStop, false, "0");
        }
    }

    private void layoutDone() {
        if (miProgress < 70)
            return;
        mProgressBar.setProgress(100);
        mProgressBar.setVisibility(View.GONE);
        mCurrentLayoutData.miStartStopId = mApp.moStartStop.miStopId;
        if (maBusesAtStop != null)
            mCurrentLayoutData.aBusesAtStop = maBusesAtStop;
    }

    private void showRoutes(CStop startStop, CStop destStop, boolean bSilent, String directStationBus) {
        try {
            CStop oDestStop = null;
            if (destStop == null) {
                boolean bFailed = true;
                String sDest = mDestView.getText().toString();
                // if (!TextUtils.isEmpty(sDest))
                oDestStop = mApp.mDBHelperBus.getStopFromSearchStr(destStop.miStopCode);
                if (oDestStop != null)
                    destStop = oDestStop;
                if (bFailed && oDestStop == null) {
                    if (!bSilent)
                        Toast.makeText(getApplicationContext(), "Invalid destination Stop",
                                Toast.LENGTH_SHORT).show();
                    return;
                }

            }
            try {
                mApp.moStartStop = startStop;
                mApp.moDestStop = destStop;

                if (mApp.moStartStop.miStopId == mApp.moDestStop.miStopId) {
                    if (!bSilent)
                        Toast.makeText(getApplicationContext(), "Same Start and Destination CStop",
                                Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                SSLog.e(TAG, "showRoutes:", e);
            }
            addRecentTrip(startStop, destStop);
            mApp.mCallHome.userPing(getString(R.string.atShowRoute),
                    mApp.moStartStop.miStopId + "_" + mApp.moDestStop.miStopId);
            //       fireTrackerEvent(getString(R.string.atShowRoute) + "_" +
            //       		mApp.moStartStop.msStopNameDetail + " - " + mApp.moDestStop.msStopNameDetail);
            Intent intent = new Intent(ActFindBus.this, ActBusTrip.class);
            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("DIRECT_STATION_BUS", directStationBus);
                startActivity(intent);
            }
        } catch (Exception e) {
            SSLog.e(TAG, "showRoutes:", e);
        }
    } // showRoutes

    private void showBusesAtStop(int iStopId) {
        if (iCurrentStopId != iStopId) {
            iCurrentStopId = iStopId;
            Calendar c = Calendar.getInstance();
            mDoW = mApp.dow(c.get(Calendar.DAY_OF_WEEK));
            miProgress = 0;
            new getBusesToNearestStation().execute("");
            new GetBusesAtStop().execute("");
        }

        // new GetArrivingBuses().execute("");

    } // showBusesTowards

    // Add a trip chosen by user
    void addRecentTrip(CStop oStartStop, CStop oDestStop) {
        if (maoRecentTrips == null)
            maoRecentTrips = new ArrayList<Trip>();
        if (masRecentTripsList == null)
            masRecentTripsList = new ArrayList<String>();
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        // remove the same Stop lower in the list
        for (Trip trip : maoRecentTrips) {
            if (trip.oStartStop.miStopId == oStartStop.miStopId &&
                    trip.oDestStop.miStopId == oDestStop.miStopId) {
                maoRecentTrips.remove(maoRecentTrips.indexOf(trip));
                break;
            }
        }
        maoRecentTrips.add(0, new Trip(oStartStop, oDestStop,
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))); // add as first trip FIFO
        int iLen = maoRecentTrips.size();
        if (iLen > MAX_RECENT_TRIPS)
            maoRecentTrips.remove(iLen - 1);

        masRecentTripsList.clear();
        for (Trip tr : maoRecentTrips) {
            masRecentTripsList.add(tr.msTrip); // + "\n - " + tr.oDestStop.msStopNameDetail);
        }
        mRecentTripsAdapter = new ArrayAdapter<String>(ActFindBus.this,
                R.layout.multiline_spinner_dropdown_item, masRecentTripsList);
        writeRecentTrips();
    } // addRecentTrips

    void addRecentStop(String sStopFull) {
        if (mApp.masRecentStops == null)
            mApp.masRecentStops = new ArrayList<String>();
        // remove the same Station lower in the list
        for (String sf : mApp.masRecentStops) {
            if (sf.equalsIgnoreCase(sStopFull)) {
                mApp.masRecentStops.remove(mApp.masRecentStops.indexOf(sf));
                break;
            }
        }
        mApp.masRecentStops.add(0, sStopFull);
        int iLen = mApp.masRecentStops.size();
        if (iLen > MAXRECENTSTOPS)
            mApp.masRecentStops.remove(iLen - 1);
    } // addRecentStop

    void setupUI() {
        if (mApp == null) {

        }
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mTvLandmarkList = (TextView) findViewById(R.id.tvLandmarkList);
        mTvBusesToStation = (TextView) findViewById(R.id.tvBusesToStation);
        tvBusStop = (TextView) findViewById(R.id.tvBusStop);
        tvBusNoList = (TextView) findViewById(R.id.tvBusNoList);
//		mStartSpinner = (Spinner) findViewById(R.id.start_spinner);
        mBtnRecentTrips = (ImageView) findViewById(R.id.btnRecentTrips);
        mBtnReverseTrip = (Button) findViewById(R.id.btnReverseTrip);
        mBtnGoTrip = (Button) findViewById(R.id.btnGoTrip);
        mBtnStopsNearStart = (ImageView) findViewById(R.id.mBtnStopsNearStart);
        mBtnClearDest = (ImageView) findViewById(R.id.btnClearDest);
        mStartView = (AutoCompleteTextView) findViewById(
                R.id.autocomplete_startstop);
        mDestView = (AutoCompleteTextView) findViewById(
                R.id.autocomplete_deststop);
//		mllStartStation = (LinearLayout) findViewById(R.id.llStartStation);
        tblBusesAtStop = (ListView) findViewById(
                R.id.tblBuseAtStop);

        tvBusStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    iCurrentStopId = -1;
                    mApp.mbAutoRefreshLocation = false;
                    displayBuses(mCurrentLayoutData.aBusesAtStop);
                    String start = mApp.moStartStop.msStopNameDetail;
                    int iStopCode = mApp.moStartStop.miStopCode;
                    if (!TextUtils.isEmpty(start)) {
                        setStartStop(iStopCode); //get stop id
                        mDestView.setText("");
                        mApp.moDestStop = null;
                    }
                } catch (Exception e) {
                    SSLog.e(TAG, "Refreshing: ", e);
                }
            }
        });

        tvBusNoList.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActFindBus.this, ActSearchBus.class);
                startActivity(intent);
            }
        });

//		StopListAdapter startAdapter, destAdapter;
//		masRecentTripsList = new ArrayList<String>();
        mStartView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(ActFindBus.this, ActSearchStop.class);
                startActivityForResult(i, ACTRESULT_START);
            }
        });

        mDestView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(ActFindBus.this, ActSearchStop.class);
                startActivityForResult(i, ACTRESULT_DEST);
            }
        });

        mStartSpinnerAdapter = new ArrayAdapter<String>(this,
                R.layout.listitem, mApp.masRecentStops);

        mStartSpinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

//		mStartSpinner.setAdapter(mStartSpinnerAdapter);
        mBtnRecentTrips.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mApp.mbAutoRefreshLocation = false;
                if (masRecentTripsList != null && masRecentTripsList.size() > 0) {
                    new AlertDialog.Builder(ActFindBus.this)
                            .setTitle("Recent Trips")
                            .setAdapter(mRecentTripsAdapter, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        Toast.makeText(ActFindBus.this, masRecentTripsList.get(which),
                                                Toast.LENGTH_LONG).show();
                                        dialog.dismiss();
                                        mApp.mCallHome.userPing(getString(R.string.atRecentTrip), "");
                                        setStartDest(maoRecentTrips.get(which).oStartStop, maoRecentTrips.get(which).oDestStop);
                                    } catch (Exception e) {
                                        SSLog_SS.e(TAG, e.getMessage());
                                    }
                                }
                            }).create().show();
                } else
                    Toast.makeText(ActFindBus.this, "No Recent Trips",
                            Toast.LENGTH_LONG).show();

            }
        });

        mBtnStopsNearStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (TextUtils.isEmpty(mStartView.getText().toString()) && mApp.getMyLocation(ActFindBus.this) == null) {
                    Toast.makeText(ActFindBus.this,
                            "Start GPS for location or enter a start stop to see near stops", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ActFindBus.this,
                            "Getting stops near the start stop", Toast.LENGTH_SHORT).show();

                    Intent i = new Intent(ActFindBus.this, ActSearchNearStop.class);
                    startActivityForResult(i, ACTRESULT_START);
                }
            }

        });

        mBtnClearDest.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mDestView.setText("");
                mApp.mbAutoRefreshLocation = false;
            }
        });

        mBtnGoTrip.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showRoutes(mApp.moStartStop, mApp.moDestStop, false, "0");
            }
        });

        mBtnReverseTrip.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String sStart = mStartView.getText().toString();
                String sDest = mDestView.getText().toString();
                if (!TextUtils.isEmpty(sStart))
                    mApp.moDestStop = mApp.mDBHelperBus.getStopFromSearchStr(mApp.moStartStop.miStopCode);
                if (!TextUtils.isEmpty(sDest))
                    mApp.moStartStop = mApp.mDBHelperBus.getStopFromSearchStr(mApp.moDestStop.miStopCode);
                mDestView.setText(sStart);
                mStartView.setText(sDest);
                showRoutes(mApp.moStartStop, mApp.moDestStop, false, "0");
                mApp.mbAutoRefreshLocation = false;
                mApp.mCallHome.userPing(getString(R.string.atFlipTrip), "");
            }
        });
        mStartView.invalidate();
        mStartView.clearFocus();
        mStartView.setThreshold(1000);
        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mApp.mbAutoRefreshLocation = false;
            String sStopNameDetail = CGlobals_BA.getInstance().getPersistentPreference(ActFindBus.this).
                    getString("BUS_START_STOP_ADDRESS", "");
            String sOStop = CGlobals_BA.getInstance().getPersistentPreference(ActFindBus.this).
                    getString("PERF_CSTOP", "");
            Type type = new TypeToken<CStop>() {
            }.getType();
            CStop cStop = new Gson().fromJson(sOStop, type);
            setStartStop(cStop.miStopCode);
        }
        // keyboardShowHide(false);
    } // setupUI ends

    public void onClickBusNearStation(View view) {
        try {
            if (!TextUtils.isEmpty(mApp.moStartStop.msStopNameDetail) &&
                    mApp.moStartStop.lat != 0.0 && mApp.moStartStop.lon != 0.0) {
                Station stn = mApp.mDBHelperBus.getNearestStation(mApp.moStartStop.lat, mApp.moStartStop.lon);
                mApp.moDestStop = mApp.mDBHelperBus.getNearestStop(stn.mdLat, stn.mdLon);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActFindBus.this).
                        putString(Constants_bus.DIRECT_STATIONS_TO_BUSES_STNAME, stn.msSearchStr);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActFindBus.this).commit();
                showRoutes(mApp.moStartStop, mApp.moDestStop, false, "1");
            } /*else {
                Toast.makeText(this, "Please enter a start location or \nturn on location by touching the GPS button",
                        Toast.LENGTH_LONG).show();
            }*/
        } catch (Exception e) {
            SSLog.e(TAG, "BusNearStation: ", e);
        }
    }

    private void setStartDest(CStop oStartStop, CStop oDestStop) {
        mStartView.setText("");
        mApp.moStartStop = oStartStop;
        mApp.moDestStop = oDestStop;
        mStartView.setText(oStartStop.msStopNameDetail);
        mDestView.setText(oDestStop.msStopNameDetail);
        mApp.mbAutoRefreshLocation = false;
        showRoutes(mApp.moStartStop, mApp.moDestStop, false, "0");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        CStop cStop;
        if (requestCode == ACTRESULT_START) {
            if (resultCode == RESULT_OK) {
                try {
                    mApp.mbAutoRefreshLocation = false;
//			         String result=data.getStringExtra("result");
                    String sOStop = data.getStringExtra("oStop");
                    Type type = new TypeToken<CStop>() {
                    }.getType();
                    cStop = new Gson().fromJson(sOStop, type);
//			    	 mApp.msBusNo = data.getStringExtra("busno");
                    if (!TextUtils.isEmpty(data.getStringExtra("stopnamedetail"))) {
                        mStartView.setText(data.getStringExtra("stopnamedetail"));
                    }
                    setStartStop(cStop.miStopCode);
                    CGlobals_BA.getInstance().getPersistentPreferenceEditor(ActFindBus.this).
                            putString("PERF_CSTOP", sOStop);
                    CGlobals_BA.getInstance().getPersistentPreferenceEditor(ActFindBus.this).
                            putString("BUS_START_STOP_ADDRESS", data.getStringExtra("stopnamedetail"));
                    CGlobals_BA.getInstance().getPersistentPreferenceEditor(ActFindBus.this).commit();
                } catch (Exception e) {
                    SSLog.e(TAG, "onActivityResult: ", e);
                }
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
        if (requestCode == ACTRESULT_DEST) {
            if (resultCode == RESULT_OK) {
//		         String result=data.getStringExtra("result");
                try {
                    String sOStop = data.getStringExtra("oStop");
                    Type type = new TypeToken<CStop>() {
                    }.getType();
                    cStop = new Gson().fromJson(sOStop, type);
//		    	 mApp.msBusNo = data.getStringExtra("busno");
                    if (!TextUtils.isEmpty(data.getStringExtra("stopnamedetail"))) {
                        mDestView.setText(data.getStringExtra("stopnamedetail"));
                    }
                    setDestStop(cStop.miStopCode);
                    CGlobals_BA.getInstance().getPersistentPreferenceEditor(ActFindBus.this).
                            putString("PERF_CSTOP_DEST", sOStop);
                    CGlobals_BA.getInstance().getPersistentPreferenceEditor(ActFindBus.this).commit();
                } catch (Exception e) {
                    SSLog.e(TAG, "onActivityResult: ", e);
                }
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }

        if (requestCode == CGlobals_BA.REQUEST_LOCATION) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    Log.d(TAG, "YES");
                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    Log.d(TAG, "NO");
                    CGlobals_lib_ss.showGPSDialog = false;
                    break;
                default:
                    break;
            }
        }
    }

    private void writeRecentStops() {
        new Thread(new Runnable() {
            public void run() {
                StringBuilder sb = new StringBuilder();
                String delim = "";
                // masRecentStops = getRecentStops();
                int iLen = mApp.masRecentStops.size();
                if (iLen < 1)
                    return;
                for (String s : mApp.masRecentStops) {
                    if (!TextUtils.isEmpty(s)) {
                        sb.append(delim + s);
                        delim = ";";
                    }
                }
                SharedPreferences spRecentStops = getSharedPreferences(
                        SELECTSTOPS, MODE_PRIVATE);
                SharedPreferences.Editor speRecentStopsEditor;
                speRecentStopsEditor = spRecentStops.edit();

                speRecentStopsEditor.putString(SELECTSTOPS, sb.toString());
                speRecentStopsEditor.commit();
            }
        }).start();
    }

    ArrayList<String> readRecentStops() {
        ArrayList<String> aRS = new ArrayList<String>();
        SharedPreferences spRecentStops = this.getSharedPreferences(SELECTSTOPS, Context.MODE_PRIVATE);
        String sRecentStops = spRecentStops.getString(SELECTSTOPS, null);
        SSLog_SS.i("BusApp: TrainHome - ", sRecentStops);
        if (sRecentStops != null) {
            boolean bStopThere = false;
            String s;
            if (sRecentStops != null & sRecentStops.trim().length() > 0) {
                String as[] = sRecentStops.split(";");
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
                                // SSLog_SS.i("BusApp: Recent Station: ", s);
                                // if (oStop != null)
                                aRS.add(s);
                            }
                        } catch (Exception e) {
                            SSLog_SS.e("BusApp: getRecentStations - ",
                                    e.getMessage());
                            SSLog_SS.e("BusApp: getRecentStations - mApp - ",
                                    mApp == null ? " null" : " ");

                        }
                    }
                }
            }
        }
        return aRS;
    } // readRecentStops

    public void clickedStartMap(View view) {
        try {
            mApp.mCurrentLocation = CGlobals_lib_ss.getInstance().getMyLocation(ActFindBus.this);
            if (!mConnectivity.connectionError(ActFindBus.this, getString(R.string.appTitle))) {
                Intent intent = new Intent(ActFindBus.this, ActWalkToStop.class);
                if (intent != null) {
                    if (mApp.mCurrentLocation != null) {
                        intent.putExtra("startlat", mApp.mCurrentLocation.getLatitude());
                        intent.putExtra("startlon", mApp.mCurrentLocation.getLongitude());
                        intent.putExtra("destlat", mApp.moStartStop.lat);
                        intent.putExtra("destlon", mApp.moStartStop.lon);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Cannot get location. \nPlease turn on internet and/or GPS", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Cannot get location\nPlease try again", Toast.LENGTH_SHORT).show();
            SSLog.e(TAG, "clickedStartMap: ", e);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        Toast.makeText(this, "Cannot get location. \nPlease turn on internet and/or GPS", Toast.LENGTH_SHORT).show();
//		mApp.showGPSDisabledAlertToUser(this);
    }

    @Override
    public void onConnected(Bundle arg0) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mApp.mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mApp.mCurrentLocation != null) {
            startPeriodicUpdates();
           /* Toast.makeText(this, "Got Location!", Toast.LENGTH_SHORT).show();*/
        } else {
            //Toast.makeText(this, "Cannot get location. Pleae make sure location services is on or\nPlease turn on GPS", Toast.LENGTH_SHORT).show();
//    		mApp.showGPSDisabledAlertToUser(this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (mApp.mbAutoRefreshLocation)
            updateMyLocation(mApp.getMyLocation(ActFindBus.this));
    }

    private void startPeriodicUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, ActFindBus.this);
    }

    ArrayList<Trip> readRecentTrips() {
        ArrayList<Trip> aRS = new ArrayList<Trip>();
        SharedPreferences spRecentTrips = getSharedPreferences(
                RECENT_TRIPS_TAG, Context.MODE_PRIVATE);

        String sRecentTrips = spRecentTrips.getString(RECENT_TRIPS_TAG,
                null);
        SSLog_SS.i(TAG + " readRecentTrips - ", sRecentTrips);
        if (sRecentTrips == null)
            return aRS;
        boolean bStopThere = false;
        String[] s;

        int iStart, iDest;
        if (sRecentTrips != null & sRecentTrips.trim().length() > 0) {
            String as[] = sRecentTrips.split("\n");
            int iLen = as.length;
            for (String stationPair : as) {
                if (stationPair != null && !stationPair.equalsIgnoreCase(SELECTRECENT)) {
                    bStopThere = false;

                    try {
                        if (!bStopThere) {
                            SSLog_SS.i(TAG + " Recent Trip - ", stationPair);
                            // if (oStop != null)
                            s = stationPair.split(";");
                            iStart = Integer.valueOf(s[0]);
                            iDest = Integer.valueOf(s[1]);
                            aRS.add(new Trip(iStart, iDest));
                        }
                    } catch (Exception e) {
                        SSLog_SS.e("TrainApp: getRecentTrip - ",
                                e.getMessage());
                    }
                }
            }
        }
        masRecentTripsList.clear();
        for (Trip tr : aRS) {
            masRecentTripsList.add(tr.msTrip);
//				masRecentTripsList.add(tr.oStartStop.msStopName + "\n" + tr.oDestStop.msStopName);
        }
        mRecentTripsAdapter = new ArrayAdapter<String>(ActFindBus.this,
                R.layout.multiline_spinner_dropdown_item, masRecentTripsList);

        return aRS;
    } // readRecentTrips

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
                    s = Integer.toString(maoRecentTrips.get(i).oStartStop.miStopId)
                            + ";" + Integer.toString(maoRecentTrips.get(i).oDestStop.miStopId);
                    if (!TextUtils.isEmpty(s)) {
                        sb.append(lineDelim + s);
                        lineDelim = "\n";
                    }
                }
                SharedPreferences spRecentTrips = getSharedPreferences(
                        RECENT_TRIPS_TAG, Context.MODE_PRIVATE);
                SharedPreferences.Editor speRecentStopsEditor;
                speRecentStopsEditor = spRecentTrips.edit();

                speRecentStopsEditor.putString(RECENT_TRIPS_TAG, sb.toString());
                speRecentStopsEditor.commit();
            }
        }).start();

    } // writeRecentTrips

    class CurrentLayoutData {
        int miStartStopId;
        ArrayList<CBus> aBusesAtStop;
        String sBusesToStation;
        TableLayout mTableLayout;

        public CurrentLayoutData() {
            sBusesToStation = "";
            aBusesAtStop = new ArrayList<CBus>();
        }
    }

    class Trip {
        CStop oStartStop, oDestStop;
        String msTrip;

        Trip(CStop startstop, CStop deststop, int iHourOfDay, int iMin) {
            oStartStop = startstop;
            oDestStop = deststop;
            init();
        }

        Trip(int startstopid, int ideststopid) {
            oStartStop = new CStop();
            oDestStop = new CStop();
            oStartStop = mApp.mDBHelperBus.getStopFromId(startstopid);
            oDestStop = mApp.mDBHelperBus.getStopFromId(ideststopid);
            init();
        }

        private void init() {
            msTrip = oStartStop.msStopNameDetail + "\n - " + oDestStop.msStopNameDetail;

        }
    }

    private class getBusesToNearestStation extends AsyncTask<String, Integer, String> {
        private String sBusesToStation;

        @Override
        protected String doInBackground(String... params) {
            try {
                Calendar c = Calendar.getInstance();
                int dow = mApp.dow(c.get(Calendar.DAY_OF_WEEK));
                double tm = c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE) / 100.00;
//			if(mApp.mbAutoRefreshLocation && loc != null) { // Nearest station from current location
//				sBusesToStation = mApp.mDBHelper.getBusesToStation(loc.getLatitude(),
//						loc.getLongitude(),  dow, tm);
//			} else { // nearest station from selected station
                if (mApp.moStartStop.lat > 0 && mApp.moStartStop.lon > 0)
                    sBusesToStation = mApp.mDBHelperBus.getBusesToStation(mApp.moStartStop.lat,
                            mApp.moStartStop.lon, dow, tm);
//			}
                publishProgress(30);
            } catch (Exception e) {
                SSLog.e(TAG, "BusesToNearestStation: ", e);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            miProgress += progress[0];
            mProgressBar.setProgress(miProgress);
        }

        @Override
        protected void onPreExecute() {
            mTvBusesToStation.setText("Getting buses to nearest station ...");
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            mTvBusesToStation.setText(sBusesToStation);
            layoutDone();
            super.onPostExecute(result);
        }
    } // getBusesToNearestStation

    private class getBusFrequencies extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                int sz = maBusesAtStop.size();
                int i = 1;
                for (CBus b : maBusesAtStop) {
                    b = mApp.mDBHelperBus.getFrequency(b, mDoW);
                    publishProgress(i * 40 / sz);
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG + " GetBusesAtStop.DoInBackground - ", e.getMessage());
            }
            publishProgress(5);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            miProgress += progress[0];
            mProgressBar.setProgress(miProgress);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                displayBuses(maBusesAtStop);
                layoutDone();
            } catch (Exception e) {
                SSLog.e(TAG, " GetBusesAtStop.DoInBackground - ", e.getMessage());
            }
            super.onPostExecute(result);
        }
    } // getBusFrequencies

    private class GetBusesAtStop extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... sUrl) {
            try {
                Calendar c = Calendar.getInstance();
                int dow = mApp.dow(c.get(Calendar.DAY_OF_WEEK));
                if (isCancelled())
                    return "Canceled";
                publishProgress(15);

                maBusesAtStop.clear();
                double tm = c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE) / 100.00;

                maBusesAtStop = mApp.mDBHelperBus.getBusesNearYou(
                        mApp.moStartStop.miStopId, dow, tm);
                publishProgress(20);

            } catch (Exception e) {
                SSLog.e(TAG, " GetBusesAtStop: OnPostExecute - ", e.getMessage());
            }
            return "Success";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            miProgress += progress[0];
            mProgressBar.setProgress(miProgress);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                new getBusFrequencies().execute("");
                new GetBusesETA().execute("");
                displayBuses(maBusesAtStop);
                layoutDone();
            } catch (Exception e) {
                SSLog.e(TAG, " GetBusesAtStop: OnPostExecute - ", e.getMessage());
            }
            super.onPostExecute(result);
        }
    } // GetBusesAtStop

    private class GetBusesETA extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                int sz = maBusesAtStop.size();
                int i = 1;
                for (CBus b : maBusesAtStop) {
                    busUpEta(b, (i == sz ? true : false));
                    publishProgress(i * 40 / sz);
                    i++;
                }
            } catch (Exception e) {
                SSLog.e(TAG, " GetBusesAtStop.DoInBackground - ", e.getMessage());
            }
            publishProgress(5);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            miProgress += progress[0];
            mProgressBar.setProgress(miProgress);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                displayBuses(maBusesAtStop);
                layoutDone();
            } catch (Exception e) {
                SSLog.e(TAG, " GetBusesAtStop: OnPostExecute - ", e.getMessage());
            }
            super.onPostExecute(result);
        }
    } // GetBusesAtStop

    private void busUpEta(final CBus cBus, final boolean isLastBus) {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_bus.GET_BUS_LOCATION_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        boolean skipRow = false;
                        System.out.println("succ" + response.toString());
                        if (TextUtils.isEmpty(response.trim())) {
                            Toast.makeText(ActFindBus.this, "No ETA", Toast.LENGTH_LONG).show();
                            skipRow = true;
                        }
                        if (response.trim().equals("-1")) {
                            Toast.makeText(ActFindBus.this, "No ETA", Toast.LENGTH_LONG).show();
                            skipRow = true;
                        }
                        if (!skipRow) {
                            try {
                                JSONArray aJson = new JSONArray(response.trim());
                                for (int i = 0; i < aJson.length(); i++) {
                                    JSONObject person = (JSONObject) aJson.get(i);
                                    busCurrentLat = person.isNull("lat") ? -999 : person
                                            .getDouble("lat");
                                    busCurrentLng = person.isNull("lng") ? -999 : person
                                            .getDouble("lng");
                                    busCLable = person.isNull("buslabel") ? "" : person
                                            .getString("buslabel");
                                    fromStopSerial = person.isNull("fromstopserial") ? 0 : person
                                            .getInt("fromstopserial");
                                    toStopSerial = person.isNull("tostopserial") ? 0 : person
                                            .getInt("tostopserial");
                                    sLastAccess = person.isNull("clientaccessdatetime") ? ""
                                            : person.getString("clientaccessdatetime");
                                    speed = person.isNull("speed") ? -999 : person.getDouble("speed");
                                    tripstatus = person.isNull("tripstatus") ? "" : person
                                            .getString("tripstatus");
                                    sDirection = person.isNull("direction") ? "" : person.getString("direction");
                                    try {
                                        if (tripstatus.equals("B")) {
                                            if (sDirection.equals("U")) {
                                                iMyDistanceUp = CGlobals_BA.getInstance().mDBHelperBus.
                                                        getNearCurrentBusStopsUp(busCurrentLat,
                                                                busCurrentLng, busCLable,
                                                                fromStopSerial, toStopSerial, iCurrentStopCode, sDirection);
                                                cBus.setMyDistanceBusUP(iMyDistanceUp);
                                            }
                                            if (sDirection.equals("D")) {
                                                iMyDistanceDown = CGlobals_BA.getInstance().mDBHelperBus.
                                                        getNearCurrentBusStopsUp(busCurrentLat,
                                                                busCurrentLng, busCLable,
                                                                fromStopSerial, toStopSerial, iCurrentStopCode, sDirection);
                                                cBus.setMyDistanceBusDOWN(iMyDistanceDown);
                                            }
                                        }
                                        cBus.setSpeed(speed);
                                        cBus.setLastAccess(sLastAccess);

                                    } catch (Exception e) {
                                        Toast.makeText(ActFindBus.this, "No ETA", Toast.LENGTH_LONG).show();
                                        e.printStackTrace();
                                    }
                                }
                            } catch (Exception e) {
                                Toast.makeText(ActFindBus.this, "No ETA", Toast.LENGTH_LONG).show();
                                SSLog_SS.e(TAG + " busLocation", e.getMessage());
                            }
                        }
                        if (isLastBus) {
                            displayBuses(maBusesAtStop);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    SSLog_SS.e(TAG + " callBusTrip", error.toString());
                } catch (Exception e) {
                    SSLog_SS.e(TAG + " callBusTrip", e.getMessage());
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params.put("buslabel", cBus.msBusLabel);
                params = CGlobals_lib_ss.getInstance().getBasicMobileParamsShort(params,
                        Constants_bus.GET_BUS_LOCATION_URL, ActFindBus.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_bus.GET_BUS_LOCATION_URL;
                try {
                    String url = url1 + "?" + getParams.toString()
                            + "&verbose=Y";
                    System.out.println("url  " + url);

                } catch (Exception e) {
                    SSLog_SS.e(TAG + " callBusTrip", e.getMessage());
                }
                return CGlobals_BA.getInstance().checkParams(params);
            }
        };
        CGlobals_BA.getInstance().getRequestQueue(ActFindBus.this).add(postRequest);
    }

} // ActFindBus
