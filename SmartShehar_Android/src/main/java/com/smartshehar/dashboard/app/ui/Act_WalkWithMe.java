package com.smartshehar.dashboard.app.ui;


import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.smartshehar.dashboard.app.CGeo;
import com.smartshehar.dashboard.app.CGlobals_db;
import com.smartshehar.dashboard.app.Constants_dp;
import com.smartshehar.dashboard.app.ContactsAutoComplete;
import com.smartshehar.dashboard.app.EmergencyContacts;
import com.smartshehar.dashboard.app.LocationService;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;


public class Act_WalkWithMe extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG,"CONNECTED");
    }

    private static final String TAG = "Acitivity_WalkWithMe: ";


    ProgressDialog mProgressDialog;
    GoogleApiClient mGoogleApiClient = null;

    private TextView mTvStatus;
    private Button btnStart;
    private EmergencyContacts emergencycontacts;
    private ContactsAutoComplete acEmail;
    private String sEmails = "";
//    CallHome mCH;
    private Location mCurrentLocation = null;
    Connectivity mConnectivity;

    /**
     * Called when the activity is first created.
     */
//        private boolean mAlternateTitle = false;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//       	AnalyticsUtils.getInstance(this).trackPageView(getString(R.string.pageBeSafeDashboardLong));
        mConnectivity = new Connectivity();

        mGoogleApiClient = new GoogleApiClient.Builder(Act_WalkWithMe.this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(Act_WalkWithMe.this)
                .addOnConnectionFailedListener(Act_WalkWithMe.this)
                .build();
        mGoogleApiClient.connect();
        mCurrentLocation = CGlobals_db.getInstance(Act_WalkWithMe.this).getBestLocation(Act_WalkWithMe.this);
        if (mCurrentLocation != null) {
            Toast.makeText(this, "Got Location!", Toast.LENGTH_SHORT).show();
        }
//		Log.d("tag",mCurrentLocation+" "+CGlobals_db.getInstance(Act_WalkWithMe.this).mPackageInfo+" "+CGlobals_db.getInstance(Act_WalkWithMe.this).mUserInfo);
        /*mCH = new CallHome(Act_WalkWithMe.this,mCurrentLocation,
                CGlobals_db.getInstance(Act_WalkWithMe.this).mPackageInfo, getString(R.string.appNameShort), getString(R.string.appCode),
				CGlobals_db.getInstance(Act_WalkWithMe.this).mUserInfo);
*/

        CGlobals_db.getInstance(Act_WalkWithMe.this).init(this);
        CGlobals_db.getInstance(Act_WalkWithMe.this).getMyLocation(Act_WalkWithMe.this);
//        mCH.userPing(getString(R.string.pageBeSafeEmergency), "");

        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setHomeButtonEnabled(true);
        setContentView(R.layout.activity_walkwithme);
        if (!Connectivity.checkConnected(Act_WalkWithMe.this)) {
            if (!mConnectivity.connectionError(Act_WalkWithMe.this)) {
                if (mConnectivity.isGPSEnable(Act_WalkWithMe.this)) {
                    Log.d(TAG, "Internet Connection");
                }
            }
        }
       /* mLocationClient = new LocationClient(this, this, this);
        mLocationClient.connect();
*/
        readPrefs();
        mTvStatus = (TextView) findViewById(R.id.tvStatus);
        btnStart = (Button) findViewById(R.id.btnStart);
        acEmail = (ContactsAutoComplete) findViewById(R.id.acEmail);
        if (!TextUtils.isEmpty(sEmails))
            acEmail.setText(sEmails);
//        AppRater.app_launched(this, getString(R.string.pageDashboardLong), CGlobals_db.getInstance(Act_WalkWithMe.this).mPackageInfo.packageName);
        emergencycontacts = new EmergencyContacts(this);
        PreferenceManager.getDefaultSharedPreferences(this);


        btnStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sEmails = acEmail.getText().toString();
                if (CGlobals_db.inWalkWithMe) {    // Stop WalkWithMe
                    CGlobals_db.inWalkWithMe = false;
                    btnStart.setText(getString(R.string.start));
                    stopService(new Intent(Act_WalkWithMe.this, LocationService.class));
                } else {    // Start WalkWithMe
                    if (sEmails.contains("@") && sEmails.contains(".")) {
                        CGlobals_db.inWalkWithMe = true;
                        btnStart.setText(getString(R.string.stop));
                        CGlobals_db.walkWithMeMode = "A";
                        stopService(new Intent(Act_WalkWithMe.this, LocationService.class));
                        startService(new Intent(Act_WalkWithMe.this, LocationService.class));
                        sendLocation();
                    } else {
                        Toast.makeText(Act_WalkWithMe.this, "Please enter valid email id/s", Toast.LENGTH_SHORT).show();
                    }
                }
                writePrefs();
                finish();
            }
        });

    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//			 TextView tvLatLon = (TextView) findViewById(R.id.tvLatLon);
            if (!CGlobals_db.inWalkWithMe)
                return;
            if (mCurrentLocation != null) {
                CGlobals_db.walkWithMeMode = "U";
                sendLocation();
            }
        }
    };

    private void sendLocation() {


        final String url = Constants_dp.SAFTEY_SHIELD_PHP_PATH + "walkwithme_updateposition.php";
        try {


            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (!response.trim().equals("-1")) {
                                if (CGlobals_db.walkWithMeMode.equals( "A")) {
                                    Toast.makeText(Act_WalkWithMe.this,
                                            "Email sent to: " + sEmails +
                                                    " to 'Walk with you'\n They will see your position on a map",
                                            Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(Act_WalkWithMe.this, "Could not send request ", Toast.LENGTH_LONG).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Log.d(TAG, "error is " + error.toString());
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    Location location = CGlobals_db.getInstance(Act_WalkWithMe.this).getMyLocation(Act_WalkWithMe.this);
                    Calendar cal = Calendar.getInstance();
                    params.put("user_email", emergencycontacts.msMyEmail);
                    params.put("email", emergencycontacts.msMyEmail);
                    params.put("friend_email", sEmails);
                    params.put("mode", CGlobals_db.walkWithMeMode);
                    params.put("stopped", CGlobals_db.inWalkWithMe ? "N" : "Y");
                    params.put("y", Integer.toString(cal.get(Calendar.YEAR)));
                    params.put("o", Integer.toString(cal.get(Calendar.MONTH)));
                    params.put("d", Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
                    params.put("h", Integer.toString(cal.get(Calendar.HOUR_OF_DAY)));
                    params.put("m", Integer.toString(cal.get(Calendar.MINUTE)));
                    params.put("s", Integer.toString(cal.get(Calendar.SECOND)));
                    SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(Act_WalkWithMe.this);
                    String msName = mSettings.getString(Constants_dp.KEY_PREF_MY_NAME, "");
                    String msPhone = mSettings.getString(Constants_dp.KEY_PREF_MY_NUMBER, "");
                    params.put("name", msName);
                    params.put("phone", msPhone);
                    try {
                        if (location != null) {
                            CGeo geo = new CGeo(Act_WalkWithMe.this);
                            geo.getAddress(location);
                            String address = geo.mAddr.firstAddressString;

                            if (!TextUtils.isEmpty(address)) {
                                params.put("address", address);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace(); // getFromLocation() may sometimes fail
                    }
                    params = CGlobals_db.getInstance(Act_WalkWithMe.this).getBasicMobileParams(params,
                            url, Act_WalkWithMe.this);
                    Log.d(TAG, "PARAM IS " + params);
                    return CGlobals_db.getInstance(Act_WalkWithMe.this).checkParams(params);
                }
            };
            CGlobals_db.getInstance(Act_WalkWithMe.this).getRequestQueue(Act_WalkWithMe.this).add(postRequest);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ERROR IS " + e.toString());
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }




    @Override
    protected void onResume() {
        keyboardShow(false);

        readPrefs();
        if (CGlobals_lib_ss.haveNetworkConnection() == 0) {
            try {
                mTvStatus = (TextView) findViewById(R.id.tvStatus);
                mTvStatus.setText(R.string.no_internet);
            } catch (Exception e) {
                SSLog.e(" Emergency - Resume: ", "error ", e.getMessage());
            }
        }
        registerReceiver(broadcastReceiver,
                new IntentFilter(LocationService.BROADCAST_ACTION));
        if (!isServiceRunning(Act_WalkWithMe.this, "com.smartshehar.dashboard.app.LocationService")) {
            CGlobals_db.inWalkWithMe = false;
        }

        if (!CGlobals_db.inWalkWithMe) {
            stopService(new Intent(Act_WalkWithMe.this, LocationService.class));
            btnStart.setText(getString(R.string.start));
        } else
            btnStart.setText(getString(R.string.stop));
        writePrefs();
        super.onResume();
    }


    @Override
    public void onPause() {
//		unregisterReceiver(broadcastReceiver);
        writePrefs();
        super.onPause();
    } // onPause

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_emergency);
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

    private void writePrefs() {
        SharedPreferences mPref = getSharedPreferences(CGlobals_lib_ss.PREFS_NAME, 0);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putBoolean(CGlobals_db.PREFINWALKWITHME, CGlobals_db.inWalkWithMe);
        editor.putInt(CGlobals_db.PREFTRAVELDIST,
                CGlobals_db.miTravelDistance);
        editor.putString(CGlobals_db.PREFINWALKWITHME_EMAILS, sEmails);
        editor.apply();
    }

    private void readPrefs() {
        SharedPreferences pref = getSharedPreferences(CGlobals_lib_ss.PREFS_NAME, 0);
        try {
            CGlobals_db.miTravelDistance =
                    pref.getInt(CGlobals_db.PREFTRAVELDIST, 0);
            CGlobals_db.inWalkWithMe = pref.getBoolean(CGlobals_db.PREFINWALKWITHME, false);
            sEmails = pref.getString(CGlobals_db.PREFINWALKWITHME_EMAILS, "");
        } catch (Exception e) {
            SSLog.e("ActMeter: getPrefs", "error", e.getMessage());
        }
    }

    void keyboardShow(boolean bShow) {
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
        }
    }
	/*public void onConnectionFailed(ConnectionResult arg0) {
		Toast.makeText(this, "Cannot get location. Please turn on GPS", Toast.LENGTH_SHORT).show();
		CGlobals_db.getInstance(Act_WalkWithMe.this).showGPSDisabledAlertToUser(this);
	}*/

    @Override
    public void onConnected(Bundle bundle) {

    }

    /*@Override
        public void onConnected(Bundle arg0) {
            mCurrentLocation = mLocationClient.getLastLocation();
            if (mCurrentLocation != null) {
                Toast.makeText(this, "Got Location!", Toast.LENGTH_SHORT).show();
    //    	    tvMyLocation.setText("My Location: " + mCurrentLocation.getLatitude() +
    //					", " + mCurrentLocation.getLongitude());
                SSLog.i(TAG, " Lat: " + Double.toString(mCurrentLocation.getLatitude()) +
                            ", Lon: " + Double.toString(mCurrentLocation.getLongitude()));
                String address = CGlobals_db.getInstance(Act_WalkWithMe.this).getAddress(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
    //		    showKeyboard(false);
    //		    if (!TextUtils.isEmpty(address) && isAutoLocation) {
    //		    	autoCompleteFrom.setText(address);
    //		    }
            } else {
                Toast.makeText(this, "Cannot get location. Please turn on GPS", Toast.LENGTH_SHORT).show();
                CGlobals_db.getInstance(Act_WalkWithMe.this).showGPSDisabledAlertToUser(this);
            }
    //		mHandler = new Handler();
    //        mHandler.post(getPassengerTrips);
        }
    */
    @Override
    public void onConnectionSuspended(int i) {
    }
} // Activity_BeSafe_Emergency


