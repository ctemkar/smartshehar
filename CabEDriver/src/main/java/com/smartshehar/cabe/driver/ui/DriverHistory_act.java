package com.smartshehar.cabe.driver.ui;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.gson.Gson;
import com.smartshehar.cabe.driver.CTrip_CED;
import com.smartshehar.cabe.driver.Constants_CED;
import com.smartshehar.cabe.driver.Earning_History_Adapter;
import com.smartshehar.cabe.driver.LocationService;
import com.smartshehar.cabe.driver.MyApplication_CED;
import com.smartshehar.cabe.driver.R;
import com.smartshehar.cabe.driver.Weekly_History_Result;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;
import lib.app.util.SSLog_SS;

/**
 * Created by jijo_soumen on 26/04/2016.
 * Driver show trip, trip cost, trip rating/comment, and driver profile history
 */
public class DriverHistory_act extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "DriverHistory_act: ";
    private CoordinatorLayout coordinatorLayout;
    Snackbar snackbar;
    LinearLayout llHome, llEarning, llRating, llAccount, llAccountDriver, llDriverHome,
            llEarningDriver, llRatingDriver;
    ImageView ivHomeImage, ivEarningImage, ivRatingImage, ivAccountImage, ivShowImage;
    TextView tvLastBookTripId, tvLastBookTime, tvLastCabeType, tvLastTripCost, tvDayOnlineHoure, tvDayTotalTrip,
            tvDayTotalTripCost, full_name, age, gender, tvAvgRating, tvTotalTrip, tvRatedTrip,
            tvFiveStarRated, tvLastTripDistance, tvTripDistance,
            tvHomeText, tvEarningText, tvRatingText, tvAccountText;
    CTrip_CED cTrip_ced;
    ListView lvAllComment, lvWeeklyTripHistory;
    ArrayList<CTrip_CED> cTripArrayList;
    ArrayList<Weekly_History_Result> cTripArrayDalSamList;
    Button sign_out_button;
    GoogleApiClient google_api_client;
    ProgressDialog pDialog;
    SimpleDateFormat df, df1;
    Calendar cal, cal1;
    Connectivity mConnectivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buidNewGoogleApiClient();
        setContentView(R.layout.footer_driverhistory);
        mConnectivity = new Connectivity();
        init();
        boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(DriverHistory_act.this);
        if (!isInternetCheck) {
            getDriverhHistoryPhp();
        } else {
            snackbar = Snackbar.make(coordinatorLayout, getString(R.string.retry_internet), Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String sResponse = CGlobals_lib_ss.getInstance().getPersistentPreference(DriverHistory_act.this)
                .getString(Constants_CED.PREF_CHECK_BOOK_RESPONSE, "");
        boolean isCheckBook = CGlobals_lib_ss.getInstance().getPersistentPreference(DriverHistory_act.this)
                .getBoolean(Constants_CED.PREF_IS_CHECK_BOOK, false);
        if (isCheckBook) {
            checkIfBookedResponse(sResponse);
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverDashBoard,
                new IntentFilter(Constants_CED.LOCATION_SERVICE_FOR_HIRE));
        Intent sendableIntent = new Intent(Constants_CED.LOCATION_SERVICE_FOR_HIRE);
        LocalBroadcastManager.getInstance(this).
                sendBroadcast(sendableIntent);
        if (google_api_client.isConnected()) {
            google_api_client.connect();
        }
        CGlobals_lib_ss.getInstance().countError1 = 1;
        CGlobals_lib_ss.getInstance().countError2 = 1;
        CGlobals_lib_ss.getInstance().countError3 = 1;
        CGlobals_lib_ss.getInstance().countError4 = 1;
        CGlobals_lib_ss.getInstance().countError5 = 1;
        CGlobals_lib_ss.getInstance().countError6 = 1;
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverDashBoard);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if ((pDialog != null) && pDialog.isShowing())
            pDialog.dismiss();
    }

    public BroadcastReceiver mMessageReceiverDashBoard = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String response = intent.getStringExtra("response");
            int errorvalue = intent.getIntExtra("errorvalue", 0);
            if (errorvalue == 1) {
                checkIfBookedResponse(response);
            }
        }
    };

    int miForHire = 0, booking_driver_id, trip_id, cancelled_driver, cancelled_passenger;
    String sUser_Type, trip_action;

    private void checkIfBookedResponse(String response) {
        pDialog.setCancelable(true);
        pDialog.cancel();
        if (TextUtils.isEmpty(response) || response.equals("-1")) {
            return;
        }
        try {
            miForHire = CGlobals_lib_ss.getInstance().getPersistentPreference(DriverHistory_act.this).
                    getInt(Constants_CED.PREF_SET_SWITCH_FLAG, 2);
            if (miForHire == 1) {
                Gson gson = new Gson();
                String json = gson.toJson(response);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DriverHistory_act.this).
                        putString(Constants_CED.PREF_SAVE_RESPONSE_PASSENGER, json);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DriverHistory_act.this).commit();
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
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DriverHistory_act.this).
                        putInt(Constants_CED.CAB_TRIP_ID_DRIVER, trip_id);
                if (trip_id > 0) {
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DriverHistory_act.this)
                            .putInt(Constants_CED.PREF_TRIP_ID_INT, trip_id);
                }
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DriverHistory_act.this).
                        putString(Constants_CED.CAB_TRIP_USER_TYPE_DRIVER, sUser_Type);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DriverHistory_act.this).
                        putString(Constants_CED.PREF_CURRENT_TRIP, response);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DriverHistory_act.this).commit();
                if (!trip_action.equals(Constants_CED.TRIP_ACTION_END) && cancelled_driver != 1 && cancelled_passenger != 1) {
                    StopLocationService();
                    Intent intent = new Intent(DriverHistory_act.this, CabEMainDriver_act.class);
                    startActivity(intent);
                }
                finish();
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG, "checkIfBookedResponse: ", e, DriverHistory_act.this);
            snackbar = Snackbar.make(coordinatorLayout, "!server error", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    protected void StopLocationService() {
        if (isMyServiceRunning()) {
            stopService(new Intent(this, LocationService.class));
        }
        stopService(new Intent(this, LocationService.class));
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {

            if ("com.smartshehar.cabe.driver.LocationService".equals(service.service.getClassName())) {
                return false;
            }
        }
        return true;
    }

    private void init() {
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .coordinatorLayout);
        llHome = (LinearLayout) findViewById(R.id.llHome);
        llEarning = (LinearLayout) findViewById(R.id.llEarning);
        llRating = (LinearLayout) findViewById(R.id.llRating);
        llDriverHome = (LinearLayout) findViewById(R.id.llDriverHome);
        llEarningDriver = (LinearLayout) findViewById(R.id.llEarningDriver);
        llRatingDriver = (LinearLayout) findViewById(R.id.llRatingDriver);
        llAccountDriver = (LinearLayout) findViewById(R.id.llAccountDriver);
        llAccount = (LinearLayout) findViewById(R.id.llAccount);
        ivHomeImage = (ImageView) findViewById(R.id.ivHomeImage);
        ivEarningImage = (ImageView) findViewById(R.id.ivEarningImage);
        ivRatingImage = (ImageView) findViewById(R.id.ivRatingImage);
        ivAccountImage = (ImageView) findViewById(R.id.ivAccountImage);
        sign_out_button = (Button) findViewById(R.id.sign_out_button);

        tvHomeText = (TextView) findViewById(R.id.tvHomeText);
        tvEarningText = (TextView) findViewById(R.id.tvEarningText);
        tvRatingText = (TextView) findViewById(R.id.tvRatingText);
        tvAccountText = (TextView) findViewById(R.id.tvAccountText);

        llHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                home();
            }
        });
        llEarning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                earning();
            }
        });
        llRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rating();
            }
        });
        llAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                account();
            }
        });
        ivHomeImage.setBackgroundResource(R.drawable.home_gray);
        ivEarningImage.setBackgroundResource(R.drawable.earning_gray);
        ivRatingImage.setBackgroundResource(R.drawable.rating_gray);
        ivAccountImage.setBackgroundResource(R.drawable.account_gray);
    }

    private void home() {
        llDriverHome.setVisibility(View.VISIBLE);
        llEarningDriver.setVisibility(View.GONE);
        llRatingDriver.setVisibility(View.GONE);
        llAccountDriver.setVisibility(View.GONE);
        llHome.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));
        llEarning.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.MidnightBlue));
        llRating.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.MidnightBlue));
        llAccount.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.MidnightBlue));
        tvHomeText.setTextColor(Color.BLACK);
        tvEarningText.setTextColor(Color.WHITE);
        tvRatingText.setTextColor(Color.WHITE);
        tvAccountText.setTextColor(Color.WHITE);
        resetButtonColors("H");
        historyHome();
    }

    private void earning() {
        llEarningDriver.setVisibility(View.VISIBLE);
        llDriverHome.setVisibility(View.GONE);
        llRatingDriver.setVisibility(View.GONE);
        llAccountDriver.setVisibility(View.GONE);
        llHome.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.MidnightBlue));
        llEarning.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));
        llRating.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.MidnightBlue));
        llAccount.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.MidnightBlue));
        tvHomeText.setTextColor(Color.WHITE);
        tvEarningText.setTextColor(Color.BLACK);
        tvRatingText.setTextColor(Color.WHITE);
        tvAccountText.setTextColor(Color.WHITE);
        resetButtonColors("E");
        boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(DriverHistory_act.this);
        if (!isInternetCheck) {
            getWeeklyTripHistoryDriver();
        } else {
            snackbar = Snackbar.make(coordinatorLayout, getString(R.string.retry_internet), Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    private void rating() {
        llRatingDriver.setVisibility(View.VISIBLE);
        llDriverHome.setVisibility(View.GONE);
        llEarningDriver.setVisibility(View.GONE);
        llAccountDriver.setVisibility(View.GONE);
        llHome.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.MidnightBlue));
        llEarning.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.MidnightBlue));
        llRating.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));
        llAccount.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.MidnightBlue));
        tvHomeText.setTextColor(Color.WHITE);
        tvEarningText.setTextColor(Color.WHITE);
        tvRatingText.setTextColor(Color.BLACK);
        tvAccountText.setTextColor(Color.WHITE);
        resetButtonColors("R");
        if (CGlobals_lib_ss.getInstance().checkConnected(DriverHistory_act.this)) {
            getDriverhRatingHistoryPhp();
        } else {
            historyRating();
        }
    }

    private void account() {
        llAccountDriver.setVisibility(View.VISIBLE);
        llEarningDriver.setVisibility(View.GONE);
        llRatingDriver.setVisibility(View.GONE);
        llDriverHome.setVisibility(View.GONE);
        llHome.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.MidnightBlue));
        llEarning.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.MidnightBlue));
        llRating.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.MidnightBlue));
        llAccount.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));
        tvHomeText.setTextColor(Color.WHITE);
        tvEarningText.setTextColor(Color.WHITE);
        tvRatingText.setTextColor(Color.WHITE);
        tvAccountText.setTextColor(Color.BLACK);
        resetButtonColors("A");
        if (CGlobals_lib_ss.getInstance().checkConnected(DriverHistory_act.this)) {
            getDriverhAccountHistoryPhp();
        } else {
            historyAccount();
        }
    }

    public void resetButtonColors(String sTripType) {

        ivHomeImage.setBackgroundResource(R.drawable.home_gray);
        ivEarningImage.setBackgroundResource(R.drawable.earning_gray);
        ivRatingImage.setBackgroundResource(R.drawable.rating_gray);
        ivAccountImage.setBackgroundResource(R.drawable.account_gray);

        if (sTripType.equals("H")) {
            ivHomeImage.setBackgroundResource(R.drawable.home);
        }
        if (sTripType.equals("E")) {
            ivEarningImage.setBackgroundResource(R.drawable.earning);
        }
        if (sTripType.equals("R")) {
            ivRatingImage.setBackgroundResource(R.drawable.rating);
        }
        if (sTripType.equals("A")) {
            ivAccountImage.setBackgroundResource(R.drawable.account);
        }
    }

    private void getDriverhHistoryPhp() {
        pDialog = new ProgressDialog(DriverHistory_act.this);
        pDialog.setMessage("Please wait...");
        pDialog.setIndeterminate(true);
        pDialog.setCancelable(false);
        pDialog.show();
        final String url = Constants_CED.DRIVER_HISTORY_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                driverHistoryResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.setCancelable(true);
                pDialog.cancel();
                // error
                if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                    Log.e(TAG,"");
                } else {
                    SSLog_SS.e(TAG, "sendFlag :-   ",
                            error, DriverHistory_act.this);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params, url,
                        DriverHistory_act.this);
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
                    SSLog_SS.e(TAG, "getPassengers map - ", e, DriverHistory_act.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, DriverHistory_act.this);
    }

    @SuppressLint("SetTextI18n")
    private void driverHistoryResponse(String response) {
        pDialog.setCancelable(true);
        pDialog.cancel();
        if (TextUtils.isEmpty(response.trim()) || response.trim().equals("-1") || response.trim().equals("0")) {
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DriverHistory_act.this)
                    .putString(Constants_CED.PREF_CABE_DRIVER_HISTORY_DAY, "");
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DriverHistory_act.this).commit();
            snackbar = Snackbar.make(coordinatorLayout, "No History", Snackbar.LENGTH_LONG);
            snackbar.show();

            llEarningDriver.setVisibility(View.GONE);
            llDriverHome.setVisibility(View.GONE);
            llRatingDriver.setVisibility(View.GONE);
            llAccountDriver.setVisibility(View.GONE);
            return;
        }
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DriverHistory_act.this)
                .putString(Constants_CED.PREF_CABE_DRIVER_HISTORY_DAY, response);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DriverHistory_act.this).commit();
        home();
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void historyHome() {
        String sForeHireTime = "", sShiftDriverTime = "";
        tvLastBookTripId = (TextView) findViewById(R.id.tvLastBookTripId);
        tvLastBookTime = (TextView) findViewById(R.id.tvLastBookTime);
        tvLastCabeType = (TextView) findViewById(R.id.tvLastCabeType);
        tvLastTripCost = (TextView) findViewById(R.id.tvLastTripCost);
        tvDayOnlineHoure = (TextView) findViewById(R.id.tvDayOnlineHoure);
        tvDayTotalTrip = (TextView) findViewById(R.id.tvDayTotalTrip);
        tvDayTotalTripCost = (TextView) findViewById(R.id.tvDayTotalTripCost);
        tvLastTripDistance = (TextView) findViewById(R.id.tvLastTripDistance);
        tvTripDistance = (TextView) findViewById(R.id.tvTripDistance);
        String reshome = CGlobals_lib_ss.getInstance().getPersistentPreference(DriverHistory_act.this)
                .getString(Constants_CED.PREF_CABE_DRIVER_HISTORY_DAY, "");
        if (TextUtils.isEmpty(reshome)) {
            snackbar = Snackbar.make(coordinatorLayout, "No History", Snackbar.LENGTH_LONG);
            snackbar.show();
            llDriverHome.setVisibility(View.GONE);
            return;
        }
        try {
            cTripArrayList = new ArrayList<>();
            JSONArray aJson = new JSONArray(reshome);
            for (int i = 0; i < aJson.length(); i++) {
                cTrip_ced = new CTrip_CED(aJson.getJSONObject(i).toString(), DriverHistory_act.this);
                cTripArrayList.add(cTrip_ced);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            double cost = 0, distance = 0;
            tvDayTotalTrip.setText(String.valueOf(cTripArrayList.size()) + " Trip");
            for (int i = 0; i < cTripArrayList.size(); i++) {
                tvLastBookTime.setText(cTripArrayList.get(0).getBooking_Time());
                tvLastCabeType.setText("");
                tvLastTripCost.setText(getResources().getString(R.string.rs) + cTripArrayList.get(0).getTrip_Cost());
                tvLastBookTripId.setText("id " + String.valueOf(cTripArrayList.get(0).getTripId()));
                double dist = Double.parseDouble(cTripArrayList.get(0).getTrip_Distance());
                tvLastTripDistance.setText("Distance: " + String.format("%.2f", dist) + " Km");
                if (!TextUtils.isEmpty(cTripArrayList.get(i).getTrip_Cost())) {
                    cost = cost + Double.parseDouble(cTripArrayList.get(i).getTrip_Cost());
                }
                if (!TextUtils.isEmpty(cTripArrayList.get(i).getTrip_Distance())) {
                    distance = distance + Double.parseDouble(cTripArrayList.get(i).getTrip_Distance());
                }
                if (!TextUtils.isEmpty(cTripArrayList.get(0).getTotal_For_Hire())) {
                    StringTokenizer tokens = new StringTokenizer(cTripArrayList.get(0).getTotal_For_Hire(), ":");
                    String hours = tokens.nextToken();
                    String mints = tokens.nextToken();
                    //String sec = tokens.nextToken();
                    sForeHireTime = "For Hire:\n" + hours + " hours " + mints + "mins "/* + sec + " s"*/;
                }
                if (!TextUtils.isEmpty(cTripArrayList.get(0).getTotal_Shift_Time())) {
                    StringTokenizer tokens = new StringTokenizer(cTripArrayList.get(0).getTotal_Shift_Time(), ":");
                    String hours = tokens.nextToken();
                    String mints = tokens.nextToken();
                    //String sec = tokens.nextToken();
                    sShiftDriverTime = "Shift Driver:\n" + hours + " hours " + mints + " mins " /*+ sec + " s"*/;
                }
                tvDayOnlineHoure.setText(sForeHireTime + "\n\n" + sShiftDriverTime);
            }
            tvTripDistance.setText("Distance:\n" + String.format("%.2f", distance) + " km");
            tvDayTotalTripCost.setText(getResources().getString(R.string.rs) + String.valueOf(cost));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void getDriverhAccountHistoryPhp() {
        pDialog = new ProgressDialog(DriverHistory_act.this);
        pDialog.setMessage("Please wait...");
        pDialog.setIndeterminate(true);
        pDialog.setCancelable(false);
        pDialog.show();
        final String url = Constants_CED.GET_DRIVER_PROFILE_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                driverAccountHistoryResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.setCancelable(true);
                pDialog.cancel();
                // error
                if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                    Log.e(TAG, "");
                } else {
                    SSLog_SS.e(TAG, "sendFlag :-   ",
                            error, DriverHistory_act.this);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params, url,
                        DriverHistory_act.this);
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
                    SSLog_SS.e(TAG, "getPassengers map - ", e, DriverHistory_act.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, DriverHistory_act.this);
    }

    @SuppressLint("SetTextI18n")
    private void driverAccountHistoryResponse(String response) {
        pDialog.setCancelable(true);
        pDialog.cancel();
        if (TextUtils.isEmpty(response.trim()) || response.trim().equals("-1")) {
            /*CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DriverHistory_act.this)
                    .putString(Constants_CED.PREF_CABE_DRIVER_ACCOUNT_HISTORY_DAY, "");
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DriverHistory_act.this).commit();*/
            snackbar = Snackbar.make(coordinatorLayout, "No Account History", Snackbar.LENGTH_LONG);
            snackbar.show();
            llEarningDriver.setVisibility(View.GONE);
            llDriverHome.setVisibility(View.GONE);
            llRatingDriver.setVisibility(View.GONE);
            llAccountDriver.setVisibility(View.GONE);
            return;
        }
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DriverHistory_act.this)
                .putString(Constants_CED.PREF_CABE_DRIVER_ACCOUNT_HISTORY_DAY, response);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DriverHistory_act.this).commit();
        historyAccount();
    }

    @SuppressLint("SetTextI18n")
    private void historyAccount() {
        SimpleDateFormat curFormaterDB = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date birthDate;
        full_name = (TextView) findViewById(R.id.full_name);
        age = (TextView) findViewById(R.id.age);
        gender = (TextView) findViewById(R.id.gender);
        ivShowImage = (ImageView) findViewById(R.id.ivShowImage);
        String reshome = CGlobals_lib_ss.getInstance().getPersistentPreference(DriverHistory_act.this)
                .getString(Constants_CED.PREF_CABE_DRIVER_ACCOUNT_HISTORY_DAY, "");
        if (TextUtils.isEmpty(reshome)) {
            snackbar = Snackbar.make(coordinatorLayout, "No Account History", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
        try {
            cTrip_ced = new CTrip_CED(reshome, DriverHistory_act.this);
            full_name.setText(cTrip_ced.getDriver_Firstname() + " " + cTrip_ced.getDriver_Lastname());
            birthDate = curFormaterDB.parse(cTrip_ced.getDriver_DOB());
            age.setText(String.valueOf(calculateAge(birthDate)));
            gender.setText(cTrip_ced.getDriver_Gender());
            if (!TextUtils.isEmpty(cTrip_ced.getImage_Name()) &&
                    !TextUtils.isEmpty(cTrip_ced.getImage_Path())) {
                String url = Constants_CED.GET_DRIVER_PROFILE_IMAGE_URL + cTrip_ced.getImage_Path() +
                        cTrip_ced.getImage_Name();

                ImageRequest request = new ImageRequest(url,
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap bitmap) {
                                if (Build.VERSION.SDK_INT < 11) {
                                    Log.i(TAG, "Not support image");
                                } else {
                                    ivShowImage.setImageBitmap(bitmap);
                                    ivShowImage.setVisibility(View.VISIBLE);
                                }
                            }
                        }, 0, 0, ImageView.ScaleType.FIT_XY, Bitmap.Config.ARGB_8888,
                        new Response.ErrorListener() {
                            public void onErrorResponse(VolleyError error) {
                                ivShowImage.setImageResource(R.drawable.ic_driver);
                            }
                        });
                MyApplication_CED.getInstance().addToRequestQueue(request);
            } else {
                ivShowImage.setImageResource(R.drawable.ic_driver);
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG, "historyAccount: ", e, DriverHistory_act.this);
        }
    }

    private void getDriverhRatingHistoryPhp() {
        pDialog = new ProgressDialog(DriverHistory_act.this);
        pDialog.setMessage("Please wait...");
        pDialog.setIndeterminate(true);
        pDialog.setCancelable(false);
        pDialog.show();
        final String url = Constants_CED.GET_DRIVER_RATING_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                driverRatingHistoryResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.setCancelable(true);
                pDialog.cancel();
                // error
                if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                    Log.e(TAG, "");
                } else {
                    SSLog_SS.e(TAG, "sendFlag :-   ",
                            error, DriverHistory_act.this);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params, url,
                        DriverHistory_act.this);
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
                    SSLog_SS.e(TAG, "getPassengers map - ", e, DriverHistory_act.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, DriverHistory_act.this);
    }

    @SuppressLint("SetTextI18n")
    private void driverRatingHistoryResponse(String response) {
        pDialog.setCancelable(true);
        pDialog.cancel();
        if (TextUtils.isEmpty(response.trim()) || response.trim().equals("-1") || response.trim().equals("0")) {
            /*CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DriverHistory_act.this)
                    .putString(Constants_CED.PREF_CABE_DRIVER_RATING_HISTORY_DAY, "");
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DriverHistory_act.this).commit();*/
            snackbar = Snackbar.make(coordinatorLayout, "No Rating History", Snackbar.LENGTH_LONG);
            snackbar.show();
            llEarningDriver.setVisibility(View.GONE);
            llDriverHome.setVisibility(View.GONE);
            llRatingDriver.setVisibility(View.GONE);
            llAccountDriver.setVisibility(View.GONE);
            return;
        }
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DriverHistory_act.this)
                .putString(Constants_CED.PREF_CABE_DRIVER_RATING_HISTORY_DAY, response);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DriverHistory_act.this).commit();
        historyRating();
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void historyRating() {
        ArrayList<String> list = new ArrayList<>();
        tvAvgRating = (TextView) findViewById(R.id.tvAvgRating);
        tvTotalTrip = (TextView) findViewById(R.id.tvTotalTrip);
        tvRatedTrip = (TextView) findViewById(R.id.tvRatedTrip);
        tvFiveStarRated = (TextView) findViewById(R.id.tvFiveStarRated);
        lvAllComment = (ListView) findViewById(R.id.lvAllComment);
        String reshome = CGlobals_lib_ss.getInstance().getPersistentPreference(DriverHistory_act.this)
                .getString(Constants_CED.PREF_CABE_DRIVER_RATING_HISTORY_DAY, "");
        if (TextUtils.isEmpty(reshome)) {
            snackbar = Snackbar.make(coordinatorLayout, "No Rating History", Snackbar.LENGTH_LONG);
            snackbar.show();
            llRatingDriver.setVisibility(View.GONE);
            return;
        }
        try {
            cTripArrayList = new ArrayList<>();
            JSONArray aJson = new JSONArray(reshome);
            for (int i = 0; i < aJson.length(); i++) {
                cTrip_ced = new CTrip_CED(aJson.getJSONObject(i).toString(), DriverHistory_act.this);
                cTripArrayList.add(cTrip_ced);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            double rating = 0;
            int m = 0;
            for (int i = 0; i < cTripArrayList.size(); i++) {
                tvTotalTrip.setText(String.valueOf(cTripArrayList.size()));
                if (!TextUtils.isEmpty(cTripArrayList.get(i).getTrip_Rating())) {
                    rating = rating + Double.parseDouble(cTripArrayList.get(i).getTrip_Rating());
                    m = m + 1;
                }
                if (!TextUtils.isEmpty(cTripArrayList.get(i).getTrip_Comment())) {
                    list.add(cTripArrayList.get(i).getTrip_Comment());
                }
            }
            tvAvgRating.setText(String.format("%.2f", (rating / m)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, list);
            lvAllComment.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getWeeklyTripHistoryDriver() {
        pDialog = new ProgressDialog(DriverHistory_act.this);
        pDialog.setMessage("Please wait...");
        pDialog.setIndeterminate(true);
        pDialog.setCancelable(false);
        pDialog.show();
        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        cal = Calendar.getInstance();
        cal.setTime(cal.getTime());
        cal.add(Calendar.DATE, -7);
        Log.d(TAG, df.format(cal.getTime()));
        df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        cal1 = Calendar.getInstance();
        final String url = Constants_CED.WEEKLY_HISTORY_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pDialog.setCancelable(true);
                pDialog.cancel();
                weeklyTripHistoryDriverResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.setCancelable(true);
                pDialog.cancel();
                // error
                if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                    Log.e(TAG, "");
                } else {
                    SSLog_SS.e(TAG, "sendFlag :-   ",
                            error, DriverHistory_act.this);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("fromdatetime", df.format(cal.getTime()));
                params.put("todatetime", df1.format(cal1.getTime()));
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params, url,
                        DriverHistory_act.this);
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
                    SSLog_SS.e(TAG, "getPassengers map - ", e, DriverHistory_act.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, DriverHistory_act.this);
    }


    @SuppressLint("SetTextI18n")
    private void weeklyTripHistoryDriverResponse(String response) {
        String oCurrentDate = "";
        String newDateStr;
        Date dateObj;
        Double costForDay = 0.0;
        Double dTotalDistance = 0.0;
        int iTripCount = 0;
        if (TextUtils.isEmpty(response) || response.equals("-1") || response.equals("0")) {
            snackbar = Snackbar.make(coordinatorLayout, "No Weekly History", Snackbar.LENGTH_LONG);
            snackbar.show();
            llEarningDriver.setVisibility(View.GONE);
            return;
        }
        lvWeeklyTripHistory = (ListView) findViewById(R.id.lvWeeklyTripHistory);
        df1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            cTripArrayList = new ArrayList<>();
            cTripArrayDalSamList = new ArrayList<>();
            JSONArray aJson = new JSONArray(response);
            for (int i = 0; i < aJson.length(); i++) {
                cTrip_ced = new CTrip_CED(aJson.getJSONObject(i).toString(), DriverHistory_act.this);
                dateObj = df1.parse((TextUtils.isEmpty(cTrip_ced.getTripCreationTime()) ? cTrip_ced.getPlanned_Start_DateTime()
                        : cTrip_ced.getTripCreationTime()));
                newDateStr = df1.format(dateObj);
                if (oCurrentDate.equals(newDateStr)) {
                    costForDay = costForDay + Double.parseDouble(TextUtils.isEmpty(cTrip_ced.getTrip_Cost()) ?
                            "0" : cTrip_ced.getTrip_Cost());
                    dTotalDistance = dTotalDistance + Double.parseDouble(TextUtils.isEmpty(cTrip_ced.getTrip_Distance()) ?
                            "0" : cTrip_ced.getTrip_Distance());
                    iTripCount = iTripCount + 1;
                } else {// calculation for day is done print and start next day
                    if (!TextUtils.isEmpty(oCurrentDate)) {
                        Weekly_History_Result weekly_history_result = new Weekly_History_Result();
                        weekly_history_result.setCurrentDate(oCurrentDate);
                        weekly_history_result.setCost(costForDay);
                        weekly_history_result.setDistance(dTotalDistance);
                        weekly_history_result.setTotalTrip(iTripCount);
                        cTripArrayDalSamList.add(weekly_history_result);
                    }
                    oCurrentDate = newDateStr;
                    costForDay = Double.parseDouble(TextUtils.isEmpty(cTrip_ced.getTrip_Cost()) ? "0" : cTrip_ced.getTrip_Cost());
                    dTotalDistance = Double.parseDouble(TextUtils.isEmpty(cTrip_ced.getTrip_Distance()) ? "0" : cTrip_ced.getTrip_Distance());
                    iTripCount = 1;
                }
                cTripArrayList.add(cTrip_ced);
            }
            Weekly_History_Result weekly_history_result = new Weekly_History_Result();
            weekly_history_result.setCurrentDate(oCurrentDate);
            weekly_history_result.setCost(costForDay);
            weekly_history_result.setDistance(dTotalDistance);
            weekly_history_result.setTotalTrip(iTripCount);
            cTripArrayDalSamList.add(weekly_history_result);
            lvWeeklyTripHistory.setAdapter(new Earning_History_Adapter(DriverHistory_act.this, cTripArrayDalSamList));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickGmailLogout(View view) {
        gPlusSignOut();
    }

    private void gPlusSignOut() {
        if (google_api_client.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(google_api_client);
            google_api_client.disconnect();
            google_api_client.connect();
            changeUI();
        }
    }

    private void changeUI() {
        sign_out_button = (Button) findViewById(R.id.sign_out_button);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DriverHistory_act.this)
                .putBoolean(Constants_CED.PREF_ISLOGIN_MY_APP, false);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DriverHistory_act.this).commit();
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DriverHistory_act.this)
                .putBoolean(Constants_CED.PREF_CABE_GOOGLE_FACEBOOK_LOGIN_DONE, false);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(DriverHistory_act.this).commit();
        Intent intent = new Intent(DriverHistory_act.this, CabERegistration_act.class);
        startActivity(intent);
        finish();
    }

    private void buidNewGoogleApiClient() {
        google_api_client = new GoogleApiClient.Builder(DriverHistory_act.this)
                .addConnectionCallbacks(DriverHistory_act.this)
                .addOnConnectionFailedListener(DriverHistory_act.this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
        google_api_client.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (google_api_client.isConnected()) {
            google_api_client.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        google_api_client.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private int calculateAge(Date birthDate) {
        int years;
        int months;
        //create calendar object for birth day
        Calendar birthDay = Calendar.getInstance();
        birthDay.setTimeInMillis(birthDate.getTime());
        //create calendar object for current day
        long currentTime = System.currentTimeMillis();
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(currentTime);
        //Get difference between years
        years = now.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR);
        int currMonth = now.get(Calendar.MONTH) + 1;
        int birthMonth = birthDay.get(Calendar.MONTH) + 1;
        //Get difference between months
        months = currMonth - birthMonth;
        //if month difference is in negative then reduce years by one and calculate the number of months.
        if (months < 0) {
            years--;
            months = 12 - birthMonth + currMonth;
            if (now.get(Calendar.DATE) < birthDay.get(Calendar.DATE))
                months--;
        } else if (months == 0 && now.get(Calendar.DATE) < birthDay.get(Calendar.DATE)) {
            years--;
            months = 11;
        }
        //Calculate the days
        if (now.get(Calendar.DATE) > birthDay.get(Calendar.DATE))
            Log.d(TAG, "Calculate the days");
        else if (now.get(Calendar.DATE) < birthDay.get(Calendar.DATE)) {
            now.add(Calendar.MONTH, -1);
        } else {
            if (months == 12) {
                years++;
            }
        }
        return years;
    }
}
