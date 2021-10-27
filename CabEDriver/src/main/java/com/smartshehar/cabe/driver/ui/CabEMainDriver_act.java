package com.smartshehar.cabe.driver.ui;

import android.Manifest;
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
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartshehar.cabe.driver.CGlobals_CED;
import com.smartshehar.cabe.driver.CTrip_CED;
import com.smartshehar.cabe.driver.Cancel_Reason_Adapter;
import com.smartshehar.cabe.driver.Constants_CED;
import com.smartshehar.cabe.driver.DatabaseHandler_CabE;
import com.smartshehar.cabe.driver.Fixed_Address;
import com.smartshehar.cabe.driver.LocationService;
import com.smartshehar.cabe.driver.MyApplication_CED;
import com.smartshehar.cabe.driver.PassengerAdapter;
import com.smartshehar.cabe.driver.R;
import com.smartshehar.cabe.driver.VehicleResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lib.app.util.CAddress;
import lib.app.util.CGlobals_lib_ss;
import lib.app.util.CSms;
import lib.app.util.Constants_lib_ss;
import lib.app.util.MyLocation;
import lib.app.util.SSLog_SS;
import lib.app.util.ui.AbstractMapFragment_lib_act;

/**
 * Created by jijo_soumen on 08/03/2016.
 * When waiting for customer and show customer location in map
 */
public class CabEMainDriver_act extends AbstractMapFragment_lib_act {

