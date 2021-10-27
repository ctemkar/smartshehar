package com.smartshehar.cabe.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.PolyUtil;
////import com.paytm.pgsdk.PaytmMerchant;
////import com.paytm.pgsdk.PaytmOrder;
////import com.paytm.pgsdk.PaytmPGService;
////import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.smartshehar.cabe.ActionBarAdapter;
import com.smartshehar.cabe.CGlobals_CabE;
import com.smartshehar.cabe.CTripCabE;
import com.smartshehar.cabe.Cancel_Reason_Adapter;
import com.smartshehar.cabe.Constants_CabE;
import com.smartshehar.cabe.CustomVolleyRequestQueue;
import com.smartshehar.cabe.LocationService;
import com.smartshehar.cabe.MyApplication_CabE;
import com.smartshehar.cabe.NavItem;
import com.smartshehar.cabe.PermissionUtil;
import com.smartshehar.cabe.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import lib.app.util.CAddress;
import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Constants_lib_ss;
import lib.app.util.MyLocation;
import lib.app.util.SSLog_SS;
import lib.app.util.SearchAddress_act;
import lib.app.util.UpgradeApp;
import lib.app.util.ui.AbstractMapFragment_lib_act;
import lib.app.util.ui.ActFeedback;

/**
 * Created by jijo_soumen on 08/03/2016.
 * Main activity for user to get a cab
 * show all cab in map
 * information of cabs
 * information of driver before trip start
 * after trip start share your trip and track your trip
 */
public class CabEMain_act extends AbstractMapFragment_lib_act implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static String TAG = "CabEMain_act: ";
    public static final int LOOKING_FOR_CAB = 1;
    public static final int WAITING_FOR_DRIVER = 2;
    private int CHANGE_LOCATION_DISTANCE = 35;
    public static final int IN_CAB = 3;
    public LinearLayout mLlRequestCab;
    public LinearLayout mLlDriverInfo, mLlDriverIsInTrip;
    protected Handler handler = new Handler();
    MyApplication_CabE myApplication;
    GoogleApiClient googleApiClient = null;
    boolean hasMovedMap = false;
    LinearLayout llBlackYellow, llCoolCab, llFleetTaxi, llTaxiPrime, llTaxiStandard, llSanghini,
            llEstimatedPriceCabEUser, llEstimatePayment, llRickshaw, llShareCab;
    CTripCabE cabTrip, cabTrip_Driver_info, cabTrip_IsInTrip, cabTripShare;
    String sVehicleNo = "";
    Marker marker, markerDriver, markerEnd, markerStart;
    ProgressDialog pDialog;
    int trip_id;
    ImageView ivDriverDisplayImage, ivDriverCarImage, ivDriverCarImageIsInTrip,
            ivDriverNumberCall, ivZoomRequestDriver, ivZoomDriverVehicle, ivDriverConnected, ivDriverConnectedStatus;
    TextView tvDriverDisplayName, tvDriverEta, tvDriverCarName, tvDriverCarNumber, tvDriverCarDistance;
    TextView tvRequestCab, tvDriverCarNumberIsInTrip, tvCallDriverIsInTrip, tvRateRideIsInTrip,
            tvShareDetailsIsInTrip, tvAddPayment, tvPriceEstimate;
    Button btnEmergency, ivCancelTrip;
    ArrayList<Marker> driverCurrentLocationMarkerArray = new ArrayList<>();
    private boolean bmcab = false;
    private LatLng centerFrom, centerTo;
    private List<Address> addresses;
    private String msServiceCode = "BY";
    private int whereAmI = LOOKING_FOR_CAB;
    Intent mLocationServiceIntent;
    private boolean gotLocationFirstTime = false;
    private int iBYEta = -1, iCCEta = -1, iFTEta = -1, iTPEta = -1, iSCEta = -1, iSGEta = -1, iAREta = -1, iSTEta = -1;
    String comment = "";
    ArrayList<NavItem> mNavItems = new ArrayList<>();
    ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    RelativeLayout mDrawerPane;
    Toolbar toolbar;
    ImageView ivCabEShare;
    NetworkImageView mIvProfileImage;
    ImageLoader mImageLoader;
    TextView mTvUserName;
    RelativeLayout profileBox;
    ////PaytmPGService Service = null;
    int randomInt = 0;
    //private boolean isEstimatedPriceOn = false;
    private static final int REQUEST_CONTACTS = 14;
    private static String[] PERMISSIONS_CONTACT = {Manifest.permission.READ_CONTACTS};
    private boolean isgotTrip = false;
    private CoordinatorLayout coordinatorLayout;
    Snackbar snackbar;
    boolean isVerifyDoneFlag = false, isLoginDoneFlag = false;
    boolean isEstimatedBY = false, isEstimatedCC = false, isEstimatedFC = false, isEstimatedSC = false,
            isEstimatedPC = false, isEstimatedSG = false, isEstimatedAR = false, isEstimatedST = false;
    int ratingCount = 0;
    LocationRequest mLocationRequest;
    private static final long INTERVAL = 1000 * 60;
    boolean isCabEDistanceDriver = false;
    String distanceFromMe = "";
    NotificationManager notificationManager;
    CTripCabE cTripcedReason;
    ArrayList<CTripCabE> arrayCancelReasonList;
    double mNearestDriverLat, mNearestDriverLng, mNearestDriverLat_Status, mNearestDriverLng_Status;
    boolean isRequestCheckDriverConnection = false;
    private final int ACT_START_ADDRESS = 17;
    private final int ACT_DESTINATION_ADDRESS = 27;
    private FusedLocationProviderClient mFusedLocationClient;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cabemain_act);
        create();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

