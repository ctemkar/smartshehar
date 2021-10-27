package com.smartshehar.android.app.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.smartshehar.android.app.CGlobals_trains;
import com.smartshehar.android.app.Station;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;

import java.util.ArrayList;
import java.util.TimeZone;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.CTime;
import lib.app.util.ui.SSActivity;

public class ActTrainTrip extends SSActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    GoogleApiClient mGoogleApiClient = null;
    static boolean active = false;
    private static String TAG = "ActTrainTrip----- ";
    private ProgressDialog m_ProgressDialog = null;
    private ArrayList<Station> mRouteStations = null;
    private TrainTripAdapter m_adapter;
    Station moStart, moDest;
    int miTrainId; // 1st bus and connecting bus (connecting TBD)
    int miStartStationId, miDestStationId, miConnStationId;
    Station moStation;
    StringBuilder sTxt;
    private ListView tripStationsList;
    private TextView mtvFromTo, mtvChangeTrain;
    private TextView nextStation;
    int iNearestStationIndex = 0;
    float[] results = new float[1]; // To get the dist from the
    float dist;
    int nDir;
    Location mLocation;
    protected InitTask _initTask; // background task to get bus stops info
    double mdLatPrv = 0, mdLonPrv = 0; // The lat long of the previous stop
    CGlobals_trains mApp;
    private int miTrainId1, miTrainId2;
    private String msLineCode1;
    private String msLineCode2;
    private String msConnStationName;
    private Spanned msChangeTrain;
    private boolean mbDirectOnly;
    private String msPlatform2;
    TextView txtvGpsMsg;
    String provider;
    boolean showReshresDia;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setHomeButtonEnabled(true);
        ab.setTitle(getString(R.string.trip));
        mApp = CGlobals_trains.getInstance();
        mApp.init(this);
        SSLog.i(this.toString(), ".onCreate");
        mApp.mCH.userPing(getString(R.string.pageTrip), "");
        setContentView(R.layout.acttraintrip);
        setupUI();
        try {
            init();
        } catch (Exception e) {
            Log.d(TAG, "oncreate " + e.toString());

        }

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                Log.d(TAG, "CHANGING LOCATION " + location.getLatitude());
                updateMyLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                SSLog.i(TAG + " onStatusChanged - ", provider);
            }

            @Override
            public void onProviderEnabled(String provider) {
                SSLog.i(TAG + " onProviderEnabled", provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                SSLog.i(TAG + " onProviderDisabled", provider);
            }
        };
        // The minimum time between updates in milliseconds
        try {
            long MIN_TIME_BW_UPDATES = 1000 * 60;
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, 0, locationListener);

        } catch (Exception e) {

            SSLog.e(TAG, "onCreate", e.getMessage());
        }
        mGoogleApiClient = new GoogleApiClient.Builder(ActTrainTrip.this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(ActTrainTrip.this)
                .addOnConnectionFailedListener(ActTrainTrip.this)
                .build();
        mGoogleApiClient.connect();
    } // eofn: create

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case CGlobals_trains.REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        Log.d(TAG, "YES");
                        Toast.makeText(this, "Getting location...", Toast.LENGTH_SHORT).show();
                        txtvGpsMsg.setVisibility(View.GONE);
                        mApp.mCurrentLocation= CGlobals_lib_ss.getInstance().getMyLocation(ActTrainTrip.this);
