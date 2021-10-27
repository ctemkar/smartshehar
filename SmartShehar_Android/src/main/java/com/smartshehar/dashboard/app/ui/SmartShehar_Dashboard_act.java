package com.smartshehar.dashboard.app.ui;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartshehar.android.app.CGlobals_trains;
import com.smartshehar.android.app.Station;
import com.smartshehar.android.app.ui.ActTrainDashboard;
import com.smartshehar.dashboard.app.ActionBarAdapter;
import com.smartshehar.dashboard.app.BootUpReceiver;
import com.smartshehar.dashboard.app.CFare;
import com.smartshehar.dashboard.app.CFareParams;
import com.smartshehar.dashboard.app.CGlobals_db;
import com.smartshehar.dashboard.app.CNearestStation;
import com.smartshehar.dashboard.app.Constants_dp;
import com.smartshehar.dashboard.app.DataBaseHandler;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSApp;
import com.smartshehar.dashboard.app.SSLog;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import in.bestbus.app.CGlobals_BA;
import in.bestbus.app.ui.ActFindBus;
import lib.app.util.CAddress;
import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;
import lib.app.util.Constants_lib_ss;
import lib.app.util.GeoHelper;
import lib.app.util.SSLog_SS;
import lib.app.util.SearchAddress_act;
import lib.app.util.UpgradeApp;
import lib.app.util.ui.ActFeedback;

