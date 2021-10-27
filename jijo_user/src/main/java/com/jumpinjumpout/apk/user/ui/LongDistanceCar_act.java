package com.jumpinjumpout.apk.user.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.CTrip;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.lib.ui.SearchAddress_act;
import com.jumpinjumpout.apk.user.CGlobals_user;
import com.jumpinjumpout.apk.user.Constants_user;
import com.jumpinjumpout.apk.user.GcmIntentService;
import com.jumpinjumpout.apk.user.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lib.app.util.CAddress;
import lib.app.util.Constants_lib_ss;
import lib.app.util.FetchAddressIntentService;
import lib.app.util.MyLocation;

/**
 * Created by jijo_soumen on 07/01/2016.
 */
public class LongDistanceCar_act extends Activity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final static String TAG = "LongDistanceCar_act: ";
    public static TextView ac_from;
    public static TextView ac_to;
    public EditText etGetAvailable;
    public FrameLayout active_trips_list_fragment_container;
    LocationRequest mLocationRequest;
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    GoogleApiClient mGoogleApiClient;
    private AddressResultReceiver mResultReceiver;
    public boolean isLocation = false;
    public ProgressDialog mProgressDialog;
    Location mCurrentLocation;
    public static double fromLat = Constants_user.INVALIDLAT,
            fromLng = Constants_user.INVALIDLNG;
    private final int ACTRESULT_FROM = 1223;
    private final int ACTRESULT_TO = 2223;
    public static double toLat = Constants_user.INVALIDLAT,
            toLng = Constants_user.INVALIDLNG;
    CTrip trip = null;
    JSONArray majActiveUsers = null;
    static public String msStatus = "";
    private static boolean mFilterTrip = false;
    static public ArrayList<CTrip> userTrips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.longdistancecar_act);
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(LongDistanceCar_act.this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        if (!MyApplication.getInstance().getPersistentPreference().getBoolean(Constants_user.PREF_GOTIT_ACTIVE_TRIPS, false)) {
            doGotIt();
        }
        init();
        mResultReceiver = new AddressResultReceiver(new Handler());
        mProgressDialog = new ProgressDialog(LongDistanceCar_act.this);
        mProgressDialog.setMessage("Please wait");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        CGlobals_lib.getInstance().maoRecentAddress = CGlobals_lib.getInstance()
                .readRecentAddresses(this);
        CGlobals_user.getInstance().setFromAddr(new CAddress());
        CGlobals_user.getInstance().setToAddr(new CAddress());
        mCurrentLocation = CGlobals_user.getInstance().getMyLocation(LongDistanceCar_act.this);
        if (mCurrentLocation != null) {
            fromLat = mCurrentLocation.getLatitude();
            fromLng = mCurrentLocation.getLongitude();
            startIntentService(mCurrentLocation);
        }
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager;
        mNotificationManager = (NotificationManager) getSystemService(ns);
        mNotificationManager.cancel(Constants_user.SERVER_NOTIFICATION_ID);
        userTrips = new ArrayList<CTrip>();
        etGetAvailable.addTextChangedListener(textWatcher);
        ac_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(LongDistanceCar_act.this,
                        SearchAddress_act.class);
                startActivityForResult(i, ACTRESULT_FROM);
            }
        });
        ac_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(LongDistanceCar_act.this,
                        SearchAddress_act.class);
                startActivityForResult(i, ACTRESULT_TO);
            }
        });
        CGlobals_lib.showGPSDialog = true;
    }

    @Override
    protected void onResume() {
        MyLocation myLocation = new MyLocation(
                MyApplication.mVolleyRequestQueue, LongDistanceCar_act.this, mGoogleApiClient);
        myLocation.getLocation(this, onLocationResult);
        CGlobals_lib.getInstance().turnGPSOn1(LongDistanceCar_act.this, mGoogleApiClient);
        CGlobals_user.getInstance().runRightActivity(this);
        GcmIntentService.arrayDeque.clear();
        CGlobals_user.getInstance().sendUpdatePosition(
                CGlobals_user.getInstance().getMyLocation(LongDistanceCar_act.this), getApplicationContext());
        refresh();
        super.onResume();
    }

    private void init() {
        ac_from = (TextView) findViewById(R.id.ac_from);
        ac_to = (TextView) findViewById(R.id.ac_to);
        etGetAvailable = (EditText) findViewById(R.id.etGetAvailable);
        active_trips_list_fragment_container = (FrameLayout) findViewById(R.id.active_trips_list_fragment_container);
    }

    // EditText TextWatcher
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            try {
                String text = etGetAvailable.getText().toString()
                        .toLowerCase(Locale.getDefault());
                MyApplication.getInstance().getPersistentPreferenceEditor().putString(Constants_user.PREF_ENTER_SEAT_BOOK, text);
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                getActiveLongDistanceTrips();
                SSLog.i(TAG, "filtered");
            } catch (Exception e) {
                SSLog.e(TAG, "EditText TextWatcher", e);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String sAddr = "";
        CAddress oAddr = null;
        if (requestCode == ACTRESULT_FROM) {
            if (resultCode == RESULT_OK) {
                sAddr = data.getStringExtra("street_address");
                fromLat = data.getDoubleExtra("lat", Constants_user.INVALIDLAT);
                fromLng = data.getDoubleExtra("lng", Constants_user.INVALIDLNG);
                oAddr = new CAddress();
                oAddr.setAddress(sAddr);
                oAddr.setLatitude(fromLat);
                oAddr.setLongitude(fromLng);
                isLocation = true;
                ac_from.setText(sAddr);
                if (!TextUtils.isEmpty(sAddr)) {
                    ac_from.setText(sAddr);
                    if (!isFinishing()) {
                    }
                }
                MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("FROMLAT", (float) fromLat);
                MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("FROMLNG", (float) fromLng);
                MyApplication.getInstance().getPersistentPreferenceEditor().putString("PASSENGER_START_ADDRESS", sAddr);
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
            }
            if (resultCode == RESULT_CANCELED) {
                // Write your code if there's no result
            }
        }
        if (requestCode == ACTRESULT_TO) {
            if (resultCode == RESULT_OK) {

                sAddr = data.getStringExtra("street_address");
                toLat = data.getDoubleExtra("lat", Constants_user.INVALIDLAT);
                toLng = data.getDoubleExtra("lng", Constants_user.INVALIDLNG);
                oAddr = new CAddress();
                oAddr.setAddress(sAddr);
                oAddr.setLatitude(toLat);
                oAddr.setLongitude(toLng);
                ac_to.setText(sAddr);
                if (!TextUtils.isEmpty(sAddr)) {
                    ac_to.setText(sAddr);
                    if (!isFinishing()) {

                    }
                }
                MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("TOLAT", (float) toLat);
                MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("TOLNG", (float) toLng);
                MyApplication.getInstance().getPersistentPreferenceEditor().putString("PASSENGER_DESTINATION_ADDRESS", sAddr);
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
            }
            if (resultCode == RESULT_CANCELED) {
                // Write your code if there's no result
            }
        }

        if (requestCode == CGlobals_lib.REQUEST_LOCATION) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    Log.d(TAG, "YES");
                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    Log.d(TAG, "NO");
                    CGlobals_lib.showGPSDialog = false;
                    finish();
                    break;
                default:
                    break;
            }
        }

        getActiveLongDistanceTrips();
        if (!TextUtils.isEmpty(sAddr)) {
            CGlobals_lib.getInstance().addRecentAddress(oAddr);
        }

    } // onActivityResult

    @Override
    protected void onPause() {
        CGlobals_lib.getInstance()
                .writeRecentAddresses(getApplicationContext());
        removeActiveUserFragment();
        super.onPause();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void doGotIt() {
        String gotmessage = "You will see trips created by your friends and your commuting community members here." +
                " Only trips passing near your current location are shown." +
                "\n\nYou can filter the rides by changing your start-location (Jump.in point)" +
                " and/or entering a destination location (Jump.out point)";
        if (!isFinishing()) {
            CGlobals_user.getInstance().gotIt(this,
                    Constants_user.PREF_GOTIT_ACTIVE_TRIPS, "", gotmessage);
        }
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string or an error message sent from the
            // intent service.
            String mAddressOutput = resultData
                    .getString(Constants_lib_ss.RESULT_DATA_KEY);

            // Show a toast message if an address was found.
            if (resultCode == Constants_user.SUCCESS_RESULT && !isLocation) {
                ac_from.setText(mAddressOutput);
                MyApplication.getInstance().getPersistentPreferenceEditor()
                        .putString("PASSENGER_START_ADDRESS", mAddressOutput);
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
            }

        }
    }

    protected void startIntentService(Location location) {

        try {
            Intent intent = new Intent(LongDistanceCar_act.this,
                    FetchAddressIntentService.class);
            intent.putExtra(Constants_lib_ss.RECEIVER,
                    mResultReceiver);
            intent.putExtra(Constants_lib_ss.LOCATION_DATA_EXTRA,
                    location);
            startService(intent);
        } catch (Exception e) {
            SSLog.e(TAG, "startIntentService: ", e);
        }
    } // startIntentServiceCurrentAddress

    public void removeActiveUserFragment() {
        try {
            LongDistanceTripList_frag fragment = (LongDistanceTripList_frag) getFragmentManager()
                    .findFragmentById(R.id.active_trips_list_fragment_container);
            if (fragment != null) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager
                        .beginTransaction();
                fragmentTransaction.remove(fragment);
                fragmentTransaction.commit();
            }
        } catch (Exception e) {
            SSLog.e(TAG, "removeActiveUserFragment: ", e);
        }
    }

    public void addActiveUsersFragment() {
        try {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager
                    .beginTransaction();
            LongDistanceTripList_frag fragment = new LongDistanceTripList_frag();
            fragmentTransaction.add(R.id.active_trips_list_fragment_container,
                    fragment);
            fragmentTransaction.commitAllowingStateLoss();
        } catch (Exception e) {
            SSLog.e(TAG, "addActiveUsersFragment: ", e);
        }
    }

    public void refresh() {
        if (TextUtils.isEmpty(ac_to.getText().toString())) {
            CGlobals_user.getInstance().setFromAddr(new CAddress());
        }
        removeActiveUserFragment();
        addActiveUsersFragment();
    }

    public void showpDialog() {
        if (!isFinishing()) {
            mProgressDialog.setMessage("Please wait ...");
            mProgressDialog.show();
        }
    }

    public void hidepDialog() {
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
            }
        }
    }

    private MyLocation.LocationResult onLocationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            if (location != null) {
                CGlobals_user.getInstance().setMyLocation(location, false);
                CGlobals_user.getInstance().sendUpdatePosition(
                        CGlobals_user.getInstance().getMyLocation(LongDistanceCar_act.this), getApplicationContext());
            }
        }


    };

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mCurrentLocation = location;
        startIntentService(mCurrentLocation);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    void formList(String response) {
        try {
            if (response.trim().equals("-1")) {
                refresh();
                return;
            }
            showpDialog();
            majActiveUsers = new JSONArray(response);
            int nPassengers = majActiveUsers.length();
            if (nPassengers == 0) {
            }
            boolean isAllowStrangers = MyApplication
                    .getInstance()
                    .getPersistentPreference()
                    .getBoolean(Constants_user.PREF_NOTIFICATION_STRANGER,
                            false);
            for (int j = 0; j < nPassengers; j++) {
                trip = new CTrip(majActiveUsers.getJSONObject(j).toString(), LongDistanceCar_act.this);
                if (trip.getCabInPlay() > 0) {
                }

                if (!trip.getIsFriend() && !isAllowStrangers
                        && !trip.isCommercial())
                    continue;
                if (Actrips_act.getFilterTrip() && trip.getInPath() < 1) {
                    continue;
                }
                userTrips.add(trip);
            }
            refresh();
            hidepDialog();
        } catch (JSONException e) {
            hidepDialog();
            String sErr = "";
            if (LongDistanceCar_act.this == null) {
                sErr = " getActivity returned null";
            } else {
                sErr = " getActivity.getApplicationContext returned null";
            }
            SSLog.e(TAG, "formList - " + response + ", " + sErr, e);
            refresh();
        } catch (Exception e) {
            hidepDialog();
            SSLog.e(TAG, "formList - ", e);
            if (Actrips_act.msTripType.equals(Constants_user.TRIP_TYPE_USER)) {
                if (Actrips_act.cabTrips == null) {
                } else {
                    msStatus = "Something went wrong. Please go back to dashboard and try again";
                }
            } else if (Actrips_act.msTripType.equals(Constants_user.TRIP_TYPE_COMMERCIAL)) {
                if (Actrips_act.cabTrips == null) {
                    msStatus = "Something went wrong. Please go back to dashboard and try again";
                } else {
                }
            }
            refresh();
        }

    } // formList

    protected void getActiveLongDistanceTrips() {
        showpDialog();
        userTrips = new ArrayList<CTrip>();
        if (userTrips.size() > 0) {
            userTrips.clear();
        }
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());

        if (fromLat == Constants_user.INVALIDLAT
                || fromLng == Constants_user.INVALIDLNG) {
            return;
        }
        final String url = Constants_user.LONG_DISTANCE_ACTIVE_USERS_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        gotActiveCommercial(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    hidepDialog();
                    getActiveUsersError(error);
                    SSLog.e(TAG, Constants_user.LONG_DISTANCE_ACTIVE_USERS_URL
                                    + ": Response.ErrorListener - ",
                            error.getMessage());
                } catch (Exception e) {
                    hidepDialog();
                    SSLog.e(TAG,
                            " getActiveUsersCommercial Response.ErrorListener (2) - ",
                            e);
                }
            }

        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("triptype", Constants_user.TRIP_TYPE_COMMERCIAL);
                params.put("tripdistance", Double
                        .toString(Constants_user.DEFAULT_WALKING_DISTANCE));
                params.put("lat",
                        String.format("%.9f", fromLat));
                params.put("lng",
                        String.format("%.9f", fromLng));
                if (toLat != Constants_user.INVALIDLAT && toLng != Constants_user.INVALIDLNG
                        && !TextUtils.isEmpty(ac_to.getText().toString())) {
                    params.put("tolat",
                            String.format("%.9f", toLat));
                    params.put("tolng",
                            String.format("%.9f", toLng));
                } else {
                    params.put("tolat",
                            String.format("%.9f", fromLat));
                    params.put("tolng",
                            String.format("%.9f", fromLng));
                }
                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        url);
                params.put("plannedstartdatetime", params.get("clientdatetime"));
                String delim = "";
                StringBuilder getParams = new StringBuilder();

                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String debugUrl = "";
                try {
                    debugUrl = url + "?" + getParams.toString() + "&verbose=Y";
                    System.out.println("debugUrl" + debugUrl);
                } catch (Exception e) {
                    SSLog.e(TAG, "getPassengers map - ", e);
                }

                return CGlobals_user.getInstance().checkParams(params);
            }
        };
        try {
            MyApplication.getInstance().addVolleyRequest(postRequest, false);
        } catch (Exception e) {
            SSLog.e(TAG, "getActiveuser CGlobals.getInstance().mVolleyReq..", e);
            CGlobals_user.getInstance().init(LongDistanceCar_act.this);
        }
    }

    private void gotActiveCommercial(String response) {
        hidepDialog();
        formList(response);
    }

    private void getActiveUsersError(VolleyError error) {
        if (error instanceof NetworkError) {
            msStatus = "";
            if (!MyApplication.getInstance().getConnectivity().connectionError(LongDistanceCar_act.this, getString(R.string.app_label))) {
            }
        } else if (error instanceof ServerError) {
        } else if (error instanceof AuthFailureError) {
        } else if (error instanceof ParseError) {
        } else if (error instanceof NoConnectionError) {
        } else if (error instanceof TimeoutError) {
        } else {
            String sErr = error != null ? error.getMessage() : "";
            msStatus = "Error: " + sErr;
            if (!TextUtils.isEmpty(sErr)) {
                SSLog.e(TAG, " getActiveUsersError - ", error.getMessage());
            } else {
                SSLog.e(TAG, " getActiveUsersError - ", sErr);
            }
        }

        Toast.makeText(LongDistanceCar_act.this,
                "Request failed. Please touch refresh to try again",
                Toast.LENGTH_SHORT).show();
        getActiveLongDistanceTrips();
    } // getActiveUsersError
}