// Get the last known location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.

                        if (location != null) {
                            // Logic to handle location object
                            if(mCurrentLocation == null) {
                                mCurrentLocation = CGlobals_lib_ss.isBetterLocation(location, mCurrentLocation) ? location
                                        : mCurrentLocation;
                                setupMap();
                            }

                        }
                    }
                });
        googleApiClient = new GoogleApiClient.Builder(CabEMain_act.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(CabEMain_act.this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
        createLocationRequest();
        MyLocation myLocation = new MyLocation(
                MyApplication_CabE.mVolleyRequestQueue, CabEMain_act.this, googleApiClient);
        myLocation.getLocation(this, onLocationResult);
        init();
        mImageLoader = CustomVolleyRequestQueue.getInstance(CabEMain_act.this)
                .getImageLoader();
        initToolBar();
        CGlobals_CabE.getInstance().init(CabEMain_act.this);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        //Action Menu Bar
        mIvProfileImage = (NetworkImageView) findViewById(R.id.ivProfileImage);
        mTvUserName = (TextView) findViewById(R.id.tvUserName);
        profileBox = (RelativeLayout) findViewById(R.id.profileBox);
        mDrawerPane = (RelativeLayout) findViewById(R.id.drawerPane);
        mNavItems.add(new NavItem("History", "", R.mipmap.ic_history));
        //mNavItems.add(new NavItem("Emergency", R.mipmap.ic_emergency));
        mNavItems.add(new NavItem("Profile", "", R.drawable.ic_person));
        /*if (!TextUtils.isEmpty(CGlobals_lib_ss.getInstance().mPackageInfo.versionName)
                && CGlobals_lib_ss.getInstance().mPackageInfo.versionName != null) {
            mNavItems.add(new NavItem("Feedback", "Version: " +
                    CGlobals_lib_ss.getInstance().mPackageInfo.versionName, R.mipmap.ic_feedback));
        } else {*/
        mNavItems.add(new NavItem("Feedback", "", R.mipmap.ic_feedback));
        // }
        // mNavItems.add(new NavItem("TEST", R.mipmap.ic_launcher));
        // DrawerLayout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // Populate the Navigtion Drawer with options
        mDrawerList = (ListView) findViewById(R.id.navList);
        ActionBarAdapter adapter = new ActionBarAdapter(CabEMain_act.this, mNavItems);
        mDrawerList.setAdapter(adapter);
        // Drawer Item click listeners
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItemFromDrawer(position);
            }
        });
        mDrawerToggle = new ActionBarDrawerToggle(CabEMain_act.this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
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

        // Rating
        boolean isRating = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this).
                getBoolean(Constants_CabE.SAVE_RATING_NOT_SEND, false);
        ratingCount = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this).
                getInt(Constants_CabE.SAVE_RATING_COUNT, 0);
        int wmi = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getInt(Constants_CabE.WHERE_AM_I, 0);
        if (isRating && wmi == LOOKING_FOR_CAB) {
            if (ratingCount <= 3) {
                updateTripRating(1);
            }
        }
        try {
            UpgradeApp.app_launched(this, CGlobals_CabE.getInstance().mPackageInfo, CGlobals_CabE.getInstance().mUserInfo,
                    getString(R.string.app_label),
                    "CEP",
                    String.valueOf(CGlobals_CabE.getInstance().mPackageInfo.versionCode), Constants_lib_ss.UPGRADE_APP_URL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getUserProfile() {
        final String url = Constants_CabE.GET_USER_PROFILE_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (pDialog != null) {
                            pDialog.setCancelable(true);
                            pDialog.dismiss();
                        }
                        getResponseUserProfile(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (pDialog != null) {
                    pDialog.setCancelable(true);
                    pDialog.dismiss();
                }
                if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                    Log.e(TAG, "");
                } else {
                    SSLog_SS.e(TAG, "getCabShow :-   ", error, CabEMain_act.this);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<>();
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        url, CabEMain_act.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                String url1 = url;
                try {
                    String url = url1 + "?verbose=Y&" + getParams.toString();
                    Log.i(TAG, "url  " + url);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "getFoundDriver", e, CabEMain_act.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, CabEMain_act.this);
    }

    @SuppressLint("SetTextI18n")
    private void getResponseUserProfile(String response) {
        if (TextUtils.isEmpty(response) || response.trim().equals("-1")) {
            String url = "https://www.google.co.in";
            mImageLoader.get(url, ImageLoader.getImageListener(mIvProfileImage,
                    R.drawable.ic_driver, R.drawable.ic_driver));
            mIvProfileImage.setImageUrl(url, mImageLoader);
            return;
        }
        cabTrip = new CTripCabE(response, CabEMain_act.this);
        if (TextUtils.isEmpty(cabTrip.getFirstName()) || TextUtils.isEmpty(cabTrip.getLastName())) {
            mTvUserName.setText("");
        } else {
            mTvUserName.setText(cabTrip.getFirstName() + " " + cabTrip.getLastName());
        }
        String sProfileImage = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getString(Constants_CabE.GMAIL_FACEBOOK_IMAGE_URL, "");
        if (!TextUtils.isEmpty(sProfileImage)) {
            mImageLoader.get(sProfileImage, ImageLoader.getImageListener(mIvProfileImage,
                    R.drawable.ic_driver, R.drawable.ic_driver));
            mIvProfileImage.setImageUrl(sProfileImage, mImageLoader);
        } else {
            String url = "https://www.google.co.in";
            mImageLoader.get(url, ImageLoader.getImageListener(mIvProfileImage,
                    R.drawable.ic_driver, R.drawable.ic_driver));
            mIvProfileImage.setImageUrl(url, mImageLoader);
        }
    }

    private void selectItemFromDrawer(int position) {
        mDrawerList.setItemChecked(position, true);
        switch (position) {
            case 0:
                Intent intentTripHistory = new Intent(CabEMain_act.this, TripHistoryList_act.class);
                startActivity(intentTripHistory);
                break;
            case 1:
                Intent intentProfile = new Intent(CabEMain_act.this, CabEUserProfile_act.class);
                startActivity(intentProfile);
                break;
            case 2:
                try {
                    String jijo_Website = "https://cabebooking.com";
                    String jijo_Twitter = "https://twitter.com/smartshehar";
                    String jijo_Feacbook = "https://www.facebook.com/smartshehar";
                    String jijo_Feedback_email = "userfeedback@smartshehar.com";
                    Intent intent_Feedback = new Intent(CabEMain_act.this, ActFeedback.class);
                    intent_Feedback.putExtra("app_label", getString(R.string.app_label));
                    intent_Feedback.putExtra("appCode", getString(R.string.appCode));
                    intent_Feedback.putExtra("versionName", CGlobals_lib_ss.getInstance().mPackageInfo.versionName);
                    intent_Feedback.putExtra("USER_FEEDBACK_EMAIL", jijo_Feedback_email);
                    intent_Feedback.putExtra("FACEBOOK_POST", jijo_Feacbook);
                    intent_Feedback.putExtra("TWITTER_POST", jijo_Twitter);
                    intent_Feedback.putExtra("WEB_SITE", jijo_Website);
                    intent_Feedback.putExtra("PACKAGE_NAME", "market://details?id=" + getPackageName());
                    startActivity(intent_Feedback);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "selectItemFromDrawer: ", e, CabEMain_act.this);
                }
                break;
        }
        // Close the drawer
        mDrawerLayout.closeDrawer(mDrawerPane);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    } // onOptions

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void initToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        CabEMain_act.this.setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }
        ivCabEShare = (ImageView) findViewById(R.id.ivCabEShare);
        ivCabEShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = getString(R.string.cabeapplink);
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(Intent.createChooser(share, "Share Cab-E"));
            }
        });
    }


    protected Runnable runnableWhereAmI = new Runnable() {
        @Override
        public void run() {
//            First looking for cabs handler, then service to make sure that driver tracks passenger
//            and then knows if trip cancelled and then back to handler when in cab
            switch (whereAmI) {
                case LOOKING_FOR_CAB:
                    boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEMain_act.this);
                    if (!isInternetCheck) {
                        getForHire(msServiceCode);
                    }
                    checkInternetGPS();
                    break;
                case IN_CAB:
                    driverGetTripStatus(); // to get position of driver and know if cancelled
                    break;
                case WAITING_FOR_DRIVER:
                    // shouldn't be here, service should be running
                    break;
            }
        }
    };
    private MyLocation.LocationResult onLocationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            processLocation(location);
        }
    };


    public BroadcastReceiver mMessageReceiverPassenger = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int iStatus = intent.getIntExtra("status", 0);
            String response = intent.getStringExtra("response");
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                    .putInt(Constants_CabE.PREF_DRIVER_STATUS, iStatus);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                    .putString(Constants_CabE.PREF_UPDATE_DRIVER_RESPONSE, response);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
            if (iStatus == 1) {
                new getCabEUpDateDriver().execute("");
            } else if (iStatus == 2) {
                new getUpDateDriverResponse().execute("");
            } else if (iStatus == Constants_CabE.VOLLEY_NETYWORK_ERROR) {
                new getCabEUpDateDriverError().execute("");
            }
            if (CGlobals_CabE.getInstance().isNewNotification) {
                new getCabEUpDateDriver().execute("");
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        setupUI();
        if (!gotLocationFirstTime) {
            pDialog = new ProgressDialog(this);
            pDialog.setMessage("Getting location, please wait");
            pDialog.setCancelable(false);
            pDialog.show();
        }
        init();
        CGlobals_CabE.getInstance().settingsrequest(CabEMain_act.this, mGoogleApiClient);
        handler.postDelayed(runnableWhereAmI, 0);
        int wmi = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getInt(Constants_CabE.WHERE_AM_I, 0);
        if (wmi == WAITING_FOR_DRIVER) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverPassenger,
                    new IntentFilter("com.smartshehar.cabe.ui.CabEMain_act"));
            Intent sendableIntent = new Intent("com.smartshehar.cabe.ui.CabEMain_act");
            LocalBroadcastManager.getInstance(this).
                    sendBroadcast(sendableIntent);
        }
        if (!isLocationServiceRunning(LocationService.class) && whereAmI == WAITING_FOR_DRIVER) {
            StartLocationService();
        }
        boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEMain_act.this);
        if (!isInternetCheck) {
            getUserProfile();
        }
        isVerifyDoneFlag = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getBoolean(Constants_CabE.PREF_CABE_NUMBER_VERIFY_DONE, false);
        isLoginDoneFlag = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getBoolean(Constants_CabE.PREF_CABE_GOOGLE_FACEBOOK_LOGIN_DONE, false);
        if (!isVerifyDoneFlag) {
            Intent intent1 = new Intent(CabEMain_act.this,
                    CabENumberVerify_act.class);
            startActivityForResult(intent1, 9);
        }
        if (!isLoginDoneFlag) {
            Intent intent5 = new Intent(CabEMain_act.this,
                    CabERegistration_act.class);
            startActivityForResult(intent5, 9);
        }
        if (wmi == WAITING_FOR_DRIVER) {
            double driverLat = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                    .getFloat(Constants_CabE.DRIVER_CAB_LAT, (float) 0.0);
            double driverLng = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                    .getFloat(Constants_CabE.DRIVER_CAB_LNG, (float) 0.0);
            if (driverLat != 0.0 && driverLng != 0.0) {
                if (mMap != null) {
                    markerDriver = mMap.addMarker(new MarkerOptions()
                            .position(
                                    new LatLng(driverLat, driverLng))
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_driver_position))
                            .alpha(0.8f).draggable(true));
                }
            }
            int iStatus = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                    .getInt(Constants_CabE.PREF_DRIVER_STATUS, -1);
            if (iStatus == 1) {
                new getCabEUpDateDriver().execute("");
            } else if (iStatus == 2) {
                new getUpDateDriverResponse().execute("");
            } else if (iStatus == Constants_CabE.VOLLEY_NETYWORK_ERROR) {
                new getCabEUpDateDriverError().execute("");
            }
        }
        if (wmi == IN_CAB) {
            double driverLat_Status = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                    .getFloat(Constants_CabE.STATUS_DRIVER_CAB_LAT, (float) 0.0);
            double driverLng_Status = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                    .getFloat(Constants_CabE.STATUS_DRIVER_CAB_LNG, (float) 0.0);
            if (driverLat_Status != 0.0 && driverLng_Status != 0.0) {
                if (mMap != null) {
                    markerDriver = mMap.addMarker(new MarkerOptions()
                            .position(
                                    new LatLng(driverLat_Status, driverLng_Status))
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_driver_position))
                            .alpha(0.8f).draggable(true));
                }
            }
        }
        if (msServiceCode.equals(Constants_CabE.SERVICE_CODE_ST)) {
            mLlMarkerLayout.setVisibility(View.GONE);
        } else {
            mLlMarkerLayout.setVisibility(View.VISIBLE);
        }
        msServiceCode = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getString(Constants_CabE.PREF_SERVICE_CODE_SAVE, "");
        CGlobals_lib_ss.getInstance().countError1 = 1;
        CGlobals_lib_ss.getInstance().countError2 = 1;
        CGlobals_lib_ss.getInstance().countError3 = 1;
        CGlobals_lib_ss.getInstance().countError4 = 1;
        CGlobals_lib_ss.getInstance().countError5 = 1;
        CGlobals_lib_ss.getInstance().countError6 = 1;
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(this).
                putBoolean(Constants_CabE.PREF_NOTIFICATION_CLEAR_FLAG, true);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(this).commit();
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
    public void onBackPressed() {
        if (whereAmI == WAITING_FOR_DRIVER) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Your cab is on its way, Do you want to cancel?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                                    .putInt(Constants_CabE.WHERE_AM_I, LOOKING_FOR_CAB);
                            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
        handler.removeCallbacks(runnableWhereAmI);
        StopLocationService();
        clearNotification();
        finish();
    }

    private void setupUI() {
        llBlackYellow = (LinearLayout) findViewById(R.id.llBalckYellow);
        llCoolCab = (LinearLayout) findViewById(R.id.llCoolCab);
        llFleetTaxi = (LinearLayout) findViewById(R.id.llFleetTaxi);
        llTaxiPrime = (LinearLayout) findViewById(R.id.llTaxiPrime);
        llTaxiStandard = (LinearLayout) findViewById(R.id.llTaxiNonPrime);
        llSanghini = (LinearLayout) findViewById(R.id.llSanghini);
        llRickshaw = (LinearLayout) findViewById(R.id.llRickshaw);
        llShareCab = (LinearLayout) findViewById(R.id.llShareCab);
        mLlRequestCab = (LinearLayout) findViewById(R.id.llRequestCab);
        mLlDriverInfo = (LinearLayout) findViewById(R.id.llDriverInfo);
        mLlDriverIsInTrip = (LinearLayout) findViewById(R.id.llDriverIsInTrip);
        llEstimatePayment = (LinearLayout) findViewById(R.id.llEstimatePayment);
        ivDriverDisplayImage = (ImageView) findViewById(R.id.ivDriverDisplayImage);
        tvDriverDisplayName = (TextView) findViewById(R.id.tvDriverDisplayName);
        ivDriverNumberCall = (ImageView) findViewById(R.id.ivDriverNumberCall);
        ivDriverConnected = (ImageView) findViewById(R.id.ivDriverConnected);
        ivDriverConnectedStatus = (ImageView) findViewById(R.id.ivDriverConnectedStatus);
        tvDriverCarDistance = (TextView) findViewById(R.id.tvDriverCarDistance);
        tvDriverEta = (TextView) findViewById(R.id.tvDriverEta);
        tvDriverCarName = (TextView) findViewById(R.id.tvDriverCarName);
        ivDriverCarImage = (ImageView) findViewById(R.id.ivDriverCarImage);
        tvDriverCarNumber = (TextView) findViewById(R.id.tvDriverCarNumber);
        tvRequestCab = (TextView) findViewById(R.id.tvRequestCab);
        tvAddPayment = (TextView) findViewById(R.id.tvAddPayment);
        tvPriceEstimate = (TextView) findViewById(R.id.tvPriceEstimate);
        ivDriverCarImageIsInTrip = (ImageView) findViewById(R.id.ivDriverCarImageIsInTrip);
        tvDriverCarNumberIsInTrip = (TextView) findViewById(R.id.tvDriverCarNumberIsInTrip);
        tvCallDriverIsInTrip = (TextView) findViewById(R.id.tvCallDriverIsInTrip);
        tvRateRideIsInTrip = (TextView) findViewById(R.id.tvRateRideIsInTrip);
        tvShareDetailsIsInTrip = (TextView) findViewById(R.id.tvShareDetailsIsInTrip);
        llEstimatedPriceCabEUser = (LinearLayout) findViewById(R.id.llEstimatedPriceCabEUser);
        ivZoomRequestDriver = (ImageView) findViewById(R.id.ivZoomRequestDriver);
        ivZoomDriverVehicle = (ImageView) findViewById(R.id.ivZoomDriverVehicle);
        btnEmergency = (Button) findViewById(R.id.btnEmergency);
        tvRequestCab = (TextView) findViewById(R.id.tvRequestCab);
        setupListeners();
    }

    private void setupListeners() {
        llBlackYellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLlMarkerLayout.setVisibility(View.VISIBLE);
                msServiceCode = Constants_CabE.SERVICE_CODE_BY;
                boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEMain_act.this);
                if (!isInternetCheck) {
                    getForHire(msServiceCode);
                }
            }
        });

        llCoolCab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLlMarkerLayout.setVisibility(View.VISIBLE);
                msServiceCode = Constants_CabE.SERVICE_CODE_CC;
                boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEMain_act.this);
                if (!isInternetCheck) {
                    getForHire(msServiceCode);
                }
            }
        });

        llFleetTaxi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLlMarkerLayout.setVisibility(View.VISIBLE);
                msServiceCode = Constants_CabE.SERVICE_CODE_FT;
                boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEMain_act.this);
                if (!isInternetCheck) {
                    getForHire(msServiceCode);
                }
            }
        });

        llTaxiPrime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLlMarkerLayout.setVisibility(View.VISIBLE);
                msServiceCode = Constants_CabE.SERVICE_CODE_TTP;
                boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEMain_act.this);
                if (!isInternetCheck) {
                    getForHire(msServiceCode);
                }
            }
        });

        llTaxiStandard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLlMarkerLayout.setVisibility(View.VISIBLE);
                msServiceCode = Constants_CabE.SERVICE_CODE_TNP;
                boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEMain_act.this);
                if (!isInternetCheck) {
                    getForHire(msServiceCode);
                }
            }
        });

        llSanghini.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLlMarkerLayout.setVisibility(View.VISIBLE);
                msServiceCode = Constants_CabE.SERVICE_CODE_SG;
                boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEMain_act.this);
                if (!isInternetCheck) {
                    getForHire(msServiceCode);
                }
            }
        });

        llRickshaw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLlMarkerLayout.setVisibility(View.VISIBLE);
                msServiceCode = Constants_CabE.SERVICE_CODE_AR;
                boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEMain_act.this);
                if (!isInternetCheck) {
                    getForHire(msServiceCode);
                }
            }
        });

        llShareCab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLlMarkerLayout.setVisibility(View.GONE);
                msServiceCode = Constants_CabE.SERVICE_CODE_ST;
                boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEMain_act.this);
                if (!isInternetCheck) {
                    getForHire(msServiceCode);
                }
            }
        });

        tvRequestCab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (centerFrom != null) {
                        Location location = CGlobals_lib_ss.getInstance().getMyLocation(CabEMain_act.this);
                        if(location == null)
                            location = mCurrentLocation;
                        float[] results = new float[1];
                        Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                                centerFrom.latitude, centerFrom.longitude, results);
                        float distanceInMeters = results[0];
                        boolean isWithin35m = distanceInMeters < CHANGE_LOCATION_DISTANCE;
                        if (!isWithin35m) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(CabEMain_act.this);
                            alertDialog.setTitle("It seems you are requesting a cab for a location different from your current location.\nAre you sure?");
                            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    checkAddressRequestCab();
                                    dialog.cancel();
                                }
                            });
                            alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    mapReady();
                                    dialog.cancel();
                                }
                            });
                            if (!CabEMain_act.this.isFinishing()) {
                                alertDialog.show();
                            }
                        } else {
                            checkAddressRequestCab();
                        }
                    } else {
                        checkAddressRequestCab();
                    }
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "tvRequestCab: ", e, CabEMain_act.this);
                }
            }
        });

        tvAddPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //// getPaytm();
            }
        });

        /*tvPriceEstimate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (msServiceCode) {
                    case "BY":
                        if (isEstimatedBY) {
                            getEstimatePrice(msServiceCode);
                        } else {
                            snackbar = Snackbar.make(coordinatorLayout, "No " + getString(R.string.service_black_and_yellow) + " available", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }
                        break;
                    case "CC":
                        if (isEstimatedCC) {
                            getEstimatePrice(msServiceCode);
                        } else {
                            snackbar = Snackbar.make(coordinatorLayout, "No " + getString(R.string.service_coolcab) + " available", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }
                        break;
                    case "FT":
                        if (isEstimatedFC) {
                            getEstimatePrice(msServiceCode);
                        } else {
                            snackbar = Snackbar.make(coordinatorLayout, "No " + getString(R.string.service_fleet_taxi) + " available", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }
                        break;
                    case "TTP":
                        if (isEstimatedPC) {
                            getEstimatePrice(msServiceCode);
                        } else {
                            snackbar = Snackbar.make(coordinatorLayout, "No " + getString(R.string.service_prime) + " available", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }
                        break;
                    case "TNP":
                        if (isEstimatedSC) {
                            getEstimatePrice(msServiceCode);
                        } else {
                            snackbar = Snackbar.make(coordinatorLayout, "No " + getString(R.string.service_standard) + " available", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }
                        break;
                    case "SG":
                        if (isEstimatedSG) {
                            getEstimatePrice(msServiceCode);
                        } else {
                            snackbar = Snackbar.make(coordinatorLayout, "No " + getString(R.string.sanghini) + " available", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }
                        break;

                    case "AR":
                        if (isEstimatedAR) {
                            getEstimatePrice(msServiceCode);
                        } else {
                            snackbar = Snackbar.make(coordinatorLayout, "No " + getString(R.string.rickshaw) + " available", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }
                        break;
                }
            }
        });*/

        btnEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CabEMain_act.this, Emergency_act.class);
                startActivity(intent);
            }
        });
    }

    public void checkAddressRequestCab() {
        if (!TextUtils.isEmpty(msServiceCode)) {
            boolean areCabsAvailable = true;
            switch (msServiceCode) {
                case Constants_CabE.SERVICE_CODE_BY:
                    if (iBYEta == -1) {
                        Toast.makeText(CabEMain_act.this, "No " + getString(R.string.service_black_and_yellow) + " available", Toast.LENGTH_LONG).show();
                        areCabsAvailable = false;
                    }
                    break;
                case Constants_CabE.SERVICE_CODE_CC:
                    if (iCCEta == -1) {
                        Toast.makeText(CabEMain_act.this, "No " + getString(R.string.service_coolcab) + " available", Toast.LENGTH_LONG).show();
                        areCabsAvailable = false;
                    }
                    break;
                case Constants_CabE.SERVICE_CODE_FT:
                    if (iFTEta == -1) {
                        Toast.makeText(CabEMain_act.this, "No " + getString(R.string.service_fleet_taxi) + " available", Toast.LENGTH_LONG).show();
                        areCabsAvailable = false;
                    }
                    break;
                case Constants_CabE.SERVICE_CODE_TTP:
                    if (iTPEta == -1) {
                        Toast.makeText(CabEMain_act.this, "No " + getString(R.string.service_prime) + " available", Toast.LENGTH_LONG).show();
                        areCabsAvailable = false;
                    }
                    break;
                case Constants_CabE.SERVICE_CODE_TNP:
                    if (iSCEta == -1) {
                        Toast.makeText(CabEMain_act.this, "No " + getString(R.string.service_standard) + " available", Toast.LENGTH_LONG).show();
                        areCabsAvailable = false;
                    }
                    break;
                case Constants_CabE.SERVICE_CODE_SG:
                    if (iSGEta == -1) {
                        Toast.makeText(CabEMain_act.this, "No " + getString(R.string.sanghini) + " available", Toast.LENGTH_LONG).show();
                        areCabsAvailable = false;
                    }
                    break;
                case Constants_CabE.SERVICE_CODE_AR:
                    if (iAREta == -1) {
                        Toast.makeText(CabEMain_act.this, "No " + getString(R.string.rickshaw) + " available", Toast.LENGTH_LONG).show();
                        areCabsAvailable = false;
                    }
                    break;
                case Constants_CabE.SERVICE_CODE_ST:
                    if (iSTEta == -1) {
                        Toast.makeText(CabEMain_act.this, "No Share Cab available", Toast.LENGTH_LONG).show();
                        areCabsAvailable = false;
                    }
                    break;
            }
            if (areCabsAvailable) {
                if (setFromTo()) {
                    mIsFromSelected = true;
                    mLlMarkerLayout.setVisibility(View.GONE);
                    bmcab = true;
                    goDirections();
                    boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEMain_act.this);
                    if (msServiceCode.equals(Constants_CabE.SERVICE_CODE_ST)) {
                        if (!isInternetCheck) {
                            shareRequestCab();
                        }
                    } else {
                        if (!isInternetCheck) {
                            requestCab(msServiceCode);
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.pleaseenterdestinationaddress),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    protected void StartLocationService() {
        StopLocationService();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverPassenger,
                new IntentFilter("com.smartshehar.cabe.ui.CabEMain_act"));
        Intent sendableIntent = new Intent("com.smartshehar.cabe.ui.CabEMain_act");
        LocalBroadcastManager.getInstance(this).
                sendBroadcast(sendableIntent);
        mLocationServiceIntent = new Intent(this, LocationService.class);
        mLocationServiceIntent.putExtra("tripid", trip_id);
        mLocationServiceIntent.putExtra("servicecode", msServiceCode);
        startService(mLocationServiceIntent);
    }

    protected void StopLocationService() {
        stopService(new Intent(CabEMain_act.this, LocationService.class));
    }

    private void setupMap() {
        try {
            LatLng latLong;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            Location location = CGlobals_lib_ss.getInstance().getMyLocation(CabEMain_act.this);
            if(location == null)
                location = mCurrentLocation;
            if (location != null) {
                latLong = new LatLng(location
                        .getLatitude(), location
                        .getLongitude());
            } else {
                latLong = new LatLng(19.0821978, 72.741118);
            }
            /* old code
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLong).zoom(15f).tilt(20).build();
            */
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLong).zoom(DEFAULT_ZOOM).bearing(90).tilt(30).build();
            mMap.setMyLocationEnabled(true);
            if (!isZoomLocationAddress) {
                mMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));
                if (location != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
                                    location.getLongitude()),
                            DEFAULT_ZOOM));
                }
                mMap.clear();
            }

            mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition arg0) {
                    if (whereAmI == IN_CAB) {
                        if (markerFrom != null)
                            markerFrom.setVisible(false);
                        if (markerTo != null)
                            markerTo.setVisible(false);
                        return;
                    }
                    if (!bmcab && whereAmI != WAITING_FOR_DRIVER) {
                        mMap.clear();
                        if (!msServiceCode.equals(Constants_CabE.SERVICE_CODE_ST)) {
                            mLlMarkerLayout.setVisibility(View.VISIBLE);
                        }
                        try {
                            if (!msServiceCode.equals(Constants_CabE.SERVICE_CODE_ST)) {
                                if (mIsFromSelected) {
                                    centerFrom = mMap.getCameraPosition().target;
                                    mIvRedPin.setVisibility(View.GONE);
                                    mIvGreenPin.setVisibility(View.VISIBLE);
                                    new GetLocationAsyncFrom(centerFrom.latitude, centerFrom.longitude, mTvFrom)
                                            .execute();
                                    if (!TextUtils.isEmpty(moToAddress.getAddress()) && moToAddress.getLatitude() != 0.0
                                            && moToAddress.getLongitude() != 0.0) {
                                        setTo(moToAddress.getAddress(),
                                                new LatLng(moToAddress.getLatitude(), moToAddress
                                                        .getLongitude()));
                                    }
                                } else {
                                    centerTo = mMap.getCameraPosition().target;
                                    mIvGreenPin.setVisibility(View.GONE);
                                    mIvRedPin.setVisibility(View.VISIBLE);
                                    if (hasMovedMap) {
                                        // 17Jan18 - Do we need to change To?
                                        /* //// new GetLocationAsyncTo(centerTo.latitude, centerTo.longitude, mTvTo)
                                                .execute(); */
                                        setFrom(moFromAddress.getAddress(),
                                                new LatLng(moFromAddress.getLatitude(), moFromAddress
                                                        .getLongitude()));
                                    }
                                }
                            }
                            hasMovedMap = true;
                        } catch (Exception e) {
                            SSLog_SS.e(TAG, "setupMap", e, CabEMain_act.this);
                        }
                    }
                }
            });

            mLlMarkerLayout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        if (mIsFromSelected) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                                            centerFrom.latitude, centerFrom.longitude),
                                    DEFAULT_ZOOM));
                            mIsFromSelected = false;
                            mIvRedPin.setVisibility(View.VISIBLE);
                            mIvGreenPin.setVisibility(View.GONE);
                        } else {
                            mIsFromSelected = true;
                            mLlMarkerLayout.setVisibility(View.GONE);
                            bmcab = true;
                            goDirections();
                        }
                    } catch (Exception e) {
                        SSLog_SS.e(TAG, "mLlMarkerLayout: ", e, CabEMain_act.this);
                    }
                }
            });
            if (pDialog != null) {
                pDialog.setCancelable(true);
                pDialog.dismiss();
            }
        } catch (Exception e) {
            if (pDialog != null) {
                pDialog.setCancelable(true);
                pDialog.dismiss();
            }
            SSLog_SS.e(TAG, "MarkerLayout: ", e, CabEMain_act.this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void onClickFrom(View v) {
        if (msServiceCode.equals(Constants_CabE.SERVICE_CODE_ST)) {
            Intent intent = new Intent(CabEMain_act.this, ListOfPointAddress_act.class);
            startActivityForResult(intent, ACT_START_ADDRESS);
        } else {
            Intent i = new Intent(CabEMain_act.this,
                    SearchAddress_act.class);
            i.putExtra("cc", getGeoCountryCode());
            startActivityForResult(i, ACTRESULT_FROM);
            try {
                if (!TextUtils.isEmpty(moFromAddress.getAddress())) {
                    CGlobals_lib_ss.getInstance().addRecentAddress(moFromAddress);
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG, "onClickFrom: ", e, CabEMain_act.this);
            }
        }
    }

    public void onClickTo(View v) {
        if (msServiceCode.equals(Constants_CabE.SERVICE_CODE_ST)) {
            Intent intent = new Intent(CabEMain_act.this, ListOfPointAddress_act.class);
            startActivityForResult(intent, ACT_DESTINATION_ADDRESS);
        } else {
            Intent i = new Intent(CabEMain_act.this,
                    SearchAddress_act.class);
            i.putExtra("cc", getGeoCountryCode());
            startActivityForResult(i, ACTRESULT_TO);
            try {
                if (!TextUtils.isEmpty(moToAddress.getAddress())) {
                    CGlobals_lib_ss.getInstance().addRecentAddress(moToAddress);
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG, "onClickTo: ", e, CabEMain_act.this);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String sAddr = "";
        CAddress oAddr = null;
        if (requestCode == ACTRESULT_FROM) if (resultCode == RESULT_OK) {
            try {
                sAddr = data.getStringExtra("add");
                if (!TextUtils.isEmpty(sAddr)) {
                    Type type = new TypeToken<CAddress>() {
                    }.getType();
                    oAddr = new Gson().fromJson(sAddr, type);
                    if (moFromAddress == null) {
                        moFromAddress = new CAddress();
                    }
                    moFromAddress.setAddress(oAddr.getAddress());
                    moFromAddress.setLatitude(oAddr.getLatitude());
                    moFromAddress.setLongitude(oAddr.getLongitude());
                    setFrom(moFromAddress, false);
                    drawFromMarkerCab(moFromAddress);
                    mIsFromSelected = false;
                    isZoomLocationAddress = true;
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                                    oAddr.getLatitude(), oAddr.getLongitude()),
                            DEFAULT_ZOOM));
                    Gson gson = new Gson();
                    String json = gson.toJson(moFromAddress);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                            .putString(Constants_CabE.PREF_FROM_ADDRESS, json);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG, "ActivityResult: ", e, CabEMain_act.this);
            }
        }
        if (requestCode == ACTRESULT_TO) {
            if (resultCode == RESULT_OK) {
                try {
                    sAddr = data.getStringExtra("add");
                    if (!TextUtils.isEmpty(sAddr)) {
                        Type type = new TypeToken<CAddress>() {
                        }.getType();
                        oAddr = new Gson().fromJson(sAddr, type);
                        if (moToAddress == null) {
                            moToAddress = new CAddress();
                        }
                        moToAddress.setAddress(oAddr.getAddress());
                        moToAddress.setLatitude(oAddr.getLatitude());
                        moToAddress.setLongitude(oAddr.getLongitude());
                        setTo(moToAddress, false);
                        drawToMarkerCab(moToAddress);
                        mIsFromSelected = false;
                        bmcab = true;
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                                        oAddr.getLatitude(), oAddr.getLongitude()),
                                DEFAULT_ZOOM));
                        Gson gson = new Gson();
                        String json = gson.toJson(moToAddress);
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                                .putString(Constants_CabE.PREF_TO_ADDRESS, json);
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
                        goDirections();
                    }
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "ActivityResult: ", e, CabEMain_act.this);
                }
            }
        }

        if (requestCode == ACT_START_ADDRESS) if (resultCode == RESULT_OK) {
            try {
                sAddr = data.getStringExtra("add");
                if (!TextUtils.isEmpty(sAddr)) {
                    Type type = new TypeToken<CTripCabE>() {
                    }.getType();
                    CTripCabE cTripCabE = new Gson().fromJson(sAddr, type);
                    if (moFromAddress == null) {
                        moFromAddress = new CAddress();
                    }
                    moFromAddress.setAddress(cTripCabE.getFormatted_Address_Fixed());
                    moFromAddress.setLatitude(cTripCabE.getLatitude_Fixed());
                    moFromAddress.setLongitude(cTripCabE.getLongitude_Fixed());
                    moFromAddress.setAdminArea(cTripCabE.getArea_Fixed());
                    moFromAddress.setLocality(cTripCabE.getLocality_Fixed());
                    moFromAddress.setSubLocality(cTripCabE.getSublocality_Fixed());
                    setFrom(moFromAddress, false);
                    drawFromMarkerCab(moFromAddress);
                    mIsFromSelected = false;
                    isZoomLocationAddress = true;
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                                    cTripCabE.getLatitude_Fixed(), cTripCabE.getLatitude_Fixed()),
                            DEFAULT_ZOOM));
                    Gson gson = new Gson();
                    String json = gson.toJson(moFromAddress);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                            .putString(Constants_CabE.PREF_FROM_ADDRESS_FIXED, sAddr);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                            .putString(Constants_CabE.PREF_FROM_ADDRESS, json);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG, "ActivityResult: ", e, CabEMain_act.this);
            }
        }

        if (requestCode == ACT_DESTINATION_ADDRESS) if (resultCode == RESULT_OK) {
            try {
                sAddr = data.getStringExtra("add");
                if (!TextUtils.isEmpty(sAddr)) {
                    Type type = new TypeToken<CTripCabE>() {
                    }.getType();
                    CTripCabE cTripCabE = new Gson().fromJson(sAddr, type);
                    if (moToAddress == null) {
                        moToAddress = new CAddress();
                    }
                    moToAddress.setAddress(cTripCabE.getFormatted_Address_Fixed());
                    moToAddress.setLatitude(cTripCabE.getLatitude_Fixed());
                    moToAddress.setLongitude(cTripCabE.getLongitude_Fixed());
                    moToAddress.setAdminArea(cTripCabE.getArea_Fixed());
                    moToAddress.setLocality(cTripCabE.getLocality_Fixed());
                    moToAddress.setSubLocality(cTripCabE.getSublocality_Fixed());
                    setTo(moToAddress, false);
                    drawToMarkerCab(moToAddress);
                    mIsFromSelected = false;
                    bmcab = true;
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                                    cTripCabE.getLatitude_Fixed(), cTripCabE.getLongitude_Fixed()),
                            DEFAULT_ZOOM));
                    Gson gson = new Gson();
                    String json = gson.toJson(moToAddress);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                            .putString(Constants_CabE.PREF_TO_ADDRESS_FIXED, sAddr);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                            .putString(Constants_CabE.PREF_TO_ADDRESS, json);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
                    goDirections();
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG, "ActivityResult: ", e, CabEMain_act.this);
            }
        }

        if (requestCode == CGlobals_CabE.REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    Log.d(TAG, "YES");
                    startLocationUpdates();
                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    Log.d(TAG, "NO");
                    finish();
                    break;
                default:
                    break;
            }
        }
        if (!TextUtils.isEmpty(sAddr)) {
            CGlobals_lib_ss.getInstance().addRecentAddress(oAddr);
        }
    }

    protected boolean goDirections() {
        String sFromAdd = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getString(Constants_CabE.PREF_FROM_ADDRESS, "");
        String sToAdd = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getString(Constants_CabE.PREF_TO_ADDRESS, "");
        Type type = new TypeToken<CAddress>() {
        }.getType();
        if (moFromAddress == null) {
            moFromAddress = new CAddress();
        }
        if (moToAddress == null) {
            moToAddress = new CAddress();
        }
        moFromAddress = new Gson().fromJson(sFromAdd, type);
        moToAddress = new Gson().fromJson(sToAdd, type);
        if (!moFromAddress.hasLatitude() || !moFromAddress.hasLongitude() || !moToAddress.hasLatitude() || !moToAddress.hasLongitude()) {
            Toast.makeText(CabEMain_act.this,
                    "Please re-enter your start and destination address",
                    Toast.LENGTH_SHORT).show();
            mapReady();
            return false;
        }
        float results[] = new float[1];
        Location.distanceBetween(moFromAddress.getLatitude(), moFromAddress.getLongitude(),
                moToAddress.getLatitude(), moToAddress.getLongitude(), results);
        if (results[0] < Constants_lib_ss.DISTANCE_SAME_FROM) {
            Toast.makeText(CabEMain_act.this,
                    "Your start and destination are too close (less than 500 m).\nPlease change your start or destination",
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if ((moFromAddress.hasLatitude() && moFromAddress
                .hasLongitude())
                && ((moToAddress.hasLatitude() && moToAddress
                .hasLongitude()) || !TextUtils
                .isEmpty(moToAddress.getAddress()))) {
            mIvGreenPin.setVisibility(View.GONE);
            mIvRedPin.setVisibility(View.GONE);
            mIsGettingDirections = true;
            isTripFrozen = true;
            clearMap();
            drawFromMarkerCab(moFromAddress);
            drawToMarkerCab(moToAddress);
            mBounds.include(
                    new LatLng(moFromAddress.getLatitude(), moFromAddress
                            .getLongitude())).include(
                    new LatLng(moToAddress.getLatitude(), moToAddress
                            .getLongitude()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                    mBounds.build(), Constants_lib_ss.MAP_PADDING));
            mInDirectionMode = true;
            SSLog_SS.i(TAG, "Plotting directions");
            String url = CabEMain_act.this.getDirectionsUrl(
                    new LatLng(moFromAddress.getLatitude(), moFromAddress
                            .getLongitude()),
                    new LatLng(moToAddress.getLatitude(), moToAddress
                            .getLongitude()));
            mRlFromTo.setVisibility(View.GONE);
            GoogleDirection downloadTask = new GoogleDirection();
            downloadTask.execute(url);
            return true;
        } else {
            Toast.makeText(CabEMain_act.this,
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

    protected void startLocationUpdates() {
        MyLocation myLocation = new MyLocation(
                MyApplication_CabE.mVolleyRequestQueue, CabEMain_act.this, mGoogleApiClient);
        myLocation.getLocation(this, onLocationResult);

        if (ActivityCompat.checkSelfPermission(CabEMain_act.this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(CabEMain_act.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ...: " + pendingResult.toString());
    }

    @Override
    protected void callDriver(JSONArray jsonArray) {
        mMap.setPadding(0, 180, 0, 300);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                mZoomFitBounds.build(), 40));
        progressCancel();
        handler.postDelayed(runnableWhereAmI, Constants_CabE.DELAY_SHOW_FOR_HIRE_CABS);
    }

    @Override
    protected String getGeoCountryCode() {
        return MyApplication_CabE.getInstance().getPersistentPreference()
                .getString(Constants_CabE.PREF_CURRENT_COUNTRY, "");
    }

    protected void checkInternetGPS() {
        try {
            if (CGlobals_lib_ss.getInstance().checkConnected(
                    CabEMain_act.this)) {
                mIvNoConnection.setVisibility(View.GONE);
            } else {
                mIvNoConnection.setVisibility(View.VISIBLE);
            }
            Location location = CGlobals_lib_ss.getInstance().getMyLocation(CabEMain_act.this);
            if (location == null) {
                location = mCurrentLocation;
            }
            long diff = System.currentTimeMillis() - location.getTime();
            if (diff > Constants_lib_ss.DRIVER_UPDATE_INTERVAL * 10) {
                MyLocation myLocation = new MyLocation(
                        CGlobals_lib_ss.mVolleyRequestQueue, CabEMain_act.this, mGoogleApiClient);
                myLocation.getLocation(this, onLocationResult);
                mIvNoLocation.setVisibility(View.VISIBLE);
            } else {
                mIvNoLocation.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG, "checkInternetGPS: ", e, CabEMain_act.this);
        }
    }

    private void getForHire(final String sServiceCode) {
        if (llBlackYellow != null) {
            llBlackYellow.setBackgroundColor(Color.WHITE);
            llCoolCab.setBackgroundColor(Color.WHITE);
            llFleetTaxi.setBackgroundColor(Color.WHITE);
            llTaxiPrime.setBackgroundColor(Color.WHITE);
            llTaxiStandard.setBackgroundColor(Color.WHITE);
            llSanghini.setBackgroundColor(Color.WHITE);
            llRickshaw.setBackgroundColor(Color.WHITE);
            llShareCab.setBackgroundColor(Color.WHITE);
            switch (sServiceCode) {
                case Constants_CabE.SERVICE_CODE_BY:
                    llBlackYellow.setBackgroundColor(Color.CYAN);
                    break;
                case Constants_CabE.SERVICE_CODE_CC:
                    llCoolCab.setBackgroundColor(Color.CYAN);
                    break;
                case Constants_CabE.SERVICE_CODE_FT:
                    llFleetTaxi.setBackgroundColor(Color.CYAN);
                    break;
                case Constants_CabE.SERVICE_CODE_TTP:
                    llTaxiPrime.setBackgroundColor(Color.CYAN);
                    break;
                case Constants_CabE.SERVICE_CODE_TNP:
                    llTaxiStandard.setBackgroundColor(Color.CYAN);
                    break;
                case Constants_CabE.SERVICE_CODE_SG:
                    llSanghini.setBackgroundColor(Color.CYAN);
                    break;
                case Constants_CabE.SERVICE_CODE_AR:
                    llRickshaw.setBackgroundColor(Color.CYAN);
                    break;
                case Constants_CabE.SERVICE_CODE_ST:
                    llShareCab.setBackgroundColor(Color.CYAN);
                    break;
            }
        }

        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                .putString(Constants_CabE.PREF_SERVICE_CODE_SAVE, sServiceCode);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();

        String sFromAdd = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getString(Constants_CabE.PREF_FROM_ADDRESS, "");
        Type type = new TypeToken<CAddress>() {
        }.getType();
        final CAddress oFromAddress = new Gson().fromJson(sFromAdd, type);
        final String url = Constants_CabE.GET_FOR_HIRE_CAB_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        showForHire(response);
                        if (snackbar != null) {
                            snackbar.dismiss();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    snackbar = Snackbar.make(coordinatorLayout, "No internet Access, Check your internet connection",
                            Snackbar.LENGTH_LONG);
                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(CabEMain_act.this, R.color.gray));
                    snackbar.show();
                    showForHireError();
                    handler.postDelayed(runnableWhereAmI, Constants_CabE.DELAY_SHOW_FOR_HIRE_CABS);
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG, "getCabShow :-   ", error, CabEMain_act.this);
                    }
                } catch (Exception e) {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG, "getCabShow :-   ", e, CabEMain_act.this);
                    }
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<>();
                params.put("servicecode", sServiceCode);
                if (oFromAddress.hasLatitude() && oFromAddress.hasLongitude()) {
                    params.put("l", String.valueOf(oFromAddress.getLatitude()));
                    params.put("o", String.valueOf(oFromAddress.getLongitude()));
                } else {
                    Location location = CGlobals_lib_ss.getInstance().getMyLocation(CabEMain_act.this);
                    params.put("l", String.valueOf(location.getLatitude()));
                    params.put("o", String.valueOf(location.getLongitude()));
                }
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        url, CabEMain_act.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                try {
                    String url1 = url + "?verbose=Y&" + getParams.toString();
                    Log.i(TAG, "url  " + url1);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "getFoundDriver", e, CabEMain_act.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, CabEMain_act.this);
    }

    private void showForHire(String response) {
        ArrayList<CTripCabE> cTripCabEsarray = new ArrayList<>();
        textViewId();
        if (TextUtils.isEmpty(response) || response.equals("-1")) {
            handler.postDelayed(runnableWhereAmI,
                    Constants_CabE.DELAY_SHOW_FOR_HIRE_CABS);
            tvCCEta.setText(getString(R.string.no_cars));
            tvBYEta.setText(getString(R.string.no_cars));
            tvFTEta.setText(getString(R.string.no_cars));
            tvTPEta.setText(getString(R.string.no_cars));
            tvTOPEta.setText(getString(R.string.no_cars));
            tvSanghiniEta.setText(getString(R.string.no_cars));
            tvRickshawEta.setText(getString(R.string.no_cars));
            tvShareCabEta.setText(getString(R.string.no_cars));
            tvBYEta.setVisibility(View.VISIBLE);
            progressBarBY.setVisibility(View.GONE);
            tvCCEta.setVisibility(View.VISIBLE);
            progressBarCC.setVisibility(View.GONE);
            tvFTEta.setVisibility(View.VISIBLE);
            progressBarFT.setVisibility(View.GONE);
            tvTPEta.setVisibility(View.VISIBLE);
            progressBarTP.setVisibility(View.GONE);
            tvTOPEta.setVisibility(View.VISIBLE);
            progressBarTOP.setVisibility(View.GONE);
            tvSanghiniEta.setVisibility(View.VISIBLE);
            progressBarSanghini.setVisibility(View.GONE);
            tvRickshawEta.setVisibility(View.VISIBLE);
            progressBarRickshaw.setVisibility(View.GONE);
            tvShareCabEta.setVisibility(View.VISIBLE);
            progressBarShareCab.setVisibility(View.GONE);
            //llEstimatePayment.setVisibility(View.GONE);
            return;
        }
        if (response.equals("0")) {
            handler.postDelayed(runnableWhereAmI,
                    Constants_CabE.DELAY_SHOW_FOR_HIRE_CABS);
            tvCCEta.setText(getString(R.string.no_cars));
            tvBYEta.setText(getString(R.string.no_cars));
            tvFTEta.setText(getString(R.string.no_cars));
            tvTPEta.setText(getString(R.string.no_cars));
            tvTOPEta.setText(getString(R.string.no_cars));
            tvSanghiniEta.setText(getString(R.string.no_cars));
            tvRickshawEta.setText(getString(R.string.no_cars));
            tvShareCabEta.setText(getString(R.string.no_cars));
            tvBYEta.setVisibility(View.VISIBLE);
            progressBarBY.setVisibility(View.GONE);
            tvCCEta.setVisibility(View.VISIBLE);
            progressBarCC.setVisibility(View.GONE);
            tvFTEta.setVisibility(View.VISIBLE);
            progressBarFT.setVisibility(View.GONE);
            tvTPEta.setVisibility(View.VISIBLE);
            progressBarTP.setVisibility(View.GONE);
            tvTOPEta.setVisibility(View.VISIBLE);
            progressBarTOP.setVisibility(View.GONE);
            tvSanghiniEta.setVisibility(View.VISIBLE);
            progressBarSanghini.setVisibility(View.GONE);
            tvRickshawEta.setVisibility(View.VISIBLE);
            progressBarRickshaw.setVisibility(View.GONE);
            tvShareCabEta.setVisibility(View.VISIBLE);
            progressBarShareCab.setVisibility(View.GONE);
            return;
        }
        for (int i = 0; i < driverCurrentLocationMarkerArray.size(); i++) {
            marker = driverCurrentLocationMarkerArray.get(i);
            driverCurrentLocationMarkerArray.get(i).remove();
        }
        driverCurrentLocationMarkerArray.clear();
        try {
            JSONArray aJson = new JSONArray(response);
            iBYEta = -1;
            iCCEta = -1;
            iFTEta = -1;
            iTPEta = -1;
            iSCEta = -1;
            iSGEta = -1;
            iAREta = -1;
            iSTEta = -1;
            String s;
            tvCCEta.setText(getString(R.string.no_cars));
            tvBYEta.setText(getString(R.string.no_cars));
            tvFTEta.setText(getString(R.string.no_cars));
            tvTPEta.setText(getString(R.string.no_cars));
            tvTOPEta.setText(getString(R.string.no_cars));
            tvSanghiniEta.setText(getString(R.string.no_cars));
            tvRickshawEta.setText(getString(R.string.no_cars));
            tvShareCabEta.setText(getString(R.string.no_cars));
            //llEstimatePayment.setVisibility(View.GONE);
            isEstimatedBY = false;
            isEstimatedCC = false;
            isEstimatedFC = false;
            isEstimatedSC = false;
            isEstimatedPC = false;
            isEstimatedSG = false;
            isEstimatedAR = false;
            isEstimatedST = false;
            for (int i = 0; i < aJson.length(); i++) {
                JSONObject joVehicle = aJson.getJSONObject(i);
                cabTrip = new CTripCabE(aJson.getJSONObject(i).toString(), CabEMain_act.this);
                cTripCabEsarray.add(cabTrip);
                sVehicleNo = CGlobals_CabE.isNullNotDefined(joVehicle, "vehicleno") ?
                        "" : joVehicle.getString("vehicleno");
                String sServiceCode = CGlobals_CabE.isNullNotDefined(joVehicle, "service_code") ?
                        "" : joVehicle.getString("service_code");
                int etainmins = CGlobals_CabE.isNullNotDefined(joVehicle, "eta") ?
                        -1 : joVehicle.getInt("eta");
                // mTvUserName.setText(cabTrip.getFirstName());
                int iETA = etainmins == 0 ? 1 : etainmins;
                //llEstimatePayment.setVisibility(View.GONE);
                switch (sServiceCode) {
                    case Constants_CabE.SERVICE_CODE_BY:
                        if (sServiceCode.equals(msServiceCode)) {
                            for (int j = 0; j < cTripCabEsarray.size(); j++) {
                                if (msServiceCode.equals(cTripCabEsarray.get(j).getService_Code())) {
                                    marker = mMap.addMarker(new MarkerOptions()
                                            .position(cTripCabEsarray.get(j).getLatLng())
                                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("black_yellow", 50, 30)))
                                            .alpha(0.8f).draggable(true)
                                            .title(cTripCabEsarray.get(j).getVehicleNo()));
                                    driverCurrentLocationMarkerArray.add(marker);
                                }
                            }
                        }
                        if ((etainmins != -1 && etainmins < iBYEta) || iBYEta == -1) {
                            iBYEta = iETA;
                            s = Integer.toString(iBYEta) + " min.";
                            tvBYEta.setText(s);
                            isEstimatedBY = true;
                        }
                        break;
                    case Constants_CabE.SERVICE_CODE_CC:
                        if (sServiceCode.equals(msServiceCode)) {
                            for (int j = 0; j < cTripCabEsarray.size(); j++) {
                                if (msServiceCode.equals(cTripCabEsarray.get(j).getService_Code())) {
                                    marker = mMap.addMarker(new MarkerOptions()
                                            .position(cTripCabEsarray.get(j).getLatLng())
                                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("cool_cab", 50, 30)))
                                            .alpha(0.8f).draggable(true)
                                            .title(cTripCabEsarray.get(j).getVehicleNo()));
                                    driverCurrentLocationMarkerArray.add(marker);
                                }
                            }
                        }
                        if ((etainmins != -1 && etainmins < iCCEta) || iCCEta == -1) {
                            iCCEta = iETA;
                            s = Integer.toString(iCCEta) + " min.";
                            tvCCEta.setText(s);
                            isEstimatedCC = true;
                        }
                        break;
                    case Constants_CabE.SERVICE_CODE_FT:
                        if (sServiceCode.equals(msServiceCode)) {
                            for (int j = 0; j < cTripCabEsarray.size(); j++) {
                                if (msServiceCode.equals(cTripCabEsarray.get(j).getService_Code())) {
                                    marker = mMap.addMarker(new MarkerOptions()
                                            .position(cTripCabEsarray.get(j).getLatLng())
                                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("fleet_taxi", 50, 30)))
                                            .alpha(0.8f).draggable(true)
                                            .title(cTripCabEsarray.get(j).getVehicleNo()));
                                    driverCurrentLocationMarkerArray.add(marker);
                                }
                            }
                        }
                        if ((etainmins != -1 && etainmins < iFTEta) || iFTEta == -1) {
                            iFTEta = iETA;
                            s = Integer.toString(iFTEta) + " min.";
                            tvFTEta.setText(s);
                            isEstimatedFC = true;
                        }
                        break;
                    case Constants_CabE.SERVICE_CODE_TTP:
                        if (sServiceCode.equals(msServiceCode)) {
                            for (int j = 0; j < cTripCabEsarray.size(); j++) {
                                if (msServiceCode.equals(cTripCabEsarray.get(j).getService_Code())) {
                                    marker = mMap.addMarker(new MarkerOptions()
                                            .position(cTripCabEsarray.get(j).getLatLng())
                                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("prime", 50, 30)))
                                            .alpha(0.8f).draggable(true)
                                            .title(cTripCabEsarray.get(j).getVehicleNo()));
                                    driverCurrentLocationMarkerArray.add(marker);
                                }
                            }
                        }
                        if ((etainmins != -1 && etainmins < iTPEta) || iTPEta == -1) {
                            iTPEta = iETA;
                            s = Integer.toString(iTPEta) + " min.";
                            tvTPEta.setText(s);
                            isEstimatedPC = true;
                        }
                        break;
                    case Constants_CabE.SERVICE_CODE_TNP:
                        if (sServiceCode.equals(msServiceCode)) {
                            for (int j = 0; j < cTripCabEsarray.size(); j++) {
                                if (msServiceCode.equals(cTripCabEsarray.get(j).getService_Code())) {
                                    marker = mMap.addMarker(new MarkerOptions()
                                            .position(cTripCabEsarray.get(j).getLatLng())
                                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("standard", 50, 30)))
                                            .alpha(0.8f).draggable(true)
                                            .title(cTripCabEsarray.get(j).getVehicleNo()));
                                    driverCurrentLocationMarkerArray.add(marker);
                                }
                            }
                        }
                        if ((etainmins != -1 && etainmins < iSCEta) || iSCEta == -1) {
                            iSCEta = iETA;
                            s = Integer.toString(iSCEta) + " min.";
                            tvTOPEta.setText(s);
                            isEstimatedSC = true;
                        }
                        break;
                    case Constants_CabE.SERVICE_CODE_SG:
                        if (sServiceCode.equals(msServiceCode)) {
                            for (int j = 0; j < cTripCabEsarray.size(); j++) {
                                if (msServiceCode.equals(cTripCabEsarray.get(j).getService_Code())) {
                                    marker = mMap.addMarker(new MarkerOptions()
                                            .position(cTripCabEsarray.get(j).getLatLng())
                                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("standard", 50, 30)))
                                            .alpha(0.8f).draggable(true)
                                            .title(cTripCabEsarray.get(j).getVehicleNo()));
                                    driverCurrentLocationMarkerArray.add(marker);
                                }
                            }
                        }
                        if ((etainmins != -1 && etainmins < iSGEta) || iSGEta == -1) {
                            iSGEta = iETA;
                            s = Integer.toString(iSGEta) + " min.";
                            tvSanghiniEta.setText(s);
                            isEstimatedSG = true;
                        }
                        break;
                    case Constants_CabE.SERVICE_CODE_AR:
                        if (sServiceCode.equals(msServiceCode)) {
                            for (int j = 0; j < cTripCabEsarray.size(); j++) {
                                if (msServiceCode.equals(cTripCabEsarray.get(j).getService_Code())) {
                                    marker = mMap.addMarker(new MarkerOptions()
                                            .position(cTripCabEsarray.get(j).getLatLng())
                                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("rickshaw", 40, 60)))
                                            .alpha(0.8f).draggable(true)
                                            .title(cTripCabEsarray.get(j).getVehicleNo()));
                                    driverCurrentLocationMarkerArray.add(marker);
                                }
                            }
                        }
                        if ((etainmins != -1 && etainmins < iAREta) || iAREta == -1) {
                            iAREta = iETA;
                            s = Integer.toString(iAREta) + " min.";
                            tvRickshawEta.setText(s);
                            isEstimatedAR = true;
                        }
                        break;
                    case Constants_CabE.SERVICE_CODE_ST:
                        if (sServiceCode.equals(msServiceCode)) {
                            for (int j = 0; j < cTripCabEsarray.size(); j++) {
                                if (msServiceCode.equals(cTripCabEsarray.get(j).getService_Code())) {
                                    marker = mMap.addMarker(new MarkerOptions()
                                            .position(cTripCabEsarray.get(j).getLatLng())
                                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("share_cab", 40, 40)))
                                            .alpha(0.8f).draggable(true)
                                            .title(cTripCabEsarray.get(j).getVehicleNo()));
                                    driverCurrentLocationMarkerArray.add(marker);
                                }
                            }
                        }
                        if ((etainmins != -1 && etainmins < iSTEta) || iSTEta == -1) {
                            iSTEta = iETA;
                            s = Integer.toString(iSTEta) + " min.";
                            tvShareCabEta.setText(s);
                            isEstimatedST = true;
                        }
                        break;
                }
            }
            tvBYEta.setVisibility(View.VISIBLE);
            progressBarBY.setVisibility(View.GONE);
            tvCCEta.setVisibility(View.VISIBLE);
            progressBarCC.setVisibility(View.GONE);
            tvFTEta.setVisibility(View.VISIBLE);
            progressBarFT.setVisibility(View.GONE);
            tvTPEta.setVisibility(View.VISIBLE);
            progressBarTP.setVisibility(View.GONE);
            tvTOPEta.setVisibility(View.VISIBLE);
            progressBarTOP.setVisibility(View.GONE);
            tvSanghiniEta.setVisibility(View.VISIBLE);
            progressBarSanghini.setVisibility(View.GONE);
            tvRickshawEta.setVisibility(View.VISIBLE);
            progressBarRickshaw.setVisibility(View.GONE);
            tvShareCabEta.setVisibility(View.VISIBLE);
            progressBarShareCab.setVisibility(View.GONE);
        } catch (Exception e) {
            SSLog_SS.e(TAG, "showCabELocation: ", e, CabEMain_act.this);
        }
        handler.postDelayed(runnableWhereAmI, Constants_CabE.DELAY_SHOW_FOR_HIRE_CABS);
    }

    private void showForHireError() {
        textViewId();
        driverCurrentLocationMarkerArray.clear();
        try {
            tvCCEta.setText(getString(R.string.no_cars));
            tvBYEta.setText(getString(R.string.no_cars));
            tvFTEta.setText(getString(R.string.no_cars));
            tvTPEta.setText(getString(R.string.no_cars));
            tvTOPEta.setText(getString(R.string.no_cars));
            tvSanghiniEta.setText(getString(R.string.no_cars));
            tvRickshawEta.setText(getString(R.string.no_cars));
            tvShareCabEta.setText(getString(R.string.no_cars));
            //llEstimatePayment.setVisibility(View.GONE);
            tvBYEta.setVisibility(View.VISIBLE);
            progressBarBY.setVisibility(View.GONE);
            tvCCEta.setVisibility(View.VISIBLE);
            progressBarCC.setVisibility(View.GONE);
            tvFTEta.setVisibility(View.VISIBLE);
            progressBarFT.setVisibility(View.GONE);
            tvTPEta.setVisibility(View.VISIBLE);
            progressBarTP.setVisibility(View.GONE);
            tvTOPEta.setVisibility(View.VISIBLE);
            progressBarTOP.setVisibility(View.GONE);
            tvSanghiniEta.setVisibility(View.VISIBLE);
            progressBarSanghini.setVisibility(View.GONE);
            tvRickshawEta.setVisibility(View.VISIBLE);
            progressBarRickshaw.setVisibility(View.GONE);
            tvShareCabEta.setVisibility(View.VISIBLE);
            progressBarShareCab.setVisibility(View.GONE);
        } catch (Exception e) {
            SSLog_SS.e(TAG, "showCabEError: ", e, CabEMain_act.this);
        }
        handler.postDelayed(runnableWhereAmI, Constants_CabE.DELAY_SHOW_FOR_HIRE_CABS);
    }

    TextView tvCCEta, tvBYEta, tvFTEta, tvTPEta, tvTOPEta, tvSanghiniEta, tvRickshawEta, tvShareCabEta;
    ProgressBar progressBarBY, progressBarCC, progressBarFT, progressBarTP, progressBarTOP,
            progressBarSanghini, progressBarRickshaw, progressBarShareCab;

    private void textViewId() {
        tvCCEta = (TextView) findViewById(R.id.tvCCEta);
        tvBYEta = (TextView) findViewById(R.id.tvBYEta);
        tvFTEta = (TextView) findViewById(R.id.tvFTEta);
        tvTPEta = (TextView) findViewById(R.id.tvTPEta);
        tvTOPEta = (TextView) findViewById(R.id.tvTOPEta);
        tvSanghiniEta = (TextView) findViewById(R.id.tvSanghiniEta);
        tvRickshawEta = (TextView) findViewById(R.id.tvRickshawEta);
        tvShareCabEta = (TextView) findViewById(R.id.tvShareCabEta);

        progressBarBY = (ProgressBar) findViewById(R.id.progressBarBY);
        progressBarCC = (ProgressBar) findViewById(R.id.progressBarCC);
        progressBarFT = (ProgressBar) findViewById(R.id.progressBarFT);
        progressBarTP = (ProgressBar) findViewById(R.id.progressBarTP);
        progressBarTOP = (ProgressBar) findViewById(R.id.progressBarTOP);
        progressBarSanghini = (ProgressBar) findViewById(R.id.progressBarSanghini);
        progressBarRickshaw = (ProgressBar) findViewById(R.id.progressBarRickshaw);
        progressBarShareCab = (ProgressBar) findViewById(R.id.progressBarShareCab);
    }

    public Bitmap resizeMapIcons(String iconName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    Map<String, String> requestParams;
    private boolean isRequestCabe = false;

    private void requestCab(final String sTypeOfCabE) {
        isRequestCabe = false;
        try {
            if (pDialog != null) {
                pDialog.setMessage(CabEMain_act.this.getString(R.string.please_wait_cabe));
                pDialog.setCancelable(false);
                pDialog.show();
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG, "requestCab: ", e, CabEMain_act.this);
        }
        snackbar = Snackbar.make(coordinatorLayout, CabEMain_act.this.getString(R.string.please_wait_cabe),
                Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
        final String url = Constants_CabE.REQUEST_CAB_URL;

        String sFromAdd = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getString(Constants_CabE.PREF_FROM_ADDRESS, "");
        String sToAdd = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getString(Constants_CabE.PREF_TO_ADDRESS, "");
        Type type = new TypeToken<CAddress>() {
        }.getType();
        if (moFromAddress == null) {
            moFromAddress = new CAddress();
        }
        if (moToAddress == null) {
            moToAddress = new CAddress();
        }
        moFromAddress = new Gson().fromJson(sFromAdd, type);
        moToAddress = new Gson().fromJson(sToAdd, type);

        final String sTripPath = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getString(Constants_lib_ss.TRIP_PATH, "");

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        snackbar.dismiss();
                        isRequestCabe = false;
                        SSLog_SS.d("ResponseTripId  ", response);
                        requestCabSuccess(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                try {
                    isgotTrip = true;
                    snackbar.dismiss();
                    isRequestCabe = false;
                    if (pDialog != null) {
                        pDialog.setCancelable(true);
                        pDialog.cancel();
                    }
                    if (error instanceof NoConnectionError) {
                        snackbar = Snackbar.make(coordinatorLayout, "No internet Access, Check your internet connection",
                                Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                    Handler handler1 = new Handler();
                    for (int a = 1; a <= 3; a++) {
                        handler1.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (isgotTrip) {
                                    boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEMain_act.this);
                                    if (!isInternetCheck) {
                                        getActiveTrip();
                                    }
                                }
                            }
                        }, 5000);
                    }
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG, "createTrip - ", error, CabEMain_act.this);
                    }
                } catch (Exception e) {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG, "createTrip - ", e, CabEMain_act.this);
                    }
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                if (isRequestCabe) {
                    requestParams = new HashMap<>();
                    return CGlobals_lib_ss.getInstance().checkParams(requestParams);
                }
                isRequestCabe = true;
                requestParams = new HashMap<>();
                if (!TextUtils.isEmpty(mDistance)) {
                    requestParams.put("tripdistance", mDistance);
                }
                if (!TextUtils.isEmpty(sTripPath)) {
                    requestParams.put("trip_directions_polyline", sTripPath);
                }
                if (iDuration != 0) {
                    requestParams.put("triptime", String.valueOf(iDuration));
                }
                requestParams.put("servicecode", sTypeOfCabE);
                if (!TextUtils.isEmpty(moFromAddress.getAddress())) {
                    requestParams.put("fromaddress", moFromAddress.getAddress());
                    requestParams.put("fromshortaddress", moFromAddress.getShortAddress());
                    requestParams.put("fromsublocality", moFromAddress.getSubLocality1());
                }
                if (!TextUtils.isEmpty(moToAddress.getAddress())) {
                    requestParams.put("toaddress", moToAddress.getAddress());
                    requestParams.put("toshortaddress", moToAddress.getShortAddress());
                    requestParams.put("tosublocality", moToAddress.getSubLocality1());
                }
                if (moFromAddress.hasLatitude() && moFromAddress.hasLongitude()) {
                    requestParams.put("fromlat", Double.toString(moFromAddress.getLatitude()));
                    requestParams.put("fromlng", Double.toString(moFromAddress.getLongitude()));
                }
                if (moToAddress.hasLatitude() && moToAddress.hasLongitude()) {
                    requestParams.put("tolat", Double.toString(moToAddress.getLatitude()));
                    requestParams.put("tolng", Double.toString(moToAddress.getLongitude()));
                }
                requestParams = CGlobals_lib_ss.getInstance().getBasicMobileParamsShort(requestParams,
                        url, CabEMain_act.this);
                requestParams.put("plannedstartdatetime", requestParams.get("ct"));
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : requestParams.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                try {
                    String url1 = url + "?" + getParams.toString();
                    Log.i(TAG, "url  " + url1);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "getFoundDriver", e, CabEMain_act.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(requestParams);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, true, CabEMain_act.this);
    }

    private void requestCabSuccess(String response) {
        if (pDialog != null) {
            pDialog.setCancelable(true);
            pDialog.cancel();
        }
        if (TextUtils.isEmpty(response.trim()) || response.trim().equals("-1")) {
            isgotTrip = true;
            Handler handler1 = new Handler();
            for (int a = 1; a <= 3; a++) {
                handler1.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isgotTrip) {
                            boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEMain_act.this);
                            if (!isInternetCheck) {
                                getActiveTrip();
                            }
                        }
                    }
                }, 5000);
            }
            return;
        }
        try {
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                    .putString(Constants_CabE.CABE_REQUEST_RESPONSE, response);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
            JSONObject jResponse = new JSONObject(response);

            try {
                String sFromAddress = CGlobals_CabE.isNullNotDefined(jResponse, "fromaddress") ?
                        "" : jResponse.getString("fromaddress");

                double dFromLat = CGlobals_CabE.isNullNotDefined(jResponse, "fromlat") ?
                        Constants_CabE.INVALIDLAT : jResponse.getDouble("fromlat");

                double dFromLng = CGlobals_CabE.isNullNotDefined(jResponse, "fromlng") ?
                        Constants_CabE.INVALIDLNG : jResponse.getDouble("fromlng");

                String sToAddress = CGlobals_CabE.isNullNotDefined(jResponse, "toaddress") ?
                        "" : jResponse.getString("toaddress");

                double dToLat = CGlobals_CabE.isNullNotDefined(jResponse, "tolat") ?
                        Constants_CabE.INVALIDLAT : jResponse.getDouble("tolat");

                double dToLng = CGlobals_CabE.isNullNotDefined(jResponse, "tolng") ?
                        Constants_CabE.INVALIDLNG : jResponse.getDouble("tolng");

                if (moFromAddress == null) {
                    moFromAddress = new CAddress();
                }
                moFromAddress.setAddress(sFromAddress);
                moFromAddress.setLatitude(dFromLat);
                moFromAddress.setLongitude(dFromLng);
                Gson gson = new Gson();
                String jsonFrom = gson.toJson(moFromAddress);
                if (moToAddress == null) {
                    moToAddress = new CAddress();
                }
                moToAddress.setAddress(sToAddress);
                moToAddress.setLatitude(dToLat);
                moToAddress.setLongitude(dToLng);
                String jsonTo = gson.toJson(moToAddress);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                        .putString(Constants_CabE.PREF_FROM_ADDRESS, jsonFrom);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                        .putString(Constants_CabE.PREF_TO_ADDRESS, jsonTo);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
            } catch (Exception e) {
                SSLog_SS.e(TAG, "requestCab adds: ", e, CabEMain_act.this);
            }

            int iDriverFound = CGlobals_CabE.isNullNotDefined(jResponse, "driver_found") ?
                    -1 : jResponse.getInt("driver_found");

            if (iDriverFound == 0) {
                snackbar = Snackbar.make(coordinatorLayout, "Sorry no driver available. Please try again", Snackbar.LENGTH_LONG);
                snackbar.show();
                return;
            }

            trip_id = jResponse.isNull("trip_id") ? -1 : jResponse.getInt("trip_id");
            int driver_connected = CGlobals_CabE.isNullNotDefined(jResponse, "driver_connected") ? -1 : jResponse.getInt("driver_connected");
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).
                    putInt(Constants_CabE.PREF_TRIP_ID_PASSENGER, trip_id);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
            //mMap.clear();
            whereAmI = WAITING_FOR_DRIVER;
            setWhereAmI(whereAmI);
            //notificationMessage("Your booking has been confirmed");
            if (driver_connected == 0) {
                if (!isRequestCheckDriverConnection) {
                    isRequestCheckDriverConnection = true;
                    AlertDialog.Builder builder = new AlertDialog.Builder(CabEMain_act.this);
                    builder.setMessage("something went wrong. Please try again")
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }

            }
            getDriverInfo();
        } catch (Exception e) {
            SSLog_SS.e(TAG, "requestCabSuccess: ", e, CabEMain_act.this);
            if (pDialog != null) {
                pDialog.setCancelable(true);
                pDialog.cancel();
            }
        }
    }

    private void getActiveTrip() {
        if (pDialog != null) {
            pDialog.setMessage(CabEMain_act.this.getString(R.string.please_wait_cabe));
            pDialog.setCancelable(false);
            pDialog.show();
        }
        final String url = Constants_CabE.GOT_CAB_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        SSLog_SS.d("ResponseTripId  ", response);
                        activeTripSuccess(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                try {
                    isgotTrip = true;
                    if (pDialog != null) {
                        pDialog.setCancelable(true);
                        pDialog.cancel();
                    }
                    if (error instanceof NoConnectionError) {
                        snackbar = Snackbar.make(coordinatorLayout, "No internet Access, Check your internet connection",
                                Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG, "getActiveTrip - ", error, CabEMain_act.this);
                    }
                } catch (Exception e) {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG, "getActiveTrip - ", e, CabEMain_act.this);
                    }
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        url, CabEMain_act.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                try {
                    String url2 = url + "?" + getParams.toString();
                    Log.i(TAG, "url  " + url2);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "getActiveTrip", e, CabEMain_act.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, true, CabEMain_act.this);
    }

    private void activeTripSuccess(String response) {
        if (TextUtils.isEmpty(response.trim()) || response.trim().equals("-1")) {
            isgotTrip = true;
            if (pDialog != null) {
                pDialog.setCancelable(true);
                pDialog.cancel();
            }
            switch (msServiceCode) {
                case Constants_CabE.SERVICE_CODE_BY:
                    Toast.makeText(CabEMain_act.this, "No " + getString(R.string.service_black_and_yellow) + " available", Toast.LENGTH_LONG).show();
                    break;
                case Constants_CabE.SERVICE_CODE_CC:
                    Toast.makeText(CabEMain_act.this, "No " + getString(R.string.service_coolcab) + " available", Toast.LENGTH_LONG).show();
                    break;
                case Constants_CabE.SERVICE_CODE_FT:
                    Toast.makeText(CabEMain_act.this, "No " + getString(R.string.service_fleet_taxi) + " available", Toast.LENGTH_LONG).show();
                    break;
                case Constants_CabE.SERVICE_CODE_TTP:
                    Toast.makeText(CabEMain_act.this, "No " + getString(R.string.service_prime) + " available", Toast.LENGTH_LONG).show();
                    break;
                case Constants_CabE.SERVICE_CODE_TNP:
                    Toast.makeText(CabEMain_act.this, "No " + getString(R.string.service_standard) + " available", Toast.LENGTH_LONG).show();
                    break;
                case Constants_CabE.SERVICE_CODE_SG:
                    Toast.makeText(CabEMain_act.this, "No " + getString(R.string.sanghini) + " available", Toast.LENGTH_LONG).show();
                    break;
                case Constants_CabE.SERVICE_CODE_AR:
                    Toast.makeText(CabEMain_act.this, "No " + getString(R.string.rickshaw) + " available", Toast.LENGTH_LONG).show();
                    break;
            }
            return;
        }
        try {
            isgotTrip = false;
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                    .putString(Constants_CabE.CABE_REQUEST_RESPONSE, response);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
            JSONObject jResponse = new JSONObject(response);

            try {
                String sFromAddress = CGlobals_CabE.isNullNotDefined(jResponse, "fromaddress") ?
                        "" : jResponse.getString("fromaddress");

                double dFromLat = CGlobals_CabE.isNullNotDefined(jResponse, "fromlat") ?
                        Constants_CabE.INVALIDLAT : jResponse.getDouble("fromlat");

                double dFromLng = CGlobals_CabE.isNullNotDefined(jResponse, "fromlng") ?
                        Constants_CabE.INVALIDLNG : jResponse.getDouble("fromlng");

                String sToAddress = CGlobals_CabE.isNullNotDefined(jResponse, "toaddress") ?
                        "" : jResponse.getString("toaddress");

                double dToLat = CGlobals_CabE.isNullNotDefined(jResponse, "tolat") ?
                        Constants_CabE.INVALIDLAT : jResponse.getDouble("tolat");

                double dToLng = CGlobals_CabE.isNullNotDefined(jResponse, "tolng") ?
                        Constants_CabE.INVALIDLNG : jResponse.getDouble("tolng");

                if (moFromAddress == null) {
                    moFromAddress = new CAddress();
                }
                moFromAddress.setAddress(sFromAddress);
                moFromAddress.setLatitude(dFromLat);
                moFromAddress.setLongitude(dFromLng);
                Gson gson = new Gson();
                String jsonFrom = gson.toJson(moFromAddress);
                if (moToAddress == null) {
                    moToAddress = new CAddress();
                }
                moToAddress.setAddress(sToAddress);
                moToAddress.setLatitude(dToLat);
                moToAddress.setLongitude(dToLng);
                String jsonTo = gson.toJson(moToAddress);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                        .putString(Constants_CabE.PREF_FROM_ADDRESS, jsonFrom);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                        .putString(Constants_CabE.PREF_TO_ADDRESS, jsonTo);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
            } catch (Exception e) {
                SSLog_SS.e(TAG, "requestCab adds: ", e, CabEMain_act.this);
            }

            int iDriverFound = CGlobals_CabE.isNullNotDefined(jResponse, "driver_found") ?
                    -1 : jResponse.getInt("driver_found");

            if (iDriverFound == 0) {
                snackbar = Snackbar.make(coordinatorLayout, "Sorry no driver available. Please try again", Snackbar.LENGTH_LONG);
                snackbar.show();
                return;
            }

            trip_id = jResponse.isNull("trip_id") ? -1
                    : jResponse.getInt("trip_id");
            int driver_connected = CGlobals_CabE.isNullNotDefined(jResponse, "driver_connected") ?
                    -1 : jResponse.getInt("driver_connected");
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).
                    putInt(Constants_CabE.PREF_TRIP_ID_PASSENGER, trip_id);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
            //mMap.clear();
            whereAmI = WAITING_FOR_DRIVER;
            setWhereAmI(whereAmI);
            if (driver_connected == 0) {
                if (!isRequestCheckDriverConnection) {
                    isRequestCheckDriverConnection = true;
                    AlertDialog.Builder builder = new AlertDialog.Builder(CabEMain_act.this);
                    builder.setMessage("something went wrong. Please try again")
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }

            }
            getDriverInfo();
        } catch (Exception e) {
            SSLog_SS.e(TAG, "activeTripSuccess: ", e, CabEMain_act.this);
            if (pDialog != null) {
                pDialog.setCancelable(true);
                pDialog.cancel();
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private void getDriverInfo() {
        String response = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getString(Constants_CabE.CABE_REQUEST_RESPONSE, "");
        try {
            if (pDialog != null) {
                pDialog.setCancelable(true);
                pDialog.cancel();
            }
            cabTrip_Driver_info = new CTripCabE(response, CabEMain_act.this);
            if (cabTrip_Driver_info.getTripId() == -1) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(CabEMain_act.this);
                alertDialog.setTitle("Sorry no driver available. Please try again");
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEMain_act.this);
                        if (!isInternetCheck) {
                            noCabFound();
                        }
                    }
                });
                if (!CabEMain_act.this.isFinishing()) {
                    alertDialog.show();
                }
                return;
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG, "getDriverInfo: ", e, CabEMain_act.this);
        }
        try {
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).
                    putInt(Constants_CabE.SAVE_RATING_COUNT, 0);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
            JSONObject jResponse = new JSONObject(response);
            trip_id = jResponse.isNull("trip_id") ? -1
                    : jResponse.getInt("trip_id");
            int etainmins = CGlobals_CabE.isNullNotDefined(jResponse, "eta") ?
                    -1 : jResponse.getInt("eta");
            if (!isLocationServiceRunning(LocationService.class)) {
                StartLocationService();
            }
            if (whereAmI != IN_CAB) {
                whereAmIUI(whereAmI);
                String sFromAdd = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                        .getString(Constants_CabE.PREF_FROM_ADDRESS, "");
                Type type = new TypeToken<CAddress>() {
                }.getType();
                CAddress oFromAddress = new Gson().fromJson(sFromAdd, type);
                markerFrom = mMap
                        .addMarker(new MarkerOptions()
                                .position(new LatLng(oFromAddress.getLatitude(), oFromAddress.getLongitude()))
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_you_marker))
                                .alpha(0.8f).draggable(true));
            }
            if (!TextUtils.isEmpty(cabTrip_Driver_info.getImage_Name()) &&
                    !TextUtils.isEmpty(cabTrip_Driver_info.getImage_Path())) {
                String url = Constants_CabE.GET_USER_PROFILE_IMAGE_FILE_NAME_URL + cabTrip_Driver_info.getImage_Path() +
                        cabTrip_Driver_info.getImage_Name();

                ImageRequest request = new ImageRequest(url,
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap bitmap) {
                                if (Build.VERSION.SDK_INT < 11) {
                                    Toast.makeText(CabEMain_act.this, getString(R.string.api11not_cabe), Toast.LENGTH_LONG).show();
                                } else {
                                    ivDriverDisplayImage.setImageBitmap(bitmap);
                                    ivDriverDisplayImage.setVisibility(View.VISIBLE);
                                }
                            }
                        }, 0, 0, ImageView.ScaleType.FIT_XY, Bitmap.Config.ARGB_8888,
                        new Response.ErrorListener() {
                            public void onErrorResponse(VolleyError error) {
                                ivDriverDisplayImage.setImageResource(R.drawable.ic_driver);
                            }
                        });
                MyApplication_CabE.getInstance().addToRequestQueue(request);
            } else {
                ivDriverDisplayImage.setImageResource(R.drawable.ic_driver);
            }
            if (mLlDriverInfo != null) {
                mLlDriverInfo.setVisibility(View.VISIBLE);
            }
            tvDriverDisplayName.setText((TextUtils.isEmpty(cabTrip_Driver_info.getFirstName()) ? ""
                    : cabTrip_Driver_info.getFirstName() + " " + cabTrip_Driver_info.getLastName()));
            if (etainmins != -1) {
                String sEta;
                if (etainmins < 3) {
                    sEta = "less than 3 min.";
                } else {
                    sEta = Integer.toString(etainmins) + " min.";
                }
                if (tvDriverEta != null) {
                    tvDriverEta.setText(sEta);
                }
            }
            String s = cabTrip_Driver_info.getVehicleCompany() + " " + cabTrip_Driver_info.getVehicleModel();
            tvDriverCarName.setText(s);
            ivDriverCarImage.setVisibility(View.GONE);
            try {
                String ss = cabTrip_Driver_info.getVehicleNo().substring(cabTrip_Driver_info.getVehicleNo().lastIndexOf(' ') + 1);
                String[] separated = cabTrip_Driver_info.getVehicleNo().split(ss);
                tvDriverCarNumber.setText(separated[0] + "\n" + ss);
            } catch (Exception e) {
                SSLog_SS.e(TAG, "getDriverInfo: ", e, CabEMain_act.this);
            }
            ivDriverNumberCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + cabTrip_Driver_info.getDriver_Phoneno()));
                    startActivity(callIntent);
                }
            });
            Gson gson = new Gson();
            String json = gson.toJson(cabTrip_Driver_info);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                    .putString(Constants_CabE.CURRENT_CAB_TRIP, json);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
        } catch (Exception e) {
            SSLog_SS.e(TAG, "getDriverInfo: ", e, CabEMain_act.this);
        }
    }

    private void noCabFound() {
        final String url = Constants_CabE.NO_CAB_FOUND_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            mMap.clear();
                        } catch (Exception e) {
                            SSLog_SS.e(TAG, "noCabFound: ", e, CabEMain_act.this);
                        }
                        if (TextUtils.isEmpty(response) || response.trim().equals("-1")) {
                            return;
                        }
                        setWhereAmI(LOOKING_FOR_CAB); // trip cancelled, back to looking
                        StopLocationService();
                        Log.i(TAG, response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String sErr = CGlobals_lib_ss.getInstance().getVolleyError(error);
                try {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.d(TAG,
                                "Failed to mVehicleNoVerify :-  "
                                        + error.getMessage());
                    }
                } catch (Exception e) {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG, "mVehicleNoVerify - " + sErr,
                                error, CabEMain_act.this);
                    }
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("canceluser", "P");
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        url, CabEMain_act.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                String debugUrl;
                try {
                    debugUrl = url + "?" + getParams.toString();
                    Log.e(TAG, debugUrl);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "getPassengers map - ", e, CabEMain_act.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, CabEMain_act.this);
    }

    private void cancelTrip() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(CabEMain_act.this);
        alertDialog.setTitle("Are you sure do you cancel your trip?");
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEMain_act.this);
                if (!isInternetCheck) {
                    getCancelReasons();
                }
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        if (!CabEMain_act.this.isFinishing()) {
            alertDialog.show();
        }
    }

    private void tripCanceledByDriver() {
        if (whereAmI != WAITING_FOR_DRIVER)
            return;
        setWhereAmI(LOOKING_FOR_CAB);
        mMap.clear();
        clearToFromPath();
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(CabEMain_act.this);
        alertDialog.setTitle("Trip canceled by driver");
        alertDialog.setMessage("We apologize, Your trip was cancelled by the driver.\nDo you want to rebook?");
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        if (!CabEMain_act.this.isFinishing()) {
            alertDialog.show();
        }
    }

    private void getCancelReasons() {
        final String url = Constants_CabE.GET_CANCEL_REASONS_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        driverReasonForCancelTrip(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String sErr = CGlobals_lib_ss.getInstance().getVolleyError(error);
                try {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.d(TAG,
                                "Failed to mVehicleNoVerify :-  "
                                        + error.getMessage());
                    }
                } catch (Exception e) {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG, "mVehicleNoVerify - " + sErr,
                                error, CabEMain_act.this);
                    }
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("canceluser", "P");
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        url, CabEMain_act.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                String debugUrl;
                try {
                    debugUrl = url + "?" + getParams.toString();
                    Log.e(TAG, debugUrl);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "getPassengers map - ", e, CabEMain_act.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, CabEMain_act.this);
    }

    private void driverReasonForCancelTrip(String response) {
        if (TextUtils.isEmpty(response) || response.equals("-1")) {
            return;
        }
        try {
            final Dialog dialog = new Dialog(CabEMain_act.this, R.style.AppCompatAlertDialogStyle);
            dialog.setContentView(R.layout.passenger_cancel_trip_reason);
            dialog.setCancelable(false);
            final ListView lvReasonList;
            lvReasonList = (ListView) dialog.findViewById(R.id.lvReasonList);
            arrayCancelReasonList = new ArrayList<>();
            JSONArray aJson = new JSONArray(response);
            for (int i = 0; i < aJson.length(); i++) {
                cTripcedReason = new CTripCabE(aJson.getJSONObject(i).toString(), CabEMain_act.this);
                arrayCancelReasonList.add(cTripcedReason);
            }
            lvReasonList.setAdapter(new Cancel_Reason_Adapter(CabEMain_act.this, arrayCancelReasonList));
            lvReasonList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String sCancelReasonCode = arrayCancelReasonList.get(position).getCancel_Reason_Code();
                    if (TextUtils.isEmpty(sCancelReasonCode)) {
                        Toast.makeText(CabEMain_act.this, "No Reason", Toast.LENGTH_SHORT).show();
                    } else {
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                                .putString(Constants_CabE.PREF_CANCEL_REASON_CODE, sCancelReasonCode);
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
                        boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEMain_act.this);
                        if (!isInternetCheck) {
                            sendCancelTrip(sCancelReasonCode);
                        }
                    }
                    dialog.dismiss();
                }
            });
            dialog.show();
        } catch (Exception e) {
            SSLog_SS.e(TAG, "ReasonCancel: ", e, CabEMain_act.this);
        }
    }


    private void sendCancelTrip(final String sCancelReasonCode) {
        final int itrip_id = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this).
                getInt(Constants_CabE.PREF_TRIP_ID_PASSENGER, -1);
        final String sServiceCode = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getString(Constants_CabE.PREF_SERVICE_CODE_SAVE, "");
        final String url = Constants_CabE.CANCEL_CAB_TRIP_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getCancelCabTrip(response);
                        mMap.clear();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String sErr = CGlobals_lib_ss.getInstance().getVolleyError(error);
                try {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.d(TAG,
                                "Failed to sendCancelTrip :-  "
                                        + error.getMessage());
                    }
                } catch (Exception e) {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG, "sendCancelTrip - " + sErr,
                                error, CabEMain_act.this);
                    }
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                if (sServiceCode.equals(Constants_CabE.SERVICE_CODE_ST)) {
                    params.put("triptype", "P");
                } else {
                    params.put("triptype", "A");
                }
                params.put("tripid", String.valueOf(itrip_id));
                params.put("cancelreasoncode", sCancelReasonCode);
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        url, CabEMain_act.this);
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, CabEMain_act.this);
    }

    private void getCancelCabTrip(String response) {
        if (TextUtils.isEmpty(response) || response.trim().equals("-1")) {
            return;
        }
        setWhereAmI(LOOKING_FOR_CAB); // trip cancelled, back to looking
        StopLocationService();
        clearToFromPath();
        Log.i(TAG, response);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverPassenger);
        } catch (Exception e) {
            SSLog_SS.e(TAG, "onPause: ", e, CabEMain_act.this);
        }
        if ((pDialog != null) && pDialog.isShowing())
            pDialog.dismiss();
    }

    private void driverGetTripStatus() {
        final int itrip_id = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this).
                getInt(Constants_CabE.PREF_TRIP_ID_PASSENGER, -1);
        final String sServiceCode = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getString(Constants_CabE.PREF_SERVICE_CODE_SAVE, "");
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_CabE.GET_TRIP_STATUS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).
                                putString(Constants_CabE.PREF_UPDATE_DRIVER_RESPONSE_STATUS, response);
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
                        new getCabEDriverStatus().execute("");
                        handler.postDelayed(runnableWhereAmI, Constants_CabE.DRIVER_GET_TRIP_STATUS_INTERVAL);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handler.postDelayed(runnableWhereAmI, Constants_CabE.DRIVER_GET_TRIP_STATUS_INTERVAL);
                new getCabEDriverStatusError().execute("");
                String sErr = CGlobals_lib_ss.getInstance().getVolleyError(error);
                try {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.d(TAG,
                                "Failed to sendCancelTrip :-  "
                                        + error.getMessage());
                    }
                } catch (Exception e) {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG, "sendCancelTrip - " + sErr,
                                error, CabEMain_act.this);
                    }
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                if (sServiceCode.equals(Constants_CabE.SERVICE_CODE_ST)) {
                    params.put("triptype", "P");
                } else {
                    params.put("triptype", "A");
                }
                params.put("tripid", String.valueOf(itrip_id));
                params = CGlobals_lib_ss.getInstance().getBasicMobileParamsShort(params,
                        Constants_CabE.GET_TRIP_STATUS, CabEMain_act.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_CabE.GET_TRIP_STATUS;
                try {
                    String url = url1 + "?" + getParams.toString();
                    System.out.println("url  " + url);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "getFoundDriver", e, CabEMain_act.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, CabEMain_act.this);
    }

    private void tripHasEnded() {
        if (whereAmI == IN_CAB || whereAmI == WAITING_FOR_DRIVER) {
            setWhereAmI(LOOKING_FOR_CAB);
            bmcab = false;
            mMap.clear();
            updateTripRating(1);
            clearToFromPath();
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                    .putInt(Constants_CabE.WHERE_AM_I, LOOKING_FOR_CAB);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                    .putFloat(Constants_CabE.DRIVER_CAB_LAT, (float) 0.0);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                    .putFloat(Constants_CabE.DRIVER_CAB_LNG, (float) 0.0);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
            handler.postDelayed(runnableWhereAmI, Constants_CabE.DELAY_SHOW_FOR_HIRE_CABS);
        }
    }

    private void updateTripRating(final int value) {
        try {
            ratingCount = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this).
                    getInt(Constants_CabE.SAVE_RATING_COUNT, 0);
            ratingCount = ratingCount + 1;
            CGlobals_CabE.getInstance().isRatingNotSubmit = true;
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).
                    putBoolean(Constants_CabE.SAVE_RATING_NOT_SEND, CGlobals_CabE.getInstance().isRatingNotSubmit);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).
                    putInt(Constants_CabE.SAVE_RATING_COUNT, ratingCount);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
            comment = "";
            String sDrive = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                    .getString(Constants_CabE.CURRENT_TRACKED_TRIP, "");
            cabTrip = new CTripCabE(sDrive, CabEMain_act.this);
            final Dialog dialog = new Dialog(CabEMain_act.this, R.style.AppCompatAlertDialogStyle);
            dialog.setContentView(R.layout.update_rating_comment);
            dialog.setTitle(cabTrip.getFirstName());
            final RatingBar ratingBar1;
            final TextView submit, tvDriverRating;
            final NetworkImageView mProImage;
            final EditText cleComment;
            ratingBar1 = (RatingBar) dialog.findViewById(R.id.ratingBar1);
            submit = (TextView) dialog.findViewById(R.id.submit);
            mProImage = (NetworkImageView) dialog.findViewById(R.id.ivPassengerImage1);
            tvDriverRating = (TextView) dialog.findViewById(R.id.tvDriverRating);
            cleComment = (EditText) dialog.findViewById(R.id.cleComment);
            int itrip_id = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this).
                    getInt(Constants_CabE.PREF_TRIP_ID_PASSENGER, -1);
            tvDriverRating.setText(getString(R.string.We_hope_you_enjoyed_your_trip) + "\n" + itrip_id);
            if (cleComment != null) {
                cleComment.setText("");
            }
            float ratingber = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                    .getFloat("RATING", 0.0f);
            ratingBar1.setRating(ratingber);
            ratingBar1.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                public void onRatingChanged(RatingBar ratingBar, float rating,
                                            boolean fromUser) {
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).putFloat("RATING", rating);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
                }
            });
            if (!TextUtils.isEmpty(cabTrip.getImage_Name()) &&
                    !TextUtils.isEmpty(cabTrip.getImage_Path())) {

                String url = Constants_CabE.GET_USER_PROFILE_IMAGE_FILE_NAME_URL + cabTrip.getImage_Path() +
                        cabTrip.getImage_Name();
                mImageLoader.get(url, ImageLoader.getImageListener(mProImage,
                        R.drawable.ic_driver, R.drawable.ic_driver));
                mProImage.setImageUrl(url, mImageLoader);
            } else {
                String url = "https://www.google.co.in";
                mImageLoader.get(url, ImageLoader.getImageListener(mProImage,
                        R.drawable.ic_driver, R.drawable.ic_driver));
                mProImage.setImageUrl(url, mImageLoader);
            }
            mProImage.setImageResource(R.drawable.ic_driver);
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (cleComment != null) {
                        comment = cleComment.getText().toString();
                    }
                    float ratingber = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                            .getFloat("RATING", 0.0f);
                    boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEMain_act.this);
                    if (!isInternetCheck) {
                        updateTripRating_CallServer(comment, ratingber, value);
                    }
                    dialog.getContext();
                    dialog.cancel();
                }
            });
            dialog.show();
        } catch (Exception e) {
            SSLog_SS.e(TAG, "TripRating: ", e, CabEMain_act.this);
        }
    }

    private void updateTripRating_CallServer(final String comment, final float ratingber, final int value) {
        final int itrip_id = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this).
                getInt(Constants_CabE.PREF_TRIP_ID_PASSENGER, -1);
        final String sServiceCode = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getString(Constants_CabE.PREF_SERVICE_CODE_SAVE, "");
        final String url = Constants_CabE.UPDATE_TRIP_RATING_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        CGlobals_CabE.getInstance().isRatingNotSubmit = false;
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).
                                putBoolean(Constants_CabE.SAVE_RATING_NOT_SEND, CGlobals_CabE.getInstance().isRatingNotSubmit);
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
                        if (value == 1) {
                            Toast.makeText(CabEMain_act.this, "We hope you enjoyed your trip.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CabEMain_act.this, "Rating Submit Successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CGlobals_CabE.getInstance().isRatingNotSubmit = true;
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).
                        putBoolean(Constants_CabE.SAVE_RATING_NOT_SEND, CGlobals_CabE.getInstance().isRatingNotSubmit);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
                if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                    Log.e(TAG, "");
                } else {
                    SSLog_SS.e(TAG, "updateTripRating:- ", error, CabEMain_act.this);
                }

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tripid", String.valueOf(itrip_id));
                if (sServiceCode.equals(Constants_CabE.SERVICE_CODE_ST)) {
                    params.put("triptype", "P");
                } else {
                    params.put("triptype", "A");
                }
                params.put("triprating", String.valueOf(ratingber));
                if (!TextUtils.isEmpty(comment)) {
                    params.put("tripcomment", comment);
                }
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        url, CabEMain_act.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                String debugUrl;
                try {
                    debugUrl = url + "?" + getParams.toString();
                    Log.e(TAG, debugUrl);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "updateTripRating:- ", e, CabEMain_act.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        getMyApplication().addVolleyRequest(postRequest, true);
    }

    MyApplication_CabE getMyApplication() {
        if (myApplication == null) {
            myApplication = MyApplication_CabE.getInstance();
        }
        return myApplication;
    }


    private class GetLocationAsyncFrom extends AsyncTask<String, Void, String> {
        double xFrom, yFrom;
        StringBuilder str;
        TextView mTvAddressFrom;

        GetLocationAsyncFrom(double latitude, double longitude, TextView tvAddress) {
            xFrom = latitude;
            yFrom = longitude;
            mTvAddressFrom = tvAddress;
        }

        @Override
        protected void onPreExecute() {
            mTvAddressFrom.setHint(" Getting address ");
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Geocoder geocoder = new Geocoder(CabEMain_act.this, Locale.ENGLISH);
                addresses = geocoder.getFromLocation(xFrom, yFrom, 1);
                str = new StringBuilder();
                if (Geocoder.isPresent()) {
                    Address returnAddress = addresses.get(0);
                    String localityString = returnAddress.getLocality();
                    String city = returnAddress.getCountryName();
                    String region_code = returnAddress.getCountryCode();
                    String zipcode = returnAddress.getPostalCode();
                    str.append(localityString).append("");
                    str.append(city).append("").append(region_code).append("");
                    str.append(zipcode).append("");
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG, "GetLocationAsync: ", e, CabEMain_act.this);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (moFromAddress == null) {
                    moFromAddress = new CAddress();
                }
                CAddress addr1 = new CAddress(addresses.get(0));
                moFromAddress.setAddress(addr1.getAddress());
                moFromAddress.setLatitude(xFrom);
                moFromAddress.setLongitude(yFrom);
                setFrom(moFromAddress, false);
                Gson gson = new Gson();
                String json = gson.toJson(moFromAddress);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                        .putString(Constants_CabE.PREF_FROM_ADDRESS, json);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
            } catch (Exception e) {
                SSLog_SS.e(TAG, "GetLocationAsync: ", e, CabEMain_act.this);
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    private class GetLocationAsyncTo extends AsyncTask<String, Void, String> {
        double xTo, yTo;
        StringBuilder str;
        TextView mTvAddressTo;

        GetLocationAsyncTo(double latitude, double longitude, TextView tvAddress) {
            xTo = latitude;
            yTo = longitude;
            mTvAddressTo = tvAddress;
        }

        @Override
        protected void onPreExecute() {
            mTvAddressTo.setHint(" Enter Destination ");
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Geocoder geocoder = new Geocoder(CabEMain_act.this, Locale.ENGLISH);
                addresses = geocoder.getFromLocation(xTo, yTo, 1);
                str = new StringBuilder();
                if (Geocoder.isPresent()) {
                    Address returnAddress = addresses.get(0);
                    String localityString = returnAddress.getLocality();
                    String city = returnAddress.getCountryName();
                    String region_code = returnAddress.getCountryCode();
                    String zipcode = returnAddress.getPostalCode();
                    str.append(localityString).append("");
                    str.append(city).append("").append(region_code).append("");
                    str.append(zipcode).append("");
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG, "GetLocationAsync: ", e, CabEMain_act.this);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (moToAddress == null) {
                    moToAddress = new CAddress();
                }
                CAddress addr1 = new CAddress(addresses.get(0));
                moToAddress.setAddress(addr1.getAddress());
                moToAddress.setLatitude(xTo);
                moToAddress.setLongitude(yTo);
                setTo(moToAddress, false);
                Gson gson = new Gson();
                String json = gson.toJson(moToAddress);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                        .putString(Constants_CabE.PREF_TO_ADDRESS, json);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
            } catch (Exception e) {
                SSLog_SS.e(TAG, "GetLocationAsync: ", e, CabEMain_act.this);
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    private void setWhereAmI(int iWi) {
        whereAmI = iWi;
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                .putInt(Constants_CabE.WHERE_AM_I, whereAmI);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
        whereAmIUI(whereAmI);
        switch (whereAmI) {
            case LOOKING_FOR_CAB:
                handler.removeCallbacks(runnableWhereAmI);
                StopLocationService();
                LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverPassenger);
                handler.postDelayed(runnableWhereAmI, Constants_CabE.DELAY_SHOW_FOR_HIRE_CABS);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.setMyLocationEnabled(true);
                break;
            case WAITING_FOR_DRIVER:
                if (!isLocationServiceRunning(LocationService.class)) {
                    StartLocationService();
                }
                showTrip();
                plotPathFromPolyLine(msTripPath);
                break;
            case IN_CAB:
                handler.removeCallbacks(runnableWhereAmI);
                StopLocationService();
                LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverPassenger);
                handler.postDelayed(runnableWhereAmI, Constants_CabE.DELAY_SHOW_FOR_HIRE_CABS);
                showTrip();
                driverIsInTripInfo();
                plotPathFromPolyLine(msTripPath);
                break;
        }
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
    }

    private void processLocation(Location location) {
        gotLocationFirstTime = true;
        CGlobals_lib_ss.setMyLocation(location, false, CabEMain_act.this);
        try {
            if (!CabEMain_act.this.isFinishing()) {
                if (pDialog != null) {
                    pDialog.hide();
                }
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG, "LocationResult", e, CabEMain_act.this);
        }

    }

    @Override
    protected void gotGoogleMapLocation(Location location) {
        processLocation(location);
    }

    @Override
    protected void mapReady() {

        Location loc = CGlobals_lib_ss.getInstance().getMyLocation(CabEMain_act.this);
        if (loc != null) {
            //setLocation(loc);
            processLocation(loc);
            mTvFrom.setHint(" Getting location ");
            startIntentServiceCurrentAddress(loc);
        }
        setupMap();
        ivCancelTrip = (Button) findViewById(R.id.ivCancelTrip);
        ivCancelTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelTrip();
            }
        });
        checkInternetGPS();
        mivZoomMyLocation.setVisibility(View.VISIBLE);
        markerDriver = null;
        markerEnd = null;
        markerStart = null;
        whereAmI = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getInt(Constants_CabE.WHERE_AM_I, LOOKING_FOR_CAB);
        setWhereAmI(whereAmI);
        if (whereAmI == WAITING_FOR_DRIVER) {
            getDriverInfo();
        }
        if (whereAmI == IN_CAB) {
            driverIsInTripInfo();
        }
    }

    void whereAmIUI(int iWi) {
        whereAmI = iWi;
        setupUI();
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                .putInt(Constants_CabE.WHERE_AM_I, whereAmI);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
        switch (whereAmI) {
            case LOOKING_FOR_CAB:
                if (mLlDriverInfo != null) {
                    mLlDriverInfo.setVisibility(View.GONE);
                }
                if (mLlRequestCab != null) {
                    mLlRequestCab.setVisibility(View.VISIBLE);
                }
                if (mRlFromTo != null) {
                    mRlFromTo.setVisibility(View.VISIBLE);
                }
                if (mLlMarkerLayout != null) {
                    if (!msServiceCode.equals(Constants_CabE.SERVICE_CODE_ST)) {
                        mLlMarkerLayout.setVisibility(View.VISIBLE);
                    }
                }
                if (ivCancelTrip != null) {
                    ivCancelTrip.setVisibility(View.GONE);
                }
                if (ivZoomRequestDriver != null) {
                    ivZoomRequestDriver.setVisibility(View.GONE);
                }
                if (ivZoomDriverVehicle != null) {
                    ivZoomDriverVehicle.setVisibility(View.GONE);
                }
                if (mLlMarkerLayout != null) {
                    if (!msServiceCode.equals(Constants_CabE.SERVICE_CODE_ST)) {
                        mLlMarkerLayout.setVisibility(View.VISIBLE);
                    }
                }
                if (mRlFromTo != null) {
                    mRlFromTo.setVisibility(View.VISIBLE);
                }
                if (mLlDriverIsInTrip != null) {
                    mLlDriverIsInTrip.setVisibility(View.GONE);
                }
                mLlTo.setVisibility(View.VISIBLE);
                if (btnEmergency != null) {
                    btnEmergency.setVisibility(View.GONE);
                }
                break;
            case WAITING_FOR_DRIVER:
                if (mRlFromTo != null) {
                    mRlFromTo.setVisibility(View.GONE);
                }
                if (mLlMarkerLayout != null) {
                    mLlMarkerLayout.setVisibility(View.GONE);
                }
                if (ivCancelTrip != null) {
                    ivCancelTrip.setVisibility(View.VISIBLE);
                }
                if (ivZoomRequestDriver != null) {
                    ivZoomRequestDriver.setVisibility(View.VISIBLE);
                }
                if (ivZoomDriverVehicle != null) {
                    ivZoomDriverVehicle.setVisibility(View.VISIBLE);
                }
                if (mLlRequestCab != null) {
                    mLlRequestCab.setVisibility(View.GONE);
                }
                if (mLlDriverInfo != null) {
                    mLlDriverInfo.setVisibility(View.VISIBLE);
                }
                if (mLlDriverIsInTrip != null) {
                    mLlDriverIsInTrip.setVisibility(View.GONE);
                }
                if (btnEmergency != null) {
                    btnEmergency.setVisibility(View.GONE);
                }
                break;
            case IN_CAB:
                StopLocationService();
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.setMyLocationEnabled(false);
                if (mRlFromTo != null) {
                    mRlFromTo.setVisibility(View.GONE);
                }
                if (mLlMarkerLayout != null) {
                    mLlMarkerLayout.setVisibility(View.GONE);
                }
                if (ivCancelTrip != null) {
                    ivCancelTrip.setVisibility(View.GONE);
                }
                if (ivZoomRequestDriver != null) {
                    ivZoomRequestDriver.setVisibility(View.GONE);
                }
                if (ivZoomDriverVehicle != null) {
                    ivZoomDriverVehicle.setVisibility(View.VISIBLE);
                }
                if (mLlRequestCab != null) {
                    mLlRequestCab.setVisibility(View.GONE);
                }
                if (mLlDriverInfo != null) {
                    mLlDriverInfo.setVisibility(View.GONE);
                }
                if (mLlDriverIsInTrip != null) {
                    mLlDriverIsInTrip.setVisibility(View.VISIBLE);
                }
                if (mLlRequestCab != null) {
                    mLlRequestCab.setVisibility(View.GONE);
                }
                if (btnEmergency != null) {
                    btnEmergency.setVisibility(View.GONE);
                }
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    private void driverIsInTripInfo() {
        String resIsInTrip = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getString(Constants_CabE.CABE_REQUEST_RESPONSE, "");
        cabTrip_IsInTrip = new CTripCabE(resIsInTrip, CabEMain_act.this);
        try {
            if (!TextUtils.isEmpty(cabTrip_IsInTrip.getImage_Name()) &&
                    !TextUtils.isEmpty(cabTrip_IsInTrip.getImage_Path())) {
                String url = Constants_CabE.GET_USER_PROFILE_IMAGE_FILE_NAME_URL + cabTrip_IsInTrip.getImage_Path() +
                        cabTrip_IsInTrip.getImage_Name();

                ImageRequest request = new ImageRequest(url,
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap bitmap) {
                                if (Build.VERSION.SDK_INT < 11) {
                                    Toast.makeText(CabEMain_act.this, getString(R.string.api11not_cabe), Toast.LENGTH_LONG).show();
                                } else {
                                    ivDriverCarImageIsInTrip.setImageBitmap(bitmap);
                                    ivDriverCarImageIsInTrip.setVisibility(View.VISIBLE);
                                }
                            }
                        }, 0, 0, ImageView.ScaleType.FIT_XY, Bitmap.Config.ARGB_8888,
                        new Response.ErrorListener() {
                            public void onErrorResponse(VolleyError error) {
                                ivDriverCarImageIsInTrip.setImageResource(R.drawable.ic_driver);
                            }
                        });
                MyApplication_CabE.getInstance().addToRequestQueue(request);
            } else {
                ivDriverCarImageIsInTrip.setImageResource(R.drawable.ic_driver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String ss = cabTrip_Driver_info.getVehicleNo().substring(cabTrip_Driver_info.getVehicleNo().lastIndexOf(' ') + 1);
            String[] separated = cabTrip_Driver_info.getVehicleNo().split(ss);
            tvDriverCarNumberIsInTrip.setText(separated[0] + "\n" + ss + "\n"
                    + (TextUtils.isEmpty(cabTrip_Driver_info.getFirstName()) ? ""
                    : cabTrip_Driver_info.getFirstName() + " " + cabTrip_Driver_info.getLastName()));
        } catch (Exception e) {
            SSLog_SS.e(TAG, "getDriverInfo: ", e, CabEMain_act.this);
        }
        tvCallDriverIsInTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + cabTrip_IsInTrip.getDriver_Phoneno()));
                startActivity(callIntent);
            }
        });

        tvRateRideIsInTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTripRating(0);
            }
        });

        tvShareDetailsIsInTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (ActivityCompat.checkSelfPermission(CabEMain_act.this, Manifest.permission.READ_CONTACTS)
                            != PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(CabEMain_act.this, Manifest.permission.WRITE_CONTACTS)
                            != PackageManager.PERMISSION_GRANTED) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(CabEMain_act.this);
                        builder1.setMessage("This app cannot work without the following permissions:\nContacts");
                        builder1.setCancelable(true);
                        builder1.setPositiveButton("Grant permission",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        requestContactsPermissions();
                                        dialog.dismiss();
                                    }
                                });
                        AlertDialog alert11 = builder1.create();
                        if (!CabEMain_act.this.isFinishing()) {
                            alert11.show();
                        }
                    } else {
                        Log.i(TAG,
                                "Contact permissions have already been granted. Displaying contact details.");
                        Intent intent = new Intent(CabEMain_act.this, WalkWithMe_act.class);
                        startActivity(intent);
                    }
                } else {
                    Intent intent = new Intent(CabEMain_act.this, WalkWithMe_act.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void requestContactsPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_CONTACTS)) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(CabEMain_act.this);
            builder1.setMessage("This app cannot work without the following permissions:\nContacts");
            builder1.setCancelable(true);
            builder1.setPositiveButton("Grant permission",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog alert11 = builder1.create();
            if (!CabEMain_act.this.isFinishing()) {
                alert11.show();
            }
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS_CONTACT, REQUEST_CONTACTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CONTACTS) {
            if (PermissionUtil.verifyPermissions(grantResults)) {
                Intent intent = new Intent(CabEMain_act.this, WalkWithMe_act.class);
                startActivity(intent);
            } else {
                Log.i(TAG, "Contacts permissions were NOT granted.");
                AlertDialog.Builder builder1 = new AlertDialog.Builder(CabEMain_act.this);
                builder1.setMessage("Contacts permissions were NOT granted.");
                builder1.setCancelable(true);
                builder1.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert11 = builder1.create();
                if (!CabEMain_act.this.isFinishing()) {
                    alert11.show();
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
/*
    public void getPaytm() {
        try {
            Random randomGenerator = new Random();
            randomInt = randomGenerator.nextInt(1000);
            Service = PaytmPGService.getStagingService();
            PaytmMerchant Merchant = new PaytmMerchant(
                    "http://www.jumpinjumpout.com/prod/external/pm/generateChecksum.php",
                    "http://www.jumpinjumpout.com/prod/external/pm/verifyChecksum.php");

            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("ORDER_ID", String.valueOf(randomInt));
            paramMap.put("MID", "jumpin91593054142241");
            paramMap.put("CUST_ID", "CUST123");
            paramMap.put("CHANNEL_ID", "WAP");
            paramMap.put("INDUSTRY_TYPE_ID", "Retail");
            paramMap.put("WEBSITE", "jumpinwap");
            paramMap.put("TXN_AMOUNT", "1");
            paramMap.put("THEME", "merchant");
            paramMap.put("EMAIL", "soumenmaity.cse@gmail.com");
            paramMap.put("MOBILE_NO", "8452983096");
            PaytmOrder Order = new PaytmOrder(paramMap);
            Service.initialize(Order, Merchant, null);
            Service.startPaymentTransaction(this, false, false, new PaytmPaymentTransactionCallback() {

                @Override
                public void someUIErrorOccurred(String inErrorMessage) {
                    Log.i("Paytm", "Paytm :");
                }

                @Override
                public void onTransactionSuccess(Bundle inResponse) {
                    Log.i("Paytm", "Paytm :");
                }

                @Override
                public void onTransactionFailure(String inErrorMessage, Bundle inResponse) {
                    Log.i("Paytm", "Paytm :");
                }

                @Override
                public void networkNotAvailable() {
                    Log.i("Paytm", "Paytm :");
                }

                @Override
                public void clientAuthenticationFailed(String inErrorMessage) {
                    Log.i("Paytm", "Paytm :");
                }

                @Override
                public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
                    Log.i("Paytm", "Paytm :");
                }
            });
        } catch (Exception e) {
            SSLog_SS.e(TAG, "getPaytm: ", e, CabEMain_act.this);
        }

    }
*/
    /*@SuppressLint("NewApi")
    private void notificationMessage(String sMessage) {
        // Instantiate the builder and set notification elements:
        Intent intent = new Intent(this, CabEMain_act.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification.Builder builder = new Notification.Builder(CabEMain_act.this)
                .setContentTitle("Cab-e")
                .setContentText(sMessage)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(contentIntent)
                .setSound(soundUri)
                .setAutoCancel(true);
        // Build the notification:
        Notification notification = builder.build();
        // Get the notification manager:
        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        // Publish the notification:
        int notificationId = 0;
        notificationManager.notify(notificationId, notification);
    }*/

    private void clearNotification() {
        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public void onClickZoomRequestDriver(View v) {
        mNearestDriverLat = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getFloat(Constants_CabE.DRIVER_CAB_LAT, (float) 0.0);
        mNearestDriverLng = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getFloat(Constants_CabE.DRIVER_CAB_LNG, (float) 0.0);
        Location location = CGlobals_lib_ss.getInstance().getMyLocation(CabEMain_act.this);
        mCurrentLocation = CGlobals_lib_ss.isBetterLocation(location, mCurrentLocation) ? location
                : mCurrentLocation;
        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        if (location != null) {
            if (location.getLatitude() != 0.0 && location.getLongitude() != 0.0) {
                bounds.include(new LatLng(location.getLatitude(), location.getLongitude()));
            }
        } else {
            if (mCurrentLocation.getLatitude() != 0.0 && mCurrentLocation.getLongitude() != 0.0) {
                bounds.include(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
            }
        }
        if (mNearestDriverLat != Constants_lib_ss.INVALIDLAT && mNearestDriverLng != Constants_lib_ss.INVALIDLNG
                && mNearestDriverLat != 0.0 && mNearestDriverLng != 0.0) {
            bounds.include(new LatLng(mNearestDriverLat, mNearestDriverLng));
        }
        if (((location.getLatitude() != 0.0 && location.getLongitude() != 0.0) ||
                (mCurrentLocation.getLatitude() != 0.0 && mCurrentLocation.getLongitude() != 0.0))
                && (mNearestDriverLat != 0.0 && mNearestDriverLng != 0.0)) {
            LatLngBounds bounds1 = bounds.build();
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels - 150;
            int padding = (int) (width * 0.12);
            mMap.setPadding(0, 0, 0, 200);
            mMap.animateCamera((CameraUpdateFactory.newLatLngBounds(bounds1, width, height, padding)));
        }
    }

    public void onClickZoomDriverVehicle(View v) {
        int wmi = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getInt(Constants_CabE.WHERE_AM_I, 0);
        if (wmi == WAITING_FOR_DRIVER) {
            mNearestDriverLat = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                    .getFloat(Constants_CabE.DRIVER_CAB_LAT, (float) 0.0);
            mNearestDriverLng = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                    .getFloat(Constants_CabE.DRIVER_CAB_LNG, (float) 0.0);
            if (mNearestDriverLat != 0.0 && mNearestDriverLng != 0.0 &&
                    mNearestDriverLat != Constants_lib_ss.INVALIDLAT && mNearestDriverLng != Constants_lib_ss.INVALIDLNG) {
                CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(mNearestDriverLat, mNearestDriverLng));
                CameraUpdate zoom = CameraUpdateFactory.zoomTo(DEFAULT_ZOOM);
                mMap.moveCamera(center);
                mMap.animateCamera(zoom);
            }
        }
        if (wmi == IN_CAB) {
            mNearestDriverLat_Status = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                    .getFloat(Constants_CabE.STATUS_DRIVER_CAB_LAT, (float) 0.0);
            mNearestDriverLng_Status = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                    .getFloat(Constants_CabE.STATUS_DRIVER_CAB_LNG, (float) 0.0);
            if (mNearestDriverLat_Status != 0.0 && mNearestDriverLng_Status != 0.0 &&
                    mNearestDriverLat_Status != Constants_lib_ss.INVALIDLAT && mNearestDriverLng_Status != Constants_lib_ss.INVALIDLNG) {
                CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(mNearestDriverLat_Status, mNearestDriverLng_Status));
                CameraUpdate zoom = CameraUpdateFactory.zoomTo(DEFAULT_ZOOM);
                mMap.moveCamera(center);
                mMap.animateCamera(zoom);
            }
        }
    }

    private class getCabEUpDateDriver extends AsyncTask<String, Integer, String> {
        boolean isInternetCheck = false, isLocationCheck = false;
        String response, trip_action;
        CTripCabE tripCabE;
        int cancelled_driver, something_went_wrong, etainmins, driver_connected;

        @SuppressLint("DefaultLocale")
        @Override
        protected String doInBackground(String... params) {
            response = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this).
                    getString(Constants_CabE.PREF_UPDATE_DRIVER_RESPONSE, "");
            try {
                if (TextUtils.isEmpty(response)) {
                    return null;
                }
                if (response.equals("-1")) {
                    return null;
                }
                tripCabE = new CTripCabE(response, CabEMain_act.this);
                JSONObject jResponse = new JSONObject(response);
                trip_action = jResponse.isNull("trip_action") ? ""
                        : jResponse.getString("trip_action");
                cancelled_driver = jResponse.isNull("cancelled_driver") ? 0
                        : jResponse.getInt("cancelled_driver");
                trip_id = jResponse.isNull("trip_id") ? -1
                        : jResponse.getInt("trip_id");
                something_went_wrong = jResponse.isNull("something_went_wrong") ? -1
                        : jResponse.getInt("something_went_wrong");
                etainmins = CGlobals_CabE.isNullNotDefined(jResponse, "eta") ?
                        -1 : jResponse.getInt("eta");
                driver_connected = CGlobals_CabE.isNullNotDefined(jResponse, "driver_connected") ?
                        -1 : jResponse.getInt("driver_connected");
                isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEMain_act.this);
                Location location = CGlobals_lib_ss.getInstance().getMyLocation(CabEMain_act.this);
                if (location == null) {
                    location = mCurrentLocation;
                }
                if (location == null) {
                    return null;
                }
                long diff = System.currentTimeMillis() - location.getTime();
                if (diff > Constants_lib_ss.DRIVER_UPDATE_INTERVAL * 10) {
                    /*MyLocation myLocation = new MyLocation(
                            CGlobals_lib_ss.mVolleyRequestQueue, CabEMain_act.this, mGoogleApiClient);
                    myLocation.getLocation(CabEMain_act.this, onLocationResult);*/
                    isLocationCheck = false;
                } else {
                    isLocationCheck = true;
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG, "getCabEUpDateDriver: ", e, CabEMain_act.this);
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
            } catch (Exception e) {
                SSLog_SS.e(TAG, "UpDateDriver: ", e, CabEMain_act.this);
            }

            try {
                if (isInternetCheck) {
                    return;
                }
                if (driver_connected == 1) {
                    ivDriverConnected.setBackgroundResource(R.drawable.btn_online);
                } else if (driver_connected == 0) {
                    ivDriverConnected.setBackgroundResource(R.drawable.btn_offline);
                }
                if (etainmins != -1) {
                    String sEta;
                    if (etainmins < 3) {
                        sEta = "less than 3 min.";
                    } else {
                        sEta = Integer.toString(etainmins) + " min.";
                    }
                    if (tvDriverEta != null) {
                        tvDriverEta.setText(sEta);
                    }
                }
                if (cancelled_driver == 1) {
                    handler.removeCallbacks(runnableWhereAmI);
                    tripCanceledByDriver();
                }
                if (!TextUtils.isEmpty(trip_action)) {
                    if (trip_action.equals(Constants_CabE.TRIP_ACTION_BEGIN)) {
                        setWhereAmI(IN_CAB);
                        CGlobals_CabE.getInstance().isNewNotification = false;
                    }
                }
                if (!TextUtils.isEmpty(trip_action)) {
                    if (trip_action.equals(Constants_CabE.TRIP_ACTION_END)) {
                        tripHasEnded();
                    }
                }
                if (something_went_wrong == 1) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CabEMain_act.this);
                    builder.setMessage("Something went wrong.")
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    /*setWhereAmI(LOOKING_FOR_CAB);
                                    mMap.clear();*/
                                    dialog.cancel();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG, "UpDateDriver: ", e, CabEMain_act.this);
            }
            super.onPostExecute(result);
        }
    }

    private class getUpDateDriverResponse extends AsyncTask<String, Integer, String> {
        int iSL;
        String response;
        double cabElng, cabELat;
        boolean isInternetCheck = false;
        CTripCabE tripCabE;

        @SuppressLint("DefaultLocale")
        @Override
        protected String doInBackground(String... params) {
            response = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this).
                    getString(Constants_CabE.PREF_UPDATE_DRIVER_RESPONSE, "");
            try {
                if (TextUtils.isEmpty(response)) {
                    return null;
                }
                if (response.equals("-1")) {
                    return null;
                }

                tripCabE = new CTripCabE(response, CabEMain_act.this);
                JSONObject jResponse = new JSONObject(response);
                iSL = isNullNotDefined(jResponse, "sl") ? -1 : jResponse.getInt("sl");
                cabELat = isNullNotDefined(jResponse, "l") ? Constants_lib_ss.INVALIDLAT : jResponse.getDouble("l");
                cabElng = isNullNotDefined(jResponse, "o") ? Constants_lib_ss.INVALIDLAT : jResponse.getDouble("o");
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                        .putFloat(Constants_CabE.DRIVER_CAB_LAT, (float) cabELat);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                        .putFloat(Constants_CabE.DRIVER_CAB_LNG, (float) cabElng);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
                if (iSL == 1) {
                    return null;
                }
                if (!isCabEDistanceDriver) {
                    LatLng latlng = new LatLng(CGlobals_lib_ss.getInstance().getMyLocation(CabEMain_act.this).getLatitude(),
                            CGlobals_lib_ss.getInstance().getMyLocation(CabEMain_act.this).getLongitude());
                    if (cabELat != Constants_lib_ss.INVALIDLAT && cabElng != Constants_lib_ss.INVALIDLNG &&
                            latlng.latitude != Constants_lib_ss.INVALIDLAT && latlng.longitude != Constants_lib_ss.INVALIDLNG) {
                        if (latlng.latitude != 0.0 && latlng.longitude != 0.0) {
                            float results[] = new float[1];
                            Location.distanceBetween(cabELat, cabElng, latlng.latitude, latlng.longitude, results);
                            try {
                                if (results[0] > 20) {
                                    distanceFromMe = String.format("%.2f", results[0] / 1000) + " km.";
                                } else {
                                    distanceFromMe = "Near You";
                                    isCabEDistanceDriver = true;
                                    //notificationMessage("Your cab has arrived");
                                }
                            } catch (Exception e) {
                                SSLog_SS.e(TAG, "getView", e, CabEMain_act.this);
                            }
                        }
                    }
                }
                isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEMain_act.this);
            } catch (Exception e) {
                SSLog_SS.e(TAG, "getCabEUpDateDriver: ", e, CabEMain_act.this);
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
                if (isInternetCheck) {
                    return;
                }
                if (tvDriverCarDistance != null && !TextUtils.isEmpty(distanceFromMe)) {
                    tvDriverCarDistance.setText(distanceFromMe);
                }
                for (int i = 0; i < driverCurrentLocationMarkerArray.size(); i++) {
                    driverCurrentLocationMarkerArray.get(i).remove();
                }
                if (cabELat != Constants_lib_ss.INVALIDLAT && cabElng != Constants_lib_ss.INVALIDLNG) {
                    if (cabELat != 0.0 && cabElng != 0.0) {
                        if (markerDriver == null) {
                            markerDriver = mMap.addMarker(new MarkerOptions()
                                    .position(
                                            new LatLng(cabELat, cabElng))
                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_driver_position))
                                    .alpha(0.8f).draggable(true)
                                    .title(sVehicleNo));
                        } else {
                            markerDriver.setPosition(new LatLng(cabELat, cabElng));
                        }
                    }
                }
                String sFromAdd = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                        .getString(Constants_CabE.PREF_FROM_ADDRESS, "");
                String sToAdd = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                        .getString(Constants_CabE.PREF_TO_ADDRESS, "");
                Type type = new TypeToken<CAddress>() {
                }.getType();
                moFromAddress = new Gson().fromJson(sFromAdd, type);
                moToAddress = new Gson().fromJson(sToAdd, type);
                markerStart = null;
                markerEnd = null;
                markerTo = null;
                markerFrom = null;
                if (markerStart == null) {
                    markerStart = mMap.addMarker(new MarkerOptions()
                            .position(
                                    new LatLng(moFromAddress.getLatitude(), moFromAddress.getLongitude()))
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_start_location))
                            .alpha(0.8f).draggable(true));
                } else {
                    markerStart.setPosition(new LatLng(moFromAddress.getLatitude(), moFromAddress.getLongitude()));
                }
                if (markerEnd == null) {
                    markerEnd = mMap.addMarker(new MarkerOptions()
                            .position(
                                    new LatLng(moToAddress.getLatitude(), moToAddress.getLongitude()))
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_end_location))
                            .alpha(0.8f).draggable(true));
                } else {
                    markerEnd.setPosition(new LatLng(moToAddress.getLatitude(), moToAddress.getLongitude()));
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG, "UpDateDriver: ", e, CabEMain_act.this);
            }
            super.onPostExecute(result);
        }
    }


    private class getCabEUpDateDriverError extends AsyncTask<String, Integer, String> {
        boolean isInternetCheck = false, isLocationCheck = false;

        @Override
        protected String doInBackground(String... params) {
            try {
                isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEMain_act.this);
                Location location = CGlobals_lib_ss.getInstance().getMyLocation(CabEMain_act.this);
                if (location == null) {
                    location = mCurrentLocation;
                }
                if (location == null) {
                    return null;
                }
                long diff = System.currentTimeMillis() - location.getTime();
                if (diff > Constants_lib_ss.DRIVER_UPDATE_INTERVAL * 10) {
                    MyLocation myLocation = new MyLocation(
                            CGlobals_lib_ss.mVolleyRequestQueue, CabEMain_act.this, mGoogleApiClient);
                    myLocation.getLocation(CabEMain_act.this, onLocationResult);
                    isLocationCheck = false;
                } else {
                    isLocationCheck = true;
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG, "DriverStatusError: ", e, CabEMain_act.this);
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
            } catch (Exception e) {
                SSLog_SS.e(TAG, "DriverStatusError: ", e, CabEMain_act.this);
            }
            super.onPostExecute(result);
        }
    }

    private class getCabEDriverStatus extends AsyncTask<String, Integer, String> {
        boolean isInternetCheck = false, isLocationCheck = false;
        String res;
        double cabELat_Status, cabElng_Status;
        int /*hasDriverCancelled,*/ something_went_wrong, driver_connected, has_joined, has_jumped_in, has_jumped_out;

        @Override
        protected String doInBackground(String... params) {
            res = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this).
                    getString(Constants_CabE.PREF_UPDATE_DRIVER_RESPONSE_STATUS, "");
            try {
                if (TextUtils.isEmpty(res.trim()) || res.trim().equals("-1")) {
                    if (pDialog != null) {
                        pDialog.setCancelable(true);
                        pDialog.cancel();
                    }
                    handler.postDelayed(runnableWhereAmI, Constants_CabE.DRIVER_GET_TRIP_STATUS_INTERVAL);
                    return null;
                }
                cabTrip = new CTripCabE(res, CabEMain_act.this);
                JSONObject jResponse = new JSONObject(res);
                cabELat_Status = jResponse.isNull("lat") ? Constants_lib_ss.INVALIDLAT
                        : jResponse.getDouble("lat");
                cabElng_Status = jResponse.isNull("lng") ? Constants_lib_ss.INVALIDLNG
                        : jResponse.getDouble("lng");
//                hasDriverCancelled = jResponse.isNull("cancelled_driver") ? 0
//                        : jResponse.getInt("cancelled_driver");
                something_went_wrong = jResponse.isNull("something_went_wrong") ? 0
                        : jResponse.getInt("something_went_wrong");
                driver_connected = jResponse.isNull("driver_connected") ? 0
                        : jResponse.getInt("driver_connected");
                has_joined = jResponse.isNull("has_joined") ? 0
                        : jResponse.getInt("has_joined");
                has_jumped_in = jResponse.isNull("has_jumped_in") ? 0
                        : jResponse.getInt("has_jumped_in");
                has_jumped_out = jResponse.isNull("has_jumped_out") ? 0
                        : jResponse.getInt("has_jumped_out");
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                        .putString(Constants_CabE.CURRENT_TRACKED_TRIP, res);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                        .putFloat(Constants_CabE.STATUS_DRIVER_CAB_LAT, (float) cabELat_Status);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                        .putFloat(Constants_CabE.STATUS_DRIVER_CAB_LNG, (float) cabElng_Status);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();

                isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEMain_act.this);
                Location location = CGlobals_lib_ss.getInstance().getMyLocation(CabEMain_act.this);
                if (location == null) {
                    location = mCurrentLocation;
                }
                if (location == null) {
                    return null;
                }
                long diff = System.currentTimeMillis() - location.getTime();
                if (diff > Constants_lib_ss.DRIVER_UPDATE_INTERVAL * 10) {
                    /*MyLocation myLocation = new MyLocation(
                            CGlobals_lib_ss.mVolleyRequestQueue, CabEMain_act.this, mGoogleApiClient);
                    myLocation.getLocation(CabEMain_act.this, onLocationResult);*/
                    isLocationCheck = false;
                } else {
                    isLocationCheck = true;
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG, "DriverStatus: ", e, CabEMain_act.this);
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
            } catch (Exception e) {
                SSLog_SS.e(TAG, "DriverStatus: ", e, CabEMain_act.this);
            }
            try {
                if (isInternetCheck) {
                    return;
                }
                for (int i = 0; i < driverCurrentLocationMarkerArray.size(); i++) {
                    driverCurrentLocationMarkerArray.get(i).remove();
                }
                if (markerDriver == null) {
                    markerDriver = mMap.addMarker(new MarkerOptions()
                            .position(
                                    new LatLng(cabELat_Status, cabElng_Status))
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_driver_position))
                            .alpha(0.8f).draggable(true));
                } else {
                    markerDriver.setPosition(new LatLng(cabELat_Status, cabElng_Status));
                }
                if (driver_connected == 1) {
                    ivDriverConnectedStatus.setBackgroundResource(R.drawable.btn_online);
                } else if (driver_connected == 0) {
                    ivDriverConnectedStatus.setBackgroundResource(R.drawable.btn_offline);
                }

                if (cabTrip.getMsTripAction().equals(Constants_CabE.TRIP_ACTION_END)) {
                    tripHasEnded();
                }
                if (has_jumped_out == 1) {
                    tripHasEnded();
                }
                if (something_went_wrong == 1) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CabEMain_act.this);
                    builder.setMessage("something went wrong.")
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // tripHasEnded();
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                String sFromAdd = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                        .getString(Constants_CabE.PREF_FROM_ADDRESS, "");
                String sToAdd = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                        .getString(Constants_CabE.PREF_TO_ADDRESS, "");
                Type type = new TypeToken<CAddress>() {
                }.getType();
                moFromAddress = new Gson().fromJson(sFromAdd, type);
                moToAddress = new Gson().fromJson(sToAdd, type);
                markerStart = null;
                markerEnd = null;
                markerTo = null;
                markerFrom = null;
                if (moFromAddress.getLatitude() != Constants_CabE.INVALIDLAT && moFromAddress.getLongitude() != Constants_CabE.INVALIDLNG) {
                    if (moFromAddress.getLatitude() != 0.0 && moFromAddress.getLongitude() != 0.0) {
                        markerStart = mMap.addMarker(new MarkerOptions()
                                .position(
                                        new LatLng(moFromAddress.getLatitude(), moFromAddress.getLongitude()))
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_start_location))
                                .alpha(0.8f).draggable(true));
                    }
                }
                if (moToAddress.getLatitude() != Constants_CabE.INVALIDLAT && moToAddress.getLongitude() != Constants_CabE.INVALIDLNG) {
                    if (moToAddress.getLatitude() != 0.0 && moToAddress.getLongitude() != 0.0) {
                        markerEnd = mMap.addMarker(new MarkerOptions()
                                .position(
                                        new LatLng(moToAddress.getLatitude(), moToAddress.getLongitude()))
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_end_location))
                                .alpha(0.8f).draggable(true));
                    }
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG, "DriverStatus: ", e, CabEMain_act.this);
            }
            super.onPostExecute(result);
        }
    }

    private class getCabEDriverStatusError extends AsyncTask<String, Integer, String> {
        boolean isInternetCheck = false, isLocationCheck = false;

        @Override
        protected String doInBackground(String... params) {
            try {
                isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEMain_act.this);
                Location location = CGlobals_lib_ss.getInstance().getMyLocation(CabEMain_act.this);
                if (location == null) {
                    location = mCurrentLocation;
                }
                if (location == null) {
                    return null;
                }
                long diff = System.currentTimeMillis() - location.getTime();
                if (diff > Constants_lib_ss.DRIVER_UPDATE_INTERVAL * 10) {
                    MyLocation myLocation = new MyLocation(
                            CGlobals_lib_ss.mVolleyRequestQueue, CabEMain_act.this, mGoogleApiClient);
                    myLocation.getLocation(CabEMain_act.this, onLocationResult);
                    isLocationCheck = false;
                } else {
                    isLocationCheck = true;
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG, "DriverStatusError: ", e, CabEMain_act.this);
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
            } catch (Exception e) {
                SSLog_SS.e(TAG, "DriverStatusError: ", e, CabEMain_act.this);
            }
            super.onPostExecute(result);
        }
    }

    public boolean isNullNotDefined(JSONObject jo, String jkey) {
        return !jo.has(jkey) || jo.isNull(jkey);
    }

    void showTrip() {
        if (!setFromTo()) {
            Toast.makeText(CabEMain_act.this, "Sorry Something went wrong with this trip.", Toast.LENGTH_SHORT).show();
            return;
        }
        Location location = CGlobals_lib_ss.getInstance().getBestLocation(this);
        mCurrentLocation = CGlobals_lib_ss.isBetterLocation(location, mCurrentLocation) ? location
                : mCurrentLocation;
        String jsonPoints = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getString(Constants_lib_ss.TRIP_PATH, "");
        if (!TextUtils.isEmpty(jsonPoints)) {
            mLatLngTripPath = PolyUtil.decode(jsonPoints);
            mDirectionsPolyline = new PolylineOptions()
                    .addAll(mLatLngTripPath);
            mDirectionsPolyline.width(4);
            mDirectionsPolyline.color(Color.BLACK);

            try {
                mMap.addPolyline(mDirectionsPolyline);
                setFromTo();
                mZoomFitBounds = new LatLngBounds.Builder();
                for (LatLng latLng : mLatLngTripPath) {
                    mZoomFitBounds.include(new LatLng(latLng.latitude,
                            latLng.longitude));
                }
                mMap.setPadding(0, 100, 0, 360);
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                        mZoomFitBounds.build(), 40));
            } catch (Exception e) {
                SSLog_SS.e(TAG, "showTrip", e, CabEMain_act.this);
            }
        }
    }

    boolean setFromTo() {

        String sFrom = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getString(Constants_CabE.PREF_FROM_ADDRESS, "");
        try {
            JSONObject jo = new JSONObject(sFrom);
            moFromAddress = new CAddress(jo);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        String sTo = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getString(Constants_CabE.PREF_TO_ADDRESS, "");
        try {
            JSONObject jo = new JSONObject(sTo);
            moToAddress = new CAddress(jo);
        } catch (Exception e) {
            SSLog_SS.e(TAG, "setFromTo: ", e, CabEMain_act.this);
            return false;
        }
        return true;

    }

    public void clearToFromPath() {
        try {
            mTvFrom.setText("");
            mTvTo.setText("");
            mapReady();
            mIsFromSelected = true;
            mIvRedPin.setVisibility(View.GONE);
            mIvGreenPin.setVisibility(View.VISIBLE);
            Location location = CGlobals_lib_ss.getInstance().getMyLocation(CabEMain_act.this);
            new GetLocationAsyncFrom(location.getLatitude(), location.getLongitude(), mTvFrom)
                    .execute();
        } catch (Exception e) {
            SSLog_SS.e(TAG, "clearToFromPath: ", e, CabEMain_act.this);
        }
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                .putString(Constants_lib_ss.TRIP_PATH, "");
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                .putString(Constants_CabE.PREF_FROM_ADDRESS, "");
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                .putString(Constants_CabE.PREF_TO_ADDRESS, "");
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
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
            SSLog_SS.e(TAG, "drawFromMarkerCab", e, CabEMain_act.this);
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
            SSLog_SS.e(TAG, "drawToMarkerCab", e, CabEMain_act.this);
        }
    } // drawToMarker

    private void shareRequestCab() {

        String sFromAdd = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getString(Constants_CabE.PREF_FROM_ADDRESS, "");
        String sToAdd = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getString(Constants_CabE.PREF_TO_ADDRESS, "");
        Type type = new TypeToken<CAddress>() {
        }.getType();
        if (moFromAddress == null) {
            moFromAddress = new CAddress();
        }
        if (moToAddress == null) {
            moToAddress = new CAddress();
        }
        moFromAddress = new Gson().fromJson(sFromAdd, type);
        moToAddress = new Gson().fromJson(sToAdd, type);
        final String url = Constants_CabE.REQUEST_SHARED_CAB_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        bookSeatDriver(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String sErr = CGlobals_lib_ss.getInstance().getVolleyError(error);
                try {
                    snackbar = Snackbar.make(coordinatorLayout, getString(R.string.retry_internet), Snackbar.LENGTH_LONG);
                    snackbar.show();
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.d(TAG,
                                "Failed to mVehicleNoVerify :-  "
                                        + error.getMessage());
                    }
                } catch (Exception e) {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG, "mVehicleNoVerify - " + sErr,
                                error, CabEMain_act.this);
                    }
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                if (moFromAddress.hasLatitude() && moFromAddress.hasLongitude()) {
                    params.put("fromlat", Double.toString(moFromAddress.getLatitude()));
                    params.put("fromlng", Double.toString(moFromAddress.getLongitude()));
                }
                if (moToAddress.hasLatitude() && moToAddress.hasLongitude()) {
                    params.put("tolat", Double.toString(moToAddress.getLatitude()));
                    params.put("tolng", Double.toString(moToAddress.getLongitude()));
                }
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        url, CabEMain_act.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                try {
                    String url1 = url + "?" + getParams.toString();
                    System.out.println("url  " + url1);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "mVehicleNoVerify", e, CabEMain_act.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, CabEMain_act.this);
    }

    private void bookSeatDriver(String response) {
        if (response.trim().equals("-1")) {
            snackbar = Snackbar.make(coordinatorLayout, getString(R.string.vehiclenonotfound_cabe), Snackbar.LENGTH_LONG);
            snackbar.show();
            mapReady();
            return;
        }
        try {
            cabTripShare = new CTripCabE(response, CabEMain_act.this);
            JSONObject jResponse = new JSONObject(response);
            try {
                String sFromAddress = CGlobals_CabE.isNullNotDefined(jResponse, "fromaddress") ?
                        "" : jResponse.getString("fromaddress");
                double dFromLat = CGlobals_CabE.isNullNotDefined(jResponse, "fromlat") ?
                        Constants_CabE.INVALIDLAT : jResponse.getDouble("fromlat");
                double dFromLng = CGlobals_CabE.isNullNotDefined(jResponse, "fromlng") ?
                        Constants_CabE.INVALIDLNG : jResponse.getDouble("fromlng");
                String sToAddress = CGlobals_CabE.isNullNotDefined(jResponse, "toaddress") ?
                        "" : jResponse.getString("toaddress");
                double dToLat = CGlobals_CabE.isNullNotDefined(jResponse, "tolat") ?
                        Constants_CabE.INVALIDLAT : jResponse.getDouble("tolat");
                double dToLng = CGlobals_CabE.isNullNotDefined(jResponse, "tolng") ?
                        Constants_CabE.INVALIDLNG : jResponse.getDouble("tolng");
                trip_id = jResponse.isNull("trip_id") ? -1 : jResponse.getInt("trip_id");
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).
                        putInt(Constants_CabE.PREF_TRIP_ID_PASSENGER, trip_id);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
                if (moFromAddress == null) {
                    moFromAddress = new CAddress();
                }
                moFromAddress.setAddress(sFromAddress);
                moFromAddress.setLatitude(dFromLat);
                moFromAddress.setLongitude(dFromLng);
                Gson gson = new Gson();
                String jsonFrom = gson.toJson(moFromAddress);
                if (moToAddress == null) {
                    moToAddress = new CAddress();
                }
                moToAddress.setAddress(sToAddress);
                moToAddress.setLatitude(dToLat);
                moToAddress.setLongitude(dToLng);
                String jsonTo = gson.toJson(moToAddress);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                        .putString(Constants_CabE.PREF_FROM_ADDRESS, jsonFrom);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                        .putString(Constants_CabE.PREF_TO_ADDRESS, jsonTo);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
            } catch (Exception e) {
                SSLog_SS.e(TAG, "requestCab adds: ", e, CabEMain_act.this);
            }

            try {
                trip_id = jResponse.isNull("trip_id") ? -1
                        : jResponse.getInt("trip_id");
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).
                        putInt(Constants_CabE.PREF_TRIP_ID_PASSENGER, trip_id);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
            } catch (Exception e) {
                e.printStackTrace();
            }

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(CabEMain_act.this);
            alertDialog.setTitle("Confirm your Book seat");
            alertDialog.setPositiveButton("Book Seat", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEMain_act.this);
                    if (!isInternetCheck) {
                        bookSeat();
                    }
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }

            });
            if (!CabEMain_act.this.isFinishing()) {
                alertDialog.show();
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG, "getVehicleAllDtetails", e, CabEMain_act.this);
            snackbar = Snackbar.make(coordinatorLayout, getString(R.string.vehiclenonotfound_cabe), Snackbar.LENGTH_LONG);
            snackbar.show();
            mapReady();
        }
    } //bookSeatDriver

    private void bookSeat() {

        String sFromAdd = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this)
                .getString(Constants_CabE.PREF_FROM_ADDRESS, "");
        Type type = new TypeToken<CAddress>() {
        }.getType();
        if (moFromAddress == null) {
            moFromAddress = new CAddress();
        }
        moFromAddress = new Gson().fromJson(sFromAdd, type);

        final int tripid = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMain_act.this).
                getInt(Constants_CabE.PREF_TRIP_ID_PASSENGER, -1);
        final String url = Constants_CabE.ADD_SHARED_PASSENGER_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        bookSeatResult(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String sErr = CGlobals_lib_ss.getInstance().getVolleyError(error);
                try {
                    snackbar = Snackbar.make(coordinatorLayout, getString(R.string.retry_internet), Snackbar.LENGTH_LONG);
                    snackbar.show();
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.d(TAG,
                                "Failed to mVehicleNoVerify :-  "
                                        + error.getMessage());
                    }
                } catch (Exception e) {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG, "mVehicleNoVerify - " + sErr,
                                error, CabEMain_act.this);
                    }
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("jumpinaddress", moFromAddress.getAddress());
                params.put("joinlat", Double.toString(moFromAddress.getLatitude()));
                params.put("joinlng", Double.toString(moFromAddress.getLongitude()));
                params.put("tripid", String.valueOf(tripid));
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        url, CabEMain_act.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                try {
                    String url1 = url + "?" + getParams.toString();
                    System.out.println("url  " + url1);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "mVehicleNoVerify", e, CabEMain_act.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, CabEMain_act.this);
    }

    private void bookSeatResult(String response) {
        if (response.trim().equals("-1") || TextUtils.isEmpty(response)) {
            snackbar = Snackbar.make(coordinatorLayout, getString(R.string.vehiclenonotfound_cabe), Snackbar.LENGTH_LONG);
            snackbar.show();
            mapReady();
            return;
        }
        try {
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this)
                    .putString(Constants_CabE.CABE_REQUEST_RESPONSE, response);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMain_act.this).commit();
            whereAmI = WAITING_FOR_DRIVER;
            setWhereAmI(whereAmI);
            getDriverInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