public class SmartShehar_Dashboard_act extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private static String TAG = "SS_DB_Act: ";
    ProgressDialog mProgressDialog;
    Connectivity mConnectivity;
    GoogleApiClient mGoogleApiClient = null;
    DataBaseHandler db = null;
    private static float COLOR_TRAIN = BitmapDescriptorFactory.HUE_CYAN;
    Location mCurrentLocation;
    String mAppName;
    final public static String ONE_TIME = "onetime";
    TextView tvTrainOne, tvTrainTwo, tvTrainThree, tvBusOne, tvBusTwo, tvBusThree, txtGps;
    TextView txtCurrentaddr, txtAddress, tvAEstFare, tvTEstFare, txtNoconnection,
            txtTTIssue, txtNTIssues, txtTMIssues, txtNMIssues, txtTOIssues, txtNOIssues, txtMyTIssues, txtMyNIssues;
    TextView tv_other, tv_governance, tv_commuting;
    ImageView iv_commuting, iv_governance, iv_other, imgWarning,imgNoInternet;
    int mDist;
    String mCity;
    double mStationLat = -999, mStationLng = -999;
    private DrawerLayout mDrawerLayout;
    FrameLayout mDrawerPane;
    ListView mDrawerList;
    ArrayList<NavItem> mNavItems = new ArrayList<>();
    private ActionBarDrawerToggle mDrawerToggle;
    String provider;
    LinearLayout llGpsMsg, llJijo, llSafetyShield;
    LinearLayout ll_other, ll_governance, ll_commuting,
            llOthers, llCommuting, llGovernance, internetErr;
    boolean mAutoUpdate = true, mAddressAutoChangeFlag = true, mRefresh, internetFailed;
    private CGlobals_lib_ss mApp = null;
    // AddressResultReceiver mResultReceiver; //commented AddressResultReceiver //
    int iAppUserId;
    GeoHelper geoHelper;
    private static final long INTERVAL = 1000 * 60;
    LocationRequest mLocationRequest;
    int interval = 1000 * 5;
    Snackbar snackbar;
    private CoordinatorLayout coordinatorLayout;
    private SwipeRefreshLayout mSwipeRefresh;
    private RelativeLayout rlBus, rlAutoTaxi, rlTrain,
            rlComplaintList, rlDashboard;


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_act);
        init();

        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
                        mRefresh = true;
                        refresh();
                    }
                }
        );
        try {
            geoHelper = new GeoHelper();
            createLocationRequest();
            mGoogleApiClient = new GoogleApiClient.Builder(SmartShehar_Dashboard_act.this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addConnectionCallbacks(SmartShehar_Dashboard_act.this)
                    .addOnConnectionFailedListener(SmartShehar_Dashboard_act.this)
                    .build();
            mGoogleApiClient.connect();
            //   setupLocationListeners();
            //   mResultReceiver = new AddressResultReceiver(new Handler()); //commented AddressResultReceiver //
            doGotIt(false);
            mApp = CGlobals_lib_ss.getInstance();
            mApp.init(this);
            // For Bus App
            CGlobals_BA.getInstance().init(SmartShehar_Dashboard_act.this);
            CGlobals_trains.getInstance().init(SmartShehar_Dashboard_act.this);
            SSLog.setContext(getApplicationContext(), CGlobals_lib_ss.msGmail);
//            refreshh();
            mConnectivity = new Connectivity();
            iAppUserId = CGlobals_lib_ss.getInstance()
                    .getPersistentPreference(SmartShehar_Dashboard_act.this).getInt(Constants_lib_ss.PREF_APPUSERID, -1);
            db = new DataBaseHandler(SmartShehar_Dashboard_act.this);

            mCity = CGlobals_lib_ss.getInstance()
                    .getPersistentPreference(SmartShehar_Dashboard_act.this).getString(Constants_dp.PREF_GET_LAST_CITY_NAME, "Mumbai");
            txtCurrentaddr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAutoUpdate = false;
                    Intent intent = new Intent(SmartShehar_Dashboard_act.this, SearchAddress_act.class);
                    startActivityForResult(intent, Constants_lib_ss.FINDADDRESS_FROM);
                }
            });
            llGpsMsg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        CGlobals_db.getInstance(SmartShehar_Dashboard_act.this).turnGPSOn(SmartShehar_Dashboard_act.this, mGoogleApiClient);
                    } catch (Exception e) {
                        Log.d(TAG, "llGpsMsg onClick " + e.toString());
                        e.printStackTrace();
                    }
                }

            });

            // Drawer layout
            mNavItems.add(new NavItem("Commuting", getString(R.string.where_are_you_going), R.drawable.ic_commuting));
            mNavItems.add(new NavItem("Governance", getString(R.string.help_your_city), R.drawable.ic_governance));
            mNavItems.add(new NavItem("Other Apps", "Local Apps", R.drawable.ic_others));
            mNavItems.add(new NavItem("Setting", "Add User Name, Options ...", R.mipmap.ic_setting));
            mNavItems.add(new NavItem("Feedback", "Version: " + getString(R.string.appCode) + "-" + mApp.mPackageInfo.versionName, R.mipmap.ic_feedback));
            mNavItems.add(new NavItem("Share", " ", R.mipmap.ic_action_share));
            mNavItems.add(new NavItem("Map", " ", R.mipmap.ic_map));
            // mNavItems.add(new NavItem("Chat", " ", R.mipmap.ic_logo));

            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mDrawerPane = (FrameLayout) findViewById(R.id.drawerPane);
            mDrawerList = (ListView) findViewById(R.id.navList);
            ActionBarAdapter adapter = new ActionBarAdapter(SmartShehar_Dashboard_act.this, mNavItems);
            ActionBar ab = getSupportActionBar();
            assert ab != null;
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
            mDrawerList.setAdapter(adapter);
            mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        selectItemFromDrawer(position);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            mDrawerToggle = new ActionBarDrawerToggle(SmartShehar_Dashboard_act.this,
                    mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    Log.d(TAG, "onDraweropened: " + getTitle());
                    invalidateOptionsMenu();
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                    Log.d(TAG, "onDrawerClosed: " + getTitle());
                    invalidateOptionsMenu();
                }
            };

            mDrawerLayout.setDrawerListener(mDrawerToggle);
            SSApp.getInstance().getMyLocation();

        } catch (Exception e) {
            e.printStackTrace();
            SSLog.e(TAG, "onCreate: ", e);
        }

        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        boolean bFirstUse = prefs.getBoolean(getString(R.string.appTitle),
                false);

        if (!bFirstUse) {
            CGlobals_lib_ss.getInstance()
                    .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this)
                    .putBoolean(getString(R.string.appTitle), true).commit();
        }
        final PackageManager pm = getPackageManager();
        llJijo.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PackageManager packageManager = getApplicationContext().getPackageManager();
                        try {
                            mAppName = (String) packageManager.getApplicationLabel(packageManager.
                                    getApplicationInfo(SmartShehar_Dashboard_act.this.getPackageName(),
                                            PackageManager.GET_META_DATA));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String appPackageName = null;
                        if (mAppName.equalsIgnoreCase("α SmartShehar")) {
                            appPackageName = "com.jumpinjumpout.apk.alpha";
                        } else if (mAppName.equalsIgnoreCase("SmartShehar")) {
                            appPackageName = "com.jumpinjumpout.apk";
                        }

                        Intent appStartIntent = pm
                                .getLaunchIntentForPackage(appPackageName);
                        try {
                            if (null != appStartIntent) {
                                startActivity(appStartIntent);
                            } else {
                                assert appPackageName != null;
                                if (appPackageName.equalsIgnoreCase("com.jumpinjumpout.apk")) {
                                    appPackageName = "com.jumpinjumpout.apk";
                                    Intent marketIntent = new Intent(Intent.ACTION_VIEW,
                                            Uri.parse("market://details?id="
                                                    + appPackageName));
                                    marketIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                                            | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                                    startActivity(marketIntent);
                                } else {
                                    showSnackbar("Please install alpha version");
                                }
                            }
                        } catch (Exception e) {
                            SSLog.e(TAG, "onCreate", e);
                        }
                    }
                }
        );
        rlBus.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            Intent intent = new Intent(SmartShehar_Dashboard_act.this, ActFindBus.class);
                            startActivity(intent);
                        } catch (Exception e) {
                            Log.d(TAG, "rlBus: " + e);
                            e.printStackTrace();
                        }
                    }
                }
        );
        rlAutoTaxi.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(SmartShehar_Dashboard_act.this, ActAutoTaxiDashboard.class);
                        startActivity(intent);
                    }
                }
        );
        rlTrain.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            Intent intent = new Intent(SmartShehar_Dashboard_act.this, ActTrainDashboard.class);
                            intent.putExtra("appCode", getString(R.string.appCode));
                            startActivity(intent);
                        } catch (Exception e) {
                            Log.d(TAG, "rlTrain: " + e);
                            e.printStackTrace();
                        }
                    }
                }
        );
        llSafetyShield.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(SmartShehar_Dashboard_act.this, Act_SafetyShield_Dashboard.class);
                        startActivity(intent);
                    }
                }
        );
        rlComplaintList.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (iAppUserId != -1) {
                            Intent intent = new Intent(SmartShehar_Dashboard_act.this, MyComplaintList.class);
                            startActivity(intent);
                        } else {
                            AlertBox();
                        }
                    }
                }
        );
        rlDashboard.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Connectivity mConnectivity = new Connectivity();
                        if (!Connectivity.checkConnected(SmartShehar_Dashboard_act.this)) {
                            if (!mConnectivity.connectionError(SmartShehar_Dashboard_act.this)) {
                                Log.d(TAG, "Internet Connection");
                            }
                        } else {
                            Intent intent = new Intent(SmartShehar_Dashboard_act.this, ActDashboardReport.class);
                            startActivity(intent);
                        }
                    }
                }
        );
        imgWarning.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSnackbar(getString(R.string.not_register));
                    }
                }
        );
        imgNoInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSnackbar(getString(R.string.interneterr));
            }
        });
        ll_commuting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    txtNoconnection.setText(getString(R.string.interneterr));
                    iv_commuting.setImageResource(R.drawable.ic_commuting_blue);
                    iv_governance.setImageResource(R.drawable.ic_governance);
                    iv_other.setImageResource(R.drawable.ic_others);
                    tv_commuting.setTextColor(Color.BLUE);
                    tv_governance.setTextColor(Color.BLACK);
                    tv_other.setTextColor(Color.BLACK);
                    llCommuting.setVisibility(View.VISIBLE);
                    llGovernance.setVisibility(View.GONE);
                    llOthers.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                    SSLog_SS.e(TAG, "LocationResult", e, SmartShehar_Dashboard_act.this);
                }
            }
        });
        ll_governance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    txtNoconnection.setText(getString(R.string.interneterr) + " & issues near by you");
                    iv_commuting.setImageResource(R.drawable.ic_commuting);
                    iv_governance.setImageResource(R.drawable.ic_governance_blue);
                    iv_other.setImageResource(R.drawable.ic_others);
                    tv_commuting.setTextColor(Color.BLACK);
                    tv_governance.setTextColor(Color.BLUE);
                    tv_other.setTextColor(Color.BLACK);
                    llCommuting.setVisibility(View.GONE);
                    llGovernance.setVisibility(View.VISIBLE);
                    llOthers.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                    SSLog_SS.e(TAG, "LocationResult", e, SmartShehar_Dashboard_act.this);
                }
            }
        });
        ll_other.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    txtNoconnection.setText(getString(R.string.interneterr));
                    iv_commuting.setImageResource(R.drawable.ic_commuting);
                    iv_governance.setImageResource(R.drawable.ic_governance);
                    iv_other.setImageResource(R.drawable.ic_other_blue);
                    tv_commuting.setTextColor(Color.BLACK);
                    tv_governance.setTextColor(Color.BLACK);
                    tv_other.setTextColor(Color.BLUE);
                    llCommuting.setVisibility(View.GONE);
                    llGovernance.setVisibility(View.GONE);
                    llOthers.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                    SSLog_SS.e(TAG, "LocationResult", e, SmartShehar_Dashboard_act.this);
                }
            }
        });
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(SmartShehar_Dashboard_act.this, BootUpReceiver.class);
        intent.putExtra(ONE_TIME, Boolean.FALSE);
        PendingIntent pi = PendingIntent.getBroadcast(SmartShehar_Dashboard_act.this.getApplicationContext(), 0, intent, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pi);
        if (mApp != null)
            if (mApp.mPackageInfo != null)
                UpgradeApp.app_launched(this, mApp.mPackageInfo, CGlobals_lib_ss.mUserInfo,
                        getString(R.string.app_name),
                        getString(R.string.appNameShort), mApp.mPackageInfo.versionCode,
                        CGlobals_lib_ss.PHP_PATH);
    }

    private void init() {
        imgNoInternet  = (ImageView) findViewById(R.id.imgNoInternet);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        txtNoconnection = (TextView) findViewById(R.id.txtNoconnection);
        txtCurrentaddr = (TextView) findViewById(R.id.txtCurrentaddr);
        tvTrainOne = (TextView) findViewById(R.id.tvTrainOne);
        tvTrainTwo = (TextView) findViewById(R.id.tvTrainTwo);
        tvTrainThree = (TextView) findViewById(R.id.tvTrainThree);
        tvBusOne = (TextView) findViewById(R.id.tvBusOne);
        tvBusTwo = (TextView) findViewById(R.id.tvBusTwo);
        tvBusThree = (TextView) findViewById(R.id.tvBusThree);
        txtGps = (TextView) findViewById(R.id.txtGps);
        llGpsMsg = (LinearLayout) findViewById(R.id.llGpsMsg);
        txtAddress = (TextView) findViewById(R.id.txtAddress);
        tvAEstFare = (TextView) findViewById(R.id.tvAEstFare);
        tvTEstFare = (TextView) findViewById(R.id.tvTEstFare);
        txtTTIssue = (TextView) findViewById(R.id.txtTTIssue);
        txtNTIssues = (TextView) findViewById(R.id.txtNTIssues);
        txtTMIssues = (TextView) findViewById(R.id.txtTMIssues);
        txtNMIssues = (TextView) findViewById(R.id.txtNMIssues);
        txtTOIssues = (TextView) findViewById(R.id.txtTOIssues);
        txtNOIssues = (TextView) findViewById(R.id.txtNOIssues);
        txtMyTIssues = (TextView) findViewById(R.id.txtMyTIssues);
        txtMyNIssues = (TextView) findViewById(R.id.txtMyNIssues);
        iv_commuting = (ImageView) findViewById(R.id.iv_commuting);
        iv_governance = (ImageView) findViewById(R.id.iv_governance);
        iv_other = (ImageView) findViewById(R.id.iv_other);
        tv_commuting = (TextView) findViewById(R.id.tv_commuting);
        tv_governance = (TextView) findViewById(R.id.tv_governance);
        tv_other = (TextView) findViewById(R.id.tv_other);
        ll_commuting = (LinearLayout) findViewById(R.id.ll_commuting);
        ll_governance = (LinearLayout) findViewById(R.id.ll_governance);
        ll_other = (LinearLayout) findViewById(R.id.ll_other);
        llCommuting = (LinearLayout) findViewById(R.id.llCommuting);
        llGovernance = (LinearLayout) findViewById(R.id.llGovernance);
        llOthers = (LinearLayout) findViewById(R.id.llOthers);
        llJijo = (LinearLayout) findViewById(R.id.llJijo);
        internetErr = (LinearLayout) findViewById(R.id.internetErr);
        llSafetyShield = (LinearLayout) findViewById(R.id.llSafetyShield);
        rlBus = (RelativeLayout) findViewById(R.id.rlBus);
        rlAutoTaxi = (RelativeLayout) findViewById(R.id.rlAutoTaxi);
        rlTrain = (RelativeLayout) findViewById(R.id.rlTrain);
        rlComplaintList = (RelativeLayout) findViewById(R.id.rlComplaintList);
        rlDashboard = (RelativeLayout) findViewById(R.id.rlDashboard);
        imgWarning = (ImageView) findViewById(R.id.imgWarning);
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(SmartShehar_Dashboard_act.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(SmartShehar_Dashboard_act.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: " + pendingResult.toString());
    }

    private void doGotIt(boolean alwayaShow) {
        CGlobals_db.getInstance(this).gotIt(this,
                Constants_dp.PREF_GOTIT_ACTIVE_TRIPS, alwayaShow, getString(R.string.helplinetext1), "");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dashboard, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (id) {
            case R.id.menu_refreshGps:
                CGlobals_db.getInstance(SmartShehar_Dashboard_act.this).turnGPSOn(SmartShehar_Dashboard_act.this, mGoogleApiClient);
                refresh();
                return true;
            case R.id.menu_refresh:
                Log.i(TAG, "Refresh menu item selected");
                // Signal SwipeRefreshLayout to start the progress indicator
                mSwipeRefresh.setRefreshing(true);
                // Start the refresh background task.
                // This method calls setRefreshing(false) when it's finished.
                refresh();
                return true;
            case R.id.menu_notification:
                Intent intent = new Intent(this, ActNotification.class);
                startActivity(intent);
                return true;
            case R.id.menu_share:
                String message = getString(R.string.androidAppLink);
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(Intent.createChooser(share, "Share "
                        + getString(R.string.appTitle)));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void refresh() {
        mCurrentLocation = CGlobals_lib_ss.getInstance().getBestLocation(this);
        mCurrentLocation = CGlobals_lib_ss.getInstance().getMyLocation(this);
        if (mRefresh)
            CGlobals_lib_ss.getInstance().setMyLocation(mCurrentLocation, false, SmartShehar_Dashboard_act.this);
        mAddressAutoChangeFlag = true;
        if (mCurrentLocation != null) {
            txtCurrentaddr.setText(mCurrentLocation.getLatitude() + ", " + mCurrentLocation.getLongitude());
            geoHelper.getAddress(SmartShehar_Dashboard_act.this, mCurrentLocation, onGeoHelperResult);
            showNearTrainStation(mCurrentLocation);//2
            showNearBusStation(mCurrentLocation);
            showFare();
            getIssueCount(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        }
        mSwipeRefresh.setRefreshing(false);
        if (!Connectivity.checkConnected(SmartShehar_Dashboard_act.this)) {
            imgNoInternet.setVisibility(View.VISIBLE);
        }
    }

    private void refresh(Location location) {

        mCurrentLocation = location;
        txtCurrentaddr.setText(mCurrentLocation.getLatitude() + ", " + mCurrentLocation.getLongitude());
        mAddressAutoChangeFlag = true;
        if (mCurrentLocation != null) {
            geoHelper.getAddress(SmartShehar_Dashboard_act.this, mCurrentLocation, onGeoHelperResult);
            showNearTrainStation(mCurrentLocation);//2
            showNearBusStation(mCurrentLocation);
            showFare();
            getIssueCount(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        }
        mSwipeRefresh.setRefreshing(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String sAddr;
        CAddress oAddr;
        Location mLocation = new Location("LOCATION");
        switch (requestCode) {
            case CGlobals_db.REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK: {// Gps turned on
                        if (mGoogleApiClient != null)
                            mGoogleApiClient.connect();
                        startLocationUpdates();
                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        // The user was asked to change settings, but chose not to
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
        }
        if (requestCode == Constants_lib_ss.FINDADDRESS_FROM) {
            if (resultCode == Activity.RESULT_OK) {
                sAddr = data.getStringExtra("add");
                Type type = new TypeToken<CAddress>() {
                }.getType();
                oAddr = new Gson().fromJson(sAddr, type);
                if (oAddr.getLatitude() != Constants_lib_ss.INVALIDLAT
                        && oAddr.getLongitude() != Constants_lib_ss.INVALIDLNG) {
                    mLocation.setLatitude(oAddr.getLatitude());
                    mLocation.setLongitude(oAddr.getLongitude());
                    mCurrentLocation = mLocation;
                    getCity(oAddr.getLatitude(), oAddr.getLongitude());
                    if (!sAddr.equals("")) {
                        mAddressAutoChangeFlag = false;
                        if (!TextUtils.isEmpty(oAddr.getAddress())) {
                            txtAddress.setText(R.string.select_address);
                            txtCurrentaddr.setText(oAddr.getAddress());///requestCode == Constants_lib_ss.FINDADDRESS_FROM
                            CGlobals_lib_ss.getInstance()
                                    .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                                    putString(Constants_dp.PREF_GET_LAST_ADDRESS, oAddr.getAddress()).commit();
                        }
                    }
                    showNearTrainStation(mCurrentLocation);///3
                    showNearBusStation(mCurrentLocation);
                    showFare();
                }
            }
        }
    }

    public void clickMunicipal(View v) {
        if (iAppUserId != -1) {
            Intent intent = new Intent(this, MunicipalViolation_act.class);
            startActivity(intent);
        } else {

            AlertBox();
        }
    }

    public void clickTrafficViolation(View v) {
        if (iAppUserId != -1) {
            Intent intent = new Intent(this, TrafficViolation_act.class);
            startActivity(intent);
        } else {
            AlertBox();
        }
    }

    public void clickTrafficSite(View v) {
        Connectivity mConnectivity = new Connectivity();
        if (!Connectivity.checkConnected(SmartShehar_Dashboard_act.this)) {
            if (!mConnectivity.connectionError(SmartShehar_Dashboard_act.this)) {
                Log.d(TAG, "Internet Connection");
            }
        } else {
            Intent intent = new Intent(SmartShehar_Dashboard_act.this, ActDashboardReport.class);
            intent.putExtra("url", Constants_dp.TRAFFIC_REPROT_URL);
            startActivity(intent);
        }
    }

    public void clickMunicipalSite(View v) {
        Connectivity mConnectivity = new Connectivity();
        if (!Connectivity.checkConnected(SmartShehar_Dashboard_act.this)) {
            if (!mConnectivity.connectionError(SmartShehar_Dashboard_act.this)) {
                Log.d(TAG, "Internet Connection");
            }
        } else {
            Intent intent = new Intent(SmartShehar_Dashboard_act.this, ActDashboardReport.class);
            intent.putExtra("url", Constants_dp.MUNICIPAL_REPROT_URL);
            startActivity(intent);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.d(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!CGlobals_lib_ss.getInstance()
                .getPersistentPreference(SmartShehar_Dashboard_act.this).getBoolean(Constants_dp.PREF_USER_REGISTRATION, false)) {
            getAppUserDetails();
            imgWarning.setVisibility(View.VISIBLE);
        } else {
            imgWarning.setVisibility(View.GONE);
        }
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
        turnOnGps();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//        findViewById(R.id.internetErr).setVisibility(View.GONE);
    }

    private void AlertBox() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SmartShehar_Dashboard_act.this);
        alertDialogBuilder.setMessage(R.string.noappuserid);
        alertDialogBuilder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    }
                });
        alertDialogBuilder.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void turnOnGps() {
        provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (!provider.contains("gps")) {
            llGpsMsg.setVisibility(View.VISIBLE);
        } else {
            llGpsMsg.setVisibility(View.GONE);
        }
        if (mAutoUpdate)
            refresh();
    }

    private void showNearTrainStation(Location loc) {
        try {

            tvTrainOne.setText("");
            tvTrainTwo.setText("");
            tvTrainThree.setText("");
            ArrayList<CNearestStation> maStation;
            maStation = CGlobals_trains.mDBHelper.getNearestStation(loc.getLatitude(), loc.getLongitude());
            if (maStation == null || maStation.size() <= 0) {
                showNear("train_station|subway_station");
                return;
            }
            if (maStation.size() > 0) {
                String mCurrentPlaceName = "";
                for (int i = 0; i < maStation.size(); i++) {
                    if (i == 3) {
                        break;
                    }
                    if (mCurrentPlaceName.equals(maStation.get(i).getStation())) {
                        continue;
                    }
                    String stationName = maStation.get(i).getStation();
                    int index = stationName.indexOf("(");
                    stationName = stationName.substring(0, index);
                    mCurrentPlaceName = stationName + "-" + String.valueOf(maStation.get(i).getDistance()) + " km";
                    if (!TextUtils.isEmpty(mCurrentPlaceName)) {
                        if (i == 0) {
                            tvTrainOne.setText(Html.fromHtml(mCurrentPlaceName));
                            CGlobals_lib_ss.getInstance()
                                    .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                                    putString(Constants_dp.PREF_LAST_TRAIN_STATION_ONE, mCurrentPlaceName).commit();
                        }
                        if (i == 1) {
                            tvTrainTwo.setText(Html.fromHtml(mCurrentPlaceName));
                            CGlobals_lib_ss.getInstance()
                                    .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                                    putString(Constants_dp.PREF_LAST_TRAIN_STATION_TWO, mCurrentPlaceName).commit();
                        }
                        if (i == 2) {
                            tvTrainThree.setText(Html.fromHtml(mCurrentPlaceName));
                            CGlobals_lib_ss.getInstance()
                                    .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                                    putString(Constants_dp.PREF_LAST_TRAIN_STATION_THREE, mCurrentPlaceName).commit();
                        }
                    }
                }
            } else {
                showNear("train_station|subway_station");
            }
        } catch (Exception e) {
            e.printStackTrace();
            SSLog_SS.e(TAG, "showNearTrainStation" + e.toString());
        }
    }

    private void showNearBusStation(Location loc) {
        try {
            tvBusOne.setText("");
            tvBusTwo.setText("");
            tvBusThree.setText("");
            ArrayList<CNearestStation> maStation;
            maStation = CGlobals_BA.mDBHelperBus.getNearestBusStation(loc.getLatitude(), loc.getLongitude());
            if (maStation == null) {
                showNear("bus_station");
                return;
            }
            if (maStation.size() > 0) {
                String mCurrentPlaceName = "";
                for (int j = 0; j < maStation.size(); j++) {
                    if (j == 3) {
                        break;
                    }
                    if (mCurrentPlaceName.equals(maStation.get(j))) {
                        continue;
                    }
                    mCurrentPlaceName = maStation.get(j).getStation() + "-" + String.valueOf(maStation.get(j).getDistance()) + " km";
                    if (!TextUtils.isEmpty(mCurrentPlaceName)) {
                        if (j == 0) {
                            tvBusOne.setText(Html.fromHtml(mCurrentPlaceName));
                            CGlobals_lib_ss.getInstance()
                                    .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                                    putString(Constants_dp.PREF_LAST__BUS_STATION_ONE, mCurrentPlaceName).commit();
                        }
                        if (j == 1) {
                            tvBusTwo.setText(Html.fromHtml(mCurrentPlaceName));
                            CGlobals_lib_ss.getInstance()
                                    .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                                    putString(Constants_dp.PREF_LAST__BUS_STATION_TWO, mCurrentPlaceName).commit();
                        }
                        if (j == 2) {
                            tvBusThree.setText(Html.fromHtml(mCurrentPlaceName));
                            CGlobals_lib_ss.getInstance()
                                    .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                                    putString(Constants_dp.PREF_LAST__BUS_STATION_THREE, mCurrentPlaceName).commit();
                        }
                    }
                }
            } else {

                showNear("bus_station");
            }
        } catch (Exception e) {
            e.printStackTrace();
            SSLog_SS.e(TAG, "showNearBusStation" + e.toString());
        }
    }

    private void showFare() {
        tvAEstFare.setText(String.valueOf("--"));
        tvTEstFare.setText(String.valueOf("--"));
        new GetEstDist().execute();
    }

    private class GetEstDist extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... sUrl) {
            try {
                Station s;
                s = CGlobals_trains.mDBHelper.getNearestStation(mCurrentLocation.getLatitude(),
                        mCurrentLocation.getLongitude(), SmartShehar_Dashboard_act.this);
                if (s != null)
                    getEstimatedDistance(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                            s.mdLat, s.mdLon);
                else {
                    if (mStationLng != -999 && mStationLat != -999) {
                        getEstimatedDistance(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                                mStationLat, mStationLng);
                    }
                }
            } catch (Exception e) {
                SSLog.e(TAG, "TrainRoute: doInBackground - ", e.getMessage());
            }
            return null;
        } // doInBackground

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                ArrayList<CFareParams> maoFareParams = null;
                if (!TextUtils.isEmpty(mCity)) {
                    if (mCity.equalsIgnoreCase("Thane") || mCity.equalsIgnoreCase("Navi Mumbai"))
                        mCity = "Mumbai";
                    maoFareParams = CGlobals_db.mDBHelper.getCityFareParameters(mCity);
                }
                CFareParams moFareParams = new CFareParams();
                if (maoFareParams != null) {
                    if (maoFareParams.size() > 0) {
                        moFareParams.objClone(maoFareParams.get(0));
                        getFare(moFareParams, "AU");
                        if (maoFareParams.size() > 1) {
                            moFareParams.objClone(maoFareParams.get(1));
                            getFare(moFareParams, "TA");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                SSLog.e(TAG, "onPostExecute ", e);
            }
        }
    }

    private void getFare(CFareParams moFareParams, String vehicletype) {
        CFare cf = new CFare(moFareParams,
                CFareParams.METERTYPEDIGITAL,
                mDist, 0);
        Log.d(TAG, "CF " + cf.msFare);
        double d, dFare = moFareParams.fMinimumFare, dTotalFare;//// auto min day fare
        Time tm = new Time();
        tm.setToNow();
        int hours = tm.hour;
        dTotalFare = CGlobals_db.mEstimatedTotalDayFare;
        if (hours >= moFareParams.fNightStart && hours <= moFareParams.fNightEnd) {
            dFare = moFareParams.fMinimumNightFare;
            dTotalFare = CGlobals_db.mEstimatedToatalNightFare;
        }
        String stationName = tvTrainOne.getText().toString();
        int index = stationName.indexOf(" -");
        if (index >= 0)
            stationName = stationName.substring(0, index);

        if (vehicletype.equalsIgnoreCase("AU")) {
            d = Double.parseDouble(new DecimalFormat("###.##").format(dTotalFare));
            tvAEstFare.setText(getString(R.string.estafare) + " " + stationName + " Rs." + String.valueOf(Math.round(d)));
        } else {
            d = Double.parseDouble(new DecimalFormat("###.##").format(dTotalFare));
            tvTEstFare.setText(getString(R.string.esttfare) + " " + stationName + " Rs." + String.valueOf(Math.round(d)));
        }
    }

    private void getEstimatedDistance(double dStartLat, double dStartLon,
                                      double dDestLat, double dDestLon) {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        String sUrl = "http://maps.googleapis.com/maps/api/directions/json?origin=" +
                Double.toString(dStartLat) + "," + Double.toString(dStartLon) + "&destination=" +
                Double.toString(dDestLat) + "," + Double.toString(dDestLon) + "&sensor=false&mode=walking";
        HttpGet httpGet = new HttpGet(sUrl);
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                try {
                    JSONObject jo = new JSONObject(builder.toString());
                    JSONObject joVal;
                    JSONArray ja = jo.getJSONArray("routes");
                    jo = ja.getJSONObject(0);
                    ja = jo.getJSONArray("legs");
                    jo = ja.getJSONObject(0);
                    joVal = jo.getJSONObject("distance");
                    mDist = joVal.getInt("value");
                    Log.d(TAG, "mDist " + mDist);

                } catch (JSONException e) {
                    Log.e("log_tag", "Error parsing data " + e.toString());
                }
            } else {
                Log.d(TAG, "Failed to download file");
            }
        } catch (Exception e) {
            e.printStackTrace();
            SSLog.e(TAG, "ClientProtocolException ", e);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        boolean flag = CGlobals_lib_ss.isBetterLocation(location, mCurrentLocation);
        mCurrentLocation = CGlobals_lib_ss.isBetterLocation(location, mCurrentLocation) ? location
                : mCurrentLocation;
        CGlobals_lib_ss.getInstance().setMyLocation(mCurrentLocation, false, SmartShehar_Dashboard_act.this);
        if (mAutoUpdate)
            refresh(mCurrentLocation);
    }

    private GeoHelper.GeoHelperResult onGeoHelperResult = new GeoHelper.GeoHelperResult() {
        @Override
        public void gotAddress(CAddress addr) {
            if (addr != null) {
                if (addr.hasLatitude() || addr.hasLongitude()) {
                    try {
                        addr.getLatitude();
                        addr.getLongitude();
                        getCity(addr.getLatitude(), addr.getLongitude());
                        if (TextUtils.isEmpty(mCity))
                            mCity = addr.getCity();
                        Log.d(TAG, "City  " + mCity);
                        if (mAddressAutoChangeFlag) {
                            if (!TextUtils.isEmpty((addr.getAddress()))) {
                                txtCurrentaddr.setText(addr.getAddress()); /////GeoHelper
                                CGlobals_lib_ss.getInstance()
                                        .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                                        putString(Constants_dp.PREF_GET_LAST_ADDRESS, addr.getAddress()).commit();
                                Log.d(TAG, "GeoHelperResult");
                            }
                            CGlobals_db.mAddress = addr.getAddress();
                            CGlobals_db.mAddress = addr.getAddress();

                            CGlobals_lib_ss.getInstance()
                                    .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                                    putString(Constants_dp.PREF_GET_LAST_POSTAL_CODE, addr.getPostalCode()).commit();
                            CGlobals_lib_ss.getInstance()
                                    .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                                    putString(Constants_dp.PREF_GET_LAST_CITY_NAME, addr.getCity()).commit();
                            CGlobals_lib_ss.getInstance()
                                    .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                                    putString(Constants_dp.PREF_GET_LAST_SUBLOCALITY, addr.getSubLocality1()).commit();
                            CGlobals_lib_ss.getInstance()
                                    .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                                    putString(Constants_dp.PREF_GET_LAST_ROUTE, addr.getRoute()).commit();
                            CGlobals_lib_ss.getInstance()
                                    .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                                    putString(Constants_dp.PREF_GET_LAST_NEIGHBOURHOOD, addr.getMsNeighborhodd()).commit();
                            CGlobals_lib_ss.getInstance()
                                    .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                                    putString(Constants_dp.PREF_GET_LAST_ADMINISTRATIVE_1, addr.getAdminstraveArea1()).commit();
                            CGlobals_lib_ss.getInstance()
                                    .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                                    putString(Constants_dp.PREF_GET_LAST_ADMINISTRATIVE_2, addr.getAdminstrativeArea2()).commit();
                            CGlobals_lib_ss.getInstance()
                                    .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                                    putString(Constants_dp.PREF_GET_LAST_LAT, String.valueOf(addr.getLatitude())).commit();
                            CGlobals_lib_ss.getInstance()
                                    .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                                    putString(Constants_dp.PREF_GET_LAST_LNG, String.valueOf(addr.getLongitude())).commit();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                internetFailed = false;
                internetErr.setVisibility(View.GONE);
            } else {
                txtAddress.setText(R.string.your_location);
                llGpsMsg.setVisibility(View.GONE);
                internetErr.setVisibility(View.VISIBLE);
                internetFailed = true;
            }
        }
    };

    private void getCity(double lat, double lng) {
        try {
            Geocoder geocoder = new Geocoder(SmartShehar_Dashboard_act.this,
                    Locale.getDefault());
            List<Address> addresses;

            addresses = geocoder.getFromLocation(lat,
                    lng, 1);
            if (addresses.size() > 0) {
                String mLocality = addresses.get(0).getLocality();
                String mSubAdmin = addresses.get(0).getSubAdminArea();

                if (TextUtils.isEmpty(mLocality))
                    mCity = mSubAdmin;
                if (TextUtils.isEmpty(mSubAdmin))
                    mCity = mLocality;
                if (!TextUtils.isEmpty(mSubAdmin) && !TextUtils.isEmpty(mLocality))
                    mCity = mLocality;
                if (addresses.get(0).hasLatitude())
                    mStationLat = addresses.get(0).getLatitude();
                if (addresses.get(0).hasLongitude())
                    mStationLng = addresses.get(0).getLongitude();
            }
            if (mAddressAutoChangeFlag) {
                showFare();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void selectItemFromDrawer(int position) {
        mDrawerList.setItemChecked(position, true);
        switch (position) {
            case 0:
                iv_commuting.setImageResource(R.drawable.ic_commuting_blue);
                iv_governance.setImageResource(R.drawable.ic_governance);
                iv_other.setImageResource(R.drawable.ic_others);
                tv_commuting.setTextColor(Color.BLUE);
                tv_governance.setTextColor(Color.BLACK);
                tv_other.setTextColor(Color.BLACK);
                llCommuting.setVisibility(View.VISIBLE);
                llGovernance.setVisibility(View.GONE);
                llOthers.setVisibility(View.GONE);
                break;
            case 1:
                iv_commuting.setImageResource(R.drawable.ic_commuting);
                iv_governance.setImageResource(R.drawable.ic_governance_blue);
                iv_other.setImageResource(R.drawable.ic_others);
                tv_commuting.setTextColor(Color.BLACK);
                tv_governance.setTextColor(Color.BLUE);
                tv_other.setTextColor(Color.BLACK);
                llCommuting.setVisibility(View.GONE);
                llGovernance.setVisibility(View.VISIBLE);
                llOthers.setVisibility(View.GONE);
                break;
            case 2:
                iv_commuting.setImageResource(R.drawable.ic_commuting);
                iv_governance.setImageResource(R.drawable.ic_governance);
                iv_other.setImageResource(R.drawable.ic_other_blue);
                tv_commuting.setTextColor(Color.BLACK);
                tv_governance.setTextColor(Color.BLACK);
                tv_other.setTextColor(Color.BLUE);
                llCommuting.setVisibility(View.GONE);
                llGovernance.setVisibility(View.GONE);
                llOthers.setVisibility(View.VISIBLE);
                break;
            case 3:
                Intent intent1 = new Intent(SmartShehar_Dashboard_act.this,
                        ActUserRegistration.class); //
                startActivity(intent1);
                break;
            case 4:
                String smartshehar_Website = "http://www.smartshehar.com ";
                String smartshehar_Twitter = "https://twitter.com/smartshehar";
                String smartshehar_Facebook = "http://www.facebook.com/smartshehar ";
                String smartshehar_Feedback_email = "userfeedback@smartshehar.com";
                String smartshehar_Packagname = "market://details?id=" + getPackageName();
                Intent intent = new Intent(SmartShehar_Dashboard_act.this, ActFeedback.class);
                intent.putExtra("app_label", getString(R.string.appTitle));
                intent.putExtra("appCode", getString(R.string.appCode));
                intent.putExtra("versionName", mApp.mPackageInfo.versionName);
                intent.putExtra("USER_FEEDBACK_EMAIL", smartshehar_Feedback_email);
                intent.putExtra("FACEBOOK_POST", smartshehar_Facebook);
                intent.putExtra("TWITTER_POST", smartshehar_Twitter);
                intent.putExtra("WEB_SITE", smartshehar_Website);
                intent.putExtra("PACKAGE_NAME", smartshehar_Packagname);
                startActivity(intent);
                break;
            case 5:
                String message = getString(R.string.androidAppLink);
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(Intent.createChooser(share, "Share "
                        + getString(R.string.appTitle)));
                break;
            case 6:
                Intent intentMap = new Intent(SmartShehar_Dashboard_act.this, ViewIssueOnMapAct.class);
                startActivity(intentMap);
                break;
        }
        mDrawerLayout.closeDrawer(mDrawerPane);
    }

    private void getAppUserDetails() {
        try {
            final String url = Constants_dp.GET_APPUSER_DETAILS_URL;
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (!response.trim().equals("-1")) {
                                parseJson(response);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    SSLog.e(TAG, "getAppUserDetails", error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params = CGlobals_db.getInstance(SmartShehar_Dashboard_act.this).getBasicMobileParams(params,
                            url, SmartShehar_Dashboard_act.this);
                    return CGlobals_db.getInstance(SmartShehar_Dashboard_act.this).checkParams(params);
                }
            };
            CGlobals_db.getInstance(SmartShehar_Dashboard_act.this).getRequestQueue(SmartShehar_Dashboard_act.this).add(postRequest);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ERROR IS " + e.toString());
        }
//        return false;
    }

    private void parseJson(String data) {
        String username, fullname, age,
                sex, phoneno;

        JSONObject jobj;
        try {
            jobj = new JSONObject(data);
            username = jobj.isNull("username") ? "" : jobj.getString("username");
            fullname = jobj.isNull("fullname") ? "" : jobj.getString("fullname");
            age = jobj.isNull("age") ? "" : jobj.getString("age");
            sex = jobj.isNull("sex") ? "" : jobj.getString("sex");
            phoneno = jobj.isNull("phoneno") ? "" : jobj.getString("phoneno");
            CGlobals_lib_ss.getInstance()
                    .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).putString(Constants_dp.PREF_USER_NAME, username).commit();
            CGlobals_lib_ss.getInstance()
                    .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).putString(Constants_dp.PREF_USER_FULL_NAME, fullname).commit();
            CGlobals_lib_ss.getInstance()
                    .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).putString(Constants_dp.PREF_USER_AGE, age).commit();
            CGlobals_lib_ss.getInstance()
                    .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).putString(Constants_dp.PREF_USER_GENDER, sex).commit();
            CGlobals_lib_ss.getInstance()
                    .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this)
                    .putString(Constants_lib_ss.PREF_PHONENO,
                            phoneno).commit();
            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(phoneno)) {
                imgWarning.setVisibility(View.VISIBLE);

            } else {
                imgWarning.setVisibility(View.GONE);
                CGlobals_lib_ss.getInstance()
                        .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                        putBoolean(Constants_dp.PREF_USER_REGISTRATION, true).commit();
            }

        } catch (JSONException e) {

            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void showNear(String type) {
        try {

            if (!Connectivity.checkConnected(SmartShehar_Dashboard_act.this)) {
                return;
            }
            if (mCurrentLocation == null) {
                mCurrentLocation = CGlobals_lib_ss.getInstance().getMyLocation(this);
            }


            StringBuilder sbValue = sbMethod(type);
            PlacesTask placesTask = new PlacesTask();
            placesTask.execute(sbValue.toString());
        } catch (Exception e) {

            e.printStackTrace();
            SSLog.e(TAG, "showNear", e.toString());
        }
    }

    public StringBuilder sbMethod(String type) {
        //use your current location here
        double mLatitude = mCurrentLocation.getLatitude();
        double mLongitude = mCurrentLocation.getLongitude();

        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location=").append(mLatitude).append(",").append(mLongitude);
//        sb.append("&radius=5000");
        sb.append("&rankby=distance");
        sb.append("&types=").append(type);
        sb.append("&sensor=true");
        sb.append("&key=AIzaSyAqpE1l1BgCartirn08RLtUffml9gR0Zpk");
        sb.append("&languages=en");

        Log.d("Map", "api: " + sb.toString());

        return sb;
    }

    private class PlacesTask extends AsyncTask<String, Integer, String> {

        String data = null;

        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                SSLog.e(TAG, "PlacesTask", e);
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result) {
            ParserTask parserTask = new ParserTask();

            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of the class ParserTask
            parserTask.execute(result);
        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d(TAG, e.toString());
            SSLog.e(TAG,"downloadUrl ",e.toString());
        } finally {
            assert iStream != null;
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;
            Place_JSON placeJson = new Place_JSON();

            try {
                jObject = new JSONObject(jsonData[0]);
                places = placeJson.parse(jObject);
            } catch (Exception e) {
                Log.d("Exception", e.toString());
                SSLog.e(TAG, "ParserTask", e);
            }
            return places;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String, String>> list) {
            try {
                String msTrainStationNmaes = "", msBusStationNmaes = "";
                if (list != null && list.size() > 0) {
                    float distMetro = -1;
                    float[] dist = new float[1];
                    for (int i = 0; i < list.size(); i++) {
                        HashMap<String, String> hmPlace = list.get(i);
                        double lat = Double.parseDouble(hmPlace.get("lat"));
                        double lng = Double.parseDouble(hmPlace.get("lng"));
                        Location.distanceBetween(lat, lng, mCurrentLocation.getLatitude(),
                                mCurrentLocation.getLongitude(), dist);
                        if (hmPlace.get("type").equals("subway_station")) {
                            if (distMetro == -1 || dist[0] < distMetro) {
                                distMetro = dist[0];
                            }
                        }
                    }
                    String mCurrentPlaceName = "";
                    for (int i = 0; i < list.size(); i++) {
                        // Getting a place from the places list
                        HashMap<String, String> hmPlace = list.get(i);

                        String name = hmPlace.get("place_name");
                        if (mCurrentPlaceName.equals(name)) {
                            continue;
                        }
                        mCurrentPlaceName = name;
                        Log.d("Map", "place: " + name);
                        String type = hmPlace.get("type");
                        if (hmPlace.get("color").equals(String.valueOf(COLOR_TRAIN))) {

                            msTrainStationNmaes = name;
                            if (!TextUtils.isEmpty(msTrainStationNmaes)) {
                                if (i == 0) {
                                    tvTrainOne.setText(Html.fromHtml(msTrainStationNmaes));
                                    CGlobals_lib_ss.getInstance()
                                            .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                                            putString(Constants_dp.PREF_LAST_TRAIN_STATION_ONE, msTrainStationNmaes).commit();
                                }
                                if (i == 1) {
                                    tvTrainTwo.setText(Html.fromHtml(msTrainStationNmaes));
                                    CGlobals_lib_ss.getInstance()
                                            .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                                            putString(Constants_dp.PREF_LAST_TRAIN_STATION_TWO, msTrainStationNmaes).commit();
                                }
                                if (i == 2) {
                                    tvTrainThree.setText(Html.fromHtml(msTrainStationNmaes));
                                    CGlobals_lib_ss.getInstance()
                                            .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                                            putString(Constants_dp.PREF_LAST_TRAIN_STATION_THREE, msTrainStationNmaes).commit();
                                }
                            }
                        } else if (type.equals("bus_station")) {
                            msBusStationNmaes = name;
                            if (!TextUtils.isEmpty(msBusStationNmaes)) {
                                if (i == 0) {
                                    tvBusOne.setText(Html.fromHtml(msBusStationNmaes));
                                    CGlobals_lib_ss.getInstance()
                                            .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                                            putString(Constants_dp.PREF_LAST__BUS_STATION_ONE, msBusStationNmaes).commit();
                                }
                                if (i == 1) {
                                    tvBusTwo.setText(Html.fromHtml(msBusStationNmaes));
                                    CGlobals_lib_ss.getInstance()
                                            .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                                            putString(Constants_dp.PREF_LAST__BUS_STATION_TWO, msBusStationNmaes).commit();
                                }
                                if (i == 2) {
                                    tvBusThree.setText(Html.fromHtml(msBusStationNmaes));
                                    CGlobals_lib_ss.getInstance()
                                            .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                                            putString(Constants_dp.PREF_LAST__BUS_STATION_THREE, msBusStationNmaes).commit();
                                }
                            }
                        }
                        if (i > 0) {
                            break;
                        }
                    }
                }
            } catch (NumberFormatException e) {
                SSLog.e(TAG, "parseTask", e);
                e.printStackTrace();
            } catch (Exception e) {

                e.printStackTrace();
                SSLog.e(TAG, "parseTask", e);
            }
            //mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 50));
        }
    }

    public class Place_JSON {

        /**
         * Receives a JSONObject and returns a list
         */
        public List<HashMap<String, String>> parse(JSONObject jObject) {

            JSONArray jPlaces = null;
            try {
                /** Retrieves all the elements in the 'places' array */
                jPlaces = jObject.getJSONArray("results");
            } catch (JSONException e) {
                SSLog.e(TAG, "Place_JSON", e);
                e.printStackTrace();
            }
            /** Invoking getPlaces with the array of json object
             * where each json object represent a place
             */
            return getPlaces(jPlaces);
        }

        private List<HashMap<String, String>> getPlaces(JSONArray jPlaces) {
            int placesCount = jPlaces.length();
            List<HashMap<String, String>> placesList = new ArrayList<>();
            HashMap<String, String> place;

            /** Taking each place, parses and adds to list object */
            for (int i = 0; i < placesCount; i++) {
                try {
                    /** Call getPlace with place JSON object to parse the place */
                    place = getPlace((JSONObject) jPlaces.get(i));
                    placesList.add(place);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return placesList;
        }

        /**
         * Parsing the Place JSON object
         */
        private HashMap<String, String> getPlace(JSONObject jPlace) {

            HashMap<String, String> place = new HashMap<>();
            String placeName = "-NA-";
            String vicinity = "-NA-";
            String latitude;
            String longitude;
            String reference;
            String type;
            float color = BitmapDescriptorFactory.HUE_MAGENTA;
            try {
                // Extracting Place name, if available
                if (!jPlace.isNull("name")) {
                    placeName = jPlace.getString("name");
                }
                JSONArray typesArray = jPlace.getJSONArray("types");
                type = typesArray.getString(0);
                if (type.equals("train_station")) {
                    color = COLOR_TRAIN;
                } else if (type.equals("subway_station")) {
                    color = COLOR_TRAIN;
                }
                // Extracting Place Vicinity, if available
                if (!jPlace.isNull("vicinity")) {
                    vicinity = jPlace.getString("vicinity");
                }

                latitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lat");
                longitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lng");
                reference = jPlace.getString("reference");
                place.put("place_name", placeName);
                place.put("vicinity", vicinity);
                place.put("lat", latitude);
                place.put("lng", longitude);
                place.put("reference", reference);
                place.put("type", type);
                place.put("color", String.valueOf(color));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return place;
        }
    }

    public void showSnackbar(String msg) {
        snackbar = Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    private void getIssueCount(final double lat, final double lng) {
        try {
            final String url = Constants_dp.SMARTSHEHAR_SUMMARY_URL;
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (!response.trim().equals("-1")) {
                                dispalyIssueCount(response);
                            } else {
                                dispalyIssueCount(null);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    dispalyIssueCount(null);
                    SSLog.e(TAG, "getAppUserDetails", error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("latitude", Double.toString(lat));
                    params.put("longitude", Double.toString(lng));
                    params.put("radious", "3");
                    params = CGlobals_db.getInstance(SmartShehar_Dashboard_act.this).getBasicMobileParams(params,
                            url, SmartShehar_Dashboard_act.this);
                    return CGlobals_db.getInstance(SmartShehar_Dashboard_act.this).checkParams(params);
                }
            };
            CGlobals_db.getInstance(SmartShehar_Dashboard_act.this).
                    getRequestQueue(SmartShehar_Dashboard_act.this).add(postRequest);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ERROR IS " + e.toString());
        }
    }

    private void dispalyIssueCount(String response) {
        JSONObject jobj;
        String mTrTIssues, mTrNIssues, mMuTIssues, mMuNIssues,
                mOTIssues, mONIssues, mMyTIssues;
        try {
            if (!TextUtils.isEmpty(response)) {
                jobj = new JSONObject(response);
                mTrTIssues = jobj.isNull("v_traffic_issues") ? "0" : jobj.getString("v_traffic_issues");
                CGlobals_lib_ss.getInstance()
                        .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                        putString(Constants_dp.PREF_LAST_TOTAL_TRAFFIC_ISSUES, mTrTIssues).commit();

                mTrNIssues = jobj.isNull("vTrNearBy") ? "0" : jobj.getString("vTrNearBy");
                CGlobals_lib_ss.getInstance()
                        .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                        putString(Constants_dp.PREF_LAST_NEAREST_TRAFFIC_ISSUES, mTrNIssues).commit();

                mMuTIssues = jobj.isNull("v_municipal_issues") ? "0" : jobj.getString("v_municipal_issues");
                CGlobals_lib_ss.getInstance()
                        .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                        putString(Constants_dp.PREF_LAST_TOTAL_MUNICIPAL_ISSUES, mMuTIssues).commit();

                mMuNIssues = jobj.isNull("vMuNearBy") ? "0" : jobj.getString("vMuNearBy");
                CGlobals_lib_ss.getInstance()
                        .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                        putString(Constants_dp.PREF_LAST_NEAREST_MUNICIPAL_ISSUES, mMuNIssues).commit();

                mOTIssues = jobj.isNull("v_open_issues") ? "0" : jobj.getString("v_open_issues");
                CGlobals_lib_ss.getInstance()
                        .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                        putString(Constants_dp.PREF_LAST_TOTAL_ISSUES, mOTIssues).commit();

                mONIssues = jobj.isNull("vOpenNearBy") ? "0" : jobj.getString("vOpenNearBy");
                CGlobals_lib_ss.getInstance()
                        .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                        putString(Constants_dp.PREF_LAST_TOTAL_NEAREST_ISSUES, mONIssues).commit();

                mMyTIssues = jobj.isNull("vMyIssues") ? "0" : jobj.getString("vMyIssues");
                CGlobals_lib_ss.getInstance()
                        .getPersistentPreferenceEditor(SmartShehar_Dashboard_act.this).
                        putString(Constants_dp.PREF_LAST_MY_ISSUES, mMyTIssues).commit();

            } else {
                mTrTIssues = CGlobals_lib_ss.getInstance()
                        .getPersistentPreference(SmartShehar_Dashboard_act.this).
                                getString(Constants_dp.PREF_LAST_TOTAL_TRAFFIC_ISSUES, "0");

                mTrNIssues = CGlobals_lib_ss.getInstance()
                        .getPersistentPreference(SmartShehar_Dashboard_act.this).
                                getString(Constants_dp.PREF_LAST_NEAREST_TRAFFIC_ISSUES, "0");

                mMuTIssues = CGlobals_lib_ss.getInstance()
                        .getPersistentPreference(SmartShehar_Dashboard_act.this).
                                getString(Constants_dp.PREF_LAST_TOTAL_MUNICIPAL_ISSUES, "0");

                mMuNIssues = CGlobals_lib_ss.getInstance()
                        .getPersistentPreference(SmartShehar_Dashboard_act.this).
                                getString(Constants_dp.PREF_LAST_NEAREST_MUNICIPAL_ISSUES, "0");

                mOTIssues = CGlobals_lib_ss.getInstance()
                        .getPersistentPreference(SmartShehar_Dashboard_act.this).
                                getString(Constants_dp.PREF_LAST_TOTAL_ISSUES, "0");

                mONIssues = CGlobals_lib_ss.getInstance()
                        .getPersistentPreference(SmartShehar_Dashboard_act.this).
                                getString(Constants_dp.PREF_LAST_TOTAL_NEAREST_ISSUES, "0");

                mMyTIssues = CGlobals_lib_ss.getInstance()
                        .getPersistentPreference(SmartShehar_Dashboard_act.this).
                                getString(Constants_dp.PREF_LAST_MY_ISSUES, "0");
            }

            if (!mTrTIssues.equals("0")) {
                txtTTIssue.setText(Html.fromHtml("<b>" + mTrTIssues + "</b>" + " " + getString(R.string.totalissues)));
                if (!mTrNIssues.equals("0"))
                    txtNTIssues.setText(Html.fromHtml("<b>" + mTrNIssues + "</b>" + " " + getString(R.string.nearbyissues)));
                else {
                    txtNTIssues.setText(getString(R.string.help_your_city) + " by loging " + getString(R.string.traffic_violation_title));
                }
            } else {
                txtTTIssue.setText(getString(R.string.somthing_wrong));
            }

            if (!mMuTIssues.equals("0")) {
                txtTMIssues.setText(Html.fromHtml("<b>" + mMuTIssues + "</b>" + " " + getString(R.string.totalissues)));
                if (!mMuNIssues.equals("0"))
                    txtNMIssues.setText(Html.fromHtml("<b>" + mMuNIssues + "</b>" + " " + getString(R.string.nearbyissues)));
                else {
                    txtNMIssues.setText(getString(R.string.help_your_city) + " by loging " + getString(R.string.municipal_title));
                }
            } else {
                txtTMIssues.setText(getString(R.string.somthing_wrong));
            }

            if (!mOTIssues.equals("0")) {
                txtTOIssues.setText(Html.fromHtml("<b>" + mOTIssues + "</b>" + " " + getString(R.string.totalissues)));
                if (!mONIssues.equals("0"))
                    txtNOIssues.setText(Html.fromHtml("<b>" + mONIssues + "</b>" + " " + getString(R.string.nearbyissues)));
            } else {
                txtTOIssues.setText(getString(R.string.somthing_wrong));
            }
            txtMyTIssues.setText(Html.fromHtml("<b>" + mMyTIssues + "</b>" + " Issues"));


        } catch (Exception e) {
            e.printStackTrace();
            SSLog_SS.e(TAG, e.toString());
        }
    }
} // SSDashboard