//                        ...
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Log.d(TAG, "NO");

                        break;
                    default:
                        break;
                }
                break;
        }
    }


    void setupUI() {
        try {
            mApp = CGlobals_trains.getInstance();
            tripStationsList = (ListView) findViewById(R.id.jlist);
            mtvFromTo = (TextView) findViewById(R.id.tvFromTo);
            mtvChangeTrain = (TextView) findViewById(R.id.changeTrain);
            nextStation = (TextView) findViewById(R.id.nextStation);
            txtvGpsMsg = (TextView) findViewById(R.id.txtvGpsMsg);
            nDir = 1;
            registerForContextMenu(tripStationsList);
            txtvGpsMsg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!provider.contains("gps")) { //if gps is disabled
                        try {
                            CGlobals_trains.getInstance().turnGPSOn(ActTrainTrip.this, mGoogleApiClient);
                        } catch (Exception e) {
                            Log.d(TAG, "tvGpsMsg onClick " + e.toString());
                            e.printStackTrace();
                        }

                    }
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "setupUI " + e.toString());
        }

    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    protected class InitTask extends AsyncTask<Context, Integer, String> {
        @Override
        protected String doInBackground(Context... params) {
            try {
                mRouteStations.clear();
                Station firstLegStation;
                if (mbDirectOnly || miConnStationId == 0) {
                    firstLegStation = getRouteStops(miTrainId1, miStartStationId, miDestStationId);
                    if (firstLegStation == null)
                        return "FALSE";
                } else {
                    firstLegStation = getRouteStops(miTrainId1, miStartStationId, miConnStationId);
                    Log.d(TAG,"firstLegStation "+firstLegStation);
                }
                if (!mbDirectOnly && miConnStationId > 0) {
                    // Station secondLegStation = ;
                    getRouteStops(miTrainId2, miConnStationId, miDestStationId);
                    String goToPlatform = TextUtils.isEmpty(msPlatform2) ? "" : " Plat. <b>" + msPlatform2 + "</b>";
                    String sChangeLine = msLineCode1.endsWith(msLineCode2) ? "" : ", Go to <b>" + msLineCode2 + "</b>";
                    msChangeTrain = Html.fromHtml("Change at " + msConnStationName + sChangeLine + goToPlatform);
                }
            } catch (Exception e) {
                Log.d(TAG, "InitTask doInBackground " + e.toString());

            }
            return "COMPLETE!";
        }

        @Override
        protected void onPreExecute() {
            // Log.i( DBHelper.TAG, "onPreExecute()" );
            super.onPreExecute();

        }

        // -- called from the publish progress
        // -- notice that the datatype of the second param gets passed to this
        // method
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            // Log.i( DBHelper.TAG, "onProgressUpdate(): " + String.valueOf(
            // values[0] ) );
            // _percentField.setText( ( values[0] * 2 ) + "%");
            // _percentField.setTextSize( values[0] );
        }

        // -- called if the cancel button is pressed
        @Override
        protected void onCancelled() {
            super.onCancelled();
            // Log.i( DBHelper.TAG, "onCancelled()" );
            // _percentField.setText( "Cancelled!" );
            // _percentField.setTextColor( 0xFFFF0000 );
        }

        // -- called as soon as doInBackground method completes
        // -- notice that the third param gets passed to this method
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                refreshListView();
                m_adapter.notifyDataSetChanged();
                if (!mbDirectOnly) {
                    mtvChangeTrain.setVisibility(View.VISIBLE);
                    mtvChangeTrain.setText(msChangeTrain);
                } else {
                    mtvChangeTrain.setVisibility(View.GONE);
                }

                m_ProgressDialog.dismiss();
            } catch (Exception e) {
                Log.d(TAG, "onPostExecute " + e.toString());
            }
        }
    }

    // eoc: inittask for running background activities


    public void updateMyLocation(final Location loc) {
        // Update your current location
        try {
            if (m_adapter != null)
                refreshListView();
        } catch (Exception e) {
            Log.d(TAG, "updateMyLocation " + e.toString());
        }

    }

    private void refreshListView() {
        try {
            mApp.mCurrentLocation= CGlobals_lib_ss.getInstance().getMyLocation(ActTrainTrip.this);
            int nShortestDist = -1;
            boolean bFirstTime = true;
            if (m_adapter == null)
                return;
            if (m_adapter.getCount() > 0)
                bFirstTime = false;
            iNearestStationIndex = 0;
            if (mRouteStations != null && mRouteStations.size() > 0) {
                for (int i = 0; i < mRouteStations.size(); i++) {
                    moStation = mRouteStations.get(i);
                    if (mApp.mCurrentLocation != null && mApp.inCityBoundingRect(moStation)
                            && !mApp.mCurrentLocation.getProvider().equals("fake")) {
                        Location.distanceBetween(moStation.mdLat, moStation.mdLon,
                                mApp.mCurrentLocation.getLatitude(),
                                mApp.mCurrentLocation.getLongitude(), results);
                        dist = results[0];
                    } else
                        dist = -1;
                    moStation.dDist = dist;
                    if (dist != -1) {
                        if ((int) dist < nShortestDist || nShortestDist == -1) {
                            nShortestDist = (int) dist;
                            iNearestStationIndex = i;
                        }
                    }
                    if (!bFirstTime) {
                        Station osi =  m_adapter.getItem(i);
                        osi.dDist = dist;
                    } else
                        m_adapter.add(moStation);
                }
                if (iNearestStationIndex <= mRouteStations.size()) {
                    moStation = mRouteStations.get(iNearestStationIndex);
                }
                if (mApp.mCurrentLocation != null) {
                    Time time = new Time("UTC");
                    time.set(mApp.mCurrentLocation.getTime());
                    time.switchTimezone(TimeZone.getDefault().getID());
                }
                int len = mRouteStations.size();
                int iNextStationIndex = -1;
                Station moNextStation = null;
                if (iNearestStationIndex <= len)
                    iNextStationIndex = iNearestStationIndex;
                if (iNearestStationIndex < mRouteStations.size() - 1) {// Not At dest station
                    float[] res = new float[1];
                    if (mApp.mOldLocation != null) {
                        Location.distanceBetween(moStation.mdLat, moStation.mdLon,
                                mApp.mOldLocation.getLatitude(),
                                mApp.mOldLocation.getLongitude(), res);
                        if (moStation.dDist - res[0] >= 0) {// Moving away from this station
                            iNextStationIndex = iNearestStationIndex + 1;
                        }
                    }
                }
                if (iNextStationIndex < len)
                    moNextStation = mRouteStations.get(iNextStationIndex);
                if (moNextStation != null)
                    nextStation.setText("Next Station: " + moNextStation.msStationName);
            }
            if (moStation != null)
                m_adapter = null;
            ArrayList<Station> aStations = new ArrayList<>();
            aStations.addAll(mRouteStations);
            m_adapter = new TrainTripAdapter(this, R.layout.stationrow, aStations);

            tripStationsList.setAdapter(this.m_adapter);

            tripStationsList.setSelection(iNearestStationIndex);
            m_adapter.notifyDataSetChanged();
        } catch (Exception e) {
            if (!showReshresDia) {
                refreshAlertBox();
                showReshresDia = true;
            }

            Log.d(TAG, "refreshListView " + e.toString());
        }
    } // refreshListView


    @Override
    public void onPause() {
        // Unregister the LocationListener to stop updating the
        // GUI when the Activity isn't visible.
        if (m_ProgressDialog != null)
            m_ProgressDialog.dismiss();

        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
        //Get an Analytics tracker to report app starts & uncaught exceptions etc.
//		GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
        //Stop the analytics tracking
//		GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    public void onResume() {
//        mylocation.getLocation(this, locationResult);
//        CGlobals_trains.getInstance().turnGPSOn(ActTrainTrip.this, mGoogleApiClient);
        turnOnGps();
        mApp.mCurrentLocation= CGlobals_lib_ss.getInstance().getMyLocation(ActTrainTrip.this);
        super.onResume();
    }

    private void turnOnGps() {
        provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (!provider.contains("gps")) {
            txtvGpsMsg.setVisibility(View.VISIBLE);
        } else {
            txtvGpsMsg.setVisibility(View.GONE);
            if (mbAutoLocation) {

//                Toast.makeText(this, "Getting Location ... ", Toast.LENGTH_SHORT).show();

                mbAutoLocation = true;
                Location location = mApp.getBestLocation(ActTrainTrip.this);
                updateMyLocation(location);

            }
        }
    }

    void init() {
        try {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                return;
            }
            miTrainId1 = extras.getInt("trainId1");
            miTrainId = miTrainId1;
            miTrainId2 = extras.getInt("trainId2");
            String msTrainNo1 = extras.getString("trainNo1");
            String msTrainNo2 = extras.getString("trainNo2");
            miStartStationId = extras.getInt("startStationId");
            miDestStationId = extras.getInt("destStationId");
            miConnStationId = extras.getInt("connStationId");
            msConnStationName = extras.getString("connStationName");
            msLineCode1 = extras.getString("lineCode1");
            msLineCode2 = extras.getString("lineCode2");
            double mdConnTime2 = extras.getDouble("connTime2");
            msPlatform2 = extras.getString("platform2");
            mbDirectOnly = extras.getBoolean("directOnly");
            if (mdConnTime2 == 0)
                mbDirectOnly = true;
            if (miTrainId1 > 0) {
                mLocation = null;
                mApp.mCurrentLocation= CGlobals_lib_ss.getInstance().getMyLocation(ActTrainTrip.this);
                if (miStartStationId == -1) {
                    moStart = CGlobals_trains.mDBHelper.getFirstStation(miTrainId);
                    miStartStationId = moStart.miStationId;
                } else {
                    moStart = CGlobals_trains.mDBHelper.getStationFromId(miStartStationId);
                }
                if (miDestStationId == -1) {
                    moDest = CGlobals_trains.mDBHelper.getLastStation(miTrainId);
                    miDestStationId = moDest.miStationId;
                } else {
                    moDest = CGlobals_trains.mDBHelper.getStationFromId(miDestStationId);
                }
                String sTrip = moStart.msStationName + " (" + msLineCode1 + ")" + " - " + moDest.msStationName;
                if (mbDirectOnly)
                    sTrip += " (" + msLineCode1 + ")" +
                            (!TextUtils.isEmpty(msTrainNo1) ? " - " + msTrainNo1 : "");
                else
                    sTrip += " (" + msLineCode2 + ")" +
                            (!TextUtils.isEmpty(msTrainNo1) ? " - " + msTrainNo2 : "");

                if (moStart != null)
                    miStartStationId = moStart.miStationId;
                if (moDest != null)
                    miDestStationId = moDest.miStationId;
                mtvFromTo.setText(sTrip);
                mRouteStations = new ArrayList<>();
                SSLog.i("Journey: Start - " + miStartStationId, ", End - " + miDestStationId);

                sTxt = new StringBuilder();
                this.m_adapter = null;
                ArrayList<Station> aStations = new ArrayList<>();
                aStations.addAll(mRouteStations);
                this.m_adapter = new TrainTripAdapter(this, R.layout.stationrow, aStations);
                tripStationsList.setAdapter(this.m_adapter);
                _initTask = new InitTask();
                m_ProgressDialog = ProgressDialog.show(ActTrainTrip.this,
                        "Retrieving your train trip ..",
                        mtvFromTo.getText().toString(), true);
                _initTask.execute(this);
                // Unregister the LocationListener to stop updating the
                // GUI when the Activity isn't visible.
                //		locationManager.removeUpdates(locationListener);
            }
        } catch (Exception e) {

            Log.d(TAG, "init " + e.toString());
        }
    } // init


    private Station getRouteStops(int iTrainId, int iStartStopId, int iDestStopId) {
        Station station = null;
        try {
            if (iTrainId > 0) {
                if (mApp.mCurrentLocation != null) {
                    Log.d(TAG,"location not null");
                }
                ArrayList<Station> routeStations ;

                routeStations = CGlobals_trains.mDBHelper.getTrainTrip(iTrainId,
                        iStartStopId, iDestStopId);
                if (routeStations == null)
                    return null;
                int i = 0;
                for (Station st : routeStations) {
                    mRouteStations.add(st);
                    if (i == 0)
                        station = st;
                    i++;
                }
            } else {
                SSLog.e("TrainTrip - ", "getRouteStops", "Invalid Train Id");
            }
            Log.i("ARRAY", "" + mRouteStations.size());
        } catch (Exception e) {

            SSLog.e("TrainTrip", "getRouteStops", e.getMessage());
            Log.d(TAG, "getRouteStops" + e.getMessage());
        }
        return station; // Return the last station for this leg for information about first station, last
    }


    private class TrainTripAdapter extends ArrayAdapter<Station> {

        private ArrayList<Station> items;
        Station si;

       /* public TrainTripAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }*/

        public TrainTripAdapter(Context context, int textViewResourceId,
                                ArrayList<Station> aRStations) {
            super(context, textViewResourceId, aRStations);
            this.items = aRStations;
            Log.d(TAG, "1");
        }

//        String tempLineCode = "";

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.stationrow, parent, false);
            }
            try {
                si = items.get(position);
                dist = 0;
                if (mApp.mCurrentLocation != null) {
                    Location.distanceBetween(si.mdLat, si.mdLon,
                            mApp.mCurrentLocation.getLatitude(),
                            mApp.mCurrentLocation.getLongitude(), results);
                    dist = results[0];
                }
                if (mdLatPrv != 0 && mdLonPrv != 0 && si.mdLat != 0 && si.mdLon != 0
                        && mApp.inCityBoundingRect(si)) {
                    Location.distanceBetween(si.mdLat, si.mdLon, mdLatPrv, mdLonPrv,
                            results);
                }
                mdLatPrv = si.mdLat;
                mdLonPrv = si.mdLon;
                if (si != null) {
                    CardView ll = (CardView) v.findViewById(R.id.llStationRow);
                    TextView tvTrainTime = (TextView) v.findViewById(R.id.traintime);
                    TextView tvIndicatorSpeed = (TextView) v.findViewById(R.id.indicatorspeed);
                    TextView tvStation = (TextView) v.findViewById(R.id.station);
                    TextView tvPlatformNo = (TextView) v.findViewById(R.id.platformno);
                    TextView tvPlatformSide = (TextView) v.findViewById(R.id.platformside);
                    TextView tvLinecode = (TextView) v.findViewById(R.id.linecode);
                    tvIndicatorSpeed.setText(si.msIndicatorSpeedCode);
                    tvStation.setText(si.msStationName);
                    tvPlatformNo.setText(si.msPlatform);
                    tvPlatformSide.setText(si.msPlatformSide);
//                    if(!tempLineCode.equalsIgnoreCase(si.msLine))
//                    {
                    tvLinecode.setText(si.msLine);
//                        tempLineCode = si.msLine;
//                    }
                    if (tvTrainTime != null) {
                        tvTrainTime.setText(new CTime(si.miTrainTimeMin).msTm);
                        if (iNearestStationIndex == position) {
                            ll.setCardBackgroundColor(getResources().getColor(R.color.lightcyan));
                        } else {
                            ll.setCardBackgroundColor(getResources().getColor(R.color.white));
                            if (si.miStationId == miConnStationId)
                                ll.setCardBackgroundColor(getResources().getColor(R.color.lightyellow));
                            else
                                ll.setCardBackgroundColor(getResources().getColor(R.color.white));
                        }
                    }
                }
            } catch (Exception e) {

                SSLog.e("TrainTrip: ", "getView - ", e.getMessage());
                Log.d(TAG, "getView " + e.toString());
            }

            return v;
        }
    }

    @Override
    public void finish() {
        if (mApp.stackActivity.size() > 0)
            mApp.tabhost.setCurrentTab(mApp.stackActivity.pop());
        if (mApp.stackActivity.size() > 0)
            mApp.tabhost.setCurrentTab(mApp.stackActivity.pop());
        else
            super.finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            setContentView(R.layout.acttraintrip);
            setupUI();
            if (m_ProgressDialog != null)
                m_ProgressDialog.dismiss();
            init();
        } catch (Exception e) {

            Log.d(TAG, "onConfigurationChanged " + e.toString());
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menutrainhome, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_location) {
            CGlobals_trains.getInstance().turnGPSOn(ActTrainTrip.this, mGoogleApiClient);

            mApp.mCurrentLocation= CGlobals_lib_ss.getInstance().getMyLocation(ActTrainTrip.this);

        } else if (item.getItemId() == R.id.menu_refresh) {
            Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show();
            getWindow().getDecorView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    init();
                }
            }, 500);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
//        savedInstanceState.putString("",);
    }

    private void refreshAlertBox() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActTrainTrip.this);
        alertDialogBuilder.setMessage(R.string.refreshscreen);
        alertDialogBuilder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Toast.makeText(ActTrainTrip.this, "Refreshing...", Toast.LENGTH_SHORT).show();
                        getWindow().getDecorView().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                showReshresDia = false;
                                init();

                            }
                        }, 500);
                    }
                });
        alertDialogBuilder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        showReshresDia = false;
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
} // Acivity
