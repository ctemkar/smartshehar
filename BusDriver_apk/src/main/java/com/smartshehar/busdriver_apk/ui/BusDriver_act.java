package com.smartshehar.busdriver_apk.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
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
import com.google.gson.Gson;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.Constants_lib;
import com.jumpinjumpout.apk.lib.SSLog;
import com.smartshehar.busdriver_apk.BusDriver_Adapter;
import com.smartshehar.busdriver_apk.BusDriver_Adapter2;
import com.smartshehar.busdriver_apk.BusDriver_Result;
import com.smartshehar.busdriver_apk.CGlobal_bd;
import com.smartshehar.busdriver_apk.Constants_bd;
import com.smartshehar.busdriver_apk.DBHelper;
import com.smartshehar.busdriver_apk.MyApplication;
import com.smartshehar.busdriver_apk.R;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lib.app.util.Connectivity;

/**
 * Created by user pc on 24-07-2015.
 */
public class BusDriver_act extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static String TAG = "BusDriver_act";
    private EditText etVehicleNo, tvBusRoute;
    private ListView lvBusRoute;
    private Button btnStart;
    private Spinner spBusStart, spBusDestination;
    DBHelper dbhelper;
    ArrayList<String> resultList;
    ArrayList<BusDriver_Result> listStart, listDestination/*, listStartAll, listDestinationAll*/;
    private ArrayList<String> array_sort = new ArrayList<String>();
    int textlength = 0;
    boolean userSelect = false;
    boolean userSelect1 = false;
    boolean userSelect2 = false;
    GoogleApiClient mGoogleApiClient = null;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    double cLat, cLon, dStartLat, dStartLng;
    String sBusStart, sBusDestination, msStartyStopSerial, msDrstiStopSerial;
    private int iTripId;
    Location msCurrentLocation;
    double dLat, dLon, dSLat, dSLon;
    private ProgressDialog mProgressDialog;
    Connectivity mConnectivity;
    private RadioButton rbUp, rbDown;
    private LinearLayout llRB;
    private TextView tvUp, tvDown;
    String direction="";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.busdriver_act);
        mConnectivity = new Connectivity();
        mGoogleApiClient = new GoogleApiClient.Builder(BusDriver_act.this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(BusDriver_act.this)
                .addOnConnectionFailedListener(BusDriver_act.this)
                .build();
        mGoogleApiClient.connect();
        init();
        dbhelper = new DBHelper(this);
        try {
            dbhelper.createDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultList = dbhelper.getRouteNo();
        ArrayAdapter<String> codeLearnArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, resultList);
        lvBusRoute.setAdapter(codeLearnArrayAdapter);
        lvBusRoute.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedFromList = (String) (lvBusRoute.getItemAtPosition(position));
                tvBusRoute.setText(selectedFromList);
                lvBusRoute.setVisibility(View.GONE);
                llRB.setVisibility(View.VISIBLE);
                InputMethodManager keyboard = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.hideSoftInputFromWindow(tvBusRoute.getWindowToken(), 0);
            }
        });

        tvBusRoute.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s,
                                          int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s,
                                      int start, int before, int count) {
                llRB.setVisibility(View.GONE);
                lvBusRoute.setVisibility(View.VISIBLE);
                textlength = tvBusRoute.getText().length();
                array_sort.clear();
                for (int i = 0; i < resultList.size(); i++) {
                    if (textlength <= resultList.get(i).length()) {
                        if (resultList.get(i).toLowerCase().contains(
                                tvBusRoute.getText().toString().toLowerCase().trim())) {
                            array_sort.add(resultList.get(i));
                        }
                    }
                }
                lvBusRoute.setAdapter(new ArrayAdapter<String>
                        (BusDriver_act.this,
                                android.R.layout.simple_list_item_1, array_sort));
                startBusName();
                busDestinationName();
            }
        });

        spBusStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                userSelect1 = true;
                return false;
            }
        });
        spBusStart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (userSelect1) {
                    String sRouteValue = tvBusRoute.getText().toString();
                    sBusStart = listStart.get(position).getStopname();
                    msStartyStopSerial = listStart.get(position).getStopserial();
                    dSLat = listStart.get(position).getLat();
                    dSLon = listStart.get(position).getLon();
                    CGlobals_lib.getInstance().getSharedPreferencesEditor(getApplicationContext()).
                            putString(Constants_lib.TRIP_FROM_ADDRESS, sBusStart);
                    CGlobals_lib.getInstance().getSharedPreferencesEditor(getApplicationContext()).commit();
                    CGlobal_bd.getInstance().getSharedPreferencesEditor(BusDriver_act.this)
                            .putFloat(Constants_bd.PREF_BUS_START_LAT, (float) dSLat);
                    CGlobal_bd.getInstance().getSharedPreferencesEditor(BusDriver_act.this)
                            .putFloat(Constants_bd.PREF_BUS_START_LON, (float) dSLon);
                    CGlobal_bd.getInstance().getSharedPreferencesEditor(BusDriver_act.this).commit();
                    userSelect1 = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spBusDestination.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                userSelect2 = true;
                return false;
            }
        });
        spBusDestination.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (userSelect2) {
                    String sRouteValue = tvBusRoute.getText().toString();
                    sBusDestination = listDestination.get(position).getStopname();
                    msDrstiStopSerial = listDestination.get(position).getStopserial();
                    dLat = listDestination.get(position).getLat();
                    dLon = listDestination.get(position).getLon();
                    CGlobals_lib.getInstance().getSharedPreferencesEditor(getApplicationContext()).
                            putString(Constants_lib.TRIP_TO_ADDRESS, sBusDestination);
                    CGlobals_lib.getInstance().getSharedPreferencesEditor(getApplicationContext()).commit();
                    CGlobal_bd.getInstance().getSharedPreferencesEditor(BusDriver_act.this)
                            .putFloat(Constants_bd.PREF_BUS_DESTINATION_LAT, (float) dLat);
                    CGlobal_bd.getInstance().getSharedPreferencesEditor(BusDriver_act.this)
                            .putFloat(Constants_bd.PREF_BUS_DESTINATION_LON, (float) dLon);
                    CGlobal_bd.getInstance().getSharedPreferencesEditor(BusDriver_act.this).commit();
                    userSelect2 = false;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etVehicleNo.getText().toString()) && TextUtils.isEmpty(tvBusRoute.getText().toString())) {
                    CGlobal_bd.getInstance().dialogMessageCustom(BusDriver_act.this, "Please enter all fields");
                } else {
                    CGlobal_bd.getInstance().getSharedPreferencesEditor(BusDriver_act.this)
                            .putString(Constants_bd.PREF_BUS_LABEL, etVehicleNo.getText().toString());
                    CGlobal_bd.getInstance().getSharedPreferencesEditor(BusDriver_act.this).commit();
                    if (!mConnectivity.connectionError(BusDriver_act.this, getString(R.string.app_label))) {
                        sendUserAccess(Constants_bd.TRIP_CREATE);
                    }
                }
            }
        });

        rbUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                direction = "U";
                setStartBus(direction);
                setBusDestination(direction);
                if (rbDown.isChecked()) {
                    rbDown.setChecked(false);
                }
                spBusStart.setVisibility(View.VISIBLE);
                spBusDestination.setVisibility(View.VISIBLE);
                btnStart.setVisibility(View.VISIBLE);
            }
        });

        rbDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                direction = "D";
                setStartBus(direction);
                setBusDestination(direction);
                if (rbUp.isChecked()) {
                    rbUp.setChecked(false);
                }
                spBusStart.setVisibility(View.VISIBLE);
                spBusDestination.setVisibility(View.VISIBLE);
                btnStart.setVisibility(View.VISIBLE);
            }
        });

        mProgressDialog = new ProgressDialog(BusDriver_act.this);
        mProgressDialog.setMessage("Please wait");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

    }

    private void startBusName() {
        String sRouteValue = tvBusRoute.getText().toString();
        if (TextUtils.isEmpty(sRouteValue)) {
            return;
        } else {
            try {
                dbhelper.createDataBase();
                String startname = dbhelper.startBusName(sRouteValue);
                tvUp.setText("To " + startname);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void busDestinationName() {
        String sRouteValue = tvBusRoute.getText().toString();
        if (TextUtils.isEmpty(sRouteValue)) {
            return;
        } else {
            try {
                dbhelper.createDataBase();
                String destname = dbhelper.destinationBusName(sRouteValue);
                tvDown.setText("To " + destname);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setStartBus(String sderection) {
        String sRouteValue = tvBusRoute.getText().toString();
        if (TextUtils.isEmpty(sRouteValue)) {
            return;
        } else {
            try {
                dbhelper.createDataBase();
            } catch (IOException e) {
                e.printStackTrace();
            }
            listStart = dbhelper.getStartBus(sRouteValue, sderection);
            spBusStart.setAdapter(new BusDriver_Adapter(BusDriver_act.this, listStart));
            sBusStart = listStart.get(0).getStopname();
            msStartyStopSerial = listStart.get(0).getStopserial();
            dSLat = listStart.get(0).getLat();
            dSLon = listStart.get(0).getLon();
            CGlobals_lib.getInstance().getSharedPreferencesEditor(getApplicationContext()).
                    putString(Constants_lib.TRIP_FROM_ADDRESS, sBusStart);
            CGlobals_lib.getInstance().getSharedPreferencesEditor(getApplicationContext()).commit();
            CGlobal_bd.getInstance().getSharedPreferencesEditor(BusDriver_act.this)
                    .putFloat(Constants_bd.PREF_BUS_START_LAT, (float) dSLat);
            CGlobal_bd.getInstance().getSharedPreferencesEditor(BusDriver_act.this)
                    .putFloat(Constants_bd.PREF_BUS_START_LON, (float) dSLon);
            CGlobal_bd.getInstance().getSharedPreferencesEditor(BusDriver_act.this).commit();
            Gson gson = new Gson();
            String json = gson.toJson(listStart);
            CGlobal_bd.getInstance().getSharedPreferencesEditor(BusDriver_act.this)
                    .putString(Constants_bd.PREFS_BUS_START_ARRAY, json);
            CGlobal_bd.getInstance().getSharedPreferencesEditor(BusDriver_act.this).commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (CGlobals_lib.getInstance().isInTrip(BusDriver_act.this)) {
            Intent intent = new Intent(BusDriver_act.this, BusDriverMap_act.class);
            startActivity(intent);
        }
        CGlobals_lib.getInstance().turnGPSOn1(BusDriver_act.this, mGoogleApiClient);
        msCurrentLocation = CGlobal_bd.getInstance().getMyLocation(BusDriver_act.this);
        if (msCurrentLocation != null) {
            dStartLat = msCurrentLocation.getLatitude();
            dStartLng = msCurrentLocation.getLongitude();
        } else {
            msCurrentLocation = CGlobal_bd.getInstance().getBestLocation(BusDriver_act.this);
        }
    }

    private void setBusDestination(String sderection) {
        String sRouteValue = tvBusRoute.getText().toString();
        if (TextUtils.isEmpty(sRouteValue)) {
            return;
        } else {
            try {
                dbhelper.createDataBase();
            } catch (IOException e) {
                e.printStackTrace();
            }
            listDestination = dbhelper.getDestinationBus(sRouteValue, sderection);
            spBusDestination.setAdapter(new BusDriver_Adapter2(BusDriver_act.this, listDestination));
            sBusDestination = listDestination.get(0).getStopname();
            msDrstiStopSerial = listDestination.get(0).getStopserial();
            dLat = listDestination.get(0).getLat();
            dLon = listDestination.get(0).getLon();
            CGlobals_lib.getInstance().getSharedPreferencesEditor(getApplicationContext()).
                    putString(Constants_lib.TRIP_TO_ADDRESS, sBusDestination);
            CGlobals_lib.getInstance().getSharedPreferencesEditor(getApplicationContext()).commit();
            CGlobal_bd.getInstance().getSharedPreferencesEditor(BusDriver_act.this)
                    .putFloat(Constants_bd.PREF_BUS_DESTINATION_LAT, (float) dLat);
            CGlobal_bd.getInstance().getSharedPreferencesEditor(BusDriver_act.this)
                    .putFloat(Constants_bd.PREF_BUS_DESTINATION_LON, (float) dLon);
            CGlobal_bd.getInstance().getSharedPreferencesEditor(BusDriver_act.this).commit();
        }
    }

    private void init() {
        etVehicleNo = (EditText) findViewById(R.id.etVehicleNo);
        tvBusRoute = (EditText) findViewById(R.id.tvBusRoute);
        lvBusRoute = (ListView) findViewById(R.id.lvBusRoute);
        spBusStart = (Spinner) findViewById(R.id.spBusStart);
        spBusDestination = (Spinner) findViewById(R.id.spBusDestination);
        btnStart = (Button) findViewById(R.id.btnStart);
        rbUp = (RadioButton) findViewById(R.id.rbUp);
        rbDown = (RadioButton) findViewById(R.id.rbDown);
        llRB = (LinearLayout) findViewById(R.id.llRB);
        tvUp = (TextView) findViewById(R.id.tvUp);
        tvDown = (TextView) findViewById(R.id.tvDown);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            dStartLat = mLastLocation.getLatitude();
            dStartLng = mLastLocation.getLongitude();
            //etVehicleNo.setText("LAT: " + cLat + " LON: " + cLon);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void sendUserAccess(final String tripStatus) {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_bd.DA_USER_ACCESS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        userAccessSucess(response, tripStatus);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(BusDriver_act.this,
                        error.getMessage(), Toast.LENGTH_LONG)
                        .show();
                SSLog.e(TAG, "sendUserAccess :-   ", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("app", Constants_bd.APP_TYPE);
                params = CGlobals_lib.getInstance().getBasicMobileParams(params,
                        Constants_bd.DA_USER_ACCESS_URL, BusDriver_act.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();

                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                return CGlobal_bd.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, true);
    } // sendUserAccess

    private void userAccessSucess(String response, final String tripStatus) {
        String sAppUserId = "-1";
        int iAppUserId = -1;
        try {
            if (response.trim().equals("-1")) {
                Toast.makeText(BusDriver_act.this, "Your device not registered", Toast.LENGTH_LONG).show();
                customDialog("Your device not registered");
            }
            if (!response.trim().equals("-1")) {
                JSONObject jResponse = new JSONObject(response);
                sAppUserId = jResponse.isNull("appuser_id") ? "-1" : jResponse
                        .getString("appuser_id");
                if (!sAppUserId.trim().equals("-1")) {
                    iAppUserId = Integer.parseInt(sAppUserId);
                    CGlobal_bd.getInstance().getSharedPreferencesEditor(BusDriver_act.this).putInt(Constants_lib.PREF_APPUSERID, iAppUserId);
                    CGlobal_bd.getInstance().getSharedPreferencesEditor(BusDriver_act.this).commit();
                    callBusTrip(tripStatus);
                }
            }
        } catch (Exception e) {
            SSLog.e(TAG, "sendUserAccess Response: " + response, e);
            Toast.makeText(BusDriver_act.this,
                    "Bad data received, try after some time", Toast.LENGTH_LONG)
                    .show();
            customDialog("Bad data received, try after some time");
        }
    } // userAccessSuccess

    private void callBusTrip(final String tripStatus) {
        showpDialog();
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        final Calendar cal = Calendar.getInstance();
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_bd.INSERT_BUSTRIP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());
                        getBusTripResult(response);
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidepDialog();
                try {
                    Log.e(TAG, error.toString());
                } catch (Exception e) {
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                if (!TextUtils.isEmpty(etVehicleNo.getText().toString())) {
                    params.put("vehicleno", etVehicleNo.getText().toString());
                }
                if (!TextUtils.isEmpty(tvBusRoute.getText().toString())) {
                    params.put("buslabel", tvBusRoute.getText().toString());
                }
                if (!TextUtils.isEmpty(sBusStart)) {
                    params.put("fromstop", sBusStart);
                    params.put("fromstopserial", msStartyStopSerial);
                }
                if (!TextUtils.isEmpty(tvBusRoute.getText().toString())) {
                    params.put("tostop", sBusDestination);
                    params.put("tostopserial", msDrstiStopSerial);
                }
                params.put("direction", direction);
                params.put("tripstatus", tripStatus);
                params.put("cdt", df.format(cal.getTime()));
                params = CGlobals_lib.getInstance().getBasicMobileParamsShort(params,
                        Constants_bd.INSERT_BUSTRIP_URL, BusDriver_act.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }

                String url1 = Constants_bd.INSERT_BUSTRIP_URL;

                try {
                    String url = url1 + "?" + getParams.toString()
                            + "&verbose=Y";
                    System.out.println("url  " + url);

                } catch (Exception e) {
                }
                return CGlobal_bd.getInstance().checkParams(params);
            }
        };
        CGlobal_bd.getInstance().getRequestQueue(BusDriver_act.this).add(postRequest);
    }  //callBusTrip

    private void getBusTripResult(String response) {
        if (TextUtils.isEmpty(response.trim()) && response.trim().equals("-1")) {
            Toast.makeText(BusDriver_act.this, "Trip not create. Please try again.", Toast.LENGTH_LONG).show();
            hidepDialog();
            return;
        }
        JSONObject jResponse;
        try {
            hidepDialog();
            jResponse = new JSONObject(response.trim());
            iTripId = jResponse.isNull("trip_id") ? 0 : jResponse
                    .getInt("trip_id");
            CGlobal_bd.getInstance().getSharedPreferencesEditor(BusDriver_act.this)
                    .putInt(Constants_bd.PREF_BUS_DRIVER_TRIP_ID, iTripId);
            CGlobal_bd.getInstance().getSharedPreferencesEditor(BusDriver_act.this).commit();
            Intent intent = new Intent(BusDriver_act.this, BusDriverMap_act.class);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(BusDriver_act.this, "Trip not create. Please try again.", Toast.LENGTH_LONG).show();
            hidepDialog();
        }

    }

    private void showpDialog() {
        try {
            if (!isFinishing()) {
                mProgressDialog.setMessage("Please wait ...");
                mProgressDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hidepDialog() {
        try {
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.cancel();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void customDialog(String message) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(BusDriver_act.this);
        builder1.setMessage(message);
        builder1.setCancelable(true);
        builder1.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        builder1.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

}