    private static String TAG = "CabEMainDriver_act: ";
    GoogleApiClient googleApiClient = null;
    Snackbar snackbar;
    CoordinatorLayout coordinatorLayout;
    Button btnStart, btnCancel;
    boolean isForHire = false;
    String trip_action;
    boolean tripcancel = false;
    CTrip_CED cTripDriver;
    String booking_time, planned_start_datetime, triptime, trip_end_notification_sent;
    int secs_to_confirm, driver_appuser_id, passenger_appuser_id, shared_cab, is_for_hire, allowstrangernotifications,
            cabTripId, hasdriver_started;
    Bitmap bmp = null;
    private boolean isVolleyCallFailedCancelTrip = false;
    CSms mSms = null;
    ImageView btnCheckMap, ivPassengerList;
    Button btnCallPassenger;
    int setBusyCreateTripDriver;
    LinearLayout llPassengerInformation;
    CTrip_CED cTripcedReason;
    ArrayList<CTrip_CED> arrayCancelReasonList;
    private double mNearestPassengerLat, mNearestPassengerLng;
    JSONObject oDirectionPoints;
    DatabaseHandler_CabE db;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cabemain_act);
        create();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        googleApiClient = new GoogleApiClient.Builder(CabEMainDriver_act.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(CabEMainDriver_act.this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
        MyLocation myLocation = new MyLocation(
                MyApplication_CED.mVolleyRequestQueue, CabEMainDriver_act.this, googleApiClient);
        myLocation.getLocation(this, onLocationResult);
        init();
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .coordinatorLayout);
        Location loc = CGlobals_lib_ss.getInstance().getMyLocation(this);
        if (loc != null) {
            setLocation(loc);
        }
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        ivPassengerList = (ImageView) findViewById(R.id.ivPassengerList);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this)
                        .putBoolean(Constants_CED.PREF_IS_CHECK_BOOK, false);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this).commit();
                isForHire = true;
                setBusyCreateTripDriver = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this)
                        .getInt(Constants_CED.PERF_SET_BUSY_CREATE_TRIP_DRIVER, 0);
                if (setBusyCreateTripDriver == 1) {
                    int iTripId = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this)
                            .getInt(Constants_CED.PREF_TRIP_ID_INT, -1);
                    Intent intent = new Intent(CabEMainDriver_act.this, CabEDriverForHire_act.class);
                    intent.putExtra("tripid", iTripId);
                    startActivity(intent);
                    StopLocationService();
                    finish();
                } else {
                    Intent intent = new Intent(CabEMainDriver_act.this, CabEDriverForHire_act.class);
                    intent.putExtra("tripid", cabTripId);
                    startActivity(intent);
                    StopLocationService();
                    finish();
                }
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this).
                        putBoolean(Constants_CED.PREF_ISIN_TRIP_MAIN, false);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this).
                        putBoolean("IS_FOR_HIRE_DRIVER", isForHire);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this).commit();

            }
        });
        mSms = new CSms(CabEMainDriver_act.this);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this)
                        .putBoolean(Constants_CED.PREF_IS_CHECK_BOOK, false);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this).commit();
                canceldialog();
            }
        });
        CGlobals_lib_ss.showGPSDialog = true;
    }

    @Override
    protected void onResume() {
        init();
        super.onResume();
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this)
                .putBoolean(Constants_CED.PREF_IS_CHECK_BOOK, false);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this).commit();
        setBusyCreateTripDriver = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this)
                .getInt(Constants_CED.PERF_SET_BUSY_CREATE_TRIP_DRIVER, 0);
        if (!isLocationServiceRunning(LocationService.class)) {
            StartLocationService();
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverMainDriver,
                new IntentFilter(Constants_CED.LOCATION_SERVICE_DRIVER_MAIN));
        Intent sendableIntent = new Intent(Constants_CED.LOCATION_SERVICE_DRIVER_MAIN);
        LocalBroadcastManager.getInstance(this).
                sendBroadcast(sendableIntent);
        if (setBusyCreateTripDriver != 2) {
            if (ivPassengerList != null) {
                ivPassengerList.setVisibility(View.GONE);
            }
        }
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
        stopService(new Intent(CabEMainDriver_act.this, LocationService.class));
        CGlobals_CED.SERVICE_CURRENT = CGlobals_CED.SERVICE_CMD;
        Intent serviceIntent = new Intent(CabEMainDriver_act.this, LocationService.class);
        serviceIntent.putExtra(Constants_CED.SERVICE_FLAG, "CMD");
        startService(serviceIntent);
    }

    protected void StopLocationService() {
        stopService(new Intent(CabEMainDriver_act.this, LocationService.class));
    }

    /*private void customerNameNumber() {
        try {
            final Dialog dialog = new Dialog(CabEMainDriver_act.this, R.style.AppCompatAlertDialogStyle);
            dialog.setContentView(R.layout.customer_name_number);
            dialog.setCancelable(false);
            final ClearableEditText cleCustomerName, cleCustomerNumber;
            TextView tvSaveCustomer, tvSkipCustomer;
            cleCustomerName = (ClearableEditText) dialog.findViewById(R.id.cleCustomerName);
            cleCustomerNumber = (ClearableEditText) dialog.findViewById(R.id.cleCustomerNumber);
            tvSaveCustomer = (TextView) dialog.findViewById(R.id.tvSaveCustomer);
            tvSkipCustomer = (TextView) dialog.findViewById(R.id.tvSkipCustomer);
            tvSaveCustomer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String sCustomerNmae = cleCustomerName.getText().toString();
                    String sCustomerNumber = cleCustomerNumber.getText().toString();
                    if (TextUtils.isEmpty(sCustomerNmae) || TextUtils.isEmpty(sCustomerNumber)) {
                        Toast.makeText(CabEMainDriver_act.this, "", Toast.LENGTH_LONG).show();
                    } else {
                        Log.d("Customer Details", sCustomerNmae + " - " + sCustomerNumber);
                        //customerDetailsSendServer(sCustomerNmae,sCustomerNumber);
                        dialog.dismiss();
                    }
                }
            });

            tvSkipCustomer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    private void bookCabPassenger() {
        String response = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this).
                getString(Constants_CED.PREF_SAVE_RESPONSE_PASSENGER, "");
        Type type = new TypeToken<String>() {
        }.getType();
        String passengerinfo = new Gson().fromJson(response, type);
        try {
            JSONObject jResponse = new JSONObject(passengerinfo);
            cTripDriver = new CTrip_CED(passengerinfo, CabEMainDriver_act.this);
            secs_to_confirm = jResponse.isNull("secs_to_confirm") ? 0
                    : jResponse.getInt("secs_to_confirm");
            driver_appuser_id = jResponse.isNull("driver_appuser_id") ? -1
                    : jResponse.getInt("driver_appuser_id");
            passenger_appuser_id = jResponse.isNull("passenger_appuser_id") ? -1
                    : jResponse.getInt("passenger_appuser_id");
            shared_cab = jResponse.isNull("shared_cab") ? -1
                    : jResponse.getInt("shared_cab");
            is_for_hire = jResponse.isNull("is_for_hire") ? -1
                    : jResponse.getInt("is_for_hire");
            allowstrangernotifications = jResponse.isNull("allowstrangernotifications") ? -1
                    : jResponse.getInt("allowstrangernotifications");
            booking_time = jResponse.isNull("booking_time") ? ""
                    : jResponse.getString("booking_time");
            cabTripId = jResponse.isNull("trip_id") ? -1
                    : jResponse.getInt("trip_id");
            final String sPhoneNo = jResponse.isNull("phoneno") ? ""
                    : jResponse.getString("phoneno");
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this)
                    .putString("DRIVER_PH_NUMBER_SAVE", sPhoneNo);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this).commit();
            TextView tvTotalDistance = (TextView) findViewById(R.id.tvTotalDistance);
            if (!TextUtils.isEmpty(cTripDriver.getFrom())) {
                if (tvTotalDistance != null) {
                    tvTotalDistance.setVisibility(View.VISIBLE);
                    tvTotalDistance.setText(cTripDriver.getFrom());
                }
            }
            btnCallPassenger = (Button) findViewById(R.id.btnCallPassenger);
            if (btnCallPassenger != null) {
                btnCallPassenger.setVisibility(View.VISIBLE);
            }
            btnCallPassenger = (Button) findViewById(R.id.btnCallPassenger);
            btnCallPassenger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(sPhoneNo)) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + sPhoneNo));
                        if (ActivityCompat.checkSelfPermission(CabEMainDriver_act.this,
                                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        startActivity(intent);
                    }
                }
            });

            btnCheckMap = (ImageView) findViewById(R.id.btnCheckMap);
            if (btnCheckMap != null) {
                btnCheckMap.setVisibility(View.VISIBLE);
            }
            btnCheckMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String response = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this).
                            getString(Constants_CED.PREF_CURRENT_TRIP, "");
                    JSONObject jResponse;
                    try {
                        jResponse = new JSONObject(response);
                        String fromLat = CGlobals_lib_ss.getInstance().isNullNotDefined(jResponse, "fromlat")
                                ? "" : jResponse.getString("fromlat");
                        String fromLng = CGlobals_lib_ss.getInstance().isNullNotDefined(jResponse, "fromlng")
                                ? "" : jResponse.getString("fromlng");
                        if (!TextUtils.isEmpty(fromLat) && !TextUtils.isEmpty(fromLng)) {
                            Intent intentNav = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("google.navigation:q=" + fromLat + "," + fromLng));
                            startActivity(intentNav);
                        } else {
                            //// Take some action when from and to is invalide
                            Log.d(TAG, "checkIfBookedResponse");
                        }
                    } catch (JSONException e) {
                        SSLog_SS.e(TAG, "onClickGoogleMap: ", e, CabEMainDriver_act.this);
                    }
                }
            });

            planned_start_datetime = jResponse.isNull("planned_start_datetime") ? ""
                    : jResponse.getString("planned_start_datetime");
            triptime = jResponse.isNull("triptime") ? ""
                    : jResponse.getString("triptime");
            hasdriver_started = jResponse.isNull("hasdriver_started") ? -1
                    : jResponse.getInt("hasdriver_started");
            trip_end_notification_sent = jResponse.isNull("trip_end_notification_sent") ? ""
                    : jResponse.getString("trip_end_notification_sent");
            //String sAddress = cTripDriver.getFrom().substring(0, 25) + "...";
            //IconGenerator tc = new IconGenerator(this);
            /*bmp = tc.makeIcon(sAddress);
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(cTripDriver.getFromLat() + .001, cTripDriver.getFromLng()))
                    .icon(BitmapDescriptorFactory.fromBitmap(bmp)));*/
            mMap.addMarker(new MarkerOptions()
                    .position(
                            new LatLng(cTripDriver.getFromLat(), cTripDriver.getFromLng()))
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_passenger_loc))
                    .alpha(0.8f).draggable(true));
            mNearestPassengerLat = cTripDriver.getFromLat();
            mNearestPassengerLng = cTripDriver.getFromLng();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MyLocation.LocationResult onLocationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            CGlobals_lib_ss.setMyLocation(location, false, CabEMainDriver_act.this);
            try {
                CGlobals_lib_ss.setMyLocation(location, false, CabEMainDriver_act.this);
                if (location != null) {
                    float results[] = new float[1];
                    if (moToAddress.getLatitude() != 0.0 && moToAddress.getLongitude() != 0.0
                            && location.getLatitude() != 0.0 && location.getLongitude() != 0.0) {
                        Location.distanceBetween(moToAddress.getLatitude(),
                                moToAddress.getLongitude(), location.getLatitude(),
                                location.getLongitude(), results);
                    }
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG, "LocationResult", e, CabEMainDriver_act.this);
            }
        }
    };

    private void stupMap() {
        try {
            LatLng latLong;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            Location location = CGlobals_lib_ss.getInstance().getMyLocation(CabEMainDriver_act.this);
            if (location != null) {
                latLong = new LatLng(location
                        .getLatitude(), location
                        .getLongitude());
            } else {
                latLong = new LatLng(12.9667, 77.5667);
            }
           /*
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLong).zoom(15f).tilt(20).build();
            */
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLong).zoom(DEFAULT_ZOOM).bearing(90).tilt(30).build();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            if (location != null) {
                mMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));
            }
            mMap.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void onClickFrom(View v) {
    }

    public void onClickTo(View v) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CGlobals_lib_ss.REQUEST_LOCATION_LIB) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
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


    @Override
    protected void callDriver(JSONArray jsonArray) {
        try {
            mMap.setPadding(0, 180, 0, 300);
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                    mZoomFitBounds.build(), 15));
            progressCancel();
            oDirectionPoints = new JSONObject();
            oDirectionPoints.put("path", jsonArray);
            setBusyCreateTripDriver = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this)
                    .getInt(Constants_CED.PERF_SET_BUSY_CREATE_TRIP_DRIVER, 0);
            if (setBusyCreateTripDriver == 2) {
                if (oDirectionPoints.toString() != null) {
                    int tripid = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this)
                            .getInt(Constants_CED.PREF_TRIP_ID_INT, -1);
                    int appuserid = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this)
                            .getInt(Constants_lib_ss.PREF_APPUSERID, -1);
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault());
                    Calendar cal = Calendar.getInstance();
                    String sDateTime = df.format(cal.getTime());
                    db = new DatabaseHandler_CabE(CabEMainDriver_act.this);
                    db.addCreateTripPath(appuserid, tripid, CGlobals_lib_ss.msGmail, sDateTime,
                            CGlobals_lib_ss.mIMEI, oDirectionPoints.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String getGeoCountryCode() {
        return MyApplication_CED.getInstance().getPersistentPreference()
                .getString(Constants_CED.PREF_CURRENT_COUNTRY, "");
    }

    @Override
    protected void gotGoogleMapLocation(Location location) {

    }

    private void canceldialog() {
        setBusyCreateTripDriver = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this)
                .getInt(Constants_CED.PERF_SET_BUSY_CREATE_TRIP_DRIVER, 0);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(CabEMainDriver_act.this);
        alertDialog.setTitle("Are you sure do you cancel your trip?");
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEMainDriver_act.this);
                if (!isInternetCheck) {
                    getCancelReasons();
                } else {
                    snackbar = Snackbar.make(coordinatorLayout, getString(R.string.retry_internet), Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        if (!CabEMainDriver_act.this.isFinishing()) {
            alertDialog.show();
        }
    }

    private void getCancelReasons() {
        progressMessage("please wait...");
        final String url = Constants_CED.GET_CANCEL_REASONS_URL;
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
                snackbar = Snackbar.make(coordinatorLayout, getString(R.string.retry_internet), Snackbar.LENGTH_LONG);
                snackbar.show();
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
                                error, CabEMainDriver_act.this);
                    }
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("canceluser", "D");
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        url, CabEMainDriver_act.this);
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
                    SSLog_SS.e(TAG, "getPassengers map - ", e, CabEMainDriver_act.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, CabEMainDriver_act.this);
    }

    private void driverReasonForCancelTrip(String response) {
        progressCancel();
        if (TextUtils.isEmpty(response) || response.equals("-1")) {
            return;
        }
        try {
            final Dialog dialog = new Dialog(CabEMainDriver_act.this, R.style.AppCompatAlertDialogStyle);
            dialog.setContentView(R.layout.driver_cancel_trip_reason);
            dialog.setCancelable(false);
            final ListView lvReasonList;
            lvReasonList = (ListView) dialog.findViewById(R.id.lvReasonList);
            arrayCancelReasonList = new ArrayList<>();
            JSONArray aJson = new JSONArray(response);
            for (int i = 0; i < aJson.length(); i++) {
                cTripcedReason = new CTrip_CED(aJson.getJSONObject(i).toString(), CabEMainDriver_act.this);
                arrayCancelReasonList.add(cTripcedReason);
            }
            lvReasonList.setAdapter(new Cancel_Reason_Adapter(CabEMainDriver_act.this, arrayCancelReasonList));
            lvReasonList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String sCancelReasonCode = arrayCancelReasonList.get(position).getCancel_Reason_Code();
                    if (TextUtils.isEmpty(sCancelReasonCode)) {
                        Toast.makeText(CabEMainDriver_act.this, "No Reason", Toast.LENGTH_SHORT).show();
                    } else {
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this)
                                .putString(Constants_CED.PREF_CANCEL_REASON_CODE, sCancelReasonCode);
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this).
                                putBoolean(Constants_CED.PREF_ISIN_TRIP_MAIN, false);
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this).commit();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                                Locale.getDefault());
                        Calendar cal = Calendar.getInstance();
                        String sDateTime = df.format(cal.getTime());
                        Location location = CGlobals_lib_ss.getInstance().getMyLocation(CabEMainDriver_act.this);
                        int appuserid = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this)
                                .getInt(Constants_lib_ss.PREF_APPUSERID, -1);
                        int tripid = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this)
                                .getInt(Constants_CED.PREF_TRIP_ID_INT, -1);
                        String sUserType = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this).
                                getString(Constants_CED.CAB_TRIP_USER_TYPE_DRIVER, "");
                        tripcancel = true;
                        db = new DatabaseHandler_CabE(CabEMainDriver_act.this);
                        db.addTripAction(location, tripid, appuserid, sUserType, Constants_CED.TRIP_ACTION_ABORT,
                                CGlobals_lib_ss.msGmail, sDateTime);
                        driverCancelCabTrip(sCancelReasonCode);

                    }
                    dialog.dismiss();
                }
            });
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void driverCancelCabTrip(final String sCancelReasonCode) {
        progressMessage("please wait...");
        setBusyCreateTripDriver = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this)
                .getInt(Constants_CED.PERF_SET_BUSY_CREATE_TRIP_DRIVER, 0);
        if (setBusyCreateTripDriver == 1) {
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this)
                    .putInt(Constants_CED.PREF_SET_SWITCH_FLAG, 1);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this).commit();
        } else if (setBusyCreateTripDriver == 2) {
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this)
                    .putInt(Constants_CED.PREF_SET_SWITCH_FLAG, 2);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this).commit();
        }
        final int iTripId = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this)
                .getInt(Constants_CED.PREF_TRIP_ID_INT, -1);
        final String sUserType = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this).
                getString(Constants_CED.CAB_TRIP_USER_TYPE_DRIVER, "");
        db = new DatabaseHandler_CabE(CabEMainDriver_act.this);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        final String sDateTime = df.format(cal.getTime());
        final int appuserid = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this)
                .getInt(Constants_lib_ss.PREF_APPUSERID, -1);
        isVolleyCallFailedCancelTrip = false;
        if (sUserType.equals("K") || sUserType.equals("W")) {
            String sDriverPhon = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this)
                    .getString("DRIVER_PH_NUMBER_SAVE", "");
            mSms.sendSMS(sDriverPhon, "Sorry, this trip was cancelled by the driver. Please try again");
        }
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this)
                .putInt(Constants_CED.PERF_SET_BUSY_CREATE_TRIP_DRIVER, 0);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this)
                .putBoolean(Constants_CED.PREF_VOLLEY_CALL_FAILED_CANCEL_TRIP, isVolleyCallFailedCancelTrip);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this).commit();
        db.addCancelDriver(appuserid, CGlobals_lib_ss.msGmail, iTripId, sUserType, sDateTime, CGlobals_lib_ss.mIMEI, sCancelReasonCode);
        if (setBusyCreateTripDriver == 2) {
            db.addFlagSetDriver(String.valueOf(2), sDateTime, appuserid, CGlobals_lib_ss.msGmail,
                    CGlobals_lib_ss.mIMEI);
        } else {
            db.addFlagSetDriver(String.valueOf(1), sDateTime, appuserid, CGlobals_lib_ss.msGmail,
                    CGlobals_lib_ss.mIMEI);
        }
        mHandler.postDelayed(new Runnable() {
            public void run() {
                progressCancel();
                StopLocationService();
                Intent intent = new Intent(CabEMainDriver_act.this, DashBoard_Driver_act.class);
                startActivity(intent);
                finish();
            }
        }, 7000);
        /*final String url = Constants_CED.CANCEL_TRIP_DRIVER_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (setBusyCreateTripDriver == 2) {
                    CGlobals_CED.getInstance().sendFlag(2, sDateTime, CabEMainDriver_act.this);
                }else{
                    CGlobals_CED.getInstance().sendFlag(1, sDateTime, CabEMainDriver_act.this);
                }
                String sErr = CGlobals_lib_ss.getInstance().getVolleyError(error);
                try {
                    SSLog_SS.d(TAG,
                            "Failed to mVehicleNoVerify :-  "
                                    + error.getMessage());
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "mVehicleNoVerify - " + sErr,
                            error, CabEMainDriver_act.this);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tripid", String.valueOf(iTripId));
                params.put("triptype", sUserType);
                params.put("cancelreasoncode", sCancelReasonCode);
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        url, CabEMainDriver_act.this);
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, CabEMainDriver_act.this);*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverMainDriver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bmp != null && !bmp.isRecycled()) {
            bmp.recycle();
            bmp = null;
        }
        System.gc();
    }

    public BroadcastReceiver mMessageReceiverMainDriver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int errorvalue = intent.getIntExtra("errorvalue", 0);
            String response = intent.getStringExtra("response");
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this).
                    putString("RECEIVER_RESPONSE", response);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this).
                    putInt("RECEIVER_ERROR_VALUE", errorvalue);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this).commit();
            /*isVolleyCallFailedCancelTrip = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this)
                    .getBoolean(Constants_CED.PREF_VOLLEY_CALL_FAILED_CANCEL_TRIP, false);
            if (isVolleyCallFailedCancelTrip) {
                String sCanReaCode = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this)
                        .getString(Constants_CED.PREF_CANCEL_REASON_CODE, "");
                driverCancelCabTrip(sCanReaCode);
            }*/
            new getCabEMainDisplay().execute("");
        }
    };

    /*private void passengerCancelTrip(String response) {
        if (TextUtils.isEmpty(response)) {
            return;
        }
        if (response.equals("-1")) {
            return;
        }
        try {
            JSONObject jResponse = new JSONObject(response);
            trip_action = jResponse.isNull("trip_action") ? ""
                    : jResponse.getString("trip_action");
            int cancelled_passenger = jResponse.isNull("cancelled_passenger") ? 0
                    : jResponse.getInt("cancelled_passenger");
            if ((trip_action.equals("A") && !tripcancel) || cancelled_passenger == 1) {
                tripcancel = true;
                if (!CabEMainDriver_act.this.isFinishing()) {
                    String massage = "Customer has cancelled the trip.";
                    new AlertDialog.Builder(CabEMainDriver_act.this)
                            .setTitle("Cancel Trip")
                            .setMessage(massage)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(CabEMainDriver_act.this, DashBoard_Driver_act.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .show();
                } else {
                    tripcancel = true;
                    finish();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    @Override
    public void onBackPressed() {
       /* String massage = "Customer has cancelled the trip.";
        new AlertDialog.Builder(CabEMainDriver_act.this)
                .setTitle("Back")
                .setMessage(massage)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();*/
    }

    @Override
    protected void mapReady() {
        stupMap();
        CGlobals_lib_ss.getInstance().turnGPSOn1(CabEMainDriver_act.this, mGoogleApiClient);
        mLlTo.setVisibility(View.GONE);
        mLlFrom.setVisibility(View.GONE);
        mLlMarkerLayout.setVisibility(View.GONE);
        setBusyCreateTripDriver = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this)
                .getInt(Constants_CED.PERF_SET_BUSY_CREATE_TRIP_DRIVER, 0);
        if (setBusyCreateTripDriver == 1) {
            Location location = CGlobals_lib_ss.getInstance().getMyLocation(CabEMainDriver_act.this);
            if (location != null) {
                moFromAddress.setLatitude(location.getLatitude());
                moFromAddress.setLongitude(location.getLongitude());
                startIntentServiceCurrentAddress(location);
            }
        }
        setBusyCreateTripDriver = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this)
                .getInt(Constants_CED.PERF_SET_BUSY_CREATE_TRIP_DRIVER, 0);
        if (setBusyCreateTripDriver == 2) {
            goDirections();
        }
        bookCabPassenger();
    }

    private class getCabEMainDisplay extends AsyncTask<String, Integer, String> {
        boolean isInternetCheck = false, isLocationCheck = false;
        int cancelled_passenger, errorvalue, iPassenger, iTotalSet, iEmptySet;
        String response;

        @Override
        protected String doInBackground(String... params) {
            response = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this).
                    getString("RECEIVER_RESPONSE", "");
            errorvalue = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this).
                    getInt("RECEIVER_ERROR_VALUE", 0);
            try {
                if (errorvalue == 1) {
                    if (TextUtils.isEmpty(response)) {
                        return null;
                    }
                    if (response.equals("-1")) {
                        return null;
                    }
                    JSONArray aJson = new JSONArray(response);
                    for (int i = 0; i < aJson.length(); i++) {
                        JSONObject jResponse = (JSONObject) aJson.get(i);
                        trip_action = jResponse.isNull("trip_action") ? ""
                                : jResponse.getString("trip_action");
                        cancelled_passenger = jResponse.isNull("cancelled_passenger") ? 0
                                : jResponse.getInt("cancelled_passenger");
                    }
                    iPassenger = aJson.length();
                }
                try {
                    setBusyCreateTripDriver = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this)
                            .getInt(Constants_CED.PERF_SET_BUSY_CREATE_TRIP_DRIVER, 0);
                    if (setBusyCreateTripDriver == 2) {
                        String sVDetails = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this)
                                .getString(Constants_CED.PERF_VEHICLE_DETAILS, "");
                        if (!TextUtils.isEmpty(sVDetails)) {
                            Type type = new TypeToken<VehicleResult>() {
                            }.getType();
                            VehicleResult vehicleResult = new Gson().fromJson(sVDetails, type);
                            iTotalSet = Integer.parseInt(vehicleResult.getVehicleSeating());
                            if (errorvalue == 1 && iPassenger != 0) {
                                iEmptySet = (iTotalSet - iPassenger);
                            } else {
                                iEmptySet = iTotalSet;
                                iPassenger = 0;
                            }
                        }
                    }
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "getCabEMainDisplay: ", e, CabEMainDriver_act.this);
                }
                isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEMainDriver_act.this);

                Location location = CGlobals_lib_ss.getInstance().getMyLocation(getApplicationContext());
                if (location == null) {
                    location = mCurrentLocation;
                }
                if (location == null) {

                    return null;
                }
                long diff = System.currentTimeMillis() - location.getTime();
                if (diff > Constants_lib_ss.DRIVER_UPDATE_INTERVAL * 10) {
                    /*MyLocation myLocation = new MyLocation(
                            CGlobals_lib_ss.mVolleyRequestQueue, CabEMainDriver_act.this, mGoogleApiClient);
                    myLocation.getLocation(CabEMainDriver_act.this, onLocationResult);*/
                    isLocationCheck = false;
                } else {
                    isLocationCheck = true;
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG, "getCabEMainDisplay: ", e, CabEMainDriver_act.this);
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
                SSLog_SS.e(TAG, "getCabEMainDisplay: ", e, CabEMainDriver_act.this);
            }
            try {
                if (isInternetCheck) {
                    return;
                }
                if (errorvalue == 1) {
                    setBusyCreateTripDriver = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this)
                            .getInt(Constants_CED.PERF_SET_BUSY_CREATE_TRIP_DRIVER, 0);
                    if (setBusyCreateTripDriver != 2) {
                        if (!TextUtils.isEmpty(trip_action)) {
                            if ((trip_action.equals("A") && !tripcancel) || cancelled_passenger == 1) {
                                tripcancel = true;
                                if (!CabEMainDriver_act.this.isFinishing()) {
                                    String massage = "Customer has cancelled the trip.";
                                    new AlertDialog.Builder(CabEMainDriver_act.this)
                                            .setTitle("Cancel Trip")
                                            .setMessage(massage)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    db = new DatabaseHandler_CabE(CabEMainDriver_act.this);
                                                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                                                            Locale.getDefault());
                                                    Calendar cal = Calendar.getInstance();
                                                    final String sDateTime = df.format(cal.getTime());
                                                    final int appuserid = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this)
                                                            .getInt(Constants_lib_ss.PREF_APPUSERID, -1);
                                                    if (setBusyCreateTripDriver == 2) {
                                                        db.addFlagSetDriver(String.valueOf(2), sDateTime, appuserid, CGlobals_lib_ss.msGmail,
                                                                CGlobals_lib_ss.mIMEI);
                                                    } else {
                                                        db.addFlagSetDriver(String.valueOf(1), sDateTime, appuserid, CGlobals_lib_ss.msGmail,
                                                                CGlobals_lib_ss.mIMEI);
                                                    }
                                                    StopLocationService();
                                                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this)
                                                            .putBoolean(Constants_CED.PREF_IS_CHECK_BOOK, false);
                                                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this).
                                                            putBoolean(Constants_CED.PREF_ISIN_TRIP_MAIN, false);
                                                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEMainDriver_act.this).commit();
                                                    Intent intent = new Intent(CabEMainDriver_act.this, DashBoard_Driver_act.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            })
                                            .show();
                                } else {
                                    tripcancel = true;
                                    finish();
                                }
                            }
                        }
                    }
                }
                setBusyCreateTripDriver = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this)
                        .getInt(Constants_CED.PERF_SET_BUSY_CREATE_TRIP_DRIVER, 0);
                if (setBusyCreateTripDriver == 2) {
                    TextView tvTotalDistance = (TextView) findViewById(R.id.tvTotalDistance);
                    if (tvTotalDistance != null) {
                        tvTotalDistance.setVisibility(View.VISIBLE);
                        tvTotalDistance.setText("Total Seat: " + String.valueOf(iTotalSet)
                                + " Booked: " + String.valueOf(iPassenger)
                                + "\nEmpty: " + String.valueOf(iEmptySet));
                    }
                    btnCallPassenger = (Button) findViewById(R.id.btnCallPassenger);
                    if (btnCallPassenger != null) {
                        btnCallPassenger.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG, "getCabEMainDisplay: ", e, CabEMainDriver_act.this);
            }
            super.onPostExecute(result);
        }
    }

    public void onClickZoomRequestDriver(View v) {
        Location location = CGlobals_lib_ss.getInstance().getMyLocation(CabEMainDriver_act.this);
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
        if (mNearestPassengerLat != Constants_lib_ss.INVALIDLAT && mNearestPassengerLng != Constants_lib_ss.INVALIDLNG
                && mNearestPassengerLat != 0.0 && mNearestPassengerLng != 0.0) {
            bounds.include(new LatLng(mNearestPassengerLat, mNearestPassengerLng));
        }
        if (((location.getLatitude() != 0.0 && location.getLongitude() != 0.0) ||
                (mCurrentLocation.getLatitude() != 0.0 && mCurrentLocation.getLongitude() != 0.0))
                && (mNearestPassengerLat != 0.0 && mNearestPassengerLng != 0.0)) {
            LatLngBounds bounds1 = bounds.build();
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels - 180;
            int padding = (int) (width * 0.12);
            mMap.setPadding(0, 0, 0, 300);
            mMap.animateCamera((CameraUpdateFactory.newLatLngBounds(bounds1, width, height, padding)));
        }
    }

    public void onClickZoomDriverVehicle(View v) {
        if (mNearestPassengerLat != 0.0 && mNearestPassengerLng != 0.0) {
            CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(mNearestPassengerLat, mNearestPassengerLng));
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(DEFAULT_ZOOM);
            mMap.moveCamera(center);
            mMap.animateCamera(zoom);
        }
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

    /*private void insert_booked_driver_busy(final int iTripId) {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_CED.INSERT_BOOKED_DRIVER_BUSY_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Intent intent = new Intent(CabEMainDriver_act.this, CabEDriverForHire_act.class);
                        intent.putExtra("tripid", iTripId);
                        startActivity(intent);
                        StopLocationService();
                        finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String sErr = CGlobals_lib_ss.getInstance().getVolleyError(error);
                try {
                    SSLog_SS.d(TAG,
                            "Failed to mVehicleNoVerify :-  "
                                    + error.getMessage());
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "mVehicleNoVerify - " + sErr,
                            error, CabEMainDriver_act.this);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tripid", String.valueOf(iTripId));
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        Constants_CED.INSERT_BOOKED_DRIVER_BUSY_URL, CabEMainDriver_act.this);
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, CabEMainDriver_act.this);
    }*/
    Fixed_Address cTripCabEFrom, cTripCabETo;

    protected boolean goDirections() {
        String sFromAdd = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this)
                .getString(Constants_CED.PREF_DRIVER_FIXED_ADDRESS_FROM, "");
        String sToAdd = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this)
                .getString(Constants_CED.PREF_DRIVER_FIXED_ADDRESS_TO, "");
        Type type = new TypeToken<Fixed_Address>() {
        }.getType();
        if (moFromAddress == null) {
            moFromAddress = new CAddress();
        }
        if (moToAddress == null) {
            moToAddress = new CAddress();
        }
        cTripCabEFrom = new Gson().fromJson(sFromAdd, type);
        cTripCabETo = new Gson().fromJson(sToAdd, type);
        moFromAddress.setAddress(cTripCabEFrom.getFormatted_Address());
        moFromAddress.setLatitude(cTripCabEFrom.getLatitude());
        moFromAddress.setLongitude(cTripCabEFrom.getLongitude());
        moToAddress.setAddress(cTripCabETo.getFormatted_Address());
        moToAddress.setLatitude(cTripCabETo.getLatitude());
        moToAddress.setLongitude(cTripCabETo.getLongitude());
        if (!moFromAddress.hasLatitude() || !moFromAddress.hasLongitude() || !moToAddress.hasLatitude() || !moToAddress.hasLongitude()) {
            Toast.makeText(CabEMainDriver_act.this,
                    "Please re-enter your start and destination address",
                    Toast.LENGTH_SHORT).show();
            mapReady();
            return false;
        }
        if ((moFromAddress.hasLatitude() && moFromAddress
                .hasLongitude())
                && ((moToAddress.hasLatitude() && moToAddress
                .hasLongitude()))) {
            clearMap();
            drawFromMarkerCab(moFromAddress);
            drawToMarkerCab(moToAddress);
            mBounds.include(
                    new LatLng(moFromAddress.getLatitude(), moFromAddress
                            .getLongitude())).include(
                    new LatLng(moToAddress.getLatitude(), moToAddress
                            .getLongitude()));
            /*mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                    mBounds.build(), Constants_lib_ss.MAP_PADDING));*/
            SSLog_SS.i(TAG, "Plotting directions");
            String url = CabEMainDriver_act.this.getDirectionsUrl(
                    new LatLng(moFromAddress.getLatitude(), moFromAddress
                            .getLongitude()),
                    new LatLng(moToAddress.getLatitude(), moToAddress
                            .getLongitude()));
            GoogleDirection downloadTask = new GoogleDirection();
            downloadTask.execute(url);
            return true;
        } else {
            Toast.makeText(CabEMainDriver_act.this,
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
            SSLog_SS.e(TAG, "drawFromMarkerCab", e, CabEMainDriver_act.this);
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
            SSLog_SS.e(TAG, "drawToMarkerCab", e, CabEMainDriver_act.this);
        }
    } // drawToMarker

    /*protected void createTripPath(final String jsonPath) {
        final int tripid = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this)
                .getInt(Constants_CED.PREF_TRIP_ID_INT, -1);
        final String url = Constants_CED.CREATE_TRIP_PATH_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "createTripPath - ");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                int appuserid = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this)
                        .getInt(Constants_lib_ss.PREF_APPUSERID, -1);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                String sDateTime = df.format(cal.getTime());
                db = new DatabaseHandler_CabE(CabEMainDriver_act.this);
                db.addCreateTripPath(appuserid, tripid, CGlobals_lib_ss.msGmail, sDateTime, CGlobals_lib_ss.mIMEI, jsonPath);
                try {
                    SSLog_SS.e(TAG, "createTrip - ", error, CabEMainDriver_act.this);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "createTrip - ", e, CabEMainDriver_act.this);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tripid", String.valueOf(tripid));
                params.put("path", jsonPath);
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params, url, CabEMainDriver_act.this);
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, true, CabEMainDriver_act.this);
    }*/ // createTripPath

    ArrayList<CTrip_CED> mPassengerTripArray;
    CTrip_CED mCTPassengerTrip;

    public void onClickPassengerList(View v) {
        try {
            String response = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this).
                    getString("RECEIVER_RESPONSE", "");
            int errorvalue = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEMainDriver_act.this).
                    getInt("RECEIVER_ERROR_VALUE", 0);
            if (errorvalue == 1) {
                if (TextUtils.isEmpty(response)) {
                    return;
                }
                if (response.equals("-1")) {
                    return;
                }
                final Dialog dialog = new Dialog(CabEMainDriver_act.this);
                dialog.setContentView(R.layout.trip_passenger_info);
                dialog.setTitle("Passenger");
                final ListView mlvPassengerList;
                mlvPassengerList = (ListView) dialog.findViewById(R.id.lvPassengerListinfo);
                mPassengerTripArray = new ArrayList<CTrip_CED>();
                JSONArray majActivePassenger = new JSONArray(response);
                for (int i = 0; i < majActivePassenger.length(); i++) {
                    mCTPassengerTrip = new CTrip_CED(majActivePassenger.getJSONObject(i)
                            .toString(), getApplicationContext());
                    mPassengerTripArray.add(mCTPassengerTrip);
                }
                mlvPassengerList.setAdapter(new PassengerAdapter(CabEMainDriver_act.this, mPassengerTripArray));
                dialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
