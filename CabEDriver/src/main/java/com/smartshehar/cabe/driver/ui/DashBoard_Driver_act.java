package com.smartshehar.cabe.driver.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartshehar.cabe.driver.CGlobals_CED;
import com.smartshehar.cabe.driver.Constants_CED;
import com.smartshehar.cabe.driver.DatabaseHandler_CabE;
import com.smartshehar.cabe.driver.LocationService;
import com.smartshehar.cabe.driver.MyApplication_CED;
import com.smartshehar.cabe.driver.MyService_CED;
import com.smartshehar.cabe.driver.R;
import com.smartshehar.cabe.driver.VehicleResult;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;
import lib.app.util.Constants_lib_ss;
import lib.app.util.FetchAddressIntentService;
import lib.app.util.MyLocation;
import lib.app.util.SSLog_SS;
import lib.app.util.UpgradeApp;

/**
 * Created by jijo_soumen on 16/03/2016.
 * Driver main screen to show For Hire
 */
public class DashBoard_Driver_act extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static String TAG = "DashBoard_Driver_act: ";
    private CoordinatorLayout coordinatorLayout;
    Snackbar snackbar;
    String trip_action, sUser_Type;
    boolean loginLogoutFlag = false;
    int miForHire = 0;
    int booking_driver_id, trip_id, cancelled_driver, cancelled_passenger;
    // for fused location
    Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    Connectivity mConnectivity;
    //private boolean isServerVolleyCallFailed = false;
    ImageView ivNoInternetConnection, ivforhire, tvCabEDriverHistory;
    LocationRequest mLocationRequest;
    private static final long INTERVAL = 1000 * 60;
    TextView tvForHireWaiting, tvchangeForHire, tvVersionCode, tvCarNumber, tvCarType;
    Toolbar toolbar;
    int iChecked = 2;
    RadioButton rbRest, rbBusy, rbForHire, rbShareForHire;
    Location loc;
    private AddressResultReceiver mResultReceiver;
    protected String mAddressOutput;
    String localAddress;
    protected ProgressDialog mProgressDialog;
    boolean mRequestingLocationUpdates = true;
    String sCostValue = "", sDistance = "";
    boolean isUpdateCose = false;
    DatabaseHandler_CabE db;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_driver_new_act);
        createLocationRequest();
        buildGoogleApiClient();
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .coordinatorLayout);
        initToolBar();
        CGlobals_CED.getInstance().init(DashBoard_Driver_act.this);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        tvForHireWaiting = (TextView) findViewById(R.id.tvForHireWaiting);
        tvchangeForHire = (TextView) findViewById(R.id.tvchangeForHire);
        tvCarNumber = (TextView) findViewById(R.id.tvCarNumber);
        tvCarType = (TextView) findViewById(R.id.tvCarType);
        ivforhire = (ImageView) findViewById(R.id.ivforhire);
        ivNoInternetConnection = (ImageView) findViewById(R.id.ivNoInternetConnection);
        rbRest = (RadioButton) findViewById(R.id.rbRest);
        rbBusy = (RadioButton) findViewById(R.id.rbBusy);
        rbForHire = (RadioButton) findViewById(R.id.rbForHire);
        rbShareForHire = (RadioButton) findViewById(R.id.rbShareForHire);
        mResultReceiver = new AddressResultReceiver(new Handler());
        loc = CGlobals_lib_ss.getInstance().getMyLocation(DashBoard_Driver_act.this);
        if (loc != null) {
            startIntentServiceCurrentAddress(loc);
        }
        try {
            tvVersionCode = (TextView) findViewById(R.id.tvVersionCode);
            tvVersionCode.setText(CGlobals_lib_ss.getInstance().mPackageInfo.versionName + "\n" + Constants_CED.ALPHA);
        } catch (Exception e) {
            Log.e(TAG, "onCreate: ", e);
        }
        mConnectivity = new Connectivity();
        if (!mConnectivity.connectionError(DashBoard_Driver_act.this, getString(R.string.app_label))) {
            Log.d(TAG, "no internet Connection");
        }
        try {
            UpgradeApp.app_launched(this, CGlobals_CED.getInstance().mPackageInfo, CGlobals_CED.getInstance().mUserInfo,
                    getString(R.string.app_label),
                    "CED",
                    String.valueOf(CGlobals_CED.getInstance().mPackageInfo.versionCode), Constants_lib_ss.UPGRADE_APP_URL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void radioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        String sDateTime = df.format(cal.getTime());
        int appuserid = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this)
                .getInt(Constants_lib_ss.PREF_APPUSERID, -1);
        db = new DatabaseHandler_CabE(DashBoard_Driver_act.this);
        switch (view.getId()) {
            case R.id.rbRest:
                if (checked) {
                    iChecked = 2;
                    rbBusy.setChecked(false);
                    rbForHire.setChecked(false);
                    rbShareForHire.setChecked(false);
                    ivforhire.setBackgroundResource(R.drawable.ic_rest);
                    tvForHireWaiting.setText("");
                    tvchangeForHire.setText("");
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                            .putInt(Constants_CED.PREF_SET_SWITCH_FLAG, iChecked);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).commit();
                    db.addFlagSetDriver(String.valueOf(iChecked), sDateTime, appuserid, CGlobals_lib_ss.msGmail,
                            CGlobals_lib_ss.mIMEI);
                    StopLocationService();
                }
                break;

            case R.id.rbBusy:
                if (checked) {
                    iChecked = 0;
                    rbRest.setChecked(false);
                    rbForHire.setChecked(false);
                    rbShareForHire.setChecked(false);
                    ivforhire.setBackgroundResource(R.drawable.ic_busy);
                    tvForHireWaiting.setText("");
                    tvchangeForHire.setText(getString(R.string.meter_down));
                    StopLocationService();
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                            .putInt(Constants_CED.PREF_SET_SWITCH_FLAG, iChecked);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).commit();
                    db.addFlagSetDriver(String.valueOf(iChecked), sDateTime, appuserid, CGlobals_lib_ss.msGmail,
                            CGlobals_lib_ss.mIMEI);
                    createTrip();
                }
                break;

            case R.id.rbForHire:
                if (checked) {
                    iChecked = 1;
                    rbBusy.setChecked(false);
                    rbRest.setChecked(false);
                    rbShareForHire.setChecked(false);
                    if (!isLocationServiceRunning(LocationService.class)) {
                        StartLocationService();
                    }
                    ivforhire.setBackgroundResource(R.drawable.ic_forhire);
                    tvForHireWaiting.setText(getString(R.string.waitingforbooking));
                    tvchangeForHire.setText(getString(R.string.meter_up));
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                            .putInt(Constants_CED.PREF_SET_SWITCH_FLAG, iChecked);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).commit();
                    db.addFlagSetDriver(String.valueOf(iChecked), sDateTime, appuserid, CGlobals_lib_ss.msGmail,
                            CGlobals_lib_ss.mIMEI);
                }
                break;

            case R.id.rbShareForHire:
                if (checked) {
                    iChecked = 3;
                    rbBusy.setChecked(false);
                    rbRest.setChecked(false);
                    rbForHire.setChecked(false);
                    setYourPointToPointTrip();
                    ivforhire.setBackgroundResource(R.drawable.ic_forhire);
                    tvForHireWaiting.setText(getString(R.string.waitingforsharebooking));
                    tvchangeForHire.setText(getString(R.string.meter_up));
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                            .putInt(Constants_CED.PREF_SET_SWITCH_FLAG, iChecked);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).commit();
                    db.addFlagSetDriver(String.valueOf(iChecked), sDateTime, appuserid, CGlobals_lib_ss.msGmail,
                            CGlobals_lib_ss.mIMEI);
                }
                break;
        }

    }

    private void initToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        DashBoard_Driver_act.this.setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Cab-e Manager");
        }
        tvCabEDriverHistory = (ImageView) findViewById(R.id.tvCabEDriverHistory);
        tvCabEDriverHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashBoard_Driver_act.this, DriverHistory_act.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        this.getWindow().setAttributes(params);
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        String sResponse = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this)
                .getString(Constants_CED.PREF_CHECK_BOOK_RESPONSE, "");
        boolean isCheckBook = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this)
                .getBoolean(Constants_CED.PREF_IS_CHECK_BOOK, false);
        if (isCheckBook) {
            //checkIfBookedResponse(sResponse);
            new getCheckIfBook(sResponse).execute("");
        }
        // LocationService BroadCast
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverDashBoard,
                new IntentFilter(Constants_CED.LOCATION_SERVICE_FOR_HIRE));
        Intent sendableIntent = new Intent(Constants_CED.LOCATION_SERVICE_FOR_HIRE);
        LocalBroadcastManager.getInstance(this).
                sendBroadcast(sendableIntent);
        // MyService Broadcast
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageBroadcastReceiverDashBoard,
                new IntentFilter(Constants_CED.ERVICE_DRIVER_ALL_PHP));
        Intent intentResponseService = new Intent(Constants_CED.ERVICE_DRIVER_ALL_PHP);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentResponseService);

        vehicleVerification();
        miForHire = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this).
                getInt(Constants_CED.PREF_SET_SWITCH_FLAG, 2);
        if (miForHire == 1) {
            if (ivforhire != null) {
                ivforhire.setBackgroundResource(R.drawable.ic_forhire);
                tvForHireWaiting.setText(getString(R.string.waitingforbooking));
                tvchangeForHire.setText(getString(R.string.meter_up));
                rbBusy.setChecked(false);
                rbRest.setChecked(false);
                rbForHire.setChecked(true);
                rbShareForHire.setChecked(false);
            }
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        } else if (miForHire == 2) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            if (ivforhire != null) {
                ivforhire.setBackgroundResource(R.drawable.ic_rest);
                tvForHireWaiting.setText("");
                tvchangeForHire.setText("");
                rbBusy.setChecked(false);
                rbRest.setChecked(true);
                rbForHire.setChecked(false);
                rbShareForHire.setChecked(false);
            }
        } else if (miForHire == 3) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            if (ivforhire != null) {
                ivforhire.setBackgroundResource(R.drawable.ic_forhire);
                tvForHireWaiting.setText(getString(R.string.waitingforbooking));
                tvchangeForHire.setText(getString(R.string.meter_up));
                rbBusy.setChecked(false);
                rbRest.setChecked(false);
                rbForHire.setChecked(false);
                rbShareForHire.setChecked(true);
            }
        } else if (miForHire == 0) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            if (ivforhire != null) {
                ivforhire.setBackgroundResource(R.drawable.ic_busy);
                tvForHireWaiting.setText("");
                tvchangeForHire.setText(getString(R.string.meter_down));
                rbBusy.setChecked(true);
                rbRest.setChecked(false);
                rbForHire.setChecked(false);
                rbShareForHire.setChecked(false);
            }
        }

        if (!isLocationServiceRunning(LocationService.class) && miForHire == 1) {
            StartLocationService();
        }
        if (CGlobals_CED.getInstance().isInTrip(DashBoard_Driver_act.this)) {
            Intent intent = new Intent(DashBoard_Driver_act.this, CabEDriverForHire_act.class);
            startActivity(intent);
        }

        boolean isinTripMain = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this).
                getBoolean(Constants_CED.PREF_ISIN_TRIP_MAIN, false);
        if(isinTripMain){
            Intent intent = new Intent(DashBoard_Driver_act.this, CabEMainDriver_act.class);
            startActivity(intent);
        }

        new getCheckInternetGPS().execute("");
        String sVDetails = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this)
                .getString(Constants_CED.PERF_VEHICLE_DETAILS, "");
        if (!TextUtils.isEmpty(sVDetails)) {
            Type type = new TypeToken<VehicleResult>() {
            }.getType();
            VehicleResult vehicleResult = new Gson().fromJson(sVDetails, type);
            tvCarNumber.setText(vehicleResult.getFull_Vehicleno());
            switch (vehicleResult.getServiceCode()) {
                case Constants_CED.SERVICE_CODE_BY:
                    tvCarType.setText(getString(R.string.service_black_and_yellow));
                    break;
                case Constants_CED.SERVICE_CODE_CC:
                    tvCarType.setText(getString(R.string.service_coolcab));
                    break;
                case Constants_CED.SERVICE_CODE_FT:
                    tvCarType.setText(getString(R.string.service_fleet_taxi));
                    break;
                case Constants_CED.SERVICE_CODE_TTP:
                    tvCarType.setText(getString(R.string.service_prime));
                    break;
                case Constants_CED.SERVICE_CODE_TNP:
                    tvCarType.setText(getString(R.string.service_standard));
                    break;
                case Constants_CED.SERVICE_CODE_SG:
                    tvCarType.setText(getString(R.string.sanghini));
                    break;
                case Constants_CED.SERVICE_CODE_AR:
                    tvCarType.setText(getString(R.string.rickshaw));
                    break;
                case Constants_CED.SERVICE_CODE_ST:
                    tvCarType.setText(getString(R.string.share_cab));
                    break;
            }

            if (vehicleResult.getServiceCode().equals(Constants_CED.SERVICE_CODE_ST)) {
                rbRest.setVisibility(View.VISIBLE);
                rbBusy.setVisibility(View.GONE);
                rbForHire.setVisibility(View.GONE);
                rbShareForHire.setVisibility(View.VISIBLE);
            } else {
                rbRest.setVisibility(View.VISIBLE);
                rbBusy.setVisibility(View.VISIBLE);
                rbForHire.setVisibility(View.VISIBLE);
                rbShareForHire.setVisibility(View.GONE);
            }
        }
        startService();
        CGlobals_lib_ss.getInstance().countError1 = 1;
        CGlobals_lib_ss.getInstance().countError2 = 1;
        CGlobals_lib_ss.getInstance().countError3 = 1;
        CGlobals_lib_ss.getInstance().countError4 = 1;
        CGlobals_lib_ss.getInstance().countError5 = 1;
        CGlobals_lib_ss.getInstance().countError6 = 1;
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(this).
                putBoolean(Constants_CED.PREF_NOTIFICATION_CLEAR_FLAG, true);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(this).commit();
    }

    protected void StartLocationService() {
        stopService(new Intent(this, LocationService.class));
        CGlobals_CED.SERVICE_CURRENT = CGlobals_CED.SERVICE_DB;
        Intent serviceIntent = new Intent(this, LocationService.class);
        serviceIntent.putExtra(Constants_CED.SERVICE_FLAG, "DB");
        startService(serviceIntent);
    }

    protected void StopLocationService() {
        stopService(new Intent(DashBoard_Driver_act.this, LocationService.class));
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
        super.onPause();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverDashBoard);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageBroadcastReceiverDashBoard);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(DashBoard_Driver_act.this);
        alertDialog.setTitle("Please press red button to log out");
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        if (!DashBoard_Driver_act.this.isFinishing()) {
            alertDialog.show();
        }
    }

    public void onClickLogout(View view) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(DashBoard_Driver_act.this);
        alertDialog.setTitle("Are you sure you want to logout?");
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                String sDateTime = df.format(cal.getTime());
                String spShiftid = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this)
                        .getString(Constants_CED.PREF_SHIFT_ID, "");
                String spVehicleid = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this)
                        .getString(Constants_CED.PREF_VEHICLE_ID, "");
                int appuserid = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this)
                        .getInt(Constants_lib_ss.PREF_APPUSERID, -1);
                db = new DatabaseHandler_CabE(DashBoard_Driver_act.this);
                db.addDriverShifEnd(appuserid, CGlobals_lib_ss.msGmail,
                        sDateTime, CGlobals_lib_ss.mIMEI, spShiftid, spVehicleid);

                StopLocationService();
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                        .putInt(Constants_CED.PREF_SET_SWITCH_FLAG, 2);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).
                        putString(Constants_CED.CAB_TRIP_USER_TYPE_DRIVER, "");
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                        .putBoolean(Constants_CED.PREF_LOGIN_LOGOUT_FLAG, false);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).commit();

                db.addFlagSetDriver(String.valueOf(0), sDateTime, appuserid, CGlobals_lib_ss.msGmail,
                        CGlobals_lib_ss.mIMEI);
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        if (!DashBoard_Driver_act.this.isFinishing()) {
            alertDialog.show();
        }
    }

    /*private void goLogout() {
        spShiftid = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this)
                .getString(Constants_CED.PREF_SHIFT_ID, "");
        spVehicleid = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this)
                .getString(Constants_CED.PREF_VEHICLE_ID, "");
        final String url = Constants_CED.DRIVER_END_SHIFT_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getLogout(response);
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                                .putBoolean(Constants_CED.PREF_CAB_E_DRIVER_END_SHIFT_BOOLEAN, true);
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).commit();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                snackbar = Snackbar.make(coordinatorLayout, getString(R.string.retry_internet), Snackbar.LENGTH_LONG);
                snackbar.show();
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                        .putBoolean(Constants_CED.PREF_CAB_E_DRIVER_END_SHIFT_BOOLEAN, false);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).commit();
                String sErr = CGlobals_lib_ss.getInstance().getVolleyError(error);
                try {
                    SSLog_SS.d(TAG,
                            "Failed to mVehicleNoVerify :-  "
                                    + error.getMessage());
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "mVehicleNoVerify - " + sErr,
                            e, DashBoard_Driver_act.this);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("shiftid", spShiftid);
                params.put("vehicleid", spVehicleid);
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        url, DashBoard_Driver_act.this);
                String jsonParams = new Gson().toJson(params);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                        .putString(Constants_CED.PREF_CAB_E_DRIVER_END_SHIFT, jsonParams);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).commit();
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
                    SSLog_SS.e(TAG, "getPassengers map - ", e, DashBoard_Driver_act.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, DashBoard_Driver_act.this);
    }*/

    private void vehicleVerification() {
        loginLogoutFlag = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this)
                .getBoolean(Constants_CED.PREF_LOGIN_LOGOUT_FLAG, false);
        if (!loginLogoutFlag) {
            Intent intent1 = new Intent(DashBoard_Driver_act.this, VehicleVerification_CED_act.class);
            startActivity(intent1);
            finish();
        }
    }

    /*public void sendFlag(final String flagName, final int flagValue) {

        final String saveDateTime = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this)
                .getString(Constants_CED.PREF_FOR_HIRE_DATE_TIME, "");
        final String url = Constants_CED.SET_FOR_HIRE_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("response" + response);
                isServerVolleyCallFailed = false;
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                        .putBoolean(Constants_CED.PREF_SERVER_VOLLEY_CALL_FAILED, isServerVolleyCallFailed);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                snackbar = Snackbar.make(coordinatorLayout, getString(R.string.retry_internet), Snackbar.LENGTH_LONG);
                snackbar.show();
                isServerVolleyCallFailed = true;
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                        .putBoolean(Constants_CED.PREF_SERVER_VOLLEY_CALL_FAILED, isServerVolleyCallFailed);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).commit();
                SSLog_SS.e(TAG, "sendFlag :-   " + flagName,
                        error, DashBoard_Driver_act.this);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("isforhire", String.valueOf(flagValue));
                params.put("fhdt", saveDateTime);
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params, url,
                        DashBoard_Driver_act.this);
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
                    SSLog_SS.e(TAG, "getPassengers map - ", e, DashBoard_Driver_act.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, DashBoard_Driver_act.this);
    }*/ // sendForHire


   /* private void checkIfBookedResponse(String response) {

        if (TextUtils.isEmpty(response) || response.equals("-1")) {
            return;
        }
        try {
            miForHire = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this).
                    getInt(Constants_CED.PREF_SET_SWITCH_FLAG, 2);
            if (miForHire == 1 && loginLogoutFlag) {
                Gson gson = new Gson();
                String json = gson.toJson(response);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).
                        putString(Constants_CED.PREF_SAVE_RESPONSE_PASSENGER, json);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).commit();
                JSONObject jResponse = new JSONObject(response);
                String sBookingDateTime = jResponse.isNull("booking_time") ? ""
                        : jResponse.getString("booking_time");
                try {
                    DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault());
                    Date bdt = df1.parse(sBookingDateTime);
                    Calendar calBdt = Calendar.getInstance();
                    calBdt.setTime(bdt);
                    Calendar today = Calendar.getInstance();

                    long diff = today.getTimeInMillis() - calBdt.getTimeInMillis(); //result in millis
                    if (diff / (1000 * 60 * 60) > 1) { // If booking was more than an hour prior it shoudl timeout
                        return;
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "Failed to parse booking datetime: " + sBookingDateTime
                            + " - " + e.getMessage());
                }
                sUser_Type = jResponse.isNull("usertype") ? ""
                        : jResponse.getString("usertype");
                booking_driver_id = jResponse.isNull("booked_driver_id") ? 0
                        : jResponse.getInt("booked_driver_id");
                trip_id = jResponse.isNull("trip_id") ? 0
                        : jResponse.getInt("trip_id");
                trip_action = jResponse.isNull("trip_action") ? ""
                        : jResponse.getString("trip_action");
                cancelled_driver = jResponse.isNull("cancelled_driver") ? 0
                        : jResponse.getInt("cancelled_driver");
                cancelled_passenger = jResponse.isNull("cancelled_passenger") ? 0
                        : jResponse.getInt("cancelled_passenger");
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).
                        putInt(Constants_CED.CAB_TRIP_ID_DRIVER, trip_id);
                if (trip_id > 0) {
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                            .putInt(Constants_CED.PREF_TRIP_ID_INT, trip_id);
                }
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).
                        putString(Constants_CED.CAB_TRIP_USER_TYPE_DRIVER, sUser_Type);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).
                        putString(Constants_CED.PREF_CURRENT_TRIP, response);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                        .putString(Constants_CED.PREF_TOTAL_TRIP_COST, "");
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                        .putString(Constants_CED.PREF_TOTAL_TRIP_DISTANCE, "");
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                        .putBoolean(Constants_CED.PREF_TRIP_COST_DISTANCE_SUBMIT, true);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).commit();
                if (!trip_action.equals(Constants_CED.TRIP_ACTION_END) && cancelled_driver != 1 && cancelled_passenger != 1) {
                    StopLocationService();
                    Intent intent = new Intent(DashBoard_Driver_act.this, CabEMainDriver_act.class);
                    startActivity(intent);
                }
                finish();
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG, "checkIfBookedResponse: ", e, DashBoard_Driver_act.this);
            snackbar = Snackbar.make(coordinatorLayout, getString(R.string.retry_internet), Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }*/

    public BroadcastReceiver mMessageBroadcastReceiverDashBoard = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String response = intent.getStringExtra("newresponse");
            int errorvalue = intent.getIntExtra("newerrorvalue", 0);
            if (errorvalue == 2) {
                createTripResponse(response);
            }
        }
    };

    public BroadcastReceiver mMessageReceiverDashBoard = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            CGlobals_lib_ss.getInstance().turnGPSOn1(DashBoard_Driver_act.this, mGoogleApiClient);
            String response = intent.getStringExtra("response");
            int errorvalue = intent.getIntExtra("errorvalue", 0);
            new getCheckInternetGPS().execute("");
            if (errorvalue == 1) {
                //checkIfBookedResponse(response);
                new getCheckIfBook(response).execute("");
                /*isServerVolleyCallFailed = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this)
                        .getBoolean(Constants_CED.PREF_SERVER_VOLLEY_CALL_FAILED, false);
                if (isServerVolleyCallFailed) {
                    int iChecked = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this)
                            .getInt(Constants_CED.PREF_SET_SWITCH_FLAG, 2);
                    boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(DashBoard_Driver_act.this);
                    if (!isInternetCheck) {
                        int appuserid = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this)
                                .getInt(Constants_lib_ss.PREF_APPUSERID, -1);
                        db = new DatabaseHandler_CabE(DashBoard_Driver_act.this);
                        sendFlag(Constants_CED.IS_FOR_HIRE, iChecked);
                        db.addFlagSetDriver(String.valueOf(iChecked), sDateTime, appuserid, CGlobals_lib_ss.msGmail, CGlobals_lib_ss.mIMEI);
                    } else {
                        snackbar = Snackbar.make(coordinatorLayout, getString(R.string.retry_internet), Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                }*/
            } else if (errorvalue == 2) {
                new getCheckLoginRequested(response).execute("");
            }
            boolean bTripCostDialog = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this)
                    .getBoolean(Constants_CED.PREF_UPDATE_TRIP_COST_DISTANCE_DIALOG, true);
            if (!bTripCostDialog && !isUpdateCose) {
                updateTripCost();
            }
        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(100); // Update location every second
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            CGlobals_lib_ss.setMyLocation(mLastLocation, true, getApplicationContext());
        }
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        CGlobals_lib_ss.setMyLocation(location, true, getApplicationContext());

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        buildGoogleApiClient();
    }

    synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        MyLocation myLocation = new MyLocation(
                MyApplication_CED.mVolleyRequestQueue, DashBoard_Driver_act.this, mGoogleApiClient);
        myLocation.getLocation(this, onLocationResult);
    }

    private MyLocation.LocationResult onLocationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {

            try {
                CGlobals_lib_ss.setMyLocation(location, false, DashBoard_Driver_act.this);
            } catch (Exception e) {
                e.printStackTrace();
                SSLog_SS.e(TAG, "LocationResult", e, DashBoard_Driver_act.this);
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CGlobals_lib_ss.REQUEST_LOCATION_LIB) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    if (mGoogleApiClient != null)
                        mGoogleApiClient.connect();
                    startLocationUpdates();
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

    protected void startLocationUpdates() {
        MyLocation myLocation = new MyLocation(
                MyApplication_CED.mVolleyRequestQueue, DashBoard_Driver_act.this, mGoogleApiClient);
        myLocation.getLocation(this, onLocationResult);

        if (ActivityCompat.checkSelfPermission(DashBoard_Driver_act.this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(DashBoard_Driver_act.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: " + pendingResult.toString());
    }

    public void createTrip() {
        progressMessage("please wait...");
        final String url = Constants_CED.TRIP_CREATE_DRIVER_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        SSLog_SS.d("ResponseTripId  ", response);
                        createTripResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                String sDateTime = df.format(cal.getTime());
                int appuserid = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this)
                        .getInt(Constants_lib_ss.PREF_APPUSERID, -1);
                Location location = CGlobals_lib_ss.getInstance().getMyLocation(DashBoard_Driver_act.this);
                db = new DatabaseHandler_CabE(DashBoard_Driver_act.this);
                db.addCreateTrip(location, appuserid, Constants_CED.TRIP_TYPE_SELF_DRIVER, Constants_CED.TRIP_ACTION_CREATE,
                        CGlobals_lib_ss.msGmail, sDateTime, localAddress, "",
                        Double.toString(location.getLatitude()), Double.toString(location.getLongitude()), "", "", "", "", "");
                progressCancel();
                try {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG, "createTrip - ", error, DashBoard_Driver_act.this);
                    }
                } catch (Exception e) {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG, "createTrip - ", e, DashBoard_Driver_act.this);
                    }
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tripaction", Constants_CED.TRIP_ACTION_CREATE);
                params.put("triptype", Constants_CED.TRIP_TYPE_SELF_DRIVER);
                if (!TextUtils.isEmpty(localAddress)) {
                    params.put("fromaddress", localAddress);
                }
                params.put("fromlat", Double.toString(loc.getLatitude()));
                params.put("fromlng", Double.toString(loc.getLongitude()));
                params = CGlobals_lib_ss.getInstance().getBasicMobileParamsShort(params,
                        url, getApplicationContext());
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }

        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, true, DashBoard_Driver_act.this);

    } // createTrip

    protected void createTripResponse(String response) {
        int iTripId;
        if (TextUtils.isEmpty(response) || response.equals("-1")) {
            snackbar = Snackbar
                    .make(coordinatorLayout, getApplicationContext().getString(R.string.tripSetupFailed_cabe)
                            + getApplicationContext().getString(
                            R.string.pleaseTryAgain_cabe), Snackbar.LENGTH_INDEFINITE)
                    .setAction("Cancel", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            snackbar.dismiss();
                        }
                    });
            snackbar.show();
            progressCancel();
        } else {
            System.out.println("response createTripResponse   " + response);
            try {
                JSONObject oJson = new JSONObject(response);
                iTripId = oJson.isNull("trip_id") ? -1 : Integer.parseInt(oJson
                        .getString("trip_id"));
                if (iTripId > 0) {
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                            .putInt(Constants_CED.PREF_TRIP_ID_INT, iTripId);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                            .commit();
                }
                Intent intent = new Intent(DashBoard_Driver_act.this, CabEMainDriver_act.class);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).
                        putString(Constants_CED.CAB_TRIP_USER_TYPE_DRIVER, Constants_CED.TRIP_TYPE_SELF_DRIVER);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                        .putInt(Constants_CED.PERF_SET_BUSY_CREATE_TRIP_DRIVER, 1);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).
                        putString(Constants_CED.PREF_SAVE_RESPONSE_PASSENGER, "");
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).
                        putBoolean(Constants_CED.PREF_ISIN_TRIP_MAIN, true);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).commit();
                startActivity(intent);
                progressCancel();
            } catch (Exception e) {
                SSLog_SS.e(TAG, "createTripResponse -  ", e, DashBoard_Driver_act.this);
                progressCancel();
            }
        }
    } // createTripResponse

    @SuppressLint("ParcelCreator")
    class AddressResultReceiver extends ResultReceiver {
        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            // Display the address string or an error message sent from the
            // intent service.
            mAddressOutput = resultData
                    .getString(Constants_lib_ss.RESULT_DATA_KEY);
            // Show a toast message if an address was found.
            if (resultCode == Constants_lib_ss.SUCCESS_RESULT) {
                if (!TextUtils.isEmpty(mAddressOutput)) {
                    localAddress = mAddressOutput;
                }
            }
        }
    }

    protected void startIntentServiceCurrentAddress(Location location) {

        try {
            Intent intent = new Intent(DashBoard_Driver_act.this,
                    FetchAddressIntentService.class);
            intent.putExtra(Constants_lib_ss.RECEIVER, mResultReceiver);
            intent.putExtra(Constants_lib_ss.LOCATION_DATA_EXTRA, location);
            startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // startIntentServiceCurrentAddress

    protected void progressCancel() {
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
            }
        }

    }

    protected void progressMessage(String msg) {
        if (!isFinishing()) {
            mProgressDialog.setMessage(msg);
            mProgressDialog.show();
        }
    }

    private void updateTripCost() {
        try {
            isUpdateCose = true;
            final Dialog dialog = new Dialog(DashBoard_Driver_act.this, R.style.AppCompatAlertDialogStyle);
            dialog.setContentView(R.layout.update_trip_cost);
            dialog.setCancelable(true);
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
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                                .putBoolean(Constants_CED.PREF_UPDATE_TRIP_COST_DISTANCE_DIALOG, true);
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                                .putString(Constants_CED.PREF_TOTAL_TRIP_COST, sCostValue);
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                                .putString(Constants_CED.PREF_TOTAL_TRIP_DISTANCE, sDistance);
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).commit();
                        int tripid = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this)
                                .getInt(Constants_CED.PREF_TRIP_ID_INT, -1);
                        String sUserType = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this).
                                getString(Constants_CED.CAB_TRIP_USER_TYPE_DRIVER, "");
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                                Locale.getDefault());
                        Calendar cal = Calendar.getInstance();
                        String sDateTime = df.format(cal.getTime());
                        int appuserid = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this)
                                .getInt(Constants_lib_ss.PREF_APPUSERID, -1);
                        db = new DatabaseHandler_CabE(DashBoard_Driver_act.this);
                        db.addTripCostDistance(appuserid, tripid, sUserType, CGlobals_lib_ss.msGmail,
                                sDateTime, CGlobals_lib_ss.mIMEI,
                                sCostValue, sDistance);
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                                .putBoolean(Constants_CED.PREF_TRIP_COST_DISTANCE_SUBMIT, true);
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).commit();
                        dialog.getContext();
                        dialog.cancel();
                    }
                }
            });
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*private void updateTripCost_CallServer(final String sCostValue, final String sDistance) {
        final int tripid = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this)
                .getInt(Constants_CED.PREF_TRIP_ID_INT, -1);
        final String sUserType = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this).
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
                int appuserid = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this)
                        .getInt(Constants_lib_ss.PREF_APPUSERID, -1);
                db = new DatabaseHandler_CabE(DashBoard_Driver_act.this);
                db.addTripCostDistance(appuserid, tripid, sUserType, CGlobals_lib_ss.msGmail,
                        sDateTime, CGlobals_lib_ss.mIMEI,
                        sCostValue, sDistance);
                try {
                    SSLog_SS.e(TAG, "updateTripCost - ", error, DashBoard_Driver_act.this);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "updateTripCost - ", e, DashBoard_Driver_act.this);
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
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params, url, DashBoard_Driver_act.this);
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, true, DashBoard_Driver_act.this);
    }*/

    private class getCheckInternetGPS extends AsyncTask<String, Integer, String> {
        boolean isInternetCheck = false;

        @Override
        protected String doInBackground(String... params) {
            try {
                isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(DashBoard_Driver_act.this);
            } catch (Exception e) {
                SSLog_SS.e(TAG, "checkInternetGPS: ", e, DashBoard_Driver_act.this);
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
                    ivNoInternetConnection.setVisibility(View.GONE);
                } else {
                    ivNoInternetConnection.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG, "checkInternetGPS: ", e, DashBoard_Driver_act.this);
            }
            super.onPostExecute(result);
        }
    }

    private class getCheckIfBook extends AsyncTask<String, Integer, String> {
        String res;

        getCheckIfBook(String response) {
            res = response;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                if (TextUtils.isEmpty(res) || res.equals("-1")) {
                    return null;
                }
                miForHire = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this).
                        getInt(Constants_CED.PREF_SET_SWITCH_FLAG, 2);
                loginLogoutFlag = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this)
                        .getBoolean(Constants_CED.PREF_LOGIN_LOGOUT_FLAG, false);
                if (miForHire == 1 && loginLogoutFlag) {
                    Gson gson = new Gson();
                    String json = gson.toJson(res);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).
                            putString(Constants_CED.PREF_SAVE_RESPONSE_PASSENGER, json);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).commit();
                    JSONObject jResponse = new JSONObject(res);
                    String sBookingDateTime = jResponse.isNull("booking_time") ? ""
                            : jResponse.getString("booking_time");
                    try {
                        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                                Locale.getDefault());
                        Date bdt = df1.parse(sBookingDateTime);
                        Calendar calBdt = Calendar.getInstance();
                        calBdt.setTime(bdt);
                        Calendar today = Calendar.getInstance();

                        long diff = today.getTimeInMillis() - calBdt.getTimeInMillis(); //result in millis
                        if (diff / (1000 * 60 * 60) > 1) { // If booking was more than an hour prior it should timeout
                            return null;
                        }
                    } catch (ParseException e) {
                        Log.e(TAG, "Failed to parse booking datetime: " + sBookingDateTime
                                + " - " + e.getMessage());
                    }
                    sUser_Type = jResponse.isNull("usertype") ? ""
                            : jResponse.getString("usertype");
                    booking_driver_id = jResponse.isNull("booked_driver_id") ? 0
                            : jResponse.getInt("booked_driver_id");
                    trip_id = jResponse.isNull("trip_id") ? 0
                            : jResponse.getInt("trip_id");
                    trip_action = jResponse.isNull("trip_action") ? ""
                            : jResponse.getString("trip_action");
                    cancelled_driver = jResponse.isNull("cancelled_driver") ? 0
                            : jResponse.getInt("cancelled_driver");
                    cancelled_passenger = jResponse.isNull("cancelled_passenger") ? 0
                            : jResponse.getInt("cancelled_passenger");
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).
                            putInt(Constants_CED.CAB_TRIP_ID_DRIVER, trip_id);
                    if (trip_id > 0) {
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                                .putInt(Constants_CED.PREF_TRIP_ID_INT, trip_id);
                    }
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).
                            putString(Constants_CED.CAB_TRIP_USER_TYPE_DRIVER, sUser_Type);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).
                            putString(Constants_CED.PREF_CURRENT_TRIP, res);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                            .putString(Constants_CED.PREF_TOTAL_TRIP_COST, "");
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                            .putString(Constants_CED.PREF_TOTAL_TRIP_DISTANCE, "");
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                            .putBoolean(Constants_CED.PREF_TRIP_COST_DISTANCE_SUBMIT, true);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                            .putInt(Constants_CED.PERF_SET_BUSY_CREATE_TRIP_DRIVER, 1);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).commit();
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG, "checkInternetGPS: ", e, DashBoard_Driver_act.this);
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
                if (miForHire == 1 && loginLogoutFlag) {
                    if (!TextUtils.isEmpty(trip_action)) {
                        if (!trip_action.equals(Constants_CED.TRIP_ACTION_END) && cancelled_driver != 1 && cancelled_passenger != 1) {
                            StopLocationService();
                            Intent intent = new Intent(DashBoard_Driver_act.this, CabEMainDriver_act.class);
                            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).
                                    putBoolean(Constants_CED.PREF_ISIN_TRIP_MAIN, true);
                            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).commit();
                            startActivity(intent);
                            finish();
                        }
                    }
                }
            } catch (Exception e) {
                snackbar = Snackbar.make(coordinatorLayout, getString(R.string.retry_internet), Snackbar.LENGTH_LONG);
                snackbar.show();
                SSLog_SS.e(TAG, "checkInternetGPS: ", e, DashBoard_Driver_act.this);
            }
            super.onPostExecute(result);
        }
    }

    private class getCheckLoginRequested extends AsyncTask<String, Integer, String> {
        String res;
        int sLogin_Requested;

        getCheckLoginRequested(String response) {
            res = response;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                if (TextUtils.isEmpty(res) || res.equals("-1")) {
                    return null;
                }
                JSONObject jResponse = new JSONObject(res);
                sLogin_Requested = jResponse.isNull("login_requested") ? -1
                        : jResponse.getInt("login_requested");
            } catch (Exception e) {
                SSLog_SS.e(TAG, "checkInternetGPS: ", e, DashBoard_Driver_act.this);
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
                if (sLogin_Requested == 1) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(DashBoard_Driver_act.this);
                    alertDialog.setTitle("Somebody wants to login to this vehicle.\n" +
                            "If you have handed over the vehicle, press yes.\n" +
                            "If you are still using the vehicle, press No.");
                    alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                                    .putString(Constants_CED.PREF_CHECK_LOGIN_REQUEST_RESPONSE, "");
                            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).commit();
                            dialog.cancel();
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                                    Locale.getDefault());
                            Calendar cal = Calendar.getInstance();
                            String sDateTime = df.format(cal.getTime());
                            String spShiftid = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this)
                                    .getString(Constants_CED.PREF_SHIFT_ID, "");
                            String spVehicleid = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this)
                                    .getString(Constants_CED.PREF_VEHICLE_ID, "");
                            int appuserid = CGlobals_lib_ss.getInstance().getPersistentPreference(DashBoard_Driver_act.this)
                                    .getInt(Constants_lib_ss.PREF_APPUSERID, -1);
                            db = new DatabaseHandler_CabE(DashBoard_Driver_act.this);
                            db.addDriverShifEnd(appuserid, CGlobals_lib_ss.msGmail,
                                    sDateTime, CGlobals_lib_ss.mIMEI, spShiftid, spVehicleid);
                            db.addFlagSetDriver(String.valueOf(0), sDateTime, appuserid, CGlobals_lib_ss.msGmail,
                                    CGlobals_lib_ss.mIMEI);
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();

                        }
                    });
                    alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this)
                                    .putString(Constants_CED.PREF_CHECK_LOGIN_REQUEST_RESPONSE, "");
                            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DashBoard_Driver_act.this).commit();
                            dialog.cancel();
                        }
                    });
                    if (!DashBoard_Driver_act.this.isFinishing()) {
                        alertDialog.show();
                    }
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG, "checkInternetGPS: ", e, DashBoard_Driver_act.this);
            }
            super.onPostExecute(result);
        }
    }

    private void setYourPointToPointTrip() {
        boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(DashBoard_Driver_act.this);
        if (!isInternetCheck) {
            Intent intent = new Intent(DashBoard_Driver_act.this, CreateShareTrip_act.class);
            startActivity(intent);
            finish();
        } else {
            snackbar = Snackbar.make(coordinatorLayout, getString(R.string.retry_internet), Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    // Method to start the service
    public void startService() {
        startService(new Intent(DashBoard_Driver_act.this, MyService_CED.class));
    }

    // Method to stop the service
//    public void stopService() {
//        stopService(new Intent(DashBoard_Driver_act.this, MyService_CED.class));
//    }
}
